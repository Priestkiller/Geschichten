from pathlib import Path

project = Path(__file__).resolve().parents[3]
release = project.parent / 'release-0.7.3'
release.mkdir(exist_ok=True)
for relative in ('app/build.gradle.kts', 'BUILD_WINDOWS.cmd', 'build-linux.sh', 'scripts/prepare-update.py', 'README.md'):
    file = project / relative
    text = file.read_text(encoding='utf-8').replace('0.7.2', '0.7.3')
    if relative == 'app/build.gradle.kts': text = text.replace('versionCode = 11', 'versionCode = 12')
    if relative == 'scripts/prepare-update.py': text = text.replace('default=11', 'default=12').replace('Standard: 11', 'Standard: 12')
    if relative == 'README.md':
        text = text.replace('Versionscode 11', 'Versionscode 12')
        text += '\n## Wartezeit bis zum ersten Text in 0.7.3\n\nDolphin, Huihui und Gemma 3 verwenden identische bereits berechnete Kontextblöcke weiter. Veränderte Inhalte werden neu berechnet; Abbrüche und Fehler verwerfen den Cache. LiteRT-Gespräche werden weiterhin frisch angelegt, weil der Systemkontext pro Antwort aktualisiert wird.\n\nUnter Einstellungen → Schneller zum ersten Text → Geschwindigkeit optimieren werden für die ausgewählte KI zwei Messungen je Prozessoreinstellung direkt auf dem Gerät durchgeführt. Die App erkennt S24 und S24 Ultra anhand der Modellkennung und speichert die gewählte Einstellung pro Modell, Dateirevision und Gerätesoftware. Eine 10-Prozent-Schwelle vermeidet Wechsel wegen kleiner Messschwankungen. Messantworten bleiben außerhalb der Geschichten. Die Produktionsgrenze von 512 Ausgabetoken und die langen sichtbaren Einführungen bleiben erhalten. Eine physische Messung auf beiden Zielgeräten steht aus.\n'
    file.write_text(text, encoding='utf-8', newline='\n')
public = (project.parent / 'Geschichten-GitHub/README.md').read_text(encoding='utf-8').replace('0.7.2', '0.7.3').replace('Versionscode 11', 'Versionscode 12')
anchor = '- **Neu: korrigierter Gesprächskontext'
assert anchor in public
public = public.replace(anchor, '- **Neu: kürzere Wartezeit bei weiteren Nachrichten für Dolphin, Huihui und Gemma 3 durch Wiederverwendung identischer Kontextblöcke.** Veränderte Texte werden neu berechnet; Abbrüche und Fehler verwerfen den Cache.\n- **Geschwindigkeit optimieren** vergleicht für jede der sechs KIs die Zeit bis zum ersten Text direkt auf dem eigenen Gerät. S24 und S24 Ultra werden erkannt. Die Einstellung und Messwerte werden pro KI und Gerätesoftware gespeichert.\n- **Korrigierter Gesprächskontext', 1)
public += '\n### Geschwindigkeit auf S24 und S24 Ultra\n\nUnter **Einstellungen → Schneller zum ersten Text → Geschwindigkeit optimieren** die jeweils geladene KI messen. Die App vergleicht bis zu drei Prozessoreinstellungen, mit zwei Messungen je Einstellung, und speichert eine deutlich schnellere Einstellung. Unterschiede unter zehn Prozent gelten als Messschwankung. Die kurzen Prüfantworten werden nicht im Verlauf gespeichert. Lass das Handy vor der Messung abkühlen; die Prüfung kann einige Minuten dauern und ist abbrechbar. Beim ersten Chat muss der Kontext einmal vollständig verarbeitet werden. Die langen Einführungen und die Antwortlänge bleiben erhalten. Die tatsächliche Verbesserung auf beiden Geräten wird durch deren eigene Messung bestimmt; es gibt keine pauschale Prozentzusage oder zusätzliche GPU-Aktivierung in diesem Update.\n'
(release / 'README-GitHub-Vorschlag.md').write_text(public, encoding='utf-8', newline='\n')
(release / 'Versionshinweise.md').write_text('''# Geschichten 0.7.3 – Schneller zum ersten Text

- **Dolphin, Huihui und Gemma 3** verwenden unveränderte bereits berechnete Kontextblöcke weiter. Damit müssen bei weiteren Nachrichten weniger Angaben erneut verarbeitet werden. Veränderte Texte werden neu berechnet. Ein Gesprächswechsel entfernt abweichende Inhalte; nach Abbrüchen oder Fehlern wird der Cache vollständig verworfen.
- Für **alle sechs Modelle** gibt es unter **Einstellungen → Schneller zum ersten Text → Geschwindigkeit optimieren** einen Vergleich direkt auf deinem Handy. Die App erkennt **Galaxy S24 und Galaxy S24 Ultra** und vergleicht die Zeit bis zum ersten Text mit mehreren Prozessoreinstellungen. Die Einstellung und Messwerte werden für diese KI und die Gerätesoftware gespeichert. Unterschiede unter zehn Prozent gelten als Messschwankung.
- Die Prüfung läuft offline, kann einige Minuten dauern und ist abbrechbar. Lass das Handy vorher abkühlen und schließe aufwendige Apps. Messantworten werden nicht in deinem Verlauf gespeichert. Bei Abbruch oder Fehler wird die bisherige Einstellung wieder geladen.
- Die letzte gemessene Zeit bis zum ersten Text ist unter Einstellungen sichtbar. Die ausführlichen Einführungen, Modell-Dateien, Geschichten und normale Antwortlänge bleiben erhalten.

Beim ersten Chat müssen die Angaben einmal vollständig verarbeitet werden. Die Cache-Verbesserung gilt für die drei GGUF-Modelle; Gemma 4 und die beiden kleinen Qwen-Modelle nutzen ihre bisherige LiteRT-Laufzeit mit der gemessenen Prozessoreinstellung. Die tatsächliche Beschleunigung auf S24 und S24 Ultra wird durch die eigene Gerätemessung bestimmt. Dieses Update schaltet keinen zusätzlichen GPU- oder NPU-Pfad frei.

**Aktualisieren:** Unter Einstellungen → App-Updates **Testversionen einbeziehen** einschalten und **Nach Updates suchen** antippen. Alternativ `Geschichten-0.7.3.apk` aus diesem Release öffnen. Die vorhandene App nicht deinstallieren.
''', encoding='utf-8', newline='\n')
print('0.7.3 / Code 12 und öffentliche Dokumentation vorbereitet.')
