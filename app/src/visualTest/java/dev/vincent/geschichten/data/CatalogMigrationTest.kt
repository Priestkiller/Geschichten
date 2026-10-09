package dev.vincent.geschichten.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Upgrades a database created with the exact schema delivered in APK 0.1.0, whose SHA-256 is
 * 1a74ab7c4dc1e38528d1a01b0305f97277368d7ab484cf50f9d3b617c8b32df4. The fixture uses a frozen
 * copy of its four-character catalog. It never invokes v2 creation or downgrades a v2 database.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CatalogMigrationTest {
    private lateinit var context: Context
    private var repository: StoryRepository? = null

    @Before
    fun createFreshContext() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun closeAndRemoveFixture() {
        repository?.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun deliveredV1UpgradesWithoutChangingAnyExistingRecordsAndReopensIdempotently() {
        createDeliveredV1Fixture(customId = "legacy-custom-figure")
        val before = snapshot()
        assertEquals(1, databaseVersion())
        assertEquals(5, before.getValue("characters").size)

        repository = StoryRepository(context)
        val characters = repository!!.characters() // Opens the real helper and runs onUpgrade.
        val after = snapshot()

        assertEquals(10, databaseVersion())
        assertEquals(91, characters.size) // Ninety included IDs plus the user's separate figure.
        assertTrue(characters.map { it.id }.containsAll(BuiltinCharacters.profiles.map { it.id }))
        assertOnlyUntouchedDefaultProfilesRevised(before, after)
        assertEquals(before.getValue("stories"), after.getValue("stories"))
        assertEquals(before.getValue("messages"), after.getValue("messages"))
        assertEquals(before.getValue("memories"), after.getValue("memories"))
        val runaStory = requireNotNull(repository!!.bundle("legacy-story-runa"))
        assertEquals("Runa – meine eigene Fassung", runaStory.character.name)
        assertEquals("Mein bearbeiteter Spielstand.", runaStory.story.summary)
        assertEquals("Mein Ring gehört Runa.", runaStory.memories.single().text)
        assertEquals("Mein gespeicherter Dialog.", runaStory.messages.last().text)

        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())

        val newStory = repository!!.createStory("vaelgor")
        val newBundle = requireNotNull(repository!!.bundle(newStory.id))
        assertEquals(4, newBundle.memories.size)
        assertEquals(MemoryKind.entries.toSet(), newBundle.memories.map { it.kind }.toSet())
        assertTrue(newBundle.memories.all { it.storyId == newStory.id })
        assertFalse(newBundle.memories.any { it.id == "legacy-memory-runa" })
        assertEquals("Mein Ring gehört Runa.", repository!!.bundle("legacy-story-runa")!!.memories.single().text)
    }

    @Test fun deliveredV7CorrectsOnlyDefaultIntroductionsAndPreservesPlayedAndEditedContent() {
        createDeliveredV6Fixture(customId = "legacy-custom-figure")
        val mira = DeliveredV7CatalogFixture.profiles.single { it.id == "mira" }
        val runa = DeliveredV7CatalogFixture.profiles.single { it.id == "runa" }
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            db.execSQL("ALTER TABLE stories ADD COLUMN start_context TEXT NOT NULL DEFAULT ''")
            DeliveredV7CatalogFixture.profiles.forEachIndexed { index, profile ->
                db.update("characters", legacyCharacterValues(profile, index), "id = ?", arrayOf(profile.id))
            }
            insertUnplayedStart(db, mira, "fresh-mira")
            insertUnplayedStart(db, mira, "played-mira")
            insertMessage(db, "player-mira", "played-mira", "USER", "Ich empfange nichts.", 1100)
            insertUnplayedStart(db, runa, "edited-runa")
            db.update("characters", ContentValues().apply { put("opening_message", "Mein eigener Einstieg.") }, "id = ?", arrayOf("runa"))
            db.version = 7
        }
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(10, databaseVersion())
        val fresh = requireNotNull(repository!!.bundle("fresh-mira"))
        assertTrue(fresh.messages.single().text.contains("„Ich bin Mira."))
        assertEquals(mira.openingMessage, repository!!.bundle("played-mira")!!.messages.first().text)
        assertEquals("Ich empfange nichts.", repository!!.bundle("played-mira")!!.messages.last().text)
        assertEquals("Mein eigener Einstieg.", current.single { it.id == "runa" }.openingMessage)
        assertEquals(runa.openingMessage, repository!!.bundle("edited-runa")!!.messages.single().text)
        assertEquals(before.getValue("memories"), snapshot().getValue("memories"))
        assertEquals(before.getValue("stories"), snapshot().getValue("stories"))
        val once = snapshot()
        repository!!.close()
        repository = StoryRepository(context)
        repository!!.characters()
        assertEquals(once, snapshot())
    }

    @Test
    fun aUsersExistingRecordWithANewReservedIdIsKeptInsteadOfReplaced() {
        createDeliveredV1Fixture(customId = "aelwyn")
        val before = snapshot()

        repository = StoryRepository(context)
        val characters = repository!!.characters()
        val after = snapshot()

        assertEquals(90, characters.size) // The existing reserved ID is retained instead of replaced.
        assertOnlyUntouchedDefaultProfilesRevised(before, after)
        val collision = characters.single { it.id == "aelwyn" }
        assertEquals("Meine eigene Archivarin", collision.name)
        assertTrue(collision.custom)
        val story = requireNotNull(repository!!.bundle("legacy-story-custom"))
        assertEquals("aelwyn", story.character.id)
        assertEquals("Meine eigene Archivarin", story.character.name)
        assertEquals("Das gehört nur zu meiner Figur.", story.memories.single().text)
    }

    @Test
    fun deliveredV2AddsSeventyFiguresWithoutChangingSavedRecordsOrOrder() {
        createDeliveredV2Fixture(customId = "legacy-custom-figure")
        val before = snapshot()
        assertEquals(2, databaseVersion())
        assertEquals(21, before.getValue("characters").size)
        val previousMaxOrder = before.getValue("characters").values.maxOf { it.getValue("sort_order")!!.toInt() }

        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(10, databaseVersion())
        val after = snapshot()
        assertOnlyUntouchedDefaultProfilesRevised(before, after)
        for (table in listOf("stories", "messages", "memories")) assertEquals(before[table], after[table])
        val added = after.getValue("characters").filterKeys { it !in before.getValue("characters") }
        assertEquals(70, added.size)
        assertTrue(added.values.all { it.getValue("sort_order")!!.toInt() > previousMaxOrder })

        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
        val story = repository!!.createStory("sana")
        val bundle = requireNotNull(repository!!.bundle(story.id))
        assertEquals(4, bundle.memories.size)
        assertEquals(MemoryKind.entries.toSet(), bundle.memories.map { it.kind }.toSet())
        assertTrue(bundle.memories.all { it.storyId == story.id && it.text.startsWith("Startvorgabe:") })
        assertTrue(bundle.memories.any { it.text.contains("Aster") })
        assertEquals("Mein Ring gehört Runa.", repository!!.bundle("legacy-story-runa")!!.memories.single().text)
    }

    @Test
    fun deliveredV2KeepsAUsersFigureWhoseIdCollidesWithAnAddition() {
        createDeliveredV2Fixture(customId = "maelis")
        val before = snapshot()
        repository = StoryRepository(context)
        val figures = repository!!.characters()
        assertEquals(90, figures.size)
        assertEquals(10, databaseVersion())
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot())
        val kept = figures.single { it.id == "maelis" }
        assertEquals("Meine eigene Archivarin", kept.name)
        assertTrue(kept.custom)
        assertEquals("Das gehört nur zu meiner Figur.", repository!!.bundle("legacy-story-custom")!!.memories.single().text)
    }

    @Test
    fun deliveredV3RevisesDefaultsButKeepsEditedProfilesAndEveryArchivedRow() {
        createDeliveredV3Fixture(customId = "legacy-custom-figure")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            for (id in listOf("leon", "elara", "tarek")) {
                val old = DeliveredV3CatalogFixture.profiles.single { it.id == id }
                val edited = when (id) {
                    "leon" -> old.copy(personality = "Mein eigener Ermittler, ohne gesetztes Custom-Flag.")
                    "elara" -> old.copy(openingMessage = "Mein selbst geschriebener Einstieg.")
                    else -> old.copy(custom = true)
                }
                db.update("characters", legacyCharacterValues(edited, 100 + id.length), "id = ?", arrayOf(id))
            }
        }
        val before = snapshot()
        assertEquals(3, databaseVersion())
        repository = StoryRepository(context)
        val current = repository!!.characters()
        val after = snapshot()
        assertEquals(10, databaseVersion())
        assertEquals(91, current.size)
        assertOnlyUntouchedDefaultProfilesRevised(before, after)
        for (id in listOf("runa", "leon", "elara", "tarek", "legacy-custom-figure")) {
            assertEquals("Preserve user variant $id", before.getValue("characters")[id], after.getValue("characters")[id])
        }
        assertEquals(BuiltinCharacters.profiles.single { it.id == "sigrid" }, current.single { it.id == "sigrid" })
        val oldStory = requireNotNull(repository!!.bundle("legacy-story-runa"))
        assertEquals("Mein gespeicherter Dialog.", oldStory.messages.last().text)
        assertEquals("Mein Ring gehört Runa.", oldStory.memories.single().text)
        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
        val fresh = repository!!.createStory("sigrid")
        val bundle = requireNotNull(repository!!.bundle(fresh.id))
        assertEquals(BuiltinCharacters.profiles.single { it.id == "sigrid" }.openingMessage, bundle.messages.single().text)
        assertEquals(4, bundle.memories.size)
    }

    @Test
    fun deliveredV3KeepsAUserIdCollisionAndCreatesNotesForAnUnchangedOlderOpening() {
        createDeliveredV3Fixture(customId = "maelis")
        val old = DeliveredV3CatalogFixture.profiles.single { it.id == "sana" }
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            db.update("characters", legacyCharacterValues(old.copy(custom = true), 200), "id = ?", arrayOf("sana"))
        }
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(90, current.size)
        assertEquals(10, databaseVersion())
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot())
        val collision = current.single { it.id == "maelis" }
        assertEquals("Meine eigene Archivarin", collision.name)
        assertTrue(collision.custom)
        val story = repository!!.createStory("sana")
        val bundle = requireNotNull(repository!!.bundle(story.id))
        assertEquals(old.openingMessage, bundle.messages.single().text)
        assertEquals(MemoryKind.entries.toSet(), bundle.memories.map { it.kind }.toSet())
        assertTrue(bundle.memories.any { it.text.contains("Aster") })
    }

    @Test
    fun deliveredV4AppendsAllFortyFiguresAndUpdatesOnlyUneditedIntroductions() {
        createDeliveredV4Fixture("legacy-custom-figure")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            val old = DeliveredV4CatalogFixture.profiles.single { it.id == "sigrid" }
            db.update("characters", legacyCharacterValues(old.copy(personality = "Meine selbst geschriebene Jarlin.", custom = false), 300), "id = ?", arrayOf(old.id))
        }
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(10, databaseVersion())
        assertEquals(91, current.size)
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot(), DeliveredV5CatalogFixture.profiles)
        val added = current.filter { it.id !in before.getValue("characters").keys }
        assertEquals(BuiltinCharacters.profiles.drop(50), added)
        val after = snapshot()
        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
        val newStory = repository!!.createStory("varkesha")
        val bundle = requireNotNull(repository!!.bundle(newStory.id))
        assertEquals(BuiltinCharacters.profiles.single { it.id == "varkesha" }.openingMessage, bundle.messages.single().text)
        assertEquals(MemoryKind.entries.toSet(), bundle.memories.map { it.kind }.toSet())
        assertTrue(bundle.memories.all { it.storyId == newStory.id })
    }

    @Test
    fun deliveredV4RetainsAUserFigureWhoseIdCollidesWithANewWorldCharacter() {
        createDeliveredV4Fixture("kira_rook")
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(90, current.size)
        assertEquals(10, databaseVersion())
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot(), DeliveredV5CatalogFixture.profiles)
        val custom = current.single { it.id == "kira_rook" }
        assertEquals("Meine eigene Archivarin", custom.name)
        assertTrue(custom.custom)
        val newStory = repository!!.createStory(custom.id)
        val bundle = requireNotNull(repository!!.bundle(newStory.id))
        assertEquals(custom.openingMessage, bundle.messages.single().text)
        assertTrue(bundle.memories.none { it.text.contains("Relic") || it.text.contains("Dante") })
    }

    @Test
    fun deliveredV5UpdatesAllNinetyDefaultOpeningsAndKeepsEveryArchiveAndOrder() {
        createDeliveredV5Fixture("legacy-custom-figure")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            val runa = DeliveredV5CatalogFixture.profiles.single { it.id == "runa" }
            db.update("characters", legacyCharacterValues(runa, 0), "id = ?", arrayOf(runa.id))
        }
        val before = snapshot()
        assertEquals(5, databaseVersion())
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(10, databaseVersion())
        assertEquals(91, current.size)
        val after = snapshot()
        assertOnlyUntouchedDefaultProfilesRevised(before, after, DeliveredV5CatalogFixture.profiles)
        for (profile in BuiltinCharacters.profiles) {
            assertEquals(profile, current.single { it.id == profile.id })
            assertEquals(before.getValue("characters").getValue(profile.id).filterKeys { it !in setOf("opening_message", "personality", "scenario") },
                after.getValue("characters").getValue(profile.id).filterKeys { it !in setOf("opening_message", "personality", "scenario") })
        }
        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
        val fresh = repository!!.createStory("runa")
        val bundle = requireNotNull(repository!!.bundle(fresh.id))
        assertEquals(BuiltinCharacters.profiles.first().openingMessage, bundle.messages.single().text)
        assertEquals(4, bundle.memories.size)
        assertEquals("Mein gespeicherter Dialog.", repository!!.bundle("legacy-story-runa")!!.messages.last().text)
    }

    @Test
    fun deliveredV5PreservesEditedFieldsWithoutCustomFlagsAndAnIdCollision() {
        createDeliveredV5Fixture("kira_rook")
        val editedIds = listOf("elara", "leon", "tarek", "sigrid", "dante_raze", "varkesha")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            editedIds.forEachIndexed { index, id ->
                val old = DeliveredV5CatalogFixture.profiles.single { it.id == id }
                val edited = when (id) {
                    "elara" -> old.copy(openingMessage = "Mein eigener Einstieg.")
                    "leon" -> old.copy(personality = "Meine eigene Persönlichkeit.")
                    "tarek" -> old.copy(custom = true)
                    "sigrid" -> old.copy(name = "Meine Jarlin")
                    "dante_raze" -> old.copy(scenario = "Eine andere Werkstatt.")
                    else -> old.copy(traits = "Meine eigenen Eigenschaften")
                }
                db.update("characters", legacyCharacterValues(edited, 300 + index), "id = ?", arrayOf(id))
            }
        }
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(10, databaseVersion())
        assertEquals(90, current.size)
        val after = snapshot()
        assertOnlyUntouchedDefaultProfilesRevised(before, after, DeliveredV5CatalogFixture.profiles)
        for (id in editedIds + listOf("runa", "kira_rook")) {
            assertEquals("Preserved edited figure $id", before.getValue("characters")[id], after.getValue("characters")[id])
        }
        val collision = current.single { it.id == "kira_rook" }
        assertTrue(collision.custom)
        assertEquals("Meine eigene Archivarin", collision.name)
        val customStory = repository!!.createStory(collision.id)
        assertTrue(repository!!.bundle(customStory.id)!!.memories.none { it.text.contains("Relic") })
    }

    @Test
    fun deliveredV6RevisesAllNinetyProfilesButPreservesPlayedArchivesAndReopensIdempotently() {
        createDeliveredV6Fixture()
        val before = snapshot()
        repository = StoryRepository(context)
        val current = repository!!.characters()
        assertEquals(10, databaseVersion())
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot(), DeliveredV6CatalogFixture.profiles)
        for (profile in BuiltinCharacters.profiles) assertEquals(profile, current.single { it.id == profile.id })
        assertEquals("", repository!!.bundle("legacy-story-runa")!!.story.startContext)
        val after = snapshot()
        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
    }

    @Test
    fun allNinetyUntouchedSingleMessageStartsGainFullEntryAndFrozenContextWithoutDuplicateMessages() {
        createDeliveredV6Fixture()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            DeliveredV6CatalogFixture.profiles.forEach { insertUnplayedStart(db, it, "start-${it.id}") }
        }
        val before = snapshot()
        repository = StoryRepository(context)
        repository!!.characters()
        for (profile in BuiltinCharacters.profiles) {
            val bundle = repository!!.bundle("start-${profile.id}")!!
            assertEquals(profile.openingMessage, bundle.messages.single().text)
            assertEquals(CharacterIntroductions.contextFor(profile), bundle.story.startContext)
            assertEquals(1000L, bundle.story.createdAt)
            assertEquals(1000L, bundle.story.updatedAt)
            assertEquals(4, bundle.memories.size)
            val expected = BuiltinCharacters.initialMemories(profile, bundle.story.id, 1000).associateBy { it.kind }
            for (note in bundle.memories) {
                assertEquals(expected.getValue(note.kind).text, note.text)
                assertEquals(expected.getValue(note.kind).createdAt, note.createdAt)
                assertEquals(expected.getValue(note.kind).updatedAt, note.updatedAt)
                assertTrue(before.getValue("memories").containsKey(note.id))
            }
        }
        assertEquals(before.getValue("stories"), snapshot().getValue("stories"))
        assertEquals(before.getValue("messages").keys, snapshot().getValue("messages").keys)
        val after = snapshot()
        repository!!.close()
        repository = StoryRepository(context)
        assertEquals(91, repository!!.characters().size)
        assertEquals(after, snapshot())
        assertTrue(repository!!.bundle("start-riven")!!.story.startContext.isNotBlank())
    }

    @Test
    fun changedScaffoldsAndPlayedStoriesRetainEveryMessageNoteAndTimestamp() {
        createDeliveredV6Fixture()
        val ids = listOf("runa", "elara", "leon", "mira", "hildis", "riven", "ena", "morga")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            ids.forEach { id -> insertUnplayedStart(db, DeliveredV6CatalogFixture.profiles.single { it.id == id }, "changed-$id") }
            fun storyValue(id: String, key: String, value: String) = db.update("stories", ContentValues().apply { put(key, value) }, "id = ?", arrayOf("changed-$id"))
            storyValue("runa", "title", "Mein Titel")
            storyValue("elara", "summary", "Meine Zusammenfassung")
            storyValue("leon", "updated_at", "1001")
            db.update("messages", ContentValues().apply { put("text", "Mein eigener Anfang.") }, "story_id = ?", arrayOf("changed-mira"))
            insertMessage(db, "played-turn", "changed-hildis", "USER", "Ich antworte.", 1001)
            db.update("memories", ContentValues().apply { put("text", "Meine Notiz.") }, "story_id = ? AND kind = 'FACT'", arrayOf("changed-riven"))
            db.update("memories", ContentValues().apply { put("pinned", 0) }, "story_id = ? AND kind = 'EVENT'", arrayOf("changed-ena"))
            db.update("memories", ContentValues().apply { put("updated_at", 1001) }, "story_id = ? AND kind = 'GOAL'", arrayOf("changed-morga"))
        }
        val before = snapshot()
        repository = StoryRepository(context)
        repository!!.characters()
        val after = snapshot()
        for (table in listOf("stories", "messages", "memories")) assertEquals(before[table], after[table])
        for (id in ids) assertEquals("", repository!!.bundle("changed-$id")!!.story.startContext)
        val fresh = repository!!.createStory("hildis")
        val frozen = fresh.startContext
        val edited = repository!!.characters().single { it.id == "hildis" }.copy(scenario = "Ein anderer Ort.", openingMessage = "Meine Szene.")
        repository!!.upsertCharacter(edited)
        assertEquals(frozen, repository!!.bundle(fresh.id)!!.story.startContext)
        val ownStart = repository!!.createStory("hildis")
        assertEquals(edited.scenario, ownStart.startContext)
        assertEquals("Meine Szene.", repository!!.bundle(ownStart.id)!!.messages.single().text)
    }

    @Test
    fun deliveredV6PreservesUserEditsWithoutCustomFlagAndReservedIdCollision() {
        createDeliveredV6Fixture("kira_rook")
        val ids = listOf("runa", "elara", "leon", "mira", "sana", "caerion", "riven", "varkesha")
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            ids.forEachIndexed { index, id ->
                val old = DeliveredV6CatalogFixture.profiles.single { it.id == id }
                val changed = when (index) {
                    0 -> old.copy(personality = "Mein Charakter.")
                    1 -> old.copy(openingMessage = "Mein Einstieg.")
                    2 -> old.copy(name = "Mein Name")
                    3 -> old.copy(scenario = "Mein Ort")
                    4 -> old.copy(traits = "Meine Eigenschaften")
                    5 -> old.copy(avatarKey = "runa")
                    6 -> old.copy(storyTitle = "Mein Titel")
                    else -> old.copy(custom = true)
                }
                db.update("characters", legacyCharacterValues(changed, 300 + index), "id = ?", arrayOf(id))
            }
        }
        val before = snapshot()
        repository = StoryRepository(context)
        assertEquals(90, repository!!.characters().size)
        assertOnlyUntouchedDefaultProfilesRevised(before, snapshot(), DeliveredV6CatalogFixture.profiles)
        for (id in ids + "kira_rook") assertEquals(before.getValue("characters")[id], snapshot().getValue("characters")[id])
        assertEquals(repository!!.characters().single {it.id=="kira_rook"}.scenario, repository!!.createStory("kira_rook").startContext)
    }

    private fun createDeliveredV6Fixture(customId: String = "legacy-custom-figure") {
        createDeliveredV5Fixture(customId)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            DeliveredV6CatalogFixture.profiles.filter { it.id != customId }.forEachIndexed { index, profile ->
                db.update("characters", legacyCharacterValues(profile, index), "id = ?", arrayOf(profile.id))
            }
            db.version = 6
        }
    }

    private fun insertUnplayedStart(db: SQLiteDatabase, profile: CharacterProfile, id: String) {
        db.insertOrThrow("stories", null, ContentValues().apply {
            put("id", id); put("character_id", profile.id); put("title", profile.storyTitle)
            put("summary", ""); put("created_at", 1000); put("updated_at", 1000)
        })
        insertMessage(db, "opening-$id", id, "CHARACTER", profile.openingMessage, 1000)
        BuiltinCharacters.initialMemories(profile, id, 1000).forEach { note ->
            db.insertOrThrow("memories", null, ContentValues().apply {
                put("id", "note-$id-${note.kind.name}"); put("story_id", id); put("kind", note.kind.name)
                put("text", note.text); put("pinned", if (note.pinned) 1 else 0)
                put("created_at", note.createdAt); put("updated_at", note.updatedAt)
            })
        }
    }

    private fun createDeliveredV5Fixture(customId: String) {
        createDeliveredV4Fixture(customId)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            DeliveredV5CatalogFixture.profiles.drop(50).forEachIndexed { index, profile ->
                db.insertWithOnConflict("characters", null, legacyCharacterValues(profile, 100 + index), SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.version = 5
        }
    }

    private fun createDeliveredV4Fixture(customId: String) {
        createDeliveredV3Fixture(customId)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            db.beginTransaction()
            try {
                DeliveredV4CatalogFixture.profiles.filter { it.id != "runa" }.forEachIndexed { index, profile ->
                    db.update("characters", legacyCharacterValues(profile, 10 + index), "id = ?", arrayOf(profile.id))
                }
                db.version = 4
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun createDeliveredV3Fixture(customId: String) {
        createDeliveredV1Fixture(customId)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            db.beginTransaction()
            try {
                DeliveredV3CatalogFixture.profiles.drop(4).forEachIndexed { index, profile ->
                    db.insertWithOnConflict("characters", null, legacyCharacterValues(profile, 10 + index), SQLiteDatabase.CONFLICT_IGNORE)
                }
                db.version = 3
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun createDeliveredV2Fixture(customId: String) {
        createDeliveredV1Fixture(customId)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null).use { db ->
            db.beginTransaction()
            try {
                DeliveredV2CatalogFixture.profiles.drop(4).forEachIndexed { index, profile ->
                    db.insertOrThrow("characters", null, legacyCharacterValues(profile, 10 + index))
                }
                db.version = 2
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun createDeliveredV1Fixture(customId: String) {
        val path = context.getDatabasePath(DATABASE_NAME)
        check(path.parentFile!!.isDirectory || path.parentFile!!.mkdirs())
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.setForeignKeyConstraintsEnabled(true)
            db.beginTransaction()
            try {
                DELIVERED_V1_SCHEMA.forEach(db::execSQL)
                DeliveredV1CatalogFixture.profiles.forEachIndexed { index, profile ->
                    val saved = if (profile.id == "runa") profile.copy(
                        name = "Runa – meine eigene Fassung", role = "Von mir bearbeitete Kundschafterin",
                        personality = "Diese Persönlichkeit wurde in 0.1.0 von mir bearbeitet.",
                        scenario = "Mein eigener Ort am See.", openingMessage = "Mein eigener Einstieg.",
                        avatarKey = "mira", custom = true,
                    ) else profile
                    db.insertOrThrow("characters", null, legacyCharacterValues(saved, index))
                }
                val custom = DeliveredV1CatalogFixture.profiles.first().copy(
                    id = customId, name = "Meine eigene Archivarin", role = "Eigene Figur", custom = true,
                    personality = "Meine selbst geschriebene Persönlichkeit.", scenario = "Mein Archiv am See.",
                    openingMessage = "Mein selbst geschriebener Anfang.",
                )
                db.insertOrThrow("characters", null, legacyCharacterValues(custom, 9))
                insertStory(db, "legacy-story-runa", "runa", "Mein bearbeiteter Spielstand.", 100)
                insertStory(db, "legacy-story-custom", customId, "Meine zweite Geschichte.", 200)
                insertMessage(db, "legacy-message-user", "legacy-story-runa", "USER", "Meine gespeicherte Frage.", 101)
                insertMessage(db, "legacy-message-answer", "legacy-story-runa", "CHARACTER", "Mein gespeicherter Dialog.", 102)
                insertMessage(db, "legacy-message-custom", "legacy-story-custom", "CHARACTER", "Meine eigene Szene.", 201)
                insertMemory(db, "legacy-memory-runa", "legacy-story-runa", "Mein Ring gehört Runa.", 103, 105)
                insertMemory(db, "legacy-memory-custom", "legacy-story-custom", "Das gehört nur zu meiner Figur.", 203, 208)
                db.version = 1
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun legacyCharacterValues(profile: CharacterProfile, order: Int) = ContentValues().apply {
        put("id", profile.id); put("name", profile.name); put("role", profile.role); put("genre", profile.genre)
        put("traits", profile.traits); put("personality", profile.personality); put("scenario", profile.scenario)
        put("story_title", profile.storyTitle); put("opening_message", profile.openingMessage)
        put("avatar_key", profile.avatarKey); put("custom", if (profile.custom) 1 else 0); put("sort_order", order)
    }

    private fun insertStory(db: SQLiteDatabase, id: String, characterId: String, summary: String, time: Long) {
        db.insertOrThrow("stories", null, ContentValues().apply {
            put("id", id); put("character_id", characterId); put("title", "Gespeicherte Geschichte $id")
            put("summary", summary); put("created_at", time); put("updated_at", time + 8)
        })
    }

    private fun insertMessage(db: SQLiteDatabase, id: String, storyId: String, role: String, text: String, time: Long) {
        db.insertOrThrow("messages", null, ContentValues().apply {
            put("id", id); put("story_id", storyId); put("role", role); put("text", text); put("created_at", time)
        })
    }

    private fun insertMemory(db: SQLiteDatabase, id: String, storyId: String, text: String, created: Long, edited: Long) {
        db.insertOrThrow("memories", null, ContentValues().apply {
            put("id", id); put("story_id", storyId); put("kind", "FACT"); put("text", text)
            put("pinned", 1); put("created_at", created); put("updated_at", edited)
        })
    }

    private fun databaseVersion(): Int = SQLiteDatabase.openDatabase(
        context.getDatabasePath(DATABASE_NAME).absolutePath, null, SQLiteDatabase.OPEN_READONLY,
    ).use { it.version }

    private fun snapshot(): Map<String, Map<String, Map<String, String?>>> = SQLiteDatabase.openDatabase(
        context.getDatabasePath(DATABASE_NAME).absolutePath, null, SQLiteDatabase.OPEN_READONLY,
    ).use { db ->
        TABLES.associateWith { table ->
            db.query(table, null, null, null, null, null, "id ASC").use { cursor ->
                buildMap {
                    while (cursor.moveToNext()) {
                        val row = cursor.columnNames.mapIndexed { index, name ->
                            name to if (cursor.isNull(index)) null else cursor.getString(index)
                        }.filter { it.first != "start_context" }.toMap()
                        put(requireNotNull(row["id"]), row)
                    }
                }
            }
        }
    }

    private fun assertOnlyUntouchedDefaultProfilesRevised(
        before: Map<String, Map<String, Map<String, String?>>>,
        after: Map<String, Map<String, Map<String, String?>>>,
        baseline: List<CharacterProfile> = DeliveredV3CatalogFixture.profiles,
    ) {
        val previous = baseline.associateBy { it.id }
        val revised = BuiltinCharacters.profiles.associateBy { it.id }
        for ((table, rows) in before) {
            if (table != "characters") assertEquals("Unchanged complete archive table $table", rows, after.getValue(table))
            for ((id, expected) in rows) {
                val oldDefault = previous[id]?.takeIf { table == "characters" }?.let {
                    legacyCharacterValues(it, expected.getValue("sort_order")!!.toInt()).valueSet()
                        .associate { value -> value.key to value.value?.toString() }
                }
                val allowed = if (oldDefault == expected) {
                    legacyCharacterValues(revised.getValue(id), expected.getValue("sort_order")!!.toInt()).valueSet()
                        .associate { value -> value.key to value.value?.toString() }
                } else expected
                assertEquals("Only untouched defaults may change: $table/$id", allowed, after.getValue(table)[id])
            }
        }
    }

    private companion object {
        const val DATABASE_NAME = "geschichten.db"
        val TABLES = listOf("characters", "stories", "messages", "memories")

        // Copied verbatim from delivered 0.1.0 StoryRepository.Database.onCreate.
        val DELIVERED_V1_SCHEMA = listOf(
            """
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
            """.trimIndent(),
            """
                CREATE TABLE stories (
                    id TEXT PRIMARY KEY NOT NULL,
                    character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE RESTRICT,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent(),
            """
                CREATE TABLE messages (
                    id TEXT PRIMARY KEY NOT NULL,
                    story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
                    role TEXT NOT NULL CHECK (role IN ('USER', 'CHARACTER')),
                    text TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
            """.trimIndent(),
            """
                CREATE TABLE memories (
                    id TEXT PRIMARY KEY NOT NULL,
                    story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
                    kind TEXT NOT NULL CHECK (kind IN ('FACT', 'LOCATION', 'GOAL', 'EVENT')),
                    text TEXT NOT NULL,
                    pinned INTEGER NOT NULL CHECK (pinned IN (0, 1)),
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent(),
            "CREATE INDEX messages_story_time ON messages(story_id, created_at)",
            "CREATE INDEX memories_story_time ON memories(story_id, created_at)",
            "CREATE INDEX stories_updated ON stories(updated_at)",
            "CREATE INDEX stories_character ON stories(character_id)",
        )
    }
}
