# Fortlaufende Gedächtnisauswertung in Geschichten 0.8.1

Stand 06.10.2026. Der lokale Debug-Teststand 0.8.1 mit Versionscode 15 erweitert das Gedächtnis aus 0.8.0 um den automatischen Abruf passender älterer Originalstellen. Neue Nutzerangaben, vollständige Figurenantworten und manuelle Gedächtniskorrekturen sind an den Antwortablauf angeschlossen. Alle sechs vorhandenen Modelle verwenden denselben Ablauf. Datenbankversion 9, Paket-ID, Signierer, Figuren und Modellgewichte bleiben erhalten; es gibt keine Veröffentlichung.

## Auslöser und Übernahme

| Auslöser | Verarbeitung | Zeitpunkt der Speicherung |
| --- | --- | --- |
| Neue Nutzernachricht | Nachlesen noch unbearbeiteter vollständiger Quellen; Vorschau eindeutiger neuer Angaben; Archivsuche zur Frage; genaue Promptplanung | Die neue Nachricht steht im Archiv. Neue Ereignisse sind vorläufig und werden erst mit einer vollständigen akzeptierten Antwort bestätigt. |
| Vollständige Figurenantwort | Antwortfilter und Faktenprüfung; Zustandsversionsprüfung; erneute Widerspruchsprüfung innerhalb der Speichertransaktion | Antwort, zulässige Nutzer-/Figurenfakten, Wissen und Quellenfortschritt werden atomar übernommen. |
| Eigene Festlegung, Notiz oder Gedächtniskorrektur | Sofortiger strukturierter Eintrag, Wissenszuordnung und neue Revision; begrenztes Nachlesen offener Quellen; aktualisierter Überblick | Sofort dauerhaft, unabhängig vom Erfolg einer späteren Generierung. |

Jede Quelle besitzt ihre unveränderte Archivnachricht und einen Verarbeitungsnachweis. Bereits bearbeitete Quellen werden nicht nochmals zu Fakten verarbeitet. Die Auswertung offener älterer Quellen bleibt auf höchstens vier ältere plus zwei jüngste Quellen je normalem Auslöser beschränkt. Das ausdrückliche Nachlesen verwendet höchstens acht ältere plus zwei jüngste Quellen. Wiederholungen erzeugen keine zweiten Fakten oder Sendequittungen.

Fragen, Vermutungen und erkannte rückwirkende Behauptungen werden nicht als neue bestätigte Ereignisse behandelt. Ein Modell kann nur bestimmte klar belegte neue Handlungen durchführen; bloße statische Behauptungen überschreiben keine gültigen Zustände. Abbruch, Fehler, Ausgabelimit oder eine veraltete Revision verhindern die Übernahme des neuen Antwortversuchs. Der Entwurf wird wiederhergestellt. Früher bestätigte Ereignisse und bereits gespeicherte Nutzerkorrekturen bleiben erhalten.

## Vollständiges Archiv und tatsächliche Modelleingabe

`ArchiveRecall` durchsucht sämtliche Originalnachrichten der richtigen Geschichte, unabhängig davon, ob sie im jüngsten Gesprächsausschnitt liegen. Deutsche Wörter werden normalisiert und mit einer einfachen Endungsbehandlung verglichen. Eigennamen der Hauptfigur und Spielerfigur sowie häufige Funktionswörter bestimmen nicht alleine die Relevanz. Die Suche verändert weder Weltzustand noch Quellenfortschritt.

Lange Quellen werden mit vollständiger Abdeckung in Abschnitte zerlegt. Satzgrenzen und Zeilenwechsel werden bevorzugt; lange ununterbrochene Absätze werden an Wortgrenzen unterteilt. Jeder Abschnitt besitzt genaue Start-/Endpositionen in seinem unveränderten Original. Seine Zeichenlänge organisiert nur die Suche. Die Aufnahme in den Prompt entscheidet ausschließlich der tatsächliche Laufzeit-Tokenizer einschließlich Rollen, Spezialtoken und 512 Ausgabetoken Reserve bei 4096 Kontexttoken.

Die Planung reserviert zuerst Profil, Figurenwissen, aktuelle Pflichtzustände und neueste Nutzernachricht. Danach erhält die jüngste vollständige Runde Platz, soweit sie passt. Bis zu vier passende historische Originalabschnitte werden vor weiterem älteren Verlauf und historischen Hintergrundtexten eingefügt. Sie enthalten Originalwortlaut, Sprecherperspektive und Herkunft. Ein gefundener relevanter Abschnitt, der zusammen mit dem Pflichtstand nicht passt, erzeugt einen erklärten Fehler und erhält den Entwurf, statt einen stillen Erinnerungserfolg vorzutäuschen.

Aktuelle Fragen führen keine erkannten überholten Zustandsbehauptungen aus dem Archiv wieder als gleichwertige Belege ein. Eine ausdrückliche Frage nach früheren Ereignissen darf historische Originale finden. Sie werden als Vergangenheit bezeichnet. Bei der Antwortprüfung ist eine erkannte historische Tatsachenbehauptung nur dann ausgenommen, wenn ein der Figur bekannter historischer Fakt im tatsächlich bereitgestellten Originalabschnitt belegt ist. Ein beliebiger Verweis auf dieselbe Quellen-ID reicht nicht aus.

Private Nutzerhandlungen, ausgeschlossene Quellen und andere Geschichten gelangen nicht in den Abruf. Bei einer neuen heimlichen Handlung wird auch ihre Frage nicht zur Auswahl passender Quellen verwendet. Der Erzähler bleibt an dieselbe Wissensgrenze gebunden wie die Hauptfigur. Gemischte öffentliche und geheime Absätze werden weiterhin als ganze private Nachricht behandelt; die Grenze aus 0.8.0 bleibt bestehen.

## Schutz von Korrekturen und späten Ergebnissen

Manuelle Korrekturen speichern zusätzlich die zu diesem Zeitpunkt letzte Archivreihenfolge. Eine nachgelesene frühere Nachricht kann den korrigierten Wert deshalb auch bei einem abweichenden oder vorauseilenden Zeitstempel nicht zurücksetzen. Eine spätere neue Handlung bleibt möglich. Ältere 0.8.0-Einträge ohne diesen Ordnungswert verwenden weiterhin ihren vorhandenen Zeitstempel; es gibt keine Löschung oder Neuberechnung aller alten Gespräche.

Jeder Antwortversuch verwendet die Geschichte und Gedächtnisrevision seiner Eingabe. Vor dem atomaren Speichern wird diese Revision erneut geprüft. Eine währenddessen gespeicherte Korrektur macht den älteren Antwortversuch ungültig. Ein verspäteter Streaming-Callback bleibt zusätzlich durch die bestehende Generierungsrevision geschützt. Die neue Prüfung an der Repository-Grenze verhindert, dass ein erkannter widersprüchlicher Antworttext über einen anderen Aufrufer dennoch gespeichert wird.

## Getrennte Abnahme

110 abschließende reguläre Tests und 73 gezielte Android-/SQLite-/ViewModel-/UI-/Migrationsprüfungen bestanden ohne Fehler oder ausgelassene Fälle. Die Läufe überschneiden sich und werden nicht addiert. Der reguläre Abschlusslauf enthält auch die letzte Quellenformat-Prüfung. Build und Lint sind erfolgreich; Lint meldet null Fehler und 125 Warnungen. Es wurden die betroffenen Prüfungen ausgeführt, keine neue vollständige Wiederholung aller übrigen visuellen Tests. Belege: `unit-test-xml/`, `targeted-test-xml/`, `integration-complete.log`, `build-final-verified.log` und `lint-final.xml`.

| Ebene | Erwartung und beobachtetes Ergebnis |
| --- | --- |
| Gespeichert | Korrektur bleibt nach Generierungsfehler erhalten. Alte Quelle mit vorauseilendem Zeitstempel ersetzt sie nicht. Eine spätere gültige Handlung kann sie entwickeln. Wiederverarbeitung ändert weder Revision noch Faktenanzahl. |
| Tatsächlich bereitgestellt | Eine neue blaue Farbe erscheint bereits im ersten Inferenzaufruf. Eine passende Losungswort-Originalstelle aus der Mitte einer langen alten Nachricht erreicht denselben Aufruf trotz vieler unterschiedlicher neuer Runden. Jüngster Verlauf und Tokenreserve bleiben berücksichtigt. |
| Gesicherte Speichergrenze | Widersprüchliche fertige Antwort wird vor Antwort-/Faktenschreiben abgelehnt. Abbruch und Ausgabelimit erzeugen keine neuen abgeschlossenen Ereignisse. Eine neue manuelle Korrektur nach der Promptplanung lässt den alten Antwortversuch scheitern. |
| Suche | Andere Geschichten, private Absätze und ausgeschlossene Quellen werden nicht abgerufen. Historischer Rückblick ist vom aktuellen Zustand getrennt; genaue Originalpositionen und vollständige Abdeckung sind geprüft. |

Die Inferenzaufruf-Prüfungen verwenden ausdrücklich einen Testanbieter und beweisen die Übergabe, nicht die Qualität eines echten Modells. Die separaten nativen Modelltests im folgenden Abschnitt verwenden die bereits vorhandenen fertigen Katalogdateien. Ein S24 ist nicht angeschlossen; diese Prüfungen sind Desktop-/Robolectric-Prüfungen.

Der erste echte Prüflauf fand einen zusätzlichen Speicherfehler: „Am Brunnen habe ich …“ wurde als Besitz des Brunnens gelesen. Die erste Person des Verbs wird nun mit dem Subjekt abgeglichen. Die unklare invertierte Form bleibt im Originalarchiv, ohne den Ort als bestätigten Besitzer zu speichern. Der positive Fall „Ich habe den Brief“ bleibt unterstützt. Ebenso wird ein explizites „Der Schlüssel gehört …“ in der Antwortprüfung erkannt. Die korrigierte abschließende Prüfszene benutzt eine eindeutige Übergabe und prüft den Briefbesitz in der Datenbank separat.

## Echte Modellantworten und verbleibende Fehler

Alle sechs vorhandenen Modelle liefen offline mit denselben festen, korrigierten Prüfeingaben: eine lange alte Quelle mit Losungswort und Briefübergabe, viele unterschiedliche jüngere Runden, eine neue unmittelbare Farbkorrektur und eine Frage zu nie belegter Vergangenheit. Die lange Quelle umfasst mehr als 9500 Zeichen; Losungswort und Übergabe liegen in ihrer Mitte. Der native Prüflauf verwendet CPU mit vier Threads, Temperatur 0,75, Top-p 0,9, Top-k 40, Wiederholungsstrafe 1,08/Fenster 256, Seed 42, Kontext 4096 und Reserve 512. Keine Gewichte wurden geändert.

18 von 18 geplanten Eingabetokenzahlen stimmen mit der tatsächlichen Laufzeit überein. Der größte Prompt beträgt 3560 Eingabetoken plus 512 Reserve, insgesamt 4072. Bei allen sechs Archivfragen sind beide passenden Originalstellen tatsächlich enthalten; bei allen sechs Farbfragen steht Blau bereits im gültigen vorläufigen Stand. Die unbekannte Geschenkgeschichte liefert keine passende Originalstelle und keine bestätigten Ereignisfakten. Der unabhängige Repository-Test bestätigt Mira als Trägerin des Briefs und prüft die Geschichte getrennt.

| Modell | Alte Originalstelle verwendet | Unmittelbare Farbkorrektur | Unbekannte Vergangenheit |
| --- | --- | --- | --- |
| Gemma 4 E2B | Morgenstern richtig, Briefgeber und Verletzung vertauscht | Blau richtig; zusätzliche unbelegte Eigentumsbehauptung | Nachfrage, aber Schwester der falschen Person |
| Huihui Qwen 3 4B | Morgenstern richtig, Briefübergabe und Verletzung vertauscht | Blau und Mira richtig | Gespräch, Wärme und kühlen Morgen erfunden |
| Dolphin Llama 3.2 3B | Nur Frage wiederholt | Blau genannt; Besitzerfrage nicht beantwortet | Frage und Erzählanweisung kopiert |
| Gemma 3 4B DBL-X | Morgenstern und Rians Hand erwähnt; Grund verändert, schlechtes Deutsch | Blau genannt; Besitzerfrage kopiert | Bruder und vorherige Erwähnung erfunden |
| Qwen 2.5 1.5B | Morgenstern richtig, Briefübergabe und Verletzung vertauscht | Blau und Mira richtig | Zunächst Nachfrage, dann unsinniger Text und Rollenwechsel |
| Qwen 3 0.6B | Fülltext wiederholt | Blau genannt; Besitzerfrage kopiert | Fülltext wiederholt |

Das echte Original lautet: „Ich gebe dir am Brunnen den Brief, weil ich mit meiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann.“ Es stammt von Rian, der Spielerfigur. Gemma antwortet trotzdem: „Ich habe dir den Brief anvertraut, weil ich mit meiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann.“ Mira übernimmt also Rians Rolle, obwohl Sprecher und Quelle mitgeliefert wurden. Huihui und Qwen 2.5 zeigen denselben Richtungsfehler. Die Speicherung und Übergabe des Briefbesitzes sind korrekt, seine sprachliche Verwendung ist hier fehlerhaft.

Die unmittelbare Farbfrage gelingt Huihui und Qwen 2.5 inhaltlich: „Der Schlüssel ist jetzt blau. Mira hat ihn.“ Das ist ein echtes Ergebnis in dieser Situation, keine allgemeine Zuverlässigkeitsquote. Gemma nennt Blau und beschreibt den Schlüssel in Miras Hand, fügt aber unbelegtes Eigentum Rians hinzu. Einige andere Modelle kopieren Teile der Frage und erzeugen keine vollständige dynamische Reaktion.

Alle 18 Antworten passieren die begrenzten vorhandenen Filter. Das ist ausdrücklich **kein** Nachweis richtiger Antworten. Die Filter können freie Rollenverwechslungen, zusätzliche rückwirkende Details oder sprachliche Umformulierungen weiterhin übersehen. Erkannte statische Modellbehauptungen bleiben unsicher und überschreiben den gültigen Stand nicht; unerkannte falsche Prosa bleibt als Antwort im Archiv möglich. Damit bleibt Ebene 3, die korrekte Verwendung durch das Modell, nur teilweise erfüllt. Eine fehlerfreie Geschichte wird nicht behauptet.

Rohantworten und vollständige tatsächliche Prompts: `models-final/*-answers.json`. Manuelle Einzelbewertung: `model-assessment.json`. Tatsächliche Token- und Übergabeprüfungen: `model-handoff-summary.json`. Die feste korrigierte Eingabe steht in `model-cases-final.json`; frühere Entwicklungsantworten unter `models/` sind keine Vergleichs-Endergebnisse, teilweise noch mit den vor der Reparatur verwendeten Formulierungen.

Die gesamte Planung einschließlich Archivabruf benötigte auf diesem Desktop nach der ersten Initialisierung 31–101 ms; die erste Planung je Modell 189–297 ms. Das sind wenige Desktop-Messungen ohne Zusatzinferenz, keine S24-Messung oder belastbare Geschwindigkeitsrangliste. Auf dem Handy kann der Aufwand anders ausfallen. Die Originalsuche arbeitet lokal und führt null zusätzliche Gedächtnis-Modellaufrufe aus.

Die Erkennung bleibt ein begrenzter deutscher Regelparser. Nicht unterstützte indirekte Rede, unbekannte Formen, komplizierte Zeiten und mehrdeutige Gegenstände benötigen gegebenenfalls eine manuelle Festlegung. Eine vorhandene Quellen-ID oder ein formal gültiger Fakt ist kein allgemeiner Semantiknachweis. Alte bereits vorhandene Fehlinterpretationen werden nicht still umgeschrieben; sie können im Gedächtnis korrigiert oder ausgeschlossen werden.

Die Archivsuche beruht auf Wortüberschneidungen. Synonyme ohne gemeinsame Wörter, indirekte Verweise und komplexe Ereigniszusammenhänge können relevante Stellen übersehen. Maximal vier passende Abschnitte und das endliche Kontextfenster begrenzen jede Antwort. Das vollständige Original bleibt trotzdem lokal nachprüfbar. Suche und Faktenprüfung benötigen keinen zusätzlichen Modellaufruf; ihre Desktop-Zeit wird gesondert protokolliert.

## Dateien und Lieferung

| Datei | Aufgabe |
| --- | --- |
| `memory/ArchiveRecall.kt` | Storybezogene Vollarchivsuche, genaue Originalabschnitte und historische Sprecherzuordnung |
| `memory/MemoryPrompt.kt` | Abruf vor Antwort, jüngste Runde, Quellenpriorität und echtes Gesamtbudget |
| `memory/MemoryDatabase.kt` | Archivreihenfolge manuell gespeicherter Korrekturen |
| `memory/MemoryReplyGuard.kt` | Bekannte aktuelle Widersprüche, belegte historische Ausnahmen und kopierte Gedächtnisvorgaben |
| `memory/StoryMemory.kt` | Korrektur des nachgewiesenen Besitzerfehlers; explizite Zugehörigkeit |
| `data/StoryRepository.kt` | Auslöser bei manuellem Speichern, Archivsuche und erneute Prüfung vor atomarer Antwortübernahme |
| `AppViewModel.kt` | Übergibt tatsächlich abgerufene Quellen an Prüfung und Speicherung |
| Neue/erweiterte Tests | Archiv, DB, Inferenzgrenze, Fehler, Versionen, Migration und echte Modell-Prüfeingaben |

Die Kotlin-Dateien liegen unter `app/src/main/java/dev/vincent/geschichten/`; vollständige Nachweise unter `docs/validation/active-memory-0.8.1/`. Build-Helfer und README zeigen den lokalen Stand 0.8.1. Die Datenbankstruktur bleibt Version 9; 8 → 9 nutzt die erhaltene Migration aus 0.8.0. Die Oberfläche aus 0.8.0 bleibt unverändert.

Die signierte lokale [Geschichten-0.8.1.apk](../Geschichten-0.8.1.apk) liegt im Projektverzeichnis. Sie besitzt Paket-ID `dev.vincent.geschichten`, Versionscode 15, Datenbankversion 9, ARM64 und mindestens Android 12/API 31. Größe: 321455909 Bytes. SHA-256: `38ffe484693d15b8b17ed70de63e22749eeeb897481bf8742b9cfdacbd688125`.

APK Signature Scheme v2 ist gültig. Der bisherige Signierer besitzt SHA-256 `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`. Die native Laufzeit und Tokenizerdaten sind bytegleich mit 0.8.0; 16-KiB-Zipausrichtung ist geprüft. Neue Prüfklassen und Rohmodelle sind nicht in der APK. Als Update installieren, ohne die bisherige App zu deinstallieren. Dieser Stand wurde nicht veröffentlicht und ist deshalb nicht über die GitHub-Updatesuche erhältlich.

Ein physisches S24 oder S24 Ultra ist weiterhin nicht verbunden. Installation, RAM-Spitzen, lange echte Gespräche und Hintergrund-/Vordergrund-/Prozessneustart unter Gerätespeicherdruck bleiben offen. Die synthetischen Tests und Desktop-Inferenz ersetzen diese Geräteprüfung nicht.
