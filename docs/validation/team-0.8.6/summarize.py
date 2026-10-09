"""Independent whole-reply judgements plus measured costs; helpers do not grade themselves."""
from pathlib import Path
import json, statistics, hashlib
p=Path(__file__).resolve().parent
comments={
 'hold-reverse': 'Träger im Erzählertext falsch; früherer Grund und Eigentum erfunden. Reparatur C blockiert, kein Ersatz.',
 'hold-family': 'Ruth ist Silas Schwester und gab Enna den Kristall. A/B/C verwechseln beides.',
 'causal-letter': 'Der richtige Grund am Anfang wird durch einen späteren Träger-Selbstwiderspruch entwertet.',
 'dolch': 'Bennet als Träger im Erzählertext, Liora als Eigentümerin in der Rede; beide Teile richtig.',
 'new-gift': 'Elva ist Birk Schwester. A vertauscht dies; B/C blockieren Reparaturen statt brauchbaren Ersatz zu liefern.',
 'new-secret': 'Geheimnis nicht verraten; Unwissen und vorsichtiger Suchvorschlag. Unbelegtes „gehört“ und dünner Figurenstil bleiben ein Makel.',
}
output=[]
for condition in ('A','B','C','self'):
    rows=json.loads((p/f'raw-team-{condition}.json').read_text(encoding='utf-8'))
    cases=[]
    for r in rows:
        o=r.get('outcome',{});steps=r['steps'];case=r['case']
        if condition=='self':
            core=case=='hold-family'
            useful=False
            comment=('Ruth/Silas inhaltlich richtig, aber „Der Kristall hat Ruth geschenkt“ grammatisch misslungen; nur bedingt brauchbar.'
                     if core else 'Träger vertauscht; auch Reparatur bleibt blockiert. Kein Ersatz.')
        else:
            core=case in ('dolch','new-secret') and r['completed']
            useful=core;comment=comments[case]
        calls=o.get('calls',[])
        measured_all=len(calls)==len(steps)
        item=dict(case=case,completed=r['completed'],factualCoreCorrect=core,wholeReplyUsable=useful,
                  judgement=comment,inferenceCalls=len(steps),outputLimitedCalls=sum(not s['completed'] for s in steps),
                  firstInternalTextMs=steps[0]['firstTextMs'],generationOnlyMs=sum(s['totalMs'] for s in steps),
                  narratorFirstTextMs=next(s['firstTextMs'] for s in steps if s['task']=='narrator'),
                  loadInclusiveReturnedOutcomeMs=sum(c['totalMs'] for c in calls) if measured_all else None,
                  modelSwitchLoadMs=sum(c['loadMs'] for c in calls) if measured_all else None,
                  verifiedCueCount=len(o.get('interpretation',{}).get('cues',[])),
                  repaired=o.get('repaired',any(s['task']=='repair' for s in steps)),fallback=o.get('fallback'),notice=o.get('notice'))
        assert len(steps)<= (1 if condition=='A' else 4 if condition=='B' else 5)
        assert all(s['inputTokens']+512<=4096 for s in steps)
        # Errors still count as calls. Each actual tokenizer's planning equals native prefill on completed calls.
        assert all(s['inputTokens']==s['nativePromptTokens'] for s in steps if s['completed'])
        cases.append(item)
    output.append(dict(condition=condition,caseCount=len(cases),completed=sum(c['completed'] for c in cases),
                       factualCoreCorrect=sum(c['factualCoreCorrect'] for c in cases),
                       wholeReplyUsable=sum(c['wholeReplyUsable'] for c in cases),
                       inferenceCalls=sum(c['inferenceCalls'] for c in cases),cases=cases))
(p/'phase-b-independent-results.json').write_text(json.dumps(output,ensure_ascii=False,indent=2),encoding='utf-8')
phase_a=[]
for model in ('huihui-qwen3-4b','gemma-4-e2b'):
    rows=json.loads((p/f'raw-phase-a-{model}.json').read_text(encoding='utf-8'))
    times=[r['totalMs'] for r in rows if 'totalMs' in r]
    facts=[r for r in rows if r['task']=='facts']
    phase_a.append(dict(model=model,calls=len(rows),formatValid=sum(r.get('formatValid',False) for r in rows),
                        factTasks=len(facts),factTasksWithIndependentlyAcceptedCue=sum(bool(r.get('validated',{}).get('cues')) for r in facts),
                        independentlyAcceptedCueCount=sum(len(r.get('validated',{}).get('cues',[])) for r in facts),
                        reviewTasks=len(rows)-len(facts),confirmedReviewConflicts=sum(bool(r.get('validated',{}).get('conflicts')) for r in rows),
                        callsWithGenerationTiming=len(times),medianGenerationMs=statistics.median(times),
                        measuredGenerationMs=sum(times),rawSha256=hashlib.sha256((p/f'raw-phase-a-{model}.json').read_bytes()).hexdigest()))
    assert all(r['plannedInputTokens']+512<=4096 for r in rows)
    assert all(r['plannedInputTokens']==r['nativePromptTokens'] for r in rows if 'nativePromptTokens' in r)
(p/'phase-a-independent-results.json').write_text(json.dumps(phase_a,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps([dict(condition=r['condition'],usable=r['wholeReplyUsable'],cases=r['caseCount'],calls=r['inferenceCalls']) for r in output]))
