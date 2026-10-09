# Prüfung von Geschichten 0.8.0

Lokaler Debug-Teststand vom 05.10.2026, Versionscode 14, Datenbankversion 9. Das neue **Gedächtnis** speichert den aktuellen Geschichtenstand, frühere Fassungen, Quellen und Figurenwissen dauerhaft in der bisherigen SQLite-Datei. Korrektur, Anheften, Ausschließen und eigene Festlegung sind in der Oberfläche erreichbar. Alte Chats, manuelle Notizen, Figuren, Profile und Entwürfe bleiben erhalten. Es gibt kein Training und keine Veröffentlichung.

Die signierte [Geschichten-0.8.0.apk](../Geschichten-0.8.0.apk) kann als Update über die bisherige App installiert werden. Paket und bestehender Signierer sind geprüft. Nicht vorher deinstallieren.

| Prüfung | Ergebnis |
| --- | --- |
| Reguläre abschließende Tests | 103 bestanden |
| Erweiterte Android-/Compose-Prüfungen | 181 bestanden, zwei auf Windows ausgelassen, kein Fehler |
| Gezielte Datenbank-/UI-/ViewModel-Abschlussprüfung | 60 bestanden |
| Exakte Token-ID-Folgen | 177 Vergleiche identisch |
| Finale echte Modellinferenz | Sechs Modelle, 36 Antworten; alle 36 Promptzählungen identisch mit der Laufzeit |
| Lint | Null Fehler, 125 Warnungen |
| APK | Erfolgreicher Produktionsbuild ohne visuelles Testprofil; v2-signiert, ARM64, 16-KiB-Ausrichtung geprüft |

Die Testläufe überschneiden sich. Die letzten zusätzlichen Parserregeln wurden im regulären Abschlusslauf geprüft; das vollständige erweiterte Profil wurde danach nicht nochmals ausgeführt. Die zwei ausgelassenen FileProvider-Dateipfadfälle benötigen Android/Linux, weil Robolectric auf Windows andere Pfadtrenner verwendet. Ein S24 war nicht verbunden; Geräteinstallation, lange echte Chats, Hintergrund/Vordergrund, Prozessneustart, Abbruch unter Gerätespeicherdruck und RAM-/Zeitmessung bleiben offen.

**Gespeichert und geladen:** Die deterministischen Tests bestätigen Gegenstandsübergabe, Farbe ohne Duplikat, Ablage, fortbestehende Verletzungen, abgeschlossene Ziele, storygetrennte Zustände, manuelle Korrektur, Wissenserwerb, Ausschlüsse, Migration, vollständige Quellen, Versionierung und atomare Antwortübernahme. Neue Fakten aus einem fehlgeschlagenen Antwortversuch werden nicht bestätigt. Die automatische Erkennung bleibt auf bestimmte deutsche Ausdrucksformen begrenzt; unerkannte freie Prosa benötigt gegebenenfalls eine eigene Festlegung.

**Vom Modell verwendet:** Gemma 4 und Huihui verwendeten im langen kontrollierten Verlauf die aktuellen Kernfakten deutlich besser. Die Wiederholung und andere Situationen zeigen weiterhin falsche Rollen, erfundene Übergaben, falsche Verletzungen und schlechte Sprache. Einige dieser Fehler werden zurückgewiesen, andere passieren die begrenzte Antwortprüfung. Das Gedächtnis ist kein Nachweis fehlerfreier Geschichten.

Der ausführliche [Gedächtnisbericht](dauerhaftes-gedaechtnis.md) enthält Datenmodell, Migration, Ablauf, Bedienung, alle Abnahmefälle, echte Vorher-/Nachher-Antworten und die verbleibenden Grenzen. Rohbelege liegen unter `docs/validation/dauerhaftes-gedaechtnis/`; `model-assessment.json` trennt tatsächlichen Inhalt von der Annahme durch einen Filter.

APK: 321373977 Bytes, SHA-256 `0b16aaf8831b24a1935351c78067af2f51ddb6b206f3cc29d09b980eff3f4a17`. Signierzertifikat SHA-256 `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`. Die APK enthält keine neuen Modellgewichte und keine Prüfklassen. Die GitHub-Updatesuche kann diesen ausschließlich lokalen Stand nicht finden.
