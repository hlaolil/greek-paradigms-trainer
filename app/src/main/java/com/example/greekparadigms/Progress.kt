package com.example.greekparadigms

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

    companion object {
        /** Interval in days after reaching each box. */
        val INTERVALS = listOf(0, 1, 2, 4, 8, 16, 32, 64, 120)
        const val MASTERED_BOX = 4
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
    val dailyGoal: Int = 50,
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
) {
    fun state(formId: String): CardState? = cards[formId]

    fun record(formId: String, correct: Boolean, today: Long): Progress = copy(
        cards = cards + (formId to Scheduler.grade(cards[formId], correct, today)),
        studyDays = studyDays + today,
        answeredToday = if (answeredDay == today) answeredToday + 1 else 1,
        answeredDay = today,
    )

    fun answeredOn(today: Long) = if (answeredDay == today) answeredToday else 0

    /** Consecutive days of study ending today (or yesterday, if not yet studied today). */
    fun streak(today: Long): Int {
        var day = if (today in studyDays) today else today - 1
        var n = 0
        while (day in studyDays) { n++; day-- }
        return n
    }

    fun dueForms(today: Long): List<Form> =
        paradigms.flatMap { it.forms }.filter { Scheduler.isDue(cards[it.id], today) }

    fun dueCount(paradigm: Paradigm, today: Long) = paradigm.forms.count { Scheduler.isDue(cards[it.id], today) }

    fun masteredCount(forms: List<Form>) = forms.count { cards[it.id]?.mastered == true }

    fun seenCount(forms: List<Form>) = forms.count { cards[it.id]?.seen == true }

    val accuracy: Double
        get() {
            val reps = cards.values.sumOf { it.reps }
            return if (reps == 0) 0.0 else cards.values.sumOf { it.correct }.toDouble() / reps
        }

    /**
     * Today's review queue: everything due, then a handful of new forms from
     * paradigms already started, so the review grows the learner's range too.
     */
    fun reviewQueue(today: Long): List<Form> {
        val due = dueForms(today).sortedBy { cards[it.id]?.due ?: 0 }
        val started = paradigms.filter { p -> p.forms.any { cards[it.id]?.seen == true } }
        val fresh = started.flatMap { p -> p.forms.filter { cards[it.id]?.seen != true } }
            .take(settings.newPerReview)
        return due + fresh
    }

    // --- Serialisation --------------------------------------------------
    // A small line-based format, so no JSON library is needed and the
    // format is easy to read in a backup.

    fun serialize(): String = buildString {
        appendLine("v1")
        appendLine("settings ${settings.requireAccents} ${settings.shuffle} ${settings.dailyGoal} ${settings.newPerReview}")
        appendLine("today $answeredDay $answeredToday")
        appendLine("days ${studyDays.sorted().joinToString(",")}")
        cards.forEach { (id, c) ->
            appendLine("card $id ${c.box} ${c.due} ${c.reps} ${c.correct} ${c.lastSeen}")
        }
    }

    companion object {
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
                        "days" -> p = p.copy(
                            studyDays = f.getOrNull(1).orEmpty().split(",").filter { it.isNotBlank() }
                                .map { it.toLong() }.toSet()
                        )
                        "card" -> if (f[1] in formById) {
                            cards[f[1]] = CardState(f[2].toInt(), f[3].toLong(), f[4].toInt(), f[5].toInt(), f[6].toLong())
                        }
                    }
                }
            }
            return p.copy(cards = cards)
        }
    }
}
