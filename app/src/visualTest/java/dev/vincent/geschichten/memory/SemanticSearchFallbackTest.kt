package dev.vincent.geschichten.memory
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class SemanticSearchFallbackTest {
    @Test fun restoredPreferenceIsClearlyActiveWithoutLoadingModelsAtStartup() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("semantic_search",0).edit().putBoolean("enabled",true).commit()
        SemanticSearch(context).use {search ->assertTrue(search.state.value.enabled);assertTrue(search.state.value.detail.startsWith("Testoption aktiv"));assertFalse(search.state.value.busy)}
    }
    @Test fun disabledOrMissingModelUsesUnchangedLexicalSearchAndDoesNotChangeStoryState()=runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("semantic_search",0).edit().clear().commit()
        StoryRepository(context).use {r ->
            val s=r.createStory("mira");r.appendMessage(s.id,ChatRole.USER,"Das Losungswort lautet Sonnenfels.");r.appendMessage(s.id,ChatRole.CHARACTER,"„Gut.“");r.appendMessage(s.id,ChatRole.USER,"Wie lautet das Losungswort?")
            val b=r.bundle(s.id)!!
            SemanticSearch(context).use {search ->
                assertFalse(search.state.value.enabled);assertNull(search.search(b))
                search.setEnabled(true);assertNull(search.search(b));assertFalse(search.indexBatch(b))
                assertTrue(ArchiveRecall.search(b,b.messages.last().text).any {it.text.contains("Sonnenfels")})
                search.setEnabled(false);assertNull(search.search(b));assertEquals(b,r.bundle(s.id))
            }
        }
    }
}
