package io.github.hlaolil.greekparadigms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import io.github.hlaolil.greekparadigms.ui.theme.LocalExtraColors
import kotlin.math.roundToInt

@Composable
fun StatsScreen(progress: Progress, today: Long, onBack: () -> Unit, onStrengthen: (List<Form>) -> Unit = {}) {
    val all = paradigms.flatMap { it.forms }
    val mastered = progress.masteredCount(all)
    val seen = progress.seenCount(all)
    val reps = progress.cards.values.sumOf { it.reps }

    Column(Modifier.fillMaxSize()) {
        AppBar("Progress", onBack = onBack)
        Readable(Modifier.weight(1f)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        Stat("$mastered", "mastered")
                        Stat("$seen", "seen")
                        Stat("${all.size}", "total forms")
                    }
                    Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        Stat("🔥 ${progress.streak(today)}", "day streak")
                        Stat("${progress.bestStreak()}", "best streak")
                        Stat("${(progress.accuracy * 100).roundToInt()}%", "accuracy")
                    }
                    Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        Stat("%,d".format(progress.xp), "total XP")
                        Stat("$reps", "answers")
                        Stat(progress.strength(all, today)?.let { percent(it) } ?: "–", "memory")
                    }
                }

                SectionHeader("Last 16 weeks")
                ActivityGrid(progress.studyDays, progress.frozenDays, today)
                if (progress.frozenDays.isNotEmpty() || progress.freezes > 0) {
                    Text(
                        "❄️ Pale squares are days a streak freeze covered. You have ${progress.freezes} in hand; " +
                            "you earn one for every 7 days of streak (up to ${Progress.MAX_FREEZES}).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                val fading = progress.fadingForms(today)
                SectionHeader("Memory")
                Text(
                    "Memory strength estimates how likely you are to recall each form today. " +
                        "It is 90% when a form falls due and fades the longer it waits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (fading.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        fading.take(5).forEachIndexed { i, f ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            val st = progress.state(f.id)!!.strength(today)
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(f.display, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${paradigmById.getValue(f.paradigmId).lemma} · ${f.parse}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(percent(st), style = MaterialTheme.typography.labelLarge, color = strengthColor(st))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onStrengthen(fading.take(20)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Strengthen ${minOf(20, fading.size)} fading forms")
                    }
                } else if (seen > 0) {
                    Text(
                        "Nothing is fading. Keep up the daily reviews.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalExtraColors.current.mastered,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                SectionHeader("By topic")
                Category.entries.forEach { c ->
                    val forms = paradigms.filter { it.category == c }.flatMap { it.forms }
                    val m = progress.masteredCount(forms)
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row {
                            Text(c.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            progress.strength(forms, today)?.let {
                                Text(
                                    "memory ${percent(it)} · ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = strengthColor(it),
                                )
                            }
                            Text(
                                "$m / ${forms.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        MasteryBar(m, progress.seenCount(forms) - m, forms.size)
                    }
                }

                val unlocked = Achievements.unlocked(progress)
                SectionHeader("Achievements · ${unlocked.size} of ${Achievements.all.size}")
                Achievements.all.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = SpacedBy8, modifier = Modifier.padding(vertical = 4.dp)) {
                        pair.forEach { a -> AchievementTile(a, a.id in unlocked, Modifier.weight(1f)) }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                val hardest = progress.cards
                    .filter { it.value.reps >= 2 }
                    .entries
                    .sortedBy { it.value.correct.toDouble() / it.value.reps }
                    .take(8)
                    .mapNotNull { e -> formById[e.key]?.let { it to e.value } }
                    .filter { it.second.correct < it.second.reps }
                if (hardest.isNotEmpty()) {
                    SectionHeader("Trickiest forms")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        hardest.forEachIndexed { i, (f, s) ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(f.display, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "${paradigmById.getValue(f.paradigmId).lemma} · ${f.parse}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text("${s.correct}/${s.reps}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementTile(a: Achievement, unlocked: Boolean, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = modifier.semantics { contentDescription = "${a.title}: ${a.detail}, ${if (unlocked) "unlocked" else "locked"}" },
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                (if (unlocked) "🏆 " else "🔒 ") + a.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(a.detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One square per day, oldest at top-left, a column per week. */
@Composable
private fun ActivityGrid(days: Set<Long>, frozen: Set<Long>, today: Long) {
    val weeks = 16
    // Align so the last column ends today.
    val start = today - (weeks * 7 - 1)
    val on = LocalExtraColors.current.mastered
    val off = MaterialTheme.colorScheme.surfaceContainerHighest
    val studied = (start..today).count { it in days }
    Row(
        Modifier.fillMaxWidth().semantics { contentDescription = "Studied on $studied of the last ${weeks * 7} days" },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(weeks) { w ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(7) { d ->
                    val day = start + w * 7 + d
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when (day) {
                                    in days -> on
                                    in frozen -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    else -> off
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: Settings,
    onChange: (Settings) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        AppBar("Settings", onBack = onBack)
        Readable(Modifier.weight(1f)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
                ToggleRow(
                    "Require accents and breathings",
                    "When typing, an answer with the right letters but wrong diacritics counts as wrong.",
                    settings.requireAccents,
                ) { onChange(settings.copy(requireAccents = it)) }
                ToggleRow(
                    "Shuffle cards",
                    "Learn the forms, not the order they come in.",
                    settings.shuffle,
                ) { onChange(settings.copy(shuffle = it)) }

                SectionHeader("Daily goal: ${settings.dailyGoal} XP")
                Slider(
                    value = settings.dailyGoal.toFloat(),
                    onValueChange = { onChange(settings.copy(dailyGoal = (it / 10).roundToInt() * 10)) },
                    valueRange = 10f..200f,
                    steps = 18,
                )
                Text(
                    "Casual 20 · Regular 50 · Serious 100 · Intense 150. Harder questions earn more: " +
                        "typing 15 XP, parsing 10, choosing 8, flashcards 5, plus a bonus for every 5 in a row.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SectionHeader("New forms per review: ${settings.newPerReview}")
                Slider(
                    value = settings.newPerReview.toFloat(),
                    onValueChange = { onChange(settings.copy(newPerReview = it.roundToInt())) },
                    valueRange = 0f..30f,
                    steps = 5,
                )
                Text(
                    "Today's review adds this many unseen forms from paradigms you've already started.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SectionHeader("Data")
                OutlinedButton(onClick = { confirmReset = true }) { Text("Reset all progress") }

                SectionHeader("About")
                Text(
                    "Spaced repetition: each correct answer roughly doubles the time until a form comes back " +
                        "(1, 2, 4, 8, 16… days); a miss brings it back today. A form counts as mastered once " +
                        "it is scheduled at least 8 days out.\n\n" +
                        "Greek text: SBL Greek New Testament, © 2010 Society of Biblical Literature and Logos Bible Software, CC BY 4.0. " +
                        "Morphology: MorphGNT, CC BY-SA 3.0.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset all progress?") },
            text = { Text("This clears every review, your streak and your history. Settings are kept.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onReset() }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ToggleRow(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
