# Prüfbericht – Geschichten 0.7.1

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.7.1, Versionscode 10, Datenbankversion 7.

## Ergebnis

[0.7.1 ist öffentlich veröffentlicht](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.1). Alle sechs Modellkarten erläutern jetzt ausführlicher, warum das Modell zum eigenen Vergleich interessant sein kann, welche Schreibaufgaben sich anbieten und welche Grenzen zu beachten sind. Zwei getrennte Links öffnen die Beschreibung des Anbieters sowie die festgelegte Downloadquelle mit Lizenz. Aussagen der Anbieter sind entsprechend gekennzeichnet; es wird keine Qualitätsrangliste für das S24 versprochen.

Dolphin wird als Alternative für Figuren, Stimmung und Sprachvorgaben beschrieben. Huihui erläutert vielseitige Dialoge, den fehlenden zusätzlichen Denkmodus und das veränderte Ablehnungsverhalten. DavidAUs Gemma 3 nennt kreatives Schreiben, Rollenspiele und Szenenfortsetzungen. Die konkrete Gemma-3-Datei hat 2.576.023.488 Byte, also rund 2,58 GB. Auch Gemma 4 und die beiden kleinen Qwen-Modelle erhalten verständliche Vergleichshinweise.

Die Beschreibungen wurden mit den offiziellen Modellkarten abgeglichen: [Dolphin](https://huggingface.co/dphn/Dolphin3.0-Llama3.2-3B), [Huihui](https://huggingface.co/huihui-ai/Huihui-Qwen3-4B-Instruct-2507-abliterated), [Qwen 3 Instruct 2507](https://huggingface.co/Qwen/Qwen3-4B-Instruct-2507), [DavidAU](https://huggingface.co/DavidAU/Gemma-3-it-4B-Uncensored-DBL-X-GGUF), [Qwen 2.5](https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct), [Qwen 3 0.6B](https://huggingface.co/litert-community/Qwen3-0.6B), [Gemma 4](https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm). Einsatzempfehlungen für den eigenen Figurenvergleich sind eine redaktionelle Ableitung, keine gemessenen Ergebnisse.

## Validierung

Produktionsbuild `assembleDebug testDebugUnitTest lintDebug` mit Updatequelle Priestkiller/Geschichten: bestanden. 58 JVM-Testfälle bestehen; zusätzlich bestehen sechs gezielte Android-/Compose-Testfälle mit nativer Robolectric-Grafik: alle sechs Modelle auswählen ohne automatischen Download, Auswahl bei laufender Einrichtung/Generierung sperren, große Schrift und Erreichbarkeit der Updates, zwei Integrationsprüfungen der gespeicherten Auswahl und Downloads sowie die erste Einrichtungsansicht. Keine Fehler oder übersprungenen Fälle. Lint: 0 Fehler, 114 bestehende Warnungen.

Originalaufnahmen der sechs Modellkarten und der Darstellung bei 130 Prozent Schriftgröße wurden erzeugt. Dolphin und die große Gemma-3-Ansicht wurden visuell geprüft; Texte bleiben lesbar und scrollbar. Die Aufnahmen verwenden vorbereitete Zustände in der tatsächlichen Compose-Oberfläche und belegen keine Modellleistung auf einem physischen Handy.

Die drei nativen Bibliotheken in der APK sind bytegleich zur geprüften Version 0.7.0. Modellgewichte, Prüfsummen, Laufzeiten und Inferenzlogik sind unverändert. Die vollständigen Downloads und nativen Proben aller sechs Modelle sowie die Streaming-/Unicode-, Abbruch- und Neustartprüfungen aller drei GGUF-Modelle aus [0.7.0](PRUEFBERICHT-0.7.0.md) gelten weiterhin; sie wurden für die Beschreibungsänderung nicht erneut ausgeführt. Die APK enthält keine Modellgewichte, Testquellen oder Signierschlüssel. ZIP-Ausrichtung für 16 KB: bestanden.

## Paket und Veröffentlichung

- APK `Geschichten-0.7.1.apk`: 313.286.124 Byte.
- APK SHA-256: `e9a1fe48bf2692d2a6dd05078d67e2d844fd66c2498e7687c6d1302b1b4225a8`.
- Updatebeschreibung: 317 Byte; SHA-256 `62f5ab855894dca2b7e0cd8e90860a94b3b50deebd10f4dd8c23b94192805e8e`.
- Signierzertifikat SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`.
- Paket `dev.vincent.geschichten`, Android 12 / minSdk 31, ARM64, Versionscode 10; kompatible bisherige Signatur.
- Release-ID 403105465; öffentlich, Testversion, kein Entwurf.
- README aktualisiert in Commit `0103f0e60e0bf513ec096f04caf926cc7d19b013`, Dateistand `171026b02247723b6e5d999a461cd6bfdb512d8b`.

Der komplette anonyme öffentliche APK-Download stimmt in Größe und SHA-256 mit der lokalen geprüften Datei überein. Öffentliche Updatebeschreibung und README stimmen bytegenau mit den vorbereiteten Dateien überein. Der tatsächlich kompilierte App-Parser erkennt 0.7.1 / Code 10 als kompatibles neueres Testupdate für 0.7.0 / Code 9.

Unter Einstellungen → App-Updates „Testversionen einbeziehen“ einschalten und nach Updates suchen. Eine vorhandene Installation nicht deinstallieren. Installation, Geschwindigkeit, Speicherverbrauch und langfristige Schreibqualität auf einem physischen S24 wurden nicht geprüft.

Nachweise: [Gesamtergebnis](validation/models-0.7.1/summary.json), [öffentliche Prüfung](validation/models-0.7.1/public-release-summary.json), [App-Parser](validation/models-0.7.1/actual-update-parser.log), [Paketprüfung](validation/models-0.7.1/check-package.py). Die XML-Testberichte, Originalaufnahmen und Logs liegen im selben privaten Prüfverzeichnis; die Browseraufnahme im übergeordneten Ordner `release-0.7.1`.
