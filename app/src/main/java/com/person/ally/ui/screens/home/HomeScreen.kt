package com.person.ally.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.Goal
import com.person.ally.data.model.GoalStatus
import com.person.ally.data.model.Habit
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightType
import com.person.ally.data.model.MoodLevel
import com.person.ally.ui.components.ProgressIndicator
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToAssessments: () -> Unit,
    onNavigateToInsight: (Long) -> Unit,
    onNavigateToGoal: (Long) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToMemories: () -> Unit,
    onNavigateToJournal: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val userProfile by app.userProfileRepository.userProfile.collectAsState(initial = null)
    val recentInsights by app.insightRepository.getRecentInsights(5).collectAsState(initial = emptyList())
    val activeGoals by app.insightRepository.getGoalsByStatus(GoalStatus.IN_PROGRESS).collectAsState(initial = emptyList())
    val memoryCount by app.memoryRepository.getMemoryCount().collectAsState(initial = 0)
    val activeHabits by app.insightRepository.getActiveHabits().collectAsState(initial = emptyList())
    val conversationCount by app.chatRepository.getConversationCount().collectAsState(initial = 0)

    var isVisible by remember { mutableStateOf(false) }
    var selectedMoodIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        val todaysMood = app.wellnessRepository.getTodaysMoodEntry()
        if (todaysMood != null) {
            selectedMoodIndex = todaysMood.moodLevel.ordinal
        }
        delay(100)
        isVisible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                HomeHeader(
                    userName = userProfile?.preferredName ?: userProfile?.name ?: "Friend",
                    onSettingsClick = onNavigateToSettings
                )
            }
        }

        // Hero Chat Card
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 100)) { it / 4 }
            ) {
                HeroChatCard(
                    onChatClick = onNavigateToChat,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Mood Check-in
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 150)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 150)) { it / 4 }
            ) {
                CompactMoodSection(
                    selectedMoodIndex = selectedMoodIndex,
                    onMoodSelected = { index ->
                        selectedMoodIndex = index
                        scope.launch {
                            val moodLevel = MoodLevel.entries[index]
                            app.wellnessRepository.logMood(
                                moodLevel = moodLevel,
                                energyLevel = getMoodEnergy(index),
                                stressLevel = 10 - getMoodEnergy(index)
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Quick Stats Grid
        item {
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 200)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 200)) { it / 4 }
            ) {
                QuickStatsGrid(
                    memoryCount = memoryCount,
                    conversationCount = conversationCount,
                    insightCount = recentInsights.size,
                    goalCount = activeGoals.size,
                    onMemoriesClick = onNavigateToMemories,
                    onInsightsClick = onNavigateToInsights,
                    onAssessmentsClick = onNavigateToAssessments,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Active Habits
        if (activeHabits.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Today's Habits",
                    action = "See all",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                HabitsRow(
                    habits = activeHabits,
                    onHabitComplete = { habit ->
                        scope.launch {
                            app.insightRepository.recordHabitCompletion(habit.id)
                        }
                    }
                )
            }
        }

        // Active Goals
        if (activeGoals.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Active Goals",
                    action = "View all",
                    onActionClick = onNavigateToInsights,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                GoalsRow(
                    goals = activeGoals.take(5),
                    onGoalClick = onNavigateToGoal
                )
            }
        }

        // Recent Insights
        if (recentInsights.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Recent Insights",
                    action = "View all",
                    onActionClick = onNavigateToInsights,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(recentInsights.take(3)) { insight ->
                InsightCard(
                    insight = insight,
                    onClick = { onNavigateToInsight(insight.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onSettingsClick: () -> Unit
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${getGreetingText()} $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroChatCard(
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onChatClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Talk to Ally",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your AI companion is ready to help",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CompactMoodSection(
    selectedMoodIndex: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        MoodOption(Icons.Filled.SentimentVeryDissatisfied, "Awful", Color(0xFFE57373)),
        MoodOption(Icons.Filled.SentimentDissatisfied, "Bad", Color(0xFFFFB74D)),
        MoodOption(Icons.Filled.SentimentNeutral, "Okay", Color(0xFFFFF176)),
        MoodOption(Icons.Filled.SentimentSatisfied, "Good", Color(0xFFAED581)),
        MoodOption(Icons.Filled.SentimentSatisfiedAlt, "Great", Color(0xFF81C784)),
        MoodOption(Icons.Filled.SentimentVerySatisfied, "Amazing", Color(0xFF4DB6AC))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moods.forEachIndexed { index, mood ->
                    MoodButton(
                        mood = mood,
                        isSelected = selectedMoodIndex == index,
                        onClick = { onMoodSelected(index) }
                    )
                }
            }
        }
    }
}

private data class MoodOption(
    val icon: ImageVector,
    val label: String,
    val color: Color
)

@Composable
private fun MoodButton(
    mood: MoodOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "moodScale"
    )

    Box(
        modifier = Modifier
            .size((36 * scale).dp)
            .clip(CircleShape)
            .background(if (isSelected) mood.color.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = mood.icon,
            contentDescription = mood.label,
            tint = if (isSelected) mood.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size((24 * scale).dp)
        )
    }
}

@Composable
private fun QuickStatsGrid(
    memoryCount: Int,
    conversationCount: Int,
    insightCount: Int,
    goalCount: Int,
    onMemoriesClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onAssessmentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.Memory,
                value = memoryCount.toString(),
                label = "Memories",
                color = MaterialTheme.colorScheme.primary,
                onClick = onMemoriesClick,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Lightbulb,
                value = insightCount.toString(),
                label = "Insights",
                color = PersonAllyTheme.extendedColors.warning,
                onClick = onInsightsClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.Flag,
                value = goalCount.toString(),
                label = "Goals",
                color = PersonAllyTheme.extendedColors.success,
                onClick = onInsightsClick,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Checklist,
                value = "Assess",
                label = "Discover you",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onAssessmentsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HabitsRow(
    habits: List<Habit>,
    onHabitComplete: (Habit) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits.take(5)) { habit ->
            HabitCard(
                habit = habit,
                onComplete = { onHabitComplete(habit) }
            )
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onComplete: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(habit.domain)

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onComplete),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(domainColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = domainColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "${habit.currentStreak}d",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = domainColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoalsRow(
    goals: List<Goal>,
    onGoalClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals) { goal ->
            GoalCard(
                goal = goal,
                onClick = { onGoalClick(goal.id) }
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onClick: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(goal.domain)

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(domainColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = domainColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = goal.domain.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = domainColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressIndicator(
                progress = goal.progress,
                color = domainColor,
                height = 4.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(goal.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insightColor = when (insight.type) {
        InsightType.PATTERN -> MaterialTheme.colorScheme.primary
        InsightType.DISCOVERY -> MaterialTheme.colorScheme.tertiary
        InsightType.GROWTH -> PersonAllyTheme.extendedColors.success
        InsightType.REFLECTION -> MaterialTheme.colorScheme.secondary
        InsightType.RECOMMENDATION -> PersonAllyTheme.extendedColors.warning
        InsightType.MILESTONE -> PersonAllyTheme.extendedColors.info
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(insightColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = insightColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = insight.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getGreetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else -> "Good evening,"
    }
}

private fun getMoodEnergy(index: Int): Int = when (index) {
    0, 1 -> 2
    2 -> 3
    3, 4 -> 4
    5 -> 5
    else -> 3
}

private val com.person.ally.data.model.LifeDomain.displayName: String
    get() = when (this) {
        com.person.ally.data.model.LifeDomain.CAREER -> "Career"
        com.person.ally.data.model.LifeDomain.RELATIONSHIPS -> "Relationships"
        com.person.ally.data.model.LifeDomain.HEALTH -> "Health"
        com.person.ally.data.model.LifeDomain.PERSONAL_GROWTH -> "Growth"
        com.person.ally.data.model.LifeDomain.FINANCE -> "Finance"
        com.person.ally.data.model.LifeDomain.CREATIVITY -> "Creativity"
        com.person.ally.data.model.LifeDomain.SPIRITUALITY -> "Spirituality"
        com.person.ally.data.model.LifeDomain.RECREATION -> "Recreation"
    }
