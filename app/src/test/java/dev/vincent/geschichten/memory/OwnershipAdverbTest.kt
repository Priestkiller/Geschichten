package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class OwnershipAdverbTest {
    private val raw get()=JsonParser.parseString(File("../docs/validation/team-0.8.6/raw-dialogue-2.json").readText()).asJsonArray.single().asJsonObject
    private val bundle get()=Gson().fromJson(raw.getAsJsonObject("input")["bundle"],StoryBundle::class.java)
    @Test fun uneditedNativeDraftAndOwnershipAdverbsKeepTheActualOwner() {
        val b=bundle
        val reply=raw.getAsJsonArray("steps").first {it.asJsonObject["task"].asString=="narrator"}.asJsonObject["answer"].asString
        MemoryReplyGuard.validate(b,reply)
        val claims=RoleEvidence.extract(b,ChatMessage(storyId=b.story.id,role=ChatRole.CHARACTER,text=reply))
        assertEquals("Oda",claims.single {it.action==RoleAction.OWNER}.actor)
        for(word in listOf("jetzt","nun","weiterhin")) {
            val text="*Liora blickt zum Dolch.* „Er gehört $word Oda.“"
            MemoryReplyGuard.validate(b,text)
            assertEquals("Oda",RoleEvidence.extract(b,ChatMessage(storyId=b.story.id,role=ChatRole.CHARACTER,text=text)).single {it.action==RoleAction.OWNER}.actor)
        }
    }
    @Test fun adverbHandlingDoesNotAllowWrongOwnersOrInventOwnersInIncompleteSentences() {
        val b=bundle
        for(word in listOf("jetzt","nun","weiterhin")) for(owner in listOf("mir","dir","Mira")) {
            val text="*Liora blickt zum Dolch.* „Er gehört $word $owner.“"
            assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(b,text)}
        }
        val incomplete="*Liora blickt zum Dolch.* „Er gehört weiterhin.“"
        assertTrue(RoleEvidence.extract(b,ChatMessage(storyId=b.story.id,role=ChatRole.CHARACTER,text=incomplete)).none {it.action==RoleAction.OWNER})
        val saved=b.memory.current.single {it.field=="owner"}
        assertEquals("Oda",saved.value);assertTrue(saved.manual)
    }
}
