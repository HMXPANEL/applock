package dev.krinry.jarvis

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.krinry.jarvis.ai.GroqApiClient
import dev.krinry.jarvis.ai.ModelInfo
import dev.krinry.jarvis.security.SecureKeyStore
import dev.krinry.jarvis.service.AutoAgentService
import dev.krinry.jarvis.service.FloatingBubbleService
import dev.krinry.jarvis.ui.components.AnimatedBackgroundOrbs
import dev.krinry.jarvis.ui.components.GlassCard
import dev.krinry.jarvis.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }

    val isAccessibilityEnabled = remember(refreshKey) { checkAccessibilityEnabled(context) }
    val hasOverlayPermission = remember(refreshKey) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
    }
    var hasAudioPermission by remember(refreshKey) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasAudioPermission = isGranted; if (isGranted) refreshKey++ }

    var agentEnabled by remember { mutableStateOf(SecureKeyStore.isAgentEnabled(context)) }
    val allReady = isAccessibilityEnabled && hasOverlayPermission && hasAudioPermission

    val providers = remember { GroqApiClient.getProviders() }
    var selectedProviderId by remember { mutableStateOf(SecureKeyStore.getApiProvider(context)) }
    val selectedProvider = remember(selectedProviderId) { GroqApiClient.getProvider(selectedProviderId) ?: providers.first() }
    var apiKey by remember(selectedProviderId) {
        mutableStateOf(selectedProvider.getApiKey(context) ?: "")
    }
    var primaryModel by remember { mutableStateOf(SecureKeyStore.getPrimaryModel(context)) }
    var fallbackModel by remember { mutableStateOf(SecureKeyStore.getFallbackModel(context)) }
    var showModelPicker by remember { mutableStateOf(false) }
    var modelPickerTarget by remember { mutableStateOf("primary") }
    var providerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { kotlinx.coroutines.delay(500); refreshKey++ }

    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {

        AnimatedBackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .systemBarsPadding()
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HERO CARD ──────────────────────────────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                cornerRadius = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    GradientStart.copy(alpha = 0.6f),
                                    GradientEnd.copy(alpha = 0.4f),
                                    CyanGlow.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(GlassWhite)
                                    .border(1.5.dp, CyanGlow.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "J",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyanGlow
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "JARVIS",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    letterSpacing = 3.sp
                                )
                                Text(
                                    "AI Voice Agent",
                                    color = CyanGlow.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassWhite)
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (agentEnabled) Icons.Default.PowerSettingsNew else Icons.Default.PowerOff,
                                    null,
                                    tint = if (agentEnabled) CyanGlow else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (agentEnabled) "Agent Active" else "Agent Offline",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        if (agentEnabled) "Tap bubble to give command" else "Turn on to start",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                Switch(
                                    checked = agentEnabled,
                                    onCheckedChange = { enabled ->
                                        agentEnabled = enabled
                                        SecureKeyStore.setAgentEnabled(context, enabled)
                                        if (enabled) startBubbleService(context)
                                        else stopBubbleService(context)
                                        refreshKey++
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = CyanGlow.copy(alpha = 0.3f),
                                        checkedThumbColor = CyanGlow,
                                        uncheckedTrackColor = GlassWhite,
                                        uncheckedThumbColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── PERMISSIONS SECTION ────────────────────────────────────────────
            LiquidSectionHeader("Setup", Icons.Default.Shield)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(Modifier.padding(4.dp)) {
                    LiquidPermissionRow(
                        icon = Icons.Default.Accessibility,
                        title = "Accessibility Service",
                        subtitle = "Read & interact with apps",
                        isGranted = isAccessibilityEnabled,
                        iconColor = BlueAccent
                    ) {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        )
                    }
                    GlassDivider()
                    LiquidPermissionRow(
                        icon = Icons.Default.Layers,
                        title = "Display Over Apps",
                        subtitle = "Floating AI bubble",
                        isGranted = hasOverlayPermission,
                        iconColor = PurpleAccent
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            )
                        }
                    }
                    GlassDivider()
                    LiquidPermissionRow(
                        icon = Icons.Default.Mic,
                        title = "Microphone",
                        subtitle = "Voice commands",
                        isGranted = hasAudioPermission,
                        iconColor = CyanGlow
                    ) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }

            if (agentEnabled) {
                Spacer(Modifier.height(12.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    cornerRadius = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (allReady) StatusGreen.copy(alpha = 0.12f)
                                else StatusRed.copy(alpha = 0.12f)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (allReady) "✅ All set! Tap floating bubble to start."
                            else "⚠️ Grant all permissions above.",
                            fontSize = 13.sp,
                            color = if (allReady) StatusGreen else StatusRed,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── AI CONFIGURATION SECTION ───────────────────────────────────────
            LiquidSectionHeader("AI Configuration", Icons.Default.Hub)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Provider",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = providerExpanded,
                        onExpandedChange = { providerExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProvider.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextSecondary,
                                cursorColor = CyanGlow,
                                focusedContainerColor = GlassWhite,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = providerExpanded,
                            onDismissRequest = { providerExpanded = false },
                            modifier = Modifier.background(NavyDark)
                        ) {
                            providers.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider.displayName, color = TextPrimary) },
                                    onClick = {
                                        selectedProviderId = provider.id
                                        SecureKeyStore.setApiProvider(context, provider.id)
                                        apiKey = provider.getApiKey(context) ?: ""
                                        primaryModel = ""
                                        fallbackModel = ""
                                        SecureKeyStore.setPrimaryModel(context, "")
                                        SecureKeyStore.setFallbackModel(context, "")
                                        providerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "${selectedProvider.displayName} API Key",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { newKey ->
                            apiKey = newKey
                            selectedProvider.saveApiKey(context, newKey)
                        },
                        placeholder = { Text("Enter API key...", color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary,
                            cursorColor = CyanGlow,
                            focusedContainerColor = GlassWhite,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (selectedProviderId != "groq") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "💡 STT (voice) always uses Groq Whisper. Set Groq API key too for voice commands.",
                            fontSize = 11.sp,
                            color = StatusAmber.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── MODELS SECTION ─────────────────────────────────────────────────
            LiquidSectionHeader("Models", Icons.Default.Psychology)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(Modifier.padding(4.dp)) {
                    LiquidSettingsRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Primary Model",
                        subtitle = primaryModel.ifEmpty { selectedProvider.defaultModel },
                        iconColor = CyanGlow
                    ) { modelPickerTarget = "primary"; showModelPicker = true }

                    GlassDivider()

                    LiquidSettingsRow(
                        icon = Icons.Default.SwapHoriz,
                        title = "Fallback Model",
                        subtitle = fallbackModel.ifEmpty { selectedProvider.defaultFallbackModel },
                        iconColor = PurpleAccent
                    ) { modelPickerTarget = "fallback"; showModelPicker = true }
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "Jarvis v1.0 • by Krinry",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showModelPicker) {
        ModelPickerDialog(
            context = context,
            target = modelPickerTarget,
            onSelect = { modelId ->
                if (modelPickerTarget == "primary") {
                    primaryModel = modelId; SecureKeyStore.setPrimaryModel(context, modelId)
                } else {
                    fallbackModel = modelId; SecureKeyStore.setFallbackModel(context, modelId)
                }
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false }
        )
    }
}

// =============================================================================
// === Reusable Components ===
// =============================================================================

@Composable
private fun LiquidSectionHeader(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp, start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CyanGlow.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = CyanGlow, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = CyanGlow
        )
    }
}

@Composable
private fun LiquidSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(1.dp, iconColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LiquidPermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isGranted) StatusGreen.copy(alpha = 0.15f)
                        else iconColor.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isGranted) StatusGreen.copy(alpha = 0.4f)
                        else iconColor.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = if (isGranted) StatusGreen else iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            if (isGranted) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(StatusGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                }
            } else {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text("Grant", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GlassDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(GlassBorder)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPickerDialog(context: Context, target: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var models by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showFreeOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch { isLoading = true; models = GroqApiClient.fetchAvailableModels(context); isLoading = false }
    }

    val filteredModels = models.filter { model ->
        (searchQuery.isEmpty() || model.id.contains(searchQuery, ignoreCase = true) || model.name.contains(searchQuery, ignoreCase = true)) &&
        (!showFreeOnly || model.isFree)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyDark,
        title = { Text(if (target == "primary") "Primary Model" else "Fallback Model", color = TextPrimary) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search models...", color = TextMuted) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextSecondary,
                        cursorColor = CyanGlow,
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showFreeOnly,
                        onCheckedChange = { showFreeOnly = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanGlow)
                    )
                    Text("Free only", fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text("${filteredModels.size} models", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanGlow)
                    }
                } else if (filteredModels.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No models found\nCheck API key", fontSize = 14.sp, color = TextSecondary)
                    }
                } else {
                    LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                        items(filteredModels) { model ->
                            Surface(
                                onClick = { onSelect(model.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                color = Color.Transparent
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(model.name.take(40), fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, color = TextPrimary)
                                        Text(model.id, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                    }
                                    if (model.isFree) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = StatusGreen.copy(alpha = 0.15f)) {
                                            Text("FREE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = CyanGlow) } },
        dismissButton = {}
    )
}

// =============================================================================
// === Helpers ===
// =============================================================================

private fun checkAccessibilityEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}

private fun startBubbleService(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "Grant overlay permission first", Toast.LENGTH_LONG).show()
        return
    }
    try {
        val intent = Intent(context, FloatingBubbleService::class.java).apply { action = FloatingBubbleService.ACTION_START }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not start Jarvis: ${e.message?.take(50)}", Toast.LENGTH_LONG).show()
    }
}

private fun stopBubbleService(context: Context) {
    try {
        context.startService(Intent(context, FloatingBubbleService::class.java).apply { action = FloatingBubbleService.ACTION_STOP })
    } catch (_: Exception) {}
}
