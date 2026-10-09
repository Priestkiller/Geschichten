# Prüfbericht – Geschichten 0.5.0

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.5.0, Versionscode 7, Datenbankversion 6.

## Ergebnis und Änderungen

[Version 0.5.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.5.0) ist als öffentliches Testupdate veröffentlicht. Alle 90 mitgelieferten Figuren haben eine individuell verfasste Einführung mit Vorgeschichte, aktuellem Konflikt und erster Begegnung. Beschreibungen erscheinen kursiv, direkte Rede hervorgehoben. Die Texte wurden anhand der tatsächlichen Persönlichkeit, Ausgangsszene und Startnotizen geprüft. Gedanken, Worte und Entscheidungen der spielenden Person bleiben offen.

Die Figurenkarten besitzen einen eigenen Profilknopf unter dem Porträt. Im Chat öffnet „⋮ → Persönlichkeit ansehen“ dasselbe Profil mit vollständigen Eigenschaften, Persönlichkeit und Einführung. Das Lesen des Profils startet keine Geschichte und verändert keine gespeicherten Daten. Die Navigation enthält vier direkt erreichbare Reiter: Figuren, Verlauf, Erinnerungen und Einstellungen.

Der unveränderte Katalogstand von 0.4.0 ist als Migrationsbasis eingefroren. Beim Update erhalten nur vollständig unveränderte mitgelieferte Profile eine neue erste Nachricht. Selbst bearbeitete Profile, eigene Figuren, ID-Kollisionen und die Reihenfolge bleiben erhalten. Bestehende Geschichten, Nachrichten, Erinnerungen und Zusammenfassungen werden nicht neu geschrieben. Neue Geschichten verwenden den neuen Prolog; vorhandene Gespräche behalten ihren ursprünglichen Einstieg.

## Validierung

53 JVM-Testfälle und 35 gezielte Android-/Compose-Testfälle bestehen: insgesamt 88 verschiedene Fälle, keine Fehler und keine übersprungenen Prüfungen. Die Android-Prüfungen laufen unter Robolectric mit nativer Grafik. Der finale Produktionsbuild wurde mit `clean assembleDebug testDebugUnitTest lintDebug` ohne eingeschaltete visuelle Testquellen erstellt. Lint meldet 0 Fehler und 108 Warnungen; die genaue Verteilung steht in den maschinenlesbaren Ergebnissen.

- Alle 90 Profile enthalten verschiedene neue Prologe. Name, ID, Persönlichkeit, Ausgangsszene, Eigenschaften, Reihenfolge und alle 90 gepackten Porträts stimmen mit 0.4.0 überein. Neun Kategorien enthalten weiterhin je zehn erwachsene Figuren, fünf weibliche und fünf männliche.
- SQLite-Migrationen prüfen die Aktualisierung aller 90 unveränderten Profile sowie den vollständigen Erhalt bearbeiteter Profile und archivierter Geschichten. Erneutes Öffnen ist idempotent. Eine neue Geschichte erhält den neuen Einstieg, ein bestehendes Gespräch behält seine Nachrichten.
- Alle 90 Profile lassen sich aus der Galerie öffnen, vollständig bis zum Ende scrollen und schließen. Ein bearbeitetes Profil wird auch im Querformat aus dem Chat korrekt angezeigt. Navigation und Rückweg aus den Einstellungen funktionieren bei schmaler Darstellung.
- Alle 90 Einführungen werden mit kursiver Beschreibung und hervorgehobener direkter Rede formatiert; keine Markierungssterne bleiben im sichtbaren Text. Der längste Prolog umfasst 1.305 Zeichen und passt einschließlich einer 1.000 Zeichen langen Nutzernachricht in das vorhandene Nachrichtenbudget.
- Der tatsächliche Compose-Code erzeugte 270 Figurenansichten: 90 Karten, 90 erste Chats und 90 Profile. Alle 27 Übersichtsbilder dieser Aufnahmen wurden visuell geprüft. Zusätzliche Ansichten dokumentieren die vier Reiter, neun Einführungsenden und ein bearbeitetes Profil im Querformat.

Die ersten Prüfläufe deckten zwei unpassende UI-Testannahmen auf: ein Scroll-Endmarker hatte keine messbare Breite, und die Querformatprüfung suchte Text an einem Container ohne Textsemantik. Der Marker wurde messbar gemacht, die Prüfung scrollt nun zum tatsächlichen Text. Die betroffenen Prüfungen wurden erneut ausgeführt und bestanden. Bei der Sichtprüfung verdeckte der zunächst über dem Porträt liegende Profilknopf Teile der Gesichter; er wurde unter das Bild verschoben. Die betroffenen 19 UI-Fälle und anschließend der finale Produktionsbuild wurden erfolgreich erneut geprüft. Frühere Fehlprotokolle bleiben als Nachweis erhalten.

Die redaktionelle Prüfung korrigierte Orts- und Sachdetails, unter anderem Wartezimmer, Werkstatt, Gerichtsraum, Aris erwachsene Cousine, die Festung Aschenrain, die Arena Dornfels und den weiterhin fließenden Giftkanal. Diese Details stimmen jetzt mit den ausgelieferten Szenen und Startnotizen überein.

## Paket und öffentliche Prüfung

| Datei / Kennung | Geprüfter Wert |
| --- | --- |
| APK | `Geschichten-0.5.0.apk` |
| Dateigröße | 306.834.434 Byte |
| APK SHA-256 | `dd5306529ddfd4f564c4b1d7986581fdc288ee713ddfe53e56656d1e9d8744e0` |
| Updatebeschreibung SHA-256 | `adab388bef039fc965c4971083caa9fd07d0da223f5f29f7d1f960ac3b4339b7` |
| Zertifikat SHA-256 | `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40` |
| Paket / Mindestversion | `dev.vincent.geschichten` / Android 12, minSdk 31, ARM64 |
| Version / Code / Kanal | 0.5.0 / 7 / test |

Die 16-KB-ZIP-Ausrichtung ist bestanden. Die Produktions-APK enthält keine Compose-Testaktivität und keine eingefrorenen Testkataloge. Paketkennung und Signatur stimmen mit den bisherigen Testversionen überein.

Release-ID 402982161 wurde am 4. Oktober 2026 um 11:22:40 UTC veröffentlicht, kein Entwurf, als Testversion markiert. APK und Updatebeschreibung sind vollständig hochgeladen. Der komplette anonyme öffentliche Download stimmt in Dateigröße und SHA-256 mit der geprüften lokalen APK überein. Updatebeschreibung und öffentliche README stimmen bytegenau mit den vorbereiteten Dateien überein.

Der tatsächliche kompilierte App-Parser erkennt das öffentliche Update mit Versionscode 7 als kompatible neuere Version für die ausgelieferten Versionscodes 2, 3, 4, 5 und 6. Bei ausgeschalteten Testversionen wird es korrekt ausgeschlossen. Die README auf main wurde aktualisiert: Commit `d51934842c37abe9dbe610f3b8131a1e1af22117`, Dateistand `3c46f866b2571ae54351cbbc231fd081222f767f`.

## Nutzung und Grenzen

Unter Einstellungen → App-Updates „Testversionen einbeziehen“ einschalten und nach Updates suchen. Für den neuen Einstieg bei einer Figur im Chatmenü „Neue Geschichte“ wählen; die Einführung lässt sich vorher im Profil lesen.

Die Installation auf einem physischen Galaxy S24 und die tatsächliche Qualität der Antworten des lokalen Sprachmodells wurden nicht getestet. Der bestehende Windows-spezifische FileProvider-Hostfall wurde nicht erneut ausgeführt; Provider und Installationscode sind unverändert. Die Tests und die redaktionelle Prüfung belegen Katalog, Darstellung, Migration und Promptübergabe, garantieren aber keine gleichbleibende Modellantwortqualität.

Nachweise: [Gesamtergebnis](validation/prologues-0.5.0/summary.json), [öffentlicher Download](validation/prologues-0.5.0/public-release-summary.json), [App-Parser](validation/prologues-0.5.0/app-parser-check.log), [README-Änderung](validation/prologues-0.5.0/github-readme-update.json). Unter `validation/prologues-0.5.0` liegen auch die Originalaufnahmen, Übersichtsbilder, finalen XML-Testberichte, frühere Fehlprotokolle, Lint- und Paketprüfungen. Die Browseraufnahme des veröffentlichten Releases liegt im übergeordneten Arbeitsordner unter `release-0.5.0/GitHub-Release-veroeffentlicht.png`.
