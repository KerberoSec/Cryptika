// domain/repository/AuthRepository.kt
package com.cryptika.messenger.domain.repository

import com.cryptika.messenger.data.remote.api.AcceptRequestResponse
import com.cryptika.messenger.data.remote.api.AcceptedSession
import com.cryptika.messenger.data.remote.api.PendingRequest

/** Repository for authentication and contact discovery operations. */
interface AuthRepository {
    /** Passwordless entry — username only. */
    suspend fun enter(username: String): Result<Unit>
    fun isLoggedIn(): Boolean
    fun getUsername(): String?
    fun getContactToken(): String?
    fun getJwtToken(): String?
    fun logout()

    suspend fun sendContactRequest(targetUsername: String, nickname: String): Result<Unit>
    suspend fun getPendingRequests(): Result<List<PendingRequest>>
    suspend fun acceptRequest(requestId: String): Result<AcceptRequestResponse>
    suspend fun rejectRequest(requestId: String): Result<Unit>
    suspend fun getAcceptedSessions(): Result<List<AcceptedSession>>

    /** Burns credentials on the server and marks local auth as burned. */
    suspend fun burnCredentials(): Result<Unit>

    /** Whether credentials have been burned (server-side deletion complete). */
    fun isCredentialsBurned(): Boolean
}
