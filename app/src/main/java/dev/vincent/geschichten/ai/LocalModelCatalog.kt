package dev.vincent.geschichten.ai

/** Pinned text models for the on-device LiteRT-LM and llama.cpp runtimes. */
enum class ModelFormat { LITERT_LM, GGUF }

data class LocalModelSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val useFor: String,
    val limitation: String,
    val repository: String,
    val revision: String,
    val fileName: String,
    val bytes: Long,
    val sha256: String,
    val format: ModelFormat = ModelFormat.LITERT_LM,
    val templateFallback: String? = null,
    val contextTokens: Int = 4096,
    val outputTokens: Int = 512,
    val informationUrl: String? = null,
) {
    val url: String get() = "https://huggingface.co/$repository/resolve/$revision/$fileName?download=true"
    val sourceUrl: String get() = "https://huggingface.co/$repository/tree/$revision"
}

object LocalModelCatalog {
    const val DEFAULT_ID = "gemma-4-e2b"
    val models: List<LocalModelSpec> = listOf(
        LocalModelSpec(
            id = DEFAULT_ID,
            name = ModelArtifact.NAME,
            subtitle = "Deine bisherige KI",
            description = "Das bisherige Modell der App und ein guter Ausgangspunkt für deinen Vergleich. " +
                "Google entwickelt diese kleine Gemma-Variante für den Einsatz direkt auf Geräten. " +
                "In der App bekommt sie die Persönlichkeit deiner Figur, eure Ausgangslage und den aktuellen Gesprächsverlauf.",
            useFor = "Deine bisherigen Geschichten fortsetzen und beobachten, wie die anderen Modelle dieselbe Figur und Szene darstellen.",
            limitation = "Der größte Download dieser Auswahl. Eine größere Datei garantiert keinen besseren Erzählstil; " +
                "auch dieses Modell kann Details verwechseln oder sich wiederholen.",
            repository = "litert-community/gemma-4-E2B-it-litert-lm",
            revision = ModelArtifact.REVISION,
            fileName = ModelArtifact.FILE_NAME,
            bytes = ModelArtifact.BYTES,
            sha256 = ModelArtifact.SHA256,
            informationUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm",
        ),
        LocalModelSpec(
            id = "qwen2.5-1.5b",
            name = "Qwen 2.5 · 1.5B",
            subtitle = "Dialoge mit klaren Vorgaben",
            description = "Interessant, wenn du deiner Figur klare Vorgaben für Verhalten und Sprache gibst. " +
                "Die Qwen-2.5-Familie wurde laut Anbieter beim Befolgen von Anweisungen und bei vorgegebenen Rollen verbessert. " +
                "Sie unterstützt mehrere Sprachen, darunter Deutsch. Diese kleine Variante braucht weniger Downloadspeicher als Gemma 4.",
            useFor = "Figurendialoge, konkrete Szenen und Gespräche mit einer klaren Rolle; vergleiche, wie gut deine Vorgaben erhalten bleiben.",
            limitation = "Bei vielen Figuren oder komplizierten Zusammenhängen können Details verloren gehen. " +
                "Eine bessere Geschichte als mit Gemma ist nicht garantiert.",
            repository = "litert-community/Qwen2.5-1.5B-Instruct",
            revision = "19edb84c69a0212f29a6ef17ba0d6f278b6a1614",
            fileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
            bytes = 1_597_931_520L,
            sha256 = "faa60663b333290c1496c499828b21d3e3254a788cacd8cce917ce0f761a2dc9",
            informationUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct",
        ),
        LocalModelSpec(
            id = "qwen3-0.6b",
            name = "Qwen 3 · 0.6B",
            subtitle = "Kleiner Download, einfache Szenen",
            description = "Der kleinste Download der gesamten Auswahl: interessant, wenn du erst einmal wenig Speicher belegen möchtest. " +
                "Diese stark komprimierte Qwen-Variante kann kurze Gespräche und erste Schreibideen liefern. " +
                "Beginne mit wenigen Beteiligten und einer überschaubaren Szene, um ihren Stil kennenzulernen.",
            useFor = "Kurze Dialoge und erste Ideen; mit wenigen Beteiligten und einer klaren Aufgabe beginnen.",
            limitation = "Das kleine Modell kann sich leichter wiederholen oder Vorgaben übersehen. " +
                "Ein kleiner Download allein sagt nicht, wie schnell es auf deinem Handy antwortet.",
            repository = "litert-community/Qwen3-0.6B",
            revision = "a3c5d805ae362dff7f580bc25f2dfb9a5a7eaa76",
            fileName = "Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm",
            bytes = 344_671_744L,
            sha256 = "03e7da1eb1108b50dffaa9bb52cc7bcbad2eb0c66ca990267f480c1e545d2856",
            informationUrl = "https://huggingface.co/litert-community/Qwen3-0.6B",
        ),
        LocalModelSpec(
            id = "dolphin3-llama3.2-3b",
            name = "Dolphin 3.0 · Llama 3.2 3B",
            subtitle = "Figuren, Gesprächsstile und klare Rollen",
            description = "Ein Kandidat für den ersten Versuch mit den drei zusätzlichen Modellen: " +
                "Dolphin hat unter diesen drei den kleinsten Download. Laut Entwickler lässt es sich über Vorgaben auf Figuren, " +
                "Stimmungen und Verhaltensregeln einstellen. Es ist ein allgemeines Gesprächsmodell auf Basis von Llama 3.2.",
            useFor = "Ausprobieren, ob Humor, Eigensinn oder die besondere Sprechweise deiner Figur überzeugend erhalten bleiben; " +
                "dieselbe Szene mit einem anderen Gesprächsstil fortsetzen.",
            limitation = "Die Qualität auf Deutsch und bei langen Handlungen kann schwanken. " +
                "Der kleinere Download ist keine Zusage für höhere Geschwindigkeit. Built with Llama.",
            repository = "bartowski/Dolphin3.0-Llama3.2-3B-GGUF",
            revision = "ac6b1ee98e3864ebd5998216f800a07d74b166b5",
            fileName = "Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf",
            bytes = 2_019_382_400L,
            sha256 = "5d6d02eeefa1ab5dbf23f97afdf5c2c95ad3d946dc3b6e9ab72e6c1637d54177",
            format = ModelFormat.GGUF,
            informationUrl = "https://huggingface.co/dphn/Dolphin3.0-Llama3.2-3B",
        ),
        LocalModelSpec(
            id = "huihui-qwen3-4b",
            name = "Huihui Qwen 3 · 4B Instruct 2507",
            subtitle = "Vielseitige Dialoge ohne zusätzlichen Denkmodus",
            description = "Eine veränderte Qwen-3-Version für vielseitige Gespräche. " +
                "Die zugrunde liegende Instruct-2507-Version antwortet ohne zusätzlichen Denkmodus. " +
                "Der Anbieter hat Ablehnungsverhalten und Inhaltsfilter reduziert. Das beschreibt die Veränderung am Modell, " +
                "aber keine Garantie für jede gewünschte Antwort.",
            useFor = "Ausführlichere Gespräche, Szenen mit mehreren Beteiligten und klare Schreibvorgaben; " +
                "prüfe, ob die Figuren unterscheidbar bleiben und der Dialog sinnvoll weitergeht.",
            limitation = "Die Änderungen am Modell können auch die Antwortqualität beeinflussen. " +
                "Es braucht mehr Downloadspeicher als die kleinen Qwen-Varianten; der Arbeitsspeicherbedarf kommt hinzu.",
            repository = "mahdisml/Huihui-Qwen3-4B-Instruct-2507-abliterated-Q4_K_M-GGUF",
            revision = "1cbc997dbe95f5ec9b196bb79090328519363b84",
            fileName = "huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf",
            bytes = 2_497_276_736L,
            sha256 = "d80ef0f08a0e64887f4a19bc9202fe1a108d9666a2c49eb9a30bc3a30995121b",
            format = ModelFormat.GGUF,
            // This quantization omits tokenizer.chat_template. The upstream
            // Qwen3-4B-Instruct-2507 tokenizer uses non-thinking ChatML.
            templateFallback = "chatml",
            informationUrl = "https://huggingface.co/huihui-ai/Huihui-Qwen3-4B-Instruct-2507-abliterated",
        ),
        LocalModelSpec(
            id = "gemma3-davidau-4b",
            name = "Gemma 3 · 4B DBL-X (DavidAU)",
            subtitle = "Kreatives Schreiben und Rollenspiele",
            description = "Interessant, wenn du vor allem Erzählstil und Atmosphäre vergleichen möchtest. " +
                "DavidAU nennt kreatives Schreiben, Rollenspiele, Handlungsideen und das Fortsetzen von Szenen als Einsatzbereiche " +
                "dieser veränderten Gemma-3-Version. In der App ist eine komprimierte Fassung eingebunden.",
            useFor = "Atmosphärische Szenen, Figurendialoge und neue Wendungen; " +
                "vergleiche, ob dir die Formulierungen gefallen und die Handlung zu deinen Figuren passt.",
            limitation = "Vom Anbieter als „Uncensored“ bezeichnet; Ablehnungen sind trotzdem möglich. " +
                "Die eingebundene Datei hat etwa 2,58 GB. Schreibqualität und Geschwindigkeit auf deinem Handy musst du selbst vergleichen.",
            repository = "DavidAU/Gemma-3-it-4B-Uncensored-DBL-X-GGUF",
            revision = "1a0f89952bf29018124d51ba1ecd29639bdbf061",
            fileName = "Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf",
            bytes = 2_576_023_488L,
            sha256 = "02829b28e6a104daecc06cc8884227ef34b9a461e34f0899cbb819cf7ed6901c",
            format = ModelFormat.GGUF,
            informationUrl = "https://huggingface.co/DavidAU/Gemma-3-it-4B-Uncensored-DBL-X-GGUF",
        ),
        LocalModelSpec(
            id = "qwen3-official-4b",
            name = "Qwen 3 · 4B Original Instruct 2507",
            subtitle = "Offizielle Ausgangsfassung · Testoption",
            description = "Die offizielle Qwen-Ausgangsfassung ohne zusätzliches Fine-Tuning oder Abliterierung, als komprimierte GGUF-Datei. " +
                "Sie bietet einen direkten Vergleich zu Huihui. In einer kleinen lokalen Prüfung halfen zusätzliche Originalstellen aus der Bedeutungssuche bei einzelnen Antworten; " +
                "auch diese Fassung verwechselte weiterhin Rollen und erfand Erinnerungen.",
            useFor = "Dieselbe Figur mit der Originalfassung vergleichen, insbesondere zusammen mit der abschaltbaren Bedeutungssuche für Erinnerungen.",
            limitation = "Experimentell, keine nachgewiesene allgemeine Verbesserung und kein automatischer Wechsel. " +
                "Etwa 2,50 GB Download; Geschwindigkeit und gemeinsamer Arbeitsspeicherbedarf auf dem S24 sind noch nicht geprüft.",
            repository = "bartowski/Qwen_Qwen3-4B-Instruct-2507-GGUF",
            revision = "ae44f08e1392f39c0e474af10c3ff8355c8b6688",
            fileName = "Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf",
            bytes = 2_497_280_736L,
            sha256 = "2fde00ce69dd4899c70d020845e2638353015bba0fdf161b3eb965f2bca4464e",
            format = ModelFormat.GGUF,
            informationUrl = "https://huggingface.co/Qwen/Qwen3-4B-Instruct-2507",
        ),
    )

    fun find(id: String?): LocalModelSpec? = models.firstOrNull { it.id == id }
    fun restored(id: String?): LocalModelSpec = find(id) ?: models.first()
}
