package com.person.ally.data.local.database

import com.person.ally.data.model.Assessment
import com.person.ally.data.model.AssessmentQuestion
import com.person.ally.data.model.AssessmentStatus
import com.person.ally.data.model.AssessmentType
import com.person.ally.data.model.QuestionType

object DefaultAssessments {
    fun getAll(): List<Assessment> = listOf(
        getPersonalityFoundation(),
        getCoreValues(),
        getEmotionalIntelligence(),
        getCommunicationStyle(),
        getLifeSatisfaction(),
        getGoalClarity(),
        getStressProfile(),
        getRelationshipPatterns()
    )

    private fun getPersonalityFoundation() = Assessment(
        id = "personality_foundation",
        title = "Personality Foundation",
        description = "Discover your core personality traits and how they influence your daily life, relationships, and decision-making.",
        type = AssessmentType.PERSONALITY,
        iconName = "Psychology",
        estimatedMinutes = 10,
        questionCount = 20,
        questions = listOf(
            AssessmentQuestion(
                id = "pf_1",
                text = "I feel energized after spending time with large groups of people.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Extraversion"
            ),
            AssessmentQuestion(
                id = "pf_2",
                text = "I prefer having a detailed plan rather than being spontaneous.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Structure"
            ),
            AssessmentQuestion(
                id = "pf_3",
                text = "I often think about abstract concepts and theoretical ideas.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Openness"
            ),
            AssessmentQuestion(
                id = "pf_4",
                text = "When making decisions, I prioritize logic over emotions.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Thinking"
            ),
            AssessmentQuestion(
                id = "pf_5",
                text = "I tend to notice small changes in my environment that others miss.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Sensing"
            ),
            AssessmentQuestion(
                id = "pf_6",
                text = "I prefer working independently rather than in a team.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Introversion"
            ),
            AssessmentQuestion(
                id = "pf_7",
                text = "I feel uncomfortable when things are uncertain or unpredictable.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Structure"
            ),
            AssessmentQuestion(
                id = "pf_8",
                text = "I enjoy exploring new ideas even if they seem impractical.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Openness"
            ),
            AssessmentQuestion(
                id = "pf_9",
                text = "I consider how my decisions will affect others' feelings.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Feeling"
            ),
            AssessmentQuestion(
                id = "pf_10",
                text = "I often rely on my gut feeling rather than detailed analysis.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Intuition"
            ),
            AssessmentQuestion(
                id = "pf_11",
                text = "I am comfortable being the center of attention.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Extraversion"
            ),
            AssessmentQuestion(
                id = "pf_12",
                text = "I like to complete tasks well before deadlines.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Structure"
            ),
            AssessmentQuestion(
                id = "pf_13",
                text = "I am drawn to creative and artistic activities.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Openness"
            ),
            AssessmentQuestion(
                id = "pf_14",
                text = "I find it difficult to say no to people, even when I should.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Feeling"
            ),
            AssessmentQuestion(
                id = "pf_15",
                text = "I prefer focusing on concrete facts rather than possibilities.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Sensing"
            ),
            AssessmentQuestion(
                id = "pf_16",
                text = "I need time alone to recharge after social interactions.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Introversion"
            ),
            AssessmentQuestion(
                id = "pf_17",
                text = "I enjoy having a flexible schedule that can change.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Perceiving"
            ),
            AssessmentQuestion(
                id = "pf_18",
                text = "I often think about the deeper meaning behind events.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Intuition"
            ),
            AssessmentQuestion(
                id = "pf_19",
                text = "I prioritize efficiency and effectiveness in my work.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Thinking"
            ),
            AssessmentQuestion(
                id = "pf_20",
                text = "I prefer practical solutions over innovative ones.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Strongly Disagree",
                maxLabel = "Strongly Agree",
                category = "Sensing"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getCoreValues() = Assessment(
        id = "core_values",
        title = "Core Values Discovery",
        description = "Identify and prioritize the values that matter most to you and guide your life decisions.",
        type = AssessmentType.VALUES,
        iconName = "Favorite",
        estimatedMinutes = 8,
        questionCount = 15,
        questions = listOf(
            AssessmentQuestion(
                id = "cv_1",
                text = "Rank how important ACHIEVEMENT is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Achievement"
            ),
            AssessmentQuestion(
                id = "cv_2",
                text = "Rank how important FAMILY is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Family"
            ),
            AssessmentQuestion(
                id = "cv_3",
                text = "Rank how important FREEDOM is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Freedom"
            ),
            AssessmentQuestion(
                id = "cv_4",
                text = "Rank how important CREATIVITY is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Creativity"
            ),
            AssessmentQuestion(
                id = "cv_5",
                text = "Rank how important SECURITY is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Security"
            ),
            AssessmentQuestion(
                id = "cv_6",
                text = "Rank how important ADVENTURE is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Adventure"
            ),
            AssessmentQuestion(
                id = "cv_7",
                text = "Rank how important HEALTH is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Health"
            ),
            AssessmentQuestion(
                id = "cv_8",
                text = "Rank how important KNOWLEDGE is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Knowledge"
            ),
            AssessmentQuestion(
                id = "cv_9",
                text = "Rank how important RELATIONSHIPS is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Relationships"
            ),
            AssessmentQuestion(
                id = "cv_10",
                text = "Rank how important INTEGRITY is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Integrity"
            ),
            AssessmentQuestion(
                id = "cv_11",
                text = "Rank how important WEALTH is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Wealth"
            ),
            AssessmentQuestion(
                id = "cv_12",
                text = "Rank how important SERVICE is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Service"
            ),
            AssessmentQuestion(
                id = "cv_13",
                text = "Rank how important BALANCE is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Balance"
            ),
            AssessmentQuestion(
                id = "cv_14",
                text = "Rank how important GROWTH is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Growth"
            ),
            AssessmentQuestion(
                id = "cv_15",
                text = "Rank how important INFLUENCE is to you:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Not Important",
                maxLabel = "Essential",
                category = "Influence"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getEmotionalIntelligence() = Assessment(
        id = "emotional_intelligence",
        title = "Emotional Intelligence",
        description = "Understand your emotional awareness, regulation, and how you navigate feelings in daily life.",
        type = AssessmentType.EMOTIONAL,
        iconName = "SelfImprovement",
        estimatedMinutes = 12,
        questionCount = 16,
        questions = listOf(
            AssessmentQuestion(
                id = "ei_1",
                text = "When someone is upset, I can usually sense it even if they don't say anything.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Empathy"
            ),
            AssessmentQuestion(
                id = "ei_2",
                text = "I can usually identify exactly what I'm feeling when I'm emotional.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Awareness"
            ),
            AssessmentQuestion(
                id = "ei_3",
                text = "I can stay calm and think clearly under pressure.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Regulation"
            ),
            AssessmentQuestion(
                id = "ei_4",
                text = "I adapt well to new social situations.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Social Skills"
            ),
            AssessmentQuestion(
                id = "ei_5",
                text = "I pursue goals even when facing obstacles or setbacks.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Motivation"
            ),
            AssessmentQuestion(
                id = "ei_6",
                text = "I understand why I react the way I do in different situations.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Awareness"
            ),
            AssessmentQuestion(
                id = "ei_7",
                text = "I can manage my emotions to avoid saying things I'd regret.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Regulation"
            ),
            AssessmentQuestion(
                id = "ei_8",
                text = "I find it easy to see things from another person's perspective.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Empathy"
            ),
            AssessmentQuestion(
                id = "ei_9",
                text = "I can effectively resolve conflicts between people.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Social Skills"
            ),
            AssessmentQuestion(
                id = "ei_10",
                text = "I stay motivated even when results take longer than expected.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Motivation"
            ),
            AssessmentQuestion(
                id = "ei_11",
                text = "I know my emotional strengths and weaknesses.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Awareness"
            ),
            AssessmentQuestion(
                id = "ei_12",
                text = "I can bounce back quickly after disappointment.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Regulation"
            ),
            AssessmentQuestion(
                id = "ei_13",
                text = "I notice when someone's mood changes during a conversation.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Empathy"
            ),
            AssessmentQuestion(
                id = "ei_14",
                text = "I can influence others' emotions positively.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Social Skills"
            ),
            AssessmentQuestion(
                id = "ei_15",
                text = "I find purpose and meaning in my daily activities.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Motivation"
            ),
            AssessmentQuestion(
                id = "ei_16",
                text = "I can tell the difference between feeling anxious and feeling excited.",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Self-Awareness"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getCommunicationStyle() = Assessment(
        id = "communication_style",
        title = "Communication Style",
        description = "Learn how you naturally communicate and how to connect more effectively with others.",
        type = AssessmentType.BEHAVIORAL,
        iconName = "Forum",
        estimatedMinutes = 7,
        questionCount = 12,
        questions = listOf(
            AssessmentQuestion(
                id = "cs_1",
                text = "In conversations, I typically:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Listen more than I speak",
                    "Speak and listen equally",
                    "Speak more than I listen",
                    "Depends heavily on the situation"
                ),
                category = "Balance"
            ),
            AssessmentQuestion(
                id = "cs_2",
                text = "When explaining something complex, I prefer to:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Use detailed step-by-step explanations",
                    "Give the big picture overview first",
                    "Use analogies and stories",
                    "Ask questions to gauge understanding"
                ),
                category = "Style"
            ),
            AssessmentQuestion(
                id = "cs_3",
                text = "In disagreements, I tend to:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Avoid conflict and seek compromise",
                    "Express my view directly",
                    "Listen first to understand",
                    "Analyze the logic of both sides"
                ),
                category = "Conflict"
            ),
            AssessmentQuestion(
                id = "cs_4",
                text = "I express appreciation to others:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely/Privately",
                maxLabel = "Often/Openly",
                category = "Expression"
            ),
            AssessmentQuestion(
                id = "cs_5",
                text = "I prefer communication that is:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Direct and to the point",
                    "Diplomatic and considerate",
                    "Detailed and thorough",
                    "Warm and personal"
                ),
                category = "Preference"
            ),
            AssessmentQuestion(
                id = "cs_6",
                text = "When receiving feedback, I prefer it to be:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Direct and specific",
                    "Balanced with positives",
                    "Private, one-on-one",
                    "Written so I can reflect"
                ),
                category = "Feedback"
            ),
            AssessmentQuestion(
                id = "cs_7",
                text = "I am comfortable with silence in conversations:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Uncomfortable",
                maxLabel = "Very Comfortable",
                category = "Comfort"
            ),
            AssessmentQuestion(
                id = "cs_8",
                text = "I share personal information with new acquaintances:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Very Cautiously",
                maxLabel = "Very Openly",
                category = "Openness"
            ),
            AssessmentQuestion(
                id = "cs_9",
                text = "My natural communication tends to focus on:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Facts and data",
                    "Ideas and possibilities",
                    "People and relationships",
                    "Actions and results"
                ),
                category = "Focus"
            ),
            AssessmentQuestion(
                id = "cs_10",
                text = "I use humor in communication:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Frequently",
                category = "Style"
            ),
            AssessmentQuestion(
                id = "cs_11",
                text = "When someone shares a problem, my first instinct is to:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Offer solutions",
                    "Listen and empathize",
                    "Ask clarifying questions",
                    "Share a similar experience"
                ),
                category = "Response"
            ),
            AssessmentQuestion(
                id = "cs_12",
                text = "I find it easy to ask for help when needed:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Very Difficult",
                maxLabel = "Very Easy",
                category = "Vulnerability"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getLifeSatisfaction() = Assessment(
        id = "life_satisfaction",
        title = "Life Satisfaction Check",
        description = "Evaluate your current satisfaction across different life domains and identify areas for growth.",
        type = AssessmentType.EMOTIONAL,
        iconName = "Spa",
        estimatedMinutes = 6,
        questionCount = 10,
        questions = listOf(
            AssessmentQuestion(
                id = "ls_1",
                text = "How satisfied are you with your CAREER or work life?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Career"
            ),
            AssessmentQuestion(
                id = "ls_2",
                text = "How satisfied are you with your close RELATIONSHIPS?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Relationships"
            ),
            AssessmentQuestion(
                id = "ls_3",
                text = "How satisfied are you with your physical HEALTH?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Health"
            ),
            AssessmentQuestion(
                id = "ls_4",
                text = "How satisfied are you with your PERSONAL GROWTH?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Growth"
            ),
            AssessmentQuestion(
                id = "ls_5",
                text = "How satisfied are you with your FINANCIAL situation?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Finance"
            ),
            AssessmentQuestion(
                id = "ls_6",
                text = "How satisfied are you with your CREATIVE expression?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Creativity"
            ),
            AssessmentQuestion(
                id = "ls_7",
                text = "How satisfied are you with your SPIRITUAL life?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Spirituality"
            ),
            AssessmentQuestion(
                id = "ls_8",
                text = "How satisfied are you with your FUN and recreation?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Recreation"
            ),
            AssessmentQuestion(
                id = "ls_9",
                text = "How satisfied are you with your physical ENVIRONMENT?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Environment"
            ),
            AssessmentQuestion(
                id = "ls_10",
                text = "Overall, how satisfied are you with your life right now?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 10,
                minLabel = "Very Unsatisfied",
                maxLabel = "Very Satisfied",
                category = "Overall"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getGoalClarity() = Assessment(
        id = "goal_clarity",
        title = "Goal Clarity Assessment",
        description = "Clarify your short-term and long-term goals and understand what drives you forward.",
        type = AssessmentType.GOALS,
        iconName = "Flag",
        estimatedMinutes = 10,
        questionCount = 10,
        questions = listOf(
            AssessmentQuestion(
                id = "gc_1",
                text = "I have a clear vision of where I want to be in 5 years:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Not at all",
                maxLabel = "Very Clear",
                category = "Long-term"
            ),
            AssessmentQuestion(
                id = "gc_2",
                text = "I know what I want to accomplish this year:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Not at all",
                maxLabel = "Very Clear",
                category = "Short-term"
            ),
            AssessmentQuestion(
                id = "gc_3",
                text = "What matters most to you right now?",
                type = QuestionType.TEXT_INPUT,
                category = "Priority"
            ),
            AssessmentQuestion(
                id = "gc_4",
                text = "I regularly make progress toward my most important goals:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Consistently",
                category = "Progress"
            ),
            AssessmentQuestion(
                id = "gc_5",
                text = "What is holding you back from achieving your goals?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Lack of time",
                    "Lack of clarity",
                    "Fear of failure",
                    "Lack of resources",
                    "Procrastination",
                    "Other priorities"
                ),
                category = "Barriers"
            ),
            AssessmentQuestion(
                id = "gc_6",
                text = "I have specific, measurable goals written down:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "None",
                maxLabel = "All key goals",
                category = "Clarity"
            ),
            AssessmentQuestion(
                id = "gc_7",
                text = "My daily actions align with my long-term goals:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Alignment"
            ),
            AssessmentQuestion(
                id = "gc_8",
                text = "What would you do if you knew you couldn't fail?",
                type = QuestionType.TEXT_INPUT,
                category = "Aspiration"
            ),
            AssessmentQuestion(
                id = "gc_9",
                text = "I review and adjust my goals regularly:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Weekly",
                category = "Review"
            ),
            AssessmentQuestion(
                id = "gc_10",
                text = "My goals feel meaningful and motivating to me:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Not at all",
                maxLabel = "Deeply",
                category = "Motivation"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getStressProfile() = Assessment(
        id = "stress_profile",
        title = "Stress Profile",
        description = "Understand your stress triggers, responses, and develop better coping strategies.",
        type = AssessmentType.EMOTIONAL,
        iconName = "Balance",
        estimatedMinutes = 8,
        questionCount = 12,
        questions = listOf(
            AssessmentQuestion(
                id = "sp_1",
                text = "How often do you feel overwhelmed by your responsibilities?",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Very Often",
                category = "Frequency"
            ),
            AssessmentQuestion(
                id = "sp_2",
                text = "When stressed, my body typically:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Tenses up (shoulders, jaw, etc.)",
                    "Has digestive issues",
                    "Gets headaches",
                    "Has trouble sleeping",
                    "Feels fatigued",
                    "Multiple symptoms"
                ),
                category = "Physical"
            ),
            AssessmentQuestion(
                id = "sp_3",
                text = "I can recognize early signs that I'm becoming stressed:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Awareness"
            ),
            AssessmentQuestion(
                id = "sp_4",
                text = "My main sources of stress are:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Work/Career",
                    "Relationships",
                    "Finances",
                    "Health",
                    "Time management",
                    "Uncertainty about future"
                ),
                category = "Sources"
            ),
            AssessmentQuestion(
                id = "sp_5",
                text = "I have effective strategies to manage stress:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Not at all",
                maxLabel = "Very Effective",
                category = "Coping"
            ),
            AssessmentQuestion(
                id = "sp_6",
                text = "When stressed, I tend to:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Withdraw and isolate",
                    "Talk to someone",
                    "Exercise or move",
                    "Distract myself (TV, phone)",
                    "Work harder/push through",
                    "Eat or drink more"
                ),
                category = "Response"
            ),
            AssessmentQuestion(
                id = "sp_7",
                text = "I maintain healthy boundaries to protect my energy:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Boundaries"
            ),
            AssessmentQuestion(
                id = "sp_8",
                text = "I take regular breaks during work or demanding tasks:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Regularly",
                category = "Recovery"
            ),
            AssessmentQuestion(
                id = "sp_9",
                text = "Stress affects my sleep quality:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Not at all",
                maxLabel = "Significantly",
                category = "Sleep"
            ),
            AssessmentQuestion(
                id = "sp_10",
                text = "I have activities that help me truly relax:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "None",
                maxLabel = "Several",
                category = "Recovery"
            ),
            AssessmentQuestion(
                id = "sp_11",
                text = "My stress levels are manageable most of the time:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Overall"
            ),
            AssessmentQuestion(
                id = "sp_12",
                text = "I ask for help when I feel overwhelmed:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Always",
                category = "Support"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )

    private fun getRelationshipPatterns() = Assessment(
        id = "relationship_patterns",
        title = "Relationship Patterns",
        description = "Explore your attachment style and patterns in close relationships.",
        type = AssessmentType.RELATIONSHIPS,
        iconName = "People",
        estimatedMinutes = 10,
        questionCount = 14,
        questions = listOf(
            AssessmentQuestion(
                id = "rp_1",
                text = "I find it easy to get close to others:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Closeness"
            ),
            AssessmentQuestion(
                id = "rp_2",
                text = "I worry about being abandoned by people I care about:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Often",
                category = "Anxiety"
            ),
            AssessmentQuestion(
                id = "rp_3",
                text = "I am comfortable depending on others:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Dependence"
            ),
            AssessmentQuestion(
                id = "rp_4",
                text = "I prefer to be self-reliant rather than depend on others:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Independence"
            ),
            AssessmentQuestion(
                id = "rp_5",
                text = "I often wonder if my partner/friends truly care about me:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Often",
                category = "Anxiety"
            ),
            AssessmentQuestion(
                id = "rp_6",
                text = "I am comfortable sharing my deepest feelings with close ones:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Vulnerability"
            ),
            AssessmentQuestion(
                id = "rp_7",
                text = "I sometimes feel suffocated when someone gets too close:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Never",
                maxLabel = "Often",
                category = "Avoidance"
            ),
            AssessmentQuestion(
                id = "rp_8",
                text = "In conflict, I tend to:",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Pursue and try to resolve immediately",
                    "Need space before discussing",
                    "Avoid the conflict entirely",
                    "Adapt to what the other person needs"
                ),
                category = "Conflict"
            ),
            AssessmentQuestion(
                id = "rp_9",
                text = "I express my needs clearly in relationships:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Rarely",
                maxLabel = "Always",
                category = "Communication"
            ),
            AssessmentQuestion(
                id = "rp_10",
                text = "I trust people until they give me a reason not to:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Trust"
            ),
            AssessmentQuestion(
                id = "rp_11",
                text = "I often give more than I receive in relationships:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Balance"
            ),
            AssessmentQuestion(
                id = "rp_12",
                text = "I feel comfortable when my partner/friends need space:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Security"
            ),
            AssessmentQuestion(
                id = "rp_13",
                text = "My relationships feel generally stable and secure:",
                type = QuestionType.SLIDER,
                minValue = 1,
                maxValue = 7,
                minLabel = "Disagree",
                maxLabel = "Agree",
                category = "Stability"
            ),
            AssessmentQuestion(
                id = "rp_14",
                text = "What quality do you most value in close relationships?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf(
                    "Trust and loyalty",
                    "Open communication",
                    "Emotional support",
                    "Shared interests",
                    "Independence and space",
                    "Fun and adventure"
                ),
                category = "Values"
            )
        ),
        status = AssessmentStatus.NOT_STARTED,
        isRepeatable = true
    )
}
