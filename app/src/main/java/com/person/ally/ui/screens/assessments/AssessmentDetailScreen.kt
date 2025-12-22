package com.person.ally.ui.screens.assessments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.data.model.AssessmentAnswer
import com.person.ally.data.model.AssessmentQuestion
import com.person.ally.data.model.QuestionType
import com.person.ally.ui.components.GradientButton
import com.person.ally.ui.components.PersonAllyCard
import com.person.ally.ui.components.SecondaryButton
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentDetailScreen(
    assessmentId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val scope = rememberCoroutineScope()

    val assessment by app.assessmentRepository.getAssessmentById(assessmentId)
        .collectAsState(initial = null)

    val colors = PersonAllyTheme.extendedColors

    if (assessment == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Assessment not found")
        }
        return
    }

    val currentAssessment = assessment!!
    val isCompleted = currentAssessment.completedAt != null

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(currentAssessment.answers) }
    var showResults by remember { mutableStateOf(isCompleted) }

    val totalQuestions = currentAssessment.questions.size
    val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentAssessment.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!showResults) {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!showResults) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = colors.gradientStart,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                AnimatedContent(
                    targetState = currentQuestionIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "question_content"
                ) { questionIndex ->
                    val question = currentAssessment.questions[questionIndex]
                    val currentAnswer = answers.find { it.questionId == question.id }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        QuestionContent(
                            question = question,
                            answer = currentAnswer,
                            onAnswerChange = { newAnswer ->
                                answers = answers.filter { it.questionId != question.id } + newAnswer
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Navigation buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentQuestionIndex > 0) {
                                SecondaryButton(
                                    text = "Previous",
                                    onClick = { currentQuestionIndex-- },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val hasAnswer = answers.any { it.questionId == question.id }

                            if (currentQuestionIndex < totalQuestions - 1) {
                                GradientButton(
                                    text = "Next",
                                    onClick = { currentQuestionIndex++ },
                                    enabled = hasAnswer,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                GradientButton(
                                    text = "Complete",
                                    onClick = {
                                        scope.launch {
                                            app.assessmentRepository.submitAssessment(
                                                assessmentId = assessmentId,
                                                answers = answers
                                            )
                                            showResults = true
                                        }
                                    },
                                    enabled = hasAnswer && answers.size == totalQuestions,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                // Results view
                AssessmentResults(
                    assessment = currentAssessment,
                    onRetake = {
                        answers = emptyList()
                        currentQuestionIndex = 0
                        showResults = false
                    },
                    onDone = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: AssessmentQuestion,
    answer: AssessmentAnswer?,
    onAnswerChange: (AssessmentAnswer) -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Column {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = colors.gradientStart.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = colors.gradientStart,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = question.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (question.description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (question.type) {
            QuestionType.YES_NO -> YesNoQuestion(
                selectedValue = answer?.selectedOptions?.firstOrNull(),
                onValueSelected = { value ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            selectedOptions = listOf(value)
                        )
                    )
                }
            )

            QuestionType.MULTIPLE_CHOICE -> MultipleChoiceQuestion(
                options = question.options,
                selectedValue = answer?.selectedOptions?.firstOrNull(),
                onValueSelected = { value ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            selectedOptions = listOf(value)
                        )
                    )
                }
            )

            QuestionType.SLIDER -> SliderQuestion(
                minLabel = question.minLabel ?: "Low",
                maxLabel = question.maxLabel ?: "High",
                value = answer?.sliderValue ?: 0.5f,
                onValueChange = { value ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            sliderValue = value
                        )
                    )
                }
            )

            QuestionType.TEXT_INPUT -> TextInputQuestion(
                value = answer?.textResponse ?: "",
                onValueChange = { value ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            textResponse = value
                        )
                    )
                }
            )

            QuestionType.SCENARIO -> ScenarioQuestion(
                options = question.options,
                selectedValue = answer?.selectedOptions?.firstOrNull(),
                onValueSelected = { value ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            selectedOptions = listOf(value)
                        )
                    )
                }
            )

            QuestionType.RANKING -> RankingQuestion(
                options = question.options,
                rankedOptions = answer?.selectedOptions ?: emptyList(),
                onRankingChange = { ranking ->
                    onAnswerChange(
                        AssessmentAnswer(
                            questionId = question.id,
                            selectedOptions = ranking
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun YesNoQuestion(
    selectedValue: String?,
    onValueSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("Yes", "No").forEach { option ->
            val isSelected = selectedValue == option
            val colors = PersonAllyTheme.extendedColors

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onValueSelected(option) },
                color = if (isSelected) colors.gradientStart else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceQuestion(
    options: List<String>,
    selectedValue: String?,
    onValueSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedValue == option
            val colors = PersonAllyTheme.extendedColors

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onValueSelected(option) },
                color = if (isSelected) {
                    colors.gradientStart.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, colors.gradientStart)
                } else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onValueSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colors.gradientStart
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderQuestion(
    minLabel: String,
    maxLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val colors = PersonAllyTheme.extendedColors
    var sliderValue by remember { mutableFloatStateOf(value) }

    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onValueChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = colors.gradientStart,
                activeTrackColor = colors.gradientStart,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = minLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = maxLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextInputQuestion(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        placeholder = { Text("Type your response...") },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun ScenarioQuestion(
    options: List<String>,
    selectedValue: String?,
    onValueSelected: (String) -> Unit
) {
    MultipleChoiceQuestion(
        options = options,
        selectedValue = selectedValue,
        onValueSelected = onValueSelected
    )
}

@Composable
private fun RankingQuestion(
    options: List<String>,
    rankedOptions: List<String>,
    onRankingChange: (List<String>) -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tap options in order of preference (1 = most important)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { option ->
            val rank = rankedOptions.indexOf(option)
            val isRanked = rank >= 0

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val newRanking = if (isRanked) {
                            rankedOptions.filter { it != option }
                        } else {
                            rankedOptions + option
                        }
                        onRankingChange(newRanking)
                    },
                color = if (isRanked) {
                    colors.gradientStart.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRanked) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = colors.gradientStart,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${rank + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AssessmentResults(
    assessment: com.person.ally.data.model.Assessment,
    onRetake: () -> Unit,
    onDone: () -> Unit
) {
    val colors = PersonAllyTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.gradientStart,
                            colors.gradientMiddle,
                            colors.gradientEnd
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Assessment Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thank you for completing the ${assessment.title}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (assessment.results.isNotEmpty()) {
            PersonAllyCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    assessment.results.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colors.gradientStart
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GradientButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "Retake Assessment",
            onClick = onRetake,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
