package dev.vincent.geschichten.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dev.vincent.geschichten.memory.*

/**
 * Local-only persistence. Calls are synchronous and should run on Dispatchers.IO.
 * Methods are serialized so compound reads/writes cannot interleave within this repository.
 * Full conversations stay in SQLite; inference-window selection belongs to StoryPrompt.
 */
class StoryRepository(context: Context) : AutoCloseable {
    private val helper = Database(context.applicationContext)

    @Synchronized
    fun characters(): List<CharacterProfile> {
        val cursor = helper.readableDatabase.query(
            "characters", null, null, null, null, null, "sort_order ASC, id ASC",
        )
        return try {
            cursor.collect { readCharacter() }
        } finally {
            cursor.close()
        }
    }

    @Synchronized
    fun upsertCharacter(profile: CharacterProfile) {
        require(profile.id.isNotBlank()) { "Eine Figur benötigt eine ID." }
        require(profile.name.isNotBlank()) { "Gib deiner Figur einen Namen." }
        val normalized = profile.copy(name = profile.name.trim())
        helper.writableDatabase.inTransaction {
            val values = normalized.toValues()
            if (update("characters", values, "id = ?", arrayOf(profile.id)) == 0) {
                values.put("sort_order", nextCharacterOrder())
                insertOrThrow("characters", null, values)
            }
            rawQuery("SELECT id FROM stories WHERE character_id=?",arrayOf(profile.id)).use {c ->
                while(c.moveToNext()) MemoryDatabase.registerCharacter(this,c.getString(0),profile.id,normalized.name)
            }
            execSQL("UPDATE story_memory SET version=version+1 WHERE story_id IN (SELECT id FROM stories WHERE character_id=?)", arrayOf(profile.id))
        }
    }

    @Synchronized
    fun stories(): List<Story> {
        val cursor = helper.readableDatabase.query(
            "stories", null, null, null, null, null, "updated_at DESC, created_at DESC, id ASC",
        )
        return try {
            cursor.collect { readStory() }
        } finally {
            cursor.close()
        }
    }

    /** Deletes conversations and their dependent messages/notes in one transaction. */
    @Synchronized
    fun clearHistory(): Int = helper.writableDatabase.inTransaction {
        // Foreign keys cascade from stories; characters and model files are independent.
        delete("stories", null, null)
    }

    @Synchronized
    fun createStory(characterId: String): Story = helper.writableDatabase.inTransaction {
        val character = findCharacter(characterId)
            ?: throw IllegalArgumentException("Diese Figur wurde nicht gefunden.")
        val now = System.currentTimeMillis()
        val story = Story(
            characterId = character.id,
            title = character.storyTitle.ifBlank { "Eine Geschichte mit ${character.name}" },
            startContext = CharacterIntroductions.contextFor(character).ifBlank {character.scenario},
            createdAt = now,
            updatedAt = now,
        )
        insertOrThrow("stories", null, story.toValues())
        MemoryDatabase.ensure(this, story.id)
        MemoryDatabase.registerCharacter(this,story.id,character.id,character.name)
        // An authored opening belongs to the initial scenario. It is not a fabricated model reply.
        if (character.openingMessage.isNotBlank()) {
            val opening = ChatMessage(
                storyId = story.id,
                role = ChatRole.CHARACTER,
                text = character.openingMessage,
                createdAt = now,
            )
            insertOrThrow("messages", null, opening.toValues())
        }
        BuiltinCharacters.initialMemories(character, story.id, now).forEach { memory ->
            insertOrThrow("memories", null, memory.toValues())
            if(memory.kind == MemoryKind.LOCATION || memory.kind == MemoryKind.GOAL) {
                val name=memory.text.removePrefix("Startvorgabe:").trim()
                val entity=MemoryRules.identity(story.id,if(memory.kind == MemoryKind.GOAL) EntityKind.GOAL else EntityKind.SCENE,
                    if(memory.kind == MemoryKind.GOAL) name else "Ausgangsszene")
                MemoryDatabase.apply(this,StateFact(java.util.UUID.randomUUID().toString(),story.id,entity,
                    if(memory.kind == MemoryKind.GOAL) "status" else "location",if(memory.kind == MemoryKind.GOAL) "Offen" else name,
                    sourceText="Verfasste Ausgangsvorgabe: $name",createdAt=now),null)
            }
        }
        story
    }

    @Synchronized
    fun bundle(storyId: String): StoryBundle? = helper.readableDatabase.inTransaction {
        val story = findStory(storyId) ?: return@inTransaction null
        val character = findCharacter(story.characterId) ?: return@inTransaction null
        val messages = query(
            "messages", null, "story_id = ?", arrayOf(storyId), null, null,
            "created_at ASC, rowid ASC",
        ).use { cursor -> cursor.collect { readMessage() } }
        val memories = query(
            "memories", null, "story_id = ?", arrayOf(storyId), null, null,
            "created_at ASC, rowid ASC",
        ).use { cursor -> cursor.collect { readMemory() } }
        StoryBundle(story, character, messages, memories, MemoryDatabase.snapshot(this, storyId))
    }

    @Synchronized
    fun appendMessage(storyId: String, role: ChatRole, text: String): ChatMessage {
        require(text.isNotBlank()) { "Eine Nachricht darf nicht leer sein." }
        return helper.writableDatabase.inTransaction {
            requireStory(storyId)
            val latestTime = rawQuery(
                "SELECT MAX(created_at) FROM messages WHERE story_id = ?", arrayOf(storyId),
            ).use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else 0L
            }
            val message = ChatMessage(
                storyId = storyId,
                role = role,
                text = text.trim(),
                createdAt = maxOf(System.currentTimeMillis(), incrementSafely(latestTime)),
            )
            insertOrThrow("messages", null, message.toValues())
            MemoryDatabase.bump(this, storyId)
            touchStory(storyId, message.createdAt)
            message
        }
    }

    @Synchronized
    fun upsertMemory(memory: MemoryEntry) {
        require(memory.id.isNotBlank()) { "Eine Erinnerung benötigt eine ID." }
        require(memory.text.isNotBlank()) { "Eine Erinnerung darf nicht leer sein." }
        helper.writableDatabase.inTransaction {
            requireStory(memory.storyId)
            val existing = query(
                "memories", null, "id = ?", arrayOf(memory.id), null, null, null,
            ).use { cursor -> if (cursor.moveToFirst()) cursor.readMemory() else null }
            require(existing == null || existing.storyId == memory.storyId) {
                "Eine Erinnerung kann nicht in eine andere Geschichte verschoben werden."
            }
            val latestEdit = rawQuery(
                "SELECT MAX(updated_at) FROM memories WHERE story_id = ?", arrayOf(memory.storyId),
            ).use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else 0L
            }
            val values = memory.copy(
                text = memory.text.trim(),
                createdAt = existing?.createdAt ?: memory.createdAt,
                updatedAt = maxOf(System.currentTimeMillis(), incrementSafely(latestEdit)),
            ).toValues()
            if (update("memories", values, "id = ? AND story_id = ?", arrayOf(memory.id, memory.storyId)) == 0) {
                insertOrThrow("memories", null, values)
            }
            MemoryDatabase.bump(this, memory.storyId)
            val savedNote=memory.copy(updatedAt=values.getAsLong("updated_at"))
            MemoryDatabase.manualNote(this,bundle(memory.storyId)!!,savedNote)
            backfillMemory(memory.storyId)
            touchStory(memory.storyId)
        }
    }

    @Synchronized
    fun deleteMemory(id: String) {
        helper.writableDatabase.inTransaction {
            val storyId = query(
                "memories", arrayOf("story_id"), "id = ?", arrayOf(id), null, null, null,
            ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            if (storyId != null) {
                MemoryDatabase.removeNoteFacts(this,id,true)
                delete("memories", "id = ? AND story_id = ?", arrayOf(id, storyId))
                MemoryDatabase.bump(this, storyId)
                touchStory(storyId)
            }
        }
    }

    @Synchronized
    fun updateSummary(storyId: String, summary: String) {
        helper.writableDatabase.inTransaction {
            requireStory(storyId)
            update("stories", ContentValues().apply { put("summary", summary.trim()) }, "id = ?", arrayOf(storyId))
            touchStory(storyId)
        }
    }

    /** Used only to recover a failed/cancelled send; never removes an answered turn. */
    @Synchronized
    fun deleteLastUserMessageIfUnanswered(storyId: String) {
        helper.writableDatabase.inTransaction {
            val last = query(
                "messages", null, "story_id = ?", arrayOf(storyId), null, null,
                "created_at DESC, rowid DESC", "1",
            ).use { cursor -> if (cursor.moveToFirst()) cursor.readMessage() else null }
            if (last?.role == ChatRole.USER) {
                delete("messages", "id = ? AND story_id = ?", arrayOf(last.id, storyId))
                MemoryDatabase.bump(this, storyId)
                touchStory(storyId)
            }
        }
    }


    /** Bounded lazy scan, never a global replay at startup. Full sources only. */
    @Synchronized
    fun backfillMemory(storyId: String, maximum: Int = 4): Int = helper.writableDatabase.inTransaction {
        val initial = bundle(storyId) ?: return@inTransaction 0
        val repaired = FactRepair.run(this,initial,maximum)
        val saved = bundle(storyId)!!
        val complete = if(saved.messages.lastOrNull()?.role == ChatRole.USER) saved.messages.dropLast(1) else saved.messages
        val unprocessed=complete.filter { it.id !in saved.memory.scannedSources }
        val sources = (unprocessed.take(maximum.coerceIn(1, 8)) + unprocessed.takeLast(2)).distinctBy {it.id}.sortedBy {it.createdAt}
        if(sources.isNotEmpty()) MemoryDatabase.bump(this,storyId)
        sources.forEach { MemoryDatabase.process(this,saved,it) }
        MemoryDatabase.overview(this,storyId)
        sources.size + repaired
    }

    @Synchronized
    fun searchArchive(storyId: String, question: String): List<ArchiveExcerpt> = bundle(storyId)?.let {ArchiveRecall.search(it,question)}.orEmpty()

    /** A completed response and its facts/checkpoints share one short transaction.
     * Native inference runs before this method. Same request is idempotent. */
    @Synchronized
    fun commitReply(storyId: String, expectedVersion: Long, userId: String, requestId: String, reply: String, recalledSources: List<ArchiveExcerpt> = emptyList(), teamWork: TeamWork? = null, teamProposals: List<StateFact> = emptyList()): Boolean = helper.writableDatabase.inTransaction {
        if(rawQuery("SELECT 1 FROM memory_commits WHERE request_id=? AND story_id=?",arrayOf(requestId,storyId)).use { it.moveToFirst() }) return@inTransaction false
        check(MemoryDatabase.version(this,storyId) == expectedVersion) { "Die Geschichte wurde inzwischen geändert. Bitte sende deine Nachricht erneut." }
        val saved = bundle(storyId) ?: error("Die Geschichte fehlt.")
        val user = saved.messages.lastOrNull()?.takeIf { it.id == userId && it.role == ChatRole.USER } ?: error("Dieser Sendeversuch ist nicht mehr aktuell.")
        require(reply.isNotBlank())
        val verifiedSources=recalledSources.filter {e -> saved.messages.any {it==e.message} && e.start>=0 && e.end<=e.message.text.length && e.start<e.end}
        val provisional=saved.copy(memory=MemoryRules.preview(saved,user))
        MemoryReplyGuard.validate(provisional,reply,verifiedSources)
        if(teamWork!=null) {
            require(teamWork.request==requestId)
            TeamProtocol.validateProposals(provisional,teamWork,teamProposals)
            check(TeamProtocol.privateConflict(provisional,reply)==null){"Die Figurenantwort verwendet nicht freigegebenes Wissen."}
        } else require(teamProposals.isEmpty())
        val response = appendMessage(storyId,ChatRole.CHARACTER,reply)
        MemoryDatabase.process(this,saved,user)
        MemoryDatabase.process(this,saved,response)
        teamProposals.forEach {f ->MemoryDatabase.apply(this,f,saved.messages.single {it.id==f.sourceId})}
        MemoryDatabase.overview(this,storyId)
        insertOrThrow("memory_commits",null,ContentValues().apply {put("story_id",storyId);put("request_id",requestId);put("user_id",userId);put("response_id",response.id);put("version",MemoryDatabase.version(this@inTransaction,storyId))})
        true
    }

    @Synchronized
    fun editStateFact(storyId: String, factId: String, value: String, pinned: Boolean, exclude: Boolean = false, known: Boolean? = null) = helper.writableDatabase.inTransaction {
        val snapshot=MemoryDatabase.snapshot(this,storyId)
        val old=snapshot.facts.firstOrNull { it.id == factId } ?: error("Dieser Eintrag fehlt.")
        MemoryDatabase.bump(this,storyId)
        if(exclude) {
            // Tombstones for ALL earlier sources in this slot prevent replay resurrection.
            snapshot.facts.filter { it.slot == old.slot }.forEach { f ->
                insertWithOnConflict("memory_exclusions",null,ContentValues().apply {put("story_id",storyId);put("event_id",f.id);put("created_at",System.currentTimeMillis())},SQLiteDatabase.CONFLICT_IGNORE)
            }
            execSQL("UPDATE state_facts SET status='EXCLUDED' WHERE story_id=? AND entity_id=? AND field=?",arrayOf(storyId,old.entity.id,old.field))
        } else if(value.trim() == old.value && old.status == FactStatus.CURRENT) {
            execSQL("UPDATE state_facts SET pinned=? WHERE id=? AND story_id=?",arrayOf<Any>(if(pinned)1 else 0,factId,storyId))
            if(known == true) MemoryDatabase.learn(this,old.id,"character",null,MemoryDatabase.version(this,storyId))
            if(known == false) execSQL("DELETE FROM fact_knowledge WHERE fact_id=? AND knower='character'",arrayOf(old.id))
        } else {
            require(value.isNotBlank())
            MemoryDatabase.apply(this,old.copy(id=java.util.UUID.randomUUID().toString(),value=value.trim(),status=FactStatus.CURRENT,pinned=pinned,
                sourceId=null,sourceText="Manuelle Korrektur: ${old.value} → ${value.trim()}",manual=true,createdAt=System.currentTimeMillis(),knownBy=if(known ?: snapshot.knowledge.any {it.factId==old.id && it.knower=="character"}) setOf("player","character") else setOf("player")),null)
        }
        backfillMemory(storyId)
        MemoryDatabase.overview(this,storyId)
        touchStory(storyId)
    }

    @Synchronized
    fun addStateFact(storyId: String, kind: EntityKind, name: String, field: String, value: String, known: Boolean) = helper.writableDatabase.inTransaction {
        requireStory(storyId)
        require(name.isNotBlank() && value.isNotBlank())
        require(field in setOf("identity","color","holder","owner","placement","location","injury","status","promise","trust","conflict","event","question"))
        if(field == "status") require(value.trim() in setOf("Offen","Abgeschlossen")) { "Bitte den Zielstatus Offen oder Abgeschlossen wählen." }
        if(field == "injury") require(value.contains(':') && value.substringBefore(':').isNotBlank() && value.substringAfter(':').isNotBlank()) { "Bitte die Körperstelle angeben, zum Beispiel linke Hand: verletzt." }
        val slotField=if(field == "injury") "injury:${value.substringBefore(':').trim().lowercase(java.util.Locale.GERMAN)}" else field
        MemoryDatabase.apply(this,StateFact(java.util.UUID.randomUUID().toString(),storyId,MemoryRules.identity(storyId,kind,name),slotField,value.trim(),
            pinned=true,manual=true,sourceText="Eigene Festlegung: ${name.trim()} · ${value.trim()}",knownBy=if(known) setOf("player","character") else setOf("player")),null)
        backfillMemory(storyId)
        MemoryDatabase.overview(this,storyId)
        touchStory(storyId)
    }

    @Synchronized
    override fun close() = helper.close()

    private class Database(context: Context) : SQLiteOpenHelper(context, "geschichten.db", null, 10) {
        override fun onConfigure(db: SQLiteDatabase) {
            super.onConfigure(db)
            db.setForeignKeyConstraintsEnabled(true)
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE characters (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    genre TEXT NOT NULL,
                    traits TEXT NOT NULL,
                    personality TEXT NOT NULL,
                    scenario TEXT NOT NULL,
                    story_title TEXT NOT NULL,
                    opening_message TEXT NOT NULL,
                    avatar_key TEXT NOT NULL,
                    custom INTEGER NOT NULL CHECK (custom IN (0, 1)),
                    sort_order INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE stories (
                    id TEXT PRIMARY KEY NOT NULL,
                    character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE RESTRICT,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL DEFAULT '',
                    start_context TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE messages (
                    id TEXT PRIMARY KEY NOT NULL,
                    story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
                    role TEXT NOT NULL CHECK (role IN ('USER', 'CHARACTER')),
                    text TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE memories (
                    id TEXT PRIMARY KEY NOT NULL,
                    story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
                    kind TEXT NOT NULL CHECK (kind IN ('FACT', 'LOCATION', 'GOAL', 'EVENT')),
                    text TEXT NOT NULL,
                    pinned INTEGER NOT NULL CHECK (pinned IN (0, 1)),
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX messages_story_time ON messages(story_id, created_at)")
            db.execSQL("CREATE INDEX memories_story_time ON memories(story_id, created_at)")
            db.execSQL("CREATE INDEX stories_updated ON stories(updated_at)")
            db.execSQL("CREATE INDEX stories_character ON stories(character_id)")
            MemoryDatabase.create(db)
            FactRepair.create(db)
            insertMissingCatalogEntries(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            var migratedVersion = oldVersion
            if (migratedVersion == 1 && newVersion >= 2) {
                // v2 expands the authored catalog. SQLiteOpenHelper wraps this upgrade in a
                // transaction. Existing characters (including edits and ID collisions), stories,
                // messages, summaries, memories, IDs, timestamps, and their ordering stay intact.
                insertMissingCatalogEntries(db)
                migratedVersion = 2
            }
            if (migratedVersion == 2 && newVersion >= 3) {
                // v3 brings every existing category to ten figures. Insert only missing IDs;
                // retain the user's edits, ordering, and every saved story and memory.
                insertMissingCatalogEntries(db)
                migratedVersion = 3
            }
            if (migratedVersion == 3 && newVersion >= 4) {
                // Revise only an exact, unedited released default. Preserve user variants,
                // sort order and all archived story/message/memory text, even in active stories.
                reviseUneditedCatalogEntries(db)
                migratedVersion = 4
            }
            if (migratedVersion == 4 && newVersion >= 5) {
                // New worlds and monsters: append missing IDs only, including on older upgrade paths.
                // Preserve every installed profile, ID collision, archive row and existing sort order.
                insertMissingCatalogEntries(db)
                migratedVersion = 5
            }
            if (migratedVersion == 5 && newVersion >= 6) {
                // Replace openings only for exact delivered defaults. Edited figures and every
                // saved conversation, note, timestamp, ID and sort order remain intact.
                reviseUneditedIntroductions(db)
                migratedVersion = 6
            }
            if (migratedVersion == 6 && newVersion >= 7) {
                db.execSQL("ALTER TABLE stories ADD COLUMN start_context TEXT NOT NULL DEFAULT ''")
                reviseUneditedChatStarts(db)
                migratedVersion = 7
            }
            if (migratedVersion == 7 && newVersion >= 8) {
                reviseAmbiguousIntroductions(db)
                migratedVersion = 8
            }
            if (migratedVersion == 8 && newVersion >= 9) {
                MemoryDatabase.create(db)
                migratedVersion = 9
            }
            if (migratedVersion == 9 && newVersion >= 10) {
                FactRepair.create(db)
                migratedVersion = 10
            }
            check(migratedVersion == newVersion) {
                "Für diese Datenbankversion ist eine ausdrückliche Migration erforderlich ($oldVersion → $newVersion)."
            }
        }

        private fun insertMissingCatalogEntries(db: SQLiteDatabase) {
            var nextOrder = db.nextCharacterOrder()
            for (character in BuiltinCharacters.profiles) {
                val inserted = db.insertWithOnConflict(
                    "characters", null,
                    character.toValues().apply { put("sort_order", nextOrder) },
                    SQLiteDatabase.CONFLICT_IGNORE,
                )
                if (inserted != -1L) nextOrder++
            }
        }

        private fun reviseAmbiguousIntroductions(db: SQLiteDatabase) {
            for (previous in BuiltinCharacters.releasedChatProfiles) {
                val next = BuiltinCharacters.profiles.first { it.id == previous.id }
                if (next == previous || db.findCharacter(previous.id) != previous) continue
                db.update("characters", ContentValues().apply { put("opening_message", next.openingMessage) },
                    "id = ?", arrayOf(previous.id))
                // Correct only an untouched, solitary opening. Played conversations retain their archive.
                db.execSQL("""
                    UPDATE messages SET text = ? WHERE text = ? AND role = ? AND story_id IN (
                        SELECT s.id FROM stories s WHERE s.character_id = ? AND s.summary = ''
                        AND s.updated_at = s.created_at AND s.title = ?
                        AND (SELECT COUNT(*) FROM messages m WHERE m.story_id = s.id) = 1
                    ) AND created_at = (SELECT created_at FROM stories WHERE id = messages.story_id)
                """.trimIndent(), arrayOf(next.openingMessage, previous.openingMessage, ChatRole.CHARACTER.name,
                    previous.id, previous.storyTitle))
            }
        }

        private fun reviseUneditedCatalogEntries(db: SQLiteDatabase) {
            val revised = BuiltinCharacters.profiles.associateBy { it.id }
            for (previous in BuiltinCharacters.previousProfiles) {
                val saved = db.findCharacter(previous.id) ?: continue
                if (!saved.custom && saved == previous) {
                    val next = revised.getValue(previous.id)
                    db.update("characters", next.toValues(), "id = ?", arrayOf(saved.id))
                }
            }
        }

        private fun reviseUneditedIntroductions(db: SQLiteDatabase) {
            val revised = BuiltinCharacters.preChatProfiles.associateBy { it.id }
            for (previous in BuiltinCharacters.preIntroductionProfiles) {
                val saved = db.findCharacter(previous.id) ?: continue
                if (!saved.custom && saved == previous) {
                    val values = ContentValues().apply {
                        put("opening_message", revised.getValue(previous.id).openingMessage)
                    }
                    db.update("characters", values, "id = ?", arrayOf(saved.id))
                }
            }
        }

        private fun reviseUneditedChatStarts(db: SQLiteDatabase) {
            val revised = BuiltinCharacters.profiles.associateBy { it.id }
            for (previous in BuiltinCharacters.preChatProfiles) {
                val saved = db.findCharacter(previous.id) ?: continue
                if (!saved.custom && saved == previous) {
                    db.update("characters", revised.getValue(saved.id).toValues(), "id = ?", arrayOf(saved.id))
                }
            }
            // A scaffold with no player turn is still an untouched start, not a played archive.
            // Any changed title, summary, note, timestamp or message keeps the old scene intact.
            for (next in revised.values) {
                if (db.findCharacter(next.id) == next) refreshUnplayedStarts(db, next)
            }
        }

        private fun refreshUnplayedStarts(db: SQLiteDatabase, next: CharacterProfile) {
            val baselines = (BuiltinCharacters.preChatProfiles + BuiltinCharacters.preIntroductionProfiles +
                BuiltinCharacters.previousProfiles).filter { it.id == next.id }
            val stories = db.query("stories", null, "character_id = ?", arrayOf(next.id), null, null, null)
                .use { cursor -> cursor.collect { readStory() } }
            for (story in stories) {
                if (story.summary.isNotBlank() || story.updatedAt != story.createdAt || story.startContext.isNotBlank()) continue
                val messages = db.query("messages", null, "story_id = ?", arrayOf(story.id), null, null, null)
                    .use { cursor -> cursor.collect { readMessage() } }
                val message = messages.singleOrNull()?.takeIf {
                    it.role == ChatRole.CHARACTER && it.createdAt == story.createdAt
                } ?: continue
                val old = baselines.firstOrNull { it.openingMessage == message.text && it.storyTitle == story.title } ?: continue
                val notes = db.query("memories", null, "story_id = ?", arrayOf(story.id), null, null, null)
                    .use { cursor -> cursor.collect { readMemory() } }
                val expected = BuiltinCharacters.initialMemories(old, story.id, story.createdAt)
                fun signature(note: MemoryEntry) = listOf(note.kind, note.text, note.pinned, note.createdAt, note.updatedAt)
                if (notes.sortedBy { it.kind.name }.map(::signature) != expected.sortedBy { it.kind.name }.map(::signature)) continue
                db.update("messages", ContentValues().apply { put("text", next.openingMessage) }, "id = ?", arrayOf(message.id))
                db.update("stories", ContentValues().apply { put("start_context", CharacterIntroductions.contextFor(next)) },
                    "id = ?", arrayOf(story.id))
                val newNotes = BuiltinCharacters.initialMemories(next, story.id, story.createdAt).associateBy { it.kind }
                for (note in notes) db.update("memories", ContentValues().apply {
                    put("text", newNotes.getValue(note.kind).text)
                }, "id = ?", arrayOf(note.id))
            }
        }
    }
}

private inline fun <T> SQLiteDatabase.inTransaction(block: SQLiteDatabase.() -> T): T {
    beginTransaction()
    return try {
        val result = block()
        setTransactionSuccessful()
        result
    } finally {
        endTransaction()
    }
}

private fun SQLiteDatabase.findCharacter(id: String): CharacterProfile? {
    val cursor = query("characters", null, "id = ?", arrayOf(id), null, null, null)
    return try {
        if (cursor.moveToFirst()) cursor.readCharacter() else null
    } finally {
        cursor.close()
    }
}

private fun SQLiteDatabase.findStory(id: String): Story? {
    val cursor = query("stories", null, "id = ?", arrayOf(id), null, null, null)
    return try {
        if (cursor.moveToFirst()) cursor.readStory() else null
    } finally {
        cursor.close()
    }
}

private fun SQLiteDatabase.requireStory(id: String): Story =
    findStory(id) ?: throw IllegalArgumentException("Diese Geschichte wurde nicht gefunden.")

private fun SQLiteDatabase.touchStory(id: String, time: Long = System.currentTimeMillis()) {
    val story = requireStory(id)
    val timestamp = maxOf(time, incrementSafely(story.updatedAt))
    update("stories", ContentValues().apply { put("updated_at", timestamp) }, "id = ?", arrayOf(id))
}

private fun SQLiteDatabase.nextCharacterOrder(): Int {
    val cursor = rawQuery("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM characters", null)
    return try {
        cursor.moveToFirst()
        cursor.getInt(0)
    } finally {
        cursor.close()
    }
}

private fun incrementSafely(value: Long): Long = if (value == Long.MAX_VALUE) value else value + 1

private inline fun <T> Cursor.collect(read: Cursor.() -> T): List<T> = buildList {
    while (moveToNext()) add(read())
}

private fun Cursor.string(column: String): String = getString(getColumnIndexOrThrow(column))
private fun Cursor.long(column: String): Long = getLong(getColumnIndexOrThrow(column))
private fun Cursor.bool(column: String): Boolean = getInt(getColumnIndexOrThrow(column)) != 0

private fun Cursor.readCharacter() = CharacterProfile(
    id = string("id"), name = string("name"), role = string("role"), genre = string("genre"),
    traits = string("traits"), personality = string("personality"), scenario = string("scenario"),
    storyTitle = string("story_title"), openingMessage = string("opening_message"),
    avatarKey = string("avatar_key"), custom = bool("custom"),
)

private fun Cursor.readStory() = Story(
    id = string("id"), characterId = string("character_id"), title = string("title"),
    summary = string("summary"), startContext = string("start_context"), createdAt = long("created_at"), updatedAt = long("updated_at"),
)

private fun Cursor.readMessage() = ChatMessage(
    id = string("id"), storyId = string("story_id"), role = ChatRole.valueOf(string("role")),
    text = string("text"), createdAt = long("created_at"),
)

private fun Cursor.readMemory() = MemoryEntry(
    id = string("id"), storyId = string("story_id"), kind = MemoryKind.valueOf(string("kind")),
    text = string("text"), pinned = bool("pinned"), createdAt = long("created_at"), updatedAt = long("updated_at"),
)

private fun CharacterProfile.toValues() = ContentValues().apply {
    put("id", id); put("name", name); put("role", role); put("genre", genre); put("traits", traits)
    put("personality", personality); put("scenario", scenario); put("story_title", storyTitle)
    put("opening_message", openingMessage); put("avatar_key", avatarKey); put("custom", if (custom) 1 else 0)
}

private fun Story.toValues() = ContentValues().apply {
    put("id", id); put("character_id", characterId); put("title", title); put("summary", summary)
    put("start_context", startContext)
    put("created_at", createdAt); put("updated_at", updatedAt)
}

private fun ChatMessage.toValues() = ContentValues().apply {
    put("id", id); put("story_id", storyId); put("role", role.name); put("text", text); put("created_at", createdAt)
}

private fun MemoryEntry.toValues() = ContentValues().apply {
    put("id", id); put("story_id", storyId); put("kind", kind.name); put("text", text)
    put("pinned", if (pinned) 1 else 0); put("created_at", createdAt); put("updated_at", updatedAt)
}
