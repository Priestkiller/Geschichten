"""Assessment by exact required spans, not the misleading presence of a whole source ID.
Gold spans make the existing frozen expected facts precise; retrieval parameters stay frozen.
"""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
gold={
'dev-transfer':[(3,'Ich reiche dir die Karte, weil meine rechte Hand verletzt ist.')],
'dev-reverse':[(3,'Oskar gibt Fenna den Kristall.'),(3,'Oskars linke Schulter ist verletzt.')],
'dev-family':[(3,'Meine Schwester heißt Wiebke.'),(3,'Dein Bruder heißt Nils.'),(3,'Wiebke hat dir eine Karte geschickt.')],
'dev-owner':[(3,'Das Buch gehört Nora.'),(3,'Es liegt nun auf dem Tisch.')],
'dev-negation':[(3,'Ich bin nicht verletzt.')],
'dev-panic':[(3,'Alva bekommt in engen, dunklen Räumen schnell Panik.')],
'dev-sound':[(3,'Dorian kann schrille Geräusche seit dem Unfall kaum ertragen.')],
'dev-multiple':[(3,'Esra meidet offene Flammen'),(5,'Die Notbeleuchtung im Tunnel läuft mit Batterien.')],
'dev-correction':[(3,'Danach lege ich den Dolch auf den Tisch.')],
'dev-unknown':[],
'hold-transfer':[(3,'Ich gebe dir das Amulett.'),(3,'Mein linkes Bein ist verletzt; deines ist unverletzt.')],
'hold-reverse':[(3,'Tammo reicht Yara das Buch.'),(3,'Tammo hat eine verletzte rechte Hand.')],
'hold-family':[(3,'Meine Schwester heißt Ruth.'),(3,'Ruth hat dir den Kristall geschenkt.')],
'hold-owner':[(3,'Der Ring gehört Amelie.'),(3,'Ruben verwahrt ihn nur.'),(3,'Der Ring liegt auf der Kommode.')],
'hold-negation':[(3,'Ich bin nicht mehr verletzt.'),(3,'Das habe ich noch nicht getan.')],
'hold-water':[(3,'Jette kann nicht schwimmen und hat Angst vor tiefem Wasser.')],
'hold-crowd':[(3,'Arne wird bei dichtem Gedränge nervös und sucht einen ruhigen Randplatz.')],
'hold-multiple':[(3,'Bea verträgt keine Nüsse'),(5,'Im Proviantbeutel liegen Haferkekse ohne Nüsse.')],
'hold-correction':[(3,'Nun lege ich sie in den Schrank.')],
'hold-unknown':[]}
fixtures={r['case']:r['bundle'] for r in json.loads((ROOT/'materialized-scenes.json').read_text())}
rows=[]
for split in ['development','heldout']:
 for r in json.loads((ROOT/f'retrieval-{split}-results.json').read_text()):
    case=r['case'];b=fixtures[case];spans=[]
    for number,needle in gold[case]:
        source=next(m for m in b['messages'] if m['id']==f'{case}-{number}');start=source['text'].index(needle);spans.append(dict(sourceId=source['id'],start=start,end=start+len(needle),text=needle))
    def assessment(selected):
        found=[g for g in spans if any(e['message']['id']==g['sourceId'] and e['start']<=g['start'] and e['end']>=g['end'] for e in selected)]
        return dict(requiredSpansFound=len(found),requiredSpans=len(spans),complete=(len(found)==len(spans)) if spans else len(selected)==0,
          selected=[dict(sourceId=e['message']['id'],start=e['start'],end=e['end'],text=e['message']['text'][e['start']:e['end']]) for e in selected],
          irrelevantSelected=sum(not any(e['message']['id']==g['sourceId'] and e['start']<=g['start'] and e['end']>=g['end'] for g in spans) for e in selected))
    row=dict(case=case,split=split,requiredSpans=spans,lexical=assessment(r['lexical'][:4]),hybrid=assessment(next(v for v in r['variants'] if v['threshold']==.30)['selected']),queryMs=r['queryMs'],indexMs=r['indexMs'],newVectorCount=r['newVectorCount'],vectorBytes=r['storedVectorBytes'])
    rows.append(row)
summary={}
for split in ['development','heldout']:
 subset=[r for r in rows if r['split']==split];summary[split]={mode:dict(complete=sum(r[mode]['complete'] for r in subset),cases=len(subset),foundSpans=sum(r[mode]['requiredSpansFound'] for r in subset),requiredSpans=sum(len(r['requiredSpans']) for r in subset),irrelevantSelected=sum(r[mode]['irrelevantSelected'] for r in subset)) for mode in ['lexical','hybrid']}
(ROOT/'retrieval-assessment.json').write_text(json.dumps(dict(method='Exact necessary source spans; unknown-past case succeeds only with no search hits. Earlier source-ID-only counters are not the final quality measure.',parametersUnchanged=True,summary=summary,rows=rows),ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,indent=2))
