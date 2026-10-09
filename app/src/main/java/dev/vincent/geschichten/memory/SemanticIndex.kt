package dev.vincent.geschichten.memory

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dev.vincent.geschichten.ai.EmbeddingModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Disposable, no-backup SQLite index. The story DB and its version remain unchanged. */
internal class SemanticIndex(context: Context) : SQLiteOpenHelper(context,java.io.File(context.noBackupFilesDir,"semantic-index.db").absolutePath,null,1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE sections(story TEXT NOT NULL,source TEXT NOT NULL,start INTEGER NOT NULL,end INTEGER NOT NULL,content_hash TEXT NOT NULL,source_revision TEXT NOT NULL,space TEXT NOT NULL,model TEXT NOT NULL,preprocessing TEXT NOT NULL,dimension INTEGER NOT NULL,index_version INTEGER NOT NULL,state_version INTEGER NOT NULL,status TEXT NOT NULL,vector BLOB,PRIMARY KEY(story,source,start,end))")
        db.execSQL("CREATE INDEX sections_story_space ON sections(story,space,status)")
    }
    override fun onUpgrade(db: SQLiteDatabase,oldVersion: Int,newVersion: Int) {error("Unbekannte Suchindex-Version; Originalgeschichten werden nicht verändert.")}
    @Synchronized fun ready(e: ArchiveExcerpt): Boolean = readableDatabase.rawQuery("SELECT content_hash,source_revision,dimension,status,space,index_version,preprocessing,model FROM sections WHERE story=? AND source=? AND start=? AND end=?",arrayOf(e.message.storyId,e.message.id,e.start.toString(),e.end.toString())).use {c ->
        c.moveToFirst() && c.getString(0)==hash(e.text) && c.getString(1)==revision(e) && c.getInt(2)==HybridRecall.DIMENSION && c.getString(3)=="READY" && c.getString(4)==EmbeddingModel.SPACE && c.getInt(5)==HybridRecall.INDEX_VERSION && c.getString(6)==HybridRecall.PREPROCESSING && c.getString(7)==EmbeddingModel.ID
    }
    @Synchronized fun put(e: ArchiveExcerpt,version: Long,vector: FloatArray?,status: String) {
        require(status in setOf("READY","PENDING","FAILED") && ((status=="READY")== (vector!=null)))
        if(vector!=null)require(vector.size==HybridRecall.DIMENSION && vector.all {it.isFinite()} && vector.any {it!=0f})
        val v=ContentValues().apply {
            put("story",e.message.storyId);put("source",e.message.id);put("start",e.start);put("end",e.end);put("content_hash",hash(e.text));put("source_revision",revision(e));put("space",EmbeddingModel.SPACE);put("model",EmbeddingModel.ID);put("preprocessing",HybridRecall.PREPROCESSING);put("dimension",HybridRecall.DIMENSION);put("index_version",HybridRecall.INDEX_VERSION);put("state_version",version);put("status",status)
            if(vector==null)putNull("vector") else put("vector",ByteBuffer.allocate(vector.size*4).order(ByteOrder.LITTLE_ENDIAN).apply {vector.forEach {putFloat(it)}}.array())
        }
        check(writableDatabase.insertWithOnConflict("sections",null,v,SQLiteDatabase.CONFLICT_REPLACE)!=-1L)
    }
    /** Pre-filtered live candidates, and full revision/hash validation, prevent stale hits. */
    @Synchronized fun search(story: String,allowed: List<ArchiveExcerpt>,query: FloatArray): List<HybridRecall.Hit> {
        val sources=allowed.associateBy {HybridRecall.key(it)};val hits=mutableListOf<HybridRecall.Hit>()
        readableDatabase.rawQuery("SELECT source,start,end,content_hash,source_revision,dimension,index_version,vector FROM sections WHERE story=? AND space=? AND model=? AND preprocessing=? AND status='READY'",arrayOf(story,EmbeddingModel.SPACE,EmbeddingModel.ID,HybridRecall.PREPROCESSING)).use {c ->
            while(c.moveToNext()) {
                val e=sources["${c.getString(0)}:${c.getInt(1)}:${c.getInt(2)}"] ?: continue
                if(c.getString(3)!=hash(e.text) || c.getString(4)!=revision(e) || c.getInt(5)!=HybridRecall.DIMENSION || c.getInt(6)!=HybridRecall.INDEX_VERSION)continue
                val bytes=c.getBlob(7) ?: continue;if(bytes.size!=HybridRecall.DIMENSION*4)continue
                val buffer=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);val v=FloatArray(HybridRecall.DIMENSION){buffer.float}
                val score=HybridRecall.cosine(query,v)
                if(score.isFinite() && score>=HybridRecall.THRESHOLD) {hits+=HybridRecall.Hit(e,score);if(hits.size>64){hits.sortByDescending {it.similarity};while(hits.size>32)hits.removeAt(hits.lastIndex)}}
            }
        }
        return hits.sortedByDescending {it.similarity}.take(32)
    }
    @Synchronized fun clear(story: String) {writableDatabase.delete("sections","story=?",arrayOf(story))}
    companion object {
        fun hash(s: String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
        fun revision(e: ArchiveExcerpt)="${e.message.createdAt}:${hash(e.message.text)}"
    }
}
