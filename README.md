# Overview

I wanted to find out by shipping something small and complete how a native Android app is actually put together. I chose a subject I know well, Biblical Greek so that all of my attention could go on the unfamiliar part.

**Greek Paradigm Trainer** drills the inflected forms of Greek nouns and verbs. You open it to a list of nine decks. Picking one opens the practice screen, which shows a lemma and a grammatical slot. You recall the form, tap **Show answer** to check yourself, then mark it **Known** or **Not known**. The app keeps a running score and a position counter, and tells you how you did at the end. A **Reference** button shows the whole paradigm as a table, and **Decks** takes you back to the list.

My purpose was to understand three things I had never worked with: how a declarative UI framework differs from the imperative code I was used to, how state drives what appears on screen, and how to structure an app so that the screens know nothing about where their data comes from.

[Software Demo Video](https://youtu.be/8Wt8cYFHZhY)

# Features (v2)

* **55 paradigms, 605 forms**: the article; personal, demonstrative, relative and interrogative pronouns; all three noun declensions (12 model nouns); 2-1-2, 3-1-3 and 3rd declension adjectives; the whole λύω indicative system (present through perfect, all voices); 2nd aorist; εἰμί; α/ε/ο contract verbs; δίδωμι; subjunctives; imperatives; infinitives; and four participles. Every paradigm has a short teaching note on the pattern behind it.
* **Spaced repetition**: each form is scheduled on its own. Each correct answer roughly doubles the interval (1, 2, 4, 8, 16… days) and a miss brings the form back today. Progress is saved on the device.
* **Daily review**: one tap reviews everything due across every paradigm, plus a few new forms from paradigms you've started. A daily goal, a streak and a 16-week activity grid track your consistency.
* **Four ways to drill**:
  * *Flashcards*: recall the form, then grade yourself.
  * *Type it*: spell the form on a built-in polytonic Greek keyboard, which handles breathings, accents, iota subscript and diaeresis in any order. You can also type Beta Code on your normal keyboard (`lo/gos` becomes λόγος). Movable ν, final sigma and alternative forms are handled, and you choose whether missing accents count as wrong.
  * *Pick the form*: multiple choice.
  * *Parse it*: see a form and identify it. Forms with more than one parsing, such as τῶν or ἔργον, are handled correctly, and the other parsings are shown afterwards.
* **In-session relearning**: a missed form comes back a few cards later, so you finish every session having got it right.
* **Paradigm tables** shaded by how well you know each cell. A *Cover* mode hides the forms so you can test yourself on the chart.
* **Search** in Greek, with or without accents, or in Latin letters ("logos", "aorist", "ἐλύθην").
* **Progress** screen with mastery by topic and your trickiest forms.
* A scholarly light and dark theme (ink on parchment / lamplight), with Greek set in a serif face.

# Architecture

* `Model.kt`, `Paradigms.kt`: the data model and the paradigm library, written as compact builders that read like grammar charts.
* `GreekText.kt`: Unicode handling for polytonic Greek (normalisation, answer checking, keyboard composition, Beta Code).
* `Progress.kt`, `Session.kt`: the spaced-repetition scheduler, saved progress, study sessions and multiple-choice generation. These are pure Kotlin and unit-tested.
* `AppState.kt`: the single source of truth and back stack. It persists progress to SharedPreferences.
* `*Screen.kt`, `GreekKeyboard.kt`, `Components.kt`: stateless Compose screens that receive data and callbacks only.

Run the tests with `./gradlew test`.

# Development Environment

I built the app in **Android Studio**, writing **Kotlin** with **Jetpack Compose** and **Material 3**. The minimum SDK is 24, and I developed and tested throughout on a physical Android phone over USB rather than an emulator. Version control was **Git** with **GitHub**. The paradigm data was generated separately from MorphGNT text files

# Useful Websites

* [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
* [State in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-state)
* [Jetpack Compose documentation](https://developer.android.com/develop/ui/compose/documentation)
* [Run apps on a hardware device](https://developer.android.com/studio/run/device)
* [MorphGNT](https://github.com/morphgnt/sblgnt)
* [SBLGNT](https://www.sblgnt.com/)

# Future Work

* Show a verse reference beside each form, with the SBLGNT text
* Add the rest of the μι verbs (τίθημι, ἵστημι) and more 3rd declension nouns
* Optionally pick the drill mode for the daily review
* Export and import progress

# Attribution

Greek text: SBL Greek New Testament, © 2010 Society of Biblical Literature and Logos Bible Software, licensed CC BY 4.0 — https://sblgnt.com

Morphological tagging: MorphGNT, licensed CC BY-SA 3.0 — https://github.com/morphgnt/sblgnt

Lemma frequencies and paradigm forms were computed from the above.
