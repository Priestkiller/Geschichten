package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*

/** Reject only source-bound, explicit contradictions with a known CURRENT slot.
 * This does not grade fluency or claim semantic coverage of arbitrary stories. */
object MemoryReplyGuard {
    fun validate(bundle: StoryBundle, reply: String, recalledSources: List<ArchiveExcerpt> = emptyList()) {
        RoleReplyGuard.validate(bundle,reply)
        if(Regex("(?im)^\\s*(?:AKTUELLES FIGURENWISSEN DER APP|NÄCHSTE NUTZERNACHRICHT|AKTUELLER STAND AUS FIGURENSICHT|ORIGINALQUELLEN ZUR AKTUELLEN FRAGE|Originalstelle \\d+(?=,))\\b").containsMatchIn(reply))
            throw IllegalStateException("Die KI hat Gedächtnisvorgaben statt einer Figurenantwort ausgegeben. Deine Nachricht bleibt für einen neuen Versuch erhalten.")
        val source=ChatMessage(storyId=bundle.story.id,role=ChatRole.CHARACTER,text=reply)
        val known=bundle.memory.forCharacter().filterNot {RoleEvidence.legacyOwnership(it)}.associateBy {it.slot}
        for(p in MemoryRules.proposals(bundle,source)) {
            val previous=known[p.fact.slot] ?: if(p.fact.field.startsWith("injury:")) known.values.singleOrNull {it.field == p.fact.field} else null
            if(previous == null) continue
            val wrongPerson=previous.entity.id != p.fact.entity.id
            if(p.fact.status != FactStatus.UNCERTAIN || !wrongPerson && p.fact.value == previous.value || p.reveal) continue
            if(Regex("(?i)\\?|\\b(vielleicht|könnte|würde|wäre|falls)\\b").containsMatchIn(p.fact.sourceText)) continue
            val learned=bundle.memory.knowledge.filter {it.knower=="character"}.map {it.factId}.toSet()
            val recalledIds=recalledSources.map {it.message.id}.toSet()
            if(Regex("(?i)\\b(damals|früher|ursprünglich)\\b").containsMatchIn(p.fact.sourceText) &&
                !Regex("(?i)\\b(jetzt|nun|aktuell)\\b").containsMatchIn(p.fact.sourceText) &&
                bundle.memory.facts.any {f -> f.status==FactStatus.HISTORICAL && f.id in learned && f.sourceId in recalledIds && f.slot==p.fact.slot && f.value==p.fact.value && recalledSources.any {e -> e.message.id==f.sourceId && e.text.contains(f.sourceText)}}) continue
            throw IllegalStateException("Die Antwort widerspricht einem bekannten Gedächtniseintrag (${previous.entity.name}: ${previous.label}). Deine Nachricht bleibt für einen neuen Versuch erhalten.")
        }
    }
}
