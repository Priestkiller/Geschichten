package dev.vincent.geschichten.ai

import android.content.Context
import android.os.Build
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ModelPerformanceStorageTest {
    @Test fun measuredSettingIsRestoredForItsModelAndDeviceOnly() = runBlocking {
        val app = RuntimeEnvironment.getApplication()
        val prefs = app.getSharedPreferences("model_selection", Context.MODE_PRIVATE)
        val first = LocalModelCatalog.models.first()
        val key = "cpu_${first.id}_${first.sha256.take(12)}_${Build.FINGERPRINT.hashCode()}"
        prefs.edit().clear().putInt(key, 6).putString(key + "_result", "Gespeicherte Messung").commit()
        val engine = LocalModelEngine(app)
        try {
            assertEquals(6, engine.state.value.cpuThreads)
            engine.selectModel("qwen3-0.6b")
            assertEquals(4, engine.state.value.cpuThreads)
            engine.selectModel(first.id)
            assertEquals(6, engine.state.value.cpuThreads)
            try { engine.optimizeForDevice(); fail("Missing model was benchmarked") } catch (_: LocalModelException) { }
            assertEquals(6, prefs.getInt(key, 4))
        } finally { engine.close() }
        val restarted = LocalModelEngine(app)
        try { assertEquals(6, restarted.state.value.cpuThreads) } finally { restarted.close() }
    }
}
