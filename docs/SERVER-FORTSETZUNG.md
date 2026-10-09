# Fortsetzung der Geschichten-Arbeit auf dem Server

Stand 10. Oktober 2026: Dieses Repository enthält den aktuellen Android-Quellcode 0.8.7 und ausgewählte Entwicklungs-/Testunterlagen. In den älteren Berichten bedeutet „lokal / nicht veröffentlicht“ den damaligen Lieferstand. Die neue Veröffentlichung ist in README und RELEASE-NOTES dokumentiert.

Für die Modelltests zuerst den [Server-Prüfbericht](PRUEFBERICHT-SERVER-2026-10-09.md) und [bisherigen Teststand](validation/server-2026-10-09/SERVER-START-HIER.txt) lesen. Der neue Arbeitsauftrag kommt vom Nutzer im Chat; die Unterlagen beschreiben bisherigen Stand und Einschränkungen.

Tatsächliche Hardware: i7-6700K, 32 GB RAM, RTX 2060 mit 6 GiB VRAM, Windows 10. Ollama 0.40.2 mit Qwen3-8B Q4_K_M und gewichtsgleicher Testdefinition. API-Zugriff funktionierte; nach einem Neustart Status und Bindeadresse erneut prüfen.

Der 28-Antworten-Test zeigte brauchbare Geschwindigkeit, aber weiterhin falsche Rollen, Verwandtschaften, Übergabegründe und erfundene Vergangenheit. Ministral 3 14B und Gemma 4 12B wurden lediglich als weitere Kandidaten vorgeschlagen, nicht installiert oder getestet. Ein größeres Modell ist noch kein nachgewiesener Fix.

Neue Versuche in neuen Ausgabeordnern erfassen; vorhandene Messungen und eingefrorene Fälle erhalten. Quellenreferenzen, tokenbegrenzte echte Eingaben, vollständige unveränderte Antworten und Laufzeiten getrennt prüfen. Ein fortlaufender Dialog muss die jeweils tatsächlich erzeugte vorherige Antwort verwenden. Keine erwarteten Lösungen in die Modelleingabe mischen.

Die Android-App ist noch nicht an Ollama angebunden. Die bisherigen Servertests schreiben nicht in ihre SQLite-Datei. Aktueller Hauptschema-Stand ist 10, Suchindexschema 1. Der Faktenhelfer bleibt standardmäßig aus. Es gibt weder ein trainiertes eigenes Modell noch einen belegten zuverlässigen Modell-Team-Gewinn.

Private Schlüssel, Build-Caches, Inferenzgewichte, alte Quellkopien und kompilierte Diagnoseklassen sind nicht veröffentlicht. Nur die benötigten Tokenizer und vendorten Bibliotheksquellen sind Teil des Android-Builds. Für Builds siehe [BUILD.md](BUILD.md) und [Signierung](../signing/README.md).

Für Windows wegen langer Pfade in den vendorten Bibliotheken mit `git clone -c core.longpaths=true https://github.com/Priestkiller/Geschichten.git` klonen. Diese Option gilt nur für den neuen Git-Checkout.
