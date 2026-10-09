package dev.vincent.geschichten.data

/** Only ambiguous self-introductions in delivered openings; never rewrites user-created prose. */
internal object CharacterDialogueRevision {
    fun revise(profile: CharacterProfile): CharacterProfile = profile.copy(
        openingMessage = profile.openingMessage.replace("„${profile.name}. ", "„Ich bin ${profile.name}. "),
    )
}
