package illyan.butler.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.domain.model.Theme
import io.github.aakira.napier.Napier

class ThemeScreen(private val content: @Composable () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ThemeScreenModel>()
        ButlerTheme(
            themeState = screenModel.theme.collectAsState(),
            dynamicColorEnabledState = screenModel.dynamicColorEnabled.collectAsState(),
            getIsNight = screenModel::isNight,
            content = content
        )
    }
}

@Composable
fun ButlerTheme(
    themeState: State<Theme?> = mutableStateOf(Theme.System),
    dynamicColorEnabledState: State<Boolean> = mutableStateOf(true),
    getIsNight: () -> Boolean = { true },
    content: @Composable () -> Unit,
) {
    val theme by themeState
    val dynamicColorEnabled by dynamicColorEnabledState
    val isNight by remember { derivedStateOf(getIsNight) }
    val isSystemInDarkTheme: Boolean = isSystemInDarkTheme()
    val isDark by remember {
        derivedStateOf {
            when (theme) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.System -> isSystemInDarkTheme
                Theme.DayNightCycle -> isNight
                null -> null
            }
        }
    }
    val dynamicLightColorScheme = dynamicDarkColorScheme()
    val dynamicDarkColorScheme = dynamicLightColorScheme()
    val targetColorScheme by remember {
        derivedStateOf {
            if (dynamicColorEnabled && canUseDynamicColors()) {
                when (theme) {
                    Theme.Dark -> dynamicDarkColorScheme
                    Theme.Light -> dynamicLightColorScheme
                    Theme.System -> if (isSystemInDarkTheme) dynamicDarkColorScheme else dynamicLightColorScheme
                    Theme.DayNightCycle -> if (isNight) dynamicDarkColorScheme else dynamicLightColorScheme
                    null -> LightColors
                }
            } else {
                when (theme) {
                    Theme.Dark -> DarkColors
                    Theme.Light -> LightColors
                    Theme.System -> if (isSystemInDarkTheme) DarkColors else LightColors
                    Theme.DayNightCycle -> if (isNight) DarkColors else LightColors
                    null -> LightColors
                }
            }
        }
    }

    LaunchedEffect(targetColorScheme) {
        val themeName = when (targetColorScheme) {
            LightColors -> "Light"
            DarkColors -> "Dark"
            dynamicLightColorScheme -> "Dynamic Light"
            dynamicDarkColorScheme -> "Dynamic Dark"
            else -> "Undefined theme"
        }
        Napier.d("Theme: $theme, Dynamic colors: ${dynamicColorEnabled && canUseDynamicColors()}, Is night: $isNight, Is system in dark theme: $isSystemInDarkTheme, Is dark: $isDark, Target color scheme: $themeName")
    }

    ThemeSystemWindow(isDark ?: isSystemInDarkTheme, dynamicColorEnabled && canUseDynamicColors())

    val colorScheme by animateColorScheme(targetColorScheme, spring(stiffness = Spring.StiffnessLow))
    CompositionLocalProvider(
        LocalTheme provides theme,
        LocalWindowSizeProvider provides getWindowSize(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}