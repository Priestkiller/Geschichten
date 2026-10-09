# Modellquellen und Einstellungen – 0.8.3

Alle Tests verwenden synthetische deutsche Szenen und echte lokale CPU-Inferenz. Kein Training, kein Adapter, keine Cloud-Inferenz.

## Textmodelle

Die bisherige Huihui-Datei und Gemma 4 E2B sind unverändert. Ihre Kennungen, Größen und SHA-256 stehen in [baseline.json](validation/quality-search-0.8.3/baseline.json); die vollständigen bestehenden Downloadrevisionen in `LocalModelCatalog.kt`.

Die neue Vergleichsfassung stammt laut [GGUF-Ersteller](https://huggingface.co/bartowski/Qwen_Qwen3-4B-Instruct-2507-GGUF/tree/ae44f08e1392f39c0e474af10c3ff8355c8b6688) aus [Qwen/Qwen3-4B-Instruct-2507](https://huggingface.co/Qwen/Qwen3-4B-Instruct-2507/tree/cdbee75f17c01a7cc42f958dc650907174af0554), ohne zusätzlich genanntes Fine-Tuning oder Abliterierung. Das offizielle Modell verwendet Apache 2.0 und keinen Denkmodus. Die Konvertierung nennt llama.cpp b6096 und eine Importance-Matrix. Die damalige genaue Basismodellrevision ist nicht angegeben; die verlinkte offizielle Revision ist die beim Quellenabruf vorhandene Revision. Die geprüfte GGUF-Datei selbst ist vollständig gepinnt:

- Repository `bartowski/Qwen_Qwen3-4B-Instruct-2507-GGUF`.
- Revision `ae44f08e1392f39c0e474af10c3ff8355c8b6688`.
- Datei `Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf`, **2.497.280.736 Bytes**.
- SHA-256 `2fde00ce69dd4899c70d020845e2638353015bba0fdf161b3eb965f2bca4464e`.

Beide Qwen-Dateien verwenden Q4_K_M und dieselbe llama.cpp-Laufzeit `d2e54583c7452353eb35d40431281f6ee984332f` (0.5.0). Unterschiedlich bleiben die Gewichte und Quantisierungs-/Kalibrierungsherkunft. Gemma ist ein zusätzlicher Architektur-/Laufzeitkontrollkandidat mit LiteRT-LM 0.17.1, kein reiner Quantisierungsvergleich.

Gemeinsames Profil: CPU 4 Threads, Kontext 4.096, Ausgabe 512, Seed 42, Temperatur 0,75, Top-P 0,9, Top-K 40, Wiederholungsstrafe 1,08 / Fenster 256. Native Endmarkierungen bestimmen den Abschluss. Huihui benötigt ChatML als Ersatz für die fehlende eingebettete Vorlage. Qwen Original verwendet seine eigene GGUF-Vorlage und seinen Tokenizer; Gemma die bestehende eigene Vorlage und den exakten SentencePiece-Tokenizer. [GGUF-Metadaten](validation/quality-search-0.8.3/gguf-metadata.json) enthalten Vorlagen und BOS/EOS.

Die getrennte Samplingkontrolle folgte den [offiziellen Empfehlungen](https://huggingface.co/Qwen/Qwen3-4B-Instruct-2507): Temperatur 0,7, Top-P 0,8, Top-K 20, Min-P 0. Beide Antworten waren identisch zu ihren gemeinsamen Kontrollen. Die bestehenden Einstellungen bleiben erhalten. Das allgemeine Ausgabelängenbeispiel von 16.384 Tokens wurde nicht übernommen; alle Antworten schlossen innerhalb von 512 Tokens ab. Größere Quantisierung und 8.192 Kontext wurden nicht als weitere gleichzeitige Änderungen eingeführt.

## EmbeddingGemma 300M

Kandidat: [google/embeddinggemma-300m](https://huggingface.co/google/embeddinggemma-300m/tree/57c266a740f537b4dc058e1b0cda161fd15afa75). Originalgewichte verlangen eine Lizenzbestätigung. Die öffentliche [GGUF-Fassung des Laufzeitprojekts](https://huggingface.co/ggml-org/embeddinggemma-300M-GGUF/tree/0f741b5a6585bd53aeb15cd1372c56f2a0f65e12) ist ohne Token erreichbar; die [Gemma-Nutzungsbedingungen](https://ai.google.dev/gemma/terms) gelten weiterhin und werden im App-Download verlinkt. Nicht mit EmbeddingGemma 2 verwechseln.

Datei `embeddinggemma-300M-Q8_0.gguf`, Revision `0f741b5a6585bd53aeb15cd1372c56f2a0f65e12`, **333.590.944 Bytes**, SHA-256 **`b5ce9d77a3fc4b3b39ccb5643c36777911cc4eb46a66962eadfa3f5f60490d63`**.

Die [gepinnte Laufzeit](https://github.com/ggml-org/llama.cpp/tree/d2e54583c7452353eb35d40431281f6ee984332f) unterstützt `gemma-embedding`, nicht kausale Aufmerksamkeit, Mean-Pooling und die vorhandenen Dense-Projektionen 768→3072→768. Die [Android-Anleitung](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md) beschreibt den NDK-Weg; hier wurde die Erweiterung tatsächlich für ARM64 in die vorhandene JNI-Bibliothek gebaut. Ein physischer Android-Inferenznachweis steht aus. Keine zweite Suchmodellalternative war nötig.

Wirksame Eingaben und Verarbeitung:

- Modell-eigener GGUF-Tokenizer, automatisch BOS 2 / EOS 1; Quelltext wird nicht als Sondertoken interpretiert.
- Dokumentpräfix `title: none | text: `; Anfragepräfix `task: search result | query: `.
- Mean-Pooling und Modellprojektionen, L2-Normalisierung, 768 Dimensionen, Cosinus.
- Modelllimit 2.048 Tokens; bewusst begrenzter Laufzeitkontext/Batches 512, CPU 2 Threads. Abschnitte höchstens 1.200 UTF-16-Zeichen. Überlange Eingaben werden abgewiesen, nicht still abgeschnitten; Wortsuche bleibt verfügbar.
- Vorverarbeitung `embeddinggemma300m-none-title-search-result-v1`, Indexversion 1. Modellkennung, Gewichts-SHA, Vorverarbeitung, Version und Dimension bestimmen den Vektorraum.

[Downloadnachweis](validation/quality-search-0.8.3/download-verification.json) und [bitgleiche erneute Inferenz](validation/quality-search-0.8.3/vectors-final-verification.json) dokumentieren die tatsächlich verwendeten Dateien und Vektoren.
