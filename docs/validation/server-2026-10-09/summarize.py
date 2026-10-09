"""Derive measured statistics; verify raw streams, requests and frozen sources."""
import hashlib
import json
import pathlib
import statistics

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[2]


def read(p):
    return json.loads(p.read_text(encoding="utf-8-sig"))


def stats(rows):
    return dict(count=len(rows),
                firstContentMedianSeconds=statistics.median(r["firstContentSeconds"] for r in rows),
                firstContentMinSeconds=min(r["firstContentSeconds"] for r in rows),
                firstContentMaxSeconds=max(r["firstContentSeconds"] for r in rows),
                totalMedianSeconds=statistics.median(r["wallSeconds"] for r in rows),
                totalMinSeconds=min(r["wallSeconds"] for r in rows),
                totalMaxSeconds=max(r["wallSeconds"] for r in rows),
                decodeMedianTokensPerSecond=statistics.median(
                    r["finalChunk"]["eval_count"]/(r["finalChunk"]["eval_duration"]/1e9) for r in rows))


plan = read(HERE/"plan-frozen.json")
review = read(HERE/"independent-review.json")
rows = [read(p) for p in sorted((HERE/"quality").glob("*.json"))
        if p.name.startswith(("replay-","mira-","grask-","qa-"))]
expected = {c["id"] for c in plan["replay"]}
expected.update(d["id"]+"-turn-"+str(i) for d in plan["dialogs"] for i in range(1,len(d["turns"])+1))
expected.update("qa-"+c["id"] for c in plan["sourceQA"])
assert {r["id"] for r in rows} == expected
assert len(rows)==28
for r in rows:
    rawpath = HERE/"quality"/(r["id"]+".ndjson")
    assert hashlib.sha256(rawpath.read_bytes()).hexdigest()==r["rawSHA256"]
    chunks = [json.loads(line) for line in rawpath.read_text(encoding="utf-8").splitlines() if line.strip()]
    assert "".join(c.get("message",{}).get("content","") for c in chunks)==r["content"]
    assert "".join(c.get("message",{}).get("thinking","") for c in chunks)==r["thinking"]
    assert chunks[-1]["done"] and r["completed"]
    assert chunks[-1]==r["finalChunk"]
    assert r["request"]["think"] is False
    assert r["request"]["model"]==plan["alias"]
    assert r["request"]["options"]==plan["qualityOptions"]
    assert r["reportedPromptTokensPlusReservedOutputWithinContext"]
    assert not r["truncated"] and not r["thinking"]
    assert "<think>" not in r["content"] and "</think>" not in r["content"]
    assert "\ufffd" not in r["content"]
for c in plan["replay"]:
    r = next(r for r in rows if r["id"]==c["id"])
    assert r["request"]["messages"][0]["content"]==c["system"]
    assert [m["content"] for m in r["request"]["messages"][1:]]==[m["text"] for m in c["history"]]
for d in plan["dialogs"]:
    history = [m["text"] for m in d["firstHistory"]]
    for i,t in enumerate(d["turns"],1):
        r=next(r for r in rows if r["id"]==d["id"]+"-turn-"+str(i))
        history.append(t["user"])
        assert [m["content"] for m in r["request"]["messages"][1:]]==history
        history.append(r["content"])
for c in plan["sourceQA"]:
    r=next(r for r in rows if r["id"]=="qa-"+c["id"])
    assert r["request"]["messages"][1]["content"]==c["source"]["text"]
    assert r["request"]["messages"][-1]["content"]==c["question"]
for p,digest in plan["sourceSHA256"].items():
    assert hashlib.sha256((ROOT/p).read_bytes()).hexdigest()==digest
proof = read(HERE/"alias-v3"/"same-weights-proof.json")
assert all(proof[k] for k in ["sameWeights","originalStillUnchanged","templateApplied"])
result = dict(transportAndInputAssertions="passed",qualityCalls=len(rows),
              firstVisibleMeaning="First non-whitespace content chunk as observed over LAN, not a hidden thinking token.",
              all=stats(rows),groups={},
              maxReportedPromptTokens=max(r["finalChunk"]["prompt_eval_count"] for r in rows),
              outputReserve=384,contextTokens=4096,
              technicallyComplete=28,truncated=0,thinkingOutputs=0,
              sameWeights=True,originalPreserved=True,noDatabaseWrites=True,
              qaCoreCorrect=sum(x["coreCorrect"] for x in review["qa"]),qaTotal=len(review["qa"]),
              qaWholeFactGrounded=sum(x["fullyFactGrounded"] for x in review["qa"]),
              replayCoreCorrect=sum(x["coreCorrect"] for x in review["replay"]),replayTotal=len(review["replay"]),
              replayWholeFactGrounded=sum(x["fullyFactGrounded"] for x in review["replay"]),
              dialogueAssessment="Qualitative, not a numerical pass count: creative details and follow-on contamination need human judgment.",
              gpuSnapshotsNote="/api/ps size_vram measures placement/reservation, not live GPU utilization percent.",
              gpuSnapshots=[dict(id=r["id"],models=r["runningModels"]["models"]) for r in rows],
              benchmarks={})
for group,prefixes in [("replay",("replay-",)),("dialogs",("mira-","grask-")),("sourceQA",("qa-",))]:
    result["groups"][group]=stats([r for r in rows if r["id"].startswith(prefixes)])
for folder in ["baseline","optimized"]:
    result["benchmarks"][folder]=[dict(id=r["id"],firstContentSeconds=r["firstContentSeconds"],
                                      totalSeconds=r["wallSeconds"],content=r["content"],metrics=r["finalChunk"])
                                     for p in sorted((HERE/folder).glob("*.json"))
                                     for r in [read(p)] if "request" in r]
(HERE/"summary.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print(json.dumps({k:v for k,v in result.items() if k not in ["gpuSnapshots","benchmarks"]},ensure_ascii=True))
