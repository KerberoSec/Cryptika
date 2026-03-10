// Navigation.kt + MainActivity.kt
package com.cryptika.messenger

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.cryptika.messenger.presentation.ui.screens.*
import com.cryptika.messenger.presentation.ui.theme.CryptikaTheme
import com.cryptika.messenger.presentation.viewmodel.CallViewModel
import com.cryptika.messenger.worker.MessageExpiryWorker
import dagger.hilt.android.AndroidEntryPoint

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyScreenshotBlockingPreference()
        MessageExpiryWorker.schedule(this)
        MessageExpiryWorker.runOnce(this)

        setContent {
            CryptikaTheme {
                CryptikaNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply screenshot blocking each time activity resumes so settings changes take effect
        applyScreenshotBlockingPreference()
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
                }
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
                }
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
                onNavigateToQrDisplay = { navController.navigate(Routes.QR_DISPLAY) }
            )
        }
    }
}
