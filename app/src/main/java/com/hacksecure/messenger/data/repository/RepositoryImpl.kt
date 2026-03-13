// data/repository/RepositoryImpl.kt
package com.cryptika.messenger.data.repository

import com.cryptika.messenger.data.local.db.*
import com.cryptika.messenger.data.local.keystore.KeystoreManager
import com.cryptika.messenger.domain.crypto.IdentityHash
import com.cryptika.messenger.domain.crypto.IdentityKeyManager
import com.cryptika.messenger.domain.model.*
import com.cryptika.messenger.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import android.util.Base64

class IdentityRepositoryImpl @Inject constructor(
    private val dao: LocalIdentityDao,
    private val identityKeyManager: IdentityKeyManager
) : IdentityRepository {

    override suspend fun getLocalIdentity(): LocalIdentity? {
        val entity = dao.getIdentity() ?: return null
        return LocalIdentity(
            publicKeyBytes = Base64.decode(entity.publicKeyBase64, Base64.NO_WRAP),
            identityHash = entity.identityHashHex.hexToByteArray(),
            createdAt = entity.createdAt
        )
    }

    override suspend fun generateIdentity(): LocalIdentity {
        val (publicKeyBytes, createdAt) = identityKeyManager.generateIdentityIfNeeded()
        val identityHash = IdentityHash.compute(publicKeyBytes)
        val entity = LocalIdentityEntity(
            publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP),
            identityHashHex = identityHash.toHexString(),
            createdAt = createdAt
        )
        dao.saveIdentity(entity)
        return LocalIdentity(publicKeyBytes, identityHash, createdAt)
    }

    override suspend fun deleteIdentity() {
        identityKeyManager.deleteIdentity()
        dao.deleteAll()
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0)
        return ByteArray(length / 2) { i ->
            ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte()
        }
    }
}

class ContactRepositoryImpl @Inject constructor(
    private val dao: ContactDao
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> = dao.getAllContacts().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getContact(id: String): Contact? = dao.getContact(id)?.toDomain()

    override suspend fun getContactByIdentityHash(hex: String): Contact? =
        dao.getContactByIdentityHash(hex)?.toDomain()

    override suspend fun saveContact(contact: Contact) = dao.insertContact(contact.toEntity())

    override suspend fun updateContact(contact: Contact) = dao.updateContact(contact.toEntity())

    override suspend fun deleteContact(id: String) = dao.deleteContact(id)

    private fun ContactEntity.toDomain() = Contact(
        id = id,
        identityHash = identityHashHex.hexToByteArray(),
        publicKeyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP),
        displayName = displayName,
        verifiedAt = verifiedAt,
        keyChangedAt = keyChangedAt
    )

    private fun Contact.toEntity() = ContactEntity(
        id = id,
        identityHashHex = identityHash.toHexString(),
        publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP),
        displayName = displayName,
        verifiedAt = verifiedAt,
        keyChangedAt = keyChangedAt
    )

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0)
        return ByteArray(length / 2) { i ->
            ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte()
        }
    }
}

class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao,
    private val keystoreManager: KeystoreManager
) : MessageRepository {

    override fun getMessages(conversationId: String): Flow<List<Message>> =
        dao.getMessages(conversationId).map { entities ->
            entities.map { entity ->
                val plaintext = if (entity.isDecryptable) {
                    // storage_hash = SHA-256(blob): recompute and verify before decryption (PPT slide 12/13)
                    if (entity.storageHashHex.isNotEmpty()) {
                        val recomputed = MessageDigest.getInstance("SHA-256").digest(entity.ciphertextBlob)
                            .joinToString("") { "%02x".format(it) }
                        if (recomputed != entity.storageHashHex) return@map entity.toDomain(null)
                    }
                    try {
                        keystoreManager.decryptMessageFromStorage(entity.id, entity.ciphertextBlob)
                    } catch (_: Exception) {
                        null
                    }
                } else null

                entity.toDomain(plaintext?.toString(Charsets.UTF_8))
            }
        }

    override suspend fun saveMessage(message: Message, plaintextBytes: ByteArray) {
        // Generate per-message storage key in Keystore
        val storageKeyAlias = keystoreManager.generateMessageKey(message.id)

        // Re-encrypt plaintext with storage key (dual encryption)
        val storedBlob = keystoreManager.encryptMessageForStorage(message.id, plaintextBytes)

        // storage_hash = SHA-256(ciphertextBlob): stored at write time, verified on every read (PPT slide 12/13)
        val storageHashHex = MessageDigest.getInstance("SHA-256").digest(storedBlob)
            .joinToString("") { "%02x".format(it) }

        val entity = MessageEntity(
            id = message.id,
            conversationId = message.conversationId,
            senderIdHex = message.senderId,
            ciphertextBlob = storedBlob,
            storageKeyAlias = storageKeyAlias,
            storageHashHex = storageHashHex,
            headerJson = buildHeaderJson(message),
            timestampMs = message.timestampMs,
            counter = message.counter,
            expiryMs = message.expiryDeadlineMs,
            isDecryptable = true,
            isOutgoing = message.isOutgoing,
            messageType = message.messageType.name,
            messageState = message.state.name
        )
        dao.insertMessage(entity)
    }

    override suspend fun getMessage(id: String): Message? {
        val entity = dao.getMessage(id) ?: return null
        val plaintext = if (entity.isDecryptable) {
            // storage_hash: recompute and verify before decryption (PPT slide 12/13)
            if (entity.storageHashHex.isNotEmpty()) {
                val recomputed = MessageDigest.getInstance("SHA-256").digest(entity.ciphertextBlob)
                    .joinToString("") { "%02x".format(it) }
                if (recomputed != entity.storageHashHex) return entity.toDomain(null)
            }
            try {
                keystoreManager.decryptMessageFromStorage(entity.id, entity.ciphertextBlob)
                    .toString(Charsets.UTF_8)
            } catch (_: Exception) { null }
        } else null
        return entity.toDomain(plaintext)
    }

    override suspend fun deleteExpiredMessages() {
        val now = System.currentTimeMillis()
        val expired = dao.getExpiredMessages(now)
        expired.forEach { entity ->
            // 1. Delete Keystore key — ciphertext becomes unrecoverable noise
            keystoreManager.deleteKeyByAlias(entity.storageKeyAlias)
            // 2. Zeroize ciphertext in DB and mark unrecoverable
            dao.zeroizeAndMarkUnrecoverable(entity.id)
            // 3. Delete the row
            dao.deleteMessage(entity.id)
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        val entity = dao.getMessage(messageId) ?: return
        keystoreManager.deleteKeyByAlias(entity.storageKeyAlias)
        dao.deleteMessage(entity.id)
    }

    override suspend fun deleteMessageByCounterAndSender(convId: String, senderHex: String, counter: Long) {
        val entity = dao.getMessageByCounterAndSender(convId, senderHex, counter) ?: return
        keystoreManager.deleteKeyByAlias(entity.storageKeyAlias)
        dao.deleteMessage(entity.id)
    }

    override suspend fun deleteConversationMessages(conversationId: String) {
        // Delete per-message Keystore keys BEFORE removing DB rows (cryptographic erasure)
        val messages = dao.getAllMessagesForConversation(conversationId)
        messages.forEach { entity ->
            try { keystoreManager.deleteKeyByAlias(entity.storageKeyAlias) } catch (_: Exception) {}
        }
        dao.deleteConversationMessages(conversationId)
    }

    override suspend fun getMaxCounter(conversationId: String, senderHex: String): Long =
        dao.getMaxCounter(conversationId, senderHex) ?: 0L

    override suspend fun updateMessageState(messageId: String, state: MessageState) =
        dao.updateMessageState(messageId, state.name)

    override suspend fun getFailedMessages(conversationId: String): List<Message> =
        dao.getFailedMessages(conversationId).map { entity ->
            val plaintext = if (entity.isDecryptable) {
                // storage_hash: recompute and verify before decryption (PPT slide 12/13)
                if (entity.storageHashHex.isNotEmpty()) {
                    val recomputed = MessageDigest.getInstance("SHA-256").digest(entity.ciphertextBlob)
                        .joinToString("") { "%02x".format(it) }
                    if (recomputed != entity.storageHashHex) return@map entity.toDomain(null)
                }
                try { keystoreManager.decryptMessageFromStorage(entity.id, entity.ciphertextBlob) }
                catch (_: Exception) { null }
            } else null
            entity.toDomain(plaintext?.toString(Charsets.UTF_8))
        }

    private fun MessageEntity.toDomain(plaintext: String?) = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderIdHex,
        content = plaintext ?: "[Message expired]",
        timestampMs = timestampMs,
        counter = counter,
        expirySeconds = 0,
        expiryDeadlineMs = expiryMs,
        isOutgoing = isOutgoing,
        state = if (!isDecryptable) MessageState.EXPIRED
                else try { MessageState.valueOf(messageState) } catch (_: Exception) { MessageState.SENT },
        messageType = try { MessageType.valueOf(messageType) } catch (_: Exception) { MessageType.TEXT }
    )

    private fun buildHeaderJson(message: Message): String =
        """{"id":"${message.id}","ctr":${message.counter},"ts":${message.timestampMs}}"""
}
