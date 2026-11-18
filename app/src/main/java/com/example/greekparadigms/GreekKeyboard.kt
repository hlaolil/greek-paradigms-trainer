package com.example.greekparadigms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greekparadigms.ui.theme.GreekFamily
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

private val LETTER_ROWS = listOf(
    "ςερτυθιοπ",
    "ασδφγηξκλ",
    "ζχψωβνμ",
)

/** Diacritic keys: what is drawn on the key, the combining mark it applies, and a spoken name. */
private data class MarkKey(val label: String, val mark: Char, val name: String)

private val MARK_KEYS = listOf(
    MarkKey("ἀ", GreekText.SMOOTH, "smooth breathing"),
    MarkKey("ἁ", GreekText.ROUGH, "rough breathing"),
    MarkKey("ά", GreekText.ACUTE, "acute"),
    MarkKey("ὰ", GreekText.GRAVE, "grave"),
    MarkKey("ᾶ", GreekText.CIRCUMFLEX, "circumflex"),
    MarkKey("ᾳ", GreekText.IOTA_SUB, "iota subscript"),
    MarkKey("ϊ", GreekText.DIAERESIS, "diaeresis"),
)

/**
 * An on-screen polytonic Greek keyboard. Type the letter first, then tap a
 * diacritic key to add it to that letter (tap again to remove it).
 */
@Composable
fun GreekKeyboard(
    text: String,
    onText: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    fun tap(next: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onText(next)
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MARK_KEYS.forEach { k ->
                Key(
                    label = k.label,
                    description = k.name,
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                ) { tap(GreekText.applyMark(text, k.mark)) }
            }
        }
        LETTER_ROWS.forEachIndexed { i, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (i == 2) {
                    Key("⌫", "backspace", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface, Modifier.weight(1.5f)) {
                        tap(GreekText.backspace(text))
                    }
                }
                row.forEach { ch ->
                    Key(ch.toString(), ch.toString(), modifier = Modifier.weight(1f)) {
                        tap(text + ch)
                    }
                }
                if (i == 2) {
                    Key("↵", "check answer", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Modifier.weight(1.5f)) {
                        onSubmit()
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(
    label: String,
    description: String,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp).semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontFamily = GreekFamily, fontSize = 22.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreekKeyboardPreview() {
    GreekParadigmsTheme { GreekKeyboard("λόγος", {}, {}) }
}
