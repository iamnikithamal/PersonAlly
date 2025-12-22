package com.person.ally.ui.screens.insights

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.Goal
import com.person.ally.data.model.GoalMilestone
import com.person.ally.data.model.GoalStatus
import com.person.ally.ui.components.GradientButton
import com.person.ally.ui.components.LoadingView
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val colors = PersonAllyTheme.extendedColors

    var goal by remember { mutableStateOf<Goal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showEditProgressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        goal = app.insightRepository.getGoalById(goalId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goal Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    goal?.let { currentGoal ->
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options"
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (!currentGoal.isCompleted) {
                                    if (currentGoal.isPaused) {
                                        DropdownMenuItem(
                                            text = { Text("Resume Goal") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                scope.launch {
                                                    val updated = currentGoal.copy(
                                                        isPaused = false,
                                                        updatedAt = System.currentTimeMillis()
                                                    )
                                                    app.insightRepository.updateGoal(updated)
                                                    goal = updated
                                                }
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Pause Goal") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Pause,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                scope.launch {
                                                    val updated = currentGoal.copy(
                                                        isPaused = true,
                                                        updatedAt = System.currentTimeMillis()
                                                    )
                                                    app.insightRepository.updateGoal(updated)
                                                    goal = updated
                                                }
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Mark Complete") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = colors.success
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                app.insightRepository.completeGoal(goalId)
                                                goal = app.insightRepository.getGoalById(goalId)
                                                Toast.makeText(context, "Goal completed!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = { Text("Delete Goal") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (goal != null) {
                GoalDetailContent(
                    goal = goal!!,
                    onUpdateProgress = { showEditProgressDialog = true },
                    onAddMilestone = { showAddMilestoneDialog = true },
                    onToggleMilestone = { milestoneId, completed ->
                        scope.launch {
                            app.insightRepository.updateGoalMilestone(goalId, milestoneId, completed)
                            goal = app.insightRepository.getGoalById(goalId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Goal not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isLoading) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            goal?.let { app.insightRepository.deleteGoal(it) }
                            showDeleteDialog = false
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

    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            onDismiss = { showAddMilestoneDialog = false },
            onAdd = { title ->
                scope.launch {
                    goal?.let { currentGoal ->
                        val newMilestone = GoalMilestone(
                            id = UUID.randomUUID().toString(),
                            title = title
                        )
                        val updated = currentGoal.copy(
                            milestones = currentGoal.milestones + newMilestone,
                            updatedAt = System.currentTimeMillis()
                        )
                        app.insightRepository.updateGoal(updated)
                        goal = updated
                    }
                }
                showAddMilestoneDialog = false
            }
        )
    }

    if (showEditProgressDialog) {
        EditProgressDialog(
            currentProgress = goal?.progress ?: 0f,
            onDismiss = { showEditProgressDialog = false },
            onSave = { newProgress ->
                scope.launch {
                    app.insightRepository.updateGoalProgress(goalId, newProgress)
                    goal = app.insightRepository.getGoalById(goalId)
                }
                showEditProgressDialog = false
            }
        )
    }
}

@Composable
private fun GoalDetailContent(
    goal: Goal,
    onUpdateProgress: () -> Unit,
    onAddMilestone: () -> Unit,
    onToggleMilestone: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(goal.domain)

    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(500),
        label = "progress"
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = domainColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = domainColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = domainColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = goal.domain.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = domainColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    StatusChip(status = goal.status)
                }
            }
        }

        item {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = domainColor
                            )

                            if (!goal.isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onUpdateProgress),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit progress",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(12.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            colors.gradientStart,
                                            colors.gradientMiddle,
                                            colors.gradientEnd
                                        )
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }

        if (goal.targetDate != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Target: ${formatDate(goal.targetDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val daysRemaining = calculateDaysRemaining(goal.targetDate)
                    if (daysRemaining >= 0 && !goal.isCompleted) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (daysRemaining <= 7) {
                                colors.warning.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when {
                                    daysRemaining == 0 -> "Due today"
                                    daysRemaining == 1 -> "1 day left"
                                    else -> "$daysRemaining days left"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (daysRemaining <= 7) colors.warning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Milestones")

                if (!goal.isCompleted) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onAddMilestone),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add milestone",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        if (goal.milestones.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "No milestones yet. Add milestones to track your progress step by step.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(goal.milestones) { milestone ->
                MilestoneItem(
                    milestone = milestone,
                    isGoalCompleted = goal.isCompleted,
                    onToggle = { onToggleMilestone(milestone.id, !milestone.isCompleted) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    MetaInfoRow(
                        label = "Created",
                        value = formatDate(goal.createdAt)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetaInfoRow(
                        label = "Last updated",
                        value = formatDate(goal.updatedAt)
                    )
                    if (goal.completedAt != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MetaInfoRow(
                            label = "Completed",
                            value = formatDate(goal.completedAt)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusChip(status: GoalStatus) {
    val colors = PersonAllyTheme.extendedColors

    val (color, text) = when (status) {
        GoalStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant to "Not Started"
        GoalStatus.IN_PROGRESS -> colors.info to "In Progress"
        GoalStatus.COMPLETED -> colors.success to "Completed"
        GoalStatus.PAUSED -> colors.warning to "Paused"
        GoalStatus.ABANDONED -> MaterialTheme.colorScheme.error to "Abandoned"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
private fun MilestoneItem(
    milestone: GoalMilestone,
    isGoalCompleted: Boolean,
    onToggle: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isGoalCompleted) {
                    Modifier.clickable(onClick = onToggle)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (milestone.isCompleted) {
            colors.success.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (milestone.isCompleted) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (milestone.isCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (milestone.isCompleted) colors.success else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (milestone.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else null,
                    color = if (milestone.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (milestone.isCompleted && milestone.completedAt != null) {
                    Text(
                        text = "Completed ${formatDate(milestone.completedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.success
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AddMilestoneDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Milestone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Complete first module") },
                singleLine = true,
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

@Composable
private fun EditProgressDialog(
    currentProgress: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var progress by remember { mutableFloatStateOf(currentProgress) }
    val colors = PersonAllyTheme.extendedColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.gradientMiddle,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..1f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.gradientMiddle,
                        activeTrackColor = colors.gradientMiddle
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(progress) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun calculateDaysRemaining(targetDate: Long): Int {
    val now = System.currentTimeMillis()
    val diff = targetDate - now
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}
