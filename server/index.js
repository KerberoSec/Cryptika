// server/index.js
// Cryptika Relay Server — v1.0.0
// Blind relay: routes encrypted packets without inspecting content
// Only knows: conversationId, messageId, routing
//
// Run: node index.js
// Dependencies: npm install express ws tweetnacl

"use strict";

const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const nacl = require("tweetnacl");
const crypto = require("crypto");

// ══════════════════════════════════════════════════════════════════════════════
// SERVER CONFIG
// ══════════════════════════════════════════════════════════════════════════════
const PORT = process.env.PORT || 8443;
const TICKET_EXPIRY_SECONDS = 3600; // 1 hour
const MAX_CONNECTIONS_PER_CONV = 10;

// Server Ed25519 signing keypair — generate once, hardcode public key in app
// In production: load from secure key store
let serverKeyPair;
try {
  const savedKey = process.env.SERVER_PRIVATE_KEY_HEX;
  if (savedKey) {
    const privBytes = Buffer.from(savedKey, "hex");
    serverKeyPair = nacl.sign.keyPair.fromSeed(privBytes);
  } else {
    serverKeyPair = nacl.sign.keyPair();
    console.log("⚠️  NEW SERVER KEYPAIR GENERATED");
    console.log("   Hardcode this public key in BuildConfig.SERVER_PUBLIC_KEY_HEX:");
    console.log("   " + Buffer.from(serverKeyPair.publicKey).toString("hex"));
    console.log("   Set SERVER_PRIVATE_KEY_HEX env var to persist across restarts");
  }
} catch (e) {
  serverKeyPair = nacl.sign.keyPair();
}

console.log(`Server public key: ${Buffer.from(serverKeyPair.publicKey).toString("hex")}`);

// ══════════════════════════════════════════════════════════════════════════════
// IN-MEMORY ROUTING TABLE
// Maps conversationId → Set<WebSocket>
// ══════════════════════════════════════════════════════════════════════════════
const conversationSockets = new Map(); // conversationId → Set<ws>
const presenceMap = new Map();         // identityHash → { connectionToken, lastSeen, online }
const wsIdentityMap = new Map();       // ws → identityHash (for WS-based presence tracking)

// Offline message buffer: conversationId → [{data: Buffer, ts: number}]
const messageBuffer = new Map();
const MAX_BUFFER_PER_CONV = 50;
const BUFFER_TTL_MS = 3_600_000; // 1 hour

// ══════════════════════════════════════════════════════════════════════════════
// EXPRESS REST API
// ══════════════════════════════════════════════════════════════════════════════
const app = express();
app.use(express.json());

// Health check
app.get("/health", (req, res) => {
  res.json({
    status: "ok",
    version: "1.0.0",
    connections: getTotalConnections(),
    timestamp: Date.now()
  });
});

/**
 * POST /api/v1/ticket
 * Issues a signed session ticket for a pair of identity hashes.
 *
 * Ticket binary (140 bytes):
 *   [32] a_id
 *   [32] b_id
 *   [8]  timestamp_ms big-endian
 *   [4]  expiry_seconds big-endian
 *   [64] Ed25519 signature of above 76 bytes
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

    // Build 76-byte payload
    const payload = Buffer.alloc(76);
    aIdBytes.copy(payload, 0);
    bIdBytes.copy(payload, 32);
    payload.writeBigInt64BE(BigInt(timestamp), 64);
    payload.writeInt32BE(expirySeconds, 72);

    // Sign with server Ed25519 key
    const signature = nacl.sign.detached(payload, serverKeyPair.secretKey);

    const ticket = Buffer.concat([payload, Buffer.from(signature)]);

    console.log(`Ticket issued: ${a_id.slice(0, 8)}... ↔ ${b_id.slice(0, 8)}...`);

    res.json({
      ticket_b64: ticket.toString("base64"),
      server_public_key_b64: Buffer.from(serverKeyPair.publicKey).toString("base64")
    });
  } catch (e) {
    console.error("Ticket error:", e);
    res.status(500).json({ error: "Internal error" });
  }
});

/**
 * POST /api/v1/presence
 * Register or query presence for an identity.
 */
app.post("/api/v1/presence", (req, res) => {
  const { identity_hash, connection_token } = req.body;

  if (!identity_hash) return res.status(400).json({ error: "identity_hash required" });

  const token = connection_token || crypto.randomBytes(32).toString("hex");

  presenceMap.set(identity_hash, {
    connectionToken: token,
    lastSeen: Date.now(),
    online: true
  });

  res.json({ online: true, token });
});

/**
 * GET /api/v1/presence/:hash
 * Query whether an identity is currently online.
 * Returns JSON: { online: boolean, lastSeen: number|null }
 */
app.get("/api/v1/presence/:hash", (req, res) => {
  const hash = req.params.hash;
  if (!hash || !/^[a-f0-9]{64}$/.test(hash))
    return res.status(400).json({ error: "invalid hash" });
  const entry = presenceMap.get(hash);
  if (!entry) return res.json({ online: false, lastSeen: null });
  // Treat entry as stale if not refreshed in 5 minutes (belt-and-suspenders)
  const stale = Date.now() - entry.lastSeen > 300_000;
  res.json({ online: stale ? false : (entry.online ?? true), lastSeen: entry.lastSeen });
});

// ══════════════════════════════════════════════════════════════════════════════
// WEBSOCKET SERVER
// ══════════════════════════════════════════════════════════════════════════════
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/ws" });

wss.on("connection", (ws, req) => {
  const url = new URL(req.url, "http://localhost");
  const conversationId = url.searchParams.get("conv");

  if (!conversationId) {
    ws.close(4001, "Missing conv parameter");
    return;
  }

  // Basic conversation ID format check
  if (!/^[a-f0-9_]+$/.test(conversationId) || conversationId.length > 200) {
    ws.close(4002, "Invalid conversation ID");
    return;
  }

  // Join conversation room
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

  // Ping/pong heartbeat — mark alive on every pong response
  ws.isAlive = true;
  ws.on("pong", () => { ws.isAlive = true; });

  // WS-based presence: read optional ?id=<identityHash> query param
  const identityHash = url.searchParams.get("id");
  if (identityHash && /^[a-f0-9]{64}$/.test(identityHash)) {
    ws.identityHash = identityHash;
    wsIdentityMap.set(ws, identityHash);
    const existing = presenceMap.get(identityHash) || {};
    presenceMap.set(identityHash, { ...existing, lastSeen: Date.now(), online: true });
  }

  console.log(`Client joined conversation ${conversationId.slice(0, 16)}... (${room.size} members)`);

  // Deliver any buffered messages to the newly-connected participant
  const backlog = messageBuffer.get(conversationId);
  if (backlog && backlog.length > 0) {
    const now = Date.now();
    for (const entry of backlog) {
      if (now - entry.ts < BUFFER_TTL_MS && ws.readyState === WebSocket.OPEN) {
        ws.send(entry.data, { binary: true });
      }
    }
    messageBuffer.delete(conversationId);
    console.log(`  → delivered ${backlog.length} buffered message(s) to late joiner`);
  }

  ws.on("message", (data, isBinary) => {
    // ALL messages are binary — relay to all OTHER sockets in the same conversation
    if (!isBinary) {
      ws.close(4004, "Text frames not accepted — binary only");
      return;
    }

    const bytes = Buffer.isBuffer(data) ? data : Buffer.from(data);

    // Parse routing header: [2 conv_len][conv_bytes][2 msgId_len][msgId_bytes][...packet]
    if (bytes.length < 6) {
      console.warn("Packet too short, ignoring");
      return;
    }

    // Log routing info (NOT content — relay is blind)
    const convLen = bytes.readUInt16BE(0);
    const msgIdOffset = 2 + convLen;
    const msgIdLen = bytes.readUInt16BE(msgIdOffset);
    const payloadOffset = msgIdOffset + 2 + msgIdLen;
    const payloadSize = bytes.length - payloadOffset;

    console.log(`Routing ${payloadSize}B packet in ${conversationId.slice(0, 8)}...`);

    // Route to all OTHER participants in this conversation
    let relayed = 0;
    for (const peer of room) {
      if (peer !== ws && peer.readyState === WebSocket.OPEN) {
        peer.send(data, { binary: true });
        relayed++;
      }
    }
    console.log(`  → relayed to ${relayed} peers`);

    // If no peer was online, buffer the packet for offline delivery
    if (relayed === 0) {
      const buf = messageBuffer.get(conversationId) || [];
      if (buf.length < MAX_BUFFER_PER_CONV) {
        buf.push({ data: Buffer.from(bytes), ts: Date.now() });
        messageBuffer.set(conversationId, buf);
        console.log(`  → buffered for offline delivery (${buf.length} queued)`);
      }
    }
  });

  ws.on("close", () => {
    room.delete(ws);
    if (room.size === 0) {
      conversationSockets.delete(conversationId);
    }
    console.log(`Client left ${conversationId.slice(0, 16)}... (${room.size} remaining)`);

    // WS-based presence: mark offline after short delay (allows quick reconnects
    // to not flash as offline, e.g. during network handoff)
    const hash = wsIdentityMap.get(ws);
    wsIdentityMap.delete(ws);
    if (hash) {
      setTimeout(() => {
        // Only mark offline if no other WebSocket for this identity is alive
        const stillConnected = [...wsIdentityMap.values()].includes(hash);
        if (!stillConnected) {
          const entry = presenceMap.get(hash);
          if (entry) presenceMap.set(hash, { ...entry, online: false, lastSeen: Date.now() });
        }
      }, 5_000);
    }
  });

  ws.on("error", (err) => {
    console.error(`WebSocket error in ${conversationId.slice(0, 8)}...:`, err.message);
    room.delete(ws);
  });
});

function getTotalConnections() {
  let total = 0;
  for (const room of conversationSockets.values()) total += room.size;
  return total;
}

// ══════════════════════════════════════════════════════════════════════════════
// HEARTBEAT — ping all clients every 30 s; terminate unresponsive ones
// This prevents zombie WebSocket connections from accumulating when a client
// vanishes without a TCP FIN (Doze mode, network cut, etc.)
// ══════════════════════════════════════════════════════════════════════════════
setInterval(() => {
  for (const room of conversationSockets.values()) {
    for (const ws of room) {
      if (!ws.isAlive) {
        // No pong received since last ping — terminate dead connection
        room.delete(ws);
        ws.terminate();
        continue;
      }
      ws.isAlive = false;
      ws.ping();
    }
  }
}, 30_000);

// Periodic cleanup of empty rooms
setInterval(() => {
  for (const [convId, room] of conversationSockets.entries()) {
    for (const ws of room) {
      if (ws.readyState !== WebSocket.OPEN) room.delete(ws);
    }
    if (room.size === 0) conversationSockets.delete(convId);
  }
  
  // Cleanup stale presence entries (older than 5 minutes)
  const cutoff = Date.now() - 300_000;
  for (const [hash, info] of presenceMap.entries()) {
    if (info.lastSeen < cutoff) presenceMap.delete(hash);
  }

  // Cleanup expired offline message buffer entries
  const bufExpiry = Date.now() - BUFFER_TTL_MS;
  for (const [convId, buf] of messageBuffer.entries()) {
    const fresh = buf.filter(e => e.ts > bufExpiry);
    if (fresh.length === 0) messageBuffer.delete(convId);
    else messageBuffer.set(convId, fresh);
  }
}, 60_000);

server.listen(PORT, () => {
  console.log(`\n🔒 Cryptika Relay Server v1.0.0`);
  console.log(`   Listening on port ${PORT}`);
  console.log(`   REST: http://localhost:${PORT}/api/v1/`);
  console.log(`   WebSocket: ws://localhost:${PORT}/ws?conv=<conversationId>`);
  console.log(`   Server is BLIND — does not log message content\n`);
});
