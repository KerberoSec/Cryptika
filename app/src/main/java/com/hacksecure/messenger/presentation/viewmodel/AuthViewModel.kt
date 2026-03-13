// presentation/viewmodel/AuthViewModel.kt
package com.cryptika.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptika.messenger.data.remote.EphemeralSessionManager
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse
import com.cryptika.messenger.data.remote.api.PendingRequest
import com.cryptika.messenger.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════════════════════
// AUTH UI STATE
// ══════════════════════════════════════════════════════════════════════════════
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val username: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(
        isLoggedIn = authRepository.isLoggedIn()
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Passwordless entry — just a public username, case-sensitive. */
    fun enter(username: String) {
        if (username.isBlank()) {
            _uiState.update { it.copy(error = "Username cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Identity is stable across logins: rotating on every login would
            // break Ed25519 signature verification for all existing contacts,
            // silently killing calls and messages. Explicit rotation is still
            // available via the "Regenerate Identity" button in Settings.
            authRepository.enter(username)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            username = authRepository.getUsername()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Entry failed: ${e.message}") }
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { AuthUiState(isLoggedIn = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTACT DISCOVERY UI STATE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Holds session info while the user is naming the contact and verifying the fingerprint,
 * before the ephemeral session is actually joined.
 */
data class PendingSessionSetup(
    val sessionUUID: String,
    val expiresAt: Long,
    val serverTime: Long,
    val peerIdentityHash: String,
    val peerPublicKeyB64: String,
    val peerNickname: String,
    val isRequester: Boolean = false
)

data class ContactDiscoveryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val requestSent: Boolean = false,
    val pendingRequests: List<PendingRequest> = emptyList(),
    val acceptedSession: AcceptRequestResponse? = null,
    val pendingSetup: PendingSessionSetup? = null
)

@HiltViewModel
class ContactDiscoveryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val ephemeralSessionManager: EphemeralSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactDiscoveryUiState())
    val uiState: StateFlow<ContactDiscoveryUiState> = _uiState.asStateFlow()

    fun sendContactRequest(targetUsername: String) {
        val nickname = "User_${java.util.UUID.randomUUID().toString().take(8)}"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, requestSent = false) }
            authRepository.sendContactRequest(targetUsername, nickname)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, requestSent = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun sendContactRequestByFingerprint(targetIdentityHash: String) {
        val nickname = "User_${java.util.UUID.randomUUID().toString().take(8)}"
        val normalized = targetIdentityHash.trim().lowercase().replace(" ", "")
        if (!normalized.matches(Regex("^[a-f0-9]{64}$"))) {
            _uiState.update { it.copy(error = "Fingerprint must be exactly 64 hex characters") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, requestSent = false) }
            authRepository.sendContactRequestByFingerprint(normalized, nickname)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, requestSent = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(isLoading = false, pendingRequests = requests) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.acceptRequest(requestId)
                .onSuccess { response ->
                    // Don't join yet — show contact setup dialog first
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingRequests = it.pendingRequests.filter { p -> p.requestId != requestId },
                            pendingSetup = PendingSessionSetup(
                                sessionUUID = response.sessionUUID,
                                expiresAt = response.expiresAt,
                                serverTime = response.serverTime,
                                peerIdentityHash = response.peerIdentityHash,
                                peerPublicKeyB64 = response.peerPublicKeyB64,
                                peerNickname = response.peerNickname
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Completes the contact setup: joins the ephemeral session with the user-chosen display name,
     * then transitions to the accepted state so navigation occurs.
     */
    fun confirmSetup(@Suppress("UNUSED_PARAMETER") displayName: String) {
        val setup = _uiState.value.pendingSetup ?: return
        val randomName = "User_${java.util.UUID.randomUUID().toString().take(8)}"
        viewModelScope.launch {
            ephemeralSessionManager.joinSession(
                sessionUUID = setup.sessionUUID,
                expiresAt = setup.expiresAt,
                peerIdentityHash = setup.peerIdentityHash,
                peerPublicKeyB64 = setup.peerPublicKeyB64,
                peerNickname = randomName
            )
            _uiState.update {
                it.copy(
                    pendingSetup = null,
                    acceptedSession = AcceptRequestResponse(
                        sessionUUID = setup.sessionUUID,
                        expiresAt = setup.expiresAt,
                        serverTime = setup.serverTime,
                        peerIdentityHash = setup.peerIdentityHash,
                        peerPublicKeyB64 = setup.peerPublicKeyB64,
                        peerNickname = randomName
                    )
                )
            }
        }
    }

    fun cancelSetup() {
        _uiState.update { it.copy(pendingSetup = null) }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.rejectRequest(requestId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingRequests = it.pendingRequests.filter { p -> p.requestId != requestId }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearAcceptedSession() {
        _uiState.update { it.copy(acceptedSession = null) }
    }

    fun clearRequestSent() {
        _uiState.update { it.copy(requestSent = false) }
    }

    /** Logout: destroy all sessions, clear auth state. */
    fun logout() {
        ephemeralSessionManager.destroyAllSessions()
        authRepository.logout()
    }

    /**
     * Polls the server for sessions accepted by the OTHER party (requester side).
     * When a session is found, joins it via EphemeralSessionManager and surfaces it.
     */
    fun pollAcceptedSessions() {
        viewModelScope.launch {
            // Don't poll if user is already setting up a contact
            if (_uiState.value.pendingSetup != null) return@launch
            authRepository.getAcceptedSessions()
                .onSuccess { sessions ->
                    val session = sessions.firstOrNull() ?: return@onSuccess
                    // Show contact setup dialog instead of joining immediately
                    _uiState.update {
                        it.copy(
                            pendingSetup = PendingSessionSetup(
                                sessionUUID = session.sessionUUID,
                                expiresAt = session.expiresAt,
                                serverTime = session.serverTime,
                                peerIdentityHash = session.peerIdentityHash,
                                peerPublicKeyB64 = session.peerPublicKeyB64,
                                peerNickname = session.peerNickname.ifBlank { "Contact" },
                                isRequester = true
                            )
                        )
                    }
                }
                .onFailure { /* silently ignore poll failures */ }
        }
    }
}
