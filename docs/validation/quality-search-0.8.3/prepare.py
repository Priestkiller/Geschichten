"""Freeze baseline and 20 synthetic scenes before any production changes."""
import copy, hashlib, json, shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def save(name, value):
    p=OUT/name
    if p.exists(): raise RuntimeError(f'Immutable file already exists: {p}')
    p.write_text(json.dumps(value,ensure_ascii=False,indent=2),encoding='utf-8')

old=ROOT/'docs/validation/roles-0.8.2'
manifest=json.loads((old/'source-changes.json').read_text())
checks=[]
for group in ('changed','newProductionAndTestFiles','requiredUnchanged','newDocuments'):
    for f in manifest[group]:
        actual=sha(ROOT/f['path']); expected=f.get('afterSha256',f.get('sha256'))
        checks.append(dict(path=f['path'],sha256=actual,matches082=actual==expected))
assert all(c['matches082'] for c in checks), checks
snap=OUT/'baseline-source'
shutil.copytree(ROOT/'app/src',snap/'app/src')
shutil.copy2(ROOT/'app/build.gradle.kts',snap/'app/build.gradle.kts')
shutil.copytree(ROOT/'app/build/tmp/kotlin-classes/debug',OUT/'baseline-classes')
frozen={str(p.relative_to(old)):sha(p) for p in old.rglob('*') if p.is_file() and p.suffix in ('.json','.java','.ps1')}
models=ROOT/'docs/validation/models-0.7.0/model-files'
save('baseline.json',dict(sourceChecks=checks,immutable082=frozen,apkSha256=sha(ROOT/'Geschichten-0.8.2.apk'),
    models=[dict(file=n,bytes=(models/n).stat().st_size,sha256=sha(models/n)) for n in ['gemma-4-E2B-it.litertlm','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf']],
    runtimes=dict(llama='d2e54583c7452353eb35d40431281f6ee984332f',litert='0.17.1'),config=dict(context=4096,reserve=512,threads=4,seed=42,temperature=.75,topP=.9,topK=40,repetitionPenalty=1.08)))

# Split is fixed in the authored definitions. Held-out outputs will only be run after parameter freeze.
specs=[
('dev-transfer','Liora','Bennet','Ich reiche dir die Karte, weil meine rechte Hand verletzt ist. Du nimmst sie entgegen.','Wer gab wem die Karte und warum?','Bennet gab Liora die Karte; Bennets rechte Hand ist verletzt.','Umgekehrte Übergabe oder Lioras Hand verletzt.'),
('dev-reverse','Oskar','Fenna','Oskar gibt Fenna den Kristall. Oskars linke Schulter ist verletzt. Fenna ist unverletzt.','Wer gab wem den Kristall und wessen Schulter ist verletzt?','Oskar gab Fenna den Kristall; Oskars linke Schulter verletzt.','Fenna als Geberin oder Verletzte.'),
('dev-family','Tessa','Joris','Meine Schwester heißt Wiebke. Dein Bruder heißt Nils. Wiebke hat dir eine Karte geschickt.','Von wessen Schwester kam die Karte und wie heißt dein Bruder?','Joris Schwester Wiebke, Tessas Bruder Nils.','Tessas Schwester Wiebke, Joris Bruder Nils.'),
('dev-owner','Emil','Nora','Das Buch gehört Nora. Emil trägt es für Nora. Es liegt nun auf dem Tisch.','Wem gehört das Buch und wo liegt es?','Nora ist Eigentümerin, Tisch ist Ablageort, Emil trug es.','Eigentum an Emil oder Tisch übertragen.'),
('dev-negation','Hedda','Sven','Ich bin nicht verletzt. Wenn ich dir das Schwert gäbe, wäre es nur geliehen. Habe ich es dir gegeben?','Was weißt du über meine Verletzung und die Übergabe?','Sven unverletzt; keine bestätigte Übergabe.','Hypothese oder Frage als vollzogene Übergabe.'),
('dev-panic','Alva','Henrik','Alva bekommt in engen, dunklen Räumen schnell Panik. Henrik ist höhenängstlich.','Wie reagierst du auf den schmalen unterirdischen Gang?','Alvas Beklemmung/Panik auf Enge berücksichtigen; nicht Henriks Höhenangst übernehmen.','Keine Angst oder Höhenangst bei Alva.'),
('dev-sound','Dorian','Ilse','Dorian kann schrille Geräusche seit dem Unfall kaum ertragen. Ilse liebt helle Glockenklänge.','Wie reagierst du, wenn der Alarm plötzlich kreischt?','Dorian belastet schriller Alarm; passende Reaktion in Gegenwart.','Ilse als Geräuschempfindlicher oder erfundener Unfallhergang.'),
('dev-multiple','Esra','Konrad','Esra meidet offene Flammen. Die Notbeleuchtung im Tunnel läuft mit Batterien.','Wie können wir den Tunnel beleuchten, ohne dich zu erschrecken?','Batteriebetriebene Notbeleuchtung; Esras Abneigung gegen Feuer.','Fackel als angstfreie Lösung oder erfundene gemeinsame Feuerkatastrophe.'),
('dev-correction','Ida','Lutz','Der Dolch liegt in der Truhe. Korrektur: Der Dolch liegt im Regal. Danach lege ich den Dolch auf den Tisch.','Wo liegt der Dolch jetzt?','Tisch; nach Korrektur nochmals verändert.','Truhe oder Regal als aktueller Ort.'),
('dev-unknown','Viktor','Maren','Viktor und Maren sehen sich heute zum ersten Mal.','Was habe ich dir gestern in der Werkstatt versprochen?','Keine belegte gemeinsame Erinnerung; natürliche Nachfrage.','Erfundene gemeinsame Begegnung oder Versprechen gestern.'),
('hold-transfer','Selin','Arvid','Ich gebe dir das Amulett. Mein linkes Bein ist verletzt; deines ist unverletzt.','Wer hat dir das Amulett gegeben und welches Bein ist verletzt?','Arvid gab Selin das Amulett; Arvids linkes Bein verletzt.','Selin als Geberin/Verletzte.'),
('hold-reverse','Tammo','Yara','Tammo reicht Yara das Buch. Tammo hat eine verletzte rechte Hand. Yara kann es öffnen.','Warum hast du mir das Buch gereicht und wer hält es nun?','Tammo gab Yara das Buch; Tammo rechte Hand verletzt; Yara hält.','Yaras Verletzung oder Tammo als Empfänger.'),
('hold-family','Enna','Silas','Meine Schwester heißt Ruth. Dein Bruder heißt Ole. Ruth hat dir den Kristall geschenkt.','Von wem kam der Kristall und mit wem ist diese Person verwandt?','Ruth ist Silas Schwester, gab Enna Kristall.','Ennas Schwester Ruth.'),
('hold-owner','Ruben','Amelie','Der Ring gehört Amelie. Ruben verwahrt ihn nur. Der Ring liegt auf der Kommode.','Ist der Ring dein Eigentum oder verwahrst du ihn nur, und wo liegt er?','Amelie Eigentümerin, Ruben Verwahrer, Kommode.','Ruben Eigentümer oder Amelie Verwahrerin.'),
('hold-negation','Mika','Bruna','Ich bin nicht mehr verletzt. Würde ich dir den Ring geben, bliebe er meiner. Das habe ich noch nicht getan.','Habe ich dir den Ring gegeben und bin ich noch verletzt?','Bruna geheilt; keine Übergabe.','Hypothetische Übergabe vollzogen/Bruna noch verletzt.'),
('hold-water','Jette','Lorenz','Jette kann nicht schwimmen und hat Angst vor tiefem Wasser. Lorenz fühlt sich auf Schiffen wohl.','Wie reagierst du auf die Überfahrt mit dem schwankenden Boot?','Jettes Furcht vor tiefem Wasser; nicht Lorenz Wohlgefühl übernehmen.','Jette als begeisterte Schwimmerin.'),
('hold-crowd','Arne','Mila','Arne wird bei dichtem Gedränge nervös und sucht einen ruhigen Randplatz. Mila ist geräuschempfindlich.','Wie findest du dich in der überfüllten Markthalle zurecht?','Arnes Abneigung gegen Gedränge/Randplatz.','Milas Geräuschempfindlichkeit als Arnes belegte Eigenschaft.'),
('hold-multiple','Bea','Timo','Bea verträgt keine Nüsse. Im Proviantbeutel liegen Haferkekse ohne Nüsse.','Was aus unserem Vorrat könntest du ohne allergische Reaktion essen?','Nussfreie Haferkekse; Beas Nussunverträglichkeit.','Nüsse empfehlen oder Timo Allergie zuschreiben.'),
('hold-correction','Oleander','Rike','Die Tasche liegt auf der Bank. Korrektur: Die Tasche liegt unter dem Bett. Nun lege ich sie in den Schrank.','Wo liegt die Tasche inzwischen?','Schrank ist letzter Stand.','Bank/Bett als aktueller Ort.'),
('hold-unknown','Frieda','Wenzel','Frieda und Wenzel treffen sich zum ersten Mal am Stadttor.','Erinnerst du dich an unseren Urlaub am Meer letztes Jahr?','Keine gesicherte gemeinsame Vergangenheit; offen nachfragen.','Gemeinsamen Urlaub als Erinnerung erfinden.'),
]
template=json.loads((old/'controlled-inputs.json').read_text())[0]['bundle']
rows=[]
for i,(case,char,player,source,q,expected,forbidden) in enumerate(specs):
    sid=case; b=copy.deepcopy(template); tick=1800000000000+i*1000
    opening=f'*{char} blickt {player} an.* „Wir können hier reden.“'
    b['character'].update(id='figure-'+case,name=char,role=f'Reisebegleitung von {player}',personality=f'{char} ist 29 Jahre alt, praktisch und herzlich, spricht klares Deutsch und bleibt in der Szene.',traits='Praktisch, herzlich, vorsichtig',scenario=f'{char} und {player} sind im Hof.',openingMessage=opening)
    b['story'].update(id=sid,characterId=b['character']['id'],title=case,startContext=b['character']['scenario'],createdAt=tick,updatedAt=tick+100)
    def msg(n,role,text): return dict(id=f'{case}-{n}',storyId=sid,role=role,text=text,createdAt=tick+n)
    messages=[msg(0,'CHARACTER',opening),msg(1,'USER',f'Ich heiße {player}.'),msg(2,'CHARACTER',f'*{char} nickt.* „Gut.“')]
    # Relevant facts in original separated sources for the two-source cases.
    sources=source.split('. ') if 'multiple' in case else [source]
    for text in sources: messages.append(msg(len(messages),'USER',text));messages.append(msg(len(messages),'CHARACTER',f'*{char} nickt kurz.* „Verstanden.“'))
    relevant=[m['id'] for m in messages if m['role']=='USER' and m['id']!=f'{case}-1']
    # Distinct natural distractors plus long original archive. Search gets exactly the same source/budget.
    for n in range(12):
        messages.append(msg(len(messages),'USER',f'Wir betrachten Wandfeld {n}. Die Steine sind grau und trocken.'))
        messages.append(msg(len(messages),'CHARACTER',f'*{char} prüft Wandfeld {n}.* '+('Die Fugen sind gerade, der Mörtel ist fest und die Oberfläche unauffällig. '*24)))
    messages.append(msg(len(messages),'USER',q));b['messages']=messages;b['memories']=[]
    identity=copy.deepcopy(template['memory']['facts'][0]);identity.update(id=case+'-identity',storyId=sid,value=player,sourceId=case+'-1',sourceText=f'Ich heiße {player}.',version=1,createdAt=tick+1);identity['entity'].update(id=case+'-person',storyId=sid)
    b['memory']=dict(version=1,facts=[identity],knowledge=[dict(factId=identity['id'],knower=k,sourceId=case+'-1',version=1) for k in ['player','character']],scannedSources=[m['id'] for m in messages[:-1]],excludedEvents=[],pendingSources=1,overview='',characterAliases=[char.lower()])
    rows.append(dict(case=case,split='development' if case.startswith('dev-') else 'heldout',bundle=b,expected=expected,forbidden=forbidden,creativeAllowed='Neue aktuelle Gesten, knappe Atmosphäre und Vorschläge; keine Spielerhandlung oder belegte Vergangenheit erfinden.',relevantSourceIds=relevant,repeat=False))
save('new-scenes.json',rows)
save('split.json',dict(development=[r['case'] for r in rows[:10]],heldout=[r['case'] for r in rows[10:]],sha256=sha(OUT/'new-scenes.json'),criterion='Whole answer, separate factual/roles/completeness/German/style/past/meta/echo/repetition/refusal/abort; acceptance independent.'))
print('Verified 0.8.2, baseline frozen, 10 development + 10 heldout scenes frozen.')
