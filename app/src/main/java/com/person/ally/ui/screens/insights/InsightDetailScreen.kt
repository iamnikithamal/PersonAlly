package com.person.ally.ui.screens.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightSource
import com.person.ally.data.model.InsightType
import com.person.ally.data.model.LifeDomain
import com.person.ally.ui.components.GradientButton
import com.person.ally.ui.components.LoadingView
import com.person.ally.ui.components.SectionHeader
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InsightDetailScreen(
    insightId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var insight by remember { mutableStateOf<Insight?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(insightId) {
        insight = app.insightRepository.getInsightById(insightId)
        isLoading = false
        insight?.let {
            if (!it.isRead) {
                app.insightRepository.markAsRead(insightId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insight") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    insight?.let { currentInsight ->
                        IconButton(
                            onClick = {
                                scope.launch {
                                    app.insightRepository.updateBookmarkStatus(
                                        insightId,
                                        !currentInsight.isBookmarked
                                    )
                                    insight = app.insightRepository.getInsightById(insightId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentInsight.isBookmarked) {
                                    Icons.Default.Bookmark
                                } else {
                                    Icons.Default.BookmarkBorder
                                },
                                contentDescription = if (currentInsight.isBookmarked) {
                                    "Remove bookmark"
                                } else {
                                    "Bookmark"
                                }
                            )
                        }

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
                                DropdownMenuItem(
                                    text = { Text("Copy to clipboard") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        val text = buildString {
                                            appendLine(currentInsight.title)
                                            appendLine()
                                            appendLine(currentInsight.content)
                                            if (currentInsight.isActionable && currentInsight.actionSuggestion != null) {
                                                appendLine()
                                                appendLine("Action: ${currentInsight.actionSuggestion}")
                                            }
                                        }
                                        clipboardManager.setText(AnnotatedString(text))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete insight") },
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
            if (insight != null) {
                InsightDetailContent(
                    insight = insight!!,
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
                        text = "Insight not found",
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
            title = { Text("Delete Insight") },
            text = { Text("Are you sure you want to delete this insight? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.insightRepository.deleteInsightById(insightId)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InsightDetailContent(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors
    val iconColor = getInsightTypeColor(insight.type, colors)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getInsightTypeIcon(insight.type),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = iconColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = insight.type.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = iconColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDateTime(insight.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = insight.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = insight.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )

        if (insight.isActionable && insight.actionSuggestion != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ActionSuggestionCard(
                suggestion = insight.actionSuggestion,
                colors = colors
            )
        }

        if (insight.relatedDomains.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Related Domains",
                modifier = Modifier.padding(bottom = 12.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                insight.relatedDomains.forEach { domain ->
                    DomainChip(
                        domain = domain,
                        color = colors.getDomainColor(domain)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                InsightMetaItem(
                    label = "Source",
                    value = insight.source.getDisplayName()
                )

                if (insight.confidence < 1.0f) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightMetaItem(
                        label = "Confidence",
                        value = "${(insight.confidence * 100).toInt()}%"
                    )
                }

                if (insight.relatedMemoryIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightMetaItem(
                        label = "Related memories",
                        value = "${insight.relatedMemoryIds.size}"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ActionSuggestionCard(
    suggestion: String,
    colors: com.person.ally.ui.theme.ExtendedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.success.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Recommend,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Suggested Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.success
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DomainChip(
    domain: LifeDomain,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = domain.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InsightMetaItem(
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

private fun getInsightTypeIcon(type: InsightType): ImageVector = when (type) {
    InsightType.PATTERN -> Icons.Default.AutoAwesome
    InsightType.DISCOVERY -> Icons.Default.Lightbulb
    InsightType.GROWTH -> Icons.Default.TrendingUp
    InsightType.REFLECTION -> Icons.Default.Psychology
    InsightType.RECOMMENDATION -> Icons.Default.Recommend
    InsightType.MILESTONE -> Icons.Default.EmojiEvents
}

private fun getInsightTypeColor(
    type: InsightType,
    colors: com.person.ally.ui.theme.ExtendedColors
): Color = when (type) {
    InsightType.PATTERN -> colors.gradientStart
    InsightType.DISCOVERY -> colors.warning
    InsightType.GROWTH -> colors.success
    InsightType.REFLECTION -> colors.info
    InsightType.RECOMMENDATION -> colors.gradientMiddle
    InsightType.MILESTONE -> colors.gradientEnd
}

private fun InsightType.getDisplayName(): String = when (this) {
    InsightType.PATTERN -> "Pattern"
    InsightType.DISCOVERY -> "Discovery"
    InsightType.GROWTH -> "Growth"
    InsightType.REFLECTION -> "Reflection"
    InsightType.RECOMMENDATION -> "Recommendation"
    InsightType.MILESTONE -> "Milestone"
}

private fun InsightSource.getDisplayName(): String = when (this) {
    InsightSource.CONVERSATION -> "Conversation"
    InsightSource.ASSESSMENT -> "Assessment"
    InsightSource.BEHAVIOR -> "Behavioral Pattern"
    InsightSource.SYSTEM_GENERATED -> "System Generated"
    InsightSource.USER_CREATED -> "User Created"
}

private fun formatDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
