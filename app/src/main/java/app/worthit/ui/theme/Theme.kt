package app.worthit.ui.theme

import android.os.Build
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warm off-white + charcoal, one violet accent, green for "good value", clay for "consider".
private val LightColors = lightColorScheme(
    primary = Color(0xFF5A3FD1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF1B0063),
    inversePrimary = Color(0xFFCABEFF),
    secondary = Color(0xFF8A5A44),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCC),
    onSecondaryContainer = Color(0xFF331206),
    tertiary = Color(0xFF2E6B4F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB1F1CE),
    onTertiaryContainer = Color(0xFF002114),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDF8F3),
    onBackground = Color(0xFF1D1B19),
    surface = Color(0xFFFDF8F3),
    onSurface = Color(0xFF1D1B19),
    surfaceVariant = Color(0xFFECE4DC),
    onSurfaceVariant = Color(0xFF4C463F),
    surfaceTint = Color(0xFF5A3FD1),
    inverseSurface = Color(0xFF32302D),
    inverseOnSurface = Color(0xFFF6F0EA),
    outline = Color(0xFF7D766E),
    outlineVariant = Color(0xFFD6CDC3),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFDF8F3),
    surfaceDim = Color(0xFFDED9D3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F2EC),
    surfaceContainer = Color(0xFFF2ECE6),
    surfaceContainerHigh = Color(0xFFECE6E0),
    surfaceContainerHighest = Color(0xFFE6E1DB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCABEFF),
    onPrimary = Color(0xFF2C009D),
    primaryContainer = Color(0xFF4224B8),
    onPrimaryContainer = Color(0xFFE6DEFF),
    inversePrimary = Color(0xFF5A3FD1),
    secondary = Color(0xFFF0BBA3),
    onSecondary = Color(0xFF4D2716),
    secondaryContainer = Color(0xFF6A3E2C),
    onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = Color(0xFF95D5B2),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF11513A),
    onTertiaryContainer = Color(0xFFB1F1CE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF151311),
    onBackground = Color(0xFFE8E1DC),
    surface = Color(0xFF151311),
    onSurface = Color(0xFFE8E1DC),
    surfaceVariant = Color(0xFF4C463F),
    onSurfaceVariant = Color(0xFFCFC6BC),
    surfaceTint = Color(0xFFCABEFF),
    inverseSurface = Color(0xFFE8E1DC),
    inverseOnSurface = Color(0xFF32302D),
    outline = Color(0xFF988F87),
    outlineVariant = Color(0xFF4C463F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3B3836),
    surfaceDim = Color(0xFF151311),
    surfaceContainerLowest = Color(0xFF100E0C),
    surfaceContainerLow = Color(0xFF1D1B19),
    surfaceContainer = Color(0xFF221F1D),
    surfaceContainerHigh = Color(0xFF2C2927),
    surfaceContainerHighest = Color(0xFF373432),
)

private val Base = Typography()

val WorthTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-2).sp),
    displayMedium = Base.displayMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp),
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

val WorthShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Springs for custom micro-interactions, tuned to match the M3 Expressive motion scheme. */
object Motion {
    fun <T> bouncy(): SpringSpec<T> = spring(dampingRatio = 0.55f, stiffness = 380f)
    fun <T> snappy(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 700f)
    fun <T> gentle(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = 200f)
    fun slide(): SpringSpec<IntOffset> =
        spring(dampingRatio = 0.86f, stiffness = 450f, visibilityThreshold = IntOffset(1, 1))
}

@Composable
fun WorthItTheme(dynamic: Boolean, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        shapes = WorthShapes,
        typography = WorthTypography,
        content = content,
    )
}
