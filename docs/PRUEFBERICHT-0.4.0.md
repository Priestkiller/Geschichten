# Prüfbericht – Geschichten 0.4.0

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.4.0, Versionscode 6, Datenbankversion 5.

## Ergebnis

Die signierte ARM64-APK wurde mit den bestehenden festgelegten Werkzeugen gebaut. 51 JVM-Testfälle und 29 gezielte Android-/UI-Testfälle sind bestanden: insgesamt 80 verschiedene Fälle ohne Fehler oder übersprungene Prüfungen. Lint meldet keine Fehler und 108 Warnungen: 91 Hinweise auf ungenutzte Ressourcen, darunter die dynamisch über ihre Namen geladenen Figurenbilder, sowie 17 bestehende übrige Hinweise. Die genaue Verteilung steht in den Nachweisen.

Der Katalog enthält 90 erwachsene Figuren, verteilt auf neun Kategorien mit jeweils zehn Figuren und fünf weiblichen sowie fünf männlichen Figuren. Neu sind 40 Figuren in Mittelerde, Blade Runner, Cyberpunk 2077 und Monster. Eigene Namen und Erscheinungen übernehmen bekannte Rollen und Vorgeschichten in alternativen Handlungen; alle Dialoge sind neu geschrieben und bekannte Enden bleiben offen.

Die 50 ausgelieferten Profile aus 0.3.1 sind Feld für Feld und in derselben Reihenfolge erhalten. Ihre 50 gepackten Porträts sind bytegleich mit der veröffentlichten APK 0.3.1. Alle 40 neuen Porträts entsprechen unverändert den mit ImageGen erzeugten PNG-Dateien; Dateihashes, Maße und Prompts sind dokumentiert. Die vier neuen Kategorien enthalten jeweils fünf weibliche und fünf männliche Figuren.

## Prüfung der Daten und Texte

- Echte SQLite-Migrationen aus den eingefrorenen Katalogständen der Datenbankversionen 1, 2, 3 und 4: neue IDs werden ergänzt, bearbeitete Standardfiguren und eigene ID-Kollisionen bleiben erhalten. Die Aktualisierung von 0.3.1 verändert keine vorhandene Profilzeile, Reihenfolge, Geschichte, Nachricht, Erinnerung oder Zusammenfassung. Erneutes Öffnen fügt keine Duplikate hinzu.
- Neue Geschichten erhalten den passenden Einstieg und vier getrennte Startnotizen. Selbst bearbeitete Ausgangsszenen bekommen keine alten Relic-Aufträge oder Ortsvorgaben zurück.
- Alle 90 Persönlichkeiten, Szenen und Einstiege passen in die vorhandenen Editorgrenzen. Der vollständige Figurenkontext wird in beiden Themenmodi an die Prompt-Erstellung übergeben und bleibt innerhalb der festgelegten Kontextgrenzen. Das tatsächliche Sprachmodell wurde hierfür nicht geladen.
- Repositoriumsprüfungen bestätigen getrennte Geschichten und Notizen, gespeicherte Nachrichten, monotone Bearbeitungszeiten und die Wiederherstellung nach einer fehlgeschlagenen Nachricht.

Die ersten Prüfläufe deckten veraltete Testannahmen auf: Altersangaben waren nicht ausschließlich als „-jährig“ formuliert, der Weg von Datenbankversion 2 ergänzt jetzt 70 statt 30 Figuren, und beim Wechsel zwischen Kategorien musste der Test zum oberhalb der Figuren liegenden Filter zurückscrollen. Diese Prüfungen wurden korrigiert und erfolgreich erneut ausgeführt. Es war dafür keine Änderung der produktiven Oberfläche nötig.

## Darstellung und Paket

Der tatsächliche Compose-Code erzeugte 90 Figurenkarten und 90 erste Chatansichten. Alle Ressourcen wurden gefunden; Karten und Chatbilder bestehen die Sichtbarkeitsprüfungen. Die 40 neuen Karten und Chats sowie alle neuen Ausgangsporträts wurden visuell geprüft. Die vier neuen Kategorien lassen sich über die horizontalen Filter erreichen und jeweils bis zur ersten und letzten Figur scrollen. Der vorhandene Bildausschnitt für Aruuns Auge wurde an zwei Bildschirmbreiten gegen die Bildquelle geprüft.

Die endgültige APK wurde nach einem sauberen Build ohne aktivierte Testquellen erstellt. Das Manifest enthält keine Compose-Testaktivität und das Paket keine eingefrorenen Testkataloge. Die 16-KB-ZIP-Ausrichtung ist bestanden. Paket-ID `dev.vincent.geschichten`, minSdk 31 und das Signaturzertifikat bleiben kompatibel mit der installierten Testapp.

| Datei / Kennung | Geprüfter Wert |
| --- | --- |
| APK | `Geschichten-0.4.0.apk` |
| Dateigröße | 306.719.750 Byte |
| SHA-256 | `3942596b01ba21a6258fb96cae9e9390522f4b2a96303b3f7062d0fd05d6be0d` |
| Zertifikat SHA-256 | `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40` |
| Versionsname / Code | 0.4.0 / 6 |
| Updatekanal | test |

## Grenzen und Nachweise

Die Installation auf einem physischen Samsung Galaxy S24 und die tatsächliche Qualität der lokalen KI-Antworten wurden nicht getestet. Ein bekannter Windows-spezifischer FileProvider-Hosttest wurde nicht erneut ausgeführt; Provider und Installationscode sind unverändert. Die gezielten Android-Prüfungen laufen unter Robolectric mit nativer Grafik, nicht auf einem angeschlossenen Handy.

Die maschinenlesbaren Ergebnisse und Originalprotokolle liegen unter [validation/worlds-0.4.0/summary.json](validation/worlds-0.4.0/summary.json). Dort sind auch alle 180 Originalaufnahmen, vier Filteransichten, Testberichte, Lintbericht und Paketprüfungen erhalten. [Katalog und Rollenbezüge](KATALOG-0.4.0.md), [Bildprompts](artwork-0.4.0/prompts.json) und [Bildnachweise](artwork-0.4.0/generated-files.json) ergänzen den Bericht.

## Öffentliche Veröffentlichung

[Release v0.4.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.4.0) wurde am 4. Oktober 2026 um 10:14:16 UTC als Testversion veröffentlicht, Release-ID 402959753, kein Entwurf. APK und Manifest sind vollständig hochgeladen. Der komplette anonyme Download der 306.719.750 Byte großen APK stimmt in Größe und SHA-256 mit der geprüften lokalen Datei überein. Manifest und öffentliche README stimmen bytegenau mit den vorbereiteten Dateien überein.

Der tatsächliche kompilierte App-Parser erkennt den veröffentlichten Release mit Versionscode 6 als passendes neueres Testupdate für die installierten Versionscodes 2, 3, 4 und 5. Bei ausgeschalteten Testversionen wird er korrekt ausgeschlossen. Die öffentliche README wurde auf dem main-Zweig aktualisiert: Commit `ff3983e87eda3a7418209b26085db59d207967e6`, Dateistand `85d7914be0bff7e75a25cc01b9477eb37bc2439f`.

Die vollständigen anonymen Nachweise stehen unter `validation/worlds-0.4.0/public-release-summary.json`, der Parsernachweis unter `app-parser-check.log`. Eine Browseraufnahme des veröffentlichten Releases liegt neben der APK in `release-0.4.0/GitHub-Release-veroeffentlicht.png` im übergeordneten Arbeitsordner.
