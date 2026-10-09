# Prüfbericht – Geschichten 0.7.4, Arbeitsstand

Prüfdatum: 4. Oktober 2026. Vorgesehene Version 0.7.4, Code 13, Datenbankversion 8. **Noch nicht veröffentlicht und nicht als vollständiger Qualitätsfix freigegeben.**

## Gemeldeter Fehler und Befund

Die Screenshots stammen aus 0.7.2. 0.7.3 änderte Geschwindigkeit und Messung, nicht den Erzählprompt. Die unverständliche Selbstvorstellung „Mira.“ stammt aus dem verfassten Einstieg. Die späteren Wort-, Rollen- und Anschlussfehler stammen aus der Modellgenerierung. Beides ließ sich lokal nachvollziehen.

Eine echte technische Ursache liegt in der Nachrichtenfolge: Die App begann ihren Modellverlauf mit einer Figurenantwort. Der eingebundene Gemma-Formatter legt Systemvorgaben in die erste Nutzernachricht; damit erschienen die Regeln bisher erst nach dem Einstieg. Eine vorgeschaltete neutrale Szenenaufforderung stellt eine korrekte Nutzer–Figur–Nutzer-Folge her. Dies gilt gemeinsam für alle sechs Modelle und alle Figuren. Die gespeicherte Geschichte wird dafür nicht geändert.

## Lokale Korrekturen

- Rollenregeln verlangen die nächste Antwort der gewählten Figur, eine direkte Reaktion auf den letzten Beitrag sowie getrenntes Wissen und getrennte Gegenstände beider Personen. Neue Aussagen der Spielerfigur haben Vorrang vor Annahmen des Einstiegs.
- Die begrenzten Figuren- und Szenendaten werden in lesbaren Abschnitten übergeben. Persönlichkeit, Ausgangslage, aktuelle Notizen und Zusammenfassung bleiben erhalten. Der laufende Dialog steht als strukturelle Nachricht im Verlauf und wird nicht zusätzlich als verschachteltes JSON in die Rollenbeschreibung kopiert. Die begrenzte und maskierte Datenaufbereitung bleibt intern für Auswahl und Diagnose erhalten.
- Eindeutig kopierte interne Datenüberschriften werden ebenso wie bisher kopierte JSON-Profile abgefangen. Die Eingabe bleibt für einen neuen Versuch erhalten. Diese Prüfung bewertet keine literarische Qualität und ersetzt keine Antworten durch vorgefertigte Geschichten.
- Verkürzte Selbstvorstellungen wie „Mira.“ werden in unveränderten Standardprofilen zu „Ich bin Mira.“. Die langen Einführungen werden nicht gekürzt. Datenbankversion 8 aktualisiert dazu nur unveränderte Profile und noch unbespielte Standardanfänge; bereits gespielte oder bearbeitete Inhalte bleiben erhalten.

## Tatsächliche Modellantworten

Die abschließende Serie verwendet aus dem kompilierten App-Code exportierte Vorgaben, Figuren, Ausgangslagen, Start-Erinnerungen und strukturelle Nachrichten. Alle sechs vollständigen vorhandenen Modelle wurden mit drei Szenen geprüft: die tatsächliche Mira-Nachricht aus dem Screenshot, eine ausdrückliche Weigerung, die Station zu betreten, und ein konkretes Leseangebot an Grask. Die Rohantworten stehen unter `validation/dialogue-0.7.4/*.final.json`.

Die Proben verwenden 4.096 Kontexttoken und begrenzen zur Untersuchung die Ausgabe auf 256 Token; die App behält 512. LiteRT-LM 0.17.1 läuft mit Seed 42, Temperatur 0,75, Top-K 40, Top-P 0,9, Wiederholungsstrafe 1,08 und Fenster 256. Der GGUF-Probelauf verwendet dieselbe Produktions-JNI-Quelle und dieselben Samplingwerte; sein Seed bleibt zufällig. Die abschließenden GGUF-Proben nutzen Windows x86-64 mit AVX2/FMA/F16C, nicht Android. Frühere Varianten liefen im generischen Windows-CPU-Backend. Dies sind keine S24-Geschwindigkeitsmessungen.

| Modell | Beobachtungen in den drei abschließenden Proben |
| --- | --- |
| Gemma 4 E2B | Erkennt Miras fehlendes Gegensignal und die Weigerung; Grask nimmt das Leseangebot wahr. Unsaubere Formulierungen und unpassende Zusätze bleiben. |
| Qwen 2.5 1.5B | Einzelne passende Reaktionen, aber falsche Behauptungen und Rollenwechsel bei Grask. |
| Qwen 3 0.6B | Kopiert Nutzertext bzw. Teile der Szenenaufforderung; für die geprüften ausführlichen Rollenspiele nicht verlässlich. |
| Dolphin 3 / Llama 3.2 3B | Reagiert teilweise auf die Situation, erzeugt aber widersprüchliche oder unlogische Fortsetzungen. |
| Huihui Qwen 3 4B | Grask zeigt das Register; Mira erkennt teilweise das Anliegen, erfindet jedoch unpassende Ausrüstung und macht später wieder widersprüchliche Annahmen. |
| Gemma 3 DBL-X | Weiterhin deutliche Wortbildungs-, Grammatik- und Sinnfehler. |

Weitere Diagnoseproben: lesbare deutsche und englische Regeln, Hintergrund statt Auftakt im Dialog, explizit markierter Nutzerbeitrag, reduzierte Temperatur/Wiederholungsstrafe sowie vollständig gierige Auswahl ohne Wiederholungsstrafe. Keine Variante beseitigt die Probleme für alle sechs Modelle. Auch die SHA-256-geprüfte alternative Q4_K_M-Komprimierung desselben Gemma-3-Modells erzeugt ausgeprägte Sprachfehler. Deshalb werden weder neue Gewichte noch die experimentellen Samplingwerte als angeblich bewährte Lösung eingebaut.

**Das Ziel einer zuverlässig sinnvollen, dynamischen Geschichte für alle sechs Modelle ist noch nicht erreicht.** Die Strukturkorrektur ist keine Garantie, dass jedes Modell die Vorgaben befolgt. Die Nutzerentscheidung über das Beibehalten bzw. Ersetzen unzuverlässiger Modelle wurde abgefragt. Keine Modelle wurden ohne diese Entscheidung entfernt oder ersetzt. GitHub-Release und öffentliche README bleiben auf 0.7.3.

## Technische Prüfung

Der signierte lokale Build mit `assembleDebug testDebugUnitTest lintDebug` ist erfolgreich: 76 JVM-Tests und 15 Datenbankmigrationstests bestehen ohne Fehler oder übersprungene Fälle. Android Lint meldet keine Fehler und 119 Warnungen. Die endgültigen JVM-Ergebnisse und Lint-Diagnosen liegen in `validation/dialogue-0.7.4/final-unit-results/` und `final-lint-results.xml`. Sie prüfen unter anderem den tatsächlichen lesbaren Modellkontext für alle 90 Figuren, den Erhalt der Nutzerbeiträge, Kontextgrenzen und das Abfangen kopierter Datenüberschriften. Die gezielte Datenbankprüfung steht in `final-migration-results.xml`.

Die Original-APK liegt lokal unter `app/build/outputs/apk/debug/app-debug.apk`: 313.384.428 Byte, SHA-256 `ced8515cdadac906d27a1631628a8ce263ecc9d58b488d62c51dbc8992e8796a`. Modellgewichte, Schlüsseldateien und Quellcode sind nicht enthalten. Zusammenfassung: [summary.json](validation/dialogue-0.7.4/summary.json). Keine Installation oder Qualitätsprüfung auf einem physischen S24/S24 Ultra wurde behauptet. Eine Veröffentlichung dieses Arbeitsstands erfolgte nicht.

## Primärquellen zur Anbindung

- [Gemma-Nachrichtenstruktur](https://ai.google.dev/gemma/docs/core/prompt-structure): Nutzer- und Modellrollen; Systemvorgaben gehören in den ersten Nutzerbeitrag.
- [Gemma-3-DBL-X-Modelldateien](https://huggingface.co/DavidAU/Gemma-3-it-4B-Uncensored-DBL-X-GGUF/tree/main): Herkunft der beiden geprüften Komprimierungen. Die Aussagen zur beobachteten deutschen Textqualität stammen aus unseren gespeicherten Proben, nicht aus Anbieterwerbung.
