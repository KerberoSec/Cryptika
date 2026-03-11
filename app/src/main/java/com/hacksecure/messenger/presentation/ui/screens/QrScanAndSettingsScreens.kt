// presentation/ui/screens/QrScanAndSettingsScreens.kt
package com.cryptika.messenger.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.cryptika.messenger.presentation.ui.components.SecureKeyboard
import com.cryptika.messenger.domain.model.AppVersion
import com.cryptika.messenger.presentation.ui.theme.*
import com.cryptika.messenger.presentation.viewmodel.*
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ══════════════════════════════════════════════════════════════════════════════
// QR SCAN SCREEN
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScanSuccess: (String) -> Unit,  // passes identityHash hex to confirmation screen
    viewModel: QrScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(scanState) {
        when (val s = scanState) {
            is ScanState.Success -> {
                // Navigate to confirmation with base64-encoded public key
                val pubKeyB64 = android.util.Base64.encodeToString(
                    s.result.publicKeyBytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
                )
                onScanSuccess(pubKeyB64)
                viewModel.reset()
            }
            is ScanState.Error -> snackbarHostState.showSnackbar(s.message)
            else -> {}
        }
    }

    var showManualEntry by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    var keyboardVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (showManualEntry) "Enter Code" else "Scan QR Code") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { showManualEntry = !showManualEntry }) {
                        Icon(
                            if (showManualEntry) Icons.Filled.CameraAlt else Icons.Filled.Keyboard,
                            contentDescription = if (showManualEntry) "Switch to camera scan" else "Enter code manually"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (showManualEntry) {
                SecureKeyboard(
                    visible = keyboardVisible,
                    onKeyPress = { key -> manualCode += key },
                    onBackspace = { if (manualCode.isNotEmpty()) manualCode = manualCode.dropLast(1) },
                    onDone = { keyboardVisible = false },
                    onToggle = { keyboardVisible = !keyboardVisible }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showManualEntry) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Enter contact code manually", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Paste the cryptika:// code shared by your contact.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { /* handled by secure keyboard */ },
                        label = { Text("Contact code") },
                        placeholder = { Text("cryptika://id/v1/...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { keyboardVisible = true },
                        minLines = 3,
                        maxLines = 6,
                        readOnly = true
                    )
                    Button(
                        onClick = {
                            val code = manualCode.trim()
                            if (code.isNotEmpty()) viewModel.processQrResult(code)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = manualCode.isNotBlank()
                    ) { Text("Add Contact") }
                }
            } else if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeDetected = { raw -> viewModel.processQrResult(raw) }
                )
                // Scanning overlay
                ScanningOverlay()
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Camera permission required")
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(modifier: Modifier, onQrCodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var hasDetected by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            if (!hasDetected) {
                                val result = decodeQrFromImage(imageProxy)
                                if (result != null) {
                                    hasDetected = true
                                    onQrCodeDetected(result)
                                }
                            }
                            imageProxy.close()
                        }
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) { /* ignore */ }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

private fun decodeQrFromImage(imageProxy: ImageProxy): String? {
    return try {
        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val source = PlanarYUVLuminanceSource(
            bytes, imageProxy.width, imageProxy.height, 0, 0, imageProxy.width, imageProxy.height, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        MultiFormatReader().apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
        }.decode(binaryBitmap).text
    } catch (_: Exception) { null }
}

@Composable
private fun ScanningOverlay() {
    Box(Modifier.fillMaxSize()) {
        // Semi-transparent corners + center frame
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(240.dp)) {
                // Corner brackets
                listOf(
                    Alignment.TopStart, Alignment.TopEnd,
                    Alignment.BottomStart, Alignment.BottomEnd
                ).forEach { alignment ->
                    Box(Modifier.size(40.dp).align(alignment)) {
                        // Corner line visual (simplified)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Transparent)
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                )
                Text(
                    "Align QR code within the frame",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-32).dp)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTACT CONFIRMATION SCREEN
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactConfirmScreen(
    publicKeyB64: String,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
    viewModel: ContactConfirmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var displayName by remember { mutableStateOf("") }
    var showKeyChangeDialog by remember { mutableStateOf<com.cryptika.messenger.domain.model.Contact?>(null) }
    var keyboardVisible by remember { mutableStateOf(false) }

    // Decode the public key
    val (publicKeyBytes, identityHash, fingerprintHex) = remember(publicKeyB64) {
        val pubKey = android.util.Base64.decode(publicKeyB64, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
        val idHash = com.cryptika.messenger.domain.crypto.IdentityHash.compute(pubKey)
        val fingerprint = idHash.joinToString("") { "%02x".format(it) }.chunked(8).joinToString(" ")
        Triple(pubKey, idHash, fingerprint)
    }

    LaunchedEffect(state) {
        when (state) {
            is ConfirmState.Saved -> onConfirmed()
            is ConfirmState.KeyChangeWarning -> showKeyChangeDialog = (state as ConfirmState.KeyChangeWarning).existingContact
            else -> {}
        }
    }

    showKeyChangeDialog?.let { existing ->
        AlertDialog(
            onDismissRequest = { showKeyChangeDialog = null },
            icon = { Icon(Icons.Filled.Warning, null, tint = WarningAmber) },
            title = { Text("Key Change Detected") },
            text = {
                Text("This contact's identity key has changed. This could indicate a device change or a security risk. Only proceed if you have verified this out-of-band.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.forceUpdateKey(existing, publicKeyBytes, identityHash)
                        showKeyChangeDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) { Text("I Understand, Update Key") }
            },
            dismissButton = {
                TextButton(onClick = { showKeyChangeDialog = null; onBack() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Contact") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        },
        bottomBar = {
            SecureKeyboard(
                visible = keyboardVisible,
                onKeyPress = { key -> displayName += key },
                onBackspace = { if (displayName.isNotEmpty()) displayName = displayName.dropLast(1) },
                onDone = { keyboardVisible = false },
                onToggle = { keyboardVisible = !keyboardVisible }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Filled.VerifiedUser,
                null,
                modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Verify Identity Fingerprint",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Identity Fingerprint", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        fingerprintHex,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "Ask your contact to read their fingerprint aloud. Make sure it matches exactly before confirming.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { /* handled by secure keyboard */ },
                label = { Text("Contact Name") },
                placeholder = { Text("Enter a name for this contact") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { keyboardVisible = true },
                singleLine = true,
                readOnly = true,
                leadingIcon = { Icon(Icons.Filled.Person, null) }
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        viewModel.saveContact(publicKeyBytes, identityHash,
                            displayName.ifBlank { "Unknown Contact" })
                    },
                    modifier = Modifier.weight(1f),
                    enabled = state !is ConfirmState.Saving
                ) {
                    if (state is ConfirmState.Saving) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Confirm & Add")
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SETTINGS SCREEN
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToQrDisplay: () -> Unit,
    onForceLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate to auth when identity is regenerated (force logout)
    LaunchedEffect(state.forceLogout) {
        if (state.forceLogout) onForceLogout()
    }

    if (state.showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRegenerateConfirm,
            icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Regenerate Identity?") },
            text = {
                Text("This will destroy all active sessions, log you out, and create a new identity key. You will need to register again. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::regenerateIdentity,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Regenerate") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRegenerateConfirm) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // ── My Identity ──────────────────────────────────────────────────
            SettingsSectionHeader("My Identity")

            ListItem(
                headlineContent = { Text("Identity Fingerprint") },
                supportingContent = {
                    Text(state.fingerprintHex, fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp, lineHeight = 16.sp)
                },
                leadingContent = { Icon(Icons.Filled.Fingerprint, null) }
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Show My QR Code") },
                leadingContent = { Icon(Icons.Filled.QrCode, null) },
                modifier = Modifier.clickable(onClick = onNavigateToQrDisplay)
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Regenerate Identity", color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text("Creates a new keypair. Existing contacts must re-verify.") },
                leadingContent = { Icon(Icons.Filled.Refresh, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable(onClick = viewModel::showRegenerateConfirm)
            )

            // ── Security ─────────────────────────────────────────────────────
            SettingsSectionHeader("Security")

            ListItem(
                headlineContent = { Text("Screenshot Blocking") },
                supportingContent = { Text("Prevents screenshots inside chat screens") },
                leadingContent = { Icon(Icons.Filled.NoPhotography, null) },
                trailingContent = {
                    Switch(
                        checked = state.screenshotBlockingEnabled,
                        onCheckedChange = viewModel::setScreenshotBlocking
                    )
                }
            )

            // Connection
            SettingsSectionHeader("Connection")

            ListItem(
                headlineContent = {
                    OutlinedButton(
                        onClick = { viewModel.pingServer() },
                        enabled = !state.isPinging
                    ) {
                        if (state.isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Test Connection")
                    }
                },
                supportingContent = if (state.pingStatus.isNotEmpty()) ({
                    Text(
                        state.pingStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.pingStatus.startsWith("Cannot")) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                    )
                }) else null,
                leadingContent = { Icon(Icons.Filled.NetworkCheck, null) }
            )

            // About
            SettingsSectionHeader("About")

            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("${AppVersion.NAME} (${AppVersion.CODE})") },
                leadingContent = { Icon(Icons.Filled.Info, null) }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp)
    )
}
