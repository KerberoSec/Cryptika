package com.cryptika.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;
import androidx.compose.runtime.*;
import androidx.navigation.*;
import androidx.navigation.compose.*;
import com.cryptika.messenger.data.local.db.ContactDao;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.local.db.MessageDao;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.presentation.ui.screens.*;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel;
import com.cryptika.messenger.worker.MessageExpiryWorker;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.*;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u00103\u001a\u0002042\b\u00105\u001a\u0004\u0018\u000106H\u0014J\b\u00107\u001a\u000204H\u0014J\b\u00108\u001a\u000204H\u0014J\b\u00109\u001a\u000204H\u0014J\u0006\u0010:\u001a\u000204R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001e\u0010\u0015\u001a\u00020\u00168\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u001e\u0010\u001b\u001a\u00020\u001c8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u001e\"\u0004\b\u001f\u0010 R\u001e\u0010!\u001a\u00020\"8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010$\"\u0004\b%\u0010&R\u001e\u0010\'\u001a\u00020(8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010*\"\u0004\b+\u0010,R\u000e\u0010-\u001a\u00020.X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00101\u001a\u000202X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006;"}, d2 = {"Lcom/cryptika/messenger/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "authRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "getAuthRepository", "()Lcom/cryptika/messenger/domain/repository/AuthRepository;", "setAuthRepository", "(Lcom/cryptika/messenger/domain/repository/AuthRepository;)V", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "getBackgroundConnectionManager", "()Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "setBackgroundConnectionManager", "(Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;)V", "contactDao", "Lcom/cryptika/messenger/data/local/db/ContactDao;", "getContactDao", "()Lcom/cryptika/messenger/data/local/db/ContactDao;", "setContactDao", "(Lcom/cryptika/messenger/data/local/db/ContactDao;)V", "conversationDao", "Lcom/cryptika/messenger/data/local/db/ConversationDao;", "getConversationDao", "()Lcom/cryptika/messenger/data/local/db/ConversationDao;", "setConversationDao", "(Lcom/cryptika/messenger/data/local/db/ConversationDao;)V", "ephemeralSessionManager", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "getEphemeralSessionManager", "()Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "setEphemeralSessionManager", "(Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;)V", "keystoreManager", "Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;", "getKeystoreManager", "()Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;", "setKeystoreManager", "(Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;)V", "messageDao", "Lcom/cryptika/messenger/data/local/db/MessageDao;", "getMessageDao", "()Lcom/cryptika/messenger/data/local/db/MessageDao;", "setMessageDao", "(Lcom/cryptika/messenger/data/local/db/MessageDao;)V", "screenOffReceiver", "Landroid/content/BroadcastReceiver;", "wipeInProgress", "", "wipeScope", "Lkotlinx/coroutines/CoroutineScope;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onResume", "onStop", "performFullWipe", "Cryptika_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager;
    @javax.inject.Inject()
    public com.cryptika.messenger.domain.repository.AuthRepository authRepository;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.local.db.MessageDao messageDao;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.local.db.ContactDao contactDao;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.local.db.ConversationDao conversationDao;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.local.keystore.KeystoreManager keystoreManager;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope wipeScope = null;
    
    /**
     * Guard: prevent two simultaneous wipe+restart cycles.
     */
    @kotlin.jvm.Volatile()
    private volatile boolean wipeInProgress = false;
    
    /**
     * Screen-off → full wipe + force re-register
     */
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver screenOffReceiver = null;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.EphemeralSessionManager getEphemeralSessionManager() {
        return null;
    }
    
    public final void setEphemeralSessionManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.EphemeralSessionManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.BackgroundConnectionManager getBackgroundConnectionManager() {
        return null;
    }
    
    public final void setBackgroundConnectionManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.AuthRepository getAuthRepository() {
        return null;
    }
    
    public final void setAuthRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.AuthRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.MessageDao getMessageDao() {
        return null;
    }
    
    public final void setMessageDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.MessageDao p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.ContactDao getContactDao() {
        return null;
    }
    
    public final void setContactDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ContactDao p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.ConversationDao getConversationDao() {
        return null;
    }
    
    public final void setConversationDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ConversationDao p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.keystore.KeystoreManager getKeystoreManager() {
        return null;
    }
    
    public final void setKeystoreManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.keystore.KeystoreManager p0) {
    }
    
    /**
     * Full session teardown:
     * 1. Destroy WebSocket sessions
     * 2. Cryptographically erase all message Keystore keys (ciphertext becomes noise)
     * 3. Hard-delete all messages, contacts and conversations from the DB
     * 4. Clear auth tokens
     * 5. Restart activity so the nav graph resets to AUTH
     */
    public final void performFullWipe() {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    /**
     * Home / Back / Minimize → full data wipe and force re-login.
     * Not triggered on configuration changes (e.g. screen rotation).
     */
    @java.lang.Override()
    protected void onStop() {
    }
}