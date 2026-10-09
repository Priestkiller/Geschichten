package dev.vincent.geschichten.ai

import android.content.Context
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests the production selection/storage boundary without constructing native inference. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ModelSelectionIntegrationTest {
    @Test fun selectionSurvivesRestartAndKeepsSeparatePartialDownloads() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("model_selection", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val dir = File(context.noBackupFilesDir, "models").apply { mkdirs() }
        val gemma = LocalModelCatalog.models.first()
        val other = LocalModelCatalog.models.last()
        val firstPart = File(dir, "${gemma.fileName}.part").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val otherPart = File(dir, "${other.fileName}.part").apply { writeBytes(byteArrayOf(4, 5)) }
        val engine = LocalModelEngine(context)
        assertEquals(3L, engine.state.value.downloadedBytes)
        engine.selectModel(other.id)
        assertEquals(other.id, engine.state.value.modelId)
        assertEquals(2L, engine.state.value.downloadedBytes)
        assertEquals(ModelStage.MISSING, engine.state.value.stage)
        assertArrayEquals(byteArrayOf(1, 2, 3), firstPart.readBytes())
        engine.close()
        val restarted = LocalModelEngine(context)
        assertEquals(other.id, restarted.state.value.modelId)
        restarted.selectModel(gemma.id)
        assertEquals(3L, restarted.state.value.downloadedBytes)
        assertArrayEquals(byteArrayOf(4, 5), otherPart.readBytes())
        restarted.close()
    }

    @Test fun unknownSelectionDoesNotChangeSavedSelectionOrCurrentState() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("model_selection", Context.MODE_PRIVATE)
        prefs.edit().putString("model_id", "qwen3-0.6b").commit()
        val engine = LocalModelEngine(context)
        try { engine.selectModel("unknown"); fail("Unknown model accepted") } catch (_: LocalModelException) { }
        assertEquals("qwen3-0.6b", engine.state.value.modelId)
        assertEquals("qwen3-0.6b", prefs.getString("model_id", null))
        engine.close()
    }

    @Test fun deletionRemovesOnlyTheInactiveModelsFilesAndNeverTheSelectedModel() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("model_selection", Context.MODE_PRIVATE).edit().clear().commit()
        val dir = File(context.noBackupFilesDir, "models").apply { mkdirs() }
        val first = LocalModelCatalog.models.first()
        val other = LocalModelCatalog.models.last()
        val currentPart = File(dir, "${first.fileName}.part").apply { writeText("CURRENT_PART") }
        val oldPart = File(dir, "${other.fileName}.part").apply { writeText("OLD_PART") }
        val oldFile = File(dir, other.fileName).apply { writeText("OLD_INCOMPLETE_MODEL") }
        val engine = LocalModelEngine(context)
        try {
            try { engine.deleteStoredModel(first.id); fail("Current model deletion accepted") } catch (_: LocalModelException) { }
            assertEquals("CURRENT_PART", currentPart.readText())
            engine.deleteStoredModel(other.id)
            assertFalse(oldPart.exists()); assertFalse(oldFile.exists())
            assertEquals("CURRENT_PART", currentPart.readText())
            assertEquals(first.id, engine.state.value.modelId)
            try { engine.deleteStoredModel("../outside"); fail("Unknown model deletion accepted") } catch (_: LocalModelException) { }
        } finally { engine.close() }
    }
}
