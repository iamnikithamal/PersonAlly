package com.person.ally.ui.screens.profile

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToAssessments: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp

    val userProfile by app.userProfileRepository.userProfile.collectAsState(initial = null)
    val universalContext by app.userProfileRepository.getUniversalContext().collectAsState(initial = null)
    val memoryCount by app.memoryRepository.getMemoryCount().collectAsState(initial = 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            ProfileHeader(
                userProfile = userProfile,
                onSettingsClick = onNavigateToSettings
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatsRow(
                userProfile = userProfile,
                memoryCount = memoryCount
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            ContextCard(
                universalContext = universalContext,
                onExportClick = onNavigateToExport,
                onEditClick = onNavigateToEdit
            )
        }

        if (userProfile?.personalityTraits?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Personality Traits",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                PersonalityTraitsSection(
                    traits = userProfile?.personalityTraits ?: emptyList()
                )
            }
        }

        if (userProfile?.coreValues?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Core Values",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                CoreValuesSection(
                    values = userProfile?.coreValues ?: emptyList()
                )
            }
        }

        if (userProfile?.lifeDomainProgress?.isNotEmpty() == true) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Life Domains Progress",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(userProfile?.lifeDomainProgress ?: emptyList()) { progress ->
                LifeDomainProgressItem(
                    progress = progress,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Quick Actions",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            QuickActionsSection(
                onAssessmentsClick = onNavigateToAssessments,
                onSettingsClick = onNavigateToSettings
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: UserProfile?,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(20.dp)
            .padding(top = 48.dp)
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
                        .size(72.dp)
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
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userProfile?.displayName ?: "Welcome",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getMembershipDuration(userProfile?.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSettingsClick),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    userProfile: UserProfile?,
    memoryCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            icon = Icons.Default.Memory,
            value = memoryCount.toString(),
            label = "Memories",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.Insights,
            value = (userProfile?.totalInsights ?: 0).toString(),
            label = "Insights",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.Checklist,
            value = (userProfile?.totalAssessmentsCompleted ?: 0).toString(),
            label = "Assessments",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Universal Context",
                            style = MaterialTheme.typography.titleMedium,
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
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onEditClick),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onExportClick),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (universalContext != null && universalContext.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = universalContext.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (universalContext.coreIdentityPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    universalContext.coreIdentityPoints.take(3).forEach { point ->
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
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your universal context is being built as you use PersonAlly. Talk to Ally and complete assessments to build your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PersonalityTraitsSection(traits: List<PersonalityTrait>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(traits) { trait ->
            PersonalityTraitCard(trait = trait)
        }
    }
}

@Composable
private fun PersonalityTraitCard(trait: PersonalityTrait) {
    Surface(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trait.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressIndicator(
                progress = trait.score,
                color = MaterialTheme.colorScheme.primary,
                height = 6.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(trait.score * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trait.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CoreValuesSection(values: List<ValueItem>) {
    val colors = PersonAllyTheme.extendedColors

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(values.size) { index ->
            val value = values[index]
            val color = colors.getDomainColor(
                com.person.ally.data.model.LifeDomain.entries[index % com.person.ally.data.model.LifeDomain.entries.size]
            )
            ValueItemCard(
                value = value,
                color = color
            )
        }
    }
}

@Composable
private fun ValueItemCard(
    value: ValueItem,
    color: Color
) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.name.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(value.importance * 100).toInt()}% important",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LifeDomainProgressItem(
    progress: LifeDomainProgress,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val domainColor = colors.getDomainColor(progress.domain)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    .background(
                        color = domainColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = progress.domain.displayName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
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
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (progress.trend) {
                                Trend.IMPROVING -> Icons.Default.TrendingUp
                                Trend.DECLINING -> Icons.Default.TrendingDown
                                else -> Icons.Default.TrendingFlat
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (progress.trend) {
                                Trend.IMPROVING -> colors.success
                                Trend.DECLINING -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(progress.score * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = domainColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProgressIndicator(
                    progress = progress.score,
                    color = domainColor,
                    height = 6.dp
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAssessmentsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionItem(
            icon = Icons.Default.Checklist,
            title = "Take Assessments",
            subtitle = "Discover more about yourself",
            onClick = onAssessmentsClick
        )
        QuickActionItem(
            icon = Icons.Default.Settings,
            title = "Settings",
            subtitle = "Customize your experience",
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        com.person.ally.data.model.LifeDomain.PERSONAL_GROWTH -> "Personal Growth"
        com.person.ally.data.model.LifeDomain.FINANCE -> "Finance"
        com.person.ally.data.model.LifeDomain.CREATIVITY -> "Creativity"
        com.person.ally.data.model.LifeDomain.SPIRITUALITY -> "Spirituality"
        com.person.ally.data.model.LifeDomain.RECREATION -> "Recreation"
    }
