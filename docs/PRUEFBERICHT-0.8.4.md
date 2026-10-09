# Geschichten 0.8.4: Reparatur automatisch erfasster Fakten

Lokaler Teststand vom 07.10.2026. Paket `dev.vincent.geschichten`, Versionscode 18.
Keine Veröffentlichung, Modellumstellung, zusätzlichen Suchmodelle oder Trainingsläufe.

## Ergebnis und Grenze

Die gezielt geprüfte Auswertung ist verbessert: In zwölf alten Fällen enthielten zuvor elf Fälle insgesamt 18 Abweichungen von unabhängig geprüften Sollwerten. Nach der begrenzten Reparatur stimmen diese Sollwerte mit der SQLite-Speicherung überein und stehen im Produktionsprompt. Zwölf zusätzliche Entwicklungsfälle und acht vorher eingefrorene Abnahmefälle bestehen die Auswertungsprüfung; die acht Abnahmefälle sind zusätzlich über echte Speicherung und Wiederöffnung geprüft.

**Ein breiter Erfolg bei vollständigen Figurenantworten ist damit noch nicht bewiesen.** Im gepaarten Vergleich der drei alten Problemgespräche besteht keines der drei Modelle die strenge Gesamtbewertung. Im finalen neuen Vierer-Test sind Huihui 1/4, Qwen Original 1/4 und Gemma 4 E2B 2/4 Antworten insgesamt brauchbar. Diese kleinen Reihen ergeben keine allgemeine Modellrangliste. Richtige Fakten und angenommene Antworten bedeuten keine fehlerfreie Geschichte.

## Unabhängige Quellen und Sollwerte

Die unveränderten 0.8.3-Diagnosen und der damalige Quellcode wurden vor Änderungen unter `validation/facts-0.8.4/baseline-0.8.3/` gesichert. `baseline-hashes.json` dokumentiert die Kopie. Alle alten Originalnachrichten und Rohantworten bleiben erhalten.

`gold-regressions.json` enthält die erste unabhängige Lesung der Originale; `gold-regressions-reviewed.json` die tatsächlich verwendete Prüfung. Eine Korrektur dieser menschlichen Lesung ist ausdrücklich protokolliert: „Wiebke hat dir eine Karte geschickt“ belegt Versand, aber keine Ankunft oder aktuelle Trägerschaft. Das anfängliche Soll „Tessa trägt die Karte“ war unbelegt. Beide Fassungen und die Begründung stehen in `gold-review.json`.

`new-cases-frozen.json` und `split-frozen.json` wurden vor der Implementierung erstellt: zwölf Entwicklungs- und acht zurückgehaltene Abnahmefälle. Sie unterscheiden bestätigte Werte, unbekannte Eigentümer/Träger und mehrdeutige Bezüge. Die neue Abnahme wurde nicht zum Auswählen von Suchparametern oder Modellen benutzt. Die späteren Schutzkorrekturen stammen aus alten Regressionen bzw. separat protokollierten Antwortfehlern; die Abnahmetexte und Sollwerte blieben unverändert.

## Geänderte Erfassung

- „hat … geschenkt“ ist ein abgeschlossenes Geschenk an den Empfänger, kein Besitz des Schenkenden. `schenkt`, `überreicht … als Geschenk` und die geprüften Perfektformen werden ebenfalls erfasst.
- `owner`, `holder` und das neue Feld `placement` haben getrennte Bedeutungen. `Niemand` bedeutet abgelegt; `Nicht abgelegt` begleitet eine bekannte Aufnahme/Übergabe. Tatsächlich unbekannte Angaben werden nicht durch diese Werte ersetzt.
- Präpositionen bleiben erhalten. Ein Bewegungsziel „in den Schrank“ wird als ruhender Standort „in dem Schrank“ gespeichert; „auf“ und „unter“ bleiben unterscheidbar. Originaltexte bleiben unverändert.
- Übergabe oder Rückgabe ändern die Trägerschaft und entfernen eine bisherige Ablage, ohne automatisch das Eigentum zu ändern. Eine Ablage entfernt den Eigentümer nicht.
- Gegenstandspronomen und wenige Umschreibungen wie „Klinge“ werden nur bei einem passenden Gegenstand zugeordnet. Zwei passende Gegenstände verhindern eine bestätigte Pronomenzuordnung. Neue Eigenschaften erzeugen nicht automatisch einen zweiten Gegenstand; unterscheidbare farbige Exemplare bleiben getrennt.
- Verletzungen werden an mehreren Körperstellen, mit Links/Rechts und in den geprüften Adjektivformen erfasst. Eine ausdrückliche allgemeine Heilung beendet die gespeicherten Einzelverletzungen derselben Person.
- Hypothesen, Wünsche, Fragen und negierte Geschenke begründen keinen vollzogenen Eigentumswechsel. Versand allein begründet keinen bestätigten Empfang.
- Großgeschriebene Wörter in untergeordneten Phrasen werden nicht allein deswegen zu neuen handelnden Personen. Bekannte Personen und ausdrücklich benannte Nutzersubjekte bleiben verwendbar, auch hinter einem vorangestellten Label.

Das genaue Lena/Arik-Beispiel läuft zusätzlich durch die echte Datenbank: ein Ring, Eigentümer Arik, Träger Niemand, **auf der Kommode**. Aufnahme, Rückgabe und anschließende Ablage verändern dieselbe Gegenstands-ID und erhalten Ariks Eigentum.

## Vorhandene Daten und Figurenwissen

Datenbankversion 10 fügt ausschließlich `memory_rule_repairs` als atomare Quellen-/Regelmarkierung hinzu. Die Migration von Version 9 verändert keine vorhandenen Nachrichten oder Fakten. Ihre Erhaltung ist vor und nach dem tatsächlichen Öffnen geprüft; frühere Migrationspfade sind ebenfalls getestet.

Die Reparatur erfolgt pro Geschichte in begrenzten Schritten, vorrangig für Quellen aktueller automatischer Fakten. Sie prüft betroffene, bereits ausgewertete Nutzerquellen und gültige verfasste Auftakte. Sie erzeugt keine pauschale Neuerfassung aller Geschichten. Fehlerhafte alte Ableitungen bleiben mit Originalwert, Herkunft und Wissen nachvollziehbar gespeichert, werden aber `UNCERTAIN` und nicht mehr als gültiger Stand geliefert. Neue Ableitungen erhalten eigene IDs und Quellen.

Manuelle Festlegungen, Ausschlüsse, Anheften und neuere gültige Quellen bleiben geschützt. Kenntnisentzug wird nicht durch eine ältere Reparatur rückgängig gemacht. Eine neuere öffentliche Bestätigung desselben Werts erhält eigene Quellenherkunft, damit eine ältere Geschenkquelle sie später nicht überschreiben kann. Wiederholte Verarbeitung derselben unveränderten Quelle bleibt über Quellenmarkierungen idempotent.

Heimliche Ablagen vermitteln der Figur keinen neuen Standort. Kenntnis eines Gegenstands allein erlaubt ihr nicht, ihn aus einem unbekannten Versteck aufzunehmen. Eine ausdrückliche öffentliche Mitteilung macht sowohl die Ablage als auch die daraus folgende fehlende Trägerschaft bekannt. Zulässige aufeinanderfolgende Eigenhandlungen werden innerhalb einer Antwort berücksichtigt. Originale, Wissensquellen und frühere Fassungen werden nicht gelöscht.

## Wirkliche Modelleingaben und Antworten

Es wurden **60 tatsächliche CPU-Generierungen zu sieben unterschiedlichen Antwortsituationen** ausgeführt, nicht 60 unabhängige Abnahmefälle. Modelle: vorhandenes Huihui-Qwen3-4B, offizielles Qwen3-4B-Instruct-2507 und Gemma 4 E2B. Die übrigen vier Textmodelle verwenden dieselbe Gedächtnisübergabe, wurden in dieser Runde aber nicht zusätzlich generativ geprüft.

| Bedingung | Inhalt | Fälle je Modell |
|---|---|---:|
| A | Unveränderter Produktionsablauf 0.8.3 | 3 |
| B | Derselbe Ablauf mit unabhängig manuell korrigierten Zustandswerten; nur Diagnose | 3 |
| C | Tatsächlich automatisch reparierter SQLite-Stand | 3 |
| D | Finaler Prompt mit korrigiertem Hinweis zu ausstehenden Quellen | 3 |
| Neue Abnahme | Vier neue Situationen mit automatisch gespeichertem Stand | 4 |
| Finale neue Abnahme | Dieselben vier Situationen nach der Promptkorrektur | 4 |

Gewichte, Laufzeit, Profile, Originale und Sampling bleiben fest: Kontext 4096, Reserve/Ausgabe 512, Seed 42, CPU 4, Temperatur 0,75, Top-p 0,9, Top-k 40, Wiederholungsstrafe 1,08/Fenster 256. Die Wortsuche ist in allen Vergleichsbedingungen gleich aktiviert; Bedeutungssuche bleibt dort gleich deaktiviert. Ihre Produktionsteile und der Rückfallpfad werden separat regressionsgeprüft.

B ist kein automatisch erreichter Zustand. Seine `manual`-Markierungen und der in 0.8.3 noch unbekannte Ablagefeldname ändern notwendigerweise die Zustandsdarstellung. Korrigierte Werte verändern außerdem die damalige Kompatibilitätsfilterung von Originalquellen. Das wird nicht als sauber isolierter Modellgewinn ausgegeben. C stammt aus realer SQLite-Reparatur, mit erhaltenen Originaltexten und originalen Zeitangaben. D entfernt die falsche Behauptung, ältere Quellen seien unausgewertet, wenn nur die gerade beantwortete Nutzernachricht noch keinen Abschluss-Checkpoint hat.

Alle `raw-*.json` enthalten unveränderte vollständige Antworten, tatsächliche ausgewählte formatierte Eingaben, Tokenzahlen, ausgewählte Originalabschnitte und Messzeiten. Die 21 finalen Eingaben wurden mit den echten Modell-Templates/Tokenizern erneut aus dem finalen Code gerendert und bytegenau gegen die aufgezeichneten Eingaben geprüft. Budgetverletzungen werden ausdrücklich gemeldet; benötigte Fakten werden nicht still abgeschnitten.

Die finale Widerspruchsprüfung derselben unveränderten Rohantworten steht separat in `final-input-check-*.json`. Zwei ursprüngliche Fehlalarme bei sachlich richtigen Huihui-Antworten sind repariert: „habe … gelegt“ ist kein Beleg gegen die Ablage; „Ihnen“ ist kein anderer Eigentümername. Die alten Guard-Ergebnisse wurden nicht überschrieben. Eine nachträglich angenommene Antwort bleibt nur dann brauchbar, wenn sie auch die unabhängige Gesamtprüfung besteht.

`whole-answer-assessment.json` trennt bereitgestellte Zielfakten, tatsächliche Verwendung, Rollen, Vollständigkeit, Deutsch, Figurenstil, ursprünglichen Guard und finale Prüfung. `answer-summary.json` fasst die manuelle Gesamtbewertung zusammen. Unbelegte Begründungen und zusätzliche Vergangenheitsereignisse zählen in der strengen Quellenprüfung als Fehler; einzelne korrekte Teilantworten retten keine insgesamt falsche Antwort.

Verbleibend: Die Modelle erfinden Begründungen für die Buchübergabe, verwechseln Geschenkrollen, wiederholen Fragen oder formulieren trotz korrekter Informationen unidiomatisch. Qwen nennt den reparierten Schrank richtig, liefert im alten Gespräch jedoch keine vollständige passende Figurenantwort. Bei der Ruth-Frage fehlt der Verwandtschaftssatz zusätzlich im gewählten Wortsucheausschnitt; das ist eine Bereitstellungslücke für Archivwissen, zusätzlich zu beobachteten Antwortfehlern. Die gezielt geprüften Gegenstands- und Verletzungszustände sind davon getrennt korrekt bereitgestellt.

## Tests, Wiederholung und offene Geräteprüfung

Die lokalen Tests und Logs liegen unter `validation/facts-0.8.4/`. Normale und gezielte Integrationsergebnisse sind separat gesichert, mit Überschneidungen zwischen beiden Suiten. `delivery-verification.json` enthält die abschließenden Zähler, Signatur, Paketversion, APK-Hash, Lint, Quelldateien und Nachweise der unveränderten 0.8.3-Artefakte.

Geprüft sind Migration/Neustart, manuelle Zustände, Ausschlüsse, Anheften, Geheimnisse, Geschichtenwechsel, Abbruch, atomarer und idempotenter Abschluss, Versionskonflikte, Tokenbudget sowie Wortsuche und semantische Suche. Zwanzig bestehende Suchsituationen verwenden die echten bereits aufgezeichneten Embedding-Vektoren mit produktiver SQLite und einer unabhängigen Kosinus-Referenz. Veränderte Zugangsentscheidungen durch korrigierte Fakten werden als Vorher/Nachher protokolliert; Suchmodell, Schwelle und Rangverfahren bleiben erhalten.

Wiederholen: `BUILD_WINDOWS.cmd` für Build, normale Tests und Lint; gezielte Klassen mit `-PvisualTests=true` wie in den gesicherten Logs. `run-probe.ps1` reproduziert die Modellbedingungen mit den eingefrorenen Klassen und Zuständen und verweigert das Überschreiben vorhandener Rohdateien. Für einen neuen Lauf eine neue Ergebniskopie verwenden. `verify-final.ps1` prüft finale Eingaben ohne neue Antwortgenerierung. Die separat eingefrorenen Golddateien bleiben unverändert.

Der Parser versteht weiterhin kein beliebiges Deutsch: komplexe Personenbezüge, verschachtelte Rede, Metaphern, nicht aufgeführte Gegenstands-/Körperarten, mehrteilige Ortsbeschreibungen, manche Perfekt- und Negationskorrekturen und Namenskollisionen sind nicht zuverlässig abgedeckt. Solche Quellen bleiben im vollständigen Archiv; eine eigene Zustandsfestlegung kann nötig sein. Ein Abschluss-Checkpoint bedeutet vollständigen Regeldurchlauf, nicht vollständiges Sprachverständnis.

Es ist kein Android-Gerät angeschlossen. Speicherbedarf, Wartezeit, physischer Neustart und Offline-Betrieb auf S24/S24 Ultra bleiben offen. Die gemessenen CPU-Zeiten stammen vom PC, teilweise aus konkurrierenden Läufen, und belegen keine Handygeschwindigkeit.

Die kompatibel signierte `Geschichten-0.8.4.apk` liegt lokal im Projekt. Als Update installieren, damit bestehende Chats erhalten bleiben. Sie wurde nicht veröffentlicht und wird deshalb nicht über die öffentliche Updatesuche angeboten.

Abschließender Nachweis: **136 normale und 78 gezielte Integrationstests erfolgreich** (14 Rollentests überschneiden sich), Lint 0 Fehler. APK: 321865597 Bytes; SHA-256 `ba86c655b815359292a6c70093eff8a2d35e8c9509c568c0e5978b9b8d9e7d3f`; unveränderte Signatur und 11 unveränderte Asset-/Bibliotheksdateien gegenüber 0.8.3.
