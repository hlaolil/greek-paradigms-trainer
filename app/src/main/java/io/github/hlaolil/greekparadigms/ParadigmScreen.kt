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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    today: Long,
    onBack: () -> Unit,
    onStudy: (StudyMode, List<Form>) -> Unit,
) {
    val nt = LocalNt.current
    var details by rememberSaveable { mutableStateOf<String?>(null) }
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
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val level = progress.level(paradigm)
                    LevelDots(level)
                    Text(
                        "  Level $level",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    progress.strength(paradigm.forms, today)?.let {
                        Text(
                            "Memory ${percent(it)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = strengthColor(it),
                        )
                    }
                }
                val occurrences = nt.occurrences(paradigm)
                if (occurrences > 0) {
                    Text(
                        "These forms occur ${timesInNt(occurrences)} in the Greek New Testament.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

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
                val modes = StudyMode.entries.filter { it != StudyMode.CONTEXT || paradigm.forms.any { f -> nt.hasExamples(f.id) } }
                modes.chunked(2).forEach { pair ->
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
                ParadigmTable(paradigm, progress, covered, onDetails = { details = it.id })
                Spacer(Modifier.height(8.dp))
                Legend()
                if (!covered) {
                    Text(
                        "Tap a form to see it in the New Testament.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                val common = paradigm.forms
                    .filter { nt.frequency(it.id).exact > 0 }
                    .sortedByDescending { nt.frequency(it.id).exact }
                    .distinctBy { it.display }
                    .take(3)
                if (common.isNotEmpty()) {
                    SectionHeader("Most common in the New Testament")
                    common.forEach { f ->
                        val ex = nt.examples(f.id).firstOrNull { it.exact }
                        Card(
                            onClick = { details = f.id },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(f.display, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                    Text(
                                        timesInNt(nt.frequency(f.id).exact),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                                Text(
                                    f.parse,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (ex != null) {
                                    Spacer(Modifier.height(6.dp))
                                    NtPhrase(ex, Modifier.fillMaxWidth(), fontSize = 17.sp)
                                    Text(
                                        ex.reference,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    details?.let { id -> formById[id]?.let { FormDetails(it, progress, today, onDismiss = { details = null }) } }
}

/** A form's parsing, memory strength, NT frequency and every NT example available. */
@Composable
private fun FormDetails(form: Form, progress: Progress, today: Long, onDismiss: () -> Unit) {
    val nt = LocalNt.current
    val freq = nt.frequency(form.id)
    val examples = nt.examples(form.id)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(form.display, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(form.parse, style = MaterialTheme.typography.bodyLarge)
                val also = Choices.allParses(form).filter { it.id != form.id }.map { it.parse }.distinct()
                if (also.isNotEmpty()) {
                    Text(
                        "Also: ${also.joinToString("; ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                progress.state(form.id)?.takeIf { it.seen }?.let { st ->
                    val strength = st.strength(today)
                    Text(
                        "Memory ${percent(strength)} · answered ${st.correct}/${st.reps} right · next review " +
                            if (st.due <= today) "today" else "in ${st.due - today} day${if (st.due - today > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = strengthColor(strength),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    when {
                        freq.exact > 0 -> "Occurs ${timesInNt(freq.exact)} in the Greek New Testament."
                        freq.pattern > 0 -> "This exact word isn't in the NT, but the pattern occurs ${timesInNt(freq.pattern)} in other verbs."
                        nt.isEmpty -> ""
                        else -> "This form doesn't occur in the Greek New Testament."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                examples.forEach { ex ->
                    Spacer(Modifier.height(10.dp))
                    NtPhrase(ex, Modifier.fillMaxWidth(), fontSize = 18.sp)
                    Text(
                        ex.reference + if (ex.exact) "" else " · ${ex.lemma}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    )
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
fun ParadigmTable(paradigm: Paradigm, progress: Progress, covered: Boolean, onDetails: (Form) -> Unit = {}) {
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
                                    .clickable {
                                        if (!covered) onDetails(form)
                                        else revealed.value = if (form.id in revealed.value) revealed.value - form.id
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
        ParadigmScreen(paradigms.first(), Progress(), 0, {}, { _, _ -> })
    }
}
