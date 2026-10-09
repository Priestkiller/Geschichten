package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Test
import java.io.File

/** Export production work packets before real host JNI calls; no inserted gold facts. */
class TeamFixturesTest {
    @Test fun exportFrozenHelperInputs() {
        val root=File("../docs/validation/team-0.8.6");val gson=GsonBuilder().setPrettyPrinting().create()
        val output=File(root,"phase-a-complete-frozen.json");if(output.exists())return
        val rows=JsonParser.parseString(File(root,"comparison-frozen.json").readText()).asJsonArray
        val oldRoot=File("../docs/validation/answers-0.8.5")
        val prior=JsonParser.parseString(File(oldRoot,"raw-corrected-hybrid-qwen3-official-4b.json").readText()).asJsonArray.map {it.asJsonObject}.associateBy {it["case"].asString}
        val badDolch=JsonParser.parseString(File(oldRoot,"raw-conversation-1-qwen3-official-4b.json").readText()).asJsonArray.single().asJsonObject
        val out=mutableListOf<Map<String,Any>>();val scenes=mutableListOf<JsonObject>()
        for(v in rows) {
            val row=v.asJsonObject;val id=row["case"].asString;var b=gson.fromJson(row["bundle"],StoryBundle::class.java)
            if(id.startsWith("new-")) {
                var memory=b.memory
                b.messages.dropLast(1).forEachIndexed {i,m ->memory=MemoryRules.preview(b.copy(messages=b.messages.take(i+1),memory=memory),m)}
                b=b.copy(memory=memory)
            }
            val evidence=RoleEvidence.trusted(b)
            val old=prior[id]
            val recall=if(old!=null)old.getAsJsonArray("recalledSources").map {gson.fromJson(it,ArchiveExcerpt::class.java)} else ArchiveRecall.search(b,b.messages.last().text,evidence=evidence)
            val work=TeamProtocol.work(b,"phase-a-$id",recall)
            val answer=when(id){"dolch"->badDolch["answer"].asString;else->old?.get("answer")?.asString}
            if(id in setOf("hold-family","causal-letter","dolch"))out+=mapOf("case" to "bad-$id","task" to "review","work" to work,"bundle" to b,"reply" to answer!!,"goldVerdict" to "conflict")
            val correct=when(id){"hold-family"->"„Ruth, deine Schwester, hat mir den Kristall geschenkt.“";"causal-letter"->"„Du konntest mit deiner verletzten Hand das Siegel nicht unbeschädigt öffnen. Deshalb hast du mir den Brief gegeben. Ich halte ihn jetzt.“";"dolch"->"„Der Dolch gehört mir, aber du hältst ihn gerade.“";else->null}
            if(correct!=null)out+=mapOf("case" to "good-$id","task" to "review","work" to work,"bundle" to b,"reply" to correct,"goldVerdict" to "clear")
            if(id=="new-gift") {
                out+=mapOf("case" to "new-bad-family","task" to "review","work" to work,"bundle" to b,"reply" to "„Elva, meine Schwester, hat den Ring geschenkt.“","goldVerdict" to "conflict")
                out+=mapOf("case" to "new-good-action","task" to "review","work" to work,"bundle" to b,"reply" to "*Tirra legt den Ring auf den Tisch.* „Nun liegt er auf dem Tisch. Elva ist deine Schwester, Birk.“","goldVerdict" to "clear")
            }
            if(id=="new-secret")out+=mapOf("case" to "new-bad-privacy","task" to "review","work" to work,"bundle" to b,"reply" to "„Der Schlüssel liegt unter der Bank.“","goldVerdict" to "conflict")
            if(id in setOf("hold-family","hold-reverse","dolch") || id.startsWith("new-"))out+=mapOf("case" to "facts-$id","task" to "facts","work" to work,"bundle" to b,"gold" to row["expected"].asString)
            val scene=row.deepCopy();scene.add("bundle",gson.toJsonTree(b));scene.add("recall",gson.toJsonTree(recall));scenes+=scene
        }
        File(root,"scenes-frozen.json").writeText(gson.toJson(scenes))
        output.writeText(gson.toJson(out))
    }
}
