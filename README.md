# Geschichten – Android 0.8.8

Die Testversion **0.8.8 (Versionscode 22)** ergänzt eine Ollama-Verbindung unter **Einstellungen → KI auf deinem PC**. Die bestehende App kann mit demselben Signierschlüssel aktualisiert werden.

Eine native Android-Testapp für deutsche Gespräche und Abenteuer mit 90 erwachsenen KI-Figuren. Die Antworten entstehen mit einem heruntergeladenen Modell auf dem Handy oder über Ollama auf deinem PC. Mindestvoraussetzung: Android 12 und ARM64. Zielgeräte sind Galaxy S24 und S24 Ultra.

## Testversion herunterladen

**[Release 0.8.8](https://github.com/Priestkiller/Geschichten/releases/tag/v0.8.8)** · [APK herunterladen](https://github.com/Priestkiller/Geschichten/releases/download/v0.8.8/Geschichten-0.8.8.apk)

Eine vorhandene Installation **nicht deinstallieren**. Die APK als Update öffnen. Alternativ unter **Einstellungen → App-Updates** die **Testversionen einbeziehen** einschalten und **Nach Updates suchen** antippen. Versionscode 22, Paket `dev.vincent.geschichten`, bisheriges Testzertifikat. Die App prüft Größe, SHA-256, Version und Signatur. Android bestätigt die Installation.

## Neu in 0.8.8: Ollama auf deinem PC

Unter **Einstellungen → KI auf deinem PC** die Serveradresse und das Modell eintragen, dann **Verbindung prüfen und verwenden** antippen. Handy und PC müssen einander im Heimnetz erreichen. Vorbelegt sind `http://192.168.178.73:11434` und `geschichten-gemma4-12b-test`; die Adresse muss zu deinem PC passen. Unterstützt wird die geprüfte Gemma-4-12B-Q4_K_M-Datei aus Ollama, auch unter einem eigenen Alias. Ollama muss für Verbindungen aus dem Heimnetz eingerichtet sein.

Die PC-KI erhält Figurenprofil, passende Erinnerungen und den ausgewählten Gesprächsverlauf. Geschichten und Gedächtnis bleiben auf dem Handy gespeichert. **PC-KI verwenden** lässt sich ausschalten, um wieder ein Handymodell zu verwenden. Abbrüche oder unvollständige Antworten stellen deine Nachricht als Entwurf wieder her.

## Änderungen seit dem veröffentlichten Stand 0.7.3

- **Dauerhaftes Gedächtnis:** aktueller Geschichtenstand, Gegenstände, Eigentümer, Träger, Ablageorte, Beziehungen, Verletzungen, Quellen, frühere Fassungen und getrenntes Figurenwissen. Unter Gedächtnis lassen sich Angaben ansehen, korrigieren, anheften und ausschließen.
- **Aktiver Gesprächsablauf:** neue Nutzerangaben, abgeschlossene Antworten und manuelle Korrekturen werden im Speicherablauf berücksichtigt. Vor der Antwort werden Profil, Zustand, passende Erinnerungen, Originalstellen und jüngste Gesprächsrunden unter dem tatsächlichen Tokenbudget zusammengestellt. Zustandsrevisionen schützen neue Korrekturen vor verspäteten Ergebnissen.
- **Gezielte Rollen- und Faktenkorrekturen:** erkannte Nutzerkorrekturen, Sprecher/Adressat, Besitz gegenüber Trägerschaft sowie Fragen und hypothetische Aussagen werden in den begrenzt unterstützten Fällen genauer getrennt. Ausschlüsse bleiben beim Originalabruf wirksam.
- **Ältere Originalstellen:** gefundene Abschnitte erhalten passenden Kontext aus derselben Nachricht. Eine experimentelle Bedeutungssuche mit separatem Suchmodell ist abschaltbar; ohne sie bleibt die Wortsuche verfügbar.
- **Siebtes Textmodell:** Qwen 3 4B Original Instruct 2507 als zusätzliche experimentelle Alternative. Die bestehende Auswahl wird nicht automatisch umgestellt.
- **Optionale Versuche:** kurze Faktenantworten aus bestätigten Zuständen und eine zusätzliche Fakten-KI. Beide bleiben standardmäßig ausgeschaltet; ein verlässlicher Qualitätsgewinn des Faktenhelfers ist nicht belegt.
- **Konkreter Fix in 0.8.7:** „Er gehört weiterhin Oda“ liest `weiterhin` als Zeitangabe und Oda als Eigentümerin. Auch `jetzt` und `nun` werden an dieser Stelle richtig behandelt. Tatsächlich gegensätzliche Eigentümerangaben bleiben Konflikte.

**Grenze:** Die vorhandenen Modelle können trotz richtig gespeichertem und übergebenem Kontext Rollen verwechseln, unbelegte Vergangenheit erfinden und unpassend erzählen. Diese Version behebt gezielt nachgewiesene Speicher-/Abruf-/Filterfehler; eine allgemein verlässliche Erzähl-KI ist noch nicht erreicht. Die ausführlichen [Prüfberichte](docs/PRUEFBERICHT-FAKTENHELFER-0.8.6.md) dokumentieren verbleibende Fehler.

## Figuren, Verlauf und Darstellung

Neun Kategorien mit je zehn Figuren, jeweils fünf weiblich und fünf männlich: nordische Fantasy, Fantasy, Krimi, Science-Fiction, Kreaturen, Mittelerde, Blade Runner, Cyberpunk 2077 und Monster. Eigene Figuren, Porträts, Eigenschaften, Schwächen und Sprechweisen; ausführliche Chat-Einstiege erklären deine Rolle, Vorgeschichte und aktuelle Begegnung. Laufende Gespräche öffnen bei der letzten Nachricht. Das Profil ist unabhängig davon erreichbar.

Die Reiter heißen **Figuren · Verlauf · Gedächtnis · Einstellungen**. Direkte Rede erscheint fett und goldfarben, Beschreibungen kursiv in hellem Grau. **Verlauf leeren** löscht nach Bestätigung Gespräche und zugehörige Erinnerungen; Figuren, Einstellungen und Modelldateien bleiben erhalten. Beim Modellwechsel kann die bisherige Datei behalten oder gelöscht werden.

## Lokale Modelle

| Modell | Download, ungefähr | Laufzeit |
| --- | ---: | --- |
| Gemma 4 E2B, bisherige KI | 2,59 GB | LiteRT-LM |
| Qwen 2.5 1.5B | 1,60 GB | LiteRT-LM |
| Qwen 3 0.6B | 345 MB | LiteRT-LM |
| Dolphin 3.0 Llama 3.2 3B | 2,02 GB | llama.cpp |
| Huihui Qwen 3 4B Instruct 2507 | 2,50 GB | llama.cpp |
| Gemma 3 4B DBL-X von DavidAU | 2,58 GB | llama.cpp |
| Qwen 3 4B Original Instruct 2507 | 2,50 GB | llama.cpp |

Die verständlichen Modellkarten nennen Einsätze, Grenzen, Downloadgröße und Anbieterlinks. Es gibt keine garantierte Qualitätsrangliste. Downloadgröße und Arbeitsspeicherbedarf sind verschieden. Nur gewählte Modelle werden geladen und mit festgelegter SHA-256 geprüft. Die APK und dieses Repository enthalten keine Inferenzgewichte; eingebettet sind lediglich benötigte Tokenizerdaten. Chats bleiben im privaten Android-App-Speicher.

## Quellcode und Arbeit auf dem Server

Seit dieser Veröffentlichung enthält `main` den aktuellen Android-Quellcode, die Figurendaten, Porträts, Tests, Gradle-Dateien und festgelegten nativen Bibliotheken. Auf dem Server kann Codex das Repository klonen:

```sh
git clone -c core.longpaths=true https://github.com/Priestkiller/Geschichten.git
```

Den geklonten Ordner in Codex als Projekt auswählen. Für den Stand der Ollama-Versuche zuerst [SERVER-FORTSETZUNG.md](docs/SERVER-FORTSETZUNG.md) und den [Server-Prüfbericht](docs/PRUEFBERICHT-SERVER-2026-10-09.md) lesen. Der vollständige frühere Codex-Chat wird durch das Klonen nicht übertragen.

Die Version 0.8.8 verwendet für die PC-KI das geprüfte `geschichten-gemma4-12b-test` im normalen Antwortmodus. Die App übergibt die bestehenden Figuren, den ausgewählten Verlauf und das vorhandene Gedächtnis an `/api/chat`. Nur sichtbarer Antworttext wird angezeigt und nach vollständigem Antwortende durch die bisherigen Speicherprüfungen verarbeitet. Serveradresse und Auswahl bleiben gespeichert; die Handymodelle bleiben verfügbar.

Die App setzt 8192 Kontexttokens, 1024 Antworttokens und `think:false`. Sie zählt den vollständigen Gemma-Prompt mit dem aus denselben Gewichten gewonnenen Tokenizer und prüft die vom Server gemeldete Eingabelänge. Unvollständige Antworten und Verbindungsfehler stellen den Entwurf wieder her. Die frühere CLI-Vergleichsdefinition mit 4096/1024 bleibt erhalten. Die Denkphase beantwortete kurze Kontrollfragen teilweise besser, lief mit vollständigen App-Szenen jedoch mehrfach in das Antwortlimit. Auch eine zusätzliche Quellenregel löste den Geschenkfall mit der Archivarin Hedda nicht zuverlässig; die bestehende Kontextplanung bleibt erhalten. Größere Modelle garantieren weiterhin keine richtige Antwort. Geschwindigkeit und das gesamte Verhalten müssen zusätzlich auf dem Handy geprüft werden.

Für Builds siehe [BUILD.md](docs/BUILD.md): JDK 17/21, Android SDK 35, NDK 27.2.12479018, CMake 3.22.1. Der bestehende Signierschlüssel wird nicht veröffentlicht; [signing/README.md](signing/README.md) erklärt eigene Testbuilds. Die veröffentlichten APKs verwenden weiterhin den bisherigen Schlüssel.

## Geprüfter Stand und Grenzen

Für 0.8.8 bestanden 159 reguläre Tests sowie der portable UI-/SQLite-Lauf mit 283 bestandenen Tests und fünf übersprungenen optionalen Modellproben (288 insgesamt; die regulären Tests sind darin enthalten). Eine Prüfung über den tatsächlichen Ollama-HTTP-Pfad mit App-Kontextplanung und SQLite war erfolgreich. Build und Lint bestanden; Lint meldete 0 Fehler und 134 Warnungen. Hauptschema 10 und Suchindexschema 1 bleiben gleich. Details und Grenzen stehen im [Prüfbericht](docs/ollama-app-verification.json) und unter [Builds](docs/BUILD.md).

APK: 327004709 Bytes, SHA-256 `5247b953a2520051a61d60b1b467ac175ee43287ed94d34295fcee577b61d2d3`. Paket, Version, bisherige Signatur und 16-KiB-Ausrichtung wurden geprüft.

Ein physisches S24 war bei diesen Abschlussprüfungen nicht angeschlossen. Installation, Geschwindigkeit, RAM und Wärmeentwicklung sowie der vollständige Updateablauf müssen auf den Geräten geprüft werden. Die Berichte aus 0.8.0 bis 0.8.7 dokumentieren damalige lokale Entwicklungsstände; ihr Hinweis „nicht veröffentlicht“ beschreibt den damaligen Zeitpunkt.

[Versionshinweise](RELEASE-NOTES.md) · [Bibliotheken und Modelllizenzen](THIRD_PARTY_NOTICES.md). Built with Llama.
