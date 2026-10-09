package dev.vincent.geschichten.memory

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import dev.vincent.geschichten.data.*

/** Incremental, source-scoped repair of the v1 object/injury grammar. The caller owns
 * the story transaction. No archive replay, deletion, manual override or model call. */
object FactRepair {
    const val RULE_VERSION=2
    fun create(db:SQLiteDatabase) = db.execSQL("""CREATE TABLE IF NOT EXISTS memory_rule_repairs (
        story_id TEXT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
        source_id TEXT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
        rule_version INTEGER NOT NULL, version INTEGER NOT NULL,
        PRIMARY KEY(source_id,rule_version))""")
    private fun target(f:StateFact)=f.entity.kind==EntityKind.ITEM && f.field in setOf("holder","owner","placement","color") || f.field.startsWith("injury:")
    private val affected=Regex("(?i)\\b(geschenkt|schenk\\p{L}*|überreich\\p{L}*|liegt|lege|legt|legst|versteck\\p{L}*|nehm\\p{L}*|nimmt|nimmst|verletz\\p{L}*|gebrochen\\p{L}*|verwundet\\p{L}*|unverletzt|geheilt|gehört|trägt|trage)\\b")
    fun mark(db:SQLiteDatabase,story:String,source:String) = db.insertWithOnConflict("memory_rule_repairs",null,ContentValues().apply {
        put("story_id",story);put("source_id",source);put("rule_version",RULE_VERSION);put("version",MemoryDatabase.version(db,story))
    },SQLiteDatabase.CONFLICT_IGNORE)
    fun run(db:SQLiteDatabase,bundle:StoryBundle,limit:Int=4):Int {
        val marked=db.rawQuery("SELECT source_id FROM memory_rule_repairs WHERE story_id=? AND rule_version=?",arrayOf(bundle.story.id,RULE_VERSION.toString())).use {c->buildSet {while(c.moveToNext())add(c.getString(0))}}
        val active=bundle.memory.current.filter {target(it) && !it.manual}.mapNotNull {it.sourceId}.toSet()
        val sources=bundle.messages.withIndex().filter {(_,m)->m.id in bundle.memory.scannedSources && m.id !in marked && affected.containsMatchIn(m.text) &&
            (m.role==ChatRole.USER || RoleEvidence.authoredStart(bundle,m))}
            .sortedWith(compareByDescending<IndexedValue<ChatMessage>> {it.value.id in active}.thenByDescending {it.index})
            .take(limit.coerceIn(1,8)).map {it.value}.sortedBy {bundle.messages.indexOf(it)}
        for(source in sources) {
            val now=MemoryDatabase.snapshot(db,bundle.story.id)
            val order=db.rawQuery("SELECT rowid FROM messages WHERE id=? AND story_id=?",arrayOf(source.id,source.storyId)).use {it.moveToFirst();it.getLong(0)}
            val old=now.facts.filter {it.sourceId==source.id && !it.manual && target(it)}
            // A user's exclusion is a boundary, including new IDs from revised rules.
            if(now.facts.none {it.sourceId==source.id && it.status==FactStatus.EXCLUDED}) {
                val before=now.facts.filter {it.sourceId!=source.id && it.sourceOrder<order && it.status !in setOf(FactStatus.EXCLUDED,FactStatus.UNCERTAIN)}
                    .groupBy {it.slot}.values.mapNotNull {it.maxWithOrNull(compareBy<StateFact>{f->f.sourceOrder}.thenBy {f->f.version})}.map {it.copy(status=FactStatus.CURRENT)}
                val prefix=now.copy(facts=before+now.facts.filter {it.status==FactStatus.EXCLUDED},knowledge=now.knowledge)
                val proposals=GroundedFacts.proposals(bundle.copy(memory=prefix),source,prefix,RoleEvidence.authoredStart(bundle,source))
                val protected=now.current.filter {it.manual}.map {it.slot}.toSet()
                val excludedSlots=now.facts.filter {it.status==FactStatus.EXCLUDED && it.sourceOrder>=order}.map {it.slot}.toSet()
                val allowed=proposals.filter {p->p.fact.slot !in protected && p.fact.slot !in excludedSlots &&
                    // A manual carry/location decision protects its coupled physical fields.
                    !(p.fact.field in setOf("holder","placement") && now.current.any {it.manual && it.entity.id==p.fact.entity.id && it.field in setOf("holder","placement")})}
                val newSlots=allowed.map {it.fact.slot}.toSet()
                for(f in old) {
                    val invalid=f.value.startsWith("In/bei ") || Regex("(?i)\\bgeschenkt\\b").containsMatchIn(f.sourceText) && f.field=="holder" ||
                        f.slot in newSlots || f.field.startsWith("injury:") && allowed.any {it.fact.entity.id==f.entity.id && it.fact.field.startsWith("injury:")}
                    if(invalid && f.slot !in protected && f.status!=FactStatus.EXCLUDED) {
                        // Keep original derivation and knowledge for audit, but do not feed a
                        // proven-invalid derivation as a current or historical truth.
                        db.execSQL("UPDATE state_facts SET status='UNCERTAIN' WHERE id=? AND manual=0",arrayOf(f.id))
                        MemoryDatabase.bump(db,bundle.story.id)
                    }
                }
                for(p in allowed) {
                    val related=old.filter {it.entity.id==p.fact.entity.id && (it.field==p.fact.field || p.fact.field in setOf("owner","placement") && it.field=="holder" || p.fact.field.startsWith("injury:") && it.field.startsWith("injury:"))}
                    val witness=related.filter {it.sourceText==p.fact.sourceText}.maxByOrNull {it.version} ?: related.maxByOrNull {it.version}
                    val knowledge=if(witness==null)p.fact.knownBy else now.knowledge.filter {it.factId==witness.id}.map {it.knower}.toSet()
                    MemoryDatabase.apply(db,p.fact.copy(knownBy=knowledge,pinned=p.fact.pinned || related.any {it.pinned}),source)
                }
            }
            mark(db,bundle.story.id,source.id)
        }
        return sources.size
    }
}
