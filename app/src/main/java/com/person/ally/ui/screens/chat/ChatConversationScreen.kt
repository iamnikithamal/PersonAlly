package com.person.ally.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.ai.agent.AllyAgent
import com.person.ally.data.local.datastore.AIPersonality
import com.person.ally.data.local.datastore.AIResponseLength
import com.person.ally.data.model.ChatMessage
import com.person.ally.data.model.MessageRole
import com.person.ally.ui.components.MarkdownText
import com.person.ally.ui.components.ToolExecutionIndicator
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                errorMessage = state.message
                streamingContent = ""
                streamingReasoning = ""
                currentToolExecution = null
            }
            is AllyAgent.AgentState.Complete -> {
                streamingContent = ""
                streamingReasoning = ""
                currentToolExecution = null
                errorMessage = null
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
        errorMessage = null
        streamingContent = ""
        streamingReasoning = ""
        pendingMessage = messageText

        scope.launch {
            // Send user message
            app.chatRepository.sendMessage(messageText)
            pendingMessage = null

            // Get the current model
            val model = currentModel ?: run {
                errorMessage = "No AI model selected. Please configure a model in Settings > AI Models."
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
                errorMessage = e.message ?: "An unexpected error occurred"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    WelcomeMessage(userName = userProfile?.name ?: "Friend")
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
            if (errorMessage != null) {
                item {
                    ErrorMessage(
                        message = errorMessage!!,
                        onRetry = {
                            val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }?.content
                            if (lastUserMessage != null) {
                                errorMessage = null
                                sendMessage(lastUserMessage)
                            }
                        },
                        onDismiss = { errorMessage = null }
                    )
                }
            }
        }

        ChatInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = { sendMessage() },
            isGenerating = isGenerating
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
private fun WelcomeMessage(userName: String) {
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
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SuggestionChips()
    }
}

@Composable
private fun SuggestionChips() {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.clickable { },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
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
                    MaterialTheme.colorScheme.surfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Show reasoning if available
                    if (reasoning.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thinking...",
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                        ToolExecutionIndicator(
                            toolName = toolExecution.first,
                            status = toolExecution.second,
                            modifier = Modifier.padding(bottom = if (content.isNotEmpty()) 8.dp else 0.dp)
                        )
                    }

                    // Show content
                    if (content.isNotEmpty()) {
                        MarkdownText(
                            markdown = content,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Show cursor
                    if (content.isEmpty() && reasoning.isEmpty() && toolExecution == null) {
                        Text(
                            text = "▋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ally is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }

            IconButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (isGenerating) "Ally is responding..." else "Message Ally...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                enabled = !isGenerating
            )

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedVisibility(
                visible = value.isNotBlank() && !isGenerating,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
