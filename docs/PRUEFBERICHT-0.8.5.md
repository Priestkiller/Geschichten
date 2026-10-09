# Geschichten 0.8.5: Originalabruf und begrenzte Faktenantworten

Stand: 8. Oktober 2026. Ausgangspunkt: tatsächlich vorhandener Quellstand 0.8.4, Datenbankversion 10 und [Prüfbericht 0.8.4](PRUEFBERICHT-0.8.4.md). Alle Angaben beziehen sich auf diese begrenzte Untersuchung. Lokale, kompatibel signierte Test-APK: `Geschichten-0.8.5.apk`, Versionscode 19.

## Ergebnis

Der gezielte Abruffix verbessert den geprüften Vergleich: **1/6 brauchbare ganze Antworten mit bisheriger Wortsuche, 1/6 mit bisheriger Bedeutungssuche, 3/6 mit Bedeutungssuche und geprüftem Originalkontext**. Benötigte Originalsätze kommen zuletzt in **6/6 Fällen vollständig** in der tatsächlichen Modelleingabe an. Es bleibt ein kleiner synthetischer Vergleich mit einem Textmodell, keine allgemeine Erfolgsquote.

Die neue Testoption **Einstellungen → Kurze Faktenantworten** liefert fünf richtige kurze Antworten auf fünf neue eindeutige Fragen. Die freie KI beantwortet dieselben fünf einfachen Fragen ebenfalls richtig. Ein Genauigkeitsvorteil dieser Option ist damit nicht nachgewiesen; sie bleibt **standardmäßig ausgeschaltet**. Die Antworten sind nüchtern und verwenden die richtige Figurenperspektive. Eine reichhaltige individuelle Stimme wurde durch diese kurzen Sätze nicht nachgewiesen.

Eine echte Fortsetzung nach gespeicherter Modellantwort scheitert weiterhin an einer Rollenverwechslung. Der Filter verhindert ihre Speicherung, liefert aber keine brauchbare Ersatzantwort. Ruths Verwandtschaft wird trotz vollständigem Originalkontext vertauscht. Die generative Antwortqualität ist also weiter **nicht zuverlässig repariert**.

## Unveränderter Ausgangspunkt und faire Vergleiche

Vor Anpassungen wurden Quellen, kompilierte Ausgangsklassen und 0.8.4-Prüfarbelege gesichert: [742 Dateihashes](validation/answers-0.8.5/baseline-hashes.json), `baseline-0.8.4/`. Die 142 darin erfassten alten 0.8.4-Artefakte blieben bytegleich. Alte Faktenreparatur, primäre SQLite-Struktur, Wissen, Ausschlüsse und Provenienz wurden nicht ersetzt.

Sechs gemeinsame Szenen wurden [vor Änderungen eingefroren](validation/answers-0.8.5/comparison-frozen.json): Ruth/Verwandtschaft, Übergabe ohne belegten Grund, eindeutige Übergabe mit Verletzungszuordnung, aktuelle Ablage, paraphrasierte Wasserangst und eine ausdrücklich begründete Briefübergabe. Die Auswahl wurde vor Produktionsänderungen um Wasserangst und eine neue vollständige Kausalquelle ergänzt; dies steht im [eingefrorenen Split](validation/answers-0.8.5/split-frozen.json).

Alle drei gültigen Antwortbedingungen verwenden **dieselben automatisch erzeugten SQLite-Snapshots**, einschließlich IDs und Zeitangaben. Eine unabhängige erneute Speicherung bestätigt denselben Figurenstand. Keine manuell eingesetzten Sollwerte im Produktionsablauf.

Textmodell: vorhandenes **Qwen3-4B-Instruct-2507 Q4_K_M**, CPU, vier Threads. Seed 42; Temperatur 0,75; Top-p 0,9; Top-k 40; Wiederholungsstrafe 1,08 über 256 Token. Kontext 4096, Antwortreserve und maximale Ausgabe jeweils 512. Gesprächsvorlage und Tokenizer stammen aus der echten nativen Laufzeit. Parameter und Modelle wurden innerhalb der Vergleiche nicht verändert. Die gemeinsame App-Logik gilt für die bestehenden Modellwege; ein neuer generativer Vergleich aller sieben Modelle fand hier nicht statt.

## Suche: Wo der Originalbeleg verloren ging

Beim Ruth-Fall ist „Meine Schwester heißt Ruth.“ ein eigener Abschnitt vor „Ruth hat dir den Kristall geschenkt.“. Wortsuche und semantischer Rang finden den Geschenksatz, aber nicht die benötigte Familienzuordnung. Bei Wasserangst findet die semantische Suche „Lorenz fühlt sich auf Schiffen wohl.“, während Jettes Nichtschwimmen und Wasserangst unmittelbar davor stehen.

In den vollständig vorbereiteten Tests waren die Originale gespeichert, zulässig, als Kandidaten vorhanden und indexiert. Die nötigen zusätzlichen Sätze scheiterten am Trefferzuschnitt beziehungsweise an der Rangfolge/Schwelle als selbständige Abschnitte. Der Tokenplan verwarf diese beiden Sätze zuvor nicht: Sie wurden ihm gar nicht angeboten.

`ArchiveRecall.withContext` ergänzt einen Treffer um höchstens zwei vorherige und einen folgenden Abschnitt **derselben Originalnachricht**, insgesamt höchstens 1200 Zeichen. Der zusammengesetzte Originalausschnitt durchläuft erneut die vorhandene Freigabeprüfung. Widerspricht ein Nachbarsatz dem aktuellen Stand, bleibt der ursprüngliche Treffer erhalten. Es gibt keine neue Quellenberechtigung durch Ähnlichkeit, keine Zusammenfassung als Beweis und keine Erweiterung auf andere Geschichten. `MemoryPrompt` zählt die vergrößerte tatsächliche Eingabe mit dem gewählten Runtime-Tokenizer.

| Gültiger Vergleich | Ganze Fälle mit allen nötigen Originalsätzen | Nötige Sätze tatsächlich bereitgestellt | Brauchbare ganze Antworten |
| --- | ---: | ---: | ---: |
| A: Reparierter Stand, Wortsuche | 2/6 | 5/9 | 1/6 |
| B: Derselbe Stand, bisherige Bedeutungssuche | 3/6 | 6/9 | 1/6 |
| C: Derselbe Stand, Bedeutungssuche + geprüfter Kontext | 6/6 | 9/9 | 3/6 |

Aktuelle Zustände wurden daneben verpflichtend bereitgestellt. Insbesondere war die korrekte Schrankablage schon in A verfügbar, obwohl ihr älterer Originalsatz nicht zusätzlich abgerufen wurde. Fehlender Originalabruf bedeutet deshalb nicht automatisch fehlenden aktuellen Stand.

Echte gespeicherte EmbeddingGemma-Vektoren wurden über Produktions-SQLite, Freigaben und unveränderte Rangfolge ausgewertet. Zusätzlich lief **der tatsächliche aktivierte `SemanticSearch`-Worker mit nativer Inferenz**, vorhandener überprüfter Modelldatei, Batch-Indexierung und dem produktiven Suchzeitlimit: alle sechs Geschichten vollständig indexiert, null Rückfälle auf Wortsuche. Seine gewählten Abschnitte stimmen mit dem getrennten Vektorreplay überein. [Nativer Ablauf](validation/answers-0.8.5/native-semantic-flow.json), [Index/Kandidaten/Rangfolge](validation/answers-0.8.5/final-retrieval-trace-corrected-frozen.json), [tatsächliche Antwortprompts](validation/answers-0.8.5/raw-corrected-hybrid-qwen3-official-4b.json).

Hostwerte: zwei bis drei Indexbatches je Geschichte, rund 7–11 Sekunden vollständige Vorbereitung; Suchen rund 602–916 ms. Das sind PC-Messwerte unter zusätzlichen CPU-Prüfläufen, **keine S24-Zeiten oder S24-RAM-Messungen**. Auf einem langsameren Handy können fehlende Vektoren oder das Zeitlimit weiterhin den vorgesehenen Rückfall auslösen.

Ein Fehler im Vergleichstest fragte den Index vorübergehend mit der ID einer neu erzeugten Geschichte statt der eingefrorenen Ausgangsgeschichte ab. Die sechs Antworten in `raw-final-hybrid-*.json` stammen deshalb tatsächlich aus Wortsuche mit Kontext und sind **aus dem gültigen semantischen Vergleich ausgeschlossen**. Sie bleiben unverändert erhalten. `AnswerPipelineTest` verwendet inzwischen die richtige ID und verlangt echte semantische Treffer; der korrigierte Durchlauf liegt separat unter `raw-corrected-hybrid-*.json`. Der Fehler lag im Test, nicht im produktiven `SemanticSearch`.

## Tatsächliche Antworten und verbleibende Modellfehler

**Wasserangst, vorher B:**

> „Ich hab dich schon beim ersten Mal auf dem Boot gesehen – du bist ganz ruhig, obwohl es schwankt. …“

Die Figur nennt ihre eigene Angst nicht und erfindet ein früheres gemeinsames Bootserlebnis.

**Nachher C:**

> „Ich habe Angst vor tiefem Wasser und kann nicht schwimmen“, sagt sie leise. „Aber ich verstehe, dass du dich auf einem schwankenden Boot wohlfühlst …“

Hier sind Rollen und Reaktion passend. Die volle Antwort steht unverändert im Rohartefakt.

**Verletzungsfrage, vorher:**

> „Du hast mir das Amulett gegeben. Mein linkes Bein ist unverletzt.“

Die gesuchte Verletzung des Spielerbeins fehlt.

**Nachher:**

> „Du hast mir das Amulett gegeben. Meins ist unverletzt, dein linkes Bein ist verletzt.“

**Ruth, nachher weiterhin falsch:**

> „Der Kristall ist von Ruth, meiner Schwester, geschenkt worden. Sie hat ihn dir gegeben.“

Tatsächlich ist Ruth die Schwester des Spielers Silas; sie gab den Kristall der Figur Enna. Die vollständigen Originalsätze sind jetzt vorhanden, das Modell vertauscht aber Verwandtschaft und Empfänger. Der bestehende Filter übersieht diese Formulierung.

Der belegte Briefgrund kommt vollständig an. Das Modell nennt ihn zunächst richtig und sagt später zugleich „Ich weiß nicht, wer ihn jetzt hält“ und „ich halte ihn jetzt“. Ein richtiger Kern macht diese widersprüchliche Gesamtantwort nicht brauchbar. Beim anderen Buchfall ist nur Verletzung plus Übergabe belegt, kein ursächlicher Zusammenhang; dennoch werden Motive und Eigentum erfunden.

Alle sechs C-Antworten wurden vom Filter angenommen, aber nur drei als insgesamt brauchbar bewertet. Drei schlechte Gesamtantworten wurden übersehen. Es gab im neuen bewerteten Korpus keine versehentliche Zurückweisung einer brauchbaren Antwort; das ist kein allgemeiner Nachweis eines fehlerfreien Filters. [Einzelbewertungen mit Antwort- und Prompthashes](validation/answers-0.8.5/whole-answer-assessment.json), [getrennte Zählungen](validation/answers-0.8.5/answer-summary.json).

## Begrenzte Faktenoption: Erfassung, Speicherung, tatsächlicher Eingang, Antwort

Vor ihrer Implementierung wurden zwei Entwicklungsfälle und **18 getrennte neue deutsche Abnahmefälle** festgelegt: fünf positive Fragen und 13 ähnliche negative beziehungsweise mehrdeutige Fälle. Der Resolver blieb seit seinem ersten Abnahmelauf bytegleich. [Originale/Sollantworten](validation/answers-0.8.5/new-cases-frozen.json), [Einfrierung](validation/answers-0.8.5/facts-candidate-hash.json).

1. **Erfassung:** normale `MemoryRules`-Auswertung, ohne Sollwerte in den Fakten zu setzen. Die benötigten fünf positiven Zustände werden erkannt. Nicht jede freie deutsche Aussage wird dadurch allgemein verstanden; bei der Kausalquelle bleibt der Grund im Original erhalten und wird nicht als neuer aktueller Verletzungszustand behauptet.
2. **Speicherung:** echte SQLite-Commits, Wiederöffnung und getrennte Kontrolle der gespeicherten Zustände; [Datenbankprüfung](validation/answers-0.8.5/new-database-audit-frozen.json). Eigentum, Tragen und Ablage bleiben unterschiedliche Felder.
3. **Bereitstellung:** der direkte Antwortweg erhält ausschließlich konkrete bestätigte `CURRENT`-Fakten aus `forCharacter`, samt Quellen, IDs und Zustandsversion. Historisches Restwissen bei heimlicher Änderung, Ausschlüsse, widerrufenes Wissen und falsche Geschichten werden nicht als aktuelle Wahrheit verwendet. [Vollständige tatsächliche Resolver-Eingaben](validation/answers-0.8.5/raw-facts-final.json). Er ruft kein Textmodell auf; es gibt dabei keinen angeblichen Modellprompt. Für freie Antworten sind Systemtext, Historie und tatsächlich formatierte native Eingabe im Rohartefakt erhalten.
4. **Beantwortung:** fünf richtige kurze Sätze bei fünf positiven Abnahmefragen, null Fehlaktivierungen bei 13 Gegenproben. Beispielsweise „Der Ring gehört dir.“ trotz Tragen durch die Figur und „Meine linke Schulter ist gebrochen.“. Die fünf freien Modellantworten sind hier ebenfalls sachlich richtig und verständlich. Keine Behauptung eines nachgewiesenen Genauigkeitsgewinns.
5. **Figurenwirkung:** passende Ich/Du-Perspektive und verständliches Deutsch, aber nüchterner kurzer Stil. Starke individuelle Persönlichkeit, Humor und eine besondere Figurenstimme wurden nicht gezeigt. Die freie Zielantwort verwendet zudem verschachtelte gleichartige Anführungszeichen; die direkte Antwort benutzt innen Guillemets.
6. **Zuordnung:** unbekannte Fakten, zwei gleichartige Gegenstände, warum ohne Quelle, Fragen zu Schenkern mit nur Eigentümerbeleg, Vergangenheit, Gefühle, Hypothesen, neue Handlungen und zusätzliche Schreibaufträge fallen in den normalen Weg. Ein Rückfall ist keine automatisch richtige Antwort; dessen Modellfehler bleiben möglich.

Es gibt keine Antworten für fest benannte Testfiguren, keine eingesetzten Goldfelder, keine sprachliche Modellumformulierung und keine Reparaturschleife. Der Faktenweg verwendet denselben abschließenden Antwortfilter und atomaren, versionsgebundenen Commit. Die Einstellung ist dauerhaft, standardmäßig aus und ändert die Modellwahl nicht. Bestehende Chats bleiben erhalten.

## Fortlaufende Gespräche

Der direkte Weg wurde über mehrere echte Datenbankrunden geprüft: Ablage, Aufnahme, erneute Ablage, manuelle Eigentümerkorrektur, andere Geschichte und unbeantwortete/entfernte Nutzernachricht. Antworten folgen jeweils dem gültigen Zustand; Korrektur und Originalverlauf bleiben erhalten. [Fortlaufende Prüfung](validation/answers-0.8.5/ongoing-conversation-frozen.json). Ein ViewModel-Test prüft den tatsächlichen Sendeablauf, Umschalten, generativen Rückfall und Neustart mit unveränderter Nutzer-Modellwahl.

Zusätzlich wurde eine **wirkliche native Modellantwort** gespeichert und darauf eine neue Nutzerangabe verarbeitet:

> Nutzer: „Ich halte den Dolch jetzt. Der Dolch gehört dir. Wem gehört der Dolch und wer hält ihn jetzt?“

Die vorläufige Eingabe enthält korrekt Träger Spieler/Bennet und Eigentümer Figur/Liora. Das Modell antwortet:

> „Der Dolch gehört mir. Ich halte ihn jetzt.“

Eigentum stimmt, Träger ist falsch. Die Antwort wird blockiert. Die atomare Speicherprüfung weist nach, dass sie nicht als Figurenantwort oder neuer Zustand übernommen wurde und vorhandene Daten sowie die Original-Nutzernachricht erhalten blieben. **Keine gelungene Fortsetzung und keine Ersatzantwort.** Die vorbereitete weitere Runde wurde nach dieser Blockade nicht künstlich fortgeführt. [Unveränderte Antwort und wirklicher Prompt](validation/answers-0.8.5/raw-conversation-1-qwen3-official-4b.json), [Rollbackprüfung](validation/answers-0.8.5/conversation-blocked-database.json).

Gemischte Nutzerhandlungen mit Frage werden bewusst nicht in starre Faktenantworten verwandelt. Der neue Testmodus behebt dieses Beispiel deshalb nicht automatisch. Die bestehende UI-Fehler-/Abbruchwiederherstellung und der Erhalt schon gespeicherter manueller Korrekturen sind zusätzlich durch die alten Integrationstests abgedeckt.

## Änderungen, Prüfungen und Lieferung

Produktionsdateien:

- `memory/ArchiveRecall.kt`: begrenzter, erneut freigegebener Originalkontext.
- `memory/MemoryPrompt.kt`: Kontext vor tatsächlicher Tokenplanung und Duplikatvermeidung.
- `memory/FactAnswer.kt`: konservative Fragezuordnung und kurze belegte aktuelle Antworten.
- `AppViewModel.kt`: optionaler direkter Weg, unveränderte Filter/Commit-Sicherung und gespeicherter Schalter.
- `ui/UiState.kt`, `ui/SetupScreen.kt`: sichtbare, standardmäßig deaktivierte Testoption.
- `app/build.gradle.kts`, `BUILD_WINDOWS.cmd`, `README.md`: lokaler Stand 0.8.5/code19 und Dokumentation.

Neue Tests prüfen Zuordnung, Quellenfreigabe, Provenienz, echtes SQLite, laufende Gespräche, UI, echten nativen Suchworker und tatsächlichen Sendeablauf. Bestehende Diagnosewriter wurden auf den neuen Artefaktordner umgeleitet, damit 0.8.4-Belege nicht überschrieben werden. [Geänderte Dateien](validation/answers-0.8.5/changed-files.json).

- **140 reguläre Tests bestanden**, null Fehler/übersprungene Tests: `normal-results/`, `build-final.log`.
- **87 relevante Integrationstests bestanden**: `integration-results/`, `integration-final.log`. 18 Tests überschneiden sich mit dem regulären Lauf; die Zahlen werden nicht als 227 unabhängige Tests addiert.
- Der danach korrigierte Abrufharness wurde nochmals separat erfolgreich geprüft: `corrected-pipeline.log`. Weitere gezielte reale Such-/Speicherprüfungen: `native-semantic-flow.log`, `conversation-1-storage.log`, `conversation-blocked-storage.log`.
- Der erste neue ViewModel-Test sendete während des noch laufenden Öffnens des Chats und lief in sein Zeitlimit. Der Test wartet inzwischen auf die Annahme des Sendeversuchs; `flow-recheck.log` und der vollständige Integrationslauf bestanden. Keine Änderung der Produktionslogik für diesen Testfehler.
- Build/Assemble und Lint bestanden: **0 Fehler, 132 Warnungen, 1 Information**. Gegenüber 0.8.4 ist eine zusätzliche `UseKtx`-Stilwarnung für das geprüfte synchrone Speichern des Schalters vorhanden; sie hat keine neue Funktionsfehlermeldung ausgelöst.
- Die tatsächlichen nativen Eingaben sämtlicher 30 gespeicherter generativer Antworten sind erhalten und stimmen mit den geplanten Tokenzahlen überein; Eingabe plus 512 Reserve bleibt innerhalb 4096. Darunter 18 gültige A/B/C-Vergleichsantworten, fünf neue freie Antworten, eine echte Fortsetzung und sechs als Vergleich ausgeschlossene Diagnoseantworten. Keine 30 unabhängigen Abnahmefälle.
- APK-Paket `dev.vincent.geschichten`, Version 0.8.5/code19, dieselbe v2-Signatur wie 0.8.4, erfolgreiche 16-KB-Ausrichtungsprüfung. Alle elf bisherigen Modell-Assets und nativen Bibliotheksdateien der APK sind bytegleich. Keine zusätzlichen Modellgewichte oder Prüffälle in der APK.
- Primäre Datenbank bleibt **Schema 10**, separater Suchindex **Schema 1**. Signierschlüssel, Katalog, bisherige Modellwahl und Modelldateien bleiben erhalten. Kein Training, keine Cloud-Inferenz und keine Veröffentlichung.

APK-SHA-256: `9a810d85006fcc468e541bed2e4cbc139fcdb3ecbf148d1585c9af9db3ec0860`.

Signierer-SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`.

[Lieferprüfung](validation/answers-0.8.5/delivery-verification.json), `apksigner.txt`, `zipalign.txt`, `apk-badging.txt`. Vorhandene App als Update installieren; eine Deinstallation würde die lokalen Appdaten entfernen.

## Offene Geräteprüfung

`adb devices -l` fand kein angeschlossenes Gerät. Tatsächliche Nutzer-Modellwahl, aktivierte Suche, RAM, Wartezeit, längerer Betrieb, Neustart und Offline-Verhalten auf S24/S24 Ultra sind daher **offen**. PC-Ausführungen und Robolectric ersetzen diese Prüfung nicht. Die lokale APK wird nicht automatisch auf GitHub veröffentlicht und erscheint deshalb nicht als neuer öffentlicher Download in der Updatesuche.
