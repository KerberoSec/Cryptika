// presentation/ui/screens/CallScreen.kt
package com.cryptika.messenger.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptika.messenger.domain.model.CallState
import com.cryptika.messenger.presentation.viewmodel.CallViewModel

// ══════════════════════════════════════════════════════════════════════════════
// CALL SCREEN — outgoing / incoming / active
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CallScreen(
    contactId: String,
    isIncoming: Boolean,
    onCallEnded: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Request microphone permission before starting audio
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (isIncoming) viewModel.answerCall()
            else viewModel.startOutgoingCall(contactId)
        } else {
            viewModel.hangup()
            onCallEnded()
        }
    }

    LaunchedEffect(contactId, isIncoming) {
        if (isIncoming) {
            viewModel.initIncomingCall(contactId)
        } else {
            // Start outgoing call (request mic permission first)
            val hasMic = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasMic) viewModel.startOutgoingCall(contactId)
            else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Navigate away once the call ends.
    // hasLeftIdle prevents firing for outgoing calls whose initial state is IDLE
    // (the state only becomes OUTGOING_RINGING after startOutgoingCall() completes its coroutine).
    var hasLeftIdle by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.callState) {
        if (uiState.callState != CallState.IDLE) hasLeftIdle = true
        if (hasLeftIdle && uiState.callState == CallState.IDLE) onCallEnded()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF1A3040))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp, horizontal = 32.dp)
        ) {
            // ── Top section: contact info ─────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lock icon indicating end-to-end encryption
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Encrypted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )

                // Contact avatar (initials placeholder)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E4A5A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.contactName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = uiState.contactName.ifEmpty { "..." },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                // Status subtitle
                Text(
                    text = when (uiState.callState) {
                        CallState.OUTGOING_RINGING -> "Calling..."
                        CallState.INCOMING_RINGING -> "Incoming encrypted call"
                        CallState.ACTIVE -> formatDuration(uiState.callDurationSeconds)
                        CallState.ENDING -> "Ending..."
                        CallState.IDLE   -> ""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f)
                )

                // Pulse animation while ringing
                if (uiState.callState == CallState.OUTGOING_RINGING ||
                    uiState.callState == CallState.INCOMING_RINGING) {
                    RingingPulse()
                }
            }

            // ── Bottom section: call action buttons ───────────────────────────
            when (uiState.callState) {
                CallState.INCOMING_RINGING -> IncomingCallButtons(
                    onAnswer = {
                        val hasMic = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasMic) viewModel.answerCall()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onReject = { viewModel.rejectCall() }  // LaunchedEffect handles navigation
                )

                CallState.OUTGOING_RINGING -> OutgoingRingingButtons(
                    onCancel = { viewModel.hangup() }  // LaunchedEffect handles navigation
                )

                CallState.ACTIVE -> ActiveCallButtons(
                    isMuted = uiState.isMuted,
                    isSpeakerOn = uiState.isSpeakerOn,
                    onToggleMute = viewModel::toggleMute,
                    onToggleSpeaker = viewModel::toggleSpeaker,
                    onHangup = { viewModel.hangup() }
                )

                else -> {}
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BUTTON ROWS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun IncomingCallButtons(
    onAnswer: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reject
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onReject,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
            ) {
                Icon(
                    Icons.Filled.CallEnd,
                    contentDescription = "Reject",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Decline", color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium)
        }

        // Answer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onAnswer,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF388E3C))
            ) {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = "Answer",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Answer", color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun OutgoingRingingButtons(onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFD32F2F))
        ) {
            Icon(
                Icons.Filled.CallEnd,
                contentDescription = "Cancel call",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Cancel", color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ActiveCallButtons(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onHangup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute toggle
        CallActionButton(
            icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
            label = if (isMuted) "Unmute" else "Mute",
            active = isMuted,
            onClick = onToggleMute
        )

        // Hang up — centred and larger
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onHangup,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
            ) {
                Icon(
                    Icons.Filled.CallEnd,
                    contentDescription = "Hang up",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("End", color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium)
        }

        // Speaker toggle
        CallActionButton(
            icon = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeDown,
            label = if (isSpeakerOn) "Speaker" else "Earpiece",
            active = isSpeakerOn,
            onClick = onToggleSpeaker
        )
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (active) Color(0xFF37474F) else Color(0xFF263238)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color(0xFF80CBC4) else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PULSE ANIMATION
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RingingPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(0xFF4CAF50).copy(alpha = alpha))
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════════════════════════════

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
