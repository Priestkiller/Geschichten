package dev.vincent.geschichten.ai

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LiteRtStoryTemplateTest {
    @OptIn(ExperimentalApi::class)
    @Test fun failedConversationCreationNeverLeaksQwenOverrideToAnotherModel() {
        val previous = ExperimentalFlags.overwritePromptTemplate
        try {
            val engine = Engine(EngineConfig("unused.litertlm", Backend.CPU(threadCount = 1)))
            for (modelId in listOf("qwen2.5-1.5b", "qwen3-0.6b", "gemma-4-e2b")) {
                ExperimentalFlags.overwritePromptTemplate = "sentinel-before-creation"
                assertThrows(IllegalStateException::class.java) {
                    LiteRtStoryTemplate.create(engine, modelId, ConversationConfig())
                }
                assertEquals("sentinel-before-creation", ExperimentalFlags.overwritePromptTemplate)
            }
        } finally {
            ExperimentalFlags.overwritePromptTemplate = previous
        }
    }
}
