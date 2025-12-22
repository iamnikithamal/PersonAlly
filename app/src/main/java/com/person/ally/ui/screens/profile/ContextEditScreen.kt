package com.person.ally.ui.screens.profile

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.UniversalContext
import com.person.ally.ui.components.GradientButton
import com.person.ally.ui.components.LoadingView
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextEditScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val colors = PersonAllyTheme.extendedColors

    val universalContext by app.userProfileRepository.getUniversalContext().collectAsState(initial = null)

    var editedSummary by remember { mutableStateOf("") }
    var editedCoreIdentity by remember { mutableStateOf<List<String>>(emptyList()) }
    var editedGoals by remember { mutableStateOf<List<String>>(emptyList()) }
    var editedChallenges by remember { mutableStateOf<List<String>>(emptyList()) }
    var editedCommunicationStyle by remember { mutableStateOf("") }
    var editedPersonalitySnapshot by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf<AddItemType?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(universalContext) {
        universalContext?.let {
            editedSummary = it.summary
            editedCoreIdentity = it.coreIdentityPoints.toMutableList()
            editedGoals = it.currentGoals.toMutableList()
            editedChallenges = it.currentChallenges.toMutableList()
            editedCommunicationStyle = it.communicationStyle
            editedPersonalitySnapshot = it.personalitySnapshot
            isLoading = false
        }
        if (universalContext == null) {
            isLoading = false
        }
    }

    fun checkForChanges() {
        hasChanges = universalContext?.let {
            editedSummary != it.summary ||
            editedCoreIdentity != it.coreIdentityPoints ||
            editedGoals != it.currentGoals ||
            editedChallenges != it.currentChallenges ||
            editedCommunicationStyle != it.communicationStyle ||
            editedPersonalitySnapshot != it.personalitySnapshot
        } ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Context") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (hasChanges) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    val updated = (universalContext ?: UniversalContext()).copy(
                                        summary = editedSummary,
                                        coreIdentityPoints = editedCoreIdentity,
                                        currentGoals = editedGoals,
                                        currentChallenges = editedChallenges,
                                        communicationStyle = editedCommunicationStyle,
                                        personalitySnapshot = editedPersonalitySnapshot,
                                        isUserEdited = true,
                                        lastEditedAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    if (universalContext != null) {
                                        app.userProfileRepository.updateUniversalContext(updated)
                                    } else {
                                        app.userProfileRepository.insertUniversalContext(updated)
                                    }
                                    isSaving = false
                                    Toast.makeText(context, "Context saved", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = colors.gradientStart
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    EditSection(
                        title = "Summary",
                        subtitle = "A brief description of who you are"
                    ) {
                        EditableTextField(
                            value = editedSummary,
                            onValueChange = {
                                editedSummary = it
                                checkForChanges()
                            },
                            placeholder = "Write a brief summary about yourself...",
                            minLines = 3
                        )
                    }
                }

                item {
                    EditSection(
                        title = "Core Identity",
                        subtitle = "Key aspects of who you are",
                        onAdd = { showAddItemDialog = AddItemType.CORE_IDENTITY }
                    ) {
                        if (editedCoreIdentity.isEmpty()) {
                            EmptyListPlaceholder(text = "No core identity points yet")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                editedCoreIdentity.forEachIndexed { index, item ->
                                    EditableListItem(
                                        text = item,
                                        onRemove = {
                                            editedCoreIdentity = editedCoreIdentity.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            checkForChanges()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    EditSection(
                        title = "Personality Snapshot",
                        subtitle = "How your personality is understood"
                    ) {
                        EditableTextField(
                            value = editedPersonalitySnapshot,
                            onValueChange = {
                                editedPersonalitySnapshot = it
                                checkForChanges()
                            },
                            placeholder = "Describe your personality traits...",
                            minLines = 2
                        )
                    }
                }

                item {
                    EditSection(
                        title = "Current Goals",
                        subtitle = "What you're working towards",
                        onAdd = { showAddItemDialog = AddItemType.GOAL }
                    ) {
                        if (editedGoals.isEmpty()) {
                            EmptyListPlaceholder(text = "No goals added yet")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                editedGoals.forEachIndexed { index, item ->
                                    EditableListItem(
                                        text = item,
                                        onRemove = {
                                            editedGoals = editedGoals.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            checkForChanges()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    EditSection(
                        title = "Current Challenges",
                        subtitle = "Areas you're working on",
                        onAdd = { showAddItemDialog = AddItemType.CHALLENGE }
                    ) {
                        if (editedChallenges.isEmpty()) {
                            EmptyListPlaceholder(text = "No challenges added yet")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                editedChallenges.forEachIndexed { index, item ->
                                    EditableListItem(
                                        text = item,
                                        onRemove = {
                                            editedChallenges = editedChallenges.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            checkForChanges()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    EditSection(
                        title = "Communication Style",
                        subtitle = "How you prefer to communicate"
                    ) {
                        EditableTextField(
                            value = editedCommunicationStyle,
                            onValueChange = {
                                editedCommunicationStyle = it
                                checkForChanges()
                            },
                            placeholder = "Describe your communication preferences...",
                            minLines = 2
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    if (hasChanges) {
                        GradientButton(
                            text = if (isSaving) "Saving..." else "Save Changes",
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    val updated = (universalContext ?: UniversalContext()).copy(
                                        summary = editedSummary,
                                        coreIdentityPoints = editedCoreIdentity,
                                        currentGoals = editedGoals,
                                        currentChallenges = editedChallenges,
                                        communicationStyle = editedCommunicationStyle,
                                        personalitySnapshot = editedPersonalitySnapshot,
                                        isUserEdited = true,
                                        lastEditedAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    if (universalContext != null) {
                                        app.userProfileRepository.updateUniversalContext(updated)
                                    } else {
                                        app.userProfileRepository.insertUniversalContext(updated)
                                    }
                                    isSaving = false
                                    Toast.makeText(context, "Context saved", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = isSaving,
                            icon = Icons.Default.Save
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    showAddItemDialog?.let { type ->
        AddItemDialog(
            type = type,
            onDismiss = { showAddItemDialog = null },
            onAdd = { newItem ->
                when (type) {
                    AddItemType.CORE_IDENTITY -> {
                        editedCoreIdentity = editedCoreIdentity + newItem
                    }
                    AddItemType.GOAL -> {
                        editedGoals = editedGoals + newItem
                    }
                    AddItemType.CHALLENGE -> {
                        editedChallenges = editedChallenges + newItem
                    }
                }
                checkForChanges()
                showAddItemDialog = null
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
}

@Composable
private fun EditSection(
    title: String,
    subtitle: String,
    onAdd: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onAdd != null) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAdd),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}

@Composable
private fun EditableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun EditableListItem(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyListPlaceholder(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AddItemDialog(
    type: AddItemType,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    AddItemType.CORE_IDENTITY -> "Add Core Identity Point"
                    AddItemType.GOAL -> "Add Goal"
                    AddItemType.CHALLENGE -> "Add Challenge"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = when (type) {
                            AddItemType.CORE_IDENTITY -> "e.g., I value authenticity..."
                            AddItemType.GOAL -> "e.g., Learn a new skill..."
                            AddItemType.CHALLENGE -> "e.g., Managing stress..."
                        }
                    )
                },
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class AddItemType {
    CORE_IDENTITY,
    GOAL,
    CHALLENGE
}
