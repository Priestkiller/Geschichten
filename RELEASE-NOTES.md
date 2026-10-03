# Geschichten 0.2.0 – 20 Figuren und App-Updates

**Testversion 0.2.0, Versionscode 2.**

## Neu: 20 Figuren zum Ausprobieren

Zu Runa, Elara, Leon und Mira kommen 16 neue Persönlichkeiten mit eigenen Porträts, Stimmen, Zielen, Schwächen und Geschichten:

- **Aelwyn**, Waldelfen-Späherin; **Borin**, zwergischer Runenschmied; **Kael**, menschlicher Paladin; **Nyra**, Tiefling-Diplomatin.
- **Sylwen**, Druidenhüterin; **Varen**, vampirischer Archivar; **Thora**, orkische Karawanenführerin; **Orin**, Rabengestaltwandler.
- **Vaelgor**, uralter Bronzedrache; **Fenrik**, sprechender Runenwolf; **Soryn**, Waldgeist in Hirschgestalt; **Seris**, Nixe und Meeresbotin.
- **Nessa**, Fuchswandlerin; **Korr**, bewusster Steinwächter; **Pyra**, Phönix; **Aruun**, sprechender Greif.

Alle Figuren sind erwachsen; die Fantasywesen handeln als eigenständige Gesprächspartner. Jeder Standardcharakter beginnt mit einer eigenen Szene und vier bearbeitbaren Startnotizen. Im Figureneditor lassen sich alle 20 Porträts auswählen. Alle Figuren verwenden dasselbe lokale KI-Modell.

## Neu: Update-Button in der App

Unter **Deine App → App-Updates** gibt es **Nach Updates suchen**. Über GitHub bereitgestellte neue Versionen zeigen ihre Änderungen und lassen sich direkt aus der App herunterladen. **Testversionen einbeziehen** ist zunächst eingeschaltet.

Vor der Installation prüft die App Dateigröße, SHA-256, Paket-ID, Version, Android-Mindestversion und die passende Signatur. Anschließend öffnet **Update installieren** den Android-Dialog. Android fragt nach der erforderlichen Freigabe; die Installation bestätigst du im Systemdialog.

Die Updatesuche startet nur auf Knopfdruck. Dafür und für neue APKs wird Internet benötigt. Das bereits eingerichtete Sprachmodell erzeugt seine Antworten weiter direkt auf dem Handy; Geschichten und Erinnerungen bleiben lokal.

## Von 0.1.0 aktualisieren

**`Geschichten-0.2.0.apk` einmal direkt herunterladen, öffnen und als Update installieren. Die vorhandene App vorher nicht deinstallieren.** Erst diese Version ergänzt den Update-Button für weitere Veröffentlichungen.

Die Datenbankmigration ergänzt fehlende Figuren und erhält vorhandene Geschichten, Erinnerungen, eigene Figuren und bereits bearbeitete Profile. Die Paket-ID `dev.vincent.geschichten` und das bisherige Signaturzertifikat bleiben gleich.

## Dateien und Voraussetzungen

| Punkt | Wert |
| --- | --- |
| APK | `Geschichten-0.2.0.apk` |
| Updatebeschreibung | `geschichten-android-update.json` |
| App-Version | 0.2.0, Versionscode 2 |
| Plattform | Android 12 oder neuer, ARM64 |
| Release-Kanal | GitHub-Vorabversion / Test |

Die exakte Dateigröße und die SHA-256-Prüfsumme stehen im mit dieser APK erzeugten Manifest.

## Teststand

Dies ist eine Testversion. Ein erfolgreicher Build und automatisierte Datenbank-, Update- und Oberflächenprüfungen ersetzen keinen Test des Sprachmodells auf einem physischen Galaxy S24. Modellgeschwindigkeit, Qualität längerer deutscher Dialoge, Speicherbedarf und Wärmeentwicklung müssen weiterhin am Handy geprüft werden. Auch der vollständige Download und die anschließende Updateinstallation sind am Zielgerät zu prüfen.
