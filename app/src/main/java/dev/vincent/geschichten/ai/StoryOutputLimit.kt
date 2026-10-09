package dev.vincent.geschichten.ai

object StoryOutputLimit {
    /** LiteRT exposes a count, not an EOS reason. Treat its boundary conservatively. */
    fun requireCompleted(decodeTokens: Int, limit: Int) {
        if (decodeTokens >= limit) throw LocalModelException(
            "Die Antwort hat das Ausgabelimit erreicht und könnte unvollständig sein. " +
                "Sie wurde nicht gespeichert; deine Nachricht bleibt für einen neuen Versuch erhalten.",
        )
    }
}
