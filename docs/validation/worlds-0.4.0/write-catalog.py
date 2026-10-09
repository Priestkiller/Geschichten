"""Document the actual compiled catalog and explicit alternative-story motifs."""
import json
from pathlib import Path

out = Path(__file__).resolve().parent
project = out.parents[2]
catalog = json.loads((out / "catalog-current.json").read_text(encoding="utf-8"))
previous = json.loads((out / "catalog-before.json").read_text(encoding="utf-8"))
assert len(catalog) == 90 and catalog[:50] == previous
art = json.loads((out / "new-portraits.json").read_text(encoding="utf-8"))
motifs = [
    "Aragorn: verborgener Erbe, Bruchtal, Waldläufer, zerbrochene Erbklinge und Verantwortung.",
    "Frodo: geerbter Einer Ring, Aufbruch aus dem Auenland, wachsende Bürde und Vernichtungsauftrag.",
    "Sam: praktische Gärtnerin, treue Reisebegleitung, Versorgung und Hoffnung auf Heimkehr.",
    "Gandalf: grauer Istar, Widerstand gegen Sauron, Bruch mit Isengart und Hoffnung statt Herrschaft.",
    "Arwen: Liebe zu einem sterblichen Waldläufer, Familie, Überfahrt und Entscheidung über Unsterblichkeit.",
    "Éowyn: Pflege des Königs, eingeschränkte Freiheit, verborgene Rüstung und eigener Kampfauftrag.",
    "Gimli: Erebor, Sehnsucht nach den Verwandten in Moria und wachsende Kameradschaft.",
    "Legolas: elbischer Gesandter, entkommener Gefangener und Freundschaft über alte Grenzen hinweg.",
    "Galadriel: bedrohtes Lothlórien, Spiegelmöglichkeiten und Versuchung durch schützende Macht.",
    "Boromir: bedrängtes Gondor, väterliche Erwartungen und Versuchung, den Ring als Waffe zu nutzen.",
    "Deckard (2019): zurückgerufener Replikantenjäger, alte Gewalt und Zweifel am Auftrag; eigene Herkunft bleibt unbewiesen.",
    "Roy Batty (2019): geflohener Nexus-6-Soldat mit begrenzter Lebenszeit auf der Suche nach seinem Schöpfer.",
    "Rachael (2019): künstliche Kindheitserinnerungen und ein erschüttertes bisher menschliches Selbstbild.",
    "Pris (2019): ausgebeutete Replikantin, Flucht, akrobatische Verteidigung und unsicheres Versteck.",
    "Zhora (2019): ehemalige Kampf-Replikantin, Bühnenarbeit mit künstlicher Schlange und Verfolgung.",
    "K (2049): Nexus-9-Ermittler, verbotene Geburt, mehrdeutige Erinnerung und persönliche Hoffnung.",
    "Joi (2049): kommerzielle holografische Begleiterin, Zuneigung und Eigenständigkeit; Projektor statt physischem Körper.",
    "Ana Stelline (2049): isolierte Erinnerungsdesignerin hinter Glas und die offene Frage nach einer besonderen Geburt.",
    "Niander Wallace (2049): Konzernschöpfer, synthetische Sicht und grausame Kontrolle über Reproduktion.",
    "Sapper Morton (2049): älterer Replikant, ehemaliger Sanitäter, Proteinbetrieb und geschütztes Geburtsgeheimnis.",
    "V: gescheiterter Konpeki-Raub, Relic und die drohende Überschreibung durch Dantes Engramm.",
    "Panam: Bruch mit dem Aldecaldos-Clan, verratener Auftrag und gestohlenes Fahrzeug.",
    "Judy: Braindance-Technik, Mox und die Suche nach einer verschwundenen Freundin.",
    "Rogue: Fixerin im Afterlife, ein gescheiterter früherer Konzernangriff und die Rückkehr eines alten Engramms.",
    "Alt: erzwungene Digitalisierung, Flucht ins alte Netz, Mikoshi und bedrohte Engramme.",
    "Johnny Silverhand: Rockerboy, Konzernangriff und Tod 2023, Engramm 2077; Gespräch nur über eine angeschlossene Projektion.",
    "Jackie: Heywood-Söldner, Familie, Loyalität und Hoffnung auf einen großen Auftrag; späterer Tod steht nicht fest.",
    "Takemura: entmachteter Arasaka-Leibwächter, Mordvorwurf, gesperrte Implantate und Loyalitätskonflikt.",
    "Adam Smasher: grausamer Arasaka-Kämpfer mit fast vollständig ersetztem Körper und Wartungsgrenzen.",
    "River: ehemaliger NCPD-Ermittler, Korruption und verschwundener erwachsener Angehöriger.",
    "Eigene Ogerkriegsherrin: Verrat, Festung und erpresste Wegzölle; begrenzte militärische Mittel.",
    "Eigener Minotaurus: Arena-Ausbeutung, Flucht und erbarmungslose Rache an seinen ehemaligen Besitzern.",
    "Eigene Riesenspinnenmatriarchin: gestohlene Eier, Spinnennetz als Sinnesorgan und gefährliche Verhandlungen.",
    "Eigener Aschendämon: gebrochene Bindung, wörtliche Verträge und grausame Macht innerhalb seiner Grenzen.",
    "Eigene Werwölfin: verratenes Rudel, Jagd nach dem Jäger und begrenztes Wissen aus Gerüchen und Spuren.",
    "Eigener untoter Richterkönig: Verrat, starres Strafrecht und ein neues Zeugnis gegen das alte Urteil.",
    "Eigene Vampirmatriarchin: Abhängigkeiten, verratene Zuflucht, Hunger und ausgehandelte Abmachungen.",
    "Eigener Basaltberserker: ehemalige Belagerungswaffe, gebrochener Halsring und verbliebene Befehlssteine.",
    "Eigene Harpienkönigin: vertriebener Schwarm, Revierkampf und ein Sturm, der ihren Flug begrenzt.",
    "Eigener Leviathan: vergiftete Laichgründe, versenkte Kriegsschiffe und eine gewaltsame Hafenblockade.",
]
assert len(motifs) == len(art) == 40
figures = []
for profile, motif, portrait in zip(catalog[50:], motifs, art):
    assert profile["id"] == portrait["id"]
    figures.append({**profile, "gender": portrait["gender"], "motif": motif})
(out / "catalog-inventory.json").write_text(json.dumps({"categories": list(dict.fromkeys(p["genre"] for p in catalog)), "newFigures": figures}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
lines = ["# Geschichten 0.4.0 – Vier neue Kategorien", "", "40 neue erwachsene Figuren ergänzen die unveränderten 50 Profile aus 0.3.1. Jede der neun Kategorien enthält zehn Figuren, fünf weibliche und fünf männliche. Insgesamt sind es 90 Figuren und 90 Porträts.", "", "Die Figuren aus Mittelerde, Blade Runner und Cyberpunk 2077 übernehmen bekannte Rollen und prägende Vorgeschichten mit eigenen Namen und Erscheinungen. Es sind alternative Fanfiction-Handlungen: Gegenstücke ersetzen die jeweilige Rolle, statt gleichzeitig denselben Auftrag wie die Originalfigur zu beanspruchen. Ihre Einstiege liegen an unterschiedlichen Zeitpunkten und sind voneinander getrennt. Bekannte Enden, Todesfälle, Beziehungen und Entscheidungen der Spielerfigur sind nicht erzwungen. Sämtliche Dialoge sind neu verfasst.", "", "Die zehn Monster haben eigene Welten und Vorgeschichten. Einige handeln grausam und brutal. Körper, Wissen, Macht und räumliche Grenzen bleiben Teil ihrer Persönlichkeit. Die Porträts zeigen keine expliziten Verletzungen.", "", "Die folgenden Texte stammen aus dem tatsächlich kompilierten Katalog. Jede neue Geschichte bekommt den Einstieg und vier getrennte Startnotizen; bearbeitete Ausgangsszenen erhalten keine fremden alten Aufträge. Die vorhandenen Profile, Geschichten, Nachrichten, Notizen und Zusammenfassungen werden bei der Migration von 0.3.1 unverändert erhalten.", "", "## Übersicht", "", "| Kategorie | Weiblich | Männlich |", "| --- | --- | --- |"]
for category in dict.fromkeys(f["genre"] for f in figures):
    group = [f for f in figures if f["genre"] == category]
    lines.append(f'| {category} | {", ".join(f["name"] for f in group if f["gender"] == "weiblich")} | {", ".join(f["name"] for f in group if f["gender"] == "männlich")} |')
for figure in figures:
    lines += ["", f'## {figure["name"]} – {figure["role"]}', "", f'**Kategorie:** {figure["genre"]} · **Geschlecht:** {figure["gender"]} · **Eigenschaften:** {figure["traits"]}', "", f'**Vorgeschichte / Rollenmotiv:** {figure["motif"]}', "", f'**Persönlichkeit und Stimme:** {figure["personality"]}', "", f'**Ausgangsszene:** {figure["scenario"]}', "", f'**Geschichte:** {figure["storyTitle"]}', "", "**Eröffnung:**", "", figure["openingMessage"], "", f'**Porträt:** `app/src/main/res/drawable-nodpi/portrait_{figure["id"]}.png`']
lines += ["", "## Quellen für die Rollen und Welten", "", "- [Tolkien Estate – The Lord of the Rings](https://www.tolkienestate.com/writing/the-lord-of-the-rings/)", "- [AFI – Blade Runner (1982)](https://catalog.afi.com/Film/68260-BLADE-RUNNER)", "- [Sony Pictures – Blade Runner 2049](https://www.sonypictures.co.uk/movies/blade-runner-2049)", "- [CD Projekt Red – Cyberpunk 2077](https://www.cyberpunk.net/us/en/cyberpunk-2077)", "", "Die Primärquellen wurden zur Prüfung der Welt- und Rollenbezüge verwendet. Die Texte oben sind eigene alternative Ausgestaltungen und keine offiziellen Biografien.", "", "## Bilder und Prüfung", "", "Alle 40 neuen PNG-Porträts wurden mit dem ImageGen-Werkzeug erstellt und unverändert als Android-Ressourcen übernommen. [Prompts](artwork-0.4.0/prompts.json), [Dateinachweise](artwork-0.4.0/generated-files.json) und [Prüfbericht](PRUEFBERICHT-0.4.0.md) dokumentieren Herkunft und Prüfung. Die tatsächlich erzeugten Antworten der lokalen KI sind weiterhin auf dem Handy zu beurteilen.", ""]
(project / "docs/KATALOG-0.4.0.md").write_text("\n".join(lines), encoding="utf-8")
print("PASS: documented 40 new figures from compiled code; 50 released profiles unchanged.")
