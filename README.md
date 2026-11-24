# Overview

I wanted to find out by shipping something small and complete how a native Android app is actually put together. I chose a subject I know well, Biblical Greek so that all of my attention could go on the unfamiliar part.

**Greek Paradigm Trainer** drills the inflected forms of Greek nouns and verbs. You open it to a list of nine decks. Picking one opens the practice screen, which shows a lemma and a grammatical slot. You recall the form, tap **Show answer** to check yourself, then mark it **Known** or **Not known**. The app keeps a running score and a position counter, and tells you how you did at the end. A **Reference** button shows the whole paradigm as a table, and **Decks** takes you back to the list.

My purpose was to understand three things I had never worked with: how a declarative UI framework differs from the imperative code I was used to, how state drives what appears on screen, and how to structure an app so that the screens know nothing about where their data comes from.

[Software Demo Video](https://youtu.be/8Wt8cYFHZhY)

# Features

### Learn from the Greek New Testament
* **55 paradigms, 605 forms**: the article; personal, demonstrative, relative and interrogative pronouns; all three noun declensions (12 model nouns); 2-1-2, 3-1-3 and 3rd declension adjectives; the whole λύω indicative system; the 2nd aorist; εἰμί; α/ε/ο contract verbs; δίδωμι; subjunctives; imperatives; infinitives; and four participles. Every paradigm has a short teaching note.
* **Real NT phrases for every form**: 1,400+ short phrases from the SBL Greek New Testament, with the form picked out, its reference, and how often it occurs in the NT. For example, ἐδάκρυσεν **ὁ** Ἰησοῦς (John 11:35), or οὐ δύναται **λυθῆναι** ἡ γραφή (John 10:35). λύω itself is rare in the NT, so its paradigms also show other regular verbs with exactly the same ending, such as ἀπεκρίθησαν for ἐλύθησαν. Forms that never occur in the NT say so plainly.
* **"In context" drill**: parse a highlighted word in a real verse. The options mix tenses and voices, so you have to read the whole word.
* **A daily NT phrase** on the home screen, drawn from forms you've studied.
* **Frequency everywhere**: each paradigm shows how often its forms occur in the NT and its most common forms. Tap any cell of a table to see that form's count, every example phrase and your memory of it.

### Remember what you learn
* **Spaced repetition**: each form is scheduled on its own. Each correct answer roughly doubles the interval (1, 2, 4, 8, 16… days) and a miss brings the form back today.
* **Memory strength**: every form has an estimated chance that you'd recall it today. It is 90% when the form falls due and fades the longer you wait. Home, each paradigm and each topic show their memory strength, and a **Strengthen** session targets fading forms first.
* **Smart lessons**: each form moves through stages as you learn it. You meet it on a flashcard, recognise it among four, parse it (in a real verse when there is one), then type it. A form missed in a session comes back as multiple choice. The daily review uses smart lessons.
* **In-session relearning**: a missed form comes back a few cards later.

### Stay motivated
* **XP**: harder questions earn more (typing 15, parsing 10, choosing 8, flashcards 5), with a combo bonus for every 5 in a row and a completion bonus that's bigger for a flawless session. You set a **daily XP goal**.
* **Streaks with streak freezes**: you earn a freeze for every 7 days of streak, up to 2. A freeze covers a missed day so the streak survives. There's also a 16-week activity grid.
* **Levels 0–5 for every paradigm**: from every form seen up to every form held for a month or more.
* **Continue your path**: the next paradigm in course order, one tap away.
* **18 achievements**, e.g. master the article, a 30-day streak, 100 forms mastered, 10 flawless sessions.

### Drill your way
* Six modes: Smart lesson, Flashcards, Type it (built-in polytonic keyboard or Beta Code, e.g. `lo/gos` gives λόγος), Pick the form, Parse it, and In context.
* Paradigm tables shaded by mastery, with a **Cover** mode for self-testing.
* Search in Greek, with or without accents, or in Latin letters ("logos", "aorist", "ἐλύθην").
* A scholarly light and dark theme.

# Architecture

* `Model.kt`, `Paradigms.kt`: the data model and the paradigm library, written as compact builders that read like grammar charts.
* `NtCorpus.kt`: reads `assets/nt_examples.tsv`, the NT frequencies and example phrases for every form.
* `tools/build_nt_examples.py`: generates that file from MorphGNT. It matches each form by lemma and parsing, prefers short verses from different books, and reports any NT spelling that disagrees with the paradigm tables. To rebuild it:
  1. Export the forms: `EXPORT_FORMS=build/forms.tsv ./gradlew :app:testDebugUnitTest --tests '*FormsExport*'`
  2. Clone https://github.com/morphgnt/sblgnt
  3. Run `python3 tools/build_nt_examples.py app/build/forms.tsv path/to/sblgnt app/src/main/assets/nt_examples.tsv`
* `GreekText.kt`: Unicode handling for polytonic Greek (normalisation, answer checking, keyboard composition, Beta Code).
* `Progress.kt`, `Session.kt`: the spaced-repetition scheduler, saved progress, study sessions and multiple-choice generation. These are pure Kotlin and unit-tested.
* `AppState.kt`: the single source of truth and back stack. It persists progress to SharedPreferences.
* `*Screen.kt`, `GreekKeyboard.kt`, `Components.kt`: stateless Compose screens that receive data and callbacks only.

Run the unit tests with `./gradlew test`, and the UI tests on a connected phone or emulator with `./gradlew connectedDebugAndroidTest`.

# Signing

* **Debug builds** use `app/debug.keystore`, which is committed, so a build from any computer installs over the previous one.
* **Release builds** are signed with `release.jks`, configured in `keystore.properties` at the project root. Both files are git-ignored. Back them up somewhere safe, because the Play Store accepts updates only when they are signed with the same key. Without `keystore.properties`, the release build is unsigned.

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

* English glosses for the NT phrases
* Add the rest of the μι verbs (τίθημι, ἵστημι) and more 3rd declension nouns
* Export and import progress

# Attribution

Greek text: SBL Greek New Testament, © 2010 Society of Biblical Literature and Logos Bible Software, licensed CC BY 4.0 — https://sblgnt.com

Morphological tagging: MorphGNT, licensed CC BY-SA 3.0 — https://github.com/morphgnt/sblgnt

Lemma frequencies, form frequencies and the example phrases in `app/src/main/assets/nt_examples.tsv` were computed from the above. That file is a derived work, licensed CC BY-SA 3.0.
