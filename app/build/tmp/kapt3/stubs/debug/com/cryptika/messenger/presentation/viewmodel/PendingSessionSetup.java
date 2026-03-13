package com.cryptika.messenger.presentation.viewmodel;

import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.api.AcceptRequestResponse;
import com.cryptika.messenger.data.remote.api.PendingRequest;
import com.cryptika.messenger.domain.repository.AuthRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

/**
 * Holds session info while the user is naming the contact and verifying the fingerprint,
 * before the ephemeral session is actually joined.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0015\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u000bH\u00c6\u0003JO\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u000b2\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010 \u001a\u00020!H\u00d6\u0001J\t\u0010\"\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011\u00a8\u0006#"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/PendingSessionSetup;", "", "sessionUUID", "", "expiresAt", "", "serverTime", "peerIdentityHash", "peerPublicKeyB64", "peerNickname", "isRequester", "", "(Ljava/lang/String;JJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", "getExpiresAt", "()J", "()Z", "getPeerIdentityHash", "()Ljava/lang/String;", "getPeerNickname", "getPeerPublicKeyB64", "getServerTime", "getSessionUUID", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "", "toString", "Cryptika_debug"})
public final class PendingSessionSetup {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String sessionUUID = null;
    private final long expiresAt = 0L;
    private final long serverTime = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String peerIdentityHash = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String peerPublicKeyB64 = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String peerNickname = null;
    private final boolean isRequester = false;
    
    public PendingSessionSetup(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, long expiresAt, long serverTime, @org.jetbrains.annotations.NotNull()
    java.lang.String peerIdentityHash, @org.jetbrains.annotations.NotNull()
    java.lang.String peerPublicKeyB64, @org.jetbrains.annotations.NotNull()
    java.lang.String peerNickname, boolean isRequester) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSessionUUID() {
        return null;
    }
    
    public final long getExpiresAt() {
        return 0L;
    }
    
    public final long getServerTime() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPeerIdentityHash() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPeerPublicKeyB64() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPeerNickname() {
        return null;
    }
    
    public final boolean isRequester() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    public final long component3() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.presentation.viewmodel.PendingSessionSetup copy(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, long expiresAt, long serverTime, @org.jetbrains.annotations.NotNull()
    java.lang.String peerIdentityHash, @org.jetbrains.annotations.NotNull()
    java.lang.String peerPublicKeyB64, @org.jetbrains.annotations.NotNull()
    java.lang.String peerNickname, boolean isRequester) {
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