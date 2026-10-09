#include <jni.h>
#include "llama.h"
#include "prompt-cache.h"
#include <atomic>
#include <algorithm>
#include <climits>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>

namespace {
struct Session {
    llama_model * model = nullptr;
    llama_context * context = nullptr;
    std::atomic<bool> cancelled{false};
    std::string template_fallback;
    std::vector<llama_token> cached_tokens;
    size_t last_reused = 0;
    size_t last_prompt_size = 0;
    size_t last_decode_size = 0;
    bool last_completed = false;
#ifdef GESCHICHTEN_CACHE_PROBE
    std::vector<float> probe_logits;
    uint32_t probe_seed = LLAMA_DEFAULT_SEED;
    std::string probe_formatted;
    std::vector<llama_token> probe_tokens;
    int probe_top_k = 40;
    float probe_top_p = 0.9f;
    float probe_temperature = 0.75f;
#endif
    ~Session() {
        if (context) llama_free(context);
        if (model) llama_model_free(model);
    }
};
struct CacheGuard {
    Session * value;
    bool committed = false;
    ~CacheGuard() {
        if (!committed) {
            value->cached_tokens.clear();
            llama_memory_clear(llama_get_memory(value->context), true);
        }
    }
};
std::once_flag backend;
bool abort_decode(void * data) { return static_cast<Session *>(data)->cancelled.load(); }
void quiet_log(ggml_log_level, const char *, void *) {}
void fail(JNIEnv * env, const char * text) {
    if (!env->ExceptionCheck()) {
        auto type = env->FindClass("java/io/IOException");
        env->ThrowNew(type, text);
        env->DeleteLocalRef(type);
    }
}
Session * session(jlong handle) {
    if (!handle) throw std::runtime_error("Die KI ist noch nicht geladen.");
    return reinterpret_cast<Session *>(handle);
}
std::string bytes(JNIEnv * env, jbyteArray data) {
    if (!data) throw std::runtime_error("Die Nachricht fehlt.");
    std::string text(env->GetArrayLength(data), '\0');
    env->GetByteArrayRegion(data, 0, text.size(), reinterpret_cast<jbyte *>(text.data()));
    return text;
}
jbyteArray byte_array(JNIEnv * env, const std::string & text) {
    auto result = env->NewByteArray(text.size());
    if (result) env->SetByteArrayRegion(result, 0, text.size(), reinterpret_cast<const jbyte *>(text.data()));
    return result;
}
// A token can end midway through an emoji or umlaut. Never emit partial UTF-8.
size_t complete_utf8(const std::string & text) {
    size_t i = 0;
    while (i < text.size()) {
        const auto c = static_cast<unsigned char>(text[i]);
        size_t n = c < 0x80 ? 1 : (c & 0xe0) == 0xc0 ? 2 : (c & 0xf0) == 0xe0 ? 3 : (c & 0xf8) == 0xf0 ? 4 : 0;
        if (!n) throw std::runtime_error("Die KI hat ungültigen Text erzeugt. Bitte erneut versuchen.");
        if (i + n > text.size()) break;
        for (size_t j = 1; j < n; ++j) {
            if ((static_cast<unsigned char>(text[i + j]) & 0xc0) != 0x80)
                throw std::runtime_error("Die KI hat ungültigen Text erzeugt. Bitte erneut versuchen.");
        }
        i += n;
    }
    return i;
}
std::vector<llama_token> prompt_tokens(JNIEnv *env, Session *s, jobjectArray roles, jobjectArray texts) {
        const auto count = env->GetArrayLength(roles);
        if (count != env->GetArrayLength(texts) || count < 2 || count > 2048)
            throw std::runtime_error("Ungültiger Gesprächsausschnitt.");
        std::vector<std::string> role_strings, content_strings;
        role_strings.reserve(count); content_strings.reserve(count);
        size_t total = 0;
        for (int i = 0; i < count; ++i) {
            auto role = static_cast<jstring>(env->GetObjectArrayElement(roles, i));
            const char * raw = env->GetStringUTFChars(role, nullptr);
            if (!raw) throw std::runtime_error("Ungültige Rolle.");
            role_strings.emplace_back(raw);
            env->ReleaseStringUTFChars(role, raw); env->DeleteLocalRef(role);
            auto text = static_cast<jbyteArray>(env->GetObjectArrayElement(texts, i));
            content_strings.push_back(bytes(env, text)); env->DeleteLocalRef(text);
            total += content_strings.back().size();
            if (total > 40000) throw std::runtime_error("Dieser Gesprächsausschnitt ist zu lang.");
        }
        std::vector<llama_chat_message> messages;
        for (int i = 0; i < count; ++i) messages.push_back({role_strings[i].c_str(), content_strings[i].c_str()});
        const char * tmpl = llama_model_chat_template(s->model, nullptr);
        if ((!tmpl || !tmpl[0]) && !s->template_fallback.empty()) tmpl = s->template_fallback.c_str();
        if (!tmpl) throw std::runtime_error("Dieser KI fehlt die Gesprächsvorlage.");
        std::vector<char> formatted(total * 2 + 4096);
        int n = llama_chat_apply_template(tmpl, messages.data(), messages.size(), true, formatted.data(), formatted.size());
        if (n < 0) throw std::runtime_error("Die Gesprächsvorlage dieser KI wird noch nicht unterstützt.");
        if (n > static_cast<int>(formatted.size())) {
            formatted.resize(n + 1);
            n = llama_chat_apply_template(tmpl, messages.data(), messages.size(), true, formatted.data(), formatted.size());
        }
        if (n <= 0 || n > static_cast<int>(formatted.size())) throw std::runtime_error("Die Gesprächsvorlage konnte nicht gelesen werden.");
        const auto vocab = llama_model_get_vocab(s->model);
        int required = llama_tokenize(vocab, formatted.data(), n, nullptr, 0, true, true);
        if (required == INT_MIN || required >= 0) throw std::runtime_error("Die Nachricht konnte nicht verarbeitet werden.");
        std::vector<llama_token> tokens(-required);
        required = llama_tokenize(vocab, formatted.data(), n, tokens.data(), tokens.size(), true, true);
        if (required < 0)
            throw std::runtime_error("Dieser Gesprächsausschnitt ist zu lang. Bitte kürze die letzte Nachricht oder einige Erinnerungen.");
        tokens.resize(required);
#ifdef GESCHICHTEN_CACHE_PROBE
        s->probe_formatted.assign(formatted.data(), n);
        s->probe_tokens = tokens;
#endif
        return tokens;
}
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_create(JNIEnv * env, jclass, jbyteArray path, jint ctx, jint threads, jbyteArray fallback) {
    try {
        std::call_once(backend, [] { llama_log_set(quiet_log, nullptr); llama_backend_init(); });
        if (ctx < 512 || ctx > 4096 || threads < 1 || threads > 8) throw std::runtime_error("Ungültige KI-Einstellung.");
        auto s = std::make_unique<Session>();
        s->template_fallback = bytes(env, fallback);
        if (!s->template_fallback.empty() && s->template_fallback != "chatml") throw std::runtime_error("Ungültige KI-Gesprächsvorlage.");
        auto file = bytes(env, path);
        auto mp = llama_model_default_params();
        mp.n_gpu_layers = 0; // Portable CPU baseline on Snapdragon and Exynos.
        s->model = llama_model_load_from_file(file.c_str(), mp);
        if (!s->model) throw std::runtime_error("Diese KI konnte nicht geladen werden. Prüfe freien Arbeitsspeicher und versuche es erneut.");
        auto cp = llama_context_default_params();
        cp.n_ctx = ctx;
        cp.n_batch = 256;
        cp.n_ubatch = 128;
        cp.n_threads = threads;
        cp.n_threads_batch = threads;
        cp.no_perf = true;
        cp.abort_callback = abort_decode;
        cp.abort_callback_data = s.get();
        s->context = llama_init_from_model(s->model, cp);
        if (!s->context) throw std::runtime_error("Der Arbeitsspeicher reicht gerade nicht zum Starten dieser KI.");
        return reinterpret_cast<jlong>(s.release());
    } catch (const std::exception & error) { fail(env, error.what()); return 0; }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_generate(JNIEnv * env, jclass, jlong handle, jobjectArray roles, jobjectArray texts, jint max_tokens, jboolean allow_token_limit, jobject callback) {
    try {
        auto s = session(handle);
        CacheGuard cache_guard{s};
        if(max_tokens < 1 || max_tokens > 512) throw std::runtime_error("Ungültige Ausgabelänge.");
        auto tokens = prompt_tokens(env,s,roles,texts);
        if(tokens.size() + max_tokens > llama_n_ctx(s->context)) throw std::runtime_error("Die Eingabe passt nicht in das Kontextfenster.");
        const auto vocab=llama_model_get_vocab(s->model);
        auto memory = llama_get_memory(s->context);
        auto shared = reusable_prompt_prefix(s->cached_tokens, tokens);
        // Preserve the original prefill batch boundaries. Recomputing only one
        // final token can select a different quantized GEMV kernel and change logits.
        shared = (shared / 256) * 256;
        // A shifted/sliding or partially failed native memory cannot be trusted.
        if (llama_memory_seq_pos_min(memory, 0) != 0 ||
            llama_memory_seq_pos_max(memory, 0) != static_cast<llama_pos>(s->cached_tokens.size()) - 1) shared = 0;
        if (shared && !llama_memory_seq_rm(memory, 0, shared, -1)) shared = 0;
        if (!shared) llama_memory_clear(memory, true);
        s->cached_tokens.resize(shared);
        s->last_reused = shared;
        s->last_prompt_size = tokens.size();
        for (size_t offset = shared; offset < tokens.size(); offset += 256) {
            if (s->cancelled.load()) throw std::runtime_error("Antwort angehalten.");
            int size = std::min<size_t>(256, tokens.size() - offset);
            auto batch = llama_batch_get_one(tokens.data() + offset, size);
            if (llama_decode(s->context, batch) != 0) throw std::runtime_error(s->cancelled.load() ? "Antwort angehalten." : "Die KI konnte die Nachricht noch nicht verarbeiten.");
            s->cached_tokens.insert(s->cached_tokens.end(), tokens.begin() + offset, tokens.begin() + offset + size);
        }
#ifdef GESCHICHTEN_CACHE_PROBE
        const auto logits = llama_get_logits_ith(s->context, -1);
        s->probe_logits.assign(logits, logits + llama_vocab_n_tokens(vocab));
#endif
        auto chain = std::unique_ptr<llama_sampler, decltype(&llama_sampler_free)>(llama_sampler_chain_init(llama_sampler_chain_default_params()), llama_sampler_free);
        llama_sampler_chain_add(chain.get(), llama_sampler_init_penalties(llama_vocab_n_tokens(vocab), 256, 1.08f, 0.0f, 0.0f));
#ifdef GESCHICHTEN_CACHE_PROBE
        llama_sampler_chain_add(chain.get(), llama_sampler_init_top_k(s->probe_top_k));
        llama_sampler_chain_add(chain.get(), llama_sampler_init_top_p(s->probe_top_p, 1));
        llama_sampler_chain_add(chain.get(), llama_sampler_init_temp(s->probe_temperature));
#else
        llama_sampler_chain_add(chain.get(), llama_sampler_init_top_k(40));
        llama_sampler_chain_add(chain.get(), llama_sampler_init_top_p(0.9f, 1));
        llama_sampler_chain_add(chain.get(), llama_sampler_init_temp(0.75f));
#endif
#ifdef GESCHICHTEN_CACHE_PROBE
        llama_sampler_chain_add(chain.get(), llama_sampler_init_dist(s->probe_seed));
#else
        llama_sampler_chain_add(chain.get(), llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
#endif
        auto callback_class = env->GetObjectClass(callback);
        auto method = env->GetMethodID(callback_class, "onChunk", "([B)V");
        env->DeleteLocalRef(callback_class);
        if (!method) return nullptr;
        std::string answer, pending;
        std::vector<char> piece(256);
        s->last_decode_size = 0;
        s->last_completed = false;
        for (int i = 0; i < max_tokens; ++i) {
            if (s->cancelled.load()) throw std::runtime_error("Antwort angehalten.");
            auto token = llama_sampler_sample(chain.get(), s->context, -1);
            if (llama_vocab_is_eog(vocab, token)) { s->last_completed = true; break; }
            ++s->last_decode_size;
            int len = llama_token_to_piece(vocab, token, piece.data(), piece.size(), 0, false);
            if (len < 0) { piece.resize(-len); len = llama_token_to_piece(vocab, token, piece.data(), piece.size(), 0, false); }
            if (len < 0) throw std::runtime_error("Die KI-Antwort konnte nicht gelesen werden.");
            pending.append(piece.data(), len);
            auto valid = complete_utf8(pending);
            if (valid) {
                auto chunk = pending.substr(0, valid);
                answer += chunk; pending.erase(0, valid);
                auto data = byte_array(env, chunk);
                if (!data) return nullptr;
                env->CallVoidMethod(callback, method, data); env->DeleteLocalRef(data);
                if (env->ExceptionCheck()) return nullptr;
            }
            if (i + 1 < max_tokens) {
                auto batch = llama_batch_get_one(&token, 1);
                if (llama_decode(s->context, batch) != 0) throw std::runtime_error(s->cancelled.load() ? "Antwort angehalten." : "Die KI konnte die Antwort noch nicht fortsetzen.");
                s->cached_tokens.push_back(token);
            }
        }
        if (!s->last_completed && !allow_token_limit)
            throw std::runtime_error("Die Antwort hat das Ausgabelimit erreicht und wurde nicht vollständig gespeichert. Deine Nachricht bleibt für einen neuen Versuch erhalten.");
        if (s->last_completed && !pending.empty())
            throw std::runtime_error("Die KI hat unvollständigen Text erzeugt. Bitte erneut versuchen.");
        auto result = byte_array(env, answer);
        cache_guard.committed = result != nullptr && !env->ExceptionCheck();
        return result;
    } catch (const std::exception & error) { fail(env, error.what()); return nullptr; }
}

extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_clearContext(JNIEnv *, jclass, jlong handle) {
    auto s = session(handle);
    s->cached_tokens.clear();
    llama_memory_clear(llama_get_memory(s->context), true);
}

extern "C" JNIEXPORT jlongArray JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_cacheStats(JNIEnv * env, jclass, jlong handle) {
    auto s = session(handle);
    jlong values[] = {static_cast<jlong>(s->last_prompt_size), static_cast<jlong>(s->last_reused),
        static_cast<jlong>(s->last_decode_size), static_cast<jlong>(s->last_completed)};
    auto result = env->NewLongArray(4);
    if (result) env->SetLongArrayRegion(result, 0, 4, values);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_resetCancellation(JNIEnv *, jclass, jlong handle) { session(handle)->cancelled.store(false); }
extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_cancel(JNIEnv *, jclass, jlong handle) { session(handle)->cancelled.store(true); }
extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_destroy(JNIEnv *, jclass, jlong handle) { delete session(handle); }

#ifdef GESCHICHTEN_CACHE_PROBE
// These inspection entry points are absent from Android/APK builds. Synthetic host tests only.
extern "C" JNIEXPORT jbyteArray JNICALL
Java_dev_vincent_geschichten_ai_ContextNativeProbe_formattedPrompt(JNIEnv * env, jclass, jlong handle) {
    return byte_array(env, session(handle)->probe_formatted);
}
extern "C" JNIEXPORT jint JNICALL
Java_dev_vincent_geschichten_ai_ContextNativeProbe_countTokens(JNIEnv * env, jclass, jlong handle, jbyteArray text) {
    try {
        const auto input = bytes(env, text);
        const auto n = llama_tokenize(llama_model_get_vocab(session(handle)->model), input.data(), input.size(), nullptr, 0, false, true);
        if (n == INT_MIN) throw std::runtime_error("Tokenizer failed");
        return n < 0 ? -n : n;
    } catch (const std::exception & error) { fail(env, error.what()); return 0; }
}
extern "C" JNIEXPORT jintArray JNICALL
Java_dev_vincent_geschichten_ai_ContextNativeProbe_promptTokens(JNIEnv * env, jclass, jlong handle) {
    const auto & tokens = session(handle)->probe_tokens;
    auto result = env->NewIntArray(tokens.size());
    if (result) env->SetIntArrayRegion(result, 0, tokens.size(), tokens.data());
    return result;
}
extern "C" JNIEXPORT jfloatArray JNICALL
Java_dev_vincent_geschichten_ai_PerformanceNativeProbe_promptLogits(JNIEnv * env, jclass, jlong handle) {
    const auto & values = session(handle)->probe_logits;
    auto result = env->NewFloatArray(values.size());
    if (result) env->SetFloatArrayRegion(result, 0, values.size(), values.data());
    return result;
}
extern "C" JNIEXPORT jfloatArray JNICALL
Java_dev_vincent_geschichten_ai_ContextNativeProbe_promptLogits(JNIEnv * env, jclass type, jlong handle) {
    return Java_dev_vincent_geschichten_ai_PerformanceNativeProbe_promptLogits(env, type, handle);
}
#endif

extern "C" JNIEXPORT jint JNICALL
Java_dev_vincent_geschichten_ai_GgufNative_countPrompt(JNIEnv *env,jclass,jlong handle,jobjectArray roles,jobjectArray texts) {
    try { return prompt_tokens(env,session(handle),roles,texts).size(); }
    catch(const std::exception &e) {fail(env,e.what());return 0;}
}

#ifdef GESCHICHTEN_CACHE_PROBE
extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_MemoryModelProbe_setSeed(JNIEnv *,jclass,jlong handle,jint seed) { session(handle)->probe_seed=seed; }
extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_QualityProbe_setSampling(JNIEnv *,jclass,jlong handle,jint top_k,jfloat top_p,jfloat temperature) {
    auto s=session(handle);s->probe_top_k=top_k;s->probe_top_p=top_p;s->probe_temperature=temperature;
}
#endif
