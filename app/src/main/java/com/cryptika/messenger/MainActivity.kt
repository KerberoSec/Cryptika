// Navigation.kt + MainActivity.kt
package com.cryptika.messenger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.cryptika.messenger.data.local.db.ContactDao
import com.cryptika.messenger.data.local.db.ConversationDao
import com.cryptika.messenger.data.local.db.MessageDao
import com.cryptika.messenger.data.local.keystore.KeystoreManager
import com.cryptika.messenger.data.remote.BackgroundConnectionManager
import com.cryptika.messenger.data.remote.EphemeralSessionManager
import com.cryptika.messenger.domain.repository.AuthRepository
import com.cryptika.messenger.presentation.ui.screens.*
import com.cryptika.messenger.presentation.ui.theme.CryptikaTheme
import com.cryptika.messenger.presentation.viewmodel.CallViewModel
import com.cryptika.messenger.worker.MessageExpiryWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

// NAVIGATION ROUTES
object Routes {
    const val AUTH             = "auth"
    const val SPLASH           = "splash"
    const val HOME             = "home"
    const val QR_DISPLAY       = "qr_display"
    const val QR_SCAN          = "qr_scan"
    const val CONTACT_CONFIRM  = "contact_confirm/{pubKeyB64}"
    const val CONTACT_DISCOVERY = "contact_discovery"
    const val CHAT             = "chat/{contactId}"
    const val EPHEMERAL_CHAT   = "ephemeral_chat/{sessionUUID}"
    const val SETTINGS         = "settings"
    /** Call screen: contactId + isIncoming flag */
    const val CALL             = "call/{contactId}/{isIncoming}"

    fun contactConfirm(pubKeyB64: String) = "contact_confirm/$pubKeyB64"
    fun chat(contactId: String) = "chat/$contactId"
    fun ephemeralChat(sessionUUID: String) = "ephemeral_chat/$sessionUUID"
    fun call(contactId: String, isIncoming: Boolean = false) =
        "call/$contactId/$isIncoming"
}

// MAIN ACTIVITY
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var ephemeralSessionManager: EphemeralSessionManager
    @Inject lateinit var backgroundConnectionManager: BackgroundConnectionManager
    @Inject lateinit var authRepository: AuthRepository

    // DAOs for cryptographic erasure on every wipe
    @Inject lateinit var messageDao: MessageDao
    @Inject lateinit var contactDao: ContactDao
    @Inject lateinit var conversationDao: ConversationDao
    @Inject lateinit var keystoreManager: KeystoreManager

    private val wipeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Guard: prevent two simultaneous wipe+restart cycles. */
    @Volatile private var wipeInProgress = false

    /**
     * Full session teardown:
     *  1. Destroy WebSocket sessions
     *  2. Cryptographically erase all message Keystore keys (ciphertext becomes noise)
     *  3. Hard-delete all messages, contacts and conversations from the DB
     *  4. Clear auth tokens
     *  5. Restart activity so the nav graph resets to AUTH
     */
    fun performFullWipe() {
        if (wipeInProgress) return
        wipeInProgress = true
        wipeScope.launch {
            withContext(Dispatchers.IO) {
                // Stop live connections
                try { ephemeralSessionManager.destroyAllSessions() } catch (_: Exception) {}
                try { backgroundConnectionManager.stopAll() } catch (_: Exception) {}

                // Delete every per-message Keystore key so ciphertext becomes unrecoverable
                try {
                    val aliases = messageDao.getAllStorageKeyAliases()
                    for (alias in aliases) {
                        try { keystoreManager.deleteKeyByAlias(alias) } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}

                // Hard-delete all persisted data
                try { messageDao.deleteAllMessages() } catch (_: Exception) {}
                try { contactDao.deleteAllContacts() } catch (_: Exception) {}
                try { conversationDao.deleteAllConversations() } catch (_: Exception) {}
            }

            // Clear auth tokens last (on main thread as SharedPreferences edit is safe there)
            try { authRepository.logout() } catch (_: Exception) {}

            // Restart to AUTH screen with a clean back-stack
            val restart = Intent(this@MainActivity, MainActivity::class.java)
            restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(restart)
            finish()
        }
    }

    /** Screen-off → full wipe + force re-register */
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                performFullWipe()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Screenshot blocking is applied per-chat inside ChatScreen via FLAG_SECURE
        MessageExpiryWorker.schedule(this)
        MessageExpiryWorker.runOnce(this)

        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        // Register callback so incoming FORCE_LOGOUT packets (peer pressed back from chat)
        // trigger a full wipe on this device too.
        backgroundConnectionManager.forceLogoutCallback = ::performFullWipe

        setContent {
            CryptikaTheme {
                CryptikaNavGraph(onFullWipe = ::performFullWipe)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(screenOffReceiver) } catch (_: Exception) {}
        wipeScope.cancel()
    }

    override fun onResume() {
        super.onResume()
        // Reset the guard so the next onStop can trigger a fresh wipe
        wipeInProgress = false
    }

    /**
     * Home / Back / Minimize → full data wipe and force re-login.
     * Not triggered on configuration changes (e.g. screen rotation).
     */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            performFullWipe()
        }
    }
}

@Composable
fun CryptikaNavGraph(onFullWipe: () -> Unit = {}) {
    val navController = rememberNavController()

    // Activity-scoped CallViewModel: watches for global incoming calls across all screens
    val callViewModel: CallViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val incomingCall by callViewModel.incomingCallData.collectAsState()

    // Auto-navigate to CallScreen when an incoming call arrives from any screen
    LaunchedEffect(incomingCall) {
        val data = incomingCall ?: return@LaunchedEffect
        val current = navController.currentBackStackEntry?.destination?.route
        if (current != Routes.CALL) {
            navController.navigate(Routes.call(data.contactId, isIncoming = true))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.AUTH
    ) {
        // Shared force-logout: perform full cryptographic wipe so AUTH is reached clean
        val forceLogout: () -> Unit = onFullWipe

        // Auth
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // Splash
        composable(Routes.SPLASH) {
            SplashScreen(
                onReady = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToQrDisplay = { navController.navigate(Routes.QR_DISPLAY) },
                onNavigateToQrScan = { navController.navigate(Routes.QR_SCAN) },
                onNavigateToChat = { contactId -> navController.navigate(Routes.chat(contactId)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToContactDiscovery = { navController.navigate(Routes.CONTACT_DISCOVERY) }
            )
        }

        // Contact Discovery
        composable(Routes.CONTACT_DISCOVERY) {
            ContactDiscoveryScreen(
                onBack = { navController.popBackStack() },
                onLogout = forceLogout,
                onSessionCreated = { sessionUUID, _, _, _ ->
                    navController.navigate(Routes.ephemeralChat(sessionUUID)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        // Ephemeral Chat
        composable(
            Routes.EPHEMERAL_CHAT,
            arguments = listOf(navArgument("sessionUUID") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionUUID = backStackEntry.arguments?.getString("sessionUUID") ?: ""
            ChatScreen(
                contactId = "",
                sessionUUID = sessionUUID,
                onBack = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onStartCall = {
                    navController.navigate(Routes.call("${sessionUUID}_ephemeral", isIncoming = false))
                },
                onForceLogout = forceLogout
            )
        }

        // QR Display
        composable(Routes.QR_DISPLAY) {
            QrDisplayScreen(onBack = { navController.popBackStack() })
        }

        // QR Scan
        composable(Routes.QR_SCAN) {
            QrScanScreen(
                onBack = { navController.popBackStack() },
                onScanSuccess = { pubKeyB64 ->
                    navController.navigate(Routes.contactConfirm(pubKeyB64))
                }
            )
        }

        // Contact Confirmation
        composable(
            Routes.CONTACT_CONFIRM,
            arguments = listOf(navArgument("pubKeyB64") { type = NavType.StringType })
        ) { backStackEntry ->
            val pubKeyB64 = backStackEntry.arguments?.getString("pubKeyB64") ?: ""
            ContactConfirmScreen(
                publicKeyB64 = pubKeyB64,
                onBack = { navController.popBackStack() },
                onConfirmed = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Chat
        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            ChatScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() },
                onStartCall = {
                    navController.navigate(Routes.call(contactId, isIncoming = false))
                },
                onForceLogout = forceLogout
            )
        }

        // Call
        composable(
            Routes.CALL,
            arguments = listOf(
                navArgument("contactId")  { type = NavType.StringType },
                navArgument("isIncoming") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val contactId  = backStackEntry.arguments?.getString("contactId") ?: ""
            val isIncoming = backStackEntry.arguments?.getBoolean("isIncoming") ?: false
            CallScreen(
                contactId  = contactId,
                isIncoming = isIncoming,
                onCallEnded = { forceLogout() }
            )
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToQrDisplay = { navController.navigate(Routes.QR_DISPLAY) },
                onNavigateToQrScan = { navController.navigate(Routes.QR_SCAN) },
                onNavigateToContactConfirm = { pubKeyB64 ->
                    navController.navigate(Routes.contactConfirm(pubKeyB64))
                },
                onForceLogout = forceLogout
            )
        }
    }
}
