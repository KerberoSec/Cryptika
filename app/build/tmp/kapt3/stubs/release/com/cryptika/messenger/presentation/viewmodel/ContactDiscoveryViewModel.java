package com.cryptika.messenger.presentation.viewmodel;

import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse;
import com.cryptika.messenger.data.remote.api.PendingRequest;
import com.cryptika.messenger.domain.model.Contact;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\u0012\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0011J\u0006\u0010\u0015\u001a\u00020\u0011J\u0006\u0010\u0016\u001a\u00020\u0011J\u0006\u0010\u0017\u001a\u00020\u0011J\u000e\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u0013J\u0006\u0010\u001a\u001a\u00020\u0011J\u0006\u0010\u001b\u001a\u00020\u0011J\u0006\u0010\u001c\u001a\u00020\u0011J\u000e\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020\u0013J\u000e\u0010 \u001a\u00020\u00112\u0006\u0010!\u001a\u00020\u0013J\f\u0010\"\u001a\u00020#*\u00020\u0013H\u0002R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006$"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ContactDiscoveryViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "ephemeralSessionManager", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "(Lcom/cryptika/messenger/domain/repository/AuthRepository;Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;Lcom/cryptika/messenger/domain/repository/ContactRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ContactDiscoveryUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptRequest", "", "requestId", "", "cancelSetup", "clearAcceptedSession", "clearError", "clearRequestSent", "confirmSetup", "displayName", "loadPendingRequests", "logout", "pollAcceptedSessions", "rejectRequest", "sendContactRequest", "targetUsername", "sendContactRequestByFingerprint", "targetIdentityHash", "hexToByteArray", "", "Cryptika_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ContactDiscoveryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> uiState = null;
    
    @javax.inject.Inject()
    public ContactDiscoveryViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> getUiState() {
        return null;
    }
    
    public final void sendContactRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String targetUsername) {
    }
    
    public final void sendContactRequestByFingerprint(@org.jetbrains.annotations.NotNull()
    java.lang.String targetIdentityHash) {
    }
    
    public final void loadPendingRequests() {
    }
    
    public final void acceptRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String requestId) {
    }
    
    /**
     * Joins the ephemeral session and saves the contact for calling support within
     * this session, then transitions to the accepted state so navigation occurs.
     */
    public final void confirmSetup(@kotlin.Suppress(names = {"UNUSED_PARAMETER"})
    @org.jetbrains.annotations.NotNull()
    java.lang.String displayName) {
    }
    
    private final byte[] hexToByteArray(java.lang.String $this$hexToByteArray) {
        return null;
    }
    
    public final void cancelSetup() {
    }
    
    public final void rejectRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String requestId) {
    }
    
    public final void clearError() {
    }
    
    public final void clearAcceptedSession() {
    }
    
    public final void clearRequestSent() {
    }
    
    /**
     * Destroy all ephemeral sessions and clear local auth state.
     */
    public final void logout() {
    }
    
    /**
     * Polls the server for sessions accepted by the OTHER party (requester side).
     * Shows the contact setup dialog when a session is found.
     */
    public final void pollAcceptedSessions() {
    }
}