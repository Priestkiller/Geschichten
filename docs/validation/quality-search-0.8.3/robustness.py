"""Post-freeze robustness probes, no changes to ranking parameters and no text generation."""
import copy,json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
template=json.loads((ROOT/'materialized-scenes.json').read_text())[0]['bundle']
specs=[
('similar-items',[("USER","Das rote Amulett gehört Nella."),("USER","Das blaue Amulett gehört Falk.")],"Wem gehört das rote Amulett?",[1],[]),
('secret',[("USER","Unbeobachtet lege ich den Ring in den Tresor."),("USER","Eine Wolke zieht vorbei.")],"Wo liegt der Ring?",[],[1]),
('excluded',[("USER","Das Losungswort lautet Silberbach."),("USER","Ein Bach fließt am Weg.")],"Wie lautet das Losungswort?",[],[1]),
('other-story',[("OTHER","Die Karte liegt im Regal."),("USER","Ein Vogel ruft.")],"Wo liegt die Karte?",[],[1]),
('model-repetition',[("CHARACTER","Ich erinnere mich: Deine Schwester hat mir den Ring geschenkt."),("CHARACTER","Ich erinnere mich: Deine Schwester hat mir den Ring geschenkt.")],"Von wem kam der Ring?",[],[1,2]),
('current-correction',[("USER","Die Karte liegt auf der Bank."),("USER","Die Karte liegt im Schrank.")],"Wo liegt die Karte jetzt?",[2],[1]),
]
rows=[]
for case,sources,q,required,forbidden in specs:
 b=copy.deepcopy(template);case='aux-'+case;b['story'].update(id=case,characterId='nella',createdAt=1800000000000,startContext='Nella und Falk stehen im Hof.');b['character'].update(id='nella',name='Nella',openingMessage='*Nella blickt Falk an.* „Wir können hier reden.“',personality='Nella spricht klar und ist vorsichtig.',role='Begleiterin von Falk',scenario='Hof');b['memories']=[]
 def msg(n,role,text):return dict(id=f'{case}-{n}',storyId=case,role=role,text=text,createdAt=1800000000000+n)
 messages=[msg(0,'CHARACTER',b['character']['openingMessage'])]
 for i,(role,text) in enumerate(sources,1):
  m=msg(i,'CHARACTER' if role=='CHARACTER' else 'USER',text)
  if role=='OTHER':m['storyId']='different-story'
  messages.append(m)
 messages.append(msg(len(messages),'USER',q));b['messages']=messages
 b['memory']=dict(version=2,facts=[],knowledge=[],scannedSources=[],excludedEvents=[],pendingSources=0,overview='',characterAliases=['nella'])
 def fact(name,field,value,status='CURRENT',source=None):
  f=dict(id=case+'-fact',storyId=case,entity=dict(id=case+'-entity',storyId=case,kind='ITEM',name=name),field=field,value=value,status=status,pinned=False,sourceId=source,sourceText='',version=2,knownBy=['player','character'],manual=True,createdAt=1800000000003,sourceOrder=3)
  b['memory']['facts'].append(f)
  if status=='CURRENT':b['memory']['knowledge'].append(dict(factId=f['id'],knower='character',sourceId=None,version=2))
 if 'excluded' in case:fact('Quelle','note','Ausgeschlossen','EXCLUDED',case+'-1')
 if 'current-correction' in case:fact('Karte','holder','In/bei Schrank')
 rows.append(dict(case=case,split='robustness',bundle=b,expected='Filter und genaue Quellenidentität erhalten',relevantSourceIds=[f'{case}-{i}' for i in required],forbiddenSourceIds=[f'{case}-{i}' for i in forbidden]))
(ROOT/'robustness-scenes.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
