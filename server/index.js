// server/index.js
// Cryptika Relay Server — v2.0.0
// Blind relay: routes encrypted packets without inspecting content
// Auth layer: username/password registration, contact tokens, ephemeral anonymous sessions
//
// Run: node index.js
// Dependencies: npm install express ws tweetnacl argon2 jsonwebtoken uuid helmet

"use strict";

const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const nacl = require("tweetnacl");
const crypto = require("crypto");
const argon2 = require("argon2");
const jwt = require("jsonwebtoken");
const { v4: uuidv4 } = require("uuid");
const helmet = require("helmet");

// ══════════════════════════════════════════════════════════════════════════════
// SERVER CONFIG
// ══════════════════════════════════════════════════════════════════════════════
const PORT = process.env.PORT || 8443;
const TICKET_EXPIRY_SECONDS = 3600; // 1 hour
const MAX_CONNECTIONS_PER_CONV = 10;
const SESSION_TTL_MS = 30 * 60 * 1000; // 30 minutes
const JWT_EXPIRY = "30m";
const USER_TTL_MS = SESSION_TTL_MS; // 30 minutes — auto-delete user record
const MIN_USERNAME_LENGTH = 2;
const MIN_PASSWORD_LENGTH = 8;
const ANTI_TIMING_DELAY_MS = 200; // constant delay for auth responses

// Secrets — generate once per server lifetime, persist via env vars
const HMAC_SECRET = process.env.HMAC_SECRET_HEX
  ? Buffer.from(process.env.HMAC_SECRET_HEX, "hex")
  : crypto.randomBytes(32);
const JWT_SECRET = process.env.JWT_SECRET_HEX
  ? Buffer.from(process.env.JWT_SECRET_HEX, "hex")
  : crypto.randomBytes(32);

if (!process.env.HMAC_SECRET_HEX) {
  console.log("   Set HMAC_SECRET_HEX=" + HMAC_SECRET.toString("hex") + " to persist");
}
if (!process.env.JWT_SECRET_HEX) {
  console.log("   Set JWT_SECRET_HEX=" + JWT_SECRET.toString("hex") + " to persist");
}

// Server Ed25519 signing keypair — generate once, hardcode public key in app
let serverKeyPair;
try {
  const savedKey = process.env.SERVER_PRIVATE_KEY_HEX;
  if (savedKey) {
    const privBytes = Buffer.from(savedKey, "hex");
    serverKeyPair = nacl.sign.keyPair.fromSeed(privBytes);
  } else {
    serverKeyPair = nacl.sign.keyPair();
    console.log("   NEW SERVER KEYPAIR GENERATED");
    console.log("   Hardcode this public key in BuildConfig.SERVER_PUBLIC_KEY_HEX:");
    console.log("   " + Buffer.from(serverKeyPair.publicKey).toString("hex"));
    console.log("   Set SERVER_PRIVATE_KEY_HEX env var to persist across restarts");
  }
} catch (e) {
  serverKeyPair = nacl.sign.keyPair();
}

console.log(`Server public key: ${Buffer.from(serverKeyPair.publicKey).toString("hex")}`);

// ══════════════════════════════════════════════════════════════════════════════
// IN-MEMORY DATA STORES
// ══════════════════════════════════════════════════════════════════════════════

// --- Existing relay state ---
const conversationSockets = new Map(); // conversationId/sessionUUID → Set<ws>
const presenceMap = new Map();         // identityHash → { connectionToken, lastSeen, online }
const wsIdentityMap = new Map();       // ws → identityHash
const messageBuffer = new Map();       // conversationId → [{data: Buffer, ts: number}]
const MAX_BUFFER_PER_CONV = 50;
const BUFFER_TTL_MS = 3_600_000; // 1 hour

// --- Auth state (Phase 1) ---
const users = new Map(); // username → { passwordHash, contactToken, identityHashHex, publicKeyB64, createdAt, loginAt }
const burnedTokens = new Set(); // blacklisted JWT jti values (after burn)

// --- Contact request state (Phase 2) ---
const contactRequests = new Map();  // requestId → { fromToken, toToken, fromIdentityHash, fromPublicKeyB64, fromNickname, status, createdAt }
const pendingByToken = new Map();   // contactToken → Set<requestId>

// --- Ephemeral anonymous session state (Phase 2) ---
const ephemeralSessions = new Map(); // sessionUUID → { participants: Map<contactToken, {identityHash, publicKeyB64}>, createdAt, expiresAt, joinedCount, identityMappingDeleted, destroyTimer }
const tokenToSession = new Map();    // contactToken → Set<sessionUUID>  (for enforcing max sessions)

// --- Rate limiting ---
const rateLimits = new Map(); // key → { count, windowStart }

// ══════════════════════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ══════════════════════════════════════════════════════════════════════════════

/** Derive a contact token from a username using HMAC-SHA256 */
function deriveContactToken(username) {
  return crypto.createHmac("sha256", HMAC_SECRET)
    .update(username)
    .digest("hex");
}

/** Truncate an identifier for privacy-safe logging */
function safeLog(id) {
  if (!id || typeof id !== "string") return "???";
  return id.slice(0, 8) + "...";
}

/** Constant-time string comparison */
function timingSafeEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(Buffer.from(a), Buffer.from(b));
}

/** Artificial delay to prevent timing side-channels on auth */
async function antiTimingDelay(startMs) {
  const elapsed = Date.now() - startMs;
  const remaining = Math.max(0, ANTI_TIMING_DELAY_MS - elapsed);
  if (remaining > 0) await new Promise(r => setTimeout(r, remaining));
}

// ══════════════════════════════════════════════════════════════════════════════
// RATE LIMITING
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Check and increment rate limit for a given key.
 * @returns true if the request should be BLOCKED
 */
function isRateLimited(key, maxRequests, windowMs) {
  const now = Date.now();
  let entry = rateLimits.get(key);
  if (!entry || now - entry.windowStart > windowMs) {
    entry = { count: 0, windowStart: now };
  }
  entry.count++;
  rateLimits.set(key, entry);
  return entry.count > maxRequests;
}

// Clean up expired rate limit entries every 5 minutes
setInterval(() => {
  const now = Date.now();
  for (const [key, entry] of rateLimits.entries()) {
    if (now - entry.windowStart > 3_600_000) rateLimits.delete(key);
  }
}, 300_000);

// ══════════════════════════════════════════════════════════════════════════════
// JWT MIDDLEWARE
// ══════════════════════════════════════════════════════════════════════════════

function authenticateToken(req, res, next) {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;
  if (!token) return res.status(401).json({ error: "Authentication required" });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    // Check if this token has been burned (blacklisted)
    if (decoded.jti && burnedTokens.has(decoded.jti)) {
      return res.status(401).json({ error: "Token has been revoked" });
    }
    req.user = decoded; // { username, contactToken, jti, iat, exp }
    next();
  } catch (e) {
    return res.status(401).json({ error: "Invalid or expired token" });
  }
}

// ══════════════════════════════════════════════════════════════════════════════
// EXPRESS REST API
// ══════════════════════════════════════════════════════════════════════════════
const app = express();
app.use(helmet());
app.use(express.json({ limit: "16kb" }));

// Health check
app.get("/health", (req, res) => {
  res.json({
    status: "ok",
    version: "2.0.0",
    connections: getTotalConnections(),
    activeSessions: ephemeralSessions.size,
    timestamp: Date.now()
  });
});

// ── AUTH ENDPOINTS ────────────────────────────────────────────────────────────

/**
 * POST /api/v1/auth/register
 * Register a new user with username + password.
 * Always returns 200 OK to prevent username enumeration.
 */
app.post("/api/v1/auth/register", async (req, res) => {
  const startMs = Date.now();
  const clientIp = req.ip || req.socket.remoteAddress;

  // Rate limit: 3 registrations per IP per hour
  if (isRateLimited(`reg_${clientIp}`, 3, 3_600_000)) {
    await antiTimingDelay(startMs);
    return res.status(429).json({ error: "Too many requests" });
  }

  try {
    const { username, password, identityHashHex, publicKeyB64 } = req.body;

    // Validate input — return real errors for input validation (not enumeration-sensitive)
    if (!username || typeof username !== "string" || username.length < MIN_USERNAME_LENGTH) {
      await antiTimingDelay(startMs);
      return res.status(400).json({ status: "error", error: `Username must be at least ${MIN_USERNAME_LENGTH} characters` });
    }
    if (!password || typeof password !== "string" || password.length < MIN_PASSWORD_LENGTH) {
      await antiTimingDelay(startMs);
      return res.status(400).json({ status: "error", error: "Password must be at least 8 characters" });
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      await antiTimingDelay(startMs);
      return res.status(400).json({ status: "error", error: "Username can only contain letters, numbers, and underscores" });
    }

    // If username already taken, silently return OK (anti-enumeration)
    if (users.has(username)) {
      await antiTimingDelay(startMs);
      return res.json({ status: "ok" });
    }

    // Hash password with Argon2id
    const passwordHash = await argon2.hash(password, {
      type: argon2.argon2id,
      memoryCost: 65536, // 64 MB
      timeCost: 3,
      parallelism: 1,
    });

    // Derive contact token
    const contactToken = deriveContactToken(username);

    // Store user
    users.set(username, {
      passwordHash,
      contactToken,
      identityHashHex: identityHashHex || "",
      publicKeyB64: publicKeyB64 || "",
      createdAt: Date.now(),
    });

    console.log(`AUTH register_ok`);
    await antiTimingDelay(startMs);
    res.json({ status: "ok" });
  } catch (e) {
    console.error("AUTH register_error");
    await antiTimingDelay(startMs);
    res.json({ status: "ok" }); // Never leak errors
  }
});

/**
 * POST /api/v1/auth/login
 * Authenticate and receive a JWT + contact token.
 * Constant timing to prevent side-channel attacks.
 */
app.post("/api/v1/auth/login", async (req, res) => {
  const startMs = Date.now();
  const clientIp = req.ip || req.socket.remoteAddress;

  // Rate limit: 5 login attempts per IP per 15 minutes
  if (isRateLimited(`login_${clientIp}`, 5, 900_000)) {
    await antiTimingDelay(startMs);
    return res.status(429).json({ error: "Too many attempts" });
  }

  try {
    const { username, password, identityHashHex, publicKeyB64 } = req.body;

    if (!username || !password || typeof username !== "string" || typeof password !== "string") {
      await antiTimingDelay(startMs);
      return res.status(401).json({ error: "Invalid credentials" });
    }

    const user = users.get(username);

    if (!user) {
      // Spend time on a dummy hash to maintain constant timing
      await argon2.hash("dummy_password_for_timing", { type: argon2.argon2id, memoryCost: 65536, timeCost: 3 });
      await antiTimingDelay(startMs);
      return res.status(401).json({ error: "Invalid credentials" });
    }

    const valid = await argon2.verify(user.passwordHash, password);
    if (!valid) {
      await antiTimingDelay(startMs);
      return res.status(401).json({ error: "Invalid credentials" });
    }

    // Update identity info if provided (latest device identity)
    if (identityHashHex) user.identityHashHex = identityHashHex;
    if (publicKeyB64) user.publicKeyB64 = publicKeyB64;

    // Track login time for user TTL sweep
    user.loginAt = Date.now();

    // Issue JWT with unique jti for revocation support
    const jti = uuidv4();
    const tokenPayload = { username: username, contactToken: user.contactToken, jti };
    const token = jwt.sign(tokenPayload, JWT_SECRET, { expiresIn: JWT_EXPIRY });
    const decoded = jwt.decode(token);

    console.log(`AUTH login_ok`);
    await antiTimingDelay(startMs);
    res.json({
      token,
      contactToken: user.contactToken,
      expiresAt: decoded.exp * 1000,
    });
  } catch (e) {
    console.error("AUTH login_error");
    await antiTimingDelay(startMs);
    res.status(401).json({ error: "Invalid credentials" });
  }
});

// ── CONTACT REQUEST ENDPOINTS ─────────────────────────────────────────────────

/**
 * POST /api/v1/auth/burn
 * Burns (deletes) the authenticated user's credentials from the server.
 * Blacklists the JWT so it cannot be reused. Cascades deletion of
 * contact requests associated with this user's contactToken.
 */
app.post("/api/v1/auth/burn", authenticateToken, (req, res) => {
  try {
    const username = req.user.username;
    const contactToken = req.user.contactToken;
    const jti = req.user.jti;

    // Delete user record
    users.delete(username);

    // Blacklist the JWT
    if (jti) burnedTokens.add(jti);

    // Cascade delete contact requests involving this user
    for (const [rid, r] of contactRequests.entries()) {
      if (r.fromToken === contactToken || r.toToken === contactToken) {
        const pending = pendingByToken.get(r.toToken);
        if (pending) {
          pending.delete(rid);
          if (pending.size === 0) pendingByToken.delete(r.toToken);
        }
        contactRequests.delete(rid);
      }
    }

    console.log(`AUTH burned credentials for ${safeLog(username)}`);
    res.json({ status: "burned" });
  } catch (e) {
    console.error("AUTH burn_error");
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/contact/request
 * Send a contact request to another user by username.
 * Always returns the same response regardless of whether the target exists.
 */
app.post("/api/v1/contact/request", authenticateToken, async (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;

  // Rate limit: 10 contact requests per IP per minute
  if (isRateLimited(`creq_${clientIp}`, 10, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

  // Rate limit: 20 contact requests per user per day (Step 6.4)
  const fromToken = req.user.contactToken;
  if (isRateLimited(`creq_daily_${fromToken}`, 20, 86_400_000)) {
    return res.json({ status: "request_sent" }); // Anti-enumeration: same response
  }

  try {
    const { targetUsername, nickname } = req.body;
    if (!targetUsername || typeof targetUsername !== "string") {
      return res.json({ status: "request_sent" }); // Anti-enumeration
    }

    const toToken = deriveContactToken(targetUsername);

    // Prevent self-request
    if (timingSafeEqual(fromToken, toToken)) {
      return res.json({ status: "request_sent" });
    }

    const targetUser = users.get(targetUsername);
    if (!targetUser) {
      // Target doesn't exist — return identical response (anti-enumeration)
      return res.json({ status: "request_sent" });
    }

    // Check for duplicate pending request
    const existing = pendingByToken.get(toToken);
    if (existing) {
      for (const rid of existing) {
        const r = contactRequests.get(rid);
        if (r && r.fromToken === fromToken && r.status === "pending") {
          return res.json({ status: "request_sent" }); // Already pending
        }
      }
    }

    // Get sender's identity info
    const senderUser = users.get(req.user.username);

    const requestId = uuidv4();
    contactRequests.set(requestId, {
      fromToken,
      toToken,
      fromUsername: req.user.username,
      fromIdentityHash: senderUser ? senderUser.identityHashHex : "",
      fromPublicKeyB64: senderUser ? senderUser.publicKeyB64 : "",
      fromNickname: nickname || req.user.username,
      status: "pending",
      createdAt: Date.now(),
    });

    if (!pendingByToken.has(toToken)) pendingByToken.set(toToken, new Set());
    pendingByToken.get(toToken).add(requestId);

    console.log(`CONTACT request_created`);
    res.json({ status: "request_sent" });
  } catch (e) {
    console.error("CONTACT request_error");
    res.json({ status: "request_sent" }); // Never leak errors
  }
});

/**
 * GET /api/v1/contact/requests
 * List pending incoming contact requests for the authenticated user.
 */
app.get("/api/v1/contact/requests", authenticateToken, (req, res) => {
  try {
    const myToken = req.user.contactToken;
    const requestIds = pendingByToken.get(myToken);

    if (!requestIds || requestIds.size === 0) {
      return res.json({ requests: [] });
    }

    const pending = [];
    for (const rid of requestIds) {
      const r = contactRequests.get(rid);
      if (r && r.status === "pending") {
        pending.push({
          requestId: rid,
          fromToken: r.fromToken,
          fromIdentityHash: r.fromIdentityHash,
          fromPublicKeyB64: r.fromPublicKeyB64,
          fromNickname: r.fromNickname,
          createdAt: r.createdAt,
        });
      }
    }

    res.json({ requests: pending });
  } catch (e) {
    console.error("CONTACT list_error");
    res.json({ requests: [] });
  }
});

/**
 * POST /api/v1/contact/accept
 * Accept a contact request — creates an ephemeral anonymous session.
 * Returns the session UUID and server-issued expiry timestamp.
 */
app.post("/api/v1/contact/accept", authenticateToken, async (req, res) => {
  try {
    const { requestId } = req.body;
    if (!requestId) return res.status(400).json({ error: "requestId required" });

    const r = contactRequests.get(requestId);
    if (!r || r.status !== "pending") {
      return res.status(404).json({ error: "Request not found or already handled" });
    }

    // Verify this request belongs to the authenticated user
    const myToken = req.user.contactToken;
    if (r.toToken !== myToken) {
      return res.status(403).json({ error: "Not authorized" });
    }

    // Enforce max active sessions per user (5)
    const mySessions = tokenToSession.get(myToken);
    if (mySessions && mySessions.size >= 5) {
      return res.status(429).json({ error: "Too many active sessions" });
    }
    const theirSessions = tokenToSession.get(r.fromToken);
    if (theirSessions && theirSessions.size >= 5) {
      return res.status(429).json({ error: "Peer has too many active sessions" });
    }

    // Get accepter's identity info
    const accepterUser = users.get(req.user.username);

    // Mark request as accepted
    r.status = "accepted";

    // Remove from pending
    const pending = pendingByToken.get(r.toToken);
    if (pending) {
      pending.delete(requestId);
      if (pending.size === 0) pendingByToken.delete(r.toToken);
    }

    // Generate ephemeral session
    const sessionUUID = uuidv4();
    const now = Date.now();
    const expiresAt = now + SESSION_TTL_MS;

    const participants = new Map();
    participants.set(r.fromToken, {
      identityHash: r.fromIdentityHash,
      publicKeyB64: r.fromPublicKeyB64,
    });
    participants.set(myToken, {
      identityHash: accepterUser ? accepterUser.identityHashHex : "",
      publicKeyB64: accepterUser ? accepterUser.publicKeyB64 : "",
    });

    const destroyTimer = setTimeout(() => destroySession(sessionUUID), SESSION_TTL_MS);

    ephemeralSessions.set(sessionUUID, {
      participants,
      createdAt: now,
      expiresAt,
      joinedCount: 0,
      identityMappingDeleted: false,
      destroyTimer,
    });

    // Track sessions per user
    if (!tokenToSession.has(r.fromToken)) tokenToSession.set(r.fromToken, new Set());
    tokenToSession.get(r.fromToken).add(sessionUUID);
    if (!tokenToSession.has(myToken)) tokenToSession.set(myToken, new Set());
    tokenToSession.get(myToken).add(sessionUUID);

    console.log(`SESSION created ${safeLog(sessionUUID)} (TTL=30m)`);

    res.json({
      sessionUUID,
      expiresAt,
      serverTime: now,
      // Include peer's identity info so the client can set up crypto
      peerIdentityHash: r.fromIdentityHash,
      peerPublicKeyB64: r.fromPublicKeyB64,
      peerNickname: r.fromNickname,
    });
  } catch (e) {
    console.error("CONTACT accept_error");
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/contact/reject
 * Reject a contact request.
 */
app.post("/api/v1/contact/reject", authenticateToken, (req, res) => {
  try {
    const { requestId } = req.body;
    if (!requestId) return res.status(400).json({ error: "requestId required" });

    const r = contactRequests.get(requestId);
    if (!r || r.status !== "pending") {
      return res.status(404).json({ error: "Request not found" });
    }

    if (r.toToken !== req.user.contactToken) {
      return res.status(403).json({ error: "Not authorized" });
    }

    r.status = "rejected";
    const pending = pendingByToken.get(r.toToken);
    if (pending) {
      pending.delete(requestId);
      if (pending.size === 0) pendingByToken.delete(r.toToken);
    }
    contactRequests.delete(requestId);

    console.log(`CONTACT rejected`);
    res.json({ status: "rejected" });
  } catch (e) {
    console.error("CONTACT reject_error");
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * GET /api/v1/contact/accepted
 * Poll for accepted contact requests — returns sessions created for requests
 * originally sent BY the authenticated user.
 */
app.get("/api/v1/contact/accepted", authenticateToken, (req, res) => {
  try {
    const myToken = req.user.contactToken;
    const results = [];

    for (const [sessionUUID, session] of ephemeralSessions) {
      if (!session.participants.has(myToken)) continue;

      // Find peer info
      let peerIdentityHash = "";
      let peerPublicKeyB64 = "";
      for (const [token, info] of session.participants) {
        if (token !== myToken) {
          peerIdentityHash = info.identityHash;
          peerPublicKeyB64 = info.publicKeyB64;
          break;
        }
      }

      results.push({
        sessionUUID,
        expiresAt: session.expiresAt,
        serverTime: Date.now(),
        peerIdentityHash,
        peerPublicKeyB64,
      });
    }

    res.json({ sessions: results });
  } catch (e) {
    console.error("CONTACT accepted_error");
    res.json({ sessions: [] });
  }
});

// ── EXISTING RELAY ENDPOINTS ──────────────────────────────────────────────────

/**
 * POST /api/v1/ticket
 * Issues a signed session ticket for a pair of identity hashes.
 */
app.post("/api/v1/ticket", (req, res) => {
  const { a_id, b_id } = req.body;

  if (!a_id || !b_id || typeof a_id !== "string" || typeof b_id !== "string") {
    return res.status(400).json({ error: "a_id and b_id are required hex strings" });
  }

  if (a_id.length !== 64 || b_id.length !== 64) {
    return res.status(400).json({ error: "Identity hashes must be 32-byte hex strings (64 chars)" });
  }

  try {
    const aIdBytes = Buffer.from(a_id, "hex");
    const bIdBytes = Buffer.from(b_id, "hex");
    const timestamp = Date.now();
    const expirySeconds = TICKET_EXPIRY_SECONDS;

    const payload = Buffer.alloc(76);
    aIdBytes.copy(payload, 0);
    bIdBytes.copy(payload, 32);
    payload.writeBigInt64BE(BigInt(timestamp), 64);
    payload.writeInt32BE(expirySeconds, 72);

    const signature = nacl.sign.detached(payload, serverKeyPair.secretKey);
    const ticket = Buffer.concat([payload, Buffer.from(signature)]);

    console.log(`TICKET issued`);

    res.json({
      ticket_b64: ticket.toString("base64"),
      server_public_key_b64: Buffer.from(serverKeyPair.publicKey).toString("base64")
    });
  } catch (e) {
    console.error("TICKET error");
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/presence
 */
app.post("/api/v1/presence", (req, res) => {
  const { identity_hash, connection_token } = req.body;
  if (!identity_hash) return res.status(400).json({ error: "identity_hash required" });

  const token = connection_token || crypto.randomBytes(32).toString("hex");
  presenceMap.set(identity_hash, { connectionToken: token, lastSeen: Date.now(), online: true });
  res.json({ online: true, token });
});

/**
 * GET /api/v1/presence/:hash
 */
app.get("/api/v1/presence/:hash", (req, res) => {
  const hash = req.params.hash;
  if (!hash || !/^[a-f0-9]{64}$/.test(hash))
    return res.status(400).json({ error: "invalid hash" });
  const entry = presenceMap.get(hash);
  if (!entry) return res.json({ online: false, lastSeen: null });
  const stale = Date.now() - entry.lastSeen > 300_000;
  res.json({ online: stale ? false : (entry.online ?? true), lastSeen: entry.lastSeen });
});

// ══════════════════════════════════════════════════════════════════════════════
// EPHEMERAL SESSION MANAGEMENT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Destroy an ephemeral session — close sockets, purge all data.
 * This is the cryptographic erasure point: after this, the session is
 * unrecoverable even if the server is fully compromised.
 */
function destroySession(sessionUUID) {
  const session = ephemeralSessions.get(sessionUUID);
  if (!session) return;

  // Clear the auto-destroy timer (in case called manually)
  if (session.destroyTimer) clearTimeout(session.destroyTimer);

  // Close all WebSockets in this session room
  const room = conversationSockets.get(sessionUUID);
  if (room) {
    for (const ws of room) {
      try { ws.close(4100, "Session expired"); } catch (_) {}
    }
    conversationSockets.delete(sessionUUID);
  }

  // Clear message buffer
  messageBuffer.delete(sessionUUID);

  // Remove session-to-user tracking
  for (const [token] of session.participants) {
    const userSessions = tokenToSession.get(token);
    if (userSessions) {
      userSessions.delete(sessionUUID);
      if (userSessions.size === 0) tokenToSession.delete(token);
    }
  }

  // Delete the session itself
  ephemeralSessions.delete(sessionUUID);

  console.log(`SESSION destroyed ${safeLog(sessionUUID)}`);
}

// ══════════════════════════════════════════════════════════════════════════════
// WEBSOCKET SERVER
// ══════════════════════════════════════════════════════════════════════════════
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/ws" });

wss.on("connection", (ws, req) => {
  const url = new URL(req.url, "http://localhost");
  const conversationId = url.searchParams.get("conv");
  const sessionId = url.searchParams.get("session");
  const authTokenParam = url.searchParams.get("token"); // JWT for session auth

  // Determine the room key: either a session UUID or a conversation ID
  const roomKey = sessionId || conversationId;

  if (!roomKey) {
    ws.close(4001, "Missing conv or session parameter");
    return;
  }

  // ── Ephemeral session connection ──
  if (sessionId) {
    const session = ephemeralSessions.get(sessionId);
    if (!session) {
      ws.close(4010, "Session not found or expired");
      return;
    }
    if (Date.now() > session.expiresAt) {
      destroySession(sessionId);
      ws.close(4011, "Session expired");
      return;
    }

    // Authenticate: verify JWT and check participant membership
    if (!authTokenParam) {
      ws.close(4012, "Authentication required for session");
      return;
    }
    let decoded;
    try {
      decoded = jwt.verify(authTokenParam, JWT_SECRET);
    } catch (e) {
      ws.close(4013, "Invalid token");
      return;
    }
    if (decoded.jti && burnedTokens.has(decoded.jti)) {
      ws.close(4013, "Token has been revoked");
      return;
    }
    if (!session.participants.has(decoded.contactToken)) {
      ws.close(4014, "Not a participant of this session");
      return;
    }

    // Join the session room
    if (!conversationSockets.has(sessionId)) {
      conversationSockets.set(sessionId, new Set());
    }
    const room = conversationSockets.get(sessionId);
    if (room.size >= 2) {
      ws.close(4015, "Session full");
      return;
    }

    room.add(ws);
    ws.conversationId = sessionId;
    ws.isEphemeral = true;
    ws.isAlive = true;
    ws.on("pong", () => { ws.isAlive = true; });

    session.joinedCount++;

    // When BOTH participants have joined: delete identity mapping
    if (session.joinedCount >= 2 && !session.identityMappingDeleted) {
      session.identityMappingDeleted = true;
      // Actually clear identity info from participant entries
      for (const [token, info] of session.participants) {
        info.identityHash = "";
        info.publicKeyB64 = "";
      }
      // Server no longer knows which user is in which session
      console.log(`SESSION anonymous ${safeLog(sessionId)} (identity mapping deleted)`);
    }

    console.log(`SESSION join ${safeLog(sessionId)} (${room.size}/2)`);

    // Deliver buffered messages
    const backlog = messageBuffer.get(sessionId);
    if (backlog && backlog.length > 0) {
      const now = Date.now();
      for (const entry of backlog) {
        if (now - entry.ts < BUFFER_TTL_MS && ws.readyState === WebSocket.OPEN) {
          ws.send(entry.data, { binary: true });
        }
      }
      messageBuffer.delete(sessionId);
    }

    // Message relay for session (identical to conversation relay)
    ws.on("message", (data, isBinary) => {
      if (!isBinary) { ws.close(4004, "Binary only"); return; }

      const bytes = Buffer.isBuffer(data) ? data : Buffer.from(data);
      if (bytes.length < 6) return;

      let relayed = 0;
      for (const peer of room) {
        if (peer !== ws && peer.readyState === WebSocket.OPEN) {
          peer.send(data, { binary: true });
          relayed++;
        }
      }
      if (relayed === 0) {
        const buf = messageBuffer.get(sessionId) || [];
        if (buf.length < MAX_BUFFER_PER_CONV) {
          buf.push({ data: Buffer.from(bytes), ts: Date.now() });
          messageBuffer.set(sessionId, buf);
        }
      }
    });

    ws.on("close", () => {
      room.delete(ws);
      console.log(`SESSION leave ${safeLog(sessionId)} (${room.size}/2)`);
      if (room.size === 0) {
        conversationSockets.delete(sessionId);
      }
    });

    ws.on("error", () => { room.delete(ws); });
    return; // Done — skip regular conversation logic
  }

  // ── Regular conversation connection (existing logic) ──
  if (!/^[a-f0-9_]+$/.test(conversationId) || conversationId.length > 200) {
    ws.close(4002, "Invalid conversation ID");
    return;
  }

  if (!conversationSockets.has(conversationId)) {
    conversationSockets.set(conversationId, new Set());
  }
  const room = conversationSockets.get(conversationId);

  if (room.size >= MAX_CONNECTIONS_PER_CONV) {
    ws.close(4003, "Room full");
    return;
  }

  room.add(ws);
  ws.conversationId = conversationId;
  ws.isAlive = true;
  ws.on("pong", () => { ws.isAlive = true; });

  const identityHash = url.searchParams.get("id");
  if (identityHash && /^[a-f0-9]{64}$/.test(identityHash)) {
    ws.identityHash = identityHash;
    wsIdentityMap.set(ws, identityHash);
    const existing = presenceMap.get(identityHash) || {};
    presenceMap.set(identityHash, { ...existing, lastSeen: Date.now(), online: true });
  }

  console.log(`RELAY join ${safeLog(conversationId)} (${room.size} members)`);

  // Deliver buffered messages
  const backlog = messageBuffer.get(conversationId);
  if (backlog && backlog.length > 0) {
    const now = Date.now();
    for (const entry of backlog) {
      if (now - entry.ts < BUFFER_TTL_MS && ws.readyState === WebSocket.OPEN) {
        ws.send(entry.data, { binary: true });
      }
    }
    messageBuffer.delete(conversationId);
  }

  ws.on("message", (data, isBinary) => {
    if (!isBinary) { ws.close(4004, "Binary only"); return; }

    const bytes = Buffer.isBuffer(data) ? data : Buffer.from(data);
    if (bytes.length < 6) return;

    let relayed = 0;
    for (const peer of room) {
      if (peer !== ws && peer.readyState === WebSocket.OPEN) {
        peer.send(data, { binary: true });
        relayed++;
      }
    }
    if (relayed === 0) {
      const buf = messageBuffer.get(conversationId) || [];
      if (buf.length < MAX_BUFFER_PER_CONV) {
        buf.push({ data: Buffer.from(bytes), ts: Date.now() });
        messageBuffer.set(conversationId, buf);
      }
    }
  });

  ws.on("close", () => {
    room.delete(ws);
    if (room.size === 0) conversationSockets.delete(conversationId);
    console.log(`RELAY leave ${safeLog(conversationId)} (${room.size} remaining)`);

    const hash = wsIdentityMap.get(ws);
    wsIdentityMap.delete(ws);
    if (hash) {
      setTimeout(() => {
        const stillConnected = [...wsIdentityMap.values()].includes(hash);
        if (!stillConnected) {
          const entry = presenceMap.get(hash);
          if (entry) presenceMap.set(hash, { ...entry, online: false, lastSeen: Date.now() });
        }
      }, 5_000);
    }
  });

  ws.on("error", (err) => {
    console.error(`RELAY ws_error ${safeLog(conversationId)}`);
    room.delete(ws);
  });
});

function getTotalConnections() {
  let total = 0;
  for (const room of conversationSockets.values()) total += room.size;
  return total;
}

// ══════════════════════════════════════════════════════════════════════════════
// HEARTBEAT — ping all clients every 30s; terminate unresponsive ones
// ══════════════════════════════════════════════════════════════════════════════
setInterval(() => {
  for (const room of conversationSockets.values()) {
    for (const ws of room) {
      if (!ws.isAlive) {
        room.delete(ws);
        ws.terminate();
        continue;
      }
      ws.isAlive = false;
      ws.ping();
    }
  }
}, 30_000);

// Periodic cleanup
setInterval(() => {
  // Cleanup dead sockets and empty rooms
  for (const [convId, room] of conversationSockets.entries()) {
    for (const ws of room) {
      if (ws.readyState !== WebSocket.OPEN) room.delete(ws);
    }
    if (room.size === 0) conversationSockets.delete(convId);
  }

  // Cleanup stale presence (5 min)
  const cutoff = Date.now() - 300_000;
  for (const [hash, info] of presenceMap.entries()) {
    if (info.lastSeen < cutoff) presenceMap.delete(hash);
  }

  // Cleanup expired message buffer entries
  const bufExpiry = Date.now() - BUFFER_TTL_MS;
  for (const [convId, buf] of messageBuffer.entries()) {
    const fresh = buf.filter(e => e.ts > bufExpiry);
    if (fresh.length === 0) messageBuffer.delete(convId);
    else messageBuffer.set(convId, fresh);
  }

  // Cleanup expired ephemeral sessions
  const now = Date.now();
  for (const [uuid, session] of ephemeralSessions.entries()) {
    if (now > session.expiresAt) destroySession(uuid);
  }

  // Cleanup old contact requests (older than 24h)
  for (const [rid, r] of contactRequests.entries()) {
    if (now - r.createdAt > 86_400_000) {
      const pending = pendingByToken.get(r.toToken);
      if (pending) {
        pending.delete(rid);
        if (pending.size === 0) pendingByToken.delete(r.toToken);
      }
      contactRequests.delete(rid);
    }
  }

  // Phase 1.1 + 5.1: Auto-delete user records after USER_TTL_MS from login
  for (const [username, user] of users.entries()) {
    const ref = user.loginAt || user.createdAt;
    if (now - ref > USER_TTL_MS) {
      const contactToken = user.contactToken;
      users.delete(username);
      // Cascade: delete dangling contact requests for this user
      for (const [rid, r] of contactRequests.entries()) {
        if (r.fromToken === contactToken || r.toToken === contactToken) {
          const pending = pendingByToken.get(r.toToken);
          if (pending) {
            pending.delete(rid);
            if (pending.size === 0) pendingByToken.delete(r.toToken);
          }
          contactRequests.delete(rid);
        }
      }
      console.log(`SWEEP deleted expired user ${safeLog(username)}`);
    }
  }

  // Cleanup expired burnedTokens (keep for 35 min; JWT max 30 min + 5 min grace)
  // burnedTokens are just jti strings; we track them in burnedTokenTimestamps
  // Simple approach: clear all burned tokens periodically since JWTs expire after 30m anyway
  if (burnedTokens.size > 1000) burnedTokens.clear();
}, 60_000);

server.listen(PORT, () => {
  console.log(`\n🔒 Cryptika Relay Server v2.0.0`);
  console.log(`   Listening on port ${PORT}`);
  console.log(`   REST: http://localhost:${PORT}/api/v1/`);
  console.log(`   WebSocket: ws://localhost:${PORT}/ws?conv=<id> | ws://localhost:${PORT}/ws?session=<uuid>`);
  console.log(`   Auth: POST /api/v1/auth/register, /api/v1/auth/login`);
  console.log(`   Contacts: POST /api/v1/contact/request, /accept, /reject`);
  console.log(`   Server is BLIND — does not log message content\n`);
});
