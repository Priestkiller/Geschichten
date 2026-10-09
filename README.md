# Geschichten – Android 0.8.7

Eine native Android-Testapp für deutsche Gespräche und Abenteuer mit 90 erwachsenen KI-Figuren. Nach dem Modelldownload entstehen die Antworten lokal auf dem Handy. Mindestvoraussetzung: Android 12 und ARM64. Zielgeräte sind Galaxy S24 und S24 Ultra.

## Testversion herunterladen

**[Release 0.8.7](https://github.com/Priestkiller/Geschichten/releases/tag/v0.8.7)** · [APK herunterladen](https://github.com/Priestkiller/Geschichten/releases/download/v0.8.7/Geschichten-0.8.7.apk)

Eine vorhandene Installation **nicht deinstallieren**. Die APK als Update öffnen. Alternativ unter **Einstellungen → App-Updates** die **Testversionen einbeziehen** einschalten und **Nach Updates suchen** antippen. Versionscode 21, Paket `dev.vincent.geschichten`, bisheriges Testzertifikat. Die App prüft Größe, SHA-256, Version und Signatur. Android bestätigt die Installation.

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

**Ollama ist noch nicht an die Android-App angebunden.** Die bisherigen Servermessungen liefen separat. Ministral 3 14B und Gemma 4 12B sind vorgeschlagene nächste Kandidaten, noch nicht auf diesem Server geprüft.

Für Builds siehe [BUILD.md](docs/BUILD.md): JDK 17/21, Android SDK 35, NDK 27.2.12479018, CMake 3.22.1. Der bestehende Signierschlüssel wird nicht veröffentlicht; [signing/README.md](signing/README.md) erklärt eigene Testbuilds. Die veröffentlichten APKs verwenden weiterhin den bisherigen Schlüssel.

## Geprüfter Stand und Grenzen

Für den unveränderten APK-Stand 0.8.7 bestanden am 8. Oktober 2026: 148 reguläre und 100 relevante Integrationstests (26 Überschneidungen), ein zusätzlicher SQLite-Nachweis, Build und Lint. Lint: 0 Fehler, 133 Warnungen, 1 Information. Hauptschema 10, Suchindexschema 1. Diese Veröffentlichung baut keine andere APK; Version, SHA-256 und Signatur wurden erneut geprüft.

Ein physisches S24 war bei diesen Abschlussprüfungen nicht angeschlossen. Installation, Geschwindigkeit, RAM und Wärmeentwicklung sowie der vollständige Updateablauf müssen auf den Geräten geprüft werden. Die Berichte aus 0.8.0 bis 0.8.7 dokumentieren damalige lokale Entwicklungsstände; ihr Hinweis „nicht veröffentlicht“ beschreibt den damaligen Zeitpunkt.

[Versionshinweise](RELEASE-NOTES.md) · [Bibliotheken und Modelllizenzen](THIRD_PARTY_NOTICES.md). Built with Llama.
