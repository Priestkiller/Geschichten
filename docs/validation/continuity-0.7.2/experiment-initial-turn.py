from pathlib import Path
import json
root = Path(__file__).resolve().parent
cases = json.loads((root / "final-prompts.json").read_text(encoding="utf-8"))[:1]
cases[0]["history"].insert(0, {"user": True, "text": "Beginne die festgelegte Szene als die beschriebene Figur."})
(root / "experiment-initial-turn-prompts.json").write_text(json.dumps(cases, ensure_ascii=False, indent=2), encoding="utf-8")
