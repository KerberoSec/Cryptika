// domain/repository/Repositories.kt — interfaces
package com.cryptika.messenger.domain.repository

import com.cryptika.messenger.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IdentityRepository {
    suspend fun getLocalIdentity(): LocalIdentity?
    suspend fun generateIdentity(): LocalIdentity
    suspend fun deleteIdentity()
}

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun getContact(id: String): Contact?
    suspend fun getContactByIdentityHash(hex: String): Contact?
    suspend fun saveContact(contact: Contact)
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(id: String)
}

interface MessageRepository {
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun saveMessage(message: Message, plaintextBytes: ByteArray)
    suspend fun getMessage(id: String): Message?
    suspend fun updateMessageState(messageId: String, state: MessageState)
    suspend fun deleteExpiredMessages()
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteMessageByCounterAndSender(convId: String, senderHex: String, counter: Long)
    suspend fun deleteConversationMessages(conversationId: String)
    suspend fun getMaxCounter(conversationId: String, senderHex: String): Long
    /** Returns all outgoing messages in FAILED state for a given conversation. */
    suspend fun getFailedMessages(conversationId: String): List<Message>
}
