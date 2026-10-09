# Lokaler Modelltest auf dem tatsächlichen Server – 9. Oktober 2026

**Ergebnis: Der Rechner eignet sich für einen lokalen 8B-Modellserver. Das getestete Qwen3-8B erfüllt die gewünschte Zuverlässigkeit für Figuren und Geschichten noch nicht.** Die Geschwindigkeit ist brauchbar, aber Verwandtschaften, unbekannte Vergangenheit und Übergabegründe werden teilweise falsch verwendet. Größere Modelle oder andere Modelle müssen deshalb mit denselben Fehlerfällen geprüft werden; die funktionierende Hardware ist noch kein Qualitätsnachweis.

## Aufbau und tatsächliche Ausführung

- Vom Nutzer bestätigte Hardware: Intel Core i7-6700K, 32 GB RAM, NVIDIA RTX 2060 mit 6 GiB eigenem Grafikspeicher. SSDs sind im gelieferten Task-Manager-Bild sichtbar.
- Tatsächlich abgefragter LAN-Server: `192.168.178.73:11434`, Ollama **0.40.2**. Der vom Nutzer gelieferte Startlog meldet CUDA, RTX 2060, 6 GiB insgesamt und 5 GiB frei. Cloud ist in dieser Serversitzung abgeschaltet.
- Installiertes Modell: `hf.co/Qwen/Qwen3-8B-GGUF:Q4_K_M`, 8.19B, 5.027.797.508 Bytes. Ursprünglicher Manifest-Digest: `b14ffb635b5bbdb62bbb1aed050de1c32b12a5b139eb5adeb529497c6622bc1b`.
- Der normale eingeschränkte Werkzeugprozess konnte die Verbindung nicht herstellen. Die genehmigte Ausführung außerhalb dieser Einschränkung erreichte die API sofort. Es war dafür keine weitere Firewalländerung nötig. Fensterautomation blieb wegen des Laufzeitfehlers „Das System kann den angegebenen Pfad nicht finden“ unbenutzbar.
- Alle Generierungen liefen über die lokale API auf diesem Server, **nicht auf dem stärkeren Entwicklungs-PC**. Kein Modell wurde zusätzlich heruntergeladen oder trainiert. Kein Online-Anbieter wurde zur Textgenerierung verwendet.

## Konfiguration und Denkmodus

Der Import enthält standardmäßig `num_ctx=40960` und `num_predict=32768`. Das sind keine geeigneten Ausgangswerte für diese Messreihe mit 6 GiB VRAM. Alle Qualitätsanfragen verwenden ausdrücklich **4096 Kontext-Tokens und höchstens 384 Ausgabe-Tokens**, Temperatur 0,7, Top-K 20, Top-P 0,8, Min-P 0, Repeat-Penalty 1, Presence-Penalty 1,5 und Seed 42. Es wurde nur eine Anfrage gleichzeitig ausgeführt.

Die kurze Originalabfrage mit `/no_think` funktioniert. Ein zusätzliches API-`think:false` mit der importierten Vorlage erzeugt dagegen einen sichtbaren `</think>`-Marker. Dafür wurde eine separate Testdefinition **`geschichten-qwen3-8b-test`** angelegt: Sie übernimmt dieselben Gewichte, erhält die Modellfähigkeiten und ergänzt die Vorlage um `/no_think` und den korrekt geschlossenen Denkabschnitt bei `think:false`. Das Originalmodell ist unverändert geblieben; gleiche Gewichte und gleiche Originalkonfiguration wurden überprüft.

Zwei frühere Vorlagenversuche wurden vom Diagnoseprogramm wegen einer zu strengen Metadatenprüfung angehalten; ihre Anfragen und Antworten bleiben in `alias` und `alias-v2` erhalten. Ollama bewirbt bei dieser benutzerdefinierten Vorlage im `/api/show` keine `thinking.values`. Das ist weiterhin eine Einschränkung der Metadaten. Die **tatsächlichen 28 API-Antworten mit explizitem `think:false`** enthalten aber weder Denktext noch Denkmarker. Das Verhalten der Testdefinition ohne dieses API-Feld in der grafischen Oberfläche wurde nicht geprüft. Die Definition ist deshalb eine getestete API-Konfiguration, keine zugesagte fertige GUI-Konfiguration.

Es wurde keine inhaltliche Antwort korrigiert, ersetzt oder als Erfolg zurechtgeschrieben. Keine Modellparameter wurden anhand der Qualitätsfehler nachjustiert.

## Geschwindigkeit und Grafikkarte

Zeit zum ersten Text bedeutet den ersten nicht leeren **Antworttext** am LAN-Testclient, nicht einen versteckten Denktoken. Die Zahlen schließen die API-Verbindung ein. Eine spätere Android-Anbindung, Datenbanksuche oder Darstellung auf dem Handy ist darin nicht gemessen.

| Messung | Erster Antworttext | Gesamte Antwort |
| --- | ---: | ---: |
| Sehr kurze Originalabfrage, Modell zunächst entladen | 4,55 s | 4,95 s |
| Dieselbe Originalabfrage, Modell bereits geladen | 0,20 s | 0,61 s |
| Sehr kurze Testkonfiguration, Modell zunächst entladen | 5,38 s | 5,80 s |
| Dieselbe Testkonfiguration, Modell bereits geladen | 0,073 s | 0,49 s |
| Acht Antworten mit alten App-Gedächtniseingaben, Median | 1,07 s | 5,62 s |
| Acht fortlaufende Mira-/Grask-Antworten, Median | 0,49 s | 13,80 s |
| Zwölf kurze Quellenfragen, Median | 0,32 s | 3,03 s |

Die längste Zeit bis zum ersten Text einer Qualitätsanfrage betrug **8,93 s**, bei einer langen Gedächtniseingabe nach dem Kontextwechsel. Längere, frisch zu verarbeitende Eingaben brauchen also weiterhin merklich Zeit. In den fortlaufenden Gesprächen lagen die gemessenen ersten Texte zwischen 0,38 und 1,10 s; deren vollständige Antworten dauerten zwischen 9,50 und 20,36 s. Der Median der nativen Ausgaberate betrug über die Qualitätsanfragen etwa **15,4 Tokens/s**, bei den fortlaufenden Gesprächen etwa 12,8 Tokens/s.

Die API meldet bei 4096 Kontext-Tokens rund **5,76 GB Modellbelegung, davon 4,19 GB auf der GPU**. Rund 1,57 GB der gemeldeten Belegung verbleiben damit außerhalb der GPU. Das Modell läuft also teilweise auf GPU und teilweise im RAM/über die CPU. Diese API-Zahlen beschreiben die Speicherplatzierung, **keine gemessene prozentuale GPU-Auslastung**. Andere Prozesse, Temperatur, andere Gesprächslängen und gleichzeitige Nutzer wurden nicht als Dauerlast untersucht.

Ein zusätzlicher Wechsel auf 2048 Kontext-Tokens wurde einmal mit einer sehr kurzen Anfrage geprüft. Dabei wurde das Modell neu geladen. Dieser einzelne Wert von 5,33 s ist **kein** fairer Vergleich der Geschwindigkeit bereits geladener Modelle und begründet keine Empfehlung, das Geschichtenfenster zu verkleinern.

## Inhaltliche Ergebnisse

Alle **28 Qualitätsantworten** wurden technisch abgeschlossen, keine durch die Ausgabegrenze abgeschnitten. Der größte vom Server gemeldete Eingabetokenstand war 3423; mit 384 reservierten Ausgabetokens lagen alle Anfragen innerhalb der 4096 Tokens. Die fortlaufenden Gespräche verwenden jeweils die **neu erzeugten Antworten dieses 8B-Modells**, keine alten 4B-Antworten als nachträglichen Gesprächsverlauf.

### Zwölf kurze Quellenfragen

**8/12 beantworten die Kernfrage richtig. Nur 6/12 bleiben auch im restlichen Antworttext bei den belegten vergangenen Fakten und richtigen Rollen.** Diese beiden Werte unterscheiden sich, weil eine richtige erste Aussage durch erfundene Nebendetails oder eine umgedrehte Übergabe entwertet werden kann.

- Richtig: Frea gehört als Schwester zur Spielerfigur; Pelle hat den rechten Arm gebrochen; die Laterne wurde nicht übergeben; Edda ist Eigentümerin und Marten Träger des Kompasses; der Becher ist auf dem Regal; Kian ist der neue Träger des Schals.
- Runa wird richtig als Selmas Schwester bezeichnet. Danach erfindet das Modell aber Wohnort, Altersreihenfolge und gemeinsame Vergangenheit.
- Es verneint richtig, dass Müdigkeit ausdrücklich der Übergabegrund war. Anschließend vertauscht es aber, wer die Karte wem gegeben hat, und ergänzt einen ungenannten Beweggrund.
- Leif ist als Schenker eindeutig genannt, wird in der Antwort trotzdem als unbekannt behandelt.
- Aus einer Frage nach einer Uhr wird eine sicher nicht erfolgte Übergabe mit festgelegtem Träger. Die Quelle belegt lediglich eine Frage; **nicht belegt** und **widerlegt** sind hier zu unterscheiden.
- Bei einem Amulett ohne genannten Eigentümer behauptet Iven Eigentum und ein Familienerbstück.
- Livs ausdrücklich genannter Grund für die Schlüsselübergabe wird durch eine erfundene Hilfsabsicht des Empfängers Arne ersetzt.

Auch die Erzählerform ist in den kurzen Quellenfällen nicht stabil: Das Modell beschreibt eigene Aktionen mehrfach mit `*Ich ...*`, obwohl dritte Person verlangt wurde. Diese Stilabweichung wurde getrennt vom Faktenwert bewertet.

### Acht alte App-Eingaben und zwei Gespräche

Bei den acht unverändert wiederholten App-/Gedächtniseingaben sind **5/8 Kernantworten korrekt**, davon **4/8 ohne zusätzliche belegwidrige Aussagen im restlichen Text**. Die Trennung von Dolch-Eigentümer und -Träger, die manuelle Eigentümerkorrektur auf Oda und die folgende Aufnahme des Dolchs funktionieren. Ruth wird dagegen der falschen Person als Schwester zugeordnet, eine Übergabe umgedreht, eine Kindheit erfunden und Elvas bereits genannte Schenkung als unbekannt behandelt. Der Schlüsselfall verrät kein verborgenes Wissen, lässt aber die Spielerfigur ohne Nutzervorgabe durch die Werkstatt streifen.

Die Mira-Runden halten die Schleusentür geschlossen, merken sich **Alex** und den **einfachen Empfänger** und erzwingen keinen gemeinsamen Eintritt. Gleichzeitig entstehen eine ungesicherte Scan-/Kartenfähigkeit und zunehmend genaue Signallokalisierung. Der Stil wiederholt dieselbe Notbeleuchtungsbeschreibung und dieselben Geräteaktionen.

Grask reagiert zunächst mit Kette/Brett statt einer klar gezeigten Registerstelle. Später nennt er zwar Hadrik Voss und zwölf Jahre, ergänzt aber eine konkrete Jagd- und Kettenvergangenheit. Im vierten Zug erfindet er ein Alter von dreißig Jahren, Bogen und Messer und widerspricht seiner vorherigen Behauptung, den Mann zu kennen. Die Altersfrage war sprachlich zwischen Mann und Eintrag mehrdeutig; daraus hätte keine unbelegte Altersangabe entstehen dürfen.

Die Gesprächsrunden werden wegen der unterschiedlichen erzählerischen Kriterien und fortgeschriebener eigener Erfindungen qualitativ bewertet, nicht zu einem künstlichen Gesamt-Prozentsatz zusammengezogen.

## Grenzen auf den drei Gedächtnisebenen

1. **Gespeicherter Stand:** Die verwendeten alten App-Datensätze und Quellen sind per SHA-256 eingefroren und unverändert. Es gab in diesem Auftrag **keine** neuen Schreibvorgänge in die Android-Datenbank. Die frühere Datenbankprüfung wird dadurch nicht ersetzt; eine spätere Server-Anbindung benötigt weiterhin eigene Speicher-/Neustarttests.
2. **Bereitstellung:** Die tatsächlichen HTTP-Anfragen, Rollen, Quellen, neuesten Nutzernachrichten, Konfigurationen, API-Antworten und nativen Tokenzahlen sind gespeichert und geprüft. Die Originalstelle stand vor der Antwort im Request. Alle Budgets waren eingehalten. Ein vollständiger interner Token-/Promptdump des Servers wurde nicht erfasst; die unveränderte bzw. angepasste Vorlage ist dagegen gesichert. Die alten App-Fixtures sind unterschiedlich: Der eingefrorene Kira-Brieffall enthält beispielsweise auch Tammo/Yara-Profilreste. Dieser bereits vorhandene Fehler begrenzt Aussagen über die Figurenidentität in genau diesem Fall.
3. **Verwendung:** Die gespeicherten ganzen Antworten wurden anhand der Rollen und Originalaussagen manuell bewertet. Trotz vorhandener Quelle sind manche Antworten falsch. Die zwölf kurzen Quellenfälle enthalten keine solchen fremden Profilreste und scheitern teilweise ebenfalls. Deshalb erklärt weder ein fehlender Datenbankzugriff noch allein die lange Gesprächshistorie sämtliche Fehler.

Die Rollen-/Erzählvorlagen für Mira und Grask sind eingefrorene App-Fixtures aus 0.7.4. Die Gedächtnis-Replays stammen aus der vorherigen 0.8.6-Prüfung. Das ist ein nachvollziehbarer Modellvergleich mit bekannten Problemen, **kein vollständiger Lauf der aktuellen Android-App gegen einen bereits eingebauten Servermodus**. Es wurde keine neue APK erstellt und kein Update veröffentlicht.

## Dateien und nachvollziehbare Entscheidung

- [Testplan, eingefrorene Quellen und Erwartungen](validation/server-2026-10-09/plan-frozen.json)
- [Unveränderte Basismessungen](validation/server-2026-10-09/baseline/)
- [Vorlagenänderung und Nachweis identischer Gewichte](validation/server-2026-10-09/alias-v3/)
- [Messungen der Testkonfiguration](validation/server-2026-10-09/optimized/)
- [Alle tatsächlichen Qualitätsanfragen und Rohstreams](validation/server-2026-10-09/quality/)
- [Unabhängige Prüfung jeder ganzen Antwort](validation/server-2026-10-09/independent-review.json)
- [Messwerte und erfolgreiche Eingabe-/Streamprüfungen](validation/server-2026-10-09/summary.json)
- [Ausführbarer API-Test](validation/server-2026-10-09/probe.py) und [Auswertung](validation/server-2026-10-09/summarize.py)

**Entscheidung:** Den Server als funktionierende Testbasis weiterverwenden. Dieses Modell mit dieser Konfiguration noch nicht als verlässlichen Geschichten-Erzähler freigeben. Der nächste Vergleich sollte eine andere bzw. größere Modellstufe an denselben Fehlerfällen messen; zugleich müssen Profilreste, Sprecherzuordnung, Unsicherheit und Antwortprüfung sauber bleiben. Es wird kein Erfolg für die spätere Android-Anbindung, andere Modelle oder ein größeres Modell vorweggenommen.

## Technische Primärquellen

- [Qwen: Originalmodell, Quantisierungen und Thinking-Umschaltung](https://huggingface.co/Qwen/Qwen3-8B-GGUF)
- [Ollama: Windows-Voraussetzungen](https://docs.ollama.com/windows)
- [Ollama: Kontext, Hostadresse, Cloud-Abschaltung und geladenes Modell](https://docs.ollama.com/faq)
- [Ollama: Modell aus einer bestehenden Definition erstellen](https://docs.ollama.com/api/create)
- [Ollama: laufende Modelle und Speicherplatzierung](https://docs.ollama.com/api/ps)
- [Ollama: Chat-Schnittstelle und Laufzeitmetriken](https://docs.ollama.com/api/chat)
