package com.person.ally.ui.screens.memories

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.MemoryImportance
import com.person.ally.data.model.Memory
import com.person.ally.data.model.MemoryCategory
import com.person.ally.ui.components.EmptyStateView
import com.person.ally.ui.theme.PersonAllyTheme
import com.person.ally.ui.theme.displayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMemory: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MemoryCategory?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    val memories by if (searchQuery.isNotBlank()) {
        app.memoryRepository.searchMemories(searchQuery).collectAsState(initial = emptyList())
    } else if (selectedCategory != null) {
        app.memoryRepository.getMemoriesByCategory(selectedCategory!!).collectAsState(initial = emptyList())
    } else {
        app.memoryRepository.getAllMemories().collectAsState(initial = emptyList())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search memories...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = "Memories",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) searchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (showSearch) "Close search" else "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMemoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add memory"
                )
            }
        }
    ) { paddingValues ->
        // Add Memory Dialog
        if (showAddMemoryDialog) {
            AddMemoryDialog(
                onDismiss = { showAddMemoryDialog = false },
                onSave = { content, category, importance, tags ->
                    scope.launch {
                        app.memoryRepository.createUserMemory(
                            content = content,
                            category = category,
                            importance = importance,
                            tags = tags
                        )
                        showAddMemoryDialog = false
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = if (selectedCategory == category) null else category
                }
            )

            if (memories.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.Memory,
                    title = if (searchQuery.isNotBlank()) "No results found" else "No memories yet",
                    subtitle = if (searchQuery.isNotBlank()) {
                        "Try a different search term"
                    } else {
                        "Your memories will appear here as you chat with Ally"
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(memories, key = { it.id }) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = { onNavigateToMemory(memory.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: MemoryCategory?,
    onCategorySelected: (MemoryCategory) -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MemoryCategory.entries) { category ->
            val isSelected = selectedCategory == category
            val categoryColor = colors.getCategoryColor(category)

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                    selectedLabelColor = categoryColor
                )
            )
        }
    }
}

@Composable
private fun MemoryCard(
    memory: Memory,
    onClick: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors
    val categoryColor = colors.getCategoryColor(memory.category)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(memory.category),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = memory.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                        Text(
                            text = formatDate(memory.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (memory.importance == MemoryImportance.HIGH || memory.importance == MemoryImportance.CRITICAL) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Important",
                        tint = colors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = memory.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (memory.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    memory.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (memory.tags.size > 3) {
                        Text(
                            text = "+${memory.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: MemoryCategory) = when (category) {
    MemoryCategory.CORE_IDENTITY -> Icons.Filled.Star
    MemoryCategory.EVOLVING_UNDERSTANDING -> Icons.Filled.Psychology
    MemoryCategory.CONTEXTUAL -> Icons.Filled.FilterList
    MemoryCategory.EPISODIC -> Icons.Filled.Memory
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onSave: (content: String, category: MemoryCategory, importance: MemoryImportance, tags: List<String>) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MemoryCategory.EVOLVING_UNDERSTANDING) }
    var selectedImportance by remember { mutableStateOf(MemoryImportance.MEDIUM) }
    var tagsText by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var importanceExpanded by remember { mutableStateOf(false) }

    val colors = PersonAllyTheme.extendedColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Memory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Memory content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        MemoryCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = colors.getCategoryColor(category),
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.displayName)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Importance Dropdown
                ExposedDropdownMenuBox(
                    expanded = importanceExpanded,
                    onExpandedChange = { importanceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedImportance.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Importance") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = importanceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    ExposedDropdownMenu(
                        expanded = importanceExpanded,
                        onDismissRequest = { importanceExpanded = false }
                    ) {
                        MemoryImportance.entries.forEach { importance ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        importance.name.lowercase()
                                            .replaceFirstChar { it.uppercase() }
                                    )
                                },
                                onClick = {
                                    selectedImportance = importance
                                    importanceExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = content.isNotBlank()) {
                        val tags = tagsText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        onSave(content, selectedCategory, selectedImportance, tags)
                    },
                color = if (content.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "Save",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (content.isNotBlank())
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Cancel",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
