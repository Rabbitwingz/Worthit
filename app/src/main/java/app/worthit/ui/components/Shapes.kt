package app.worthit.ui.components

import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import app.worthit.data.Category
import kotlinx.coroutines.delay

private fun android.graphics.Path.fitInto(size: Size, rotation: Float = 0f): android.graphics.Path {
    val bounds = RectF()
    @Suppress("DEPRECATION")
    computeBounds(bounds, true)
    val m = android.graphics.Matrix()
    m.setRectToRect(bounds, RectF(0f, 0f, size.width, size.height), android.graphics.Matrix.ScaleToFit.CENTER)
    if (rotation != 0f) m.postRotate(rotation, size.width / 2f, size.height / 2f)
    transform(m)
    return this
}

/** Any Material 3 Expressive polygon as a Compose [Shape]. */
class PolygonShape(private val polygon: RoundedPolygon, private val rotation: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(polygon.toPath().fitInto(size, rotation).asComposePath())
}

fun morphPath(morph: Morph, progress: Float, size: Size) =
    morph.toPath(progress).fitInto(size).asComposePath()

fun Category.shape(): RoundedPolygon = when (this) {
    Category.Electronics -> MaterialShapes.Cookie4Sided
    Category.Fashion -> MaterialShapes.Flower
    Category.Food -> MaterialShapes.Cookie9Sided
    Category.Travel -> MaterialShapes.Sunny
    Category.Hobbies -> MaterialShapes.Clover4Leaf
    Category.Entertainment -> MaterialShapes.SoftBurst
    Category.Experiences -> MaterialShapes.Burst
    Category.Home -> MaterialShapes.Arch
    Category.Fitness -> MaterialShapes.Pentagon
    Category.Education -> MaterialShapes.Gem
    Category.Other -> MaterialShapes.Cookie6Sided
}

val HeroShapes: List<RoundedPolygon>
    get() = listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Flower,
        MaterialShapes.SoftBurst,
    )

@Composable
fun ShapeBadge(
    polygon: RoundedPolygon,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = remember(polygon) { PolygonShape(polygon) }
    Box(modifier.size(size).clip(shape).background(color), contentAlignment = Alignment.Center, content = content)
}

/**
 * A blob that keeps morphing between expressive shapes on a spring, slowly spinning.
 * Drawing happens in the draw phase only, so it is cheap.
 */
@Composable
fun MorphingBlob(
    color: Color,
    modifier: Modifier = Modifier,
    shapes: List<RoundedPolygon> = HeroShapes,
    holdMillis: Long = 1400,
    spinMillis: Int = 24_000,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val morphs = remember(shapes) { shapes.indices.map { Morph(shapes[it], shapes[(it + 1) % shapes.size]) } }
    var index by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(morphs) {
        while (true) {
            delay(holdMillis)
            progress.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 90f))
            index = (index + 1) % morphs.size
            progress.snapTo(0f)
        }
    }
    val spin by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(spinMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .drawBehind {
                    val path = morphPath(morphs[index], progress.value, size)
                    rotate(spin) { drawPath(path, color) }
                },
        )
        content()
    }
}
