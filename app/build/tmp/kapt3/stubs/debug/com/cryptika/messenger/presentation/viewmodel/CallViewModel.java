package com.cryptika.messenger.presentation.viewmodel;

import android.util.Base64;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.websocket.RelayEvent;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.*;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.cryptika.messenger.data.remote.CallManager;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import java.util.UUID;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\u0018J\u000e\u0010\u001a\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\u001cJ\b\u0010\u001d\u001a\u00020\u0018H\u0014J\u0006\u0010\u001e\u001a\u00020\u0018J\b\u0010\u001f\u001a\u00020\u0018H\u0002J\u000e\u0010 \u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\u001cJ\b\u0010!\u001a\u00020\u0018H\u0002J\u0006\u0010\"\u001a\u00020\u0018J\u0006\u0010#\u001a\u00020\u0018R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\r0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014\u00a8\u0006$"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/CallViewModel;", "Landroidx/lifecycle/ViewModel;", "callManager", "Lcom/cryptika/messenger/data/remote/CallManager;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "(Lcom/cryptika/messenger/data/remote/CallManager;Lcom/cryptika/messenger/domain/repository/ContactRepository;Lcom/cryptika/messenger/domain/repository/IdentityRepository;Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/CallUiState;", "durationJob", "Lkotlinx/coroutines/Job;", "incomingCallData", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/cryptika/messenger/domain/model/IncomingCallData;", "getIncomingCallData", "()Lkotlinx/coroutines/flow/StateFlow;", "uiState", "getUiState", "answerCall", "", "hangup", "initIncomingCall", "contactId", "", "onCleared", "rejectCall", "startDurationCounter", "startOutgoingCall", "stopDurationCounter", "toggleMute", "toggleSpeaker", "Cryptika_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class CallViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.CallManager callManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.CallUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.CallUiState> uiState = null;
    
    /**
     * Mirror of CallManager.incomingCallData � for global nav observer in MainActivity.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.IncomingCallData> incomingCallData = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job durationJob;
    
    @javax.inject.Inject()
    public CallViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.CallManager callManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.CallUiState> getUiState() {
        return null;
    }
    
    /**
     * Mirror of CallManager.incomingCallData � for global nav observer in MainActivity.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.IncomingCallData> getIncomingCallData() {
        return null;
    }
    
    public final void startOutgoingCall(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId) {
    }
    
    public final void initIncomingCall(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId) {
    }
    
    public final void answerCall() {
    }
    
    public final void rejectCall() {
    }
    
    public final void hangup() {
    }
    
    public final void toggleMute() {
    }
    
    public final void toggleSpeaker() {
    }
    
    private final void startDurationCounter() {
    }
    
    private final void stopDurationCounter() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}