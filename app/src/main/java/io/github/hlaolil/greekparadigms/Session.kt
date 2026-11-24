package io.github.hlaolil.greekparadigms

import kotlin.random.Random

enum class StudyMode(val title: String, val blurb: String) {
    SMART("Smart lesson", "Each form moves from recognising to parsing to typing as you learn it"),
    FLASHCARD("Flashcards", "Recall the form, reveal it and grade yourself"),
    TYPE("Type it", "Spell the form on a polytonic Greek keyboard"),
    CHOOSE("Pick the form", "Choose the right form from four"),
    PARSE("Parse it", "See a form and identify it"),
    CONTEXT("In context", "Parse the word in a real New Testament verse"),
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
    val xp: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    /** Set once the completion bonus has been paid, so it is paid only once. */
    val rewarded: Boolean = false,
) {
    val current: Form? get() = queue.getOrNull(position)?.let { formById[it] }
    val finished: Boolean get() = position >= queue.size
    val total: Int get() = queue.distinct().size
    val answered: Int get() = firstTry.size
    val correct: Int get() = firstTry.count { it.value }
    val missed: List<Form> get() = firstTry.filter { !it.value }.keys.mapNotNull { formById[it] }

    /** True if this is the first time the current form has been answered in this session. */
    val isFirstAttempt: Boolean get() = current?.let { it.id !in firstTry } ?: false

    val perfect: Boolean get() = finished && total > 0 && correct == total

    /** XP paid when the session ends: more for a flawless run. */
    val completionBonus: Int get() = if (total == 0) 0 else 10 + if (perfect) 10 else 0

    /**
     * How the current card is asked. Fixed modes ask every card the same way. A smart
     * lesson picks by how well the form is known, the way a language course does:
     * meet it on a flashcard, recognise it among others, parse it (in a real verse
     * when there is one), then produce it by typing. A form missed earlier in the
     * session comes back as multiple choice, to rebuild it gently.
     */
    fun cardMode(state: CardState?, hasExample: Boolean): StudyMode {
        val form = current ?: return mode
        return when (mode) {
            StudyMode.CONTEXT -> if (hasExample) StudyMode.CONTEXT else StudyMode.PARSE
            StudyMode.SMART -> when {
                form.id in firstTry -> StudyMode.CHOOSE
                state == null || !state.seen -> StudyMode.FLASHCARD
                state.box <= 1 -> StudyMode.CHOOSE
                state.box == 2 -> if (hasExample) StudyMode.CONTEXT else StudyMode.PARSE
                state.box >= 5 && hasExample && position % 2 == 1 -> StudyMode.CONTEXT
                else -> StudyMode.TYPE
            }
            else -> mode
        }
    }

    /**
     * Records an answer to the current card, asked as [asked]. A first-time correct
     * answer earns XP by difficulty, a corrected retry a little, and every fifth
     * correct answer in a row a combo bonus.
     */
    fun answer(isCorrect: Boolean, asked: StudyMode = mode): Session {
        val form = current ?: return this
        val nextCombo = if (isCorrect) combo + 1 else 0
        val gained = when {
            !isCorrect -> 0
            form.id !in firstTry -> xpFor(asked)
            else -> RETRY_XP
        } + if (isCorrect && nextCombo % COMBO_STEP == 0) COMBO_BONUS else 0
        val first = if (form.id in firstTry) firstTry else firstTry + (form.id to isCorrect)
        var q = queue
        var r = retries
        val used = retries[form.id] ?: 0
        if (!isCorrect && used < MAX_RETRIES) {
            val at = (position + 1 + REINSERT_GAP).coerceAtMost(queue.size)
            q = queue.toMutableList().apply { add(at, form.id) }
            r = retries + (form.id to used + 1)
        }
        return copy(
            queue = q, position = position + 1, firstTry = first, retries = r,
            xp = xp + gained, combo = nextCombo, bestCombo = maxOf(bestCombo, nextCombo),
        )
    }

    /** One tab-separated line, so a session in progress can outlive the app's process. */
    fun serialize(): String = listOf(
        title.replace('\t', ' '),
        mode.name,
        seed.toString(),
        position.toString(),
        queue.joinToString(","),
        firstTry.entries.joinToString(",") { "${it.key}=${if (it.value) 1 else 0}" },
        retries.entries.joinToString(",") { "${it.key}=${it.value}" },
        "$xp,$combo,$bestCombo,${if (rewarded) 1 else 0}",
    ).joinToString("\t")

    companion object {
        const val MAX_RETRIES = 2
        const val REINSERT_GAP = 3
        const val RETRY_XP = 2
        const val COMBO_STEP = 5
        const val COMBO_BONUS = 5

        /** XP for a first-time correct answer, by how hard the question is. */
        fun xpFor(mode: StudyMode): Int = when (mode) {
            StudyMode.FLASHCARD -> 5
            StudyMode.CHOOSE -> 8
            StudyMode.PARSE, StudyMode.CONTEXT, StudyMode.SMART -> 10
            StudyMode.TYPE -> 15
        }

        /** Restores a [serialize]d session, or null if it is corrupt or refers to forms that no longer exist. */
        fun deserialize(text: String): Session? = runCatching {
            val f = text.split("\t")
            fun list(s: String) = s.split(",").filter { it.isNotEmpty() }
            fun pairs(s: String) = list(s).associate { it.substringBefore('=') to it.substringAfter('=') }
            val queue = list(f[4])
            if (queue.any { it !in formById }) return null
            val extra = f.getOrNull(7)?.split(",")?.map { it.toInt() }
            Session(
                title = f[0],
                mode = StudyMode.valueOf(f[1]),
                seed = f[2].toInt(),
                position = f[3].toInt().coerceIn(0, queue.size),
                queue = queue,
                firstTry = pairs(f[5]).mapValues { it.value == "1" },
                retries = pairs(f[6]).mapValues { it.value.toInt() },
                xp = extra?.getOrNull(0) ?: 0,
                combo = extra?.getOrNull(1) ?: 0,
                bestCombo = extra?.getOrNull(2) ?: 0,
                rewarded = extra?.getOrNull(3) == 1,
            )
        }.getOrNull()

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

    /**
     * Options for parsing a word in an NT verse. For verbs and participles the
     * distractors include other tenses and voices of the same verb (λύω, εἰμί), so
     * the learner has to read the whole form, not just its ending. As with [parses],
     * nothing with the same spelling as [form] is offered.
     */
    fun contextParses(form: Form, random: Random): List<Form> {
        val own = paradigmById.getValue(form.paradigmId)
        val verbal = own.category in setOf(Category.INDICATIVE, Category.NON_INDICATIVE, Category.PARTICIPLES)
        val relatives = if (!verbal) emptyList() else paradigms.filter {
            it.id != own.id && it.category == own.category &&
                (it.lemma == own.lemma || own.category == Category.PARTICIPLES)
        }
        fun usable(f: Form) = f.id != form.id && !GreekText.sameForm(f.answers, form.answers)
        // Same person/number (or case) in another tense is the most instructive distractor.
        val otherTense = relatives.flatMap { it.forms }.filter { usable(it) && it.parse == form.parse }.shuffled(random)
        val samePara = own.forms.filter(::usable).distinctBy { it.parse }.shuffled(random)
        val picked = (otherTense.take(2) + samePara).distinctBy { label(it, true) }.take(3)
        return (picked + form).shuffled(random)
    }

    /** "3rd person plural", or with [withTitle] "Aorist passive indicative · 3rd person plural". */
    fun label(form: Form, withTitle: Boolean): String =
        if (!withTitle) form.parse else "${paradigmById.getValue(form.paradigmId).title} · ${form.parse}"

    /** Every parsing of [form]'s spelling within its paradigm, e.g. both nominative and accusative for ἔργον. */
    fun allParses(form: Form): List<Form> =
        paradigmById.getValue(form.paradigmId).forms.filter { GreekText.sameForm(it.answers, form.answers) }
}
