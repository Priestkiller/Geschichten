# Prüfung von Gesprächskontext, Figurenwissen und Gedächtnis

Stand: 05.10.2026. Untersucht wurde der vorhandene lokale Quellstand **0.7.4, versionCode 13**, einschließlich seiner bereits vorhandenen Änderungen. Diese Prüfung veröffentlicht kein Update. Die fertige APK ist ein lokaler Debug-Build mit dem bisherigen Projektschlüssel.

## Ergebnis

Mehrere technische Ursachen sind **bestätigt und gezielt repariert**:

1. Die beiden Qwen-LiteRT-Dateien ließen alle früheren Figurenantworten beim Anwenden ihrer eingebetteten Vorlage weg. Die Kotlin-API liefert die Rolle `model`, die Vorlagen akzeptierten nur `assistant`. Das war erst in der tatsächlich formatierten Modelleingabe sichtbar.
2. Die Verlaufsauswahl konnte einen wichtigen Gegenstandswechsel nach drei Runden entfernen, obwohl die erste Zusammenfassung erst nach sechs Runden vorgesehen war.
3. Die Zusammenfassungseingabe schnitt einen wichtigen Nutzerfakt mitten im Text ab, während lange Figurenbeschreibungen einen großen Teil ihres Budgets erhielten.
4. Eine historische Start-Ortsnotiz wurde weiterhin als „Current location“ übergeben, obwohl der Verlauf bereits den Hof belegte.
5. Das Erreichen des Ausgabelimits wurde als erfolgreicher Abschluss behandelt. Eine fehlende Endmarkierung beziehungsweise der Laufzeitzähler wird jetzt geprüft, bevor eine Antwort gespeichert wird.
6. Ein absichtlich verspäteter Streaming-Callback konnte die Anzeige nach Abbruch beziehungsweise während einer neuen Anfrage verändern. Anfrageversion und Chat-ID grenzen ihn jetzt ab.

**Die Reparaturen ergeben noch kein zuverlässig widerspruchsfreies Erzählmodell.** Echte Antworten zeigen weiterhin falschen Gegenstandsbesitz, wiederaufgenommene erledigte Ziele, Rollenverwechslungen und fehlerhafte Zusammenfassungen. Ein vorhandener Unsloth-Adapter verbessert einige kurze Prüfdialoge, scheitert aber ebenfalls am langen Verlauf. Er ist in der App nicht eingebunden.

Validierung: **82 normale Tests erfolgreich**, zusätzlich **55 ausgewählte Integrationstests erfolgreich**; die Läufe überschneiden sich. Native Vorlagen, Tokenizer und Grenzfälle wurden mit allen sechs vorhandenen Katalogdateien geprüft. Ausführliche Antwortqualität wurde mit vier dieser Dateien sowie mit Basis/Adapter des Trainingspiloten untersucht. Ein S24 war nicht angeschlossen; Geräteprüfungen bleiben offen.

## Belege, Schutz des Ausgangsstands und Einstufungen

Der Ausgangsstand wurde vor den Reparaturen als `source-before.zip` und SHA-256-Liste gesichert. Keine zuvor vorhandene Quelldatei wurde gelöscht. Änderungen gegenüber genau diesem Stand liegen als einzelne `.diff`-Dateien vor. Die SQLite-Struktur, Katalogdateien, Figureninhalte und Modellgewichte wurden durch diese Reparaturen nicht geändert. Tests verwenden ausschließlich synthetische Daten in einer Testdatenbank. Auf private Handy-Chats wurde nicht zugegriffen.

Alle genannten Diagnoseartefakte liegen in:

`F:\Geschichte App KI\geschichten-android\docs\validation\chat-kontext-pruefung`

Wichtige Dateien: `inputs-before.json`, `inputs-final.json`, `template-before/`, `template-regression.json`, `rendered-regression.json`, `model-artifacts.json`, `litert-tokenizers.json`, `audit-evidence.json`, `final-results/`, `integration-results/`, `final-build.log`, `integration-final.log` sowie die Modellantworten `*-after.json` und `adapter-comparison.json`.

| Einstufung | Bedeutung |
|---|---|
| **Bestätigter Fehler** | Durch Quellpfad und reproduzierbaren technischen Test oder durch die aufgezeichnete echte Modellantwort belegt. Ein Modellfehler belegt diese Antwort, keine allgemeine Fehlerquote. |
| **Begründete Vermutung** | Ein konkreter Mechanismus ist im Code erkennbar, seine vermutete Auswirkung wurde nicht reproduziert. |
| **Ungeprüft** | Kein ausreichender Test oder kein verfügbares Zielgerät; keine Erfolgsaussage. |

Die erste eingeschränkte Baseline-Ausführung blieb beim nativen Build hängen. Nach dem gezielten Beenden des zu diesem Auftrag gehörenden Ninja-Prozesses bestand der Wiederholungslauf: `baseline-build-retry.log`, Build/Test/Lint erfolgreich. Die ursprünglichen 76 normalen Tests hatten keine Fehler. Vorhandene Warnungen sind unten getrennt aufgeführt.

## 1. Tatsächliche Architektur und vollständiger Nachrichtenweg

Native Android-App: Kotlin, Jetpack Compose, `AndroidViewModel`, Coroutines/StateFlow und `SQLiteOpenHelper`; Paket `dev.vincent.geschichten`, minSdk 31, target/compileSdk 35, ARM64. AGP 8.9.2, Gradle 8.11.1, Kotlin 2.4.0; lokales JDK 21 mit JVM-Ziel 17.

Der Inferenzanbieter `LocalModelEngine` verwendet entweder **LiteRT-LM Android 0.17.1** oder den eingebundenen **llama.cpp-Commit `d2e54583c7452353eb35d40431281f6ee984332f`** über JNI. Die Handy-Ausführung bleibt lokal. Netzwerkzugriffe betreffen Downloads/Updates; der untersuchte Antwortpfad sendet keine Chats an einen Online-Anbieter.

Der tatsächliche Ablauf:

1. `AppViewModel.sendMessage()` nimmt den getrimmten Entwurf entgegen und prüft Bereitschaft, Länge und laufende Arbeit. Ein zweiter Sendeversuch während der Generierung wird blockiert.
2. Die Eingabe wird zunächst dauerhaft als Entwurf gesichert, anschließend als `USER` mit `storyId` in SQLite gespeichert. Ein zuvor unbeantworteter Versuch wird kontrolliert ersetzt.
3. `StoryRepository.bundle(storyId)` liest Geschichte, zugehöriges Figurenprofil, Nachrichten und Erinnerungen. Nachrichten werden mit `created_at ASC, rowid ASC` gelesen; neue Nachrichten erhalten monoton steigende Zeitstempel.
4. Vor der Antwort prüft `StorySummaryPlan` jetzt, ob noch nicht zusammengefasste abgeschlossene Runden aus dem Kontext fallen würden. Falls nötig wird zuerst lokal zusammengefasst. Danach wird das Bundle neu aus SQLite gelesen.
5. `StoryPrompt.system()` wählt Figuren-/Szenenwissen aus; `StoryPrompt.window()` wählt einen begrenzten Verlauf. Die aktuelle Nutzernachricht bleibt genau einmal enthalten.
6. LiteRT erhält `systemInstruction`, frühere Nachrichten als strukturierte `initialMessages` und die letzte Nutzernachricht separat in `sendMessageAsync`. GGUF erhält Rollen/Text getrennt und formatiert sie genau einmal in der nativen Laufzeit.
7. Streaming steht vorübergehend in `partialReply`. Die Oberfläche zeigt den vollständigen gespeicherten Verlauf und diesen separaten Antwortpuffer; sie zeigt deshalb mehr Verlauf als der wirksame Modellkontext.
8. Nach vollständigem Laufzeitabschluss und Formatprüfung wird die Antwort als `CHARACTER` unter der ursprünglich erfassten `storyId` gespeichert. Erst danach folgt gegebenenfalls die periodische Zusammenfassung.
9. Bei Fehler oder Abbruch vor dem Antwort-Commit wird kein Fragment als normale Antwort gespeichert. Die Eingabe wird als dauerhafter Entwurf wiederhergestellt. War die Antwort bereits gespeichert, meldet die App stattdessen den Fehler der nachfolgenden Zusammenfassung.

Zuständige Dateien und Funktionen:

| Datei | Zuständigkeit |
|---|---|
| [AppViewModel.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/AppViewModel.kt>) | `sendMessage`, `maybeSummarize`, `recoverSafely`, `selectStory`, `stopGeneration`, Modell-/Erinnerungswechsel |
| [StoryRepository.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/data/StoryRepository.kt>) | `bundle`, `appendMessage`, `createStory`, `upsertMemory`, `updateSummary`, Transaktionen und Migrationen |
| [StoryPrompt.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/StoryPrompt.kt>) | `system`, `encodedContext`, `window`, Kürzung und Auswahl von Notizen |
| [StorySummaryPrompt.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/StorySummaryPrompt.kt>) | Quellenfenster, sachliche Zusammenfassung, `checkedSummary` |
| [StorySummaryPlan.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/StorySummaryPlan.kt>) | Frühe/periodische Zusammenfassungsintervalle |
| [LocalModelEngine.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/LocalModelEngine.kt>) | Auswahl/Laden, LiteRT-Konfiguration, Streaming, Abbruch, Mutex |
| [LiteRtStoryTemplate.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/LiteRtStoryTemplate.kt>) | Gezielte Qwen-Rollenkompatibilität |
| [GgufEngine.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/GgufEngine.kt>) und [gguf-jni.cpp](<F:/Geschichte App KI/geschichten-android/app/src/main/cpp/gguf-jni.cpp>) | GGUF-Vorlage/Tokenizer, Kontextgrenze, Cache, Sampling, Endtoken |
| [StoryOutputLimit.kt](<F:/Geschichte App KI/geschichten-android/app/src/main/java/dev/vincent/geschichten/ai/StoryOutputLimit.kt>) | Vorsichtige Ablehnung von LiteRT-Antworten am Ausgabelimit |

`StoryGeneration` ist eine kleine injizierbare Schnittstelle für die technischen ViewModel-Tests. Die normale App verwendet unverändert den echten lokalen Anbieter. Die Testimplementierung ist nicht Bestandteil der APK.

## 2. Speicherung, Zuordnung und Wiederherstellung

**Geprüfte Architektur, kein bestätigter Zuordnungsfehler in den ausgeführten Tests:** Die Datenbank heißt `geschichten.db`, Version 8. `characters` enthält Profile, `stories` referenziert `character_id`, `messages` und `memories` referenzieren `story_id`. Fremdschlüssel und transaktionale Repository-Methoden sichern diese Beziehungen. Die Repository-Methoden sind synchronisiert; ein Bundle wird in einem konsistenten Lesevorgang erstellt.

Die Ausgangslage `stories.start_context` wird beim Erstellen eingefroren. Das Figurenprofil wird beim Lesen erneut geladen; eine spätere Profiländerung kann deshalb in einem bestehenden Chat wirksam werden, ohne die alte Ausgangslage umzuschreiben. `stories.summary` enthält die wirksame Zusammenfassung. Die sichtbare automatische Erinnerung trägt die ID `auto-summary-<storyId>`; sie wird nicht zusätzlich als normale Notiz in den Prompt kopiert.

Erinnerungen haben Typ `FACT`, `LOCATION`, `GOAL` oder `EVENT`. Alle werden auf die aktuelle Geschichte eingeschränkt. Auswahl: neueste Ortsnotiz, angeheftete Notizen, danach weitere Notizen nach Wortüberschneidung mit der letzten Eingabe und Aktualität. Es gibt keine Vektorsuche, keine gemeinsame weltweite Wissensdatenbank und keinen chatübergreifenden Nutzer-Speicher.

SharedPreferences `preferences` enthalten Entwürfe und `summary_turns_<storyId>`; `model_selection` enthält Modellwahl und Leistungsparameter. Gewichte liegen auf Android unter `noBackupFilesDir/models`, Laufzeit-Dateicaches unter `cacheDir/litertlm`. Ein Dateicache für Gewichte ist kein Gesprächsgedächtnis.

**G, technisch bestanden:** Eine gespeicherte beantwortete Runde bleibt bei neuer ViewModel-/Repository-Instanz erhalten; eine unbeantwortete Eingabe wird zum Entwurf zurückgeholt. Der Wechsel von Mira zu Grask überträgt weder Rian-Nachrichten noch den Entwurf. Datenbanktests prüfen außerdem Löschen des Verlaufs mit Kaskaden sowie die Migrationen bestehender Daten. Diese Tests laufen mit Robolectric/SQLite, nicht auf einem echten Android-Prozess.

**Begründete Vermutung:** `upsertMemory`, `updateSummary` und der Preferences-Checkpoint sind jeweils gesichert, bilden zusammen jedoch keine einzige atomare Operation. Ein Prozessabbruch zwischen ihnen könnte eine zeitweise abweichende sichtbare Erinnerung/Zusammenfassung erzeugen oder ein Intervall erneut zusammenfassen lassen. Ein solcher Fehlerzeitpunkt wurde nicht injiziert. Eine Datenverlustbehauptung wäre daher unbelegt.

**Ungeprüft:** Echter Android-Prozesskill, Hintergrund/Vordergrund, Speicherdruck, App-Neustart auf S24 und tatsächliche Gerätepräferenzen. Die Screenshots früherer Versionen beweisen nicht, welche Datei auf dem Handy heute geladen ist.

## 3. Modelldateien, Quantisierung, Adapter und wirksame Einstellungen

Alle sechs lokal verfügbaren Testdateien wurden vollständig gehasht und stimmen in Größe/SHA-256 mit dem Katalog überein. Die vollständigen Repository-Revisionen und Prüfsummen stehen in `catalog-final.json` und `model-artifacts.json`.

| Katalogmodell / tatsächliche Testdatei | Format / belegte Quantisierung | Kontext laut GGUF-Metadaten | Aktiv in der App |
|---|---|---:|---:|
| Gemma 4 E2B — `gemma-4-E2B-it.litertlm` | LiteRT; Quantisierung nicht vollständig tensorweise ausgelesen | Für dieses Artefakt nicht als Modell-Maximum bestimmt | 4.096 |
| Qwen 2.5 1.5B — `Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm` | LiteRT; Exportname bezeichnet q8; keine vollständige Tensorprüfung | Nicht aus diesem Export bestimmt | 4.096 |
| Qwen 3 0.6B — `Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm` | LiteRT; Exportname bezeichnet 4-Bit-Gewichte/FP32-Aktivierungen; keine vollständige Tensorprüfung | Exportmetadaten: 4.096 | 4.096 |
| Dolphin 3.0 Llama 3.2 3B — `Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf` | Tatsächliche Tensoren: Q4_K, Q6_K, F32 | 131.072 | 4.096 |
| Huihui Qwen 3 4B Instruct 2507 — `huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf` | Tatsächliche Tensoren: Q4_K, Q6_K, F32 | 262.144 | 4.096 |
| Gemma 3 DavidAU — `Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf` | Tatsächliche Tensoren: Q4_0, ein Q6_K-Tensor, F32 | 131.072 | 4.096 |

Die großen GGUF-Metadatenwerte sind nicht das aktivierte Handy-Fenster. Bei den ersten beiden LiteRT-Dateien fehlt in den ausgelesenen LLM-Metadaten ein gesetzter `max_num_tokens`-Wert; daraus wird kein Modell-Maximum erfunden. Die App übergibt ausdrücklich 4.096 an `EngineConfig`. Native Überlauftests bestätigen diese Laufzeitgrenze.

Standardmodell im Quellstand ist Gemma 4 E2B. Unbekannte gespeicherte Modell-IDs werden über `LocalModelCatalog.restored()` zum Standard zurückgeführt; das ist kein automatischer Qualitätswechsel während eines Chats. Die ausgewählte Datei wird beim Laden geprüft. CPU ist der normale LiteRT-Pfad, GPU nur ein möglicher Rückfall nach fehlgeschlagener CPU-Initialisierung. Ein OOM oder Generierungsfehler bewirkt keinen stillen Wechsel zu einem anderen Modell.

Wirksame Samplingwerte: Temperatur **0,75**, top-k **40**, top-p **0,9**, Wiederholungsstrafe **1,08**, Fenster **256**. GGUF: zusätzliche Frequenz-/Präsenzstrafe 0, Sampler wird nach dem Prefill angelegt. LiteRT: Thinking deaktiviert, Budget 0, keine automatische Tool-Ausführung. Alle Katalogmodelle haben **512 Ausgabetokens**. Normale App-Seeds sind zufällig; die LiteRT-Proben verwenden Seed 42. Die CPU-Threadzahl ist gespeichert pro Modell/Gerätekonfiguration, gültig 1–6, sonst 4. GGUF Prefill-Batches: 256, Microbatch: 128. Angezeigte Threads/Backend stammen aus dem Laufzeitzustand; die Übergabe ist im Ladepfad nachvollzogen. Gerätemessungen wurden nicht behauptet.

**Adapterprüfung:** Die App übergibt keine `LoraConfig` und der GGUF-Ladepfad lädt keinen separaten Adapter. Keine der sechs katalogisierten Dateien ist der lokale Geschichten-Pilotexport. Änderungen des öffentlichen Modellanbieters sind nicht mit unserem Unsloth-Adapter gleichzusetzen.

Der vorhandene Pilot liegt unter `training/geschichten-pilot-v1/runs/geschichten-qwen3-4b-v1/adapter`. Basis: `unsloth/Qwen3-4B-Instruct-2507-unsloth-bnb-4bit`, Revision `7744afa8566e264af1a92a806d8d9aae00cc7c78`; LoRA r=16, alpha=32, sieben Projektionsmodulgruppen. Adapter: 132.187.888 Bytes, SHA-256 `07e9a6314fa42e4457df5adce34f1d85e57fe63a6471243bbd2295e1cd03194b`. Der Hash entspricht dem vorhandenen `artifact-report.json`; der Adapter wurde nicht geändert.

**2.048 Tokens gehören zur Trainingssequenzlänge dieses Piloten.** Die neue PC-Evaluation aktiviert 4.096, und die App aktiviert ebenfalls 4.096. Ein trainierter GGUF-Merge/Android-Export ist für diesen Adapter nicht vorhanden und wurde nicht neu erstellt. Eine doppelte Adapteranwendung in der App ist daher nicht belegt; aktuell fehlt seine Integration vollständig.

## 4. Tatsächliche Vorlagen, Rollen, Spezialtoken und Reparatur

**Bestätigter Fehler — Qwen-LiteRT:** Die korrekt gespeicherten `CHARACTER`-Nachrichten waren zwar in `initialMessages`, fehlten aber im fertigen Prompt. In A fehlte die eine Figuren-Eröffnung; in E fehlten alle fünf Figurennachrichten. Die Nutzertexte und Systemregeln waren vorhanden. Beleg: `template-before/qwen25-tokens.json`, `template-before/qwen06-tokens.json` sowie die tatsächlich aus den Dateien extrahierten Jinja-Vorlagen in `litert-tokenizers.json`.

Reparatur: `LiteRtStoryTemplate.create()` verwendet für genau diese beiden festgelegten Modell-IDs eine Text-ChatML-Vorlage, die `model` auf `assistant` abbildet. Texte und Rollen bleiben strukturiert; das Template wird weiterhin genau einmal in LiteRT angewendet. Für Qwen 0.6B wird am Generierungsbeginn der leere Thinking-Block für den deaktivierten Denkmodus gesetzt. Für Gemma bleibt die eingebettete Vorlage aktiv. Gewichte, Dateien und Prüfsummen werden nicht verändert.

Die öffentliche experimentelle Option `overwritePromptTemplate` wird nur synchron um `createConversation` gesetzt und im `finally` auf den vorherigen Wert zurückgesetzt. Die App serialisiert Inferenzoperationen zusätzlich. Ein neuer Test prüft die Wiederherstellung auch bei fehlgeschlagener Erstellung. Die Option wird in der festgelegten [LiteRT-LM-Engine 0.17.1](https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.17.1/kotlin/java/com/google/ai/edge/litertlm/Engine.kt) beim Erstellen der Conversation gelesen. Bei einem späteren Laufzeit-Upgrade muss dieser Kompatibilitätspfad erneut geprüft werden.

**Nachher belegt:** Alle ausgewählten System-/Verlaufstexte erscheinen in A/E bei allen sechs Dateien in richtiger Reihenfolge; die neueste Nutzernachricht erscheint einmal. Bei beiden reparierten Qwen-Dateien wurde zusätzlich jede vollständige User-/Assistant-Nachricht mitsamt Markern genau einmal geprüft. `check_rendered_inputs.py` schlägt fehl, wenn das wieder verloren geht.

GGUF verwendet die eingebettete Vorlage. Huihui enthält keine `tokenizer.chat_template`, daher den im Katalog festgelegten ChatML-Fallback. Der verwendete llama.cpp-Formatter rendert Dolphin als ChatML, Gemma 3 mit `user/model`-Turnmarkern. Trotz des Jinja-BOS-Hinweises in Gemma 3 zeigen die tatsächlich tokenisierten Eingaben nur einen BOS am Anfang: Gemma 3 Token 2; Dolphin Token 128000. Huihui hat `add_bos=false` und beginnt direkt mit Token 151644. Die Generierung beginnt jeweils mit der richtigen Assistant-/Model-Präfixrolle.

LiteRT-Tokenizer wurden aus den tatsächlichen Dateien gelesen: Gemma 4 SentencePiece, beide Qwen-Dateien eingebettetes Hugging-Face-Tokenizer-JSON. Gemma 4 fügt automatisch Starttoken 2 hinzu; Qwen 2.5 fügt `<|endoftext|>` hinzu; Qwen 0.6B besitzt keinen zusätzlichen Starttoken. Vollständig formatierter Text plus diese Starttokens stimmt in A/E exakt mit den nativen Prefill-Zählern überein. Gemma-4-Stop-IDs: 1, 50, 106; Qwen: `<|im_end|>`. GGUF entscheidet über `llama_vocab_is_eog`.

Diese automatischen Start-/Stop-Tokens sind in den exportierten [LLM-Metadaten](https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.17.1/runtime/proto/llm_metadata.proto) definiert. Einzelne Inhaltszählungen dürfen deshalb nicht als vollständige Promptlänge behandelt werden.

**Kein bestätigter Fehler** für doppelte Vorlagenanwendung oder doppelte BOS-Tokens in den geprüften Fällen. Das beweist nicht die Kompatibilität beliebiger zusätzlich importierter Modelle; die App prüft hier ihre sechs festgelegten Dateien.

## 5. Kontextbudget und nachvollziehbare Kürzungen

Vier verschiedene Größen:

| Größe | Tatsächlicher Wert / Aussage |
|---|---|
| Trainingslänge Geschichten-Pilot | 2.048 Tokens |
| Modellseitige Metadaten | Teilweise deutlich größer, siehe Modellübersicht; nicht automatisch Handy-Limit |
| Aktiviertes Laufzeitfenster | 4.096 Tokens bei allen sechs Modellen |
| Antwortlimit | 512 Tokens; bewusst begrenzte Leistungsproben verwenden intern weniger |

`StoryPrompt` verwendet zusätzlich **Zeichenbudgets**, ausdrücklich keine Token-Garantie: System höchstens 3.600 Zeichen, Verlauf höchstens 4.000 Zeichen, maximal 14 gespeicherte Nachrichten, Nutzerbeitrag höchstens 1.000 Zeichen. Gegebenenfalls kommt eine synthetische User-Aufforderung vor einer Figuren-Eröffnung hinzu. Deren Platz wird vorab im Verlauf reserviert.

Die Auswahl nimmt einen zusammenhängenden Suffix vollständiger User-/Figurenrunden. Passt die nächste ältere Runde nicht, wird davor beendet; es werden keine zeitlich unzusammenhängenden älteren Runden hineinübersprungen. Eine ungewöhnlich lange Figureneröffnung bleibt vollständig in SQLite/UI, wird für die Inferenz gegebenenfalls auf einen Schlussteil bis 900 Zeichen reduziert. Ein einzelner sehr langer Eröffnungsabsatz kann mitten im Absatz beginnen. Andere zu lange ältere Runden werden ganz weggelassen. Normale Nutzerbeiträge über 1.000 Zeichen werden abgelehnt, nicht still abgeschnitten.

Systemfelder werden separat begrenzt: Persönlichkeit bis 650, Rolle 80, Eigenschaften 90, Welt 40, Ausgangslage 650, Ortsnotiz 200, Zusammenfassung 600, angeheftete Notizen gemeinsames Budget 360 und bis zu vier ausgewählte Notizen, weitere Notizen 140 Zeichen. Restbudget/JSON-Escaping kann die Felder zusätzlich verkleinern. Die 90 ausführlichen sichtbaren Profile/Einführungen werden deshalb nicht vollständig in jedes Modelleingabefenster übernommen.

Neue lokale Diagnosefunktion `StoryPrompt.window()` liefert `selectedMessageIds`, `omittedMessageIds`, `shortenedMessageIds` und `stopReason` (`character-budget`/`message-budget`). Sie verändert den gespeicherten Verlauf nicht und schreibt keine Chattexte automatisch in Logs. Die ausführlichen Dateien dieses Auftrags stammen aus dem synthetischen Host-Diagnosepfad; ein neuer Diagnosebildschirm wurde nicht eingebaut.

### Exakte Eingabelängen einschließlich nativer Formatierung

| Modell | A: kurzer Dialog | E: Hof / Schlüssel / Hand | E + reservierte 512 Antworttokens |
|---|---:|---:|---:|
| Dolphin GGUF | 534 | 752 | 1.264 |
| Gemma 3 GGUF | 500 | 693 | 1.205 |
| Huihui GGUF | 535 | 753 | 1.265 |
| Gemma 4 LiteRT | 504 | 697 | 1.209 |
| Qwen 2.5 LiteRT, repariert | 536 | 754 | 1.266 |
| Qwen 0.6B LiteRT, repariert | 539 | 757 | 1.269 |

GGUF prüft ausdrücklich `inputTokens + maxOutputTokens <= 4096`; bei 512 bleiben maximal 3.584 Eingabetokens. LiteRT wird auf 4.096 Gesamtfenster und 512 neue Tokens konfiguriert; der native Eingabeüberlauf ist nachgewiesen. Eine separate Prüfung der App vor dem Prefill mit exakt reservierten 512 Tokens existiert dort nicht. Die letzte Spalte ist das konservative Planungsbudget für diese Testeingaben, keine Behauptung einer zusätzlichen LiteRT-Appprüfung.

Getrennte Inhaltszählung in E, jeweils **System / früherer Verlauf / letzte Nachricht**, ohne zusätzliche Rollenmarker: Dolphin 413/251/24; Gemma 3 und Gemma 4 402/210/21; Huihui und beide Qwen-LiteRT 414/252/24. Diese unabhängig tokenisierten Stücke sind nicht allgemein additiv. Die verbindlichen Gesamtlängen stehen in der Tabelle.

Beispiel Gemma 4, einzelne Systembestandteile: Regeln 251 Tokens, Persönlichkeit 42, Rolle 10, Eigenschaften 12, Welt 4, Ausgangslage 55, Ortsnotiz 22. Eine Zusammenfassung und andere Erinnerungen fehlen in E absichtlich; im langen Prüffall kommen sie hinzu. Die entsprechenden feldweisen Zählungen stehen in `litert-tokenizers.json` und den drei `*-tokens-cache.json`; dort ist jede benutzte Datei mit ihrem passenden Tokenizer gemessen.

**Bestätigter Fehler/Grenze, weiterhin offen:** Zeichenbudgets garantieren keinen Platz im Tokenfenster. Der gezielte Stressfall mit 2.000 seltenen Schriftzeichen im System und 3.800 Verlaufzeichen passt in die üblichen Zeichenlimits, überschreitet aber das native Fenster. Alle sechs Dateien melden Überlauf. LiteRT gemessen: Gemma 4 **7.362**, Qwen 2.5 **4.862**, Qwen 0.6B **4.865** Eingabetokens. Kein stiller Erfolg/Verlaufverlust wurde in diesem Fall beobachtet. Die normale Fehlerrückholung bleibt wirksam.

**Ungeprüft:** LiteRT genau am Eingabe-/Ausgabe-Gesamtfensterrand, insbesondere ein Abschluss wegen vollem KV-Fenster vor 512 Ausgabetokens. Der neue Ausgabelimit-Guard ersetzt keine vollständige tokenbasierte Eingabeplanung.

## 6. Historienverlust und Zusammenfassung: Vorher/Nachher

**Bestätigter Fehler — zu später Zeitpunkt:** `B-before-summary` enthält Eröffnung, drei abgeschlossene Runden und die aktuelle Frage. Der Schlüsselwechsel liegt in Runde 1, gefolgt von zwei langen Wanddialogen. Die gespeicherten acht Nachrichten bleiben vorhanden; im ausgewählten Fenster fehlen der Wechsel und die erste Antwort. Der ursprüngliche Sechs-Runden-Zeitplan würde noch keine Zusammenfassung erzeugen. Das Modell sieht deshalb die alte Ausgangslage „Mira trägt den Schlüssel“ ohne deren Korrektur.

Nachher erkennt `StorySummaryPlan.nextCheckpoint(..., beforeReply=true)` die weggelassene, noch nicht erfasste Runde. Checkpoint 3 wird vor der Antwort erstellt. Der Integrationstest belegt die Reihenfolge Zusammenfassung → Speicherung → erneutes Bundle → eigentliche Generierung. Die eigentliche Modelleingabe enthält die frisch gespeicherte Zusammenfassung; die aktuelle Nutzernachricht wird nicht doppelt übergeben. Bei größerem Rückstand werden geordnete Intervalle von höchstens sechs abgeschlossenen Runden abgearbeitet. Die laufende unbeantwortete Nachricht zählt nicht als abgeschlossene Runde. Jede neue Verdichtung bekommt nur die noch nicht verarbeiteten Runden nach dem vorherigen Checkpoint, zusätzlich zur bisherigen Zusammenfassung. Der Test mit Checkpoint 3 und neuer Runde 4 prüft ausdrücklich, dass Runde 1–3 nicht erneut als neue Quellen erscheinen.

**Bestätigter Fehler — Quelle mitten im Nutzertext verloren:** `summary-middle` legt den eindeutigen Schlüssel-in-Truhe-Fakt mitten in einen Nutzerbeitrag, daneben sechs lange Figurenantworten. Vorher fehlte genau dieser Fakt im tatsächlichen Zusammenfassungsprompt. Nachher erhalten die Nutzertexte Vorrang, wenn sie gemeinsam passen; erst dann wird der übrige Platz an Figurenantworten verteilt. Der gezielte Test zeigt den Fakt jetzt unverändert in der Quelle.

Die Zusammenfassung bleibt begrenzt: Quellenfenster 4.200 Zeichen, bis sechs abgeschlossene Runden; vorhandene Zusammenfassung bis 600, sonst historische Ausgangslage bis 650 Zeichen. Wenn die Nutzertexte gemeinsam nicht passen, erhalten auch sie proportional markierte Ausschnitte. **Eine vollständige Sicherung aller Fakten beliebig langer Runden ist damit weiterhin nicht belegt.** Wiederholte Zusammenfassungen können Fakten verlieren.

**Bestätigter Fehler — abgeschnittener Zusammenfassungstext:** Vorher wurde die erzeugte Zusammenfassung mit `take(500)` still abgeschnitten. Jetzt verwirft `checkedSummary()` leere/zu lange Texte und interne Formatlecks; die bisherige Zusammenfassung bleibt erhalten. Das bedeutet mehr sichtbare Fehler statt still beschädigter Erinnerung. Eine korrekte, aber über 500 Zeichen lange Zusammenfassung wird ebenfalls nicht gespeichert.

**Bestätigte verbleibende Modellschwäche:** Korrekte Quellen garantieren keine korrekte Zusammenfassung. Gemma 4 und Huihui erzeugten trotz des erhaltenen Truhen-Fakts gleichzeitig den alten Gürtelbesitz. Qwen 0.6B geriet beim frühen Zusammenfassen in eine Schleife bis 512 Tokens. Der letzte Fall würde vom neuen Produktionsguard abgelehnt; kurze semantisch falsche Zusammenfassungen bestehen den Formatguard weiterhin.

**Geprüftes bestehendes Verhalten mit Grenze:** Wird die automatische Zusammenfassungsnotiz manuell angeheftet, stoppt ihre automatische Ersetzung. Das schützt die Korrektur, stoppt aber auch die weitere automatische Fortschreibung. Die neue frühe Zusammenfassung hebt diesen Schutz nicht auf. Eine Trennung von geschützter Faktkorrektur und fortlaufender Zusammenfassung ist als Folgeschritt sinnvoll.

## 7. Figurenwissen, Szene und Widerspruchsquellen

**Bestätigter Fehler — Startort als Gegenwart:** In E belegt der Verlauf den gemeinsamen Gang in den Hof, die anfänglich erzeugte LOCATION-Notiz lautet aber noch „Startvorgabe: Im verschlossenen Turmzimmer“. Vorher wurde sie mit `Current location` ausgezeichnet. Nachher lautet die Beschriftung `Location note (recent conversation takes priority)`. Ausgangslage und frühere Zusammenfassung werden ebenfalls historisch mit Vorrang späterer Handlungen/Korrekturen gekennzeichnet.

Zusätzliche Regeln erhalten unveränderte Verletzungen/Beziehungen/Gegenstände, unterscheiden Vermutungen/Fragen von Ereignissen und verbieten die Behauptung einer unbekannten früheren Erinnerung. Die Regeln sind in allen Modellpfaden identisch. Es wurde keine starre Antwort für Mira/Grask eingebaut.

Eine Korrekturregel allein ersetzt noch keinen Zustand: Die historische Ausgangslage bleibt als Herkunft der Szene im Prompt. Es gibt keinen gesonderten, aus Nachrichten belegten aktuellen Zustandsdatensatz für `Schlüssel.besitzer`, `Schlüssel.ort`, `Ort`, `Verletzung` und `Ziel.status`. Auch eine manuelle GOAL-/LOCATION-Notiz wird nicht automatisch durch die Nutzeraktion umgeschrieben. Modellantworten können daher ältere Angaben erneut aufgreifen.

**Bestätigte Modellfehler mit bekannter Quelle:** In B-long ist die ursprüngliche Bronzeschlüssel-/Turm-Ausgangslage noch historisch enthalten, daneben eine ausdrücklich korrekte Zusammenfassung „silberner Schlüssel bei Rian / Hof / Ziel erledigt“. Huihui kehrt zum alten Besitz und Ziel zurück; der Adapter erfindet zwei Schlüssel. Die Herkunft der konkurrierenden Angaben ist damit nachvollziehbar. Ob genau eine bestimmte Promptstelle die Ausgabe kausal bestimmt, wäre ohne weitere Ablationsprüfung nur eine Vermutung.

Die gespeicherten Nachrichten tragen eindeutige IDs; eine Zusammenfassung speichert keine einzelnen Quellen-IDs/Faktversionen. **Bestätigte Architekturgrenze:** Eine einmal gespeicherte falsche Verdichtung lässt sich derzeit nicht automatisch durch Quellenvergleich erkennen. Ein Rückgriff auf das vollständige SQLite-Archiv wäre möglich, wird jedoch vom normalen Promptpfad nicht durchgeführt.

**Ungeprüft:** Vollständige semantische Bewertung aller 90 Figuren, gezielte hypothetische Ereignisfolgen und alle festgelegten Beziehungen. Die bestehenden Figuren-/Prompttests prüfen die Zusammenstellung und Budgets, nicht 90 echte Erzählverläufe. Erlaubte neue kreative Details sind nicht automatisch ein Fehler; F prüft ausdrücklich die Behauptung über eine angeblich bereits besprochene Vergangenheit.

## 8. KV-Cache, Streaming, Abbruch und Änderungen

**GGUF, geprüft:** Der Cache enthält einen tatsächlich tokenisierten Promptpräfix. Wiederverwendung nur bei identischen Token-IDs, auf 256er Prefill-Grenzen abgerundet; native KV-Positionen werden überprüft und die überholte Fortsetzung entfernt. Die vollständige gültige Eingabe kann erneut als Text übergeben werden, ohne jedes Token neu zu berechnen. Abbruch, Fehler, Kontext-/Modellwechsel und Löschen des Verlaufs leeren beziehungsweise ersetzen den Cache.

Für alle drei GGUF-Dateien wurden Prefill-Logits mit wiederverwendetem Cache gegen frischen Kontext verglichen: gleicher Prompt, neue Zusammenfassung, entfernter Verlauf, geänderte letzte Nachricht und geändertes Figurenprofil. Die maximale erlaubte Abweichung betrug 0,05; alle Vergleiche bestanden. Nach absichtlichem Fehler am Ausgabelimit und Abbruch war bei erneutem Versuch kein alter Präfix wiederverwendet. Beleg: `*-tokens-cache.json`. Diese Prüfung ist technisch aussagekräftiger als der Vergleich zweier zufällig gesampelter Texte.

**LiteRT, geprüft:** Für jede Antwort entsteht eine neue Conversation aus dem aktuellen Bundle. A/E begannen mit KV-Zähler 0. Es gibt daher keinen im normalen Pfad weitergeführten alten Gesprächs-KV-Cache. Die Conversation wird bei Abbruch nativ gestoppt; der Code wartet auf den terminalen Callback, bevor Ressourcen geschlossen werden. Der Operation-Mutex verhindert gleichzeitiges Entladen/Generieren.

**Bestätigter Fehler — verspätete Anzeige:** Der technische Test sendete nach Abbruch absichtlich einen alten Callback, anschließend nochmals während einer neuen Generierung in einem anderen Chat. Vor der Reparatur scheiterte er (`late-event-before-guard.xml`). Jetzt erhöht `generationRevision` bei Start/Stop die Anfrageversion; der Callback muss diese Version, `busy` und die aktuelle `storyId` erfüllen. Der Regressionstest besteht. Das ist ein nachgewiesener Fehler an der ViewModel-Grenze, kein Nachweis eines bereits geschehenen falschen SQLite-Commits auf dem Handy.

**H, technisch bestanden:** Doppelsenden, blockierter Wechsel während laufender Antwort, Abbruch, Entwurfrückholung, spätes Fragment und neuer Versuch. Teilantworten werden weder in den Verlauf noch in die Zusammenfassung übernommen. Die Speicherung einer vollständigen Antwort verwendet die erfasste Chat-ID.

**Ungeprüft / keine UI-Funktion:** Nachrichtenbearbeitung und freie Regeneration einer bereits gespeicherten Antwort werden aktuell nicht angeboten. Die native Cacheprüfung mit verändertem/entferntem Input belegt die Cachemechanik, keine Bedienfunktion für Bearbeiten/Regenerieren. Ein echter Modellwechsel oder Lifecycle-Stresstest auf S24 wurde nicht durchgeführt.

## 9. Unvollständige Antworten am Ausgabelimit

**Vorher bestätigt:** Der echte GGUF-JNI-Pfad und LiteRT 0.17.1 lieferten bei einem künstlichen Limit von einem Texttoken einen Erfolgsabschluss für Fragmente wie `*`, `„` oder `*M`. Ein Erfolgs-Callback allein bedeutete keinen vollständigen Text. Die damalige Formatprüfung konnte solche Fragmente nicht zuverlässig als abgeschnitten erkennen. Belege: `gguf-before.json`, `litert-before.json`.

GGUF verfolgt jetzt, ob ein tatsächliches EOG-Token erreicht wurde. Ohne EOG am erlaubten Ausgabelimit wird ein Fehler zurückgegeben und der Cache geleert. Bewusst acht Token lange Leistungsproben dürfen das Limit erreichen; normale Antworten/Zusammenfassungen nicht.

LiteRT verwendet aktivierte numerische Benchmarkzähler. Erreicht die erzeugte Antwort 512 Tokens, wird sie vorsichtig als möglicherweise unvollständig abgelehnt. Die Kotlin-Schnittstelle liefert hier keinen zuverlässigen Endgrund/EOS-Indikator. **Grenze der Reparatur:** Ein tatsächlich vollständiger Abschluss genau bei 512 kann ebenfalls abgelehnt werden. Umgekehrt ist ein Abbruch wegen des Gesamtfensterrandes vor 512 noch nicht gesondert geprüft. Die [Conversation-API](https://raw.githubusercontent.com/google-ai-edge/LiteRT-LM/v0.17.1/kotlin/java/com/google/ai/edge/litertlm/Conversation.kt) stellt hierfür die gemessenen Zähler bereit.

Der Integrationstest erzeugt ein Fragment und anschließend einen Limitfehler: kein CHARACTER-Commit, keine neue Zusammenfassung, leere Teilanzeige, exakte Eingabe wieder als Entwurf. Zusätzlich erzeugte Qwen 0.6B in der echten B-early-Probe 512 repetitive Tokens; das ist ein praktischer Anwendungsfall dieser Sperre. Die Rohdiagnose speichert den Kandidaten als Testbeleg, die App würde ihn ablehnen.

## 10. Prüfdialoge A–H und echte Antwortqualität

Ausgangsszene exakt nach Auftrag: Nutzer Rian, KI Mira, verschlossenes Turmzimmer, bronzener Schlüssel an Miras Gürtel, Rians linke Hand verletzt, Ziel Zimmer verlassen. C führt den Schlüssel Mira → Rian → Truhe; D korrigiert nur die Farbe zu Silber; E führt beide in den Hof, Schlüssel bei Rian, Verletzung bleibt und Ziel ist erledigt.

Technische Eingaben stammen aus dem kompilierten Produktions-`StoryPrompt`/`StorySummaryPrompt`, nicht aus einer Python-Nachbildung. A/C/D/E/F verwenden feste synthetische frühere Antworten, damit der erwartete Zustand eindeutig bleibt. B umfasst sowohl die reale Zeichen-Kürzungsschwelle als auch 20 zusätzliche abgeschlossene Füllrunden. B-long enthält eine bewusst korrekte Zusammenfassung als kontrollierte Eingabe; sie ist nicht als erfolgreiche automatische Zusammenfassung ausgegeben. Frühe/mittlere Zusammenfassungen sind eigene echte Modellgenerierungen. Der deterministische Integrationstest verwendet dagegen ausdrücklich eine Test-Inferenz und bewertet nur Reihenfolge/Speicherung.

LiteRT-PC: offizielle JVM-Laufzeit 0.17.1, CPU 4 Threads, 4.096 Kontext, 512 Antworttokens, Seed 42. GGUF-PC: identische Produktions-JNI-Quelle und derselbe llama.cpp-Commit, Windows AVX2 statt Android ARM64, CPU 4 Threads, zufälliger nativer Samplerseed. Alle qualitativen Aussagen beziehen sich auf die vorhandenen aufgezeichneten Antworten, nicht auf S24-Geschwindigkeit oder statistisch gesicherte Modellranglisten.

| Probe | Technischer Zustand | Beobachtete echte Ausgabe nach Reparaturen |
|---|---|---|
| A kurz | Vollständige relevante Quelle und korrekte Rollenmarker bei allen sechs Dateien | Gemma 4 erhält die Grundfakten, formuliert aber unklar „Wir sind wir“. Huihui lässt den Schlüssel gleichzeitig am Gürtel und im Schloss sein. Qwen 2.5 erfindet Besitz bei beiden Figuren; Qwen 0.6B nennt den Nutzer als „der Nutzer“. |
| B vor Zusammenfassung | Frühere Besitzänderung liegt weiterhin im Archiv, fehlt im normalen Suffix; früher Checkpoint löst jetzt aus | Huihui fasst Übergabe korrekt zusammen, lässt aber Verletzung/Ziel weg. Gemma 4 nennt widersprüchlichen Besitz. Qwen 0.6B läuft bis zum Ausgabelimit. |
| B lang mit korrekter Zusammenfassung | Hof, Silber, Rian als Besitzer und erledigtes Ziel sind in der wirklichen Eingabe vorhanden | Gemma 4 behält Hof/Schlüssel, verlangt erneut das Verlassen des Turmzimmers. Huihui kehrt zu Miras Bronzeschlüssel zurück. Kleine Qwen-Antworten bleiben widersprüchlich oder unverständlich. |
| C Besitz/Truhe | Alle Transfers in richtiger Reihenfolge vorhanden | Gemma 4 und Huihui nennen die Truhe. Qwen 2.5/0.6B behaupten zusätzlich weiterhin Besitz bei einer Figur. |
| D Silber | Spätere Korrektur vorhanden, Standort nicht gelöscht | Gemma 4, Huihui und Qwen 2.5 nennen Silber/Truhe korrekt. Qwen 0.6B umschreibt nur „andere Farbe“. |
| E Hof/Hand/Ziel | Vollständige Ereignisse vorhanden, Startort als historische Notiz gekennzeichnet | Huihui trifft den Kernzustand. Gemma 4 fragt trotz bereits erreichtem Ziel nochmals, ob es erreicht sei. Qwen 2.5 spricht Mira als Gegenüber an und antwortet aus falscher Rolle. Qwen 0.6B gibt nur eine knappe Erfolgsaussage ohne die verlangten Details. |
| F unbekannte Vergangenheit | Geschenk/Farbe nie als Fakt vorgegeben; Frage fordert bei Unbekanntheit eine entsprechende Antwort | Huihui erfindet das angeblich vergessene Geschenk. Gemma 4 erfindet keine Farbe, macht aber Rians Schwester zu ihrer Schwester. Qwen 2.5 verwechselt ebenfalls den Bezug; Qwen 0.6B antwortet am Thema vorbei. |
| G Wiederherstellung/Isolation | ViewModel-/SQLite-Rekonstruktion und Chat-Trennung bestanden | Kein echter Android-Neustart-/Modellantworttest auf dem Handy. |
| H Änderungen/Unterbrechung | Abbruch, verspätete Events, Doppelsenden und native Cacheänderungen bestanden | Edit-/Regenerations-UI nicht vorhanden; Lifecycle/Modellwechsel auf S24 offen. |

Die Rohantworten stehen in `gemma4-after.json`, `huihui-after.json`, `qwen25-after.json`, `qwen06-after.json`. Dolphin und Gemma 3 wurden hier auf Template, Tokens, Cache und Limitverhalten geprüft; eine vollständige A–F-Qualitätsserie für diese beiden Dateien ist **ungeprüft**.

### Vorhandener Trainingsadapter gegen seine eigene Basis

`compare_adapter.py` lädt denselben vorhandenen PEFT-Piloten lokal und erzeugt je Fall einmal mit deaktiviertem und einmal mit aktivem Adapter. A/C/D/E/B-long/F, Seed 42, für E zusätzlich Seed 31415: **14 Antworten**. Offizielles Qwen-Instruct-2507-Template aus der festgelegten Quelle; `activeAdapters` wird pro Antwort erfasst. Kein Training, Merge, Upload oder Export.

Bei C/D treffen Basis und Adapter Truhe/Silber. Bei E erhalten beide Seeds mit Adapter Hof, Rian als Schlüsselbesitzer, Verletzung und erledigtes Ziel korrekt. A ist beim Adapter klarer. **B-long scheitert aber auch mit Adapter:** Er nennt Mira mit Bronzeschlüssel und Rian mit silbernem Schlüssel, obwohl nur ein korrigierter Schlüssel existiert, und behandelt das Verlassen des Zimmers wieder als Aufgabe. F erkennt der Adapter als unbekannt; die Basis verwechselt die Familienbeziehung.

Damit ist eine Verbesserung kurzer Dialoge beobachtet, keine belastbare Freigabe des Adapters. Der PC-Vergleich verwendet bitsandbytes 4-Bit/CUDA, nicht die Android-Quantisierung. Hugging-Face-Wiederholungsstrafe hat nicht identisch dieselbe 256-Token-Samplermechanik wie die App. Innerhalb der Basis/Adapter-Paare sind die Bedingungen gleich; Unterschiede zu den Android-Katalogantworten dürfen nicht automatisch dem Training zugeschrieben werden.

## 11. Verifikation, Befehle und Artefakte

Arbeitsverzeichnis der Gradle-Befehle: `F:\Geschichte App KI\geschichten-android`. Umgebung vorher laden:

```powershell
. 'F:\Geschichte App KI\Build-Umgebung.ps1'
.\gradlew.bat --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lint
```

Finaler normaler Lauf: **BUILD SUCCESSFUL in 1m 39s**, 82 Tests, 0 Fehler/0 übersprungen. Zuvor vollständiger sauberer ARM64-Build erfolgreich in 1m49; spätere gezielte Templateänderungen wurden erneut gebaut/geprüft. Lint: 0 Fehler und **119 Warnungen**; identische Anzahl und Warnungs-IDs wie in der Baseline. Die bestehenden Warnungen betreffen überwiegend ungenutzte Ressourcen und KTX-Hinweise; sie wurden nicht als neue Kontextfehler ausgegeben.

Ausgewählter Android-/SQLite-Testlauf:

```powershell
.\gradlew.bat --no-daemon -PvisualTests=true :app:testDebugUnitTest `
  --tests 'dev.vincent.geschichten.ai.StoryContextAuditTest' `
  --tests 'dev.vincent.geschichten.ai.StoryPromptTest' `
  --tests 'dev.vincent.geschichten.ai.StorySummaryPromptTest' `
  --tests 'dev.vincent.geschichten.ChatContextIntegrationTest' `
  --tests 'dev.vincent.geschichten.data.StoryRepositoryIntegrationTest' `
  --tests 'dev.vincent.geschichten.AppViewModelHistoryIntegrationTest' `
  --tests 'dev.vincent.geschichten.data.CatalogMigrationTest'
```

55 ausgewählte Tests bestanden. Testnamen, Fehlerzahlen und XML-Berichte sind separat erhalten; Modellqualität wird nicht aus diesen Tests abgeleitet. Die fünf neuen ViewModel-/SQLite-Prüfungen verwenden eine kontrollierte Test-Inferenz; die fünf neuen Kontextprüfungen prüfen echte Produktionszusammenstellung. `LiteRtStoryTemplateTest` prüft die globale Vorlagenwiederherstellung bei Fehler. `check_rendered_inputs.py` prüft zusätzlich die echten nativen Ausgaben: 12 A/E-Eingaben, sechs Überläufe, vier Vorher-Vorlagenfälle und sechs Tokenzählungen.

Host-Diagnosebefehle im Belegordner:

```powershell
& '.\docs\validation\chat-kontext-pruefung\run-token-probes.ps1' -Runtime gguf
& '.\docs\validation\chat-kontext-pruefung\run-token-probes.ps1' -Runtime litert
& 'C:\Users\Vince\.unsloth\studio\unsloth_studio\Scripts\python.exe' `
  '.\docs\validation\chat-kontext-pruefung\inspect_litert_tokenizers.py'
& 'C:\Users\Vince\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' `
  '.\docs\validation\chat-kontext-pruefung\check_rendered_inputs.py'
& 'C:\Users\Vince\.unsloth\studio\unsloth_studio\Scripts\python.exe' `
  '.\docs\validation\chat-kontext-pruefung\compare_adapter.py'
```

`AuditCases.java` exportiert die Eingaben aus kompilierten Kotlin-Klassen. `AuditLiteRT.java` verwendet die tatsächliche JVM-Laufzeit und im finalen Pfad die produktive `LiteRtStoryTemplate.create()`. `ContextNativeProbe.java` verwendet die produktive JNI-Quelle mit ausschließlich lokalem Diagnose-Makro. Der Host-CMake-Pfad steht unter `docs/validation/dialogue-0.7.4/native-host`, die gebaute DLL unter `native-final`. Ein Upgrade/anderes Modell erfordert Neuausführung; die JSON-Dateien sind keine universellen Schablonen.

**Korrigierter Diagnosefehler, kein Appfehler:** Ein Python-Helfer las UTF-8 zunächst mit Windows-cp1252. Dadurch waren ein Adaptervergleich und die zuerst erzeugten zusätzlichen Tokenproben ungültig. Betroffene Dateien wurden unter `discarded-cp1252-harness/` erhalten und nicht bewertet. Alle Python-Lesevorgänge dieser finalen Helfer nennen UTF-8 ausdrücklich; betroffene Messungen wurden wiederholt. Die ursprünglichen Java-Qualitätsantworten für Gemma 4/Huihui waren korrekt eingelesen. Der erneute Produktionsexport stimmt inhaltlich exakt mit `inputs-after.json` überein.

**Während der Reparatur verursachte und behobene Regressionen:** Die erweiterten Regeln verkleinerten zunächst das Notizbudget zu stark; bestehende Prompttests schlugen an. `system()` reserviert jetzt nur tatsächlich ausgegebene Szenenfelder, keine in diesem Layout ungenutzten Dialogduplikate. Die bestehenden 23 Prompttests bestehen wieder. Ein anfänglicher Rückgabetypfehler der neuen Inferenzschnittstelle wurde vor dem fertigen Build korrigiert. Der frühe Zusammenfassungszeitpunkt benötigte außerdem eine Intervallkorrektur: Nach einem Teilcheckpoint darf die nächste Verdichtung ältere, bereits verarbeitete Runden nicht nochmals als neue Quellen behandeln. Die zusätzliche ViewModel-Regression dafür besteht. Diese Entwicklungsfehler sind von den ursprünglich bestätigten Appfehlern getrennt; sie wurden nicht veröffentlicht.

Finale APK:

`F:\Geschichte App KI\geschichten-android\app\build\outputs\apk\debug\app-debug.apk`

SHA-256: `fcc767afba172643b52d674846ff59014b437344df0d9cf993d344a88c7dfbd5`; 313.400.812 Bytes. Paket/Version: `dev.vincent.geschichten` / 0.7.4 / 13. Signaturprüfung besteht, bisheriger Projekt-Debug-Zertifikatfingerabdruck `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`. Native Bibliotheken ausschließlich ARM64. Die APK enthält keine synthetische Test-Inferenz und keine `ContextNativeProbe`-JNI-Exporte; nachgewiesen durch DEX-/Symbolprüfung. Es wurde nichts auf einem Gerät installiert oder öffentlich freigegeben.

## 12. Geräteprüfung und begründete nächste Schritte

`adb devices -l` meldet keine angeschlossenen Geräte (`devices.txt`). **Ungeprüft** bleiben S24/S24 Ultra: RAM-Spitzen, echte erste-Token-Zeit, längere Gespräche unter thermischer Last, Prozessneustart, Hintergrund/Vordergrund, GPU-Fallback und eventueller Zusatzaufwand der aktivierten LiteRT-Zähler. PC-Zeiten werden nicht als Handy-Leistungsversprechen verwendet. Frühe Zusammenfassungen können vor einer Antwort zusätzliche Wartezeit verursachen; diese Abwägung schützt zunächst die Kontextabdeckung.

Priorisierte Folgeschritte aus den belegten Ergebnissen:

1. **Aktuellen Szenenzustand getrennt von Erzählprosa führen.** Ein kleiner typisierter Zustand für Personen, Gegenstand-ID/Besitz/Ort, Verletzungen und Zielstatus mit Quellen-Nachrichten-IDs ist durch die Schlüssel-/Zielfehler gerechtfertigt. Bestehende SQLite-Struktur gezielt erweitern; keine zusätzliche Datenbank erforderlich. Ein Modell darf Änderungen vorschlagen, aber unbelegte oder widersprüchliche Vorschläge dürfen nicht ungeprüft den gültigen Zustand überschreiben. Nutzerkorrekturen benötigen eine nachvollziehbare Vorrangregel. Zunächst dieselben Mira/Rian-Tests und Grask prüfen, danach weitere Figuren.
2. **Tokenplanung an die tatsächliche Laufzeit koppeln.** Rollenmarker, Starttoken und Antwortreserve vorab mit dem passenden Tokenizer berücksichtigen. LiteRT benötigt dafür einen geprüften kompatiblen Zählpfad; nicht weiter pauschal Zeichen pro Token schätzen. Den Randfall „KV voll vor 512“ gesondert messen. Bis dahin bleibt der sichtbare Überlauffehler statt einer stillen Kürzung.
3. **Zusammenfassung und geschützte Korrektur trennen.** Gültige Fakten und Fortschritt unabhängig fortschreiben; das angeheftete Korrekturwissen nicht als Stoppschalter der gesamten Verdichtung verwenden. Summary/Memory/Checkpoint möglichst gemeinsam konsistent committen. Den vermuteten Absturzzeitpunkt vorher mit gezielter Fehlereinjektion reproduzieren.
4. **Kurze kontrollierte Qualitätsserien wiederholen.** Gleiche echten Eingaben, wenige Seeds, Faktenkriterien getrennt von Deutsch/Stil. Dolphin und Gemma 3 sind für A–F noch offen. Kein pauschaler Modelltausch: Die vorhandenen Befunde unterscheiden Eingabefehler und Modellfehler bereits.
5. **S24 anschließen und Lifecycle prüfen.** Neu starten, pending USER wiederherstellen, abbrechen, Figuren-/Modellwechsel, lange/volle Eingabe, RAM/TTFT beobachten. Alle bestehenden Chats erhalten. Erst danach den Reparaturbuild als Handy-Update bewerten.

Ein neues Training, Bücherimport oder größeres Modell wurde in diesem Auftrag nicht gestartet. Die verbleibenden Qualitätsfehler belegen vor allem den Bedarf an überprüfbarem Szenenzustand und sauberer tokenbasierter Kontextplanung; Training kann danach gezielt bewertet werden.
