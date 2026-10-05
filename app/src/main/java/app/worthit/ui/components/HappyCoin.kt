package app.worthit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val Gold = Color(0xFFF6B73C)
private val GoldDark = Color(0xFFC9831A)
private val Ink = Color(0xFF1C1259)
private val Blush = Color(0xFFF25C78)
private val Shine = Color(0xFFFBF8FC)

/** The scalloped coin outline from the app icon, radius 30 around (0, 0). */
private val coinPath: Path by lazy {
    val samples = 144
    val lobes = 12
    val amp = 0.045f
    val scale = 30f / (1f + amp)
    val pts = List(samples) { i ->
        val t = i.toFloat() / samples * 2f * PI.toFloat()
        val k = 1f + amp * cos(lobes * t)
        Offset(scale * k * cos(t), scale * k * sin(t))
    }
    Path().apply {
        for (i in 0..samples) {
            val p = pts[i % samples]
            val q = pts[(i + 1) % samples]
            val mid = Offset((p.x + q.x) / 2f, (p.y + q.y) / 2f)
            if (i == 0) moveTo(mid.x, mid.y) else quadraticTo(p.x, p.y, mid.x, mid.y)
        }
        close()
    }
}

private val happyEyes = Path().apply {
    moveTo(-12f, -4f); relativeQuadraticTo(4f, -5f, 8f, 0f)
    moveTo(4f, -4f); relativeQuadraticTo(4f, -5f, 8f, 0f)
}
private val smile = Path().apply { moveTo(-9f, 5f); relativeQuadraticTo(9f, 9f, 18f, 0f) }
private val grin = Path().apply { moveTo(-10f, 4f); relativeQuadraticTo(10f, 13f, 20f, 0f); close() }
private val tongue = Path().apply {
    moveTo(-4.5f, 9.6f); relativeQuadraticTo(4.5f, -3.6f, 9f, 0f); relativeQuadraticTo(-4.5f, 4.4f, -9f, 0f); close()
}
private val sparklePath = Path().apply {
    moveTo(0f, -7.5f)
    relativeLineTo(2.2f, 5.3f); relativeLineTo(5.3f, 2.2f); relativeLineTo(-5.3f, 2.2f); relativeLineTo(-2.2f, 5.3f)
    relativeLineTo(-2.2f, -5.3f); relativeLineTo(-5.3f, -2.2f); relativeLineTo(5.3f, -2.2f)
    close()
}

/**
 * Worth It's mascot, matching the launcher icon. The sparkle twinkles and the coin wiggles now and then.
 * Bump [excite] to make it pop its eyes open and hop by [hop] units; tapping does a bigger cheer.
 */
@Composable
fun HappyCoin(
    modifier: Modifier = Modifier,
    excite: Int = 0,
    hop: Float = 4f,
    sparkleColor: Color = MaterialTheme.colorScheme.primary,
    idle: Boolean = true,
    onTap: (() -> Unit)? = null,
) {
    val lift = remember { Animatable(0f) }
    val tilt = remember { Animatable(0f) }
    var excited by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    suspend fun cheer(height: Float) = coroutineScope {
        launch {
            lift.animateTo(-height, spring(dampingRatio = 0.6f, stiffness = 900f))
            lift.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = 260f))
        }
        launch {
            excited = true
            delay(950)
            excited = false
        }
    }

    LaunchedEffect(excite) { if (excite > 0) cheer(hop) }
    LaunchedEffect(idle) {
        while (idle) {
            delay(Random.nextLong(4_500, 8_000))
            tilt.animateTo(-9f, spring(dampingRatio = 0.5f, stiffness = 500f))
            tilt.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = 180f))
        }
    }

    val twinkle by rememberInfiniteTransition(label = "twinkle").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
        label = "twinkle",
    )
    val faceMix by animateFloatAsState(if (excited) 1f else 0f, spring(dampingRatio = 0.7f, stiffness = 600f), label = "face")

    val tapModifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        scope.launch { cheer(9f) }
        onTap?.invoke()
    }

    Canvas(
        modifier
            .then(tapModifier)
            .graphicsLayer {
                val unit = size.minDimension / 66f
                translationY = lift.value * unit
                rotationZ = tilt.value
            },
    ) {
        val unit = size.minDimension / 66f
        withTransform({
            translate(center.x, center.y)
            scale(unit, unit, pivot = Offset.Zero)
        }) {
            translate(0f, 3f) { drawPath(coinPath, GoldDark) }
            drawPath(coinPath, Gold)
            drawFace(faceMix)
            withTransform({
                translate(23f, -17.5f)
                scale(0.75f + twinkle * 0.35f, 0.75f + twinkle * 0.35f, pivot = Offset.Zero)
                rotate(twinkle * 25f, pivot = Offset.Zero)
            }) { drawPath(sparklePath, sparkleColor) }
        }
    }
}

private fun DrawScope.drawFace(excitedMix: Float) {
    val calm = 1f - excitedMix
    // Cheeks stay put in both moods.
    drawCircle(Blush, radius = 3.5f, center = Offset(-14f, 4f), alpha = 0.55f)
    drawCircle(Blush, radius = 3.5f, center = Offset(14f, 4f), alpha = 0.55f)
    if (calm > 0.01f) {
        val stroke = Stroke(width = 3.5f, cap = StrokeCap.Round)
        drawPath(happyEyes, Ink, alpha = calm, style = stroke)
        drawPath(smile, Ink, alpha = calm, style = stroke)
    }
    if (excitedMix > 0.01f) {
        val pop = 0.6f + 0.4f * excitedMix
        for (x in listOf(-8f, 8f)) {
            drawCircle(Ink, radius = 3.9f * pop, center = Offset(x, -5f), alpha = excitedMix)
            drawCircle(Shine, radius = 1.3f * pop, center = Offset(x + 1.2f, -6.2f), alpha = excitedMix)
        }
        drawPath(grin, Ink, alpha = excitedMix)
        drawPath(tongue, Blush, alpha = excitedMix)
    }
}
