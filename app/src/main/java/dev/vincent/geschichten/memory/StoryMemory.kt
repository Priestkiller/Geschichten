package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import java.security.MessageDigest
import java.util.Locale

enum class FactStatus { CURRENT, HISTORICAL, UNCERTAIN, EXCLUDED }
enum class EntityKind { PERSON, ITEM, SCENE, GOAL, RELATIONSHIP, EVENT }
data class MemoryEntity(val id: String, val storyId: String, val kind: EntityKind, val name: String)
data class StateFact(
    val id: String, val storyId: String, val entity: MemoryEntity, val field: String, val value: String,
    val status: FactStatus = FactStatus.CURRENT, val pinned: Boolean = false,
    val sourceId: String? = null, val sourceText: String = "", val version: Long = 0,
    val knownBy: Set<String> = setOf("player", "character"), val manual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceOrder: Long = 0,
) {
    val slot get() = entity.id + ":" + field
    val label get() = if(field.startsWith("injury:")) "Verletzung" else if(field.startsWith("promise:")) "Versprechen" else when (field) {
        "holder" -> if(Regex("(?i)\\bgehört\\b").containsMatchIn(sourceText)) "Eigentümer (alte Zuordnung)" else "Träger"; "color" -> "Farbe"; "location" -> "Aufenthaltsort"
        "owner" -> "Eigentümer"; "placement" -> "Ablage"
        "identity" -> "Name"; "injury" -> "Verletzung"; "status" -> "Ziel"; "promise" -> "Versprechen"
        "trust" -> "Vertrauen"; "conflict" -> "Konflikt"; "question" -> "Offene Frage"
        else -> "Wichtige Erinnerung"
    }
}
data class Knowledge(val factId: String, val knower: String, val sourceId: String?, val version: Long)
data class MemorySnapshot(
    val version: Long = 0, val facts: List<StateFact> = emptyList(), val knowledge: List<Knowledge> = emptyList(),
    val scannedSources: Set<String> = emptySet(), val excludedEvents: Set<String> = emptySet(),
    val pendingSources: Int = 0, val overview: String = "",
    val characterAliases: Set<String> = emptySet(),
) {
    val current get() = facts.filter { it.status == FactStatus.CURRENT }
    fun forCharacter(): List<StateFact> {
        val learned = knowledge.filter { it.knower == "character" }.map { it.factId }.toSet()
        val liveSlots=current.map {it.slot}.toSet()
        return facts.filter { it.slot in liveSlots && it.id in learned && it.status !in setOf(FactStatus.EXCLUDED, FactStatus.UNCERTAIN) }
            .groupBy { it.slot }.values.mapNotNull { group -> group.firstOrNull {it.status == FactStatus.CURRENT} ?: group.maxWithOrNull(compareBy<StateFact> { f -> f.createdAt }.thenBy {f -> f.sourceOrder}.thenBy {f -> f.version}) }
    }
}
data class MemoryProposal(val fact: StateFact, val reason: String, val reveal: Boolean = false)

/** Conservative, source-bound German rules. Unrecognised language stays in the archive.
 * No generated JSON is accepted as evidence. A question, hypothesis or remembered past
 * cannot establish an event. IDs describe entities, not changing adjectives/owners. */
object MemoryRules {
    fun characterIdentity(story: String, character: CharacterProfile) = MemoryEntity(digest("$story:character:${character.id}"),story,EntityKind.PERSON,character.name)
    fun identity(story: String, kind: EntityKind, name: String): MemoryEntity = MemoryEntity(
        digest("$story:${kind.name}:${normal(name)}"), story, kind, name.trim())
    fun digest(text: String): String = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }.take(32)
    private fun normal(s: String) = s.lowercase(Locale.GERMAN).trim().replace(Regex("\\s+"), " ")
    fun privateAction(text: String) = Regex("(?i)heimlich|unbemerkt|unbeobachtet|ohne.{0,45}(?:sieht|bemerkt|beobachtet|wissen)").containsMatchIn(text)
    fun visibleText(message: ChatMessage): String = if (privateAction(message.text) && message.role == ChatRole.USER)
        "*Du handelst unbeobachtet. Die Figur erfährt dabei keine neuen Tatsachen; reagiere nur auf wahrnehmbare Vorgänge.*" else message.text
    private val hypothetical = Regex("(?i)\\?|\\b(vielleicht|vermutlich|angeblich|könnte|würde|wäre|falls|erinnerst|damals|gestern|früher|hatte|hattest|hättest)\\b")
    private val items = "Schlüssel|Schwert|Dolch|Ring|Brief|Amulett|Buch|Gerät|Pistole|Karte|Kristall|Trank|Tasche|Münze|Anhänger|Artefakt|Messer|Gewehr|Schriftrolle|Kette"
    private val colors = "silber(?:n|ne|nen|ner|nes)?|bronze(?:n|ne|nen|ner|nes)?|gold(?:en|ene|enen|ener|enes)?|rot(?:e|en|er|es)?|blau(?:e|en|er|es)?|grün(?:e|en|er|es)?|schwarz(?:e|en|er|es)?|weiß(?:e|en|er|es)?"
    private fun color(value: String) = when { value.startsWith("silber") -> "silbern"; value.startsWith("bronze") -> "bronzen"; value.startsWith("gold") -> "golden"; else -> value.replace(Regex("(?:en|er|es|e)$"), "") }

    fun proposals(bundle: StoryBundle, message: ChatMessage, snapshot: MemorySnapshot = bundle.memory, authored: Boolean = false): List<MemoryProposal> {
        require(message.storyId == bundle.story.id)
        if(RoleEvidence.hasReportedSpeech(bundle,message))return emptyList()
        val story = bundle.story.id; val character = bundle.character.name
        val player = snapshot.current.firstOrNull { it.entity.id == identity(story, EntityKind.PERSON, "Du").id && it.field == "identity" }?.value ?: "Du"
        fun actor(name: String): String = when (normal(name)) {
            "ich", "mir", "mich" -> if(message.role == ChatRole.USER) "Du" else character
            "du", "dir", "dich", "dein", "deine" -> if(message.role == ChatRole.USER) character else "Du"
            normal(player) -> "Du"
            normal(character) -> character
            in snapshot.characterAliases -> character
            else -> name.trim()
        }
        val result = mutableListOf<MemoryProposal>()
        // Scan the FULL source; punctuation does not truncate its middle. Rule matches are
        // bounded to clauses. The complete source length is checkpointed only after this call.
        val clauses = message.text.split(Regex("(?<=[.!?;\\n])\\s+|\\n+"))
        for ((clauseIndex,raw) in clauses.withIndex()) {
            val clause = raw.trim().trim('*', '“', '”', '„', '"')
            if (clause.isBlank()) continue
            val uncertain = hypothetical.containsMatchIn(clause)
            val secret = message.role == ChatRole.USER && privateAction(message.text)
            val knowers = if (secret) setOf("player") else setOf("player", "character")
            fun add(kind: EntityKind, name: String, field: String, value: String, reason: String, creative: Boolean = false) {
                val candidates = snapshot.current.map { it.entity }.distinctBy {it.id}.filter {it.kind == kind && it.name.contains(name,true)}
                val entity = if(kind in setOf(EntityKind.ITEM,EntityKind.GOAL) && candidates.size == 1) candidates.single()
                    else if(kind == EntityKind.PERSON && name == character) candidates.singleOrNull {it.name == character} ?: characterIdentity(story,bundle.character)
                    else if(kind == EntityKind.RELATIONSHIP) MemoryEntity(digest("$story:relationship:${bundle.character.id}:player"),story,kind,name)
                    else identity(story, kind, name)
                val old = snapshot.current.firstOrNull { it.entity.id == entity.id && it.field == field }
                // MODEL assertions cannot rewrite player facts just by mentioning them.
                val modelUncertain = message.role == ChatRole.CHARACTER && !authored && (!creative || uncertain)
                val ambiguousObject = kind == EntityKind.ITEM && (candidates.size > 1 || Regex("(?i)anderen|zweiten|weiteren").containsMatchIn(clause))
                val status = if (uncertain || modelUncertain || ambiguousObject) FactStatus.UNCERTAIN else FactStatus.CURRENT
                if (old?.value == value && status == FactStatus.CURRENT &&
                    !(message.role==ChatRole.USER && !secret && old.sourceId!=message.id)) return
                if (status == FactStatus.CURRENT && field == "status" && old?.value == "Abgeschlossen" && value == "Offen" && !Regex("(?i)wieder|erneut|nehme.{0,15}auf").containsMatchIn(clause)) return
                val id = digest("${message.id}:$clauseIndex:${entity.id}:$field:$value")
                if (id in snapshot.excludedEvents) return
                result += MemoryProposal(StateFact(id, story, entity, field, value, status, old?.pinned ?: false,
                    message.id, clause, snapshot.version + 1, knowers, createdAt=message.createdAt,sourceOrder=(bundle.messages.indexOfFirst {it.id==message.id}+1).toLong()), reason)
            }
            Regex("(?i)\\bIch (?:heiße|bin bekannt als|bin) (?-i:([A-ZÄÖÜ][\\p{L}-]+))").find(clause)?.let {
                if (message.role == ChatRole.USER) add(EntityKind.PERSON, "Du", "identity", it.groupValues[1], "Eigener Name")
            }
            Regex("(?i)\\b(${Regex.escape(character)}|${Regex.escape(player)}|ich|wir)\\s+(?:bin|ist|sind|gehen|gehe|geht|betreten|betrete|betritt|stehen|stehe|steht)\\s+(?:jetzt |nun |gemeinsam |zusammen )?(?:in den|in die|auf dem|auf den|im|in|am)\\s+([\\p{L}-]+)").find(clause)?.let {
                val subject = it.groupValues[1]; val place = it.groupValues[2].replaceFirstChar { c -> c.uppercase() }
                val persons = if (subject.equals("wir", true)) listOf("Du", character) else listOf(actor(subject))
                persons.forEach { person -> add(EntityKind.PERSON, person, "location", place, "Ortsangabe", message.role == ChatRole.CHARACTER && person == character && !secret) }
                if(subject.equals("wir",true)) add(EntityKind.SCENE,"Ausgangsszene","location",place,"Gemeinsamer Ortswechsel")
            }
            Regex("(?i)\\b(?:Ziel|Aufgabe|Vorhaben)\\s*[„\"']?(.{1,100}?)['\"“]?\\s+(?:ist|wurde|bleibt)\\s+(?:noch |jetzt |bereits |nun |bisher )?(nicht )?(abgeschlossen|erledigt|offen|wieder aufgenommen)").find(clause)?.let {
                add(EntityKind.GOAL, it.groupValues[1].trim(' ', ',', '„', '“', '"'), "status", if (it.groupValues[2].isEmpty() && it.groupValues[3].lowercase(Locale.GERMAN) in listOf("abgeschlossen", "erledigt")) "Abgeschlossen" else "Offen", "Expliziter Zielstatus")
            }
            Regex("(?i)\\bIch verspreche (?:dir|${Regex.escape(character)}),?\\s+(.+)").find(clause)?.let {
                val other=player.takeUnless {p -> p=="Du"} ?: "Spielerfigur"
                val speaker=if(message.role==ChatRole.USER) other else character
                val receiver=if(message.role==ChatRole.USER) character else other
                add(EntityKind.RELATIONSHIP, "Du und $character", "promise:${digest("$speaker:${normal(it.groupValues[1])}")}", "$speaker verspricht $receiver: ${it.groupValues[1]}", "Ausdrückliche Zusicherung",message.role == ChatRole.CHARACTER && !secret)
            }
            Regex("(?i)\\b(?:Mein|Unser) Ziel ist(?: es)?[, :]?\\s+(.+)").find(clause)?.let {
                add(EntityKind.GOAL,it.groupValues[1].trim().trimEnd('.'),"status","Offen","Ausdrückliches Vorhaben")
            }
            Regex("(?i)\\bIch (?:beschließe|entscheide mich),?\\s+(.+)").find(clause)?.let {
                add(EntityKind.EVENT,it.groupValues[1].trim().trimEnd('.'),"event",clause,"Ausdrückliche Entscheidung")
            }
            Regex("(?i)\\b${Regex.escape(character)} (vertraut|misstraut) mir,? weil (.+)").find(clause)?.let {
                add(EntityKind.RELATIONSHIP, "Du und $character", "trust", it.value, "Beziehungsänderung mit Grund")
            }
            // Public item assertions are interpreted by GroundedFacts below. Merely
            // mentioning an item and the addressee's name must not reveal its owner.
        }
        return (result + GroundedFacts.proposals(bundle,message,snapshot,authored)).distinctBy { it.fact.id + it.reveal }
    }

    fun preview(bundle: StoryBundle, message: ChatMessage): MemorySnapshot {
        val proposals = proposals(bundle, message)
        val facts = bundle.memory.facts.toMutableList(); val knowledge = bundle.memory.knowledge.toMutableList()
        for (proposal in proposals) {
            val f = proposal.fact.copy(version=bundle.memory.version + facts.size + 1)
            if (proposal.reveal) { knowledge += Knowledge(f.id, "character", message.id, bundle.memory.version + 1); continue }
            if (f.status == FactStatus.CURRENT) for (i in facts.indices) {
                if (facts[i].slot == f.slot && facts[i].status == FactStatus.CURRENT) facts[i] = facts[i].copy(status = FactStatus.HISTORICAL)
            }
            facts += f
            if (f.status == FactStatus.CURRENT) f.knownBy.forEach { knowledge += Knowledge(f.id, it, message.id, f.version) }
        }
        return bundle.memory.copy(facts = facts, knowledge = knowledge)
    }
}
