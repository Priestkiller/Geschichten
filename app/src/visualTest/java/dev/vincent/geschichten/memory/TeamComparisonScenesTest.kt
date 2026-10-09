package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class TeamComparisonScenesTest {
    @Test fun freezeSameSourceBoundWorkingStateForEveryComparisonCondition() {
        val root=File("../docs/validation/team-0.8.6");val out=File(root,"phase-b-scenes-frozen.json");if(out.exists())return
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        val gson=GsonBuilder().setPrettyPrinting().create();val output=mutableListOf<JsonObject>()
        StoryRepository(context).use {repo ->
            for(v in JsonParser.parseString(File(root,"scenes-frozen.json").readText()).asJsonArray) {
                val row=v.asJsonObject;val id=row["case"].asString;var b=gson.fromJson(row["bundle"],StoryBundle::class.java)
                var recall=row.getAsJsonArray("recall").map {gson.fromJson(it,ArchiveExcerpt::class.java)}
                if(id=="dolch" || id.startsWith("new-")) {
                    repo.upsertCharacter(b.character);val s=repo.createStory(b.character.id)
                    b.messages.drop(1).forEach {repo.appendMessage(s.id,it.role,it.text)}
                    repeat(20){repo.backfillMemory(s.id,8)}
                    val stored=repo.bundle(s.id)!!;b=stored.copy(memory=MemoryRules.preview(stored,stored.messages.last()))
                    assertEquals(stored,StoryRepository(context).use {it.bundle(s.id)})
                    recall=ArchiveRecall.search(b,b.messages.last().text)
                    if(id=="dolch")assertEquals("Bennet",RoleEvidence.player(b))
                }
                val current=row.deepCopy();current.add("bundle",gson.toJsonTree(b));current.add("recall",gson.toJsonTree(recall));output+=current
            }
        }
        out.writeText(gson.toJson(output))
    }
}
