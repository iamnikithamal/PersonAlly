package com.person.ally.ai.agent

import com.google.gson.Gson
import com.person.ally.ai.model.FunctionDefinition
import com.person.ally.ai.model.FunctionParameters
import com.person.ally.ai.model.ParameterProperty
import com.person.ally.ai.model.ToolDefinition
import com.person.ally.data.model.MemoryCategory
import com.person.ally.data.model.MemoryImportance
import com.person.ally.data.repository.AssessmentRepository
import com.person.ally.data.repository.InsightRepository
import com.person.ally.data.repository.MemoryRepository
import com.person.ally.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.first

/**
 * Registry for all tools available to the AI agent.
 * Tools allow the AI to interact with app data and perform actions.
 */
class ToolRegistry(
    private val memoryRepository: MemoryRepository,
    private val userProfileRepository: UserProfileRepository,
    private val insightRepository: InsightRepository,
    private val assessmentRepository: AssessmentRepository
) {
    private val gson = Gson()
    private val tools = mutableMapOf<String, Tool>()

    init {
        registerDefaultTools()
    }

    /**
     * Register all default tools
     */
    private fun registerDefaultTools() {
        // Memory Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "search_memories",
                        description = "Search through the user's memories to find relevant information. Use this when you need to recall something about the user or find context for the conversation.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "query" to ParameterProperty(
                                    type = "string",
                                    description = "The search query to find relevant memories"
                                ),
                                "category" to ParameterProperty(
                                    type = "string",
                                    description = "Optional category filter",
                                    enum = listOf("CORE_IDENTITY", "EVOLVING_UNDERSTANDING", "CONTEXTUAL", "EPISODIC")
                                ),
                                "limit" to ParameterProperty(
                                    type = "integer",
                                    description = "Maximum number of memories to return (default 5)"
                                )
                            ),
                            required = listOf("query")
                        )
                    )
                ),
                executor = { args -> executeSearchMemories(args) }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_recent_memories",
                        description = "Get the user's most recent memories. Useful for understanding recent context.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "limit" to ParameterProperty(
                                    type = "integer",
                                    description = "Number of recent memories to retrieve (default 5)"
                                ),
                                "category" to ParameterProperty(
                                    type = "string",
                                    description = "Optional category filter",
                                    enum = listOf("CORE_IDENTITY", "EVOLVING_UNDERSTANDING", "CONTEXTUAL", "EPISODIC")
                                )
                            ),
                            required = emptyList()
                        )
                    )
                ),
                executor = { args -> executeGetRecentMemories(args) }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "create_memory",
                        description = "Create a new memory to store important information about the user. Use this when the user shares something significant that should be remembered.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "content" to ParameterProperty(
                                    type = "string",
                                    description = "The content of the memory"
                                ),
                                "category" to ParameterProperty(
                                    type = "string",
                                    description = "The category of the memory",
                                    enum = listOf("CORE_IDENTITY", "EVOLVING_UNDERSTANDING", "CONTEXTUAL", "EPISODIC")
                                ),
                                "importance" to ParameterProperty(
                                    type = "string",
                                    description = "The importance level of the memory",
                                    enum = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
                                ),
                                "tags" to ParameterProperty(
                                    type = "array",
                                    description = "Tags to help categorize the memory",
                                    items = ParameterProperty(type = "string")
                                )
                            ),
                            required = listOf("content", "category")
                        )
                    )
                ),
                executor = { args -> executeCreateMemory(args) }
            )
        )

        // User Profile Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_user_profile",
                        description = "Get the user's profile information including name, preferences, and stats.",
                        parameters = FunctionParameters(
                            properties = emptyMap(),
                            required = emptyList()
                        )
                    )
                ),
                executor = { executeGetUserProfile() }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_user_context",
                        description = "Get the universal context summary of who the user is, including personality, goals, challenges, and preferences.",
                        parameters = FunctionParameters(
                            properties = emptyMap(),
                            required = emptyList()
                        )
                    )
                ),
                executor = { executeGetUserContext() }
            )
        )

        // Goals Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_active_goals",
                        description = "Get the user's active goals and their progress.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "limit" to ParameterProperty(
                                    type = "integer",
                                    description = "Maximum number of goals to return (default 5)"
                                )
                            ),
                            required = emptyList()
                        )
                    )
                ),
                executor = { args -> executeGetActiveGoals(args) }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "create_goal",
                        description = "Create a new goal for the user. Use this when the user wants to set a new goal.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "title" to ParameterProperty(
                                    type = "string",
                                    description = "The title of the goal"
                                ),
                                "description" to ParameterProperty(
                                    type = "string",
                                    description = "A detailed description of the goal"
                                ),
                                "category" to ParameterProperty(
                                    type = "string",
                                    description = "The life domain category for this goal",
                                    enum = listOf("CAREER", "RELATIONSHIPS", "HEALTH", "PERSONAL_GROWTH", "FINANCE", "CREATIVITY", "SPIRITUALITY", "OTHER")
                                ),
                                "targetDate" to ParameterProperty(
                                    type = "string",
                                    description = "Target completion date in ISO format (optional)"
                                )
                            ),
                            required = listOf("title", "category")
                        )
                    )
                ),
                executor = { args -> executeCreateGoal(args) }
            )
        )

        // Insights Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_recent_insights",
                        description = "Get recent insights and patterns discovered about the user.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "limit" to ParameterProperty(
                                    type = "integer",
                                    description = "Maximum number of insights to return (default 5)"
                                )
                            ),
                            required = emptyList()
                        )
                    )
                ),
                executor = { args -> executeGetRecentInsights(args) }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "create_insight",
                        description = "Create a new insight or pattern discovered about the user.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "title" to ParameterProperty(
                                    type = "string",
                                    description = "A brief title for the insight"
                                ),
                                "content" to ParameterProperty(
                                    type = "string",
                                    description = "The detailed insight content"
                                ),
                                "category" to ParameterProperty(
                                    type = "string",
                                    description = "The category of the insight",
                                    enum = listOf("PATTERN", "DISCOVERY", "GROWTH", "REFLECTION", "SUGGESTION")
                                )
                            ),
                            required = listOf("title", "content", "category")
                        )
                    )
                ),
                executor = { args -> executeCreateInsight(args) }
            )
        )

        // Assessment Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_available_assessments",
                        description = "Get available personality and psychology assessments that the user can take.",
                        parameters = FunctionParameters(
                            properties = emptyMap(),
                            required = emptyList()
                        )
                    )
                ),
                executor = { executeGetAvailableAssessments() }
            )
        )

        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_assessment_results",
                        description = "Get results from assessments the user has completed.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "assessmentId" to ParameterProperty(
                                    type = "string",
                                    description = "Optional specific assessment ID to get results for"
                                )
                            ),
                            required = emptyList()
                        )
                    )
                ),
                executor = { args -> executeGetAssessmentResults(args) }
            )
        )

        // Utility Tools
        registerTool(
            Tool(
                definition = ToolDefinition(
                    type = "function",
                    function = FunctionDefinition(
                        name = "get_current_date_time",
                        description = "Get the current date and time. Useful for time-aware responses.",
                        parameters = FunctionParameters(
                            properties = emptyMap(),
                            required = emptyList()
                        )
                    )
                ),
                executor = { executeGetCurrentDateTime() }
            )
        )
    }

    /**
     * Register a new tool
     */
    fun registerTool(tool: Tool) {
        tools[tool.definition.function.name] = tool
    }

    /**
     * Get all tool definitions for the API
     */
    fun getToolDefinitions(): List<ToolDefinition> {
        return tools.values.map { it.definition }
    }

    /**
     * Execute a tool by name with arguments
     */
    suspend fun executeTool(name: String, arguments: Map<String, Any?>): String {
        val tool = tools[name] ?: return "Error: Tool '$name' not found"
        return try {
            tool.executor(arguments)
        } catch (e: Exception) {
            "Error executing tool: ${e.message}"
        }
    }

    // Tool Executors

    private suspend fun executeSearchMemories(args: Map<String, Any?>): String {
        val query = args["query"] as? String ?: return "Error: query is required"
        val category = (args["category"] as? String)?.let { MemoryCategory.valueOf(it) }
        val limit = (args["limit"] as? Number)?.toInt() ?: 5

        val memories = memoryRepository.searchMemories(query).first().take(limit)

        if (memories.isEmpty()) {
            return "No memories found matching '$query'"
        }

        return buildString {
            appendLine("Found ${memories.size} memories:")
            memories.forEach { memory ->
                appendLine("- [${memory.category}] ${memory.content}")
                if (memory.tags.isNotEmpty()) {
                    appendLine("  Tags: ${memory.tags.joinToString(", ")}")
                }
            }
        }
    }

    private suspend fun executeGetRecentMemories(args: Map<String, Any?>): String {
        val limit = (args["limit"] as? Number)?.toInt() ?: 5
        val category = (args["category"] as? String)?.let { MemoryCategory.valueOf(it) }

        val memories = if (category != null) {
            memoryRepository.getMemoriesByCategory(category).first().take(limit)
        } else {
            memoryRepository.getAllMemories().first().take(limit)
        }

        if (memories.isEmpty()) {
            return "No recent memories found"
        }

        return buildString {
            appendLine("Recent memories:")
            memories.forEach { memory ->
                appendLine("- [${memory.category}] ${memory.content}")
            }
        }
    }

    private suspend fun executeCreateMemory(args: Map<String, Any?>): String {
        val content = args["content"] as? String ?: return "Error: content is required"
        val categoryStr = args["category"] as? String ?: return "Error: category is required"
        val importanceStr = args["importance"] as? String ?: "MEDIUM"
        val tagsList = (args["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        val category = try {
            MemoryCategory.valueOf(categoryStr)
        } catch (e: Exception) {
            return "Error: Invalid category '$categoryStr'"
        }

        val importance = try {
            MemoryImportance.valueOf(importanceStr)
        } catch (e: Exception) {
            MemoryImportance.MEDIUM
        }

        val memory = com.person.ally.data.model.Memory(
            content = content,
            category = category,
            importance = importance,
            tags = tagsList,
            isUserCreated = false
        )

        val id = memoryRepository.insertMemory(memory)
        return "Memory created successfully with ID: $id"
    }

    private suspend fun executeGetUserProfile(): String {
        val profile = userProfileRepository.userProfile.first()
            ?: return "User profile not found"

        return buildString {
            appendLine("User Profile:")
            appendLine("- Name: ${profile.name}")
            profile.preferredName?.let { appendLine("- Preferred name: $it") }
            appendLine("- Conversation streak: ${profile.currentStreak} days")
            appendLine("- Total conversations: ${profile.totalConversations}")
            appendLine("- Total memories: ${profile.totalMemories}")
            appendLine("- Total insights: ${profile.totalInsights}")
            appendLine("- Assessments completed: ${profile.totalAssessmentsCompleted}")
        }
    }

    private suspend fun executeGetUserContext(): String {
        val context = userProfileRepository.universalContext.first()
            ?: return "User context not found"

        return buildString {
            appendLine("User Context Summary:")
            if (context.summary.isNotBlank()) {
                appendLine(context.summary)
            }
            if (context.personalitySnapshot.isNotBlank()) {
                appendLine("\nPersonality: ${context.personalitySnapshot}")
            }
            if (context.coreIdentityPoints.isNotEmpty()) {
                appendLine("\nCore Identity:")
                context.coreIdentityPoints.forEach { appendLine("- $it") }
            }
            if (context.currentGoals.isNotEmpty()) {
                appendLine("\nCurrent Goals:")
                context.currentGoals.forEach { appendLine("- $it") }
            }
            if (context.currentChallenges.isNotEmpty()) {
                appendLine("\nCurrent Challenges:")
                context.currentChallenges.forEach { appendLine("- $it") }
            }
        }
    }

    private suspend fun executeGetActiveGoals(args: Map<String, Any?>): String {
        val limit = (args["limit"] as? Number)?.toInt() ?: 5
        val goals = insightRepository.getActiveGoals().first().take(limit)

        if (goals.isEmpty()) {
            return "No active goals found"
        }

        return buildString {
            appendLine("Active Goals:")
            goals.forEach { goal ->
                appendLine("- ${goal.title} (${goal.progress}% complete)")
                goal.description?.let { appendLine("  Description: $it") }
            }
        }
    }

    private suspend fun executeCreateGoal(args: Map<String, Any?>): String {
        val title = args["title"] as? String ?: return "Error: title is required"
        val categoryStr = args["category"] as? String ?: return "Error: category is required"
        val description = args["description"] as? String

        val category = try {
            com.person.ally.data.model.LifeDomain.valueOf(categoryStr)
        } catch (e: Exception) {
            return "Error: Invalid category '$categoryStr'"
        }

        val goal = com.person.ally.data.model.Goal(
            title = title,
            description = description,
            domain = category
        )

        val id = insightRepository.insertGoal(goal)
        return "Goal created successfully: '$title'"
    }

    private suspend fun executeGetRecentInsights(args: Map<String, Any?>): String {
        val limit = (args["limit"] as? Number)?.toInt() ?: 5
        val insights = insightRepository.getRecentInsights(limit).first()

        if (insights.isEmpty()) {
            return "No recent insights found"
        }

        return buildString {
            appendLine("Recent Insights:")
            insights.forEach { insight ->
                appendLine("- [${insight.type}] ${insight.title}")
                appendLine("  ${insight.content}")
            }
        }
    }

    private suspend fun executeCreateInsight(args: Map<String, Any?>): String {
        val title = args["title"] as? String ?: return "Error: title is required"
        val content = args["content"] as? String ?: return "Error: content is required"
        val categoryStr = args["category"] as? String ?: return "Error: category is required"

        val category = try {
            com.person.ally.data.model.InsightType.valueOf(categoryStr)
        } catch (e: Exception) {
            return "Error: Invalid category '$categoryStr'"
        }

        val insight = com.person.ally.data.model.Insight(
            title = title,
            content = content,
            type = category
        )

        val id = insightRepository.insertInsight(insight)
        return "Insight created: '$title'"
    }

    private suspend fun executeGetAvailableAssessments(): String {
        val assessments = assessmentRepository.getAvailableAssessments().first()

        if (assessments.isEmpty()) {
            return "No assessments available"
        }

        return buildString {
            appendLine("Available Assessments:")
            assessments.forEach { assessment ->
                appendLine("- ${assessment.title}")
                appendLine("  ${assessment.description}")
                appendLine("  Duration: ~${assessment.estimatedMinutes} minutes")
            }
        }
    }

    private suspend fun executeGetAssessmentResults(args: Map<String, Any?>): String {
        val assessmentId = args["assessmentId"] as? String
        val completedAssessments = assessmentRepository.getCompletedAssessments().first()

        val filtered = if (assessmentId != null) {
            completedAssessments.filter { it.id == assessmentId }
        } else {
            completedAssessments
        }

        if (filtered.isEmpty()) {
            return "No completed assessments found"
        }

        return buildString {
            appendLine("Assessment Results:")
            filtered.forEach { assessment ->
                appendLine("- ${assessment.title}")
                assessment.results.forEach { result ->
                    appendLine("  ${result.dimension}: ${result.score}")
                    result.interpretation?.let { appendLine("  Interpretation: $it") }
                }
            }
        }
    }

    private fun executeGetCurrentDateTime(): String {
        val now = java.time.LocalDateTime.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")
        return "Current date and time: ${now.format(formatter)}"
    }
}

/**
 * Represents a tool that can be called by the AI
 */
data class Tool(
    val definition: ToolDefinition,
    val executor: suspend (Map<String, Any?>) -> String
)
