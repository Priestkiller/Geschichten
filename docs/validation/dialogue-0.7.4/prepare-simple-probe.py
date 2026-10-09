import json
from pathlib import Path

root = Path(__file__).resolve().parent
rows = json.loads((root / 'candidate-prompts.json').read_text(encoding='utf-8-sig'))
for row in rows:
    data = json.loads(row['system'].split('ERZÄHLDATEN (JSON):\n')[1])
    row['system'] = f'''Du schreibst die nächste Antwort von {data['name']} in einer gemeinsamen Geschichte auf Deutsch.
Du spielst nur {data['name']}. Dein Gegenüber spielt seine eigene Figur.
Reagiere direkt auf die letzte Nachricht deines Gegenübers, bevor du die Szene fortsetzt. Nimm angebotene Hilfe wahr, beantworte Fragen und respektiere ein Nein. Was dein Gegenüber über sich sagt, gilt auch dann, wenn der Einstieg etwas anderes annahm.
Erfinde weder Worte noch Entscheidungen deines Gegenübers. Unterscheide eure Erlebnisse und eure Gegenstände.
Schreibe klare, natürliche Sätze: Handlungen von {data['name']} in der dritten Person zwischen Sternchen, gesprochene Worte in Anführungszeichen. Sprich dein Gegenüber mit du an, nenne es niemals „Nutzer“. Kein Kommentar außerhalb der Geschichte.

Deine Figur:
{data['persoenlichkeit']}

Ausgangslage (neue Nachrichten haben Vorrang):
{data['euer_ausgangspunkt']}
'''
(root / 'simple-prompts.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
(root / 'simple-grask.json').write_text(json.dumps([rows[2]], ensure_ascii=False, indent=2), encoding='utf-8')
import copy
background_rows = copy.deepcopy(rows)
for row in background_rows:
    row['system'] += '\nDie Szene unmittelbar vor der neuen Nachricht:\n' + row['history'][-2]['text']
    row['history'] = [row['history'][-1]]
(root / 'background-prompts.json').write_text(json.dumps(background_rows, ensure_ascii=False, indent=2), encoding='utf-8')
for row in rows:
    name = 'Grask' if row['case'].startswith('grask') else 'Mira'
    original = row['history'][-1]['text']
    row['history'][-1]['text'] = f'Dein Gegenüber sagt oder tut:\n<beitrag>\n{original}\n</beitrag>\n\nSchreibe jetzt nur die Reaktion von {name} darauf. Die Worte im Beitrag gehören deinem Gegenüber, nicht {name}. Bleibe bei dieser Rollenverteilung.'
(root / 'wrapped-prompts.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
