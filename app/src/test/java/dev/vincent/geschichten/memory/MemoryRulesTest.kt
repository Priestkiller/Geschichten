package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.ai.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MemoryRulesTest {
    @Test fun knowingAnItemDoesNotRevealItsSecretPlacementToACharacterAction() {
        var b=update(empty(),"Ich nehme den Ring.")
        b=update(b,"Ich lege den Ring heimlich unter das Bett.")
        assertFalse(MemoryPrompt.core(b,false).contains("Bett"))
        assertThrows(IllegalStateException::class.java) {MemoryReplyGuard.validate(b,"Mira nimmt den Ring.")}
        b=update(b,"Der Ring liegt unter dem Bett.")
        assertEquals("Niemand",b.memory.forCharacter().single {it.field=="holder"}.value)
        MemoryReplyGuard.validate(b,"Mira nimmt den Ring.")
        b=update(b,"Mira nimmt den Ring.",ChatRole.CHARACTER)
        b=update(b,"Mira legt den Ring auf den Tisch. Mira nimmt den Ring wieder.",ChatRole.CHARACTER)
        assertEquals("Mira",b.memory.current.single {it.field=="holder"}.value)
        assertEquals("Nicht abgelegt",b.memory.current.single {it.field=="placement"}.value)
        var hidden=update(empty(),"Der Ring gehört Mira. Ich lege ihn heimlich in die Truhe.")
        hidden=update(hidden,"Ich sage Mira, der Ring liegt in der Truhe.")
        assertFalse(hidden.memory.forCharacter().any {it.field=="owner"})
    }
    @Test fun pendingLatestInputDoesNotFalselyLabelTheProcessedArchiveUnknown() {
        val old=ChatMessage(storyId="a",role=ChatRole.USER,text="Ich nehme den Ring.")
        val last=ChatMessage(storyId="a",role=ChatRole.USER,text="Wer hält ihn?")
        val b=empty().copy(messages=listOf(old,last),memory=MemorySnapshot(scannedSources=setOf(old.id),pendingSources=1))
        assertFalse(MemoryPrompt.core(b,false).contains("Ältere Quellen sind teilweise"))
        assertTrue(MemoryPrompt.core(b.copy(memory=b.memory.copy(scannedSources=emptySet(),pendingSources=2)),false).contains("Ältere Quellen sind teilweise"))
    }
    @Test fun ordinaryCapitalizedNounsAreNotPeopleAndAliasesKeepCreativeActionsGrounded() {
        var b=update(empty(),"Ich habe das Gerät.")
        b=update(b,"Vor Begeisterung hält sie das Gerät.",ChatRole.CHARACTER)
        assertFalse(b.memory.facts.any {it.field=="holder" && it.value=="Begeisterung"})
        b=update(b,"Mira hat den Schlüssel.")
        b=b.copy(character=profile.copy(name="Tara"),memory=b.memory.copy(characterAliases=setOf("mira","tara")))
        b=update(b,"Tara legt den Schlüssel auf den Tisch.",ChatRole.CHARACTER)
        assertEquals("auf dem Tisch",b.memory.current.first {it.field=="placement" && it.entity.name=="Schlüssel"}.value)
    }
    @Test fun perfectActionAuxiliariesDoNotBecomeCurrentCarryingClaims() {
        var b=update(empty(),"Ich heiße Rian. Ich schenke Mira den Ring. Mira legt ihn hinter die Truhe.")
        MemoryReplyGuard.validate(b,"„Der Ring gehört mir. Ich habe ihn gerade hinter die Truhe gelegt.“")
        val absent=update(empty(),"Ich habe den Ring nicht gefunden.")
        assertFalse(absent.memory.current.any {it.field=="holder"})
        val formal=update(empty(),"Ich heiße Rian. Der Trank gehört mir. Ich nehme ihn.")
        MemoryReplyGuard.validate(formal,"„Der Trank gehört Ihnen.“")
    }
    private val profile=CharacterProfile(id="mira",name="Mira",role="Begleiterin",genre="Fantasy",traits="Neugierig",personality="Mira spricht klar und freundlich.",scenario="Turm",storyTitle="Turm",openingMessage="„Gehen wir?“")
    private fun empty(story: String = "a")=StoryBundle(Story(id=story,characterId="mira",title="Turm"),profile,emptyList(),emptyList())
    private fun update(b:StoryBundle,text:String,role:ChatRole=ChatRole.USER):StoryBundle {
        val m=ChatMessage(storyId=b.story.id,role=role,text=text)
        return b.copy(messages=b.messages+m,memory=MemoryRules.preview(b,m))
    }
    private fun value(b:StoryBundle,field:String)=b.memory.current.firstOrNull {it.field == field}?.value
    @Test fun transferAndContainerReplaceTheSameItemSlot() {
        var b=update(empty(),"Ich heiße Rian. Mira hat den bronzenen Schlüssel.")
        val id=b.memory.current.first {it.field=="holder"}.entity.id
        b=update(b,"Mira gibt Rian den Schlüssel.");assertEquals("Du",value(b,"holder"))
        b=update(b,"Ich lege den Schlüssel in die Truhe.");assertEquals("Niemand",value(b,"holder"));assertEquals("in der Truhe",value(b,"placement"))
        assertEquals(id,b.memory.current.first {it.field=="holder"}.entity.id)
        assertEquals(1,b.memory.current.count {it.field=="holder"})
        assertEquals(2,b.memory.facts.count {it.field=="holder" && it.status==FactStatus.HISTORICAL})
    }
    @Test fun correctionChangesColorWithoutDuplicatingItem() {
        var b=update(empty(),"Mira hat den bronzenen Schlüssel.")
        b=update(b,"Der Schlüssel ist tatsächlich silbern.")
        assertEquals("silbern",value(b,"color"));assertEquals(1,b.memory.current.map {it.entity.id}.distinct().size)
    }
    @Test fun locationPreservesMultipleInjuries() {
        var b=update(empty(),"Meine linke Hand ist verletzt. Meine rechte Hand ist gebrochen.")
        b=update(b,"Wir sind nun im Hof.")
        assertEquals(2,b.memory.current.count {it.field.startsWith("injury:")})
        assertEquals(2,b.memory.current.count {it.entity.kind == EntityKind.PERSON && it.field=="location" && it.value=="Hof"})
    }
    @Test fun completedGoalRequiresExplicitReopening() {
        var b=update(empty(),"Ziel Flucht ist offen.");b=update(b,"Ziel Flucht ist abgeschlossen.")
        b=update(b,"Ziel Flucht ist offen.");assertEquals("Abgeschlossen",value(b,"status"))
        b=update(b,"Ziel Flucht ist wieder aufgenommen.");assertEquals("Offen",value(b,"status"))
    }
    @Test fun secretChangesWorldButNotKnowledgeUntilTold() {
        var b=update(empty(),"Ich habe den Schlüssel.")
        // The supported stative uses 'hat', the first person action establishes ownership.
        b=update(b,"Ich nehme den Schlüssel.")
        b=update(b,"Ich lege den Schlüssel heimlich in die Truhe.")
        assertEquals("Niemand",value(b,"holder"));assertEquals("in der Truhe",value(b,"placement"))
        assertFalse(b.memory.forCharacter().any {it.value.contains("Truhe")})
        assertFalse(MemoryRules.visibleText(b.messages.last()).contains("Truhe"))
        b=update(b,"Ich sage Mira, der Schlüssel liegt in der Truhe.")
        assertTrue(b.memory.forCharacter().any {it.value.contains("Truhe")})
        assertEquals("Niemand",b.memory.forCharacter().single {it.field=="holder"}.value)
    }
    @Test fun questionsHypothesesAndPastClaimsDoNotOverwriteFacts() {
        var b=update(empty(),"Der Schlüssel ist silbern.")
        b=update(b,"Ist der Schlüssel rot? Vielleicht ist der Schlüssel blau. Gestern war der Schlüssel schwarz.")
        assertEquals("silbern",value(b,"color"));assertFalse(b.memory.forCharacter().any {it.value=="blau"})
    }
    @Test fun generatedUnfoundedClaimsRemainUncertain() {
        var b=update(empty(),"Ich nehme den Schlüssel.")
        b=update(b,"Mira hat den bronzenen Schlüssel.",ChatRole.CHARACTER)
        assertEquals("Du",value(b,"holder"));assertFalse(b.memory.current.any {it.field=="color"})
        assertTrue(b.memory.facts.any {it.status==FactStatus.UNCERTAIN})
    }
    @Test fun clearNewCharacterActionRequiresPriorOwnership() {
        var b=update(empty(),"Mira hat den Schlüssel.")
        b=update(b,"Mira legt den Schlüssel in die Truhe.",ChatRole.CHARACTER)
        assertEquals("Niemand",value(b,"holder"));assertEquals("in der Truhe",value(b,"placement"))
    }
    @Test fun uncertainOrExcludedFactsAreNeverPrompted() {
        val b=update(empty(),"Vielleicht ist der Schlüssel blau.")
        assertFalse(MemoryPrompt.core(b,false).contains("blau"))
        assertFalse(MemoryPrompt.core(b,false).contains("FRÜHERE ZUSAMMENFASSUNG"))
    }
    @Test fun tokenizerBudgetIsBindingAndLatestStateIndivisible()=runBlocking {
        var b=update(empty(),"Der Schlüssel ist silbern.")
        repeat(8){b=update(b,"Eine sehr lange Frage. ".repeat(30));b=update(b,"Eine sehr lange Antwort. ".repeat(30),ChatRole.CHARACTER)}
        b=update(b,"Welche Farbe hat er?")
        // Deterministic test tokenizer with a deliberately different token/character ratio.
        val plan=MemoryPrompt.plan(b,false,context=3000,reserve=512,count={s,h->s.length+h.sumOf {it.text.length}})
        assertTrue(plan.inputTokens+512<=3000);assertTrue(plan.omittedSourceIds.isNotEmpty())
        assertTrue(plan.system.contains("silbern"));assertTrue(plan.history.last().text.endsWith("Welche Farbe hat er?"))
    }
    @Test fun oversizedRequiredContextIsReportedNotSilentlyDropped()=runBlocking {
        val b=update(empty(),"Welche Farbe hat er?")
        try {MemoryPrompt.plan(b,false,context=512,reserve=512,count={_,_->1});fail("Expected explicit overflow")}
        catch(e:IllegalStateException){assertTrue(e.message!!.contains("nichts gelöscht"))}
    }
    @Test fun wholeLongSourceIsScannedIncludingItsMiddle() {
        val b=update(empty(),"Vorspann. ".repeat(700)+"Der Schlüssel ist silbern. "+"Nachspann. ".repeat(700))
        assertEquals("silbern",value(b,"color"))
    }
    @Test fun replyGuardRejectsContradictionButAllowsGroundedCreativeChange() {
        val b=update(empty(),"Mira hat den silbernen Schlüssel.")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"Mira hat den roten Schlüssel.")}
        MemoryReplyGuard.validate(b,"Mira legt den Schlüssel in die Truhe.")
        MemoryReplyGuard.validate(b,"„Könnte der Schlüssel rot sein?“")
    }
    @Test fun uppercaseCompletedGoalAndExplicitTrustHaveSources() {
        var b=update(empty(),"Ziel Flucht ist Abgeschlossen.")
        assertEquals("Abgeschlossen",value(b,"status"))
        b=update(b,"Mira vertraut mir, weil ich ihr geholfen habe.")
        assertTrue(b.memory.current.any {it.field=="trust" && it.sourceText.contains("weil")})
    }
    @Test fun speakerPronounsAndNegatedPossessionAreNotMixed() {
        var b=update(empty(),"Ich heiße Rian. Du hast den Schlüssel.")
        assertEquals("Mira",value(b,"holder"))
        b=update(b,"Ich habe den Schlüssel.");assertEquals("Du",value(b,"holder"))
        MemoryReplyGuard.validate(b,"„Ich habe den Schlüssel nicht.“")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"„Ich habe den Schlüssel.“")}
        val input=update(b,"Wo ist der Schlüssel?")
        val prompt=MemoryPrompt.core(input,false)
        assertTrue(prompt.contains("Träger: Rian (Spielerfigur)"))
    }
    @Test fun repeatedValueWithinOneSourceKeepsTheFinalChange() {
        val b=update(empty(),"Der Schlüssel ist rot. Der Schlüssel ist blau. Der Schlüssel ist rot.")
        assertEquals("rot",value(b,"color"))
        assertEquals(2,b.memory.facts.count {it.field=="color" && it.status==FactStatus.HISTORICAL})
    }
    @Test fun supersededOpeningFactsDoNotReturnThroughBackground()=runBlocking {
        var b=empty().copy(story=empty().story.copy(startContext="Mira hat den bronzenen Schlüssel."))
        b=update(b,"Mira hat den bronzenen Schlüssel.",ChatRole.CHARACTER)
        b=update(b,"Der Schlüssel ist silbern. Ich nehme den Schlüssel.")
        b=update(b,"„Gut.“",ChatRole.CHARACTER);b=update(b,"Welche Farbe hat er?")
        val plan=MemoryPrompt.plan(b,false,count={s,h -> (s.length+h.sumOf {it.text.length})/4})
        assertFalse(plan.system.contains("bronzenen"));assertFalse(plan.history.any {it.text.contains("bronzenen") && !it.user})
        assertTrue(plan.system.contains("silbern"))
    }
    @Test fun multiplePromisesPersistAndTrustChangesHaveAReason() {
        var b=update(empty(),"Ich verspreche dir, die Tür zu öffnen.",ChatRole.CHARACTER)
        b=update(b,"Ich verspreche dir, deinen Brief zu überbringen.",ChatRole.CHARACTER)
        assertEquals(2,b.memory.current.count {it.field.startsWith("promise:")})
        b=update(b,"Mira vertraut mir, weil ich ihr geholfen habe.")
        b=update(b,"Mira misstraut mir, weil ich sie belogen habe.")
        assertEquals(1,b.memory.current.count {it.field=="trust"})
        assertTrue(b.memory.current.first {it.field=="trust"}.value.contains("misstraut"))
    }
    @Test fun conjunctionDoesNotBecomeOwnerAndPromptLeaksAreRejected() {
        val held=update(empty(),"Ich heiße Rian. Mira hat den Schlüssel.")
        MemoryReplyGuard.validate(held,"*Mira blickt Rian an und nimmt den Schlüssel in ihre Hand.* „Ich habe den Schlüssel.“")
        val stored=update(held,"Ich lege den Schlüssel in die Truhe.")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(stored,"„Der Schlüssel ist silbern und jetzt trägt Mira ihn.“")}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(held,"Schlüssel · Farbe: silbern\nNÄCHSTE NUTZERNACHRICHT:\nWo ist er?")}
    }
    @Test fun negationLaterInAClauseDoesNotHideAnEarlierContradiction() {
        val b=update(empty(),"Ich nehme den Schlüssel. Ziel Flucht ist abgeschlossen.")
        MemoryReplyGuard.validate(b,"Ich habe den Schlüssel nicht gesehen.")
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"Ich habe den Schlüssel, aber ich weiß nicht, wo er liegt.")}
        assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,"Ziel Flucht ist noch offen.")}
    }
    @Test fun identicalPastTurnsDoNotFillTheContextRepeatedly()=runBlocking {
        var b=empty();repeat(20){b=update(b,"Wir betrachten die Mauer.");b=update(b,"*Mira betrachtet die Mauer.*",ChatRole.CHARACTER)}
        b=update(b,"Was geschieht jetzt?")
        val plan=MemoryPrompt.plan(b,false,count={s,h -> (s.length+h.sumOf {it.text.length})/4})
        assertEquals(1,plan.history.count {it.text=="*Mira betrachtet die Mauer.*"})
        assertEquals(41,b.messages.size);assertEquals(38,plan.omittedSourceIds.size)
    }
}
