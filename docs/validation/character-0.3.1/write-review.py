"""Record the editorial review, using the actual compiled catalog and frozen release baseline."""
import json
from pathlib import Path

OUTPUT = Path(__file__).resolve().parent
PROJECT = OUTPUT.parents[2]
before = json.loads((OUTPUT / "catalog-before.json").read_text(encoding="utf-8"))
after = json.loads((OUTPUT / "catalog-reviewed.json").read_text(encoding="utf-8"))
old = {f["id"]: f for f in before}
assert len(after) == len(old) == 50
assert {f["id"] for f in after} == set(old)
for figure in after:
    original = old[figure["id"]]
    for key in original.keys() - {"personality", "openingMessage", "scenario"}:
        assert figure[key] == original[key], (figure["id"], key)
    assert figure["personality"] != original["personality"]
    assert figure["openingMessage"] != original["openingMessage"]
    assert len(figure["personality"]) <= 630 and len(figure["scenario"]) <= 480
    assert len(figure["openingMessage"]) <= 1500

intro = """# Figuren, Geschichten und Stimmen – 0.3.1

Alle 50 Standardfiguren wurden einzeln auf Motivation, inneren Konflikt, Wissen, Körper, Sprechweise und passenden Einstieg geprüft. Die Rollen, Porträts, Altersangaben und fünf Kategorien bleiben erhalten. Jeder Eintrag enthält das tatsächlich kompilierte überarbeitete Profil und den verfassten neuen Einstieg.

## Redaktionelle Änderungen

- Jede Persönlichkeit verbindet ein persönliches Motiv mit einer Schwäche und einer erkennbaren Reaktion auf Druck. Vertrauen entwickelt sich durch Verhalten statt sofortiger Vertrautheit.
- Satzbau, Wortwahl, Humor und Anrede unterscheiden sich. Borin spricht knapp, Liv denkt rasch in Messungen, Torben bildhaft, Varen förmlich, Nessa ausweichend und Ivo vorsichtig. Niemand benötigt einen künstlichen Dialekt oder eine ständig wiederholte Floskel.
- Die Einstiege beginnen unterschiedlich: Beobachtung, Arbeitsanweisung, Gespräch, Zögern oder Eingeständnis. Der trockene Witz dient einzelnen Stimmen, nicht mehr als einheitliche Begrüßung aller Figuren.
- Tareks zunehmende Schwerkraft wird als zusätzliche Last beschrieben; nichts schwebt dadurch. Sigrids offene Schatulle hat kein angeblich unversehrtes Siegel. Leons Zugang zum geschlossenen Café ist erklärt. Bei Seris ist die neue Kette nicht schon im Einstieg verrostet. Veshra benennt die abweichenden Rätseltexte.
- Ermittlungen trennen Hinweis und Beweis, technische Geschichten erklären begrenzte Messungen und Mittel, Fantasyfiguren besitzen keine beliebigen Lösungen. Nicht humanoide Kreaturen handeln mit dem Körper, den ihr Porträt und ihr Profil beschreiben.

## Was die KI bekommt

Die frühere Grenze von 480 Zeichen schnitt Teile der Persönlichkeit ab. Jetzt passen alle 50 überarbeiteten Profile vollständig in das Persönlichkeitsfeld des Prompts. Auch die vollständigen Einstiegsszenen kommen zu Beginn an. Genre, aktuelle Notizen und Verlauf bleiben Teil der Vorgaben. In längeren Geschichten haben die aktuelle Zusammenfassung und der aktuelle Ort Vorrang vor einer ausführlichen Wiederholung der Ausgangsszene. Der gesamte Systemprompt bleibt auf 3.000 Zeichen begrenzt.

Automatisierte Prüfungen vergleichen die an die KI gesendeten JSON-Felder mit den kompletten Profilen, in beiden Themenmodi und mit fortgeschrittenem Spielstand. Das belegt die Übertragung der Vorgaben, keine bereits erzeugten Antworten auf einem Galaxy S24. Die Qualität tatsächlicher lokaler Modellantworten muss dort mit verschiedenen Gesprächsverläufen geprüft werden.

## Bestehende Figuren und Geschichten

Das Update überarbeitet nur Profile, die exakt einem unveränderten Standardprofil aus 0.3.0 entsprechen und nicht als eigene Bearbeitung markiert sind. Jede abweichende Nutzerfassung bleibt vollständig erhalten. Alte Chats, Startnachrichten, Zusammenfassungen, Notizen, Titel und Zeitstempel werden nicht umgeschrieben. Neue Geschichten mit einem aktualisierten Standardprofil beginnen mit dem neuen Einstieg.
"""
lines = [intro]
for category in ("Nordische Fantasy", "Fantasy", "Krimi", "Science-Fiction", "Kreaturen"):
    lines.append(f"\n## {category}\n")
    for figure in (f for f in after if f["genre"] == category):
        lines.extend([
            f"\n### {figure['name']} – {figure['role']}\n",
            f"**Geschichte:** {figure['storyTitle']}\n",
            figure["personality"] + "\n",
            "**Ausgangsszene:** " + figure["scenario"] + "\n",
            "**Neuer Einstieg:**\n",
            figure["openingMessage"] + "\n",
        ])
(PROJECT / "docs/FIGUREN-UND-STIMMEN-0.3.1.md").write_text("\n".join(lines), encoding="utf-8")
print("PASS: all 50 individually revised profiles and openings recorded; identity, roles and portraits retained.")
