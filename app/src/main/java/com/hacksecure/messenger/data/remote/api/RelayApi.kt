// data/remote/api/RelayApi.kt
package com.cryptika.messenger.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

data class TicketRequest(val a_id: String, val b_id: String)
data class TicketResponse(val ticket_b64: String, val server_public_key_b64: String)
data class PresenceRequest(val identity_hash: String, val connection_token: String)
data class PresenceResponse(val online: Boolean, val token: String?)

interface RelayApi {
    // Full URL supplied at call-time so the server address can change at runtime.
    @POST
    suspend fun requestTicket(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: TicketRequest
    ): TicketResponse

    @POST
    suspend fun registerPresence(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: PresenceRequest
    ): PresenceResponse
}
