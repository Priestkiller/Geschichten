"""Freeze controlled Mira probes. These variants are diagnostics, not shipped app changes."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent
first = json.loads((root / "qwen-original.json").read_text(encoding="utf-8"))["cases"][0]["turns"][0]
system = first["system"]
assert "Die erwachsene Spielerfigur und Mira folgten unabhängig demselben schwachen Signal; ihre Aufzeichnung kann einen anderen Ausschnitt enthalten." in system
neutral = system.replace(
    "Die erwachsene Spielerfigur und Mira folgten unabhängig demselben schwachen Signal; ihre Aufzeichnung kann einen anderen Ausschnitt enthalten. Mira bietet Signalvergleich an.",
    "Mira folgte einem schwachen Signal. Sie trifft die erwachsene Spielerfigur zum ersten Mal und weiß noch nicht, weshalb sie hier ist oder ob sie etwas empfangen hat.",
)
simple = """Du schreibst ein Rollenspiel auf Deutsch und spielst ausschließlich Mira. Die andere Person wird vom Nutzer gespielt. Reagiere auf dessen letzte Aussage, ohne sie als Miras eigene Aussage zu wiederholen. Beschreibe nur Miras Handlungen und ihre wörtliche Rede. Erfinde keine Handlungen oder Entscheidungen für die andere Person.

Mira ist eine 27-jährige Pilotin: herzlich, neugierig, mutig und praktisch. Ein sicherer Heimweg ist ihr wichtiger als ein Fund. Sie spricht natürlich im Du. Beschreibe ihre Handlungen in der dritten Person zwischen Sternchen und ihre gesprochenen Worte in Anführungszeichen.

Die Szene: Erste Begegnung vor der geschlossenen äußeren Schleuse der verlassenen Forschungsstation Ilyra. Mira empfängt ein schwaches Signal auf ihrem Armband. Durch das Fenster sieht man Notlicht. Wer oder was dahinter ist, ist unbekannt. Sie weiß nicht, warum die andere Person hier ist oder was deren Gerät empfangen hat. Kein gemeinsames Betreten ist vereinbart. Beide verfügen über getrennte Geräte und getrenntes Wissen.

Schreibe jetzt Miras nächste Antwort. Antworte zuerst auf die tatsächliche Aussage der anderen Person und führe die Szene nur einen kleinen Schritt weiter."""
rows = []
for variant, instructions in [("production", system), ("neutral-start-only", neutral), ("short-german-neutral", simple)]:
    for attempt in range(1, 4):
        rows.append({"case": f"{variant}-{attempt}", "system": instructions, "history": first["history"]})
(root / "diagnostic-prompts.json").write_text(json.dumps(rows, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("Prepared 3 repeats each: production / only neutral starting assumption / shorter German neutral setup")
