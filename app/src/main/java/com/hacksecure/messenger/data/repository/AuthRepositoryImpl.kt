// data/repository/AuthRepositoryImpl.kt
package com.cryptika.messenger.data.repository

import android.util.Base64
import com.cryptika.messenger.data.local.AuthStore
import com.cryptika.messenger.data.remote.ServerConfig
import com.cryptika.messenger.data.remote.api.*
import com.cryptika.messenger.domain.repository.AuthRepository
import com.cryptika.messenger.domain.repository.IdentityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authStore: AuthStore,
    private val serverConfig: ServerConfig,
    private val identityRepository: IdentityRepository
) : AuthRepository {

    private fun authHeader(): String = "Bearer ${authStore.jwtToken.orEmpty()}"

    override suspend fun enter(username: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val identity = identityRepository.getLocalIdentity()
                    ?: identityRepository.generateIdentity()
                val response = authApi.enter(
                    url = "${serverConfig.apiBaseUrl}/api/v1/auth/enter",
                    request = EnterRequest(
                        username = username,
                        identityHashHex = identity.identityHex,
                        publicKeyB64 = Base64.encodeToString(identity.publicKeyBytes, Base64.NO_WRAP)
                    )
                )
                authStore.jwtToken = response.token
                authStore.contactToken = response.contactToken
                authStore.username = username
                authStore.tokenExpiresAt = response.expiresAt
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try {
                    JSONObject(errorBody ?: "").optString("error", "Entry failed")
                } catch (_: Exception) { "Entry failed" }
                Result.failure(Exception(msg))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun isLoggedIn(): Boolean = authStore.isLoggedIn

    override fun getUsername(): String? = authStore.username

    override fun getContactToken(): String? = authStore.contactToken

    override fun getJwtToken(): String? = authStore.jwtToken

    override fun logout() {
        authStore.clear()
    }

    override suspend fun sendContactRequest(targetUsername: String, nickname: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                authApi.sendContactRequest(
                    url = "${serverConfig.apiBaseUrl}/api/v1/contact/request",
                    auth = authHeader(),
                    request = ContactRequestBody(targetUsername, nickname)
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getPendingRequests(): Result<List<PendingRequest>> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApi.getPendingRequests(
                    url = "${serverConfig.apiBaseUrl}/api/v1/contact/requests",
                    auth = authHeader()
                )
                Result.success(response.requests)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun acceptRequest(requestId: String): Result<AcceptRequestResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApi.acceptContactRequest(
                    url = "${serverConfig.apiBaseUrl}/api/v1/contact/accept",
                    auth = authHeader(),
                    request = AcceptRequestBody(requestId)
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun rejectRequest(requestId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                authApi.rejectContactRequest(
                    url = "${serverConfig.apiBaseUrl}/api/v1/contact/reject",
                    auth = authHeader(),
                    request = RejectRequestBody(requestId)
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAcceptedSessions(): Result<List<AcceptedSession>> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApi.getAcceptedSessions(
                    url = "${serverConfig.apiBaseUrl}/api/v1/contact/accepted",
                    auth = authHeader()
                )
                Result.success(response.sessions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun burnCredentials(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                authApi.burnCredentials(
                    url = "${serverConfig.apiBaseUrl}/api/v1/auth/burn",
                    auth = authHeader()
                )
                authStore.credentialsBurned = true
                Result.success(Unit)
            } catch (e: Exception) {
                // Even if server call fails, mark as burned locally to prevent re-use attempts
                authStore.credentialsBurned = true
                Result.failure(e)
            }
        }

    override fun isCredentialsBurned(): Boolean = authStore.credentialsBurned
}
