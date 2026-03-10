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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011J\u001e\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0017R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0018"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ContactConfirmViewModel;", "Landroidx/lifecycle/ViewModel;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "(Lcom/cryptika/messenger/domain/repository/ContactRepository;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ConfirmState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "forceUpdateKey", "", "existing", "Lcom/cryptika/messenger/domain/model/Contact;", "newPublicKeyBytes", "", "newIdentityHash", "saveContact", "publicKeyBytes", "identityHash", "displayName", "", "Cryptika_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ContactConfirmViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.ConfirmState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ConfirmState> state = null;
    
    @javax.inject.Inject()
    public ContactConfirmViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ConfirmState> getState() {
        return null;
    }
    
    public final void saveContact(@org.jetbrains.annotations.NotNull()
    byte[] publicKeyBytes, @org.jetbrains.annotations.NotNull()
    byte[] identityHash, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName) {
    }
    
    public final void forceUpdateKey(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact existing, @org.jetbrains.annotations.NotNull()
    byte[] newPublicKeyBytes, @org.jetbrains.annotations.NotNull()
    byte[] newIdentityHash) {
    }
}