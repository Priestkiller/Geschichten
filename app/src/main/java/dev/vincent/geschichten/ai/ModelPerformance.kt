package dev.vincent.geschichten.ai

/** Compare measured prefill latency; retain four threads when differences are just noise. */
internal object ModelPerformance {
    val testSystem = "Du beantwortest eine kurze Frage auf Deutsch. Lies die folgenden Angaben sorgfältig. " +
        "Gib nur eine kurze sachliche Antwort aus. " +
        (1..24).joinToString(" ") { "Eintrag $it: Die Bücherei öffnet um neun Uhr und schließt um achtzehn Uhr. Montags bleibt sie geschlossen." }
    const val testQuestion = "Wann öffnet die Bücherei dienstags?"
    fun candidates(processors: Int): List<Int> = listOf(4, 2, 6).filter { it <= processors.coerceAtLeast(1) }.ifEmpty { listOf(1) }
    fun select(measurements: Map<Int, Long>): Int {
        require(measurements.isNotEmpty() && measurements.values.all { it > 0 })
        val fastest = measurements.minBy { it.value }.key
        val baseline = measurements[4] ?: return fastest
        return if (measurements.getValue(fastest).toDouble() < baseline * 0.9) fastest else 4
    }
    fun deviceName(model: String): String = when {
        model.startsWith("SM-S928", true) -> "Galaxy S24 Ultra"
        model.startsWith("SM-S921", true) -> "Galaxy S24"
        model.startsWith("SM-S926", true) -> "Galaxy S24+"
        else -> model.take(60).ifBlank { "Dein Handy" }
    }
}
