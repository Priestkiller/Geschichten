# Bibliotheken und externe Bestandteile

Dieses Projekt verwendet Bibliotheken über festgelegte Gradle-Abhängigkeiten. Ihre jeweiligen Lizenzhinweise bleiben maßgeblich. Die Datei `app/src/main/assets/LICENSE-APACHE-2.0.txt` enthält den Apache-2.0-Lizenztext aus dem verwendeten LiteRT-LM-Release und wird in der App mitgeliefert.

| Bestandteil | Herkunft | Lizenz laut Projekt |
| --- | --- | --- |
| LiteRT-LM 0.17.1 | Google AI Edge, https://github.com/google-ai-edge/LiteRT-LM | Apache License 2.0 |
| Gemma 4 E2B, LiteRT-LM-Artefakt | https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm | Apache License 2.0 laut Modellkarte |
| Qwen 2.5 1.5B und Qwen 3 0.6B, LiteRT-LM-Artefakte | https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct und https://huggingface.co/litert-community/Qwen3-0.6B | Apache License 2.0 laut Modellkarten |
| llama.cpp v0.5.0, Revision d2e54583c7452353eb35d40431281f6ee984332f | https://github.com/ggml-org/llama.cpp/tree/d2e54583c7452353eb35d40431281f6ee984332f | MIT; LICENSE-LLAMA-CPP.txt in der App |
| Dolphin 3.0 Llama 3.2 3B, GGUF Q4_K_M | https://huggingface.co/bartowski/Dolphin3.0-Llama3.2-3B-GGUF | Llama 3.2 Community License; LICENSE-LLAMA-3.2.txt in der App. Built with Llama. |
| Huihui Qwen 3 4B Instruct 2507, GGUF Q4_K_M | https://huggingface.co/mahdisml/Huihui-Qwen3-4B-Instruct-2507-abliterated-Q4_K_M-GGUF | Apache License 2.0 laut Modellkarte |
| Gemma 3 4B DBL-X von DavidAU, GGUF Q4_0 | https://huggingface.co/DavidAU/Gemma-3-it-4B-Uncensored-DBL-X-GGUF | Modellkarte nennt Apache License 2.0; für die Gemma-3-Grundlage gelten die Google Gemma Terms of Use. LICENSE-GEMMA-3.txt enthält die offiziellen Bedingungen. |
| AndroidX / Jetpack Compose | https://android.googlesource.com/platform/frameworks/support/ | Apache License 2.0 |
| Kotlin | JetBrains, https://github.com/JetBrains/kotlin | Apache License 2.0 |
| kotlinx.coroutines | JetBrains, https://github.com/Kotlin/kotlinx.coroutines | Apache License 2.0 |

Die Modelle werden unverändert in festgelegten Revisionen von den genannten Bezugsquellen geladen. Sie sind nicht in die APK eingebettet. Revisionen, exakte Bytegrößen, Dateiformate und Prüfsummen stehen in `LocalModelCatalog.kt`. Die ursprüngliche Gemma-Datei bleibt mit `ModelArtifact.kt` kompatibel. Jede Modellkarte ist über „Modellinfos und Lizenz“ in der Auswahl erreichbar. Eigene Änderungen am llama.cpp-Quellcode gibt es nicht; die zusätzliche JNI-Brücke gehört zum App-Code.

Die Entwicklungswerkzeuge und optionalen Robolectric-/Roborazzi-Tests haben eigene Lizenzdateien in ihren ursprünglichen Distributionen. Sie werden nicht als Laufzeitfunktionen in die App aufgenommen.

Die Porträts und das Szenenbild wurden für dieses Projekt erzeugt. Die App verwendet keinen Emochi-Markennamen und keine von der Emochi-Webseite kopierten Figurenbilder oder App-Dateien. Die verfassten Figurenbeschreibungen und Ausgangsszenen gehören zum Projektinhalt.
