package app.worthit.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.Fmt
import app.worthit.data.Profile
import app.worthit.data.Purchase
import app.worthit.data.Status
import app.worthit.data.Verdict
import app.worthit.data.WorthMath
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.CountdownRing
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.pressScale
import app.worthit.ui.components.shape

@Composable
fun verdictColor(v: Verdict?): Pair<Color, Color> {
    val s = MaterialTheme.colorScheme
    return when (v) {
        Verdict.Yes -> s.tertiaryContainer to s.onTertiaryContainer
        Verdict.Maybe -> s.secondaryContainer to s.onSecondaryContainer
        Verdict.No -> s.surfaceContainerHighest to s.onSurfaceVariant
        null -> s.surfaceContainerHighest to s.onSurface
    }
}

@Composable
fun Tag(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(12.dp), color = container, contentColor = content) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

fun timeShort(price: Double, profile: Profile): String? = Fmt.time(price, profile.income())?.compact

@Composable
fun PurchaseRow(
    p: Purchase,
    profile: Profile,
    now: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(if (selected) scheme.secondaryContainer else scheme.surfaceContainerLow, label = "rowBg")
    val shape = RoundedCornerShape(if (selected) 20.dp else 28.dp)
    Surface(
        modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.97f)
            .clip(shape)
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(),
                onLongClick = onLongClick?.let { { haptic.performHapticFeedback(HapticFeedbackType.LongPress); it() } },
                onClick = onClick,
            ),
        shape = shape,
        color = bg,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ShapeBadge(p.category.shape(), if (selected) scheme.primary else scheme.surfaceContainerHighest, 52.dp) {
                if (selected) Icon(Icons.Rounded.Check, null, tint = scheme.onPrimary)
                else Text(p.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val time = timeShort(p.price, profile)
                Text(
                    Fmt.money(p.price, profile.currency) + (time?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusTag(p, now)
        }
    }
}

@Composable
fun StatusTag(p: Purchase, now: Long) {
    val s = MaterialTheme.colorScheme
    when (p.status) {
        Status.Wishlist -> {
            val until = p.coolOffUntil
            if (until != null && until > now) {
                val total = (until - p.createdAt).coerceAtLeast(1)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        CountdownRing(1f - (until - now).toFloat() / total, Modifier.size(34.dp))
                        Text("⏳", fontSize = 13.sp)
                    }
                    Text(Fmt.durationLeft(until - now), style = MaterialTheme.typography.labelSmall, color = s.onSurfaceVariant)
                }
            } else {
                Tag("Still want it?", s.primaryContainer, s.onPrimaryContainer)
            }
        }
        Status.Bought -> {
            val v = p.verdict
            if (v != null) {
                val (c, fg) = verdictColor(v)
                Tag("${v.emoji} ${v.label}", c, fg)
            } else Tag("Bought", s.surfaceContainerHighest, s.onSurfaceVariant)
        }
        Status.Saving -> Tag("Saving", s.tertiaryContainer, s.onTertiaryContainer)
        Status.Skipped -> Tag("Let go 🌱", s.surfaceContainerHighest, s.onSurfaceVariant)
        Status.Considering -> Tag("Thinking", s.secondaryContainer, s.onSecondaryContainer)
    }
}

private val whenOptions = listOf(
    "Today" to 0,
    "This week" to 3,
    "A week ago" to 8,
    "A month ago" to 31,
    "3 months ago" to 91,
    "6 months ago" to 182,
    "A year ago" to 365,
)

/** "When did you buy it?" Logging older purchases lets reviews and insights start right away. */
@Composable
fun BoughtWhenSheet(onDismiss: () -> Unit, onPick: (Long) -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).navigationBarsPadding()) {
            Text("When did you buy it?", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "We'll check in a few times to see if it was worth it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            ChoiceGroup(
                options = whenOptions,
                selected = null,
                onSelect = { (_, days) -> onPick(System.currentTimeMillis() - days * WorthMath.DAY_MS) },
                label = { it.first },
            )
        }
    }
}

@Composable
fun VerdictPicker(selected: Verdict?, onSelect: (Verdict) -> Unit, labels: Map<Verdict, String> = emptyMap()) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Verdict.entries.forEach { v ->
            val sel = v == selected
            val (c, fg) = verdictColor(v)
            val interaction = remember { MutableInteractionSource() }
            val bg by animateColorAsState(if (sel) c else MaterialTheme.colorScheme.surfaceContainerHighest, label = "v")
            val haptic = LocalHapticFeedback.current
            Surface(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.SegmentTick); onSelect(v) },
                modifier = Modifier.weight(1f).pressScale(interaction, 0.92f),
                shape = RoundedCornerShape(if (sel) 16.dp else 24.dp),
                color = bg,
                contentColor = if (sel) fg else MaterialTheme.colorScheme.onSurface,
                interactionSource = interaction,
            ) {
                Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(v.emoji, fontSize = if (sel) 26.sp else 22.sp)
                    Text(labels[v] ?: v.label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
    }
}
