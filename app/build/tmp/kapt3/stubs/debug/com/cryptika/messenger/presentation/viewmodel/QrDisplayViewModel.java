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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0010\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\rH\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0014"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/QrDisplayViewModel;", "Landroidx/lifecycle/ViewModel;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "(Lcom/cryptika/messenger/domain/repository/IdentityRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/QrDisplayUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "buildQrPayload", "", "publicKeyBytes", "", "formatFingerprint", "hex", "loadIdentity", "", "Cryptika_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class QrDisplayViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.QrDisplayUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.QrDisplayUiState> uiState = null;
    
    @javax.inject.Inject()
    public QrDisplayViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.QrDisplayUiState> getUiState() {
        return null;
    }
    
    private final void loadIdentity() {
    }
    
    /**
     * QR payload: "cryptika://id/v1/<BASE64URL_NO_PADDING>"
     * Binary: [1 byte version=0x01][32 bytes public key]
     */
    private final java.lang.String buildQrPayload(byte[] publicKeyBytes) {
        return null;
    }
    
    /**
     * Groups fingerprint hex into blocks of 8 for readability
     */
    private final java.lang.String formatFingerprint(java.lang.String hex) {
        return null;
    }
}