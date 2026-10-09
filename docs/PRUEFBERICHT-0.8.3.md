# Geschichten 0.8.3 – lokaler Teststand

**Ergebnis:** Die Bedeutungssuche verbessert den Abruf und in einer kleinen Matrix einzelne vollständige Antworten. Ein allgemein zuverlässiger Modellwechsel ist nicht nachgewiesen. Die Suche bleibt zunächst ausgeschaltet; Qwen Original ist eine ausdrücklich experimentelle Alternative. Die bestehende Modellauswahl wird nicht automatisch geändert. Nichts wurde veröffentlicht.

## APK und Bedienung

Die [Geschichten-0.8.3.apk](../Geschichten-0.8.3.apk) als Update installieren, die bestehende App vorher nicht deinstallieren. Unter Einstellungen stehen **Bedeutungssuche für Erinnerungen** und **Qwen 3 · 4B Original Instruct 2507** zur Verfügung. Das Suchmodell herunterladen, die Testoption aktivieren und eine Geschichte öffnen. Vorbereitung läuft abschnittsweise im Hintergrund. „Erinnerungen vorbereiten“, „Anhalten“ und „Suchindex dieser Geschichte neu aufbauen“ sind vorhanden. Bei ausgeschalteter Option, fehlenden Vektoren, Zeitlimit oder Fehlern bleibt die Wortsuche aktiv.

APK **321.767.673 Bytes**, Paket `dev.vincent.geschichten`, Version **0.8.3 / 17**, SHA-256 **`c0a4131c0636160b50aa040247660c9d54547bdd0ecaf3c275fb00a4646cc141`**. Signatur SHA-256 **`3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`**, identisch zur bisherigen Signierung. APK-Signatur und 16-KB-Ausrichtung geprüft. Die Updatesuche findet diesen rein lokalen Stand nicht als GitHub-Release.

## Ausgangssicherung und Versuche

Der Anfangsquellstand entsprach 0.8.2. [Baseline](validation/quality-search-0.8.3/baseline.json), Quellkopie, kompilierte Klassen, Modelldateien und Einstellungen wurden gesichert. Die endgültigen 0.8.2-Eingaben und Rohantworten sind unverändert. Bei deren zwölf Antworten stimmen ausgewählte Eingabe, echte Tokenzahl und Filterentscheidung weiterhin überein. Der bekannte Fehler einzelner alter Diagnosefelder wurde berücksichtigt: Verglichen wurde der tatsächlich gewählte Prompt, nicht ein verworfener Budgetversuch.

[20 neue deutsche Szenen](validation/quality-search-0.8.3/new-scenes.json) wurden vor Anpassungen in zehn Entwicklungs- und zehn Abnahmefälle getrennt. Sie definieren erwartete Fakten, unzulässige Behauptungen und erlaubte aktuelle Ergänzungen für neue Namen, Übergaben, Verletzungen, Familie, Besitz/Ablage, Negationen, Hypothesen, Korrekturen, Synonyme, mehrere Quellen und unbekannte Vergangenheit. [Materialisierte Zustände](validation/quality-search-0.8.3/materialized-scenes.json) stammen aus den unveränderten 0.8.2-Regeln; die tatsächliche SQLite-Speicherung wurde unabhängig geprüft.

Matrix: 24 erste technische Entwicklungsantworten (vier Szenen, kurz/lang, drei Modelle), zwei Samplingkontrollen, 30 Produktionsantworten (zehn Abnahmefälle, drei Modelle), acht Hybridantworten (vier gemeinsame Abnahmefälle, zwei Modelle). **64 echte Generierungen über 14 verschiedene Antwortszenen**, keine 64 unabhängigen Geschichten. Erste technische Fälle hatten vereinfachte Momentaufnahmen; die Hauptabnahme verwendet aus Quellen materialisierte Zustände. Gleiche Prompts, insbesondere der Familienfall ohne zusätzlich gefundenen Beleg, sind Wiederholungen. Abnahmefälle wurden nicht zur Feinabstimmung verwendet.

Quellen, Revisionen, Lizenzen, Unterschiede, Hashes und Laufzeitprofile: [MODELLQUELLEN-0.8.3.md](MODELLQUELLEN-0.8.3.md).

## Abrufqualität

Die endgültige Bewertung verlangt sämtliche benötigten **genauen Originalabschnitte** unter maximal vier ausgewählten Stellen. Eine Nachrichten-ID mit nur einer von mehreren notwendigen Aussagen reicht nicht. Bei unbeantwortbarer Vergangenheit ist kein Treffer die korrekte Suchentscheidung. Die frühen Zähler allein nach Nachrichten-ID sind keine Qualitätskennzahlen.

| Fälle | Wortsuche vollständig | Hybrid vollständig | Benötigte Abschnitte Wortsuche / Hybrid | Irrelevante Treffer Wortsuche / Hybrid |
| --- | ---: | ---: | ---: | ---: |
| Entwicklung, 10 | 6 | 8 | 10/14 · 12/14 | 2 · 3 |
| Abnahme, 10 | 3 | 6 | 8/16 · 12/16 | 1 · 2 |

Wasserangst, Gedränge und nussfreie Haferkekse werden bei anderer Wortwahl besser gefunden. Familienzuordnung, einzelne Gründe und eine Ablage mit Pronomen bleiben unvollständig. Beim Entwicklungsfall mit schrillem Alarm liegt ein benötigter Beleg unter der eingefrorenen Schwelle.

Sechs zusätzliche Robustheitsproben erfolgten nach dem Einfrieren ohne Parameteränderung: Rotes und blaues Amulett bleiben getrennte Originale; das blaue ist ein irrelevanter zusätzlicher Treffer. Geheimnisse, Ausschlüsse, fremde Geschichten und wiederholte unbestätigte Modellvergangenheit werden nicht übernommen. Nach einer aktuellen manuellen Korrektur wird nur der passende neue Ort ausgewählt. [Abschnittsbewertung](validation/quality-search-0.8.3/retrieval-assessment.json), [Robustheitsdaten](validation/quality-search-0.8.3/retrieval-robustness-results.json).

## Vollständige Antwortqualität

Fakten, Rollen, Vollständigkeit, Deutsch, Figurenstil, erfundene Vergangenheit, Metakommentare, Fragekopien, Schleifen, Ausweichen und Abbruch wurden separat nach Lesen der gesamten Ausgabe einschließlich Erzählertext bewertet. Filterannahme ist keine Qualitätsbewertung.

Die strenge Zehn-Fälle-Abnahme mit bisheriger Suche ergab **0/10 vollständig brauchbare Antworten je Modell**, obwohl beide Qwen-Fassungen 8/10 und Gemma 9/10 annahmen. Teilweise richtige Eigentümerantworten wurden wegen „in“ statt „auf“ der Kommode nicht vollständig richtig gezählt. Dies bewertet den gesamten geprüften Ablauf, keine allgemeine Modelluntauglichkeit: fehlende oder falsch gespeicherte Informationen tragen bei.

Dieselben vier Fälle (Familie, Wasser, Gedränge, Nussunverträglichkeit), gleiches Profil, Sampling und Budget:

| Textmodell | Bisherige Suche | Hybrid |
| --- | ---: | ---: |
| Huihui Qwen 3 4B | 0/4 brauchbar | **1/4 brauchbar** |
| Qwen 3 Original 4B | 0/4 brauchbar | **2/4 brauchbar** |

Beide beantworten mit Hybrid die nussfreien Haferkekse richtig; Qwen Original gestaltet zusätzlich eine passende Reaktion auf Gedränge. Familie bleibt falsch/unvollständig, Wasser enthält weiterhin unbelegte Erinnerungen oder falsche Beruhigung. Alle acht Hybridantworten wurden angenommen, darunter fünf nicht vollständig brauchbare. Keine Filterlockerung; kein belegter Fehlalarm bei einer vollständig brauchbaren Antwort. Keine Endlosschleife und kein Ausgabelimit-Abbruch in den 64 Läufen. Die zwei offiziellen Samplingkontrollen erzeugten dieselben Antworten wie ihre bisherigen Kontrollen; keine Einstellungsumstellung.

[Getrennte ganze Antwortbewertungen](validation/quality-search-0.8.3/answer-assessment.json), `development-*.json` und `acceptance-*.json` enthalten unveränderte Rohantworten und wirkliche Eingaben. **Alle 64** wurden mit den finalen APK-Klassen und echten Tokenizern erneut gerendert: ausgewählte Eingabe, Format, Tokenzahl und Filterentscheidung stimmen. Die aktuelle Frage steht je Eingabe genau einmal. Nachweise: `final-prompts-*.json`.

## Drei Ebenen und verbleibende Fehler

1. **Speicherung:** Alle 20 Szenen bewahren Originale und speichern dieselben Zustandswerte wie die geprüften Momentaufnahmen. Das bedeutet nicht, dass alle Werte die Erzählwahrheit korrekt abbilden. Bestehende Regeln können „geschenkt“ als Besitz des Schenkenden missverstehen, Verletzungen unvollständig erfassen und einen neuen Ablageort mit „sie“ nicht auflösen. Träger und Ablageort bleiben ein gemeinsames Feld; „In/bei“ verliert die genaue Präposition. Diese Fehler wurden nicht durch Suchtreffer automatisch überschrieben. [Datenbanknachweis](validation/quality-search-0.8.3/database-scenes-audit.json).
2. **Bereitstellung:** Pflichtzustände kommen tatsächlich an, teilweise bereits unvollständig/veraltet wegen Ebene 1. Produktions-SQLite und Rangfolge mit echten aufgezeichneten Native-Vektoren reproduzieren die Quellenauswahl aller 20 Szenen. Der endgültige Tokenplaner übernimmt passende ganze Originalabschnitte im echten Budget. `requiredSourceIdsActuallyRecalled` ist nur eine Diagnose; maßgeblich sind die genauen Stellen und Prompttexte.
3. **Verwendung:** Modelle verwechseln Perspektiven, lassen Fragen offen, erfinden Vergangenheit oder kopieren Fragen. Manche Fehler werden angenommen; das betrifft auch korrekt vorhandene Informationen. Bedeutungssuche allein löst diese Ebene nicht.

## Architektur und Aufwand

`EmbeddingModel.kt`/`embedding-jni.cpp`: echte lokale Inferenz in der bestehenden Laufzeit. `SemanticSearch.kt`: expliziter Download, Prüfung, ein Worker, Abbruch, Wortsuche-Rückfall. `SemanticIndex.kt`: separate erneuerbare SQLite-Datei im No-Backup-Verzeichnis, Schema 1. **Geschichten-Datenbank bleibt Schema 9**, kompilierte Datenbankklasse bytegleich zu 0.8.2. Kein externer Server und keine zusätzliche Auswertungs-KI.

Einträge enthalten Geschichte, Nachricht, Grenzen, Inhalts-/Quellenrevisionshash, Modell/Vorverarbeitung, Dimension, Index-/Zustandsversion, Status und Vektor. Unveränderte Vektoren werden wiederverwendet. Quellenregeln greifen vor Ranking und erneut vor Promptübernahme; aktuelle Korrekturen, Geheimnisse, Ausschlüsse, Löschungen und Geschichte gelten unabhängig von späterer Indexbereinigung. Treffer erzeugen keine Weltfakten. Bestehende atomare Transaktionen und Figurenwissensregeln bleiben erhalten. Automatische experimentelle Namensdarstellung bleibt aus.

Höchstens 16 Abschnitte pro Operation, Freigabe zwischen Batches, zwei Threads, 512 Tokens. Suchmodell nach Operation entladen. Suche: 1,5-Sekunden-Rückfallrahmen mit kooperativer Prüfung und separatem Native-Abbruch. Indexdekodierung wird nach sechs Sekunden angehalten. Modelllade-/Dateizugriffe sind nicht überall sofort abbrechbar; harte maximale Wartezeit und Speicherdruck müssen auf dem Handy geprüft werden. App-Start indexiert nicht alle Chats; nur die geöffnete Geschichte nach ausdrücklicher Aktivierung.

**Desktop-CPU, teils parallel zu Texttests, warme Dateicaches:** anfängliches Laden etwa 265 ms; mediane Query-Inferenz 32 ms (Entwicklung) / 171 ms (Abnahme). Kumulierte Erstindexierung 9,64 s / 34,46 s, 195 / 182 neue Dokumentvektoren. Numerische Nutzdaten der Story-Sätze zusammen etwa 1,98 MB; SQLite-Seiten/Metadaten zusätzlich. Download 333,59 MB; Native-Protokoll etwa 311,91 MiB gemappte Gewichte, 1 MiB Ausgabe, 12,26 MiB Rechenpuffer. Keine garantierten Android-RAM-/Zeitwerte.

Alle **397 ursprünglichen** eindeutigen Eingaben erzeugen mit finaler Implementierung bitgleiche Vektoren; weitere 13 Robustheitseingaben wurden mit derselben Implementierung berechnet. [Native-Nachweis](validation/quality-search-0.8.3/vectors-final-verification.json). Einheitsvektoren in einzelnen Unit-Tests prüfen nur Programmlogik, nicht Suchqualität.

## Validierung und Reproduktion

**130 normale Tests**, **72 gezielte Integrations-/Bedienungstests**, keine Fehler; 14 Rollentests überlappen, keine Addition zu 202 unabhängigen Tests. Build/Lint erfolgreich: **0 Fehler, 131 Warnungen, 1 Information**, einschließlich bestehender Warnungen. APK enthält die neue Schnittstelle, keine Prüfhilfen und keine neuen Gewichte. Vorhandene tokenizer-only-GGUF-Assets, Bilder und andere native Bibliotheken bleiben erhalten. Alle bisherigen Figuren und langen Einführungen unverändert. [Dateiänderungen und Lieferprüfsummen](validation/quality-search-0.8.3/delivery-verification.json).

Aus Projektordner, nachdem `../Build-Umgebung.ps1` geladen wurde:

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug -PupdateRepository=Priestkiller/Geschichten -PupdateIncludePrerelease=true --no-daemon --max-workers=2 '-Pkotlin.compiler.execution.strategy=in-process'
./docs/validation/quality-search-0.8.3/build-native.ps1
./docs/validation/quality-search-0.8.3/verify-final-inputs.ps1
./docs/validation/quality-search-0.8.3/verify-regressions.ps1
```

`prepare.py` sicherte den ursprünglichen Stand und verweigert Überschreiben; im veränderten Projekt keine neue Baseline darüber erzeugen. `download.py`, `materialize.ps1`, `embedding-pilot.py`, `retrieval.py`, `run-quality.ps1`, `run-acceptance.ps1`, `assess-retrieval.py`, `assess-answers.py` dokumentieren Download, Eingaben und Experimente. Antwortskripte überschreiben keine vorhandenen Rohdaten; Wiederholung in getrenntem Ergebnisordner. Die gezielten Gradle-Befehle stehen vollständig in `integration-final-check.log` und im Arbeitsablauf; ausgewählte Tests sind in den XML-Verzeichnissen nachvollziehbar.

Eine frühe Wiederholung alter Tests schrieb zwei **Diagnosedateien** erneut in den 0.8.2-Ordner (`contamination-after.json`, `database-long-run.json`). Das war ein Ablagefehler; sie schreiben nun in den neuen Ordner. Vorher-/Nachher-Hashes sind offengelegt, frühe neue Ausgaben separat erhalten. Endgültige 0.8.2-Eingaben, Rohantworten und A/B/C-Inferenzdaten blieben unverändert.

## Empfehlung und offene Geräteprüfungen

Freiwillig Bedeutungssuche und Qwen Original ausprobieren, besonders bei umschriebenen älteren Fakten. Keine automatische Standardaktivierung oder allgemeine Qualitätszusage. Die dokumentierten Speicher-/Rollenfehler bleiben ein weiterer Schwerpunkt; größeres Kontextfenster oder weniger Quantisierung sind nicht als Lösung belegt.

**Kein ADB-Gerät angeschlossen.** Für S24/S24 Ultra offen: tatsächlich wirksame Einstellungen, gemeinsamer Spitzen-RAM, kalter/wiederholter Start, erste/vollständige Antwortzeit, lange echte Chats, Hintergrund/Vordergrund, Native-Abbruch, Prozessneustart, Geschichtenwechsel und Korrekturen unter Gerätebedingungen, Offline-Betrieb nach Downloads. Desktop-/Robolectric-Ergebnisse sind dafür kein Ersatz.
