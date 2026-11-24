package io.github.hlaolil.greekparadigms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.hlaolil.greekparadigms.ui.theme.LocalExtraColors

/** Top bar with an optional back button and trailing actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onBack != null) {
                TextButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) {
                    Text("‹", style = MaterialTheme.typography.headlineMedium)
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

/** A stacked bar: mastered, then learning, then unseen. */
@Composable
fun MasteryBar(mastered: Int, learning: Int, total: Int, modifier: Modifier = Modifier) {
    val extra = LocalExtraColors.current
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(track)
            .semantics { contentDescription = "$mastered of $total mastered, $learning learning" }
    ) {
        if (total > 0) {
            if (mastered > 0) Box(Modifier.weight(mastered.toFloat()).fillMaxHeight().background(extra.mastered))
            if (learning > 0) Box(Modifier.weight(learning.toFloat()).fillMaxHeight().background(extra.learning))
            val rest = total - mastered - learning
            if (rest > 0) Box(Modifier.weight(rest.toFloat()).fillMaxHeight())
        }
    }
}

/**
 * A plain progress bar in the same style as [MasteryBar]. Material's indicator draws
 * a tinted track and a stop dot, which look like stray marks when progress is zero.
 */
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: androidx.compose.ui.unit.Dp = 6.dp,
) {
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
            .semantics { contentDescription = "${(f * 100).toInt()}% complete" }
    ) {
        if (f > 0f) Box(Modifier.fillMaxWidth(f).fillMaxHeight().background(color))
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

/** A small figure with a caption, used in rows of statistics. */
@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A rounded tag, e.g. a "12 due" badge. */
@Composable
fun Badge(text: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(50)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** Centres content and caps its width so layouts stay readable on tablets. */
@Composable
fun Readable(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 640.dp).fillMaxWidth()) { content() }
    }
}

val SpacedBy8 = Arrangement.spacedBy(8.dp)
