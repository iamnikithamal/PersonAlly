package com.person.ally.ui.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.JournalEntry
import com.person.ally.data.model.JournalEntryType
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (Long) -> Unit,
    onNavigateToNewEntry: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val allEntries by app.wellnessRepository.allJournalEntries.collectAsState(initial = emptyList())
    val favoriteEntries by app.wellnessRepository.getFavoriteJournalEntries().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<JournalEntryType?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val filteredEntries = remember(allEntries, searchQuery, selectedFilter, showFavoritesOnly, favoriteEntries) {
        var entries = if (showFavoritesOnly) favoriteEntries else allEntries
        if (searchQuery.isNotBlank()) {
            entries = entries.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }
        if (selectedFilter != null) {
            entries = entries.filter { it.entryType == selectedFilter }
        }
        entries
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewEntry,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Entry")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Search Bar
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { -it }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search journal entries...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }
            }

            // Filter Chips
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { it / 4 }
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = showFavoritesOnly,
                                onClick = { showFavoritesOnly = !showFavoritesOnly },
                                label = { Text("Favorites") },
                                leadingIcon = {
                                    Icon(
                                        if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        items(JournalEntryType.entries.toTypedArray()) { type ->
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = {
                                    selectedFilter = if (selectedFilter == type) null else type
                                },
                                label = { Text(type.displayName) }
                            )
                        }
                    }
                }
            }

            // Stats Row
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    JournalStatsRow(
                        totalEntries = allEntries.size,
                        favoriteCount = favoriteEntries.size
                    )
                }
            }

            // Journal Entries
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredEntries.isEmpty()) {
                item {
                    EmptyJournalState(
                        showFavoritesOnly = showFavoritesOnly,
                        hasFilter = selectedFilter != null || searchQuery.isNotBlank(),
                        onCreateNew = onNavigateToNewEntry
                    )
                }
            } else {
                items(filteredEntries) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { onNavigateToEntry(entry.id) },
                        onToggleFavorite = {
                            scope.launch {
                                app.wellnessRepository.toggleJournalFavorite(entry.id, !entry.isFavorite)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalStatsRow(
    totalEntries: Int,
    favoriteCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = totalEntries.toString(),
            label = "Total Entries",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = favoriteCount.toString(),
            label = "Favorites",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = entry.entryType.getColor()
    val typeIcon = entry.entryType.getIcon()

    PersonAllyCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = typeColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.entryType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (entry.isFavorite) PersonAllyTheme.extendedColors.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = entry.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(entry.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${entry.wordCount} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${entry.readingTimeMinutes} min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyJournalState(
    showFavoritesOnly: Boolean,
    hasFilter: Boolean,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                showFavoritesOnly -> "No favorite entries"
                hasFilter -> "No matching entries"
                else -> "Start your journal"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                showFavoritesOnly -> "Mark entries as favorites to see them here"
                hasFilter -> "Try adjusting your search or filters"
                else -> "Capture your thoughts, reflections, and experiences"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasFilter && !showFavoritesOnly) {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onCreateNew),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Write your first entry",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
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
private fun JournalEntryType.getColor() = when (this) {
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
