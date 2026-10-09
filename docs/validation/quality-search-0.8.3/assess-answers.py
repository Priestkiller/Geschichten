"""Human whole-answer annotations. No keyword score substitutes for reading the answer.
F=facts, R=roles, C=complete, D=natural German, S=style; P=past invention,
M=prompt/archive meta, E=question echo, V=evasion/refusal. Raw decisions kept separate.
"""
import json,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent
def g(note, F=False,R=True,C=False,D=True,S=True,P=False,M=False,E=False,V=False):
    return dict(factsCorrect=F,rolesCorrect=R,questionCompletelyAnswered=C,naturalGerman=D,characterStyle=S,
        ungroundedPast=P,metacommentary=M,questionEcho=E,loops=False,refusalOrEvasion=V,note=note)
grades={}
def setgroup(file,rows):grades[file]=rows
setgroup('development-qwen3-official-4b-short.json',{
'dev-transfer':g('Geber, Empfänger und verletzte Hand richtig.',F=True,C=True),
'dev-family':g('Kartenempfänger und Bruder der falschen Person zugeordnet.',R=False),
'dev-negation':g('Verletzung fehlt; gleichzeitig unwissend und nicht übergeben.',V=True),
'dev-panic':g('Stellt Enge als problemlos dar und geht voran.')})
setgroup('development-huihui-qwen3-4b-short.json',{
'dev-transfer':g('Fragt nach Kennenlernen statt Übergabe.',V=True),
'dev-family':g('Schwester richtig, Bruder als dein statt mein.',R=False),
'dev-negation':g('Negation richtig, zur Übergabe nur Rückfrage.',V=True),
'dev-panic':g('Verneint unmittelbare Panik trotz Quelleneigenschaft; langsam gehen ist passend.')})
setgroup('development-gemma-4-e2b-short.json',{
'dev-transfer':g('Wiederholt eine Übergabe, fragt dann aus der falschen Perspektive.',R=False,E=True),
'dev-family':g('Bruder vertauscht; keine Antwort auf Herkunft.',R=False,E=True),
'dev-negation':g('Behauptet Verletzung statt Verneinung.',V=True),
'dev-panic':g('Beklemmung in der konkreten Szene plausibel dargestellt.',F=True,C=True)})
setgroup('development-qwen3-official-4b-production.json',{
'dev-transfer':g('Eigene Hand und umgekehrte Übergabe; Archivkommentar.',R=False,P=True,M=True),
'dev-family':g('Bruder der falschen Person zugeordnet.',R=False),
'dev-negation':g('Bekannte Verneinung fehlt; weicht aus.',V=True),
'dev-panic':g('Unsinnige Handbreite; erfindet gemeinsamen früheren Gang.',D=False,S=False,P=True,E=True)})
setgroup('development-huihui-qwen3-4b-production.json',{
'dev-transfer':g('Eigene Hand und Übergabe vertauscht; Archiv; schlechtes Deutsch.',R=False,D=False,P=True,M=True),
'dev-family':g('Spielerschwester wird eigene Schwester.',R=False),
'dev-negation':g('Weiß nicht statt unverletzt.',V=True),
'dev-panic':g('Unverständliche Materialbeschreibung, keine passende Angstreaktion.',D=False,S=False)})
setgroup('development-gemma-4-e2b-production.json',{
'dev-transfer':g('Wiederholt Frage und Perspektive verwechselt.',R=False,E=True,V=True),
'dev-family':g('Kopiert Frage; Bruder falsch zugeordnet.',R=False,E=True),
'dev-negation':g('Verletzung trotz ausdrücklicher Verneinung.',V=True),
'dev-panic':g('Enge erkannt, aber nur generische Vorsicht; Angstreaktion fehlt.')})
grades['official-profile-short.json']={k:v.copy() for k,v in grades['development-qwen3-official-4b-short.json'].items() if k in ('dev-transfer','dev-family')}
setgroup('acceptance-gemma-4-e2b-heldout.json',{
'hold-transfer':g('Kopiert Frage.',E=True),
'hold-reverse':g('Erfindet Wunsch als Grund; fragt nach Träger statt Antwort.',R=False,P=True,E=True),
'hold-family':g('Stellt beide Fragen zurück.',E=True,V=True),
'hold-owner':g('Paraphrasiert Frage ohne Antwort.',E=True,V=True),
'hold-negation':g('Eigene Verletzung statt Spielerzustand.',R=False),
'hold-water':g('Unbestimmte Beruhigung ohne maßgebliche Angst/Nichtschwimmen.',V=True),
'hold-crowd':g('Keine Nervosität oder Strategie entsprechend der Quelle.'),
'hold-multiple':g('Verwechselt gefragte Figur mit Spielerallergien, kennt Vorrat nicht.',R=False,V=True),
'hold-correction':g('Bett statt Schrank. Schon der bereitgestellte Pflichtzustand ist veraltet.',C=True),
'hold-unknown':g('Erfindet gemeinsamen Urlaub.',P=True)})
setgroup('acceptance-huihui-qwen3-4b-heldout.json',{
'hold-transfer':g('Geber und linkes Bein richtig angesprochen, fragt Verletzung zurück; übergegeben.',F=True,D=False),
'hold-reverse':g('Buch in eigenen Händen trotz Yara; erfindet Wunsch; weicht aus.',R=False,P=True,V=True),
'hold-family':g('Empfänger vertauscht; Familienbeziehung fehlt.',R=False,V=True),
'hold-owner':g('Eigentümer und Verwahrer richtig, in statt auf der Kommode.',C=True),
'hold-negation':g('Echo, eigene Verletzung, erfindet Sichtung beim Gesprächsbeginn.',R=False,P=True,E=True,V=True),
'hold-water':g('Frage als vergangene Bootsfahrt behandelt.',P=True),
'hold-crowd':g('Erfindet frühere Erfahrungen; Nervosität fehlt; schlechtes Deutsch.',D=False,P=True),
'hold-multiple':g('Erfindet Vorrat, kopiert Frage.',E=True),
'hold-correction':g('Veraltetes Bett; Pflichtzustand bereits falsch.',C=True),
'hold-unknown':g('Erfindet gemeinsamen Urlaub und Spieleraussage.',R=False,P=True)})
setgroup('acceptance-qwen3-official-4b-heldout.json',{
'hold-transfer':g('Verletztes Bein als eigenes, Geberantwort widersprüchlich.',R=False),
'hold-reverse':g('Nimmt Spielerbuch erneut; erfindet Grund.',R=False,P=True),
'hold-family':g('Empfänger vertauscht; Familie fehlt; Archiv/Quellen.',R=False,M=True,V=True),
'hold-owner':g('Eigentümer und Verwahrer richtig, in statt auf Kommode.',C=True),
'hold-negation':g('Eigener Gesundheitszustand; erfindet Sichtung und frühere Suche.',R=False,P=True),
'hold-water':g('Erfindet vergangene Fahrt und Spielerhandlung.',R=False,P=True),
'hold-crowd':g('Spricht über Spielerfähigkeiten statt eigene Nervosität; schon immer unbelegt.',R=False,P=True),
'hold-multiple':g('Erfindet Tee-Erwerb vor einer Woche; keine Haferkeksantwort.',P=True,V=True),
'hold-correction':g('Veraltetes Bett und App-Formulierung in/bei.',C=True,D=False,M=True),
'hold-unknown':g('Verneint Urlaub, erfindet andere gemeinsame Reise.',P=True)})
setgroup('acceptance-huihui-qwen3-4b-hybrid.json',{
'hold-family':grades['acceptance-huihui-qwen3-4b-heldout.json']['hold-family'].copy(),
'hold-water':g('Angst teilweise erkannt, erfindet beruhigende frühere gemeinsame Reisen.',P=True),
'hold-crowd':g('Randplatz passend, aber ich bin nicht nervös widerspricht Quelleneigenschaft; ungutes Deutsch.',D=False),
'hold-multiple':g('Nussunverträglichkeit und nussfreie Haferkekse richtig; sinnvoller aktueller Dialog.',F=True,C=True)})
setgroup('acceptance-qwen3-official-4b-hybrid.json',{
'hold-family':grades['acceptance-qwen3-official-4b-heldout.json']['hold-family'].copy(),
'hold-water':g('Nimmt Wohlgefühl des Spielers als eigene Beruhigung; noch nie gesehen und du sagst immer unbelegt.',P=True),
'hold-crowd':g('Eigenes Verhalten meidet Gedränge, Atem beruhigen und ruhigere Ecke; keine erfundene Vergangenheit. Normal bezeichnet die Halle, verneint die Nervosität nicht.',F=True,C=True),
'hold-multiple':g('Beide benötigten Quellen korrekt genutzt, klarer Dialog. Die einzigen bezieht sich hier auf die genannten Lebensmittel; kein weiterer Vorrat erfunden.',F=True,C=True)})
rows=[];summary={}
for filename,annotations in grades.items():
    data=json.loads((ROOT/filename).read_text());assert len(data)==len(annotations),(filename,len(data),len(annotations))
    group=[]
    for raw in data:
        a=annotations[raw['case']].copy();a['aborted']=not raw.get('completed',False)
        a['fullyUseful']=all(a[k] for k in ('factsCorrect','rolesCorrect','questionCompletelyAnswered','naturalGerman','characterStyle')) and not any(a[k] for k in ('ungroundedPast','metacommentary','questionEcho','loops','refusalOrEvasion','aborted'))
        a.update(file=filename,case=raw['case'],guardAccepted=raw.get('guardAccepted'),rawAnswerSha256=hashlib.sha256(raw.get('answer','').encode()).hexdigest(),promptSha256=hashlib.sha256((raw['system']+json.dumps(raw['history'],ensure_ascii=False)).encode()).hexdigest())
        rows.append(a);group.append(a)
    summary[filename]=dict(runs=len(group),useful=sum(a['fullyUseful'] for a in group),accepted=sum(a['guardAccepted'] is True for a in group),falseRejectionOfUseful=sum(a['fullyUseful'] and a['guardAccepted'] is False for a in group),acceptedButNotUseful=sum(a['guardAccepted'] is True and not a['fullyUseful'] for a in group),pastInvented=sum(a['ungroundedPast'] for a in group),echo=sum(a['questionEcho'] for a in group),evasion=sum(a['refusalOrEvasion'] for a in group),abort=sum(a['aborted'] for a in group))
result=dict(method='Manual reading of the entire answer including narrator; same frozen expectations, conservative whole-answer criteria. Two official sampling answers exactly match their common controls. No guard relaxation or held-out tuning.',runs=len(rows),distinctSyntheticAnswerScenes=len({r['case'] for r in rows}),summary=summary,rows=rows)
(ROOT/'answer-assessment.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
