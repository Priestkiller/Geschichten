package dev.vincent.geschichten.ai

import org.junit.Assert.*
import org.junit.Test

class ModelPerformanceTest {
    @Test fun meaningfulImprovementWinsButSmallNoiseKeepsBaseline() {
        assertEquals(6, ModelPerformance.select(mapOf(4 to 2000L, 2 to 2300L, 6 to 1500L)))
        assertEquals(4, ModelPerformance.select(mapOf(4 to 2000L, 2 to 2100L, 6 to 1900L)))
        assertEquals(2, ModelPerformance.select(mapOf(2 to 1000L, 1 to 2000L)))
    }
    @Test fun comparisonRespectsAvailableProcessorsAndRecognizesBothPhones() {
        assertEquals(listOf(4, 2, 6), ModelPerformance.candidates(8))
        assertEquals(listOf(2), ModelPerformance.candidates(2))
        assertEquals(listOf(1), ModelPerformance.candidates(1))
        assertEquals("Galaxy S24", ModelPerformance.deviceName("SM-S921B"))
        assertEquals("Galaxy S24 Ultra", ModelPerformance.deviceName("SM-S928U1"))
    }
    @Test fun timingPromptIsBoundedAndDoesNotReduceProductionOutputLimits() {
        assertTrue(ModelPerformance.testSystem.length < StoryPrompt.MAX_SYSTEM_CHARS)
        assertTrue(ModelPerformance.testQuestion.length < StoryPrompt.MAX_USER_MESSAGE_CHARS)
        assertTrue(LocalModelCatalog.models.all { it.outputTokens == 512 })
    }
}
