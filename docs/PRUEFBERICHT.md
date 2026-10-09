# Prüfbericht – Geschichten 0.2.1

## Ergänzung: Windows-Build und Updatesuche am 4. Oktober 2026

Die auf dem Galaxy S24 angezeigte Installation 0.2.0 verwendet die richtige Quelle `Priestkiller/Geschichten` und berücksichtigt Testversionen. Vor der Veröffentlichung war die am 4. Oktober erneut abgefragte GitHub-Release-Liste leer. Die Ursache der erfolglosen Updatesuche war die fehlende Veröffentlichung von APK und Updatebeschreibung.

Das private Quellpaket 0.2.1 wurde unter Windows entpackt. JDK 21 und Android SDK API 35 mit Build Tools 35.0.0 wurden lokal im Arbeitsordner bereitgestellt. Der normale Build ohne visuelles Testprofil lief mit `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` erfolgreich durch: **43 Tests bestanden, keine Fehler und keine übersprungenen Tests; Lint 0 Fehler und 43 Warnungen**. Die nachfolgenden 57 Fälle und 38 Lint-Warnungen beschreiben weiterhin die frühere Linux-Prüfung; native Bildtests wurden hier nicht erneut ausgeführt.

Die neu gebaute APK ist bytegenau identisch mit der bisherigen finalen 0.2.1-APK: 138.454.734 Byte, SHA-256 `e60bee138a77efb0f5ab7707489b645afcb36480210b295603cdca88d6f81461`. Paket, Version 0.2.1 / Code 3, ARM64, Mindest-SDK und APK-Signatur wurden erneut geprüft. Der Signierer stimmt auch mit der lokal vorhandenen APK 0.2.0 überein. Die 16-KB-ZIP-Ausrichtungsprüfung ist bestanden.

APK, frisch erzeugtes Manifest und Versionshinweise liegen im Arbeitsordner unter `../release-0.2.1`; zusätzlich wurde `../Geschichten-0.2.1-GitHub-Release.zip` erstellt. Nach ausdrücklicher Nutzerfreigabe wurden APK und Manifest am 4. Oktober 2026 um 00:45 Uhr (Europe/Berlin) als [öffentlicher Testrelease v0.2.1](https://github.com/Priestkiller/Geschichten/releases/tag/v0.2.1) veröffentlicht. Beide Dateien sind vollständig hochgeladen; der Release ist veröffentlicht (`draft=false`) und als Testversion markiert (`prerelease=true`). Der private Quellcode und der Signierschlüssel wurden nicht hochgeladen.

Die öffentliche Release-Liste wurde ohne Anmeldung und mit denselben API-Headern wie in der App abgerufen. Das veröffentlichte Manifest stimmt bytegenau mit der lokalen Datei überein. Die öffentliche APK wurde vollständig heruntergeladen; Größe und SHA-256 stimmen mit der geprüften lokalen APK überein. Alle Weiterleitungsziele entsprechen den von der App zugelassenen HTTPS-Hosts. Der tatsächlich gebaute Updateparser wurde gegen diese echten Release- und Manifestdaten ausgeführt: Er erkennt 0.2.1 / Code 3 als neueres kompatibles Testupdate für 0.2.0 / Code 2. Diese Prüfung verwendet den App-Parser auf der JVM; Download und Updateinstallation innerhalb der laufenden App auf dem S24 bleiben als Gerätetest offen.

Die öffentliche README wurde nach gesonderter Nutzerfreigabe ebenfalls auf den veröffentlichten Stand 0.2.1 und die direkten Downloadlinks aktualisiert.

Die neuen Nachweise stehen unter [validation/windows-2026-10-04](validation/windows-2026-10-04): Buildprotokoll, JVM-Testberichte, Lint, Zusammenfassung, APK-/Manifest-Prüfung, ZIP-Ausrichtungsprüfung, öffentliche Release-Daten, Downloadprüfung und die Prüfung mit dem App-Parser. Das Bild der veröffentlichten Dateien liegt im Arbeitsordner unter `../release-0.2.1/GitHub-Release-veroeffentlicht.jpg`.

## Historischer Prüfstand vom 3. Oktober 2026

**Stand: 3. Oktober 2026.** Die Korrektur für abgeschnittene Porträtköpfe im Chat wurde gebaut und geprüft. **57 unterschiedliche automatisierte Testfälle wurden für diesen Änderungsstand ausgeführt und bestanden: 43 normale Tests und 14 gezielte native UI-/Bildtests.** Alle 20 Figuren wurden sowohl im Chat als auch auf ihrer Auswahlkarte visuell geprüft. Die finale APK enthält Version **0.2.1, Versionscode 3**, mit unveränderter Paket-ID und bisherigem Signaturzertifikat.

## Ergebnisübersicht

| Prüfung | Ergebnis |
| --- | --- |
| Abschließender normaler Clean-/APK-/Test-/Lint-Build | BUILD SUCCESSFUL in 1m 23s |
| Normale JVM-Tests | 43 bestanden, 0 Fehler, 0 übersprungen |
| Gezielte native Fälle | 14 unterschiedliche Fälle bestanden |
| Aktuell ausgeführte unterschiedliche Fälle insgesamt | **57** |
| Neue Aruun-Prüfungen | 360 × 780 dp und 430 × 900 dp bestanden |
| Vergleich des gerenderten Augenbereichs mit dem Original | Mittlerer RGB-Fehler ca. 0,402 und 0,397; Grenzwert 18,0 |
| Dedizierte native Bildaufnahmen | 43 erzeugt und gesichert |
| Visuelle Prüfung | 20 Chats + 20 Auswahlkarten sowie 3 ergänzende Ansichten geprüft |
| Android Lint | 0 Fehler, 38 Warnungen |
| APK-Signatur | Verifiziert, APK Signature Scheme v2 |
| Signaturzertifikat gegenüber bisherigen Versionen | Identisch |
| ZIP-Ausrichtung und native ELF-LOAD-Ausrichtung | 16-KB-Kompatibilität geprüft |
| Paketinhalt | Produktionsklassen vorhanden; keine Projekt-Testklassen enthalten |
| Gebauter Quellstand | Gespeicherte SHA-256-Liste bestätigt |
| Öffentlicher APK-Download und Installation daraus | Weiterhin nicht verifiziert |

Die 63 Testfälle des früheren Standes 0.2.0 wurden **nicht** erneut als vollständige Gruppe ausgeführt und werden nicht in die aktuelle Zahl hineingerechnet. Die bisherigen Nachweise liegen getrennt unter validation/history-0.2.0.

## Korrektur und Bildprüfung

19 Figuren haben ein Hochformatporträt, aber kein eigenes breites Szenenbild. Zuvor wurde dieses Porträt zentriert in ein festes 235-dp-Banner beschnitten. Bei Aruun fiel dadurch der obere Kopfbereich aus dem sichtbaren Ausschnitt.

Der Porträt-Fallback verwendet jetzt ein responsives **5:4-Format**, richtet den Bildinhalt oben mittig aus und dunkelt erst die unteren 18 Prozent ab. Runas vorhandenes breites Szenenbild verwendet weiter sein bisheriges Format mit 235 dp Höhe. Die Bilddateien selbst, Figureninhalte, Modellkonfiguration und Datenbank wurden für diesen Fix nicht ersetzt.

Die neue Testklasse verwendet die echten ausgelieferten Szenentexte und ersten Nachrichten. Das Modell steht auf MISSING; es werden weder erfundene Dialoge noch KI-Ausgaben als Testergebnis ausgegeben. Geprüft werden vollständig sichtbare Bildrahmen und Auswahlkarten sowie der tatsächlich gerenderte Aruun-Augenbereich an der erwarteten oberen Position. Der Pixeltest liest keine Alignment-Konfiguration aus.

Die erste Testausführung traf bei den beiden Pixelproben auf einen Timeout der Compose-PixelCopy-Aufnahme in Robolectric. Die Aufnahme wurde ausschließlich in der neuen Testdatei auf den funktionierenden nativen Roborazzi-Renderer umgestellt. Die beiden Fälle wurden gezielt erneut ausgeführt und bestanden. Die übrigen zwölf Fälle, darunter der vollständige 20-Figuren-Durchlauf, waren bereits erfolgreich.

Ein gesonderter nativer Vorher-Lauf wurde nicht ausgeführt. Ausgangsnachweis sind der vom Nutzer übermittelte Screenshot und die nachvollziehbare bisherige Crop-Geometrie. Die gespeicherten Renderbilder dokumentieren den korrigierten Zustand.

Die vollständige Figurenliste und Sichtprüfung stehen in [artwork-visual-review-0.2.1.md](validation/artwork-visual-review-0.2.1.md). Es wurden keine blockierenden Ausschnitts- oder Beschriftungsfehler festgestellt. Bei Aelwyn bleibt im Original wenig Abstand über dem Haar; Kaels Genrebeschriftung hat etwas geringeren Kontrast, bleibt aber lesbar.

## Herkunft der 57 aktuellen Tests

| Bereich | Fälle |
| --- | ---: |
| Modell-Downloadprotokoll | 10 |
| Geschichten-Prompt | 12 |
| Zusammenfassungs-Prompt | 2 |
| Figurenkatalog | 3 |
| Update-Richtlinien | 16 |
| Bisherige native Compose-Oberflächen | 9 |
| Reale MainActivity mit ViewModel und SQLite | 1 |
| Neue Artwork-Regression | 4 |
| **Gesamt** | **57** |

Die vier neuen Fälle sind Aruun bei zwei Bildschirmgrößen, eine Runa-Referenz und ein vollständiger Durchlauf aller 20 Chatansichten sowie aller 20 Auswahlkarten. Der letzte Fall produziert 40 Bilder, ist aber **ein** automatisierter Testfall.

Die native Prüfung lief auf dem korrigierten UI-Quellstand vor dem abschließenden Versionsstempel. SHA-256-Vergleiche bestätigen, dass diese UI-Quellen exakt denen des finalen Builds entsprechen. Anschließend wurde die endgültige APK ohne das optionale visuelle Testprofil gebaut.

## Bildnachweise

Die dedizierten 43 Bilder liegen unter screenshots/artwork-regression:

- [Aruun bei 360 × 780 dp](screenshots/artwork-regression/aruun-360x780.png)
- [Aruun bei 430 × 900 dp](screenshots/artwork-regression/aruun-430x900.png)
- [Runa mit ihrem bisherigen Szenenbild](screenshots/artwork-regression/runa-landscape-reference.png)
- Unter all-20: jeweils chat-FIGUR.png und card-FIGUR.png für sämtliche 20 Figuren.

Diese 43 Ansichten wurden aus dem tatsächlichen Compose-Code mit Robolectric Native Graphics und Roborazzi, Android API 35 und xxhdpi gerendert. Zusätzlich wurden zehn bestehende Standardaufnahmen der allgemeinen UI und des realen Appstarts aktualisiert. Sie sind keine physischen S24-Aufnahmen und belegen keine Modell-Inferenz.

## Endgültige APK

| Eigenschaft | Wert |
| --- | --- |
| Datei | Geschichten-0.2.1.apk |
| Exakte Größe | 138.454.734 Byte, etwa 138,5 MB / 132,0 MiB |
| Paket-ID | dev.vincent.geschichten |
| Version | 0.2.1, Versionscode 3 |
| Mindestversion | Android 12 / API 31 |
| Ziel-/Compile-SDK | Android 15 / API 35 |
| Native Architektur | arm64-v8a |
| Signaturschema | APK Signature Scheme v2 |
| Updatequelle | Priestkiller/Geschichten |

**SHA-256 der endgültigen APK:**

    e60bee138a77efb0f5ab7707489b645afcb36480210b295603cdca88d6f81461

**SHA-256 des unveränderten Signaturzertifikats:**

    3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40

Gleiche Paket-ID, gleicher Signierer und höherer Versionscode erfüllen die grundlegenden Paketvoraussetzungen für ein Update der vorhandenen Testinstallation. Eine bereits installierte Geschichten-App soll dafür nicht deinstalliert werden. Der private projektbezogene Schlüssel bleibt erhalten und gehört nicht in öffentliche GitHub-Dateien.

## Lint und Buildhinweise

Lint meldet 38 Warnungen: 21 UnusedResources, 10 UseKtx, 2 DiscouragedApi sowie jeweils eine ApplySharedPref-, ModifierParameter-, ChromeOsAbiSupport-, ObsoleteSdkInt- und MonochromeLauncherIcon-Warnung. Die Bilder werden dynamisch über ihre Ressourcennamen geladen. Der synchrone SharedPreferences-Zugriff sichert einen offenen Nachrichtenentwurf vor dem Entfernen seines unbeantworteten Verlaufseintrags.

Weitere Compilerhinweise betreffen ersetzbare Icon-Zugriffe und die künftig höhere Gradle-Mindestversion. Die festgelegte aktuelle Werkzeugkombination wurde erfolgreich gebaut. Native SDK-Bibliotheken wurden unverändert verpackt; ihre 16-KB-ELF-Ausrichtung wurde gesondert bestätigt.

Die generierten Buildausgaben lagen erneut außerhalb des synchronisierten Projektverzeichnisses unter /tmp, damit temporäre Synchronisationsdateien den Ressourcenbau nicht stören. Die abschließend geprüften APK-Dateien und Berichte wurden danach ins Projekt übernommen.

## Bereitstellung und Grenzen

Das öffentliche Repository Priestkiller/Geschichten besteht und die Veröffentlichung ist freigegeben. Die zuletzt erneut geprüfte Release-Liste war leer. Ein öffentlicher APK-Download und ein vollständiges Update aus GitHub sind weiterhin nicht verifiziert. Diese lokale APK-Prüfung wird davon getrennt dokumentiert.

Die finale 0.2.1-APK wurde in dieser Umgebung nicht auf einem physischen S24 installiert. Das Sprachmodell wurde nicht vollständig heruntergeladen, initialisiert oder ausgeführt. Antwortgeschwindigkeit, Qualität längerer Dialoge, RAM-Verbrauch, Wärme und Akkunutzung wurden nicht gemessen.

Unter validation stehen Rohberichte beider nativer Läufe, die Zusammenführung auf 14 bestandene Fälle, die 43 normalen Ergebnisse, Lint, Manifest, Build-Konfiguration, APK-Metadaten, Signatur-/Ausrichtungsprüfungen und der Quellstand. summary.json enthält die aktuellen Zahlen und verweist auf die getrennten historischen Nachweise. Buildanleitung: [BUILD.md](BUILD.md).
