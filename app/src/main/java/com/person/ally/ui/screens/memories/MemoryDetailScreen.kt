package com.person.ally.ui.screens.memories

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.ImportanceLevel
import com.person.ally.data.model.Memory
import com.person.ally.data.model.MemoryCategory
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoryDetailScreen(
    memoryId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val memory by app.memoryRepository.getMemoryById(memoryId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    val colors = PersonAllyTheme.extendedColors

    if (memory == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Memory not found")
        }
        return
    }

    val currentMemory = memory!!
    val categoryColor = colors.getCategoryColor(currentMemory.category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit memory */ }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Category Header
            PersonAllyCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(currentMemory.category),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentMemory.category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor
                        )
                        Text(
                            text = getImportanceLabel(currentMemory.importance),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentMemory.importance == ImportanceLevel.HIGH ||
                        currentMemory.importance == ImportanceLevel.CRITICAL
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Important",
                            tint = colors.gradientEnd,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            SectionHeader(title = "Memory Content")
            Spacer(modifier = Modifier.height(12.dp))
            PersonAllyCard {
                Text(
                    text = currentMemory.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Context
            if (!currentMemory.context.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Context")
                Spacer(modifier = Modifier.height(12.dp))
                PersonAllyCard {
                    Text(
                        text = currentMemory.context,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Tags
            if (currentMemory.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Tags")
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentMemory.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Metadata
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Details")
            Spacer(modifier = Modifier.height(12.dp))
            PersonAllyCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetadataRow(
                        label = "Created",
                        value = formatDateTime(currentMemory.createdAt)
                    )
                    MetadataRow(
                        label = "Last updated",
                        value = formatDateTime(currentMemory.updatedAt)
                    )
                    MetadataRow(
                        label = "Access count",
                        value = currentMemory.accessCount.toString()
                    )
                    if (currentMemory.lastAccessedAt != null) {
                        MetadataRow(
                            label = "Last accessed",
                            value = formatDateTime(currentMemory.lastAccessedAt)
                        )
                    }
                    MetadataRow(
                        label = "Confidence",
                        value = "${(currentMemory.confidence * 100).toInt()}%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Memory") },
            text = { Text("Are you sure you want to delete this memory? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.memoryRepository.deleteMemory(currentMemory)
                            onNavigateBack()
                        }
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
}

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getCategoryIcon(category: MemoryCategory) = when (category) {
    MemoryCategory.CORE_IDENTITY -> Icons.Filled.Star
    MemoryCategory.EVOLVING_UNDERSTANDING -> Icons.Filled.Psychology
    MemoryCategory.CONTEXTUAL -> Icons.Filled.FilterList
    MemoryCategory.EPISODIC -> Icons.Filled.Memory
}

private fun getImportanceLabel(importance: ImportanceLevel) = when (importance) {
    ImportanceLevel.LOW -> "Low importance"
    ImportanceLevel.MEDIUM -> "Medium importance"
    ImportanceLevel.HIGH -> "High importance"
    ImportanceLevel.CRITICAL -> "Critical importance"
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
