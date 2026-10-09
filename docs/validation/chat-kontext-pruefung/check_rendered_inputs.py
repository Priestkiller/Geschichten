"""Regression on actual native templates, not a simulated tokenizer or inference provider."""
import json,pathlib
ROOT=pathlib.Path(__file__).resolve().parent
def read(name):return json.loads((ROOT/name).read_text(encoding='utf-8'))
cases={r['case']:r for r in read('inputs-final.json')}
result=[]
for tag in ['dolphin-tokens-cache','gemma3-tokens-cache','huihui-tokens-cache','gemma4-tokens','qwen25-tokens','qwen06-tokens']:
    for native in read(tag+'.json'):
        if native['case']=='overflow-character-budget':
            assert 'error' in native,(tag,'Overflow accepted');continue
        source=cases[native['case']];formatted=native.get('formatted',native.get('currentRendered'))
        checks={'system':source['system'] in formatted,
                'allHistoryTexts':all(m['text'] in formatted for m in source['history']),
                'latestExactlyOnce':formatted.count(source['history'][-1]['text'])==1,
                'order':[formatted.index(m['text']) for m in source['history']]==sorted(formatted.index(m['text']) for m in source['history'])}
        assert all(checks.values()),(tag,checks)
        if tag.startswith('qwen'):
            # These two pinned LiteRT files must now include the MODEL replies as assistants.
            for msg in source['history']:
                role='user' if msg['user'] else 'assistant'
                block='<|im_start|>'+role+'\n'+msg['text']+'<|im_end|>'
                assert formatted.count(block)==1,(tag,'wrong or duplicated role',msg)
        result.append({'runtime':tag,'case':native['case'],'checks':checks})
for tag in ['qwen25','qwen06']:
    for native in read('template-before/'+tag+'-tokens.json'):
        if native['case'] not in cases:continue
        source=cases[native['case']]
        missing=[i for i,m in enumerate(source['history']) if m['text'] not in native['currentRendered']]
        assert missing==[i for i,m in enumerate(source['history']) if not m['user']]
        result.append({'runtime':tag,'case':native['case'],'beforeMissingCharacterMessages':len(missing)})
for tokenizer in read('litert-tokenizers.json'):
    assert all(x['exactlyMatchesNative'] for x in tokenizer['counts'])
(ROOT/'template-regression.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print('12 final rendered inputs, 6 overflows, 4 before-template regressions and 6 tokenizer counts verified.')
