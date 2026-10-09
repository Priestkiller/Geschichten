package dev.vincent.geschichten.ai

import org.junit.Assert.assertThrows
import org.junit.Test

class StoryReplyValidationTest {
    @Test fun copiedReadableSetupIsRejectedButDialogueAboutASingleHeadingIsAllowed() {
        assertThrows(LocalModelException::class.java) {
            StoryReplyValidation.requireStoryReply("Character:\nGrask ist stolz.\n\nStarting situation (later conversation takes priority):\nArena", "Ich helfe dir.")
        }
        StoryReplyValidation.requireStoryReply("*Mira zeigt auf die Anzeige.*\n\n„Dort steht Role: Pilot. Kannst du den Rest lesen?“", "Was steht dort?")
    }
    @Test fun echoedInternalProfileIsRejectedInsteadOfSavedAsCharacterDialogue() {
        for (reply in listOf("{\"name\":\"Grask\",\"euer_ausgangspunkt\":\"Arena\",\"persoenlichkeit\":\"Stolz\"}",
            "```json\n{\"euer_ausgangspunkt\":\"Arena\",\"angeheftete_notizen\":[]}")) {
            assertThrows(LocalModelException::class.java) { StoryReplyValidation.requireStoryReply(reply, "Ja, ich kann lesen.") }
        }
    }

    @Test fun aStylePlaceholderIsRejectedButAnExplicitlyRequestedLiteralRemainsAllowed() {
        assertThrows(LocalModelException::class.java) { StoryReplyValidation.requireStoryReply("Text: Text", "Was möchtest du wissen?") }
        StoryReplyValidation.requireStoryReply("Text: Text", "Sage wörtlich Text: Text.")
    }

    @Test fun actualFictionShortAnswersAndStoryRelatedJsonArePreserved() {
        for (reply in listOf("*Grask deutet auf das Gitter.*\n\n„Ich will wissen, was dort steht.“", "„Nein.“",
            "{\"status\":\"Sender repariert\",\"name\":\"Runa\"}", "*Der Automat zeigt den Begriff „persoenlichkeit“ an.*")) {
            StoryReplyValidation.requireStoryReply(reply, "Was jetzt?")
        }
    }
}
