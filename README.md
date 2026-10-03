# Geschichten

Geschichten ist eine native Android-Testapp für Gespräche und Abenteuer mit KI-Figuren. Die Antworten entstehen nach dem einmaligen Modelldownload auf dem Handy.

## Android-Testversion 0.2.0

- 20 erwachsene Figuren mit eigenen Persönlichkeiten, Geschichten und Porträts, darunter Fantasywesen wie ein Bronzedrache, ein Runenwolf, ein Waldgeist, ein Phönix und ein Greif.
- Eigene Figuren, mehrere lokale Geschichten und bearbeitbare Erinnerungen.
- App-Updates über GitHub: In **Deine App → App-Updates → Nach Updates suchen** prüfen, herunterladen und die Installation in Android bestätigen.
- ARM64, mindestens Android 12. Als Zielgerät ist ein Samsung Galaxy S24 vorgesehen; Modellgeschwindigkeit und Langzeitverhalten müssen auf dem Gerät getestet werden.

Die APK `Geschichten-0.2.0.apk` und ihre Updatebeschreibung stehen gemeinsam im jeweiligen GitHub-Release. Für die erste Installation beziehungsweise den Wechsel von 0.1.0 wird die APK heruntergeladen und direkt geöffnet. **Eine vorhandene Installation vorher nicht deinstallieren.** Danach steht der integrierte Update-Button zur Verfügung. Für Testveröffentlichungen muss **Testversionen einbeziehen** eingeschaltet sein.

## Einrichtung und Daten

Die etwa 2,6 GB große Modelldatei wird einmal in der App heruntergeladen. Danach ist für neue Antworten keine Internetverbindung erforderlich. Chats, Figuren und Erinnerungen bleiben im privaten App-Speicher. Die Updatesuche überträgt keine Gesprächsinhalte.

Eine Aktualisierung mit derselben App-Signatur übernimmt bestehende App-Daten. Vorhandene Figuren werden durch die Katalogerweiterung nicht überschrieben. Eine Deinstallation oder das Löschen der App-Daten entfernt die lokalen Geschichten und das Modell.

## Dieses Repository

Dieses Repository dient der Verteilung signierter Test-APKs und der zugehörigen Updatebeschreibung. Hinweise zu den verwendeten Bibliotheken und zum Modell stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Die Versionshinweise im jeweiligen Release dokumentieren den veröffentlichten Stand. Die App gleicht Versionsnummer, Dateigröße, SHA-256-Prüfsumme, Paketkennung und die Signatur der installierten App ab, bevor sie eine APK an die Android-Installation übergibt.
