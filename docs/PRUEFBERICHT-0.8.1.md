# Prüfung von Geschichten 0.8.1

Lokaler Debug-Teststand vom 06.10.2026, Versionscode 15, Datenbankversion 9. Der gesamte Antwortablauf verwendet das Gedächtnis automatisch. Ältere passende Originalstellen werden zur aktuellen Frage aus dem vollständigen Archiv abgerufen und mit dem tatsächlichen Tokenizer eingeplant. Manuelle Korrekturen sind sofort gespeichert; Archivreihenfolge und Zustandsrevision schützen sie vor altem Nachlesen und verspäteten Antworten.

Die signierte [Geschichten-0.8.1.apk](../Geschichten-0.8.1.apk) ist als kompatibles lokales Update gebaut. Nicht vorher deinstallieren. Es gibt keine Veröffentlichung und keine neue Modell-Datei.

110 reguläre Abschlussprüfungen und 73 gezielte Android-/SQLite-/ViewModel-/UI-/Migrationsprüfungen bestanden. Die Läufe überschneiden sich. Lint: null Fehler, 125 Warnungen. Ein in der Prüfung nachgewiesener Besitzerfehler bei „Am Brunnen habe ich …“ wurde repariert. Neue Quellen werden automatisch verarbeitet; unveränderte Quellen werden anhand ihrer gespeicherten Fortschrittsmarkierungen ausgelassen.

Die Nachweise unterscheiden **gespeicherten Stand**, **tatsächliche Modelleingabe** und **echte Modellantwort**. Der Archivabruf und die unmittelbare Übergabe neuer Angaben sind unabhängig von der Antwortqualität geprüft. Kleine Modelle können weiterhin Beteiligte, Sprache und unbekannte Vergangenheit falsch darstellen. Die begrenzte Antwortprüfung erkennt einige Widersprüche; unerkannte unplausible Prosa ist weiterhin möglich.

18 echte Antworten mit allen sechs vorhandenen Modellen wurden getrennt bewertet. In allen 18 Fällen stimmt die geplante Tokenzahl mit der Laufzeit überein. Die Originalstellen und die neue blaue Farbe sind tatsächlich übergeben. Huihui und Qwen 2.5 beantworten die Farb-/Besitzerfrage korrekt. Mehrere Modelle vertauschen aber die Beteiligten der alten Briefübergabe; unbekannte Vergangenheit wird teilweise erfunden. Die korrekte Modellverwendung ist somit nur teilweise erreicht, trotz korrekter Speicherung und Übergabe.

Der ausführliche [Bericht zur fortlaufenden Auswertung](aktives-gedaechtnis-0.8.1.md) enthält Ablauf, Auslöser, Tests, echte Antworten und verbleibende Fehler pro Ebene. Rohbelege stehen unter `docs/validation/active-memory-0.8.1/`. Die Datenbankstruktur und UI aus 0.8.0 bleiben erhalten; eine neue globale Auswertung alter Chats beim App-Start erfolgt nicht.

APK: 321455909 Bytes, SHA-256 `38ffe484693d15b8b17ed70de63e22749eeeb897481bf8742b9cfdacbd688125`. Paket-ID und bestehender Signierer sind geprüft; native Laufzeit und Tokenizer sind bytegleich mit 0.8.0. Ein S24 ist nicht angeschlossen. Geräteinstallation, RAM und Verhalten bei Prozess-/Hintergrundwechseln bleiben offen.
