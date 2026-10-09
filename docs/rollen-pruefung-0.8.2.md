# Rollen, Originalquellen und falsche Erinnerungen – Geschichten 0.8.2

## Ergebnis

Mehr nachweislich falsche Antworten werden vor ihrer dauerhaften Übernahme abgefangen. Die neue Prüfung erkennt zwölf der sechzehn fehlerhaften Originalantworten aus 0.8.1; beide sachlich richtigen Originalantworten und acht zusätzliche richtige Formulierungen werden akzeptiert. Die Modelle erzählen trotzdem noch nicht zuverlässig konsistente Geschichten. Eine Zurückweisung erzeugt keine richtige Ersatzantwort.

Der lokale Teststand trennt Eigentümer von Träger oder Ablageort, erhält ausdrückliche Negationen bei Verletzungen und verhindert die nachgewiesene Wiederverwendung einer erfundenen Geschenkgeschichte. Die Datenbank bleibt auf Version 9. Ihre Implementierung ist bytegleich mit 0.8.1. Die Modellgewichte, Laufzeiten, Vorlagen, Tokenizer, Samplingwerte und 90 Figuren werden nicht geändert.

Die automatisch erzeugte Darstellung mit eindeutigen Namen ist **nicht im Produktionsablauf aktiviert**. Manuell geprüfte Referenzen verbesserten einzelne Antworten; zwei automatische Fassungen beantworteten gerade die entscheidende Briefbegründung nicht zuverlässig. Die App behält historische Originalausschnitte. Die strukturierte Rollenerkennung dient der Prüfung und Quellenbewertung.

Die [signierte APK](../Geschichten-0.8.2.apk) ist ausschließlich lokal bereitgestellt. Nicht vorher deinstallieren. Es erfolgt keine Veröffentlichung, kein Training, keine Adapterintegration und keine zusätzliche Online-KI. Ein S24 ist nicht angeschlossen; Installation, RAM, thermisches Verhalten und Wartezeit auf dem Handy sind offen.

## Ausgangslage und vollständig gerenderter Prompt

Ausgangspunkt sind die endgültigen [Prüfeingaben aus 0.8.1](validation/active-memory-0.8.1/model-cases-final.json), die [nativen Antworten](validation/active-memory-0.8.1/models-final/) und ihre [inhaltliche Bewertung](validation/active-memory-0.8.1/model-assessment.json). Frühere Entwicklungsfälle ersetzen diesen Ausgangspunkt nicht.

Der Brief-Fall enthält dreißig Nachrichten. In einer langen Nutzernachricht stehen mitten im Text das Losungswort Morgenstern und die Übergabe: Rian gibt Mira den Brief, weil Rian mit seiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann. Der damalige Abruf liefert diese beiden Originalabschnitte. Der Brief ist im bereitgestellten Stand bei Mira; die Verletzung und der Grund stehen im Originaltext. Eine vollständige strukturierte Extraktion dieser Verletzungsform war damit noch nicht bewiesen.

Der Produktionsprompt ordnet das Profil Mira zu. Die jüngsten Dialogrunden und die letzte Frage haben die richtigen API-Rollen. Die letzte Nachricht ist USER, mit der Frage, warum **ich dir** den Brief anvertraut habe. Die historischen Ausschnitte stehen im SYSTEM-Text, ausdrücklich als Quellenmaterial mit Sprecher, Archivzeit, Abschnittsgrenzen und der vorhandenen Erklärung **ich = Spielerfigur, du = Mira**. Sie werden nicht als neue aktive USER-Nachrichten eingespeist. Diese Sprecherkennzeichnung existierte bereits in 0.8.1 und ist keine neue Reparatur.

Es gibt keine nachgewiesene widersprüchliche Anweisung, Mira solle Rian spielen. Aktueller Stand, Erinnerung direkt vor der letzten Frage und historische Ausgangsszene enthalten Wiederholungen. Die zusammengesetzte Bezeichnung „Besitzer / Standort“ vermischt außerdem Tragen und Eigentum. Im Quellenzitat stehen weiterhin die Perspektivwörter der Spielerfigur. Dass Modelle diese Wörter in ihre eigene Rede übernehmen, ist beobachtet; ein bestimmter interner Mechanismus oder eine einzelne Ursache ist damit nicht bewiesen.

Mit unverändertem 0.8.1-Code und Seed 42 werden die endgültigen Gemma- und Huihui-Antworten **wortgleich reproduziert**. Mira erklärt in beiden Fällen sinngemäß, sie habe Rian den Brief wegen ihrer eigenen verletzten Hand anvertraut. Geber, Empfänger und verletzte Person sind vertauscht. Die alten Filter akzeptieren diese Antworten.

## Kontrollierter Vergleich A, B und C

Zwei bereits vorhandene Modelle, keine allgemeine Rangliste:

| Eigenschaft | Festlegung |
| --- | --- |
| Gemma | Gemma 4 E2B, festgelegte LiteRT-LM-Datei |
| Huihui | Qwen 3 4B Instruct 2507, festgelegtes Q4_K_M-GGUF |
| Laufzeit | Je Modell unverändert, Desktop-CPU mit vier Threads |
| Kontext / Ausgabe | 4.096 / höchstens 512 Token |
| Sampling | Temperatur 0,75; Top-p 0,9; Top-k 40; Wiederholungsstrafe 1,08, Fenster 256 |
| Wiederholungen | Seeds 42 und 31415 für den Brief; Seed 42 für die vier weiteren Fälle |

**A:** Unveränderter Produktionsplaner und Filter aus 0.8.1. Für den Brief stimmen SYSTEM und Dialogliste exakt mit den endgültigen 0.8.1-Dateien überein.

**B:** Nur der historische Quellenblock von A wird durch eine manuell geprüfte Referenz mit Namen, Richtung, Verletzung, Grund beziehungsweise Eigentumszuordnung ersetzt. Die aktive Dialogliste bleibt bytegleich. Der Versuch beweist keine automatische Extraktion.

**C:** Kurze Rollenfestlegung, dieselbe geprüfte Referenz und dieselbe letzte Frage. Der übrige lange Verlauf entfällt. C ist ein anderer Promptaufbau und keine isolierte Messung allein der Kontextlänge.

| Fall | Inhalt | Gemma Token A / B / C | Huihui Token A / B / C |
| --- | --- | ---: | ---: |
| Rian / Mira | Brief Rian → Mira, Rians Hand und Siegel | 3454 / 3332 / 179 | 3324 / 3201 / 209 |
| Kora / Levin | Amulett Kora → Levin, Koras rechte Schulter | 3329 / 3252 / 151 | 3445 / 3366 / 176 |
| Juna / Tarek | Junas linke Hand, Tareks unverletzte Schulter, schwere Tür | 3403 / 3281 / 143 | 3545 / 3418 / 170 |
| Niko / Selma | Nikos Schwester Alma, Selmas Bruder Aron, Karte von Alma | 3440 / 3256 / 139 | 3550 / 3356 / 167 |
| Malik / Daria | Daria trägt das Amulett, Malik besitzt es | 3323 / 3256 / 133 | 3434 / 3367 / 151 |

Alle 36 Antworten sind echte Inferenz. In allen 36 Fällen stimmt die vorab gezählte Eingabe mit der Laufzeit überein. Die vollständigen gerenderten Prompts, unveränderten Antworten und alten Filterentscheidungen stehen in [Gemma A/B/C](validation/roles-0.8.2/gemma-4-e2b-comparison.json) und [Huihui A/B/C](validation/roles-0.8.2/huihui-qwen3-4b-comparison.json). [Vergleich und Bewertung](validation/roles-0.8.2/comparison-assessment.json) enthält den Nachweis des einzigen ersetzten Blocks, die Tokenzahlen, Prompt-Prüfsummen und Einzelurteile.

| Modell | Brauchbare vollständige Antworten A | B | C |
| --- | ---: | ---: | ---: |
| Gemma 4 E2B | 1 / 6 | 2 / 6 | 0 / 6 |
| Huihui Qwen 3 4B | 1 / 6 | 3 / 6 | 2 / 6 |

Die sechs Antworten je Bedingung sind fünf Situationen mit einer zweiten Brief-Wiederholung. Unvollständige Antworten und erfundene zusätzliche Motive oder Spielerhandlungen zählen nicht als brauchbar. Einzelne Grammatik- oder Formatmängel sind zusätzlich vermerkt. Diese kleinen Zahlen sind keine allgemeine Zuverlässigkeitsquote.

Wichtige Einzelbefunde:

- Huihui beantwortet den Brief unter B in beiden Wiederholungen sachlich richtig. Der alte Filter weist beide richtigen Antworten wegen einer falschen Besitzinterpretation von „Rian hat mir den Brief anvertraut“ zurück.
- Gemma vertauscht die Briefrollen unter A, B und C. Auch eine sehr kurze eindeutige Referenz beseitigt das Problem nicht.
- Beide Modelle ordnen Alma unter C der eigenen Figur zu, obwohl die Referenz Alma als Nikos Schwester bezeichnet. Gemma nennt im kurzen Verletzungsfall zudem Jonas statt Juna.
- Huihui ordnet die Familie unter B richtig zu. In B beim Amulett stimmen die gesprochenen Rollen, aber der Erzähler erfindet eine neue Übergabe durch Malik. Diese gesamte Antwort zählt deshalb nicht als richtig.
- Huihuis C-Antwort „Daria trägt … es gehört Malik“ ist richtig, wird aber vom alten Filter ebenfalls abgelehnt. Träger und Eigentümer wurden im alten Zustandsfeld vermischt.

Die Fehler treten somit auch bei kurzem eindeutigem Kontext auf. Ein reiner Tokenmangel oder nur die Länge des Archivs erklärt sie nicht. B zeigt einen begrenzten Nutzen des Quellenformats; Länge, Wortlaut und Format ändern sich dabei gemeinsam. Eine einzige Antwort erlaubt keine weitergehende Ursachenbehauptung.

## Automatische Ereignisdarstellung: geprüft und deaktiviert

Nach B wurden zwei automatische Fassungen mit denselben fünf Situationen und Brief-Wiederholungen geprüft, jeweils zwölf echte Antworten. Die erste Fassung ist unter [production-prototype](validation/roles-0.8.2/production-prototype/) erhalten, die natürlichere zweite unter [automatic-second](validation/roles-0.8.2/automatic-second/). Die manuelle Referenz wurde dabei nicht an den Produktionsplaner übergeben.

Die Extraktion ordnet im Briefausschnitt Rian als Handelnden, Mira als Empfängerin, den Brief als Gegenstand und Rian als verletzte Person korrekt zu. Beide automatischen Darstellungen liefern trotzdem keine vollständig brauchbare Briefbegründung in den zwei Huihui-Wiederholungen: Die erste ergänzt Zittern, erste Besichtigung oder eigenes Einpacken; die zweite behauptet, den mitgeteilten Grund nicht genau zu kennen. Andere Situationen gelingen teilweise. Das rechtfertigt keine allgemeine Aktivierung.

Die App nutzt deshalb weiterhin die Originalausschnitte. `renderNamed` bleibt als ausdrücklich experimentelle, testbare Darstellung vorhanden und ist im Produktionsplaner ausgeschaltet. Auch bei Gemma wird kein angeblich repariertes Format aktiviert. Es gibt keine zusätzliche lokale Auswertungs-KI und keine versteckte Reparaturschleife.

## Änderungen im Produktionscode

| Bereich | Änderung und Zweck |
| --- | --- |
| `RoleEvidence.kt` | Temporäre rollenbezogene Ereignisse: Sprecher, Handelnder, Empfänger, Gegenstand, betroffene Person, Zeitbezug, Negation, exakte Originalspanne, Nachrichten-ID, Herkunft und Figurenwissen. Keine neue Tabelle. |
| `RoleReplyGuard.kt` | Prüft explizite Rollenwidersprüche in Rede **und** Erzählertext, falsche Familienzuordnung, unbewiesenes Eigentum und bestimmte unbelegte Vergangenheitsbehauptungen. Erkennt außerdem die beobachteten reinen Frage-Echos, Anweisungskopien und Wiederholungsschleifen. |
| `RoleSourcePolicy.kt`, `ArchiveRecall.kt`, `MemoryPrompt.kt` | Ungeprüfte historische Modellbehauptungen werden im erkannten Bereich weder als passende Quelle noch als jüngste Dialogbelege erneut eingespeist. Originalnachrichten bleiben im Archiv. |
| `StoryMemory.kt`, `StoryRepository.kt`, `StateMemoryUi.kt` | Explizites „gehört“ wird als Eigentümer gespeichert, „hat/trägt“ als Träger. Beide nutzen vorhandene `state_facts`-Zeilen und die vorhandene TEXT-Spalte `field`. Der neue fachliche Schlüssel `owner` benötigt keine Datenbankmigration. Die UI unterscheidet die beiden Angaben. |
| Verletzungsregeln | „Nicht verletzt“ wird als negativer Zustand gespeichert und tatsächlich vor der Antwort bereitgestellt. Es wird keine Verletzung daraus erzeugt. Ausweichende oder mehrdeutige Negationen werden nicht in ein eindeutiges positives Ereignis verwandelt. |

Es gibt keine pauschale Ersetzung von „ich“, „du“, „mein“ oder „dein“. Direkte Rede wird innerhalb erkannter Sprecherbereiche gelesen; zugeschriebene Aussagen sind nicht automatisch Wahrheit. Verschachtelte oder ungelöste Sprecherbezüge, gleiche Namen und mehrere mögliche Gegenstände werden nicht durch einen erfundenen eindeutigen Bezug ersetzt. „Er/sie“ im mehrdeutigen Übergabegrund wird nicht automatisch der gebenden Person als Verletzung zugeordnet. Fragen und erkannte hypothetische Aussagen, einschließlich „wäre“ und „wenn“, begründen keine bestätigten Rollenereignisse.

Ein exakter Quellenausschnitt wird als `[start, end)` der unveränderten Nachricht gespeichert. Nachrichten-ID und Archivzeit ordnen ihn zu; die ID allein bestätigt seine Wahrheit nicht. Der Parser arbeitet auf dem geschichtenspezifischen Snapshot. Bestehende Zustandsrevisionen und Transaktionsprüfungen verhindern weiterhin die Übernahme verspäteter Ergebnisse nach einer Korrektur.

Ein verfasster Anfang wird anhand derselben Herkunftsbedingungen wie die vorhandene Auswertung erkannt: erste Figuren-Nachricht, Erstellungszeit der Geschichte und Übereinstimmung mit dem verfassten Auftakt. Eine gültige verfasste Vergangenheit wird dadurch nicht pauschal als Modellhalluzination behandelt. Die Originaltexte und langen Einführungen werden nicht bearbeitet.

Explizite neue Gegenwartsaktionen bleiben möglich. Die Tests erhalten insbesondere Miras zulässige Rückgabe und das Ablegen des tatsächlich getragenen Schlüssels in einer Truhe. Ein gegenwärtiges erzähltes Ereignis ist etwas anderes als eine unbelegte rückblickende Behauptung. Die Erkennung deckt nicht jede freie kreative Formulierung ab.

Alte „gehört“-Angaben im bisherigen `holder`-Feld bleiben erhalten und werden als „Eigentümer (alte Zuordnung)“ bezeichnet. Sie werden nicht still umgeschrieben und nicht als Beweis für den aktuellen Träger verwendet. Eine ausdrückliche neue Festlegung kann Eigentümer und Träger anschließend getrennt setzen.

## Nachgewiesene Verstärkung einer falschen Erinnerung

Die tatsächliche Huihui-Antwort aus 0.8.1 behauptet ein Geschenk der eigenen Schwester, Wärme und ein Gespräch an einem kühlen Morgen. Der alte Filter akzeptiert sie. Die Antwort wird archiviert und als verarbeitet markiert. Ein bestätigter Geschenk-Fakt entsteht dabei nicht: `giftFacts` ist leer. Bei einer späteren Frage ruft die alte Suche die falsche Modellantwort trotzdem als Originalbeleg ab.

[Vorher](validation/roles-0.8.2/contamination-before.json) enthält diese akzeptierte Antwort und den später gefundenen falschen Ausschnitt. [Nachher](validation/roles-0.8.2/contamination-after.json) enthält dieselbe unveränderte Antwort als bereits vorhandenen Altbestand: Sie bleibt archiviert und verarbeitet, wird aber nicht mehr als Beleg abgerufen. Der neue Filter verhindert ihre Übernahme bei einer neuen Generierung. Die jüngsten Modellnachrichten werden ebenfalls geprüft, damit dieselbe Behauptung nicht über einen zweiten Eingabekanal zurückkehrt.

Die Regression prüft außerdem Wiederholung ohne Nutzerbestätigung und eine ausdrücklich bestätigende Nutzerfestlegung. Wiederholte Modellbehauptungen und Verarbeitungsmarkierungen bestätigen keine Vergangenheit. Die ausdrückliche Nutzerfestlegung ist eine andere Quelle. Das ist ein belegter Schutz für diesen erkannten Ablauf, kein vollständiger Wahrheitsprüfer für sämtliche deutschen Vergangenheitsformulierungen.

## Antwortprüfung und endgültige echte Modellantworten

Der [Antwortkorpus](validation/roles-0.8.2/answer-guard-corpus.json) verwendet alle achtzehn unveränderten Originalantworten der sechs Modelle aus 0.8.1. Davon sind sechzehn falsch, unvollständig oder sprachlich unbrauchbar und zwei sachlich richtig. Acht weitere richtige Formulierungen prüfen Umformulierungen, Perspektiven und die Trennung von Eigentum und Tragen.

| Prüfung | Ergebnis |
| --- | ---: |
| Erkannte fehlerhafte Originalantworten | 12 / 16 |
| Übersehene fehlerhafte Originalantworten | 4 / 16 |
| Versehentlich zurückgewiesene richtige Originalantworten | 0 / 2 |
| Versehentlich zurückgewiesene zusätzliche richtige Antworten | 0 / 8 |

Übersehen werden weiterhin Dolphins unvollständige Farb-/Trägerantwort, Gemma 3s teilweise Briefantwort, Gemma 3s unvollständige Farbantwort und Qwen 3 0.6Bs unvollständige Farbantwort. Die Prüfung kann ausdrücklich falsche Zuordnungen besser erkennen als sämtliche Formen einer ausweichenden Antwort.

Anschließend wurden zwölf echte Antworten mit dem endgültigen Produktionsablauf erzeugt. [Rohantworten, Eingaben und Laufzeiten](validation/roles-0.8.2/production-final/) sowie [Einzelbewertung](validation/roles-0.8.2/production-assessment.json) erhalten jede Antwort vollständig.

| Modell, sechs Antworten | Vollständig sachlich richtig | Für den Nutzer insgesamt brauchbar | Zurückgewiesen | Akzeptiert, aber schlecht oder teilweise |
| --- | ---: | ---: | ---: | ---: |
| Gemma 4 E2B | 1 | 1 | 3 | 2 |
| Huihui Qwen 3 4B | 3 | 2 | 2 | 2 |

Huihui verwendet in beiden Brief-Wiederholungen Rian als Geber, Mira als Empfängerin und Rians Hand als Grund richtig. Eine Antwort ist insgesamt brauchbar; die andere spricht störend über „Archiv“ und „Originalstelle“ und zählt trotz richtiger Fakten nicht als brauchbare Figurenantwort. Beide Modelle beantworten Koras Übergabe korrekt. Beide verwechselt man im Familienfall weiter; der neue Filter weist diese Antworten zurück. Die falschen Eigentumsantworten werden ebenfalls zurückgewiesen. Gemmas falsche eigene Verletzung wird abgefangen; seine zurückgefragte Briefbegründung rutscht durch.

Es gibt insgesamt 72 neue echte Generierungen: 36 A/B/C, zwölf pro automatischer Fassung und zwölf im endgültigen Ablauf. Die vier anderen Modelle wurden in diesem Auftrag nicht erneut ausgeführt; ihre tatsächlichen 0.8.1-Antworten wurden durch die gemeinsame Prüfung bewertet. Keine dieser Zahlen ist eine umfassende Bewertung für alle 90 Figuren.

Die letzte konservative Grammatikfassung wurde ohne weitere Generierung gegen diese tatsächlichen Eingaben und Antworten geprüft. In allen zwölf Fällen sind SYSTEM und Dialogliste bytegleich, die native Tokenzahl stimmt überein und die Filterentscheidung bleibt gleich. Der Nachweis steht unter [apk-classes-verification](validation/roles-0.8.2/apk-classes-verification/).

**Diagnosegrenze:** Beim Huihui-Eigentumsfall im ersten Versuch und beim Huihui-Familienfall im endgültigen Lauf erfasste das frühe `formattedPrompt`-Feld den letzten Budgetversuch statt der gewählten Eingabe. Die tatsächliche Generierung erhielt die separat gespeicherten gewählten SYSTEM-/Dialogwerte. Diese Originaldateien wurden nicht umgeschrieben. Der [native Prompt-Audit](validation/roles-0.8.2/prompt-capture-audit.json) rendert alle achtzehn gewählten Huihui-Eingaben dieser drei Phasen erneut, stimmt ihre Tokenzahlen mit der tatsächlichen Inferenz ab und liefert `actualSelectedFormattedPrompt` sowie den ausdrücklich falschen Vergleichsindikator für die beiden alten Diagnosefelder. Die Probe wurde für spätere Wiederholungen korrigiert. A/B/C zählte die gewählte Eingabe bereits ausdrücklich vor der Erfassung.

## Aufwand, Regressionen und verbleibende Grenzen

Der Produktionsablauf benötigt **null zusätzliche Modellaufrufe**. Es gibt keinen automatischen Reparaturversuch. Die rollenbezogene Grammatik und die Quellenbewertung arbeiten lokal und lesen das vollständige Archiv. Ihr Aufwand wächst mit dem gespeicherten Text; die neue temporäre Darstellung wird nicht als zusätzliche Datenbankstruktur gespeichert. Die unveränderten Verarbeitungsmarkierungen verhindern weiterhin die erneute dauerhafte Auswertung gleicher Quellen.

Im endgültigen Desktop-Lauf liegt die mediane Gesamtplanung einschließlich exakter Tokenzählung bei 64 ms für Gemma und 55 ms für Huihui. Das ist kein isolierter Parservergleich gegen 0.8.1. Median bis zum ersten Text: ungefähr 4,31 Sekunden beziehungsweise 62,24 Sekunden; Gesamtdauer ungefähr 5,99 beziehungsweise 79,94 Sekunden. Dies sind Desktop-CPU-Messungen mit den beschriebenen langen Prüfeingaben, keine S24-Prognose. Der separate SQLite-Test mit hundert Gesprächsrunden liegt im aktuellen Artefakt [database-long-run.json](validation/roles-0.8.2/database-long-run.json); er misst keine Inferenz.

124 reguläre Unit-Tests und 62 gezielte Android-/SQLite-/ViewModel-/UI-/Migrationsprüfungen bestehen. Die Läufe überschneiden sich in den 14 Rollentests und dürfen nicht als unabhängige Gesamtsumme addiert werden. [Normale Ergebnisse](validation/roles-0.8.2/normal-results/), [gezielte Ergebnisse](validation/roles-0.8.2/integration-results/) und Buildprotokolle sind erhalten. Android Lint meldet null Fehler, 125 Warnungen und eine Information. Compose-Screenshots prüfen die geänderte Benennung; sie beweisen keine Handy-Inferenz.

Die Regressionen prüfen Speicherung und unmittelbare Modelleingabe getrennt: Eigentümer bleibt bei einer neuen Übergabe erhalten, Träger ändert sich; „nicht verletzt“ wird gespeichert und vor der Antwort als Negation übergeben; eine positive Verletzungsbehauptung widerspricht diesem Zustand. Daneben bestehen die Prüfungen für Privatsphäre, manuelle Korrekturen, spätes Nachlesen, veraltete Generierungen, Abbruch, Originalverlauf, Neustart und Katalogmigration.

Offen bleiben freie komplexe Sprache, nicht erkannte Gegenstände, mehrdeutige Bezüge, unvollständige Antworten, Metakommentare und historische Behauptungen außerhalb der begrenzten Grammatik. Die konservative Quellenprüfung kann auch einen tatsächlich richtigen Rückblick auslassen, wenn sie keinen passenden unabhängigen Beleg erkennt. Die zehn richtigen Korpusantworten sind keine Garantie gegen andere Fehlalarme. Die kurzen Kontrollfälle zeigen eine Grenze der vorhandenen Modelle; sie wird nicht durch weitere Datenbankfelder oder immer mehr Promptregeln als gelöst dargestellt.

Ein gestreamter Entwurf kann vor Abschluss sichtbar sein. Die Prüfung verhindert die dauerhafte Übernahme eines erkannten Fehlers; sie ersetzt ihn nicht automatisch durch richtigen Text. Bereits gespeicherte Korrekturen bleiben bei Fehlern erhalten. Ein angeschlossenes S24 war nicht verfügbar. Die Geräteprüfung bleibt ausdrücklich offen.

## Reproduzierbarkeit und APK

`run-comparison.ps1` verwendet den eingefrorenen 0.8.1-Klassenstand und die eingefrorenen Hilfsquellen. `controlled-inputs.json` wird bei vorhandener Datei nicht neu exportiert. `run-production.ps1` prüft den aktuellen Produktionsstand. `verify-final.ps1` vergleicht den abschließenden Planer und Filter mit den aufgezeichneten Antworten, ohne eine neue Antwort zu erzeugen. Die menschlichen Bewertungen sind separat in `assess-comparison.py` und `assess-production.py` festgehalten; sie verändern keine Antworttexte.

Paket `dev.vincent.geschichten`, Version 0.8.2, Versionscode 16, Datenbankversion 9, Android ab API 31, ARM64. Die APK verwendet denselben Testsignierer wie 0.8.1. Die Prüfung von Signatur, 16-KiB-Ausrichtung, nativen Bibliotheken, Tokenizer-Assets und Abwesenheit der Diagnoseklassen steht in [apk-verification.json](validation/roles-0.8.2/apk-verification.json) und den zugehörigen Werkzeugprotokollen. Alle App-Assets sind bytegleich mit 0.8.1. [source-changes.json](validation/roles-0.8.2/source-changes.json) dokumentiert Änderungen gegenüber den vorhandenen 0.8.0-/0.8.1-Prüfsummen.

Dateigröße und SHA-256 der ausgelieferten APK stehen im [kurzen Prüfbericht](PRUEFBERICHT-0.8.2.md). Die lokale Updatesuche findet diesen Stand nicht, weil kein GitHub-Release veröffentlicht wurde.
