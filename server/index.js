// server/index.js
// Cryptika Relay Server v3.0.0
// Blind relay: routes encrypted packets without inspecting content
// Auth layer: username-only entry, contact tokens, ephemeral anonymous sessions
// No passwords, no logs, no stored data: pure ephemeral blind relay
//
// Run: node index.js
// Dependencies: npm install express ws tweetnacl jsonwebtoken uuid helmet

"use strict";

const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const nacl = require("tweetnacl");
const crypto = require("crypto");
const jwt = require("jsonwebtoken");
const { v4: uuidv4 } = require("uuid");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

//
// SERVER CONFIG
//
const PORT = process.env.PORT || 8443;
const TICKET_EXPIRY_SECONDS = 3600; // 1 hour
const MAX_CONNECTIONS_PER_CONV = 10;
const SESSION_TTL_MS = 30 * 60 * 1000; // 30 minutes
const JWT_EXPIRY = "30m";
const USER_TTL_MS = SESSION_TTL_MS; // 30 minutes: auto-delete user record
const MIN_USERNAME_LENGTH = 1;

// Secrets: generate once per server lifetime, persist via env vars
if (process.env.NODE_ENV === "production" && (!process.env.HMAC_SECRET_HEX || !process.env.JWT_SECRET_HEX)) {
  console.error("FATAL: HMAC_SECRET_HEX and JWT_SECRET_HEX environment variables must be set in production mode");
  process.exit(1);
}

const HMAC_SECRET = process.env.HMAC_SECRET_HEX
  ? Buffer.from(process.env.HMAC_SECRET_HEX, "hex")
  : crypto.randomBytes(32);
const JWT_SECRET = process.env.JWT_SECRET_HEX
  ? Buffer.from(process.env.JWT_SECRET_HEX, "hex")
  : crypto.randomBytes(32);

if (!process.env.HMAC_SECRET_HEX) {
  console.warn("WARNING: HMAC_SECRET_HEX is not set; generating ephemeral random HMAC secret.");
}
if (!process.env.JWT_SECRET_HEX) {
  console.warn("WARNING: JWT_SECRET_HEX is not set; generating ephemeral random JWT secret.");
}

// Server Ed25519 signing keypair: generate once, hardcode public key in app
let serverKeyPair;
try {
  const savedKey = process.env.SERVER_PRIVATE_KEY_HEX;
  if (savedKey) {
    const privBytes = Buffer.from(savedKey, "hex");
    if (privBytes.length === 64) {
      serverKeyPair = nacl.sign.keyPair.fromSecretKey(privBytes);
    } else if (privBytes.length === 32) {
      serverKeyPair = nacl.sign.keyPair.fromSeed(privBytes);
    } else {
      throw new Error("Invalid key length: must be 32 or 64 bytes");
    }
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

//
// IN-MEMORY DATA STORES
//

// --- Existing relay state ---
const conversationSockets = new Map(); // conversationId/sessionUUID â†’ Set<ws>
const presenceMap = new Map();         // identityHash â†’ { connectionToken, lastSeen, online }
const wsIdentityMap = new Map();       // ws â†’ identityHash
const messageBuffer = new Map();       // conversationId â†’ [{data: Buffer, ts: number}]
const MAX_BUFFER_PER_CONV = 50;
const BUFFER_TTL_MS = 3_600_000; // 1 hour

// --- Auth state (Phase 1) ---
const users = new Map(); // username â†’ { contactToken, identityHashHex, publicKeyB64, createdAt }
const burnedTokens = new Map(); // jti → burnedAtMs: tracks revoked JWTs until their natural expiry

// --- Contact request state (Phase 2) ---
const contactRequests = new Map();  // requestId â†’ { fromToken, toToken, fromIdentityHash, fromPublicKeyB64, fromNickname, status, createdAt }
const pendingByToken = new Map();   // contactToken â†’ Set<requestId>

// --- Ephemeral anonymous session state (Phase 2) ---
const ephemeralSessions = new Map(); // sessionUUID â†’ { participants: Map<contactToken, {identityHash, publicKeyB64}>, createdAt, expiresAt, joinedCount, identityMappingDeleted, destroyTimer }
const tokenToSession = new Map();    // contactToken â†’ Set<sessionUUID>  (for enforcing max sessions)

// --- Rate limiting ---
const rateLimits = new Map(); // key â†’ { count, windowStart }

//
// UTILITY FUNCTIONS
//

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

/**
 * Verify detached Ed25519 signature proof of possession for auth enter.
 * Signs SHA-256("Cryptika-Auth:" + username + ":" + timestamp_ms)
 */
function verifyAuthProof(pubKeyBytes, username, timestamp_ms, signatureB64) {
  if (!signatureB64 || typeof signatureB64 !== "string") {
    return false;
  }
  if (timestamp_ms === undefined || timestamp_ms === null) {
    return false;
  }
  let ts = timestamp_ms;
  if (typeof ts === "string" && /^\d+$/.test(ts)) {
    ts = Number(ts);
  }
  if (typeof ts !== "number" || !Number.isInteger(ts)) {
    return false;
  }
  const now = Date.now();
  if (Math.abs(now - ts) > 300_000) {
    return false;
  }
  if (!pubKeyBytes || pubKeyBytes.length !== 32) {
    return false;
  }
  let sigBytes;
  try {
    sigBytes = Buffer.from(signatureB64, "base64");
  } catch (_) {
    return false;
  }
  if (sigBytes.length !== 64) {
    return false;
  }

  const authPayload = "Cryptika-Auth:" + username + ":" + timestamp_ms;
  const hash = crypto.createHash("sha256").update(authPayload).digest();
  if (nacl.sign.detached.verify(hash, sigBytes, pubKeyBytes)) {
    return true;
  }

  if (typeof username === "string" && username.trim() !== username) {
    const trimmedPayload = "Cryptika-Auth:" + username.trim() + ":" + timestamp_ms;
    const trimmedHash = crypto.createHash("sha256").update(trimmedPayload).digest();
    if (nacl.sign.detached.verify(trimmedHash, sigBytes, pubKeyBytes)) {
      return true;
    }
  }

  return false;
}

/** Artificial delay to prevent timing side-channels on auth: no longer needed (passwordless) */

//
// RATE LIMITING
//

/**
 * Check and increment rate limit for a given key.
 * @returns true if the request should be BLOCKED
 */
function isRateLimited(key, maxRequests, windowMs) {
  const now = Date.now();
  let entry = rateLimits.get(key);
  if (!entry || now - entry.windowStart > windowMs) {
    entry = { count: 0, windowStart: now, windowMs };
  }
  entry.count++;
  rateLimits.set(key, entry);
  return entry.count > maxRequests;
}

// Clean up expired rate limit entries every 5 minutes
setInterval(() => {
  const now = Date.now();
  for (const [key, entry] of rateLimits.entries()) {
    const limitWindow = entry.windowMs || 3_600_000;
    if (now - entry.windowStart > limitWindow) rateLimits.delete(key);
  }
}, 300_000);

//
// JWT MIDDLEWARE
//

function authenticateToken(req, res, next) {
  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;
  if (!token) return res.status(401).json({ error: "Authentication required" });

  try {
    const decoded = jwt.verify(token, JWT_SECRET, { algorithms: ["HS256"] });
    // Check if this token has been burned (blacklisted)
    if (decoded.jti && burnedTokens.has(decoded.jti)) {
      return res.status(401).json({ error: "Token has been revoked" });
    }
    req.user = decoded; // { username, contactToken, jti, iat, exp }
    const u = users.get(decoded.username);
    if (!u) return res.status(401).json({ error: "User account no longer exists or has been burned" });
    u.lastActive = Date.now();
    next();
  } catch (e) {
    return res.status(401).json({ error: "Invalid or expired token" });
  }
}

function extractBearerToken(headers) {
  const authHeader = headers?.authorization;
  return authHeader && authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;
}

function findUserByIdentityHash(identityHashHex) {
  for (const [username, user] of users.entries()) {
    if (user?.identityHashHex === identityHashHex) {
      return { username, user };
    }
  }
  return null;
}

//
// EXPRESS REST API
//
const app = express();
app.use(helmet());
app.use(express.json({ limit: "16kb" }));

// Rate limiters using express-rate-limit
const globalLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 120,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many requests" },
});

const authLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many requests" },
});

const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 60,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many requests" },
});

// Attach global limiter to /api/v1 router
app.use("/api/v1", globalLimiter);

// Health check
const healthCheckHandler = (req, res) => {
  res.json({
    status: "ok",
    version: "2.0.0",
    connections: getTotalConnections(),
    activeSessions: ephemeralSessions.size,
    timestamp: Date.now()
  });
};
app.get("/health", healthCheckHandler);
app.get("/api/v1/health", healthCheckHandler);

// AUTH ENDPOINTS

/**
 * POST /api/v1/auth/enter
 * Passwordless entry: provide a public username to get a JWT + contact token.
 * If username is already taken, the existing user is replaced (ephemeral model).
 * Case-sensitive usernames. No passwords stored anywhere.
 */
app.post("/api/v1/auth/enter", authLimiter, (req, res) => {
  try {
    const { username, identityHashHex, publicKeyB64, signatureB64, timestamp_ms } = req.body;

    if (!username || typeof username !== "string" || username.trim().length < MIN_USERNAME_LENGTH) {
      return res.status(400).json({ error: `Username must be at least ${MIN_USERNAME_LENGTH} character` });
    }

    const trimmed = username.trim();
    if (trimmed.length > 64) {
      return res.status(400).json({ error: "Username cannot exceed 64 characters" });
    }

    // Require cryptographic identity and proof of possession unconditionally
    if (!identityHashHex || !publicKeyB64 || !signatureB64 || timestamp_ms === undefined || timestamp_ms === null) {
      return res.status(400).json({
        error: "identityHashHex, publicKeyB64, signatureB64, and timestamp_ms are required"
      });
    }

    if (typeof identityHashHex !== "string" || !/^[0-9a-fA-F]{64}$/.test(identityHashHex)) {
      return res.status(400).json({ error: "Invalid identityHashHex format" });
    }

    if (typeof publicKeyB64 !== "string") {
      return res.status(400).json({ error: "Invalid publicKeyB64 format" });
    }

    let pubBytes;
    try {
      pubBytes = Buffer.from(publicKeyB64, "base64");
    } catch (_) {
      return res.status(400).json({ error: "Invalid base64 in publicKeyB64" });
    }
    if (pubBytes.length !== 32) {
      return res.status(400).json({ error: "Invalid publicKeyB64 length (must be 32 bytes)" });
    }

    const computedHash = crypto.createHash("sha256").update(pubBytes).digest("hex");
    if (!timingSafeEqual(identityHashHex.toLowerCase(), computedHash.toLowerCase())) {
      return res.status(400).json({ error: "identityHashHex does not match publicKeyB64" });
    }

    // Unconditionally verify Ed25519 signature proof over Cryptika-Auth:<username>:<timestamp>
    if (!verifyAuthProof(pubBytes, trimmed, timestamp_ms, signatureB64)) {
      return res.status(401).json({
        error: "Ed25519 signature required to verify identity ownership"
      });
    }

    // Check if username is already taken by an active user
    const existingUser = users.get(trimmed);
    const isExistingActive = existingUser && (Date.now() - (existingUser.lastActive || existingUser.createdAt) <= USER_TTL_MS);

    if (isExistingActive) {
      if (existingUser.identityHashHex && !timingSafeEqual(existingUser.identityHashHex.toLowerCase(), identityHashHex.toLowerCase())) {
        return res.status(409).json({
          error: "Username is currently in use by another user. Please try a different username."
        });
      }
    }

    // Derive contact token (case-sensitive: different case = different token)
    const contactToken = deriveContactToken(trimmed);

    // Store/replace user (purely ephemeral, no password)
    users.set(trimmed, {
      contactToken,
      identityHashHex,
      publicKeyB64,
      createdAt: existingUser?.createdAt || Date.now(),
      lastActive: Date.now(),
    });

    // Issue JWT
    const jti = uuidv4();
    const tokenPayload = { username: trimmed, contactToken, jti };
    const token = jwt.sign(tokenPayload, JWT_SECRET, { expiresIn: JWT_EXPIRY });
    const decoded = jwt.decode(token);

    res.json({
      token,
      contactToken,
      expiresAt: decoded.exp * 1000,
    });
  } catch (e) {
    res.status(500).json({ error: "Internal error" });
  }
});

// CONTACT REQUEST ENDPOINTS

/**
 * POST /api/v1/auth/burn
 * Burns (deletes) the authenticated user's credentials from the server.
 * Blacklists the JWT so it cannot be reused. Cascades deletion of
 * contact requests associated with this user's contactToken.
 */
app.post("/api/v1/auth/burn", authLimiter, authenticateToken, (req, res) => {
  try {
    const username = req.user.username;
    const contactToken = req.user.contactToken;
    const jti = req.user.jti;
    const existingUser = users.get(username);
    const userHash = existingUser?.identityHashHex;

    // Delete user record so handle is burned from server memory
    users.delete(username);

    // Unconditionally blacklist JWT and disconnect sockets
    if (jti) burnedTokens.set(jti, Date.now());

    // Disconnect active WebSockets belonging to this user
    for (const [convId, room] of conversationSockets.entries()) {
      for (const ws of room) {
        if (ws.contactToken === contactToken || (userHash && ws.identityHash === userHash)) {
          try { ws.close(4001, "Account burned"); } catch (_) {}
          room.delete(ws);
          wsIdentityMap.delete(ws);
        }
      }
      if (room.size === 0) conversationSockets.delete(convId);
    }
    if (userHash) presenceMap.delete(userHash);

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

    res.json({ status: "burned" });
  } catch (e) {
    /* blind: no logging */
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/contact/request
 * Send a contact request to another user by username.
 * Always returns the same response regardless of whether the target exists.
 */
app.post("/api/v1/contact/request", apiLimiter, authenticateToken, async (req, res) => {
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
    if (!targetUsername || typeof targetUsername !== "string" || !targetUsername.trim()) {
      return res.json({ status: "request_sent" }); // Anti-enumeration
    }

    const trimmedTarget = targetUsername.trim();
    const toToken = deriveContactToken(trimmedTarget);

    // Prevent self-request
    if (timingSafeEqual(fromToken, toToken)) {
      return res.json({ status: "request_sent" });
    }

    // Note: We intentionally store requests even if targetUser doesn't exist yet.
    // This allows users to receive pending requests when they register later.
    // The request is indexed by toToken (derived from username), so it will be
    // available when the target user registers and fetches their pending requests.

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
    const fromIdentityHash = req.body.identityHashHex || (senderUser ? senderUser.identityHashHex : "");
    const fromPublicKeyB64 = req.body.publicKeyB64 || (senderUser ? senderUser.publicKeyB64 : "");

    const requestId = uuidv4();
    contactRequests.set(requestId, {
      fromToken,
      toToken,
      fromUsername: req.user.username,
      fromIdentityHash,
      fromPublicKeyB64,
      fromNickname: nickname || req.user.username,
      status: "pending",
      createdAt: Date.now(),
    });

    if (!pendingByToken.has(toToken)) pendingByToken.set(toToken, new Set());
    pendingByToken.get(toToken).add(requestId);

    /* blind: no logging */
    res.json({ status: "request_sent" });
  } catch (e) {
    /* blind: no logging */
    res.json({ status: "request_sent" }); // Never leak errors
  }
});

/**
 * POST /api/v1/contact/request-by-fingerprint
 * Send a contact request by peer identity fingerprint (identity hash hex).
 * Returns anti-enumeration response semantics identical to /contact/request.
 */
app.post("/api/v1/contact/request-by-fingerprint", apiLimiter, authenticateToken, async (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;

  // Same limits as username-based contact requests
  if (isRateLimited(`creq_${clientIp}`, 10, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

  const fromToken = req.user.contactToken;
  if (isRateLimited(`creq_daily_${fromToken}`, 20, 86_400_000)) {
    return res.json({ status: "request_sent" });
  }

  try {
    const { targetIdentityHash, nickname } = req.body;
    if (!targetIdentityHash || typeof targetIdentityHash !== "string" || !/^[a-f0-9]{64}$/.test(targetIdentityHash)) {
      return res.json({ status: "request_sent" });
    }

    const senderUser = users.get(req.user.username);
    if (senderUser?.identityHashHex && timingSafeEqual(senderUser.identityHashHex, targetIdentityHash)) {
      return res.json({ status: "request_sent" });
    }

    const found = findUserByIdentityHash(targetIdentityHash);
    if (!found) {
      return res.json({ status: "request_sent" });
    }

    const toToken = found.user.contactToken;
    if (!toToken || timingSafeEqual(fromToken, toToken)) {
      return res.json({ status: "request_sent" });
    }

    const existing = pendingByToken.get(toToken);
    if (existing) {
      for (const rid of existing) {
        const r = contactRequests.get(rid);
        if (r && r.fromToken === fromToken && r.status === "pending") {
          return res.json({ status: "request_sent" });
        }
      }
    }

    const fromIdentityHash = req.body.identityHashHex || (senderUser ? senderUser.identityHashHex : "");
    const fromPublicKeyB64 = req.body.publicKeyB64 || (senderUser ? senderUser.publicKeyB64 : "");

    const requestId = uuidv4();
    contactRequests.set(requestId, {
      fromToken,
      toToken,
      fromUsername: req.user.username,
      fromIdentityHash,
      fromPublicKeyB64,
      fromNickname: nickname || req.user.username,
      status: "pending",
      createdAt: Date.now(),
    });

    if (!pendingByToken.has(toToken)) pendingByToken.set(toToken, new Set());
    pendingByToken.get(toToken).add(requestId);

    res.json({ status: "request_sent" });
  } catch (e) {
    res.json({ status: "request_sent" });
  }
});

/**
 * GET /api/v1/contact/requests
 * List pending incoming contact requests for the authenticated user.
 */
app.get("/api/v1/contact/requests", apiLimiter, authenticateToken, (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`creq_get_${clientIp}`, 60, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

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
    /* blind: no logging */
    res.json({ requests: [] });
  }
});

/**
 * POST /api/v1/contact/accept
 * Accept a contact request: creates an ephemeral anonymous session.
 * Returns the session UUID and server-issued expiry timestamp.
 */
app.post("/api/v1/contact/accept", apiLimiter, authenticateToken, async (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`accept_${clientIp}`, 20, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

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
      nickname: r.fromNickname,
    });
    const accepterIdentityHash = req.body.identityHashHex || (accepterUser ? accepterUser.identityHashHex : "");
    const accepterPublicKeyB64 = req.body.publicKeyB64 || (accepterUser ? accepterUser.publicKeyB64 : "");

    participants.set(myToken, {
      identityHash: accepterIdentityHash,
      publicKeyB64: accepterPublicKeyB64,
      nickname: req.user.username,
    });

    const destroyTimer = setTimeout(() => destroySession(sessionUUID), SESSION_TTL_MS);

    ephemeralSessions.set(sessionUUID, {
      participants,
      createdAt: now,
      expiresAt,
      joinedTokens: new Set(),
      identityMappingDeleted: false,
      destroyTimer,
    });

    // Track sessions per user
    if (!tokenToSession.has(r.fromToken)) tokenToSession.set(r.fromToken, new Set());
    tokenToSession.get(r.fromToken).add(sessionUUID);
    if (!tokenToSession.has(myToken)) tokenToSession.set(myToken, new Set());
    tokenToSession.get(myToken).add(sessionUUID);

    // Clean up accepted request
    contactRequests.delete(requestId);

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
    /* blind: no logging */
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/contact/reject
 * Reject a contact request.
 */
app.post("/api/v1/contact/reject", apiLimiter, authenticateToken, (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`reject_${clientIp}`, 20, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

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

    /* blind: no logging */
    res.json({ status: "rejected" });
  } catch (e) {
    /* blind: no logging */
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * GET /api/v1/contact/accepted
 * Poll for accepted contact requests: returns sessions created for requests
 * originally sent BY the authenticated user.
 */
app.get("/api/v1/contact/accepted", apiLimiter, authenticateToken, (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`accepted_get_${clientIp}`, 60, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

  try {
    const myToken = req.user.contactToken;
    const results = [];

    for (const [sessionUUID, session] of ephemeralSessions) {
      if (!session.participants.has(myToken)) continue;

      // Find peer info
      let peerIdentityHash = "";
      let peerPublicKeyB64 = "";
      let peerNickname = "";
      for (const [token, info] of session.participants) {
        if (token !== myToken) {
          peerIdentityHash = info.identityHash;
          peerPublicKeyB64 = info.publicKeyB64;
          peerNickname = info.nickname || "";
          break;
        }
      }

      results.push({
        sessionUUID,
        expiresAt: session.expiresAt,
        serverTime: Date.now(),
        peerIdentityHash,
        peerPublicKeyB64,
        peerNickname,
      });
    }

    res.json({ sessions: results });
  } catch (e) {
    /* blind: no logging */
    res.json({ sessions: [] });
  }
});

// EXISTING RELAY ENDPOINTS

/**
 * POST /api/v1/ticket
 * Dual-signature ticket: User A signs the payload, server verifies and countersigns.
 *
 * Only the initiator (caller whose identity hash == a_id) may call this.
 * Server verifies User A's Ed25519 signature before issuing a countersignature,
 * so the server acts as a notary that independently confirms authorship.
 *
 * Request body: { a_id, b_id, timestamp_ms, expiry_seconds, user_a_sig_b64 }
 * Response:     { ticket_b64 }  — 204-byte ticket (payload + userASig + serverSig)
 */
app.post("/api/v1/ticket", apiLimiter, authenticateToken, (req, res) => {
  const { a_id, b_id, timestamp_ms, expiry_seconds, user_a_sig_b64 } = req.body;

  // Strict type checks — prevent JSON type-confusion attacks
  if (!a_id || !b_id || !user_a_sig_b64) {
    return res.status(400).json({ error: "Missing required fields" });
  }
  if (typeof a_id !== "string" || typeof b_id !== "string" || typeof user_a_sig_b64 !== "string") {
    return res.status(400).json({ error: "a_id, b_id, user_a_sig_b64 must be strings" });
  }
  if (typeof timestamp_ms !== "number" || !Number.isInteger(timestamp_ms)) {
    return res.status(400).json({ error: "timestamp_ms must be an integer" });
  }
  if (typeof expiry_seconds !== "number" || !Number.isInteger(expiry_seconds)) {
    return res.status(400).json({ error: "expiry_seconds must be an integer" });
  }

  // Enforce strict lowercase hex — Buffer.from(x,"hex") silently drops invalid chars otherwise
  const hexPattern = /^[a-f0-9]{64}$/;
  if (!hexPattern.test(a_id) || !hexPattern.test(b_id)) {
    return res.status(400).json({ error: "Identity hashes must be 64-char lowercase hex" });
  }

  // Participants must be distinct
  if (a_id === b_id) {
    return res.status(400).json({ error: "a_id and b_id must differ" });
  }

  try {
    const caller = users.get(req.user.username);
    if (!caller || !caller.identityHashHex || !caller.publicKeyB64) {
      return res.status(403).json({ error: "Caller identity not registered" });
    }

    // Only the initiator (caller) may request a ticket.
    // Verifying that a_id matches caller's registered identity hash
    if (!timingSafeEqual(caller.identityHashHex.toLowerCase(), a_id.toLowerCase())) {
      return res.status(403).json({ error: "Only the initiator (a_id) may request a ticket" });
    }

    // Timestamp freshness — prevents pre-generated and replayed ticket requests
    const now = Date.now();
    const CLOCK_SKEW_MS = 300_000; // 5 minutes
    if (Math.abs(now - timestamp_ms) > CLOCK_SKEW_MS) {
      return res.status(400).json({ error: "Timestamp outside acceptable range" });
    }

    // Cap expiry to server maximum
    if (expiry_seconds <= 0 || expiry_seconds > TICKET_EXPIRY_SECONDS) {
      return res.status(400).json({ error: "Invalid expiry_seconds" });
    }

    // Build the 76-byte payload using the verified caller identity
    const aIdBytes = Buffer.from(caller.identityHashHex, "hex");
    const bIdBytes = Buffer.from(b_id, "hex");
    const payload = Buffer.alloc(76);
    aIdBytes.copy(payload, 0);
    bIdBytes.copy(payload, 32);
    payload.writeBigInt64BE(BigInt(timestamp_ms), 64);
    payload.writeInt32BE(expiry_seconds, 72);

    // Decode and length-check user_a_sig before verifying
    const userASig = Buffer.from(user_a_sig_b64, "base64");
    if (userASig.length !== 64) {
      return res.status(400).json({ error: "user_a_sig_b64 must encode exactly 64 bytes" });
    }

    // Verify User A's Ed25519 signature over SHA-256(payload)
    const callerPubKeyBytes = Buffer.from(caller.publicKeyB64, "base64");
    if (callerPubKeyBytes.length !== 32) {
      return res.status(500).json({ error: "Invalid registered public key length" });
    }
    const payloadHash = crypto.createHash("sha256").update(payload).digest();
    if (!nacl.sign.detached.verify(payloadHash, userASig, callerPubKeyBytes)) {
      return res.status(403).json({ error: "User A signature verification failed" });
    }

    // Server countersigns SHA-256(payload || user_a_sig)
    const combined = Buffer.concat([payload, userASig]); // 140 bytes
    const combinedHash = crypto.createHash("sha256").update(combined).digest();
    const serverSig = nacl.sign.detached(combinedHash, serverKeyPair.secretKey);

    // Full 204-byte ticket: payload(76) + userASig(64) + serverSig(64)
    const ticket = Buffer.concat([payload, userASig, Buffer.from(serverSig)]);

    res.json({ ticket_b64: ticket.toString("base64") });
  } catch (e) {
    /* blind: no logging */
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/presence
 */
app.post("/api/v1/presence", apiLimiter, authenticateToken, (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`presence_${clientIp}`, 60, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

  const { identity_hash, connection_token } = req.body;
  if (!identity_hash) return res.status(400).json({ error: "identity_hash required" });

  // Verify that the identity hash belongs to the authenticated caller
  const callerIdentityHash = users.get(req.user.username)?.identityHashHex;
  if (callerIdentityHash && identity_hash !== callerIdentityHash) {
    return res.status(403).json({ error: "Identity hash does not match authenticated user" });
  }

  const token = connection_token || crypto.randomBytes(32).toString("hex");
  presenceMap.set(identity_hash, { connectionToken: token, lastSeen: Date.now(), online: true });
  res.json({ online: true, token });
});

/**
 * GET /api/v1/presence/:hash
 */
app.get("/api/v1/presence/:hash", apiLimiter, (req, res) => {
  const clientIp = req.ip || req.socket.remoteAddress;
  if (isRateLimited(`get_presence_${clientIp}`, 120, 60_000)) {
    return res.status(429).json({ error: "Too many requests" });
  }

  const hash = req.params.hash;
  if (!hash || !/^[a-f0-9]{64}$/.test(hash))
    return res.status(400).json({ error: "invalid hash" });
  const entry = presenceMap.get(hash);
  if (!entry) return res.json({ online: false, lastSeen: null });
  const stale = Date.now() - entry.lastSeen > 300_000;
  res.json({ online: stale ? false : (entry.online ?? true), lastSeen: entry.lastSeen });
});

//
// EPHEMERAL SESSION MANAGEMENT
//

/**
 * Destroy an ephemeral session: close sockets, purge all data.
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
}

//
// WEBSOCKET SERVER
//
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/ws", maxPayload: 65536 });

function handleEphemeralSessionSocket(ws, sessionId, decoded) {
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

  if (!session.participants.has(decoded.contactToken)) {
    ws.close(4014, "Not a participant of this session");
    return;
  }

  // Join the session room
  if (!conversationSockets.has(sessionId)) {
    conversationSockets.set(sessionId, new Set());
  }
  const room = conversationSockets.get(sessionId);
  // Replace any stale connection from the same participant (e.g. rapid reconnect or network switch)
  for (const oldWs of room) {
    if (oldWs.contactToken === decoded.contactToken) {
      try { oldWs.close(4000, "Replaced by new connection"); } catch (_) {}
      room.delete(oldWs);
      wsIdentityMap.delete(oldWs);
    }
  }
  if (room.size >= 2) {
    ws.close(4015, "Session full");
    return;
  }

  room.add(ws);
  ws.conversationId = sessionId;
  ws.contactToken = decoded.contactToken;
  ws.isEphemeral = true;
  ws.isAlive = true;
  ws.on("pong", () => { ws.isAlive = true; });

  session.joinedTokens = session.joinedTokens || new Set();
  session.joinedTokens.add(decoded.contactToken);

  // Deliver buffered messages intended for this participant
  const backlog = messageBuffer.get(sessionId);
  if (backlog && backlog.length > 0) {
    const now = Date.now();
    const remaining = [];
    for (const entry of backlog) {
      if (entry.senderToken !== decoded.contactToken && now - entry.ts < BUFFER_TTL_MS && ws.readyState === WebSocket.OPEN) {
        ws.send(entry.data, { binary: true });
      } else if (entry.senderToken === decoded.contactToken && now - entry.ts < BUFFER_TTL_MS) {
        remaining.push(entry);
      }
    }
    if (remaining.length > 0) {
      messageBuffer.set(sessionId, remaining);
    } else {
      messageBuffer.delete(sessionId);
    }
  }

  // Message relay for session
  ws.on("message", (data, isBinary) => {
    if (!isBinary) { ws.close(4004, "Binary only"); return; }

    const bytes = Buffer.isBuffer(data) ? data : Buffer.from(data);
    if (bytes.length < 6) return;

    let relayed = 0;
    for (const peer of room) {
      if (peer !== ws && peer.readyState === WebSocket.OPEN) {
        if (peer.bufferedAmount > 512 * 1024) {
          continue; // Avoid flooding slow consumer
        }
        peer.send(data, { binary: true });
        relayed++;
      }
    }
    if (relayed === 0) {
      const buf = messageBuffer.get(sessionId) || [];
      if (buf.length < MAX_BUFFER_PER_CONV) {
        buf.push({ senderToken: decoded.contactToken, data: Buffer.from(bytes), ts: Date.now() });
        messageBuffer.set(sessionId, buf);
      }
    }
  });

  ws.on("close", () => {
    room.delete(ws);
    wsIdentityMap.delete(ws);
    // Notify remaining peer that this user disconnected
    const PEER_DISCONNECTED = Buffer.from([0xFF, 0xFE, ...Buffer.from("PEER_DISCONNECTED")]);
    for (const peer of room) {
      if (peer.readyState === WebSocket.OPEN) {
        try { peer.send(PEER_DISCONNECTED, { binary: true }); } catch (_) {}
      }
    }
    if (room.size === 0) {
      conversationSockets.delete(sessionId);
    }
    // If the session has expired, destroy it
    if (Date.now() > session.expiresAt) {
      destroySession(sessionId);
    }
  });

  ws.on("error", () => {
    room.delete(ws);
    wsIdentityMap.delete(ws);
  });
}

function handleConversationSocket(ws, conversationId, caller, identityHash) {
  if (!/^[a-f0-9_]+$/.test(conversationId) || conversationId.length > 200) {
    ws.close(4002, "Invalid conversation ID");
    return;
  }

  // Best-effort membership check: if the caller's identity is known, verify they are
  // one of the two participants embedded in the standard conv ID ("<hash64>_<hash64>").
  if (caller.identityHashHex) {
    const parts = conversationId.split("_");
    if (parts.length === 2 && parts[0].length === 64 && parts[1].length === 64) {
      if (caller.identityHashHex !== parts[0] && caller.identityHashHex !== parts[1]) {
        ws.close(4014, "Not a participant of this conversation");
        return;
      }
    }
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

  if (identityHash && /^[a-f0-9]{64}$/.test(identityHash)) {
    // If authenticated user has a registered identity hash, prevent spoofing a different ID
    if (caller?.identityHashHex && !timingSafeEqual(caller.identityHashHex, identityHash)) {
      ws.close(4014, "Identity hash mismatch");
      return;
    }
    ws.identityHash = identityHash;
    wsIdentityMap.set(ws, identityHash);
    const existing = presenceMap.get(identityHash) || {};
    presenceMap.set(identityHash, { ...existing, lastSeen: Date.now(), online: true });
  } else if (caller?.identityHashHex) {
    ws.identityHash = caller.identityHashHex;
    wsIdentityMap.set(ws, caller.identityHashHex);
    const existing = presenceMap.get(caller.identityHashHex) || {};
    presenceMap.set(caller.identityHashHex, { ...existing, lastSeen: Date.now(), online: true });
  }

  // Deliver buffered messages intended for this participant
  const backlog = messageBuffer.get(conversationId);
  if (backlog && backlog.length > 0) {
    const now = Date.now();
    const remaining = [];
    for (const entry of backlog) {
      if ((!identityHash || entry.senderHash !== identityHash) && now - entry.ts < BUFFER_TTL_MS && ws.readyState === WebSocket.OPEN) {
        ws.send(entry.data, { binary: true });
      } else if (identityHash && entry.senderHash === identityHash && now - entry.ts < BUFFER_TTL_MS) {
        remaining.push(entry);
      }
    }
    if (remaining.length > 0) {
      messageBuffer.set(conversationId, remaining);
    } else {
      messageBuffer.delete(conversationId);
    }
  }

  ws.on("message", (data, isBinary) => {
    if (!isBinary) { ws.close(4004, "Binary only"); return; }

    const bytes = Buffer.isBuffer(data) ? data : Buffer.from(data);
    if (bytes.length < 6) return;

    let relayed = 0;
    for (const peer of room) {
      if (peer !== ws && peer.readyState === WebSocket.OPEN) {
        if (peer.bufferedAmount > 512 * 1024) {
          continue; // Avoid flooding slow consumer
        }
        peer.send(data, { binary: true });
        relayed++;
      }
    }
    if (relayed === 0) {
      const buf = messageBuffer.get(conversationId) || [];
      if (buf.length < MAX_BUFFER_PER_CONV) {
        buf.push({ senderHash: identityHash || "", data: Buffer.from(bytes), ts: Date.now() });
        messageBuffer.set(conversationId, buf);
      }
    }
  });

  ws.on("close", () => {
    room.delete(ws);
    const PEER_DISCONNECTED = Buffer.from([0xFF, 0xFE, ...Buffer.from("PEER_DISCONNECTED")]);
    for (const peer of room) {
      if (peer !== ws && peer.readyState === WebSocket.OPEN) {
        try { peer.send(PEER_DISCONNECTED, { binary: true }); } catch (_) {}
      }
    }
    if (room.size === 0) conversationSockets.delete(conversationId);

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

  ws.on("error", () => {
    room.delete(ws);
    wsIdentityMap.delete(ws);
  });
}

wss.on("connection", (ws, req) => {
  const authToken = extractBearerToken(req.headers);
  if (!authToken) {
    ws.close(4012, "Authentication required");
    return;
  }

  let decoded;
  try {
    decoded = jwt.verify(authToken, JWT_SECRET, { algorithms: ["HS256"] });
  } catch (e) {
    ws.close(4013, "Invalid token");
    return;
  }

  if (decoded.jti && burnedTokens.has(decoded.jti)) {
    ws.close(4013, "Token has been revoked");
    return;
  }

  const caller = users.get(decoded.username);
  if (!caller) {
    ws.close(4013, "User account no longer exists");
    return;
  }

  const url = new URL(req.url, "http://localhost");
  const conversationId = url.searchParams.get("conv");
  const sessionId = url.searchParams.get("session");

  if (sessionId) {
    handleEphemeralSessionSocket(ws, sessionId, decoded);
  } else if (conversationId) {
    const identityHash = url.searchParams.get("id");
    handleConversationSocket(ws, conversationId, caller, identityHash);
  } else {
    ws.close(4001, "Missing conv or session parameter");
  }
});

function getTotalConnections() {
  let total = 0;
  for (const room of conversationSockets.values()) total += room.size;
  return total;
}

//
// HEARTBEAT: ping all clients every 30s; terminate unresponsive ones
//
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
      if (ws.readyState !== WebSocket.OPEN) {
        wsIdentityMap.delete(ws);
        room.delete(ws);
      }
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

  // Auto-delete user records after USER_TTL_MS of inactivity
  for (const [username, user] of users.entries()) {
    const ref = user.lastActive || user.createdAt;
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
    }
  }

  // Cleanup expired burnedTokens: evict entries whose corresponding JWTs have expired
  // (JWT_EXPIRY = 30 min + 5 min grace = 35 min). Never bulk-clear: that would re-validate
  // recently revoked tokens that are still within their 30-minute window.
  const REVOCATION_GRACE_MS = 35 * 60 * 1000;
  const nowBurned = Date.now();
  for (const [jti, burnedAt] of burnedTokens.entries()) {
    if (nowBurned - burnedAt > REVOCATION_GRACE_MS) burnedTokens.delete(jti);
  }
}, 60_000);

if (require.main === module) {
  server.listen(PORT, () => {
    console.log(`\nCryptika Relay Server v3.0.0`);
    console.log(`   Listening on port ${PORT}`);
    console.log(`   Auth: POST /api/v1/auth/enter (passwordless)`);
    console.log(`   Server is BLIND -- no passwords, no logs, only ciphertext relay\n`);
  });
} else {
  module.exports = {
    app,
    server,
    safeLog,
    timingSafeEqual,
    deriveContactToken,
    isRateLimited,
    verifyAuthProof,
    users,
    burnedTokens,
    ephemeralSessions,
    presenceMap,
  };
}
