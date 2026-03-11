// presentation/viewmodel/AuthViewModel.kt
package com.cryptika.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptika.messenger.data.remote.EphemeralSessionManager
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse
import com.cryptika.messenger.data.remote.api.PendingRequest
import com.cryptika.messenger.domain.repository.AuthRepository
import com.cryptika.messenger.domain.repository.IdentityRepository
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
    val registerSuccess: Boolean = false,
    val username: String? = null,
    val credentialsBurned: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(
        isLoggedIn = authRepository.isLoggedIn(),
        credentialsBurned = authRepository.isCredentialsBurned()
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(username: String, password: String) {
        if (username.length < 2) {
            _uiState.update { it.copy(error = "Username must be at least 2 characters") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _uiState.update { it.copy(error = "Username can only contain letters, numbers, and underscores") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(username, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Registration failed: ${e.message}") }
                }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(username, password)
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
                    _uiState.update { it.copy(isLoading = false, error = "Login failed: ${e.message}") }
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

    fun clearRegisterSuccess() {
        _uiState.update { it.copy(registerSuccess = false) }
    }

    /**
     * Re-registration: delete old identity, clear burned state, generate new identity,
     * then register with new credentials for a new ephemeral session.
     */
    fun reRegister(username: String, password: String) {
        if (username.length < 2) {
            _uiState.update { it.copy(error = "Username must be at least 2 characters") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _uiState.update { it.copy(error = "Username can only contain letters, numbers, and underscores") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Delete old identity — new identity key pair for unlinkability
                identityRepository.deleteIdentity()
                // 2. Generate fresh identity
                identityRepository.generateIdentity()
                // 3. Clear burned state and logout old session
                authRepository.logout()
                _uiState.update { it.copy(credentialsBurned = false) }

                // 4. Register with new identity
                authRepository.register(username, password)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, registerSuccess = true, isLoggedIn = false) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = "Re-registration failed: ${e.message}") }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Re-registration failed: ${e.message}") }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTACT DISCOVERY UI STATE
// ══════════════════════════════════════════════════════════════════════════════
data class ContactDiscoveryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val requestSent: Boolean = false,
    val pendingRequests: List<PendingRequest> = emptyList(),
    val acceptedSession: AcceptRequestResponse? = null
)

@HiltViewModel
class ContactDiscoveryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val ephemeralSessionManager: EphemeralSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactDiscoveryUiState())
    val uiState: StateFlow<ContactDiscoveryUiState> = _uiState.asStateFlow()

    fun sendContactRequest(targetUsername: String) {
        val nickname = authRepository.getUsername() ?: "Anonymous"
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
                    // Join the ephemeral session via EphemeralSessionManager
                    ephemeralSessionManager.joinSession(
                        sessionUUID = response.sessionUUID,
                        expiresAt = response.expiresAt,
                        peerIdentityHash = response.peerIdentityHash,
                        peerPublicKeyB64 = response.peerPublicKeyB64,
                        peerNickname = response.peerNickname
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            acceptedSession = response,
                            pendingRequests = it.pendingRequests.filter { p -> p.requestId != requestId }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
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

    /**
     * Polls the server for sessions accepted by the OTHER party (requester side).
     * When a session is found, joins it via EphemeralSessionManager and surfaces it.
     */
    fun pollAcceptedSessions() {
        viewModelScope.launch {
            authRepository.getAcceptedSessions()
                .onSuccess { sessions ->
                    val session = sessions.firstOrNull() ?: return@onSuccess
                    // Join this session
                    ephemeralSessionManager.joinSession(
                        sessionUUID = session.sessionUUID,
                        expiresAt = session.expiresAt,
                        peerIdentityHash = session.peerIdentityHash,
                        peerPublicKeyB64 = session.peerPublicKeyB64,
                        peerNickname = "Contact"  // server doesn't return nickname for requester
                    )
                    _uiState.update {
                        it.copy(
                            acceptedSession = AcceptRequestResponse(
                                sessionUUID = session.sessionUUID,
                                expiresAt = session.expiresAt,
                                serverTime = session.serverTime,
                                peerIdentityHash = session.peerIdentityHash,
                                peerPublicKeyB64 = session.peerPublicKeyB64,
                                peerNickname = "Contact"
                            )
                        )
                    }
                }
                .onFailure { /* silently ignore poll failures */ }
        }
    }
}
