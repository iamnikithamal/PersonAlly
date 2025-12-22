package com.person.ally.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ModelCategory
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiModelsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val colors = PersonAllyTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }

    val providers by app.aiRepository.getAllProviders().collectAsState(initial = emptyList())
    val allModels by app.aiRepository.getAllModels().collectAsState(initial = emptyList())
    val currentModel by app.aiRepository.currentModel.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    var expandedProviderId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<ModelCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditAliasDialog by remember { mutableStateOf<AiModel?>(null) }

    // Filter models based on search and category
    val filteredModels = remember(allModels, searchQuery, selectedCategory) {
        allModels.filter { model ->
            val matchesSearch = searchQuery.isBlank() ||
                    model.displayName.contains(searchQuery, ignoreCase = true) ||
                    model.alias?.contains(searchQuery, ignoreCase = true) == true ||
                    model.modelId.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || model.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    // Group models by provider
    val modelsByProvider = remember(filteredModels) {
        filteredModels.groupBy { it.providerId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Models") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                providers.forEach { provider ->
                                    if (provider.supportsDynamicModels) {
                                        app.aiRepository.refreshModelsFromProvider(provider.id)
                                    }
                                }
                                isRefreshing = false
                                snackbarHostState.showSnackbar("Models refreshed")
                            }
                        },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh models"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Search and Filters
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search models...") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryFilterChip(
                            label = "All",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                        CategoryFilterChip(
                            label = "Chat",
                            selected = selectedCategory == ModelCategory.CHAT,
                            onClick = { selectedCategory = ModelCategory.CHAT }
                        )
                        CategoryFilterChip(
                            label = "Reasoning",
                            selected = selectedCategory == ModelCategory.REASONING,
                            onClick = { selectedCategory = ModelCategory.REASONING }
                        )
                        CategoryFilterChip(
                            label = "Vision",
                            selected = selectedCategory == ModelCategory.VISION,
                            onClick = { selectedCategory = ModelCategory.VISION }
                        )
                    }
                }
            }

            // Current Model Card
            currentModel?.let { model ->
                item {
                    CurrentModelCard(
                        model = model,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Providers and their models
            providers.forEach { provider ->
                val providerModels = modelsByProvider[provider.id] ?: emptyList()
                val isExpanded = expandedProviderId == provider.id || expandedProviderId == null

                item {
                    ProviderHeader(
                        provider = provider,
                        modelCount = providerModels.size,
                        enabledCount = providerModels.count { it.isEnabled },
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedProviderId = if (isExpanded) {
                                if (expandedProviderId == null) provider.id else null
                            } else {
                                provider.id
                            }
                        },
                        onToggleEnabled = { enabled ->
                            scope.launch {
                                app.aiRepository.setProviderEnabled(provider.id, enabled)
                            }
                        },
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                app.aiRepository.refreshModelsFromProvider(provider.id)
                                isRefreshing = false
                                snackbarHostState.showSnackbar("${provider.name} models refreshed")
                            }
                        }
                    )
                }

                if (isExpanded) {
                    items(
                        items = providerModels,
                        key = { it.id }
                    ) { model ->
                        ModelItem(
                            model = model,
                            isCurrentModel = currentModel?.id == model.id,
                            onSetDefault = {
                                scope.launch {
                                    app.aiRepository.setDefaultModel(model.id)
                                    snackbarHostState.showSnackbar("${model.displayName} set as default")
                                }
                            },
                            onToggleEnabled = { enabled ->
                                scope.launch {
                                    app.aiRepository.setModelEnabled(model.id, enabled)
                                }
                            },
                            onEditAlias = {
                                showEditAliasDialog = model
                            }
                        )
                    }
                }
            }

            // Empty state
            if (filteredModels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No models found" else "No AI models available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Alias Dialog
    showEditAliasDialog?.let { model ->
        var aliasText by remember { mutableStateOf(model.alias ?: "") }

        AlertDialog(
            onDismissRequest = { showEditAliasDialog = null },
            title = { Text("Edit Model Alias") },
            text = {
                Column {
                    Text(
                        text = "Model: ${model.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = aliasText,
                        onValueChange = { aliasText = it },
                        label = { Text("Alias") },
                        placeholder = { Text("Enter a custom name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.aiRepository.updateModelAlias(
                                model.id,
                                aliasText.takeIf { it.isNotBlank() }
                            )
                            showEditAliasDialog = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAliasDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun CurrentModelCard(
    model: AiModel,
    modifier: Modifier = Modifier
) {
    val colors = PersonAllyTheme.extendedColors

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.gradientStart,
                                colors.gradientMiddle
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Current Model",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = model.alias ?: model.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = model.modelId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ModelCapabilityBadges(model = model, compact = true)
        }
    }
}

@Composable
private fun ProviderHeader(
    provider: AiProvider,
    modelCount: Int,
    enabledCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$enabledCount of $modelCount models enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (provider.supportsDynamicModels) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Switch(
                checked = provider.isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary
                )
            )

            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: AiModel,
    isCurrentModel: Boolean,
    onSetDefault: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onEditAlias: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrentModel) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = model.isEnabled) { onSetDefault() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.alias ?: model.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrentModel) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (model.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Default",
                            modifier = Modifier.size(16.dp),
                            tint = colors.gradientMiddle
                        )
                    }
                }
                Text(
                    text = model.modelId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                model.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                ModelCapabilityBadges(model = model, compact = false)
            }

            IconButton(onClick = onEditAlias) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit alias",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = model.isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ModelCapabilityBadges(
    model: AiModel,
    compact: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (model.isThinkingModel) {
            CapabilityBadge(
                icon = Icons.Default.Psychology,
                label = if (compact) null else "Reasoning",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        if (model.supportsToolCalling) {
            CapabilityBadge(
                icon = Icons.Default.Code,
                label = if (compact) null else "Tools",
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (model.supportsVision) {
            CapabilityBadge(
                icon = Icons.Default.Visibility,
                label = if (compact) null else "Vision",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun CapabilityBadge(
    icon: ImageVector,
    label: String?,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}
