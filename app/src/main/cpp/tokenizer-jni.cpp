#include <jni.h>
#include <memory>
#include <vector>
#include <string>
#include "llama.h"
#include "sentencepiece_processor.h"

namespace {
struct Tokenizer {
    std::unique_ptr<sentencepiece::SentencePieceProcessor> sp;
    llama_model * vocab_model = nullptr;
    ~Tokenizer() { if(vocab_model) llama_model_free(vocab_model); }
};
std::string raw(JNIEnv *env,jbyteArray bytes) {
    std::string s(env->GetArrayLength(bytes),'\0');
    env->GetByteArrayRegion(bytes,0,s.size(),reinterpret_cast<jbyte *>(s.data())); return s;
}
void error(JNIEnv *env,const char *what) { env->ThrowNew(env->FindClass("java/io/IOException"),what); }
}
extern "C" JNIEXPORT jlong JNICALL
Java_dev_vincent_geschichten_ai_ExactTokenizerNative_create(JNIEnv *env,jclass,jbyteArray path,jboolean sentence_piece) {
    auto t=std::make_unique<Tokenizer>(); auto file=raw(env,path);
    if(sentence_piece) {
        t->sp=std::make_unique<sentencepiece::SentencePieceProcessor>();
        if(!t->sp->Load(file).ok()) { error(env,"Der Tokenizer konnte nicht geladen werden.");return 0; }
    } else {
        auto params=llama_model_default_params();params.vocab_only=true;
        t->vocab_model=llama_model_load_from_file(file.c_str(),params);
        if(!t->vocab_model) { error(env,"Der Tokenizer konnte nicht geladen werden.");return 0; }
    }
    return reinterpret_cast<jlong>(t.release());
}
extern "C" JNIEXPORT jintArray JNICALL
Java_dev_vincent_geschichten_ai_ExactTokenizerNative_encode(JNIEnv *env,jclass,jlong handle,jbyteArray data) {
    if(!handle) {error(env,"Der Tokenizer fehlt.");return nullptr;}
    auto t=reinterpret_cast<Tokenizer *>(handle);auto text=raw(env,data);std::vector<int> ids;
    if(t->sp) {
        if(!t->sp->Encode(text,&ids).ok()) {error(env,"Die Eingabe konnte nicht tokenisiert werden.");return nullptr;}
    } else {
        auto vocab=llama_model_get_vocab(t->vocab_model);
        auto count=llama_tokenize(vocab,text.data(),text.size(),nullptr,0,false,true);
        ids.resize(-count);
        count=llama_tokenize(vocab,text.data(),text.size(),ids.data(),ids.size(),false,true);
        if(count<0) {error(env,"Die Eingabe konnte nicht tokenisiert werden.");return nullptr;}
        ids.resize(count);
    }
    auto result=env->NewIntArray(ids.size());env->SetIntArrayRegion(result,0,ids.size(),ids.data());return result;
}
extern "C" JNIEXPORT void JNICALL
Java_dev_vincent_geschichten_ai_ExactTokenizerNative_destroy(JNIEnv *,jclass,jlong handle) { delete reinterpret_cast<Tokenizer *>(handle); }
