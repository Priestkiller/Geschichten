"""Bounded Ollama tests on the user's actual LAN server. No Android DB writes."""
import argparse
import copy
import hashlib
import json
import pathlib
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone

ROOT = pathlib.Path(__file__).resolve().parents[3]
HERE = pathlib.Path(__file__).resolve().parent
BASE = "http://192.168.178.73:11434"
MODEL = "hf.co/Qwen/Qwen3-8B-GGUF:Q4_K_M"
ALIAS = "geschichten-qwen3-8b-test"
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))
OPTIONS = dict(num_ctx=4096, num_predict=384, seed=42, temperature=0.7,
               top_k=20, top_p=0.8, min_p=0, repeat_penalty=1,
               presence_penalty=1.5)


def read(path):
    return json.loads((ROOT / path).read_text(encoding="utf-8-sig"))


def save(path, value):
    # Never silently replace an earlier measurement or frozen input.
    with path.open("x", encoding="utf-8") as f:
        json.dump(value, f, ensure_ascii=False, indent=2)
        f.write("\n")


def api(path, body=None, timeout=30):
    data = None if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"})
    with OPENER.open(req, timeout=timeout) as r:
        return json.load(r)


def prepare():
    sources = ["docs/validation/team-0.8.6/raw-team-A.json",
               "docs/validation/team-0.8.6/raw-dialogue-1.json",
               "docs/validation/team-0.8.6/raw-dialogue-2.json",
               "docs/validation/model-comparison-0.7.4/qwen-original.json",
               "docs/validation/helper-diagnosis-0.8.6/cases-frozen.json"]
    single = []
    for path in sources[:3]:
        for row in read(path):
            step = next(x for x in row["steps"] if x["task"] == "narrator")
            single.append(dict(id="replay-" + row["case"], source=path,
                               system=step["system"], history=step["history"],
                               fixtureOnly=True, expectedNotSentToModel=True))
    dialogs = []
    for c in read(sources[3])["cases"]:
        dialogs.append(dict(id=c["id"], source=sources[3],
                            firstHistory=c["turns"][0]["history"][:-1],
                            turns=[dict(user=t["user"], system=t["system"],
                                        expected=t["expected"]) for t in c["turns"]],
                            oldGeneratedAnswersNotReused=True,
                            contextNote="Frozen 0.7.4 roleplay fixture; new model answers retained each turn."))
    extra = [c for c in read(sources[4]) if c["split"] == "acceptance"]
    if len(extra) != 12:
        raise RuntimeError("Unexpected acceptance fixture count")
    frozen = dict(createdAt=datetime.now(timezone.utc).isoformat(),
                  server=BASE, reportedHardware=dict(cpu="Intel i7-6700K", ramGB=32, gpu="RTX 2060", vramGiB=6),
                  originalModel=MODEL, alias=ALIAS, qualityOptions=OPTIONS,
                  sourceSHA256={p: hashlib.sha256((ROOT/p).read_bytes()).hexdigest() for p in sources},
                  replay=single, dialogs=dialogs, sourceQA=extra,
                  gate="Manual: correctness, speaker roles, no invented established fact, refusal respected, coherent German. All failures retained.",
                  scope="Model-only server test. Not an Android DB persistence/retrieval integration test.",
                  noDownloads=True, noCloud=True, noHelper=True)
    save(HERE / "plan-frozen.json", frozen)
    print(json.dumps({"replay":len(single), "dialogTurns":sum(len(d["turns"]) for d in dialogs), "sourceQA":len(extra)}), flush=True)


def chat(out, ident, messages, model=MODEL, options=None, think=None):
    opts = dict(OPTIONS if options is None else options)
    body = dict(model=model, messages=messages, options=opts, stream=True, keep_alive="10m")
    if think is not None:
        body["think"] = think
    row = dict(id=ident, request=body, startUTC=datetime.now(timezone.utc).isoformat(),
               server=BASE, content="", thinking="", completed=False)
    rawpath = out / (ident + ".ndjson")
    first_content = first_thought = None
    start = time.perf_counter()
    try:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        req = urllib.request.Request(BASE + "/api/chat", data=data,
                                     headers={"Content-Type":"application/json"})
        with OPENER.open(req, timeout=240) as response, rawpath.open("xb") as raw:
            for line in response:
                raw.write(line)
                raw.flush()
                if not line.strip():
                    continue
                chunk = json.loads(line)
                if "error" in chunk:
                    raise RuntimeError(chunk["error"])
                msg = chunk.get("message", {})
                content = msg.get("content", "")
                thought = msg.get("thinking", "")
                if content.strip() and first_content is None:
                    first_content = time.perf_counter() - start
                if thought.strip() and first_thought is None:
                    first_thought = time.perf_counter() - start
                row["content"] += content
                row["thinking"] += thought
                if chunk.get("done"):
                    row["finalChunk"] = chunk
                    row["completed"] = True
        if not row["completed"]:
            raise RuntimeError("Stream ended without done=true")
    except Exception as e:
        row["error"] = repr(e)
        if isinstance(e, urllib.error.HTTPError):
            row["errorBody"] = e.read().decode("utf-8", errors="replace")
    row["wallSeconds"] = time.perf_counter() - start
    row["firstContentSeconds"] = first_content
    row["firstThinkingSeconds"] = first_thought
    row["rawSHA256"] = hashlib.sha256(rawpath.read_bytes()).hexdigest() if rawpath.exists() else None
    try:
        row["runningModels"] = api("/api/ps")
    except Exception as e:
        row["runningModelsError"] = repr(e)
    row["truncated"] = row.get("finalChunk",{}).get("done_reason") == "length"
    count = row.get("finalChunk",{}).get("prompt_eval_count")
    row["reportedPromptTokensPlusReservedOutputWithinContext"] = (count+opts["num_predict"] <= opts["num_ctx"]) if count is not None else None
    save(out/(ident + ".json"), row)
    print(json.dumps({"id":ident,"completed":row["completed"],"firstContentSeconds":first_content,
                      "seconds":row["wallSeconds"],"truncated":row["truncated"],
                      "thinkingChars":len(row["thinking"]),"preview":row["content"][:90],
                      "error":row.get("error")},ensure_ascii=True), flush=True)
    return row


def metadata(out):
    save(out/"server-version.json", api("/api/version"))
    save(out/"server-models.json", api("/api/tags"))
    save(out/"original-model-show.json", api("/api/show",dict(model=MODEL)))
    save(out/"running-before.json", api("/api/ps"))


def alias(out):
    show = api("/api/show", dict(model=MODEL))
    template = show["template"]
    old = '<|im_start|>assistant\n<think>\n{{ end }}\n{{- end }}'
    new = ('<|im_start|>assistant\n'
           '{{ if and $.IsThinkSet (not $.Think) -}}\n<think>\n\n</think>\n\n'
           '{{ else -}}\n<think>\n{{ end -}}\n{{ end }}\n{{- end }}')
    if template.count(old) != 1:
        raise RuntimeError("Imported template changed; cannot safely patch")
    changed = template.replace(old, new)
    user_end = '{{ .Content }}<|im_end|>'
    if changed.count(user_end) != 1:
        raise RuntimeError("Cannot safely locate the user-turn ending")
    changed = changed.replace(user_end, '{{ .Content }}{{ if and $.IsThinkSet (not $.Think) }} /no_think{{ end }}<|im_end|>')
    body = dict(model=ALIAS, **{"from":MODEL}, template=changed,
                capabilities=show["capabilities"],
                parameters=dict(num_ctx=4096, num_predict=384), stream=False)
    save(out/"alias-create-request.json", body)
    result = api("/api/create", body, timeout=120)
    save(out/"alias-create-response.json", result)
    revised = api("/api/show",dict(model=ALIAS))
    save(out/"alias-model-show.json", revised)
    original_from = next(l for l in show["modelfile"].splitlines() if l.startswith("FROM "))
    revised_from = next(l for l in revised["modelfile"].splitlines() if l.startswith("FROM "))
    proof = dict(originalWeightFrom=original_from, aliasWeightFrom=revised_from,
                 sameWeights=original_from==revised_from,
                 originalStillUnchanged=api("/api/show",dict(model=MODEL))==show,
                 templateApplied=revised["template"]==changed)
    save(out/"same-weights-proof.json", proof)
    if not proof["sameWeights"] or not proof["originalStillUnchanged"] or not proof["templateApplied"]:
        raise RuntimeError("Weight/original preservation check failed")
    print(json.dumps({"aliasCreated":ALIAS,"sameWeights":True,"thinking":revised.get("thinking"),"capabilities":revised.get("capabilities")}), flush=True)


def benchmark(out, optimized):
    model = ALIAS if optimized else MODEL
    # Explicit unload establishes a cold load. No user transcript is deleted.
    save(out/"unload-response.json", api("/api/generate",dict(model=model,keep_alive=0,stream=False)))
    save(out/"running-after-unload.json", api("/api/ps"))
    message = [dict(role="user",content="Antworte auf Deutsch mit einem kurzen Satz: Bist du bereit? /no_think")]
    opts = dict(OPTIONS,num_predict=128)
    prefix = "optimized" if optimized else "imported"
    chat(out,prefix+"-cold-4096",message,model,opts,False if optimized else None)
    chat(out,prefix+"-warm-4096",message,model,opts,False if optimized else None)
    if not optimized:
        chat(out,"imported-think-false",message,model,opts,False)
    else:
        chat(out,"optimized-2048",message,model,dict(opts,num_ctx=2048),False)


def model_messages(system, history):
    return [dict(role="system",content=system)] + [
        dict(role="user" if m["user"] else "assistant",content=m["text"]) for m in history]


def quality(out):
    plan = json.loads((HERE/"plan-frozen.json").read_text(encoding="utf-8"))
    for case in plan["replay"]:
        chat(out,case["id"],model_messages(case["system"],case["history"]),ALIAS,think=False)
    for dialog in plan["dialogs"]:
        history = copy.deepcopy(dialog["firstHistory"])
        for i, t in enumerate(dialog["turns"],1):
            history.append(dict(user=True,text=t["user"]))
            result = chat(out,dialog["id"]+"-turn-"+str(i),model_messages(t["system"],history),ALIAS,think=False)
            if not result["completed"] or not result["content"].strip() or result["truncated"]:
                print("STOP_DIALOG "+dialog["id"]+" incomplete reply",flush=True)
                break
            history.append(dict(user=False,text=result["content"]))
    for c in plan["sourceQA"]:
        system = (f'Continue this German roleplay. You play "{c["addressee"]}". '
                  f'The user plays "{c["speaker"]}". '
                  'Write only your next reply in fluent German. Refer to yourself as ich in dialogue, '
                  'and describe your own actions in third person between asterisks. '
                  'The first user message is the original source, spoken by the player to your character. '
                  'Keep the two persons and their belongings separate. Answer the latest question from that source. '
                  'Do not invent a missing past event or reason. Questions and hypothetical events are not facts. '
                  'Do not act for the player. If a past fact is unknown, say so.')
        messages = [dict(role="system",content=system),dict(role="user",content=c["source"]["text"]),
                    dict(role="assistant",content='„Ich höre zu.“'),dict(role="user",content=c["question"])]
        chat(out,"qa-"+c["id"],messages,ALIAS,think=False)


def main():
    p=argparse.ArgumentParser()
    p.add_argument("mode",choices=["prepare","baseline","alias","optimized","quality"])
    p.add_argument("--output")
    args=p.parse_args()
    if args.mode=="prepare":
        prepare()
        return
    if not args.output:
        raise SystemExit("Explicit fresh output directory required")
    out=HERE/args.output
    out.mkdir(exist_ok=False)
    metadata(out)
    if args.mode=="baseline": benchmark(out,False)
    elif args.mode=="alias": alias(out)
    elif args.mode=="optimized": benchmark(out,True)
    elif args.mode=="quality": quality(out)
    print("COMPLETE "+args.mode,flush=True)


if __name__=="__main__":
    main()
