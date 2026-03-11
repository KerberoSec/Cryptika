package com.cryptika.messenger.presentation.viewmodel;

import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse;
import com.cryptika.messenger.data.remote.api.PendingRequest;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BM\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\rH\u00c6\u0003JQ\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rH\u00c6\u0001J\u0013\u0010 \u001a\u00020\u00032\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020#H\u00d6\u0001J\t\u0010$\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0013R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013\u00a8\u0006%"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ContactDiscoveryUiState;", "", "isLoading", "", "error", "", "requestSent", "pendingRequests", "", "Lcom/cryptika/messenger/data/remote/api/PendingRequest;", "acceptedSession", "Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;", "pendingSetup", "Lcom/cryptika/messenger/presentation/viewmodel/PendingSessionSetup;", "(ZLjava/lang/String;ZLjava/util/List;Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;Lcom/cryptika/messenger/presentation/viewmodel/PendingSessionSetup;)V", "getAcceptedSession", "()Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;", "getError", "()Ljava/lang/String;", "()Z", "getPendingRequests", "()Ljava/util/List;", "getPendingSetup", "()Lcom/cryptika/messenger/presentation/viewmodel/PendingSessionSetup;", "getRequestSent", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "", "toString", "Cryptika_debug"})
public final class ContactDiscoveryUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    private final boolean requestSent = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.cryptika.messenger.data.remote.api.PendingRequest> pendingRequests = null;
    @org.jetbrains.annotations.Nullable()
    private final com.cryptika.messenger.data.remote.api.AcceptRequestResponse acceptedSession = null;
    @org.jetbrains.annotations.Nullable()
    private final com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup pendingSetup = null;
    
    public ContactDiscoveryUiState(boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String error, boolean requestSent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.cryptika.messenger.data.remote.api.PendingRequest> pendingRequests, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.data.remote.api.AcceptRequestResponse acceptedSession, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup pendingSetup) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public final boolean getRequestSent() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.cryptika.messenger.data.remote.api.PendingRequest> getPendingRequests() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.data.remote.api.AcceptRequestResponse getAcceptedSession() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup getPendingSetup() {
        return null;
    }
    
    public ContactDiscoveryUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.cryptika.messenger.data.remote.api.PendingRequest> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.data.remote.api.AcceptRequestResponse component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryUiState copy(boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String error, boolean requestSent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.cryptika.messenger.data.remote.api.PendingRequest> pendingRequests, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.data.remote.api.AcceptRequestResponse acceptedSession, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup pendingSetup) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}