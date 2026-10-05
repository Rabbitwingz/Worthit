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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.R

/*
 * "Dusk" palette: a soft iris accent, warm apricot for "consider", sage for "good value",
 * on calm neutral surfaces. Containers are kept low-chroma so big cards never shout.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF5B4FC4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4DFFF),
    onPrimaryContainer = Color(0xFF1C1259),
    inversePrimary = Color(0xFFC6BFFF),
    secondary = Color(0xFF8E4E2C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC8),
    onSecondaryContainer = Color(0xFF341000),
    tertiary = Color(0xFF356A53),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBDEFD4),
    onTertiaryContainer = Color(0xFF002115),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF8FC),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FC),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE5E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    surfaceTint = Color(0xFF5B4FC4),
    inverseSurface = Color(0xFF303036),
    inverseOnSurface = Color(0xFFF3EFF7),
    outline = Color(0xFF787680),
    outlineVariant = Color(0xFFC9C5D0),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFBF8FC),
    surfaceDim = Color(0xFFDCD9DD),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F2F7),
    surfaceContainer = Color(0xFFEFECF2),
    surfaceContainerHigh = Color(0xFFE9E6EC),
    surfaceContainerHighest = Color(0xFFE3E0E6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC6BFFF),
    onPrimary = Color(0xFF2B2175),
    primaryContainer = Color(0xFF353064),
    onPrimaryContainer = Color(0xFFE4DFFF),
    inversePrimary = Color(0xFF5B4FC4),
    secondary = Color(0xFFFFB68E),
    onSecondary = Color(0xFF532200),
    secondaryContainer = Color(0xFF4A3426),
    onSecondaryContainer = Color(0xFFFFDCC8),
    tertiary = Color(0xFF9FD4B8),
    onTertiary = Color(0xFF003826),
    tertiaryContainer = Color(0xFF1F4536),
    onTertiaryContainer = Color(0xFFBDEFD4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121116),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF121116),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC8C5D0),
    surfaceTint = Color(0xFFC6BFFF),
    inverseSurface = Color(0xFFE5E1E9),
    inverseOnSurface = Color(0xFF303036),
    outline = Color(0xFF928F9A),
    outlineVariant = Color(0xFF47464F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF39383F),
    surfaceDim = Color(0xFF121116),
    surfaceContainerLowest = Color(0xFF0D0C11),
    surfaceContainerLow = Color(0xFF1B1A20),
    surfaceContainer = Color(0xFF1F1E25),
    surfaceContainerHigh = Color(0xFF29282F),
    surfaceContainerHighest = Color(0xFF34333A),
)

// Google Sans Flex, the Material 3 Expressive typeface. Display text uses its rounded axis.
private val weights = listOf(400, 500, 600, 700, 800, 900)

private fun flex(weight: Int, round: Float) = Font(
    R.font.google_sans_flex,
    weight = FontWeight(weight),
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("ROND", round),
    ),
)

val SansFlex = FontFamily(weights.map { flex(it, 0f) })
val RoundedFlex = FontFamily(weights.map { flex(it, 100f) })

private val Base = Typography()

private fun TextStyle.display(weight: Int, tracking: TextUnit) = copy(
    fontFamily = RoundedFlex,
    fontWeight = FontWeight(weight),
    letterSpacing = tracking,
    fontFeatureSettings = "tnum",
)

private fun TextStyle.text(weight: Int) = copy(fontFamily = SansFlex, fontWeight = FontWeight(weight))

val WorthTypography = Typography(
    displayLarge = Base.displayLarge.display(800, (-1.5).sp),
    displayMedium = Base.displayMedium.display(800, (-1).sp),
    displaySmall = Base.displaySmall.display(700, (-0.5).sp),
    headlineLarge = Base.headlineLarge.display(700, (-0.5).sp),
    headlineMedium = Base.headlineMedium.display(700, (-0.25).sp),
    headlineSmall = Base.headlineSmall.display(600, 0.sp),
    titleLarge = Base.titleLarge.text(600),
    titleMedium = Base.titleMedium.text(600),
    titleSmall = Base.titleSmall.text(600),
    bodyLarge = Base.bodyLarge.text(400),
    bodyMedium = Base.bodyMedium.text(400),
    bodySmall = Base.bodySmall.text(400),
    labelLarge = Base.labelLarge.text(600),
    labelMedium = Base.labelMedium.text(600),
    labelSmall = Base.labelSmall.text(500),
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
