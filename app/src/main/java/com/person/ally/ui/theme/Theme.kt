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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = SurfaceContainerLight,
    onPrimaryContainer = OnSurfaceLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SurfaceContainerLight,
    onSecondaryContainer = OnSurfaceLight,
    tertiary = TertiaryLight,
    onTertiary = OnPrimaryLight,
    tertiaryContainer = SurfaceContainerLight,
    onTertiaryContainer = OnSurfaceLight,
    error = ErrorLight,
    onError = OnPrimaryLight,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Scrim,
    inverseSurface = SurfaceDark,
    inverseOnSurface = OnSurfaceDark,
    inversePrimary = PrimaryDark,
    surfaceDim = Color(0xFFE6E0E0),
    surfaceBright = SurfaceLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = SurfaceVariantLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = Color(0xFFFFE5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = SurfaceContainerDark,
    onPrimaryContainer = OnSurfaceDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SurfaceContainerDark,
    onSecondaryContainer = OnSurfaceDark,
    tertiary = TertiaryDark,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = SurfaceContainerDark,
    onTertiaryContainer = OnSurfaceDark,
    error = ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = Color(0xFF49454F),
    scrim = Scrim,
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurfaceLight,
    inversePrimary = PrimaryLight,
    surfaceDim = Color(0xFF151212),
    surfaceBright = SurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF100D0D),
    surfaceContainerLow = BackgroundDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = Color(0xFF4A4040)
)

data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color,
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
}

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessLight,
        warning = WarningLight,
        info = InfoLight,
        memoryCore = MemoryCoreLight,
        memoryEvolving = MemoryEvolvingLight,
        memoryContextual = MemoryContextualLight,
        memoryEpisodic = MemoryEpisodicLight,
        domainCareer = DomainCareerLight,
        domainRelationships = DomainRelationshipsLight,
        domainHealth = DomainHealthLight,
        domainGrowth = DomainGrowthLight,
        domainFinance = DomainFinanceLight,
        domainCreativity = DomainCreativityLight,
        domainSpirituality = DomainSpiritualityLight,
        domainRecreation = DomainRecreationLight,
        gradientStart = GradientStart,
        gradientMiddle = GradientMiddle,
        gradientEnd = GradientEnd
    )
}

private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,
    memoryCore = MemoryCoreLight,
    memoryEvolving = MemoryEvolvingLight,
    memoryContextual = MemoryContextualLight,
    memoryEpisodic = MemoryEpisodicLight,
    domainCareer = DomainCareerLight,
    domainRelationships = DomainRelationshipsLight,
    domainHealth = DomainHealthLight,
    domainGrowth = DomainGrowthLight,
    domainFinance = DomainFinanceLight,
    domainCreativity = DomainCreativityLight,
    domainSpirituality = DomainSpiritualityLight,
    domainRecreation = DomainRecreationLight,
    gradientStart = GradientStart,
    gradientMiddle = GradientMiddle,
    gradientEnd = GradientEnd
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,
    memoryCore = MemoryCoreDark,
    memoryEvolving = MemoryEvolvingDark,
    memoryContextual = MemoryContextualDark,
    memoryEpisodic = MemoryEpisodicDark,
    domainCareer = DomainCareerDark,
    domainRelationships = DomainRelationshipsDark,
    domainHealth = DomainHealthDark,
    domainGrowth = DomainGrowthDark,
    domainFinance = DomainFinanceDark,
    domainCreativity = DomainCreativityDark,
    domainSpirituality = DomainSpiritualityDark,
    domainRecreation = DomainRecreationDark,
    gradientStart = GradientStart,
    gradientMiddle = GradientMiddle,
    gradientEnd = GradientEnd
)

@Composable
fun PersonAllyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
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
