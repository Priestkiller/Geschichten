# GitHub-Testupdates für Geschichten

Quelle: [Priestkiller/Geschichten](https://github.com/Priestkiller/Geschichten). Aktuelle Veröffentlichung: [0.8.7](https://github.com/Priestkiller/Geschichten/releases/tag/v0.8.7), Versionscode 21, Kanal Test. Frühere Berichte beschreiben ihre damaligen lokalen Entwicklungsstände.

Unter **Einstellungen → App-Updates** die **Testversionen einbeziehen** einschalten und **Nach Updates suchen** antippen. Die Veröffentlichung enthält die signierte `Geschichten-0.8.7.apk` und die `geschichten-android-update.json`. Beide gehören zum selben Release. Android bestätigt die Installation; die vorhandene App vorher nicht deinstallieren.

Ein Tag, eine Datei auf `main` oder ein Release-Entwurf reichen für die Updatesuche nicht. Das vollständige Testrelease muss `draft:false`, `prerelease:true` und eine Beschreibung haben. Das Manifest nennt Kanal `test`, Paket-ID, Versionscode/-name, Android-Mindestversion und genaue APK-Bytegröße/SHA-256. Die App wählt nach Versionscode und prüft APK-Metadaten und die Signatur gegen die installierte App.

Das zusätzliche Server-Übergabepaket ist kein APK-Update. Der Android-Quellcode liegt auf `main`; Signierschlüssel und lokale Inferenzgewichte werden nicht veröffentlicht.

## Spätere Testupdates vorbereiten

Zuerst den Versionscode und Versionsnamen erhöhen, den APK-Stand abschließend prüfen und mit dem bestehenden privaten Testschlüssel signieren. Einen eigenen neu erzeugten Schlüssel nicht für kompatible Updates verwenden.

Das lokale Hilfsskript liest die fertige APK und prüft Version, Paket, ARM64 und den bestehenden Signierer. Mit eingerichteten Java-/Android-SDK-Pfaden im Projektordner:

```sh
python scripts/prepare-update.py --apk Geschichten-0.8.7.apk --version-name 0.8.7 --version-code 21 --output geschichten-android-update.json
```

Bei Bedarf `--sdk` angeben. Keine APK nach dem Erzeugen des Manifests verändern. Erst beide geprüften Assets vollständig an einen Release-Entwurf anhängen, danach den Entwurf veröffentlichen. README und Versionshinweise müssen den tatsächlichen Stand und offene Fehler beschreiben.

Anschließend anonym die veröffentlichten Release-Metadaten, Manifest-Bytes, volle APK-Bytegröße und SHA-256 sowie den Tag/Quellstand prüfen. Dieser HTTP-Nachweis ersetzt nicht den vollständigen Upgrade-Ablauf auf einem physischen Android-Gerät.

Die Updatesuche überträgt keine Gesprächsinhalte an GitHub. Das eingerichtete Textmodell schreibt weiter lokal. Eine Ollama-Anbindung der Android-App ist in 0.8.7 noch nicht umgesetzt.
