"""Literal schema/source checks plus documented independent interpretation of 0.8.6 errors."""
from pathlib import Path
import json,hashlib,re,collections
root=Path(__file__).resolve().parents[3];p=Path(__file__).resolve().parent;old=root/'docs/validation/team-0.8.6'
categories=('format','source_assignment','output_limit','person_or_event_understanding','missed_contradiction','unsupported_complaint')
rows=[]
def audit(ref,case,task,step,correct=None,wrong=None):
    flags=set();notes=[];reply='';work={};obj=None
    text=step.get('answer',step.get('partialAnswer',''));error=step.get('error','')
    if 'Ausgabelimit' in error or step.get('decoded')==512:
        flags.add('output_limit');notes.append('Ausgabe unvollständig; wird nicht als gültige Freigabe gezählt.')
    try:
        payload=step['history'][0]['text'].split('DATEN:\n',1)[1]
        work,pos=json.JSONDecoder().raw_decode(payload)
        if 'ANTWORT (Daten):\n' in payload[pos:]:reply=json.loads(payload[pos:].split('ANTWORT (Daten):\n',1)[1])
    except Exception:notes.append('Original-Arbeitsstand im Rohdatensatz nachlesen.')
    try:
        obj=json.loads(text)
        expected={'story','request','version','facts','unknown'} if task=='facts' else {'story','request','version','verdict','issues'}
        if not isinstance(obj,dict) or set(obj)!=expected:flags.add('format');notes.append('Zusatz-/fehlende Felder gegenüber ursprünglichem Vertrag.')
        if any(obj.get(k)!=work.get(k) for k in ('story','request','version')):flags.add('format');notes.append('Modellecho der technischen Bindung stimmt nicht.')
        sources={s['key']:s for s in work.get('sources',[])}
        facts={f['key']:f for f in work.get('facts',[])}
        if task=='facts':
            for f in obj.get('facts',[]):
                if f.get('type') not in ('state','family','transfer','injury','reason'):
                    flags.add('format');notes.append('Schemafremder Faktentyp; ursprünglicher Parser konnte diesen nur als unsicher verwerfen.')
                src=sources.get(f.get('source'));quote=f.get('quote','')
                if not src or not quote or quote not in src['quote']:
                    flags.add('source_assignment');notes.append('Fakt nennt keinen unveränderten freigegebenen Originalausschnitt.')
                if src and '?' in quote and f.get('type') in ('transfer','state','reason','family','injury'):
                    flags.add('source_assignment');notes.append('Eine Frage wird als Beleg einer tatsächlichen Aussage/Handlung verwendet.')
                    if f.get('type') in ('transfer','reason'):flags.add('person_or_event_understanding')
                if src and f.get('type')=='family':
                    for m in re.finditer(r'(Meine|Deine) (Schwester|Bruder) heißt ([\w-]+)',src['quote'],re.I):
                        owner=src['speaker'] if m[1].lower()=='meine' else work.get('character')
                        if f.get('target','').lower()==m[3].lower() and f.get('actor','').lower()!=owner.lower():
                            flags.add('person_or_event_understanding');notes.append('Originalpronomen wird dem falschen Verwandten zugeordnet.')
                        if f.get('actor','').lower()==m[3].lower() and f.get('target','').lower()==owner.lower():
                            flags.add('person_or_event_understanding');notes.append('Richtung der Familienbeziehung vertauscht.')
                if src and f.get('type')=='transfer' and 'an sich nimmt' in quote and f.get('target')!=work.get('character'):
                    flags.add('person_or_event_understanding');notes.append('Nimmt-an-sich wird zur falschen Übergaberichtung gemacht.')
                if src and f.get('field')=='owner' and 'an sich nimmt' in quote:
                    flags.add('source_assignment');notes.append('An-sich-Nehmen belegt kein Eigentum.')
                if src and f.get('type')=='injury' and f.get('item','').lower()=='dolch':
                    flags.add('person_or_event_understanding');notes.append('Dolch-Beobachtung wird zu einer Verletzung erklärt.')
        else:
            for issue in obj.get('issues',[]):
                claim=issue.get('claim','');key=issue.get('evidence','')
                if key not in sources and key not in facts and key!='answer':
                    flags.add('source_assignment');notes.append('Unbekannter oder zusammengeklebter Belegalias.')
                if not claim or claim not in reply:
                    flags.add('source_assignment');notes.append('Beanstandeter Original-Antwortausschnitt ist nicht tatsächlich vorhanden.')
            if correct and obj.get('issues'):
                flags.add('unsupported_complaint');notes.append('Unabhängig richtiger Fakteninhalt wird beanstandet; ggf. Sprachmangel ist kein Rollenbeweis.')
            if case=='new-good-action' and any(i.get('type')!='unanswered' for i in obj.get('issues',[])):
                flags.add('unsupported_complaint');notes.append('Zulässige Handlung/Verwandtschaft beanstandet. Fehlender Geschenkgeber bleibt legitime separate Beanstandung.')
            if wrong and obj.get('verdict')=='clear':
                flags.add('missed_contradiction');notes.append('Tatsächlicher Fehler erhält ein rohes clear; Parserfehler und Endfreigabe werden getrennt bewertet.')
    except Exception:
        flags.add('format');notes.append('Kein vollständiges JSON nach dem ursprünglichen Ausgabeauftrag.')
    rows.append(dict(reference=ref,case=case,task=task,categories=sorted(flags),observations=list(dict.fromkeys(notes)),rawVerdict=obj.get('verdict') if isinstance(obj,dict) else None,
                     meaningNotCertifiedByParser=True,sourceChecksDoNotExhaustSemanticErrors=True))
for model in ('huihui-qwen3-4b','gemma-4-e2b'):
    fn=f'raw-phase-a-{model}.json'
    for i,r in enumerate(json.loads((old/fn).read_text(encoding='utf-8'))):
        case=r['case'];audit(f'{fn}[{i}]',case,r['task'],r,case.startswith('good-'),case.startswith('bad-') or case.startswith('new-bad-'))
for fn in ('raw-team-B.json','raw-team-C.json','raw-team-self.json','raw-dialogue-1.json','raw-dialogue-2.json'):
    for i,r in enumerate(json.loads((old/fn).read_text(encoding='utf-8'))):
        for j,s in enumerate(r['steps']):
            if s['role']!='HELPER':continue
            case=r['case'];task='facts' if s['task']=='facts' else 'review'
            correct=(case in ('dolch','new-secret','dialogue-1','dialogue-2')) or (fn=='raw-team-self.json' and case=='hold-family')
            if fn=='raw-team-self.json' and case=='dolch':correct=False
            wrong=case in ('hold-reverse','hold-family','causal-letter','new-gift') and not correct
            audit(f'{fn}[{i}].steps[{j}]',case,task,s,correct,wrong)
summary={k:sum(k in r['categories'] for r in rows) for k in categories}
result=dict(scope='All 59 actual helper calls from 0.8.6; categories overlap. Literal proof/schema defects and known independently reviewed scene failures. This is not a complete semantic truth oracle.',callCount=len(rows),categoryCounts=summary,rows=rows,
            originalsSha256={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in old.glob('raw-*.json')})
assert len(rows)==59
(p/'legacy-errors-classified.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'calls':len(rows),'categories':summary}))
