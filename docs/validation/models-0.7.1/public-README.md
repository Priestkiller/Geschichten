# Geschichten

Geschichten ist eine native Android-Testapp für Gespräche und Abenteuer mit KI-Figuren. Die Antworten entstehen nach dem einmaligen Modelldownload auf dem Handy.

## Android-Testversion 0.7.1

**Aktuelle Testversion:** [Release 0.7.1](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.1) mit APK und Updatebeschreibung. Installierte Versionen ab 0.2.0 können das Update bei eingeschalteten Testversionen finden.

- **Neu: verständliche Beschreibungen für alle sechs auswählbaren Modelle**, mit Einsatzgebieten, Grenzen, Downloadgröße und Anbieterlinks.
- 90 erwachsene Figuren: zehn pro Kategorie, jeweils fünf weibliche und fünf männliche, mit eigenem Porträt, Persönlichkeit und Sprechweise.
- Ausführliche Chat-Einstiege für alle 90 Figuren, jeweils etwa 450 bis 550 Wörter. Du erfährst deine Rolle, eure mögliche Vorgeschichte und warum die Figur dich jetzt anspricht. Die Szene führt direkt zu ihren ersten Worten; Beschreibungen erscheinen kursiv, direkte Rede hervorgehoben.
- **Profil** auf der Figurenkarte und **⋮ → Persönlichkeit ansehen** im Chat zeigen, wer die Figur ist, ihre Eigenschaften, Schwächen und Sprechweise. Der Geschichteneinstieg erscheint beim Start eines neuen Chats.
- Laufende Gespräche öffnen direkt bei der letzten Nachricht. Der vollständige Einstieg bleibt am Anfang gespeichert.
- Vier Reiter: **Figuren · Verlauf · Erinnerungen · Einstellungen**. Der Verlauf enthält deine gespeicherten Geschichten.
- Nordische Fantasy, Fantasy, Krimi, Science-Fiction, Kreaturen, Mittelerde, Blade Runner, Cyberpunk 2077 und Monster.
- Eigene Figuren in den bekannten Welten übernehmen prägende Rollen und Vorgeschichten in alternativen Fanfiction-Handlungen. Die Dialoge sind neu verfasst; bekannte Enden und deine Entscheidungen bleiben offen.
- Monster mit eigenen Konflikten und körperlichen Grenzen, darunter grausame Oger, Dämonen, Untote und eine Riesenspinnenmatriarchin. Einige erwachsene Figuren tragen freizügigere Kleidung.
- Eigene Figuren, mehrere lokale Geschichten und bearbeitbare Erinnerungen. Aktueller Verlauf und Notizen haben Vorrang vor dem Einstieg.
- App-Updates unter **Einstellungen → App-Updates → Nach Updates suchen**. **Testversionen einbeziehen** einschalten. Ältere Versionen haben ihre bisherige Updateansicht.
- ARM64, mindestens Android 12. Zielgerät ist ein Samsung Galaxy S24; tatsächliche Antwortqualität, Modellgeschwindigkeit und Langzeitverhalten müssen auf dem Gerät getestet werden.

Die [APK `Geschichten-0.7.1.apk`](https://github.com/Priestkiller/Geschichten/releases/download/v0.7.1/Geschichten-0.7.1.apk) und `geschichten-android-update.json` gehören zum [Release v0.7.1](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.1). Für die erste Installation oder den Wechsel von 0.1.0 die APK herunterladen und direkt öffnen. **Eine vorhandene Installation vorher nicht deinstallieren.**

Version 0.7.1 verwendet Versionscode 10, dieselbe Paket-ID und das bisherige Signaturzertifikat.

## Einrichtung und Daten

Unter **Einstellungen → Welche KI passt zu dir?** stehen sechs Modelle zur Auswahl. Wähle eines aus und tippe oben auf **KI herunterladen**. Bereits heruntergeladene Modelle lassen sich offline wechseln; die Auswahl bleibt beim nächsten App-Start erhalten. Deine Geschichten, Figuren und Erinnerungen sowie die anderen Downloads bleiben beim Wechsel erhalten. Während eine Antwort oder Einrichtung läuft, ist der Wechsel gesperrt.

| Modell | Download, ungefähr |
| --- | ---: |
| Gemma 4 E2B – bisherige KI | 2,59 GB |
| Qwen 2.5 1.5B | 1,60 GB |
| Qwen 3 0.6B | 345 MB |
| Dolphin 3.0 Llama 3.2 3B | 2,02 GB |
| Huihui Qwen 3 4B Instruct 2507 | 2,50 GB |
| Gemma 3 4B DBL-X von DavidAU | 2,58 GB |

Die ausführlicheren Beschreibungen helfen beim Ausprobieren. Es gibt keine vorgegebene Qualitätsrangliste: Welche Antworten für deine Figur passen, entscheidest du selbst. Für den Modellvergleich wurden keine neuen Geschichten oder Beispielantworten in die App eingebaut. Die vorhandene Gemma-Datei kann weiterverwendet werden. Nur gewählte Modelle werden heruntergeladen. Die Downloadgröße ist nicht der gesamte Arbeitsspeicherbedarf; hinzu kommen Kontext und Laufzeit.

Jedes Modell wird vor dem Laden anhand seiner festgelegten SHA-256-Prüfsumme geprüft. Danach ist für neue Antworten keine Internetverbindung erforderlich. Chats, Figuren und Erinnerungen bleiben im privaten App-Speicher. Die Updatesuche überträgt keine Gesprächsinhalte.

Eine Aktualisierung mit derselben App-Signatur übernimmt bestehende App-Daten. Seit Version 0.6.0 erhalten unveränderte mitgelieferte Profile und vollständig unberührte Starts ohne eigene Antwort die ausführlichen Einstiege. Bereits gespielte Gespräche, eigene Figuren und bearbeitete Fassungen bleiben erhalten. Laufende Gespräche öffnen bei der letzten Nachricht. Für einen neuen Einstieg bei einer bereits gespielten Geschichte **Neue Geschichte** im Chatmenü wählen. Die Ausgangslage wird pro neuer Geschichte gespeichert, damit die KI deine anfängliche Rolle auch bei längerem Verlauf berücksichtigen kann. Eine Deinstallation oder das Löschen der App-Daten entfernt die lokalen Geschichten und das Modell.

## Dieses Repository

Dieses Repository dient der Verteilung signierter Test-APKs und der zugehörigen Updatebeschreibung. Hinweise zu den verwendeten Bibliotheken und zum Modell stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Die Versionshinweise im jeweiligen Release dokumentieren den veröffentlichten Stand. Die App gleicht Versionsnummer, Dateigröße, SHA-256-Prüfsumme, Paketkennung und die Signatur der installierten App ab, bevor sie eine APK an die Android-Installation übergibt.

### Modellbeschreibungen in 0.7.1

Alle sechs Modellkarten erklären jetzt ausführlicher, für welche Schreibaufgaben das jeweilige Modell interessant sein kann, was du selbst vergleichen solltest und welche Grenzen es gibt. Links führen getrennt zur Beschreibung des Anbieters und zur eingebundenen Downloadquelle mit Lizenz. Die Größen beziehen sich auf die konkreten Dateien; DavidAUs Gemma-3-Datei hat etwa 2,58 GB. Ein bestimmter Erzählstil oder eine höhere Geschwindigkeit auf dem S24 werden nicht garantiert.
