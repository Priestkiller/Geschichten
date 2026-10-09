# Geschichten 0.8.7 – Dauerhaftes Gedächtnis und Faktenkorrekturen

**Testversion 0.8.7, Versionscode 21.** Dieses Update bündelt die bisher lokalen Änderungen seit dem öffentlichen Stand 0.7.3.

- Dauerhaftes, bearbeitbares Geschichten-Gedächtnis mit Quellen, Verlauf, Eigentümern, Trägern, Orten, Beziehungen und getrenntem Figurenwissen.
- Auswertung im Gesprächsablauf, sofortige Berücksichtigung gespeicherter Nutzerkorrekturen, revisionsgebundene Verarbeitung und Abruf relevanter älterer Originalstellen unter dem tatsächlichen Tokenbudget.
- Gezielte Verbesserungen bei Rollen, Fragen, Vermutungen, hypothetischen Ereignissen und belegten Fakten. Gefundene Originalabschnitte enthalten mehr passenden Kontext aus derselben Nachricht.
- Optional einschaltbare Bedeutungssuche, Qwen 3 4B Original Instruct 2507 als siebtes Textmodell sowie standardmäßig ausgeschaltete Versuche für kurze Faktenantworten und eine zusätzliche Fakten-KI.
- Fix in 0.8.7: Bei „Er gehört weiterhin Oda“ wird weiterhin als Zeitangabe erkannt. Oda bleibt die Eigentümerin; eine korrekte Antwort wird deswegen nicht mehr blockiert. Dasselbe gilt an dieser Stelle für jetzt/nun. Echte Eigentümerwidersprüche bleiben Konflikte.
- Das Repository enthält jetzt auch den aktuellen Android-Quellcode und die Testunterlagen für die Arbeit mit Codex auf dem Server.

**Antwortqualität bleibt begrenzt:** Modelle können weiterhin Personen verwechseln, unbekannte Vergangenheit erfinden und unverständlich schreiben. Ein verlässlicher Gewinn des Faktenhelfers ist nicht belegt; er bleibt standardmäßig aus. Es wurde kein eigenes Modell trainiert. Die Android-App hat noch keine Ollama-Anbindung.

Die bestehenden 90 Figuren, langen Einstiege, Profile, Modellwechsel und Darstellungsfunktionen bleiben erhalten. Hauptschema 10, Suchindexschema 1. Paket-ID und Signatur sind kompatibel mit den bisherigen Testupdates.

**Installieren:** Einstellungen → App-Updates → Testversionen einbeziehen → Nach Updates suchen. Alternativ die APK aus diesem Release öffnen. Die vorhandene App nicht deinstallieren. Android bestätigt die Installation.

**Prüfung:** unveränderte APK des am 8. Oktober geprüften Stands; 148 reguläre und 100 relevante Integrationstests (26 Überschneidungen), zusätzlicher SQLite-Nachweis, Build/Lint bestanden. APK-Version, Bytegröße, SHA-256 und bisherige Signatur wurden für die Veröffentlichung erneut geprüft. Ein physisches S24 war nicht angeschlossen; die Installation und der gesamte Updateablauf auf dem Gerät bleiben zu prüfen.

APK: `Geschichten-0.8.7.apk`, 321708671 Bytes. SHA-256: `fd15af5bb3928674a4b965ab0d773dceee4bd5f84068b45fb9663da283873071`.

Die bisherigen Server-Testskripte und Messungen liegen unter docs im Repository; sie sind kein vollständiger Chat-Export. Der Android-Quellcode liegt auf `main` und in GitHubs automatisch erzeugtem Quellcodearchiv.

---

# Geschichten 0.2.0 – 20 Figuren und App-Updates

**Testversion 0.2.0, Versionscode 2.**

## Neu: 20 Figuren zum Ausprobieren

Zu Runa, Elara, Leon und Mira kommen 16 neue Persönlichkeiten mit eigenen Porträts, Stimmen, Zielen, Schwächen und Geschichten:

- **Aelwyn**, Waldelfen-Späherin; **Borin**, zwergischer Runenschmied; **Kael**, menschlicher Paladin; **Nyra**, Tiefling-Diplomatin.
- **Sylwen**, Druidenhüterin; **Varen**, vampirischer Archivar; **Thora**, orkische Karawanenführerin; **Orin**, Rabengestaltwandler.
- **Vaelgor**, uralter Bronzedrache; **Fenrik**, sprechender Runenwolf; **Soryn**, Waldgeist in Hirschgestalt; **Seris**, Nixe und Meeresbotin.
- **Nessa**, Fuchswandlerin; **Korr**, bewusster Steinwächter; **Pyra**, Phönix; **Aruun**, sprechender Greif.

Alle Figuren sind erwachsen; die Fantasywesen handeln als eigenständige Gesprächspartner. Jeder Standardcharakter beginnt mit einer eigenen Szene und vier bearbeitbaren Startnotizen. Im Figureneditor lassen sich alle 20 Porträts auswählen. Alle Figuren verwenden dasselbe lokale KI-Modell.

## Neu: Update-Button in der App

Unter **Deine App → App-Updates** gibt es **Nach Updates suchen**. Über GitHub bereitgestellte neue Versionen zeigen ihre Änderungen und lassen sich direkt aus der App herunterladen. **Testversionen einbeziehen** ist zunächst eingeschaltet.

Vor der Installation prüft die App Dateigröße, SHA-256, Paket-ID, Version, Android-Mindestversion und die passende Signatur. Anschließend öffnet **Update installieren** den Android-Dialog. Android fragt nach der erforderlichen Freigabe; die Installation bestätigst du im Systemdialog.

Die Updatesuche startet nur auf Knopfdruck. Dafür und für neue APKs wird Internet benötigt. Das bereits eingerichtete Sprachmodell erzeugt seine Antworten weiter direkt auf dem Handy; Geschichten und Erinnerungen bleiben lokal.

## Von 0.1.0 aktualisieren

**`Geschichten-0.2.0.apk` einmal direkt herunterladen, öffnen und als Update installieren. Die vorhandene App vorher nicht deinstallieren.** Erst diese Version ergänzt den Update-Button für weitere Veröffentlichungen.

Die Datenbankmigration ergänzt fehlende Figuren und erhält vorhandene Geschichten, Erinnerungen, eigene Figuren und bereits bearbeitete Profile. Die Paket-ID `dev.vincent.geschichten` und das bisherige Signaturzertifikat bleiben gleich.

## Dateien und Voraussetzungen

| Punkt | Wert |
| --- | --- |
| APK | `Geschichten-0.2.0.apk` |
| Updatebeschreibung | `geschichten-android-update.json` |
| App-Version | 0.2.0, Versionscode 2 |
| Plattform | Android 12 oder neuer, ARM64 |
| Release-Kanal | GitHub-Vorabversion / Test |

Die exakte Dateigröße und die SHA-256-Prüfsumme stehen im mit dieser APK erzeugten Manifest.

## Teststand

Dies ist eine Testversion. Ein erfolgreicher Build und automatisierte Datenbank-, Update- und Oberflächenprüfungen ersetzen keinen Test des Sprachmodells auf einem physischen Galaxy S24. Modellgeschwindigkeit, Qualität längerer deutscher Dialoge, Speicherbedarf und Wärmeentwicklung müssen weiterhin am Handy geprüft werden. Auch der vollständige Download und die anschließende Updateinstallation sind am Zielgerät zu prüfen.
