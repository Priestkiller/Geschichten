package dev.vincent.geschichten.ai

import org.junit.Assert.*
import org.junit.Test

class LocalModelCatalogTest {
    @Test fun existingGemmaDownloadRemainsCompatible() {
        val original = LocalModelCatalog.restored(null)
        assertEquals(ModelArtifact.FILE_NAME, original.fileName)
        assertEquals(ModelArtifact.SHA256, original.sha256)
        assertEquals(ModelArtifact.BYTES, original.bytes)
        assertEquals(ModelArtifact.REVISION, original.revision)
        assertEquals(ModelFormat.LITERT_LM, original.format)
    }

    @Test fun immutableArtifactsIncludingTheExplicitComparisonHaveSeparateSafeDownloadPaths() {
        val models = LocalModelCatalog.models
        assertEquals(7, models.size)
        assertEquals(7, models.map { it.id }.toSet().size)
        assertEquals(7, models.map { it.fileName }.toSet().size)
        for (spec in models) {
            assertTrue(spec.revision.matches(Regex("[a-f0-9]{40}")))
            assertTrue(spec.sha256.matches(Regex("[a-f0-9]{64}")))
            assertFalse(spec.fileName.contains('/'))
            assertFalse(spec.fileName.contains('\\'))
            assertTrue(spec.bytes > 300_000_000L)
            assertTrue(spec.url.contains("/resolve/${spec.revision}/"))
            assertEquals(4096, spec.contextTokens)
            assertEquals(512, spec.outputTokens)
            assertTrue(spec.name.isNotBlank() && spec.description.isNotBlank() && spec.limitation.isNotBlank())
        }
        assertEquals(4, models.count { it.format == ModelFormat.GGUF && it.fileName.endsWith(".gguf") })
        assertEquals(3, models.count { it.format == ModelFormat.LITERT_LM && it.fileName.endsWith(".litertlm") })
    }

    @Test fun unknownSavedModelFallsBackWithoutAcceptingArbitraryDownloads() {
        assertNull(LocalModelCatalog.find("../unknown"))
        assertEquals(LocalModelCatalog.DEFAULT_ID, LocalModelCatalog.restored("../unknown").id)
    }
}
