# Prüfbericht – Geschichten 0.7.2

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.7.2, Versionscode 11, Datenbankversion 7.

## Änderungen

Die gemeinsame Gesprächsübergabe behält den tatsächlichen Schluss langer Einführungen, einschließlich der letzten Figurenworte. Die vorherige Fassung konnte diesen ersten Beitrag durch die kompakte Ausgangslage ersetzen. Bei Grask fehlte dadurch seine konkrete Bitte, das Register zu lesen. Die gemeinsame Korrektur gilt für alle 90 mitgelieferten und selbst angelegten Figuren sowie alle sechs auswählbaren Modelle. Sprecher, aktuelle Nutzerantwort und noch offene Aufgabe sind zusätzlich im begrenzten Systemkontext zugeordnet. Unveränderte Startziele werden nach dem ersten Nutzerbeitrag nicht erneut als aktuelle Aufgabe eingesetzt; bearbeitete Notizen und neuer Verlauf bleiben erhalten. Die sichtbaren Einführungen werden nicht gekürzt oder umgeschrieben.

Direkte Rede in ausgeglichenen Anführungszeichen wird fett und goldfarben dargestellt; die umgebende Erzählung kursiv in hellem Grau. Eigene Texte ohne Redezeichen werden nicht pauschal als Figurenrede eingestuft. Der Formatter wird für Einführungen, gespeicherte und neue Figurenantworten gemeinsam verwendet.

„Verlauf leeren“ löscht nach Bestätigung Geschichten, Nachrichten und zugehörige Erinnerungen in einer Datenbanktransaktion sowie gespeicherte Entwürfe und Zusammenfassungsmarken. Eigene Figuren, Einstellungen und Modelldateien bleiben erhalten. Die Anwendung verhindert gleichzeitig laufende Bearbeitungen und verwirft veraltete Ergebnisse einer zuvor gestarteten Verlaufsladung.

Nach dem erfolgreichen Laden eines anderen Modells bietet die App „Behalten“ und „Modell löschen“ für die bisherige vollständige Datei an. Bei fehlgeschlagenem Laden bleibt sie erhalten. Die ausstehende Auswahl übersteht einen Neustart. Bereits behaltene inaktive Modelle können später über ihre Modellkarte gelöscht werden. Die gemeinsame Laufzeitsperre und die Prüfung der ausgewählten Modell-ID verhindern das Löschen der aktuellen KI; eine alte vollständige Datei und ihr eventuell vorhandener Teildownload werden entfernt. Gespräche bleiben unabhängig davon bestehen.

Erkannte Kopien interner Profildaten und der leere Formatplatzhalter „Text: Text“ werden nicht als fertige Modellantwort gespeichert. Die Eingabe bleibt für einen erneuten Versuch im Entwurf erhalten. Diese Prüfung schreibt keine Geschichte und ersetzt keine Antwort durch eine Vorlage.

## Validierung

Der endgültige Produktionsbuild mit `assembleDebug testDebugUnitTest lintDebug`, Updatequelle Priestkiller/Geschichten und Testversionen eingeschaltet ist erfolgreich. 70 JVM-Testfälle bestehen ohne Fehler oder übersprungene Fälle. Zusätzlich bestehen 21 gezielte Robolectric-/Compose- und Speicherintegrationstests. Sie prüfen unter anderem vollständige Einführungen und Rollen für alle 90 Figuren, lange und feindliche Nutzereingaben, Profilkontext, Formatierung, die tatsächliche Verlaufslöschung über das ViewModel, Datenbankkaskaden, Erhalt eigener Figuren und Einstellungen, beide Modell-Dialogaktionen und den Schutz der ausgewählten Datei. Die Android-/UI-Prüfung lief vor der abschließenden, separat geprüften Antwortvalidierung und letzten gemeinsamen Promptformulierung; UI und Speicherlogik wurden danach nicht verändert.

Android Lint: keine Fehler, 118 Warnungen. APK-Paketkennung, Versionscode, Mindestversion und bisherige Signatur sind geprüft. ZIP-Ausrichtung für 16 KB besteht. Die drei ARM64-Bibliotheken sind bytegleich mit 0.7.1. Modellgewichte, private Signierschlüssel und Quellcode sind nicht im Veröffentlichungspaket enthalten.

Die neuen Dialoge und Textfarben sind anhand gespeicherter UI-Aufnahmen geprüft. Die kurze Grask-Nachricht der Formatierungsaufnahme ist eine ausdrücklich gesetzte Testnachricht, keine erfolgreiche KI-Antwort und keine neu eingebaute App-Geschichte.

## Tatsächliche Modellproben und Grenzen

Mit den vollständigen vorhandenen Gewichten liefen native Windows-CPU-Proben für LiteRT-LM und die drei GGUF-Modelle. Sie dienten der Untersuchung mehrerer Promptvarianten; diese Zwischenstände sind keine sechs erfolgreichen Qualitätsprüfungen der endgültigen Fassung. Frühere LiteRT-Proben hatten außerdem andere Vorbefüllungs- und Wiederholungsparameter. Die letzte Gemma-Probe verwendet den tatsächlich kompilierten endgültigen Prompt, strukturelle Nachrichtenrollen, LiteRT-LM 0.17.1, vier Threads, 4.096 Kontexttoken, Seed 42, Temperatur 0,75, Top-K 40, Top-P 0,9, keine vorab gefüllte Präambel und Wiederholungsstrafe 1,08. Der separate Prüfablauf begrenzt die Antwort auf 256 Token.

Die Ergebnisse bleiben schwankend. Eine vorletzte Gemma-Probe nannte das Lesen des Registers konkret. Die endgültige Probe stellt wieder eine breite Rückfrage und führt die erbetene Lesehilfe nicht sauber weiter. Die anderen Modellproben zeigten ebenfalls Anschluss-, Sprach- oder Rollenprobleme; einzelne Ausgaben kopierten interne Angaben oder einen Formatplatzhalter. Diese zwei technischen Ausgabeformen werden jetzt abgewiesen. **Die Ursache des fehlenden Kontextschlusses ist korrigiert; eine verlässlich passende Fortsetzung für jede Figur und jedes Modell ist durch diese Proben nicht nachgewiesen.** Es gibt keine behauptete Qualitätsrangliste und keine Garantie für widerspruchsfreie Antworten.

Installation, Geschwindigkeit, Speicherbedarf und Langzeitverhalten auf einem physischen S24 wurden nicht geprüft. Bereits gespeicherte Antworten bleiben erhalten. Ein neuer Einstieg kann im Chatmenü mit „Neue Geschichte“ gestartet werden.

## Paket

- APK: `Geschichten-0.7.2.apk`, 314.048.910 Byte.
- SHA-256: `56335e9bb0433652e6d53e1937f316d28062bebcf0377e4f71293eb38fc9145c`.
- Signierzertifikat SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`.
- Paket `dev.vincent.geschichten`, Android 12 / minSdk 31, ARM64, Versionscode 11.

Nachweise: [Gesamtergebnis](validation/continuity-0.7.2/summary.json), [endgültige native Gemma-Ausgabe](validation/continuity-0.7.2/final-verified-gemma-answer.json), [Build](validation/continuity-0.7.2/final-verified-build.log), [Android-/UI-Prüfung](validation/continuity-0.7.2/verified-ui-tests.log). Test-XML, Paketprüfung und Promptvarianten liegen im selben privaten Prüfverzeichnis.

## Veröffentlichung

[Release v0.7.2](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.2) ist öffentlich als Testversion veröffentlicht, Release-ID 403153029. Die zwei hochgeladenen Dateien sind die geprüfte APK und die 317 Byte große Updatebeschreibung; deren SHA-256 lautet `eb21f6a0e05deb78efeb30c1a4c369543554ca9cf0597af34d312092f27b9b5d`. Die öffentliche README wurde im Commit `8b8b78d921a723a7ad4e7d9e29d07420df61a3ca` aktualisiert, Dateistand `eb8db44c749fa3ee154b04da4fe36952baec2957`.

Der vollständige anonyme öffentliche APK-Download stimmt in Dateigröße und SHA-256 mit der geprüften lokalen APK überein. Die öffentliche Updatebeschreibung und README stimmen bytegenau mit den vorbereiteten Dateien überein. Der tatsächlich kompilierte, unveränderte Updateparser der App erkennt 0.7.2 / Code 11 als passendes neueres Testupdate für 0.7.1 / Code 10. Nachweise: [öffentliche Prüfung](validation/continuity-0.7.2/public-release-summary.json), [App-Parser](validation/continuity-0.7.2/actual-update-parser.log).

In der App unter Einstellungen → App-Updates „Testversionen einbeziehen“ einschalten und „Nach Updates suchen“ antippen. Die vorhandene App nicht deinstallieren. Der signierte Updateweg übernimmt gespeicherte Daten.
