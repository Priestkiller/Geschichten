# Geschichten 0.8.6: begrenzter lokaler Modell-Team-Versuch

Stand: 08.10.2026. Lokaler Teststand, Versionscode **20**, Hauptdatenbank **10**, separater Suchindex **1**. Keine Veröffentlichung, Cloud-Inferenz, neuen Modelle, Trainingsläufe oder Adapter.

## Ergebnis und Entscheidung

Der abschaltbare Ablauf ist eingebaut, aber **kein zuverlässiger Qualitätsgewinn nachgewiesen**. In der gemeinsamen Auswahl ergeben sich für den korrigierten bisherigen Ablauf A, zusätzliche Prüfung B und vollständiges Team C jeweils **2/6 brauchbare ganze Antworten**. Die Zusatzaufrufe beheben Ruths falsche Familienzuordnung und den Selbstwiderspruch beim Brief nicht. Reparaturen werden teilweise blockiert, ohne einen brauchbaren Ersatz zu liefern. Das ist kein Erfolg.

**„Zusätzliche Fakten-KI“ bleibt standardmäßig ausgeschaltet.** „Kurze Faktenantworten“ bleibt unabhängig ebenfalls standardmäßig aus. Der Nutzer kann den Versuch ausdrücklich einschalten und Helfer und Erzähler getrennt auswählen. Ein gleicher Datei-Hash wird als Selbstprüfung angezeigt; die bisherige Erzählmodellauswahl wird nicht geändert.

Zwei tatsächlich fortgesetzte Dialogrunden mit manueller Eigentümerkorrektur enden sachlich richtig. Die Helferinterpretationen und Beanstandungen sind dabei jedoch überwiegend falsch oder unbelegt. Dieser Verlauf beweist die Speicherung und Verwendung der Korrektur, keinen Qualitätsgewinn durch den Helfer. Die Erzählweise bleibt schematisch.

Alle Eingaben sind synthetisch. Die Rohdaten liegen unter [validation/team-0.8.6](validation/team-0.8.6/). Sie wurden nach der Generierung nicht sprachlich korrigiert. Sollangaben und unabhängige Bewertungen werden dem Modell nicht als Antworten vorgegeben.

## Gesicherter Ausgangspunkt und Vergleichsgrenzen

Der tatsächliche Quellstand war 0.8.5/code19. Der Bericht [0.8.5](PRUEFBERICHT-0.8.5.md), die gültigen korrigierten Antworten und die ausgeschlossenen Diagnosen wurden geprüft. `baseline-0.8.5/` enthält eine Kopie von Quellen, Prüfartefakten und kompilierten Klassen; `baseline-hashes.json` enthält 746 ursprüngliche Dateiprüfsummen. Die Originalartefakte unter `validation/answers-0.8.5/` bleiben unverändert.

Für Buch, Ruth und Brief stammen Ausgangszustand und Suchquellen aus den gültigen korrigierten 0.8.5-Vergleichen. Die drei Antworten von A sind bytegleich mit `raw-corrected-hybrid-qwen3-official-4b.json`. Die wegen falscher Geschichten-ID ausgeschlossenen `raw-final-hybrid-*.json` werden nicht als semantische Ergebnisse verwendet.

Der Dolchfall wird aus Originalnachrichten in echter SQLite vollständig nachausgewertet: Der ältere Diagnoseschnappschuss hatte die Spieleridentität nicht vollständig verarbeitet. Jetzt ist Bennet im Zustand vorhanden. Zwei neue Fälle werden ebenfalls in SQLite materialisiert. **Alle vier Vergleichsbedingungen verwenden je Fall denselben korrigierten Zustand und dieselben Quellen.** Die 2/6 dieses Versuchs sind wegen der anderen Fallauswahl nicht mit den 3/6 der gesamten früheren 0.8.5-Auswahl gleichzusetzen.

Die alten drei Fälle verwenden die echten zuvor gewonnenen semantischen Treffer samt erneut geprüftem Nachbarkontext. Dolch und neue Fälle verwenden eingefrorene Wortsuche als Rückfall. Die native Bedeutungssuche wird in diesen neuen Modellpaaren nicht zusätzlich behauptet. Ihre produktive Integration und die bestehenden Suchtests bleiben erhalten; der echte native Suchnachweis aus 0.8.5 wird nicht als neuer Smartphone-Test ausgegeben.

Entwicklung und neue Fälle wurden vor der jeweiligen Inferenz festgelegt (`split-frozen.json`, `phase-a-complete-frozen.json`, `phase-b-scenes-frozen.json`). Die zwei neuen Fälle sind nur eine kleine zusätzliche Abnahmeauswahl; Phase B verwendet sie nach dem Helfertest erneut. Das ist kein unabhängiger großer Blindtest. Es wurde anschließend keine weitere Modell-/Promptmatrix auf diese Fälle optimiert.

## Eingebauter Ablauf

1. Die neue Nutzernachricht wird im bestehenden Ablauf vorläufig ausgewertet. Geschichte, Anfrage, Gedächtnisrevision, Spielerfigur, antwortende Figur und zulässige Originalstellen bilden einen festen Arbeitsstand.
2. Die Fakten-KI erhält einen eigenen sachlichen Auftrag. Sie darf maximal vier Vorschläge mit Quellenalias und wortgetreuem Ausschnitt liefern. Jeder Alias ist an die vorhandene Nachrichten-ID, Originalrevision und Abschnittsgrenzen gebunden.
3. Format und Bindung werden geprüft. Beziehungen, Übergaben und Verletzungen müssen zusätzlich unabhängig im Original erkannt werden. Zustandsvorschläge müssen zu einem bereits originalgebundenen bestätigten Zustand passen. Eine Frage, eine vorhandene Quellen-ID oder gültiges JSON allein genügen nicht. Nicht belegbare Interpretationen bleiben unsicher.
4. Nur unabhängig bestätigte kompakte Originalzuordnungen ergänzen die normale Erzähleingabe. Persönlichkeit, jüngste Gesprächsrunden, aktuelle Zustände und Originalbelege bleiben vorhanden. Freie Helferprosa und seine Unsicherheitslisten werden nicht als Erinnerungen weitergegeben.
5. Die vollständige Figurenantwort einschließlich Erzählertext wird separat geprüft. Beanstandungen benötigen einen tatsächlichen Antwortausschnitt und einen passenden Original-/Zustandsbeleg. Ein „clear“ ist kein Wahrheitszertifikat. Unbelegbare Beanstandungen werden als unsicher behandelt.
6. Der vorhandene deterministische Schutzfilter bleibt wirksam. Bei bestätigtem Fehler ist genau eine erneute Figurenantwort mit belegtem Reparaturauftrag und genau eine erneute Prüfung erlaubt. Bestehende bestätigte Konflikte können nicht durch eine Helferfreigabe aufgehoben werden. Einige unabhängig nachprüfbare Familien-, Geheimnis-, Motiv- und interne Konflikte werden nach der Reparatur nochmals kontrolliert.
7. Erst die finale zulässige Antwort und erneut geprüfte Zusatzvorschläge gehen in die bestehende kurze versionsgebundene SQLite-Transaktion. Die Zusatzvorschläge sind Originalereignis-Notizen im vorhandenen `EVENT`-Typ, keine neue Wissensdatenbank oder automatisch voll verstandene Familienstruktur.

Normal C: **3 Inferenzaufrufe**; mit Reparatur höchstens **5**. B lässt die vorausgehende Auswertung weg und benötigt 2 beziehungsweise höchstens 4. Keine JSON-Neuversuche oder weiteren versteckten Schleifen. Die direkte Faktenoption umgeht bei ihren wenigen geeigneten Fragen die Textmodelle; ihre Ergebnisse zählen nicht zur Teamqualität.

Im Team-Modus werden Token von Entwürfen nicht im Chat angezeigt. Es erscheinen kurze Statusmeldungen; interne JSON-Ergebnisse bleiben außerhalb des Gesprächs. Technischer Ausfall eines Helferschritts wird ausdrücklich gemeldet. Ein bereits bestätigter Fehler darf dabei nicht still freigegeben werden. Scheitert die Prüfung der Reparatur technisch oder inhaltlich, bleibt die Nutzereingabe erhalten.

## Wissen, Korrekturen, Abbruch und Ressourcen

Geheime oder ausgeschlossene Quellen werden nicht als Faktenhelferbelege zugelassen. Ein Geheimnis wird weder als kompakter Hinweis noch im Reparaturauftrag offengelegt. Auch manuell entzogener Wissensstand wird geprüft. Nachrichten und Antworttexte sind zu untersuchende Daten; Hilfsausgaben enthalten keine ausführbaren Werkzeuge oder SQL-Befehle.

Gespeicherte manuelle Korrekturen bleiben auch bei fehlgeschlagener Generierung erhalten. Vor dem Abschluss werden Geschichte, Anfrage, aktuelle Revision, Originalrevision und Berechtigung erneut geprüft. Späte Ergebnisse nach Korrektur, Abbruch oder Geschichtenwechsel können nicht committen. Zusatznotizen sind zunächst vorläufig und werden idempotent gespeichert. Eine korrigierte Zusatznotiz darf auch nicht als alter Hinweis zum Erzähler zurückkehren.

Erzähl-KI und Helfer benutzen getrennte Auswahlpräferenzen und dieselben unveränderten Modelldateien. Jeder Rollenwechsel beendet native Arbeit unter dem jeweiligen Operations-Lock, entlädt die andere Text-KI, lädt die benötigte und verwirft ihren Gesprächscache. LiteRT erhält frische Gespräche. Der aktive Suchlauf wird vor dem Team beendet; der Suchencoder ist geschlossen, bevor die Textinferenz beginnt. Zu keiner Team-Inferenz werden zwei große Textmodelle parallel geladen. Hintergrundindizierung bleibt der vorhandene getrennte Ablauf; eine Spitzen-RAM-Garantie wird daraus nicht abgeleitet.

Nach dem Team wird der Helfer freigegeben; bei normalem Abschluss lädt die App den gewählten Erzähler wieder. Diese zusätzliche Wiederherstellung wird im Desktop-Inferenzvergleich nicht mitgemessen. Nach Abbruch kann die Erzählerdatei zunächst nur als heruntergeladen erscheinen und wieder geladen werden.

Jeder Helfer zählt seinen eigenen tatsächlich formatierten Prompt, nicht mit dem Tokenizer des Erzählers. Auch Hinweise und Reparaturdaten sind in dessen Tokenplanung enthalten. Budget pro Aufruf: 4.096 Kontexttoken einschließlich 512 Antwortreserve. Optionale ältere Helferquellen dürfen entfallen; erforderlicher Stand, Frage und wichtigste Quelle werden nicht still gekürzt. Bei Überfüllung wird der Schritt angehalten beziehungsweise ausdrücklich auf den bisherigen Ablauf zurückgefallen.

## Dateien, Modelle und Konfiguration

Alle Gewichte lagen bereits lokal vor. Es gab keinen zusätzlichen Modell-Download. Suchmodell: das unveränderte `embeddinggemma-300M-Q8_0.gguf`, ausschließlich für den Suchindex.

| Aufgabe / Kandidat | Tatsächliche Datei | SHA-256 |
| --- | --- | --- |
| Erzähler, Selbstprüfungs-Kontrolle | `quality-search-0.8.3/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf` | `2fde00ce69dd4899c70d020845e2638353015bba0fdf161b3eb965f2bca4464e` |
| Helfer 1 | `models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf` | `d80ef0f08a0e64887f4a19bc9202fe1a108d9666a2c49eb9a30bc3a30995121b` |
| Helfer 2 | `models-0.7.0/model-files/gemma-4-E2B-it.litertlm` | `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c` |

Pfade in der Tabelle beginnen unter `docs/validation/`. Die GGUF-Dateien sind Q4_K_M; llama.cpp ist weiterhin v0.5.0/`d2e54583c7452353eb35d40431281f6ee984332f`. Qwen-Original: `bartowski/Qwen_Qwen3-4B-Instruct-2507-GGUF`, Revision `ae44f08e1392f39c0e474af10c3ff8355c8b6688`. Huihui: `mahdisml/Huihui-Qwen3-4B-Instruct-2507-abliterated-Q4_K_M-GGUF`, Revision `1cbc997dbe95f5ec9b196bb79090328519363b84`.

Qwen nutzt den eingebetteten BPE-Tokenizer und seine nicht denkende ChatML-Vorlage. Huihui fehlt die eingebettete Gesprächsvorlage; für genau diese Datei wird der bestehende geprüfte ChatML-Fallback benutzt. Die tatsächlichen formatierten Eingaben einschließlich Markierungen stehen in jedem Rohdatensatz.

Gemma: unverändertes CPU/GPU-LiteRTLM-Artefakt von `litert-community/gemma-4-E2B-it-litert-lm`, Revision `b3ca0d2f076785a8f4b2219ddbd2bdb99954eae1`, 2.588.147.712 Bytes. Keine eigenständig neu bestimmte Bit-Quantisierung. Laufzeit LiteRT-LM 0.17.1, Gemma-4-Vorlage und ihr SentencePiece-Tokenizer `gemma4.model`. Der Helfertest verwendet CPU/4 Threads; die vorhandene GPU-Fallback-Möglichkeit wurde hier nicht als Handy-Leistungsnachweis genutzt.

Für beide Helfer und den Erzähler: Temperatur 0,75; topP 0,9; topK 40; Wiederholungsstrafe 1,08/256; Denken aus; 4 CPU-Threads; Testseed 42. Die Produktions-App nutzt weiterhin ihre bisherige Zufallsseed-Auswahl. Jeder native GGUF-Aufruf löscht den Cache, jeder LiteRT-Aufruf erstellt einen eigenen Kontext. Die Messung lief am PC mit Ryzen 7 9800X3D, rund 66,1 GB installiertem RAM; RTX 5080 vorhanden, GGUF-Inferenz dennoch CPU. [Hostprotokoll](validation/team-0.8.6/host-hardware.json).

## Phase A: zwei Helfer, ohne Produktionswirkung

14 eingefrorene Aufgaben je Kandidat: fünf Faktenauswertungen und neun Prüfungen. Enthalten sind drei alte falsche und drei alte richtige Antworten, zwei neue Fehler, eine neue grundsätzlich zulässige Gegenwartshandlung und neue Faktensituationen.

| Ergebnis | Huihui 4B | Gemma 4 E2B |
| --- | ---: | ---: |
| Struktur/Bindung verwertbar | 12/14 | 1/14 |
| Alte falsche Antworten mit unabhängig bestätigtem Prüffund | 0/3 | 0/3 |
| Alte richtige Antworten roh nicht als „clear“ eingestuft | 2/3 | 3/3 |
| Faktenaufgaben mit wenigstens einem originalbestätigten Hinweis | 3/5 | 0/5 |
| Anzahl solcher Hinweise | 4 | 0 |
| Median gemessener Generierung | 23,64 s | 8,67 s* |

\* Gemma-Zeiten sind für zwölf Aufrufe vorhanden. Bei zwei Ausgabelimit-Fehlern blieben Antwort/Tokenstatistik erhalten, aber der ältere Messwrapper lieferte keine abschließende Zeit. Diese Fehler werden nicht als schnelle erfolgreiche Antworten bewertet.

Huihui benennt zum Beispiel Ruths Geschenk an Enna richtig, verdreht aber daneben die Familie. Nur unabhängig aus dem Original nachgewiesene Teilinformationen passieren den Filter. Das ist kein insgesamt korrektes Verständnis. Er gibt den falschen Brief als „clear“ frei und erreicht bei der falschen Dolchantwort das Ausgabelimit. Gemma fügt häufig unbekannte Felder ein, verändert die Geschichten-ID oder kopiert das Schema.

Für die begrenzte Phase B wurde Huihui wegen der deutlich höheren Strukturquote gewählt, **nicht als zuverlässig empfohlener Helfer**. Beide Kandidaten sind qualitativ unzureichend. Rohdaten: [Huihui](validation/team-0.8.6/raw-phase-a-huihui-qwen3-4b.json), [Gemma](validation/team-0.8.6/raw-phase-a-gemma-4-e2b.json), [getrennte Auswertung](validation/team-0.8.6/phase-a-independent-results.json).

Eine Bewertung wurde transparent ergänzt: `new-good-action` beschreibt eine erlaubte Ringablage und die richtige Schwester, beantwortet aber nicht den Geschenkgeber. Eine Beanstandung dieses fehlenden Antwortteils ist kein Fehlalarm. Die eingefrorene ursprüngliche Bezeichnung wurde nicht still geändert; siehe `gold-review.json`. Falsch behauptete Familienfehler bleiben falsch.

## Phase B: tatsächliche ganze Antworten

A = korrigierter bisheriger Ablauf; B = Huihui-Prüfung und höchstens eine Reparatur; C = zusätzlich vorausgehende Huihui-Auswertung. Erzähler immer dasselbe originale Qwen-4B mit unveränderten Parametern. Profile, aktuelle Zustände, Originale und Suchtreffer sind innerhalb der Paare gleich. Nur Auftrags-ID, Zusatzhinweise und gegebenenfalls belegter Reparaturauftrag unterscheiden sich.

| Fall | A | B | C |
| --- | --- | --- | --- |
| Übergabe ohne belegten Grund | Falscher Träger, erfundenes Motiv/Eigentum | Gleicher Fehler, Helferformat ungültig | Reparatur blockiert |
| Ruth: Schwester und Geschenkempfänger | Beides vertauscht | Unverändert, Helfer „clear“ | Unverändert, Helfer „clear“ |
| Brief: Grund und Träger | Richtiger Grund, später Selbstwiderspruch | Unverändert, Helfer „clear“ | Unverändert, Helfer „clear“ |
| Dolch: Nutzerhandlung, Eigentümer/Träger | Ganze Antwort richtig | Richtig; unbelegte Helferbeanstandung | Richtig; Helferprüfung am Ausgabelimit, ausdrücklicher Rückfall |
| Neuer Ring-/Familienfall | Schwester vertauscht | Reparatur blockiert | Reparatur blockiert |
| Geheimer Schlüssel | Kein Geheimnis verraten; vorsichtiger Vorschlag | Gleich | Gleich |
| **Brauchbare ganze Antworten** | **2/6** | **2/6** | **2/6** |
| Alle Inferenzaufrufe, auch fehlgeschlagene | 6 | 14 | 22 |

Beim geheimen Schlüssel ist „ich habe gehört“ eine unbelegte kleine Ausschmückung; der eigentliche Suchvorschlag bleibt als Möglichkeit markiert. Diese Antwort wird im begrenzten Test als brauchbar mit Stilmakel bewertet, nicht als exakte belegte Vergangenheitsauskunft. Eine strengere Bewertung würde alle drei Bedingungen gleichermaßen auf 1/6 senken und das fehlende Nutzenresultat nicht verändern.

B beendet fünf, C vier von sechs Fällen technisch mit einer freigegebenen Antwort. Mehr abgeschlossene oder bestandene Schutzprüfungen bedeuten nicht mehr richtige Antworten: Ruth und Brief sind trotzdem falsch. Umgekehrt zählt die erfolgreiche Blockierung des Ringfehlers als Schutzwirkung, nicht als brauchbarer Ersatz.

In C wurden für diese sechs konkreten Aufrufe **keine neuen Helferhinweise akzeptiert**. Sie waren unbelegt, falsch zugeordnet oder formal ungültig. Deshalb werden die schon ohne Helfer richtigen Antworten nicht dessen Leistung zugerechnet.

Selbstprüfung mit denselben offiziellen Qwen-Gewichten: Ruth wird durch einen originalbestätigten Familienhinweis inhaltlich besser. Der Satz „Der Kristall hat Ruth geschenkt“ ist allerdings grammatisch misslungen. Der Dolch wird schlechter und bleibt nach Reparatur blockiert. Damit 1/2 richtiger sachlicher Kern, 0/2 ohne diesen Sprachvorbehalt brauchbare Gesamtantworten; kein stabiler Vorteil auf der gemeinsamen Teilmenge. Drei beziehungsweise fünf Aufrufe entsprechen dem Budget von C. Die Selbstprüfung ist kein unabhängiges Modell.

Rohdaten und echte Eingaben: [A](validation/team-0.8.6/raw-team-A.json), [B](validation/team-0.8.6/raw-team-B.json), [C](validation/team-0.8.6/raw-team-C.json), [Selbstprüfung](validation/team-0.8.6/raw-team-self.json). [Unabhängige Ganzantwortbewertung und gemessene Zeiten](validation/team-0.8.6/phase-b-independent-results.json).

Nach Einfrieren der Vergleichsklassen wurden eine zusätzliche Reparaturkontrolle für unbelegte Motive und der Schutz manuell korrigierter Zusatzhinweise ergänzt. Der finale Controller wurde mit sämtlichen 14 gespeicherten B/C/Selbstprüfungsfällen erneut ausgeführt. Eingaben, eigene native Tokenzählung, Templates, Aufrufzahlen und finale Entscheidungen/Antworten bleiben identisch. Das ist ein Controller-/Tokenizer-Abgleich mit **echten unveränderten Rohantworten, keine zusätzliche Generierung und kein neuer Qualitätsnachweis**: `final-controller-replay-{B,C,self}.json`.

## Echter fortlaufender Dialog mit Korrektur

Die tatsächlich richtige C-Dolchantwort wird unverändert als vorausgehende Runde verwendet. Die Geschichte wird aus ihren Originalen in SQLite rekonstruiert; dabei ändern sich Nachrichten-IDs, nicht Texte. Diese notwendige Rekonstruktion ist von den folgenden exakt versionsgebundenen Runden getrennt dokumentiert. Vor Runde 1 wird Eigentümerin **Oda** manuell und als der Figur bekannt gespeichert. Plan und zwei neue Nutzereingaben wurden vor deren Inferenz eingefroren.

| Ebene | Runde 1: Ablage neben der Schale | Runde 2: Bennet nimmt den Dolch wieder |
| --- | --- | --- |
| Tatsächlich gespeicherter / vorläufiger Stand | Oda; neue Ablage neben der Schale | Oda; aktueller Träger Bennet, nicht abgelegt |
| Tatsächliche Erzähleingabe | Oda und neue Ablage im nativen Prompt | Oda und Bennet im nativen Prompt |
| Unveränderte finale Modellantwort | „Der Dolch gehört Oda. Er liegt jetzt neben der Schale.“ | „Der Dolch gehört Oda. Du hältst ihn jetzt.“ |
| Speichern / erneutes Öffnen | Originale erhalten, finale Antwort und zulässige Änderungen atomar | Originale erhalten, finale Antwort und zulässige Änderungen atomar |

Runde 1 benötigt drei Aufrufe; Hinweise leer, falsche Helferbeanstandungen als unsicher verworfen. Runde 2 benötigt fünf Aufrufe: Die erste Prüfung erreicht das Ausgabelimit; der vorhandene Schutzfilter beanstandet den ersten Entwurf trotz richtigen sachlichen Kerns. Die Reparatur antwortet korrekt. Auch ihre Helferprüfung behauptet unbelegte Fehler. Das belegt weder einen besseren Helfer noch volle Erzählqualität. Die Mimik wiederholt sich und die neugierige Nutzerhandlung bekommt nur eine dünne Reaktion.

Es wurde keine fehlgeschlagene Runde mit einer künstlichen Sollantwort fortgesetzt. Die unabhängige Sichtprüfung der ganzen Antwort ist vor jedem echten Commit separat gespeichert. Rohdaten: `raw-dialogue-1.json`, `raw-dialogue-2.json`; Eingaben: `dialogue-*-scenes-frozen.json`; gespeicherte Zustände und Wiederöffnungsnachweis: `dialogue-*-stored-{before,after}.json`, `dialogue-*-accepted.json`, synthetische SQLite-Snapshots. Die manuelle Eigentümerkorrektur bleibt bestehen.

Bei der ersten Vorbereitung der zweiten Runde hielt Gradle den Test wegen nicht als Task-Eingabe erfasster Umgebungsparameter für aktuell. Es wurde keine neue Vorlage erzeugt und keine Inferenz gestartet. Der fehlgeschlagene Start und die erzwungene tatsächliche Vorbereitung sind getrennt erhalten (`raw-dialogue-2-missing-fixture.log`, `dialogue-prepare-2-forced.log`). Dieser Lauf zählt nicht als Modellantwort.

## Aufwand: PC-Messwerte, kein S24-Benchmark

Summe der tatsächlichen Generierungszeiten pro Fall, ohne Suchlauf, Laden, Tokenplanung, SQLite/UI und abschließende Erzähler-Wiederherstellung. Bei blockierten Fällen enthält die Zeit alle tatsächlich versuchten Aufrufe, kein erfolgreich freigegebenes Ergebnis.

| Fall | A | B | C |
| --- | ---: | ---: | ---: |
| Buch ohne Grund | 55,94 s | 76,40 s | 187,43 s, blockiert |
| Ruth | 44,56 s | 62,99 s | 87,49 s |
| Brief | 49,26 s | 62,92 s | 80,18 s |
| Dolch | 8,72 s | 39,34 s | 72,13 s |
| Neuer Ring | 7,25 s | 63,71 s, blockiert | 89,62 s, blockiert |
| Geheimer Schlüssel | 7,33 s | 16,68 s | 44,09 s |

Für vollständig gemessene erfolgreiche Teamabläufe sind zusätzliche Lade-/Planungszeiten in `outcome.calls` vorhanden. Beispiel geheimer Schlüssel: B rund 19,00 s bis zur Rückgabe des freigabefähigen Ergebnisses, C rund 47,80 s; davon Rollenwechsel-Laden rund 2,31 beziehungsweise 3,69 s. Der erste interne C-Text nach 7,71 s ist ein **Helfertext**, keine sichtbare Figurenantwort. Die App zeigt den ganzen Teamtext erst nach Prüfung.

Dialogrunde 1: rund 83,98 s reine Generierung, 87,85 s gemessene Schritte einschließlich Rollenladen/Planung. Runde 2: rund 157,03 s reine Generierung; wegen des fehlgeschlagenen Prüfschritts ist die Summe der vorhandenen `outcome.calls` keine vollständige Gesamtdauer. Fehlende Zeitanteile werden nicht mit Null ersetzt. Für B/C/Selbstprüfung sind alle 44 tatsächlichen Aufrufe in `steps` erhalten, auch wenn ein Fehler keinen erfolgreichen `TeamCall` erzeugt.

Die nativen Tests laden jeweils nur einen GGUF-Textmodell-Handle. Kalte und wiederholte Rollenwechsel sind enthalten. Spitzen-RAM, thermische Dauerlast, native Abbrüche auf einem Handy, Hintergrund/Vordergrund, Prozessverlust und tatsächliche UI-Freigabezeit auf S24/S24 Ultra sind **offen**. `adb-devices.txt` zeigt kein angeschlossenes Gerät. Die PC-Zeiten sind keine Smartphone-Leistungsangaben.

## Tests und Lieferung

- **146 reguläre Tests:** 0 Fehler, 0 Fehlschläge, 0 übersprungen.
- **98 relevante Integrationstests:** 0 Fehler, 0 Fehlschläge, 0 übersprungen; 24 überschneiden sich mit den regulären Tests. Die Zahlen dürfen nicht als 244 verschiedene Tests addiert werden.
- Zusätzliche gezielte Fixture-/Dialogläufe sowie finale Controller-Replays separat protokolliert.
- Build und Lint bestanden. Lint: **0 Fehler, 133 Warnungen, 1 Information**; eine zusätzliche bestehende Stilwarnung `UseKtx` für geprüfte synchrone Einstellungsspeicherung gegenüber 0.8.5. Kein Lint-Fehler verschwiegen.
- Tests prüfen getrennte Auswahl und Standard-Aus, echte SQLite-Transaktion/Idempotenz/Wiederöffnen, gefälschte Vorschläge, Versionskonflikte, Geheimnisse, Quellenänderungen, manuelle Korrektur, Geschichtenbindung, keine Anzeige des Entwurfs, Abbruch und begrenzte Aufrufzahlen. Test-Doubles belegen diese Mechanik, nicht die Modellqualität.
- 28 echte Helfer-Aufrufe in Phase A, 50 echte Aufrufe in Phase B einschließlich Selbstprüfung, 8 echte weitere Dialogaufrufe: **86 echte lokale Generierungen**, darunter unvollständige Fehlerläufe. Controller-Replays zählen nicht dazu.

Neue Produktdateien: `memory/TeamProtocol.kt`, `memory/TeamRunner.kt`, `ai/TeamEnginePort.kt`. Integration in `AppViewModel.kt`, `LocalModelEngine.kt`, `StoryRepository.kt`, `ui/UiState.kt`, `ui/SetupScreen.kt`, `ui/StoryApp.kt`; Versions-/Bauskript und lokale README angepasst. Neue Tests: TeamProtocol, TeamRunner, TeamPersistence, TeamFlow, HelperSelection, TeamUi; reine Messvorlagen: TeamFixtures, TeamComparisonScenes, TeamConversationProbe. Bestehende Diagnosewriter wurden auf ein neues Ausgabeziel umgestellt, um 0.8.5-Artefakte zu schützen. Vollständige Dateiliste und Prüfsummen: `changed-files.json`, `delivery-verification.json`, `artifact-hashes.json`.

**APK:** `Geschichten-0.8.6.apk`, 322.155.301 Bytes, SHA-256 `25babcd30bca4a2225695d000f05f1cec3ebb7d432b15c3a0de6fe9982d7be4f`. Paket `dev.vincent.geschichten`, Android mindestens 12/API31, Ziel API35, ARM64. APK-v2-Signatur und 16-KiB-Ausrichtung geprüft. Zertifikat SHA-256 `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`, kompatibel zum bisherigen lokalen Testschlüssel. Native Bibliotheken und bisherige Assets werden bytegleich mit 0.8.5 geprüft. Keine Prüfdatensätze oder zusätzlichen Modellgewichte in der APK; vorhandene GGUF-Tokenizerdateien sind Wortschatzdaten, keine neuronalen Gewichte.

Als Update installieren, die vorhandene App vorher nicht deinstallieren. Der Versuch ist unter Einstellungen verfügbar und bleibt aus. Dieser lokale Stand wird von der GitHub-Updatesuche nicht gefunden, weil **keine Veröffentlichung** erfolgt.

## Verbleibende Fehler und praktische Grenzen

Die Helfer verstehen deutsche Rollen und Quellen nicht zuverlässig. Ein formal gültiges Ergebnis kann trotzdem falsch sein. Die unabhängige Validierung akzeptiert nur begrenzte, bereits regelgestützt nachprüfbare Muster; damit gewinnt sie wenig neue sprachliche Abdeckung. Kompakte Übergabehinweise können sprachlich schematisch sein. Unbelegte oder nicht mechanisch nachprüfbare Interpretationen werden nicht als bestätigter Weltzustand gespeichert.

Familie, erfundene Gründe, innerer Widerspruch und fehlende Fragebestandteile werden vom Helferauftrag geprüft, aber nicht allgemein zuverlässig erkannt. Allgemeine semantische Vollständigkeit ist kein implementierter Wahrheitsbeweis. Die zusätzlichen Reparaturkontrollen decken begrenzte Muster ab und können weitere Formulierungen übersehen. Auch der vorhandene Schutzfilter kann bei Gegenwartserzählung zu streng reagieren. Eine vollständige gemeinsame Vergangenheit oder neue Familienstruktur wird durch den Versuch nicht sicher verstanden.

Die Erzähleingaben sind source-/zustandsgebunden und die Speicherung ist abgesichert; richtige Verwendung durch das Modell bleibt eine eigene, teilweise fehlgeschlagene Ebene. Mehr JSON, mehr Notizen, mehr erkannte oder blockierte Fehler und mehr Rechenzeit ersetzen keine brauchbare Figurenantwort. Dieser Test rechtfertigt deshalb keine automatische Aktivierung und keine Empfehlung eines zuverlässigen lokalen Prüfhelfers.
