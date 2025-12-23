package com.person.ally.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.person.ally.data.model.JournalEntry
import com.person.ally.data.model.JournalEntryType
import com.person.ally.data.model.MoodLevel
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEntryScreen(
    entryId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    var existingEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(JournalEntryType.FREE_WRITE) }
    var selectedMood by remember { mutableStateOf<MoodLevel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isNewEntry = entryId == null || entryId == -1L
    val wordCount = remember(content) {
        content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    }

    // Load existing entry if editing
    LaunchedEffect(entryId) {
        if (!isNewEntry && entryId != null) {
            app.wellnessRepository.getJournalEntryById(entryId)?.let { entry ->
                existingEntry = entry
                title = entry.title
                content = entry.content
                selectedType = entry.entryType
                selectedMood = entry.mood
            }
        }
    }

    fun saveEntry() {
        if (title.isBlank()) return

        scope.launch {
            if (isNewEntry) {
                app.wellnessRepository.createJournalEntry(
                    title = title.trim(),
                    content = content.trim(),
                    entryType = selectedType,
                    mood = selectedMood
                )
            } else {
                existingEntry?.let { entry ->
                    app.wellnessRepository.updateJournalEntry(
                        entry.copy(
                            title = title.trim(),
                            content = content.trim(),
                            entryType = selectedType,
                            mood = selectedMood
                        )
                    )
                }
            }
            onNavigateBack()
        }
    }

    fun deleteEntry() {
        scope.launch {
            existingEntry?.let { entry ->
                app.wellnessRepository.deleteJournalEntry(entry)
            }
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this journal entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteEntry()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewEntry) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNewEntry) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = { saveEntry() },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Entry title...") },
                textStyle = MaterialTheme.typography.titleLarge,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Entry Type Selection
            Text(
                text = "Entry Type",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JournalEntryType.entries.forEach { type ->
                    EntryTypeChip(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mood Selection (Optional)
            Text(
                text = "How are you feeling? (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodLevel.entries.forEach { mood ->
                    MoodSelector(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = {
                            selectedMood = if (selectedMood == mood) null else mood
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Input
            Text(
                text = "Write your thoughts...",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Start writing...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$wordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = title.isNotBlank()) { saveEntry() },
                shape = RoundedCornerShape(12.dp),
                color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = if (isNewEntry) "Save Entry" else "Update Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (title.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EntryTypeChip(
    type: JournalEntryType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val typeColor = type.getChipColor()

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(type.displayName) },
        leadingIcon = {
            Icon(
                imageVector = type.getIcon(),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun MoodSelector(
    mood: MoodLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) {
                        mood.getColor().copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = mood.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) mood.getColor() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private val JournalEntryType.displayName: String
    get() = when (this) {
        JournalEntryType.REFLECTION -> "Reflection"
        JournalEntryType.GRATITUDE -> "Gratitude"
        JournalEntryType.ACHIEVEMENT -> "Achievement"
        JournalEntryType.CHALLENGE -> "Challenge"
        JournalEntryType.LEARNING -> "Learning"
        JournalEntryType.IDEA -> "Idea"
        JournalEntryType.DREAM -> "Dream"
        JournalEntryType.FREE_WRITE -> "Free Write"
    }

@Composable
private fun JournalEntryType.getChipColor() = when (this) {
    JournalEntryType.REFLECTION -> MaterialTheme.colorScheme.primary
    JournalEntryType.GRATITUDE -> PersonAllyTheme.extendedColors.success
    JournalEntryType.ACHIEVEMENT -> PersonAllyTheme.extendedColors.warning
    JournalEntryType.CHALLENGE -> PersonAllyTheme.extendedColors.error
    JournalEntryType.LEARNING -> MaterialTheme.colorScheme.secondary
    JournalEntryType.IDEA -> MaterialTheme.colorScheme.tertiary
    JournalEntryType.DREAM -> PersonAllyTheme.extendedColors.info
    JournalEntryType.FREE_WRITE -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun JournalEntryType.getIcon(): ImageVector = when (this) {
    JournalEntryType.REFLECTION -> Icons.Default.Psychology
    JournalEntryType.GRATITUDE -> Icons.Default.Favorite
    JournalEntryType.ACHIEVEMENT -> Icons.Default.EmojiEvents
    JournalEntryType.CHALLENGE -> Icons.Default.Warning
    JournalEntryType.LEARNING -> Icons.Default.School
    JournalEntryType.IDEA -> Icons.Default.Lightbulb
    JournalEntryType.DREAM -> Icons.Default.NightsStay
    JournalEntryType.FREE_WRITE -> Icons.Default.Edit
}

@Composable
private fun MoodLevel.getColor() = when (this) {
    MoodLevel.VERY_LOW -> PersonAllyTheme.extendedColors.error
    MoodLevel.LOW -> PersonAllyTheme.extendedColors.warning
    MoodLevel.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    MoodLevel.GOOD -> PersonAllyTheme.extendedColors.success
    MoodLevel.GREAT -> MaterialTheme.colorScheme.primary
    MoodLevel.AMAZING -> PersonAllyTheme.extendedColors.info
}
