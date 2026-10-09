# Geschichten

Geschichten ist eine native Android-Testapp für Gespräche und Abenteuer mit KI-Figuren. Die Antworten entstehen nach dem einmaligen Modelldownload auf dem Handy.

## Android-Testversion 0.6.0

**Aktuelle Testversion:** [Release 0.6.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.6.0) mit APK und Updatebeschreibung. Installierte Versionen ab 0.2.0 können das Update bei eingeschalteten Testversionen finden.

- 90 erwachsene Figuren: zehn pro Kategorie, jeweils fünf weibliche und fünf männliche, mit eigenem Porträt, Persönlichkeit und Sprechweise.
- **Neu: ausführliche Chat-Einstiege für alle 90 Figuren, jeweils etwa 450 bis 550 Wörter.** Du erfährst deine Rolle, eure mögliche Vorgeschichte und warum die Figur dich jetzt anspricht. Die Szene führt direkt zu ihren ersten Worten; Beschreibungen erscheinen kursiv, direkte Rede hervorgehoben.
- **Profil** auf der Figurenkarte und **⋮ → Persönlichkeit ansehen** im Chat zeigen, wer die Figur ist, ihre Eigenschaften, Schwächen und Sprechweise. Der Geschichteneinstieg erscheint beim Start eines neuen Chats.
- Laufende Gespräche öffnen direkt bei der letzten Nachricht. Der vollständige Einstieg bleibt am Anfang gespeichert.
- Vier Reiter: **Figuren · Verlauf · Erinnerungen · Einstellungen**. Der Verlauf enthält deine gespeicherten Geschichten.
- Nordische Fantasy, Fantasy, Krimi, Science-Fiction, Kreaturen, Mittelerde, Blade Runner, Cyberpunk 2077 und Monster.
- Eigene Figuren in den bekannten Welten übernehmen prägende Rollen und Vorgeschichten in alternativen Fanfiction-Handlungen. Die Dialoge sind neu verfasst; bekannte Enden und deine Entscheidungen bleiben offen.
- Monster mit eigenen Konflikten und körperlichen Grenzen, darunter grausame Oger, Dämonen, Untote und eine Riesenspinnenmatriarchin. Einige erwachsene Figuren tragen freizügigere Kleidung.
- Eigene Figuren, mehrere lokale Geschichten und bearbeitbare Erinnerungen. Aktueller Verlauf und Notizen haben Vorrang vor dem Einstieg.
- App-Updates unter **Einstellungen → App-Updates → Nach Updates suchen**. **Testversionen einbeziehen** einschalten. Ältere Versionen haben ihre bisherige Updateansicht.
- ARM64, mindestens Android 12. Zielgerät ist ein Samsung Galaxy S24; tatsächliche Antwortqualität, Modellgeschwindigkeit und Langzeitverhalten müssen auf dem Gerät getestet werden.

Die [APK `Geschichten-0.6.0.apk`](https://github.com/Priestkiller/Geschichten/releases/download/v0.6.0/Geschichten-0.6.0.apk) und `geschichten-android-update.json` gehören zum [Release v0.6.0](https://github.com/Priestkiller/Geschichten/releases/tag/v0.6.0). Für die erste Installation oder den Wechsel von 0.1.0 die APK herunterladen und direkt öffnen. **Eine vorhandene Installation vorher nicht deinstallieren.**

Version 0.6.0 verwendet Versionscode 8, dieselbe Paket-ID und das bisherige Signaturzertifikat.

## Einrichtung und Daten

Die etwa 2,6 GB große Modelldatei wird einmal in der App heruntergeladen. Danach ist für neue Antworten keine Internetverbindung erforderlich. Chats, Figuren und Erinnerungen bleiben im privaten App-Speicher. Die Updatesuche überträgt keine Gesprächsinhalte.

Eine Aktualisierung mit derselben App-Signatur übernimmt bestehende App-Daten. Version 0.6.0 aktualisiert unveränderte mitgelieferte Profile und vollständig unberührte Starts ohne eigene Antwort. Bereits gespielte Gespräche, eigene Figuren und bearbeitete Fassungen bleiben erhalten. Laufende Gespräche öffnen bei der letzten Nachricht. Für einen neuen Einstieg bei einer bereits gespielten Geschichte **Neue Geschichte** im Chatmenü wählen. Die Ausgangslage wird pro neuer Geschichte gespeichert, damit die KI deine anfängliche Rolle auch bei längerem Verlauf berücksichtigen kann. Eine Deinstallation oder das Löschen der App-Daten entfernt die lokalen Geschichten und das Modell.

## Dieses Repository

Dieses Repository dient der Verteilung signierter Test-APKs und der zugehörigen Updatebeschreibung. Hinweise zu den verwendeten Bibliotheken und zum Modell stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Die Versionshinweise im jeweiligen Release dokumentieren den veröffentlichten Stand. Die App gleicht Versionsnummer, Dateigröße, SHA-256-Prüfsumme, Paketkennung und die Signatur der installierten App ab, bevor sie eine APK an die Android-Installation übergibt.
