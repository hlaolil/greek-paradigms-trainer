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
import androidx.compose.ui.text.input.ImeAction
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
                item { TodayCard(progress, today, onReview, onStart = { onOpen(paradigms.first()) }) }
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
private fun TodayCard(progress: Progress, today: Long, onReview: (List<Form>) -> Unit, onStart: () -> Unit) {
    val queue = progress.reviewQueue(today)
    val due = progress.dueForms(today).size
    val answered = progress.answeredOn(today)
    val goal = progress.settings.dailyGoal
    val streak = progress.streak(today)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Today", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Stat("$due", "due")
                Stat("$answered/$goal", "answered")
                Stat("$streak", "day streak")
            }
            Spacer(Modifier.height(12.dp))
            ProgressBar(
                fraction = answered.toFloat() / goal.coerceAtLeast(1),
                track = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(16.dp))
            when {
                queue.isNotEmpty() -> Button(onClick = { onReview(queue) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (due > 0) "Review $due due · ${queue.size - due} new" else "Learn ${queue.size} new forms")
                }
                progress.cards.isEmpty() -> {
                    Text(
                        "Start with the article: it appears in nearly every verse of the New Testament.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Begin with ὁ, ἡ, τό") }
                }
                else -> Text(
                    "All caught up. Open a new paradigm below to keep growing.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ParadigmRow(p: Paradigm, progress: Progress, today: Long, onClick: () -> Unit) {
    val mastered = progress.masteredCount(p.forms)
    val learning = progress.seenCount(p.forms) - mastered
    val due = progress.dueCount(p, today)
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GreekParadigmsTheme {
        HomeScreen(Progress(), 0, {}, {}, {}, {})
    }
}
