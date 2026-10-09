package dev.vincent.geschichten.ai

/** Rejects leaked setup data, without editing, shortening or replacing generated fiction. */
internal object StoryReplyValidation {
    fun requireStoryReply(reply: String, userMessage: String) {
        val text = reply.trim().removePrefix("```json").removePrefix("```").trim()
        val internalFields = listOf("\"euer_ausgangspunkt\"", "\"persoenlichkeit\"", "\"angeheftete_notizen\"", "\"jetzt_fortzusetzen\"")
        val copiedData = text.startsWith("{") && internalFields.count { text.contains(it) } >= 2
        val setupHeadings = Regex("(?im)^\\s*(?:\\*\\*)?(?:Character|Role|Traits|World|Starting situation(?: \\(later conversation takes priority\\))?|Current location|Established events|Pinned story notes)\\s*:")
        val copiedReadableSetup = setupHeadings.findAll(text).count() >= 2
        val placeholder = Regex("(?i)^\\*{0,2}Text\\s*:\\s*Text\\*{0,2}[.!]?$" ).matches(text) &&
            !Regex("(?i)Text\\s*:\\s*Text").containsMatchIn(userMessage)
        if (copiedData || copiedReadableSetup || placeholder) throw LocalModelException(
            "Die KI hat Erzählvorgaben statt einer Antwort ausgegeben. Deine Nachricht bleibt erhalten. Versuche es erneut oder wähle eine andere KI.",
        )
    }
}
