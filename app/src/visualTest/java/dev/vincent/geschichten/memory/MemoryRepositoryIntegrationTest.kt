package dev.vincent.geschichten.memory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.data.*
import com.google.gson.GsonBuilder
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class MemoryRepositoryIntegrationTest {
    private lateinit var context: Context
    private lateinit var repo: StoryRepository
    private val character=CharacterProfile(id="test-mira",name="Mira",role="Gefährtin von Rian",genre="Fantasy",traits="Praktisch, herzlich, vorsichtig",personality="Mira ist 27 Jahre alt. Sie ist praktisch, herzlich, mutig und vorsichtig. Sie spricht klar. Sie kennt Rian, aber nicht seine unausgesprochenen Gedanken.",scenario="Du spielst Rian. Mira und Rian sind im verschlossenen Turmzimmer. Mira besitzt den bronzenen Schlüssel. Rians linke Hand ist verletzt. Ziel: das Turmzimmer verlassen.",storyTitle="Der Turm",openingMessage="*Mira hat den bronzenen Schlüssel.* „Wie geht es deiner Hand, Rian?“",custom=true)
    @Before fun setup(){context=ApplicationProvider.getApplicationContext();context.deleteDatabase("geschichten.db");repo=StoryRepository(context);repo.upsertCharacter(character)}
    @After fun teardown(){repo.close();context.deleteDatabase("geschichten.db")}
    private fun fresh()=repo.createStory(character.id).also {repo.backfillMemory(it.id)}
    private fun bundle(s:Story)=repo.bundle(s.id)!!
    private fun turn(s:Story,text:String,reply:String="*Mira nickt.* „Verstanden.“") {
        val user=repo.appendMessage(s.id,ChatRole.USER,text);val b=bundle(s)
        repo.commitReply(s.id,b.memory.version,user.id,"req-${user.id}",reply)
    }
    private fun state(s:Story,field:String)=bundle(s).memory.current.firstOrNull {it.field==field}
    private fun establish(s:Story)=turn(s,"Ich heiße Rian. Mira hat den bronzenen Schlüssel. Meine linke Hand ist verletzt. Wir sind im Turmzimmer. Ziel Flucht ist offen.")

    @Test fun storiesTransferCorrectionsMoveInjuryAndGoalsStayIndependent(){
        val a=fresh();val b=fresh();establish(a);establish(b)
        val item=state(a,"holder")!!.entity.id
        turn(a,"Mira gibt Rian den Schlüssel.");assertEquals("Du",state(a,"holder")!!.value)
        turn(a,"Ich lege den Schlüssel in die Truhe. Der Schlüssel ist silbern.")
        assertEquals("Niemand",state(a,"holder")!!.value);assertEquals("in der Truhe",state(a,"placement")!!.value);assertEquals(item,state(a,"color")!!.entity.id)
        turn(a,"Wir sind nun im Hof. Ziel Flucht ist abgeschlossen.")
        assertTrue(bundle(a).memory.current.any {it.field.startsWith("injury:") && it.value.contains("verletzt")})
        assertEquals("Abgeschlossen",state(a,"status")!!.value)
        assertEquals("Mira",state(b,"holder")!!.value);assertEquals("bronzen",state(b,"color")!!.value)
        assertTrue(bundle(a).memory.facts.any {it.status==FactStatus.HISTORICAL})
    }
    @Test fun previewIsProvisionalAndCommitIsAtomicIdempotentAndVersioned(){
        val s=fresh();establish(s);val user=repo.appendMessage(s.id,ChatRole.USER,"Ich lege den Schlüssel in die Truhe.")
        val saved=bundle(s);val preview=MemoryRules.preview(saved,user)
        assertTrue(preview.current.any {it.field=="placement" && it.value=="in der Truhe"});assertEquals("Mira",state(s,"holder")!!.value)
        assertTrue(repo.commitReply(s.id,saved.memory.version,user.id,"once","„Verstanden.“"))
        val after=bundle(s);assertEquals(saved.messages.size+1,after.messages.size);assertTrue(user.id in after.memory.scannedSources)
        assertFalse(repo.commitReply(s.id,saved.memory.version,user.id,"once","„Noch einmal.“"));assertEquals(after.messages.size,bundle(s).messages.size)
        val pending=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist blau.");val old=bundle(s)
        repo.editStateFact(s.id,state(s,"color")!!.id,"rot",true)
        assertThrows(IllegalStateException::class.java){repo.commitReply(s.id,old.memory.version,pending.id,"late","„Blau.“")}
        assertEquals(old.messages.size,bundle(s).messages.size);assertEquals("rot",state(s,"color")!!.value)
        repo.deleteLastUserMessageIfUnanswered(s.id)
    }
    @Test fun secretsRevealAndUnknownPastStaySeparate(){
        val s=fresh();establish(s);turn(s,"Ich nehme den Schlüssel.")
        turn(s,"Ich lege den Schlüssel heimlich in die Truhe.")
        assertEquals("Niemand",state(s,"holder")!!.value);assertEquals("in der Truhe",state(s,"placement")!!.value)
        assertFalse(bundle(s).memory.forCharacter().any {it.value.contains("Truhe")})
        turn(s,"Welche Farbe hatte das Geschenk meiner Schwester, über das wir angeblich gesprochen haben?")
        assertFalse(bundle(s).memory.current.any {it.value.contains("Geschenk")})
        turn(s,"Ich sage Mira, der Schlüssel liegt in der Truhe.")
        assertTrue(bundle(s).memory.forCharacter().any {it.value.contains("Truhe")})
        val public=state(s,"placement")!!
        assertTrue(public.sourceText.contains("Ich sage Mira"))
        assertTrue(bundle(s).memory.knowledge.any {it.factId==public.id && it.knower=="character" && it.sourceId==public.sourceId})
        val privateIds=bundle(s).memory.facts.filter {it.sourceText.contains("heimlich")}.map {it.id}.toSet()
        assertFalse(bundle(s).memory.knowledge.any {it.factId in privateIds && it.knower=="character"})
    }
    @Test fun correctionsPinAndExclusionDoNotFreezeOrResurrect(){
        val s=fresh();establish(s);val color=state(s,"color")!!
        repo.editStateFact(s.id,color.id,"silbern",true)
        assertTrue(MemoryPrompt.core(bundle(s),false).contains("silbern"))
        turn(s,"Mira gibt Rian den Schlüssel.");assertEquals("Du",state(s,"holder")!!.value)
        turn(s,"Der Schlüssel ist rot.");assertEquals("rot",state(s,"color")!!.value);assertTrue(state(s,"color")!!.pinned)
        val fact=state(s,"color")!!;repo.editStateFact(s.id,fact.id,fact.value,true,true)
        assertNull(state(s,"color"));repeat(3){repo.backfillMemory(s.id)}
        assertNull(state(s,"color"));assertFalse(MemoryPrompt.core(bundle(s),false).contains("Farbe: rot"))
        turn(s,"Der Schlüssel ist jetzt blau.");assertEquals("blau",state(s,"color")!!.value)
    }
    @Test fun repeatedSummariesCannotRestoreOldStateAndManualNotesStillWork(){
        val s=fresh();establish(s);val note=MemoryEntry(storyId=s.id,kind=MemoryKind.FACT,text="Der Schlüssel ist silbern.",pinned=true)
        repo.upsertMemory(note);assertEquals("silbern",state(s,"color")!!.value)
        repeat(3){repo.updateSummary(s.id,"Mira besitzt einen bronzenen Schlüssel. Die Flucht ist offen.");turn(s,"Eine Frage zur Wand.")}
        assertEquals("silbern",state(s,"color")!!.value)
        assertFalse(MemoryPrompt.core(bundle(s),false).contains("Farbe: bronzen"))
        repo.deleteMemory(note.id);assertNull(state(s,"color"))
        assertTrue(bundle(s).messages.any {it.text.contains("bronzenen")})
    }
    @Test fun fullSourcesCheckpointsAndLateBackfillPreserveChronology(){
        val s=fresh()
        val old=repo.appendMessage(s.id,ChatRole.USER,"Vorspann. ".repeat(500)+"Der Schlüssel ist rot. "+"Nachspann. ".repeat(500))
        repo.appendMessage(s.id,ChatRole.CHARACTER,"„Verstanden.“")
        repeat(6){repo.appendMessage(s.id,ChatRole.USER,"Eine Frage.");repo.appendMessage(s.id,ChatRole.CHARACTER,"„Eine Antwort.“")}
        val correction=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist silbern.");repo.appendMessage(s.id,ChatRole.CHARACTER,"„Ja.“")
        repo.backfillMemory(s.id,1);assertEquals("silbern",state(s,"color")!!.value)
        repeat(6){repo.backfillMemory(s.id,2)}
        assertEquals("silbern",state(s,"color")!!.value);assertTrue(old.id in bundle(s).memory.scannedSources);assertTrue(correction.id in bundle(s).memory.scannedSources)
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READONLY).use {db->
            db.rawQuery("SELECT processed_chars FROM memory_sources WHERE source_id=?",arrayOf(old.id)).use {it.moveToFirst();assertEquals(old.text.length,it.getInt(0))}
        }
    }
    @Test fun restartAndProfileEditRetainFrozenSceneAndMemory(){
        val s=fresh();establish(s);val start=s.startContext
        repo.upsertCharacter(character.copy(personality="Mira ist eigensinnig und humorvoll.",scenario="Ein anderes Land."))
        repo.close();repo=StoryRepository(context)
        assertEquals(start,bundle(s).story.startContext);assertTrue(bundle(s).character.personality.contains("humorvoll"))
        assertEquals("bronzen",state(s,"color")!!.value);assertTrue(bundle(s).memory.scannedSources.isNotEmpty())
    }
    @Test fun migrationFromEightPreservesArchiveManualNotesDraftAndOrder(){
        val s=fresh();establish(s);repo.upsertMemory(MemoryEntry(id="manual-old",storyId=s.id,kind=MemoryKind.FACT,text="Eigene unveränderte Notiz",pinned=true))
        repo.updateSummary(s.id,"Ungeprüfte alte Zusammenfassung")
        val before=bundle(s);context.getSharedPreferences("preferences",0).edit().putString("draft_${s.id}","Mein Entwurf").commit()
        repo.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db->
            db.execSQL("PRAGMA foreign_keys=OFF")
            listOf("memory_character_aliases","memory_note_links","memory_commits","memory_exclusions","memory_sources","fact_knowledge","state_facts","memory_entities","story_memory").forEach {db.execSQL("DROP TABLE $it")}
            db.version=8
        }
        repo=StoryRepository(context);val after=bundle(s)
        assertEquals(before.messages,after.messages);assertEquals(before.character,after.character);assertEquals(before.story,after.story);assertEquals(before.memories,after.memories)
        assertEquals("Mein Entwurf",context.getSharedPreferences("preferences",0).getString("draft_${s.id}",null))
        assertTrue(after.memory.current.any {it.value=="Eigene unveränderte Notiz"})
        assertFalse(after.memory.current.any {it.value=="Ungeprüfte alte Zusammenfassung"})
    }
    @Test fun exportControlledFixturesForRealModelValidation(){
        // These are immutable 0.8.0 diagnostic inputs, not current-generation replies.
        val file=File("../docs/validation/dauerhaftes-gedaechtnis/model-cases.json")
        if(file.exists()) {
            assertEquals(5,com.google.gson.JsonParser.parseString(file.readText()).asJsonArray.size())
            return
        }
        val rows=mutableListOf<Map<String,Any>>()
        fun save(label:String,s:Story,question:String) {repo.appendMessage(s.id,ChatRole.USER,question);val b=bundle(s);rows+=mapOf("case" to label,"bundle" to b.copy(memory=MemoryRules.preview(b,b.messages.last())));repo.deleteLastUserMessageIfUnanswered(s.id)}
        val s=fresh();establish(s);save("short",s,"Wo sind wir, wer hat den Schlüssel und was ist mit meiner Hand?")
        turn(s,"Mira gibt Rian den Schlüssel. Ich lege den Schlüssel in die Truhe.")
        turn(s,"Der Schlüssel ist silbern.");save("container",s,"Welche Farbe hat der Schlüssel und wer trägt ihn jetzt?")
        turn(s,"Ich nehme den Schlüssel. Wir sind nun im Hof. Ziel Flucht ist abgeschlossen.")
        repeat(20){
            repo.appendMessage(s.id,ChatRole.USER,"Wir betrachten die Mauer im Hof.")
            repo.appendMessage(s.id,ChatRole.CHARACTER,"*Mira betrachtet schweigend die Mauer.* "+"Der Stein bleibt kalt. ".repeat(25))
            repo.backfillMemory(s.id)
        }
        repo.updateSummary(s.id,"Rian besitzt den silbernen Schlüssel. Mira und Rian sind im Hof. Rians linke Hand ist verletzt. Ziel Flucht ist abgeschlossen.")
        save("long",s,"Wo sind wir, wer hat welchen Schlüssel, was ist mit meiner Hand und dem Ziel Flucht?")
        save("unknown",s,"Welche Farbe hatte das Geschenk meiner Schwester, über das wir angeblich gesprochen haben? Falls es unbekannt ist, sag das.")
        turn(s,"Ich lege den Schlüssel heimlich in die Truhe.","*Mira betrachtet weiter die Mauer.*")
        save("secret",s,"Mira, weißt du, wo der Schlüssel gerade ist?")
        file.parentFile!!.mkdirs()
        // Keep the fixed fixtures identical throughout a running six-model comparison.
        if(!file.exists()) file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(rows),Charsets.UTF_8)
        assertEquals(5,rows.size)
    }
    @Test fun secretManualEventsNeverLeakThroughOverviewAndInjuriesStaySeparate(){
        val s=fresh();establish(s)
        repo.addStateFact(s.id,EntityKind.EVENT,"Heimlicher Plan","event","Der Schatz ist unter der Brücke.",false)
        assertFalse(bundle(s).memory.overview.contains("Schatz"))
        assertFalse(MemoryPrompt.core(bundle(s),false).contains("Schatz"))
        repo.addStateFact(s.id,EntityKind.PERSON,"Mira","injury","linke Hand: verletzt",true)
        repo.addStateFact(s.id,EntityKind.PERSON,"Mira","injury","rechte Hand: gebrochen",true)
        assertEquals(2,bundle(s).memory.current.count {it.entity.name=="Mira" && it.field.startsWith("injury:")})
        assertThrows(IllegalArgumentException::class.java){repo.addStateFact(s.id,EntityKind.GOAL,"Flucht","status","Vielleicht",true)}
    }
    @Test fun lateBackfillWithIdenticalTimestampsCannotReplaceNewerSource(){
        val s=fresh();val old=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist rot.")
        repo.appendMessage(s.id,ChatRole.CHARACTER,"„Gut.“")
        repeat(5){repo.appendMessage(s.id,ChatRole.USER,"Eine Wandfrage.");repo.appendMessage(s.id,ChatRole.CHARACTER,"„Gut.“")}
        val new=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist silbern.");repo.appendMessage(s.id,ChatRole.CHARACTER,"„Gut.“")
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db ->
            db.execSQL("UPDATE messages SET created_at=? WHERE story_id=?",arrayOf<Any>(old.createdAt,s.id))
        }
        repo.backfillMemory(s.id,1);assertEquals("silbern",state(s,"color")!!.value)
        repeat(8){repo.backfillMemory(s.id,1)}
        assertEquals("silbern",state(s,"color")!!.value)
        assertTrue(bundle(s).memory.scannedSources.containsAll(listOf(old.id,new.id)))
        assertEquals("silbern",bundle(s).memory.forCharacter().first {it.field=="color"}.value)
    }
    @Test fun oneHundredTurnsKeepCurrentStateWithBoundedLocalWork(){
        val s=fresh();establish(s);val timings=mutableListOf<Long>()
        repeat(100) {index ->
            val start=System.nanoTime()
            turn(s,if(index%2==0) "Ich nehme den Schlüssel. Der Schlüssel ist rot." else "Ich lege den Schlüssel in die Truhe. Der Schlüssel ist silbern.")
            timings+=(System.nanoTime()-start)/1_000_000
        }
        assertEquals("Niemand",state(s,"holder")!!.value);assertEquals("in der Truhe",state(s,"placement")!!.value);assertEquals("silbern",state(s,"color")!!.value)
        assertEquals(1,bundle(s).memory.current.count {it.field=="holder"})
        assertTrue(bundle(s).memory.current.any {it.field.startsWith("injury:")})
        val values=timings.sorted();val file=File("../docs/validation/team-0.8.6/regression-output/database-long-run.json")
        file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(mapOf("environment" to "Desktop Robolectric SQLite; no inference, no S24", "turns" to 100,"medianMs" to values[50],"p95Ms" to values[94],"maxMs" to values.last(),"facts" to bundle(s).memory.facts.size,"currentFacts" to bundle(s).memory.current.size,"messages" to bundle(s).messages.size,"dbBytes" to context.getDatabasePath("geschichten.db").length())),Charsets.UTF_8)
    }
    @Test fun manualCorrectionPreservesPrivacyUntilExplicitlyDisclosed(){
        val s=fresh();establish(s);turn(s,"Ich nehme den Schlüssel.");turn(s,"Ich lege den Schlüssel heimlich in die Truhe.")
        val fact=state(s,"holder")!!
        repo.editStateFact(s.id,fact.id,"In/bei Schrank",true)
        assertFalse(bundle(s).memory.forCharacter().any {it.value.contains("Schrank")})
        val changed=state(s,"holder")!!;repo.editStateFact(s.id,changed.id,changed.value,true,known=true)
        assertTrue(bundle(s).memory.forCharacter().any {it.value.contains("Schrank")})
    }
    @Test fun renamingTheProfileKeepsItsPersonIdentityAndOwnerReference(){
        val s=fresh();establish(s)
        val before=bundle(s).memory.current.first {it.entity.name=="Mira" && it.field=="location"}.entity.id
        repo.upsertCharacter(character.copy(name="Tara"))
        assertTrue(MemoryPrompt.core(bundle(s),false).contains("Träger: Tara"))
        turn(s,"Tara ist nun im Hof.")
        assertEquals(before,bundle(s).memory.current.first {it.entity.name=="Tara" && it.field=="location"}.entity.id)
        assertTrue("mira" in bundle(s).memory.characterAliases)
    }

    @Test fun manualCorrectionTriggersBoundedWorkAndIgnoresOldFutureTimestamp() {
        val s=fresh();establish(s)
        val old=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist rot.")
        val response=repo.appendMessage(s.id,ChatRole.CHARACTER,"„Gut.“")
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db ->
            val later=System.currentTimeMillis()+20_000
            db.execSQL("UPDATE messages SET created_at=? WHERE id=?",arrayOf<Any>(later,old.id))
            db.execSQL("UPDATE messages SET created_at=? WHERE id=?",arrayOf<Any>(later+1,response.id))
        }
        repo.editStateFact(s.id,state(s,"color")!!.id,"blau",true)
        assertEquals("blau",state(s,"color")!!.value)
        assertTrue(old.id in bundle(s).memory.scannedSources)
        val version=bundle(s).memory.version;val facts=bundle(s).memory.facts.size
        assertEquals(0,repo.backfillMemory(s.id));assertEquals(version,bundle(s).memory.version)
        assertEquals(facts,bundle(s).memory.facts.size)
        turn(s,"Der Schlüssel ist grün.")
        assertEquals("grün",state(s,"color")!!.value)
    }

    @Test fun acceptedNewCharacterActionIsProcessedImmediatelyWithItsOriginalSource() {
        val s=fresh();establish(s)
        turn(s,"Was tust du?","*Mira legt den Schlüssel in die Truhe.* „Er bleibt dort.“")
        val after=bundle(s)
        assertEquals("Niemand",state(s,"holder")!!.value);assertEquals("in der Truhe",state(s,"placement")!!.value)
        assertEquals(after.messages.last().id,state(s,"holder")!!.sourceId)
        assertTrue(after.messages.takeLast(2).all {it.id in after.memory.scannedSources})
        assertEquals(0,repo.backfillMemory(s.id))
    }

    @Test fun fullArchiveLookupDoesNotAlterAStoredFactAndIsStoryScoped() {
        val a=fresh();val b=fresh();establish(a)
        turn(a,"Das Losungswort am Brunnen lautet Morgenstern.")
        turn(b,"Das Losungswort am Brunnen lautet Abendrot.")
        repeat(10){turn(a,"Ein neuer Vorgang Nummer $it.")}
        val before=bundle(a)
        val hits=repo.searchArchive(a.id,"Wie lautet das Losungswort am Brunnen?")
        assertTrue(hits.any {it.text.contains("Morgenstern")});assertFalse(hits.any {it.text.contains("Abendrot")})
        assertEquals(before,bundle(a))
    }

    @Test fun repositoryRejectsContradictoryCompletedReplyBeforeWritingAnyFacts() {
        val s=fresh();establish(s);turn(s,"Ich nehme den Schlüssel.")
        val user=repo.appendMessage(s.id,ChatRole.USER,"Der Schlüssel ist blau.")
        val before=bundle(s)
        assertThrows(IllegalStateException::class.java){repo.commitReply(s.id,before.memory.version,user.id,"bad","„Ich habe den Schlüssel.“")}
        assertEquals(before,bundle(s));assertFalse(user.id in bundle(s).memory.scannedSources)
        assertEquals("bronzen",state(s,"color")!!.value)
    }
}
