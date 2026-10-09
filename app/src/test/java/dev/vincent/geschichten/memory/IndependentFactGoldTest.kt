package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

object FactGoldHarness {
    val root=File("../docs/validation/facts-0.8.4")
    val gson=GsonBuilder().setPrettyPrinting().create()
    fun rows(file:String)=JsonParser.parseString(File(root,file).readText()).asJsonArray.map {it.asJsonObject}
    fun fresh(row:JsonObject):StoryBundle {
        val id=row["case"].asString;val name=row["character"].asString
        val p=CharacterProfile(id=id+"-profile",name=name,role="Begleitung",genre="Fantasy",traits="Aufmerksam",personality="Spricht klar und freundlich.",scenario="Werkstatt",storyTitle="Werkstatt",openingMessage="„Ich höre zu.“")
        val story=Story(id=id,characterId=p.id,title="Werkstatt",createdAt=1000)
        val identity=ChatMessage(id=id+"-identity",storyId=id,role=ChatRole.USER,text="Ich heiße ${row["player"].asString}.",createdAt=1001)
        val source=ChatMessage(id=id+"-source",storyId=id,role=ChatRole.USER,text=row["source"].asString,createdAt=1002)
        var b=StoryBundle(story,p,listOf(identity,source),emptyList())
        b=b.copy(memory=MemoryRules.preview(b,identity));return b.copy(memory=MemoryRules.preview(b,source))
    }
    fun issues(row:JsonObject,b:StoryBundle):List<String> {
        val errors=mutableListOf<String>()
        for(g in row.getAsJsonArray("gold")) {
            val a=g.asJsonArray;val matches=b.memory.current.filter {it.entity.kind.name==a[0].asString && it.entity.name==a[1].asString && it.field==a[2].asString}
            if(matches.size!=1 || matches.singleOrNull()?.value!=a[3].asString)errors+="Expected $a; actual ${matches.map {it.value}}"
        }
        for(v in row.getAsJsonArray("unknown") ?: JsonArray())if(b.memory.current.any {it.entity.kind==EntityKind.ITEM && it.field==v.asString})errors+="Unknown ${v.asString} was asserted"
        for(f in b.memory.facts.filter {it.sourceId!=null}) {
            val original=b.messages.firstOrNull {it.id==f.sourceId}
            if(original==null || !original.text.contains(f.sourceText))errors+="Missing exact provenance ${f.id}"
        }
        return errors
    }
    fun write(name:String,results:List<Map<String,Any>>) {File("../docs/validation/team-0.8.6/regression-output",name).apply {parentFile!!.mkdirs()}.writeText(gson.toJson(results))}
}
class IndependentFactGoldTest {
    @Test fun developmentOriginalsMatchIndependentGold() {
        val results=FactGoldHarness.rows("new-cases-frozen.json").filter {it["split"].asString=="development"}.map {row->
            val b=FactGoldHarness.fresh(row);mapOf("case" to row["case"].asString,"original" to row["source"].asString,"gold" to row["gold"],"facts" to b.memory.current,"issues" to FactGoldHarness.issues(row,b),"core" to MemoryPrompt.core(b,false))
        }
        FactGoldHarness.write("development-interpretation.json",results)
        assertTrue(results.filter {(it["issues"] as List<*>).isNotEmpty()}.toString(),results.all {(it["issues"] as List<*>).isEmpty()})
    }
    @Test fun formerAcceptanceCasesAreIndependentGoldRegressions() {
        val originals=JsonParser.parseString(File("../docs/validation/quality-search-0.8.3/materialized-scenes.json").readText()).asJsonArray.associate {it.asJsonObject["case"].asString to it.asJsonObject}
        val results=FactGoldHarness.rows("gold-regressions-reviewed.json").map {row->
            val b=FactGoldHarness.gson.fromJson(originals.getValue(row["case"].asString)["bundle"],StoryBundle::class.java)
            var snapshot=b.memory.copy(facts=emptyList(),knowledge=emptyList())
            b.messages.dropLast(1).forEachIndexed {i,m->val prefix=b.copy(messages=b.messages.take(i+1),memory=snapshot);snapshot=MemoryRules.preview(prefix,m)}
            val current=b.copy(memory=snapshot)
            mapOf("case" to row["case"].asString,"gold" to row["gold"],"facts" to snapshot.current,"issues" to FactGoldHarness.issues(row,current),"core" to MemoryPrompt.core(current,false))
        }
        FactGoldHarness.write("regression-interpretation.json",results)
        assertTrue(results.filter {(it["issues"] as List<*>).isNotEmpty()}.toString(),results.all {(it["issues"] as List<*>).isEmpty()})
    }
}
