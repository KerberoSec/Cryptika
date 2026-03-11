// presentation/ui/screens/AuthScreens.kt
package com.cryptika.messenger.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptika.messenger.presentation.ui.components.SecureKeyboard
import com.cryptika.messenger.presentation.viewmodel.AuthViewModel
import com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel

// ══════════════════════════════════════════════════════════════════════════════
// USERNAME ENTRY SCREEN (passwordless)
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var keyboardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onAuthenticated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cryptika",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            SecureKeyboard(
                visible = keyboardVisible,
                onKeyPress = { key -> username += key },
                onBackspace = { if (username.isNotEmpty()) username = username.dropLast(1) },
                onDone = {
                    keyboardVisible = false
                    if (username.isNotBlank()) {
                        viewModel.clearError()
                        viewModel.enter(username.trim())
                    }
                },
                onToggle = { keyboardVisible = !keyboardVisible }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Username",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ephemeral encrypted messaging",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No passwords. No accounts. Just a public username.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { /* handled by secure keyboard */ },
                label = { Text("Username") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                supportingText = { Text("Case-sensitive • Tap keyboard below to type") }
            )

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    viewModel.enter(username.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading && username.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enter")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTACT DISCOVERY SCREEN
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDiscoveryScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSessionCreated: (sessionUUID: String, peerIdentityHash: String, peerPublicKeyB64: String, peerNickname: String) -> Unit,
    viewModel: ContactDiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var targetUsername by remember { mutableStateOf("") }
    var setupDisplayName by remember { mutableStateOf("") }
    var keyboardVisible by remember { mutableStateOf(false) }
    // Which field is active: "target" for contact search, "displayName" for setup dialog
    var activeField by remember { mutableStateOf("target") }

    // ── Contact Setup Dialog ── shows after accepting / when poll finds accepted session
    uiState.pendingSetup?.let { setup ->
        val fingerprint = remember(setup.peerPublicKeyB64) {
            try {
                val pubKeyBytes = android.util.Base64.decode(
                    setup.peerPublicKeyB64,
                    android.util.Base64.NO_WRAP
                )
                val hash = com.cryptika.messenger.domain.crypto.IdentityHash.compute(pubKeyBytes)
                hash.joinToString("") { "%02x".format(it) }.chunked(8).joinToString(" ")
            } catch (_: Exception) {
                setup.peerIdentityHash.chunked(8).joinToString(" ")
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.cancelSetup() },
            icon = { Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("New Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (setup.isRequester) "Contact accepted" else "New contact request",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Anonymous",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    Text(
                        "Identity Fingerprint",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        fingerprint,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Verify this fingerprint with your contact out-of-band to ensure authenticity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmSetup("")
                        setupDisplayName = ""
                    }
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Confirm & Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.cancelSetup()
                    setupDisplayName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Poll for pending requests on enter and periodically every 5 seconds
    LaunchedEffect(Unit) {
        viewModel.loadPendingRequests()
        while (true) {
            kotlinx.coroutines.delay(5_000)
            viewModel.loadPendingRequests()
        }
    }

    // Poll for accepted sessions (requester side) every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5_000)
            viewModel.pollAcceptedSessions()
        }
    }

    // Handle accepted session
    LaunchedEffect(uiState.acceptedSession) {
        val session = uiState.acceptedSession ?: return@LaunchedEffect
        onSessionCreated(
            session.sessionUUID,
            session.peerIdentityHash,
            session.peerPublicKeyB64,
            session.peerNickname
        )
        viewModel.clearAcceptedSession()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPendingRequests() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
        bottomBar = {
            SecureKeyboard(
                visible = keyboardVisible,
                onKeyPress = { key ->
                    when (activeField) {
                        "target" -> targetUsername += key
                        "displayName" -> setupDisplayName += key
                    }
                },
                onBackspace = {
                    when (activeField) {
                        "target" -> if (targetUsername.isNotEmpty()) targetUsername = targetUsername.dropLast(1)
                        "displayName" -> if (setupDisplayName.isNotEmpty()) setupDisplayName = setupDisplayName.dropLast(1)
                    }
                },
                onDone = { keyboardVisible = false },
                onToggle = { keyboardVisible = !keyboardVisible }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Send contact request ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Send Contact Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetUsername,
                        onValueChange = { /* handled by secure keyboard */ },
                        label = { Text("Username") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeField = "target"
                                keyboardVisible = true
                            },
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.sendContactRequest(targetUsername.trim())
                            targetUsername = ""
                        },
                        enabled = !uiState.isLoading && targetUsername.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Request")
                    }

                    if (uiState.requestSent) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Request sent!",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Pending incoming requests ──
            Text(
                "Incoming Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.pendingRequests.isEmpty()) {
                Text(
                    "No pending requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(uiState.pendingRequests) { request ->
                        PendingRequestCard(
                            request = request,
                            onAccept = { viewModel.acceptRequest(request.requestId) },
                            onReject = { viewModel.rejectRequest(request.requestId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: com.cryptika.messenger.data.remote.api.PendingRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    request.fromNickname,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Token: ${request.fromToken.take(12)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, "Accept", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onReject) {
                Icon(Icons.Default.Close, "Reject", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
