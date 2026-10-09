from pathlib import Path
import json

project = Path(__file__).resolve().parents[3]
workspace = project.parent
release = workspace / "release-0.7.2"
release.mkdir(exist_ok=True)
public = (workspace / "Geschichten-GitHub/README.md").read_text(encoding="utf-8").replace("0.7.1", "0.7.2")
public = public.replace("Versionscode 10", "Versionscode 11")
previous = "- **Neu: verständliche Beschreibungen für alle sechs auswählbaren Modelle**, mit Einsatzgebieten, Grenzen, Downloadgröße und Anbieterlinks."
assert previous in public
public = public.replace(previous, "- **Neu: stimmigere Anschlüsse für alle Figuren und alle sechs Modelle.** Die KI erhält bei langen Einführungen die tatsächlichen letzten Worte der Figur, damit deine Zusage oder Frage an die passende Aufgabe anschließt.\n- **Direkte Rede ist fett und goldfarben**, Situationsbeschreibungen erscheinen kursiv in hellem Grau. Dies gilt auch für bestehende Nachrichten.\n- **Verlauf leeren** löscht nach Bestätigung alle Gespräche, zugehörigen Erinnerungen und Entwürfe. Figuren, Einstellungen und Modell-Downloads bleiben erhalten.\n- Verständliche Beschreibungen für alle sechs Modelle, mit Einsatzgebieten, Grenzen, Downloadgröße und Anbieterlinks.")
public = public.replace("### Modellbeschreibungen in 0.7.2", "### Modellbeschreibungen")
public += "\n### Gesprächskontext in 0.7.2\n\nDie lange sichtbare Einführung bleibt vollständig gespeichert. Für die begrenzte KI-Übergabe werden ihre tatsächliche letzte Begegnung und direkte Rede behalten; die Ausgangslage steht zusätzlich in den Erzählangaben. Dies gilt für alle Modelle und Figuren. Die KI soll eine Zusage oder Absage konkret aufnehmen und die laufende Szene weiterführen. Bereits gespeicherte Antworten werden nicht umgeschrieben. Für einen frischen Einstieg **Neue Geschichte** im Chatmenü wählen. Die Antwortqualität bleibt modellabhängig.\n"
(release / "README-GitHub-Vorschlag.md").write_text(public, encoding="utf-8", newline="\n")
private_file = project / "README.md"
private = private_file.read_text(encoding="utf-8").replace("0.7.1", "0.7.2").replace("Versionscode 10", "Versionscode 11")
anchor = "- Die Reiter heißen **Figuren · Verlauf · Erinnerungen · Einstellungen**."
assert anchor in private
private = private.replace(anchor, anchor + "\n- **Verlauf leeren** löscht nach Bestätigung Gespräche, Nachrichten, zugehörige Erinnerungen und Entwürfe; Figuren, Einstellungen und Modell-Dateien bleiben erhalten.\n- Alle Modelle erhalten die konkrete letzte Begegnung und Frage aus langen Einführungen. Die Einführungen bleiben in SQLite und UI vollständig.\n- Direkte Rede wird fett und goldfarben, Beschreibungen kursiv in hellem Grau dargestellt; vorhandene Nachrichten werden nur für die Anzeige formatiert.")
private_file.write_text(private, encoding="utf-8", newline="\n")
cases = json.loads((Path(__file__).resolve().parent / "after-prompts.json").read_text(encoding="utf-8"))
(Path(__file__).resolve().parent / "grask-single.json").write_text(json.dumps(cases[:1], ensure_ascii=False, indent=2), encoding="utf-8")
print("Prepared 0.7.2 release documentation and single-case native prompt.")
