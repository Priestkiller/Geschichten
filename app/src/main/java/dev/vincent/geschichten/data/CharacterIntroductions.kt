package dev.vincent.geschichten.data

/** Authored story entries: the player's place in the scene precedes the character biography. */
internal object CharacterIntroductions {
    const val MAX_OPENING_CHARS = 8_000
    const val MAX_PERSONALITY_CHARS = 1_800

    private val entries by lazy {
        NorthernStarts.entries + FantasyStarts.entries + CrimeStarts.entries + ScienceStarts.entries +
            CreatureStarts.entries + MiddleEarthStarts.entries + BladeRunnerStarts.entries +
            CyberpunkStarts.entries + MonsterStarts.entries
    }

    fun revise(profile: CharacterProfile): CharacterProfile {
        val entry = entries.getValue(profile.id)
        val previous = ReleasedIntroductionsV6.revise(profile).openingMessage.split("\n\n")
        val worldConflict = previous[1].removeSurrounding("*").substringBefore("Du ").trim()
        val encounter = Regex("„[^“]+“").replace(entry.encounter?.trimIndent()?.trim() ?: profile.openingMessage) { "**${it.value}**" }
        val opening = listOf(narration(entry.player), previous[0], narration(entry.history),
            narration(worldConflict), narration(entry.scene), encounter).joinToString("\n\n")
        val sentenceEnd = profile.personality.indexOf(". ", startIndex = profile.name.length).let {
            if (it < 0) profile.personality.length else it + 1
        }
        val identity = profile.personality.take(sentenceEnd)
        val behavior = profile.personality.drop(sentenceEnd).trim()
        return profile.copy(
            personality = "$identity\n\n${entry.nature.trimIndent().trim()}\n\n$behavior",
            scenario = entry.context.trimIndent().trim(), openingMessage = opening,
        )
    }

    /** User edits never regain an old role or quest merely because their ID matches. */
    fun contextFor(profile: CharacterProfile): String {
        val current = BuiltinCharacters.profiles.firstOrNull { it.id == profile.id } ?: return ""
        return if (profile.scenario == current.scenario && profile.openingMessage == current.openingMessage &&
            profile.name == current.name) entries.getValue(profile.id).context.trimIndent().trim() else ""
    }

    fun scenarioForHistory(profile: CharacterProfile, firstMessage: String?, startContext: String): String {
        if (startContext.isBlank() && firstMessage != profile.openingMessage && contextFor(profile).isNotBlank()) {
            return BuiltinCharacters.preIntroductionProfiles.first { it.id == profile.id }.scenario
        }
        return profile.scenario
    }

    /** The UI biography is fuller; retain all authored voice/body limits in the model budget. */
    fun personalityForInference(profile: CharacterProfile): String {
        val current = BuiltinCharacters.profiles.firstOrNull { it.id == profile.id }
        return if (current?.personality == profile.personality) {
            BuiltinCharacters.preIntroductionProfiles.first { it.id == profile.id }.personality
        } else profile.personality
    }

    private fun narration(text: String): String = text.trimIndent().trim().split("\n\n")
        .joinToString("\n\n") { paragraph ->
            val source = paragraph.trim()
            buildString {
                var offset = 0
                Regex("„[^“]+“").findAll(source).forEach { quote ->
                    if (quote.range.first > offset) append("*${source.substring(offset, quote.range.first)}*")
                    append("**${quote.value}**")
                    offset = quote.range.last + 1
                }
                if (offset < source.length) append("*${source.substring(offset)}*")
            }
        }
}

/** Individual prose; no generated filler, model calls, or random openings. */
internal data class StoryStart(
    val player: String, val history: String, val scene: String, val nature: String, val context: String,
    val encounter: String? = null,
)
