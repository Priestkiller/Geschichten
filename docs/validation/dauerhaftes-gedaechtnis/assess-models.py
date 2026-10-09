"""Attach explicit human review to genuine recorded replies; never infer pass from a guard."""
from pathlib import Path
import json
folder = Path(__file__).resolve().parent
order = [("short",42),("container",42),("long",42),("long",31415),("unknown",42),("secret",42)]
# Reviewed against the actual answer text, including narration. These are case judgements,
# not a general quality score. A rejected reply remains a model failure in this assessment.
review = {
"gemma-4-e2b": [
 ("wrong", "Ort und Besitz stimmen; die Erzählung gibt Mira zusätzlich Rians verletzte linke Hand."),
 ("correct", "Silbern, in der Truhe, Mira trägt den Schlüssel nicht."),
 ("correct", "Hof, ein silberner Schlüssel bei Rian, Rians linke Hand verletzt, Flucht abgeschlossen."),
 ("wrong", "Erfindet zusätzlich einen bronzenen Schlüssel bei Mira; vom Wächter zurückgewiesen."),
 ("correct", "Keine erfundene Erinnerung oder Farbe; sagt, dass das Geschenk unbekannt ist. Sprachfehler verschrungkter."),
 ("wrong", "Rede nennt Rian, aber die Erzählung lässt Mira den Schlüssel halten. Geheimen Ort nicht behauptet.")],
"huihui-qwen3-4b": [
 ("correct", "Ort, Besitzer und Rians linke Hand korrekt."),
 ("correct", "Silbern und in der Truhe."),
 ("correct", "Alle abgefragten aktuellen Fakten korrekt; sprachlicher Fehler silbernenden."),
 ("partial", "Aktuelle Kernfakten stimmen; den du ihm gegeben hast verwechselt die Beteiligten der Übergabe und weitere ungestützte Aussage über Erwähnen."),
 ("wrong", "Keine Farbe erfunden, aber nennt Rians angebliche Schwester meine Schwester und verwechselt damit die Rollen; unbekanntes Ereignis nicht geklärt."),
 ("correct", "Kein Wissen über das heimliche Verstecken; zuletzt bekannte Person wird als unsichere Rückfrage genannt.")],
"dolphin3-llama3.2-3b": [
 ("incomplete", "Nennt nur den Ort und fragt zu Besitz/Hand zurück, obwohl diese bekannt sind."),
 ("wrong", "Silberne Farbe, aber falscher aktueller Besitzer statt Truhe; sprachlich er trägt jetzt Rian."),
 ("wrong", "Übernimmt Rians Rolle; vom Wächter wegen Besitz zurückgewiesen."),
 ("wrong", "Schlüssel und Verletzung werden Mira zugeschrieben; vom Wächter zurückgewiesen."),
 ("wrong", "Erfindet Blauß als Farbe der unbekannten Erinnerung und antwortet als Rian."),
 ("wrong", "Behauptet den geheimen Truhenort. Ein früherer öffentlicher Truhenort ist im Verlauf, die neue geheime Nachricht ist maskiert: keine nachgewiesene Datenleck-Ursache.")],
"gemma3-davidau-4b": [
 ("partial", "Ort und Besitz stimmen; linke Hand nicht eindeutig und nicht gestützte verschlimmerte Wunde. Schwere Sprachfehler."),
 ("wrong", "Hebt den Schlüssel hoch und gibt Besitz einer Person statt Ablageort Truhe an."),
 ("incomplete", "Ort, Farbe, verletzte Hand und Abschluss genannt; Besitzer und linke Körperseite nicht eindeutig beantwortet. Sprache fehlerhaft."),
 ("wrong", "Mira hat wieder den alten Schlüssel; vom Wächter zurückgewiesen."),
 ("wrong", "Erfindet Gespräch und rote Rosen für unbekanntes Geschenk."),
 ("wrong", "Behauptet aktuellen Besitz bei Rian sicher, obwohl nur zuletzt beobachtetes Wissen vorliegt.")],
"qwen2.5-1.5b": [
 ("wrong", "Übernimmt Rians Rolle; vom Wächter wegen Verletzung zurückgewiesen."),
 ("wrong", "Nennt silbern und Truhe, schreibt aber die bereits geschehene Nutzerhandlung nochmals selbst und unsinnige Arm-Verletzungs-Handlung."),
 ("incomplete", "Wiederholt eine Übergabe; beantwortet Ort, Verletzung und Ziel nicht."),
 ("wrong", "Falscher Besitzer, Verletzungsrolle und abgeschlossenes Ziel wieder offen; zurückgewiesen."),
 ("wrong", "Keine Farbe erfunden, aber nennt Rians angebliche Schwester meine Schwester; Rollenfehler und unbekanntes Ereignis nicht geklärt."),
 ("wrong", "Mira hebt den Schlüssel ohne Grundlage selbst hoch; beantwortet die Wissensfrage nicht.")],
"qwen3-0.6b": [
 ("wrong", "Falsche Verletzungsrolle; vom Wächter zurückgewiesen."),
 ("wrong", "Gibt Mira Besitz statt Truhe; grammatisch ich trägt, vom Regelwächter nicht erkannt."),
 ("incomplete", "Kopiert die wiederholte Mauerbeschreibung statt die Frage zu beantworten."),
 ("incomplete", "Auch bei zweiter Seed nur Wiederholung statt Antwort."),
 ("incomplete", "Mauerwiederholung statt Klärung unbekannter Vergangenheit."),
 ("incomplete", "Kein geheimer Ort behauptet, aber keine Antwort auf die Frage.")]
}
expected = {
"short":"Turmzimmer; Mira hat den einzigen bronzenen Schlüssel; Rians linke Hand ist verletzt; korrekte Rollen auch in der Erzählung.",
"container":"Derselbe Schlüssel ist silbern und liegt in der Truhe; keine Person trägt ihn aktuell.",
"long":"Hof; ein silberner Schlüssel bei Rian; Rians linke Hand bleibt verletzt; Ziel Flucht bleibt abgeschlossen; keine neue Übergabe erfinden.",
"unknown":"Keine belegte gemeinsame Erinnerung, Schwester oder Geschenkfarbe. Unbekanntes ehrlich klären.",
"secret":"Mira kennt die neue Ablage nicht. Letztbekannter Besitz Rian darf unsicher genannt werden; keine geheime Truhe als Tatsache oder erfundene Mira-Handlung."
}
pairs=[]
for model, notes in review.items():
    before=json.loads((folder/(model+"-answers.json")).read_text(encoding="utf-8"))
    after=json.loads((folder/"final-models"/(model+"-answers.json")).read_text(encoding="utf-8"))
    for (case,seed),(result,note) in zip(order,notes):
        b=next(row for row in before if row["case"]==case and row["seed"]==seed and not row["after"])
        a=next(row for row in after if row["case"]==case and row["seed"]==seed)
        pairs.append(dict(model=model,case=case,seed=seed,expected=expected[case],manualAfterResult=result,manualAfterNotes=note,beforeAccepted=b["accepted"],afterAccepted=a["accepted"],beforeAnswer=b.get("answer",b.get("partialAnswer")),afterAnswer=a.get("answer",a.get("partialAnswer")),afterError=a.get("error"),beforeArtifact=model+"-answers.json",afterArtifact="final-models/"+model+"-answers.json"))
(folder/"model-assessment.json").write_text(json.dumps(dict(method="Manual review of complete generated text, separately from deterministic state/prompt/guard tests. Fixed diagnostic seeds 42 and 31415. No general reliability percentage; one character and synthetic cases only.", pairs=pairs),ensure_ascii=False,indent=2),encoding="utf-8")
print("36 genuine before/after pairs reviewed separately from acceptance guards.")
