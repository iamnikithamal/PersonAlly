package com.person.ally.ui.screens.schedule

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.ScheduleItem
import com.person.ally.data.model.ScheduleItemType
import com.person.ally.data.model.SchedulePriority
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToItem: (Long) -> Unit,
    onNavigateToNewItem: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val todaysItems by app.wellnessRepository.getTodaysScheduleItems().collectAsState(initial = emptyList())
    val upcomingItems by app.wellnessRepository.getUpcomingScheduleItems(20).collectAsState(initial = emptyList())
    val overdueItems by app.wellnessRepository.getOverdueScheduleItems().collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf<ScheduleItemType?>(null) }
    var showCompleted by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val currentDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val filteredTodaysItems = remember(todaysItems, selectedFilter, showCompleted) {
        var items = todaysItems
        if (!showCompleted) {
            items = items.filter { !it.isCompleted }
        }
        if (selectedFilter != null) {
            items = items.filter { it.itemType == selectedFilter }
        }
        items
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Schedule")
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
                onClick = onNavigateToNewItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule Item")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Filter Chips
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { -it }
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = showCompleted,
                                onClick = { showCompleted = !showCompleted },
                                label = { Text("Show Completed") },
                                leadingIcon = {
                                    Icon(
                                        if (showCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        items(ScheduleItemType.entries.toTypedArray()) { type ->
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = {
                                    selectedFilter = if (selectedFilter == type) null else type
                                },
                                label = { Text(type.displayName) },
                                leadingIcon = {
                                    Icon(
                                        type.getIcon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Overdue Section
            if (overdueItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically { it / 4 }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PersonAllyTheme.extendedColors.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Overdue (${overdueItems.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PersonAllyTheme.extendedColors.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                items(overdueItems.take(5)) { item ->
                    ScheduleItemCard(
                        item = item,
                        onClick = { onNavigateToItem(item.id) },
                        onToggleComplete = {
                            scope.launch {
                                if (item.isCompleted) {
                                    app.wellnessRepository.uncompleteScheduleItem(item.id)
                                } else {
                                    app.wellnessRepository.completeScheduleItem(item.id)
                                }
                            }
                        },
                        isOverdue = true,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Today's Schedule Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${filteredTodaysItems.size} items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (filteredTodaysItems.isEmpty()) {
                item {
                    EmptyScheduleState(
                        message = "No items scheduled for today",
                        onAddClick = onNavigateToNewItem
                    )
                }
            } else {
                items(filteredTodaysItems) { item ->
                    ScheduleItemCard(
                        item = item,
                        onClick = { onNavigateToItem(item.id) },
                        onToggleComplete = {
                            scope.launch {
                                if (item.isCompleted) {
                                    app.wellnessRepository.uncompleteScheduleItem(item.id)
                                } else {
                                    app.wellnessRepository.completeScheduleItem(item.id)
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Upcoming Section
            if (upcomingItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upcoming",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(upcomingItems.filter { !isSameDay(it.scheduledAt, System.currentTimeMillis()) }.take(10)) { item ->
                    ScheduleItemCard(
                        item = item,
                        onClick = { onNavigateToItem(item.id) },
                        onToggleComplete = {
                            scope.launch {
                                if (item.isCompleted) {
                                    app.wellnessRepository.uncompleteScheduleItem(item.id)
                                } else {
                                    app.wellnessRepository.completeScheduleItem(item.id)
                                }
                            }
                        },
                        showDate = true,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemCard(
    item: ScheduleItem,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isOverdue: Boolean = false,
    showDate: Boolean = false
) {
    val priorityColor = item.priority.getColor()
    val typeIcon = item.itemType.getIcon()

    PersonAllyCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleComplete)
                    .background(
                        color = if (item.isCompleted) {
                            PersonAllyTheme.extendedColors.success.copy(alpha = 0.1f)
                        } else if (isOverdue) {
                            PersonAllyTheme.extendedColors.error.copy(alpha = 0.1f)
                        } else {
                            priorityColor.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else typeIcon,
                    contentDescription = null,
                    tint = if (item.isCompleted) {
                        PersonAllyTheme.extendedColors.success
                    } else if (isOverdue) {
                        PersonAllyTheme.extendedColors.error
                    } else {
                        priorityColor
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    color = if (item.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(item.scheduledAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) PersonAllyTheme.extendedColors.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (showDate) {
                        Text(
                            text = formatDate(item.scheduledAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (item.durationMinutes > 0) {
                        Text(
                            text = "${item.durationMinutes}min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Priority indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = priorityColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun EmptyScheduleState(
    message: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onAddClick),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Add Item",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private val ScheduleItemType.displayName: String
    get() = when (this) {
        ScheduleItemType.ROUTINE -> "Routine"
        ScheduleItemType.TASK -> "Task"
        ScheduleItemType.EVENT -> "Event"
        ScheduleItemType.REMINDER -> "Reminder"
        ScheduleItemType.APPOINTMENT -> "Appointment"
        ScheduleItemType.SELF_CARE -> "Self-Care"
    }

private fun ScheduleItemType.getIcon(): ImageVector = when (this) {
    ScheduleItemType.ROUTINE -> Icons.Default.Loop
    ScheduleItemType.TASK -> Icons.Default.Task
    ScheduleItemType.EVENT -> Icons.Default.Event
    ScheduleItemType.REMINDER -> Icons.Default.NotificationsActive
    ScheduleItemType.APPOINTMENT -> Icons.Default.CalendarMonth
    ScheduleItemType.SELF_CARE -> Icons.Default.Spa
}

@Composable
private fun SchedulePriority.getColor(): Color = when (this) {
    SchedulePriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    SchedulePriority.MEDIUM -> MaterialTheme.colorScheme.primary
    SchedulePriority.HIGH -> PersonAllyTheme.extendedColors.warning
    SchedulePriority.URGENT -> PersonAllyTheme.extendedColors.error
}
