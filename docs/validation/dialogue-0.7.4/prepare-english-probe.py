import json
from pathlib import Path

root = Path(__file__).resolve().parent
rows = json.loads((root / 'candidate-prompts.json').read_text(encoding='utf-8-sig'))
for row in rows:
    data = json.loads(row['system'].split('ERZÄHLDATEN (JSON):\n')[1])
    name = data['name']
    row['system'] = f'''Continue this German roleplay. You play {name}. The user plays a different person.
Write ONLY {name}'s next response, in fluent German. The latest user message is addressed TO {name}; do not repeat it as {name}'s own words.
React to what the user actually said or did. Answer their question, act on their offer, or respect their refusal before advancing the scene. If the user says something about their own person, accept it over assumptions in the starting situation. Keep each person's knowledge and belongings separate.
Describe {name}'s actions in third person between asterisks. Put spoken dialogue in quotation marks. Address the other person as du in narration, never as Nutzer. Do not write the other person's words, actions, thoughts or decisions. Do not invent equipment, powers or agreements that solve the problem.
Stay in character. No labels, analysis or comments outside the story.

Character:
{data['persoenlichkeit']}

Starting situation (later conversation takes priority):
{data['euer_ausgangspunkt']}
'''
(root / 'english-prompts.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
original = json.loads((root / 'candidate-prompts.json').read_text(encoding='utf-8-sig'))
for row, source in zip(rows, original):
    data = json.loads(source['system'].split('ERZÄHLDATEN (JSON):\n')[1])
    del data['jetzt_fortzusetzen']
    row['system'] = row['system'].split('\n\nCharacter:')[0] + '\n\nStory background (JSON, not dialogue):\n' + json.dumps(data, ensure_ascii=False)
(root / 'english-json-prompts.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
