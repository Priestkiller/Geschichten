#include <jni.h>
#include "llama.h"
#include <atomic>
#include <algorithm>
#include <cmath>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

namespace {
struct EmbeddingSession {
    llama_model *model = nullptr;
    llama_context *context = nullptr;
    std::atomic<bool> cancelled{false};
    ~EmbeddingSession() { if(context) llama_free(context); if(model) llama_model_free(model); }
};
bool abort_embedding(void *p) { return static_cast<EmbeddingSession *>(p)->cancelled.load(); }
std::string input_bytes(JNIEnv *env, jbyteArray input) {
    std::string s(env->GetArrayLength(input), '\0');
    env->GetByteArrayRegion(input,0,s.size(),reinterpret_cast<jbyte *>(s.data())); return s;
}
void embedding_fail(JNIEnv *env,const char *message) {
    if(!env->ExceptionCheck()) { auto type=env->FindClass("java/io/IOException");env->ThrowNew(type,message);env->DeleteLocalRef(type); }
}
EmbeddingSession *create_embedding(const char *path) {
    // Same pinned CPU backend as text generation; no GPU/cloud fallback.
    llama_backend_init();
    auto s=std::make_unique<EmbeddingSession>();
    auto mp=llama_model_default_params(); mp.n_gpu_layers=0;
    s->model=llama_model_load_from_file(path,mp);
    if(!s->model) throw std::runtime_error("Das Suchmodell konnte nicht geladen werden.");
    char architecture[64]{};
    llama_model_meta_val_str(s->model,"general.architecture",architecture,sizeof(architecture));
    if(std::string(architecture)!="gemma-embedding" || llama_model_n_embd_out(s->model)!=768)
        throw std::runtime_error("Unpassendes Suchmodell oder falsche Vektordimension.");
    auto cp=llama_context_default_params();
    cp.n_ctx=512;cp.n_batch=512;cp.n_ubatch=512;cp.n_seq_max=1;
    cp.n_threads=2;cp.n_threads_batch=2;cp.embeddings=true;
    cp.pooling_type=LLAMA_POOLING_TYPE_MEAN;cp.attention_type=LLAMA_ATTENTION_TYPE_NON_CAUSAL;
    cp.abort_callback=abort_embedding;cp.abort_callback_data=s.get();
    s->context=llama_init_from_model(s->model,cp);
    if(!s->context) throw std::runtime_error("Das Suchmodell benötigt mehr freien Arbeitsspeicher.");
    return s.release();
}
std::vector<float> embed(EmbeddingSession *s,const std::string &text) {
    if(!s) throw std::runtime_error("Das Suchmodell ist nicht geladen.");
    if(text.empty() || text.size()>16000) throw std::runtime_error("Suchabschnitt zu lang.");
    auto vocab=llama_model_get_vocab(s->model);
    // Add the model's BOS/EOS, but never interpret source text as special tokens.
    int n=llama_tokenize(vocab,text.data(),text.size(),nullptr,0,true,false);
    if(n>=0 || n<-512) throw std::runtime_error("Suchabschnitt überschreitet 512 Tokens.");
    std::vector<llama_token> tokens(-n);
    if(llama_tokenize(vocab,text.data(),text.size(),tokens.data(),tokens.size(),true,false)!=-n)
        throw std::runtime_error("Suchabschnitt konnte nicht tokenisiert werden.");
    if(s->cancelled.load()) throw std::runtime_error("Suche angehalten.");
    llama_memory_clear(llama_get_memory(s->context),true);
    auto batch=llama_batch_init(tokens.size(),0,1);
    for(size_t i=0;i<tokens.size();++i) {
        batch.token[i]=tokens[i];batch.pos[i]=i;batch.n_seq_id[i]=1;batch.seq_id[i][0]=0;batch.logits[i]=true;
    }
    batch.n_tokens=tokens.size();
    int result=llama_model_has_encoder(s->model) ? llama_encode(s->context,batch) : llama_decode(s->context,batch);llama_batch_free(batch);
    if(result!=0 || s->cancelled.load()) throw std::runtime_error("Suchauswertung angehalten oder fehlgeschlagen.");
    const float *v=llama_get_embeddings_seq(s->context,0);
    if(!v) throw std::runtime_error("Das Suchmodell hat keinen Vektor erzeugt.");
    std::vector<float> output(v,v+768);double norm=0;
    for(float f:output) { if(!std::isfinite(f)) throw std::runtime_error("Ungültiger Suchvektor.");norm+=double(f)*f; }
    if(norm<=1e-20) throw std::runtime_error("Leerer Suchvektor.");
    norm=std::sqrt(norm);for(auto &f:output) f/=norm;
    return output;
}
}
extern "C" JNIEXPORT jlong JNICALL Java_dev_vincent_geschichten_ai_EmbeddingNative_create(JNIEnv *env,jclass,jbyteArray path) {
    try{return reinterpret_cast<jlong>(create_embedding(input_bytes(env,path).c_str()));}
    catch(const std::exception &e){embedding_fail(env,e.what());return 0;}
}
extern "C" JNIEXPORT jfloatArray JNICALL Java_dev_vincent_geschichten_ai_EmbeddingNative_encode(JNIEnv *env,jclass,jlong h,jbyteArray input) {
    try {auto v=embed(reinterpret_cast<EmbeddingSession *>(h),input_bytes(env,input));auto r=env->NewFloatArray(v.size());env->SetFloatArrayRegion(r,0,v.size(),v.data());return r;}
    catch(const std::exception &e){embedding_fail(env,e.what());return nullptr;}
}
extern "C" JNIEXPORT void JNICALL Java_dev_vincent_geschichten_ai_EmbeddingNative_cancel(JNIEnv *,jclass,jlong h) {if(h)reinterpret_cast<EmbeddingSession *>(h)->cancelled=true;}
extern "C" JNIEXPORT void JNICALL Java_dev_vincent_geschichten_ai_EmbeddingNative_destroy(JNIEnv *,jclass,jlong h) {delete reinterpret_cast<EmbeddingSession *>(h);}
#ifdef GESCHICHTEN_CACHE_PROBE
extern "C" __declspec(dllexport) void *embedding_probe_create(const char *path) {try{return create_embedding(path);}catch(...){return nullptr;}}
extern "C" __declspec(dllexport) int embedding_probe_encode(void *s,const char *text,float *out) {try{auto v=embed(static_cast<EmbeddingSession *>(s),text);std::copy(v.begin(),v.end(),out);return v.size();}catch(...){return -1;}}
extern "C" __declspec(dllexport) void embedding_probe_destroy(void *s) {delete static_cast<EmbeddingSession *>(s);}
#endif
