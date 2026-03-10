<div align="center">

# Cryptika — Complete Technical Reference

**A self-hosted, end-to-end encrypted Android messenger with encrypted voice calls,  
self-destructing messages, and a cryptographically blind relay server.**

No accounts. No phone numbers. No metadata beyond connection timing.  
Identity is a locally generated Ed25519 keypair — nothing else.

![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-blue)
![Crypto](https://img.shields.io/badge/Crypto-Ed25519%20%7C%20X25519%20%7C%20ChaCha20--Poly1305-red)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Self--Hosted](https://img.shields.io/badge/Server-Self--Hosted-orange)

</div>

---

## Table of Contents

1. [What Is Cryptika](#1-what-is-cryptika)
2. [High-Level System Diagram](#2-high-level-system-diagram)
3. [How The App Starts — First Launch Flow](#3-how-the-app-starts--first-launch-flow)
4. [Identity System — Ed25519 Deep Dive](#4-identity-system--ed25519-deep-dive)
5. [Adding a Contact — QR Exchange Flow](#5-adding-a-contact--qr-exchange-flow)
6. [Networking Layer — WebSocket Architecture](#6-networking-layer--websocket-architecture)
7. [The Session Ticket — Kerberos-Style Auth](#7-the-session-ticket--kerberos-style-auth)
8. [Cryptographic Handshake — X25519 DH Step by Step](#8-cryptographic-handshake--x25519-dh-step-by-step)
9. [Hash Ratchet — Forward Secrecy Mechanism](#9-hash-ratchet--forward-secrecy-mechanism)
10. [Sending a Message — Complete Step-by-Step Flow](#10-sending-a-message--complete-step-by-step-flow)
11. [Receiving a Message — Complete Step-by-Step Flow](#11-receiving-a-message--complete-step-by-step-flow)
12. [Wire Packet Format — Byte-Level Breakdown](#12-wire-packet-format--byte-level-breakdown)
13. [Encrypted Storage — How Messages Rest On Disk](#13-encrypted-storage--how-messages-rest-on-disk)
14. [Message Expiry — Cryptographic Self-Destruction](#14-message-expiry--cryptographic-self-destruction)
15. [Delete For Both Sides — How It Works](#15-delete-for-both-sides--how-it-works)
16. [Encrypted Voice Calls — Full Protocol](#16-encrypted-voice-calls--full-protocol)
17. [Background Operation — Doze, Reconnect, Services](#17-background-operation--doze-reconnect-services)
18. [Relay Server — Internal Architecture](#18-relay-server--internal-architecture)
19. [Dependency Injection — How Hilt Wires Everything](#19-dependency-injection--how-hilt-wires-everything)
20. [Database Schema — Tables, Migrations, Indexes](#20-database-schema--tables-migrations-indexes)
21. [All Source Files Explained](#21-all-source-files-explained)
22. [Complete Data Flow: Alice Sends Bob a Message](#22-complete-data-flow-alice-sends-bob-a-message)
23. [Threat Model and Security Analysis](#23-threat-model-and-security-analysis)
24. [Cryptographic Algorithm Rationale](#24-cryptographic-algorithm-rationale)
25. [Android Permissions Explained](#25-android-permissions-explained)
26. [Tech Stack Reference](#26-tech-stack-reference)
27. [Building, Running, and Configuring](#27-building-running-and-configuring)
28. [Docker Deployment](#28-docker-deployment)
29. [Troubleshooting](#29-troubleshooting)
30. [FAQ](#30-faq)
31. [Future Roadmap](#31-future-roadmap)
32. [License](#32-license)

---

## 1. What Is Cryptika

Cryptika is a fully open-source, self-hostable secure messenger for Android. It is designed around one core principle: **the relay server must never be trusted**.

Even if the relay server is completely compromised — captured by an adversary, subpoenaed, or running malicious code — it cannot read any message content, discover who is talking to whom (beyond connection metadata), or forge messages. Every security property holds under full server compromise.

### The core design decisions

| Decision | Reason |
|----------|--------|
| No accounts, no usernames | An account system requires a server to know your identity. Cryptika eliminates this entirely. |
| Identity = Ed25519 keypair | Your identity is mathematically provable, never stored centrally, and lost only if you lose your device. |
| In-person QR contact exchange | Trust-on-first-use security requires physical verification. There is no central key server to ask. |
| Relay server is "blind" | The server routes opaque binary blobs. It never has access to decryption keys. |
| Hash ratchet per session | Each message uses a unique key derived by a one-way SHA-256 chain, providing forward secrecy. |
| Per-message Android Keystore keys | Even with full access to the SQLite database file, messages are unreadable without the Keystore keys, which are hardware-backed. |
| Self-destructing messages | Key deletion makes ciphertexts permanently undecryptable — not just deleted rows. |

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
                    |                  |
                    | Routes packets   |
                    | by conv ID only. |
                    | Cannot read      |
                    | content.         |
                    |                  |
                    | Buffers msgs for |
                    | offline peers.   |
                    +------------------+

Everything inside the WebSocket frames is encrypted.
The relay server sees binary blobs + conversation IDs (hashes).
```

### What Alice's phone contains

```
Android Keystore (hardware-backed)
  - cryptika_identity_wrapping_key     AES-256-GCM  (wraps identity private key)
  - cryptika_db_passphrase_key         AES-256-GCM  (wraps SQLCipher passphrase)
  - msg_<uuid_1>                       AES-256-GCM  (wraps message #1 blob)
  - msg_<uuid_2>                       AES-256-GCM  (wraps message #2 blob)
  - ... one key per stored message ...

SharedPreferences: cryptika_identity_prefs
  - identity_public_key_base64         (plaintext, it's public)
  - identity_private_key_encrypted     (AES-256-GCM blob, key in Keystore)
  - identity_hash_hex                  SHA-256(public_key) as hex

SharedPreferences: cryptika_db_prefs
  - db_passphrase_encrypted            (AES-256-GCM blob, key in Keystore)

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
  |     Android API requires startForegroundService() for a service that
  |     will call startForeground(). The service is started immediately
  |     so the "messaging is active" notification appears within 5 seconds
  |     (Android requirement).
  |
  +-- BackgroundConnectionManager.startAllConnections()
        Queries the local database for all known contacts,
        and for each one calls ensureConnected(conversationId).
        This ensures WebSocket connections are live before any UI is shown.
```

### MainActivity startup

```
MainActivity.onCreate()
  |
  +-- Check SharedPreferences "cryptika_settings" for screenshots_blocked
  |     If true: window.setFlags(FLAG_SECURE, FLAG_SECURE)
  |
  +-- setContent { CryptikaNavGraph() }
        NavController is created.
        Start destination = SPLASH route.

SplashViewModel (launched on SPLASH screen)
  |
  +-- identityKeyManager.hasIdentity()  [checks SharedPrefs]
  |     |
  |     +-- false (first launch):
  |     |     identityKeyManager.generateIdentity()
  |     |       1. Generate Ed25519 keypair via BouncyCastle
  |     |       2. Encrypt private key seed with Keystore AES-256-GCM
  |     |       3. Store encrypted blob in cryptika_identity_prefs
  |     |       4. Store public key in plain text
  |     |       5. Compute and store identity hash = SHA-256(pubkey)
  |     |     Insert row into local_identity table
  |     |     Navigate to HOME
  |     |
  |     +-- true (returning launch):
  |           Load existing identity from prefs
  |           Navigate to HOME
```

---

## 4. Identity System — Ed25519 Deep Dive

**File: `domain/crypto/IdentityKeyManager.kt`**

### Why Ed25519

Ed25519 signatures are deterministic (no random nonce per signing operation). This is critical: ECDSA with a weak random number generator has historically led to private key recovery (the PlayStation 3 hack, the Bitcoin vanity address attacks). Ed25519 has no such vulnerability.

Ed25519 also has 32-byte keys and 64-byte signatures — compact, fast, and well-audited.

### Key generation

```kotlin
// BouncyCastle Ed25519 keypair generation
val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider())
keyPairGenerator.initialize(EdDSAParameterSpec(EdDSANamedCurveTable.getByName("Ed25519")))
val keyPair = keyPairGenerator.generateKeyPair()

val publicKeyBytes  = keyPair.public.encoded   // 32 bytes (raw public key)
val privateKeySeed  = keyPair.private.encoded  // 32 bytes (seed, not expanded key)
```

### Private key protection

The 32-byte private key seed is **immediately** wrapped:

```
1. Create an AES-256-GCM key in Android Keystore:
   KeyGenParameterSpec.Builder("cryptika_identity_wrapping_key", PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
     .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
     .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
     .setKeySize(256)
     .build()

2. Generate a random IV (12 bytes from SecureRandom)

3. Encrypt the private key seed:
   cipher = Cipher.getInstance("AES/GCM/NoPadding")
   cipher.init(Cipher.ENCRYPT_MODE, keystoreKey, GCMParameterSpec(128, iv))
   encryptedSeed = cipher.doFinal(privateKeySeed)

4. Store in SharedPreferences:
   "identity_private_key_encrypted" = Base64(iv + encryptedSeed)

5. Call fill(0) on privateKeySeed ByteArray immediately
```

After step 5, the raw private key bytes exist nowhere in the app's memory or storage. They only materialize transiently inside `sign()` for the exact duration of one signing operation.

### Signing a message

```
sign(data: ByteArray): ByteArray
  |
  +-- Load "identity_private_key_encrypted" from SharedPrefs → Base64 decode → iv + blob
  |
  +-- Keystore.getKey("cryptika_identity_wrapping_key")
  |
  +-- Cipher.init(DECRYPT_MODE, key, GCMParameterSpec(128, iv))
  |
  +-- privateKeySeed = cipher.doFinal(blob)   ← raw bytes now in memory
  |
  +-- Reconstruct BouncyCastle Ed25519 private key from seed
  |
  +-- signature = Ed25519Signer.sign(privateKeySeed, data)
  |
  +-- privateKeySeed.fill(0)   ← wipe from memory immediately
  |
  +-- return signature  (64 bytes)
```

### Identity fingerprint

```
identityHash = SHA-256(publicKeyBytes)   // 32 bytes
displayed as: AABBCCDD EEFF0011 22334455 66778899 AABBCCDD EEFF0011 22334455 66778899
              (8 groups of 8 hex chars, for manual verification if desired)
```

### QR code format

```
QR text:   "cryptika://id/v1/<BASE64URL_NO_PADDING>"

Binary:    byte[0]   = 0x01  (version byte, allows future format changes)
           bytes[1..32] = Ed25519 public key  (32 bytes)

Total binary payload: 33 bytes → after Base64URL → 44 characters
Full QR string: "cryptika://id/v1/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                                                                   ↑ 44 chars
```

The deep-link scheme `cryptika://id/v1/` is registered in AndroidManifest.xml so that if the QR is scanned by any QR app (not just Cryptika's scanner), it can launch the app and auto-import the contact.

---

## 5. Adding a Contact — QR Exchange Flow

### What happens when Alice scans Bob's QR code

```
Bob's phone shows QrDisplayScreen
  |
  +-- QrDisplayViewModel builds the QR string
  |     publicKey = identityKeyManager.getPublicKeyBytes()
  |     payload = byteArrayOf(0x01) + publicKey
  |     qrString = "cryptika://id/v1/" + Base64UrlNoPadding.encode(payload)
  |     Shows QR code via ZXing BarcodeEncoder

Alice's phone scans on QrScanScreen
  |
  +-- CameraX ImageProxy frames fed to ZXing BarcodeDecoder
  |     On scan: rawValue = "cryptika://id/v1/<base64>"
  |
  +-- QrScanViewModel.onQrScanned(rawValue)
  |     1. Check starts with "cryptika://id/v1/"
  |     2. Base64URL decode → 33 bytes
  |     3. Check bytes[0] == 0x01 (version)
  |     4. Extract bytes[1..32] = Bob's Ed25519 public key
  |     5. Compute Bob's identity hash = SHA-256(publicKeyBytes)
  |     6. Store temporarily in savedStateHandle
  |     7. Navigate to CONTACT_CONFIRM screen

ContactConfirmScreen shows:
  - Bob's identity hash (for verbal verification with Bob)
  - Input field for Bob's nickname

ContactConfirmViewModel.saveContact()
  |
  +-- Check if contact with this identityHash already exists in DB
  |     If YES (key unchanged): update nickname, show "contact updated" toast
  |     If YES (key CHANGED):   show KEY_CHANGE_WARNING dialog
  |                              User must explicitly accept the new key
  |     If NO:                  insert new contact row
  |
  +-- contactRepository.upsertContact(Contact(
  |       identityHashHex = SHA-256(publicKey).toHex(),
  |       publicKeyBase64 = Base64.encode(publicKey),
  |       nickname = enteredNickname,
  |       addedAt = System.currentTimeMillis()
  |   ))
  |
  +-- conversationRepository.upsertConversation(Conversation(
  |       conversationId = buildConversationId(myHash, contactHash),
  |       contactIdentityHash = contactHash
  |   ))
  |
  +-- BackgroundConnectionManager.ensureConnected(conversationId)
        ← immediately starts the WebSocket for this new conversation
```

### How the conversation ID is built

```kotlin
fun buildConversationId(hashA: String, hashB: String): String {
    // Sort lexicographically so both sides always compute the same ID
    val sorted = listOf(hashA, hashB).sorted()
    return "${sorted[0]}_${sorted[1]}"
}
```

Both Alice and Bob independently run this function with their own hash and the other's hash. Because both hashes are fixed (they came from the public keys), and both sides sort the same way, they arrive at the same conversation ID without any coordination.

---

## 6. Networking Layer — WebSocket Architecture

### The three-tier networking stack

```
RelayWebSocketClient       ← thin OkHttp wrapper; one-socket, one-conversation
        |
BackgroundConnectionManager ← orchestrates all conversations; routes packets
        |
ChatViewModel / CallManager ← consume packets for delivery to UI or audio
```

### RelayWebSocketClient — what it does

**File: `data/remote/websocket/RelayWebSocketClient.kt`**

One instance exists per active conversation. Its responsibilities:

1. **Connect** to `ws://host:8443/ws?conv=<conversationId>&id=<myIdentityHash>`
2. **Send binary frames** (prepend the relay envelope header)
3. **Receive binary frames** (strip the relay envelope header, deliver payload to callback)
4. **Reconnect** with exponential backoff when the connection drops

```
Connection URL:
  ws://13.203.169.244:8443/ws?conv=aabb...ccdd_eeff...0011&id=1234...5678
                                  ↑                          ↑
                              conversationId              our identity hash
                              (not linked to real identity  (for presence tracking)
                               at the server level)
```

### Relay envelope framing (what the WebSocket client actually sends)

```
Every outgoing binary WebSocket frame:

+------------------+------------------+-------------------+--------------------+------------------+
| 2 bytes (BE)     | N bytes          | 2 bytes (BE)      | M bytes            | P bytes          |
| len(convId)      | conversationId   | len(messageId)    | messageId (UUID)   | raw packet bytes |
|                  | (UTF-8 string)   |                   | (UTF-8 string)     |                  |
+------------------+------------------+-------------------+--------------------+------------------+

Example:
  convId = "a1b2c3..._d4e5f6..."  (length ~129 chars for two 64-hex hashes + underscore)
  msgId  = "550e8400-e29b-41d4-a716-446655440000"  (UUID v4, 36 chars)
```

The server reads the first 4 fields (conv length, conv, msgId length, msgId) for routing. Everything after the 4th field is the actual packet — the server never reads it.

### Exponential backoff reconnection

```kotlin
// Inside RelayWebSocketClient
private var backoffMs = 1_000L          // starts at 1 second
private val maxBackoffMs = 30_000L      // caps at 30 seconds

fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    reconnectJob = reconnectScope.launch {
        delay(backoffMs)
        backoffMs = minOf(backoffMs * 2, maxBackoffMs)  // double each time, cap at 30s
        connect()
    }
}
```

Why `reconnectScope.launch { delay() }` instead of `Handler(Looper.getMainLooper()).postDelayed()`?

Android's Doze mode stops the main Looper from running `Handler` callbacks. Coroutine `delay()` uses `DefaultDelay` which is backed by `ScheduledExecutorService`, which **does** fire in Doze. This ensures reconnection happens even when the device has been sitting idle for hours.

### BackgroundConnectionManager — the core engine

**File: `data/remote/BackgroundConnectionManager.kt`**

This is an application-scoped `@Singleton` injected by Hilt. Every part of the app that needs a connection goes through it.

```
Internal state:

data class ConvState(
    val client: RelayWebSocketClient,
    var sessionState: SessionState,        // AWAITING_HANDSHAKE / HANDSHAKE_IN_PROGRESS / ACTIVE
    var sendRatchet: HashRatchet?,
    var recvRatchet: HashRatchet?,
    var pendingHandshakeOffer: ByteArray?,  // our offer, held until peer's offer arrives
    var ticketBytes: ByteArray?
)

private val conversations = ConcurrentHashMap<String, ConvState>()
```

**Packet routing by magic byte (first byte of raw packet):**

```kotlin
fun onPacketReceived(convId: String, packet: ByteArray) {
    when (packet[0]) {
        0x01.toByte() -> handleHandshakePacket(convId, packet)
        0x02.toByte() -> callManager.onCallSignalReceived(packet)
        0x03.toByte() -> callManager.onAudioFrameReceived(packet)
        else          -> handleMessagePacket(convId, packet)
    }
}
```

**Message delivery when ChatViewModel is/isn't active:**

```kotlin
// ChatViewModel registers itself when user opens a chat:
fun registerReceiver(convId: String, receiver: (DecryptedMessage) -> Unit) {
    activeReceivers[convId] = receiver
}

// On packet receive:
fun deliverMessage(convId: String, decrypted: DecryptedMessage) {
    val uiReceiver = activeReceivers[convId]
    if (uiReceiver != null) {
        // Chat screen is open — deliver directly to ViewModel (StateFlow update)
        uiReceiver(decrypted)
    } else {
        // App is in background — save to database silently
        coroutineScope.launch { repository.saveMessage(decrypted) }
    }
}
```

### Network reconnect on connectivity change

```kotlin
// BackgroundConnectionManager registers a NetworkCallback at startup:
connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        // Called when WiFi, LTE, or other network becomes available
        reconnectAll()
    }
})

fun reconnectAll() {
    conversations.forEach { (convId, state) ->
        if (!state.client.isConnected()) {
            state.client.connect()
        }
    }
}
```

This ensures that when a phone wakes up, moves from WiFi to LTE, or regains signal, all WebSocket connections are re-established within seconds.

---

## 7. The Session Ticket — Kerberos-Style Auth

**Files: `domain/crypto/TicketManager.kt`, `server/index.js`**

### Why tickets at all?

Without a ticket, Alice and Bob could complete an X25519 DH exchange with each other directly. The ticket adds server-side authorization: the relay server cryptographically asserts "I have seen request to pair identity A with identity B and I authorize it." This binds the server's blessing into the session key.

If someone tries to impersonate the server or issue their own tickets, `TicketManager.verifyTicket()` rejects them because the signature won't match the hardcoded server public key.

### How a ticket is obtained

```
Alice's app                         Relay Server (POST /api/v1/ticket)
  |                                       |
  +-- Request body:                       |
  |   { "hashA": "<alice_hash>",          |
  |     "hashB": "<bob_hash>" }           |
  |                                       |
  |   (Both hashes are always sorted      |
  |    lexicographically before sending   |
  |    so the ticket is always the same   |
  |    regardless of who requests first)  |
  |                                       |
  |   POST /api/v1/ticket ------------->  |
  |                                       +-- Build 76-byte payload:
  |                                       |   [32] sorted_hash_min
  |                                       |   [32] sorted_hash_max
  |                                       |   [8]  timestamp_ms (now)
  |                                       |   [4]  expiry_secs  (3600)
  |                                       |
  |                                       +-- signature = Ed25519.sign(serverPrivKey, payload)
  |                                       |
  |                                       +-- ticket = payload + signature  (140 bytes total)
  |                                       |
  |   <-- Response: { "ticket": "<b64>" } |
  |
  +-- Base64.decode(ticket) → 140 bytes
  +-- TicketManager.verifyTicket(ticketBytes)
       1. Extract bytes[0..75]  = payload
       2. Extract bytes[76..139] = signature (64 bytes)
       3. Ed25519.verify(serverPublicKey, payload, signature)   ← REJECT if fails
       4. Extract timestamp_ms from bytes[64..71]
       5. Verify |now - timestamp_ms| < 5 minutes               ← REJECT if outside window
       6. Extract expiry_secs from bytes[72..75]
       7. Verify now < timestamp_ms + expiry_secs * 1000        ← REJECT if expired
       8. ACCEPT — ticket is valid
```

### How the ticket hash enters the session key

```
ticketHash = SHA-256(ticketBytes)   // 32 bytes

K0 = SHA-256(
    sharedSecret     // 32 bytes from X25519
  | sorted_A_id      // 32 bytes  (identity hash of participant with smaller hash)
  | sorted_B_id      // 32 bytes
  | ticketHash       // 32 bytes  ← server authorization bound into key
  | timestamp_ms     // 8 bytes   ← prevents key reuse across sessions
)
```

Because the ticket is signed by the server's private key, and the ticket hash is mixed into K0, any session that used a forged or unsigned ticket will derive a different K0 than the legitimate peer, and all messages will fail to decrypt.

---

## 8. Cryptographic Handshake — X25519 DH Step by Step

**Files: `domain/crypto/HandshakeManager.kt`, `domain/crypto/SessionKeyManager.kt`**

### When does the handshake happen?

The handshake runs automatically whenever:
1. The WebSocket connection is first established
2. The WebSocket reconnects after a drop (fresh ephemeral keys are generated each time)
3. The relay server delivers a buffered HANDSHAKE_OFFER from the peer

### Step-by-step handshake for Alice

```
State: WebSocket just connected for conversation "alice_bob_conv_id"

STEP 1: Generate ephemeral X25519 keypair
  SessionKeyManager.generateEphemeralKeyPair()
  |
  +-- keyPairGenerator.initialize(new X25519KeyGenParameterSpec())
  +-- ephKeyPair = keyPairGenerator.generateKeyPair()
  +-- ephPubBytes = ephKeyPair.public.encoded    // 32 bytes
  +-- Store ephKeyPair temporarily in ConvState

STEP 2: Build HANDSHAKE_OFFER (97 bytes)
  HandshakeManager.createOffer(ephPubBytes)
  |
  +-- header    = byteArrayOf(0x01)              // magic byte
  +-- toSign    = SHA-256(header + ephPubBytes)  // 32 bytes
  +-- signature = identityKeyManager.sign(toSign) // 64 bytes via Ed25519
  +-- offer     = header + ephPubBytes + signature
  //             [1 byte][32 bytes][64 bytes] = 97 bytes total

STEP 3: Send offer through WebSocket
  relayWebSocketClient.send(offer)
  // The offer travels: Alice → Relay Server → Bob

STEP 4: Wait for Bob's offer to arrive
  // Meanwhile Bob does steps 1-3 with his own ephemeral key

STEP 5: Bob's offer arrives as a binary packet
  handleHandshakePacket(convId, packet)
  |
  +-- Parse: magic=packet[0], bobEphPub=packet[1..32], bobSig=packet[33..96]
  |
  +-- Verify Bob's signature:
  |     toVerify = SHA-256(byteArrayOf(0x01) + bobEphPub)
  |     Ed25519.verify(bobPublicKey, toVerify, bobSig)
  |     // bobPublicKey comes from contacts table — PINNED at QR scan
  |     // REJECT immediately if signature invalid (someone is impersonating Bob)
  |
  +-- Perform X25519 DH:
  |     sharedSecret = X25519.doPhase(aliceEphPriv, bobEphPub)
  |     // 32-byte shared secret — same value Bob will compute from his side
  |
  +-- Zeroize Alice's ephemeral private key:
  |     aliceEphPriv.encoded.fill(0)
  |
  +-- Fetch ticket from server (POST /api/v1/ticket) or use cached ticket
  |
  +-- Derive session root key:
  |     K0 = SHA-256(sharedSecret | A_id | B_id | ticketHash | ts)
  |
  +-- Create send and receive ratchets:
  |     sendRatchet = HashRatchet(K0, direction = SEND)
  |     recvRatchet = HashRatchet(K0, direction = RECV)
  |     // Two separate independent ratchets from the same root
  |
  +-- SessionState = ACTIVE
  |
  +-- If there are queued outgoing messages (from before session was ready):
        flush them now
```

### Why fresh ephemeral keys on every reconnect?

If Alice and Bob use the same long-term key for X25519 DH every session, an attacker who records all encrypted traffic and later compromises one long-term key can decrypt everything. Fresh ephemeral keys mean each session derives a fresh K0 — previous sessions remain secure even if the ephemeral keys from a later session are compromised.

---

## 9. Hash Ratchet — Forward Secrecy Mechanism

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

At any point, a snapshot of memory shows only the current ratchet state (one key). Past keys are gone. An attacker who steals the phone right now and reads Kn out of memory cannot compute Kn-1, Kn-2, etc. (SHA-256 is not invertible).

### Two separate ratchets

```
K0
├── sendRatchet = HashRatchet(SHA-256(K0 | "send"))
│     K_s1, K_s2, K_s3, ...   (Alice's outgoing message keys)
└── recvRatchet = HashRatchet(SHA-256(K0 | "recv"))
      K_r1, K_r2, K_r3, ...   (Alice's incoming message keys)
```

Bob's sendRatchet is seeded with the same value as Alice's recvRatchet, and vice versa. So Alice's K_s1 == Bob's K_r1. They always match because both are derived from the same K0 with the same deterministic function.

### Out-of-order message handling

Network conditions can cause message 4 to arrive before message 3.

```
Alice receives: msg_seq=4 before msg_seq=3

recvRatchet.advanceTo(4):
  - current position = 2 (last decoded was #2)
  - compute K_r3 = SHA-256(K_r2)  → store in lookaheadBuffer[3] with TTL=now+30s
  - compute K_r4 = SHA-256(K_r3)  → use for decryption of msg #4
  - ratchet is now at position 4

When msg_seq=3 arrives later:
  lookaheadBuffer[3] exists and hasn't expired → use it for decryption
  lookaheadBuffer[3].fill(0) → remove from buffer

Limits:
  - lookaheadBuffer max size = 50 entries
  - After 30 seconds, any buffered key is destroyed (expired message = failed decryption)
```

---

## 10. Sending a Message — Complete Step-by-Step Flow

**Files: `presentation/viewmodel/ViewModels.kt` (ChatViewModel), `domain/crypto/MessageProcessor.kt`**

### User taps Send in the chat

```
UI: ChatScreen.kt
  User types text, taps Send button
  ChatViewModel.sendMessage(text, conversationId)

ChatViewModel.sendMessage()
  |
  +-- Check BackgroundConnectionManager.isSessionActive(convId)
  |     If NOT active: queue message in pendingQueue, wait for ACTIVE state
  |
  +-- Generate message UUID: messageId = UUID.randomUUID().toString()
  |
  +-- Build domain Message object:
  |     Message(
  |       messageId    = messageId,
  |       convId       = conversationId,
  |       senderHash   = myIdentityHash,
  |       plaintext    = text,
  |       timestampMs  = System.currentTimeMillis(),
  |       expiryMs     = userConfiguredExpiry ?: 0,
  |       messageState = MessageState.SENDING,
  |       isOutgoing   = true
  |     )
  |
  +-- repository.saveMessage(message)
  |     ← Immediately saved in SENDING state so user sees it in the list
  |
  +-- messageProcessor.encryptMessage(text, convId)
  |
  |   INSIDE MessageProcessor.encryptMessage():
  |   ==========================================
  |   1. Get sendRatchet for convId from BCM
  |
  |   2. sendRatchet.advance()
  |        ratchetKey = SHA-256(previousKey)
  |        counter    = previousCounter + 1
  |        previousKey.fill(0)
  |        returns (ratchetKey: ByteArray, counter: Int)
  |
  |   3. Build JSON header:
  |        header = {
  |          "sid": conversationId,     // session ID
  |          "ts": timestampMs,          // milliseconds since epoch
  |          "ctr": counter,            // ratchet counter
  |          "exp": expiryMs,           // 0 = never expires
  |          "type": "TEXT"             // or "DEL", "CALL", etc.
  |        }
  |        headerBytes = header.toJson().toByteArray(UTF-8)
  |
  |   4. Compute header hash (AEAD additional data):
  |        headerHash = SHA-256(headerBytes)   // 32 bytes
  |
  |   5. Derive deterministic nonce:
  |        counterBytes = counter.toLong().toByteArray()  // 8 bytes big-endian
  |        nonce = SHA-256(ratchetKey + counterBytes)[0..11]  // first 12 bytes
  |        // This nonce is fully deterministic — same key + same counter always
  |        // produces the same nonce. Since each (key, counter) pair is used exactly
  |        // once, nonces are never reused.
  |
  |   6. AEAD encrypt with ChaCha20-Poly1305:
  |        params = AEADParameters(ratchetKey, 128, nonce, headerHash)
  |        cipher = ChaCha20Poly1305()  // BouncyCastle
  |        cipher.init(ENCRYPT_MODE, params)
  |        ciphertext = cipher.doFinal(plaintext.toByteArray(UTF-8))
  |        // ciphertext = encrypted_bytes + 16-byte_Poly1305_tag (auth tag)
  |
  |   7. Zeroize ratchet key:
  |        ratchetKey.fill(0)
  |
  |   8. Compute signature input:
  |        sigInput = SHA-256(headerBytes + ciphertext)   // 32 bytes
  |
  |   9. Sign with identity key:
  |        signature = identityKeyManager.sign(sigInput)  // 64 bytes Ed25519
  |
  |   10. Serialize wire packet:
  |        packet = [
  |          headerBytes.size as 4-byte big-endian int,
  |          headerBytes,
  |          ciphertext.size as 4-byte big-endian int,
  |          ciphertext,
  |          signature  // always 64 bytes, no length prefix needed
  |        ]
  |        return packet
  |
  +-- BackgroundConnectionManager.sendPacket(convId, messageId, packet)
  |     wraps packet in relay envelope and sends via WebSocket
  |
  +-- On WebSocket send callback (success/failure):
        repository.updateMessageState(messageId,
            if (success) MessageState.SENT else MessageState.FAILED)
        // UI updates: "✓" appears next to the message
```

---

## 11. Receiving a Message — Complete Step-by-Step Flow

**Files: `data/remote/BackgroundConnectionManager.kt`, `domain/crypto/MessageProcessor.kt`**

### Packet arrives on the WebSocket

```
RelayWebSocketClient.onMessage(bytes: ByteBuffer)
  |
  +-- Strip relay envelope header (convId, msgId) from the front
  +-- Extract raw packet
  +-- Deliver to BackgroundConnectionManager.onPacketReceived(convId, rawPacket)
  |
  +-- BCM checks magic byte:
        packet[0] != 0x01, 0x02, 0x03 → message packet
        → handleMessagePacket(convId, packet)

handleMessagePacket(convId, rawPacket):
  |
  +-- messageProcessor.decryptMessage(rawPacket, convId)
  |
  |   INSIDE MessageProcessor.decryptMessage():
  |   ==========================================
  |   1. Parse wire packet:
  |        headerLen = readInt32BE(packet, 0)
  |        headerBytes = packet[4 .. 4+headerLen]
  |        cipherLen = readInt32BE(packet, 4+headerLen)
  |        ciphertext = packet[8+headerLen .. 8+headerLen+cipherLen]
  |        signature = packet[8+headerLen+cipherLen .. end]  // last 64 bytes
  |
  |   2. VERIFY Ed25519 signature FIRST (before touching any crypto material):
  |        sigInput = SHA-256(headerBytes + ciphertext)
  |        Ed25519.verify(senderPublicKey, sigInput, signature)
  |        // senderPublicKey comes from contacts table (pinned at QR scan)
  |        // REJECT immediately if invalid — do not proceed further
  |
  |   3. Parse JSON header:
  |        header = JSON.parse(headerBytes)
  |        // { sid, ts, ctr, exp, type }
  |
  |   4. VERIFY timestamp:
  |        if |System.currentTimeMillis() - header.ts| > 5 * 60 * 1000:
  |            REJECT ("message timestamp too far from current time")
  |        // Prevents replay of old packets
  |
  |   5. VERIFY counter (replay protection):
  |        lastSeenCounter = recvRatchet.getLastSeenCounter(convId)
  |        if header.ctr <= lastSeenCounter:
  |            REJECT ("counter not advancing — possible replay attack")
  |
  |   6. Advance recv ratchet to this counter:
  |        (ratchetKey, _) = recvRatchet.advanceTo(header.ctr)
  |        // If ctr is ahead of current position, intermediate keys are computed
  |        // and cached in the lookahead buffer
  |
  |   7. Derive deterministic nonce:
  |        counterBytes = header.ctr.toLong().toByteArray()
  |        nonce = SHA-256(ratchetKey + counterBytes)[0..11]
  |
  |   8. Compute header hash (AEAD additional data):
  |        headerHash = SHA-256(headerBytes)
  |
  |   9. AEAD decrypt:
  |        params = AEADParameters(ratchetKey, 128, nonce, headerHash)
  |        cipher = ChaCha20Poly1305()
  |        cipher.init(DECRYPT_MODE, params)
  |        plaintext = cipher.doFinal(ciphertext)
  |        // If the Poly1305 tag is invalid → exception → REJECT
  |        // The tag covers: ciphertext + headerHash (AD)
  |        // Any tampering with either the header or ciphertext will fail here
  |
  |   10. Zeroize ratchet key:
  |         ratchetKey.fill(0)
  |
  |   11. Update last-seen counter:
  |         recvRatchet.setLastSeenCounter(header.ctr)
  |         // ONLY done after complete success
  |
  |   12. Return DecryptedMessage(plaintext, header.ts, header.ctr, header.exp, header.type)
  |
  +-- BCM delivers decrypted message:
        activeReceiver = registeredReceivers[convId]
        if (activeReceiver != null):
            activeReceiver(decryptedMessage)   // chat is open, update StateFlow
        else:
            coroutineScope.launch {
                repository.saveMessage(decryptedMessage.toIncomingMessage())
                // saved silently to DB; notification is NOT shown (per design: no push notif)
            }
```

---

## 12. Wire Packet Format — Byte-Level Breakdown

### Full wire packet (message)

```
Byte offset   Length   Field
-----------   ------   -----
0             4        header_length (big-endian Int32)
4             N        JSON header bytes (UTF-8)
                         { "sid": "...", "ts": 1234567890123, "ctr": 5,
                           "exp": 0, "type": "TEXT" }
4+N           4        ciphertext_length (big-endian Int32)
8+N           M-16     encrypted plaintext (ChaCha20 stream-cipher output)
8+N+M-16      16       Poly1305 authentication tag
8+N+M         64       Ed25519 signature of SHA-256(headerBytes + ciphertext)

Total:        8+N+M+64 bytes
```

Typical sizes:
- `N` (header JSON): ~80–100 bytes
- `M` (ciphertext for a 100-char message + 16-byte tag): ~116 bytes
- Typical total: ~270 bytes per message

### Full relay envelope (what leaves the device)

```
Byte offset   Length   Field
-----------   ------   -----
0             2        len(conversationId) as big-endian Uint16
2             ~129     conversationId (UTF-8: two 64-hex hashes + underscore)
~131          2        len(messageId) as big-endian Uint16
~133          36       messageId (UUID v4 string: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
~169          rest     wire packet (the full message packet above)
```

The relay server parses up to byte ~169, then blindly forwards the rest.

### HANDSHAKE_OFFER packet (97 bytes, fixed)

```
Byte offset   Length   Field
-----------   ------   -----
0             1        0x01 (magic byte = handshake)
1             32       ephemeral X25519 public key
33            64       Ed25519 signature of SHA-256(bytes[0..32])
Total:        97 bytes
```

### CALL_SIGNAL packet (122 bytes, fixed)

```
Byte offset   Length   Field
-----------   ------   -----
0             1        0x02 (magic byte = call signal)
1             1        signal_type (OFFER=1, ANSWER=2, REJECT=3, HANGUP=4, BUSY=5)
2             16       call_id (random 16 bytes, unique per call)
18            8        timestamp_ms (big-endian Int64)
26            32       ephemeral X25519 public key (zeroed for REJECT/HANGUP/BUSY)
58            64       Ed25519 signature of SHA-256(bytes[0..57])
Total:        122 bytes
```

### AUDIO_FRAME packet (variable)

```
Byte offset   Length   Field
-----------   ------   -----
0             1        0x03 (magic byte = audio frame)
1             4        sequence_number (big-endian Int32, monotonic per direction)
5             12       nonce = SHA-256(encKey | seq_8bytes)[0..11]
17            320+16   ChaCha20-Poly1305(pcm_frame) — 160 samples × 2 bytes + 16-byte tag
Total:        ~353 bytes per audio frame
```

---

## 13. Encrypted Storage — How Messages Rest On Disk

**Files: `data/local/db/AppDatabase.kt`, `data/local/keystore/KeystoreManager.kt`, `data/repository/RepositoryImpl.kt`**

### Double encryption at rest

```
DISK LAYOUT for one message:

+-------------------------------------------+
|  cryptika.db  (SQLCipher AES-256)         |
|  Passphrase keys in Android Keystore      |
|                                           |
|  messages table row:                      |
|    messageId = "abc123..."                |
|    ciphertextBlobBase64 = "XXXXXX..."     |  ← AES-256-GCM encrypted blob
|    storageHashHex = "a3f9..."             |  ← SHA-256 of the blob above
|    counter = 42                           |
|    expiryMs = 1700000000000               |
|    messageState = "DELIVERED"             |
|    ...                                    |
+-------------------------------------------+

ANDROID KEYSTORE contains:
  msg_abc123...  → AES-256-GCM key that encrypted the blob in ciphertextBlobBase64
```

### What "ciphertextBlobBase64" contains

The "blob" is not the wire ciphertext (ChaCha20-Poly1305). It is the **decrypted plaintext re-encrypted** with a per-message Keystore key:

```
On saveMessage():
  1. messageProcessor already decrypted the wire packet → plaintext string
  2. KeystoreManager.encryptForStorage(messageId, plaintext.toByteArray()):
       a. Create new AES-256-GCM key in Android Keystore, alias = "msg_<messageId>"
       b. Generate 12-byte random IV via SecureRandom
       c. Cipher.init(ENCRYPT_MODE, keystoreKey, GCMParameterSpec(128, iv))
       d. cipherBlob = iv + cipher.doFinal(plaintext_bytes)
          // IV is prepended to the ciphertext for later decryption
       e. storageHash = SHA-256(cipherBlob)
  3. Store cipherBlob (Base64-encoded) and storageHash in messages table
```

### On getMessage()

```
1. Load row from messages table: ciphertextBlobBase64, storageHashHex
2. Base64.decode(ciphertextBlobBase64) → cipherBlob (raw bytes)
3. VERIFY: SHA-256(cipherBlob) == storageHashHex
   → If mismatch: throw TamperDetectedException (database has been modified externally)
4. KeystoreManager.decryptFromStorage(messageId, cipherBlob):
   a. Extract IV = cipherBlob[0..11]
   b. Extract encryptedData = cipherBlob[12..]
   c. Keystore.getKey("msg_<messageId>")
   d. Cipher.init(DECRYPT_MODE, keystoreKey, GCMParameterSpec(128, iv))
   e. plaintext = cipher.doFinal(encryptedData)
   f. return plaintext
5. Return Message with plaintext filled in
```

### Why this double-encryption matters

| Attack | SQLCipher alone | SQLCipher + Keystore keys |
|--------|----------------|--------------------------|
| Copy the .db file off the device | Blocked by SQLCipher | Blocked by SQLCipher AND Keystore |
| Root device + read the db file | Possible if SQLCipher key is found | Still blocked — Keystore keys cannot be exported from hardware |
| Keystore key backup | N/A | Android Keystore keys are NOT backed up (excluded from cloud backup) |
| Delete one message securely | Must overwrite the row | Delete the Keystore key → ciphertext is permanent garbage |

---

## 14. Message Expiry — Cryptographic Self-Destruction

**Files: `worker/MessageExpiryWorker.kt`, `data/repository/RepositoryImpl.kt`, `presentation/viewmodel/ViewModels.kt`**

### The expiry field

Every message has an `expiryMs` field in the database. A value of `0` means "never expires." Any non-zero value is a Unix timestamp in milliseconds — the message must be destroyed after that moment.

### Setting expiry

In the chat Settings, the user sets a per-chat default expiry (e.g., "1 hour"). When ChatViewModel sends a message, it reads this setting and sets `expiryMs = System.currentTimeMillis() + defaultExpiryMillis`. The expiry is transmitted in the wire packet header's `"exp"` field so the receiver's app also knows when to destroy it.

### Three-step cryptographic destruction

```
repository.deleteExpiredMessages():
  |
  +-- Query: SELECT messageId, ciphertextBlobBase64
             FROM messages
             WHERE expiryMs > 0 AND expiryMs < :now
  |
  +-- For each expired message:
  |     Step 1: Delete Keystore entry
  |               KeystoreManager.deleteMessageKey(messageId)
  |               → androidKeyStore.deleteEntry("msg_<messageId>")
  |               → The AES-256-GCM decryption key is gone
  |               → ciphertextBlobBase64 is now permanently unreadable noise
  |
  |     Step 2: Overwrite the blob in the database
  |               UPDATE messages SET ciphertextBlobBase64 = ''
  |               WHERE messageId = ?
  |               → Belt-and-suspenders: ciphertext is wiped even though it's
  |                 already unreadable without the key
  |
  |     Step 3: Delete the database row
  |               DELETE FROM messages WHERE messageId = ?
```

### Expiry timing

Two mechanisms ensure timely expiry:

1. **WorkManager `MessageExpiryWorker`** — scheduled to repeat with a 15-minute flexible interval. Handles messages that expired while the app was in the background. WorkManager is battery-efficient and survives Doze.

2. **ChatViewModel coroutine timer** — when the chat screen is open, the ViewModel finds the message with the nearest upcoming expiry and schedules:
   ```kotlin
   coroutineScope.launch {
       delay(timeUntilNextExpiry)
       repository.deleteExpiredMessages()
       updateMessageList()
   }
   ```
   This ensures messages self-destruct in real time while you are watching.

---

## 15. Delete For Both Sides — How It Works

**File: `presentation/viewmodel/ViewModels.kt` (ChatViewModel)**

When Alice long-presses a message and taps "Delete for Everyone":

```
ChatViewModel.deleteMessageForBoth(messageId, counter):
  |
  +-- Build a special TEXT message with body: "__DEL__:<counter>"
  |     where <counter> is the ratchet counter of the message to be deleted
  |
  +-- Send this signal through the normal message pipeline
  |     (it is encrypted and signed like any other message)
  |
  +-- Locally: repository.deleteMessage(messageId)  ← delete from Alice's DB

On Bob's side, when the "__DEL__:<counter>" message arrives:
  |
  +-- MessageProcessor decrypts it normally
  |
  +-- BCM checks plaintext for "__DEL__:" prefix
  |
  +-- Extract the counter value
  |
  +-- Find the message in Bob's DB with that counter in the same conversation
  |
  +-- repository.deleteMessage(foundMessageId)
  |
  +-- If chat is open: ChatViewModel updates message list (message disappears)
```

The signal message itself is transient — it is processed and discarded; it is never saved to the database as a visible message.

---

## 16. Encrypted Voice Calls — Full Protocol

**Files: `data/remote/CallManager.kt`, `domain/model/CallModels.kt`, `presentation/viewmodel/ViewModels.kt` (CallViewModel), `data/remote/CallForegroundService.kt`**

### Call state machine

```
                IDLE
                  |
    Alice taps call Bob
                  |
           OUTGOING_CALLING ←── Alice
           |  (sends OFFER)
           |
    Bob's phone receives OFFER
           |
    INCOMING_RINGING ←── Bob
           |
    Bob taps Answer
           |
    Bob sends ANSWER                           Alice receives ANSWER
           |                                        |
    ACTIVE ←── Bob                          ACTIVE ←── Alice
           |                                        |
    Both start AudioRecord + AudioTrack             |
           |... bidirectional encrypted audio ...   |
           |                                        |
    Either side taps Hangup                         |
           |                                        |
    ENDED ←── sends HANGUP ──────────────────→ ENDED
```

### Starting a call — Alice calls Bob

```
CallManager.startCall(contactHash):
  |
  +-- Generate 16-byte call_id: SecureRandom().nextBytes(16)
  |
  +-- Generate fresh ephemeral X25519 keypair (independent from messaging session)
  |     callEphKeyPair = SessionKeyManager.generateEphemeralKeyPair()
  |
  +-- Build CALL_SIGNAL-OFFER (122 bytes):
  |     packet = [
  |       0x02,                              // magic
  |       0x01,                              // signal type = OFFER
  |       call_id,                           // 16 random bytes
  |       timestampMs as 8-byte big-endian,  // freshness
  |       callEphKeyPair.public.encoded,     // 32-byte X25519 pub key
  |       // signature last:
  |       Ed25519.sign(SHA-256(bytes[0..57]))  // 64 bytes
  |     ]
  |
  +-- Send packet via BackgroundConnectionManager.sendPacket(convId, ...)
  |
  +-- callState.value = CallState.OUTGOING_CALLING
  |
  +-- Start 60-second ring timeout: if no ANSWER in 60s, auto-cancel

Bob's phone receives the packet:
  |
  +-- BackgroundConnectionManager routes: packet[0] == 0x02 → CallManager.onCallSignalReceived
  |
  +-- CallManager.onCallSignalReceived(packet):
  |     Parse signal type = OFFER
  |     Parse call_id (16 bytes)
  |     Parse timestamp — verify |now - ts| < 5 min
  |     Parse aliceEphPub (32 bytes)
  |     Verify Ed25519 signature of SHA-256(bytes[0..57]) with Alice's pinned public key
  |     → REJECT if invalid
  |
  +-- callState.value = CallState.INCOMING_RINGING
  +-- incomingCallData.value = IncomingCallData(callerHash, callId, aliceEphPub)
  |
  +-- CallViewModel observes incomingCallData → auto-navigates to CallScreen
```

### Bob answers — key exchange and session setup

```
Bob taps Answer in CallScreen → CallViewModel.answerCall()

CallManager.answerCall(callId, aliceEphPub):
  |
  +-- Generate Bob's fresh ephemeral X25519 keypair
  |
  +-- Build CALL_SIGNAL-ANSWER (122 bytes):
  |     [0x02][0x02][call_id][ts][bobEphPub][signature]
  |
  +-- Send ANSWER via WebSocket
  |
  +-- Perform X25519 DH from Bob's side:
  |     sharedSecret = X25519(bobEphPriv, aliceEphPub)
  |     bobEphPriv.fill(0)  ← destroy immediately
  |
  +-- Derive direction-specific call keys:
  |     callerEncKey = SHA-256(sharedSecret | "caller_send" | call_id_bytes)
  |     calleeEncKey = SHA-256(sharedSecret | "callee_send" | call_id_bytes)
  |     // Bob is callee: he sends with calleeEncKey, receives with callerEncKey
  |
  +-- Start audio engine:
  |     AudioRecord(source=MIC, rate=16000, channel=MONO, encoding=PCM_16BIT, ...)
  |     AudioTrack(stream=VOICE_CALL, rate=16000, channel=MONO, encoding=PCM_16BIT, ...)
  |
  +-- Start CallForegroundService (foregroundServiceType=microphone)
  |     Required for AudioRecord to keep working when app goes to background
  |
  +-- Start send coroutine:
  |     loop:
  |       read 160-sample frame from AudioRecord (= 320 bytes)
  |       seq = sendSequenceAtomic.incrementAndGet()
  |       nonce = SHA-256(calleeEncKey | seq_as_8_bytes)[0..11]
  |       cipherFrame = ChaCha20Poly1305.encrypt(key=calleeEncKey, nonce, pcm_bytes)
  |       packet = [0x03][seq_4bytes][nonce][cipherFrame]
  |       send via WebSocket
  |
  +-- Start receive coroutine + watchdog:
        WATCHDOG: if lastAudioRxMs + 15000 < now → hangup (no audio for 15s)
        On each AUDIO_FRAME received:
          lastAudioRxMs = now
          parse seq, nonce (or recompute from encKey + seq_bytes)
          decrypt ChaCha20Poly1305(key=callerEncKey, nonce, cipherFrame)
          AudioTrack.write(decryptedPcm, 0, 320)
```

### Alice receives ANSWER — key exchange from caller side

```
Alice's phone receives CALL_SIGNAL-ANSWER:
  |
  +-- Parse bobEphPub, verify timestamp, verify Ed25519 signature
  |
  +-- X25519 DH from Alice's side:
  |     sharedSecret = X25519(aliceEphPriv, bobEphPub)
  |     aliceEphPriv.fill(0)
  |
  +-- Derive same keys (Alice is caller):
  |     callerEncKey = SHA-256(sharedSecret | "caller_send" | call_id_bytes)
  |     calleeEncKey = SHA-256(sharedSecret | "callee_send" | call_id_bytes)
  |     // Alice is caller: she sends with callerEncKey, receives with calleeEncKey
  |
  +-- Start audio engine + send/receive coroutines (same as Bob's above)
```

### Why direction-specific keys prevent nonce collisions

```
Counter-based nonce: nonce = SHA-256(encKey | seq)[0..12]

Alice sends frame #1:  nonce = SHA-256(callerEncKey | 1)[0..12]
Bob sends frame #1:    nonce = SHA-256(calleeEncKey | 1)[0..12]

callerEncKey != calleeEncKey  →  nonce_alice_1 != nonce_bob_1
```

Even though both use sequence number 1, the nonces are different because the keys fed into the nonce derivation are different. Without this, both sides would produce the same nonce for the same counter under the same key, which would be catastrophic for ChaCha20 (stream cipher reuse reveals XOR of plaintexts).

---

## 17. Background Operation — Doze, Reconnect, Services

### Android Doze mode problem

Android's Doze mode (introduced in API 23) aggressively restricts background operations when the device is idle:
- `Handler.postDelayed()` callbacks are deferred indefinitely
- Network access is restricted to "maintenance windows" (a few minutes per hour)
- `AlarmManager` alarms (unless `setExactAndAllowWhileIdle`) don't fire

Cryptika solves this with two mechanisms:

### 1. Foreground Services

```
ConnectionForegroundService
  - IMPORTANCE_MIN notification (no sound, no peek, appears in notification shade)
  - Keeps the process alive indefinitely
  - Android guarantees network access to foreground service processes
  - Started in CryptikaApp.onCreate(), stays running until user explicitly stops
```

```
CallForegroundService
  - Separate service with foregroundServiceType="microphone"
  - Required for AudioRecord to function when app is not in foreground
  - Started when a call becomes ACTIVE
  - Shows "Call in progress" notification with a Hang Up action button
  - Stopped when call ends
```

### 2. Doze-safe reconnection

```kotlin
// BAD (breaks in Doze):
Handler(Looper.getMainLooper()).postDelayed({ reconnect() }, backoffMs)

// GOOD (works in Doze):
reconnectScope.launch {
    delay(backoffMs)   // Kotlin delay uses ScheduledExecutorService, works in Doze
    reconnect()
}
```

The `reconnectScope` uses `Dispatchers.IO` which uses a thread pool — thread pool schedulers are not suspended by Doze.

### 3. NetworkCallback for instant reconnect on wake

```kotlin
// Registered in BackgroundConnectionManager init block:
val request = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .build()
connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
    override fun onAvailable(network: Network) {
        // Called every time a network becomes available:
        //   - Phone wakes from Doze
        //   - WiFi reconnects
        //   - LTE signal restored
        //   - VPN connects
        reconnectAll()
    }
})
```

This ensures that as soon as connectivity is restored (even momentarily during a Doze maintenance window), all WebSocket connections are re-established and any queued messages are flushed.

---

## 18. Relay Server — Internal Architecture

**File: `server/index.js`**

### In-memory data structures

```javascript
// Tracks which WebSocket sockets are in each conversation room
const conversationSockets = new Map()
// conversationSockets : Map<convId: String, sockets: Set<WebSocket>>

// Maps each socket to the identity hash that opened it (for presence)
const wsIdentityMap = new Map()
// wsIdentityMap : Map<ws: WebSocket, identityHash: String>

// Offline message buffer: messages for conversations with no connected peers
const offlineBuffer = new Map()
// offlineBuffer : Map<convId: String, entries: Array<{data: Buffer, ts: Number}>>
// Max 50 entries per conversation, 1-hour TTL per entry

// Online/offline presence for each identity hash
const presenceMap = new Map()
// presenceMap : Map<hash: String, {online: Boolean, lastSeen: Number}>
```

### WebSocket connection flow

```
Client connects: ws://host:8443/ws?conv=<convId>&id=<hash>
  |
  +-- Parse query params: convId, identityHash
  |
  +-- Add socket to conversationSockets[convId] Set
  |
  +-- wsIdentityMap.set(ws, identityHash)
  |
  +-- presenceMap.set(identityHash, { online: true, lastSeen: Date.now() })
  |
  +-- Check offlineBuffer[convId]:
  |     If entries exist:
  |       For each buffered message (in FIFO order):
  |         ws.send(entry.data)   ← deliver immediately to the just-connected client
  |       offlineBuffer.delete(convId)  ← clear buffer after delivery
  |
  +-- Start 30s ping interval for this socket:
        setInterval(() => ws.ping(), 30000)

Client sends a message:
  |
  +-- ws.on("message", (data) => {
  |     if (typeof data === "string") { ws.close(4004, "text not allowed"); return }
  |     // Only binary frames accepted
  |
  |     const room = conversationSockets.get(convId)
  |     let delivered = false
  |     for (const peer of room) {
  |       if (peer !== ws && peer.readyState === WebSocket.OPEN) {
  |         peer.send(data)   ← forward to all other sockets in the room
  |         delivered = true
  |       }
  |     }
  |     if (!delivered) {
  |       // No peers connected — buffer the message
  |       if (!offlineBuffer.has(convId)) offlineBuffer.set(convId, [])
  |       const buf = offlineBuffer.get(convId)
  |       if (buf.length < 50) buf.push({ data, ts: Date.now() })
  |       // Messages beyond 50 are silently dropped (DoS protection)
  |     }
  |   })

Client disconnects:
  |
  +-- conversationSockets.get(convId).delete(ws)
  +-- wsIdentityMap.delete(ws)
  +-- setTimeout(() => {
  |     presenceMap.set(hash, { online: false, lastSeen: Date.now() })
  |   }, 5000)
  |   // 5-second delay prevents brief network handoffs from
  |   // flashing as "offline" in the UI
  +-- If room is empty: conversationSockets.delete(convId)
```

### Cleanup cycle (runs every 60 seconds)

```javascript
setInterval(() => {
    const now = Date.now()

    // 1. Evict expired offline buffer entries (older than 1 hour)
    for (const [convId, entries] of offlineBuffer) {
        const fresh = entries.filter(e => now - e.ts < 3_600_000)
        if (fresh.length > 0) offlineBuffer.set(convId, fresh)
        else offlineBuffer.delete(convId)
    }

    // 2. Evict stale presence entries (older than 5 minutes)
    for (const [hash, info] of presenceMap) {
        if (now - info.lastSeen > 300_000) presenceMap.delete(hash)
    }

    // 3. Evict empty conversation rooms
    for (const [convId, sockets] of conversationSockets) {
        if (sockets.size === 0) conversationSockets.delete(convId)
    }
}, 60_000)
```

### REST API implementation

```
POST /api/v1/ticket
  Body: { "hashA": "...", "hashB": "..." }
  |
  +-- Sort hashA, hashB lexicographically → min, max
  +-- payload = Buffer.concat([
  |       Buffer.from(min, 'hex'),           // 32 bytes
  |       Buffer.from(max, 'hex'),           // 32 bytes
  |       writeBigInt64BE(BigInt(Date.now())),// 8 bytes
  |       writeInt32BE(3600)                 // 4 bytes
  |   ])  // 76 bytes
  +-- signature = nacl.sign.detached(payload, serverPrivateKey)  // 64 bytes (tweetnacl)
  +-- ticket = Buffer.concat([payload, signature])  // 140 bytes
  +-- Response: { "ticket": ticket.toString('base64') }

GET /api/v1/presence/:hash
  +-- entry = presenceMap.get(hash) ?? { online: false, lastSeen: null }
  +-- Response: { "online": entry.online, "lastSeen": entry.lastSeen }
```

---

## 19. Dependency Injection — How Hilt Wires Everything

**File: `di/AppModule.kt`**

Hilt is Google's DI framework built on Dagger. It generates the dependency graph at compile time (via KAPT) and provides singletons throughout the app lifecycle.

### The dependency graph

```
@Singleton scope (one instance for the whole app):

AppModule.provideIdentityKeyManager(context)
  → IdentityKeyManager(@ApplicationContext Context)
    (needs Context to access SharedPreferences and Android Keystore)

AppModule.provideKeystoreManager()
  → KeystoreManager()

AppModule.provideHashRatchetFactory()
  → HashRatchetFactory  (creates new HashRatchet instances on demand)

AppModule.provideMessageProcessor(identityKeyManager)
  → MessageProcessor(identityKeyManager)

AppModule.provideAppDatabase(context)
  → AppDatabase.build(context)
    SQLCipher passphrase is loaded from Keystore inside this call

AppModule.provideRelayApi(serverConfig)
  → Retrofit.create(RelayApi::class.java)

AppModule.provideBackgroundConnectionManager(
    context, relayApi, identityKeyManager, sessionKeyManager,
    handshakeManager, messageProcessor, repository, callManager)
  → BackgroundConnectionManager(...)
    ← the core networking singleton

AppModule.provideCallManager(
    context, identityKeyManager, sessionKeyManager, relayWsClientFactory)
  → CallManager(...)

AppModule.provideRepositoryImpl(
    appDatabase, keystoreManager)
  → RepositoryImpl(appDatabase.contactDao(), appDatabase.messageDao(), ...)
    bound as IContactRepository, IMessageRepository, IConversationRepository
```

### How a ViewModel gets dependencies

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: IMessageRepository,           // from Hilt
    private val backgroundConnectionManager: BackgroundConnectionManager,  // Singleton
    private val identityKeyManager: IdentityKeyManager,          // Singleton
    savedStateHandle: SavedStateHandle                           // from Navigation
) : ViewModel() {
    // All dependencies are injected at construction time by Hilt
    // No manual new/getInstance() calls anywhere in the ViewModel
}
```

### Hilt annotation processing at build time

```
./gradlew assembleDebug
  |
  +-- KAPT processes @HiltAndroidApp, @HiltViewModel, @Module, @Provides
  |     → generates Dagger components in app/build/generated/hilt/
  |
  +-- KSP processes @Entity, @Dao, @Database (Room)
  |     → generates DAO implementations in app/build/generated/ksp/
  |
  +-- Kotlin compiler compiles everything together
```

---

## 20. Database Schema — Tables, Migrations, Indexes

**File: `data/local/db/AppDatabase.kt`**

The database file is `cryptika.db` (renamed from `hacksecure.db` in the app rename). It is encrypted with SQLCipher using a 32-byte random passphrase stored in Android Keystore.

### `contacts` table

```sql
CREATE TABLE contacts (
    identityHashHex TEXT NOT NULL PRIMARY KEY,  -- SHA-256(publicKey) as 64 hex chars
    publicKeyBase64 TEXT NOT NULL,               -- Base64-encoded 32-byte Ed25519 pub key
    nickname        TEXT NOT NULL,               -- user-assigned display name
    addedAt         INTEGER NOT NULL             -- System.currentTimeMillis() at scan time
);
```

### `conversations` table

```sql
CREATE TABLE conversations (
    conversationId       TEXT NOT NULL PRIMARY KEY,  -- min_hash + "_" + max_hash
    contactIdentityHash  TEXT NOT NULL,              -- FK → contacts.identityHashHex
    lastMessagePreview   TEXT NOT NULL DEFAULT '',   -- truncated plaintext for home screen
    lastMessageTs        INTEGER NOT NULL DEFAULT 0, -- for sorting conversation list
    unreadCount          INTEGER NOT NULL DEFAULT 0  -- badge count
);
```

### `messages` table

```sql
CREATE TABLE messages (
    messageId            TEXT NOT NULL PRIMARY KEY,  -- UUID v4
    conversationId       TEXT NOT NULL,              -- FK → conversations.conversationId
    senderHash           TEXT NOT NULL,              -- identity hash of sender
    ciphertextBlobBase64 TEXT NOT NULL,              -- AES-256-GCM encrypted plaintext
    storageHashHex       TEXT NOT NULL DEFAULT '',   -- SHA-256(blob) for tamper detection
    counter              INTEGER NOT NULL,           -- ratchet counter from wire packet
    timestampMs          INTEGER NOT NULL,           -- from wire packet header
    expiryMs             INTEGER NOT NULL DEFAULT 0, -- 0 = no expiry
    messageState         TEXT NOT NULL DEFAULT 'DELIVERED', -- SENDING/SENT/DELIVERED/FAILED
    isOutgoing           INTEGER NOT NULL DEFAULT 0  -- 0=incoming, 1=outgoing
);

CREATE INDEX idx_messages_convId ON messages(conversationId);
CREATE INDEX idx_messages_expiry ON messages(expiryMs);
CREATE INDEX idx_messages_counter ON messages(counter);
```

### `local_identity` table

```sql
CREATE TABLE local_identity (
    id              INTEGER NOT NULL PRIMARY KEY,  -- always 1 (singleton row)
    publicKeyBase64 TEXT NOT NULL,                 -- this device's Ed25519 public key
    identityHashHex TEXT NOT NULL                  -- SHA-256(publicKey)
);
```

### Migration history

```
v1 (initial schema):
  contacts, conversations, messages (without messageState), local_identity

v2:
  ALTER TABLE messages ADD COLUMN messageState TEXT NOT NULL DEFAULT 'DELIVERED'

v3:
  ALTER TABLE messages ADD COLUMN storageHashHex TEXT NOT NULL DEFAULT ''
```

Room handles migrations automatically. If a user upgrades from v1 or v2, Room runs the migration SQL before allowing any database operations.

---

## 21. All Source Files Explained

### Application layer

| File | Purpose |
|------|---------|
| `CryptikaApp.kt` | `@HiltAndroidApp` application class. Starts `ConnectionForegroundService` and calls `BackgroundConnectionManager.startAllConnections()` on app creation. |
| `MainActivity.kt` | Single-Activity host for all Compose screens. Applies `FLAG_SECURE` based on settings. Creates the NavController and renders `CryptikaNavGraph`. Observes `CallViewModel.incomingCallData` to auto-navigate to `CallScreen`. |

### Dependency injection

| File | Purpose |
|------|---------|
| `di/AppModule.kt` | Hilt `@Module` with `@Provides` methods for every singleton: IdentityKeyManager, KeystoreManager, MessageProcessor, AppDatabase, BackgroundConnectionManager, CallManager, RelayApi, all Repositories. |

### Domain layer — models

| File | Purpose |
|------|---------|
| `domain/model/DomainModels.kt` | All domain data classes: `Contact`, `Message`, `Conversation`, `MessageState` enum, `WirePacket`, `HandshakeOffer`, `MessageType` enum. Pure Kotlin, zero Android imports. |
| `domain/model/CallModels.kt` | `CallState` sealed class (IDLE, OUTGOING_CALLING, INCOMING_RINGING, ACTIVE, ENDED), `CallSignalType` enum, `IncomingCallData` data class. |

### Domain layer — crypto

| File | Key responsibility |
|------|--------------------|
| `domain/crypto/IdentityKeyManager.kt` | Ed25519 keypair generation; Keystore-wrapping of private key seed; `sign()` and `verify()` functions; identity hash computation; QR payload encoding. |
| `domain/crypto/SessionKeyManager.kt` | X25519 ephemeral keypair generation; `computeSharedSecret()`; K₀ derivation with `SHA-256(secret‖A‖B‖ticketHash‖ts)`. |
| `domain/crypto/HandshakeManager.kt` | Builds 97-byte `HANDSHAKE_OFFER`; verifies peer offer signature; completes DH and returns session keys. |
| `domain/crypto/HashRatchet.kt` | SHA-256 chain ratchet; `advance()` returns next key + counter; `advanceTo(n)` for out-of-order; lookahead buffer (max 50, 30s TTL); all past keys zeroized. |
| `domain/crypto/AEADCipher.kt` | Thin wrapper around BouncyCastle `ChaCha20Poly1305`; `encrypt(key, nonce, plaintext, ad)` and `decrypt(key, nonce, ciphertext, ad)`; deterministic nonce derivation helper. |
| `domain/crypto/TicketManager.kt` | Parses 140-byte ticket; verifies Ed25519 signature against hardcoded server public key; checks timestamp freshness and expiry; extracts ticket hash for K₀. |
| `domain/crypto/MessageProcessor.kt` | Combines all crypto steps: `encryptMessage()` (9-step send pipeline) and `decryptMessage()` (9-step receive pipeline, strict validation order). |

### Domain layer — repository interfaces

| File | Purpose |
|------|---------|
| `domain/repository/Repositories.kt` | Kotlin interfaces: `IContactRepository`, `IMessageRepository`, `IConversationRepository`. ViewModels depend on these interfaces, not concrete implementations (Clean Architecture). |

### Data layer — local

| File | Purpose |
|------|---------|
| `data/local/db/AppDatabase.kt` | Room `@Database(version=3)`; SQLCipher integration; all `@Dao` interfaces (`ContactDao`, `MessageDao`, `ConversationDao`, `LocalIdentityDao`); migration v1→v2, v2→v3. |
| `data/local/keystore/KeystoreManager.kt` | Android Keystore AES-256-GCM operations: `encryptForStorage(id, bytes)` creates per-message key and encrypts; `decryptFromStorage(id, blob)` decrypts; `deleteMessageKey(id)` nukes the key; DB passphrase management. |
| `data/repository/RepositoryImpl.kt` | Implements all repository interfaces. `saveMessage()` handles per-message Keystore encryption + hash. `getMessages()` decrypts + tamper-checks each message. `deleteExpiredMessages()` performs three-step cryptographic destruction. |

### Data layer — remote

| File | Purpose |
|------|---------|
| `data/remote/ServerConfig.kt` | `@Singleton` holding the mutable relay URL. Changed by `SettingsViewModel` and read by `RelayWebSocketClient` on every connection attempt. |
| `data/remote/BackgroundConnectionManager.kt` | The core engine. One `ConvState` (containing `RelayWebSocketClient` + session state + ratchets) per conversation. Registers `NetworkCallback`. Routes packets by magic byte. Manages handshake state machine. |
| `data/remote/CallManager.kt` | Manages the full call lifecycle: outgoing/incoming signals, X25519 key exchange, `AudioRecord`/`AudioTrack` loops, `sendSequenceAtomic` (`AtomicInteger`), audio watchdog, DTMF if added. |
| `data/remote/ConnectionForegroundService.kt` | Minimal `Service` subclass. Calls `startForeground()` with a silent notification. Keeps the process alive for messaging. |
| `data/remote/CallForegroundService.kt` | Foreground service with `foregroundServiceType="microphone"`. Shows "Call in progress" notification. Keeps AudioRecord alive in background. |
| `data/remote/api/RelayApi.kt` | Retrofit `@GET`/`@POST` interface for `/health`, `/api/v1/ticket`, `/api/v1/presence`. |
| `data/remote/websocket/RelayWebSocketClient.kt` | `OkHttpClient.newWebSocket()` wrapper. Frames outgoing packets with relay envelope. Strips relay envelope from incoming frames. Exponential backoff reconnect via coroutine delay. |

### Presentation layer — ViewModels

| ViewModel | Screen | Key responsibilities |
|-----------|--------|---------------------|
| `SplashViewModel` | SplashScreen | Detects first launch; generates identity; navigates to HOME |
| `HomeViewModel` | HomeScreen | Collects contacts+conversations Flow from DB; builds `ConversationItem` list; handles new-message badge clearing |
| `QrDisplayViewModel` | QrDisplayScreen | Renders own identity as QR string (`cryptika://id/v1/…`) |
| `QrScanViewModel` | QrScanScreen | Parses scanned QR strings; validates format + version byte; extracts contact public key |
| `ContactConfirmViewModel` | ContactConfirmScreen | Saves/updates contact in DB; detects and warns on key change; triggers BCM to connect |
| `ChatViewModel` | ChatScreen | Full send pipeline; receive registration with BCM; retry on failure; expiry timer; delete-for-both; unread count reset |
| `SettingsViewModel` | SettingsScreen | Screenshot toggle; default expiry; server URL change + ping test; identity regeneration |
| `CallViewModel` | CallScreen | Wraps `CallManager` state flows; exposes call actions to UI; observes `incomingCallData` |

### Presentation layer — UI screens

| File | Screens |
|------|---------|
| `presentation/ui/screens/SplashAndHomeScreens.kt` | `SplashScreen` (progress indicator during identity gen), `HomeScreen` (conversation list, FAB to add contact) |
| `presentation/ui/screens/ChatScreen.kt` | Message list (LazyColumn), message input bar with send button, message bubbles with state indicator and expiry countdown |
| `presentation/ui/screens/QrScanAndSettingsScreens.kt` | `QrDisplayScreen` (shows own QR), `QrScanScreen` (CameraX viewfinder), `SettingsScreen` (all settings controls) |
| `presentation/ui/screens/CallScreen.kt` | In-call UI: contact name, call timer, mute button, speaker button, hang-up button |
| `presentation/ui/theme/Theme.kt` | `CryptikaTheme` — Material 3 `MaterialTheme` wrapper with custom color scheme (dark mode optimised) |

### Worker

| File | Purpose |
|------|---------|
| `worker/MessageExpiryWorker.kt` | `CoroutineWorker` run by WorkManager every 15 minutes. Calls `repository.deleteExpiredMessages()`. Battery-efficient background expiry. |

---

## 22. Complete Data Flow: Alice Sends Bob a Message

This is the canonical end-to-end trace of what happens when Alice types "Hello" and taps Send, assuming a session is already established.

```
ALICE'S DEVICE
==============

1. User input
   ChatScreen: user types "Hello", taps Send button
   → ChatViewModel.sendMessage("Hello", convId = "alice_hash_bob_hash")

2. Database save (SENDING state)
   messageId = UUID.randomUUID() = "msg-uuid-abc"
   RepositoryImpl.saveMessage(Message(id=msg-uuid-abc, state=SENDING, isOutgoing=true, ...))
   → KeystoreManager creates Keystore key "msg_msg-uuid-abc"
   → AES-256-GCM encrypts "Hello" → cipherBlob
   → SHA-256(cipherBlob) → storageHashHex
   → Room INSERT into messages table
   → UI: message appears immediately with ⏳ indicator

3. Encrypt for transit
   MessageProcessor.encryptMessage("Hello", convId):
   a. sendRatchet.advance() → K_17, counter=17
   b. header = {"sid":"alice_hash_bob_hash","ts":1700000000000,"ctr":17,"exp":0,"type":"TEXT"}
   c. headerHash = SHA-256(headerBytes)
   d. nonce = SHA-256(K_17 | 17_as_8bytes)[0..11]
   e. ciphertext = ChaCha20Poly1305.encrypt(K_17, nonce, "Hello", ad=headerHash)
   f. K_17.fill(0)
   g. sigInput = SHA-256(headerBytes | ciphertext)
   h. sig = Ed25519.sign(alicePrivKey, sigInput)  → 64 bytes
   i. packet = [headerLen_4B][headerBytes][cipherLen_4B][ciphertext][sig]

4. Relay envelope
   RelayWebSocketClient.send(convId, "msg-uuid-abc", packet):
   envelope = [convIdLen_2B][convId][msgIdLen_2B]["msg-uuid-abc"][packet]
   WebSocket.send(binaryFrame = envelope)

5. Network: binary frame travels over TCP to relay server

RELAY SERVER (server/index.js)
==============================

6. Server routing
   ws.on("message", data):
   Parse envelope → convId = "alice_hash_bob_hash", msgId = "msg-uuid-abc"
   Room = conversationSockets.get(convId) = {aliceSocket, bobSocket}
   bobSocket.send(data)   ← forward entire envelope including routing header

7. If Bob is offline:
   offlineBuffer.get(convId).push({data, ts: now})
   ← message held for up to 1 hour

BOB'S DEVICE
============

8. Bob's WebSocket receives the frame
   RelayWebSocketClient.onMessage(bytes):
   Strip relay envelope → extract raw packet
   Deliver to BackgroundConnectionManager.onPacketReceived(convId, packet)

9. Packet routing
   packet[0] is not 0x01/0x02/0x03 → message packet
   handleMessagePacket(convId, packet)

10. Decrypt
    MessageProcessor.decryptMessage(packet, convId):
    a. Parse: headerBytes, ciphertext, sig
    b. VERIFY sig: Ed25519.verify(alicePublicKey, SHA-256(header+cipher), sig) ← OK
    c. Parse header: ts=1700000000000, ctr=17
    d. VERIFY timestamp: |now - ts| < 5 min ← OK
    e. VERIFY counter: 17 > lastSeen(16) ← OK
    f. recvRatchet.advanceTo(17) → K_r17
    g. nonce = SHA-256(K_r17 | 17_as_8bytes)[0..11]
    h. headerHash = SHA-256(headerBytes)
    i. plaintext = ChaCha20Poly1305.decrypt(K_r17, nonce, ciphertext, ad=headerHash) = "Hello"
    j. K_r17.fill(0)
    k. lastSeen = 17

11. Delivery
    BCM: is ChatViewModel for this convId registered?
      YES (Bob's chat screen is open) → chatVM.onMessageReceived(decryptedMsg) → StateFlow update → UI recomposes
      NO (app in background) → RepositoryImpl.saveMessage(decryptedMsg) → silent DB write

12. If Bob's chat is open:
    ChatScreen LazyColumn recomposes with new "Hello" bubble

BACK ON ALICE'S DEVICE
=======================

13. Relay WebSocket send callback fires (success)
    BackgroundConnectionManager updates message state:
    RepositoryImpl.updateMessageState("msg-uuid-abc", MessageState.SENT)
    UI: ⏳ → ✓ (sent indicator)
```

---

## 23. Threat Model and Security Analysis

### Attacker capabilities considered

| Attacker level | Can/cannot do |
|----------------|--------------|
| Passive network observer | Can see: TCP connections to relay IP, approximate message sizes, timing |
| Active network attacker (MitM) | Can intercept: WebSocket frames (but cannot decrypt or forge them) |
| Compromised relay server | Can read: conversation IDs, connection times, message sizes; CANNOT read: content |
| Physical access to locked device | Cannot access messages (SQLCipher + Keystore hardware binding) |
| Physical access to unlocked device | Can see: displayed messages, app content (same as user) |
| Rooted device, full OS control | Keystore keys may be extractable from memory during active use; past messages may be readable from DB if SQLCipher passphrase is found in memory |

### Security properties and their cryptographic basis

| Property | Mechanism | What breaks it |
|----------|-----------|----------------|
| Message confidentiality | ChaCha20-Poly1305 with per-message ratchet key | Compromising the current ratchet state |
| Past message confidentiality (forward secrecy) | SHA-256 ratchet: past keys zeroized | Nothing — past keys are provably gone |
| Message authenticity | Ed25519 outer signature | Compromising Alice's identity private key |
| Message integrity (no tampering) | Poly1305 authentication tag | Breaking Poly1305 (computationally infeasible) |
| Replay prevention | Monotonic counter check before decryption | None — deterministic |
| Identity authentication | Signatures verified against pinned public key from QR scan | Compromising the contact's private key |
| Session authorization | Server ticket hash in K₀ | Compromising the server's Ed25519 private key (then the attacker could issue tickets, but still can't read old sessions) |
| Storage confidentiality | SQLCipher AES-256 + per-message Keystore key | Full OS/hardware compromise |
| Expiry integrity | Keystore key deletion — ciphertext becomes noise | Nothing — key deletion is irreversible |
| Call confidentiality | X25519 DH per call + direction-specific ChaCha20-Poly1305 | Compromising call ephemeral private key (deleted immediately after DH) |

### Known limitations

1. **Traffic analysis:** The relay server and any network observer can see when conversations are active, the frequency of messages, and approximate message sizes. Cryptika does not pad messages to a fixed size.

2. **Contact graph:** The relay server knows which conversation IDs are active. Since conversation IDs are `SHA-256(A) + "_" + SHA-256(B)`, and public keys are never transmitted to the server, the server cannot identify participants without already having their public keys. However, correlation attacks are possible if an attacker controls the relay and also has one participant's public key.

3. **Break-in recovery (post-compromise security):** Cryptika uses a one-directional SHA-256 ratchet. If an attacker takes a snapshot of the ratchet state (e.g., via a sophisticated malware), they can decrypt all future messages until the session is re-established (which happens on every reconnect). Signal's Double Ratchet provides better recovery properties but is significantly more complex.

4. **No sealed sender:** The relay server can observe which socket (IP address) is sending to which conversation. In Signal's sealed sender protocol, even the sender's identity is hidden from the server. Cryptika does not implement this.

---

## 24. Cryptographic Algorithm Rationale

| Algorithm | Alternative | Rationale |
|-----------|-------------|-----------|
| **ChaCha20-Poly1305** | AES-256-GCM | ChaCha20 is constant-time on all hardware. AES-GCM on CPUs without AES-NI hardware is vulnerable to timing side-channels. For an Android app running on diverse hardware (budget phones without AES hardware), ChaCha20 is safer. Both are IETF AEAD standards. |
| **Ed25519** | ECDSA (P-256), RSA-2048 | Ed25519 is deterministic (no per-signature random nonce). ECDSA with a weak RNG has historically led to private key recovery. RSA-2048 has 256-byte keys and is much slower. Ed25519's 32-byte keys and 64-byte signatures are compact and fast. |
| **X25519 (Curve25519)** | ECDH (P-256), RSA DH | Curve25519 is specifically designed to be safe against implementation errors: the x-coordinate-only computation has no exceptional points, making it harder to introduce bugs. P-256 implementations have historically had subtler constant-time issues. |
| **SHA-256 ratchet** | HMAC-SHA-256 ratchet, HKDF | Simple cryptographic properties: SHA-256 is a well-understood one-way function. For a deterministic derivation of an independent key from the previous key, SHA-256(prev_key) is sound and auditable. HKDF would be marginally stronger as a KDF but adds complexity. |
| **BouncyCastle 1.70** | Conscrypt, Android-native Crypto | Ed25519 and X25519 require API 33+ in Android's built-in provider. BouncyCastle provides a complete, audited implementation compatible with API 26+. |
| **SQLCipher 4.5.4** | Android EncryptedFile API, manual AES | SQLCipher is a proven, audited full-database AES-256 extension to SQLite. It integrates seamlessly with Room. The EncryptedFile API only encrypts individual files, not queryable structured data. Manual encryption would require implementing indexing securely, which is complex. |
| **AES-256-GCM (Keystore)** | AES-256-CBC, RSA OAEP | AES-GCM provides both confidentiality and authenticity (AEAD). It is used for storage (Keystore keys) where the key is hardware-backed. GCM is the standard for Android Keystore AEAD operations. |

---

## 25. Android Permissions Explained

| Permission | `android.permission.*` | Required for | Moment of grant |
|-----------|----------------------|-------------|----------------|
| INTERNET | `INTERNET` | WebSocket relay connection, HTTP REST calls | Declared in Manifest — automatic, no runtime prompt |
| ACCESS_NETWORK_STATE | `ACCESS_NETWORK_STATE` | `ConnectivityManager.NetworkCallback` registration | Declared in Manifest — automatic |
| FOREGROUND_SERVICE | `FOREGROUND_SERVICE` | `ConnectionForegroundService.startForeground()` | Declared in Manifest — automatic on API < 28; prompted on API 28+ |
| FOREGROUND_SERVICE_MICROPHONE | `FOREGROUND_SERVICE_MICROPHONE` | `CallForegroundService` with microphone access | API 29+; declared in Manifest |
| RECORD_AUDIO | `RECORD_AUDIO` | `AudioRecord` for voice capture during calls | Runtime prompt (when user first places or receives call) |
| CAMERA | `CAMERA` | CameraX viewfinder for QR code scanning | Runtime prompt (when user first taps "Add Contact") |
| POST_NOTIFICATIONS | `POST_NOTIFICATIONS` | Foreground service notifications (API 33+) | Runtime prompt on API 33+ at first notification |
| RECEIVE_BOOT_COMPLETED | `RECEIVE_BOOT_COMPLETED` | `BootReceiver` to restart `ConnectionForegroundService` after reboot | Declared in Manifest — automatic |

---

## 26. Tech Stack Reference

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Language | Kotlin | 1.9.22 | 100% Kotlin; coroutines, sealed classes, extension functions |
| UI | Jetpack Compose | BOM 2024.02.00 | Declarative UI; single-activity; no XML layouts |
| UI design | Material 3 | via Compose BOM | Dark-mode-first color scheme, CryptikaTheme |
| Architecture | Clean Architecture + MVVM | — | Separation of domain logic from Android framework |
| DI | Hilt (Dagger 2) | 2.51 | Compile-time DI graph; KAPT annotation processing |
| Async | Kotlin Coroutines + Flow | 1.7.3 | Structured concurrency; StateFlow for reactive UI |
| Crypto: signatures | BouncyCastle — Ed25519 | 1.70 | Identity signing and verification |
| Crypto: DH | BouncyCastle — X25519 (Curve25519) | 1.70 | Session key agreement |
| Crypto: AEAD | BouncyCastle — ChaCha20-Poly1305 | 1.70 | Message encryption + authentication |
| Crypto: storage | Android Keystore — AES-256-GCM | API 26+ | Hardware-backed key storage |
| Database | Room | 2.6.1 | ORM over SQLite; Flow-based reactive queries |
| DB encryption | SQLCipher for Android | 4.5.4 | AES-256 whole-database encryption |
| HTTP | Retrofit | 2.9.0 | REST calls to relay server (ticket, presence) |
| HTTP serialization | Gson | 2.10.1 | JSON encoding/decoding |
| WebSocket | OkHttp | 4.12.0 | Persistent binary WebSocket connections |
| Background work | WorkManager | 2.9.0 | Periodic message expiry (battery-safe) |
| QR scanning | zxing-android-embedded | 4.3.0 | ZXing integration without requiring ZXing app |
| Camera | CameraX | 1.3.1 | Camera2 API abstraction for QR viewfinder |
| Code generation | KSP (Room) | 1.9.22-1.0.17 | Faster annotation processing for Room DAOs |
| Code generation | KAPT (Hilt) | via AGP | Hilt Dagger component generation |
| Build | Gradle + AGP | 8.4 + 8.2.2 | Build system |
| Server runtime | Node.js | 18 LTS | Relay server event loop |
| Server framework | Express | 4.x | REST API routing |
| Server WebSocket | ws | 8.x | Low-level WebSocket server |
| Server crypto | tweetnacl | 0.14.x | Ed25519 for server ticket signing |
| Container | Docker + docker-compose | 20+ / 2.x | Production relay server deployment |
| Min Android | 8.0 Oreo | API 26 | Lowest API with stable Keystore AES-GCM |
| Target Android | 14 | API 34 | Latest tested SDK |

---

## 27. Building, Running, and Configuring

### Prerequisites

| Tool | Minimum version | Notes |
|------|----------------|-------|
| Android Studio | Hedgehog (2023.1.1) | Koala or later also work |
| JDK | 17 | Required by AGP 8.x |
| Android SDK Build Tools | 34.0.0 | Installed via SDK Manager |
| Gradle | 8.4 | Downloaded automatically by wrapper |
| Node.js | 18 LTS | For relay server only |
| Docker | 20+ | Optional, for production server |

### Step-by-step setup

```bash
# 1. Clone
git clone <repo-url> CryptikaMessenger
cd CryptikaMessenger

# 2. Start the relay server and note the public key
cd server
npm install
node index.js
# Output (first run):
#   Generated new server keypair
#   Server public key: 40d2741437877e85fc86799bee6942028189f990b8dc8f1e28edf90b54ac88c5
#   Cryptika relay server listening on port 8443
# Keep this terminal open OR note the SERVER_PRIVATE_KEY_HEX from the output

# 3. Configure the Android app
# Edit app/build.gradle.kts:
```
```kotlin
android {
    defaultConfig {
        buildConfigField(
            "String", "SERVER_PUBLIC_KEY_HEX",
            "\"40d2741437877e85fc86799bee6942028189f990b8dc8f1e28edf90b54ac88c5\""
        )
        buildConfigField(
            "String", "RELAY_BASE_URL",
            "\"ws://10.0.2.2:8443\""  // emulator
            // "\"ws://192.168.1.X:8443\""  // physical device on same LAN
            // "\"wss://your-domain.com:8443\""  // production
        )
    }
}
```
```bash
# 4. Build debug APK
cd ..   # back to project root
./gradlew assembleDebug

# APK at: app/build/outputs/apk/debug/app-debug.apk

# 5. Install directly
./gradlew installDebug
# or: adb install app/build/outputs/apk/debug/app-debug.apk

# 6. Other useful Gradle tasks
./gradlew test                     # unit tests
./gradlew connectedAndroidTest     # instrumented tests (needs device)
./gradlew lint                     # static analysis
./gradlew clean assembleDebug      # full clean build (fixes Hilt/KSP cache issues)
./gradlew app:dependencies         # view full dependency tree
```

---

## 28. Docker Deployment

### docker-compose.yml overview

```yaml
version: '3.8'
services:
  relay:
    build: ./server
    ports:
      - "8443:8443"
    environment:
      - PORT=8443
      - SERVER_PRIVATE_KEY_HEX=   # set this to persist the keypair
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8443/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    restart: unless-stopped
```

### Dockerfile

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 8443
CMD ["node", "index.js"]
```

### Persisting the server keypair

```bash
# One-time: generate a keypair outside Docker to get the private key
node -e "
const nacl = require('tweetnacl');
const kp = nacl.sign.keyPair();
console.log('PUBLIC:', Buffer.from(kp.publicKey).toString('hex'));
console.log('PRIVATE:', Buffer.from(kp.secretKey).toString('hex'));
"
# Copy the PRIVATE hex into docker-compose.yml SERVER_PRIVATE_KEY_HEX
# Copy the PUBLIC hex into app/build.gradle.kts SERVER_PUBLIC_KEY_HEX

# Deploy
docker-compose up -d

# Verify running
docker-compose ps
curl http://your-server-ip:8443/health
# {"status":"ok","connections":0,"timestamp":1700000000000}

# View live logs
docker-compose logs -f

# Update server code
docker-compose down
docker-compose build
docker-compose up -d
```

### AWS EC2 firewall

If hosting on EC2, add a Security Group inbound rule:
- Protocol: TCP
- Port: 8443
- Source: 0.0.0.0/0 (or a specific CIDR for stricter control)

---

## 29. Troubleshooting

### Build errors

**`MissingBindingException` or `ComponentProcessingException` (Hilt)**
```bash
./gradlew clean assembleDebug --no-build-cache
# Stale Hilt/KAPT generated code. A clean build always fixes this.
```

**`KspException: Query method parameters should either be a type that can be converted into a column or a List`**
```bash
./gradlew clean assembleDebug
# Stale KSP (Room) cache. Clean build required.
```

**`java.lang.RuntimeException: Cannot create an instance of class ... ViewModel`**
Ensure the ViewModel has `@HiltViewModel` annotation and its constructor uses `@Inject`.

---

### Connection issues

**"No connection" / "Connecting..." spinner in HomeScreen**

1. Verify server is running:
   ```bash
   curl http://SERVER_IP:8443/health
   # Expected: {"status":"ok",...}
   ```
2. Check `RELAY_BASE_URL` in `build.gradle.kts`:
   - Emulator → `ws://10.0.2.2:8443`
   - Physical device on LAN → `ws://192.168.1.X:8443` (your machine's LAN IP, not `localhost`)
   - Production → ensure port 8443 is open in firewall/security group
3. After changing the URL, run `./gradlew clean assembleDebug`
4. You can also change the URL at runtime: Settings → Server URL

**Server connects but no messages received**

Check that both devices are connecting to the same `conversationId`. The conversation ID is derived deterministically from both identity hashes. If one device regenerated its identity (Settings → Regenerate Identity), the conversation ID changes and both sides must re-add each other.

---

### Audio / call issues

**Call connects but audio is one-way**

- Check both devices have RECORD_AUDIO permission granted (Settings → Apps → Cryptika → Permissions)
- Ensure `CallForegroundService` is running (visible as "Call in progress" notification)
- The audio watchdog closes calls with 15s no-receive; one-way audio will eventually trigger this
- Check logcat for `AudioRecord.startRecording()` errors — some devices return no audio if RECORD_AUDIO is pseudo-granted but hardware-denied

**Calls always show as "Busy"**

Another call is active on the callee's device. `CallManager` checks `callState != IDLE` and sends BUSY automatically.

---

### Expiry / message deletion issues

**Messages aren't expiring**

- WorkManager requires battery optimisation to be disabled for Cryptika: Settings → Battery → Cryptika → Unrestricted
- Check WorkManager status: `adb shell dumpsys jobscheduler` (look for MessageExpiryWorker)
- Expiry only works if the expiry timestamp was set — check that "Default Expiry" is configured in Settings

---

### Server public key mismatch

If the relay server was restarted without persisting `SERVER_PRIVATE_KEY_HEX`, it generated a new keypair. The app's hardcoded `SERVER_PUBLIC_KEY_HEX` no longer matches. Symptoms: ticket requests return `402` or ticket verification in `TicketManager` throws.

Fix: note the new public key from server startup logs, update `build.gradle.kts`, rebuild.

---

## 30. FAQ

**Q: Does this work without an internet connection (e.g., LAN only)?**  
A: Yes. The relay server is self-hosted and the relay URL is configurable. As long as both devices can reach the server on port 8443 (LAN, VPN, or internet), it works.

**Q: What happens when I lose my phone or reinstall the app?**  
A: Your Ed25519 identity keypair is stored in Android Keystore and SharedPreferences. On uninstall, they are deleted. You must regenerate your identity on a new device, and contacts must re-add you via QR exchange. There is no account recovery — by design.

**Q: Can I read messages on multiple devices simultaneously?**  
A: No. The architecture is single-identity, single-device. Multi-device support (linked devices via a secondary keypair mechanism) is on the future roadmap.

**Q: Can the server operator read my messages?**  
A: No. The relay server only sees binary blobs wrapped in conversation-ID envelopes. The conversation ID is `SHA-256(A) + "_" + SHA-256(B)` — not linked to any real identity without having both public keys. Message content is encrypted with keys derived entirely on-device. Even with full server access, messages are unreadable.

**Q: Why not use Signal Protocol / Double Ratchet?**  
A: The SHA-256 ratchet used here provides forward secrecy (past messages are safe) but not break-in recovery (future messages after a state compromise are not automatically recovered). Signal's Double Ratchet adds this at the cost of significantly greater complexity. Cryptika prioritises auditability and simplicity. A future version may upgrade to Double Ratchet.

**Q: What happens if the server's signing key is compromised?**  
A: An attacker with the server's Ed25519 private key can issue fraudulent tickets. These tickets could be used to bind a fake session authorization into K₀. However, they still cannot break the X25519 DH or read existing messages (the server key never touches the session keys). The worst case is the attacker could forge a ticket that causes K₀ to be derived differently — both peers would fail to decrypt each other's messages, making the attack highly visible. The attacker cannot silently read messages.

**Q: Can Cryptika be used over Tor?**  
A: Yes. Route Android traffic through Tor (e.g., via Orbot) or connect to a relay server hosted as a Tor hidden service. Cryptika itself makes no DNS queries that would leak identity.

**Q: How large is the encrypted APK?**  
A: The debug APK is approximately 12–14 MB. The majority is BouncyCastle (~3 MB) and ZXing (~1 MB). The application code itself is small.

**Q: Is there a desktop or iOS version?**  
A: No. Cryptika is Android-only. Desktop and iOS ports would require reimplementing the full crypto pipeline and UI. There are no current plans.

---

## 31. Future Roadmap

| Version | Feature | Technical notes |
|---------|---------|----------------|
| v1.0.x | Stability and bug fixes | Performance profiling, memory leak audit |
| v1.1.0 | Voice notes | Opus-encoded audio messages; same 6-stage pipeline; AAC fallback |
| v1.2.0 | TLS for relay | wss:// and https:// with Let's Encrypt or self-signed cert |
| v1.3.0 | Message reactions | Stored locally as `__RXN__:<counter>:<emoji>` side-channel packets |
| v1.4.0 | FCM wake-up | FCM delivers a zero-content push; app wakes and pulls from relay |
| v1.5.0 | Read receipts | `__READ__:<counter>` signal packet; not persisted on server |
| v2.0.0 | Video calls | WebRTC DataChannel for media; same X25519 DH key derivation per call |
| v2.1.0 | File / image transfer | Chunked binary transfers; same 6-stage encryption; no cloud upload |
| v2.2.0 | Disappearing media | Per-file Keystore key; same expiry mechanism as text |
| v3.0.0 | Group messaging | Sender Keys (similar to Signal Groups) or MLS-based multi-party KEM |
| v3.1.0 | App lock screen | Biometric / PIN lock before entering app; optional auto-lock timer |
| v4.0.0 | Double Ratchet upgrade | Replace SHA-256 chain with HKDF-based Double Ratchet for post-compromise security |
| Future | Sealed sender | Hide sender identity from relay server (à la Signal sealed sender) |
| Future | Multi-device | Linked secondary devices via signed device key bundles |
| Future | Web relay dashboard | Simple web UI to monitor relay server status, connections, buffer size |

---

## 32. License

```
MIT License

Copyright (c) 2025 Cryptika Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

*Cryptika — private by design.*

*The relay is blind. The server learns nothing. Only you and your contact can read your messages.*

*Every key that can be deleted will be deleted. Every past key that can be zeroized is zeroized.*

*Security is not a feature — it is the foundation everything else is built on.*

</div>
