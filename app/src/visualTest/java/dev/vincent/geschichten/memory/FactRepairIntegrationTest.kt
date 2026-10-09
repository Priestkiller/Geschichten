package dev.vincent.geschichten.memory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class FactRepairIntegrationTest {
    private val context get()=ApplicationProvider.getApplicationContext<Context>()
    private val gson=FactGoldHarness.gson
    private val originals=File("../docs/validation/quality-search-0.8.3/materialized-scenes.json")
    private fun oldFixtures()=JsonParser.parseString(originals.readText()).asJsonArray.associate {it.asJsonObject["case"].asString to it.asJsonObject}
    @Test fun laterPublicConfirmationOfTheSameValueProtectsItFromOlderGiftRepair() {
        context.deleteDatabase("geschichten.db")
        val fixture=gson.fromJson(oldFixtures().getValue("hold-family")["bundle"],StoryBundle::class.java)
        StoryRepository(context).use {r->
            val legacy=seedLegacy(r,fixture)
            val user=r.appendMessage(legacy.story.id,ChatRole.USER,"Ruth hat den Kristall.")
            r.commitReply(legacy.story.id,r.bundle(legacy.story.id)!!.memory.version,user.id,user.id,"„Verstanden.“")
            while(r.backfillMemory(legacy.story.id,8)>0){}
            val current=r.bundle(legacy.story.id)!!.memory.current.single {it.field=="holder"}
            assertEquals("Ruth",current.value);assertEquals(user.id,current.sourceId)
            assertEquals("Enna",r.bundle(legacy.story.id)!!.memory.current.single {it.field=="owner"}.value)
        }
    }
    @Test fun exactRequestedGiftPlacementPickupAndReturnKeepOneItemAndItsOwner() {
        context.deleteDatabase("geschichten.db")
        val row=FactGoldHarness.rows("new-cases-frozen.json").single {it["case"].asString=="dev-gift"}
        val profile=FactGoldHarness.fresh(row).character
        StoryRepository(context).use {r->
            r.upsertCharacter(profile);val s=r.createStory(profile.id)
            fun turn(text:String) {val m=r.appendMessage(s.id,ChatRole.USER,text);r.commitReply(s.id,r.bundle(s.id)!!.memory.version,m.id,m.id,"„Verstanden.“")}
            turn("Ich heiße Lena.");turn(row["source"].asString)
            val first=r.bundle(s.id)!!;assertEquals(emptyList<String>(),FactGoldHarness.issues(row,first))
            val id=first.memory.current.first {it.entity.kind==EntityKind.ITEM}.entity.id
            fun check(holder:String,place:String) {
                val b=r.bundle(s.id)!!;val item=b.memory.current.filter {it.entity.kind==EntityKind.ITEM}
                assertEquals(setOf(id),item.map {it.entity.id}.toSet())
                assertEquals("Arik",item.single {it.field=="owner"}.value)
                assertEquals(holder,item.single {it.field=="holder"}.value)
                assertEquals(place,item.single {it.field=="placement"}.value)
                assertTrue(MemoryPrompt.core(b,false).contains("Ring · Ablage: $place"))
            }
            check("Niemand","auf der Kommode")
            turn("Arik nimmt den Ring von der Kommode auf.");check("Arik","Nicht abgelegt")
            turn("Arik gibt Lena den Ring zurück.");check("Du","Nicht abgelegt")
            turn("Lena legt ihn unter den Tisch.");check("Niemand","unter dem Tisch")
        }
    }
    /** Import frozen v1 derivations and their exact sources into a real schema-9 file.
     * No v2 parsing is used to define the initial state or the gold. */
    private fun seedLegacy(r:StoryRepository,fixture:StoryBundle):StoryBundle {
        r.upsertCharacter(fixture.character);val s=r.createStory(fixture.character.id)
        fixture.messages.drop(1).forEach {r.appendMessage(s.id,it.role,it.text)}
        val b=r.bundle(s.id)!!;val mapping=fixture.messages.zip(b.messages).associate {it.first.id to it.second.copy(createdAt=it.first.createdAt)}
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db->
            db.beginTransaction()
            try {
                db.execSQL("UPDATE stories SET created_at=? WHERE id=?",arrayOf<Any>(fixture.story.createdAt,s.id))
                for(m in mapping.values)db.execSQL("UPDATE messages SET created_at=? WHERE id=?",arrayOf<Any>(m.createdAt,m.id))
                for(f in fixture.memory.facts) {
                    val source=mapping[f.sourceId]
                    val entity=if(f.entity.kind==EntityKind.PERSON && f.entity.name==fixture.character.name)MemoryRules.characterIdentity(s.id,fixture.character) else MemoryRules.identity(s.id,f.entity.kind,f.entity.name)
                    val knowers=fixture.memory.knowledge.filter {it.factId==f.id}.map {it.knower}.toSet()
                    MemoryDatabase.apply(db,f.copy(storyId=s.id,entity=entity,sourceId=source?.id,createdAt=source?.createdAt ?: f.createdAt,knownBy=knowers),source)
                }
                for(m in b.messages.dropLast(1))db.execSQL("INSERT INTO memory_sources(story_id,source_id,processed_chars,version) VALUES (?,?,?,?)",arrayOf<Any>(s.id,m.id,m.text.length,MemoryDatabase.version(db,s.id)))
                db.setTransactionSuccessful()
            } finally {db.endTransaction()}
        }
        return r.bundle(s.id)!!
    }
    @Test fun schemaNineRepairPreservesOriginalsHistoryAndKnowledgeAndFeedsIndependentGold()=runBlocking {
        context.deleteDatabase("geschichten.db")
        val source=oldFixtures();val results=mutableListOf<Map<String,Any>>();val inference=JsonArray()
        StoryRepository(context).use {r->
            for(row in FactGoldHarness.rows("gold-regressions-reviewed.json")) {
                val input=source.getValue(row["case"].asString);val fixture=gson.fromJson(input["bundle"],StoryBundle::class.java)
                val before=seedLegacy(r,fixture)
                val beforeErrors=FactGoldHarness.issues(row,before)
                while(r.backfillMemory(before.story.id,8)>0){}
                val after=r.bundle(before.story.id)!!
                val errors=FactGoldHarness.issues(row,after)
                assertEquals(before.messages,after.messages)
                assertTrue(after.memory.facts.map {it.id}.containsAll(before.memory.facts.map {it.id}))
                assertTrue(after.memory.knowledge.containsAll(before.memory.knowledge))
                val stable=after.memory.version;r.backfillMemory(after.story.id,8);assertEquals(stable,r.bundle(after.story.id)!!.memory.version)
                val plan=MemoryPrompt.plan(after,false,count={s,h->(s.length+h.sumOf {it.text.length})/4})
                val provided=plan.system+plan.history.joinToString("\n"){it.text}
                val player=after.memory.current.firstOrNull {it.field=="identity" && it.entity.name=="Du"}?.value ?: "Spielerfigur"
                fun named(s:String)=if(s=="Du") "$player (Spielerfigur)" else s
                val absent=row.getAsJsonArray("gold").filter {g->
                    val a=g.asJsonArray;val f=after.memory.current.singleOrNull {it.entity.kind.name==a[0].asString && it.entity.name==a[1].asString && it.field==a[2].asString}
                    val line=if(f==null) "MISSING GOLD" else "${named(a[1].asString)} · ${f.label}: ${if(a[2].asString in setOf("holder","owner"))named(a[3].asString) else a[3].asString}"
                    !plan.system.contains(line) || !plan.history.last().text.contains(line)
                }
                results+=mapOf("case" to row["case"].asString,"beforeIssues" to beforeErrors,"afterIssues" to errors,"before" to before.memory,"after" to after.memory,"system" to plan.system,"history" to plan.history,"provisionIssues" to absent,"originalsPreserved" to true,"oldFactIdsPreserved" to true,"knowledgePreserved" to true,"idempotent" to true)
                val actual=input.deepCopy();actual.add("bundle",gson.toJsonTree(after));actual.addProperty("condition","C: actual real SQLite legacy repair, independent gold checked")
                if(row["case"].asString in setOf("hold-family","hold-reverse","hold-correction"))inference.add(actual)
            }
        }
        FactGoldHarness.write("repair-database-audit.json",results)
        File("../docs/validation/team-0.8.6/regression-output/automatic-scenes.json").writeText(gson.toJson(inference))
        assertTrue(results.filter {(it["afterIssues"] as List<*>).isNotEmpty() || (it["provisionIssues"] as List<*>).isNotEmpty()}.toString(),results.all {(it["afterIssues"] as List<*>).isEmpty() && (it["provisionIssues"] as List<*>).isEmpty()})
    }
    @Test fun actualSchemaNineMigrationAddsOnlyAtomicRepairCheckpoints() {
        context.deleteDatabase("geschichten.db")
        val fixture=gson.fromJson(oldFixtures().getValue("hold-family")["bundle"],StoryBundle::class.java)
        lateinit var before:StoryBundle
        StoryRepository(context).use {r->before=seedLegacy(r,fixture)}
        SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db->db.execSQL("DROP TABLE memory_rule_repairs");db.version=9}
        StoryRepository(context).use {r->
            assertEquals(before,r.bundle(before.story.id))
            SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READONLY).use {db->assertEquals(10,db.version);db.rawQuery("SELECT COUNT(*) FROM memory_rule_repairs",null).use {assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}}
            r.backfillMemory(before.story.id,8);assertEquals("Enna",r.bundle(before.story.id)!!.memory.current.first {it.field=="owner"}.value)
        }
    }
    @Test fun manualSlotsNewerEventsExclusionsAndRevokedKnowledgeSurviveRepair() {
        for(mode in listOf("manual","newer","excluded","knowledge","pinned")) {
            context.deleteDatabase("geschichten.db")
            val fixture=gson.fromJson(oldFixtures().getValue("hold-correction")["bundle"],StoryBundle::class.java)
            StoryRepository(context).use {r->
                val before=seedLegacy(r,fixture);val old=before.memory.current.first {it.field=="holder"}
                // Directly establish user controls before repair, preserving older sources.
                SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,SQLiteDatabase.OPEN_READWRITE).use {db->
                    when(mode) {
                        "manual"->MemoryDatabase.apply(db,old.copy(id="manual-control",value="Uta",manual=true,sourceId=null,sourceText="Manuelle Korrektur",createdAt=System.currentTimeMillis()+10),null)
                        "excluded"->{db.execSQL("UPDATE state_facts SET status='EXCLUDED' WHERE entity_id=?",arrayOf(old.entity.id));db.execSQL("INSERT INTO memory_exclusions(story_id,event_id,created_at) VALUES (?,?,?)",arrayOf<Any>(before.story.id,old.id,0))}
                        "knowledge"->db.execSQL("DELETE FROM fact_knowledge WHERE fact_id=? AND knower='character'",arrayOf(old.id))
                        "pinned"->db.execSQL("UPDATE state_facts SET pinned=1 WHERE id=?",arrayOf(old.id))
                    }
                }
                if(mode=="newer") {
                    val m=r.appendMessage(before.story.id,ChatRole.USER,"Die Tasche liegt auf dem Tisch.");val b=r.bundle(before.story.id)!!
                    r.commitReply(b.story.id,b.memory.version,m.id,"newer-request","„Verstanden.“")
                }
                while(r.backfillMemory(before.story.id,8)>0){}
                val after=r.bundle(before.story.id)!!
                assertTrue(after.messages.take(before.messages.size)==before.messages)
                when(mode) {
                    "manual"->{assertEquals("Uta",after.memory.current.first {it.field=="holder"}.value);assertFalse(after.memory.current.any {it.field=="placement"})}
                    "newer"->assertEquals("auf dem Tisch",after.memory.current.first {it.field=="placement"}.value)
                    "excluded"->assertFalse(after.memory.forCharacter().any {it.entity.kind==EntityKind.ITEM})
                    "knowledge"->assertFalse(after.memory.forCharacter().any {it.field=="placement" && it.value=="in dem Schrank"})
                    "pinned"->assertTrue(after.memory.current.first {it.field=="placement"}.pinned)
                }
            }
        }
    }
}
