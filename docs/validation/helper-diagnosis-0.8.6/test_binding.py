import unittest,json
from pathlib import Path
from binding import bind
class BindingTests(unittest.TestCase):
 def setUp(self):
  self.s=json.loads((Path(__file__).parent/'cases-frozen.json').read_text(encoding='utf-8'))[0]['source']
  self.text=json.dumps({'answer':'Nur diagnostisch','source':'S1','quote':self.s['text']})
 def test_app_adds_all_identities_and_no_semantic_certification(self):
  r=bind(self.s,self.s,self.text,'B');self.assertEqual(self.s['story'],r['story']);self.assertFalse(r['semanticTruthConfirmed'])
 def test_foreign_and_stale_story_request_revision_originals_are_rejected(self):
  for k in ('story','request','version','sourceId','sourceRevision'):
   with self.subTest(k=k),self.assertRaises(ValueError):bind(self.s,{**self.s,k:'different'},self.text,'B')
  with self.assertRaises(ValueError):bind(self.s,{**self.s,'text':'changed'},self.text,'B')
 def test_private_excluded_forged_alias_quote_and_commands_are_rejected(self):
  for key,val in [('characterKnows',False),('excluded',True)]:
   with self.assertRaises(ValueError):bind(self.s,{**self.s,key:val},self.text,'B')
  for changes in ({'source':'S2'},{'quote':'Erfundener Ausschnitt'},{'sql':'DROP TABLE messages'},{'story':'injected'}):
   with self.assertRaises(ValueError):bind(self.s,self.s,json.dumps({**json.loads(self.text),**changes}),'B')
 def test_unknown_is_distinct_from_proved_or_disproved(self):
  t=json.dumps({'verdict':'unbekannt','source':'S1','quote':''})
  self.assertEqual('unbekannt',bind(self.s,self.s,t,'C')['interpretation']['verdict'])
  for v in ('belegt','widerlegt'):
   with self.assertRaises(ValueError):bind(self.s,self.s,json.dumps({'verdict':v,'source':'S1','quote':''}),'C')
if __name__=='__main__':unittest.main()
