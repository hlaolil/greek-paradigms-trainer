package io.github.hlaolil.greekparadigms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hlaolil.greekparadigms.ui.theme.GreekCardStyle
import io.github.hlaolil.greekparadigms.ui.theme.GreekFamily
import io.github.hlaolil.greekparadigms.ui.theme.GreekParadigmsTheme
import io.github.hlaolil.greekparadigms.ui.theme.LocalExtraColors
import kotlin.random.Random

@Composable
fun StudyScreen(
    session: Session,
    progress: Progress,
    today: Long,
    newAchievements: List<Achievement>,
    onAnswer: (correct: Boolean, asked: StudyMode) -> Unit,
    onFinish: () -> Unit,
    onRestart: (List<Form>, StudyMode) -> Unit,
    onExit: () -> Unit,
) {
    val nt = LocalNt.current
    Column(Modifier.fillMaxSize()) {
        AppBar("${session.mode.title} · ${session.title}", onBack = onExit)
        val fraction = if (session.queue.isEmpty()) 1f else session.position.toFloat() / session.queue.size
        ProgressBar(fraction, height = 4.dp)

        val form = session.current
        if (form == null) {
            LaunchedEffect(session.rewarded) { onFinish() }
            Results(session, progress, today, newAchievements, onRestart, onExit)
            return@Column
        }

        val key = "${session.position}-${form.id}"
        // Chosen once per card, so it doesn't change when the answer is recorded.
        val asked = remember(key) { session.cardMode(progress.state(form.id), nt.hasExamples(form.id)) }
        val example = remember(key) { nt.exampleFor(form.id, session.seed + session.position) }
        val random = Random(session.seed + session.position)

        StatusRow(session, asked)
        val answer: (Boolean) -> Unit = { correct -> onAnswer(correct, asked) }
        when (asked) {
            StudyMode.FLASHCARD -> FlashcardCard(key, form, example, answer)
            StudyMode.TYPE -> TypeCard(key, form, example, progress.settings.requireAccents, answer)
            StudyMode.CHOOSE -> ChooseFormCard(key, form, example, random, answer)
            StudyMode.PARSE -> ParseCard(key, form, example, random, answer)
            StudyMode.CONTEXT, StudyMode.SMART ->
                if (example != null) ContextCard(key, form, example, random, answer)
                else ParseCard(key, form, null, random, answer)
        }
    }
}

/** Score, the current combo and XP earned so far, and in a smart lesson how this card is asked. */
@Composable
private fun StatusRow(session: Session, asked: StudyMode) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildString {
                append("${session.correct} correct · ${session.answered} of ${session.total}")
                if (session.mode == StudyMode.SMART) append(" · ${asked.title}")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        AnimatedVisibility(session.combo >= 3, enter = fadeIn() + scaleIn()) {
            Badge(
                "🔥 ${session.combo} in a row",
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.width(8.dp))
        Badge("${session.xp} XP", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

/** The NT example for a form, once its answer is showing. */
@Composable
private fun ExampleAfterAnswer(form: Form, example: NtExample?) {
    val nt = LocalNt.current
    Spacer(Modifier.height(16.dp))
    if (example != null) {
        NtExampleCard(example, nt.frequency(form.id))
    } else if (!nt.isEmpty) {
        Text(
            "This form doesn't occur in the New Testament.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The paradigm a form belongs to, shown above every prompt. */
@Composable
private fun Context(form: Form) {
    val p = paradigmById.getValue(form.paradigmId)
    Text(
        "${p.lemma} · ${p.title}",
        style = MaterialTheme.typography.labelLarge,
        fontFamily = GreekFamily,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Prompt(text: String, greek: Boolean = false) {
    Text(
        text,
        style = if (greek) GreekCardStyle else MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    )
}

/** Other slots in the paradigm with the same spelling, e.g. accusative for ἔργον. */
@Composable
private fun AlsoParses(form: Form) {
    val also = Choices.allParses(form).filter { it.id != form.id }.map { it.parse }.distinct()
    if (also.isNotEmpty()) {
        Text(
            "Same form: ${also.joinToString("; ")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
private fun CardColumn(content: @Composable () -> Unit) {
    Readable {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}

// --- Flashcards -------------------------------------------------------------

@Composable
private fun FlashcardCard(key: String, form: Form, example: NtExample?, onAnswer: (Boolean) -> Unit) {
    var revealed by rememberSaveable(key) { mutableStateOf(false) }
    CardColumn {
        Context(form)
        Spacer(Modifier.height(32.dp))
        Prompt(form.parse)
        Spacer(Modifier.height(24.dp))
        if (!revealed) {
            Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Show answer")
            }
        }
        AnimatedVisibility(revealed, enter = fadeIn()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(form.display, style = GreekCardStyle, textAlign = TextAlign.Center)
                AlsoParses(form)
                ExampleAfterAnswer(form, example)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onAnswer(false) },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    ) { Text("Missed it") }
                    Button(
                        onClick = { onAnswer(true) },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    ) { Text("Got it") }
                }
            }
        }
    }
}

// --- Typing -------------------------------------------------------------------

@Composable
private fun TypeCard(key: String, form: Form, example: NtExample?, requireAccents: Boolean, onAnswer: (Boolean) -> Unit) {
    var typed by rememberSaveable(key) { mutableStateOf("") }
    var verdict by rememberSaveable(key) { mutableStateOf<GreekText.Verdict?>(null) }
    var gaveUp by rememberSaveable(key) { mutableStateOf(false) }
    var latin by rememberSaveable { mutableStateOf(false) }
    val submit = { if (verdict == null && typed.isNotBlank()) verdict = GreekText.check(typed, form.answers, requireAccents) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f)) {
            CardColumn {
                Context(form)
                Spacer(Modifier.height(16.dp))
                Prompt(form.parse)
                if (latin && verdict == null) {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = GreekText.fromBetaCode(it) },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = GreekFamily, textAlign = TextAlign.Center),
                        placeholder = { Text("lo/gos → λόγος") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done, autoCorrectEnabled = false,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Beta Code: ) smooth  ( rough  / acute  \\ grave  = circumflex  | iota subscript",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    AnswerBox(typed, verdict)
                }
                Spacer(Modifier.height(16.dp))
                val v = verdict
                if (v != null) {
                    Feedback(v, form)
                    ExampleAfterAnswer(form, example)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        // Lets the learner overrule the checker, but only for an answer they actually gave.
                        if (v == GreekText.Verdict.WRONG && !gaveUp) {
                            OutlinedButton(onClick = { onAnswer(true) }, modifier = Modifier.weight(1f)) {
                                Text("I was right")
                            }
                        }
                        Button(
                            onClick = { onAnswer(v != GreekText.Verdict.WRONG) },
                            modifier = Modifier.weight(1f),
                        ) { Text("Continue") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { latin = !latin }) {
                            Text(if (latin) "Greek keyboard" else "Type in Latin (Beta Code)")
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { gaveUp = true; verdict = GreekText.Verdict.WRONG }) { Text("I don't know") }
                    }
                    if (latin) {
                        Button(onClick = submit, enabled = typed.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                            Text("Check")
                        }
                    }
                }
            }
        }
        if (verdict == null && !latin) {
            GreekKeyboard(typed, onText = { typed = it }, onSubmit = submit, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun AnswerBox(text: String, verdict: GreekText.Verdict?) {
    val extra = LocalExtraColors.current
    val border = when (verdict) {
        null -> MaterialTheme.colorScheme.outline
        GreekText.Verdict.CORRECT -> extra.mastered
        GreekText.Verdict.CORRECT_IGNORING_ACCENTS -> extra.learning
        GreekText.Verdict.WRONG -> MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, border),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
    ) {
        Text(
            text.ifEmpty { "…" },
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = GreekFamily,
            textAlign = TextAlign.Center,
            color = if (text.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun Feedback(verdict: GreekText.Verdict, form: Form) {
    val extra = LocalExtraColors.current
    val (headline, color) = when (verdict) {
        GreekText.Verdict.CORRECT -> "Correct" to extra.mastered
        GreekText.Verdict.CORRECT_IGNORING_ACCENTS -> "Right letters. Check the accents and breathings:" to extra.learning
        GreekText.Verdict.WRONG -> "The answer is" to MaterialTheme.colorScheme.error
    }
    Text(headline, color = color, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    if (verdict != GreekText.Verdict.CORRECT || form.answers.size > 1) {
        Text(form.display, style = GreekCardStyle, textAlign = TextAlign.Center)
    }
    AlsoParses(form)
}

// --- Multiple choice ----------------------------------------------------------

@Composable
private fun ChooseFormCard(key: String, form: Form, example: NtExample?, random: Random, onAnswer: (Boolean) -> Unit) {
    val options = remember(key) { Choices.forms(form, random) }
    var picked by rememberSaveable(key) { mutableStateOf<Int?>(null) }
    val correctIndex = options.indexOf(form.display)
    CardColumn {
        Context(form)
        Spacer(Modifier.height(16.dp))
        Prompt(form.parse)
        options.forEachIndexed { i, option ->
            OptionButton(option, i, picked, correctIndex, greek = true) { if (picked == null) picked = i }
        }
        ContinueAfterPick(picked, correctIndex, form, onAnswer) { ExampleAfterAnswer(form, example) }
    }
}

@Composable
private fun ParseCard(key: String, form: Form, example: NtExample?, random: Random, onAnswer: (Boolean) -> Unit) {
    val options = remember(key) { Choices.parses(form, random) }
    var picked by rememberSaveable(key) { mutableStateOf<Int?>(null) }
    val correctIndex = options.indexOfFirst { it.id == form.id }
    CardColumn {
        Context(form)
        Spacer(Modifier.height(8.dp))
        Prompt(form.answers.first(), greek = true)
        options.forEachIndexed { i, option ->
            OptionButton(option.parse, i, picked, correctIndex, greek = false) { if (picked == null) picked = i }
        }
        ContinueAfterPick(picked, correctIndex, form, onAnswer) { ExampleAfterAnswer(form, example) }
    }
}

/**
 * Parsing in context: a real NT phrase with one word picked out. The options give
 * tense and voice as well as person, number and case, so the verse has to be read.
 */
@Composable
private fun ContextCard(key: String, form: Form, example: NtExample, random: Random, onAnswer: (Boolean) -> Unit) {
    val options = remember(key) { Choices.contextParses(form, random) }
    var picked by rememberSaveable(key) { mutableStateOf<Int?>(null) }
    val correctIndex = options.indexOfFirst { it.id == form.id }
    val mixed = options.any { it.paradigmId != form.paradigmId }
    val nt = LocalNt.current
    CardColumn {
        Text(
            "Parse the highlighted word",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        NtPhrase(example, Modifier.fillMaxWidth(), fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            example.reference + if (example.exact) "" else " · ${example.lemma}, which follows the pattern of λύω",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        options.forEachIndexed { i, option ->
            OptionButton(Choices.label(option, mixed), i, picked, correctIndex, greek = false) { if (picked == null) picked = i }
        }
        ContinueAfterPick(picked, correctIndex, form, onAnswer) {
            val p = paradigmById.getValue(form.paradigmId)
            Spacer(Modifier.height(8.dp))
            Text(
                "${example.word.trimEnd(',', '.', ';', '·', ':')} = ${p.title.lowercase()}, ${form.parse}" +
                    if (example.exact) "" else " (like ${form.display})",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            val freq = nt.frequency(form.id)
            if (freq.exact + freq.pattern > 0) {
                Text(
                    if (freq.exact > 0) "${form.display} occurs ${timesInNt(freq.exact)} in the NT"
                    else "This pattern occurs ${timesInNt(freq.pattern)} in the NT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OptionButton(text: String, index: Int, picked: Int?, correctIndex: Int, greek: Boolean, onClick: () -> Unit) {
    val extra = LocalExtraColors.current
    val state = when {
        picked == null -> null
        index == correctIndex -> extra.mastered
        index == picked -> MaterialTheme.colorScheme.error
        else -> null
    }
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(if (state != null) 2.dp else 1.dp, state ?: MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = state?.copy(alpha = 0.12f) ?: Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 4.dp),
    ) {
        Text(
            text,
            fontFamily = if (greek) GreekFamily else null,
            fontSize = if (greek) 22.sp else 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ContinueAfterPick(
    picked: Int?,
    correctIndex: Int,
    form: Form,
    onAnswer: (Boolean) -> Unit,
    after: @Composable () -> Unit = {},
) {
    if (picked == null) return
    val right = picked == correctIndex
    Spacer(Modifier.height(8.dp))
    AlsoParses(form)
    after()
    Spacer(Modifier.height(16.dp))
    Button(onClick = { onAnswer(right) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        Text(if (right) "Correct · Continue" else "Continue")
    }
}

// --- Results ------------------------------------------------------------------

@Composable
private fun Results(
    session: Session,
    progress: Progress,
    today: Long,
    newAchievements: List<Achievement>,
    onRestart: (List<Form>, StudyMode) -> Unit,
    onExit: () -> Unit,
) {
    val pct = if (session.total == 0) 0 else (100 * session.correct / session.total)
    val missed = session.missed
    val all = session.queue.distinct().mapNotNull { formById[it] }
    CardColumn {
        Spacer(Modifier.height(16.dp))
        Text(
            if (session.perfect) "Flawless!" else "Lesson complete",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text("$pct%", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "${session.correct} of ${session.total} right first time",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Stat("+${session.xp + session.completionBonus}", "XP earned")
            Stat("${session.bestCombo}", "best combo")
            Stat("🔥 ${progress.streak(today)}", "day streak")
        }
        Spacer(Modifier.height(8.dp))
        val goal = progress.settings.dailyGoal
        val xpToday = progress.xpOn(today)
        Text(
            if (xpToday >= goal) "Daily goal reached: $xpToday / $goal XP" else "Daily goal: $xpToday / $goal XP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        ProgressBar(xpToday.toFloat() / goal.coerceAtLeast(1), color = MaterialTheme.colorScheme.tertiary)

        newAchievements.forEach { a ->
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Achievement unlocked", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(a.title, style = MaterialTheme.typography.titleLarge)
                    Text(a.detail, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            when {
                pct == 100 -> "These forms will come back in a few days to make sure they stick."
                pct >= 80 -> "Strong work. The misses below will come round again soon."
                pct >= 50 -> "Getting there. Go over the misses once more while they're fresh."
                else -> "A hard set. Look at the paradigm table, then try the misses again."
            },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        if (missed.isNotEmpty()) {
            Button(onClick = { onRestart(missed, session.mode) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Practise the ${missed.size} missed")
            }
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(onClick = { onRestart(all, session.mode) }, modifier = Modifier.fillMaxWidth()) {
            Text("Go again")
        }
        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Done") }

        if (missed.isNotEmpty()) {
            SectionHeader("Missed", Modifier.fillMaxWidth())
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth(),
            ) {
                missed.forEachIndexed { i, f ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(f.parse, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                paradigmById.getValue(f.paradigmId).let { "${it.lemma} · ${it.title}" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(f.display, fontFamily = GreekFamily, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenPreview() {
    GreekParadigmsTheme {
        StudyScreen(
            Session.start("λόγος", StudyMode.FLASHCARD, paradigmById.getValue("logos").forms, shuffle = false),
            Progress(), 0, emptyList(), { _, _ -> }, {}, { _, _ -> }, {},
        )
    }
}
