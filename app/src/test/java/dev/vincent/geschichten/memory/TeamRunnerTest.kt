package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.ai.ModelMessage
import dev.vincent.geschichten.data.StoryBundle
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TeamRunnerTest {
    private fun bundle(id:String)=Gson().fromJson(JsonParser.parseString(File("../docs/validation/team-0.8.6/phase-b-scenes-frozen.json").readText()).asJsonArray.first {it.asJsonObject["case"].asString==id}.asJsonObject["bundle"],StoryBundle::class.java)
    private class Port(val replies:List<String>):TeamPort {
        var role=TeamRole.HELPER;val tasks=mutableListOf<String>();var next=0;var failReview=false
        override suspend fun activate(role:TeamRole){this.role=role}
        override suspend fun count(system:String,history:List<ModelMessage>)=100
        override suspend fun generate(system:String,history:List<ModelMessage>,onToken:(String)->Unit):String {
            if(role==TeamRole.NARRATOR){tasks+="narrator";onToken("UNGEPRUEFTER_ENTWURF");return replies[next++]}
            val input=history.last().text.substringAfter("DATEN:\n").substringBefore("\nANTWORT (Daten):")
            val work=JsonParser.parseString(input).asJsonObject
            val facts=system.contains("Faktenauswertung.");tasks+=if(facts)"facts" else "review"
            if(!facts && failReview)throw IllegalStateException("Technischer Testfehler")
            return JsonObject().apply {
                add("story",work["story"]);add("request",work["request"]);add("version",work["version"])
                if(facts){add("facts",JsonArray());add("unknown",JsonArray())}else{addProperty("verdict","clear");add("issues",JsonArray())}
            }.toString()
        }
    }
    @Test fun completeTeamUsesThreeCallsAndKeepsSeparateContexts()=runBlocking {
        val b=bundle("dolch");val port=Port(listOf("„Der Dolch gehört mir, aber du hältst ihn.“"))
        val result=TeamRunner.run(b,"request",false,emptyList(),port)
        assertEquals(listOf("facts","narrator","review"),port.tasks);assertFalse(result.repaired)
        assertEquals("„Der Dolch gehört mir, aber du hältst ihn.“",result.reply)
    }
    @Test fun confirmedDeterministicConflictCannotBeClearedAndAllowsOnlyOneRepair()=runBlocking {
        val b=bundle("dolch");val good="„Der Dolch gehört mir, aber du hältst ihn.“"
        val port=Port(listOf("„Der Dolch gehört mir. Ich halte ihn jetzt.“",good))
        val result=TeamRunner.run(b,"request",false,emptyList(),port)
        assertTrue(result.repaired);assertEquals(5,port.tasks.size);assertEquals(good,result.reply)
        val bad=Port(listOf("„Ich halte den Dolch.“","„Ich halte den Dolch.“"))
        assertThrows(IllegalStateException::class.java){runBlocking {TeamRunner.run(b,"request",false,emptyList(),bad)}}
        assertEquals(5,bad.tasks.size)
    }
    @Test fun helperFailureCannotReleaseKnownBadDraftAndCancellationStopsNextSteps()=runBlocking<Unit> {
        val b=bundle("dolch");val bad=Port(listOf("„Ich halte den Dolch.“","„Du hältst den Dolch.“"));bad.failReview=true
        assertThrows(IllegalStateException::class.java){runBlocking {TeamRunner.run(b,"request",false,emptyList(),bad)}}
        assertEquals(5,bad.tasks.size)
        val never=object:TeamPort {override suspend fun activate(role:TeamRole){throw CancellationException("stop")};override suspend fun count(system:String,history:List<ModelMessage>)=error("late count");override suspend fun generate(system:String,history:List<ModelMessage>,onToken:(String)->Unit)=error("late generation")}
        assertThrows(CancellationException::class.java){runBlocking {TeamRunner.run(b,"request",false,emptyList(),never)}}
    }
}
