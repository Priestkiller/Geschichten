package dev.vincent.geschichten.memory

import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Real untouched negatives from 0.8.1, plus correct paraphrases. Scores are separate. */
class RoleAnswerCorpusTest {
    @Test fun evaluateRecordedAnswersAndPositiveCounterexamples() {
        val gson=GsonBuilder().setPrettyPrinting().create()
        val root=File("../docs/validation/active-memory-0.8.1")
        val input=JsonParser.parseString(File(root,"model-cases-final.json").readText(Charsets.UTF_8)).asJsonArray.associate {it.asJsonObject["case"].asString to gson.fromJson(it.asJsonObject["bundle"],StoryBundle::class.java)}
        val review=JsonParser.parseString(File(root,"model-assessment.json").readText(Charsets.UTF_8)).asJsonObject["rows"].asJsonArray
        val results=mutableListOf<Map<String,Any>>()
        review.forEach {value ->
            val r=value.asJsonObject;val bundle=input.getValue(r["case"].asString);val answer=r["answer"].asString
            var rejected=false;var error=""
            try {MemoryReplyGuard.validate(bundle,answer)}catch(e:IllegalStateException){rejected=true;error=e.message.orEmpty()}
            results+=mapOf("model" to r["model"].asString,"case" to r["case"].asString,"manualVerdict081" to r["manualResult"].asString,"answer" to answer,"rejected082" to rejected,"error" to error)
        }
        val positives=mapOf(
            "archive" to listOf("*Mira sieht Rian an.* „Morgenstern. Du gabst mir den Brief, weil deine Hand verletzt ist.“","„Rian hat mir den Brief anvertraut, weil er mit seiner verletzten Hand das Siegel nicht unbeschädigt öffnen konnte.“","„Das Losungswort lautet Morgenstern. Du hast mir den Brief gegeben.“"),
            "correction" to listOf("„Der Schlüssel ist blau. Ich trage ihn.“","*Mira zeigt Rian den blauen Schlüssel.* „Ich habe ihn.“"),
            "unknown" to listOf("„Ich weiß nicht, welches Geschenk deiner Schwester du meinst. Erzählst du mir davon?“","„Wir haben darüber noch nicht gesprochen. Welche Schwester meinst du?“","„Ich kann mich an kein solches Gespräch erinnern.“"))
        var falsePositives=0
        positives.forEach {(case,answers)->answers.forEach {answer ->
            var error="";try {MemoryReplyGuard.validate(input.getValue(case),answer)}catch(e:IllegalStateException){falsePositives++;error=e.message.orEmpty()}
            results+=mapOf("case" to case,"positiveReference" to true,"answer" to answer,"rejected082" to error.isNotEmpty(),"error" to error)
        }}
        val out=File("../docs/validation/team-0.8.6/regression-output/answer-guard-corpus.json");out.writeText(gson.toJson(results),Charsets.UTF_8)
        assertEquals("Correct answers must not be rejected: "+results.filter {it["positiveReference"]==true && it["rejected082"]==true},0,falsePositives)
        assertTrue(results.any {it["manualVerdict081"]=="wrong_roles" && it["rejected082"]==true})
        assertTrue(results.any {it["manualVerdict081"]=="correct_facts" && it["rejected082"]==false})
    }
}
