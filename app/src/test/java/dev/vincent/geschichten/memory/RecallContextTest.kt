package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test

class RecallContextTest {
    private fun scene(text:String):StoryBundle {
        val p=CharacterProfile(id="p",name="Aline",role="Begleitung",genre="Fantasy",traits="Aufmerksam",personality="Klar",scenario="Werkstatt",storyTitle="Werkstatt",openingMessage="„Hallo.“")
        val s=Story(id="s",characterId=p.id,title="Werkstatt")
        val m=ChatMessage(id="original",storyId=s.id,role=ChatRole.USER,text=text)
        return StoryBundle(s,p,listOf(m,ChatMessage(storyId=s.id,role=ChatRole.USER,text="Von wem kam der Kristall?")),emptyList())
    }
    @Test fun adjacentOriginalFamilyClauseArrivesWithGiftAndExactOffsets() {
        val b=scene("Meine Schwester heißt Bera. Dein Bruder heißt Erik. Bera hat dir den Kristall geschenkt.")
        val hit=ArchiveRecall.search(b,b.messages.last().text).single()
        assertFalse(hit.text.contains("Meine Schwester"))
        val e=ArchiveRecall.withContext(b,hit,b.messages.last().text)
        assertEquals(b.messages.first().text,e.text);assertEquals(0,e.start)
        assertTrue(ArchiveRecall.eligible(b,e,b.messages.last().text))
    }
    @Test fun staleContextAndPrivateExcludedOrForeignOriginalsCannotBeReintroduced() {
        val b=scene("Der Kristall liegt auf dem Tisch. Nun lege ich ihn unter die Bank.")
        val processed=b.copy(memory=MemoryRules.preview(b,b.messages.first()))
        val e=ArchiveRecall.sections(b.messages.first()).last()
        assertEquals(e,ArchiveRecall.withContext(processed,e,"Wo liegt der Kristall?"))
        val secret=scene("Ich handele heimlich. Bera hat dir den Kristall geschenkt.")
        assertTrue(ArchiveRecall.search(secret,"Von wem kam der Kristall?").isEmpty())
        val foreign=e.copy(message=e.message.copy(storyId="other"));assertFalse(ArchiveRecall.eligible(b,foreign,"damals"))
        val entity=MemoryRules.identity(b.story.id,EntityKind.ITEM,"Kristall")
        val excluded=b.copy(memory=MemorySnapshot(facts=listOf(StateFact("x",b.story.id,entity,"owner","Bera",FactStatus.EXCLUDED,sourceId=e.message.id))))
        assertFalse(ArchiveRecall.eligible(excluded,e,"damals"))
    }
}
