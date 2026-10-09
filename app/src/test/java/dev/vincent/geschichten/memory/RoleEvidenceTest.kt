package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RoleEvidenceTest {
    private val char=CharacterProfile(id="mira",name="Mira",role="Gefährtin",genre="Fantasy",traits="Neugierig",personality="Mira spricht klar.",scenario="Hof",storyTitle="Hof",openingMessage="„Hallo.“")
    private fun empty()=StoryBundle(Story(id="a",characterId="mira",title="Hof"),char,emptyList(),emptyList())
    private fun user(b:StoryBundle,text:String):StoryBundle {val m=ChatMessage(storyId="a",role=ChatRole.USER,text=text);return b.copy(messages=b.messages+m,memory=MemoryRules.preview(b,m))}
    private fun established():StoryBundle {
        var b=user(empty(),"Ich heiße Rian. Ich gebe dir den Brief, weil ich mit meiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann.")
        b=b.copy(messages=b.messages+ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="*Mira nickt.* „Verstanden.“"))
        return b
    }
    @Test fun namesDirectionsReasonAndExactSourceAreExtractedWithoutChangingTheOriginal() {
        val b=established();val m=b.messages.first();val events=RoleEvidence.extract(b,m)
        val e=events.first {it.action==RoleAction.TRANSFER}
        assertEquals("Rian",e.actor);assertEquals("Mira",e.target);assertEquals("Brief",e.item)
        assertTrue(e.reason.contains("Rian"));assertTrue(e.reason.contains("Siegel"))
        assertEquals(m.text.substring(e.start,e.end),e.original)
        assertTrue(events.any {it.action==RoleAction.INJURY && it.actor=="Rian"})
        val unresolved=ChatMessage(storyId="a",role=ChatRole.USER,text="Rian gibt Mira den Brief, weil sie mit ihrer verletzten Hand das Siegel nicht unbeschädigt öffnen kann.")
        assertFalse(RoleEvidence.extract(b,unresolved).any {it.action==RoleAction.INJURY})
        assertNull(RoleEvidence.renderNamed(b,ArchiveExcerpt(unresolved,0,unresolved.text.length,1)))
    }
    @Test fun correctRephrasingsAreAcceptedAndReversedRolesAreRejected() {
        val b=established()
        listOf("„Du gabst mir den Brief.“","„Rian hat mir den Brief anvertraut.“","„Du hast den Brief an mich gegeben.“","„Du hast mir den Brief gegeben, weil deine Hand verletzt ist.“").forEach {MemoryReplyGuard.validate(b,it)}
        listOf("„Ich habe dir den Brief anvertraut, weil ich mit meiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann.“","„Ich habe den Brief an dich gegeben.“","*Mira berührt ihre Hand, die verletzt ist.* „Du hast mir den Brief gegeben.“").forEach {assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,it)}}
    }
    @Test fun presentCreativeReturnIsAllowedAndDoesNotRewriteThePast() {
        val b=established()
        MemoryReplyGuard.validate(b,"*Mira gibt Rian den Brief zurück.* „Hier, nimm ihn.“")
        MemoryReplyGuard.validate(b,"„Ich gebe dir den Brief zurück.“")
    }
    @Test fun hypothesesQuestionsAndNegatedSourcesDoNotBecomeConfirmedEvents() {
        val b=user(empty(),"Ich heiße Rian.")
        for(text in listOf("Vielleicht gebe ich dir den Brief.","Habe ich dir den Brief gegeben?","Ich habe dir den Brief nicht gegeben.","Meine Hand ist nicht verletzt.","Meine Hand wäre verletzt.","Wenn ich dir den Brief gebe, ist meine Hand verletzt.","Meine Schwester heißt nicht Alma.")) {
            val m=ChatMessage(storyId="a",role=ChatRole.USER,text=text)
            assertTrue(RoleEvidence.extract(b,m).none {it.knownToCharacter && !it.negated})
        }
    }
    @Test fun attributedQuoteHasItsActualSpeakerAndIsNotAutomaticWorldTruth() {
        val b=established();val quoted=ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="Rian sagte: „Ich habe dir den Brief gegeben.“")
        val e=RoleEvidence.extract(b,quoted).first {it.action==RoleAction.TRANSFER}
        assertEquals("Rian",e.actor);assertEquals("Mira",e.target)
        MemoryReplyGuard.validate(b,quoted.text)
        val reported=ChatMessage(storyId="a",role=ChatRole.USER,text="Mira sagt: „Ich habe den Schlüssel.“")
        assertTrue(MemoryRules.proposals(b,reported).isEmpty())
        assertTrue(RoleEvidence.extract(b,reported).all {it.origin==EvidenceOrigin.UNVERIFIED_MODEL_CLAIM})
    }
    @Test fun nestedOrUnresolvedSpeechAndAmbiguousSameNamesAreNotAssignedInventedRoles() {
        val b=established();val ambiguous=b.copy(character=char.copy(name="Rian"))
        assertTrue(RoleEvidence.extract(ambiguous,b.messages.first()).isEmpty())
        val foreign=ChatMessage(storyId="a",role=ChatRole.USER,text="Alma sagt: „Ich habe dir den Brief gegeben.“")
        assertTrue(RoleEvidence.extract(b,foreign).none {it.action==RoleAction.TRANSFER})
        val third=ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="Aron sitzt am Fenster. Er hält seine verletzte Hand.")
        assertTrue(RoleEvidence.extract(b,third).none {it.action==RoleAction.INJURY})
        val ambiguousItem=ChatMessage(storyId="a",role=ChatRole.USER,text="Ich gebe dir einen zweiten Brief.")
        assertTrue(RoleEvidence.extract(b,ambiguousItem).none {it.action==RoleAction.TRANSFER})
    }
    @Test fun ownershipAndCarryingAreIndependentAndCorrectAnswersAreNotRejected() {
        var b=user(empty(),"Ich heiße Rian. Der Schlüssel gehört mir. Du trägst den Schlüssel für mich.")
        b=user(b,"Wer trägt den Schlüssel und wem gehört er?")
        assertEquals("Du",b.memory.current.first {it.field=="owner"}.value)
        assertEquals("Mira",b.memory.current.first {it.field=="holder"}.value)
        MemoryReplyGuard.validate(b,"„Ich trage den Schlüssel. Der Schlüssel gehört Rian.“")
        MemoryReplyGuard.validate(b,"*Mira trägt den Schlüssel.* „Er gehört dir.“")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"*Mira trägt den Schlüssel.* „Er gehört mir.“")}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Der Schlüssel gehört Mira.“")}
    }
    @Test fun additionalOwnershipClaimsNeedExplicitEvidence() {
        val b=user(empty(),"Ich heiße Rian. Mira hat den Schlüssel.")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Der Schlüssel ist blau und gehört Rian.“")}
    }
    @Test fun familyOwnersAreNotTheSpeakingCharacterByDefault() {
        val b=user(empty(),"Ich heiße Rian. Meine Schwester heißt Alma. Dein Bruder heißt Aron.")
        MemoryReplyGuard.validate(b,"„Deine Schwester heißt Alma. Mein Bruder heißt Aron.“")
        MemoryReplyGuard.validate(b,"„Alma ist deine Schwester und Aron mein Bruder.“")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Meine Schwester heißt Alma. Dein Bruder heißt Aron.“")}
    }
    @Test fun unknownPastAndWrongFamilyReferenceAreRejectedButHonestQuestionsRemain() {
        val b=user(user(empty(),"Ich heiße Rian."),"Welche Farbe hatte das Geschenk meiner Schwester, über das wir angeblich gesprochen haben?")
        MemoryReplyGuard.validate(b,"„Ich weiß nicht, welches Geschenk du meinst. Kannst du mir etwas darüber erzählen?“")
        MemoryReplyGuard.validate(b,"„Nicht das Geschenk meiner Schwester meinst du, sondern deiner. Was weißt du darüber?“")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Wir haben über ein Geschenk meiner Schwester an einem kühlen Morgen gesprochen.“")}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Du meinst das Geschenk meiner Schwester?“")}
    }
    @Test fun secretSourceAndExcludedOriginalCannotSupplyRoleEvidence() {
        val b=user(empty(),"Ich heiße Rian. Ich gebe dir den Brief heimlich, ohne dass du es bemerkst.")
        assertFalse(RoleEvidence.trusted(b).any {it.action==RoleAction.TRANSFER})
    }
    @Test fun storedSourceIdDoesNotConfirmARetrospectiveModelClaim() {
        var b=user(empty(),"Ich heiße Rian.")
        val m=ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="„Ich habe dir den Brief gegeben.“")
        // Simulate a legacy source-linked record, without a user witness.
        val entity=MemoryEntity(id="brief",storyId="a",kind=EntityKind.ITEM,name="Brief")
        val f=StateFact(id="old",storyId="a",entity=entity,field="holder",value="Du",sourceId=m.id,sourceText=m.text)
        b=b.copy(messages=b.messages+m,memory=b.memory.copy(facts=b.memory.facts+f,knowledge=b.memory.knowledge+Knowledge(factId=f.id,knower="character",sourceId=m.id,version=0)))
        assertFalse(RoleEvidence.trusted(b).any {it.action==RoleAction.TRANSFER})
        assertFalse(RoleSourcePolicy.eligible(b,ArchiveExcerpt(m,0,m.text.length,1)))
        val mixed=ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="*Mira gibt Rian den Brief.* „Ich habe dir den Schlüssel gegeben.“")
        val extracted=RoleEvidence.extract(b,mixed)
        assertEquals(EvidenceOrigin.NEW_CREATIVE_ACTION,extracted.first {it.item=="Brief"}.origin)
        assertEquals(EvidenceOrigin.UNVERIFIED_MODEL_CLAIM,extracted.first {it.item=="Schlüssel"}.origin)
    }
    @Test fun legitimateAuthoredPastIsPreservedAndDoesNotBecomeAModelHallucination() {
        val text="*Mira sieht Rian an.* „Meine Schwester heißt Alma. Wir haben über ein Geschenk meiner Schwester gesprochen.“"
        val intro=ChatMessage(storyId="a",role=ChatRole.CHARACTER,text=text,createdAt=101)
        val b=empty().copy(story=empty().story.copy(createdAt=101),character=char.copy(openingMessage=text),messages=listOf(intro))
        assertTrue(RoleEvidence.trusted(b).any {it.action==RoleAction.FAMILY && it.origin==EvidenceOrigin.AUTHORED_START})
        assertEquals(text,RoleSourcePolicy.visible(b,intro))
        MemoryReplyGuard.validate(b,"„Wir haben über ein Geschenk meiner Schwester gesprochen.“")
        assertFalse(RoleEvidence.authoredStart(b,intro.copy(id="generated",createdAt=102)))
    }
    @Test fun namedRenderingUsesActualSourcesButUnprovenConversionIsNotEnabledInProduction()=runBlocking {
        var b=established();repeat(10){b=user(b,"Wir betrachten die Mauer $it.");b=b.copy(messages=b.messages+ChatMessage(storyId="a",role=ChatRole.CHARACTER,text="*Mira betrachtet die Mauer $it.*"))}
        b=user(b,"Warum habe ich dir den Brief gegeben?")
        val counter:suspend (String,List<dev.vincent.geschichten.ai.ModelMessage>)->Int={s,h ->(s.length+h.sumOf {it.text.length})/4}
        val original=MemoryPrompt.plan(b,false,count=counter)
        val experimental=ArchiveRecall.render(b,original.recalledSources,namedEvidence=true)
        assertTrue(experimental.contains("Handelnde Person: Rian"));assertTrue(experimental.contains("Empfänger: Mira"))
        assertFalse(original.system.contains("Handelnde Person: Rian"))
        assertTrue(original.system.contains("Ich gebe dir den Brief"))
        assertTrue(original.inputTokens+512<=4096)
    }
}
