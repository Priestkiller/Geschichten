# Geschichten

Geschichten ist eine native Android-Testapp für Gespräche und Abenteuer mit KI-Figuren. Die Antworten entstehen nach dem einmaligen Modelldownload auf dem Handy.

## Android-Testversion 0.3.0

**Bereitstellungsstand:** [Testversion 0.3.0 ist veröffentlicht](https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.0): APK und Updatebeschreibung stehen gemeinsam bereit. Eine installierte Version 0.2.0 oder 0.2.1 kann das Update bei eingeschalteten Testversionen finden.

- 50 erwachsene Figuren mit eigenen Persönlichkeiten, Geschichten und Porträts: zehn in jeder vorhandenen Kategorie, jeweils fünf weibliche und fünf männliche.
- Kategorien: Nordische Fantasy, Fantasy, Krimi, Science-Fiction und Kreaturen.
- 30 neue Figuren mit eigenen Einstiegen und Startnotizen; einige tragen freizügigere Outfits.
- Korrigierte Bildausschnitte im Chat: Porträts folgen der Bildschirmbreite und sind oben mittig ausgerichtet.
- Eigene Figuren, mehrere lokale Geschichten und bearbeitbare Erinnerungen.
- App-Updates über GitHub: In **Deine App → App-Updates → Nach Updates suchen** prüfen, herunterladen und die Installation in Android bestätigen.
- ARM64, mindestens Android 12. Als Zielgerät ist ein Samsung Galaxy S24 vorgesehen; Modellgeschwindigkeit und Langzeitverhalten müssen auf dem Gerät getestet werden.

Die [APK `Geschichten-0.3.0.apk`](https://github.com/Priestkiller/Geschichten/releases/download/v0.3.0/Geschichten-0.3.0.apk) und `geschichten-android-update.json` sind im [Release v0.3.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.0) veröffentlicht. In Version 0.2.0 oder 0.2.1 lässt sich das Update über **Deine App → App-Updates → Nach Updates suchen** herunterladen. **Testversionen einbeziehen** muss eingeschaltet sein. Für die erste Installation oder den Wechsel von 0.1.0 die APK herunterladen und direkt öffnen. **Eine vorhandene Installation vorher nicht deinstallieren.**

Version 0.3.0 verwendet Versionscode 4, dieselbe Paket-ID und das bisherige Signaturzertifikat.

## Einrichtung und Daten

Die etwa 2,6 GB große Modelldatei wird einmal in der App heruntergeladen. Danach ist für neue Antworten keine Internetverbindung erforderlich. Chats, Figuren und Erinnerungen bleiben im privaten App-Speicher. Die Updatesuche überträgt keine Gesprächsinhalte.

Eine Aktualisierung mit derselben App-Signatur übernimmt bestehende App-Daten. Vorhandene Figuren werden durch die Katalogerweiterung nicht überschrieben. Eine Deinstallation oder das Löschen der App-Daten entfernt die lokalen Geschichten und das Modell.

## Dieses Repository

Dieses Repository dient der Verteilung signierter Test-APKs und der zugehörigen Updatebeschreibung. Hinweise zu den verwendeten Bibliotheken und zum Modell stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Die Versionshinweise im jeweiligen Release dokumentieren den veröffentlichten Stand. Die App gleicht Versionsnummer, Dateigröße, SHA-256-Prüfsumme, Paketkennung und die Signatur der installierten App ab, bevor sie eine APK an die Android-Installation übergibt.
