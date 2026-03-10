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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b \b\u0086\b\u0018\u00002\u00020\u0001Bg\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\n\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\f\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0012J\u000f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010#\u001a\u00020\bH\u00c6\u0003J\t\u0010$\u001a\u00020\nH\u00c6\u0003J\t\u0010%\u001a\u00020\fH\u00c6\u0003J\t\u0010&\u001a\u00020\nH\u00c6\u0003J\t\u0010\'\u001a\u00020\u000fH\u00c6\u0003J\t\u0010(\u001a\u00020\fH\u00c6\u0003J\t\u0010)\u001a\u00020\u000fH\u00c6\u0003Jk\u0010*\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\n2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\f2\b\b\u0002\u0010\u0011\u001a\u00020\u000fH\u00c6\u0001J\u0013\u0010+\u001a\u00020\n2\b\u0010,\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010-\u001a\u00020\fH\u00d6\u0001J\t\u0010.\u001a\u00020\u000fH\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\r\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u001bR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0010\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0018R\u0011\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001aR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001b\u00a8\u0006/"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ChatUiState;", "", "messages", "", "Lcom/cryptika/messenger/domain/model/Message;", "contact", "Lcom/cryptika/messenger/domain/model/Contact;", "connectionState", "Lcom/cryptika/messenger/domain/model/ConnectionState;", "sessionEstablished", "", "defaultExpirySeconds", "", "isLoading", "inputText", "", "selectedExpirySeconds", "serverRelayUrl", "(Ljava/util/List;Lcom/cryptika/messenger/domain/model/Contact;Lcom/cryptika/messenger/domain/model/ConnectionState;ZIZLjava/lang/String;ILjava/lang/String;)V", "getConnectionState", "()Lcom/cryptika/messenger/domain/model/ConnectionState;", "getContact", "()Lcom/cryptika/messenger/domain/model/Contact;", "getDefaultExpirySeconds", "()I", "getInputText", "()Ljava/lang/String;", "()Z", "getMessages", "()Ljava/util/List;", "getSelectedExpirySeconds", "getServerRelayUrl", "getSessionEstablished", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "Cryptika_debug"})
public final class ChatUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.cryptika.messenger.domain.model.Message> messages = null;
    @org.jetbrains.annotations.Nullable()
    private final com.cryptika.messenger.domain.model.Contact contact = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.model.ConnectionState connectionState = null;
    private final boolean sessionEstablished = false;
    private final int defaultExpirySeconds = 0;
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String inputText = null;
    private final int selectedExpirySeconds = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String serverRelayUrl = null;
    
    public ChatUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<com.cryptika.messenger.domain.model.Message> messages, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.domain.model.Contact contact, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.ConnectionState connectionState, boolean sessionEstablished, int defaultExpirySeconds, boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.lang.String inputText, int selectedExpirySeconds, @org.jetbrains.annotations.NotNull()
    java.lang.String serverRelayUrl) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.cryptika.messenger.domain.model.Message> getMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.domain.model.Contact getContact() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.ConnectionState getConnectionState() {
        return null;
    }
    
    public final boolean getSessionEstablished() {
        return false;
    }
    
    public final int getDefaultExpirySeconds() {
        return 0;
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getInputText() {
        return null;
    }
    
    public final int getSelectedExpirySeconds() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getServerRelayUrl() {
        return null;
    }
    
    public ChatUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.cryptika.messenger.domain.model.Message> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.domain.model.Contact component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.ConnectionState component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    public final int component8() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.presentation.viewmodel.ChatUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.cryptika.messenger.domain.model.Message> messages, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.domain.model.Contact contact, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.ConnectionState connectionState, boolean sessionEstablished, int defaultExpirySeconds, boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.lang.String inputText, int selectedExpirySeconds, @org.jetbrains.annotations.NotNull()
    java.lang.String serverRelayUrl) {
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