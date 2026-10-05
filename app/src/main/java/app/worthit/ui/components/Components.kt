package app.worthit.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import app.worthit.data.Fmt
import app.worthit.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

// ---------- Small helpers ----------

fun HapticFeedback.tick() = performHapticFeedback(HapticFeedbackType.SegmentTick)
fun HapticFeedback.confirm() = performHapticFeedback(HapticFeedbackType.Confirm)

@Composable
fun rememberNow(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        delay(30_000)
        value = System.currentTimeMillis()
    }
}

@Composable
fun screenPadding(extraBottom: Dp = 0.dp): PaddingValues {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return PaddingValues(start = 16.dp, end = 16.dp, top = top + 12.dp, bottom = bottom + 120.dp + extraBottom)
}

/** Shrinks huge display numbers so ₹10,00,00,000 still fits on a phone. */
fun TextStyle.fitFor(text: String, comfortable: Int = 9): TextStyle {
    val len = text.length
    if (len <= comfortable) return this
    val factor = (comfortable.toFloat() / len).coerceIn(0.5f, 1f)
    return copy(
        fontSize = fontSize * factor,
        lineHeight = if (lineHeight != androidx.compose.ui.unit.TextUnit.Unspecified) lineHeight * factor else lineHeight,
    )
}

/** Lets a horizontally scrolling row run to the screen/card edge while its content stays aligned. */
fun Modifier.bleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val px = if (constraints.hasBoundedWidth) horizontal.roundToPx() else 0
    val wide = if (px == 0) constraints
    else constraints.copy(minWidth = constraints.minWidth + px * 2, maxWidth = constraints.maxWidth + px * 2)
    val p = measurable.measure(wide)
    layout((p.width - px * 2).coerceAtLeast(0), p.height) { p.place(-px, 0) }
}

/** Expressive press feedback: squish on press, spring back on release. */
fun Modifier.pressScale(source: InteractionSource, pressed: Float = 0.94f): Modifier = composed {
    val isPressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(if (isPressed) pressed else 1f, spring(dampingRatio = 0.45f, stiffness = 600f), label = "press")
    graphicsLayer { scaleX = s; scaleY = s }
}

/** Staggered "rise into place" entrance that only plays once per item. */
@Composable
fun Modifier.reveal(index: Int): Modifier {
    var shown by rememberSaveable { mutableStateOf(false) }
    val anim = remember { Animatable(if (shown) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!shown) {
            shown = true
            delay(index * 70L)
            anim.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 220f))
        }
    }
    return this.graphicsLayer {
        alpha = anim.value.coerceIn(0f, 1f)
        translationY = (1f - anim.value) * 64.dp.toPx()
    }
}

// ---------- Buttons & chips ----------

enum class BtnStyle { Filled, Tonal, Outlined, Text }

@Composable
fun ExpressiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: BtnStyle = BtnStyle.Filled,
    enabled: Boolean = true,
    large: Boolean = false,
    vertical: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // M3 Expressive: buttons morph from round to squircle while pressed.
    val corner by animateDpAsState(if (pressed) 14.dp else 40.dp, Motion.snappy(), label = "corner")
    val transparent = style == BtnStyle.Outlined || style == BtnStyle.Text
    val container = when (style) {
        BtnStyle.Filled -> scheme.primary
        BtnStyle.Tonal -> scheme.secondaryContainer
        else -> Color.Transparent
    }
    val content = when (style) {
        BtnStyle.Filled -> scheme.onPrimary
        BtnStyle.Tonal -> scheme.onSecondaryContainer
        else -> scheme.primary
    }
    val bg by animateColorAsState(
        if (enabled || transparent) container else scheme.onSurface.copy(alpha = 0.10f), label = "bg",
    )
    val fg by animateColorAsState(if (enabled) content else scheme.onSurface.copy(alpha = 0.38f), label = "fg")
    Surface(
        onClick = { haptic.confirm(); onClick() },
        modifier = modifier.pressScale(interaction, 0.95f),
        enabled = enabled,
        shape = RoundedCornerShape(corner),
        color = bg,
        contentColor = fg,
        border = if (style == BtnStyle.Outlined) BorderStroke(1.dp, scheme.outlineVariant) else null,
        interactionSource = interaction,
    ) {
        if (vertical) {
            Column(
                Modifier.heightIn(min = 76.dp).padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (icon != null) Icon(icon, null, Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        } else {
            Row(
                Modifier
                    .heightIn(min = if (large) 64.dp else 52.dp)
                    .padding(horizontal = if (large) 28.dp else 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon, null, Modifier.size(if (large) 22.dp else 18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text,
                    style = if (large) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 48.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val corner by animateDpAsState(if (pressed) size / 4 else size / 2, Motion.snappy(), label = "corner")
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = { haptic.tick(); onClick() },
        modifier = modifier.size(size).pressScale(interaction, 0.9f),
        shape = RoundedCornerShape(corner),
        color = container,
        contentColor = content,
        interactionSource = interaction,
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription) }
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val corner by animateDpAsState(if (selected) 12.dp else 20.dp, Motion.bouncy(), label = "chipCorner")
    val bg by animateColorAsState(if (selected) scheme.primary else scheme.surfaceContainerHighest, label = "chipBg")
    val fg by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurface, label = "chipFg")
    Surface(
        onClick = { haptic.tick(); onClick() },
        modifier = modifier.pressScale(interaction, 0.92f),
        shape = RoundedCornerShape(corner),
        color = bg,
        contentColor = fg,
        interactionSource = interaction,
    ) {
        Row(Modifier.height(40.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(Motion.bouncy()) + fadeIn() + scaleIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Icon(Icons.Rounded.Check, null, Modifier.padding(end = 6.dp).size(18.dp))
            }
            if (leading != null) Text("$leading  ", fontSize = 16.sp)
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
fun <T> ChoiceGroup(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    leading: ((T) -> String)? = null,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            ChoiceChip(label(option), option == selected, { onSelect(option) }, leading = leading?.invoke(option))
        }
    }
}

// ---------- Numbers ----------

/** Each character rolls vertically when it changes, like a mechanical counter. */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Row(modifier) {
        text.forEachIndexed { i, c ->
            key(text.length - i) {
                AnimatedContent(
                    targetState = c,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically(spring(dampingRatio = 0.7f, stiffness = 500f)) { if (up) it else -it } + fadeIn()) togetherWith
                            (slideOutVertically { if (up) -it else it } + fadeOut()) using SizeTransform(clip = false)
                    },
                    label = "roll",
                ) { ch ->
                    Text(ch.toString(), style = style, color = color, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

/** Counts up from zero (or the previous value) with an expressive ease. */
@Composable
fun CountUpText(
    target: Double,
    format: (Double) -> String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    durationMillis: Int = 900,
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target) {
        anim.animateTo(target.toFloat(), tween(durationMillis, easing = FastOutSlowInEasing))
    }
    val shown = if (anim.isRunning) anim.value.toDouble() else target
    val finalText = format(target)
    Text(
        format(shown),
        style = style.fitFor(finalText),
        modifier = modifier,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
    )
}

// ---------- Visualisations ----------

/** Your working month as dots. Each dot fills like a little pie as the purchase "eats" days. */
@Composable
fun DayGrid(days: Double, monthDays: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val month = monthDays.coerceIn(1, 31)
    val cap = month * 3
    val shown = days.coerceIn(0.0, cap.toDouble()).toFloat()
    val total = max(month, ceil(shown).toInt()).coerceAtMost(cap)
    val cols = if (month <= 12) month else ceil(month / 2.0).toInt()
    val rows = ceil(total / cols.toFloat()).toInt()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(shown) {
        anim.animateTo(shown, tween((600 + shown * 40).toInt().coerceAtMost(2200), easing = FastOutSlowInEasing))
    }
    val colors = listOf(scheme.primary, scheme.tertiary, scheme.secondary)
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val gap = 6.dp
        val d = min((maxWidth - gap * (cols - 1)) / cols, 26.dp)
        Canvas(Modifier.fillMaxWidth().height((d + gap) * rows - gap)) {
            val dPx = d.toPx()
            val rowGap = gap.toPx()
            val colGap = if (cols > 1) (size.width - dPx * cols) / (cols - 1) else 0f
            for (i in 0 until total) {
                val r = i / cols
                val c = i % cols
                val cx = c * (dPx + colGap) + dPx / 2
                val cy = r * (dPx + rowGap) + dPx / 2
                val fill = colors[(i / month).coerceAtMost(2)]
                drawCircle(scheme.surfaceContainerHighest, radius = dPx / 2, center = Offset(cx, cy))
                val f = (anim.value - i).coerceIn(0f, 1f)
                if (f >= 1f) {
                    drawCircle(fill, radius = dPx / 2, center = Offset(cx, cy))
                } else if (f > 0f) {
                    drawArc(fill, -90f, 360f * f, useCenter = true, topLeft = Offset(cx - dPx / 2, cy - dPx / 2), size = Size(dPx, dPx))
                }
            }
        }
    }
}

@Composable
fun CountdownRing(fraction: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val anim by animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.gentle(), label = "ring")
    Canvas(modifier) {
        val stroke = 4.dp.toPx()
        drawArc(track, 0f, 360f, false, style = Stroke(stroke), topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
        drawArc(color, -90f, 360f * anim, false, style = Stroke(stroke, cap = StrokeCap.Round), topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
    }
}

@Composable
fun Bar(fraction: Float, color: Color, modifier: Modifier = Modifier, delayMillis: Int = 0) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        delay(delayMillis.toLong())
        anim.animateTo(fraction.coerceIn(0f, 1f), spring(dampingRatio = 0.7f, stiffness = 120f))
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        val r = size.height / 2
        drawRoundRect(track, cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r))
        val w = (size.width * anim.value).coerceAtLeast(if (anim.value > 0f) size.height else 0f)
        drawRoundRect(color, size = Size(w, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r))
    }
}

private data class Particle(
    val x0: Float, val vx: Float, val vy: Float, val spin: Float,
    val color: Int, val w: Float, val h: Float,
)

/** A celebratory burst. Bump [trigger] to fire it. */
@Composable
fun Confetti(trigger: Int) {
    if (trigger == 0) return
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(scheme.primary, scheme.tertiary, scheme.secondary, scheme.primaryContainer, scheme.tertiaryContainer, Color(0xFFFFC94D))
    val particles = remember(trigger) {
        List(110) {
            Particle(
                x0 = 0.5f + Random.nextFloat() * 0.2f - 0.1f,
                vx = Random.nextFloat() * 1.3f - 0.65f,
                vy = -(0.5f + Random.nextFloat() * 0.9f),
                spin = Random.nextFloat() * 2f - 1f,
                color = Random.nextInt(palette.size),
                w = 6f + Random.nextFloat() * 8f,
                h = 10f + Random.nextFloat() * 10f,
            )
        }
    }
    val t = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { t.animateTo(1f, tween(2000, easing = LinearEasing)) }
    Canvas(Modifier.fillMaxSize()) {
        val time = t.value
        if (time >= 1f) return@Canvas
        val density = this.density
        particles.forEach { p ->
            val x = size.width * p.x0 + p.vx * size.width * time
            val y = size.height * 0.4f + p.vy * size.height * time + 1.5f * size.height * time * time
            val alpha = (1f - time * time * time).coerceIn(0f, 1f)
            rotate(p.spin * time * 900f, pivot = Offset(x, y)) {
                drawRect(
                    palette[p.color],
                    topLeft = Offset(x - p.w * density / 2, y - p.h * density / 2),
                    size = Size(p.w * density, p.h * density),
                    alpha = alpha,
                )
            }
        }
    }
}

// ---------- Layout pieces ----------

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = color) {
        Column(Modifier.padding(22.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
            }
            content()
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.displaySmall)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        MorphingBlob(MaterialTheme.colorScheme.primaryContainer, Modifier.size(132.dp), holdMillis = 2200) {
            Text(emoji, fontSize = 44.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------- Inputs ----------

class GroupingTransformation(private val indian: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val intPart = raw.substringBefore('.')
        val rest = raw.substring(intPart.length)
        val grouped = Fmt.groupDigits(intPart, indian)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= intPart.length) {
                    var digits = 0
                    var i = 0
                    while (i < grouped.length && digits < offset) {
                        if (grouped[i] != ',') digits++
                        i++
                    }
                    return i
                }
                return grouped.length + (offset - intPart.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= grouped.length) return grouped.take(offset).count { it != ',' }
                return (intPart.length + (offset - grouped.length)).coerceAtMost(raw.length)
            }
        }
        return TransformedText(AnnotatedString(grouped + rest), mapping)
    }
}

fun sanitizeAmount(input: String): String {
    val ints = StringBuilder()
    val decimals = StringBuilder()
    var dot = false
    for (c in input) {
        when {
            c.isDigit() && !dot -> if (ints.length < 12) ints.append(c)
            c.isDigit() && dot -> if (decimals.length < 2) decimals.append(c)
            c == '.' && !dot -> dot = true
        }
    }
    var head = ints.toString().trimStart('0')
    if (head.isEmpty() && (dot || ints.isNotEmpty())) head = "0"
    return if (dot) "$head.$decimals" else head
}

fun Double.toInput(): String = when {
    this <= 0.0 -> ""
    this == Math.floor(this) -> this.toLong().toString()
    else -> String.format(java.util.Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
}

/** A borderless, big-type field that feels like writing on the card itself. */
@Composable
fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onIme: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val color = LocalContentColor.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = style.copy(color = color),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onAny = { onIme() }),
        visualTransformation = visualTransformation,
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(placeholder, style = style, color = color.copy(alpha = 0.38f), maxLines = 1)
                inner()
            }
        },
    )
}

@Composable
fun MoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    currency: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(sanitizeAmount(it)) },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        prefix = { Text(Fmt.symbol(currency)) },
        supportingText = supporting?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        visualTransformation = GroupingTransformation(Fmt.isIndian(currency)),
    )
}

@Composable
fun Stepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    range: IntRange,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    RollingText(value.toString(), MaterialTheme.typography.displaySmall)
                    Text(" $unit", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
            RoundIconButton(Icons.Rounded.Remove, "Less", { if (value > range.first) onChange(value - 1) }, size = 52.dp)
            Spacer(Modifier.width(10.dp))
            RoundIconButton(
                Icons.Rounded.Add, "More", { if (value < range.last) onChange(value + 1) },
                container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary, size = 52.dp,
            )
        }
    }
}

// ---------- Navigation ----------

data class NavItem(val label: String, val icon: ImageVector)

/** A floating pill nav bar. The active tab grows to reveal its label on a spring. */
@Composable
fun ExpressiveNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier.navigationBarsPadding().padding(bottom = 12.dp),
        shape = CircleShape,
        color = scheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
    ) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                val bg by animateColorAsState(if (sel) scheme.primary else Color.Transparent, Motion.snappy(), label = "navBg")
                val fg by animateColorAsState(if (sel) scheme.onPrimary else scheme.onSurfaceVariant, label = "navFg")
                val interaction = remember { MutableInteractionSource() }
                Row(
                    Modifier
                        .pressScale(interaction, 0.88f)
                        .clip(CircleShape)
                        .background(bg)
                        .clickable(interactionSource = interaction, indication = null) {
                            if (!sel) haptic.tick()
                            onSelect(i)
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, item.label, tint = fg)
                    AnimatedVisibility(
                        visible = sel,
                        enter = expandHorizontally(Motion.bouncy()) + fadeIn(),
                        exit = shrinkHorizontally(Motion.snappy()) + fadeOut(),
                    ) {
                        Text(
                            item.label,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                            color = fg,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

