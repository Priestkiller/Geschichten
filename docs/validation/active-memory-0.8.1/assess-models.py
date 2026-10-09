"""Explicit manual review of the complete recorded answers, separate from acceptance."""
from pathlib import Path
import json
out=Path(__file__).resolve().parent
review={
"gemma-4-e2b":[("wrong_roles","Morgenstern und Wortlaut des Grundes vorhanden, aber Mira gibt statt Rian den Brief und beansprucht dessen Verletzung."),("partial","Blaue Farbe und Mira hält den Schlüssel; zusätzliche unbelegte Eigentumsbehauptung gehört Rian."),("wrong_roles","Fragt nach Details, nennt jedoch Rians angebliche Schwester meine Schwester.")],
"huihui-qwen3-4b":[("wrong_roles","Morgenstern richtig; Geber, Empfänger und verletzte Person im Grund vertauscht."),("correct_facts","Blau und Mira als Träger korrekt. Direkte Rede ist unquoted, Stil unpersönlich."),("invented_past","Erfindet ein Gespräch, Wärme und einen kühlen Morgen, zudem falsche Schwester.")],
"dolphin3-llama3.2-3b":[("unanswered","Wiederholt die Frage; keine Losungswort- oder Begründungsantwort."),("incomplete","Blaue Farbe genannt, fragt aber nach bekanntem Besitzer zurück."),("unanswered","Kopiert Frage und Erzählanweisung statt einer Figurenantwort.")],
"gemma3-davidau-4b":[("partial","Morgenstern, Verletzung bei Rian und Siegel erwähnt; Grund verändert, starke Sprachfehler und zusätzliche Briefbehauptung."),("incomplete","Blau genannt; Besitzerfrage nur kopiert, Stein als Ablenkung."),("invented_past","Erfindet einen Bruder, der ein Schwesterngeschenk erwähnt hat; starke Sprachfehler.")],
"qwen2.5-1.5b":[("wrong_roles","Morgenstern richtig; gibt den Brief in der falschen Richtung und beansprucht Rians Verletzung."),("correct_facts","Blau und Mira als Träger korrekt; keine ausgestaltete Figurenreaktion."),("poor_language","Fragt zunächst nach fehlenden Angaben; unsinniger Zusatz, Rollenwechsel zur eigenen Schwester.")],
"qwen3-0.6b":[("unanswered","Wiederholt Stein-Fülltext statt Originale zu verwenden."),("incomplete","Blau genannt; Besitzerfrage kopiert, keine vollständige Antwort."),("unanswered","Wiederholt Fülltext statt unbekannte Vergangenheit zu klären.")]
}
expected={"archive":"Morgenstern. Rian gab Mira den Brief, weil Rian mit seiner verletzten Hand das Siegel nicht unbeschädigt öffnen konnte.","correction":"Die unmittelbar genannte blaue Farbe gilt. Mira hat den Schlüssel. Kein zusätzlicher Eigentümer wurde belegt.","unknown":"Keine belegte gemeinsame Erinnerung; kein Geschenk, Bruder, Gespräch oder Farbe erfinden. Als Mira nach fehlenden Informationen fragen, Rians und Miras Rollen erhalten."}
rows=[]
for model,notes in review.items():
    recorded=json.loads((out/"models-final"/(model+"-answers.json")).read_text(encoding="utf-8"))
    assert len(recorded)==3
    for (case,(result,note)) in zip(["archive","correction","unknown"],notes):
        row=next(r for r in recorded if r["case"]==case)
        rows.append(dict(model=model,case=case,expected=expected[case],manualResult=result,manualNotes=note,accepted=row["accepted"],answer=row.get("answer",row.get("partialAnswer")),artifact="models-final/"+model+"-answers.json"))
(out/"model-assessment.json").write_text(json.dumps(dict(method="Manual evaluation of complete actual text; factual correctness is separate from guard acceptance. One synthetic story, one seed; no general reliability percentage.",rows=rows),ensure_ascii=False,indent=2),encoding="utf-8")
print("18 genuine answers assessed separately from persistence and prompt handoff.")
