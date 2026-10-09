# Prüfbericht – Geschichten 0.6.0

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.6.0, Versionscode 8, Datenbankversion 7.

## Ergebnis und Änderungen

[Version 0.6.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.6.0) ist als öffentliches Testupdate veröffentlicht. Alle 90 mitgelieferten Figuren haben einen ausführlichen, individuell verfassten Geschichteneinstieg. Die Texte umfassen 453 bis 548 Wörter, im Durchschnitt 500 Wörter beziehungsweise 3.401 Zeichen. Sie bleiben vollständig im Chat und in der Datenbank gespeichert.

Ein neuer Chat erklärt die Rolle der spielenden Person, eine mögliche gemeinsame Vorgeschichte oder den ersten Kontakt, den Ort, den aktuellen Konflikt und den Anlass der Ansprache. Die Szene führt zu den ersten Worten der Figur und lässt die nächste Antwort offen. Die Geschichten wurden an Persönlichkeit, Sprechweise, körperliche Möglichkeiten, Wissen und Ausgangsszene der jeweiligen Figur angepasst. Beschreibungen erscheinen kursiv, direkte Rede hervorgehoben.

Das Profil zeigt stattdessen zuerst die Identität und anschließend Eigenschaften, Charakter, Schwächen und Sprechweise. Es lässt sich über „Profil“ auf der Figurenkarte oder „⋮ → Persönlichkeit ansehen“ im Chat öffnen. Bei neuen beziehungsweise noch unbeantworteten Gesprächen erscheint der vollständige Einstieg am Anfang. Bereits gespielte Gespräche öffnen direkt bei der letzten Nachricht. Die vier Reiter heißen Figuren, Verlauf, Erinnerungen und Einstellungen.

Eine kompakte individuelle Ausgangslage wird pro neuer Geschichte gespeichert und an die lokale KI übergeben. Dadurch bleibt die anfängliche Rolle auch bei längerem Verlauf berücksichtigt. Im begrenzten Modellfenster vertritt diese Ausgangslage den langen ersten Text; die sichtbare Einführung wird nicht gekürzt. Das Modell erhält für unveränderte Standardfiguren die vorhandene kompakte Persönlichkeit mit Verhaltens-, Körper-, Wissens- und Sprachvorgaben. Zusammenfassung und aktueller Verlauf bleiben berücksichtigt. Die tatsächliche Antwortqualität des Modells ist davon getrennt und auf dem Handy zu beurteilen.

## Datenübernahme

Die ausgelieferten Kataloge von 0.4.0 und 0.5.0 sind als genaue historische Migrationsgrundlagen eingefroren. Nur vollständig unveränderte mitgelieferte Profile erhalten die neuen Persönlichkeits- und Einstiegstexte. Eigene Figuren, bearbeitete Profile, ID-Kollisionen und die Reihenfolge bleiben erhalten.

Nur vollständig unberührte Starts ohne eigene Antwort werden auf den neuen Einstieg umgestellt. Dazu müssen die einzige Figuren-Nachricht, Titel, Zusammenfassung, Startnotizen einschließlich Art, Text, Fixierung und Zeitstempeln sowie der gespeicherte Zustand exakt zum ursprünglichen Start passen. IDs, Zeitstempel und Nachrichtenanzahl bleiben dabei erhalten. Bereits gespielte oder bearbeitete Geschichten behalten ihre Nachrichten, Zusammenfassungen und Erinnerungen; die neue Spielerrolle wird ihnen nicht nachträglich aufgezwungen. Für einen neuen Einstieg bei einem bereits gespielten Gespräch „Neue Geschichte“ im Chatmenü wählen.

## Validierung

55 JVM-Testfälle und 41 gezielte Android-/Compose-Testfälle bestehen: insgesamt 96 verschiedene Fälle, keine Fehler und keine übersprungenen Prüfungen. Die Android-Prüfungen laufen unter Robolectric mit nativer Grafik. Der finale Produktionsbuild wurde mit `clean assembleDebug testDebugUnitTest lintDebug` ohne eingeschaltete visuelle Testquellen erfolgreich erstellt. Lint meldet 0 Fehler und 108 Warnungen; Kategorien und Einzelfälle sind in den maschinenlesbaren Ergebnissen festgehalten.

- Alle 90 Einstiege sind individuell und vollständig. Neun Kategorien enthalten je zehn erwachsene Figuren, fünf weibliche und fünf männliche. IDs, Namen, Rollen, Kategorien, Eigenschaften, Geschichtentitel, Reihenfolge und Porträtzuordnung stimmen mit 0.5.0 überein.
- Die Migration prüft alle 90 unveränderten Profile und alle 90 unberührten Startgeschichten. Bereits gespielte Gespräche sowie bearbeitete Profile, Nachrichten, Titel, Zusammenfassungen und Notizen bleiben erhalten. Wiederholtes Öffnen ist idempotent.
- Die Kontextprüfung verwendet die Ausgangslagen aller 90 Figuren gemeinsam mit gespeicherten Zusammenfassungen und aktuellen Ortsnotizen. Sie prüft die begrenzte Promptübergabe sowie den Vorrang späterer Ereignisse. Eigene Änderungen bleiben berücksichtigt.
- Alle 90 Profile sind erreichbar und vollständig lesbar. Alle 90 neuen Chats zeigen den vollständigen Einstieg und lassen sich bis zum Textende scrollen. Ein bestehender Chat mit 21 Nachrichten öffnet am Ende. Weitere Prüfungen decken die vier Reiter, schmale Darstellung, ein bearbeitetes Profil im Querformat und formatierte direkte Rede ohne sichtbare Markierungssterne ab.
- Der tatsächliche Compose-Code erzeugte 450 Figurenansichten: 90 Karten, 90 ursprüngliche erste Chatansichten, 90 Profile, 90 neue ausführliche Chatstarts und 90 Einstiegstextenden. Alle 27 Übersichtsbilder der neuen Starts, Textenden und Profile wurden visuell geprüft. Originalaufnahmen und zusätzliche Navigationsansichten sind erhalten.

Die ersten Prüfläufe zeigten drei Probleme: Eine gespeicherte Zusammenfassung passte mit den bisherigen internen Anweisungen nicht vollständig in das Modellbudget, eine Migrationsprüfung verwendete falsche gemeinsame Zeitstempel für die Startnotizen und der Chat-Endmarker hatte keine messbare Breite. Die internen Anweisungen wurden verkürzt, die Prüfung auf die tatsächlich fortlaufenden Zeitstempel korrigiert und der Endmarker messbar gemacht. Die Einführungstexte selbst bleiben ungekürzt. Die betroffenen Prüfungen und anschließend der finale Produktionsbuild bestanden. Frühere Fehlprotokolle bleiben als Nachweis erhalten.

## Paket und öffentliche Prüfung

| Datei / Kennung | Geprüfter Wert |
| --- | --- |
| APK | `Geschichten-0.6.0.apk` |
| Dateigröße | 307.162.114 Byte |
| APK SHA-256 | `4b92721237051eff7b8cbefd948b77d0b4eaf86ea8d40840615f1d653469e7e0` |
| Updatebeschreibung SHA-256 | `628fe2400be55a7cd43d2ce9b27632461b79a9b4e86f5c86a9b6eacd9b8cc766` |
| Zertifikat SHA-256 | `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40` |
| Paket / Mindestversion | `dev.vincent.geschichten` / Android 12, minSdk 31, ARM64 |
| Version / Code / Kanal | 0.6.0 / 8 / test |

Die 16-KB-ZIP-Ausrichtung ist bestanden. Die Produktions-APK enthält keine Compose-Testaktivität und keine Testkataloge. Alle 90 Porträts und das bereits vorhandene Szenenbild sind bytegleich zu 0.5.0. Paketkennung und Signatur stimmen mit den bisherigen Testversionen überein.

Release-ID 403032470 wurde am 4. Oktober 2026 um 13:25:38 UTC veröffentlicht, kein Entwurf, als Testversion markiert. APK und Updatebeschreibung sind vollständig hochgeladen. Der komplette anonyme öffentliche APK-Download stimmt in Dateigröße und SHA-256 mit der geprüften lokalen Datei überein. Die öffentliche Updatebeschreibung und README stimmen bytegenau mit den vorbereiteten Dateien überein.

Der tatsächliche kompilierte App-Parser erkennt den öffentlichen Release mit Versionscode 8 als kompatible neuere Testversion für die ausgelieferten Versionscodes 2 bis 7. Bei ausgeschalteten Testversionen wird er korrekt ausgeschlossen. Die README auf main wurde aktualisiert: Commit `07d220185d5fbf1e6b3cc9def0c31dbd7fcc2800`, Dateistand `ddbeff2949ebdcf918ce8a3691ba7d2a94d3a318`.

## Nutzung und Grenzen

Unter Einstellungen → App-Updates „Testversionen einbeziehen“ einschalten und nach Updates suchen. Alternativ die APK aus dem Release direkt als Update öffnen. Die vorhandene Installation vorher nicht deinstallieren.

Die Installation auf einem physischen Galaxy S24 und die tatsächliche Qualität der lokal erzeugten Antworten wurden nicht getestet. Der bestehende Windows-spezifische FileProvider-Hostfall wurde nicht erneut ausgeführt; Provider und Installationscode sind unverändert. Die Prüfungen belegen Katalog, Darstellung, Migration, Paket und Promptübergabe, garantieren aber keine gleichbleibende Modellantwortqualität.

Nachweise: [Gesamtergebnis](validation/chat-starts-0.6.0/summary.json), [öffentlicher Download](validation/chat-starts-0.6.0/public-release-summary.json), [App-Parser](validation/chat-starts-0.6.0/app-parser-check.log), [README-Änderung](validation/chat-starts-0.6.0/github-readme-update.json). Unter `validation/chat-starts-0.6.0` liegen außerdem der tatsächlich kompilierte finale Katalog, Originalaufnahmen, Übersichtsbilder, finale XML-Testberichte, frühere Fehlprotokolle, Lint- und Paketprüfungen. Die Browseraufnahme liegt im übergeordneten Arbeitsordner unter `release-0.6.0/GitHub-Release-veroeffentlicht.png`.
