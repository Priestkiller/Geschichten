"""Human-reviewed whole-answer grades; never alter native answers or supply them as prompts."""
import json, hashlib, statistics
from pathlib import Path
root=Path(__file__).resolve().parent
# Grades follow row order (two letter seeds x A/B/C, then four cases x A/B/C).
grades={
 'gemma-4-e2b':[
  ('wrong','Geber, Empfänger und verletzte Person vertauscht.'),
  ('wrong','Geber/Empfänger vertauscht, Weitergabe als Grund erfunden.'),
  ('wrong','Erzähler nimmt korrekt entgegen, Rede behauptet die Gegenrichtung; Grund unbelegt.'),
  ('partial','Losungswort richtig; fragt die Begründung aus falscher Perspektive zurück.'),
  ('wrong','Geber/Empfänger vertauscht, Bedarf des Empfängers als Grund erfunden.'),
  ('wrong','Geber/Empfänger und Verletzung vertauscht.'),
  ('correct','Kora gibt Levin, eigene Schulter verletzt, korrekter Grund; Gegenwartsform.'),
  ('partial','Richtung und Schulter richtig, Grund du brauchst es unbelegt.'),
  ('partial','Schulter richtig; neuer Hilfe-/Bedarfsgrund, Erzähler als Rede ausgegeben.'),
  ('partial','Junas Hand und Zusammenhang korrekt, bestätigt aber bekannte Angaben nur fragend.'),
  ('correct','Junas/deine Hand verletzt, kausaler Zusammenhang korrekt.'),
  ('wrong','Jonas statt Juna als verletzte Person.'),
  ('wrong','Alma als eigene Schwester und Karte an Niko statt Selma.'),
  ('wrong','Karte an Selma korrekt, Alma weiterhin als eigene Schwester; Alma zusätzlich in Szene.'),
  ('wrong','Alma als eigene Schwester.'),
  ('partial','Träger beantwortet, Eigentümer fehlt; ihr Amulett ist mehrdeutig.'),
  ('correct','Daria trägt, Malik besitzt; keine neue Handlung erfunden.'),
  ('partial','Tragen für Malik korrekt, Eigentum nur indirekt nahegelegt, nicht beantwortet.')],
 'huihui-qwen3-4b':[
  ('wrong','Geber/Empfänger und Verletzung vertauscht.'),
  ('correct','Rian gab Mira; Rians Hand und Grund korrekt, Morgenstern korrekt.'),
  ('wrong','Geber/Empfänger und Verletzung vertauscht, zusätzliche eigene Lieferung erfunden.'),
  ('wrong','Geber/Empfänger und Verletzung vertauscht.'),
  ('correct','Rian gab Mira; Rians Hand und Grund korrekt, Morgenstern korrekt.'),
  ('wrong','Geber/Empfänger und Verletzung vertauscht, zusätzliche Absicht erfunden.'),
  ('correct','Kora gibt Levin, Koras Schulter, korrekter Grund; Gegenwartsform.'),
  ('partial','Richtung, eigene Schulter und Grund korrekt; unbelegte Einschätzung von Levins Tragen.'),
  ('partial','Richtung und Schulter korrekt, zusätzliche symbolische Motivation erfunden.'),
  ('partial','Junas Hand korrekt; erzählt zusätzlich früheres Feststellen und beantwortet falsche Warum-Frage.'),
  ('partial','Richtige verletzte Person und Bezug zur Tür, fragt bereits bekannten Zustand erneut ab.'),
  ('correct','Juna verletzt; deshalb kann Juna die Tür nicht öffnen.'),
  ('wrong','Alma als eigene Schwester, Aron als Nikos Bruder.'),
  ('correct','Alma Nikos Schwester, Aron Selmas Bruder, Alma sendet Karte.'),
  ('wrong','Alma als eigene Schwester.'),
  ('wrong','Träger vertauscht, widersprüchliches gegenseitiges Tragen, Eigentümer fehlt.'),
  ('partial','Rollen korrekt; Erzähler erfindet neue Übergabe durch Malik.'),
  ('correct','Daria trägt, Malik besitzt, vollständig korrekt.')]
}
rows=[]; proof=[]
for model,manual in grades.items():
 source=root/f'{model}-comparison.json'; actual=json.loads(source.read_text(encoding='utf-8'))
 assert len(actual)==len(manual)==18
 baseline=json.loads((root.parent/'active-memory-0.8.1/models-final'/f'{model}-answers.json').read_text(encoding='utf-8'))
 anchor=next(r for r in baseline if r['case']=='archive')
 for r,(grade,note) in zip(actual,manual):
  assert r['plannedInputTokens']==r['nativePromptTokens'] and r['accepted']
  if r['case']=='letter-rian-mira' and r['condition']=='A':
   assert r['system']==anchor['system'] and r['history']==anchor['history']
   if r['seed']==42: assert r['answer']==anchor['answer']
  rows.append({k:r[k] for k in ['model','case','condition','seed','plannedInputTokens','nativePromptTokens','firstTextMs','totalMs','guard081Accepted','answer']}|{'manualWholeGrade':grade,'usableWholeAnswer':grade=='correct','notes':note,'artifact':source.name})
 for case in dict.fromkeys(r['case'] for r in actual):
  abc={r['condition']:r for r in actual if r['case']==case and r['seed']==42}
  assert abc['A']['history']==abc['B']['history']
  ref=abc['B']['system']; a=abc['A']['system']; start=a.index('\nORIGINALQUELLEN ZUR AKTUELLEN FRAGE:'); end=a.index('Ende Originalstelle '+str(len(abc['A']['recalledSources']))+'.',start)+len('Ende Originalstelle '+str(len(abc['A']['recalledSources']))+'.')
  assert ref==a[:start]+'\nMANUELL GEPRÜFTE EREIGNISDARSTELLUNG (NUR TESTREFERENZ):\n'+abc['A']['manualReference']+'\n'+a[end:]
  proof.append({'model':model,'case':case,'ABSameHistory':True,'onlyArchiveBlockReplaced':True,'tokens':{c:x['plannedInputTokens'] for c,x in abc.items()},'renderedPromptSha256':{c:hashlib.sha256(x['formattedPrompt'].encode()).hexdigest() for c,x in abc.items()}})
summary=[{'model':model,'condition':c,'usable':sum(r['usableWholeAnswer'] for r in rows if r['model']==model and r['condition']==c),'total':6} for model in grades for c in 'ABC']
payload={'method':'Whole-answer human review; strict usability includes all requested facts and no fabricated past, motives or player action. Partial answers are not counted as correct. One question per new scenario, anchor with two seeds. No general model ranking or causal inference. B/C manual references are not automatic extraction.','sampling':{'CPUThreads':4,'context':4096,'outputLimit':512,'temperature':0.75,'topP':0.9,'topK':40,'repetitionPenalty':1.08,'repetitionWindow':256,'seeds':[42,31415]},'promptComparison':proof,'summary':summary,'rows':rows}
(root/'comparison-assessment.json').write_text(json.dumps(payload,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False))
