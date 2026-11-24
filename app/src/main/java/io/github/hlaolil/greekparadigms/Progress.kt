package io.github.hlaolil.greekparadigms

import kotlin.math.pow

/**
 * Spaced-repetition state for a single form, Leitner style: every correct
 * answer moves the card up a box and roughly doubles its interval; a miss
 * sends it back to box 0 so it comes up again today.
 */
data class CardState(
    val box: Int = 0,
    val due: Long = 0,       // epoch day on which the card is next due
    val reps: Int = 0,       // total times answered
    val correct: Int = 0,    // total times answered correctly
    val lastSeen: Long = -1, // epoch day of the last answer
) {
    val seen: Boolean get() = reps > 0
    val mastered: Boolean get() = box >= MASTERED_BOX

    /**
     * Memory strength: an estimate of the chance of recalling the form today.
     * It is 90% on the day a card falls due and keeps fading after that, so a
     * form left too long shows up as "fading". A card just missed starts at 50%.
     */
    fun strength(today: Long): Double {
        if (!seen) return 0.0
        val elapsed = (today - lastSeen).coerceAtLeast(0).toDouble()
        return if (box == 0) 0.5 * 0.5.pow(elapsed)
        else 0.9.pow(elapsed / INTERVALS[box])
    }

    companion object {
        /** Interval in days after reaching each box. */
        val INTERVALS = listOf(0, 1, 2, 4, 8, 16, 32, 64, 120)
        const val MASTERED_BOX = 4

        /** Below this strength a form counts as fading. */
        const val FADING = 0.8
    }
}

enum class Mastery { NEW, LEARNING, MASTERED }

fun CardState?.mastery(): Mastery = when {
    this == null || !seen -> Mastery.NEW
    mastered -> Mastery.MASTERED
    else -> Mastery.LEARNING
}

object Scheduler {
    fun grade(state: CardState?, correct: Boolean, today: Long): CardState {
        val s = state ?: CardState()
        val box = if (correct) (s.box + 1).coerceAtMost(CardState.INTERVALS.lastIndex) else 0
        return s.copy(
            box = box,
            due = today + CardState.INTERVALS[box],
            reps = s.reps + 1,
            correct = s.correct + if (correct) 1 else 0,
            lastSeen = today,
        )
    }

    fun isDue(state: CardState?, today: Long) = state != null && state.seen && state.due <= today
}

/** User preferences. */
data class Settings(
    val requireAccents: Boolean = false,
    val shuffle: Boolean = true,
    val dailyGoal: Int = 50, // XP per day
    val newPerReview: Int = 10,
)

/**
 * Everything the app remembers between launches. The whole object is
 * immutable; [ProgressStore] persists each new version.
 */
data class Progress(
    val cards: Map<String, CardState> = emptyMap(),
    val studyDays: Set<Long> = emptySet(),
    val answeredToday: Int = 0,
    val answeredDay: Long = -1,
    val settings: Settings = Settings(),
    val xp: Long = 0,
    val xpToday: Int = 0,
    val xpDay: Long = -1,
    /** Streak freezes in hand; one is earned for every 7 days of streak, up to [MAX_FREEZES]. */
    val freezes: Int = 0,
    /** Missed days that a freeze covered, so the streak survived them. */
    val frozenDays: Set<Long> = emptySet(),
    val lessons: Int = 0,
    val perfectLessons: Int = 0,
) {
    fun state(formId: String): CardState? = cards[formId]

    fun record(formId: String, correct: Boolean, today: Long, xpGained: Int = 0): Progress {
        var p = this
        if (today !in studyDays) {
            // Spend freezes on any days missed since the last study day, if that saves the streak.
            val gap = missedDays(today)
            if (gap.isNotEmpty() && gap.size <= freezes) {
                p = p.copy(freezes = freezes - gap.size, frozenDays = frozenDays + gap)
            }
        }
        p = p.copy(
            cards = cards + (formId to Scheduler.grade(cards[formId], correct, today)),
            studyDays = studyDays + today,
            answeredToday = if (answeredDay == today) answeredToday + 1 else 1,
            answeredDay = today,
        ).addXp(xpGained, today)
        if (today !in studyDays) {
            val streak = p.streak(today)
            if (streak > 0 && streak % 7 == 0) p = p.copy(freezes = (p.freezes + 1).coerceAtMost(MAX_FREEZES))
        }
        return p
    }

    fun addXp(amount: Int, today: Long): Progress = if (amount <= 0) this else copy(
        xp = xp + amount,
        xpToday = if (xpDay == today) xpToday + amount else amount,
        xpDay = today,
    )

    /** Called once when a session ends: counts the lesson and pays its completion bonus. */
    fun completeLesson(perfect: Boolean, bonusXp: Int, today: Long): Progress = copy(
        lessons = lessons + 1,
        perfectLessons = perfectLessons + if (perfect) 1 else 0,
    ).addXp(bonusXp, today)

    fun answeredOn(today: Long) = if (answeredDay == today) answeredToday else 0
    fun xpOn(today: Long) = if (xpDay == today) xpToday else 0

    /** Days with no study between the last study day and [today], exclusive, not already frozen. */
    private fun missedDays(today: Long): List<Long> {
        val last = (studyDays + frozenDays).filter { it < today }.maxOrNull() ?: return emptyList()
        return ((last + 1) until today).toList()
    }

    /**
     * Consecutive days of study ending today (or yesterday, if not yet studied today).
     * A day covered by a streak freeze keeps the streak alive without adding to it.
     * Missed days that the freezes in hand will cover (they are spent on the next
     * answer) don't break the streak either.
     */
    fun streak(today: Long): Int {
        val covered = studyDays + frozenDays
        var day = today
        if (today !in studyDays) {
            val last = covered.filter { it < today }.maxOrNull() ?: return 0
            if (today - last - 1 > freezes) return 0
            day = last
        }
        var n = 0
        while (day in covered) {
            if (day in studyDays) n++
            day--
        }
        return n
    }

    /** The longest streak ever. */
    fun bestStreak(): Int {
        var best = 0
        var run = 0
        var prev = Long.MIN_VALUE
        for (d in (studyDays + frozenDays).sorted()) {
            if (d != prev + 1) run = 0
            if (d in studyDays) run++
            best = maxOf(best, run)
            prev = d
        }
        return best
    }

    fun dueForms(today: Long): List<Form> =
        paradigms.flatMap { it.forms }.filter { Scheduler.isDue(cards[it.id], today) }

    fun dueCount(paradigm: Paradigm, today: Long) = paradigm.forms.count { Scheduler.isDue(cards[it.id], today) }

    fun masteredCount(forms: List<Form>) = forms.count { cards[it.id]?.mastered == true }

    fun seenCount(forms: List<Form>) = forms.count { cards[it.id]?.seen == true }

    /** Average memory strength of the forms already studied, or null if none have been. */
    fun strength(forms: List<Form>, today: Long): Double? {
        val seen = forms.mapNotNull { cards[it.id] }.filter { it.seen }
        return if (seen.isEmpty()) null else seen.sumOf { it.strength(today) } / seen.size
    }

    fun fadingForms(today: Long, forms: List<Form> = allForms): List<Form> =
        forms.filter { f -> cards[f.id]?.let { it.seen && it.strength(today) < CardState.FADING } == true }
            .sortedBy { cards[it.id]!!.strength(today) }

    /**
     * A paradigm's level, like a skill level in a language course:
     * 0 not finished, 1 every form seen, 2 and 3 on the way, 4 every form
     * mastered, 5 every form held for a month or more.
     */
    fun level(p: Paradigm): Int {
        val states = p.forms.map { cards[it.id] }
        if (states.any { it == null || !it.seen }) return 0
        val minBox = states.minOf { it!!.box }
        val avgBox = states.sumOf { it!!.box }.toDouble() / states.size
        return when {
            minBox >= 6 -> 5
            minBox >= CardState.MASTERED_BOX -> 4
            avgBox >= 3 -> 3
            avgBox >= 1.5 -> 2
            else -> 1
        }
    }

    /** The next paradigm along the learning path: the first, in course order, with unseen forms. */
    fun nextOnPath(): Paradigm? = paradigms.firstOrNull { p -> p.forms.any { cards[it.id]?.seen != true } }

    val accuracy: Double
        get() {
            val reps = cards.values.sumOf { it.reps }
            return if (reps == 0) 0.0 else cards.values.sumOf { it.correct }.toDouble() / reps
        }

    /**
     * Today's review queue: everything due, weakest memory first, then a handful
     * of new forms from paradigms already started, so the review grows the
     * learner's range too.
     */
    fun reviewQueue(today: Long): List<Form> {
        val due = dueForms(today).sortedBy { cards[it.id]?.strength(today) ?: 0.0 }
        val started = paradigms.filter { p -> p.forms.any { cards[it.id]?.seen == true } }
        val fresh = started.flatMap { p -> p.forms.filter { cards[it.id]?.seen != true } }
            .take(settings.newPerReview)
        return due + fresh
    }

    // --- Serialisation --------------------------------------------------
    // A small line-based format, so no JSON library is needed and the
    // format is easy to read in a backup. Lines added after v2 are optional.

    fun serialize(): String = buildString {
        appendLine("v2")
        appendLine("settings ${settings.requireAccents} ${settings.shuffle} ${settings.dailyGoal} ${settings.newPerReview}")
        appendLine("today $answeredDay $answeredToday")
        appendLine("days ${studyDays.sorted().joinToString(",")}")
        appendLine("xp $xp $xpDay $xpToday")
        appendLine("freeze $freezes ${frozenDays.sorted().joinToString(",")}")
        appendLine("lessons $lessons $perfectLessons")
        cards.forEach { (id, c) ->
            appendLine("card $id ${c.box} ${c.due} ${c.reps} ${c.correct} ${c.lastSeen}")
        }
    }

    companion object {
        const val MAX_FREEZES = 2

        private fun days(s: String?) = s.orEmpty().split(",").filter { it.isNotBlank() }.map { it.toLong() }.toSet()

        fun deserialize(text: String?): Progress {
            if (text.isNullOrBlank()) return Progress()
            var p = Progress()
            val cards = mutableMapOf<String, CardState>()
            text.lineSequence().forEach { line ->
                val f = line.trim().split(" ")
                runCatching {
                    when (f[0]) {
                        "settings" -> p = p.copy(
                            settings = Settings(
                                requireAccents = f[1].toBoolean(),
                                shuffle = f[2].toBoolean(),
                                dailyGoal = f[3].toInt(),
                                newPerReview = f.getOrNull(4)?.toInt() ?: Settings().newPerReview,
                            )
                        )
                        "today" -> p = p.copy(answeredDay = f[1].toLong(), answeredToday = f[2].toInt())
                        "days" -> p = p.copy(studyDays = days(f.getOrNull(1)))
                        "xp" -> p = p.copy(xp = f[1].toLong(), xpDay = f[2].toLong(), xpToday = f[3].toInt())
                        "freeze" -> p = p.copy(freezes = f[1].toInt(), frozenDays = days(f.getOrNull(2)))
                        "lessons" -> p = p.copy(lessons = f[1].toInt(), perfectLessons = f[2].toInt())
                        "card" -> {
                            // v1 saves used positional IDs; translate them to slot-named ones.
                            val id = if (f[1] in formById) f[1] else legacyFormIds[f[1]]
                            if (id != null) {
                                cards[id] = CardState(f[2].toInt(), f[3].toLong(), f[4].toInt(), f[5].toInt(), f[6].toLong())
                            }
                        }
                    }
                }
            }
            return p.copy(cards = cards)
        }
    }
}

val allForms: List<Form> by lazy { paradigms.flatMap { it.forms } }

/** An achievement, unlocked when [unlocked] holds for the learner's progress. */
class Achievement(val id: String, val title: String, val detail: String, val unlocked: (Progress) -> Boolean)

object Achievements {
    private fun masteredAll(p: Progress, paradigmId: String): Boolean =
        paradigmById[paradigmId]?.forms?.all { p.state(it.id)?.mastered == true } == true

    private fun masteredCategory(p: Progress, c: Category): Boolean =
        paradigms.filter { it.category == c }.flatMap { it.forms }.all { p.state(it.id)?.mastered == true }

    val all: List<Achievement> = listOf(
        Achievement("first", "First steps", "Answer your first form") { it.cards.isNotEmpty() },
        Achievement("lesson", "Lesson learned", "Finish a session") { it.lessons >= 1 },
        Achievement("perfect", "Flawless", "Finish a session without a miss") { it.perfectLessons >= 1 },
        Achievement("perfect10", "Precision", "Finish 10 flawless sessions") { it.perfectLessons >= 10 },
        Achievement("streak3", "Kindled", "Reach a 3-day streak") { it.bestStreak() >= 3 },
        Achievement("streak7", "A week in the Word", "Reach a 7-day streak") { it.bestStreak() >= 7 },
        Achievement("streak30", "Faithful", "Reach a 30-day streak") { it.bestStreak() >= 30 },
        Achievement("streak100", "Unwearied", "Reach a 100-day streak") { it.bestStreak() >= 100 },
        Achievement("article", "ὁ, ἡ, τό", "Master the article") { masteredAll(it, "art") },
        Achievement("pronouns", "Who's who", "Master every pronoun paradigm") { masteredCategory(it, Category.PRONOUNS) },
        Achievement("nouns", "Declined", "Master every noun paradigm") { masteredCategory(it, Category.NOUNS) },
        Achievement("indicative", "Indicative", "Master every indicative verb paradigm") { masteredCategory(it, Category.INDICATIVE) },
        Achievement("participles", "Participle wrangler", "Master every participle") { masteredCategory(it, Category.PARTICIPLES) },
        Achievement("m100", "Centurion", "Master 100 forms") { it.masteredCount(allForms) >= 100 },
        Achievement("m300", "Grammarian", "Master 300 forms") { it.masteredCount(allForms) >= 300 },
        Achievement("mall", "Every form", "Master all ${allForms.size} forms") { it.masteredCount(allForms) == allForms.size },
        Achievement("xp1000", "1,000 XP", "Earn 1,000 XP") { it.xp >= 1_000 },
        Achievement("xp10000", "10,000 XP", "Earn 10,000 XP") { it.xp >= 10_000 },
    )

    fun unlocked(p: Progress): Set<String> = all.filter { it.unlocked(p) }.map { it.id }.toSet()
}
