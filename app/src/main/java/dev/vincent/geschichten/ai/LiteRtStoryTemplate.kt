package dev.vincent.geschichten.ai

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags

/** Compatibility for the two pinned Qwen exports, whose templates ignore LiteRT's MODEL role. */
object LiteRtStoryTemplate {
    private val creationLock = Any()

    @JvmStatic
    @OptIn(ExperimentalApi::class)
    fun create(engine: Engine, modelId: String, config: ConversationConfig): Conversation = synchronized(creationLock) {
        val template = when (modelId) {
            "qwen2.5-1.5b" -> CHATML
            "qwen3-0.6b" -> CHATML + "{%- if add_generation_prompt -%}{{- '<think>\\n\\n</think>\\n\\n' -}}{%- endif -%}"
            else -> null
        }
        // This flag is read synchronously by createConversation, not by later streaming.
        // Scope and restore the process-global override; Gemma keeps its embedded template.
        val previous = ExperimentalFlags.overwritePromptTemplate
        try {
            ExperimentalFlags.overwritePromptTemplate = template
            engine.createConversation(config)
        } finally {
            ExperimentalFlags.overwritePromptTemplate = previous
        }
    }

    // Text-only ChatML: translate MODEL to assistant while preserving message boundaries.
    // Tokenization and BOS/EOS handling remain with the actual embedded tokenizer/runtime.
    private val CHATML = """
        {%- for message in messages -%}
        {%- set role = 'assistant' if message['role'] == 'model' else message['role'] -%}
        {{- '<|im_start|>' + role + '\n' -}}
        {%- if message['content'] is string -%}
        {{- message['content'] -}}
        {%- else -%}
        {%- for item in message['content'] -%}
        {%- if item['type'] == 'text' -%}{{- item['text'] -}}{%- endif -%}
        {%- endfor -%}
        {%- endif -%}
        {{- '<|im_end|>\n' -}}
        {%- endfor -%}
        {%- if add_generation_prompt -%}{{- '<|im_start|>assistant\n' -}}{%- endif -%}
    """.trimIndent()
}
