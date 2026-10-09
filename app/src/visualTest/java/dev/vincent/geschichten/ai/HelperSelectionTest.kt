package dev.vincent.geschichten.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class HelperSelectionTest {
    @Test fun helperSelectionAndUnloadNeverChangeNarratorPreference()=runBlocking {
        val app=ApplicationProvider.getApplicationContext<Context>()
        val prefs=app.getSharedPreferences("model_selection",0);prefs.edit().clear().putString("model_id","qwen3-official-4b").commit()
        app.getSharedPreferences("helper_model_selection",0).edit().clear().commit()
        val before=prefs.all
        val narrator=LocalModelEngine(app);val helper=LocalModelEngine(app,"helper_model_selection","huihui-qwen3-4b")
        try {helper.selectModel("gemma-4-e2b");helper.unload();assertEquals(before,prefs.all);assertEquals("qwen3-official-4b",narrator.state.value.modelId);assertEquals("gemma-4-e2b",helper.state.value.modelId)}finally{narrator.close();helper.close()}
    }
}
