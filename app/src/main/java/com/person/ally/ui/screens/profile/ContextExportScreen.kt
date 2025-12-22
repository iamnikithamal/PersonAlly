package com.person.ally.ui.screens.profile

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.repository.ExportFormat
import com.person.ally.ui.components.GradientButton
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextExportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val colors = PersonAllyTheme.extendedColors

    val universalContext by app.userProfileRepository.getUniversalContext().collectAsState(initial = null)

    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var isExporting by remember { mutableStateOf(false) }
    var exportPreview by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedFormat) {
        exportPreview = when (selectedFormat) {
            ExportFormat.JSON -> app.userProfileRepository.exportContextAsJson()
            ExportFormat.MARKDOWN -> app.userProfileRepository.exportContextAsMarkdown()
            ExportFormat.PLAIN_TEXT -> app.userProfileRepository.exportContextAsPlainText()
            ExportFormat.FULL_PROFILE -> app.userProfileRepository.exportFullProfile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Context") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormatOption(
                        icon = Icons.Default.Code,
                        title = "JSON",
                        subtitle = "Universal context data in JSON format",
                        isSelected = selectedFormat == ExportFormat.JSON,
                        onClick = { selectedFormat = ExportFormat.JSON }
                    )
                    ExportFormatOption(
                        icon = Icons.Default.Description,
                        title = "Markdown",
                        subtitle = "Formatted document for easy reading",
                        isSelected = selectedFormat == ExportFormat.MARKDOWN,
                        onClick = { selectedFormat = ExportFormat.MARKDOWN }
                    )
                    ExportFormatOption(
                        icon = Icons.Default.TextSnippet,
                        title = "Plain Text",
                        subtitle = "Simple text file",
                        isSelected = selectedFormat == ExportFormat.PLAIN_TEXT,
                        onClick = { selectedFormat = ExportFormat.PLAIN_TEXT }
                    )
                    ExportFormatOption(
                        icon = Icons.Default.Person,
                        title = "Full Profile",
                        subtitle = "Complete profile with all settings",
                        isSelected = selectedFormat == ExportFormat.FULL_PROFILE,
                        onClick = { selectedFormat = ExportFormat.FULL_PROFILE }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                PersonAllyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getFormatFileName(selectedFormat),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = {
                                    exportPreview?.let {
                                        clipboardManager.setText(AnnotatedString(it))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            if (exportPreview != null) {
                                Text(
                                    text = exportPreview!!,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 15,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Export Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientButton(
                        text = "Share",
                        onClick = {
                            scope.launch {
                                isExporting = true
                                try {
                                    val file = app.userProfileRepository.exportToFile(selectedFormat)
                                    if (file != null) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = getMimeType(selectedFormat)
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Context"))
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                isExporting = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isLoading = isExporting,
                        icon = Icons.Default.Share
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                exportPreview?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copy",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContextInfoCard(universalContext = universalContext)
            }
        }
    }
}

@Composable
private fun ExportFormatOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                colors.gradientStart.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 0.dp else 1.dp
        )
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
                        color = if (isSelected) {
                            colors.gradientStart.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) colors.gradientStart else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) colors.gradientStart else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = colors.gradientStart,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextInfoCard(
    universalContext: UniversalContext?
) {
    val colors = PersonAllyTheme.extendedColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "About Your Context",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (universalContext != null) {
                InfoRow(
                    label = "Version",
                    value = "v${universalContext.version}"
                )
                InfoRow(
                    label = "Last Generated",
                    value = formatDate(universalContext.lastGeneratedAt)
                )
                if (universalContext.isUserEdited) {
                    InfoRow(
                        label = "Last Edited",
                        value = formatDate(universalContext.lastEditedAt ?: universalContext.updatedAt)
                    )
                }
                InfoRow(
                    label = "Core Identity Points",
                    value = universalContext.coreIdentityPoints.size.toString()
                )
                InfoRow(
                    label = "Current Goals",
                    value = universalContext.currentGoals.size.toString()
                )
            } else {
                Text(
                    text = "No context data available yet. Start chatting with Ally to build your context!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

private fun getFormatFileName(format: ExportFormat): String = when (format) {
    ExportFormat.JSON -> "context.json"
    ExportFormat.MARKDOWN -> "context.md"
    ExportFormat.PLAIN_TEXT -> "context.txt"
    ExportFormat.FULL_PROFILE -> "full_profile.json"
}

private fun getMimeType(format: ExportFormat): String = when (format) {
    ExportFormat.JSON, ExportFormat.FULL_PROFILE -> "application/json"
    ExportFormat.MARKDOWN -> "text/markdown"
    ExportFormat.PLAIN_TEXT -> "text/plain"
}

private fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}
