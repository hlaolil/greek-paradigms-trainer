package com.example.greekparadigms

import java.text.Normalizer

/** Text utilities for polytonic Greek: answer checking and on-screen keyboard composition. */
object GreekText {

    // Combining diacritics used in polytonic Greek.
    const val SMOOTH = '̓'
    const val ROUGH = '̔'
    const val ACUTE = '́'
    const val GRAVE = '̀'
    const val CIRCUMFLEX = '͂'
    const val IOTA_SUB = 'ͅ'
    const val DIAERESIS = '̈'

    private val BREATHINGS = setOf(SMOOTH, ROUGH)
    private val ACCENTS = setOf(ACUTE, GRAVE, CIRCUMFLEX)
    private val ALL_MARKS = setOf(SMOOTH, ROUGH, ACUTE, GRAVE, CIRCUMFLEX, IOTA_SUB, DIAERESIS)

    /** Canonical display order of marks on one letter: breathing/diaeresis, then accent, then iota subscript. */
    private fun markRank(c: Char) = when (c) {
        SMOOTH, ROUGH, DIAERESIS -> 0
        ACUTE, GRAVE, CIRCUMFLEX -> 1
        IOTA_SUB -> 2
        else -> 3
    }

    enum class Verdict { CORRECT, CORRECT_IGNORING_ACCENTS, WRONG }

    /** Expands `λύουσι(ν)` to both `λύουσι` and `λύουσιν`. */
    fun expand(answer: String): List<String> {
        val open = answer.indexOf('(')
        val close = answer.indexOf(')')
        if (open < 0 || close < open) return listOf(answer)
        val without = answer.removeRange(open, close + 1)
        val with = answer.removeRange(close, close + 1).removeRange(open, open + 1)
        return (expand(without) + expand(with)).distinct()
    }

    /**
     * Normalises for strict comparison: NFC, lower case, final sigma, and
     * grave treated as acute (a word quoted on its own takes an acute).
     */
    fun strict(s: String): String {
        val d = Normalizer.normalize(s.trim().lowercase(), Normalizer.Form.NFD)
            .replace(GRAVE, ACUTE)
            .replace('’', SMOOTH) // typographic apostrophe used as smooth breathing
            .replace(Regex("\\s+"), " ")
        return finalSigma(Normalizer.normalize(d, Normalizer.Form.NFC))
    }

    /** Normalises for lenient comparison: all breathings, accents and iota subscripts removed. */
    fun bare(s: String): String {
        val d = Normalizer.normalize(strict(s), Normalizer.Form.NFD)
        return d.filter { it !in ALL_MARKS && Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
    }

    /** Converts a σ at the end of each word to ς, and a ς mid-word to σ. */
    fun finalSigma(s: String): String {
        val sb = StringBuilder(s)
        for (i in sb.indices) {
            if (sb[i] == 'σ' || sb[i] == 'ς') {
                val atEnd = i == sb.lastIndex || !isGreekLetterOrMark(sb[i + 1])
                sb.setCharAt(i, if (atEnd) 'ς' else 'σ')
            }
        }
        return sb.toString()
    }

    private fun isGreekLetterOrMark(c: Char) =
        Character.isLetter(c) || Character.getType(c) == Character.NON_SPACING_MARK.toInt()

    /** Checks a typed [input] against all acceptable [answers]. */
    fun check(input: String, answers: List<String>, requireAccents: Boolean): Verdict {
        if (input.isBlank()) return Verdict.WRONG
        val all = answers.flatMap { expand(it) }
        val s = strict(input)
        if (all.any { strict(it) == s }) return Verdict.CORRECT
        val b = bare(input)
        if (all.any { bare(it) == b }) {
            return if (requireAccents) Verdict.WRONG else Verdict.CORRECT_IGNORING_ACCENTS
        }
        return Verdict.WRONG
    }

    /** True if two displayed forms share any acceptable spelling (used to accept homonymous slots). */
    fun sameForm(a: List<String>, b: List<String>): Boolean {
        val sa = a.flatMap { expand(it) }.map { strict(it) }.toSet()
        return b.flatMap { expand(it) }.any { strict(it) in sa }
    }

    /**
     * Toggles a diacritic on the last letter of [text], as an on-screen keyboard key does.
     * Breathings replace each other, accents replace each other, pressing the same mark
     * again removes it, and the result is recomposed to precomposed (NFC) characters.
     */
    fun applyMark(text: String, mark: Char): String {
        if (text.isEmpty()) return text
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        // Find the start of the last base letter.
        var base = nfd.length - 1
        while (base > 0 && nfd[base] in ALL_MARKS) base--
        if (!Character.isLetter(nfd[base])) return text
        val head = nfd.substring(0, base)
        val letter = nfd[base]
        val marks = nfd.substring(base + 1).toMutableList()
        if (mark in marks) {
            marks.remove(mark)
        } else {
            when (mark) {
                in BREATHINGS -> marks.removeAll { it in BREATHINGS || it == DIAERESIS }
                in ACCENTS -> marks.removeAll { it in ACCENTS }
                DIAERESIS -> marks.removeAll { it in BREATHINGS }
            }
            marks.add(mark)
        }
        marks.sortBy { markRank(it) }
        val rebuilt = head + letter + marks.joinToString("")
        return Normalizer.normalize(rebuilt, Normalizer.Form.NFC)
    }

    /** Removes the last letter together with its diacritics. */
    fun backspace(text: String): String {
        if (text.isEmpty()) return text
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        var i = nfd.length - 1
        while (i > 0 && nfd[i] in ALL_MARKS) i--
        return Normalizer.normalize(nfd.substring(0, i), Normalizer.Form.NFC)
    }

    /**
     * Converts Beta Code-style Latin typing to Greek, so a hardware or phone keyboard
     * can be used: `lo/gos` → `λόγος`, `a)rxh/` → `ἀρχή`, `h(me/ra|` → `ἡμέρᾳ`.
     */
    fun fromBetaCode(input: String): String {
        val out = StringBuilder()
        for (ch in input) {
            val mark = BETA_MARKS[ch]
            if (mark != null && out.isNotEmpty()) {
                val applied = applyMark(out.toString(), mark)
                out.setLength(0)
                out.append(applied)
                continue
            }
            out.append(BETA_LETTERS[ch.lowercaseChar()] ?: ch)
        }
        return finalSigma(out.toString())
    }

    private val BETA_LETTERS = mapOf(
        'a' to 'α', 'b' to 'β', 'g' to 'γ', 'd' to 'δ', 'e' to 'ε', 'z' to 'ζ', 'h' to 'η',
        'q' to 'θ', 'i' to 'ι', 'k' to 'κ', 'l' to 'λ', 'm' to 'μ', 'n' to 'ν', 'c' to 'ξ',
        'o' to 'ο', 'p' to 'π', 'r' to 'ρ', 's' to 'σ', 't' to 'τ', 'u' to 'υ', 'f' to 'φ',
        'x' to 'χ', 'y' to 'ψ', 'w' to 'ω', 'v' to 'ϝ',
    )

    private val BETA_MARKS = mapOf(
        ')' to SMOOTH, '(' to ROUGH, '/' to ACUTE, '\\' to GRAVE,
        '=' to CIRCUMFLEX, '|' to IOTA_SUB, '+' to DIAERESIS,
    )

    /** Rough Latin transliteration, used for search. */
    fun transliterate(s: String): String {
        val map = mapOf(
            'α' to "a", 'β' to "b", 'γ' to "g", 'δ' to "d", 'ε' to "e", 'ζ' to "z", 'η' to "e",
            'θ' to "th", 'ι' to "i", 'κ' to "k", 'λ' to "l", 'μ' to "m", 'ν' to "n", 'ξ' to "x",
            'ο' to "o", 'π' to "p", 'ρ' to "r", 'σ' to "s", 'ς' to "s", 'τ' to "t", 'υ' to "u",
            'φ' to "ph", 'χ' to "ch", 'ψ' to "ps", 'ω' to "o",
        )
        return bare(s).map { map[it] ?: it.toString() }.joinToString("")
    }
}
