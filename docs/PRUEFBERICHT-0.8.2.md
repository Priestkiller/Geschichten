# Prüfung von Geschichten 0.8.2

Lokaler Teststand vom 06.10.2026, Versionscode 16, Datenbankversion 9. Die signierte [Geschichten-0.8.2.apk](../Geschichten-0.8.2.apk) kann mit derselben Paket-ID und demselben Testsignierer als Update installiert werden. Nicht vorher deinstallieren. Es wurde kein GitHub-Update veröffentlicht.

Die gemeinsame Antwortprüfung erkennt mehr vertauschte Rollen, falsche Verletzungszuordnungen, verwechselte Familienbeziehungen, zusätzliche Eigentumsbehauptungen und bestimmte erfundene gemeinsame Vergangenheiten. Eigentümer und Träger werden in vorhandenen Faktenzeilen getrennt behandelt. Ausdrückliches „nicht verletzt“ wird korrekt gespeichert und vor der Antwort übergeben. Originalnachrichten und bestehende Korrekturen bleiben erhalten.

124 reguläre Unit-Tests und 62 gezielte Android-/SQLite-/ViewModel-/UI-/Migrationsprüfungen bestehen; die Läufe überschneiden sich in 14 Tests. Android Lint: null Fehler, 125 Warnungen und eine Information. Die Datenbankimplementierung, nativen Bibliotheken, Tokenizer und alle App-Assets sind bytegleich mit 0.8.1. Die langen Einführungen, Figuren, Modelle und Samplingwerte bleiben erhalten.

Die kontrollierte Diagnose reproduziert die endgültigen Briefantworten aus 0.8.1 wortgleich. 36 A/B/C-Antworten, zwei automatische Versuche mit jeweils zwölf Antworten und zwölf endgültige Antworten sind getrennt bewertet. Die neue Prüfung erkennt zwölf der sechzehn fehlerhaften Originalantworten; beide richtigen Originalantworten und acht weitere richtige Formulierungen werden akzeptiert. Vier alte fehlerhafte Antworten rutschen weiter durch.

Der endgültige Lauf liefert eine insgesamt brauchbare Gemma-Antwort und zwei brauchbare Huihui-Antworten bei je sechs Prüfantworten. Huihui verwendet beide Briefrollen und Rians Verletzung sachlich richtig, spricht in einer Wiederholung aber über interne Quellen statt natürlich als Figur. Gemma verwechselt weiterhin Rollen. Abgelehnte Antworten zählen nicht als richtig beantwortet. Ein zusätzlicher Reparaturaufruf wird nicht eingebaut.

Die automatische Darstellung mit eindeutigen Namen bleibt deaktiviert, weil der Nutzen der manuell geprüften Referenzen nicht zuverlässig in die automatische Briefbegründung übertragen wurde. Die App verwendet weiter Originalausschnitte. Der nachgewiesene falsche Geschenk-Rückblick wird aus dem Modellabruf ausgeschlossen, während der unveränderte Alttext im Archiv bleibt. Sprache außerhalb der begrenzten Grammatik bleibt offen.

Der [ausführliche Bericht](rollen-pruefung-0.8.2.md) enthält gerenderte Prompts, Rohantworten, Bewertungen, den Schutz vor wiederverwendeten falschen Quellen, Rechenaufwand und Grenzen. [APK-Nachweise](validation/roles-0.8.2/apk-verification.json): 321595389 Bytes, SHA-256 `fa7273d431b67020693389f4d1e404baad0587dc488e9c7d1a279e4a1bed09e1`. Signatur und 16-KiB-Ausrichtung sind geprüft. Diagnoseklassen und zusätzliche Modellgewichte sind nicht in der APK.

Ein S24 war nicht angeschlossen. Installation, Wartezeit, RAM und Verhalten auf dem Handy bleiben ungeprüft. Dieser Stand verbessert die Übernahme erkannter Faktenfehler; er garantiert weiterhin keine konsistente Erzählqualität der vorhandenen Modelle.
