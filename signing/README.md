# Signierung eigener Testbuilds

Der bestehende private Testschlüssel `debug.keystore` ist nicht im Repository. Die veröffentlichten APKs sind weiterhin mit diesem Schlüssel signiert und können deshalb vorhandene Installationen aktualisieren.

Für einen eigenen Testbuild lässt sich mit dem JDK ein eigener Schlüssel im Projektordner erzeugen:

```sh
keytool -genkeypair -keystore signing/debug.keystore -storetype JKS -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Geschichten Local Test"
```

Dieser eigene Schlüssel hat eine andere Signatur und ist kein kompatibles Update für die veröffentlichte App. Eigene Tests auf einem getrennten Testgerät oder Emulator durchführen. Keystore-Dateien bleiben durch `.gitignore` ausgeschlossen. Einen eigenen Build nicht als offizielles GitHub-Testupdate veröffentlichen.
