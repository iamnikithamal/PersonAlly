package com.person.ally.ui.screens.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.Goal
import com.person.ally.data.model.GoalStatus
import com.person.ally.data.model.Habit
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightType
import com.person.ally.ui.components.EmptyStateView
import com.person.ally.ui.components.LoadingView
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInsight: (Long) -> Unit,
    onNavigateToGoal: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedInsightType by remember { mutableStateOf<InsightType?>(null) }

    val allInsights by app.insightRepository.getAllInsights().collectAsState(initial = null)
    val unreadCount by app.insightRepository.getUnreadCount().collectAsState(initial = 0)
    val bookmarkedInsights by app.insightRepository.getBookmarkedInsights().collectAsState(initial = emptyList())
    val activeGoals by app.insightRepository.getGoalsByStatus(GoalStatus.IN_PROGRESS).collectAsState(initial = emptyList())
    val completedGoals by app.insightRepository.getCompletedGoals().collectAsState(initial = emptyList())
    val activeHabits by app.insightRepository.getActiveHabits().collectAsState(initial = emptyList())

    val tabs = listOf("Insights", "Goals", "Habits")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Insights",
                            fontWeight = FontWeight.SemiBold
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    if (unreadCount > 0) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    scope.launch {
                                        app.insightRepository.markAllAsRead()
                                    }
                                },
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Mark all read",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> InsightsTab(
                    insights = allInsights,
                    bookmarkedInsights = bookmarkedInsights,
                    selectedType = selectedInsightType,
                    onTypeSelected = { selectedInsightType = it },
                    onInsightClick = onNavigateToInsight,
                    onBookmarkToggle = { insight, bookmarked ->
                        scope.launch {
                            app.insightRepository.updateBookmarkStatus(insight.id, bookmarked)
                        }
                    },
                    onMarkAsRead = { insightId ->
                        scope.launch {
                            app.insightRepository.markAsRead(insightId)
                        }
                    }
                )
                1 -> GoalsTab(
                    activeGoals = activeGoals,
                    completedGoals = completedGoals,
                    onGoalClick = onNavigateToGoal
                )
                2 -> HabitsTab(
                    habits = activeHabits,
                    onHabitComplete = { habitId ->
                        scope.launch {
                            app.insightRepository.recordHabitCompletion(habitId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun InsightsTab(
    insights: List<Insight>?,
    bookmarkedInsights: List<Insight>,
    selectedType: InsightType?,
    onTypeSelected: (InsightType?) -> Unit,
    onInsightClick: (Long) -> Unit,
    onBookmarkToggle: (Insight, Boolean) -> Unit,
    onMarkAsRead: (Long) -> Unit
) {
    when {
        insights == null -> {
            LoadingView(
                modifier = Modifier.fillMaxSize(),
                message = "Loading insights..."
            )
        }
        insights.isEmpty() -> {
            EmptyStateView(
                icon = Icons.Default.Lightbulb,
                title = "No insights yet",
                subtitle = "As you use PersonAlly, insights about your patterns and growth will appear here.",
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            val filteredInsights = if (selectedType != null) {
                insights.filter { it.type == selectedType && !it.isDismissed }
            } else {
                insights.filter { !it.isDismissed }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    InsightTypeFilters(
                        selectedType = selectedType,
                        onTypeSelected = onTypeSelected,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                if (bookmarkedInsights.isNotEmpty() && selectedType == null) {
                    item {
                        SectionHeader(
                            title = "Bookmarked",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookmarkedInsights.take(5)) { insight ->
                                CompactInsightCard(
                                    insight = insight,
                                    onClick = { onInsightClick(insight.id) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    SectionHeader(
                        title = if (selectedType != null) "${selectedType.getDisplayName()}s" else "All Insights",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(
                    items = filteredInsights,
                    key = { it.id }
                ) { insight ->
                    InsightCard(
                        insight = insight,
                        onClick = {
                            if (!insight.isRead) {
                                onMarkAsRead(insight.id)
                            }
                            onInsightClick(insight.id)
                        },
                        onBookmarkToggle = { bookmarked ->
                            onBookmarkToggle(insight, bookmarked)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightTypeFilters(
    selectedType: InsightType?,
    onTypeSelected: (InsightType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        items(InsightType.entries.toList()) { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.getDisplayName()) },
                leadingIcon = {
                    Icon(
                        imageVector = type.getIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight,
    onClick: () -> Unit,
    onBookmarkToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val iconColor = when (insight.type) {
        InsightType.PATTERN -> MaterialTheme.colorScheme.primary
        InsightType.DISCOVERY -> colors.warning
        InsightType.GROWTH -> colors.success
        InsightType.REFLECTION -> colors.info
        InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.secondary
        InsightType.MILESTONE -> MaterialTheme.colorScheme.tertiary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (insight.isRead) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = iconColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = insight.type.getIcon(),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = insight.type.getDisplayName(),
                                style = MaterialTheme.typography.labelSmall,
                                color = iconColor
                            )
                            if (!insight.isRead) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                        Text(
                            text = formatRelativeTime(insight.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onBookmarkToggle(!insight.isBookmarked) }
                ) {
                    Icon(
                        imageVector = if (insight.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (insight.isBookmarked) "Remove bookmark" else "Bookmark",
                        tint = if (insight.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = insight.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (insight.isActionable && insight.actionSuggestion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.success.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recommend,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = insight.actionSuggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.success
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactInsightCard(
    insight: Insight,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = insight.type.getIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = insight.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoalsTab(
    activeGoals: List<Goal>,
    completedGoals: List<Goal>,
    onGoalClick: (Long) -> Unit
) {
    if (activeGoals.isEmpty() && completedGoals.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Flag,
            title = "No goals yet",
            subtitle = "Set goals to track your progress across life domains.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (activeGoals.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Active Goals",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                items(
                    items = activeGoals,
                    key = { it.id }
                ) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            if (completedGoals.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Completed",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                items(
                    items = completedGoals.take(5),
                    key = { it.id }
                ) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(goal.domain)
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(500),
        label = "progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (goal.isCompleted) {
            colors.success.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
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
                            .size(36.dp)
                            .background(
                                color = domainColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (goal.isCompleted) colors.success else domainColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = goal.domain.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = domainColor
                    )
                }

                Text(
                    text = "${(goal.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) colors.success else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .background(
                            color = if (goal.isCompleted) colors.success else domainColor,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            if (goal.milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${goal.milestones.count { it.isCompleted }}/${goal.milestones.size} milestones",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HabitsTab(
    habits: List<Habit>,
    onHabitComplete: (Long) -> Unit
) {
    if (habits.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.CheckCircle,
            title = "No habits yet",
            subtitle = "Build consistent habits to improve your daily routine.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 0.dp)
        ) {
            items(
                items = habits,
                key = { it.id }
            ) { habit ->
                HabitCard(
                    habit = habit,
                    onComplete = { onHabitComplete(habit.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(habit.domain)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = domainColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = habit.title.first().uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = domainColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = habit.frequency.getDisplayName(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onComplete),
                    color = colors.success.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete habit",
                        tint = colors.success,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = habit.currentStreak > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${habit.currentStreak} day streak",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.warning,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${habit.totalCompletions} total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun InsightType.getDisplayName(): String = when (this) {
    InsightType.PATTERN -> "Pattern"
    InsightType.DISCOVERY -> "Discovery"
    InsightType.GROWTH -> "Growth"
    InsightType.REFLECTION -> "Reflection"
    InsightType.RECOMMENDATION -> "Recommendation"
    InsightType.MILESTONE -> "Milestone"
}

private fun InsightType.getIcon(): ImageVector = when (this) {
    InsightType.PATTERN -> Icons.Default.AutoAwesome
    InsightType.DISCOVERY -> Icons.Default.Lightbulb
    InsightType.GROWTH -> Icons.Default.TrendingUp
    InsightType.REFLECTION -> Icons.Default.Psychology
    InsightType.RECOMMENDATION -> Icons.Default.Recommend
    InsightType.MILESTONE -> Icons.Default.EmojiEvents
}

private fun com.person.ally.data.model.HabitFrequency.getDisplayName(): String = when (this) {
    com.person.ally.data.model.HabitFrequency.DAILY -> "Daily"
    com.person.ally.data.model.HabitFrequency.WEEKLY -> "Weekly"
    com.person.ally.data.model.HabitFrequency.SPECIFIC_DAYS -> "Specific days"
    com.person.ally.data.model.HabitFrequency.INTERVAL -> "Interval"
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 7 -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
