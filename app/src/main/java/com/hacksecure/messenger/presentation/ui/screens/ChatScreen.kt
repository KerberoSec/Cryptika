// presentation/ui/screens/ChatScreen.kt
package com.cryptika.messenger.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptika.messenger.domain.model.*
import com.cryptika.messenger.presentation.ui.theme.*
import com.cryptika.messenger.presentation.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactId: String,
    onBack: () -> Unit,
    onStartCall: () -> Unit = {},
    onForceLogout: () -> Unit = {},
    sessionUUID: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var showExpiryPicker by remember { mutableStateOf(false) }
    var keyboardVisible by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Register ViewModel as lifecycle observer for background/foreground reconnect
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
    }

    // Per-chat screenshot blocking: apply FLAG_SECURE only while inside a chat
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val prefs = context.getSharedPreferences("cryptika_settings", android.content.Context.MODE_PRIVATE)
        val blockingEnabled = prefs.getBoolean("screenshot_blocking", true)
        if (blockingEnabled && activity != null) {
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            // Always clear FLAG_SECURE when leaving the chat screen
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    LaunchedEffect(contactId, sessionUUID) {
        if (sessionUUID != null) {
            viewModel.initEphemeralSession(sessionUUID)
        } else {
            viewModel.initConversation(contactId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChatEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is ChatEvent.MessageRejected -> snackbarHostState.showSnackbar(
                    "Message verification failed. Possible tampering detected."
                )
                is ChatEvent.SessionSecured -> snackbarHostState.showSnackbar("Session secured")
                is ChatEvent.RetrySucceeded -> snackbarHostState.showSnackbar("Messages re-queued")
                is ChatEvent.PeerDisconnected -> snackbarHostState.showSnackbar(
                    "Peer disconnected — session destroyed. All data wiped."
                )
                is ChatEvent.ForceLogout -> onForceLogout()
                else -> {}
            }
        }
    }

    // Retry failed messages automatically when relay reconnects
    LaunchedEffect(state.connectionState) {
        if (state.connectionState == ConnectionState.CONNECTED_RELAY) {
            viewModel.retryFailedMessages()
        }
    }

    // Auto-scroll to bottom when a genuinely new last message arrives.
    // Key on the last message's id so state updates (SENDING→SENT) and intermediate
    // expiry deletions don't trigger a spurious scroll.
    val lastMessageId = state.messages.lastOrNull()?.id
    LaunchedEffect(lastMessageId) {
        if (state.messages.isNotEmpty()) {
            // The LazyColumn prepends 0 or 1 non-message header items:
            //   • SessionStatusBanner  when sessionEstablished == true
            //   • ConnectionTroubleshootCard when !sessionEstablished && DISCONNECTED/ERROR
            // These are mutually exclusive, so headerCount is always 0 or 1.
            val headerCount = if (state.sessionEstablished) 1
                              else if (state.connectionState == ConnectionState.DISCONNECTED ||
                                       state.connectionState == ConnectionState.ERROR) 1
                              else 0
            listState.animateScrollToItem(headerCount + state.messages.size - 1)
        }
    }

    // Ephemeral session expired overlay
    val isEphemeralExpired = state.ephemeralState is com.cryptika.messenger.domain.model.EphemeralSessionState.Expired

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                contact = state.contact,
                connectionState = state.connectionState,
                sessionEstablished = state.sessionEstablished,
                onBack = onBack,
                onStartCall = if (state.isEphemeral) {{}} else onStartCall,
                ephemeralState = state.ephemeralState
            )
        },
        bottomBar = {
            if (!isEphemeralExpired) {
                Column {
                    ChatInputBar(
                        text = state.inputText,
                        onTextChange = { /* handled by secure keyboard */ },
                        onSend = {
                            viewModel.sendMessage()
                            keyboardVisible = false
                        },
                        sessionEstablished = state.sessionEstablished,
                        expirySeconds = state.selectedExpirySeconds,
                        onTimerClick = { showExpiryPicker = true },
                        onFieldClick = { keyboardVisible = true }
                    )
                    com.cryptika.messenger.presentation.ui.components.SecureKeyboard(
                        visible = keyboardVisible,
                        onKeyPress = { key -> viewModel.updateInputText(state.inputText + key) },
                        onBackspace = {
                            if (state.inputText.isNotEmpty())
                                viewModel.updateInputText(state.inputText.dropLast(1))
                        },
                        onDone = {
                            if (state.inputText.isNotBlank() && state.sessionEstablished) {
                                viewModel.sendMessage()
                            }
                            keyboardVisible = false
                        },
                        onToggle = { keyboardVisible = !keyboardVisible }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Disconnection troubleshoot card
            if (!state.sessionEstablished &&
                (state.connectionState == ConnectionState.DISCONNECTED ||
                 state.connectionState == ConnectionState.ERROR)) {
                item {
                    ConnectionTroubleshootCard(
                        connectionState = state.connectionState,
                        onRetry = viewModel::retryConnection
                    )
                }
            }

            // Session status header
            if (state.sessionEstablished) {
                item {
                    SessionStatusBanner()
                }
            }

            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onExpired = viewModel::triggerLocalExpiry,
                    onDeleteMessage = viewModel::deleteMessage
                )
            }
        }

        // Expired overlay for ephemeral sessions
        if (isEphemeralExpired) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.TimerOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Session Expired",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "This ephemeral session has ended.\nAll messages have been destroyed.\nYou will be logged out.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(onClick = onForceLogout) {
                            Text("Return to Login")
                        }
                    }
                }
            }
        }
    }

    // Expiry picker bottom sheet
    if (showExpiryPicker) {
        ExpiryPickerSheet(
            selectedSeconds = state.selectedExpirySeconds,
            onSelect = { seconds ->
                viewModel.setExpirySeconds(seconds)
                showExpiryPicker = false
            },
            onDismiss = { showExpiryPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    contact: Contact?,
    connectionState: ConnectionState,
    sessionEstablished: Boolean,
    onBack: () -> Unit,
    onStartCall: () -> Unit = {},
    ephemeralState: com.cryptika.messenger.domain.model.EphemeralSessionState = com.cryptika.messenger.domain.model.EphemeralSessionState.None
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = contact?.displayName ?: "...",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ConnectionIndicator(connectionState)
                    when (val es = ephemeralState) {
                        is com.cryptika.messenger.domain.model.EphemeralSessionState.Active -> {
                            val mins = (es.remainingMs / 60000).toInt()
                            val secs = ((es.remainingMs % 60000) / 1000).toInt()
                            Text(
                                text = "Ephemeral • ${mins}m ${secs}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (es.remainingMs < 120_000) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                            )
                        }
                        is com.cryptika.messenger.domain.model.EphemeralSessionState.Expired -> {
                            Text(
                                text = "Session Expired",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {
                            Text(
                                text = connectionState.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = connectionState.indicatorColor()
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            if (contact?.hasKeyChanged == true) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Warning, "Key Change Warning", tint = WarningAmber)
                }
            }
            // Voice call button — visible once the contact is loaded
            if (contact != null) {
                IconButton(onClick = onStartCall) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Start encrypted voice call",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
private fun ConnectionIndicator(state: ConnectionState) {
    val color = state.indicatorColor()
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private val ConnectionState.label get() = when (this) {
    ConnectionState.CONNECTED_P2P -> "Direct P2P"
    ConnectionState.CONNECTED_RELAY -> "Online"
    ConnectionState.CONNECTING -> "Connecting..."
    ConnectionState.DISCONNECTED -> "Disconnected"
    ConnectionState.ERROR -> "Connection Error"
}

@Composable
private fun ConnectionState.indicatorColor(): Color = when (this) {
    ConnectionState.CONNECTED_P2P -> P2PGreen
    ConnectionState.CONNECTED_RELAY -> RelayYellow
    ConnectionState.CONNECTING -> MaterialTheme.colorScheme.onSurfaceVariant
    ConnectionState.DISCONNECTED, ConnectionState.ERROR -> DisconnectedGray
}

@Composable
private fun SessionStatusBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lock, null, modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text(
                    "End-to-End Encrypted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    onExpired: () -> Unit = {},
    onDeleteMessage: (String, Boolean) -> Unit = { _, _ -> }
) {
    val isOutgoing = message.isOutgoing

    // Rejected/tampered message
    if (message.state == MessageState.REJECTED) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error)
                    Text(
                        "Message verification failed. Possible tampering detected.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        return
    }

    // Expired message
    if (message.state == MessageState.EXPIRED) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
        ) {
            Text(
                "Message expired",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        return
    }

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (isOutgoing) 18.dp else 4.dp,
                            bottomEnd = if (isOutgoing) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isOutgoing) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .combinedClickable(onClick = {}, onLongClick = { showMenu = true })
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Expiry countdown
                    message.expiryDeadlineMs?.let { deadline ->
                        val remaining = remember(deadline) {
                            mutableStateOf(((deadline - System.currentTimeMillis()) / 1000).coerceAtLeast(0))
                        }
                        LaunchedEffect(deadline) {
                            while (remaining.value > 0) {
                                kotlinx.coroutines.delay(1000)
                                remaining.value = ((deadline - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                            }
                            onExpired()
                        }
                        Text(
                            text = formatCountdown(remaining.value),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (remaining.value < 10) MaterialTheme.colorScheme.error
                                    else if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestampMs)),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (isOutgoing) {
                            val icon = when (message.state) {
                                MessageState.SENT -> Icons.Filled.Check
                                MessageState.DELIVERED -> Icons.Filled.DoneAll
                                MessageState.FAILED -> Icons.Filled.ErrorOutline
                                else -> Icons.Filled.Schedule
                            }
                            val tintColor = if (message.state == MessageState.FAILED)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            Icon(icon, null, modifier = Modifier.size(12.dp), tint = tintColor)
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete for me") },
                    onClick = { showMenu = false; onDeleteMessage(message.id, false) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null) }
                )
                if (isOutgoing) {
                    DropdownMenuItem(
                        text = { Text("Delete for both") },
                        onClick = { showMenu = false; onDeleteMessage(message.id, true) },
                        leadingIcon = { Icon(Icons.Filled.DeleteForever, null) }
                    )
                }
            }
        }
    }
}

private fun formatCountdown(seconds: Long): String = when {
    seconds >= 3600 -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
    else -> "${seconds}s"
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    sessionEstablished: Boolean,
    expirySeconds: Int,
    onTimerClick: () -> Unit,
    onFieldClick: () -> Unit = {}
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Timer / Expiry picker button
            IconButton(onClick = onTimerClick) {
                Icon(
                    Icons.Filled.Timer,
                    "Set expiry",
                    tint = if (expirySeconds > 0) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Text input — read-only, taps open secure keyboard
            OutlinedTextField(
                value = text,
                onValueChange = { /* blocked — secure keyboard only */ },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onFieldClick() },
                placeholder = { Text("Message...") },
                maxLines = 5,
                readOnly = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Send button — disabled until session established
            FilledIconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && sessionEstablished,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Filled.Send, "Send")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpiryPickerSheet(
    selectedSeconds: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "None",
        30 to "30 seconds",
        60 to "1 minute",
        300 to "5 minutes",
        1800 to "30 minutes",
        3600 to "1 hour",
        86400 to "24 hours"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Message Expiry",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            options.forEach { (seconds, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = {
                        Icon(
                            if (seconds == 0) Icons.Filled.TimerOff else Icons.Filled.Timer,
                            null
                        )
                    },
                    trailingContent = {
                        if (selectedSeconds == seconds) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { onSelect(seconds) }
                )
            }
        }
    }
}

@Composable
private fun ConnectionTroubleshootCard(
    connectionState: ConnectionState,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Filled.WifiOff, null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    if (connectionState == ConnectionState.ERROR) "Connection error" else "Not connected",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                "User is currently unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Retry Connection") }
        }
    }
}
