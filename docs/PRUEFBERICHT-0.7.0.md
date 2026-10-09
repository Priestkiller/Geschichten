# Prüfbericht – Geschichten 0.7.0

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.7.0, Versionscode 9, Datenbankversion 7.

## Ergebnis

[0.7.0 ist veröffentlicht](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.0). Unter Einstellungen → Welche KI passt zu dir? sind sechs Modelle auswählbar: Gemma 4 E2B, Qwen 2.5 1.5B, Qwen 3 0.6B, Dolphin 3.0 Llama 3.2 3B, Huihui Qwen 3 4B Instruct 2507 und DavidAUs Gemma 3 4B DBL-X. Es wurden keine neuen Geschichte- oder Antworttexte eingefügt. Die 90 vorhandenen Figuren und ausführlichen Einführungen bleiben erhalten.

Die App speichert die Auswahl, lädt nur nach Betätigung des Downloadknopfs herunter und erhält vorhandene Modell-Dateien sowie getrennte angehaltene Downloads. Beim Wechsel wird nur eine Laufzeit gehalten. Bestehende Gespräche und Erinnerungen bleiben gespeichert. Auswahl und Senden sind während Einrichtung beziehungsweise Modellwechsel gesperrt. Vor dem Laden wird die SHA-256 der ausgewählten Datei überprüft. Die ursprüngliche Gemma-Datei bleibt verwendbar.

## Prüfungen

Der Produktionsbuild mit eingestellter Updatequelle Priestkiller/Geschichten besteht. 58 JVM-Testfälle und zehn Android-/Compose-Testfälle unter Robolectric bestehen ohne Fehler oder übersprungene Fälle. Lint: 0 Fehler, 114 Warnungen. Die sechs zusätzlichen Warnungen gegenüber 0.6.0 betreffen fünf Abhängigkeitsversionen und eine KTX-Stilempfehlung. Die native Android-Bibliothek wurde in allen 74 Übersetzungsschritten mit `-O3` optimiert.

Alle sechs vollständig heruntergeladenen, unveränderlich referenzierten Modell-Dateien stimmen in Größe und SHA-256 mit dem Katalog überein. Alle sechs wurden auf Windows mit nativen CPU-Laufzeiten geladen und erzeugten eine Antwort auf eine neutrale Probe. Für LiteRT-LM wurde die offizielle JVM-Laufzeit 0.17.1 verwendet; für GGUF wurde der tatsächliche Produktions-JNI-Quellcode mit llama.cpp v0.5.0 für Windows kompiliert. Alle drei GGUF-Modelle bestanden Unicode-/Streaming-Abgleich, Abbruch aus einem zweiten Thread und anschließende neue Generierung. Huihui verwendet wegen des fehlenden eingebetteten Chattemplates das anhand des ursprünglichen Qwen-Tokenizers geprüfte ChatML-Format.

Die vollständige signierte APK enthält weder Modellgewichte noch Testquellen oder Signierschlüssel. Die ZIP-Ausrichtung und alle ELF-LOAD-Ausrichtungen der drei ARM64-Bibliotheken erfüllen 16 KB.

## Veröffentlichung

- APK: `Geschichten-0.7.0.apk`, 313.286.124 Byte.
- APK SHA-256: `9d6d0b794fa156f1dc91bf6faafd0d99cb3de107170e30402321a5c2e7022f8c`.
- Signierzertifikat SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`.
- Paket: `dev.vincent.geschichten`; Android 12 / minSdk 31; ARM64; gleicher Signierer wie bisher.
- Release-ID 403094217; veröffentlicht 2026-10-04 16:05:14 UTC; Testversion, kein Entwurf.
- Öffentliche README: Commit `c0a9168808bf65ffae42fed0b5f3ce9d15a2be72`.
- Öffentliche Lizenzhinweise: Commit `3e151c7fd6c2f56ac1d33564af2727940b97e4c0`.

Der vollständige anonyme öffentliche APK-Download stimmt in Bytegröße und SHA-256 mit der lokalen geprüften Datei überein. Updatebeschreibung und README sind öffentlich erreichbar und stimmen mit den vorbereiteten Dateien überein. Der tatsächlich kompilierte App-Parser erkennt Versionscode 9 als kompatibles neueres Update für 0.6.0 / Code 8 bei eingeschalteten Testversionen.

## Grenzen und Nachweise

Ein physisches Galaxy S24 wurde nicht installiert oder vermessen. Die Desktop-Proben bestätigen Laden und Generieren, keine Rangliste für deutsche Schreibqualität, lange Geschichten oder Geschwindigkeit auf dem Handy. Modell-Dateigröße und Arbeitsspeicherbedarf sind unterschiedliche Größen.

Nachweise: [Gesamtergebnis](validation/models-0.7.0/summary.json), [öffentliche Prüfung](validation/models-0.7.0/public-release-summary.json), [App-Parser](validation/models-0.7.0/actual-update-parser.log). Native Modellproben, Downloadprüfungen, XML-Testberichte, Lintbericht und Compose-Aufnahmen liegen im selben privaten Prüfverzeichnis.
