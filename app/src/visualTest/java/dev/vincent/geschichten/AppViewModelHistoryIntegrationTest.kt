package dev.vincent.geschichten

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.data.StoryRepository
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppViewModelHistoryIntegrationTest {
    @Test fun clearingAlsoRemovesDraftsAndSummaryCheckpointsAndPreservesPreferencesAndModelFiles() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        application.deleteDatabase("geschichten.db")
        val story = StoryRepository(application).use { it.createStory("grask") }
        val preferences = application.getSharedPreferences("preferences", 0)
        preferences.edit().clear().putBoolean("adult_themes", true)
            .putString("draft_${story.id}", "Noch nicht gesendeter Entwurf")
            .putInt("summary_turns_${story.id}", 4).commit()
        val selection = application.getSharedPreferences("model_selection", 0)
        selection.edit().putString("model_id", "qwen3-0.6b").commit()
        val sentinel = File(application.filesDir, "download-retained-test.bin").apply { writeText("MODEL_FILE_SENTINEL") }
        val store = ViewModelStore()
        val model = AppViewModel(application)
        store.put("history-model", model)
        try {
            await { model.state.value.stories.size == 1 }
            model.openStory(story.id)
            await { model.state.value.current?.story?.id == story.id }
            assertEquals("Noch nicht gesendeter Entwurf", model.state.value.draft)
            await { model.clearHistory(); model.state.value.historyBusy || model.state.value.stories.isEmpty() }
            await { !model.state.value.historyBusy && model.state.value.stories.isEmpty() }
            assertNull(model.state.value.current)
            assertEquals("", model.state.value.draft)
            assertFalse(preferences.contains("draft_${story.id}"))
            assertFalse(preferences.contains("summary_turns_${story.id}"))
            assertTrue(preferences.getBoolean("adult_themes", false))
            assertEquals("qwen3-0.6b", selection.getString("model_id", null))
            assertEquals("MODEL_FILE_SENTINEL", sentinel.readText())
            assertEquals(90, model.state.value.characters.size)
        } finally { store.clear(); sentinel.delete() }
    }

    private fun await(condition: () -> Boolean) {
        val deadline = System.nanoTime() + 10_000_000_000L
        while (!condition()) {
            check(System.nanoTime() < deadline) { "ViewModel did not finish within 10 seconds" }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
    }
}
