package dev.vincent.geschichten.memory

/** Frozen development settings; similarity ranks sources, never certifies their truth. */
object HybridRecall {
    const val THRESHOLD = 0.30f
    const val DIMENSION = 768
    const val PREPROCESSING = "embeddinggemma300m-none-title-search-result-v1"
    const val INDEX_VERSION = 1
    data class Hit(val excerpt: ArchiveExcerpt, val similarity: Float)
    fun key(e: ArchiveExcerpt) = "${e.message.id}:${e.start}:${e.end}"
    fun merge(lexical: List<ArchiveExcerpt>, semantic: List<Hit>, limit: Int = 12): List<ArchiveExcerpt> {
        val scores=linkedMapOf<String,Double>();val entries=linkedMapOf<String,ArchiveExcerpt>()
        val ranked=semantic.filter {it.similarity.isFinite() && it.similarity>=THRESHOLD}.sortedByDescending {it.similarity}.take(32).map {it.excerpt}
        for(list in listOf(lexical,ranked))list.take(32).forEachIndexed {i,e ->
            val key=key(e);scores[key]=(scores[key] ?: 0.0)+1.0/(61+i);entries[key]=e
        }
        return scores.entries.sortedByDescending {it.value}.take(limit.coerceIn(1,24)).map {entries.getValue(it.key)}
    }
    fun cosine(a: FloatArray,b: FloatArray): Float {
        require(a.size==DIMENSION && b.size==DIMENSION)
        var dot=0.0;var na=0.0;var nb=0.0
        for(i in a.indices){if(!a[i].isFinite() || !b[i].isFinite())return Float.NaN;dot+=a[i].toDouble()*b[i];na+=a[i].toDouble()*a[i];nb+=b[i].toDouble()*b[i]}
        return if(na<=0 || nb<=0) Float.NaN else (dot/kotlin.math.sqrt(na*nb)).toFloat()
    }
}
