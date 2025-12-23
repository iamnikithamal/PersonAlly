package com.person.ally.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.DailyBriefing
import com.person.ally.data.model.Goal
import com.person.ally.data.model.GoalStatus
import com.person.ally.data.model.Habit
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightType
import com.person.ally.data.model.LifeDomain
import com.person.ally.ui.components.PersonAllyCard
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
    onNavigateToMemories: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val userProfile by app.userProfileRepository.userProfile.collectAsState(initial = null)
    val todayBriefing by app.insightRepository.getTodayBriefing().collectAsState(initial = null)
    val recentInsights by app.insightRepository.getRecentInsights(5).collectAsState(initial = emptyList())
    val activeGoals by app.insightRepository.getGoalsByStatus(GoalStatus.IN_PROGRESS).collectAsState(initial = emptyList())
    val memoryCount by app.memoryRepository.getMemoryCount().collectAsState(initial = 0)
    val activeHabits by app.insightRepository.getActiveHabits().collectAsState(initial = emptyList())
    val conversationCount by app.chatRepository.getConversationCount().collectAsState(initial = 0)

    var isVisible by remember { mutableStateOf(false) }
    var selectedMoodIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Welcome Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it }
            ) {
                WelcomeHeader(
                    userName = userProfile?.name ?: "Friend",
                    onChatClick = onNavigateToChat
                )
            }
        }

        // Quick Stats Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 4 }
            ) {
                QuickStatsRow(
                    memoryCount = memoryCount,
                    conversationCount = conversationCount,
                    insightCount = recentInsights.size,
                    goalCount = activeGoals.size,
                    onMemoriesClick = onNavigateToMemories,
                    onInsightsClick = onNavigateToInsights
                )
            }
        }

        // Daily Mood Check-in
        item {
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                MoodCheckInSection(
                    selectedMoodIndex = selectedMoodIndex,
                    onMoodSelected = { index ->
                        selectedMoodIndex = index
                        // Mood tracking - recorded for user feedback
                        // Future: persist mood entries in a dedicated table
                    }
                )
            }
        }

        // Quick Actions Grid
        item {
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                QuickActionsSection(
                    onChatClick = onNavigateToChat,
                    onAssessmentsClick = onNavigateToAssessments,
                    onMemoriesClick = onNavigateToMemories,
                    onInsightsClick = onNavigateToInsights
                )
            }
        }

        // Daily Briefing
        if (todayBriefing != null) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    DailyBriefingCard(briefing = todayBriefing!!)
                }
            }
        }

        // Active Habits Section
        if (activeHabits.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Today's Habits",
                    action = "${activeHabits.size} active",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
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

        // Active Goals Section
        if (activeGoals.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Active Goals",
                    action = "View All",
                    onActionClick = onNavigateToInsights,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeGoals.take(5)) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onNavigateToGoal(goal.id) }
                        )
                    }
                }
            }
        }

        // Recent Insights Section
        if (recentInsights.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Recent Insights",
                    action = "View All",
                    onActionClick = onNavigateToInsights,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(recentInsights.take(3)) { insight ->
                InsightCard(
                    insight = insight,
                    onClick = { onNavigateToInsight(insight.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }

        // Productivity Tools Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Productivity Tools",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            ProductivityToolsSection(
                onChatClick = onNavigateToChat,
                onAssessmentsClick = onNavigateToAssessments
            )
        }

        // Life Domains Overview
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Life Domains",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            LifeDomainsGrid(modifier = Modifier.padding(horizontal = 20.dp))
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WelcomeHeader(
    userName: String,
    onChatClick: () -> Unit
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(top = 48.dp)
    ) {
        Text(
            text = currentDate,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getGreetingText(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onChatClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "Chat with Ally",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    memoryCount: Int,
    conversationCount: Int,
    insightCount: Int,
    goalCount: Int,
    onMemoriesClick: () -> Unit,
    onInsightsClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            QuickStatChip(
                icon = Icons.Filled.Memory,
                value = memoryCount.toString(),
                label = "Memories",
                onClick = onMemoriesClick
            )
        }
        item {
            QuickStatChip(
                icon = Icons.Filled.Chat,
                value = conversationCount.toString(),
                label = "Chats",
                onClick = {}
            )
        }
        item {
            QuickStatChip(
                icon = Icons.Filled.Lightbulb,
                value = insightCount.toString(),
                label = "Insights",
                onClick = onInsightsClick
            )
        }
        item {
            QuickStatChip(
                icon = Icons.Filled.Flag,
                value = goalCount.toString(),
                label = "Goals",
                onClick = onInsightsClick
            )
        }
    }
}

@Composable
private fun QuickStatChip(
    icon: ImageVector,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
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
private fun MoodCheckInSection(
    selectedMoodIndex: Int,
    onMoodSelected: (Int) -> Unit
) {
    val moods = listOf(
        MoodOption(Icons.Filled.SentimentVeryDissatisfied, "Very Low", Color(0xFFE57373)),
        MoodOption(Icons.Filled.SentimentDissatisfied, "Low", Color(0xFFFFB74D)),
        MoodOption(Icons.Filled.SentimentNeutral, "Okay", Color(0xFFFFF176)),
        MoodOption(Icons.Filled.SentimentSatisfied, "Good", Color(0xFFAED581)),
        MoodOption(Icons.Filled.SentimentSatisfiedAlt, "Great", Color(0xFF81C784)),
        MoodOption(Icons.Filled.SentimentVerySatisfied, "Amazing", Color(0xFF4DB6AC))
    )

    PersonAllyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEmotions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moods.forEachIndexed { index, mood ->
                    MoodButton(
                        mood = mood,
                        isSelected = selectedMoodIndex == index,
                        onClick = { onMoodSelected(index) }
                    )
                }
            }

            if (selectedMoodIndex >= 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You're feeling ${moods[selectedMoodIndex].label.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
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
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = tween(200),
        label = "moodScale"
    )

    Surface(
        modifier = Modifier
            .size((40 * scale).dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) mood.color.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = mood.label,
                tint = if (isSelected) mood.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size((28 * scale).dp)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onChatClick: () -> Unit,
    onAssessmentsClick: () -> Unit,
    onMemoriesClick: () -> Unit,
    onInsightsClick: () -> Unit
) {
    val actions = listOf(
        QuickAction(Icons.Filled.Chat, "Chat", "Talk to Ally", MaterialTheme.colorScheme.primary, onChatClick),
        QuickAction(Icons.Filled.Checklist, "Assess", "Self-discovery", MaterialTheme.colorScheme.secondary, onAssessmentsClick),
        QuickAction(Icons.Filled.Memory, "Memories", "Your thoughts", MaterialTheme.colorScheme.tertiary, onMemoriesClick),
        QuickAction(Icons.Filled.Lightbulb, "Insights", "View insights", PersonAllyTheme.extendedColors.warning, onInsightsClick)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            QuickActionCard(
                action = action,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = action.onClick),
        shape = RoundedCornerShape(16.dp),
        color = action.color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = action.color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = action.color
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyBriefingCard(briefing: DailyBriefing) {
    PersonAllyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Daily Briefing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Your personalized summary",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = briefing.greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (briefing.keyInsights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                briefing.keyInsights.take(3).forEach { insight ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .padding(top = 6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        contentPadding = PaddingValues(horizontal = 20.dp),
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

    PersonAllyCard(
        modifier = Modifier.width(160.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = domainColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Loop,
                        contentDescription = null,
                        tint = domainColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onComplete),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Complete",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = PersonAllyTheme.extendedColors.warning,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${habit.currentStreak} day streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

    PersonAllyCard(
        modifier = Modifier.width(180.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = domainColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = domainColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = goal.domain.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = domainColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall,
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
                text = "${(goal.progress * 100).toInt()}% complete",
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

    PersonAllyCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = insightColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = insightColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (insight.isBookmarked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = insight.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
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

@Composable
private fun ProductivityToolsSection(
    onChatClick: () -> Unit,
    onAssessmentsClick: () -> Unit
) {
    val tools = listOf(
        ProductivityTool(Icons.Filled.Notes, "Quick Note", "Capture a thought", MaterialTheme.colorScheme.primary),
        ProductivityTool(Icons.Filled.Timer, "Focus Timer", "Stay productive", MaterialTheme.colorScheme.secondary),
        ProductivityTool(Icons.Filled.Event, "Schedule", "Plan your day", MaterialTheme.colorScheme.tertiary),
        ProductivityTool(Icons.Filled.BarChart, "Progress", "Track growth", PersonAllyTheme.extendedColors.success)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools) { tool ->
            ProductivityToolCard(
                tool = tool,
                onClick = {
                    when (tool.title) {
                        "Quick Note" -> onChatClick()
                        "Progress" -> onAssessmentsClick()
                        else -> {}
                    }
                }
            )
        }
    }
}

private data class ProductivityTool(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val color: Color
)

@Composable
private fun ProductivityToolCard(
    tool: ProductivityTool,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = tool.color.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tool.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = tool.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LifeDomainsGrid(modifier: Modifier = Modifier) {
    val colors = PersonAllyTheme.extendedColors
    val domains = LifeDomain.entries.take(6)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        domains.chunked(3).forEach { rowDomains ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowDomains.forEach { domain ->
                    LifeDomainItem(
                        domain = domain,
                        color = colors.getDomainColor(domain),
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowDomains.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LifeDomainItem(
    domain: LifeDomain,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = domain.displayName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = domain.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

private fun getMoodValue(index: Int): Int = when (index) {
    0 -> 1
    1 -> 2
    2 -> 3
    3 -> 4
    4 -> 5
    5 -> 6
    else -> 3
}

private fun getMoodEnergy(index: Int): Int = when (index) {
    0, 1 -> 2
    2 -> 3
    3, 4 -> 4
    5 -> 5
    else -> 3
}

private val LifeDomain.displayName: String
    get() = when (this) {
        LifeDomain.CAREER -> "Career"
        LifeDomain.RELATIONSHIPS -> "Relationships"
        LifeDomain.HEALTH -> "Health"
        LifeDomain.PERSONAL_GROWTH -> "Personal Growth"
        LifeDomain.FINANCE -> "Finance"
        LifeDomain.CREATIVITY -> "Creativity"
        LifeDomain.SPIRITUALITY -> "Spirituality"
        LifeDomain.RECREATION -> "Recreation"
    }
