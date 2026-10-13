package com.example.greekparadigms



data class Form( //data class to hold a form
    val id: Int = 0,
    val paradigm: String,
    val slot: String,
    val form: String
)

//λόγος (2nd decl.) — nominative singular  (10 forms)
val logos = listOf(
    Form(1,  "λόγος (2nd decl.)", "nominative singular", "λόγος"),
    Form(2,  "λόγος (2nd decl.)", "genitive singular",   "λόγου"),
    Form(3,  "λόγος (2nd decl.)", "dative singular",     "λόγῳ"),
    Form(4,  "λόγος (2nd decl.)", "accusative singular", "λόγον"),
    Form(5,  "λόγος (2nd decl.)", "vocative singular",   "λόγε"),
    Form(6,  "λόγος (2nd decl.)", "nominative plural",   "λόγοι"),
    Form(7,  "λόγος (2nd decl.)", "genitive plural",     "λόγων"),
    Form(8,  "λόγος (2nd decl.)", "dative plural",       "λόγοις"),
    Form(9,  "λόγος (2nd decl.)", "accusative plural",   "λόγους"),
    Form(10, "λόγος (2nd decl.)", "vocative plural",     "λόγοι"),
)

// ὁ, ἡ, τό — definite article  (24 forms)
val article = listOf(
    Form(0, "ὁ, ἡ, τό — definite article", "masculine nominative singular", "ὁ"),
    Form(1, "ὁ, ἡ, τό — definite article", "masculine genitive singular", "τοῦ"),
    Form(2, "ὁ, ἡ, τό — definite article", "masculine dative singular", "τῷ"),
    Form(3, "ὁ, ἡ, τό — definite article", "masculine accusative singular", "τόν"),
    Form(4, "ὁ, ἡ, τό — definite article", "masculine nominative plural", "οἱ"),
    Form(5, "ὁ, ἡ, τό — definite article", "masculine genitive plural", "τῶν"),
    Form(6, "ὁ, ἡ, τό — definite article", "masculine dative plural", "τοῖς"),
    Form(7, "ὁ, ἡ, τό — definite article", "masculine accusative plural", "τούς"),
    Form(8, "ὁ, ἡ, τό — definite article", "feminine nominative singular", "ἡ"),
    Form(9, "ὁ, ἡ, τό — definite article", "feminine genitive singular", "τῆς"),
    Form(10, "ὁ, ἡ, τό — definite article", "feminine dative singular", "τῇ"),
    Form(11, "ὁ, ἡ, τό — definite article", "feminine accusative singular", "τήν"),
    Form(12, "ὁ, ἡ, τό — definite article", "feminine nominative plural", "αἱ"),
    Form(13, "ὁ, ἡ, τό — definite article", "feminine genitive plural", "τῶν"),
    Form(14, "ὁ, ἡ, τό — definite article", "feminine dative plural", "ταῖς"),
    Form(15, "ὁ, ἡ, τό — definite article", "feminine accusative plural", "τάς"),
    Form(16, "ὁ, ἡ, τό — definite article", "neuter nominative singular", "τό"),
    Form(17, "ὁ, ἡ, τό — definite article", "neuter genitive singular", "τοῦ"),
    Form(18, "ὁ, ἡ, τό — definite article", "neuter dative singular", "τῷ"),
    Form(19, "ὁ, ἡ, τό — definite article", "neuter accusative singular", "τό"),
    Form(20, "ὁ, ἡ, τό — definite article", "neuter nominative plural", "τά"),
    Form(21, "ὁ, ἡ, τό — definite article", "neuter genitive plural", "τῶν"),
    Form(22, "ὁ, ἡ, τό — definite article", "neuter dative plural", "τοῖς"),
    Form(23, "ὁ, ἡ, τό — definite article", "neuter accusative plural", "τά"),
)

// ἀρχή — 1st decl. fem. (η)  (7 forms)
// not attested in the NT: vocative singular, genitive plural, vocative plural
val arche = listOf(
    Form(0, "ἀρχή — 1st decl. fem. (η)", "nominative singular", "ἀρχή"),
    Form(1, "ἀρχή — 1st decl. fem. (η)", "genitive singular", "ἀρχῆς"),
    Form(2, "ἀρχή — 1st decl. fem. (η)", "dative singular", "ἀρχῇ"),
    Form(3, "ἀρχή — 1st decl. fem. (η)", "accusative singular", "ἀρχήν"),
    Form(4, "ἀρχή — 1st decl. fem. (η)", "nominative plural", "ἀρχαί"),
    Form(5, "ἀρχή — 1st decl. fem. (η)", "dative plural", "ἀρχαῖς"),
    Form(6, "ἀρχή — 1st decl. fem. (η)", "accusative plural", "ἀρχάς"),
)

// ἡμέρα — 1st decl. fem. (α)  (8 forms)

val hemera = listOf(
    Form(0, "ἡμέρα — 1st decl. fem. (α)", "nominative singular", "ἡμέρα"),
    Form(1, "ἡμέρα — 1st decl. fem. (α)", "genitive singular", "ἡμέρας"),
    Form(2, "ἡμέρα — 1st decl. fem. (α)", "dative singular", "ἡμέρᾳ"),
    Form(3, "ἡμέρα — 1st decl. fem. (α)", "accusative singular", "ἡμέραν"),
    Form(4, "ἡμέρα — 1st decl. fem. (α)", "nominative plural", "ἡμέραι"),
    Form(5, "ἡμέρα — 1st decl. fem. (α)", "genitive plural", "ἡμερῶν"),
    Form(6, "ἡμέρα — 1st decl. fem. (α)", "dative plural", "ἡμέραις"),
    Form(7, "ἡμέρα — 1st decl. fem. (α)", "accusative plural", "ἡμέρας"),
)

// ἔργον — 2nd decl. neut.  (8 forms)

val ergon = listOf(
    Form(0, "ἔργον — 2nd decl. neut.", "nominative singular", "ἔργον"),
    Form(1, "ἔργον — 2nd decl. neut.", "genitive singular", "ἔργου"),
    Form(2, "ἔργον — 2nd decl. neut.", "dative singular", "ἔργῳ"),
    Form(3, "ἔργον — 2nd decl. neut.", "accusative singular", "ἔργον"),
    Form(4, "ἔργον — 2nd decl. neut.", "nominative plural", "ἔργα"),
    Form(5, "ἔργον — 2nd decl. neut.", "genitive plural", "ἔργων"),
    Form(6, "ἔργον — 2nd decl. neut.", "dative plural", "ἔργοις"),
    Form(7, "ἔργον — 2nd decl. neut.", "accusative plural", "ἔργα"),
)

// πατήρ — 3rd decl.  (10 forms)
val pater = listOf(
    Form(0, "πατήρ — 3rd decl.", "nominative singular", "πατήρ"),
    Form(1, "πατήρ — 3rd decl.", "genitive singular", "πατρός"),
    Form(2, "πατήρ — 3rd decl.", "dative singular", "πατρί"),
    Form(3, "πατήρ — 3rd decl.", "accusative singular", "πατέρα"),
    Form(4, "πατήρ — 3rd decl.", "vocative singular", "πάτερ"),
    Form(5, "πατήρ — 3rd decl.", "nominative plural", "πατέρες"),
    Form(6, "πατήρ — 3rd decl.", "genitive plural", "πατέρων"),
    Form(7, "πατήρ — 3rd decl.", "dative plural", "πατράσι(ν)"),
    Form(8, "πατήρ — 3rd decl.", "accusative plural", "πατέρας"),
    Form(9, "πατήρ — 3rd decl.", "vocative plural", "πατέρες"),
)

// πνεῦμα — 3rd decl. neut.  (8 forms)

val pneuma = listOf(
    Form(0, "πνεῦμα — 3rd decl. neut.", "nominative singular", "πνεῦμα"),
    Form(1, "πνεῦμα — 3rd decl. neut.", "genitive singular", "πνεύματος"),
    Form(2, "πνεῦμα — 3rd decl. neut.", "dative singular", "πνεύματι"),
    Form(3, "πνεῦμα — 3rd decl. neut.", "accusative singular", "πνεῦμα"),
    Form(4, "πνεῦμα — 3rd decl. neut.", "nominative plural", "πνεύματα"),
    Form(5, "πνεῦμα — 3rd decl. neut.", "genitive plural", "πνευμάτων"),
    Form(6, "πνεῦμα — 3rd decl. neut.", "dative plural", "πνεύμασι(ν)"),
    Form(7, "πνεῦμα — 3rd decl. neut.", "accusative plural", "πνεύματα"),
)

// εἰμί — present indicative  (6 forms)
val eimi = listOf(
    Form(0, "εἰμί — present indicative", "1st person singular", "εἰμί"),
    Form(1, "εἰμί — present indicative", "2nd person singular", "εἶ"),
    Form(2, "εἰμί — present indicative", "3rd person singular", "ἐστί(ν)"),
    Form(3, "εἰμί — present indicative", "1st person plural", "ἐσμέν"),
    Form(4, "εἰμί — present indicative", "2nd person plural", "ἐστέ"),
    Form(5, "εἰμί — present indicative", "3rd person plural", "εἰσί(ν)"),
)

// πιστεύω — pres. act. indic.  (6 forms)
val pisteuo = listOf(
    Form(0, "πιστεύω — pres. act. indic.", "1st person singular", "πιστεύω"),
    Form(1, "πιστεύω — pres. act. indic.", "2nd person singular", "πιστεύεις"),
    Form(2, "πιστεύω — pres. act. indic.", "3rd person singular", "πιστεύει"),
    Form(3, "πιστεύω — pres. act. indic.", "1st person plural", "πιστεύομεν"),
    Form(4, "πιστεύω — pres. act. indic.", "2nd person plural", "πιστεύετε"),
    Form(5, "πιστεύω — pres. act. indic.", "3rd person plural", "πιστεύουσι(ν)"),
)

data class Deck(val name: String, val forms: List<Form>)

val decks = listOf(
    Deck("ὁ, ἡ, τό — definite article", article),
    Deck("λόγος — 2nd decl. masc.", logos),
    Deck("ἀρχή — 1st decl. fem. (η)", arche),
    Deck("ἡμέρα — 1st decl. fem. (α)", hemera),
    Deck("ἔργον — 2nd decl. neut.", ergon),
    Deck("πατήρ — 3rd decl.", pater),
    Deck("πνεῦμα — 3rd decl. neut.", pneuma),
    Deck("εἰμί — present indicative", eimi),
    Deck("πιστεύω — pres. act. indic.", pisteuo),
)