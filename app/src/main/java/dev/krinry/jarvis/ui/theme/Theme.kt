package dev.krinry.jarvis.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisDarkScheme = darkColorScheme(
    primary           = GradientStart,
    onPrimary         = Color.White,
    primaryContainer  = GradientEnd,
    secondary         = CyanGlow,
    onSecondary       = Color.Black,
    tertiary          = PurpleAccent,
    background        = DeepSpace,
    onBackground      = TextPrimary,
    surface           = NavyDark,
    onSurface         = TextPrimary,
    surfaceVariant    = NavyMid,
    onSurfaceVariant  = TextSecondary,
    outline           = GlassBorder,
    error             = StatusRed,
    onError           = Color.White
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = JarvisDarkScheme,
        typography  = Typography,
        content     = content
    )
}
