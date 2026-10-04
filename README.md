# Geschichten

Geschichten ist eine native Android-Testapp für Gespräche und Abenteuer mit KI-Figuren. Die Antworten entstehen nach dem einmaligen Modelldownload auf dem Handy.

## Android-Testversion 0.4.0

**Aktuelle Testversion:** [Release 0.4.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.4.0) mit APK und Updatebeschreibung. Installierte Versionen ab 0.2.0 können das Update bei eingeschalteten Testversionen finden.

- 90 erwachsene Figuren mit eigener Persönlichkeit, Stimme, Ausgangsszene und Porträt: zehn pro Kategorie, jeweils fünf weibliche und fünf männliche.
- Neu: **Mittelerde, Blade Runner, Cyberpunk 2077 und Monster**, mit jeweils zehn Figuren. Die bisherigen fünf Kategorien bleiben enthalten.
- Eigene Figuren in den bekannten Welten übernehmen prägende Rollen und Vorgeschichten in alternativen Fanfiction-Handlungen. Die Dialoge sind neu verfasst; bekannte Enden und deine Entscheidungen bleiben offen.
- Monster mit eigenen Konflikten und körperlichen Grenzen, darunter grausame Oger, Dämonen, Untote und eine Riesenspinnenmatriarchin. Einige erwachsene Figuren tragen freizügigere Kleidung.
- Die vollständigen Persönlichkeiten und anfänglichen Ausgangsszenen werden an die KI übertragen. Aktueller Verlauf und Notizen haben Vorrang vor dem Einstieg.
- Eigene Figuren, mehrere lokale Geschichten und bearbeitbare Erinnerungen.
- App-Updates unter **Deine App → App-Updates → Nach Updates suchen**. **Testversionen einbeziehen** einschalten.
- ARM64, mindestens Android 12. Zielgerät ist ein Samsung Galaxy S24; tatsächliche Antwortqualität, Modellgeschwindigkeit und Langzeitverhalten müssen auf dem Gerät getestet werden.

Die [APK `Geschichten-0.4.0.apk`](https://github.com/Priestkiller/Geschichten/releases/download/v0.4.0/Geschichten-0.4.0.apk) und `geschichten-android-update.json` gehören zum [Release v0.4.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.4.0). Für die erste Installation oder den Wechsel von 0.1.0 die APK herunterladen und direkt öffnen. **Eine vorhandene Installation vorher nicht deinstallieren.**

Version 0.4.0 verwendet Versionscode 6, dieselbe Paket-ID und das bisherige Signaturzertifikat.

## Einrichtung und Daten

Die etwa 2,6 GB große Modelldatei wird einmal in der App heruntergeladen. Danach ist für neue Antworten keine Internetverbindung erforderlich. Chats, Figuren und Erinnerungen bleiben im privaten App-Speicher. Die Updatesuche überträgt keine Gesprächsinhalte.

Eine Aktualisierung mit derselben App-Signatur übernimmt bestehende App-Daten. Version 0.4.0 ergänzt fehlende Figuren. Alle 50 Profile aus 0.3.1, eigene Figuren und bearbeitete Fassungen bleiben erhalten. Vorhandene Geschichten, Chatnachrichten, Zusammenfassungen und Notizen werden nicht umgeschrieben. Eine Deinstallation oder das Löschen der App-Daten entfernt die lokalen Geschichten und das Modell.

## Dieses Repository

Dieses Repository dient der Verteilung signierter Test-APKs und der zugehörigen Updatebeschreibung. Hinweise zu den verwendeten Bibliotheken und zum Modell stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Die Versionshinweise im jeweiligen Release dokumentieren den veröffentlichten Stand. Die App gleicht Versionsnummer, Dateigröße, SHA-256-Prüfsumme, Paketkennung und die Signatur der installierten App ab, bevor sie eine APK an die Android-Installation übergibt.
