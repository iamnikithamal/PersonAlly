package com.person.ally.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.person.ally.data.model.LifeDomainProgress
import com.person.ally.data.model.PersonalityTrait
import com.person.ally.data.model.Trend
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.model.UserProfile
import com.person.ally.data.model.ValueItem
import com.person.ally.ui.components.ProgressIndicator
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToAssessments: () -> Unit,
    onNavigateToMemories: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp

    val userProfile by app.userProfileRepository.userProfile.collectAsState(initial = null)
    val universalContext by app.userProfileRepository.getUniversalContext().collectAsState(initial = null)
    val memoryCount by app.memoryRepository.getMemoryCount().collectAsState(initial = 0)

    var isVisible by remember { mutableStateOf(false) }

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
        // Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                ProfileHeader(
                    userProfile = userProfile,
                    onSettingsClick = onNavigateToSettings,
                    onEditClick = onNavigateToEdit
                )
            }
        }

        // Stats Row
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 100)) { it / 4 }
            ) {
                StatsRow(
                    userProfile = userProfile,
                    memoryCount = memoryCount,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Universal Context Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 150)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 150)) { it / 4 }
            ) {
                ContextCard(
                    universalContext = universalContext,
                    onExportClick = onNavigateToExport,
                    onEditClick = onNavigateToEdit,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Personality Traits
        if (userProfile?.personalityTraits?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(
                    title = "Personality Traits",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                TraitsRow(traits = userProfile?.personalityTraits ?: emptyList())
            }
        }

        // Core Values
        if (userProfile?.coreValues?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(
                    title = "Core Values",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                ValuesRow(values = userProfile?.coreValues ?: emptyList())
            }
        }

        // Life Domains Progress
        if (userProfile?.lifeDomainProgress?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(
                    title = "Life Domains",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(userProfile?.lifeDomainProgress ?: emptyList()) { progress ->
                DomainProgressItem(
                    progress = progress,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle(
                title = "Quick Actions",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            QuickActionsCard(
                onMemoriesClick = onNavigateToMemories,
                onAssessmentsClick = onNavigateToAssessments,
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: UserProfile?,
    onSettingsClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = userProfile?.displayName
                        ?.split(" ")
                        ?.take(2)
                        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        ?.joinToString("")
                        ?: "?"

                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userProfile?.displayName ?: "Welcome",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getMembershipDuration(userProfile?.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    userProfile: UserProfile?,
    memoryCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            icon = Icons.Filled.Memory,
            value = memoryCount.toString(),
            label = "Memories",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Filled.Insights,
            value = (userProfile?.totalInsights ?: 0).toString(),
            label = "Insights",
            color = PersonAllyTheme.extendedColors.warning,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Filled.Checklist,
            value = (userProfile?.totalAssessmentsCompleted ?: 0).toString(),
            label = "Assessed",
            color = PersonAllyTheme.extendedColors.success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
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

@Composable
private fun ContextCard(
    universalContext: UniversalContext?,
    onExportClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Universal Context",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (universalContext?.isUserEdited == true) "User edited" else "Auto-generated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onExportClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (universalContext != null && universalContext.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = universalContext.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (universalContext.coreIdentityPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    universalContext.coreIdentityPoints.take(2).forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your context is being built as you use PersonAlly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun TraitsRow(traits: List<PersonalityTrait>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(traits) { trait ->
            TraitCard(trait = trait)
        }
    }
}

@Composable
private fun TraitCard(trait: PersonalityTrait) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = trait.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressIndicator(
                progress = trait.score,
                color = MaterialTheme.colorScheme.primary,
                height = 4.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(trait.score * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ValuesRow(values: List<ValueItem>) {
    val colors = PersonAllyTheme.extendedColors

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(values.size) { index ->
            val value = values[index]
            val color = colors.getDomainColor(
                com.person.ally.data.model.LifeDomain.entries[index % com.person.ally.data.model.LifeDomain.entries.size]
            )
            ValueCard(value = value, color = color)
        }
    }
}

@Composable
private fun ValueCard(
    value: ValueItem,
    color: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.name.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DomainProgressItem(
    progress: LifeDomainProgress,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(progress.domain)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(domainColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = progress.domain.displayName.first().toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = domainColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progress.domain.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (progress.trend) {
                                Trend.IMPROVING -> Icons.Filled.TrendingUp
                                Trend.DECLINING -> Icons.Filled.TrendingDown
                                else -> Icons.Filled.TrendingFlat
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when (progress.trend) {
                                Trend.IMPROVING -> colors.success
                                Trend.DECLINING -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(progress.score * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = domainColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                ProgressIndicator(
                    progress = progress.score,
                    color = domainColor,
                    height = 4.dp
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onMemoriesClick: () -> Unit,
    onAssessmentsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            ActionItem(
                icon = Icons.Filled.Memory,
                title = "Memories",
                subtitle = "View saved memories",
                onClick = onMemoriesClick
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            ActionItem(
                icon = Icons.Filled.Checklist,
                title = "Assessments",
                subtitle = "Discover more about yourself",
                onClick = onAssessmentsClick
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            ActionItem(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                subtitle = "Customize your experience",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun getMembershipDuration(createdAt: Long?): String {
    if (createdAt == null) return "New member"

    val now = System.currentTimeMillis()
    val diff = now - createdAt
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days < 1 -> "Joined today"
        days < 7 -> "Member for $days days"
        days < 30 -> "Member for ${days / 7} weeks"
        days < 365 -> "Member for ${days / 30} months"
        else -> "Member for ${days / 365} years"
    }
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
