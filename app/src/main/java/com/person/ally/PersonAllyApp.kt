package com.person.ally

import android.app.Application
import android.content.Intent
import com.person.ally.ai.agent.AllyAgent
import com.person.ally.ai.agent.ToolRegistry
import com.person.ally.data.local.database.PersonAllyDatabase
import com.person.ally.data.local.datastore.SettingsDataStore
import com.person.ally.data.repository.AiRepository
import com.person.ally.data.repository.AssessmentRepository
import com.person.ally.data.repository.ChatRepository
import com.person.ally.data.repository.InsightRepository
import com.person.ally.data.repository.MemoryRepository
import com.person.ally.data.repository.UserProfileRepository
import com.person.ally.ui.debug.DebugActivity
import com.person.ally.util.CrashHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PersonAllyApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: PersonAllyDatabase by lazy {
        PersonAllyDatabase.getInstance(this)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(this)
    }

    val memoryRepository: MemoryRepository by lazy {
        MemoryRepository(database.memoryDao())
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.chatDao())
    }

    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao(), this)
    }

    val assessmentRepository: AssessmentRepository by lazy {
        AssessmentRepository(database.assessmentDao(), userProfileRepository)
    }

    val insightRepository: InsightRepository by lazy {
        InsightRepository(database.insightDao())
    }

    val aiRepository: AiRepository by lazy {
        AiRepository(database.aiModelDao())
    }

    val toolRegistry: ToolRegistry by lazy {
        ToolRegistry(
            memoryRepository = memoryRepository,
            userProfileRepository = userProfileRepository,
            insightRepository = insightRepository,
            assessmentRepository = assessmentRepository
        )
    }

    val allyAgent: AllyAgent by lazy {
        AllyAgent(
            aiRepository = aiRepository,
            toolRegistry = toolRegistry
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupCrashHandler()
        initializeAi()
    }

    private fun initializeAi() {
        applicationScope.launch(Dispatchers.IO) {
            aiRepository.ensureDefaultsExist()
            aiRepository.initialize()
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                CrashHandler.saveCrashLog(this, throwable)
                val intent = Intent(this, DebugActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    putExtra(DebugActivity.EXTRA_CRASH_LOG, CrashHandler.getStackTraceString(throwable))
                }
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            } catch (e: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: PersonAllyApp? = null

        fun getInstance(): PersonAllyApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
