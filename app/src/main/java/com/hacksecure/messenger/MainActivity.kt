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

// ══════════════════════════════════════════════════════════════════════════════
// NAVIGATION ROUTES
// ══════════════════════════════════════════════════════════════════════════════
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

// ══════════════════════════════════════════════════════════════════════════════
// MAIN ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var ephemeralSessionManager: EphemeralSessionManager
    @Inject lateinit var backgroundConnectionManager: BackgroundConnectionManager
    @Inject lateinit var authRepository: AuthRepository

    private val wipeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Screen-off → destroy all sessions, wipe auth, force re-register */
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                wipeScope.launch {
                    withContext(Dispatchers.IO) {
                        ephemeralSessionManager.destroyAllSessions()
                        backgroundConnectionManager.stopAll()
                    }
                    authRepository.logout()
                    // Restart activity so nav graph resets to AUTH
                    val restart = Intent(this@MainActivity, MainActivity::class.java)
                    restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(restart)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Screenshot blocking is now per-chat — applied in ChatScreen, not globally
        MessageExpiryWorker.schedule(this)
        MessageExpiryWorker.runOnce(this)

        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        setContent {
            CryptikaTheme {
                CryptikaNavGraph()
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
    }

    /** Back / Home / Minimize → destroy all sessions, wipe auth, force re-enter username */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            wipeScope.launch {
                withContext(Dispatchers.IO) {
                    ephemeralSessionManager.destroyAllSessions()
                    backgroundConnectionManager.stopAll()
                }
                authRepository.logout()
                val restart = Intent(this@MainActivity, MainActivity::class.java)
                restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(restart)
                finish()
            }
        }
    }

    private fun applyScreenshotBlockingPreference() {
        val prefs = getSharedPreferences("cryptika_settings", MODE_PRIVATE)
        val blockingEnabled = prefs.getBoolean("screenshot_blocking", true)
        if (blockingEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun CryptikaNavGraph() {
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
        // Shared force-logout: clear back stack and navigate to AUTH
        val forceLogout: () -> Unit = {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }

        // ── Auth ─────────────────────────────────────────────────────────────
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // ── Splash ──────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onReady = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToQrDisplay = { navController.navigate(Routes.QR_DISPLAY) },
                onNavigateToQrScan = { navController.navigate(Routes.QR_SCAN) },
                onNavigateToChat = { contactId -> navController.navigate(Routes.chat(contactId)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToContactDiscovery = { navController.navigate(Routes.CONTACT_DISCOVERY) }
            )
        }

        // ── Contact Discovery ────────────────────────────────────────────────
        composable(Routes.CONTACT_DISCOVERY) {
            ContactDiscoveryScreen(
                onBack = { navController.popBackStack() },
                onLogout = forceLogout,
                onSessionCreated = { sessionUUID, peerIdHash, peerPubKeyB64, peerNickname ->
                    navController.navigate(Routes.ephemeralChat(sessionUUID)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        // ── Ephemeral Chat ───────────────────────────────────────────
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
                onForceLogout = forceLogout
            )
        }

        // ── QR Display ───────────────────────────────────────────────────────
        composable(Routes.QR_DISPLAY) {
            QrDisplayScreen(onBack = { navController.popBackStack() })
        }

        // ── QR Scan ──────────────────────────────────────────────────────────
        composable(Routes.QR_SCAN) {
            QrScanScreen(
                onBack = { navController.popBackStack() },
                onScanSuccess = { pubKeyB64 ->
                    navController.navigate(Routes.contactConfirm(pubKeyB64))
                }
            )
        }

        // ── Contact Confirmation ─────────────────────────────────────────────
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

        // ── Chat ─────────────────────────────────────────────────────────────
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

        // ── Call ──────────────────────────────────────────────────────────────
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
                onCallEnded = { navController.popBackStack() }
            )
        }

        // ── Settings ─────────────────────────────────────────────────────────
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToQrDisplay = { navController.navigate(Routes.QR_DISPLAY) },
                onNavigateToQrScan = { navController.navigate(Routes.QR_SCAN) },
                onForceLogout = forceLogout
            )
        }
    }
}
