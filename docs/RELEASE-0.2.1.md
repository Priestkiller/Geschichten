# Geschichten 0.2.1 – Bessere Bildausschnitte im Chat

**Bugfix-Testversion 0.2.1, Versionscode 3.** Die Bildkorrektur ist umgesetzt, alle 20 Figuren sind visuell geprüft, und der abschließende APK-Build war erfolgreich. Insgesamt sind 57 unterschiedliche Tests bestanden: 43 normale Tests und 14 native Testfälle. Der [Prüfbericht](PRUEFBERICHT.md) dokumentiert die Ergebnisse und die zugehörigen Nachweise.

## Anlass und Korrektur

Bei Aruun war der Greifkopf im Bild über dem Chat abgeschnitten. Die Ursache betrifft die Darstellung der Porträts als Szenenbilder: Für 19 Figuren ohne eigenes breites Szenenbild wurde das jeweilige Hochformatporträt in ein festes, 235 dp hohes Banner eingepasst und zentriert beschnitten. Dabei konnten Köpfe, Ohren, Hörner oder Geweihe am oberen Rand verloren gehen.

Die Korrektur gibt diesen Porträt-Szenen ein **responsives Seitenverhältnis von 5:4**. Die Bildhöhe folgt damit der verfügbaren Breite. Die Bilder werden **oben mittig ausgerichtet (`TopCenter`)**, damit der obere Bildbereich im Ausschnitt bleibt. Der abdunkelnde Verlauf setzt erst in den unteren 18 Prozent der Darstellung ein und verdeckt den Kopfbereich nicht mehr zusätzlich.

Runas eigenes Szenenbild verwendet weiterhin sein bisheriges passendes Format. Die Korrektur betrifft die Anzeige der vorhandenen Bilder; Modell, Figurenpersönlichkeiten und Geschichten werden dadurch nicht ersetzt.

## Stand der Bildprüfung

- Alle **20 Originalporträts** wurden angesehen.
- Sämtliche **20 Chatdarstellungen und 20 Figurenkarten** wurden als native Android-Bilder geprüft und visuell freigegeben: insgesamt 40 Ansichten.
- Zusätzlich liegen Aruun-Aufnahmen bei **360 dp und 430 dp Breite** sowie eine Runa-Referenz vor. Damit umfasst die gezielte Bildprüfung **43 Render-PNGs**.
- **14 native Testfälle sind bestanden:** vier neue Bildtests, neun vorhandene UI-Tests und ein Starttest der echten App.
- Der normale abschließende Build war erfolgreich; seine **43 weiteren Tests sind bestanden**. Lint meldet **0 Fehler und 38 Warnungen**. Signatur, unverändertes Zertifikat, Paketmetadaten und 16-KB-Ausrichtung der endgültigen APK sind geprüft.

Es wurde **kein nativer Vorher-Lauf** durchgeführt. Für den vorherigen Fehler wurden der vom Nutzer gelieferte Screenshot und die Bildgeometrie herangezogen. Die 43 Render-PNGs dokumentieren den korrigierten Stand. Der finale Prüfbericht führt die zugehörigen Nachweise auf. Diese virtuellen Android-Aufnahmen belegen keinen Modelllauf auf einem physischen S24.

## Installation über die vorhandene App

Die endgültige Datei **`Geschichten-0.2.1.apk` direkt auf dem Handy öffnen und als Update installieren. Eine vorhandene Geschichten-App vorher nicht deinstallieren.** Die Paket-ID `dev.vincent.geschichten` und der bisherige Signaturschlüssel bleiben erhalten; der Versionscode steigt auf 3.

Das lokale KI-Modell bleibt unverändert. Eine bereits abgeschlossene Modelleinrichtung soll bei einem regulären Update weiter nutzbar sein. Vorhandene Geschichten, Erinnerungen und eigene Figuren bleiben im App-Speicher. Eine Deinstallation würde diese Daten und die Modelldatei entfernen.

## Bereitstellung über GitHub

Das öffentliche Repository [Priestkiller/Geschichten](https://github.com/Priestkiller/Geschichten) besteht, und die Veröffentlichung ist freigegeben. Die APK-Releases **0.2.0 und 0.2.1 stehen weiterhin aus**, weil der bekannte Browserfehler den Upload der Release-Dateien blockiert hat. Die zuletzt geprüfte GitHub-Release-Liste ist leer. Ein erreichbares Repository allein stellt noch keinen APK-Download bereit.

Für die neue Veröffentlichung sind der Tag `v0.2.1`, der Testkanal und diese zwei Release-Dateien vorgesehen:

| Datei | Inhalt |
| --- | --- |
| `Geschichten-0.2.1.apk` | Abschließend gebaute und geprüfte ARM64-Testapp |
| `geschichten-android-update.json` | Manifest für Version 0.2.1, Versionscode 3, mit Größe und SHA-256 der endgültigen APK |

Das Manifest wurde mit `scripts/prepare-update.py` aus der endgültigen 0.2.1-APK erzeugt. Es enthält die geprüfte Dateigröße von **138.454.734 Byte** und die SHA-256-Prüfsumme:

```text
e60bee138a77efb0f5ab7707489b645afcb36480210b295603cdca88d6f81461
```

APK, Manifest und Veröffentlichungstexte liegen im vorbereiteten öffentlichen Paket `Geschichten-0.2.1-GitHub-Release.zip`. Die Bereitstellung dieses Pakets bedeutet noch keine Veröffentlichung auf GitHub.

## Geräteprüfung

Die tatsächliche Laufzeit des lokalen Modells auf dem Galaxy S24 wurde nicht gemessen. Geschwindigkeit, Qualität längerer Dialoge, RAM-Nutzung und Wärmeentwicklung bleiben am Handy zu prüfen. Ein vollständiger öffentlicher GitHub-APK-Download und die anschließende Installation auf dem Gerät sind weiterhin nicht verifiziert.
