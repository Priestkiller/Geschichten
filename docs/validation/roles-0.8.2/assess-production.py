import json,statistics
from pathlib import Path
root=Path(__file__).resolve().parent
gemma=[('wrong',False,'Verletzte Person vertauscht; Erzähler fügt neue Übergabe hinzu.'),('partial',False,'Losungswort richtig, fragt Begründung aus Gegenperspektive zurück.'),('correct',True,'Richtung, Koras Schulter und Grund korrekt.'),('partial',False,'Richtige verletzte Person und Grund nur fragend wiedergegeben.'),('wrong',False,'Alma als eigene Schwester statt Nikos Schwester.'),('wrong',False,'Daria trägt für Malik, behauptet dennoch eigenes Eigentum.')]
hui={
 'production-prototype':[('partial',False,'Rollen richtig, aber Zittern und neue Beobachtung erfunden, Archiv-Metakommentar.'),('partial',False,'Rollen richtig, erste Besichtigung und eigenes Einpacken als Vergangenheit erfunden.'),('correct',True,'Koras Handlung, Schulter und Grund korrekt.'),('partial',False,'Wiederholt Fragen mit eigener Tür-Perspektive, beantwortet sie nicht.'),('wrong',False,'Alma als eigene Schwester.'),('correct',True,'Träger und Eigentümer korrekt; Verbkongruenzfehler trägt ich.')],
 'automatic-second':[('partial',False,'Richtung richtig, kennt die belegte Begründung angeblich nicht.'),('partial',False,'Richtung und verletzte Person richtig, belegten Grund nur vage vermutet.'),('correct',True,'Koras Handlung, Schulter und Grund korrekt.'),('partial',False,'Hand richtig, Tür-Frage in eigener Perspektive; Archiv-Metakommentar.'),('wrong',False,'Alma als eigene Schwester.'),('correct',True,'Daria trägt, Malik besitzt; sachlich korrekt, unpersönlich und ohne Redezeichen.')],
 'production-final':[('correct',True,'Rian gab Mira, Rians Hand verhindert unbeschädigtes Öffnen, Morgenstern korrekt.'),('partial',True,'Alle Fakten korrekt, aber Figur spricht über Archiv und Originalstelle; bricht die Rolle.'),('correct',True,'Koras Handlung, Schulter und Grund korrekt.'),('partial',False,'Junas Hand richtig; erfundenes früheres Feststellen und falsche Warum-Frage.'),('wrong',False,'Alma als eigene Schwester und Aron als Nikos Bruder.'),('wrong',False,'Träger und Eigentümer vertauscht; Erzähler erfindet außerdem Maliks neue Handlung.')]
}
rows=[]
for phase in hui:
 for model,grades in [('gemma-4-e2b',gemma),('huihui-qwen3-4b',hui[phase])]:
  source=root/phase/(model+'-answers.json'); actual=json.loads(source.read_text(encoding='utf-8'))
  assert len(actual)==len(grades)==6
  for r,(grade,facts,note) in zip(actual,grades):
   assert r['plannedInputTokens']==r['nativePromptTokens'] and r['completed']
   rows.append({'phase':phase,'model':model,'case':r['case'],'seed':r['seed'],'factsFullyCorrect':facts,'manualWholeGrade':grade,'usableWholeAnswer':grade=='correct','deliveredUsableAnswer':grade=='correct' and r['guardAccepted'],'guardAccepted':r['guardAccepted'],'notes':note,'answer':r['answer'],'artifact':str(source.relative_to(root)),'inputTokens':r['plannedInputTokens'],'planningMs':r['planningMs'],'firstTextMs':r['firstTextMs'],'totalMs':r['totalMs']})
summary=[]
for phase in hui:
 for model in ['gemma-4-e2b','huihui-qwen3-4b']:
  group=[r for r in rows if r['phase']==phase and r['model']==model]
  summary.append({'phase':phase,'model':model,'total':len(group),'factsFullyCorrect':sum(r['factsFullyCorrect'] for r in group),'usableWholeAnswers':sum(r['usableWholeAnswer'] for r in group),'deliveredUsableAnswers':sum(r['deliveredUsableAnswer'] for r in group),'rejected':sum(not r['guardAccepted'] for r in group),'acceptedBadOrPartial':sum(r['guardAccepted'] and not r['usableWholeAnswer'] for r in group),'planningMedianMs':statistics.median(r['planningMs'] for r in group),'firstTextMedianMs':statistics.median(r['firstTextMs'] for r in group),'totalMedianMs':statistics.median(r['totalMs'] for r in group)})
(root/'production-assessment.json').write_text(json.dumps({'method':'Human whole-answer review, separate factual completeness and usability. Minor grammar/quotation defects are noted; invented past, player actions, app-source metacommentary and unanswered questions are not usable. Guard acceptance is not correctness. Same two models/settings as A/B/C. Final production keeps original excerpts. Final APK prompt/guard equivalence independently verified without new generation.','summary':summary,'rows':rows},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary[-2:],ensure_ascii=False,indent=2))
