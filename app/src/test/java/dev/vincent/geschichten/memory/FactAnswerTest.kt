package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

object AnswerCases {
    val root=File("../docs/validation/team-0.8.6/legacy-output")
    val gson=GsonBuilder().setPrettyPrinting().create()
    fun rows()=JsonParser.parseString(File("../docs/validation/answers-0.8.5/new-cases-frozen.json").readText()).asJsonArray.map {it.asJsonObject}
    fun empty(row:JsonObject):StoryBundle {
        val id=row["case"].asString
        val p=CharacterProfile(id=id,name=row["character"].asString,role="Begleitung",genre="Fantasy",traits="Aufmerksam",personality="Spricht klar und freundlich.",scenario="Werkstatt",storyTitle="Werkstatt",openingMessage="„Ich höre zu.“")
        return StoryBundle(Story(id=id,characterId=p.id,title="Werkstatt",createdAt=1),p,emptyList(),emptyList())
    }
    fun interpreted(row:JsonObject):StoryBundle {
        var b=empty(row)
        fun add(text:String) {val m=ChatMessage(id="${b.story.id}-${b.messages.size}",storyId=b.story.id,role=ChatRole.USER,text=text,createdAt=(b.messages.size+2).toLong());b=b.copy(messages=b.messages+m);b=b.copy(memory=MemoryRules.preview(b,m))}
        add("Ich heiße ${row["player"].asString}.");add(row["source"].asString)
        if(row["case"].asString=="new-last-known")add("Ich lege den Kristall heimlich auf den Tisch.")
        return b
    }
}
class FactAnswerTest {
    @Test fun independentFrozenNewQuestionsAndNegativeIntents() {
        val results=AnswerCases.rows().map {row ->
            val b=AnswerCases.interpreted(row);val answer=FactAnswers.resolve(b,row["question"].asString)
            mapOf("case" to row["case"].asString,"split" to row["split"].asString,"source" to row["source"].asString,"question" to row["question"].asString,"expected" to row["expected"].asString,"activated" to (answer!=null),"answer" to (answer?.text ?: "fallback"),"proof" to (answer?.facts.orEmpty()),"stored" to b.memory,
                "correct" to if(row["activate"].asBoolean) answer?.text==row["expected"].asString else answer==null)
        }
        File(AnswerCases.root,"question-assignment.json").writeText(AnswerCases.gson.toJson(results))
        assertTrue(results.filter {it["correct"]!=true}.toString(),results.all {it["correct"]==true})
    }
    @Test fun unknownExcludedUncertainWrongStoryAndRevokedKnowledgeCannotSupplyAnAnswer() {
        val row=AnswerCases.rows().first {it["case"].asString=="dev-carry"};val b=AnswerCases.interpreted(row)
        val fact=b.memory.current.first {it.field=="holder"};val q="Wer trägt das Buch?"
        for(status in listOf(FactStatus.EXCLUDED,FactStatus.UNCERTAIN,FactStatus.HISTORICAL))assertNull(FactAnswers.resolve(b.copy(memory=b.memory.copy(facts=b.memory.facts.map {if(it.id==fact.id)it.copy(status=status) else it})),q))
        assertNull(FactAnswers.resolve(b.copy(memory=b.memory.copy(knowledge=b.memory.knowledge.filterNot {it.factId==fact.id && it.knower=="character"})),q))
        assertNull(FactAnswers.resolve(b.copy(memory=b.memory.copy(facts=b.memory.facts.map {if(it.id==fact.id)it.copy(storyId="another") else it})),q))
    }
}
