package dev.vincent.geschichten.memory

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dev.vincent.geschichten.data.*

/** Uses the SAME SQLite connection and caller transaction as the message archive. */
object MemoryDatabase {
    fun create(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE story_memory (story_id TEXT PRIMARY KEY REFERENCES stories(id) ON DELETE CASCADE, version INTEGER NOT NULL DEFAULT 0, overview TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE memory_entities (id TEXT PRIMARY KEY, story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE, kind TEXT NOT NULL, name TEXT NOT NULL)")
        db.execSQL("""CREATE TABLE state_facts (id TEXT PRIMARY KEY, story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
            entity_id TEXT NOT NULL REFERENCES memory_entities(id), field TEXT NOT NULL, value TEXT NOT NULL,
            status TEXT NOT NULL, pinned INTEGER NOT NULL DEFAULT 0, source_id TEXT REFERENCES messages(id) ON DELETE SET NULL,
            source_text TEXT NOT NULL, version INTEGER NOT NULL, manual INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL, source_order INTEGER NOT NULL DEFAULT 0)""")
        db.execSQL("CREATE INDEX state_story_slot ON state_facts(story_id, entity_id, field, version)")
        db.execSQL("CREATE UNIQUE INDEX state_one_current ON state_facts(story_id,entity_id,field) WHERE status = 'CURRENT'")
        db.execSQL("CREATE TABLE fact_knowledge (fact_id TEXT NOT NULL REFERENCES state_facts(id) ON DELETE CASCADE, knower TEXT NOT NULL, source_id TEXT REFERENCES messages(id) ON DELETE SET NULL, version INTEGER NOT NULL, PRIMARY KEY(fact_id,knower))")
        db.execSQL("CREATE TABLE memory_sources (story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE, source_id TEXT PRIMARY KEY REFERENCES messages(id) ON DELETE CASCADE, processed_chars INTEGER NOT NULL, version INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE memory_exclusions (story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE, event_id TEXT PRIMARY KEY, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE memory_commits (story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE, request_id TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES messages(id) ON DELETE CASCADE, response_id TEXT NOT NULL REFERENCES messages(id) ON DELETE CASCADE, version INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE memory_note_links (note_id TEXT NOT NULL REFERENCES memories(id) ON DELETE CASCADE, fact_id TEXT NOT NULL REFERENCES state_facts(id) ON DELETE CASCADE, PRIMARY KEY(note_id,fact_id))")
        db.execSQL("CREATE TABLE memory_character_aliases (story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE, alias TEXT NOT NULL, PRIMARY KEY(story_id,alias))")
        db.execSQL("INSERT INTO story_memory(story_id) SELECT id FROM stories")
        db.rawQuery("SELECT s.id,c.id,c.name FROM stories s JOIN characters c ON c.id=s.character_id",null).use {c ->
            while(c.moveToNext()) registerCharacter(db,c.getString(0),c.getString(1),c.getString(2))
        }
        // Legacy summaries remain in their original columns, never promoted to current facts.
        db.query("memories", null, null, null, null, null, "created_at ASC,rowid ASC").use { c ->
            while (c.moveToNext()) {
                val story = c.s("story_id"); val id = c.s("id"); val text = c.s("text")
                if (id.startsWith("auto-summary-") && c.i("pinned") == 0) continue
                val kind = MemoryKind.valueOf(c.s("kind"))
                val frozen=db.rawQuery("SELECT start_context FROM stories WHERE id=?",arrayOf(story)).use {if(it.moveToFirst())it.getString(0) else ""}
                val authored = (text.startsWith("Startvorgabe:") || frozen.isNotBlank() && frozen==text) && c.l("updated_at") == c.l("created_at")
                val entity = MemoryRules.identity(story, if (kind == MemoryKind.GOAL) EntityKind.GOAL else EntityKind.EVENT, text.removePrefix("Startvorgabe:").trim())
                val fact=StateFact(MemoryRules.digest("legacy:$id"), story, entity, "note", text,
                    if (authored) FactStatus.UNCERTAIN else FactStatus.CURRENT, c.getInt(c.getColumnIndexOrThrow("pinned")) != 0,
                    sourceText = "Übernommene Notiz: $text", manual = !authored, createdAt = c.l("updated_at"))
                apply(db,fact,null)
                linkNote(db,id,fact.id)
            }
        }
    }

    fun ensure(db: SQLiteDatabase, story: String) = db.execSQL("INSERT OR IGNORE INTO story_memory(story_id) VALUES (?)", arrayOf(story))
    fun registerCharacter(db: SQLiteDatabase, story: String, characterId: String, name: String) {
        val id=MemoryRules.digest("$story:character:$characterId")
        db.insertWithOnConflict("memory_entities",null,ContentValues().apply {put("id",id);put("story_id",story);put("kind",EntityKind.PERSON.name);put("name",name)},SQLiteDatabase.CONFLICT_IGNORE)
        db.execSQL("UPDATE memory_entities SET name=? WHERE id=?",arrayOf(name,id))
        db.execSQL("UPDATE memory_entities SET name=? WHERE id=?",arrayOf("Du und $name",MemoryRules.digest("$story:relationship:$characterId:player")))
        db.insertWithOnConflict("memory_character_aliases",null,ContentValues().apply {put("story_id",story);put("alias",name.lowercase(java.util.Locale.GERMAN))},SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun version(db: SQLiteDatabase, story: String): Long = db.rawQuery("SELECT version FROM story_memory WHERE story_id=?", arrayOf(story)).use { if (it.moveToFirst()) it.getLong(0) else 0 }
    fun bump(db: SQLiteDatabase, story: String): Long { ensure(db, story); db.execSQL("UPDATE story_memory SET version=version+1 WHERE story_id=?", arrayOf(story)); return version(db, story) }
    fun snapshot(db: SQLiteDatabase, story: String): MemorySnapshot {
        val facts = mutableListOf<StateFact>(); val knowledge = mutableListOf<Knowledge>()
        db.rawQuery("SELECT f.*, e.kind, e.name FROM state_facts f JOIN memory_entities e ON e.id=f.entity_id WHERE f.story_id=? ORDER BY f.version ASC,f.created_at ASC,f.rowid ASC", arrayOf(story)).use { c ->
            while (c.moveToNext()) facts += StateFact(c.s("id"), story, MemoryEntity(c.s("entity_id"), story, EntityKind.valueOf(c.s("kind")), c.s("name")),
                c.s("field"), c.s("value"), FactStatus.valueOf(c.s("status")), c.i("pinned") != 0, c.nullS("source_id"), c.s("source_text"), c.l("version"),
                manual = c.i("manual") != 0, createdAt = c.l("created_at"),sourceOrder=c.l("source_order"))
        }
        db.rawQuery("SELECT k.* FROM fact_knowledge k JOIN state_facts f ON f.id=k.fact_id WHERE f.story_id=?", arrayOf(story)).use { c ->
            while (c.moveToNext()) knowledge += Knowledge(c.s("fact_id"), c.s("knower"), c.nullS("source_id"), c.l("version"))
        }
        fun strings(sql: String) = db.rawQuery(sql, arrayOf(story)).use { c -> buildSet { while(c.moveToNext()) add(c.getString(0)) } }
        val sources = strings("SELECT source_id FROM memory_sources WHERE story_id=?")
        val pending = db.rawQuery("SELECT COUNT(*) FROM messages WHERE story_id=? AND id NOT IN (SELECT source_id FROM memory_sources WHERE story_id=?)", arrayOf(story,story)).use { it.moveToFirst(); it.getInt(0) }
        val overview = db.rawQuery("SELECT overview FROM story_memory WHERE story_id=?", arrayOf(story)).use { if(it.moveToFirst()) it.getString(0) else "" }
        return MemorySnapshot(version(db, story), facts, knowledge, sources, strings("SELECT event_id FROM memory_exclusions WHERE story_id=?"), pending, overview, strings("SELECT alias FROM memory_character_aliases WHERE story_id=?"))
    }

    fun apply(db: SQLiteDatabase, proposed: StateFact, source: ChatMessage?): Boolean {
        require(source == null || source.id == proposed.sourceId && source.storyId == proposed.storyId && source.text.contains(proposed.sourceText)) { "Die Änderung passt nicht zur Quelle." }
        if (db.rawQuery("SELECT 1 FROM memory_exclusions WHERE event_id=?", arrayOf(proposed.id)).use { it.moveToFirst() }) return false
        if (db.rawQuery("SELECT 1 FROM state_facts WHERE id=?", arrayOf(proposed.id)).use { it.moveToFirst() }) return false
        val current = snapshot(db, proposed.storyId).current.firstOrNull { it.slot == proposed.slot }
        // Late backfill cannot restore a pre-correction source, nor reopen a completed goal.
        val sourceOrder=if(source==null) {
            if(proposed.manual) db.rawQuery("SELECT COALESCE(MAX(rowid),0) FROM messages WHERE story_id=?",arrayOf(proposed.storyId)).use {it.moveToFirst();it.getLong(0)} else 0
        } else db.rawQuery("SELECT rowid FROM messages WHERE id=? AND story_id=?",arrayOf(source.id,source.storyId)).use {check(it.moveToFirst()) {"Die Quelle fehlt im Archiv."};it.getLong(0)}
        var fact = proposed.copy(sourceOrder=sourceOrder)
        if (source != null && current?.manual == true && (if(current.sourceOrder>0) sourceOrder<=current.sourceOrder else source.createdAt<=current.createdAt)) fact = fact.copy(status = FactStatus.UNCERTAIN)
        else if(source != null && current != null && (source.createdAt < current.createdAt || source.createdAt == current.createdAt && sourceOrder < current.sourceOrder) && fact.status == FactStatus.CURRENT) fact=fact.copy(status=FactStatus.HISTORICAL)
        if (fact.field == "status" && current?.value == "Abgeschlossen" && fact.value == "Offen" && !Regex("(?i)wieder|erneut|aufgenommen").containsMatchIn(fact.sourceText) && !fact.manual) fact = fact.copy(status = FactStatus.UNCERTAIN)
        bump(db,fact.storyId)
        db.insertWithOnConflict("memory_entities", null, ContentValues().apply { put("id",fact.entity.id);put("story_id",fact.storyId);put("kind",fact.entity.kind.name);put("name",fact.entity.name) }, SQLiteDatabase.CONFLICT_IGNORE)
        if (fact.status == FactStatus.CURRENT) db.execSQL("UPDATE state_facts SET status='HISTORICAL' WHERE story_id=? AND entity_id=? AND field=? AND status='CURRENT'", arrayOf(fact.storyId,fact.entity.id,fact.field))
        db.insertOrThrow("state_facts", null, ContentValues().apply {
            put("id",fact.id);put("story_id",fact.storyId);put("entity_id",fact.entity.id);put("field",fact.field);put("value",fact.value)
            put("status",fact.status.name);put("pinned",if(if(fact.manual) fact.pinned else current?.pinned == true || fact.pinned) 1 else 0)
            put("source_id",fact.sourceId);put("source_text",fact.sourceText);put("version",version(db,fact.storyId));put("manual",if(fact.manual) 1 else 0);put("created_at",fact.createdAt);put("source_order",fact.sourceOrder)
        })
        if(fact.status in setOf(FactStatus.CURRENT,FactStatus.HISTORICAL)) fact.knownBy.forEach { learn(db,fact.id,it,fact.sourceId,version(db,fact.storyId)) }
        return true
    }
    fun learn(db: SQLiteDatabase, fact: String, knower: String, source: String?, version: Long) {
        db.insertWithOnConflict("fact_knowledge",null,ContentValues().apply {put("fact_id",fact);put("knower",knower);put("source_id",source);put("version",version)},SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun process(db: SQLiteDatabase, bundle: StoryBundle, source: ChatMessage) {
        if(source.id in snapshot(db,bundle.story.id).scannedSources) return
        val current = bundle.copy(memory=snapshot(db,bundle.story.id))
        val authored=source.id == bundle.messages.firstOrNull()?.id && source.role == ChatRole.CHARACTER && source.createdAt == bundle.story.createdAt && source.text == bundle.character.openingMessage
        MemoryRules.proposals(current,source,authored=authored).forEach { p ->
            if(p.reveal) learn(db,p.fact.id,"character",source.id,version(db,bundle.story.id)) else apply(db,p.fact,source)
        }
        db.insertOrThrow("memory_sources",null,ContentValues().apply { put("story_id",source.storyId);put("source_id",source.id);put("processed_chars",source.text.length);put("version",version(db,source.storyId)) })
        FactRepair.mark(db,source.storyId,source.id)
    }
    fun overview(db: SQLiteDatabase, story: String) {
        // Overview is derived from full, source-bound facts; no extra model latency and
        // no blind summary checkpoint. Current slots are always retrieved separately.
        val snapshot=snapshot(db,story)
        val text=snapshot.forCharacter().filter { it.status == FactStatus.CURRENT && (it.entity.kind == EntityKind.RELATIONSHIP || it.entity.kind == EntityKind.EVENT) }
            .takeLast(8).joinToString("\n") { "${it.entity.name}: ${it.value}" }
        db.execSQL("UPDATE story_memory SET overview=? WHERE story_id=?",arrayOf(text,story))
    }
    fun linkNote(db: SQLiteDatabase, note: String, fact: String) {
        db.insertWithOnConflict("memory_note_links",null,ContentValues().apply {put("note_id",note);put("fact_id",fact)},SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun removeNoteFacts(db: SQLiteDatabase, note: String, exclude: Boolean) {
        val linked="SELECT fact_id FROM memory_note_links WHERE note_id=?"
        // Excluding a note also excludes earlier versions of its slots. They cannot
        // become the character's last-known fact or be replayed from the same source.
        val affected=if(exclude) "SELECT f.id FROM state_facts f JOIN state_facts n ON f.story_id=n.story_id AND f.entity_id=n.entity_id AND f.field=n.field WHERE n.id IN ($linked)" else linked
        if(exclude) db.execSQL("INSERT OR IGNORE INTO memory_exclusions(story_id,event_id,created_at) SELECT story_id,id,? FROM state_facts WHERE id IN ($affected)",arrayOf<Any>(System.currentTimeMillis(),note))
        db.execSQL("UPDATE state_facts SET status=? WHERE id IN ($affected) AND status != 'EXCLUDED'",arrayOf(if(exclude) "EXCLUDED" else "HISTORICAL",note))
    }
    fun manualNote(db: SQLiteDatabase, bundle: StoryBundle, note: MemoryEntry) {
        removeNoteFacts(db,note.id,false)
        val source=ChatMessage(id="manual-${note.id}-${note.updatedAt}",storyId=note.storyId,role=ChatRole.USER,text=note.text,createdAt=note.updatedAt)
        val proposed=MemoryRules.proposals(bundle,source)
        val facts=proposed.filterNot {it.reveal}.map {it.fact.copy(sourceId=null,manual=true,pinned=note.pinned,sourceText="Eigene Notiz: ${note.text}")}
            .ifEmpty {listOf(StateFact(java.util.UUID.randomUUID().toString(),note.storyId,
                MemoryEntity(MemoryRules.digest("${note.storyId}:note:${note.id}"),note.storyId,EntityKind.EVENT,note.kind.title),"note",note.text,pinned=note.pinned,manual=true,sourceText="Eigene Notiz: ${note.text}",createdAt=note.updatedAt))}
        facts.forEach {if(apply(db,it,null)) linkNote(db,note.id,it.id)}
        overview(db,note.storyId)
    }
    private fun Cursor.s(n:String)=getString(getColumnIndexOrThrow(n))
    private fun Cursor.nullS(n:String)=if(isNull(getColumnIndexOrThrow(n)))null else s(n)
    private fun Cursor.i(n:String)=getInt(getColumnIndexOrThrow(n))
    private fun Cursor.l(n:String)=getLong(getColumnIndexOrThrow(n))
}
