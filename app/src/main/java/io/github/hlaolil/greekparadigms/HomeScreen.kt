package io.github.hlaolil.greekparadigms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hlaolil.greekparadigms.ui.theme.GreekFamily
import io.github.hlaolil.greekparadigms.ui.theme.GreekParadigmsTheme

@Composable
fun HomeScreen(
    progress: Progress,
    today: Long,
    onOpen: (Paradigm) -> Unit,
    onReview: (List<Form>) -> Unit,
    onLearn: (Paradigm) -> Unit,
    onStrengthen: (List<Form>) -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query) { search(query) }

    Column(Modifier.fillMaxSize()) {
        AppBar("Greek Paradigms", actions = {
            TextButton(onClick = onStats) { Text("Progress") }
            TextButton(onClick = onSettings) { Text("Settings") }
        })
        Readable(Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            ) {
                item {
                    TodayCard(progress, today, onReview, onLearn, onStrengthen, onStart = { onOpen(paradigms.first()) })
                }
                item { VerseOfTheDay(progress, today) }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search: λόγος, logos, aorist, participle…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                }
                Category.entries.forEach { category ->
                    val inCategory = filtered.filter { it.category == category }
                    if (inCategory.isNotEmpty()) {
                        item(key = "h-${category.name}") { SectionHeader(category.title) }
                        items(inCategory, key = { it.id }) { p ->
                            ParadigmRow(p, progress, today, onClick = { onOpen(p) })
                        }
                    }
                }
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "No paradigms match “$query”.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayCard(
    progress: Progress,
    today: Long,
    onReview: (List<Form>) -> Unit,
    onLearn: (Paradigm) -> Unit,
    onStrengthen: (List<Form>) -> Unit,
    onStart: () -> Unit,
) {
    val queue = progress.reviewQueue(today)
    val due = progress.dueForms(today).size
    val xp = progress.xpOn(today)
    val goal = progress.settings.dailyGoal
    val streak = progress.streak(today)
    val strength = progress.strength(allForms, today)
    val fading = progress.fadingForms(today)
    val next = progress.nextOnPath()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (progress.freezes > 0) {
                    Badge(
                        "❄️ ${progress.freezes} streak freeze${if (progress.freezes > 1) "s" else ""}",
                        MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Stat("🔥 $streak", "day streak")
                Stat("$xp/$goal", "XP today")
                Stat("$due", "due")
                Stat(strength?.let { percent(it) } ?: "–", "memory")
            }
            Spacer(Modifier.height(12.dp))
            ProgressBar(
                fraction = xp.toFloat() / goal.coerceAtLeast(1),
                color = if (xp >= goal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                track = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
            )
            if (xp >= goal) {
                Text(
                    "Daily goal reached. Well done!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            if (progress.cards.isEmpty()) {
                Text(
                    "Start with the article: it appears in nearly every verse of the New Testament.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Begin with ὁ, ἡ, τό") }
            } else {
                if (queue.isNotEmpty()) {
                    Button(onClick = { onReview(queue) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (due > 0) "Review $due due · ${queue.size - due} new" else "Learn ${queue.size} new forms")
                    }
                } else {
                    Text("All caught up on reviews.", style = MaterialTheme.typography.bodyMedium)
                }
                if (next != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onLearn(next) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Continue path: ${next.lemma} · ${next.title}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (fading.isNotEmpty()) {
                    TextButton(onClick = { onStrengthen(fading.take(20)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Strengthen ${fading.size} fading form${if (fading.size > 1) "s" else ""}")
                    }
                }
            }
        }
    }
}

/**
 * A phrase from the Greek NT using a form the learner has studied, changing daily,
 * so the paradigms are always seen doing their real work.
 */
@Composable
private fun VerseOfTheDay(progress: Progress, today: Long) {
    val nt = LocalNt.current
    if (nt.isEmpty) return
    val pick = remember(today, progress.cards.size > 0) {
        val studied = allForms.filter { progress.state(it.id)?.seen == true && nt.hasExamples(it.id) }
        val pool = studied.ifEmpty { paradigms.first().forms.filter { nt.hasExamples(it.id) } }
        if (pool.isEmpty()) null else pool[Math.floorMod(today.toInt() * 31 + 7, pool.size)].let { f ->
            f to nt.examples(f.id).filter { it.exact }.ifEmpty { nt.examples(f.id) }.let { it[Math.floorMod(today.toInt(), it.size)] }
        }
    } ?: return
    val (form, example) = pick
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "FROM THE GREEK NEW TESTAMENT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(8.dp))
            NtPhrase(example, Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            val p = paradigmById.getValue(form.paradigmId)
            Text(
                "${example.reference} · ${example.word.trimEnd(',', '.', ';', '·', ':')}: ${form.parse} (${p.lemma}${if (example.exact) "" else ", pattern from ${example.lemma}"})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ParadigmRow(p: Paradigm, progress: Progress, today: Long, onClick: () -> Unit) {
    val mastered = progress.masteredCount(p.forms)
    val learning = progress.seenCount(p.forms) - mastered
    val due = progress.dueCount(p, today)
    val level = progress.level(p)
    val occurrences = LocalNt.current.occurrences(p)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.lemma, style = MaterialTheme.typography.titleLarge, fontFamily = GreekFamily)
                    Text(
                        "${p.title} · ${p.gloss}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (due > 0) {
                    Badge("$due due", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                } else if (mastered == p.forms.size) {
                    Badge("mastered", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Spacer(Modifier.height(10.dp))
            MasteryBar(mastered, learning, p.forms.size)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelDots(level)
                Text(
                    if (level > 0) "  Level $level" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (occurrences > 0) {
                    Text(
                        "${timesInNt(occurrences)} in the NT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GreekParadigmsTheme {
        HomeScreen(Progress(), 0, {}, {}, {}, {}, {}, {})
    }
}
