// data/remote/api/AuthApi.kt
package com.cryptika.messenger.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

// Request / Response DTOs

data class EnterRequest(
    val username: String,
    val identityHashHex: String,
    val publicKeyB64: String,
    val signatureB64: String? = null,
    val timestampMs: Long? = null,
    val timestamp_ms: Long? = timestampMs
) {
    constructor(
        username: String,
        identityHashHex: String,
        publicKeyB64: String,
        signatureB64: String? = null,
        timestampMs: Long? = null
    ) : this(
        username = username,
        identityHashHex = identityHashHex,
        publicKeyB64 = publicKeyB64,
        signatureB64 = signatureB64,
        timestampMs = timestampMs,
        timestamp_ms = timestampMs
    )
}

data class EnterResponse(
    val token: String,
    val contactToken: String,
    val expiresAt: Long
)

data class ContactRequestBody(
    val targetUsername: String,
    val nickname: String,
    val identityHashHex: String? = null,
    val publicKeyB64: String? = null
)

data class ContactRequestByFingerprintBody(
    val targetIdentityHash: String,
    val nickname: String,
    val identityHashHex: String? = null,
    val publicKeyB64: String? = null
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

data class AcceptRequestBody(
    val requestId: String,
    val identityHashHex: String? = null,
    val publicKeyB64: String? = null
)

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
    val peerPublicKeyB64: String,
    val peerNickname: String = ""
)

data class AcceptedSessionsResponse(val sessions: List<AcceptedSession>)

data class BurnRequestBody(val forceDisconnect: Boolean = true)

data class BurnResponse(val status: String)

// Retrofit Interface

interface AuthApi {

    @POST
    suspend fun enter(@Url url: String, @Body request: EnterRequest): EnterResponse

    @POST
    suspend fun sendContactRequest(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: ContactRequestBody
    ): ContactRequestResponse

    @POST
    suspend fun sendContactRequestByFingerprint(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: ContactRequestByFingerprintBody
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
        @Header("Authorization") auth: String,
        @Body request: BurnRequestBody = BurnRequestBody(forceDisconnect = true)
    ): BurnResponse
}
