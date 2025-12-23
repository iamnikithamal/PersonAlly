package com.person.ally.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiModelTypeConverters
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.provider.DeepInfraProvider
import com.person.ally.data.local.dao.AiModelDao
import com.person.ally.data.local.dao.AssessmentDao
import com.person.ally.data.local.dao.ChatDao
import com.person.ally.data.local.dao.InsightDao
import com.person.ally.data.local.dao.MemoryDao
import com.person.ally.data.local.dao.UserProfileDao
import com.person.ally.data.local.dao.WellnessDao
import com.person.ally.data.model.Assessment
import com.person.ally.data.model.AssessmentTypeConverters
import com.person.ally.data.model.ChatMessage
import com.person.ally.data.model.ChatTypeConverters
import com.person.ally.data.model.Conversation
import com.person.ally.data.model.DailyBriefing
import com.person.ally.data.model.Goal
import com.person.ally.data.model.Habit
import com.person.ally.data.model.HabitCompletion
import com.person.ally.data.model.DailyCheckin
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightTypeConverters
import com.person.ally.data.model.JournalEntry
import com.person.ally.data.model.Memory
import com.person.ally.data.model.MemoryTypeConverters
import com.person.ally.data.model.MoodEntry
import com.person.ally.data.model.ScheduleItem
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.model.UserProfile
import com.person.ally.data.model.UserProfileTypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Memory::class,
        ChatMessage::class,
        Conversation::class,
        Assessment::class,
        UserProfile::class,
        UniversalContext::class,
        Insight::class,
        DailyBriefing::class,
        Goal::class,
        Habit::class,
        HabitCompletion::class,
        AiProvider::class,
        AiModel::class,
        MoodEntry::class,
        JournalEntry::class,
        ScheduleItem::class,
        DailyCheckin::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(
    MemoryTypeConverters::class,
    ChatTypeConverters::class,
    AssessmentTypeConverters::class,
    UserProfileTypeConverters::class,
    InsightTypeConverters::class,
    AiModelTypeConverters::class
)
abstract class PersonAllyDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun insightDao(): InsightDao
    abstract fun aiModelDao(): AiModelDao
    abstract fun wellnessDao(): WellnessDao

    companion object {
        private const val DATABASE_NAME = "personally_database"

        @Volatile
        private var INSTANCE: PersonAllyDatabase? = null

        fun getInstance(context: Context): PersonAllyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PersonAllyDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: PersonAllyDatabase) {
            database.userProfileDao().insertUserProfile(UserProfile())
            database.userProfileDao().insertUniversalContext(UniversalContext())
            database.assessmentDao().insertAssessments(getDefaultAssessments())
            populateDefaultAiProviders(database)
        }

        private suspend fun populateDefaultAiProviders(database: PersonAllyDatabase) {
            // Add default DeepInfra provider
            val deepInfraProvider = DeepInfraProvider.createDefaultProvider()
            database.aiModelDao().insertProvider(deepInfraProvider)

            // Add default models for DeepInfra
            val deepInfra = DeepInfraProvider(deepInfraProvider)
            val defaultModels = deepInfra.getDefaultModels()
            database.aiModelDao().insertModels(defaultModels)
        }

        private fun getDefaultAssessments(): List<Assessment> {
            return DefaultAssessments.getAll()
        }

        /**
         * Extension function to get default models from DeepInfraProvider
         * This is a workaround since getDefaultModels() is protected
         */
        private fun DeepInfraProvider.getDefaultModels(): List<AiModel> {
            // We'll create the models directly here instead
            return createDefaultDeepInfraModels()
        }

        private fun createDefaultDeepInfraModels(): List<AiModel> {
            return DefaultAiModels.getDeepInfraModels()
        }
    }
}
