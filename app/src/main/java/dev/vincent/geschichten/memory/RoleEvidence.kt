package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import java.util.Locale

enum class RoleAction { TRANSFER, INJURY, FAMILY, CARRY, OWNER }
enum class EvidenceOrigin { USER_DECLARATION, AUTHORED_START, CONFIRMED_MODEL_ACTION, NEW_CREATIVE_ACTION, UNVERIFIED_MODEL_CLAIM }
data class RoleEvent(val action: RoleAction, val actor: String, val target: String = "", val item: String = "",
    val value: String = "", val reason: String = "", val past: Boolean = false, val negated: Boolean = false,
    val messageId: String, val start: Int, val end: Int, val original: String, val knownToCharacter: Boolean,
    val origin: EvidenceOrigin, val speaker: String? = null) {
    fun sameClaim(other: RoleEvent) = action==other.action && actor==other.actor && target==other.target && item.equals(other.item,true) && value.equals(other.value,true)
}

/** Narrow source-grounded role grammar. It never rewrites original pronouns or archive
 * messages. Ambiguous/nested speech is not assigned an invented speaker. No model call. */
object RoleEvidence {
    // Same authored-opening provenance used by the existing database backfill.
    fun authoredStart(bundle:StoryBundle,message:ChatMessage)=message.role==ChatRole.CHARACTER &&
        message.id==bundle.messages.firstOrNull()?.id && message.createdAt==bundle.story.createdAt &&
        message.text==bundle.character.openingMessage
    private val objects="Brief|Karte|Amulett|Schlüssel|Schwert|Ring|Buch|Gerät|Tasche|Dolch|Kristall"
    private val uncertain=Regex("(?i)\\?|\\b(vielleicht|vermutlich|angeblich|könnte|würde|wäre|hätte|falls|wenn|angenommen)\\b")
    private val pastCue=Regex("(?i)\\b(damals|gestern|früher|erinnere|erinnerung|besprochen|gesprochen|hatten|gab|gabst|übergeben|gegeben|anvertraut|geschenkt|schickte|geschickt)\\b")
    private val relevant=Regex("(?i)\\b($objects|Hand|Schulter|Arm|Bein|Schwester|Bruder)\\b")
    private val ownershipTime="(?:(?:jetzt|nun|weiterhin)\\s+)?"
    private val pronominalOwner=Regex("(?i)\\b(?:er|sie|es)\\s+gehört\\s+$ownershipTime(mir|dir|(?!jetzt\\b|nun\\b|weiterhin\\b)[\\p{L}-]+)\\b")
    fun player(bundle: StoryBundle)=bundle.memory.forCharacter().firstOrNull {it.field=="identity" && it.entity.name=="Du"}?.value ?: "Spielerfigur"
    private fun playerAliases(bundle: StoryBundle):Set<String> {
        val known=bundle.memory.knowledge.filter {it.knower=="character"}.map {it.factId}.toSet()
        return bundle.memory.facts.filter {it.field=="identity" && it.entity.name=="Du" && it.id in known}.map {it.value}.toSet()+player(bundle)
    }
    private fun canonical(bundle: StoryBundle,word:String): String {
        val trimmed=word.trim().trim('’','\'')
        return when {
            trimmed.equals("Du",true) || playerAliases(bundle).any {it.equals(trimmed,true)} -> player(bundle)
            trimmed.equals(bundle.character.name,true) || trimmed.lowercase(Locale.GERMAN) in bundle.memory.characterAliases -> bundle.character.name
            else -> trimmed
        }
    }
    private data class Piece(val start:Int,val end:Int,val speaker:String?,val reported:Boolean,val quoted:Boolean=false)
    private fun pieces(bundle:StoryBundle,message:ChatMessage): List<Piece> {
        val text=message.text;val default=if(message.role==ChatRole.USER) player(bundle) else bundle.character.name
        val result=mutableListOf<Piece>();var from=0
        val quotes=Regex("„([^“]*)“|\"([^\"]*)\"").findAll(text).toList()
        for(q in quotes) {
            if(q.range.first>from) result+=Piece(from,q.range.first,default,false)
            val prefix=text.substring(maxOf(from,q.range.first-100),q.range.first)
            val attribution=Regex("(?i)([\\p{L}’'-]+)\\s+(?:sagt|sagte|flüstert|flüsterte|antwortet|antwortete)(?:\\s+zu\\s+[\\p{L}-]+)?\\s*[:*,]?\\s*$").find(prefix)
            val echoed=Regex("(?i)deine Worte|du sagtest|du hast gesagt").containsMatchIn(prefix)
            val nested=q.value.drop(1).dropLast(1).contains(Regex("[„“\"]"))
            val speaker=if(nested) null else if(echoed) {if(default==bundle.character.name)player(bundle) else bundle.character.name} else attribution?.groupValues?.get(1)?.let {canonical(bundle,it)} ?: default
            result+=Piece(q.range.first+1,q.range.last,speaker,speaker!=default,true)
            from=q.range.last+1
        }
        if(from<text.length) result+=Piece(from,text.length,default,false)
        return result
    }
    fun hasReportedSpeech(bundle:StoryBundle,message:ChatMessage)= (message.text.contains('"') || message.text.contains('„')) && pieces(bundle,message).any {it.reported || it.speaker==null}
    fun legacyOwnership(fact:StateFact)=fact.field=="holder" && Regex("(?i)\\bgehört\\b").containsMatchIn(fact.sourceText)
    fun extract(bundle:StoryBundle,message:ChatMessage,from:Int=0,to:Int=message.text.length): List<RoleEvent> {
        require(message.storyId==bundle.story.id)
        val requested=message.text.substring(from,to)
        if(!relevant.containsMatchIn(requested) && !pronominalOwner.containsMatchIn(requested))return emptyList()
        if(player(bundle).equals(bundle.character.name,true))return emptyList()
        val result=mutableListOf<RoleEvent>();val main=bundle.character.name;val other=player(bundle)
        val mentionedItems=Regex("(?i)\\b($objects)\\b").findAll(message.text).toList()
        val uniqueAntecedent=mentionedItems.map {it.value}.distinctBy {it.lowercase()}.singleOrNull()
        val creativeNarration=message.role==ChatRole.CHARACTER && Regex("(?i)\\*\\s*${Regex.escape(main)}\\s+(gibt|reicht|übergibt)\\b").containsMatchIn(message.text) && !Regex("(?i)damals|gestern|früher|vielleicht").containsMatchIn(message.text)
        var narrativeFocus:String?=null
        for(piece in pieces(bundle,message)) {
            if(piece.speaker==null) continue
            val lo=maxOf(from,piece.start);val hi=minOf(to,piece.end);if(lo>=hi)continue
            val section=message.text.substring(lo,hi)
            val breaks=Regex("(?<=[.!?])\\s+|\\n+").findAll(section).map {it.range.last+1}.toList()+section.length
            var offset=0;var pieceFocus=if(piece.quoted)piece.speaker else narrativeFocus
            for(end in breaks.distinct()) {
                val raw=section.substring(offset,end);val start=lo+offset;offset=end
                val text=raw.trim().trim('*','„','“','"')
                val clearItemReference=uniqueAntecedent!=null && mentionedItems.any {it.range.last<start}
                if(text.isBlank() || (!relevant.containsMatchIn(text) && !(clearItemReference && pronominalOwner.containsMatchIn(text))) || uncertain.containsMatchIn(text)) continue
                val speaker=piece.speaker
                val addressee=if(speaker==main)other else if(speaker==other)main else null
                fun person(word:String):String?=when(word.lowercase(Locale.GERMAN).trim()) {
                    "ich","mir","mich","mein","meine","meiner","meinem" -> speaker
                    "du","dir","dich","dein","deine","deiner","deinem" -> addressee
                    "er","sie","ihre","seine","seiner","ihrer" -> null
                    else -> canonical(bundle,word)
                }
                val private=message.role==ChatRole.USER && MemoryRules.privateAction(message.text)
                val declared=message.role==ChatRole.USER && !piece.reported
                var focus=pieceFocus
                val subjectAtStart=Regex("^\\s*(${Regex.escape(main)}|${Regex.escape(other)})\\s+",RegexOption.IGNORE_CASE).find(text)
                if(subjectAtStart!=null){focus=person(subjectAtStart.groupValues[1])!!;pieceFocus=focus;if(!piece.quoted)narrativeFocus=focus}
                else if(Regex("(?i)^\\s*(ich|du)\\b").containsMatchIn(text)) {focus=person(text.trim().substringBefore(' '));pieceFocus=focus}
                else if(!piece.quoted && Regex("^(?-i:[A-ZÄÖÜ][\\p{L}-]+)\\s+(?:sitzt|steht|blickt|hält|hat|betrachtet)\\b").containsMatchIn(text)) {focus=null;pieceFocus=null;narrativeFocus=null}
                fun emit(action:RoleAction,actor:String?,target:String="",item:String="",value:String="",reason:String="",past:Boolean=pastCue.containsMatchIn(text),negated:Boolean=Regex("(?i)\\b(?:nicht|nie|kein\\p{L}*)\\b").containsMatchIn(text)) {
                    if(actor==null)return
                    if(action in setOf(RoleAction.TRANSFER,RoleAction.CARRY,RoleAction.OWNER) && (Regex("(?i)\\b(?:anderen?|zweiten?|weiteren?|neuen?)\\b").containsMatchIn(text) || bundle.memory.facts.map {it.entity}.distinctBy {it.id}.count {it.kind==EntityKind.ITEM && it.name.contains(item,true)}>1))return
                    // A stored fact with this source ID is not independent confirmation
                    // of the model's retrospective claim. Only accepted contemporaneous
                    // actions can use that fact; retrospective claims need another witness.
                    val confirmed=message.role==ChatRole.CHARACTER && !pastCue.containsMatchIn(text) && bundle.memory.facts.any {f ->
                        f.sourceId==message.id && f.status in setOf(FactStatus.CURRENT,FactStatus.HISTORICAL) &&
                            bundle.memory.knowledge.any {it.factId==f.id && it.knower=="character"} &&
                            ((f.field=="holder" && action in setOf(RoleAction.TRANSFER,RoleAction.CARRY) &&
                            canonical(bundle,f.value)==(if(action==RoleAction.TRANSFER)target else actor) && f.entity.name.equals(item,true)) ||
                            (action==RoleAction.INJURY && f.field.startsWith("injury:") && canonical(bundle,f.entity.name)==actor && f.value.contains(value,true)) ||
                            (action==RoleAction.OWNER && f.field=="owner" && canonical(bundle,f.value)==actor && f.entity.name.equals(item,true)))}
                    result+=RoleEvent(action,actor,target,item,value,reason,past,negated,message.id,start,lo+end,raw,!private,
                        if(declared) EvidenceOrigin.USER_DECLARATION else if(authoredStart(bundle,message)) EvidenceOrigin.AUTHORED_START else if(confirmed) EvidenceOrigin.CONFIRMED_MODEL_ACTION else if(creativeNarration && !past && actor==main && action in setOf(RoleAction.TRANSFER,RoleAction.INJURY)) EvidenceOrigin.NEW_CREATIVE_ACTION else EvidenceOrigin.UNVERIFIED_MODEL_CLAIM,speaker)
                }
                val before=Regex("(?i)\\b([\\p{L}’'-]+)\\s+(?:(?:habe|hast|hat)\\s+)?(?:gebe|gibst|gibt|gab|gabst|übergebe|übergibt|übergab|reiche|reicht|überreiche|überreicht|schenke|schenkst|schenkt|schicke|schickst|schickt|schickte)?\\s*(dir|mir|${Regex.escape(main)}|${Regex.escape(other)})\\b.{0,32}?\\b($objects)\\b").find(text)
                val after=Regex("(?i)\\b([\\p{L}’'-]+)\\s+(?:habe|hast|hat|gebe|gibst|gibt|gab|gabst|übergebe|übergibt)\\s+(?:den|die|das|einen|eine|ein)\\s+($objects)\\s+(?:an\\s+)?(dir|mir|dich|mich|${Regex.escape(main)}|${Regex.escape(other)})\\b").find(text)
                val transfer=before ?: after
                if(transfer!=null && (Regex("(?i)\\b(gebe|gibst|gibt|gab|gabst|übergebe|übergibt|übergab|reiche|reicht|überreiche|überreicht|schenke|schenkst|schenkt|schicke|schickst|schickt|schickte)\\b").containsMatchIn(transfer.value) || Regex("(?i)\\b(gegeben|übergeben|anvertraut|gereicht|geschickt|geschenkt|überreicht|zurückgegeben)\\b").containsMatchIn(text.substring(transfer.range.last+1)))) {
                    val word=transfer.groupValues[1]
                    val actor=if(Regex("(?i)\\b(am|im|bei|unter|hinter)\\s*$").containsMatchIn(text.substring(0,transfer.range.first)))null else if(word.lowercase() in setOf("und","dann","nun","anschließend","daraufhin")) {if(subjectAtStart!=null || Regex("(?i)^\\s*(ich|du)\\b").containsMatchIn(text))focus else null} else if(word.lowercase() in setOf("ich","du") || word.firstOrNull()?.isUpperCase()==true)person(word) else null
                    val recipient=person(transfer.groupValues[if(before!=null)2 else 3]);val transferredItem=transfer.groupValues[if(before!=null)3 else 2]
                    if(recipient!=null) {
                        var reason=""
                        val injuryReason=Regex("(?i)weil\\s+(ich|du|er|sie|${Regex.escape(main)}|${Regex.escape(other)})\\s+mit\\s+(meiner|deiner|seiner|ihrer)\\s+verletzten\\s+(Hand|Schulter|Bein)\\s+das\\s+(Siegel|Tor)\\s+nicht\\s+(unbeschädigt\\s+)?öffnen\\s+(?:kann|konnte)").find(text)
                        if(injuryReason!=null && actor!=null) {
                            // Two named participants do not make er/sie a unique patient.
                            // Do not turn this unresolved reference into a named fact.
                            val subject=injuryReason.groupValues[1]
                            val owner=injuryReason.groupValues[2]
                            val patient=if(subject.lowercase() in setOf("er","sie")) null else person(subject)
                            val explicitOwner=if(owner.lowercase().startsWith("mein") || owner.lowercase().startsWith("dein"))person(owner) else patient
                            if(patient!=null && explicitOwner==patient) {
                                reason="Grund der Übergabe: $patient konnte mit $patient${if(patient.endsWith('s')) "’" else "s"} verletzter ${injuryReason.groupValues[3]} das ${injuryReason.groupValues[4]} nicht ${injuryReason.groupValues[5]}öffnen."
                                emit(RoleAction.INJURY,patient,item=injuryReason.groupValues[3],value="verletzt",past=true,negated=false)
                            }
                        }
                        emit(RoleAction.TRANSFER,actor,recipient,transferredItem,value=if(Regex("(?i)schick").containsMatchIn(text))"schickte" else "übergab",reason=reason,negated=Regex("(?i)\\b(?:nicht|nie|kein\\p{L}*)\\b").containsMatchIn(text.substring(0,text.indexOf("weil").takeIf {it>=0} ?: text.length)))
                        if(actor!=null)focus=actor
                    }
                }
                Regex("(?i)\\b(meine?|deine?|ihre?|seine?|(?-i:[A-ZÄÖÜ][\\p{L}-]*(?:s|['’])))\\s+(linke\\p{L}* |rechte\\p{L}* )?(Hand|Schulter|Arm|Bein)\\b.{0,24}?\\b(verletzt|gebrochen|verwundet|unverletzt|geheilt)\\b").findAll(text).forEach {m ->
                    val rawOwner=m.groupValues[1];val owner=when(rawOwner.lowercase()) {"ihre","ihrer","seine","seiner" -> if(message.role==ChatRole.CHARACTER && Regex("(?i)^\\s*(sie|er|${Regex.escape(main)}|${Regex.escape(other)})\\b").containsMatchIn(text))focus else null;else -> if(rawOwner.endsWith("'") || rawOwner.endsWith("’")) person(rawOwner.dropLast(1)) else if(rawOwner.endsWith("s") && rawOwner.lowercase() !in setOf("meines","deines"))person(rawOwner.dropLast(1)) else person(rawOwner)}
                    emit(RoleAction.INJURY,owner,item=(m.groupValues[2]+m.groupValues[3]).trim(),value=m.groupValues[4],negated=Regex("(?i)\\b(?:nicht|nie|kein\\p{L}*)\\b").containsMatchIn(m.value))
                }
                Regex("(?i)\\b(meine?|deine?|[A-ZÄÖÜ][\\p{L}’'-]*s)\\s+(Schwester|Bruder)\\s+(?:heißt|ist|namens)\\s+([\\p{L}-]+)").findAll(text).forEach {m ->
                    val w=m.groupValues[1];emit(RoleAction.FAMILY,if(w.lowercase().startsWith("mein")||w.lowercase().startsWith("dein"))person(w) else person(w.dropLast(1)),m.groupValues[3],value=m.groupValues[2].lowercase())
                }
                Regex("(?i)\\b(meine?|deine?|meiner|deiner)\\s+(Schwester|Bruder)\\s+(?!(?:heißt|ist|namens)\\b)(?-i:([A-ZÄÖÜ][\\p{L}-]+))").findAll(text).forEach {m ->emit(RoleAction.FAMILY,person(m.groupValues[1]),m.groupValues[3],value=m.groupValues[2].lowercase())}
                Regex("(?i)\\b($objects)\\s+gehört\\s+$ownershipTime(mir|dir|${Regex.escape(main)}|${Regex.escape(other)})\\b").findAll(text).forEach {m ->emit(RoleAction.OWNER,person(m.groupValues[2]),item=m.groupValues[1])}
                Regex("(?i)\\b($objects)\\s+ist\\s+[^.!?]{0,60}?\\bund\\s+gehört\\s+$ownershipTime(mir|dir|${Regex.escape(main)}|${Regex.escape(other)})\\b").findAll(text).forEach {m ->emit(RoleAction.OWNER,person(m.groupValues[2]),item=m.groupValues[1])}
                val objectNames=Regex("(?i)\\b($objects)\\b").findAll(text).map {it.value}.distinctBy {it.lowercase()}.toList()
                if(objectNames.size==1)Regex("(?i)\\b(?:er|sie|es)\\s+gehört\\s+$ownershipTime(mir|dir|${Regex.escape(main)}|${Regex.escape(other)})\\b").findAll(text).forEach {m ->emit(RoleAction.OWNER,person(m.groupValues[1]),item=objectNames.single())}
                // Cross-sentence ownership is resolved only with one explicit preceding
                // object in this reply and no competing object/entity. It is not a global
                // pronoun rewrite; ambiguous references remain unassigned.
                if(objectNames.isEmpty() && clearItemReference)pronominalOwner.findAll(text).forEach {m ->emit(RoleAction.OWNER,person(m.groupValues[1]),item=uniqueAntecedent)}
                Regex("(?i)\\b(ich|du|${Regex.escape(main)}|${Regex.escape(other)})\\s+(?:trage|trägst|trägt|habe|hast|hat)\\s+(?:jetzt |gerade )?(?:das|den|die|einen|eine|ein)\\s+($objects)\\b").findAll(text).forEach {m ->
                    if(!Regex("(?i)\\b(gegeben|übergeben|anvertraut|geschenkt|gereicht|geschickt|überreicht|zurückgegeben)\\b").containsMatchIn(text.substring(m.range.last+1)))emit(RoleAction.CARRY,person(m.groupValues[1]),item=m.groupValues[2])
                }
            }
        }
        return result.distinctBy {listOf(it.action,it.actor,it.target,it.item,it.value,it.messageId,it.start)}
    }
    fun trusted(bundle:StoryBundle):List<RoleEvent> {
        val excluded=bundle.memory.facts.filter {it.status==FactStatus.EXCLUDED}.mapNotNull {it.sourceId}.toSet()
        val sourceEvents=bundle.messages.filter {it.storyId==bundle.story.id && it.id !in excluded}.flatMap {extract(bundle,it)}
            .filter {it.knownToCharacter && !it.negated && it.origin!=EvidenceOrigin.UNVERIFIED_MODEL_CLAIM}
        val manual=bundle.memory.forCharacter().filter {it.status==FactStatus.CURRENT && !legacyOwnership(it)}.mapNotNull {f ->
            val actor=canonical(bundle,if(f.field.startsWith("injury:"))f.entity.name else f.value)
            val kind=when {f.field.startsWith("injury:")->RoleAction.INJURY;f.field=="owner"->RoleAction.OWNER;f.field=="holder" && f.value!=GroundedFacts.NO_HOLDER && !f.value.startsWith("In/bei ")->RoleAction.CARRY;else->return@mapNotNull null}
            RoleEvent(kind,actor,item=if(kind==RoleAction.INJURY)f.value.substringBefore(':').trim() else f.entity.name,value=if(kind==RoleAction.INJURY)f.value.substringAfter(':').trim() else "",messageId=f.sourceId ?: "manual:${f.id}",start=0,end=f.sourceText.length,original=f.sourceText,knownToCharacter=true,origin=EvidenceOrigin.USER_DECLARATION)
        }
        return sourceEvents+manual
    }
    fun describesPast(text:String)=pastCue.containsMatchIn(text)

    fun renderNamed(bundle:StoryBundle,excerpt:ArchiveExcerpt,ground:List<RoleEvent> = trusted(bundle)):String? {
        val events=extract(bundle,excerpt.message,excerpt.start,excerpt.end).filter {it.knownToCharacter && !it.negated && (it.origin!=EvidenceOrigin.UNVERIFIED_MODEL_CLAIM || ground.any {g->g.sameClaim(it)})}
        if(events.isEmpty())return null
        if(Regex("(?i)\\b(weil|denn)\\b").containsMatchIn(excerpt.text) && events.none {it.reason.isNotEmpty()})return null
        return events.sortedBy {if(it.action==RoleAction.TRANSFER) 0 else 1}.joinToString("\n") {e -> when(e.action) {
            RoleAction.TRANSFER -> "Vergangenheit: ${e.actor} ${e.value} ${e.target} den ${e.item}. Handelnde Person: ${e.actor}. Gegenstand: ${e.item}. Empfänger: ${e.target}. ${e.reason}"
            RoleAction.INJURY -> "Betroffene Person: ${e.actor}. Zustand: ${e.actor}${if(e.actor.endsWith('s')) "’" else "s"} ${e.item} ist ${e.value}."
            RoleAction.FAMILY -> "Familienzuordnung: ${e.target} ist ${e.value} von ${e.actor}."
            RoleAction.CARRY -> "Träger: ${e.actor}. Getragener Gegenstand: ${e.item}. Tragen belegt kein Eigentum."
            RoleAction.OWNER -> "Eigentümer: ${e.actor}. Gegenstand: ${e.item}. Eigentum belegt keinen aktuellen Träger."
        }}
    }
}
