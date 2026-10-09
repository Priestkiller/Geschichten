package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ArchiveRecallTest {
    private val person=CharacterProfile(id="mira",name="Mira",role="Begleiterin",genre="Fantasy",traits="Neugierig",personality="Mira spricht klar.",scenario="Hof",storyTitle="Hof",openingMessage="„Hallo.“")
    private fun bundle(messages: List<ChatMessage>, memory: MemorySnapshot=MemorySnapshot())=StoryBundle(Story(id="a",characterId="mira",title="Hof"),person,messages,emptyList(),memory)
    private fun m(text: String, role: ChatRole=ChatRole.USER, story: String="a")=ChatMessage(storyId=story,role=role,text=text)

    @Test fun searchReadsTheMiddleOfAFullOriginalAndRetainsExactOffsets() {
        val original=m("Vorspann ".repeat(1000)+"Das Losungswort am Brunnen lautet Morgenstern. "+"Nachspann ".repeat(1000))
        val question=m("Wie lautet das Losungswort am Brunnen?")
        val b=bundle(listOf(original,m("„Gut.“",ChatRole.CHARACTER),question))
        val hits=ArchiveRecall.search(b,question.text)
        assertTrue(hits.any {it.message.id==original.id && it.text.contains("Morgenstern")})
        assertEquals(original.text,ArchiveRecall.sections(original).joinToString("") {it.text})
        hits.forEach {assertEquals(original.text.substring(it.start,it.end),it.text)}
    }
    @Test fun matchingOriginalIsActuallyIncludedBeforeRecentFiller()=runBlocking {
        val old=m("Am Brunnen lautet das Losungswort Morgenstern.")
        val messages=mutableListOf(old,m("„Ich merke es mir.“",ChatRole.CHARACTER))
        repeat(14) {i ->messages+=m("Ein neuer Vorgang Nummer $i. ".repeat(60));messages+=m("*Mira betrachtet die Wand $i.* ".repeat(60),ChatRole.CHARACTER)}
        messages+=m("Wie lautet das Losungswort am Brunnen?")
        val b=bundle(messages)
        val plan=MemoryPrompt.plan(b,false,count={s,h -> (s.length+h.sumOf {it.text.length})/4})
        assertTrue(plan.system.contains("Originalstelle"));assertTrue(plan.system.contains(old.text))
        assertTrue(plan.recalledSources.any {it.message.id==old.id})
        assertTrue(plan.history.any {it.text.contains("Wand 13")})
        assertTrue(plan.inputTokens+512<=4096)
    }
    @Test fun anotherStoryPrivateAndExcludedSourcesNeverEnterRecall() {
        val public=m("Das Losungswort am Brunnen lautet Morgenstern.")
        val private=m("Ich lege den Schlüssel heimlich in den Brunnen.")
        val excluded=m("Das Losungswort lautet Abendrot.")
        val hidden=StateFact("x","a",MemoryRules.identity("a",EntityKind.EVENT,"Notiz"),"note","Abendrot",status=FactStatus.EXCLUDED,sourceId=excluded.id)
        val b=bundle(listOf(public,private,excluded,m("Das Losungswort lautet Fremdwelt.",story="b"),m("Was besprachen wir damals am Brunnen?")),MemorySnapshot(facts=listOf(hidden)))
        val text=ArchiveRecall.search(b,b.messages.last().text).joinToString {it.text}
        assertTrue(text.contains("Morgenstern"));assertFalse(text.contains("heimlich"));assertFalse(text.contains("Abendrot"));assertFalse(text.contains("Fremdwelt"))
        assertTrue(ArchiveRecall.search(b,"Ich suche heimlich das Losungswort am Brunnen.").isEmpty())
    }
    @Test fun currentQueriesDoNotReintroduceSupersededColorsButPastQueriesCanFindThem() {
        val old=m("Der Schlüssel ist rot.");val new=m("Der Schlüssel ist silbern.")
        var b=bundle(emptyList())
        b=b.copy(messages=listOf(old),memory=MemoryRules.preview(b,old))
        b=b.copy(messages=b.messages+new,memory=MemoryRules.preview(b,new))
        b=b.copy(messages=b.messages+m("Welche Farbe hat der Schlüssel jetzt?"))
        assertFalse(ArchiveRecall.search(b,b.messages.last().text).any {it.text.contains("rot")})
        assertTrue(ArchiveRecall.search(b,"Welche Farbe hatte der Schlüssel damals?").any {it.text.contains("rot")})
        val source=ArchiveRecall.sections(old)
        MemoryReplyGuard.validate(b,"Damals: Der Schlüssel ist rot.",source)
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"Damals: Der Schlüssel ist blau.",source)}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"Jetzt: Der Schlüssel ist rot.",source)}
    }
    @Test fun noMatchingOriginalIsNotInventedAndSearchDoesNotChangeState() {
        val b=bundle(listOf(m("Eine Mauer."),m("Welche Farbe hatte das Geschenk meiner Schwester?")))
        val before=b.memory
        assertTrue(ArchiveRecall.search(b,b.messages.last().text).isEmpty())
        assertEquals(before,b.memory)
    }
    @Test fun budgetUsesTheCounterForEveryWholeSourceCandidate()=runBlocking {
        val old=m("Das Losungswort lautet Morgenstern.")
        val b=bundle(listOf(old,m("„Gut.“",ChatRole.CHARACTER),m("Eine Mauer."),m("*Mira schaut hin.*",ChatRole.CHARACTER),m("Wie lautet das Losungswort?")))
        var calls=0
        val plan=MemoryPrompt.plan(b,false,count={s,h -> calls++;(s.length+h.sumOf {it.text.length})/3})
        assertTrue(calls>=3);assertTrue(plan.recalledSources.isNotEmpty());assertTrue(plan.inputTokens+512<=4096)
    }

    @Test fun anInvertedFirstPersonSentenceCannotTurnAPlaceIntoAnOwner() {
        val b=bundle(emptyList())
        val source=m("Am Brunnen habe ich dir den Brief anvertraut.")
        assertFalse(MemoryRules.proposals(b,source).any {it.fact.status==FactStatus.CURRENT && it.fact.value=="Brunnen"})
        val explicit=m("Ich habe den Brief.")
        assertTrue(MemoryRules.proposals(b,explicit).any {it.fact.field=="holder" && it.fact.value=="Du" && it.fact.status==FactStatus.CURRENT})
        val user=m("Ich heiße Rian. Mira hat den Schlüssel.")
        val known=b.copy(messages=listOf(user),memory=MemoryRules.preview(b,user))
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(known,"Der Schlüssel ist blau und er gehört Rian.")}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(known,"Originalstelle 1, Spielerfigur: Ein Brief.")}
    }
}
