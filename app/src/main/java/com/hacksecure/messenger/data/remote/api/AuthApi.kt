// data/remote/api/AuthApi.kt
package com.cryptika.messenger.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

// ── Request / Response DTOs ──────────────────────────────────────────────────

data class RegisterRequest(
    val username: String,
    val password: String,
    val identityHashHex: String,
    val publicKeyB64: String
)

data class RegisterResponse(val status: String)

data class LoginRequest(
    val username: String,
    val password: String,
    val identityHashHex: String,
    val publicKeyB64: String
)

data class LoginResponse(
    val token: String,
    val contactToken: String,
    val expiresAt: Long
)

data class ContactRequestBody(
    val targetUsername: String,
    val nickname: String
)

data class ContactRequestResponse(val status: String)

data class PendingRequest(
    val requestId: String,
    val fromToken: String,
    val fromIdentityHash: String,
    val fromPublicKeyB64: String,
    val fromNickname: String,
    val createdAt: Long
)

data class PendingRequestsResponse(val requests: List<PendingRequest>)

data class AcceptRequestBody(val requestId: String)

data class AcceptRequestResponse(
    val sessionUUID: String,
    val expiresAt: Long,
    val serverTime: Long,
    val peerIdentityHash: String,
    val peerPublicKeyB64: String,
    val peerNickname: String
)

data class RejectRequestBody(val requestId: String)
data class RejectRequestResponse(val status: String)

data class AcceptedSession(
    val sessionUUID: String,
    val expiresAt: Long,
    val serverTime: Long,
    val peerIdentityHash: String,
    val peerPublicKeyB64: String
)

data class AcceptedSessionsResponse(val sessions: List<AcceptedSession>)

data class BurnResponse(val status: String)

// ── Retrofit Interface ───────────────────────────────────────────────────────

interface AuthApi {

    @POST
    suspend fun register(@Url url: String, @Body request: RegisterRequest): RegisterResponse

    @POST
    suspend fun login(@Url url: String, @Body request: LoginRequest): LoginResponse

    @POST
    suspend fun sendContactRequest(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: ContactRequestBody
    ): ContactRequestResponse

    @GET
    suspend fun getPendingRequests(
        @Url url: String,
        @Header("Authorization") auth: String
    ): PendingRequestsResponse

    @POST
    suspend fun acceptContactRequest(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: AcceptRequestBody
    ): AcceptRequestResponse

    @POST
    suspend fun rejectContactRequest(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: RejectRequestBody
    ): RejectRequestResponse

    @GET
    suspend fun getAcceptedSessions(
        @Url url: String,
        @Header("Authorization") auth: String
    ): AcceptedSessionsResponse

    @POST
    suspend fun burnCredentials(
        @Url url: String,
        @Header("Authorization") auth: String
    ): BurnResponse
}
