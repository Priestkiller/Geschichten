"""Isolated proposed interface. App-owned identities; no database writes or model commands."""
import hashlib,json
def bind(captured,live,text,mode):
    for k in ('story','request','version','sourceId','sourceRevision'):
        if captured[k]!=live[k]:raise ValueError('fremder/veralteter Arbeitsstand: '+k)
    if live['excluded'] or not live['characterKnows']:raise ValueError('Quelle nicht für die Figur freigegeben')
    if hashlib.sha256(live['text'].encode()).hexdigest()!=live['sourceRevision']:raise ValueError('Originalrevision stimmt nicht')
    result=json.loads(text)
    keys={'verdict','source','quote'} if mode=='C' else {'answer','source','quote'}
    if not isinstance(result,dict) or set(result)!=keys or not all(isinstance(v,str) for v in result.values()):raise ValueError('Format')
    if result['source']!=captured['alias']:raise ValueError('Quellenalias ist nicht gebunden')
    if result['quote'] and result['quote'] not in live['text']:raise ValueError('Ausschnitt ist kein unverändertes Original')
    if mode=='C' and result['verdict'] not in ('belegt','widerlegt','unbekannt'):raise ValueError('Urteil')
    if mode=='C' and result['verdict']!='unbekannt' and not result['quote']:raise ValueError('Urteil ohne Beleg')
    return {'story':captured['story'],'request':captured['request'],'version':captured['version'],
            'sourceId':captured['sourceId'],'sourceRevision':captured['sourceRevision'],'interpretation':result,
            'semanticTruthConfirmed':False}
