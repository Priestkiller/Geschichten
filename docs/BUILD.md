# Android-Build

## Projekt

- App: Geschichten, Teststand 0.8.8 (Versionscode 22, Hauptschema 10, Suchindexschema 1)
- Paket: `dev.vincent.geschichten`
- Native Android-App mit Kotlin und Jetpack Compose
- Mindestversion: Android 12 / API 31
- Ziel- und Compile-SDK: Android 15 / API 35
- Architektur der Test-APK: `arm64-v8a` (unter anderem Galaxy S24)

## Reproduzierbare Werkzeugversionen

| Werkzeug / Bibliothek | Festgelegte Version |
| --- | --- |
| Gradle Wrapper | 8.11.1 |
| Android Gradle Plugin | 8.9.2 |
| Kotlin + Compose Compiler Plugin | 2.4.0 |
| D8 / R8 | 9.1.56, explizit auf dem Build-Classpath |
| Java-Bytecode-Ziel | 17 |
| Android Build Tools | 35.0.0 (AGP-Standard) |
| Compose BOM | 2025.04.01 |
| Activity Compose | 1.10.1 |
| Lifecycle | 2.9.0 |
| Coroutines Android | 1.11.0 |
| LiteRT-LM Android | 0.17.1 |
| llama.cpp | v0.5.0 / d2e54583c7452353eb35d40431281f6ee984332f |
| SentencePiece | 0.2.1, statischer Tokenizer |

Die explizite D8/R8-Version ist erforderlich, weil LiteRT-LM 0.17.1 mit Kotlin 2.4 gebaut wurde. Der Gradle Wrapper prüft die heruntergeladene Gradle-Distribution anhand der in `gradle-wrapper.properties` hinterlegten SHA-256-Prüfsumme.

## Auf Windows bauen

1. Android Studio mit Android SDK Platform 35 und Build Tools 35.0.0 installieren.
2. Dieses Projektverzeichnis in Android Studio öffnen und den Gradle-Sync abschließen.
3. `BUILD_WINDOWS.cmd` starten. Alternativ im Projektverzeichnis `gradlew.bat :app:assembleDebug` ausführen.
4. Die APK liegt unter `app/build/outputs/apk/debug/app-debug.apk`. Der Helfer kopiert sie zusätzlich nach `Geschichten-0.8.1.apk` im Projektverzeichnis. Dieser Teststand wird nicht automatisch veröffentlicht.

Der Helfer erkennt das Java Development Kit in `JAVA_HOME` oder das mit Android Studio installierte JDK. Empfohlen werden JDK 17 oder 21. Er erkennt das Android SDK über `ANDROID_HOME`, `ANDROID_SDK_ROOT` oder den üblichen Installationsort `%LOCALAPPDATA%\Android\Sdk`.

Beim ersten Build ist eine Internetverbindung für Entwicklungsbibliotheken notwendig. Diese Verbindung gehört zum Build und ist unabhängig von der späteren lokalen Textgenerierung der App.

## Auf Linux bauen

Ein JDK 17 oder 21 und das Android SDK 35 bereitstellen. `ANDROID_HOME` auf das SDK setzen oder den SDK-Pfad in einer eigenen `local.properties` angeben. Anschließend:

```sh
./build-linux.sh
```

## GitHub-Updatequelle konfigurieren

Die App enthält eine integrierte Suche nach GitHub-Releases. Der Quellcode bekommt keine eingebauten Zugangstoken. Die vorgesehene öffentliche Updatequelle ist `Priestkiller/Geschichten`; sie steht als Standard in `gradle.properties`. Ein anderes öffentliches Repository kann beim Build als `owner/repository` angegeben werden:

```sh
./gradlew :app:assembleDebug -PupdateRepository=OWNER/REPOSITORY
```

Für die private Test-App ist das Berücksichtigen von Vorabversionen zunächst aktiviert; die App bietet dafür eine sichtbare Einstellung. Der Build-Standard kann mit `-PupdateIncludePrerelease=false` geändert werden. Der konfigurierte Repository-Name allein bedeutet noch nicht, dass ein GitHub-Repository oder eine Release-Datei veröffentlicht wurde. Der Stand der Bereitstellung und der tatsächlich geprüften Updatefunktionen wird im Prüfbericht getrennt ausgewiesen.

APK-Dateien werden ausschließlich im eigenen Update-Cache abgelegt. Die Installationsfreigabe gilt für diesen Ordner, nicht für den gesamten App-Speicher. Android zeigt den Installationsdialog erst nach der entsprechenden Aktion in der App.

Der Signaturschlüssel `signing/debug.keystore` gehört **nicht in das GitHub-Repository**. Er bleibt im privaten Arbeitsstand erhalten. APKs und Prüfsummen gehören zu den Release-Dateien. Für einen Build aus einem Repository-Klon muss der private Testschlüssel gesondert bereitgestellt werden, wenn die erzeugte APK als Update der vorhandenen privaten Version installiert werden soll.

## Test-APK installieren

Die APK auf ein kompatibles Android-Handy übertragen, dort öffnen und die angezeigte Installation bestätigen. Android kann die Installation aus dieser Dateiquelle zunächst sperren; in diesem Fall zeigt es den passenden Einstellungsdialog an.

Die Test-APK wird mit dem für dieses Projekt neu erzeugten Schlüssel `signing/debug.keystore` signiert. Dieser Testschlüssel ist im privaten Quellpaket enthalten, damit spätere Testversionen dasselbe Zertifikat verwenden und als Update installiert werden können. Sie ist kein Play-Store-Release. Für spätere Veröffentlichungen muss ein separater sicherer Release-Schlüssel eingerichtet werden.

## Validierung und Grenzen

Eine erfolgreich gebaute und signierte APK belegt noch keine geprüfte KI-Geschwindigkeit, Speichernutzung oder Temperaturentwicklung auf dem S24. Diese Punkte müssen am tatsächlichen Handy geprüft werden. Die konkrete Prüfung dieses Standes wird im separaten Prüfbericht dokumentiert.

### Optionale native UI- und Datenbankprüfungen

Die normalen JVM-Tests laufen über `:app:testDebugUnitTest`. Weitere Prüfungen mit Robolectric und Roborazzi werden gezielt aktiviert. Beim Wechsel zwischen den beiden Profilen zuerst `:app:clean` ausführen, damit keine zuvor kompilierten Testklassen des anderen Profils erhalten bleiben:

```sh
./gradlew :app:clean :app:testDebugUnitTest -PvisualTests=true
```

Dieser Aufruf rendert die tatsächlichen Compose-Oberflächen mit Android-Native-Graphics, prüft die lokale SQLite-Speicherung und startet die reale MainActivity inklusive ViewModel. Die Modell-Einrichtung wird dabei geöffnet, aber weder ein Modell heruntergeladen noch eine KI-Antwort erzeugt. Die Renderbilder liegen unter `app/build/ui-screenshots`.

Das Profil ist absichtlich optional. Vor der Weitergabe einer APK wieder einen normalen Build ohne `-PvisualTests=true` ausführen, damit keine Activity aus dem Compose-Testmanifest in der Test-APK enthalten ist:

```sh
./gradlew :app:clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Offizielle Referenzen

- [AGP 8.9 – Kompatibilität mit Gradle 8.11.1 und JDK 17](https://developer.android.com/build/releases/agp-8-9-0-release-notes)
- [Kotlin-Gradle-Kompatibilität](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Erforderliche D8/R8-Versionen für Kotlin](https://developer.android.com/build/kotlin-support)
- [Gradle 8.11.1 Release Notes](https://docs.gradle.org/8.11.1/release-notes.html)
- [LiteRT-LM Android 0.17.1 – Maven-Metadaten](https://dl.google.com/dl/android/maven2/com/google/ai/edge/litertlm/litertlm-android/0.17.1/litertlm-android-0.17.1.pom)

## Öffentlicher Quellcode ab 0.8.7

Der private bestehende Signierschlüssel und die lokale Datei Build-Umgebung.ps1 sind nicht im Repository. JDK/SDK selbst einrichten; für eigene Signaturen siehe [signing/README.md](../signing/README.md). Mit installiertem SDK/JDK: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon` (Windows), entsprechend `./gradlew` auf Linux. Der Updatequellenwert steht in gradle.properties.

## Ollama-Version 0.8.8

Die neuen reinen JVM-Prüfungen für HTTP, Abbruch, Rollen und Antwortende gehören zum regulären Testlauf. Für UI und SQLite aus dem öffentlichen Klon ist das portable Profil verfügbar:

```sh
./gradlew :app:clean :app:testDebugUnitTest -PvisualTests=true -PportableVisualTests=true
```

Dieses ausdrücklich gewählte Profil lässt vier historische Diagnoseklassen aus: `AnswerPipelineTest`, `OwnershipAdverbPersistenceTest` und `RoleContaminationInvestigationTest` benötigen nicht veröffentlichte frühere Messdateien beziehungsweise eine damalige SQLite-Datei; `ActiveMemoryModelFixturesTest` erzeugt einen alten Wiederholungstext, den der aktuelle Antwortfilter zurückweist. Ohne `portableVisualTests` bleibt das vollständige bisherige Profil erhalten. Fünf weitere optionale Modellproben melden bei fehlenden privaten Eingaben einen Skip.

`OllamaAppFlowIntegrationTest` kann zusätzlich den wirklichen lokalen Ollama-Server aufrufen: Test-JVM-Systemproperty `ollamaLive=true` und `ollamaTokenizerExecutable` auf das Windows-Werkzeug `llama-tokenize.exe` setzen. Dies ist ein expliziter Live-Test mit synthetischen Geschichten, keine Verbindung zum Speicher eines echten Handys. Die normale Ausführung überspringt diesen Live-Fall. Die Windows-Prüfung verwendet den Produktions-HTTP-Pfad, echte App-Kontextplanung und SQLite; die Android-JNI-Vokabulardatei wird mit dem gleichen nativen Tokenizercode geprüft.

Vor einer weiterzugebenden APK wieder normal ohne visuelles Testmanifest bauen:

```sh
./gradlew :app:clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```
