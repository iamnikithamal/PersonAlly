package com.person.ally.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.person.ally.PersonAllyApp
import com.person.ally.ai.agent.AllyAgent
import com.person.ally.ai.model.ErrorCategory
import com.person.ally.data.local.datastore.AIPersonality
import com.person.ally.data.local.datastore.AIResponseLength
import com.person.ally.data.model.ChatMessage
import com.person.ally.data.model.MessageRole
import com.person.ally.ui.components.MarkdownText
import com.person.ally.ui.components.ToolExecutionStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    conversationId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToMemory: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val userProfile by app.userProfileRepository.userProfile.collectAsState(initial = null)
    val universalContext by app.userProfileRepository.getUniversalContext().collectAsState(initial = null)
    val currentConversation by app.chatRepository.currentConversation.collectAsState(initial = null)
    val messages by app.chatRepository.currentMessages.collectAsState(initial = emptyList())
    val aiPersonality by app.settingsDataStore.aiPersonality.collectAsState(initial = AIPersonality.WARM)
    val aiResponseLength by app.settingsDataStore.aiResponseLength.collectAsState(initial = AIResponseLength.BALANCED)
    val currentModel by app.aiRepository.currentModel.collectAsState()
    val isGenerating by app.aiRepository.isGenerating.collectAsState()
    val agentState by app.allyAgent.agentState.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var streamingContent by remember { mutableStateOf("") }
    var streamingReasoning by remember { mutableStateOf("") }
    var currentToolExecution by remember { mutableStateOf<Pair<String, ToolExecutionStatus>?>(null) }
    var errorState by remember { mutableStateOf<AllyAgent.AgentState.Error?>(null) }
    var pendingMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Initialize conversation on first load
    LaunchedEffect(conversationId) {
        if (conversationId != null && conversationId > 0) {
            app.chatRepository.setCurrentConversation(conversationId)
        } else {
            app.chatRepository.startNewConversation()
        }
    }

    // Scroll to bottom when messages change or streaming content updates
    LaunchedEffect(messages.size, streamingContent) {
        if (messages.isNotEmpty() || streamingContent.isNotEmpty()) {
            val targetIndex = if (streamingContent.isNotEmpty()) messages.size else messages.size - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex.coerceAtLeast(0))
            }
        }
    }

    // Handle agent state changes
    LaunchedEffect(agentState) {
        when (val state = agentState) {
            is AllyAgent.AgentState.Thinking -> {
                streamingReasoning = state.reasoning
                currentToolExecution = null
            }
            is AllyAgent.AgentState.Generating -> {
                streamingContent = state.partialContent
                currentToolExecution = null
            }
            is AllyAgent.AgentState.ExecutingTool -> {
                currentToolExecution = state.toolName to ToolExecutionStatus.EXECUTING
            }
            is AllyAgent.AgentState.Error -> {
                errorState = state
                streamingContent = ""
                streamingReasoning = ""
                currentToolExecution = null
            }
            is AllyAgent.AgentState.Complete -> {
                streamingContent = ""
                streamingReasoning = ""
                currentToolExecution = null
                errorState = null
            }
            AllyAgent.AgentState.Idle -> {
                // Reset state when idle
            }
        }
    }

    fun sendMessage(text: String? = null) {
        val messageText = text ?: inputText.trim()
        if (messageText.isBlank() || isGenerating) return

        inputText = ""
        keyboardController?.hide()
        errorState = null
        streamingContent = ""
        streamingReasoning = ""
        pendingMessage = messageText

        scope.launch {
            // Send user message
            app.chatRepository.sendMessage(messageText)
            pendingMessage = null

            // Get the current model
            val model = currentModel ?: run {
                errorState = AllyAgent.AgentState.Error(
                    message = "No AI model selected. Please configure a model in Settings > AI Models.",
                    isRetryable = false,
                    category = ErrorCategory.MODEL_NOT_FOUND,
                    suggestedAction = "Select a model in Settings"
                )
                return@launch
            }

            // Build system prompt
            val userName = userProfile?.preferredName ?: userProfile?.name ?: "Friend"
            val contextSummary = universalContext?.summary
            val systemPrompt = app.allyAgent.generateSystemPrompt(
                userName = userName,
                userContext = contextSummary,
                personality = aiPersonality.name.lowercase(),
                responseLength = aiResponseLength.name.lowercase()
            )

            // Convert existing messages to API format
            val conversationHistory = messages.map { msg ->
                com.person.ally.ai.model.ChatMessage(
                    role = when (msg.role) {
                        MessageRole.USER -> com.person.ally.ai.model.MessageRole.USER
                        MessageRole.ASSISTANT -> com.person.ally.ai.model.MessageRole.ASSISTANT
                        MessageRole.SYSTEM -> com.person.ally.ai.model.MessageRole.SYSTEM
                    },
                    content = msg.content
                )
            }

            // Process with AI agent
            val startTime = System.currentTimeMillis()
            try {
                val response = app.allyAgent.processMessage(
                    userMessage = messageText,
                    model = model,
                    systemPrompt = systemPrompt,
                    existingHistory = conversationHistory.takeLast(20)
                ) { chunk ->
                    // Chunks are handled via agentState
                }

                // Save assistant response
                val responseTimeMs = System.currentTimeMillis() - startTime
                val memoryIds = response.toolResults
                    .filter { it.toolName == "create_memory" && it.success }
                    .mapNotNull { result ->
                        Regex("ID: (\\d+)").find(result.result)?.groupValues?.get(1)?.toLongOrNull()
                    }

                val convId = currentConversation?.id ?: return@launch
                app.chatRepository.addAssistantMessage(
                    conversationId = convId,
                    content = response.content,
                    tokensUsed = response.tokensUsed,
                    responseTimeMs = responseTimeMs,
                    memoryExtracted = memoryIds.isNotEmpty(),
                    extractedMemoryIds = memoryIds,
                    contextUsed = conversationHistory.isNotEmpty()
                )
            } catch (e: Exception) {
                errorState = AllyAgent.AgentState.Error.fromMessage(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        ConversationTopBar(
            conversationTitle = currentConversation?.title ?: "New Chat",
            showMenu = showMenu,
            currentModelName = currentModel?.getDisplayNameOrAlias(),
            onBackClick = onNavigateBack,
            onMenuClick = { showMenu = !showMenu },
            onDismissMenu = { showMenu = false },
            onClearChat = {
                scope.launch {
                    currentConversation?.let { conv ->
                        app.chatRepository.deleteMessagesForConversation(conv.id)
                    }
                }
                showMenu = false
            },
            onStopGeneration = {
                app.allyAgent.cancel()
                streamingContent = ""
                streamingReasoning = ""
            },
            isGenerating = isGenerating
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty() && !isGenerating && pendingMessage == null) {
                item {
                    WelcomeMessage(
                        userName = userProfile?.preferredName ?: userProfile?.name ?: "Friend",
                        onSuggestionClick = { suggestion -> sendMessage(suggestion) }
                    )
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatMessageBubble(
                    message = message,
                    onMemoryClick = if (message.extractedMemoryIds.isNotEmpty()) {
                        { onNavigateToMemory(message.extractedMemoryIds.first()) }
                    } else null
                )
            }

            // Show streaming content
            if (isGenerating && (streamingContent.isNotEmpty() || streamingReasoning.isNotEmpty() || currentToolExecution != null)) {
                item {
                    StreamingMessageBubble(
                        content = streamingContent,
                        reasoning = streamingReasoning,
                        toolExecution = currentToolExecution
                    )
                }
            }

            // Show typing indicator when generating but no content yet
            if (isGenerating && streamingContent.isEmpty() && streamingReasoning.isEmpty() && currentToolExecution == null) {
                item {
                    TypingIndicator()
                }
            }

            // Show error message
            if (errorState != null) {
                item {
                    ErrorMessage(
                        error = errorState!!,
                        onRetry = {
                            val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }?.content
                            if (lastUserMessage != null) {
                                errorState = null
                                sendMessage(lastUserMessage)
                            }
                        },
                        onDismiss = { errorState = null }
                    )
                }
            }
        }

        ModernChatInput(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = { sendMessage() },
            onStop = {
                app.allyAgent.cancel()
                streamingContent = ""
                streamingReasoning = ""
            },
            isProcessing = isGenerating,
            modelName = currentModel?.getDisplayNameOrAlias() ?: "No model",
            isThinkingModel = currentModel?.capabilities?.reasoning == true,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationTopBar(
    conversationTitle: String,
    showMenu: Boolean,
    currentModelName: String?,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onClearChat: () -> Unit,
    onStopGeneration: () -> Unit,
    isGenerating: Boolean
) {
    TopAppBar(
        title = {
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
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ally",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentModelName ?: "No model selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (isGenerating) {
                IconButton(onClick = onStopGeneration) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop generation",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Clear chat") },
                        onClick = onClearChat
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun WelcomeMessage(
    userName: String,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hi $userName!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "I'm Ally, your personal companion.\nHow can I help you today?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        SuggestionChips(onSuggestionClick = onSuggestionClick)
    }
}

@Composable
private fun SuggestionChips(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "How am I doing?",
        "Help me reflect",
        "What patterns do you see?",
        "Let's set a goal"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                row.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.clickable { onSuggestionClick(suggestion) },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onMemoryClick: (() -> Unit)?
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        MarkdownText(
                            markdown = message.content,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (message.extractedMemoryIds.isNotEmpty() && onMemoryClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onMemoryClick),
                            color = if (isUser) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isUser) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Memory saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUser) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatTime(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(
                start = if (!isUser) 40.dp else 0.dp,
                end = if (isUser) 0.dp else 0.dp
            )
        )
    }
}

@Composable
private fun StreamingMessageBubble(
    content: String,
    reasoning: String,
    toolExecution: Pair<String, ToolExecutionStatus>?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streaming")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 16.dp
                ),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Show reasoning if available
                    if (reasoning.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .alpha(pulseAlpha)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thinking...",
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Text(
                            text = reasoning.take(200) + if (reasoning.length > 200) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Show tool execution
                    if (toolExecution != null) {
                        ToolExecutionChip(
                            toolName = toolExecution.first,
                            status = toolExecution.second,
                            modifier = Modifier.padding(bottom = if (content.isNotEmpty()) 8.dp else 0.dp)
                        )
                    }

                    // Show content
                    if (content.isNotEmpty()) {
                        MarkdownText(
                            markdown = content,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Show cursor when no content yet
                    if (content.isEmpty() && reasoning.isEmpty() && toolExecution == null) {
                        Text(
                            text = "▋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.alpha(pulseAlpha)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolExecutionChip(
    toolName: String,
    status: ToolExecutionStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tool")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "toolScale"
    )

    Surface(
        modifier = modifier.scale(if (status == ToolExecutionStatus.EXECUTING) scale else 1f),
        shape = RoundedCornerShape(8.dp),
        color = when (status) {
            ToolExecutionStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
            ToolExecutionStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (status) {
                    ToolExecutionStatus.SUCCESS -> "✅"
                    ToolExecutionStatus.ERROR -> "❌"
                    else -> "⚙️"
                },
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = toolName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(alpha)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    error: AllyAgent.AgentState.Error,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    // Use the structured error category for proper handling
    val errorTitle = when (error.category) {
        ErrorCategory.AUTHENTICATION -> "Authentication Error"
        ErrorCategory.RATE_LIMIT -> "Rate Limited"
        ErrorCategory.NETWORK -> "Connection Error"
        ErrorCategory.SERVER -> "Server Error"
        ErrorCategory.INVALID_REQUEST -> "Invalid Request"
        ErrorCategory.CONTEXT_LENGTH -> "Message Too Long"
        ErrorCategory.CONTENT_FILTER -> "Content Filtered"
        ErrorCategory.MODEL_NOT_FOUND -> "Model Not Found"
        ErrorCategory.QUOTA_EXCEEDED -> "Quota Exceeded"
        ErrorCategory.UNKNOWN -> "Error"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = errorTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                    )

                    // Show suggested action if available
                    error.suggestedAction?.let { action ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = action,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (error.isRetryable) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onRetry),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                } else {
                    // For non-retryable errors, show a dismiss-style button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isProcessing: Boolean,
    modelName: String,
    isThinkingModel: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(200)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text input area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Model chip
                ModelChip(
                    modelName = modelName,
                    isThinkingModel = isThinkingModel,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Text input
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 120.dp),
                    enabled = !isProcessing,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (value.isNotBlank() && !isProcessing) {
                                onSend()
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = if (isProcessing) "Ally is responding..." else "Message Ally...",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 15.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Send/Stop button
            AnimatedVisibility(
                visible = value.isNotBlank() || isProcessing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                if (isProcessing) {
                    // Stop button
                    IconButton(
                        onClick = onStop,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop generation",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Send button
                    IconButton(
                        onClick = {
                            if (value.isNotBlank()) {
                                onSend()
                                focusManager.clearFocus()
                            }
                        },
                        enabled = value.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelChip(
    modelName: String,
    isThinkingModel: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isThinkingModel) Icons.Outlined.Psychology else Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isThinkingModel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = modelName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
