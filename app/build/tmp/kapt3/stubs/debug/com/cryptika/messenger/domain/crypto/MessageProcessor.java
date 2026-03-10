package com.cryptika.messenger.domain.crypto;

import com.cryptika.messenger.domain.model.CryptoError;
import com.cryptika.messenger.domain.model.MessageHeader;
import com.cryptika.messenger.domain.model.MessageType;
import com.cryptika.messenger.domain.model.WirePacket;
import com.google.gson.Gson;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * MessageProcessor handles the full send/receive cryptographic pipeline:
 *
 * Send:
 *  plaintext → header → nonce derivation → AEAD encrypt → Ed25519 sign → wire packet
 *
 * Receive (strict order — any step failure rejects message):
 *  1. Parse wire packet
 *  2. Verify Ed25519 signature
 *  3. Verify timestamp (±5 min)
 *  4. Verify counter (strictly > last seen)
 *  5. Advance ratchet to counter position
 *  6. Derive nonce
 *  7. AEAD decrypt with header_hash as additional data
 *  8. Return plaintext
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u0000 $2\u00020\u0001:\u0001$B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\bH\u0002J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0011\u001a\u00020\bJ\u001a\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00100\u00152\u0006\u0010\u0016\u001a\u00020\bJ.\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u000e0\u00152\u0006\u0010\u0018\u001a\u00020\b2\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001cJ\u0010\u0010\u001d\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\u0010H\u0002J\u000e\u0010\u001f\u001a\u00020\b2\u0006\u0010 \u001a\u00020\u0013J\f\u0010!\u001a\u00020\b*\u00020\"H\u0002J\f\u0010#\u001a\u00020\"*\u00020\bH\u0002R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "", "sendRatchet", "Lcom/cryptika/messenger/domain/crypto/HashRatchet;", "recvRatchet", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "peerPublicKeyBytes", "", "myIdentityHash", "gson", "Lcom/google/gson/Gson;", "(Lcom/cryptika/messenger/domain/crypto/HashRatchet;Lcom/cryptika/messenger/domain/crypto/HashRatchet;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;[B[BLcom/google/gson/Gson;)V", "lastSeenRecvCounter", "", "deserializeHeader", "Lcom/cryptika/messenger/domain/model/MessageHeader;", "bytes", "deserializeWirePacket", "Lcom/cryptika/messenger/domain/model/WirePacket;", "receive", "Lkotlin/Pair;", "packetBytes", "send", "plaintext", "expirySeconds", "", "messageType", "Lcom/cryptika/messenger/domain/model/MessageType;", "serializeHeader", "header", "serializeWirePacket", "packet", "hexToByteArray", "", "toHexString", "Companion", "Cryptika_debug"})
public final class MessageProcessor {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.HashRatchet sendRatchet = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.HashRatchet recvRatchet = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final byte[] peerPublicKeyBytes = null;
    @org.jetbrains.annotations.NotNull()
    private final byte[] myIdentityHash = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    public static final long TIMESTAMP_TOLERANCE_MS = 300000L;
    private long lastSeenRecvCounter = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.MessageProcessor.Companion Companion = null;
    
    public MessageProcessor(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.HashRatchet sendRatchet, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.HashRatchet recvRatchet, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    byte[] peerPublicKeyBytes, @org.jetbrains.annotations.NotNull()
    byte[] myIdentityHash, @org.jetbrains.annotations.NotNull()
    com.google.gson.Gson gson) {
        super();
    }
    
    /**
     * Encrypts, signs, and serializes a plaintext message.
     *
     * @param plaintext UTF-8 message text
     * @param expirySeconds 0 = no expiry
     * @param messageType TEXT by default; VOICE_NOTE for Phase 2
     * @return serialized WirePacket bytes ready to send over network
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<byte[], java.lang.Long> send(@org.jetbrains.annotations.NotNull()
    byte[] plaintext, int expirySeconds, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageType messageType) {
        return null;
    }
    
    /**
     * Receives, verifies, and decrypts a wire packet.
     *
     * Strict validation order — any failure throws CryptoError and rejects message.
     *
     * @return Pair of (decrypted plaintext, MessageHeader)
     * @throws CryptoError on any validation failure
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<byte[], com.cryptika.messenger.domain.model.MessageHeader> receive(@org.jetbrains.annotations.NotNull()
    byte[] packetBytes) {
        return null;
    }
    
    private final byte[] serializeHeader(com.cryptika.messenger.domain.model.MessageHeader header) {
        return null;
    }
    
    private final com.cryptika.messenger.domain.model.MessageHeader deserializeHeader(byte[] bytes) {
        return null;
    }
    
    /**
     * Wire packet binary format:
     * [4 bytes big-endian: header_length]
     * [header_length bytes: JSON header]
     * [4 bytes big-endian: ciphertext_length]
     * [ciphertext_length bytes: ciphertext + poly1305 tag]
     * [64 bytes: Ed25519 signature]
     */
    @org.jetbrains.annotations.NotNull()
    public final byte[] serializeWirePacket(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.WirePacket packet) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.WirePacket deserializeWirePacket(@org.jetbrains.annotations.NotNull()
    byte[] bytes) {
        return null;
    }
    
    private final byte[] hexToByteArray(java.lang.String $this$hexToByteArray) {
        return null;
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/MessageProcessor$Companion;", "", "()V", "TIMESTAMP_TOLERANCE_MS", "", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}