package dev.vincent.geschichten.ai

/**
 * Immutable, public, ungated upstream artifact. No account/API key is needed.
 *
 * Verified 2026-10-03 against the Hugging Face model API (blobs=true). The size
 * and SHA-256 are the LFS metadata, not a checksum inferred from an HTTP ETag.
 * https://huggingface.co/api/models/litert-community/gemma-4-E2B-it-litert-lm?blobs=true
 * Runtime: com.google.ai.edge.litertlm:litertlm-android:0.17.1 (Google Maven).
 * Model card: https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm
 * Model and runtime are Apache-2.0. This is the CPU/GPU mobile artifact, NOT
 * the Tensor/Qualcomm NPU variants. No Exynos NPU support is assumed.
 */
object ModelArtifact {
    const val NAME = "Gemma 4 E2B"
    const val FILE_NAME = "gemma-4-E2B-it.litertlm"
    const val REVISION = "b3ca0d2f076785a8f4b2219ddbd2bdb99954eae1"
    const val BYTES = 2_588_147_712L
    const val SHA256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c"
    const val URL = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/" +
        REVISION + "/" + FILE_NAME + "?download=true"
    const val CONTEXT_TOKENS = 4096
    const val OUTPUT_TOKENS = 512
    const val FREE_SPACE_RESERVE = 536_870_912L
}
