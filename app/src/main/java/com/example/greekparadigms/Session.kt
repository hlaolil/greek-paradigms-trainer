package com.example.greekparadigms

import kotlin.random.Random

enum class StudyMode(val title: String, val blurb: String) {
    FLASHCARD("Flashcards", "Recall the form, reveal it and grade yourself"),
    TYPE("Type it", "Spell the form on a polytonic Greek keyboard"),
    CHOOSE("Pick the form", "Choose the right form from four"),
    PARSE("Parse it", "See a form and identify it"),
}

/**
 * One run through a set of forms. Immutable: each answer returns a new session.
 *
 * A form answered wrongly is put back a few cards later (at most twice), so
 * the learner sees it again before the session ends. Only the first answer to
 * each form counts for the score and for spaced repetition.
 */
data class Session(
    val title: String,
    val mode: StudyMode,
    val queue: List<String>,
    val position: Int = 0,
    val firstTry: Map<String, Boolean> = emptyMap(),
    val retries: Map<String, Int> = emptyMap(),
    val seed: Int = 0,
) {
    val current: Form? get() = queue.getOrNull(position)?.let { formById[it] }
    val finished: Boolean get() = position >= queue.size
    val total: Int get() = queue.distinct().size
    val answered: Int get() = firstTry.size
    val correct: Int get() = firstTry.count { it.value }
    val missed: List<Form> get() = firstTry.filter { !it.value }.keys.mapNotNull { formById[it] }

    /** True if this is the first time the current form has been answered in this session. */
    val isFirstAttempt: Boolean get() = current?.let { it.id !in firstTry } ?: false

    fun answer(isCorrect: Boolean): Session {
        val form = current ?: return this
        val first = if (form.id in firstTry) firstTry else firstTry + (form.id to isCorrect)
        var q = queue
        var r = retries
        val used = retries[form.id] ?: 0
        if (!isCorrect && used < MAX_RETRIES) {
            val at = (position + 1 + REINSERT_GAP).coerceAtMost(queue.size)
            q = queue.toMutableList().apply { add(at, form.id) }
            r = retries + (form.id to used + 1)
        }
        return copy(queue = q, position = position + 1, firstTry = first, retries = r)
    }

    companion object {
        const val MAX_RETRIES = 2
        const val REINSERT_GAP = 3

        fun start(title: String, mode: StudyMode, forms: List<Form>, shuffle: Boolean, seed: Int = Random.nextInt()): Session {
            val ids = forms.map { it.id }
            return Session(title, mode, if (shuffle) ids.shuffled(Random(seed)) else ids, seed = seed)
        }
    }
}

object Choices {
    /** Up to four distinct forms, one of them [form]'s; returned as display strings. */
    fun forms(form: Form, random: Random): List<String> {
        val own = paradigmById.getValue(form.paradigmId)
        val pool = (own.forms + paradigms.filter { it.category == own.category && it.id != own.id }
            .flatMap { it.forms }.shuffled(random))
        val distractors = mutableListOf<Form>()
        for (f in pool.filter { it.paradigmId == own.id }.shuffled(random) + pool.filter { it.paradigmId != own.id }) {
            if (distractors.size == 3) break
            if (GreekText.sameForm(f.answers, form.answers)) continue
            if (distractors.any { GreekText.sameForm(it.answers, f.answers) }) continue
            distractors += f
        }
        return (distractors.map { it.display } + form.display).shuffled(random)
    }

    /**
     * Up to four parsings from the same paradigm. Slots that share [form]'s
     * spelling (e.g. τῶν in every gender) are left out so that exactly one
     * option is right.
     */
    fun parses(form: Form, random: Random): List<Form> {
        val own = paradigmById.getValue(form.paradigmId)
        val others = own.forms.filter { it.id != form.id && !GreekText.sameForm(it.answers, form.answers) }
            .distinctBy { it.parse }
            .shuffled(random)
            .take(3)
        return (others + form).shuffled(random)
    }

    /** Every parsing of [form]'s spelling within its paradigm, e.g. both nominative and accusative for ἔργον. */
    fun allParses(form: Form): List<Form> =
        paradigmById.getValue(form.paradigmId).forms.filter { GreekText.sameForm(it.answers, form.answers) }
}
