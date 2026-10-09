"""Test-only adapter for the official Qwen3.5 non-thinking template suffix."""
from pathlib import Path
import hashlib
import json

root = Path(__file__).resolve().parent
source_path = root.parents[2] / "app/src/main/cpp/gguf-jni.cpp"
source = source_path.read_text(encoding="utf-8")
needle = '        const auto vocab = llama_model_get_vocab(s->model);'
assert source.count(needle) == 1
insert = r'''        // Probe only: exact text-only generation suffix for enable_thinking=false.
        // Verified against Qwen/Qwen3.5-4B's pinned official chat_template.jinja.
        const std::string no_thinking = "<think>\n\n</think>\n\n";
        formatted.resize(n + no_thinking.size() + 1);
        std::copy(no_thinking.begin(), no_thinking.end(), formatted.begin() + n);
        n += static_cast<int>(no_thinking.size());
'''
adapted = source.replace(needle, insert + needle)
target = root / "qwen35-probe-jni.cpp"
target.write_text(adapted, encoding="utf-8")
receipt = {"productionJniSha256": hashlib.sha256(source_path.read_bytes()).hexdigest(),
           "probeJniSha256": hashlib.sha256(target.read_bytes()).hexdigest(),
           "difference": "Append official <think>\\n\\n</think>\\n\\n prefix before generation; no sampling or scene changes",
           "androidAppChanged": False}
(root / "qwen35-probe-adapter.json").write_text(json.dumps(receipt,indent=2)+"\n",encoding="utf-8")
print(json.dumps(receipt))
