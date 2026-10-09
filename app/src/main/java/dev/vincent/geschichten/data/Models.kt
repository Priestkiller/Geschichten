package dev.vincent.geschichten.data

import java.util.UUID

data class CharacterProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val genre: String,
    val traits: String,
    val personality: String,
    val scenario: String,
    val storyTitle: String,
    val openingMessage: String,
    val avatarKey: String = "runa",
    val custom: Boolean = false,
)

data class Story(
    val id: String = UUID.randomUUID().toString(),
    val characterId: String,
    val title: String,
    val summary: String = "",
    // Authored relationship and scene, frozen when this story is created.
    val startContext: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class ChatRole { USER, CHARACTER }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val storyId: String,
    val role: ChatRole,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class MemoryKind(val title: String) {
    FACT("Wichtige Fakten"),
    LOCATION("Aktueller Ort"),
    GOAL("Offene Aufgabe"),
    EVENT("Gemeinsame Erlebnisse"),
}

data class MemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val storyId: String,
    val kind: MemoryKind,
    val text: String,
    val pinned: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)

data class StoryBundle(
    val story: Story,
    val character: CharacterProfile,
    val messages: List<ChatMessage>,
    val memories: List<MemoryEntry>,
    val memory: dev.vincent.geschichten.memory.MemorySnapshot = dev.vincent.geschichten.memory.MemorySnapshot(),
)
