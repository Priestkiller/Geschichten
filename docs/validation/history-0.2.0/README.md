# Prüfbelege für Geschichten 0.2.0

`summary.json` enthält den zusammengefassten Endstand. Alle `normal-TEST-*.xml` stammen aus dem abschließenden normalen Build. Die `visual-TEST-*.xml` enthalten je Testklasse das letzte bestandene Ergebnis: der vollständige erweiterte Profil-Lauf wurde für die zusätzliche Kreaturenfilter-Aufnahme durch einen gezielten Lauf von `UiScreenshotTest` ergänzt.

`visual-build.txt` dokumentiert den vollständigen Erweiterungslauf mit 62 bestandenen Testfällen und einem zunächst fehlschlagenden neuen Testselektor. Der Selektor verwendete einen beim Scrollen verschwindenden Genre-Chip. Er wurde auf den stabilen horizontalen Scrollbereich umgestellt; `visual-ui-confirmation.txt` und das neue UI-JUnit-XML belegen danach alle neun UI-Tests als bestanden. Die App musste dafür nicht geändert werden. Der Endstand umfasst 63 verschiedene bestandene Testfälle, keine übersprungenen Tests.

`normal-build.txt` dokumentiert den abschließenden erfolgreichen APK-/Test-/Lint-Aufruf ohne das optionale visuelle Testprofil. Weil temporäre Dateisynchronisationsdateien zweimal in generierte Ressourcen gerieten, wurden ausschließlich die Buildausgaben dieses letzten Laufs mit einem lokalen Gradle-Init-Skript nach `/tmp/geschichten-build-020` verlegt. Der App-Quellstand blieb unverändert und ist durch `source-sha256.txt` festgehalten. Diese technische Ausweichmaßnahme wird auf einem gewöhnlichen lokalen Android-Studio-Build nicht benötigt.

APK- und ELF-Prüfungen beziehen sich auf die endgültige Datei `Geschichten-0.2.0.apk`. Der öffentliche GitHub-Upload ist noch nicht freigegeben und nicht als durchgeführt dokumentiert. Die Bilddateien zeigen native Compose-Oberflächen in Robolectric, keine physische S24-Aufnahme und keinen Nachweis von Modell-Inferenz.
