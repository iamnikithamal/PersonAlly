package com.person.ally.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.person.ally.data.local.datastore.ThemeMode
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.MemoryCategory

/**
 * PersonAlly Theme System - Material 3 Green Theme
 *
 * A clean, professional, modern theme using green as the primary color.
 */

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Neutral100,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Teal40,
    onSecondary = Neutral100,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = DeepPurple40,
    onTertiary = Neutral100,
    tertiaryContainer = DeepPurple90,
    onTertiaryContainer = DeepPurple10,
    error = Error40,
    onError = Neutral100,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral100,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    scrim = Neutral0,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Green80,
    surfaceDim = Neutral90,
    surfaceBright = Neutral99,
    surfaceContainerLowest = Neutral100,
    surfaceContainerLow = Neutral95,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = Neutral90,
    surfaceContainerHighest = Neutral90
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = DeepPurple80,
    onTertiary = DeepPurple20,
    tertiaryContainer = DeepPurple30,
    onTertiaryContainer = DeepPurple90,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    scrim = Neutral0,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Green40,
    surfaceDim = Neutral10,
    surfaceBright = Neutral30,
    surfaceContainerLowest = Neutral0,
    surfaceContainerLow = Neutral10,
    surfaceContainer = Neutral20,
    surfaceContainerHigh = Neutral20,
    surfaceContainerHighest = Neutral30
)

/**
 * Extended colors for semantic and domain-specific purposes
 */
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val memoryCore: Color,
    val memoryEvolving: Color,
    val memoryContextual: Color,
    val memoryEpisodic: Color,
    val domainCareer: Color,
    val domainRelationships: Color,
    val domainHealth: Color,
    val domainGrowth: Color,
    val domainFinance: Color,
    val domainCreativity: Color,
    val domainSpirituality: Color,
    val domainRecreation: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentTertiary: Color,
    val gradientStart: Color,
    val gradientMiddle: Color,
    val gradientEnd: Color
) {
    fun getDomainColor(domain: LifeDomain): Color = when (domain) {
        LifeDomain.CAREER -> domainCareer
        LifeDomain.RELATIONSHIPS -> domainRelationships
        LifeDomain.HEALTH -> domainHealth
        LifeDomain.PERSONAL_GROWTH -> domainGrowth
        LifeDomain.FINANCE -> domainFinance
        LifeDomain.CREATIVITY -> domainCreativity
        LifeDomain.SPIRITUALITY -> domainSpirituality
        LifeDomain.RECREATION -> domainRecreation
    }

    fun getCategoryColor(category: MemoryCategory): Color = when (category) {
        MemoryCategory.CORE_IDENTITY -> memoryCore
        MemoryCategory.EVOLVING_UNDERSTANDING -> memoryEvolving
        MemoryCategory.CONTEXTUAL -> memoryContextual
        MemoryCategory.EPISODIC -> memoryEpisodic
    }
}

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessLight,
        successContainer = SuccessContainer,
        onSuccessContainer = Green10,
        warning = WarningLight,
        warningContainer = WarningContainer,
        onWarningContainer = Color(0xFF3D3100),
        info = InfoLight,
        infoContainer = InfoContainer,
        onInfoContainer = Color(0xFF003350),
        memoryCore = MemoryCoreIdentity,
        memoryEvolving = MemoryEvolvingUnderstanding,
        memoryContextual = MemoryContextual,
        memoryEpisodic = MemoryEpisodic,
        domainCareer = DomainCareer,
        domainRelationships = DomainRelationships,
        domainHealth = DomainHealth,
        domainGrowth = DomainPersonalGrowth,
        domainFinance = DomainFinance,
        domainCreativity = DomainCreativity,
        domainSpirituality = DomainSpirituality,
        domainRecreation = DomainRecreation,
        accentPrimary = Green50,
        accentSecondary = Teal50,
        accentTertiary = DeepPurple50,
        gradientStart = Green40,
        gradientMiddle = Teal40,
        gradientEnd = DeepPurple40
    )
}

private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    successContainer = SuccessContainer,
    onSuccessContainer = Green10,
    warning = WarningLight,
    warningContainer = WarningContainer,
    onWarningContainer = Color(0xFF3D3100),
    info = InfoLight,
    infoContainer = InfoContainer,
    onInfoContainer = Color(0xFF003350),
    memoryCore = MemoryCoreIdentity,
    memoryEvolving = MemoryEvolvingUnderstanding,
    memoryContextual = MemoryContextual,
    memoryEpisodic = MemoryEpisodic,
    domainCareer = DomainCareer,
    domainRelationships = DomainRelationships,
    domainHealth = DomainHealth,
    domainGrowth = DomainPersonalGrowth,
    domainFinance = DomainFinance,
    domainCreativity = DomainCreativity,
    domainSpirituality = DomainSpirituality,
    domainRecreation = DomainRecreation,
    accentPrimary = Green50,
    accentSecondary = Teal50,
    accentTertiary = DeepPurple50,
    gradientStart = Green40,
    gradientMiddle = Teal40,
    gradientEnd = DeepPurple40
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = Green90,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = Color(0xFFFFE082),
    info = InfoDark,
    infoContainer = InfoContainerDark,
    onInfoContainer = Color(0xFFB3E5FC),
    memoryCore = MemoryCoreIdentity,
    memoryEvolving = MemoryEvolvingUnderstanding,
    memoryContextual = MemoryContextual,
    memoryEpisodic = MemoryEpisodic,
    domainCareer = DomainCareer,
    domainRelationships = DomainRelationships,
    domainHealth = DomainHealth,
    domainGrowth = DomainPersonalGrowth,
    domainFinance = DomainFinance,
    domainCreativity = DomainCreativity,
    domainSpirituality = DomainSpirituality,
    domainRecreation = DomainRecreation,
    accentPrimary = Green70,
    accentSecondary = Teal70,
    accentTertiary = DeepPurple70,
    gradientStart = Green70,
    gradientMiddle = Teal70,
    gradientEnd = DeepPurple70
)

@Composable
fun PersonAllyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Disabled by default for consistent green theme
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object PersonAllyTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

@Composable
fun MemoryCategory.getColor(): Color = when (this) {
    MemoryCategory.CORE_IDENTITY -> PersonAllyTheme.extendedColors.memoryCore
    MemoryCategory.EVOLVING_UNDERSTANDING -> PersonAllyTheme.extendedColors.memoryEvolving
    MemoryCategory.CONTEXTUAL -> PersonAllyTheme.extendedColors.memoryContextual
    MemoryCategory.EPISODIC -> PersonAllyTheme.extendedColors.memoryEpisodic
}

@Composable
fun LifeDomain.getColor(): Color = when (this) {
    LifeDomain.CAREER -> PersonAllyTheme.extendedColors.domainCareer
    LifeDomain.RELATIONSHIPS -> PersonAllyTheme.extendedColors.domainRelationships
    LifeDomain.HEALTH -> PersonAllyTheme.extendedColors.domainHealth
    LifeDomain.PERSONAL_GROWTH -> PersonAllyTheme.extendedColors.domainGrowth
    LifeDomain.FINANCE -> PersonAllyTheme.extendedColors.domainFinance
    LifeDomain.CREATIVITY -> PersonAllyTheme.extendedColors.domainCreativity
    LifeDomain.SPIRITUALITY -> PersonAllyTheme.extendedColors.domainSpirituality
    LifeDomain.RECREATION -> PersonAllyTheme.extendedColors.domainRecreation
}

fun LifeDomain.getDisplayName(): String = when (this) {
    LifeDomain.CAREER -> "Career"
    LifeDomain.RELATIONSHIPS -> "Relationships"
    LifeDomain.HEALTH -> "Health"
    LifeDomain.PERSONAL_GROWTH -> "Personal Growth"
    LifeDomain.FINANCE -> "Finance"
    LifeDomain.CREATIVITY -> "Creativity"
    LifeDomain.SPIRITUALITY -> "Spirituality"
    LifeDomain.RECREATION -> "Recreation"
}

fun LifeDomain.getIconName(): String = when (this) {
    LifeDomain.CAREER -> "Work"
    LifeDomain.RELATIONSHIPS -> "Favorite"
    LifeDomain.HEALTH -> "FitnessCenter"
    LifeDomain.PERSONAL_GROWTH -> "TrendingUp"
    LifeDomain.FINANCE -> "AccountBalance"
    LifeDomain.CREATIVITY -> "Brush"
    LifeDomain.SPIRITUALITY -> "SelfImprovement"
    LifeDomain.RECREATION -> "SportsEsports"
}

val MemoryCategory.displayName: String
    get() = when (this) {
        MemoryCategory.CORE_IDENTITY -> "Core Identity"
        MemoryCategory.EVOLVING_UNDERSTANDING -> "Evolving Understanding"
        MemoryCategory.CONTEXTUAL -> "Contextual"
        MemoryCategory.EPISODIC -> "Episodic"
    }
