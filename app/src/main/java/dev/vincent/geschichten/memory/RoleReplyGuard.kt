package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*

data class RoleConflict(val code:String,val detail:String,val sourceId:String?)
object RoleReplyGuard {
    private fun body(s:String)=s.lowercase().replace(Regex("[\\p{Punct}]"),"").trim()
    private fun sameBody(a:String,b:String):Boolean {
        val left=body(a);val right=body(b)
        val qualifiers=listOf("linke","rechte")
        if(qualifiers.any {left.startsWith(it)} && qualifiers.any {right.startsWith(it)} && left.substringBefore(' ')!=right.substringBefore(' '))return false
        return left.substringAfterLast(' ')==right.substringAfterLast(' ')
    }
    fun conflicts(bundle:StoryBundle,reply:String):List<RoleConflict> {
        val source=ChatMessage(storyId=bundle.story.id,role=ChatRole.CHARACTER,text=reply)
        val ground=RoleEvidence.trusted(bundle)
        val claims=RoleEvidence.extract(bundle,source)
        val conflicts=mutableListOf<RoleConflict>()
        for(c in claims) {
            // A denied role assignment is not an affirmative competing claim.
            if(c.negated) continue
            when(c.action) {
                RoleAction.TRANSFER -> {
                    val prior=ground.filter {it.action==c.action && it.item.equals(c.item,true)}
                    val creative=!c.past && bundle.memory.forCharacter().any {it.field=="holder" && it.entity.name.equals(c.item,true) && it.value==c.actor}
                    if(!creative && prior.isNotEmpty() && prior.none {it.sameClaim(c) && !c.negated})
                        conflicts+=RoleConflict("TRANSFER_ROLES","${c.item}: Die belegte Übergabe ist ${prior.last().actor} → ${prior.last().target}; die Antwort nennt ${c.actor} → ${c.target}.",prior.last().messageId)
                }
                RoleAction.INJURY -> {
                    val prior=ground.filter {it.action==c.action && sameBody(it.item,c.item) && it.value.equals(c.value,true)}
                    val creative=Regex("(?i)schneidet sich|stößt.{0,30}Schulter|verletzt sich|stürzt").containsMatchIn(c.original)
                    if(!creative && prior.isNotEmpty() && prior.none {it.actor==c.actor}) conflicts+=RoleConflict("INJURY_PERSON","${c.item}: Belegt ist ${prior.last().actor}; die Antwort ordnet den Zustand ${c.actor} zu.",prior.last().messageId)
                }
                RoleAction.FAMILY -> {
                    val prior=ground.filter {it.action==c.action && it.target.equals(c.target,true) && it.value==c.value}
                    if(prior.isNotEmpty() && prior.none {it.actor==c.actor})conflicts+=RoleConflict("FAMILY_OWNER","${c.target}: Belegt ist ${c.value} von ${prior.last().actor}; eine Zuordnung zu ${c.actor} ist unbelegt.",prior.last().messageId)
                }
                RoleAction.OWNER -> {
                    val prior=ground.filter {it.action==c.action && it.item.equals(c.item,true)}
                    if(prior.isNotEmpty() && prior.last().actor!=c.actor)conflicts+=RoleConflict("OWNERSHIP","${c.item}: Eigentümer ist ${prior.last().actor}, nicht ${c.actor}.",prior.last().messageId)
                    if(prior.isEmpty())conflicts+=RoleConflict("UNSUPPORTED_OWNERSHIP","Für den behaupteten Eigentümer von ${c.item} gibt es keine ausdrückliche Quelle. Tragen allein belegt kein Eigentum.",null)
                }
                RoleAction.CARRY -> Unit
            }
        }
        if(RoleSourcePolicy.unsupportedPast(reply,bundle))conflicts+=RoleConflict("UNSUPPORTED_PAST","Für die behauptete gemeinsame Vergangenheit fehlt eine bestätigte Quelle.",null)
        val last=bundle.messages.lastOrNull {it.role==ChatRole.USER}?.text.orEmpty()
        if(Regex("(?i)Geschenk.{0,40}meiner (Schwester|Bruder)").containsMatchIn(last) &&
            Regex("(?i)Geschenk.{0,40}meiner (Schwester|Bruder)").containsMatchIn(reply) &&
            !RoleEvidence.hasReportedSpeech(bundle,source) &&
            !Regex("(?i)(?:nicht|keineswegs).{0,25}Geschenk.{0,40}meiner").containsMatchIn(reply))conflicts+=RoleConflict("FAMILY_REFERENCE","Die Frage betrifft die Familie der Spielerfigur; die Antwort spricht von der eigenen Familie.",null)
        if(Regex("(?i)nächste\\s+nutz(?:er)?(?:nachricht| Nachricht)\\s+lautet|in der dritten Person zwischen Sternchen").containsMatchIn(reply))
            conflicts+=RoleConflict("INSTRUCTION_COPY","Die Antwort enthält eine kopierte Chat-/Erzählanweisung.",null)
        val quoted=Regex("„([^“]*)“|\"([^\"]*)\"").findAll(reply).map {it.groupValues[1].ifBlank {it.groupValues[2]}.trim()}.filter {it.isNotBlank()}.toList()
        if(last.endsWith('?') && quoted.isNotEmpty() && quoted.all {it==last.trim()} && !Regex("(?i)wiederhole|zitiere").containsMatchIn(last))
            conflicts+=RoleConflict("QUESTION_ECHO","Die Figur wiederholt nur deine Frage, statt zu antworten.",null)
        val sentences=reply.split(Regex("(?<=[.!?])\\s+")).map {it.trim().trim('*','„','“','"')}.filter {it.length>12}
        if(sentences.groupingBy {it}.eachCount().values.any {it>=5} && !Regex("(?i)wiederhole|refrain|mantra|zähle").containsMatchIn(last))
            conflicts+=RoleConflict("REPETITION_LOOP","Die Antwort wiederholt denselben Füllsatz mindestens fünfmal.",null)
        return conflicts.distinctBy {it.code+it.detail}
    }
    fun validate(bundle:StoryBundle,reply:String) {
        val conflicts=conflicts(bundle,reply)
        check(conflicts.isEmpty()) {"Die Antwort verwechselt belegte Rollen oder behauptet unbelegte Vergangenheit: ${conflicts.first().detail} Deine Nachricht bleibt für einen neuen Versuch erhalten."}
    }
}
