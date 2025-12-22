package com.person.ally.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.local.datastore.AIPersonality
import com.person.ally.data.local.datastore.AIResponseLength
import com.person.ally.data.local.datastore.TextSize
import com.person.ally.data.local.datastore.ThemeMode
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiModels: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val colors = PersonAllyTheme.extendedColors

    val themeMode by app.settingsDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColors by app.settingsDataStore.dynamicColorsEnabled.collectAsState(initial = true)
    val notificationsEnabled by app.settingsDataStore.notificationsEnabled.collectAsState(initial = true)
    val hapticFeedback by app.settingsDataStore.hapticFeedback.collectAsState(initial = true)
    val memoryAutoCapture by app.settingsDataStore.memoryAutoCapture.collectAsState(initial = true)
    val memorySuggestions by app.settingsDataStore.memorySuggestions.collectAsState(initial = true)
    val aiResponseLength by app.settingsDataStore.aiResponseLength.collectAsState(initial = AIResponseLength.BALANCED)
    val aiPersonality by app.settingsDataStore.aiPersonality.collectAsState(initial = AIPersonality.WARM)
    val aiProactivity by app.settingsDataStore.aiProactivity.collectAsState(initial = 5)
    val textSize by app.settingsDataStore.textSize.collectAsState(initial = TextSize.MEDIUM)
    val reduceAnimations by app.settingsDataStore.reduceAnimations.collectAsState(initial = false)
    val analyticsEnabled by app.settingsDataStore.analyticsEnabled.collectAsState(initial = false)
    val crashReporting by app.settingsDataStore.crashReporting.collectAsState(initial = true)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showResponseLengthDialog by remember { mutableStateOf(false) }
    var showPersonalityDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme",
                        subtitle = themeMode.getDisplayName(),
                        onClick = { showThemeDialog = true }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Palette,
                        title = "Dynamic Colors",
                        subtitle = "Use colors from your wallpaper (Android 12+)",
                        checked = dynamicColors,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setDynamicColors(it) }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "Text Size",
                        subtitle = textSize.getDisplayName(),
                        onClick = { showTextSizeDialog = true }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Reduce Animations",
                        subtitle = "Simplify motion for accessibility",
                        checked = reduceAnimations,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setReduceAnimations(it) }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "AI Preferences") {
                    SettingsItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI Models",
                        subtitle = "Manage AI providers and models",
                        onClick = onNavigateToAiModels
                    )
                    SettingsItem(
                        icon = Icons.Default.Psychology,
                        title = "AI Personality",
                        subtitle = aiPersonality.getDisplayName(),
                        onClick = { showPersonalityDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Response Length",
                        subtitle = aiResponseLength.getDisplayName(),
                        onClick = { showResponseLengthDialog = true }
                    )
                    SettingsSliderItem(
                        title = "AI Proactivity",
                        subtitle = "How often Ally offers insights",
                        value = aiProactivity.toFloat(),
                        onValueChange = {
                            scope.launch { app.settingsDataStore.setAIProactivity(it.toInt()) }
                        },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            item {
                SettingsSection(title = "Memory & Learning") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Memory,
                        title = "Auto-Capture Memories",
                        subtitle = "Automatically save important information",
                        checked = memoryAutoCapture,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setMemoryAutoCapture(it) }
                        }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Memory Suggestions",
                        subtitle = "Get prompts to save potential memories",
                        checked = memorySuggestions,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setMemorySuggestions(it) }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Notifications") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Enable Notifications",
                        subtitle = "Receive daily briefings and insights",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setNotificationsEnabled(it) }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Feedback & Accessibility") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibration on interactions",
                        checked = hapticFeedback,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setHapticFeedback(it) }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Privacy & Data") {
                    SettingsSwitchItem(
                        icon = Icons.Default.BugReport,
                        title = "Crash Reporting",
                        subtitle = "Help improve the app by sharing crash data",
                        checked = crashReporting,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setCrashReporting(it) }
                        }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Info,
                        title = "Analytics",
                        subtitle = "Share anonymous usage statistics",
                        checked = analyticsEnabled,
                        onCheckedChange = {
                            scope.launch { app.settingsDataStore.setAnalyticsEnabled(it) }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete All Data",
                        subtitle = "Permanently remove all your data",
                        onClick = { showDeleteDataDialog = true },
                        isDestructive = true
                    )
                }
            }

            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        SelectionDialog(
            title = "Theme",
            options = ThemeMode.entries.map { it.getDisplayName() },
            selectedIndex = ThemeMode.entries.indexOf(themeMode),
            onDismiss = { showThemeDialog = false },
            onSelect = { index ->
                scope.launch {
                    app.settingsDataStore.setThemeMode(ThemeMode.entries[index])
                }
                showThemeDialog = false
            }
        )
    }

    if (showResponseLengthDialog) {
        SelectionDialog(
            title = "Response Length",
            options = AIResponseLength.entries.map { it.getDisplayName() },
            selectedIndex = AIResponseLength.entries.indexOf(aiResponseLength),
            onDismiss = { showResponseLengthDialog = false },
            onSelect = { index ->
                scope.launch {
                    app.settingsDataStore.setAIResponseLength(AIResponseLength.entries[index])
                }
                showResponseLengthDialog = false
            }
        )
    }

    if (showPersonalityDialog) {
        SelectionDialog(
            title = "AI Personality",
            options = AIPersonality.entries.map { it.getDisplayName() },
            selectedIndex = AIPersonality.entries.indexOf(aiPersonality),
            onDismiss = { showPersonalityDialog = false },
            onSelect = { index ->
                scope.launch {
                    app.settingsDataStore.setAIPersonality(AIPersonality.entries[index])
                }
                showPersonalityDialog = false
            }
        )
    }

    if (showTextSizeDialog) {
        SelectionDialog(
            title = "Text Size",
            options = TextSize.entries.map { it.getDisplayName() },
            selectedIndex = TextSize.entries.indexOf(textSize),
            onDismiss = { showTextSizeDialog = false },
            onSelect = { index ->
                scope.launch {
                    app.settingsDataStore.setTextSize(TextSize.entries[index])
                }
                showTextSizeDialog = false
            }
        )
    }

    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Delete All Data") },
            text = {
                Text("This will permanently delete all your memories, conversations, insights, and profile data. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.settingsDataStore.clearAllSettings()
                            app.userProfileRepository.deleteUserProfile()
                            app.userProfileRepository.deleteUniversalContext()
                        }
                        showDeleteDataDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = colors.gradientStart.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    val colors = PersonAllyTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = colors.gradientMiddle
            )
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun ThemeMode.getDisplayName(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System default"
}
