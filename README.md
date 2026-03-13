<div align="center">

# Cryptika — Complete Technical Reference

**A self-hosted, end-to-end encrypted Android messenger with ephemeral sessions,  
encrypted voice calls, self-destructing messages, and a cryptographically blind relay server.**

No passwords. No stored data. No metadata beyond connection timing.  
Identity is a locally generated Ed25519 keypair — nothing else.

![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-blue)
![Crypto](https://img.shields.io/badge/Crypto-Ed25519%20%7C%20X25519%20%7C%20ChaCha20--Poly1305-red)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Self--Hosted](https://img.shields.io/badge/Server-Self--Hosted-orange)
![Server](https://img.shields.io/badge/Server-Blind%20%26%20Ephemeral-critical)

</div>

---

## Table of Contents

1. [What Is Cryptika](#1-what-is-cryptika)
2. [High-Level System Diagram](#2-high-level-system-diagram)
3. [How The App Starts — First Launch Flow](#3-how-the-app-starts--first-launch-flow)
4. [Passwordless Entry — No Accounts, No Passwords](#4-passwordless-entry--no-accounts-no-passwords)
5. [Custom Secure Keyboard — System IME Blocked](#5-custom-secure-keyboard--system-ime-blocked)
6. [Identity System — Ed25519 Deep Dive](#6-identity-system--ed25519-deep-dive)
7. [Contact Discovery — Username-Based Ephemeral Pairing](#7-contact-discovery--username-based-ephemeral-pairing)
8. [Ephemeral Sessions — 5-Phase Credential Architecture](#8-ephemeral-sessions--5-phase-credential-architecture)
9. [Adding a Contact — QR Exchange Flow](#9-adding-a-contact--qr-exchange-flow)
10. [Networking Layer — WebSocket Architecture](#10-networking-layer--websocket-architecture)
11. [Cryptographic Handshake — X25519 DH Step by Step](#11-cryptographic-handshake--x25519-dh-step-by-step)
12. [Hash Ratchet — Forward Secrecy Mechanism](#12-hash-ratchet--forward-secrecy-mechanism)
13. [Sending a Message — Complete Step-by-Step Flow](#13-sending-a-message--complete-step-by-step-flow)
14. [Receiving a Message — Complete Step-by-Step Flow](#14-receiving-a-message--complete-step-by-step-flow)
15. [Wire Packet Format — Byte-Level Breakdown](#15-wire-packet-format--byte-level-breakdown)
16. [Encrypted Storage — How Messages Rest On Disk](#16-encrypted-storage--how-messages-rest-on-disk)
17. [Message Expiry — Cryptographic Self-Destruction](#17-message-expiry--cryptographic-self-destruction)
18. [Peer Disconnect — Automatic Session Destruction](#18-peer-disconnect--automatic-session-destruction)
19. [Auto-Logout — Screen Off, Back, Home, Minimize](#19-auto-logout--screen-off-back-home-minimize)
20. [Per-Chat Screenshot Blocking](#20-per-chat-screenshot-blocking)
21. [Encrypted Voice Calls — Full Protocol](#21-encrypted-voice-calls--full-protocol)
22. [Background Operation — Doze, Reconnect, Services](#22-background-operation--doze-reconnect-services)
23. [Blind Relay Server — Internal Architecture](#23-blind-relay-server--internal-architecture)
24. [Server REST API Reference](#24-server-rest-api-reference)
25. [Dependency Injection — How Hilt Wires Everything](#25-dependency-injection--how-hilt-wires-everything)
26. [Database Schema — Tables, Migrations, Indexes](#26-database-schema--tables-migrations-indexes)
27. [All Source Files Explained](#27-all-source-files-explained)
28. [Complete Data Flow: Alice Sends Bob a Message](#28-complete-data-flow-alice-sends-bob-a-message)
29. [Threat Model and Security Analysis](#29-threat-model-and-security-analysis)
30. [Android Permissions Explained](#30-android-permissions-explained)
31. [Tech Stack Reference](#31-tech-stack-reference)
32. [Building, Running, and Configuring](#32-building-running-and-configuring)
33. [Docker Deployment](#33-docker-deployment)
34. [Troubleshooting](#34-troubleshooting)
35. [License](#35-license)

---

## 1. What Is Cryptika

Cryptika is a fully open-source, self-hostable secure messenger for Android. It is designed around one core principle: **the relay server must never be trusted**.

Even if the relay server is completely compromised — captured by an adversary, subpoenaed, or running malicious code — it cannot read any message content, discover who is talking to whom (beyond connection metadata), or forge messages. Every security property holds under full server compromise.

The server is **blind**: it stores nothing to disk, writes no logs, and purges all in-memory state automatically. After the first message is sent, the user's server-side credentials are burned — the server forgets they ever existed.

### The core design decisions

| Decision | Reason |
|----------|--------|
| Passwordless entry (username only) | No passwords to steal, no accounts to breach. A single public username is all it takes to enter. |
| Identity = Ed25519 keypair | Your identity is mathematically provable, never stored centrally, and exists only on your device. |
| Custom in-app keyboard | System keyboards can log keystrokes. Cryptika blocks the system IME entirely and provides its own secure keyboard. |
| Ephemeral sessions | All chat sessions are time-limited. When the session ends, all data is cryptographically erased from both devices. |
| 5-phase credential burn | Server credentials are burned after the first message — the server forgets your identity entirely. |
| Blind relay server | No logs, no database, no disk writes. All state is in-memory and auto-purged. |
| Random display names | When two users connect, both see random pseudonyms — real usernames are never shared over the wire. |
| Hash ratchet per session | Each message uses a unique key derived by a one-way SHA-256 chain, providing forward secrecy. |
| Per-message Keystore keys | Even with root access and the SQLite database, messages are unreadable without hardware-backed Keystore keys. |
| Auto-destruct on any exit | Screen off, back button, home button, minimize — any form of leaving the app triggers full wipe and logout. |
| Peer disconnect wipe | When one user's connection drops, both sides are wiped instantly. |
| 3-second ephemeral default | Chat messages self-destruct in 3 seconds by default. |

---

## 2. High-Level System Diagram

```
+------------------+                         +------------------+
|   Alice's Phone  |                         |   Bob's Phone    |
|                  |                         |                  |
|  +------------+  |                         |  +------------+  |
|  | Cryptika   |  |   WebSocket binary      |  | Cryptika   |  |
|  | Android App|<------------------------------>| Android App|  |
|  +------------+  |         |               |  +------------+  |
|                  |         |               |                  |
+------------------+         |               +------------------+
                              |
                    +---------v---------+
                    |   Relay Server    |
                    |   (Node.js)       |
                    |                   |
                    | BLIND:            |
                    | • No logs         |
                    | • No database     |
                    | • No disk writes  |
                    | • In-memory only  |
                    | • Auto-purge      |
                    |                   |
                    | Routes encrypted  |
                    | binary blobs.     |
                    | Cannot read       |
                    | content.          |
                    +-------------------+

Everything inside the WebSocket frames is encrypted.
The relay server sees binary blobs + ephemeral session UUIDs.
```

### What Alice's phone contains

```
Android Keystore (hardware-backed)
  - cryptika_identity_wrapping_key     AES-256-GCM  (wraps identity private key)
  - cryptika_db_passphrase_key         AES-256-GCM  (wraps SQLCipher passphrase)
  - msg_<uuid_1>                       AES-256-GCM  (wraps message #1 blob)
  - msg_<uuid_2>                       AES-256-GCM  (wraps message #2 blob)
  - ... one key per stored message ...

EncryptedSharedPreferences: cryptika_auth_prefs  (AES-256-SIV + AES-256-GCM)
  - jwtToken                           (30-minute JWT from server)
  - contactToken                       (HMAC-SHA256 derived token)
  - username                           (public username entered by user)
  - tokenExpiresAt                     (JWT expiry timestamp)
  - credentialsBurned                  (boolean — true after first message sent)

SharedPreferences: cryptika_identity_prefs
  - identity_public_key_base64         (plaintext, it's public)
  - identity_private_key_encrypted     (AES-256-GCM blob, key in Keystore)
  - identity_hash_hex                  SHA-256(public_key) as hex

SQLCipher database: cryptika.db  (AES-256 encrypted, passphrase from Keystore)
  Tables: contacts, messages, conversations, local_identity
```

---

## 3. How The App Starts — First Launch Flow

### Application class: CryptikaApp.kt

When Android creates the application process, `CryptikaApp.onCreate()` runs:

```
CryptikaApp.onCreate()
  |
  +-- Hilt.init()
  |     Hilt's generated app component is created, all @Singleton
  |     bindings are initialized lazily via AppModule.kt
  |
  +-- startForegroundService(ConnectionForegroundService)
  |     Started immediately so the "messaging is active" notification
  |     appears within 5 seconds (Android requirement).
  |
  +-- BackgroundConnectionManager.startAllConnections()
        Queries the local database for all known contacts,
        and for each one calls ensureConnected(conversationId).
```

### MainActivity startup

```
MainActivity.onCreate()
  |
  +-- enableEdgeToEdge()
  |
  +-- MessageExpiryWorker.schedule(this)   ← periodic background expiry
  |
  +-- registerReceiver(screenOffReceiver, ACTION_SCREEN_OFF)
  |     ← screen-off triggers full wipe + logout
  |
  +-- setContent { CryptikaNavGraph() }
        NavController created.
        Start destination = AUTH route.
        ← User must enter username every launch (no persistent login)
```

### Auth → Splash → Home

```
AUTH screen (username entry via SecureKeyboard)
  |
  +-- User enters username, taps "Enter"
  |
  +-- AuthViewModel.enter(username)
  |     1. Ensure Ed25519 identity exists (generate if first launch)
  |     2. POST /api/v1/auth/enter → receive JWT + contactToken
  |     3. Store in EncryptedSharedPreferences (AuthStore)
  |     4. Navigate to SPLASH
  |
SPLASH screen
  |
  +-- SplashViewModel checks identity is initialized
  |     Navigate to HOME
  |
HOME screen
  |
  +-- Shows conversation list
  +-- FAB to add contacts (QR or Contact Discovery)
```

---

## 4. Passwordless Entry — No Accounts, No Passwords

**There are no passwords anywhere in Cryptika.**

The entry flow uses a single endpoint: `POST /api/v1/auth/enter`. The user provides only a public username. The server never stores passwords. Identity is proven purely through Ed25519 public key cryptography.

### Entry flow

```
User types username via SecureKeyboard
  |
  +-- AuthViewModel.enter(username)
  |     |
  |     +-- identityRepository.getLocalIdentity()
  |     |     If null: identityRepository.generateIdentity()  ← first launch
  |     |
  |     +-- authRepository.enter(username)
  |           |
  |           +-- POST /api/v1/auth/enter
  |                 Body: { username, identityHashHex, publicKeyB64 }
  |                 ← No password field
  |                 |
  |                 Server:
  |                 1. Derive contactToken = HMAC-SHA256(HMAC_SECRET, username)
  |                 2. Store user in ephemeral in-memory Map (30-min TTL)
  |                 3. Sign JWT (30-min expiry)
  |                 4. Return { token, contactToken, expiresAt }
  |
  +-- AuthStore saves: jwtToken, contactToken, username, expiresAt
  |
  +-- Navigate to SPLASH → HOME
```

### Auth screen UI

- Username field is `readOnly = true` — input only through the custom SecureKeyboard
- Single "Enter" button (no register/login split)
- Subtitle: "No passwords. No accounts. Just a public username."
- `windowSoftInputMode="stateAlwaysHidden"` in AndroidManifest prevents system keyboard

---

## 5. Custom Secure Keyboard — System IME Blocked

**File: `presentation/ui/components/SecureKeyboard.kt`**

System keyboards (Gboard, SwiftKey, etc.) can log keystrokes, sync to cloud, and leak typed data. Cryptika eliminates this risk entirely with a custom in-app keyboard.

### How it works

```
SecureKeyboard composable
  |
  +-- Three keyboard modes: LOWER, UPPER, SYMBOLS
  |
  +-- Layout: 4 rows of keys in a full-width panel
  |     Row 1: q w e r t y u i o p  (or symbols)
  |     Row 2: a s d f g h j k l    (or symbols)
  |     Row 3: ⇧ z x c v b n m ⌫
  |     Row 4: ?123  space  Done
  |
  +-- Haptic feedback on every key press (HapticFeedbackType.TextHandleMove)
  |
  +-- Auto-reverts to lowercase after typing one uppercase character
  |
  +-- AnimatedVisibility for smooth expand/collapse
  |
  +-- Toggle bar: "Show/Hide Secure Keyboard" label
```

### System keyboard blocking

Every text field in the app uses this pattern:

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { /* no-op — handled by SecureKeyboard callbacks */ },
    readOnly = true,   // ← blocks system IME from opening
    ...
)
```

Combined with `android:windowSoftInputMode="stateAlwaysHidden|adjustResize"` in the AndroidManifest, the system keyboard is never invoked anywhere in the app.

### Where it's used

- **AuthScreen** — username entry
- **ContactDiscoveryScreen** — target username entry + contact name
- **ChatScreen** — message composition
- **QrScanAndSettingsScreens** — settings input fields

---

## 6. Identity System — Ed25519 Deep Dive

**File: `domain/crypto/IdentityKeyManager.kt`**

### Why Ed25519

Ed25519 signatures are deterministic — no random nonce per signing operation. This eliminates the vulnerability class that gave us the PlayStation 3 hack and Bitcoin vanity address attacks. Ed25519 has 32-byte keys and 64-byte signatures — compact, fast, well-audited.

### Key generation

```kotlin
val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider())
val keyPair = keyPairGenerator.generateKeyPair()

val publicKeyBytes  = keyPair.public.encoded   // 32 bytes
val privateKeySeed  = keyPair.private.encoded  // 32 bytes (seed)
```

### Private key protection

The 32-byte private key seed is **immediately** wrapped with Android Keystore:

```
1. Create AES-256-GCM key in Keystore: alias "cryptika_identity_wrapping_key"
2. Generate random 12-byte IV
3. Encrypt: cipher.doFinal(privateKeySeed) → encryptedSeed
4. Store: SharedPrefs["identity_private_key_encrypted"] = Base64(iv + encryptedSeed)
5. privateKeySeed.fill(0)  ← raw bytes gone from memory
```

After step 5, the raw private key exists nowhere in the app's memory or storage. It materializes transiently inside `sign()` for the exact duration of one signing operation.

### Identity fingerprint

```
identityHash = SHA-256(publicKeyBytes)   // 32 bytes
displayed as: AABBCCDD EEFF0011 22334455 66778899 AABBCCDD EEFF0011 22334455 66778899
              (8 groups of 8 hex chars, for visual verification)
```

Both users verify fingerprints out-of-band during the contact setup dialog to confirm they are talking to the right person.

---

## 7. Contact Discovery — Username-Based Ephemeral Pairing

**Files: `AuthScreens.kt`, `AuthViewModel.kt`, `AuthRepositoryImpl.kt`, `EphemeralSessionManager.kt`**

Cryptika supports two methods of adding contacts:
1. **QR code exchange** (in-person, highest security)
2. **Username-based contact discovery** (remote)

### Username-based contact discovery flow

```
ALICE (wants to chat with Bob)                    BOB (waiting for requests)
========================================         ========================================

ContactDiscoveryScreen:                          ContactDiscoveryScreen:
  Alice enters Bob's username                      (idle, polling every 5 seconds)
  Taps "Send Request"
  |
  +-- sendContactRequest()
  |     nickname = "User_<random_8chars>"          ← real username hidden
  |     POST /api/v1/contact/request
  |       { targetUsername: "bob",
  |         fromNickname: "User_a3f8b2c1" }
  |
  |     Server:
  |     1. Lookup Bob by contactToken =
  |        HMAC-SHA256(SECRET, "bob")               Bob's poll returns Alice's request:
  |     2. Store pending request                    |
  |     3. Return { status: "request_sent" }        +-- GET /api/v1/contact/requests
  |        (always same response, even if             → Shows: PendingRequestCard with
  |         "bob" doesn't exist — anti-enumeration)     "User_a3f8b2c1" + Accept/Reject
  |
  |                                                  Bob taps "Accept"
  |                                                  |
  |                                                  +-- acceptRequest(requestId)
  |                                                  |     POST /api/v1/contact/accept
  |                                                  |     → Creates ephemeral session (30m TTL)
  |                                                  |     → Returns sessionUUID + peer info
  |                                                  |
  |                                                  +-- Shows Contact Setup Dialog:
  |                                                        • "Anonymous" (peer name hidden)
  |                                                        • Identity Fingerprint (monospace)
  |                                                        • "Verify fingerprint out-of-band"
  |                                                        • [Confirm & Chat] button
  |                                                  |
  |                                                  +-- confirmSetup()
  |                                                        randomName = "User_<random_8chars>"
  |                                                        joinSession(sessionUUID, randomName)
  |                                                        → Navigate to EPHEMERAL_CHAT
  |
  Alice's polling detects accepted session:
  |
  +-- pollAcceptedSessions() every 5s
  |     GET /api/v1/contact/accepted
  |     → Found! Shows Contact Setup Dialog
  |
  +-- confirmSetup()
        randomName = "User_<random_8chars>"
        joinSession(sessionUUID, randomName)
        → Navigate to EPHEMERAL_CHAT
```

### Key security properties

1. **Anti-enumeration**: `POST /api/v1/contact/request` always returns `{ status: "request_sent" }` whether the target username exists or not. An attacker cannot probe for usernames.
2. **Random display names**: Both users see random `"User_XXXXXXXX"` pseudonyms. Real usernames are never transmitted to the peer.
3. **Identity fingerprint verification**: The contact setup dialog shows an Ed25519 fingerprint for out-of-band verification.
4. **Rate limiting**: 10 requests per IP per minute, 20 per user per day.
5. **Self-request prevention**: Server uses timing-safe comparison to prevent requesting oneself.

---

## 8. Ephemeral Sessions — 5-Phase Credential Architecture

**File: `EphemeralSessionManager.kt`**

Every chat in Cryptika is an ephemeral session with a fixed lifetime (default: 30 minutes). When the session ends, all data is cryptographically erased from both devices.

### The 5 phases

```
PHASE 1 — ENTRY
  User enters username → POST /api/v1/auth/enter
  Server creates in-memory user record with 30-min TTL.
  User record auto-purges after TTL expires.

PHASE 2 — CONTACT DISCOVERY & SESSION CREATION
  POST /api/v1/contact/request → POST /api/v1/contact/accept
  Server creates ephemeral session with 30-min TTL.
  Max 5 active sessions per user.

PHASE 3 — SESSION JOIN & HANDSHAKE
  Both users call EphemeralSessionManager.joinSession():
    1. Save peer as ephemeral contact in local DB
    2. Create WebSocket connected to the session room
    3. Schedule auto-destroy timer
    4. Perform X25519 DH handshake → derive session key
    5. Create HashRatchet for forward secrecy

PHASE 4 — CREDENTIAL BURN
  After the FIRST message is sent:
    triggerBurnOnFirstMessage()
      → POST /api/v1/auth/burn
      → Server deletes user record, blacklists JWT, cascades contact requests
      → AuthStore marks credentialsBurned = true
  The server now has NO record the user ever existed.

PHASE 5 — SESSION DESTRUCTION (Cryptographic Erasure)
  Triggered by: session expiry, peer disconnect, WS drop, screen off,
                back/home/minimize, or manual exit.
  EphemeralSessionManager.destroySession():
    1. Close WebSocket
    2. Zeroize all crypto material (DH keys, session keys, ratchet state)
    3. Delete all messages for this conversation from DB
    4. Delete the ephemeral contact record
    5. Unregister all handlers
  destroyAllSessions() wipes everything at once.
```

### Server-side cleanup

```
User TTL:    30 minutes → auto-purge user record
Session TTL: 30 minutes → auto-destroy session, close WebSocket rooms
JWT:         30 minutes → expires naturally, blacklisted on burn
Periodic:    60-second cleanup sweep for expired records + cascade deletion
```

### Identity mapping deletion

When both users have joined a session, the server deletes the identity mapping:

```javascript
if (session.joinedCount >= 2 && !session.identityMappingDeleted) {
    session.identityMappingDeleted = true;
    for (const [token, info] of session.participants) {
        info.identityHash = "";    // ← gone
        info.publicKeyB64 = "";    // ← gone
    }
}
```

The server retains only the session UUID and WebSocket routing. It no longer knows who is in the session.

---

## 9. Adding a Contact — QR Exchange Flow

### What happens when Alice scans Bob's QR code

```
Bob's phone shows QrDisplayScreen
  |
  +-- QrDisplayViewModel builds the QR string
  |     payload = byteArrayOf(0x01) + publicKeyBytes
  |     qrString = "cryptika://id/v1/" + Base64UrlNoPadding.encode(payload)

Alice's phone scans on QrScanScreen
  |
  +-- CameraX frames fed to ZXing BarcodeDecoder
  |     1. Check starts with "cryptika://id/v1/"
  |     2. Decode → 33 bytes; check version byte == 0x01
  |     3. Extract 32-byte Ed25519 public key
  |     4. Navigate to CONTACT_CONFIRM screen

ContactConfirmScreen
  |
  +-- Shows Bob's identity hash for verbal verification
  +-- Input field for nickname
  +-- contactRepository.saveContact(...)
  +-- BackgroundConnectionManager.ensureConnected(conversationId)
```

The deep-link scheme `cryptika://id` is registered in AndroidManifest.xml, allowing any QR scanner app to launch Cryptika directly.

### How conversation IDs are built

```kotlin
fun buildConversationId(hashA: String, hashB: String): String {
    val sorted = listOf(hashA, hashB).sorted()
    return "${sorted[0]}_${sorted[1]}"
}
```

Both sides compute the same ID because the hashes are sorted lexicographically.

---

## 10. Networking Layer — WebSocket Architecture

### The three-tier networking stack

```
RelayWebSocketClient       ← thin OkHttp wrapper; one socket, one conversation
        |
BackgroundConnectionManager ← orchestrates all conversations; routes packets
EphemeralSessionManager     ← manages ephemeral session WebSocket lifecycle
        |
ChatViewModel / CallManager ← consume packets for delivery to UI or audio
```

### WebSocket connection modes

The server supports two WebSocket connection modes:

**1. Ephemeral session mode** (for contact-discovery-based chats):
```
GET ws://host:8443/ws?session=<sessionUUID>
Authorization: Bearer <JWT>
```
- JWT verified from the Authorization header on connect
- Participant membership checked
- 2-participant rooms only
- On disconnect: sends PEER_DISCONNECTED control frame to remaining peer, destroys session

**2. Conversation mode** (for QR-based chats):
```
ws://host:8443/ws?conv=<conversationId>&id=<identityHash>
```
- Up to 10 connections per room
- Message buffering for offline peers (max 50 messages, 1-hour TTL)
- Presence tracking

### Relay envelope framing

```
Every outgoing binary WebSocket frame:

+------------------+------------------+-------------------+--------------------+------------------+
| 2 bytes (BE)     | N bytes          | 2 bytes (BE)      | M bytes            | P bytes          |
| len(convId)      | conversationId   | len(messageId)    | messageId (UUID)   | raw packet bytes |
+------------------+------------------+-------------------+--------------------+------------------+
```

The server reads the routing fields, then blindly forwards the rest.

### Exponential backoff reconnection

```kotlin
private var backoffMs = 1_000L
private val maxBackoffMs = 30_000L

fun onFailure(...) {
    reconnectJob = reconnectScope.launch {
        delay(backoffMs)
        backoffMs = minOf(backoffMs * 2, maxBackoffMs)
        connect()
    }
}
```

Uses coroutine `delay()` which works in Android Doze mode (backed by `ScheduledExecutorService`).

---

## 11. Cryptographic Handshake — X25519 DH Step by Step

**Files: `HandshakeManager.kt`, `SessionKeyManager.kt`**

### When does the handshake happen?

The handshake runs automatically whenever:
1. A new ephemeral session WebSocket connects
2. A regular WebSocket connection is first established
3. The WebSocket reconnects after a drop (fresh ephemeral keys each time)

### Step-by-step

```
STEP 1: Generate ephemeral X25519 keypair
  ephPubBytes = 32 bytes; ephKeyPair stored temporarily

STEP 2: Build HANDSHAKE_OFFER (97 bytes)
  header    = byteArrayOf(0x01)                  // magic byte
  toSign    = SHA-256(header + ephPubBytes)       // 32 bytes
  signature = identityKeyManager.sign(toSign)     // 64 bytes Ed25519
  offer     = header + ephPubBytes + signature    // [1][32][64] = 97 bytes

STEP 3: Send offer via WebSocket → peer receives it

STEP 4: Peer's offer arrives
  Parse: magic, peerEphPub, peerSig
  Verify Ed25519 signature against PINNED public key from contacts table
  REJECT if invalid (impersonation attempt)

STEP 5: Derive session key
  sharedSecret = X25519(myEphPriv, peerEphPub)    // 32 bytes
  myEphPriv.fill(0)                                // zeroize immediately

  K0 = SHA-256(sharedSecret | id_min | id_max)

  sendRatchet = HashRatchet(SHA-256(K0 | "send"))
  recvRatchet = HashRatchet(SHA-256(K0 | "recv"))

  SessionState = ACTIVE
```

### Why fresh ephemeral keys every time?

An attacker who records traffic and later compromises one key cannot decrypt past sessions. Fresh ephemeral keys mean each session derives a fresh K0 — previous sessions remain secure.

---

## 12. Hash Ratchet — Forward Secrecy Mechanism

**File: `domain/crypto/HashRatchet.kt`**

### The chain

```
K0  (root — never used for encryption, only for seeding)
 |
SHA-256
 |
K1  → encrypt message #1  → K0.fill(0)    // K0 destroyed
 |
SHA-256
 |
K2  → encrypt message #2  → K1.fill(0)    // K1 destroyed
 |
SHA-256
 |
K3  → encrypt message #3  → K2.fill(0)    // K2 destroyed
 |
...
```

At any point, a snapshot of memory shows only the current ratchet key. Past keys are gone — SHA-256 is not invertible.

### Two separate ratchets

```
K0
├── sendRatchet = HashRatchet(SHA-256(K0 | "send"))
│     K_s1, K_s2, K_s3, ...   (outgoing message keys)
└── recvRatchet = HashRatchet(SHA-256(K0 | "recv"))
      K_r1, K_r2, K_r3, ...   (incoming message keys)
```

Both peers derive the same ratchets from the same K0. Alice's K_s1 == Bob's K_r1.

### Out-of-order message handling

```
Alice receives msg_seq=4 before msg_seq=3:

recvRatchet.advanceTo(4):
  - Compute K_r3, store in lookaheadBuffer[3] (TTL=30s)
  - Compute K_r4, use for decryption of msg #4

When msg_seq=3 arrives later:
  - Use lookaheadBuffer[3] for decryption
  - lookaheadBuffer[3].fill(0) → remove from buffer

Limits: max 50 lookahead entries, 30s TTL per entry
```

---

## 13. Sending a Message — Complete Step-by-Step Flow

### User taps Send in the chat

```
ChatViewModel.sendMessage()
  |
  +-- Generate UUID: messageId
  |
  +-- Save to DB in SENDING state (user sees ⏳)
  |
  +-- MessageProcessor.encryptMessage():
  |     1. sendRatchet.advance() → (ratchetKey, counter)
  |     2. Build JSON header: { sid, ts, ctr, exp, type: "TEXT" }
  |     3. headerHash = SHA-256(headerBytes)
  |     4. nonce = SHA-256(ratchetKey + counterBytes)[0..11]
  |     5. ciphertext = ChaCha20-Poly1305.encrypt(ratchetKey, nonce, plaintext, ad=headerHash)
  |     6. ratchetKey.fill(0)
  |     7. sigInput = SHA-256(headerBytes + ciphertext)
  |     8. signature = Ed25519.sign(sigInput)   // 64 bytes
  |     9. packet = [headerLen][header][cipherLen][ciphertext][signature]
  |
  +-- Send via WebSocket (relay envelope wrapping)
  |
  +-- Update DB: SENDING → SENT (user sees ✓)
```

### Ephemeral session default: 3-second self-destruct

For ephemeral sessions, `selectedExpirySeconds = 3` — messages self-destruct after 3 seconds by default.

---

## 14. Receiving a Message — Complete Step-by-Step Flow

```
WebSocket receives binary frame
  |
  +-- Strip relay envelope → raw packet
  |
  +-- MessageProcessor.decryptMessage():
  |     1. Parse: headerBytes, ciphertext, signature
  |     2. VERIFY Ed25519 signature against sender's pinned public key
  |        → REJECT if invalid (tampering / impersonation)
  |     3. Parse JSON header (sid, ts, ctr, exp, type)
  |     4. VERIFY timestamp: |now - ts| < 5 minutes (anti-replay)
  |     5. VERIFY counter: ctr > lastSeenCounter (anti-replay)
  |     6. Advance recvRatchet to this counter
  |     7. Derive deterministic nonce
  |     8. headerHash = SHA-256(headerBytes)
  |     9. AEAD decrypt: ChaCha20-Poly1305(ratchetKey, nonce, ciphertext, ad=headerHash)
  |        → REJECT if Poly1305 tag invalid (tampering)
  |    10. ratchetKey.fill(0)
  |    11. Update lastSeenCounter
  |
  +-- If chat is open: update StateFlow → UI
  +-- If backgrounded: save to encrypted DB silently
```

---

## 15. Wire Packet Format — Byte-Level Breakdown

### Message packet

```
Offset   Length   Field
------   ------   -----
0        4        header_length (big-endian Int32)
4        N        JSON header bytes (UTF-8)
4+N      4        ciphertext_length (big-endian Int32)
8+N      M        ChaCha20-Poly1305 ciphertext + 16-byte Poly1305 tag
8+N+M    64       Ed25519 signature of SHA-256(header + ciphertext)
```

### Handshake offer (97 bytes fixed)

```
Offset   Length   Field
------   ------   -----
0        1        0x01 (magic = handshake)
1        32       ephemeral X25519 public key
33       64       Ed25519 signature of SHA-256(bytes[0..32])
```

### Peer disconnect control frame

```
Offset   Length   Field
------   ------   -----
0        2        0xFF 0xFE (control prefix)
2        17       "PEER_DISCONNECTED" (ASCII)
```

### Call signal packet (122 bytes fixed)

```
Offset   Length   Field
------   ------   -----
0        1        0x02 (magic = call signal)
1        1        signal_type (OFFER=1, ANSWER=2, REJECT=3, HANGUP=4, BUSY=5)
2        16       call_id (random 16 bytes)
18       8        timestamp_ms (big-endian Int64)
26       32       ephemeral X25519 public key
58       64       Ed25519 signature of SHA-256(bytes[0..57])
```

### Audio frame packet (variable)

```
Offset   Length   Field
------   ------   -----
0        1        0x03 (magic = audio frame)
1        4        sequence_number (big-endian Int32)
5        variable ChaCha20-Poly1305(PCM frame) — encrypted audio + 16-byte tag
```

---

## 16. Encrypted Storage — How Messages Rest On Disk

### Double encryption at rest

Messages are encrypted twice:

**Layer 1 — SQLCipher**: The entire database is AES-256 encrypted. The passphrase is stored in Android Keystore.

**Layer 2 — Per-message Keystore keys**: Each message is additionally encrypted with its own AES-256-GCM key stored in Android Keystore (hardware-backed, non-exportable).

```
On saveMessage():
  1. Plaintext decrypted from wire packet
  2. Generate AES-256-GCM key in Keystore: alias "msg_<messageId>"
  3. Encrypt plaintext with this key → cipherBlob
  4. storageHash = SHA-256(cipherBlob)  // tamper detection
  5. Store cipherBlob + storageHash in SQLCipher DB

On getMessage():
  1. Load cipherBlob from DB
  2. VERIFY: SHA-256(cipherBlob) == storageHash  // detect tampering
  3. Decrypt with Keystore key "msg_<messageId>"
  4. Return plaintext
```

### Why this matters

| Attack | SQLCipher alone | SQLCipher + Keystore |
|--------|----------------|---------------------|
| Copy .db file off device | Blocked by SQLCipher | Blocked by both |
| Root device + read DB | Possible if key found | Blocked — Keystore keys non-exportable |
| Delete one message securely | Must overwrite row | Delete Keystore key → permanent garbage |

---

## 17. Message Expiry — Cryptographic Self-Destruction

### Three-step cryptographic destruction

```
For each expired message:

Step 1: Delete Keystore key
  androidKeyStore.deleteEntry("msg_<messageId>")
  → The AES-256-GCM decryption key is GONE
  → ciphertextBlob is now permanently unreadable noise

Step 2: Overwrite the blob in DB
  UPDATE messages SET ciphertextBlobBase64 = '' WHERE messageId = ?
  → Belt-and-suspenders

Step 3: Delete the row
  DELETE FROM messages WHERE messageId = ?
```

### Expiry timing

1. **WorkManager** — `MessageExpiryWorker` runs every 15 minutes for background expiry
2. **ChatViewModel timer** — when chat is open, schedules destruction at exact expiry time for real-time self-destruct
3. **Default for ephemeral sessions: 3 seconds**

---

## 18. Peer Disconnect — Automatic Session Destruction

When one user's connection drops (screen off, internet lost, app closed), the other user is notified and both sessions are destroyed.

### Server-side (ephemeral session WebSocket close)

```javascript
ws.on("close", () => {
    room.delete(ws);
    // Send control frame to remaining peer
    const PEER_DISCONNECTED = Buffer.from([0xFF, 0xFE, ...Buffer.from("PEER_DISCONNECTED")]);
    for (const peer of room) {
        if (peer.readyState === WebSocket.OPEN) {
            peer.send(PEER_DISCONNECTED, { binary: true });
        }
    }
    destroySession(sessionId);  // ← server destroys session
});
```

### Client-side

```
EphemeralSessionManager receives PEER_DISCONNECTED:
  |
  +-- Fire peerDisconnectedCallback
  |
  +-- destroySession()  ← cryptographic erasure
  |
  +-- ChatViewModel reacts:
        emit PeerDisconnected → show snackbar "Peer disconnected — session destroyed"
        delay(1500ms)
        emit ForceLogout → navigate to AUTH
```

### WebSocket disconnect / error also triggers destruction

Any WebSocket disconnect or error event on an ephemeral session triggers the same flow:

```kotlin
is RelayEvent.Disconnected -> {
    peerDisconnectedCallbacks[session.sessionUUID]?.invoke()
    destroySession(session.sessionUUID)
}
is RelayEvent.Error -> {
    peerDisconnectedCallbacks[session.sessionUUID]?.invoke()
    destroySession(session.sessionUUID)
}
```

**Internet gone = both sides wiped.**

---

## 19. Auto-Logout — Screen Off, Back, Home, Minimize

Cryptika enforces that the user must re-enter their username every time they return to the app. Any form of leaving the app triggers a full wipe.

### Screen-off wipe

```kotlin
// MainActivity — BroadcastReceiver for ACTION_SCREEN_OFF
private val screenOffReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            wipeScope.launch {
                withContext(Dispatchers.IO) {
                    ephemeralSessionManager.destroyAllSessions()
                    backgroundConnectionManager.stopAll()
                }
                authRepository.logout()
                // Restart activity → AUTH screen
                startActivity(Intent(...).addFlags(NEW_TASK | CLEAR_TASK))
                finish()
            }
        }
    }
}
```

### Back / Home / Minimize wipe

```kotlin
// MainActivity.onStop() — fires when activity is no longer visible
override fun onStop() {
    super.onStop()
    if (!isChangingConfigurations) {  // skip during screen rotation
        wipeScope.launch {
            withContext(Dispatchers.IO) {
                ephemeralSessionManager.destroyAllSessions()
                backgroundConnectionManager.stopAll()
            }
            authRepository.logout()
            startActivity(Intent(...).addFlags(NEW_TASK | CLEAR_TASK))
            finish()
        }
    }
}
```

### What gets destroyed

1. All ephemeral sessions → cryptographic erasure (keys, messages, contacts)
2. All background WebSocket connections → stopped
3. Auth state → cleared (JWT, contactToken, username wiped from EncryptedSharedPreferences)
4. Activity → restarted, navigates to AUTH screen
5. User must enter username again to use the app

---

## 20. Per-Chat Screenshot Blocking

**File: `ChatScreen.kt`**

```kotlin
DisposableEffect(Unit) {
    val activity = context as? android.app.Activity
    val prefs = context.getSharedPreferences("cryptika_settings", Context.MODE_PRIVATE)
    val blockingEnabled = prefs.getBoolean("screenshot_blocking", true)
    if (blockingEnabled && activity != null) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    onDispose {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
```

- `FLAG_SECURE` is applied **only while inside a chat screen**
- Cleared when leaving the chat
- Controlled by settings (default: enabled)
- Prevents screenshots, screen recording, and app preview in recent apps

---

## 21. Encrypted Voice Calls — Full Protocol

**Files: `CallManager.kt`, `CallModels.kt`, `ViewModels.kt` (CallViewModel), `CallScreen.kt`**

### Call state machine

```
        IDLE
          |
  Alice taps Call
          |
  OUTGOING_RINGING ──→ [60s timeout] ──→ auto-cancel
          |
  Bob receives OFFER
          |
  INCOMING_RINGING (Bob)
          |
  Bob taps Answer
          |
  ACTIVE (bidirectional encrypted audio)
          |
  Either taps Hangup
          |
        ENDED
```

### Key exchange for calls

```
Alice sends CALL_SIGNAL-OFFER (122 bytes):
  [0x02][OFFER][call_id][timestamp][aliceEphPub][signature]

Bob sends CALL_SIGNAL-ANSWER (122 bytes):
  [0x02][ANSWER][call_id][timestamp][bobEphPub][signature]

Both sides:
  sharedSecret = X25519(myEphPriv, peerEphPub)
  callerEncKey = SHA-256(sharedSecret | "caller_send" | call_id)
  calleeEncKey = SHA-256(sharedSecret | "callee_send" | call_id)
```

### Audio streaming

```
AudioRecord: 16kHz, mono, PCM 16-bit
  → Read 160-sample frames (320 bytes)
  → ChaCha20-Poly1305 encrypt with direction-specific key
  → Send as AUDIO_FRAME packet over WebSocket

AudioTrack: 16kHz, mono, PCM 16-bit
  → Receive AUDIO_FRAME
  → Decrypt ChaCha20-Poly1305
  → Write to AudioTrack

Watchdog: if no audio received for 15 seconds → auto-hangup
```

### Why direction-specific keys

```
Alice sends frame #1: nonce = SHA-256(callerEncKey | 1)[0..12]
Bob sends frame #1:   nonce = SHA-256(calleeEncKey | 1)[0..12]

callerEncKey ≠ calleeEncKey → nonces always different
```

Without this, both sides using the same sequence number under the same key would produce nonce collision — catastrophic for stream ciphers.

### Foreground service

`CallForegroundService` with `foregroundServiceType="microphone"` keeps `AudioRecord` alive when the app is backgrounded.

---

## 22. Background Operation — Doze, Reconnect, Services

### Foreground services

```
ConnectionForegroundService
  - foregroundServiceType="remoteMessaging"
  - IMPORTANCE_MIN notification (silent)
  - Keeps process alive for WebSocket connections

CallForegroundService
  - foregroundServiceType="microphone"
  - Shows "Call in progress" notification
  - Keeps AudioRecord alive in background
```

### Doze-safe reconnection

```kotlin
// Kotlin coroutine delay works in Doze (uses ScheduledExecutorService)
reconnectScope.launch {
    delay(backoffMs)
    reconnect()
}
```

### Network change detection

```kotlin
connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
    override fun onAvailable(network: Network) {
        reconnectAll()  // re-establish all WebSocket connections
    }
})
```

---

## 23. Blind Relay Server — Internal Architecture

**File: `server/index.js`**

The server is designed to be **cryptographically blind**: it has no ability to read message content, no persistent storage, and minimal logging.

### Blind properties

- **No database**: All state is in-memory JavaScript Maps
- **No disk writes**: Nothing is ever written to disk
- **No logging**: All error handlers use `/* blind: no logging */`
- **`safeLog()`**: The few operational logs truncate identifiers
- **Auto-purge**: All data has TTLs and is swept every 60 seconds
- **Identity erasure**: After both users join, identity hashes are deleted from the session

### In-memory data structures

```javascript
const users = new Map()               // token → { username, identityHash, ... }
const contactRequests = new Map()      // requestId → { from, to, ... }
const ephemeralSessions = new Map()    // sessionUUID → { participants, ws rooms }
const conversationSockets = new Map()  // convId → Set<WebSocket>
const offlineBuffer = new Map()        // convId → Array<{data, ts}>
const presenceMap = new Map()          // hash → { online, lastSeen }
const jwtBlacklist = new Set()         // burned JWTs
```

### Cleanup cycle (every 60 seconds)

```javascript
setInterval(() => {
    // 1. Evict expired offline buffer entries (>1 hour)
    // 2. Evict stale presence entries (>5 minutes)
    // 3. Evict empty conversation rooms
    // 4. Evict expired user records (>30 min TTL)
    // 5. Cascade delete orphaned contact requests
    // 6. Evict expired sessions (>30 min TTL)
}, 60_000)
```

### Server startup message

```
Server is BLIND — no passwords, no logs, only ciphertext relay
```

---

## 24. Server REST API Reference

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/health` | GET | None | Server health check + stats |
| `/api/v1/auth/enter` | POST | None | Passwordless entry → JWT (30m) |
| `/api/v1/auth/burn` | POST | JWT | Burn credentials from server |
| `/api/v1/contact/request` | POST | JWT | Send contact request by username |
| `/api/v1/contact/requests` | GET | JWT | List pending incoming requests |
| `/api/v1/contact/accept` | POST | JWT | Accept request → create session |
| `/api/v1/contact/reject` | POST | JWT | Reject a contact request |
| `/api/v1/contact/accepted` | GET | JWT | Poll for accepted sessions (requester) |
| `/api/v1/ticket` | POST | None | Ed25519-signed session ticket |
| `/api/v1/presence` | POST | None | Update online presence |
| `/api/v1/presence/:hash` | GET | None | Check peer online status |
| `/ws` | WebSocket | Query | Binary relay (session or conversation mode) |

### Rate limiting

- `POST /api/v1/auth/enter` — 10 per IP per minute
- `POST /api/v1/contact/request` — 10 per IP per minute, 20 per user per day

---

## 25. Dependency Injection — How Hilt Wires Everything

**File: `di/AppModule.kt`**

### Singleton scope — one instance for the entire app

```
AppModule provides:
  IdentityKeyManager, KeystoreManager, HashRatchetFactory,
  MessageProcessor, AppDatabase, RelayApi, ServerConfig,
  BackgroundConnectionManager, EphemeralSessionManager,
  CallManager, AuthStore, AuthRepository, all Repositories
```

### How a ViewModel gets dependencies

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val identityKeyManager: IdentityKeyManager,
    private val serverConfig: ServerConfig,
    private val backgroundConnectionManager: BackgroundConnectionManager,
    private val ephemeralSessionManager: EphemeralSessionManager,
    // ... all injected by Hilt, no manual instantiation
) : ViewModel()
```

---

## 26. Database Schema — Tables, Migrations, Indexes

**File: `data/local/db/AppDatabase.kt`**

The database is encrypted with SQLCipher (AES-256), passphrase stored in Android Keystore.

### `contacts` table

```sql
CREATE TABLE contacts (
    identityHashHex TEXT NOT NULL PRIMARY KEY,
    publicKeyBase64 TEXT NOT NULL,
    nickname        TEXT NOT NULL,
    addedAt         INTEGER NOT NULL
);
```

### `conversations` table

```sql
CREATE TABLE conversations (
    conversationId       TEXT NOT NULL PRIMARY KEY,
    contactIdentityHash  TEXT NOT NULL,
    lastMessagePreview   TEXT NOT NULL DEFAULT '',
    lastMessageTs        INTEGER NOT NULL DEFAULT 0,
    unreadCount          INTEGER NOT NULL DEFAULT 0
);
```

### `messages` table

```sql
CREATE TABLE messages (
    messageId            TEXT NOT NULL PRIMARY KEY,
    conversationId       TEXT NOT NULL,
    senderHash           TEXT NOT NULL,
    ciphertextBlobBase64 TEXT NOT NULL,
    storageHashHex       TEXT NOT NULL DEFAULT '',
    counter              INTEGER NOT NULL,
    timestampMs          INTEGER NOT NULL,
    expiryMs             INTEGER NOT NULL DEFAULT 0,
    messageState         TEXT NOT NULL DEFAULT 'DELIVERED',
    isOutgoing           INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_messages_convId ON messages(conversationId);
CREATE INDEX idx_messages_expiry ON messages(expiryMs);
CREATE INDEX idx_messages_counter ON messages(counter);
```

### `local_identity` table

```sql
CREATE TABLE local_identity (
    id              INTEGER NOT NULL PRIMARY KEY,
    publicKeyBase64 TEXT NOT NULL,
    identityHashHex TEXT NOT NULL
);
```

---

## 27. All Source Files Explained

### Application layer

| File | Purpose |
|------|---------|
| `CryptikaApp.kt` | `@HiltAndroidApp` application class. Starts foreground service + all connections. |
| `MainActivity.kt` | Single-Activity host. Navigation graph. Screen-off BroadcastReceiver. `onStop()` auto-logout. |

### Dependency injection

| File | Purpose |
|------|---------|
| `di/AppModule.kt` | Hilt `@Module` — provides all singletons. |

### Domain — crypto

| File | Purpose |
|------|---------|
| `domain/crypto/IdentityKeyManager.kt` | Ed25519 keypair; Keystore wrapping; sign/verify. |
| `domain/crypto/SessionKeyManager.kt` | X25519 ephemeral keypair; DH shared secret; K₀ derivation. |
| `domain/crypto/HandshakeManager.kt` | 97-byte signed offer; signature verification; DH completion. |
| `domain/crypto/HashRatchet.kt` | SHA-256 ratchet chain; lookahead buffer; key zeroization. |
| `domain/crypto/AEADCipher.kt` | ChaCha20-Poly1305 AEAD wrapper; deterministic nonce derivation. |
| `domain/crypto/TicketManager.kt` | 140-byte ticket verification; server public key pinning. |
| `domain/crypto/MessageProcessor.kt` | Full encrypt/decrypt pipeline (sign→encrypt→send / verify→decrypt). |
| `domain/crypto/IdentityHash.kt` | SHA-256 identity hash computation. |

### Domain — models

| File | Purpose |
|------|---------|
| `domain/model/DomainModels.kt` | `Contact`, `Message`, `Conversation`, `MessageState`, `ConnectionState`, `EphemeralSessionState`, etc. |
| `domain/model/CallModels.kt` | `CallState` sealed class, `CallSignalType` enum, `IncomingCallData`. |

### Domain — repository interfaces

| File | Purpose |
|------|---------|
| `domain/repository/AuthRepository.kt` | Auth interface: enter, burn, send/accept/reject request. |
| `domain/repository/Repositories.kt` | `ContactRepository`, `MessageRepository`, `IdentityRepository`, `ConversationRepository`. |

### Data — local

| File | Purpose |
|------|---------|
| `data/local/AuthStore.kt` | EncryptedSharedPreferences — JWT, contactToken, username storage. |
| `data/local/db/AppDatabase.kt` | Room + SQLCipher DB; entities, DAOs, migrations. |
| `data/local/keystore/KeystoreManager.kt` | Android Keystore operations: per-message encryption, DB passphrase. |

### Data — remote

| File | Purpose |
|------|---------|
| `data/remote/api/AuthApi.kt` | Retrofit API interfaces + all DTOs (EnterRequest, AcceptRequestResponse, etc.). |
| `data/remote/BackgroundConnectionManager.kt` | Global WebSocket connection manager for QR-based conversations. |
| `data/remote/EphemeralSessionManager.kt` | Ephemeral session lifecycle: join, handshake, send, destroy, burn. |
| `data/remote/CallManager.kt` | Encrypted voice call protocol: signals, key exchange, audio loops. |
| `data/remote/ConnectionForegroundService.kt` | Foreground service for WebSocket persistence. |
| `data/remote/CallForegroundService.kt` | Foreground service with microphone for voice calls. |
| `data/remote/ServerConfig.kt` | Server URL configuration singleton. |
| `data/remote/websocket/RelayWebSocketClient.kt` | OkHttp WebSocket wrapper; relay envelope framing; exponential backoff. |

### Data — repository

| File | Purpose |
|------|---------|
| `data/repository/AuthRepositoryImpl.kt` | Auth + contact API implementation; credential burn logic. |
| `data/repository/RepositoryImpl.kt` | Contact, message, conversation, identity repositories; per-message encryption/tamper-check. |

### Presentation — UI components

| File | Purpose |
|------|---------|
| `presentation/ui/components/SecureKeyboard.kt` | Custom in-app keyboard (LOWER/UPPER/SYMBOLS); blocks system IME. |
| `presentation/ui/components/SecureInputField.kt` | Input field companion; intercepts press to toggle keyboard without system IME. |

### Presentation — screens

| File | Screens |
|------|---------|
| `presentation/ui/screens/AuthScreens.kt` | `AuthScreen` (username entry), `ContactDiscoveryScreen` (add contact by username), Contact Setup Dialog (fingerprint verification). |
| `presentation/ui/screens/SplashAndHomeScreens.kt` | `SplashScreen` (identity check), `HomeScreen` (conversation list). |
| `presentation/ui/screens/ChatScreen.kt` | Chat UI: message list, secure keyboard input, message bubbles, ephemeral countdown, connection state indicator, per-chat FLAG_SECURE. |
| `presentation/ui/screens/CallScreen.kt` | Voice call UI: caller info, state display, mute/speaker/hangup. |
| `presentation/ui/screens/QrScanAndSettingsScreens.kt` | `QrDisplayScreen`, `QrScanScreen`, `ContactConfirmScreen`, `SettingsScreen`. |
| `presentation/ui/theme/Theme.kt` | `CryptikaTheme` — Material 3 dark-mode-optimized theme. |

### Presentation — ViewModels

| ViewModel | Class | Purpose |
|-----------|-------|---------|
| `AuthViewModel` | `AuthViewModel.kt` | Passwordless entry flow. |
| `ContactDiscoveryViewModel` | `AuthViewModel.kt` | Contact request, accept, reject, poll, credential setup. |
| `SplashViewModel` | `ViewModels.kt` | Identity initialization check. |
| `HomeViewModel` | `ViewModels.kt` | Conversation list, badge counts. |
| `ContactConfirmViewModel` | `ViewModels.kt` | QR-scanned contact save + key change detection. |
| `ChatViewModel` | `ViewModels.kt` | Full chat: send, receive, retry, expiry, delete-for-both, ephemeral countdown. |
| `SettingsViewModel` | `ViewModels.kt` | Screenshot toggle, server URL, identity regeneration. |
| `CallViewModel` | `ViewModels.kt` | Voice call state machine, mute/speaker, duration. |

### Worker

| File | Purpose |
|------|---------|
| `worker/MessageExpiryWorker.kt` | WorkManager periodic task (15 min) — background message expiry. |

### Server

| File | Purpose |
|------|---------|
| `server/index.js` | Complete blind relay server — REST API + WebSocket relay. Single file. |
| `server/package.json` | Node.js dependencies (express, ws, tweetnacl, jsonwebtoken, etc.). |
| `server/Dockerfile` | Docker build: node:20-alpine, production, port 8443, healthcheck. |
| `server/DOCKER.md` | Full deployment guide. |

---

## 28. Complete Data Flow: Alice Sends Bob a Message

This traces the full end-to-end path for an ephemeral session message.

```
ALICE'S DEVICE
==============

1. Alice enters username via SecureKeyboard → POST /api/v1/auth/enter → JWT
2. Alice navigates to Contact Discovery, enters Bob's username
3. POST /api/v1/contact/request (with random nickname "User_a3f8b2c1")
4. Bob accepts. Alice polls → finds accepted session.
5. Contact Setup Dialog: "Anonymous" + fingerprint. Alice taps "Confirm & Chat"
6. EphemeralSessionManager.joinSession():
   - Save ephemeral contact with random display name
   - Open WebSocket: `ws://host:8443/ws?session=<UUID>` with `Authorization: Bearer <JWT>`
   - X25519 DH handshake → session key K0
   - Create send/recv HashRatchets
7. Alice types "Hello" via SecureKeyboard, taps Send
8. MessageProcessor encrypts:
   - Ratchet advance → K_1
   - ChaCha20-Poly1305 encrypt
   - Ed25519 sign
   - Wire packet assembled
9. triggerBurnOnFirstMessage() → POST /api/v1/auth/burn
   Server deletes Alice's user record. Alice is now a ghost.

RELAY SERVER (BLIND)
====================

10. Server receives binary blob on session WebSocket
11. Forwards to all other sockets in the session room → Bob
12. Server has NO idea what the blob contains

BOB'S DEVICE
=============

13. Bob's WebSocket receives the frame
14. MessageProcessor decrypts:
    - Verify Ed25519 signature
    - Verify timestamp, verify counter
    - Advance recv ratchet → K_r1
    - ChaCha20-Poly1305 decrypt → "Hello"
15. Message saved to encrypted DB → displayed in UI
16. Ephemeral expiry: 3 seconds later, message self-destructs:
    - Keystore key deleted → ciphertext is permanent garbage
    - DB row overwritten and deleted
```

---

## 29. Threat Model and Security Analysis

### What Cryptika protects against

| Threat | Protection |
|--------|-----------|
| Server compromise | All messages are E2E encrypted. Server sees only opaque blobs. |
| Server logging | Server writes no logs. All state is in-memory with auto-purge. |
| Mass surveillance | No accounts, no phone numbers. Username-only entry. |
| Traffic analysis | Binary WebSocket frames; session UUIDs are random. |
| Message replay | Timestamp + monotonic counter verification on every message. |
| Message tampering | Every message is Ed25519 signed by the sender's identity key. |
| Ratchet key compromise | SHA-256 hash ratchet — compromising Kn reveals nothing about K1..Kn-1. |
| Device seizure | Per-message Keystore keys (hardware-backed, non-exportable). Auto-wipe on screen off. |
| Keystroke logging | Custom SecureKeyboard; system IME completely blocked. |
| Screenshot capture | FLAG_SECURE applied per-chat; blocks screenshots, screen recording, and recent-apps preview. |
| Post-session forensics | Cryptographic erasure: all keys zeroized, messages deleted, contacts removed. |
| Username enumeration | Contact request API always returns same response regardless of whether target exists. |
| Credential theft | Server credentials burned after first message. 30-minute JWT TTL. |
| Nonce reuse (calls) | Direction-specific encryption keys prevent nonce collision even with same sequence numbers. |

### What Cryptika does NOT protect against

| Threat | Reason |
|--------|--------|
| Physical access while app is open | If someone has the phone unlocked and Cryptika is in the foreground, they can read messages. |
| Compromised device OS | A rooted device with a compromised kernel can bypass Keystore protections. |
| Shoulder surfing | Someone looking at the screen can read messages. |
| Connection timing analysis | The server can observe when connections are made (but not the content or real identity). |

---

## 30. Android Permissions Explained

| Permission | Purpose |
|------------|---------|
| `INTERNET` | WebSocket connections to relay server |
| `ACCESS_NETWORK_STATE` | Detect connectivity changes for reconnection |
| `CAMERA` | QR code scanning (CameraX) |
| `WAKE_LOCK` | WorkManager — keep CPU awake for expiry worker |
| `RECEIVE_BOOT_COMPLETED` | WorkManager — reschedule after reboot |
| `VIBRATE` | Haptic feedback on SecureKeyboard |
| `FOREGROUND_SERVICE` | Background WebSocket persistence |
| `FOREGROUND_SERVICE_REMOTE_MESSAGING` | Messaging foreground service type |
| `FOREGROUND_SERVICE_MICROPHONE` | Voice call foreground service type |
| `POST_NOTIFICATIONS` | Required for API 33+ foreground service notifications |
| `RECORD_AUDIO` | Voice calls (AudioRecord) |
| `BLUETOOTH_CONNECT` | Audio routing to Bluetooth headsets during calls |
| `MODIFY_AUDIO_SETTINGS` | Set audio mode for voice calls |

### Activity configuration

```xml
android:windowSoftInputMode="stateAlwaysHidden|adjustResize"
android:screenOrientation="portrait"
```

`stateAlwaysHidden` prevents the system keyboard from ever appearing — all input goes through the SecureKeyboard.

---

## 31. Tech Stack Reference

### Android app

| Category | Library | Version |
|----------|---------|---------|
| Language | Kotlin | 1.9.22 |
| UI | Jetpack Compose | BOM 2024.02.00 |
| Design | Material 3 | latest |
| DI | Hilt | 2.51 |
| Database | Room | 2.6.1 |
| DB Encryption | SQLCipher | 4.5.4 |
| Secure Prefs | AndroidX Security-Crypto | 1.1.0-alpha06 |
| Networking | OkHttp | 4.12.0 |
| REST | Retrofit | 2.9.0 |
| Crypto | BouncyCastle | 1.70 |
| QR | ZXing | 4.3.0 |
| Camera | CameraX | 1.3.1 |
| Background | WorkManager | 2.9.0 |
| Min SDK | 26 | Android 8.0 |
| Target SDK | 34 | Android 14 |

### Server

| Category | Library |
|----------|---------|
| Runtime | Node.js 20 (Alpine Docker) |
| HTTP | Express |
| WebSocket | ws |
| Crypto | tweetnacl (Ed25519) |
| Auth | jsonwebtoken (JWT) |
| Security | helmet (HTTP headers) |
| IDs | uuid |

---

## 32. Building, Running, and Configuring

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Node.js 20+ (for server)
- Docker (optional, for server deployment)

### Building the Android app

```bash
# Clone the repository
git clone <repo-url>
cd HackSecureMessenger

# Build debug APK
./gradlew assembleDebug

# The APK is at: app/build/outputs/apk/debug/app-debug.apk
```

### Running the server locally

```bash
cd server
npm install
node index.js
# Server starts on port 8443
```

### Server environment variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `PORT` | No | Server port (default: 8443) |
| `HMAC_SECRET_HEX` | Yes | 64-char hex — HMAC key for contact tokens |
| `JWT_SECRET_HEX` | Yes | 64-char hex — JWT signing secret |
| `SERVER_PRIVATE_KEY_HEX` | Yes | 64-char hex — Ed25519 private key for session tickets |

### Configuring the Android app

In `app/build.gradle.kts`, update the server connection details:

```kotlin
buildConfigField("String", "RELAY_BASE_URL", "\"http://YOUR_SERVER_IP:8443\"")
buildConfigField("String", "SERVER_PUBLIC_KEY_HEX", "\"YOUR_SERVER_PUBLIC_KEY_HEX\"")
```

The server public key is printed to the console on first startup when it generates a new keypair.

---

## 33. Docker Deployment

### Quick start

```bash
cd server
docker build -t cryptika-server .
docker run -d -p 8443:8443 --name cryptika cryptika-server
```

### Docker Compose (recommended)

```yaml
# docker-compose.yml
services:
  cryptika-server:
    build: ./server
    ports:
      - "8443:8443"
    environment:
      - PORT=8443
      - HMAC_SECRET_HEX=<your_64_char_hex>
      - JWT_SECRET_HEX=<your_64_char_hex>
      - SERVER_PRIVATE_KEY_HEX=<your_64_char_hex>
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8443/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

```bash
docker compose up -d
docker compose logs -f    # view logs
```

### Key persistence

**Important**: If you don't set `HMAC_SECRET_HEX`, `JWT_SECRET_HEX`, and `SERVER_PRIVATE_KEY_HEX` as environment variables, the server generates new random keys on every restart. This means:
- All existing JWTs become invalid
- The server public key changes (app must be updated)
- All active sessions are lost

Always set these in your `docker-compose.yml` environment section for production.

---

## 34. Troubleshooting

### "No Server" / Connection failed

1. Check server is running: `curl http://YOUR_SERVER:8443/health`
2. Verify `RELAY_BASE_URL` in `app/build.gradle.kts` matches the server
3. Verify `SERVER_PUBLIC_KEY_HEX` matches the server's actual public key
4. Check firewall allows port 8443

### App requires re-login on every open

This is **by design**. Cryptika auto-logs out on:
- Screen off
- Back button
- Home button
- Minimize
- App switch
- Internet loss (for ephemeral sessions)

### Messages disappearing quickly

Ephemeral sessions default to **3-second message expiry**. You can change the timer before sending.

### "Peer disconnected" notification

This appears when your chat partner's connection drops (screen off, internet lost, app closed). Both sessions are wiped. This is by design — ephemeral sessions don't survive disconnection.

---

## 35. License

MIT License

Copyright (c) 2024 Cryptika

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.