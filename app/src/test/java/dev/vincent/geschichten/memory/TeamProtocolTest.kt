package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TeamProtocolTest {
    private val rows get()=JsonParser.parseString(File("../docs/validation/team-0.8.6/phase-a-complete-frozen.json").readText()).asJsonArray.map {it.asJsonObject}
    private val gson=Gson()
    private fun row(id:String)=rows.first {it["case"].asString==id}
    private fun frame(w:TeamWork,facts:JsonArray)=JsonObject().apply {addProperty("story",w.story);addProperty("request",w.request);addProperty("version",w.version);add("facts",facts);add("unknown",JsonArray())}.toString()
    @Test fun exactLiteralSourceDoesNotCertifySwappedRolesAndInstructionsCannotWriteState() {
        val r=row("facts-hold-family");val b=gson.fromJson(r["bundle"],StoryBundle::class.java);val w=gson.fromJson(r["work"],TeamWork::class.java)
        val src=w.sources.first {it.quote.contains("Meine Schwester")}
        fun proposal(actor:String)=JsonObject().apply {addProperty("type","family");addProperty("actor",actor);addProperty("target","Ruth");addProperty("item","");addProperty("field","");addProperty("value","Schwester");addProperty("source",src.key);addProperty("quote","Meine Schwester heißt Ruth.")}
        val wrong=JsonArray().apply {add(proposal("Enna"))};assertTrue(TeamProtocol.interpret(b,w,frame(w,wrong)).proposals.isEmpty())
        val valid=JsonArray().apply {add(proposal("Silas"))};val result=TeamProtocol.interpret(b,w,frame(w,valid));assertEquals(listOf("Ruth ist Schwester von Silas."),result.cues)
        TeamProtocol.validateProposals(b,w,result.proposals)
        val manual=result.proposals.single().copy(id="manual-event",manual=true,value="Manuell korrigierte Zuordnung")
        val corrected=b.copy(memory=b.memory.copy(facts=b.memory.facts+manual))
        val protected=TeamProtocol.interpret(corrected,w,frame(w,valid))
        assertTrue(protected.cues.isEmpty());assertTrue(protected.proposals.isEmpty())
        val poison=JsonParser.parseString(frame(w,valid)).asJsonObject.apply {addProperty("sql","DROP TABLE messages")}
        assertThrows(IllegalArgumentException::class.java){TeamProtocol.interpret(b,w,poison.toString())}
        assertThrows(IllegalArgumentException::class.java){TeamProtocol.interpret(b,w.copy(version=w.version+1),frame(w,valid))}
        assertThrows(IllegalArgumentException::class.java){TeamProtocol.validateProposals(b,w,result.proposals.map {it.copy(value="Ruth ist Schwester von Enna.")})}
    }
    @Test fun helperCannotPromotePrivateExcludedOrModifiedOriginals() {
        val r=row("facts-new-secret");val b=gson.fromJson(r["bundle"],StoryBundle::class.java);val w=TeamProtocol.work(b,"new",emptyList())
        assertFalse(gson.toJson(w).contains("unter der Bank"))
        val private=b.messages.first {MemoryRules.privateAction(it.text)}
        val e=ArchiveExcerpt(private,0,private.text.length,0)
        assertTrue(TeamProtocol.work(b,"new",listOf(e)).sources.none {it.sourceId==private.id})
        val hidden="„Der Schlüssel liegt unter der Bank.“"
        assertNotNull(TeamProtocol.privateConflict(b,hidden))
        assertFalse(TeamProtocol.repair(w,TeamReview(listOf(TeamProtocol.privateConflict(b,hidden)!!),emptyList(),"conflict")).contains("unter der Bank"))
    }
    @Test fun correctDifferentGermanAndNewCurrentActionsAreNotOverruledByModelJudgement() {
        for(id in listOf("good-hold-family","good-causal-letter","new-good-action")) {
            val r=row(id);val b=gson.fromJson(r["bundle"],StoryBundle::class.java);val w=gson.fromJson(r["work"],TeamWork::class.java)
            val reply=r["reply"].asString
            val issue=JsonObject().apply {addProperty("type","state");addProperty("claim",reply);addProperty("evidence","F0");addProperty("detail","Erfundener Modellverdacht")}
            val raw=JsonObject().apply {addProperty("story",w.story);addProperty("request",w.request);addProperty("version",w.version);addProperty("verdict","conflict");add("issues",JsonArray().apply {add(issue)})}
            assertTrue(TeamProtocol.review(b,w,reply,raw.toString()).conflicts.isEmpty())
        }
    }
}
