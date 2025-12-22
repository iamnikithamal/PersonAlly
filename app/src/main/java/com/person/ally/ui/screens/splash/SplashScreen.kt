package com.person.ally.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.person.ally.PersonAllyApp
import com.person.ally.ui.theme.PersonAllyTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: (isOnboarded: Boolean) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PersonAllyApp
    val settingsDataStore = app.settingsDataStore
    val onboardingCompleted by settingsDataStore.onboardingCompleted.collectAsState(initial = null)

    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(400)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(onboardingCompleted) {
        if (onboardingCompleted != null) {
            delay(1500)
            onSplashComplete(onboardingCompleted!!)
        }
    }

    val colors = PersonAllyTheme.extendedColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.gradientStart,
                                colors.gradientMiddle,
                                colors.gradientEnd
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PersonAlly",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your AI Life Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}
