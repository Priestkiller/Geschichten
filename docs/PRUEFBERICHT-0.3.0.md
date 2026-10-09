# Prüfbericht – Geschichten 0.3.0

Stand: 4. Oktober 2026, Windows. Die fertige signierte APK und das zugehörige Update-Manifest liegen unter `../../release-0.3.0`. Nach ausdrücklicher Nutzerfreigabe wurde [Testversion 0.3.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.0) am 4. Oktober um 02:00 Uhr (Europe/Berlin) veröffentlicht; die öffentliche README wurde ebenfalls aktualisiert.

## Umfang

Der Standardkatalog wurde von 20 auf 50 erwachsene Figuren erweitert. Nordische Fantasy, Fantasy, Krimi, Science-Fiction und Kreaturen enthalten jeweils zehn Figuren, davon fünf weibliche und fünf männliche. Die 30 Ergänzungen haben individuelle Persönlichkeiten, Ziele, Schwächen, Wissensgrenzen, Einstiegsszenen, vier Startnotizen und eigene Porträts. Einige Outfits sind freizügiger; alle dargestellten menschlichen Figuren sind erwachsen.

Die 20 bisherigen Profile wurden mit einer eingefrorenen Kopie des gelieferten 0.2.1-Quellarchivs verglichen. Ihre Profilfelder sind unverändert. Die 20 bisherigen Porträtdateien und Runas Szenenbild sind bytegenau identisch mit dem ursprünglichen Archiv. Die Ergänzungen sind in [KATALOG-0.3.0.md](KATALOG-0.3.0.md) dokumentiert; die finalen Bildprompts und Dateipfade liegen unter `artwork-0.3.0`.

Die SQLite-Datenbank verwendet Version 3. Die Migration ergänzt fehlende Katalogeinträge, ohne vorhandene Figuren mit gleicher ID zu überschreiben. Sie erhält bearbeitete Profile, eigene Figuren, Geschichten, Nachrichten, Erinnerungen und die vorhandene Sortierung. Neue Standardfiguren werden hinten angefügt. Die Startnotizen gehören jeweils zur neu begonnenen Geschichte; bearbeitete Einstiegsszenen erhalten keine widersprüchlichen Originalnotizen.

## Ergebnisse

| Prüfung | Ergebnis |
| --- | --- |
| Abschließender normaler Clean-/APK-/Test-/Lint-Build | `BUILD SUCCESSFUL in 42s` |
| Normale JVM-Tests im finalen Build | 46 bestanden; 0 Fehler, 0 übersprungen |
| SQLite-Migrationstests | 4 bestanden, einschließlich realer v2-Datenbank mit ursprünglichen 20 Profilen und vorhandenen Nutzerdaten |
| Unterschiedliche Testfälle über die ausgeführten Läufe | 72 ausgeführt; 71 bestanden, 1 unter Windows fehlgeschlagen |
| Finaler Renderfall für alle Figuren | Bestanden; 50 Auswahlkarten und 50 erste Chatansichten geprüft |
| Lint | 0 Fehler, 68 Warnungen |
| Paket, Version, Architektur, Mindest-SDK, Signatur | Bestanden |
| Update-Manifest: Dateiname, Größe, SHA-256 und Versionscode | Bestanden |
| 16-KB-ZIP-Ausrichtung | Bestanden |
| Visuelle Testaktivität in der finalen APK | `ComposeTestActivity` nicht enthalten |
| Installation und Modellinferenz auf einem echten S24 | Noch nicht geprüft |

Der erste Lauf mit aktiviertem Android-/UI-Testprofil ergab 70 erfolgreiche und zwei fehlgeschlagene Fälle bei insgesamt 72 Fällen. Der Renderfall scheiterte zunächst am noch nicht übernommenen letzten Porträt. Nach dessen Übernahme wurde er erfolgreich wiederholt und nach den letzten Beschriftungsänderungen nochmals erfolgreich ausgeführt. Die finalen 100 Bildschirmaufnahmen wurden anschließend aus dem tatsächlichen Compose-Code gesichert und visuell geprüft. Gesichter sind sichtbar; lange Rollenbeschriftungen wurden an zwei Stellen gekürzt. Das Testprofil wurde vor dem abschließenden normalen Build durch `:app:clean` entfernt.

Ein bestehender positiver Fall aus `UpdateFileProviderIntegrationTest` bleibt auf diesem Windows-Rechner fehlgeschlagen: AndroidX FileProvider verknüpft Wurzelpfade mit `/`, während die kanonischen Dateipfade des Windows-Robolectric-Kontexts `\` verwenden. Der Fehler lautet, dass kein konfiguriertes Wurzelverzeichnis für die APK im Testcache gefunden wurde. Der negative Fall für private Dateien besteht. Diese Pfadbegrenzung ist durch den konkreten Fehlerbericht und die Bytecode-Ausgabe der verwendeten AndroidX-Core-1.16.0-Klasse belegt. Die produktive FileProvider-Konfiguration und die Installationslogik wurden für die Katalogerweiterung nicht verändert. Ein erfolgreiches Öffnen des Android-Installers auf dem S24 wird daraus nicht abgeleitet.

Von den 68 Lint-Warnungen betreffen 51 dynamisch über ihren Namen ausgewählte Bildressourcen (`UnusedResources`); die APK enthält nachweislich alle 50 Porträts. Die übrigen Warnungen betreffen Android-/Compose-Konventionen und die bewusste Beschränkung auf ARM64. Keine Fehler wurden gemeldet.

## Finale APK

- Datei: `Geschichten-0.3.0.apk`
- Paket: `dev.vincent.geschichten`
- Version: `0.3.0`, Versionscode `4`
- Architektur: `arm64-v8a`; Mindest-Android-SDK `31`
- Größe: `211095708` Byte (ca. 201,3 MiB)
- SHA-256: `168e9e3b4df615d5df4fa1b1aacfdabe485d0c01b1b8f1f9c39bbcd6975e970d`
- Zertifikat-SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`

Das Zertifikat entspricht der vorhandenen Testinstallation. Das geprüfte Manifest verwendet Kanal `test` und Code 4. APK und Manifest stehen gemeinsam im veröffentlichten Testrelease bereit. Die vorhandene App auf dem Handy zum Installieren eines Updates nicht vorher deinstallieren.

## Öffentliche Veröffentlichung

Release-ID `402740738`, Tag `v0.3.0`, veröffentlicht am `2026-10-04T00:00:14Z`. GitHub bestätigt `draft=false`, `prerelease=true` und zwei vollständig hochgeladene Release-Dateien. Größe und SHA-256 beider Dateien stimmen mit den lokalen Nachweisen überein. Die automatisch angebotenen Quellarchive enthalten ausschließlich das öffentliche Dokumentationsrepository; der private Android-Quellcode und Signierschlüssel wurden nicht hochgeladen.

Die Release-Liste wurde anschließend ohne Anmeldung mit denselben API-Headern wie in der App abgerufen. Manifest und aktuelle README sind öffentlich erreichbar und bytegenau identisch mit den vorbereiteten Dateien. Die öffentliche APK wurde vollständig heruntergeladen und erneut anhand von Größe und SHA-256 geprüft; alle Download-Weiterleitungen verwenden die vom App-Updater zugelassenen HTTPS-Hosts.

Der tatsächliche kompilierte App-Parser wurde auf der JVM gegen diese öffentlichen Release- und Manifestdaten ausgeführt. Er erkennt 0.3.0 / Code 4 als kompatibles neueres Testupdate für 0.2.0 / Code 2 und 0.2.1 / Code 3. Bei ausgeschalteter Berücksichtigung von Testversionen wird v0.3.0 korrekt ausgeschlossen. Diese Prüfung ersetzt weiterhin keinen Installationstest auf einem echten Handy.

README-Commit auf `main`: `0bc83e7ad7f3297ee0a3958baf23ac0a79d0b11f`, Inhalts-SHA `25f953e6a110018109636456044c07769f2753f4`. Der Inhalt wurde über den Connector und zusätzlich anonym über die öffentliche Rohdatei überprüft. Die Nachweise heißen `public-release-summary.json`, `github-releases.json`, `public-download-check.log`, `app-parser-check.log` und `github-readme-update.json`. Ein Bildschirmnachweis liegt unter `../../release-0.3.0/GitHub-Release-veroeffentlicht.png`.

## Nachweise und Grenzen

Alle Protokolle, XML-Testberichte, Lint-Befunde, der APK-Manifestauszug, die ZIP-Ausrichtungsprüfung und die maschinenlesbare Zusammenfassung liegen unter [validation/catalog-0.3.0](validation/catalog-0.3.0). `audit-final-package.py` prüft die finalen lokalen Nachweise, die erhaltenen Originalbilder, die Verteilung, die gepackten Porträts und die Übereinstimmung von APK und Manifest. Die Originalporträtübersichten und die 100 tatsächlichen UI-Aufnahmen liegen dort ebenfalls.

Die Tests und Veröffentlichungsprüfungen belegen Datenbankmigration, Profil-/Startnotizenlogik, vorhandene Download-/Updateauswahl, die gerenderte Oberfläche und den öffentlich verfügbaren Testrelease. Sie belegen keinen Modelllauf auf dem S24, keine tatsächliche Antwortqualität der 30 neuen Figuren und keine Installation auf einem echten Handy. Der frühere Prüfbericht für 0.2.1 bleibt als historischer Nachweis erhalten.
