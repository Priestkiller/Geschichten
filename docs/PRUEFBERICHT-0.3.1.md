# Prüfbericht – Geschichten 0.3.1

Stand: 4. Oktober 2026, Windows. Die signierte APK, das Update-Manifest und die Versionshinweise liegen unter `../../release-0.3.1`. Nach Nutzerfreigabe wurde [Testversion 0.3.1](https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.1) am 4. Oktober um 09:46 Uhr (Europe/Berlin) veröffentlicht; die öffentliche README wurde ebenfalls aktualisiert. Die projektbezogene Freigabe für weitere fertige Testupdates ist [separat dokumentiert](VEROEFFENTLICHUNGSFREIGABE.md).

## Überarbeitung

Alle 50 Standardfiguren wurden einzeln redaktionell geprüft und erhalten eine überarbeitete Persönlichkeit sowie eine neue Startnachricht. Die [vollständige Übersicht](FIGUREN-UND-STIMMEN-0.3.1.md) enthält die tatsächlich kompilierten Profile, Ausgangsszenen und Einstiege. Die Figuren unterscheiden sich in Motivation, innerem Konflikt, Verhalten unter Druck, Wortwahl, Satzbau, Anrede, Wissen und körperlichen Möglichkeiten. Die Einstiege variieren zwischen Beobachtung, Arbeitsanweisung, Zögern, Gespräch und Eingeständnis. Trockener Humor bleibt bei passenden Figuren erhalten.

Konkrete Widersprüche wurden berichtigt: Tareks zunehmende Schwerkraft verursacht zusätzliche Last, Sigrids bereits geöffnete Schatulle hat kein unversehrtes Siegel, Leons Zugang zum geschlossenen Café ist durch den Besitzer erlaubt, Seris neue Kette ist nicht schon verrostet und Veshra nennt die beiden abweichenden Rätseltexte. Rollen, Titel, IDs, Porträts und die Verteilung von fünf Frauen und fünf Männern in jeder der fünf Kategorien bleiben erhalten. Die 50 Porträts in der neuen APK sind bytegenau identisch mit der veröffentlichten APK 0.3.0.

Das Persönlichkeitsfeld im KI-Prompt erlaubte bisher nur 480 kodierte Zeichen und schnitt damit wichtige Vorgaben ab. Die neue Grenze von 650 reicht für sämtliche überarbeiteten Standardprofile. Die Szenengrenze wächst von 240 auf 500 kodierte Zeichen, sodass zu Beginn alle vollständigen Ausgangsszenen übertragen werden. Das Genre ist ausdrücklich enthalten. Zusätzliche Vorgaben verbinden Stimme und Motive mit Entscheidungen, begrenzen Wissen und körperliche Handlungen, verlangen passende Ursachen und Folgen und vermeiden wiederholte Vorstellungen oder dauernde Rückfragen. Der Systemprompt bleibt auf 3.000 Zeichen begrenzt; bei umfangreichem Spielstand haben die aktuelle Zusammenfassung und der aktuelle Ort Vorrang vor der ausführlichen Ausgangsszene.

## Bestehende Daten

Die SQLite-Datenbank verwendet Version 4. Die Migration vergleicht jeden gespeicherten Standarddatensatz mit einer eingefrorenen Kopie des tatsächlich ausgelieferten 0.3.0-Profils. Nur vollständige Übereinstimmungen ohne Bearbeitungsmarkierung erhalten die neuen Texte. Jede abweichende Nutzerfassung bleibt vollständig erhalten, auch bei fehlender Bearbeitungsmarkierung. Sortierung und sämtliche Geschichten, Nachrichten, Erinnerungen, Zusammenfassungen sowie Zeitstempel bleiben erhalten. Neue Geschichten einer aktualisierten Standardfigur verwenden den neuen Einstieg; vorhandene Startnachrichten werden nicht ersetzt.

Die sechs Migrationstests verwenden reale SQLite-Dateien. Sie decken ältere Katalogversionen, eine vollständige eingefrorene v3-Datenbank, eigene Figuren mit kollidierenden IDs, Änderungen einzelner Textfelder, Bearbeitungsmarkierungen, wiederholtes Öffnen und die vier Startnotizen neu begonnener Geschichten ab.

## Ergebnisse

| Prüfung | Ergebnis |
| --- | --- |
| Abschließender normaler Clean-/APK-/Test-/Lint-Build | `BUILD SUCCESSFUL in 36s` |
| JVM-Tests im finalen Build | 49 bestanden; 0 Fehler, 0 übersprungen |
| Gezielte Android-/UI-Testfälle | 26 bestanden; 0 Fehler, 0 übersprungen |
| Unterschiedliche Fälle beider erfolgreichen Läufe | 75 bestanden |
| Vollständige Persönlichkeiten und erste Szenen im Prompt | Alle 50 Figuren, beide Themenmodi, exakter JSON-Vergleich bestanden |
| Fortgeschrittener Spielstand | Vollständige Persönlichkeit, aktuelle Zusammenfassung und Ort für alle 50 Figuren erhalten |
| Tatsächliche Compose-Ansichten | 50 Auswahlkarten und 50 erste Chatansichten erfolgreich gerendert; alle fünf Chatübersichten visuell geprüft |
| Lint | 0 Fehler, 68 Warnungen |
| Paket, Version, Architektur, Mindest-SDK und Signatur | Bestanden |
| Update-Manifest: Dateiname, Größe und SHA-256 | Bestanden |
| 16-KB-ZIP-Ausrichtung | Bestanden |
| Visuelle Testaktivität in der finalen APK | `ComposeTestActivity` nicht enthalten |
| Physische S24-Installation und lokale Modellantworten | Noch nicht geprüft |

Der erste Android-/UI-Lauf scheiterte beim Einrichten von Robolectric an fehlendem Schreibzugriff auf seine Download-Sperrdatei außerhalb des Arbeitsordners; es wurden dabei keine eigentlichen Testfälle ausgeführt. Der freigegebene Wiederholungslauf führte alle 26 Fälle erfolgreich aus. Danach entfernte ein normaler Clean-Build das visuelle Testprofil vor dem abschließenden APK-Build. Die finalen JVM-Ergebnisse wurden aus dem passenden Gradle-Testcache wiederhergestellt; der erste JVM-Lauf hatte dieselben 49 Fälle erfolgreich ausgeführt.

Die gezielten Android-/UI-Prüfungen umfassen sechs Katalogmigrationen, drei Repository-Integrationsfälle, einen App-Start, vier Bild-/Katalogregressionen, neun Bildschirmfälle und drei Updateoberflächenfälle. Der bekannte Windows-Pfadfall des AndroidX FileProviders aus dem historischen Bericht 0.3.0 wurde nicht erneut ausgeführt; Provider und Installationscode wurden hier nicht geändert. Die 75 erfolgreichen Fälle belegen daher keine vollständige Prüfung aller unveränderten App-Bereiche.

Von den 68 Lint-Warnungen betreffen 51 dynamisch ausgewählte Bildressourcen. Die APK enthält nachweislich alle 50 Porträts. Die übrigen Warnungen betreffen bestehende Android-/Compose-Konventionen und die ARM64-Beschränkung.

## Finale Dateien

- APK: `Geschichten-0.3.1.apk`
- Paket: `dev.vincent.geschichten`
- Version: `0.3.1`, Versionscode `5`
- Architektur: `arm64-v8a`; Mindest-SDK `31`
- Größe: `211161244` Byte
- SHA-256: `797cf6158e4ecfe937a0e2c6f6026210063cff08056d6327ca306674cd214f0a`
- Zertifikat-SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`

Das Zertifikat entspricht der vorhandenen Testinstallation. Das Manifest verwendet Kanal `test` und Versionscode 5. Die vorhandene App vor der Installation nicht deinstallieren. Die APK ist signiert und bereit für ein kompatibles Update; eine erfolgreiche Installation auf einem echten S24 wird daraus nicht abgeleitet.

## Nachweise und Grenzen

### Öffentliche Veröffentlichung

GitHub bestätigt Release-ID `402903653`, Tag `v0.3.1`, `draft=false`, `prerelease=true` und Veröffentlichung am `2026-10-04T07:46:19Z`. Die beiden vollständig hochgeladenen Release-Dateien sind APK und Update-Manifest. Größe und GitHub-SHA-256 beider Dateien entsprechen den lokalen Dateien. Die automatisch angebotenen Quellarchive betreffen ausschließlich das öffentliche Dokumentationsrepository.

Die Release-Liste wurde anonym mit denselben API-Headern wie in der App abgerufen. Manifest und README sind öffentlich erreichbar und bytegenau identisch mit den vorbereiteten Dateien. Die gesamte öffentliche APK wurde heruntergeladen und anhand von Größe und SHA-256 erneut geprüft; sämtliche Download-Weiterleitungen verwenden zulässige HTTPS-Hosts.

Der tatsächliche kompilierte App-Parser erkennt anhand dieser öffentlichen Daten 0.3.1 / Code 5 als kompatibles neueres Testupdate für 0.2.0 / Code 2, 0.2.1 / Code 3 und 0.3.0 / Code 4. Bei ausgeschalteten Testversionen wird 0.3.1 korrekt ausgeschlossen.

README-Commit auf `main`: `ff7c0c016eb5ebea7213ac1cfb19093784860670`; Inhalts-SHA: `5fcf81ac642fde7d7a3a87de66f82526db8678f7`. Der Inhalt wurde über den Connector und zusätzlich anonym geprüft. Ein Bildschirmnachweis des veröffentlichten Releases mit beiden Dateien liegt unter `../../release-0.3.1/GitHub-Release-veroeffentlicht.png`.

### Lokale Prüfnachweise

Protokolle, XML-Berichte, Lint-Befunde, 100 UI-Aufnahmen, der eingefrorene v3-Katalog, der tatsächlich kompilierte neue Katalog und die Paketprüfungen liegen unter [validation/character-0.3.1](validation/character-0.3.1). `audit-final-package.py` prüft diese finalen Nachweise und erzeugt `summary.json`.

Die Überarbeitung und die Tests belegen stimmige verfasste Einstiege, unterscheidbare Profilvorgaben, deren vollständige Übertragung, Datenmigration und Anzeige. Sie belegen keinen tatsächlichen Lauf der lokalen KI auf dem S24. Ob das Modell die Stimmen und Motive über längere Gespräche einhält, muss mit realen Antworten am Handy beurteilt werden. Frühere Prüfberichte bleiben als historische Nachweise erhalten.
