package com.example.greekparadigms

/**
 * The groups that paradigms are listed under on the home screen,
 * in the order a typical first-year grammar introduces them.
 */
enum class Category(val title: String) {
    PRONOUNS("Article & Pronouns"),
    NOUNS("Nouns"),
    ADJECTIVES("Adjectives"),
    INDICATIVE("Verbs · Indicative"),
    NON_INDICATIVE("Verbs · Subjunctive, Imperative, Infinitive"),
    PARTICIPLES("Participles"),
}

/**
 * One cell of a paradigm: a single inflected form in a single grammatical slot.
 *
 * [answers] holds every acceptable spelling, e.g. `["ἐμοῦ", "μου"]`. A trailing
 * movable nu is written `(ν)` and is accepted with or without the ν.
 */
data class Form(
    val id: String,
    val paradigmId: String,
    val row: Int,
    val col: Int,
    val parse: String,
    val answers: List<String>,
) {
    /** The form as it is shown to the learner. */
    val display: String get() = answers.joinToString(" / ")
}

/**
 * A complete paradigm laid out as a table of [rowLabels] × [colLabels].
 * Cells with no form (e.g. the plural of εἷς) are simply absent from [forms].
 */
data class Paradigm(
    val id: String,
    val lemma: String,
    val gloss: String,
    val title: String,
    val category: Category,
    val rowLabels: List<String>,
    val colLabels: List<String>,
    val forms: List<Form>,
    val note: String,
) {
    fun cell(row: Int, col: Int): Form? = forms.firstOrNull { it.row == row && it.col == col }
}

// ---------------------------------------------------------------------------
// Builders. Forms are written as whitespace-separated strings so that the
// data files read like the paradigm charts in a grammar. Within one cell,
// alternatives are separated by "/" and a missing cell is written "-".
// ---------------------------------------------------------------------------

private val CASES = listOf("nominative", "genitive", "dative", "accusative", "vocative")
private val CASE_ABBR = listOf("Nom", "Gen", "Dat", "Acc", "Voc")
private val PERSONS = listOf("1st", "2nd", "3rd")

private fun cells(s: String): List<String> = s.trim().split(Regex("\\s+"))

private fun answersOf(cell: String): List<String>? =
    if (cell == "-") null else cell.split("/").map { it.trim() }.filter { it.isNotEmpty() }

/** General table builder: [grid] has one whitespace-separated string per row. */
fun grid(
    id: String,
    lemma: String,
    gloss: String,
    title: String,
    category: Category,
    note: String,
    rows: List<String>,
    cols: List<String>,
    grid: List<String>,
    parse: (row: Int, col: Int) -> String,
): Paradigm {
    require(grid.size == rows.size) { "$id: expected ${rows.size} rows, got ${grid.size}" }
    val forms = mutableListOf<Form>()
    grid.forEachIndexed { r, line ->
        val c = cells(line)
        require(c.size == cols.size) { "$id row $r: expected ${cols.size} cells, got ${c.size}" }
        c.forEachIndexed { col, cell ->
            answersOf(cell)?.let { forms += Form("$id.$r.$col", id, r, col, parse(r, col), it) }
        }
    }
    return Paradigm(id, lemma, gloss, title, category, rows, cols, forms, note)
}

/** A noun: [sg] and [pl] list nom, gen, dat, acc, voc. */
fun noun(
    id: String, lemma: String, gloss: String, title: String, note: String,
    sg: String, pl: String,
): Paradigm {
    val s = cells(sg)
    val p = cells(pl)
    require(s.size == 5 && p.size == 5) { "$id: nouns need 5 singular and 5 plural forms" }
    return grid(
        id, lemma, gloss, title, Category.NOUNS, note,
        rows = CASE_ABBR, cols = listOf("Singular", "Plural"),
        grid = (0 until 5).map { "${s[it]} ${p[it]}" },
    ) { r, c -> "${CASES[r]} ${if (c == 0) "singular" else "plural"}" }
}

/**
 * A word that inflects for gender: [m], [f] and [n] each list
 * nom, gen, dat, acc singular followed by nom, gen, dat, acc plural.
 * Use [colLabels] to merge columns, e.g. a two-termination adjective.
 */
fun gendered(
    id: String, lemma: String, gloss: String, title: String, category: Category, note: String,
    vararg columns: Pair<String, String>,
): Paradigm {
    val cols = columns.map { cells(it.second) }
    cols.forEach { require(it.size == 8) { "$id: gendered columns need 8 forms" } }
    val rows = (0 until 8).map { "${CASE_ABBR[it % 4]} ${if (it < 4) "sg" else "pl"}" }
    return grid(
        id, lemma, gloss, title, category, note,
        rows = rows, cols = columns.map { it.first },
        grid = (0 until 8).map { r -> cols.joinToString(" ") { it[r] } },
    ) { r, c ->
        "${columns[c].first.lowercase()} ${CASES[r % 4]} ${if (r < 4) "singular" else "plural"}"
    }
}

/** A finite verb: six forms in the order 1s 2s 3s 1p 2p 3p. */
fun finite(
    id: String, lemma: String, gloss: String, title: String, category: Category, note: String,
    forms: String,
): Paradigm {
    val f = cells(forms)
    require(f.size == 6) { "$id: finite paradigms need 6 forms" }
    return grid(
        id, lemma, gloss, title, category, note,
        rows = PERSONS, cols = listOf("Singular", "Plural"),
        grid = (0 until 3).map { "${f[it]} ${f[it + 3]}" },
    ) { r, c -> "${PERSONS[r]} person ${if (c == 0) "singular" else "plural"}" }
}

/** An imperative: four forms in the order 2s 3s 2p 3p. */
fun imperative(
    id: String, lemma: String, gloss: String, title: String, note: String,
    forms: String,
): Paradigm {
    val f = cells(forms)
    require(f.size == 4) { "$id: imperatives need 4 forms" }
    val persons = listOf("2nd", "3rd")
    return grid(
        id, lemma, gloss, title, Category.NON_INDICATIVE, note,
        rows = persons, cols = listOf("Singular", "Plural"),
        grid = listOf("${f[0]} ${f[2]}", "${f[1]} ${f[3]}"),
    ) { r, c -> "${persons[r]} person ${if (c == 0) "singular" else "plural"}" }
}
