package io.github.hlaolil.greekparadigms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hlaolil.greekparadigms.ui.theme.GreekFamily
import io.github.hlaolil.greekparadigms.ui.theme.GreekParadigmsTheme
import io.github.hlaolil.greekparadigms.ui.theme.LocalExtraColors

@Composable
fun ParadigmScreen(
    paradigm: Paradigm,
    progress: Progress,
    onBack: () -> Unit,
    onStudy: (StudyMode, List<Form>) -> Unit,
) {
    var weakOnly by rememberSaveable { mutableStateOf(false) }
    var covered by rememberSaveable { mutableStateOf(false) }
    val weak = paradigm.forms.filter { progress.state(it.id)?.mastered != true }
    val studyForms = if (weakOnly && weak.isNotEmpty()) weak else paradigm.forms

    Column(Modifier.fillMaxSize()) {
        AppBar(paradigm.title, onBack = onBack)
        Readable(Modifier.weight(1f)) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                Text(paradigm.lemma, style = MaterialTheme.typography.displaySmall)
                Text(
                    "${paradigm.gloss} · ${paradigm.forms.size} forms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                val mastered = progress.masteredCount(paradigm.forms)
                MasteryBar(mastered, progress.seenCount(paradigm.forms) - mastered, paradigm.forms.size)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    Text(
                        paradigm.note,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                SectionHeader("Practise")
                Row(horizontalArrangement = SpacedBy8) {
                    FilterChip(
                        selected = weakOnly,
                        onClick = { weakOnly = !weakOnly },
                        enabled = weak.isNotEmpty() && weak.size < paradigm.forms.size,
                        label = { Text("Unmastered only (${weak.size})") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                StudyMode.entries.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = SpacedBy8, modifier = Modifier.padding(vertical = 4.dp)) {
                        pair.forEach { mode ->
                            ModeCard(mode, Modifier.weight(1f)) { onStudy(mode, studyForms) }
                        }
                    }
                }

                SectionHeader("Paradigm")
                Row(horizontalArrangement = SpacedBy8) {
                    FilterChip(
                        selected = covered,
                        onClick = { covered = !covered },
                        label = { Text(if (covered) "Covered: tap a cell to reveal" else "Cover forms to self-test") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                ParadigmTable(paradigm, progress, covered)
                Spacer(Modifier.height(8.dp))
                Legend()
            }
        }
    }
}

@Composable
private fun ModeCard(mode: StudyMode, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier.heightIn(min = 96.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(mode.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(mode.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The paradigm as a grid, each cell tinted by how well it is known. */
@Composable
fun ParadigmTable(paradigm: Paradigm, progress: Progress, covered: Boolean) {
    val extra = LocalExtraColors.current
    val revealed = remember(paradigm.id, covered) { mutableStateOf(setOf<String>()) }
    val outline = MaterialTheme.colorScheme.outlineVariant
    val labelWidth = if (paradigm.rowLabels.any { it.length > 6 }) 72.dp else 52.dp
    val compact = paradigm.colLabels.size >= 3

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, outline, RoundedCornerShape(12.dp))
    ) {
        Row(Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(vertical = 8.dp)) {
            Spacer(Modifier.width(labelWidth))
            paradigm.colLabels.forEach {
                Text(
                    it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                )
            }
        }
        paradigm.rowLabels.forEachIndexed { r, label ->
            val groupBreak = paradigm.rowLabels.size == 8 && r == 4
            if (groupBreak) Box(Modifier.fillMaxWidth().height(1.dp).background(outline))
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(labelWidth).padding(start = 12.dp),
                )
                paradigm.colLabels.indices.forEach { c ->
                    val form = paradigm.cell(r, c)
                    Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (form != null) {
                            val tint = when (progress.state(form.id).mastery()) {
                                Mastery.MASTERED -> extra.mastered.copy(alpha = 0.14f)
                                Mastery.LEARNING -> extra.learning.copy(alpha = 0.16f)
                                Mastery.NEW -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            }
                            val hidden = covered && form.id !in revealed.value
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (hidden) MaterialTheme.colorScheme.surfaceContainerHighest else tint)
                                    .clickable(enabled = covered) {
                                        revealed.value = if (form.id in revealed.value) revealed.value - form.id
                                        else revealed.value + form.id
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .semantics { contentDescription = if (hidden) "${form.parse}, hidden" else "${form.parse}: ${form.display}" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (hidden) "?" else form.answers.joinToString("\n"),
                                    fontFamily = GreekFamily,
                                    fontSize = if (compact) 16.sp else 19.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        } else {
                            Text("—", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    val extra = LocalExtraColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot(extra.mastered.copy(alpha = 0.5f), "Mastered")
        LegendDot(extra.learning.copy(alpha = 0.5f), "Learning")
        LegendDot(MaterialTheme.colorScheme.surfaceContainerHighest, "New")
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(12.dp).height(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun ParadigmScreenPreview() {
    GreekParadigmsTheme {
        ParadigmScreen(paradigms.first(), Progress(), {}, { _, _ -> })
    }
}
