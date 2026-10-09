package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HybridRecallTest {
    private val character=CharacterProfile(id="alva",name="Alva",role="Begleiterin",genre="Fantasy",traits="Vorsichtig",personality="Spricht ruhig.",scenario="Hof",storyTitle="Hof",openingMessage="„Hallo.“")
    private fun m(text: String,story: String="s",role: ChatRole=ChatRole.USER)=ChatMessage(storyId=story,role=role,text=text)
    private fun b(messages: List<ChatMessage>)=StoryBundle(Story(id="s",characterId="alva",title="Hof"),character,messages,emptyList())
    @Test fun reciprocalRankingAddsMeaningHitsAndRetainsExactLexicalIdentity() {
        val exact=ArchiveRecall.sections(m("Die Karte gehört Leander.")).single()
        val meaning=ArchiveRecall.sections(m("Alva hat Angst in engen Räumen.")).single()
        val below=ArchiveRecall.sections(m("Ein Fluss.")).single()
        val result=HybridRecall.merge(listOf(exact),listOf(HybridRecall.Hit(meaning,.5f),HybridRecall.Hit(below,.29f)))
        assertEquals(listOf(exact,meaning),result)
        assertEquals(listOf(exact),HybridRecall.merge(listOf(exact),emptyList()))
    }
    @Test fun cosineRejectsWrongSpacesAndNonFiniteVectors() {
        val x=FloatArray(768).apply {this[0]=1f};val y=FloatArray(768).apply {this[1]=1f}
        assertEquals(1f,HybridRecall.cosine(x,x),.0001f);assertEquals(0f,HybridRecall.cosine(x,y),.0001f)
        assertTrue(HybridRecall.cosine(x,FloatArray(768)).isNaN())
        assertThrows(IllegalArgumentException::class.java){HybridRecall.cosine(x,FloatArray(128))}
    }
    @Test fun semanticCandidatesCannotBypassStorySecretsExclusionsOrUnconfirmedModelPast() {
        val secret=m("Heimlich lege ich den Ring in die Tasche.")
        val excluded=m("Das Losungswort lautet Sonnenfels.")
        val model=m("Ich erinnere mich: Deine Schwester hat mir den Ring geschenkt.",role=ChatRole.CHARACTER)
        val other=m("Der Ring liegt dort.",story="other")
        val question=m("Was weißt du über den Ring?")
        val bundle=b(listOf(secret,excluded,model,model.copy(id="repeat"),other,question)).let {it.copy(memory=MemorySnapshot(facts=listOf(StateFact("excluded","s",MemoryRules.identity("s",EntityKind.EVENT,"Quelle"),"note","excluded",FactStatus.EXCLUDED,sourceId=excluded.id))))}
        assertTrue(ArchiveRecall.candidates(bundle,question.text).isEmpty())
    }
    @Test fun oldLocationsCannotResetCurrentCorrections() {
        val old=m("Die Karte liegt auf der Bank.");val correct=m("Die Karte liegt auf dem Tisch.");val question=m("Wo liegt die Karte jetzt?")
        var bundle=b(listOf(old,correct,question));bundle=bundle.copy(memory=MemoryRules.preview(bundle,old));bundle=bundle.copy(memory=MemoryRules.preview(bundle,correct))
        assertFalse(ArchiveRecall.eligible(bundle,ArchiveRecall.sections(old).single(),question.text))
        assertTrue(ArchiveRecall.eligible(bundle,ArchiveRecall.sections(old).single(),"Wo lag sie früher?"))
    }
    @Test fun revalidationRejectsStaleSourceRevisionsAndDeletedOriginals() {
        val original=m("Alva meidet enge Räume.");val e=ArchiveRecall.sections(original).single();val question=m("Wie reagierst du im Tunnel?")
        assertFalse(ArchiveRecall.eligible(b(listOf(original.copy(text="Alva liebt enge Räume."),question)),e,question.text))
        assertFalse(ArchiveRecall.eligible(b(listOf(question)),e,question.text))
    }
    @Test fun selectedMeaningOriginalActuallyArrivesWithinBudgetAndQuestionOnce()=runBlocking {
        val original=m("Alva bekommt in engen dunklen Räumen Panik.");val question=m("Wie reagierst du auf den schmalen Tunnel?")
        val bundle=b(listOf(original,m("„Gut.“",role=ChatRole.CHARACTER),m("Eine Mauer."),m("*Alva nickt.*",role=ChatRole.CHARACTER),question))
        val e=ArchiveRecall.sections(original).single()
        val plan=MemoryPrompt.planWithRecall(bundle,false,listOf(e),count={s,h -> (s.length+h.sumOf {it.text.length})/3})
        assertTrue(plan.system.contains(original.text));assertEquals(listOf(e),plan.recalledSources)
        val prompt=plan.system+plan.history.joinToString(""){it.text}
        assertEquals(prompt.indexOf(question.text),prompt.lastIndexOf(question.text))
        assertTrue(plan.inputTokens+plan.reserve<=4096)
    }
}
