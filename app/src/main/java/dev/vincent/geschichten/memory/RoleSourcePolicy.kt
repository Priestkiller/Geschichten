package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*

/** A processed source is not a confirmed fact. Repetition by the model never proves it. */
object RoleSourcePolicy {
    private val speculation=Regex("(?i)\\?|\\b(vielleicht|vermutlich|angeblich|könnte|würde|wäre|hätte|falls|wenn|angenommen)\\b")
    private val sharedPast=Regex("(?i)\\b(?:wir|ich|du)\\s+(?:haben|habe|hast|hatten|hatte|hattest)\\b.{0,100}\\b(?:gesprochen|besprochen|erlebt|getroffen|gesehen|geschenkt)\\b|\\bich erinnere mich\\b|\\berwähnte\\b.{0,120}\\b(?:Geschenk|Schwester|Bruder)\\b")
    fun supported(event:RoleEvent,ground:List<RoleEvent>) = event.origin!=EvidenceOrigin.UNVERIFIED_MODEL_CLAIM || ground.any {it.sameClaim(event) && it.negated==event.negated}
    fun eligible(bundle:StoryBundle,excerpt:ArchiveExcerpt,ground:List<RoleEvent> = RoleEvidence.trusted(bundle)):Boolean {
        if(excerpt.message.role==ChatRole.USER) return !speculation.containsMatchIn(excerpt.text)
        if(RoleEvidence.authoredStart(bundle,excerpt.message))return true
        val events=RoleEvidence.extract(bundle,excerpt.message,excerpt.start,excerpt.end)
        if(events.isNotEmpty()) return events.all {supported(it,ground)}
        if(Regex("(?i)(?:nicht|nie).{0,25}(?:gesprochen|besprochen)|keine gemeinsame Erinnerung|kann mich.{0,20}nicht erinnern").containsMatchIn(excerpt.text))return true
        if(sharedPast.containsMatchIn(excerpt.text) && !Regex("(?i)\\b(nicht|nie|keine)\\b").containsMatchIn(excerpt.text)) return false
        return !RoleEvidence.describesPast(excerpt.text)
    }
    fun visible(bundle:StoryBundle,message:ChatMessage,ground:List<RoleEvent> = RoleEvidence.trusted(bundle)):String {
        if(message.role==ChatRole.USER) return MemoryRules.visibleText(message)
        val allowed=ArchiveRecall.sections(message).filter {eligible(bundle,it,ground)}
        return allowed.joinToString(""){it.text}.ifBlank {"*Eine ungeprüfte Behauptung über die Vergangenheit wurde für die Modelleingabe ausgelassen; es gibt daraus keine bestätigte Erinnerung.*"}
    }
    fun unsupportedPast(reply:String,bundle:StoryBundle):Boolean {
        if(!sharedPast.containsMatchIn(reply) || Regex("(?i)\\b(nicht gesprochen|nie besprochen|nicht erinnern|keine Erinnerung)\\b").containsMatchIn(reply))return false
        val key=Regex("(?i)Geschenk|Schwester|Bruder|Treffen|Morgen").findAll(reply).map {it.value.lowercase()}.toSet()
        val sources=bundle.messages.filter {(it.role==ChatRole.USER || RoleEvidence.authoredStart(bundle,it)) && !MemoryRules.privateAction(it.text)}
        return key.isNotEmpty() && sources.none {m -> !speculation.containsMatchIn(m.text) && key.count {m.text.contains(it,true)}>=minOf(2,key.size)}
    }
}
