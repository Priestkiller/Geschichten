package dev.vincent.geschichten.ai

import com.google.gson.JsonParser
import dev.vincent.geschichten.data.ChatMessage
import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.MemoryEntry
import dev.vincent.geschichten.data.MemoryKind
import dev.vincent.geschichten.data.StoryBundle
import dev.vincent.geschichten.data.CharacterIntroductions
import java.util.Locale

/** Content-free diagnostics: IDs and shortening decisions, never automatic transcript logging. */
data class HistoryWindow(
    val messages: List<ModelMessage>,
    val selectedMessageIds: List<String>,
    val omittedMessageIds: List<String>,
    val shortenedMessageIds: List<String>,
    val stopReason: String?,
)

/**
 * Selects an inference window; it never changes the saved conversation or invents a summary.
 * Character counts are deliberately conservative estimates, NOT a tokenizer or a token guarantee.
 * The native engine additionally enforces its 4,096-token limit and must report overflow normally.
 */
object StoryPrompt {
    const val MAX_USER_MESSAGE_CHARS = 1_000
    const val MAX_SYSTEM_CHARS = 3_600
    const val MAX_HISTORY_CHARS = 4_000
    private const val MAX_HISTORY_MESSAGES = 14
    private const val MAX_OPENING_TAIL_CHARS = 900
    private const val OPEN_SCENE = "Eröffne die Szene aus deinen Erzählangaben. Schreibe nur für deine Figur; die andere Figur wird vom Nutzer gespielt."

    /** Plain role instructions and labelled scene facts; actual dialogue stays in history. */
    fun system(bundle: StoryBundle, adultThemes: Boolean = false): String {
        val encoded = encodedContext(bundle, adultThemes, dialogueDetails = false)
        val data = JsonParser.parseString(encoded.substringAfter("ERZÄHLDATEN (JSON):\n")).asJsonObject
        val result = buildString {
            append(encoded.substringBefore("\n\nERZÄHLDATEN (JSON):"))
            fun field(label: String, key: String) {
                data[key]?.takeIf { it.isJsonPrimitive && it.asString.isNotBlank() }?.let {
                    // Keep control characters and embedded quotes escaped even in the readable layout.
                    append("\n\n").append(label).append(":\n").append(it.toString().removeSurrounding("\""))
                }
            }
            field("Character", "persoenlichkeit")
            field("Role", "rolle")
            field("Traits", "eigenschaften")
            field("World", "genre")
            field("Starting situation (later conversation takes priority)", "euer_ausgangspunkt")
            field("Starting scene", "ausgangsszene")
            field("Location note (recent conversation takes priority)", "aktueller_ort_oder_startort")
            field("Earlier summary (later corrections take priority)", "gespeicherte_zusammenfassung")
            for ((key, label) in listOf("angeheftete_notizen" to "Pinned story notes", "weitere_passende_notizen" to "Other story notes")) {
                data[key]?.asJsonArray?.takeIf { it.size() > 0 }?.let { notes ->
                    append("\n\n").append(label).append(":\n")
                    append(notes.joinToString("\n") { it.toString().removeSurrounding("\"") })
                }
            }
            data["jetzt_fortzusetzen"]?.asJsonObject?.get("offene_aufgabe_der_szene")?.let {
                append("\n\nUnresolved request in this scene:\n").append(it.toString().removeSurrounding("\""))
            }
        }
        check(result.length <= MAX_SYSTEM_CHARS) { "Die Erzählvorgaben sind zu lang." }
        return result
    }

    /** Bounded, escaped context selection, also retained for diagnostics and field-level tests. */
    internal fun encodedContext(bundle: StoryBundle, adultThemes: Boolean = false, dialogueDetails: Boolean = true): String {
        val memories = bundle.memories.filter { it.storyId == bundle.story.id && it.text.isNotBlank() }
        val newestLocation = memories.filter { it.kind == MemoryKind.LOCATION }
            .maxWithOrNull(compareBy<MemoryEntry> { it.updatedAt }.thenBy { it.createdAt }.thenBy { it.id })
        val recentInput = bundle.messages.lastOrNull { it.role == ChatRole.USER && it.storyId == bundle.story.id }?.text.orEmpty()
        val recentTokens = words(recentInput)
        val automaticSummaryId = "auto-summary-${bundle.story.id}"
        val pins = memories.filter { it.pinned && it.kind != MemoryKind.LOCATION && it.id != automaticSummaryId &&
            !(bundle.story.startContext.isNotBlank() && it.text.startsWith("Startvorgabe:") && it.updatedAt == it.createdAt) }
            .sortedWith(
                compareBy<MemoryEntry> { it.text.startsWith("Startvorgabe:") }
                    .thenByDescending { relevance(it.text, recentTokens) }
                    .thenByDescending { it.updatedAt }
                    .thenBy { memoryPriority(it.kind) },
            )
        val otherNotes = memories.filter { !it.pinned && it.kind != MemoryKind.LOCATION && it.id != automaticSummaryId }
            .sortedWith(compareByDescending<MemoryEntry> { relevance(it.text, recentTokens) }.thenByDescending { it.updatedAt })

        // Allocate character and pinned-state space before optional summaries and secondary notes.
        // Each value is escaped independently and bounded before appending; the JSON stays valid.
        return BoundedData(instructionsFor(quoted(bundle.character.name, 70), adultThemes), MAX_SYSTEM_CHARS, currentDialogue(bundle, dialogueDetails)).apply {
            field("name", bundle.character.name, 70)
            field("rolle", bundle.character.role, 80)
            field("genre", bundle.character.genre, 40)
            field("euer_ausgangspunkt", bundle.story.startContext, 650)
            field("eigenschaften", bundle.character.traits, 90)
            field("persoenlichkeit", CharacterIntroductions.personalityForInference(bundle.character), 650)
            field("aktueller_ort_oder_startort", newestLocation?.text.orEmpty(), 200)
            field("gespeicherte_zusammenfassung", bundle.story.summary, 600)
            arrayField("angeheftete_notizen", memoryTexts(pins), 360)
            if (bundle.story.startContext.isBlank()) field("ausgangsszene", CharacterIntroductions.scenarioForHistory(
                bundle.character, bundle.messages.firstOrNull()?.text, bundle.story.startContext), 500)
            arrayField("weitere_passende_notizen", memoryTexts(otherNotes), 140)
        }.finish()
    }

    private fun instructionsFor(name: String, adultThemes: Boolean): String = """
        Continue this German roleplay. You play $name. The user plays a different person.
        Write ONLY $name's next response, in fluent German. The latest user message is addressed TO $name; do not repeat it as $name's own words.
        React to what the user actually said or did. Answer their question, act on their offer, or respect their refusal before advancing the scene. Accept what the user says about their own person over assumptions in the starting situation. Keep each person's knowledge and belongings separate.
        Later actions and explicit corrections override older scene notes and summaries. Keep unchanged injuries, relationships and possessions. A question or possibility is not an event. If a past fact was never established, do not claim to remember it.
        Describe $name's actions in third person between asterisks. Put spoken dialogue in quotation marks. Address the other person directly in narration, never as "the user". Do not write their words, actions, thoughts or decisions. Do not invent equipment, powers or agreements that solve the problem.
        Stay in character. No labels, analysis or comments outside the story. Do not repeat the opening.
    """.trimIndent() + "\n" + if (adultThemes) {
        "Adult characters may use strong language, dark themes and consensual romance. Fade out sexual intimacy without explicit detail."
    } else "Keep the story suitable for young readers, without sexual scenes or graphic violence."

    /**
     * Keeps a suffix of complete USER/CHARACTER turns plus the pending USER message.
     * Role information is returned structurally; callers must not flatten it into a system string.
     * Oversized old turns are omitted from inference only, never deleted from the archive.
     */
    fun history(bundle: StoryBundle): List<ModelMessage> = window(bundle).messages

    fun window(bundle: StoryBundle): HistoryWindow {
        val savedMessages = bundle.messages.filter { it.storyId == bundle.story.id && it.text.isNotBlank() }
        // The original entry stays in SQLite/UI. Retain its actual final encounter
        // and spoken question in history; the frozen background belongs to system data.
        val messages = savedMessages.mapIndexed { index, message ->
            if (index == 0 && message.role == ChatRole.CHARACTER &&
                message.text.length > MAX_HISTORY_CHARS - MAX_USER_MESSAGE_CHARS) {
                message.copy(text = openingTail(message.text))
            } else message
        }
        if (messages.isEmpty()) return HistoryWindow(emptyList(), emptyList(), emptyList(), emptyList(), null)
        val pending = messages.last()
        if (pending.role == ChatRole.USER) {
            require(pending.text.length <= MAX_USER_MESSAGE_CHARS) {
                "Bitte kürze deine Nachricht auf höchstens $MAX_USER_MESSAGE_CHARS Zeichen, damit genug Platz für die Geschichte bleibt."
            }
        }

        val turns = mutableListOf<MutableList<ChatMessage>>()
        for (message in messages) {
            if (turns.isEmpty() || message.role == ChatRole.USER) {
                turns += mutableListOf(message)
            } else {
                turns.last() += message
            }
        }
        // Every supported instruction template expects user/model turns. In particular,
        // Gemma folds system rules into the first user turn, which must precede the opening.
        var remaining = MAX_HISTORY_CHARS - OPEN_SCENE.length
        var remainingMessages = MAX_HISTORY_MESSAGES
        val selected = ArrayDeque<List<ChatMessage>>()
        var stopReason: String? = null
        for (turn in turns.asReversed()) {
            val length = turn.sumOf { it.text.length.toLong() }
            if (length > remaining || turn.size > remainingMessages) {
                stopReason = if (length > remaining) "character-budget" else "message-budget"
                break
            }
            selected.addFirst(turn)
            remaining -= length.toInt()
            remainingMessages -= turn.size
        }
        val history = selected.flatMap { turn ->
            turn.map { message -> ModelMessage(user = message.role == ChatRole.USER, text = message.text) }
        }
        val ids = selected.flatten().map { it.id }
        return HistoryWindow(
            messages = if (history.firstOrNull()?.user == false) listOf(ModelMessage(true, OPEN_SCENE)) + history else history,
            selectedMessageIds = ids,
            omittedMessageIds = savedMessages.filter { it.id !in ids }.map { it.id },
            shortenedMessageIds = messages.filterIndexed { index, message -> message.id in ids && message.text != savedMessages[index].text }.map { it.id },
            stopReason = stopReason,
        )
    }

    private fun openingTail(text: String): String {
        val selected = ArrayDeque<String>()
        var used = 0
        for (paragraph in text.split(Regex("\\n\\s*\\n")).asReversed()) {
            val required = paragraph.length + if (selected.isEmpty()) 0 else 2
            if (used + required > MAX_OPENING_TAIL_CHARS) break
            selected.addFirst(paragraph)
            used += required
        }
        if (selected.isNotEmpty()) return selected.joinToString("\n\n")
        // An unusually long single paragraph still keeps its final question.
        var start = (text.length - MAX_OPENING_TAIL_CHARS).coerceAtLeast(0)
        if (start < text.length && text[start].isLowSurrogate()) start++
        return text.substring(start)
    }

    private fun latestCharacterWords(bundle: StoryBundle): String {
        val text = bundle.messages.lastOrNull { it.storyId == bundle.story.id && it.role == ChatRole.CHARACTER }?.text.orEmpty()
        val speech = Regex("„[^“]+“|“[^”]+”|«[^»]+»|\"[^\"]+\"").findAll(text).map { it.value }.toList()
        val lastTwo = speech.takeLast(2).joinToString("\n")
        val words = if (lastTwo.length <= 350) lastTwo else speech.lastOrNull().orEmpty()
        val source = words.ifBlank { text }
        var start = (source.length - 350).coerceAtLeast(0)
        if (start < source.length && source[start].isLowSurrogate()) start++
        return source.substring(start)
    }

    /** Reserve the live exchange before optional old notes; no new dialogue or facts are authored here. */
    private fun currentDialogue(bundle: StoryBundle, details: Boolean = true): String {
        val latest = bundle.messages.lastOrNull { it.storyId == bundle.story.id && it.text.isNotBlank() }
        val words = latestCharacterWords(bundle)
        if (latest?.role != ChatRole.USER || words.isBlank()) return ""
        if (!details) {
            val firstReply = bundle.story.summary.isBlank() && bundle.messages.count { it.storyId == bundle.story.id && it.role == ChatRole.USER } == 1
            val task = bundle.memories.filter { it.storyId == bundle.story.id && it.kind == MemoryKind.GOAL && it.text.isNotBlank() &&
                (firstReply || !it.text.startsWith("Startvorgabe:") || it.updatedAt > it.createdAt) }
                .maxWithOrNull(compareBy<MemoryEntry> { it.updatedAt }.thenBy { it.createdAt }.thenBy { it.id }) ?: return ""
            return "\"jetzt_fortzusetzen\": {\"offene_aufgabe_der_szene\": " + quoted(task.text.removePrefix("Startvorgabe: "), 250) + "}"
        }
        return buildString {
            append("\"jetzt_fortzusetzen\": {\n\"sprecher_der_letzten_figurenworte\": ")
            append(quoted(bundle.character.name, 70))
            append(",\n\"letzte_figurenworte\": ").append(quoted(words, 365))
            append(",\n\"sprecher_der_antwort\": \"Nutzer\"")
            append(",\n\"naechster_sprecher\": ").append(quoted(bundle.character.name, 70))
            val firstReply = bundle.story.summary.isBlank() && bundle.messages.count { it.storyId == bundle.story.id && it.role == ChatRole.USER } == 1
            val task = bundle.memories.filter { it.storyId == bundle.story.id && it.kind == MemoryKind.GOAL && it.text.isNotBlank() &&
                (firstReply || !it.text.startsWith("Startvorgabe:") || it.updatedAt > it.createdAt) }
                .maxWithOrNull(compareBy<MemoryEntry> { it.updatedAt }.thenBy { it.createdAt }.thenBy { it.id })
            if (task != null) append(",\n\"offene_aufgabe_der_szene\": ").append(quoted(task.text.removePrefix("Startvorgabe: "), 250))
            // Do not duplicate a clipped version of a long USER message; history keeps it exactly.
            if (latest.text.length <= 300) {
                val encoded = quoted(latest.text, 1_803)
                if (encoded.length <= 802) append(",\n\"antwort_des_nutzers\": ").append(encoded)
            }
            append("\n}")
        }
    }

    private fun memoryPriority(kind: MemoryKind): Int = when (kind) {
        MemoryKind.LOCATION -> 0
        MemoryKind.FACT -> 1
        MemoryKind.GOAL -> 2
        MemoryKind.EVENT -> 3
    }

    private fun memoryTexts(memories: List<MemoryEntry>): List<String> = memories.take(4).map {
        "${it.kind.title}: ${it.text}"
    }

    private fun words(text: String): Set<String> = WORD.findAll(text.lowercase(Locale.GERMAN))
        .map { it.value }.filter { it !in STOPWORDS }.take(60).toSet()

    private fun relevance(text: String, query: Set<String>): Int = if (query.isEmpty()) 0 else words(text).count { it in query }

    private val WORD = Regex("[\\p{L}\\p{N}]{3,}")
    private val STOPWORDS = setOf(
        "der", "die", "das", "den", "dem", "des", "ein", "eine", "einen", "einem", "einer",
        "und", "oder", "aber", "ist", "sind", "war", "mit", "von", "für", "dass", "ich", "mir",
        "mich", "dich", "dir", "sie", "wir", "uns", "ihr", "als", "auf", "aus", "bei", "wie",
        "was", "hat", "habe", "haben", "noch", "nicht", "auch", "sich", "sagt", "frage",
    )

    private class BoundedData(instructions: String, totalLimit: Int, private val finalField: String = "") {
        private val limit = totalLimit - if (finalField.isBlank()) 0 else finalField.length + 2
        private val output = StringBuilder(instructions).append("\n\nERZÄHLDATEN (JSON):\n{\n")
        private var hasFields = false

        fun field(name: String, value: String, fieldBudget: Int) {
            if (value.isBlank()) return
            val prefix = (if (hasFields) ",\n" else "") + "\"$name\": "
            val room = limit - output.length - prefix.length - 2 // Reserve the closing newline and brace.
            if (room < 6) return
            output.append(prefix).append(quoted(value, minOf(fieldBudget, room)))
            hasFields = true
        }

        /** Split the encoded allowance fairly so one long note cannot consume every other pin. */
        fun arrayField(name: String, values: List<String>, fieldBudget: Int) {
            if (values.isEmpty()) return
            val prefix = (if (hasFields) ",\n" else "") + "\"$name\": "
            val room = minOf(fieldBudget, limit - output.length - prefix.length - 2)
            val entries = values.take(4)
            val eachBudget = (room - 2 - (entries.size - 1) * 2) / entries.size
            if (eachBudget < 6) return
            output.append(prefix).append('[')
            entries.forEachIndexed { index, text ->
                if (index != 0) output.append(", ")
                output.append(quoted(text, eachBudget))
            }
            output.append(']')
            hasFields = true
        }

        fun finish(): String {
            if (finalField.isNotBlank()) {
                if (hasFields) output.append(",\n")
                output.append(finalField)
            }
            return output.append("\n}").toString()
        }
    }

    /** Quote/truncate without splitting escape sequences, UTF-16 surrogate pairs, or JSON syntax. */
    private fun quoted(text: String, budget: Int): String {
        val result = StringBuilder("\"")
        var offset = 0
        while (offset < text.length) {
            val codePoint = Character.codePointAt(text, offset)
            val escaped = when (codePoint) {
                34 -> "\\\""
                92 -> "\\\\"
                10 -> "\\n"
                13 -> "\\r"
                9 -> "\\t"
                in 0..31 -> "\\u" + codePoint.toString(16).padStart(4, '0')
                else -> String(Character.toChars(codePoint))
            }
            // Leave room for a visible shortening mark as well as the final quote.
            if (result.length + escaped.length + 2 > budget) {
                result.append('…')
                break
            }
            result.append(escaped)
            offset += Character.charCount(codePoint)
        }
        return result.append('"').toString()
    }
}
