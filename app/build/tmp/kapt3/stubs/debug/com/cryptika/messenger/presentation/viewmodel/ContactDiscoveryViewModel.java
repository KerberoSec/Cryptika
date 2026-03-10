package com.cryptika.messenger.presentation.viewmodel;

import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse;
import com.cryptika.messenger.data.remote.api.PendingRequest;
import com.cryptika.messenger.domain.repository.AuthRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0012\u001a\u00020\u000fJ\u0006\u0010\u0013\u001a\u00020\u000fJ\u0006\u0010\u0014\u001a\u00020\u000fJ\u0006\u0010\u0015\u001a\u00020\u000fJ\u0006\u0010\u0016\u001a\u00020\u000fJ\u000e\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u0011R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u001a"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ContactDiscoveryViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "ephemeralSessionManager", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "(Lcom/cryptika/messenger/domain/repository/AuthRepository;Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ContactDiscoveryUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptRequest", "", "requestId", "", "clearAcceptedSession", "clearError", "clearRequestSent", "loadPendingRequests", "pollAcceptedSessions", "rejectRequest", "sendContactRequest", "targetUsername", "Cryptika_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ContactDiscoveryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> uiState = null;
    
    @javax.inject.Inject()
    public ContactDiscoveryViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState> getUiState() {
        return null;
    }
    
    public final void sendContactRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String targetUsername) {
    }
    
    public final void loadPendingRequests() {
    }
    
    public final void acceptRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String requestId) {
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
     * Polls the server for sessions accepted by the OTHER party (requester side).
     * When a session is found, joins it via EphemeralSessionManager and surfaces it.
     */
    public final void pollAcceptedSessions() {
    }
}