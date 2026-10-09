# Vereinfachter Faktenhelfer: Ergebnis und gezielte Korrektur 0.8.7

08.10.2026. Ausgangspunkt: tatsächlicher Stand 0.8.6/code20 und dessen unveränderte [Originalartefakte](validation/team-0.8.6/). Entscheidung: **für die getesteten Kandidaten als Produktionshelfer nicht ausreichend hilfreich**. Keine weitere Helferstufe und keine erneute Chatintegration. Die Zusatz-KI bleibt standardmäßig ausgeschaltet. Die neue lokale APK 0.8.7/code21 entsteht ausschließlich wegen eines unabhängig belegten Filterfehlers.

## Ausgangsfehler getrennt erfassen

Alle 59 tatsächlichen Helferaufrufe aus 0.8.6 sind mit ihrer ursprünglichen Datei-/Zeilenposition einzeln erfasst: [Fehlerzuordnung](validation/helper-diagnosis-0.8.6/legacy-errors-classified.json). Kategorien überlappen; eine defekte Ausgabe kann mehrere Fehler enthalten. Nachweisbare Kategorien: 27 Format-/Bindungsechos, 33 falsche Original- oder Antwortbezüge, 5 unvollständige Ausgaben, 8 konkrete Personen-/Ereignisfehldeutungen, 13 rohe Freigaben trotz tatsächlichem Widerspruch, 8 unbelegte Beanstandungen unabhängig richtigen Fakteninhalts. Das sind keine vollständigen semantischen Fehlerzahlen. Formal unbrauchbare Antworten können weitere Inhaltsfehler enthalten; ein misslungenes Modellecho bedeutet außerdem keine erfolgte App-Freigabe.

Beispiele: „Meine Schwester“ wird der angesprochenen statt der sprechenden Person zugeordnet; An-sich-Nehmen wird mit Eigentum verwechselt; eine Frage dient als Ereignisbeleg. Prüfer paraphrasieren einen angeblich beanstandeten Satz, der gar nicht in der Antwort steht, oder nennen zusammengesetzte Belegaliase wie `F1,F2`. Der falsche Brief erhält teilweise `clear`. Eine legitime Beanstandung des fehlenden Geschenkgebers in `new-good-action` wird ausdrücklich von falschen Beanstandungen der erlaubten Ablage/Familie getrennt.

## Vorab festgelegter isolierter Versuch

Sechs Entwicklungsfälle verwenden manuell ausgewählte, unveränderte Originalausschnitte aus 0.8.6. Zwölf neue Abnahmefälle mit anderen Namen, Gegenständen und Formulierungen wurden **vor der Inferenz** festgelegt. Enthalten sind richtige/falsche Rollen, Verneinung, bloße Frage, unbekanntes Eigentum, Eigentümer/Träger, fehlender/ausdrücklicher Grund und neue Gegenwartshandlungen. [Eingaben und ausschließlich zur Bewertung verwendete Sollangaben](validation/helper-diagnosis-0.8.6/cases-frozen.json), [Prüfplan](validation/helper-diagnosis-0.8.6/plan-frozen.json).

Diese Quellen sind **Diagnoseeingaben, kein Nachweis automatischen Abrufs**. Die Herkunft alter Abschnitte ist mit Nachrichten-ID und Grenzen dokumentiert; neue Originale sind synthetisch verfasst. Der App-Arbeitsstand bindet Geschichte, Anfrage, Revision und Quellenalias außerhalb der Modelleingabe. Das Modell bekommt nur Original S1, Sprecher/Adressat und eine Frage beziehungsweise Behauptung. Keine Figurenrolle, vollständige Geschichte, IDs zum Wiederholen oder Sollantworten.

Verglichen werden A: kurze freie Sachauskunft; B: dieselbe Auskunft in `answer/source/quote`; C: eine einzelne Behauptung mit `belegt/widerlegt/unbekannt` und Originalbeleg. G ist eine **separate technische Formatbedingung** für B mit nativer Begrenzung. Keine Wiederholungen, Reparaturen oder nachträglichen Promptvarianten.

Zwei vorhandene Dateien: Huihui Qwen3 4B Q4_K_M, SHA-256 `d80ef0f08a0e64887f4a19bc9202fe1a108d9666a2c49eb9a30bc3a30995121b`, und Gemma 4 E2B LiteRTLM, SHA-256 `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c`. Dateien, Revisionen und Tokenizer bleiben wie 0.8.6. GGUF: eingebetteter Qwen-BPE-Tokenizer plus vorhandener ChatML-Fallback; Gemma: eigenes SentencePiece/Gemma-4-Template, LiteRT-LM 0.17.1. Kein Download, Training, Adapter oder Cloudaufruf.

Ausgangsprofil: Temperatur **0,75**, topP **0,9**, topK **40**, Wiederholungsstrafe **1,08**, Fenster **256**, Testseed **42**, CPU **4 Threads**, Denken aus, Kontext **4.096**, Ausgabe **512**. Genau ein zusätzliches Profil verändert nur die Temperatur auf **0,2**. Der kurze sachliche Auftrag ist zwischen Profilen identisch. Gewichte, Quantisierung und Quellen bleiben gleich. Die tatsächlichen formatierten Eingaben, nativen Tokenzahlen, unveränderten Ausgaben, Zeiten und Parameter stehen in den Rohdateien. Alle 132 Generierungen sind vollständig; keine Überschreitung des Ausgabelimits. Das eigene Kontextbudget wurde für jeden Aufruf geprüft.

## Technische Bindung und Formatvorgaben

Der isolierte Schnittstellenprototyp bindet die technische Identität aus dem erfassten Arbeitsstand. Ein Modell darf nur S1 und einen unveränderten Ausschnitt zurückgeben. Fremde Geschichte/Anfrage, veraltete Zustands-/Originalrevision, ausgeschlossene oder nicht bekannte Quelle, erfundener Alias/Ausschnitt und zusätzliche Befehlsfelder werden abgewiesen. Vier Tests prüfen diese Fälle getrennt. Er liefert ausdrücklich **kein semantisches Wahrheitszertifikat** und schreibt nicht in die Datenbank. [Prototyp](validation/helper-diagnosis-0.8.6/binding.py), [Tests](validation/helper-diagnosis-0.8.6/test_binding.py). Die bestehende produktive Quellen-/Wissensprüfung bleibt erhalten; der Prototyp wird nicht neu in die App eingebaut.

Die [Dokumentation der fest eingebundenen llama.cpp-Revision](../third_party/llama.cpp-d2e54583c7452353eb35d40431281f6ee984332f/grammars/README.md) beschreibt GBNF. Die vorhandene Android-Brücke bietet diese Auswahl bisher nicht an. Getestet wurde deshalb eine separate Diagnose-DLL mit denselben bereits gebauten Laufzeitarchiven; die App-Brücke wurde nicht verändert. LiteRT-LM **v0.17.1** bietet `enableResponseFormat` und JSON-Schema-Ausgaben über LLGuidance, bestätigt an den tatsächlichen JAR-APIs und der [versionsgebundenen Dokumentation](https://github.com/google-ai-edge/LiteRT-LM/blob/v0.17.1/kotlin/java/com/google/ai/edge/litertlm/Config.kt).

Beide nativen Begrenzungen erzeugen im Versuch gültige B-Objekte. Die Grammatik lässt freie Antwort-/Zitatstrings zu und enthält keine erwarteten Namen oder Lösungen. Ein vorgeschriebener S1-Alias beweist dennoch keinen richtigen Bezug. Selbst eine syntaktisch gültige englische Übersetzung als angebliches Originalzitat wird erst durch die separate Quellenprüfung abgewiesen.

## Entwicklung: Format und Inhalt getrennt

| Modell / Profil | A Inhalt | B Inhalt / strikt gültiges Format | C Inhalt / strikt gültiges Format |
| --- | ---: | ---: | ---: |
| Huihui 0,75 | 6/6 | 6/6 · 6/6 | 6/6 · 6/6 |
| Huihui 0,2 | 6/6 | 6/6 · 6/6 | 6/6 · 6/6 |
| Gemma 0,75 | 5/6 | 6/6 · 0/6 | 4/6 · 0/6 |
| Gemma 0,2 | 5/6 | 5/6 · 0/6 | 4/6 · 0/6 |

Gemma umhüllt die unbeschränkten JSON-Ergebnisse mit Markdown. Für die Inhaltsbewertung wurde der lesbare Inhalt unabhängig untersucht; für die Schnittstelle bleibt dieses Format ungültig. Bei nativer Begrenzung und 0,2 wird das B-Format 6/6 gültig, der Inhalt bleibt nur 5/6 richtig. Der fehlende Grund wird weiter nicht sauber verstanden. Huihui erreicht unter beiden Profilen identische inhaltliche Ergebnisse; die geringere Temperatur hat keinen nachgewiesenen Vorteil.

Vor Sichtung der Abnahme wurde **Huihui mit dem Ausgangsprofil 0,75** gewählt; bei gleicher Qualität bleibt das Ausgangsprofil erhalten. Gemma scheitert bereits in der Entwicklung. [Auswahl vor Abnahme](validation/helper-diagnosis-0.8.6/selected-before-acceptance.json), [getrennte Ergebnisse](validation/helper-diagnosis-0.8.6/development-summary.json), [Huihui-Rohdaten](validation/helper-diagnosis-0.8.6/raw-development-huihui-qwen3-4b.json), [Gemma-Rohdaten](validation/helper-diagnosis-0.8.6/raw-development-gemma-4-e2b.json).

## Neue unabhängige Abnahme: nicht bestanden

| Bedingung | Format richtig | Inhalt richtig | Quellenbindung richtig | Inhalt + Format + Beleg gemeinsam |
| --- | ---: | ---: | ---: | ---: |
| A freie Auskunft | 12/12 | **8/12** | außerhalb der Ausgabe gebunden | 8/12 |
| B kleines JSON | 12/12 | **8/12** | 11/12 | **7/12** |
| C einzelne Behauptung | 12/12 | **9/12** | 11/12 | **8/12** |
| G erzwungenes B-Format | 12/12 | **8/12** | 11/12 | **7/12** |

Konkrete Inhaltsfehler: Leif gibt laut Quelle Hedda die Kette; der Helfer nennt Alva. Runa ist Selmas Schwester; er nennt Enno und bestätigt diese falsche Rollenbehauptung. Aus einer Bitte um die Uhr wird eine tatsächliche Übergabe beziehungsweise eine sicher nicht geschehene Übergabe. Aus Zeigen eines Amuletts wird Eigentum. Der behauptete Müdigkeitsgrund ist unbekannt, wird aber als widerlegt eingeordnet. C ergibt **eine falsche Freigabe und zwei falsche Beanstandungen unbekannter Angaben**. Unbekannt wird nicht nachträglich als richtiges Widerlegen gewertet.

B verändert im Becherfall die Wortfolge des angeblich wörtlichen Zitats; C/G übersetzen es sogar ins Englische. Inhaltlich ist der neue Ablageort richtig, aber die Quellenbindung ungültig. Formatverbesserung wird deshalb weder als Verständnisgewinn noch als Beweis einer neuen sprachlichen Gedächtnisabdeckung gezählt.

Die vorher festgelegte Schwelle von mindestens 11/12 inhaltlich richtigen Aufgaben je A/B/C ohne falsche Rollenfreigaben oder falsche Widerlegungen wird verfehlt. **Der vereinfachte Ansatz wird für diese beiden Kandidaten beendet.** Kein Vergleich erneut integrierter Helferstufen mit dem Chat und keine zusätzliche Produktionsfunktion. Eine andere spätere Modell-/Methodenwahl ist durch diese kleine Auswahl nicht beurteilt. [Unveränderte Abnahmeantworten](validation/helper-diagnosis-0.8.6/raw-acceptance-huihui-qwen3-4b.json), [unabhängige Einzelbewertungen](validation/helper-diagnosis-0.8.6/acceptance-independent-review.json), [Entscheidung](validation/helper-diagnosis-0.8.6/decision.json).

Ein punktueller zusätzlicher sprachlicher Beitrag ist sichtbar: Der Helfer versteht den ausdrücklich mit „da“ genannten Frostgrund richtig, während die spezielle Rollenregel für Gründe auf „weil … verletzte Hand … Siegel“ begrenzt ist. Dieser Einzelfall wird anerkannt, aber weder als neues bestätigtes Gedächtnis gespeichert noch als Vorteil gegenüber der Erzähl-KI ausgegeben. Der Schnittstellenprototyp prüft Herkunft, nicht die Wahrheit beliebiger freier Aussagen; die produktive Regelvalidierung würde dadurch nicht automatisch sprachlich breiter.

## Aufwand und Gerät

84 Entwicklungsaufrufe und 48 Abnahmeaufrufe, insgesamt **132 echte lokale Inferenzaufrufe**, jeweils frischer Kontext. Median am PC in der Abnahme: A **2,32 s**, B **3,97 s**, C **4,17 s**, G **5,28 s**. Gemessen ab dem Diagnoseaufruf einschließlich dessen Formatierung/Tokenzählung; initiales Modellladen, automatischer Abruf, Android/UI und Speicherumschaltungen sind nicht enthalten. Kurze Quellen sind schneller als der umfassende Auftrag aus 0.8.6, beweisen aber keinen Gesamtgewinn in der App.

Keine S24-/S24-Ultra-Verbindung über ADB. Handy-Wartezeit, Spitzen-RAM und Dauerlast bleiben **offen**. PC-Messwerte sind keine Smartphone-Leistungsangaben.

## Getrennter Produktionsfehler: „weiterhin“ als Eigentümer

Die vollständige ursprüngliche Antwort aus der zweiten echten Dialogrunde wurde erneut untersucht, einschließlich Erzählertext und gültigem Zustand. Sie sagt „Du hältst ihn jetzt. Er gehört weiterhin Oda.“ Der Nutzer hatte den Dolch gerade wieder genommen; Oda war manuell als Eigentümerin gespeichert. Der Erzählertext hat ein unklar verwendetes „sie“, aber keine abweichende Eigentümerfestlegung. Der Eigentümerkonflikt war nachweislich ein **Fehlalarm**: Der Parser meldete wörtlich „Eigentümer ist Oda, nicht weiterhin“.

Gezielte Korrektur in `RoleEvidence.kt`: `jetzt`, `nun`, `weiterhin` werden unmittelbar nach `gehört` als Zeitangaben behandelt; der tatsächliche folgende Eigentümer wird gelesen. Unvollständige Sätze erzeugen keinen Eigentümer aus dem Adverb. Andere Rollen-/Vergangenheits-/Wissensfilter bleiben unverändert. Gegensätzliche Aussagen mit `mir`, `dir` oder `Mira` werden weiterhin blockiert.

Der **unveränderte echte Entwurf** besteht jetzt den Schutzfilter. Ein zusätzlicher Test übernimmt ihn in eine Kopie der echten synthetischen SQLite-Vorlage aus 0.8.6, prüft atomare Speicherung, vollständige Originalnachrichten, weiterhin manuelle Eigentümerin Oda und Neustart. Das ist ein Parser-/Speicherungsnachweis mit einer alten echten Modellantwort, **keine neu verbesserte Modellgenerierung**. [Vorher](validation/helper-diagnosis-0.8.6/guard-before.json), [nachher](validation/helper-diagnosis-0.8.6/guard-after.json), [gespeicherter Stand](validation/helper-diagnosis-0.8.6/repaired-draft-stored.json).

## Lokale Lieferung 0.8.7

148 reguläre Tests und 100 relevante Integrationstests bestanden; 26 überschneiden sich. Ein weiterer SQLite-Produktionsnachweis und vier isolierte Bindungstests bestanden. Build/Lint bestanden: **0 Fehler, 133 Warnungen, 1 Information**. Datenbankschema 10 und Suchindex 1 bleiben gleich. Figuren, Chats, Modelle, bestehender Such-/Gedächtnisablauf und Nutzer-Modellauswahl erhalten. Kein Training, Download oder Veröffentlichung.

`Geschichten-0.8.7.apk`, code21, Paket `dev.vincent.geschichten`, gleiche kompatible Testsignatur, APK-v2 und 16-KiB-Ausrichtung geprüft. SHA-256 **`fd15af5bb3928674a4b965ab0d773dceee4bd5f84068b45fb9663da283873071`**. Als Update installieren, die alte App vorher nicht deinstallieren. Die APK enthält ausschließlich die gezielte Produktionsreparatur; Diagnosebrücke, Testfälle und Modellgewichte werden nicht mitgeliefert.

Vollständige geänderte Dateiliste, Quell-/Artefaktprüfsummen, erhaltene 0.8.6-Rohdaten, native Tokenprüfung und Signatur: [Lieferprüfung](validation/helper-diagnosis-0.8.6/delivery-verification.json). Die Zusatz-KI bleibt standardmäßig aus; die bessere Syntax rechtfertigt ihre Aktivierung nicht.
