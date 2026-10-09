# Prüfbericht – Geschichten 0.7.3

Prüfdatum: 4. Oktober 2026. Android-Testversion 0.7.3, Versionscode 12, Datenbankversion 7.

## Änderung und Ziel

Das Update richtet sich gegen die lange Wartezeit bis zum ersten Antworttext. Für alle sechs auswählbaren KIs vergleicht „Einstellungen → Schneller zum ersten Text → Geschwindigkeit optimieren“ die CPU-Einstellungen direkt auf dem jeweiligen Gerät. Galaxy S24, S24+ und S24 Ultra werden anhand ihrer Modellkennung angezeigt; die Auswahl beruht auf tatsächlichen Messungen und nicht auf einer behaupteten pauschalen Rangliste für Exynos oder Snapdragon.

Verglichen werden vier, zwei und sechs Threads, soweit das Gerät entsprechend viele Prozessoren meldet. Nach einer kurzen Aufwärmanfrage laufen zwei neutrale, identische Anfragen je Einstellung. Sie verwenden acht Ausgabetoken und einen längeren Bibliothekstext für die Vorverarbeitung. GGUF-Kontext wird zwischen diesen Messanfragen gelöscht. Die App übernimmt eine andere Einstellung nur bei mehr als zehn Prozent Verbesserung gegenüber vier Threads. Die Einstellung und Messergebnisse werden pro Modell, Dateirevision und Gerätesoftware gespeichert. Die kurzen Messantworten werden weder in Geschichten noch in Entwürfe geschrieben. Bei zu warmem Gerät bricht die Prüfung ab; bei Fehler oder Abbruch versucht die App, die bisherige Einstellung wieder zu laden. Die Messung der letzten normalen Chatantwort wird separat angezeigt; die automatische Zusammenfassung überschreibt sie nicht.

Für Dolphin, Huihui und Gemma 3 hält die JNI-Laufzeit erfolgreich verarbeitete Kontexttoken im Arbeitsspeicher. Sie verwendet nur exakt identische, vollständige 256-Token-Blöcke wieder. Abweichende Enden werden entfernt und neu verarbeitet. Wenn der native Speicher verschoben wurde, unvollständig ist oder das Entfernen scheitert, wird vollständig neu gerechnet. Abbrüche, Fehler und Fehler in Streaming-Callbacks verwerfen den Cache. „Verlauf leeren“ entfernt auch den aktiven GGUF-Kontext. Dadurch wird keine fremde Szene aus einem vorherigen Gespräch übernommen.

LiteRT-Gespräche werden weiterhin frisch angelegt, weil sich ihr Systemkontext pro Antwort ändern kann. Die bisherigen Modellgewichte, der Kontext von 4.096 Token und die Produktionsgrenze von 512 Ausgabetoken bleiben erhalten. Es wird kein neuer GPU- oder NPU-Pfad aktiviert. Die sichtbaren langen Einführungen werden nicht gekürzt.

## Automatische Prüfungen

Der endgültige Produktionsbuild mit `assembleDebug testDebugUnitTest lintDebug`, Updatequelle Priestkiller/Geschichten und Testversionen eingeschaltet besteht. 73 JVM-Tests bestehen ohne Fehler, Ausfälle oder übersprungene Fälle. Zusätzlich bestehen 17 gezielte Robolectric-/Compose- und Speicherintegrationstests für die neue Messansicht, Abbrechen, Schutz während laufender Vorgänge, Wiederherstellung gespeicherter Einstellungen, Modellwahl und Modelllöschung, tatsächliche Verlaufslöschung, App-Start und Updatesuche. Die Oberflächentests verwenden denselben abschließenden Quellstand; die drei Updatesuche-Tests wurden separat ausgeführt.

Android Lint meldet keine Fehler und 119 Warnungen. Die APK ist mit dem bisherigen Zertifikat signiert; Paketkennung, Mindestversion, Versionscode und 16-KiB-ZIP-Ausrichtung sind geprüft. Von den drei ARM64-Bibliotheken ändert sich ausschließlich `libgeschichten_gguf.so`. Modellgewichte, private Schlüssel und Quellcode werden nicht als Release-Dateien hochgeladen. Die Logit-Diagnose ist nur im privaten Windows-Prüfbuild eingeschaltet und fehlt in der Android-Bibliothek.

Die Messkarte wurde anhand einer gespeicherten UI-Aufnahme visuell geprüft. Die dort genannten Sekunden sind ausdrücklich gekennzeichnete Darstellungs-Testdaten, keine Messung eines S24 Ultra.

## Native Modellprüfungen

Mit den sechs vollständigen vorhandenen Modelldateien bestehen 18 native Windows-CPU-Proben: jeweils zwei, vier und sechs Threads. Sie prüfen Laden, tatsächliche Textgenerierung und UTF-8-Streaming mit den gleichen CPU-Thread-Einstellungen. Die kurzen Prüfantworten dienen der Laufzeitprüfung und sind keine Qualitätsbewertung von Geschichten. Die gemessenen Desktop-Zeiten werden nicht als Empfehlungen für die Zielhandys benutzt.

Die Cache-Prüfung vergleicht die vollständigen nächsten Token-Logits aus frischer Verarbeitung und Wiederverwendung, einschließlich identischer Anfrage, geändertem Gesprächsende und anderer Geschichte. Weitere Fälle prüfen die erneute Verarbeitung nach Abbruch und absichtlichem Callback-Fehler. Die Grenze beträgt 0,05 absolute Logit-Abweichung; eine beliebige neue Textausgabe allein gilt nicht als Beweis eines korrekten Caches. Die Prüfdaten und der endgültige Status stehen in [summary.json](validation/performance-0.7.3/summary.json).

Die erste Variante mit beliebiger Wiederverwendung bis kurz vor das letzte Token zeigte bei Dolphin eine Abweichung von 0,553. Ursache waren unterschiedliche quantisierte Rechenpfade bei Einzel- und Blockverarbeitung. Die endgültige Variante behält die ursprünglichen 256-Token-Blockgrenzen bei. Für Gemma 3 wurde der Bibliothekstext verlängert, damit auch nach Ändern der Frage mindestens ein vollständiger identischer Block übrig bleibt; die kürzere Prüfvariante wurde konservativ vollständig neu gerechnet.

## Grenzen

Es war kein physisches S24 oder S24 Ultra per ADB verbunden. Installation, tatsächliche Geschwindigkeit, Arbeitsspeicherbedarf und Langzeitverhalten auf beiden Geräten sind daher nicht nachgewiesen. Die App führt den relevanten Vergleich auf dem jeweiligen Handy durch. Die neutralen Messanfragen sind eine Momentaufnahme; längere reale Gespräche, Hitze und andere Apps können andere Zeiten ergeben. Auch die numerische Gleichheit des Caches auf ARM64 wird durch Windows-Proben nicht bewiesen.

Beim ersten Chat muss der Kontext vollständig verarbeitet werden. Änderungen in den Anfangsblöcken, ein Modellwechsel, verschobener nativer Kontext oder Abbrüche können erneut vollständige Verarbeitung erfordern. Es gibt keine pauschale Beschleunigungszusage und keine neue Garantie für widerspruchsfreie Modellantworten.

## Paket

- APK: `Geschichten-0.7.3.apk`, 314.124.498 Byte.
- SHA-256: `20e85af6795a1fb7ce7f2e69a11e6fdff663311f8e0c2aacc96c19ac964ffacd`.
- Zertifikat SHA-256: `3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40`.
- Paket `dev.vincent.geschichten`, Android 12 / minSdk 31, ARM64, Versionscode 12.

Nachweise: [Build](validation/performance-0.7.3/final-build.log), [Oberflächentests](validation/performance-0.7.3/final-ui-tests.log), [Updatesuche-Oberflächentests](validation/performance-0.7.3/final-update-ui-tests.log), [CPU-Einstellungen](validation/performance-0.7.3/thread-configurations.json). Die Veröffentlichung und anonyme Prüfung sind abgeschlossen.

## Veröffentlichung

[Release v0.7.3](https://github.com/Priestkiller/Geschichten/releases/tag/v0.7.3) ist öffentlich als Testversion veröffentlicht, Release-ID 403177506. Die zwei hochgeladenen Dateien sind die geprüfte APK und die 317 Byte große Updatebeschreibung. Die öffentliche README wurde im Commit `e68ed38c84e18cc895b6656dae2a46dff4547f0e` aktualisiert.

Der vollständige anonyme APK-Download stimmt in Größe und SHA-256 mit der geprüften lokalen Datei überein. Manifest und README stimmen bytegenau mit den vorbereiteten Dateien überein. Der tatsächlich kompilierte App-Parser erkennt Version 0.7.3 (Code 12) als passendes Update für 0.7.2 (Code 11), wenn Testversionen eingeschaltet sind.

Alle drei endgültigen nativen Cache-Proben bestehen je acht Fälle; die maximale verglichene Logit-Abweichung beträgt jeweils 0,0. Dies belegt die geprüften Windows-Rechenpfade und ist kein physischer Handy-Benchmark. Details: [öffentliche Prüfung](validation/performance-0.7.3/public-release-summary.json), [App-Updateparser](validation/performance-0.7.3/public-app-parser.log), [Dolphin](validation/performance-0.7.3/dolphin-cache.json), [Huihui](validation/performance-0.7.3/huihui-cache.json), [Gemma 3](validation/performance-0.7.3/gemma3-cache.json).
