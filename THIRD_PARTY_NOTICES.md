# Bibliotheken und externe Bestandteile

Die Android-App verwendet die nachfolgend aufgeführten Bibliotheken. Ihre jeweiligen Lizenzhinweise bleiben maßgeblich. Der Apache-2.0-Lizenztext aus dem verwendeten LiteRT-LM-Release wird als Datei `LICENSE-APACHE-2.0.txt` in den Assets der App mitgeliefert.

| Bestandteil | Herkunft | Lizenz laut Projekt |
| --- | --- | --- |
| LiteRT-LM 0.17.1 | [Google AI Edge](https://github.com/google-ai-edge/LiteRT-LM) | Apache License 2.0 |
| Gemma 4 E2B, LiteRT-LM-Artefakt | [Verwendetes Modell](https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm) | Apache License 2.0 laut Modellkarte |
| AndroidX / Jetpack Compose | [Android Open Source Project](https://android.googlesource.com/platform/frameworks/support/) | Apache License 2.0 |
| Kotlin | [JetBrains](https://github.com/JetBrains/kotlin) | Apache License 2.0 |
| kotlinx.coroutines | [Kotlin](https://github.com/Kotlin/kotlinx.coroutines) | Apache License 2.0 |
| Gson 2.14.0 | [Google Gson](https://github.com/google/gson) | Apache License 2.0 |

Das Modell wird unverändert in einer festgelegten Revision von der genannten Bezugsquelle geladen. Die Modelldatei wird separat bei der ersten Einrichtung heruntergeladen.

| Modellangabe für App-Version 0.2.0 | Wert |
| --- | --- |
| Datei | `gemma-4-E2B-it.litertlm` |
| Revision | `b3ca0d2f076785a8f4b2219ddbd2bdb99954eae1` |
| Größe | 2.588.147.712 Byte |
| SHA-256 | `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c` |

Die Entwicklungswerkzeuge und optionalen Robolectric-/Roborazzi-Tests haben eigene Lizenzdateien in ihren ursprünglichen Distributionen. Sie werden nicht als Laufzeitfunktionen in die App aufgenommen.

Die Porträts und das Szenenbild wurden für dieses Projekt erzeugt. Die App verwendet keinen Emochi-Markennamen und keine von der Emochi-Webseite kopierten Figurenbilder oder App-Dateien. Die verfassten Figurenbeschreibungen und Ausgangsszenen gehören zum Projektinhalt.
