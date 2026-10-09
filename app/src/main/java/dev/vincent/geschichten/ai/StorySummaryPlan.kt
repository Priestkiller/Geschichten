package dev.vincent.geschichten.ai

import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.StoryBundle

/** Summarize a completed interval before an uncovered turn leaves the inference window. */
object StorySummaryPlan {
    fun nextCheckpoint(bundle: StoryBundle, checkpoint: Int, beforeReply: Boolean): Int? {
        val own = bundle.messages.filter { it.storyId == bundle.story.id && it.text.isNotBlank() }
        val completed = (if (own.lastOrNull()?.role == ChatRole.USER) own.dropLast(1) else own)
            .filter { it.role == ChatRole.USER }
        val previous = checkpoint.coerceIn(0, completed.size)
        if (completed.size == previous) return null
        val uncovered = if (beforeReply) {
            val omitted = StoryPrompt.window(bundle).omittedMessageIds.toSet()
            completed.drop(previous).any { it.id in omitted }
        } else completed.size - previous >= 6
        return if (uncovered) minOf(previous + 6, completed.size) else null
    }
}
