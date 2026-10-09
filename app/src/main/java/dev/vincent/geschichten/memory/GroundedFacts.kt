package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import java.util.Locale

/** Bounded German grammar for physical objects and injuries. Original text is never
 * rewritten. Unknown references remain unassigned; all emitted facts retain the exact
 * clause and source. Ownership, physical carrying and spatial placement are separate. */
object GroundedFacts {
    const val NO_HOLDER = "Niemand"
    const val NOT_PLACED = "Nicht abgelegt"
    private val objects = "Schlüssel|Schwert|Dolch|Ring|Brief|Amulett|Buch|Gerät|Pistole|Karte|Kristall|Trank|Tasche|Münze|Anhänger|Artefakt|Messer|Gewehr|Schriftrolle|Kette"
    private val colors = "silber\\p{L}*|bronze\\p{L}*|gold\\p{L}*|rot(?:e|en|er|es)?|blau(?:e|en|er|es)?|grün(?:e|en|er|es)?|schwarz(?:e|en|er|es)?|weiß(?:e|en|er|es)?"
    private val hypothetical = Regex("(?i)\\?|\\b(vielleicht|vermutlich|angeblich|könnte|würde|wäre|hätte|falls|wenn|angenommen|erinnerst|damals|gestern|früher|hatte|hattest|hättest|wünsche|wünschte|möchte|will|sollte)\\b")
    private val name = "(?:ich|du|mir|dir|mich|dich|(?-i:[A-ZÄÖÜ][\\p{L}’'-]+))"
    private val determiner = "(?:(?:den|die|das|der|dem|ein|eine|einen|einer|einem)\\s+)?"
    private val time = "(?:(?:jetzt|nun|noch|anschließend|danach|wieder|vorsichtig|weiterhin)\\s+)*"
    private val place = "((?:in|auf|unter|hinter|neben|vor|über)\\s+(?:der|dem|die|das|den|einer|einem|eine|einen)\\s+(?-i:[A-ZÄÖÜ][\\p{L}-]+)|(?:im|am)\\s+(?-i:[A-ZÄÖÜ][\\p{L}-]+))"
    private val conditions = "unverletzt|verletzt|blutet|gebrochen|verwundet|geheilt"
    private val possessionAuxiliary=Regex("(?i)\\b(?:gegeben|übergeben|gereicht|überreicht|geschenkt|zurückgegeben|geschickt|anvertraut|gelegt|abgelegt|versteckt|gesteckt|genommen|aufgenommen|geholt|gesehen|gesucht|verloren|gefunden|gehalten)\\b")
    private val body = "(?:(?:linke\\p{L}*|rechte\\p{L}*)\\s+)?(?:Hand|Arm|Bein|Schulter|Kopf)"
    private fun normal(s: String) = s.lowercase(Locale.GERMAN).trim()
    private fun color(s:String) = when {s.startsWith("silber")->"silbern";s.startsWith("bronze")->"bronzen";s.startsWith("gold")->"golden";else->s.replace(Regex("(?:en|er|es|e)$"),"")}
    private fun bodyName(s:String):String {
        val words=normal(s).split(' ')
        if(words.size==1)return words.single()
        val side=if(words.first().startsWith("link"))"link" else "recht"
        return side+(if(words.last()=="bein")"es" else "e")+" "+words.last()
    }
    // State describes where the item rests, so accusative destinations become dative.
    private fun restingPlace(s:String):String {
        val words=s.split(' ').toMutableList()
        if(words[0]=="im")return "in dem "+words.drop(1).joinToString(" ")
        if(words[0]=="am")return "an dem "+words.drop(1).joinToString(" ")
        if(words.size>=3)words[1]=when(words[1]){"die"->"der";"das","den"->"dem";"eine"->"einer";"einen"->"einem";else->words[1]}
        return words.joinToString(" ")
    }
    fun proposals(bundle:StoryBundle,message:ChatMessage,snapshot:MemorySnapshot=bundle.memory,authored:Boolean=false):List<MemoryProposal> {
        require(message.storyId==bundle.story.id)
        if(RoleEvidence.hasReportedSpeech(bundle,message))return emptyList()
        val story=bundle.story.id;val character=bundle.character.name
        var player=snapshot.current.firstOrNull {it.field=="identity" && it.entity.name=="Du"}?.value ?: "Du"
        val result=mutableListOf<MemoryProposal>();val working=snapshot.current.associateBy {it.slot}.toMutableMap()
        val mentioned=linkedSetOf<MemoryEntity>()
        val secret=message.role==ChatRole.USER && MemoryRules.privateAction(message.text)
        val knowers=if(secret)setOf("player") else setOf("player","character")
        fun actor(word:String):String? {
            // Capitalized dative address is unambiguous here; lowercase ihnen and
            // subject Sie can denote other people and remain unresolved.
            if(word=="Ihnen")return if(message.role==ChatRole.USER)character else "Du"
            return when(normal(word)) {
            "ich","mir","mich","mein","meine","meiner","meinem","meines" -> if(message.role==ChatRole.USER)"Du" else character
            "du","dir","dich","dein","deine","deiner","deinem","deines" -> if(message.role==ChatRole.USER)character else "Du"
            "er","sie","es","ihre","seine","und","dann","nun","danach","anschließend","der","die","das","in","auf","unter" -> null
            normal(player)->"Du"
            normal(character)->character
            in snapshot.characterAliases->character
            else -> word.takeIf {it.firstOrNull()?.isUpperCase()==true}
            }
        }
        fun entities()=working.values.filter {it.entity.kind==EntityKind.ITEM}.map {it.entity}.distinctBy {it.id}
        fun base(e:MemoryEntity)=Regex("(?i)\\b($objects)\\b").find(e.name)?.value ?: e.name
        fun resolve(word:String,descriptor:String?=null):MemoryEntity? {
            if(normal(word) in setOf("er","sie","es","ihn")) {
                // Never choose the most recent of several candidates arbitrarily.
                val candidates=mentioned.toList().ifEmpty {entities()}
                return candidates.distinctBy {it.id}.singleOrNull()
            }
            val paraphrase=when(normal(word)){"klinge"->setOf("Schwert","Dolch","Messer");"schmuckstück"->setOf("Ring","Amulett","Anhänger","Kette");"waffe"->setOf("Schwert","Dolch","Pistole","Messer","Gewehr");else->null}
            if(paraphrase!=null)return entities().filter {base(it) in paraphrase}.singleOrNull()
            val candidates=entities().filter {base(it).equals(word,true)}
            if(descriptor!=null) {
                val matches=candidates.filter {e->working[e.id+":color"]?.value==descriptor || e.name.startsWith("$descriptor ",true) ||
                    snapshot.facts.any {it.entity.id==e.id && it.field=="color" && it.value==descriptor && it.status!=FactStatus.EXCLUDED}}
                if(matches.size==1)return matches.single()
                if(matches.size>1)return null
                if(candidates.size==1 && message.role==ChatRole.CHARACTER)return candidates.single()
                if(candidates.isNotEmpty())return MemoryRules.identity(story,EntityKind.ITEM,"$descriptor ${word.replaceFirstChar {it.uppercase()}}")
            }
            if(candidates.size>1)return null
            return candidates.singleOrNull() ?: MemoryRules.identity(story,EntityKind.ITEM,word.replaceFirstChar {it.uppercase()})
        }
        val clauses=message.text.split(Regex("(?<=[.!?;\\n])\\s+|\\n+"))
        var lastBody:String?=null
        for((index,raw) in clauses.withIndex()) {
            val clause=raw.trim().trim('*','„','“','”','"');if(clause.isBlank())continue
            val uncertain=hypothetical.containsMatchIn(clause)
            fun validSubject(m:MatchResult,inverted:Boolean=false):Boolean {
                val word=m.groupValues[1];val who=actor(word) ?: return false
                val known=who in setOf("Du",character) || working.values.any {
                    it.entity.kind==EntityKind.PERSON && it.entity.name==who ||
                    it.entity.kind==EntityKind.ITEM && it.field in setOf("holder","owner") && it.value==who && who!=NO_HOLDER
                }
                if(known)return true
                // German ordinary nouns are also capitalized. Uppercase alone must not
                // turn a subordinate phrase into a new person carrying an object.
                return message.role==ChatRole.USER && !inverted && clause.substring(0,m.range.first).trim().matches(
                    Regex("(?i)(?:[\\p{L}\\p{N}_ -]{1,40}:\\s*)?(?:Dann|Nun|Jetzt|Danach|Anschließend)?"))
            }
            Regex("(?i)^Ich heiße (?-i:([A-ZÄÖÜ][\\p{L}-]+))").find(clause)?.let {if(message.role==ChatRole.USER && !uncertain)player=it.groupValues[1]}
            fun emit(entity:MemoryEntity,field:String,value:String,reason:String,creative:Boolean=false) {
                val old=working[entity.id+":"+field]
                val status=if(uncertain || message.role==ChatRole.CHARACTER && !authored && !creative)FactStatus.UNCERTAIN else FactStatus.CURRENT
                // A new public user assertion is a fresh source even when the value is
                // unchanged. Otherwise an older faulty source could later overwrite that
                // independently confirmed value during repair. Same-source repeats and
                // private repetitions do not create another unchanged world version.
                val confirmation=message.role==ChatRole.USER && !secret && old?.sourceId!=message.id
                if(old?.value==value && status==FactStatus.CURRENT && !confirmation)return
                val id=MemoryRules.digest("facts-v2:${message.id}:$index:${entity.id}:$field:$value")
                // Existing slot tombstones must also stop a changed rule's new IDs.
                if(id in snapshot.excludedEvents || snapshot.facts.any {it.slot==entity.id+":"+field && it.sourceId==message.id && it.status==FactStatus.EXCLUDED})return
                val f=StateFact(id,story,entity,field,value,status,old?.pinned ?: false,message.id,clause,snapshot.version+1,knowers,
                    createdAt=message.createdAt,sourceOrder=(bundle.messages.indexOfFirst {it.id==message.id}+1).toLong())
                result+=MemoryProposal(f,reason)
                if(status==FactStatus.CURRENT)working[f.slot]=f
            }
            fun personEntity(who:String)=if(who==character)MemoryRules.characterIdentity(story,bundle.character) else MemoryRules.identity(story,EntityKind.PERSON,who)
            val refs=mutableListOf<Pair<String,MemoryEntity>>()
            val matches=Regex("(?i)\\b($objects|Klinge|Schmuckstück|Waffe)\\b").findAll(clause).toList()
            for(m in matches) {
                val descriptor=Regex("(?i)\\b($colors)\\s+$").find(clause.substring(0,m.range.first))?.groupValues?.get(1)?.let {color(normal(it))}
                val e=resolve(m.value,descriptor) ?: continue
                refs+=m.value to e
                if(!uncertain)mentioned+=e
                if(descriptor!=null && !uncertain)emit(e,"color",descriptor,"Explizite Farbe")
                Regex("(?i)\\b${Regex.escape(m.value)}\\b.{0,30}\\b(?:ist|war tatsächlich|ist tatsächlich|ist in Wirklichkeit)\\s+$time($colors)\\b").find(clause)?.let {emit(e,"color",color(normal(it.groupValues[1])),"Explizite Farbe")}
            }
            // Only a singular reference to one already mentioned object can stand alone.
            if(refs.isEmpty())Regex("(?i)\\b(er|sie|es|ihn)\\b").find(clause)?.let {m->resolve(m.value)?.let {refs+=m.value to it}}
            for((word,e) in refs.distinctBy {it.second.id}) {
                val reference="(?:${Regex.escape(word)}|${if(refs.map {it.second.id}.distinct().size==1 && mentioned.size<=1) "er|sie|es|ihn" else "(?!)"})"
                val objectPhrase="$determiner(?:(?:$colors)\\s+)?$reference\\b"
                fun holder()=working[e.id+":holder"]?.value?.let {if(normal(it) in snapshot.characterAliases)character else it}
                val knownPlace=(snapshot.forCharacter()+result.filter {it.fact.status==FactStatus.CURRENT && "character" in it.fact.knownBy}.map {it.fact}).any {
                    it.entity.id==e.id && it.field=="placement" && it.value==working[e.id+":placement"]?.value && it.value!=NOT_PLACED}
                val own=holder()==character && !secret
                val subject="($name)\\s+"
                val transfer="(?:gebe|gibst|gibt|reiche|reichst|reicht|übergebe|übergibst|übergibt|überreiche|überreichst|überreicht|schenke|schenkst|schenkt)"
                val participle="(?:gegeben|übergeben|gereicht|überreicht|geschenkt|zurückgegeben|geschickt|anvertraut)"
                val prefix="(?i)\\b$subject(?:$transfer|(?:habe|hast|hat))\\s+$time"
                val before=Regex(prefix+"($name)\\s+$objectPhrase").find(clause)
                val after=Regex(prefix+"$objectPhrase\\s+(?:an\\s+)?($name)\\b").find(clause)
                val transferMatch=before ?: after
                if(transferMatch!=null) {
                    val suffix=clause.substring(transferMatch.range.last+1)
                    val auxiliary=Regex("(?i)\\b(?:habe|hast|hat)\\b").containsMatchIn(transferMatch.value)
                    // Sending does not establish arrival or physical custody.
                    val done=(!auxiliary || Regex("(?i)\\b$participle\\b").containsMatchIn(suffix)) && !Regex("(?i)\\bgeschickt\\b").containsMatchIn(suffix)
                    val negated=Regex("(?i)\\b(?:nicht|nie|kein\\p{L}*)\\b").containsMatchIn(transferMatch.value+suffix.substringBefore(','))
                    val giver=actor(transferMatch.groupValues[1]);val receiver=actor(transferMatch.groupValues[2])
                    if(done && !negated && giver!=null && receiver!=null && validSubject(transferMatch)) {
                        val creative=own && giver==character
                        emit(e,"holder",receiver,"Belegte Übergabe",creative)
                        if(working[e.id+":placement"]!=null)emit(e,"placement",NOT_PLACED,"Übergabe beendet Ablage",creative)
                        if(Regex("(?i)\\b(schenke|schenkst|schenkt|geschenkt)\\b|als\\s+Geschenk").containsMatchIn(clause))emit(e,"owner",receiver,"Ausdrückliches Geschenk",creative)
                    }
                }
                val possession=Regex("(?i)\\b$subject(?:hat|habe|hast|trägt|trage|trägst|hält|halte|hältst)\\s+$time$objectPhrase").find(clause)
                if(possession!=null && validSubject(possession) && !possessionAuxiliary.containsMatchIn(clause.substring(possession.range.last+1).substringBefore(',')) && !Regex("(?i)\\bnicht\\b").containsMatchIn(clause.substring(possession.range.last+1).substringBefore(','))) {
                    actor(possession.groupValues[1])?.let {emit(e,"holder",it,"Explizites Tragen");if(working[e.id+":placement"]!=null)emit(e,"placement",NOT_PLACED,"Tragen beendet Ablage")}
                }
                Regex("(?i)\\b(?:hat|trägt|hält)\\s+($name)\\s+$time$objectPhrase").find(clause)?.let {m->
                    if(validSubject(m,true) && !possessionAuxiliary.containsMatchIn(clause.substring(m.range.last+1)) && !Regex("(?i)\\bnicht\\b").containsMatchIn(clause.substring(m.range.last+1)))actor(m.groupValues[1])?.let {emit(e,"holder",it,"Tragen mit invertiertem Subjekt")}
                }
                Regex("(?i)\\b$reference\\s+gehört\\s+$time($name)\\b").find(clause)?.let {m->
                    if(!Regex("(?i)\\bnicht\\b").containsMatchIn(clause.substring(m.range.last+1)))actor(m.groupValues[1])?.let {emit(e,"owner",it,"Explizites Eigentum")}
                }
                val stative=Regex("(?i)\\b$reference\\s+(?:liegt|ist)\\s+$time$place").find(clause)
                val laying=Regex("(?i)\\b$subject(?:lege|legst|legt|verstecke|versteckst|versteckt|stecke|steckst|steckt)\\s+$time$objectPhrase(?:\\s+(?:jetzt|nun|anschließend|heimlich))?\\s+$place").find(clause)
                    ?: Regex("(?i)\\b(?:lege|legst|legt|verstecke|versteckst|versteckt|stecke|steckst|steckt)\\s+($name)\\s+$time$objectPhrase\\s+$place").find(clause)
                val spatial=stative ?: laying
                if(spatial!=null && (stative!=null || validSubject(laying!!)) && !Regex("(?i)\\bnicht\\b").containsMatchIn(spatial.value)) {
                    // A person-pronoun subject (er legt...) is deliberately unresolved.
                    val creative=laying!=null && own && actor(laying.groupValues[1])==character
                    val location=restingPlace(spatial.groupValues[if(stative!=null)1 else 2])
                    emit(e,"holder",NO_HOLDER,"Gegenstand abgelegt",creative)
                    emit(e,"placement",location,"Räumliche Zuordnung mit Präposition",creative)
                }
                val pickup=Regex("(?i)\\b$subject(?:nehme|nimmst|nimmt)\\s+$time$objectPhrase").find(clause)
                if(pickup!=null && validSubject(pickup) && !Regex("(?i)\\bnicht\\b").containsMatchIn(clause.substring(pickup.range.last+1)))actor(pickup.groupValues[1])?.let {receiver->
                    val creative=receiver==character && !secret && (own || holder()==NO_HOLDER && knownPlace)
                    emit(e,"holder",receiver,"Aufnahme",creative)
                    if(working[e.id+":placement"]!=null)emit(e,"placement",NOT_PLACED,"Aufnahme beendet Ablage",creative)
                }
            }
            fun injury(who:String?,part:String,condition:String,match:String) {
                if(who==null)return
                val b=bodyName(part);val c=normal(condition)
                val absent=Regex("(?i)\\bnicht\\s+(?:(?:mehr|länger)\\s+)?${Regex.escape(c)}\\b").containsMatchIn(match)
                if(Regex("(?i)\\b(?:nicht|kein\\p{L}*)\\b").containsMatchIn(match) && !absent)return
                emit(personEntity(who),"injury:$b","$b: ${if(absent && c in setOf("verletzt","verwundet")) "unverletzt" else if(absent) "nicht $c" else c}","Expliziter Körperzustand")
                if(!uncertain)lastBody=b
            }
            // Possessive and named genitive body subjects; each match is processed.
            Regex("(?i)\\b(meine?|deine?|(?-i:[A-ZÄÖÜ][\\p{L}-]+['’]?s?))\\s+($body)\\s+(?:ist|sind)\\s+((?:nicht\\s+(?:(?:mehr|länger)\\s+)?)?($conditions))\\b").findAll(clause).forEach {m->
                val w=m.groupValues[1];val who=if(normal(w).startsWith("mein") || normal(w).startsWith("dein"))actor(w) else actor(w.trimEnd('\'','’').removeSuffix("s"))
                injury(who,m.groupValues[2],m.groupValues[4],m.value)
            }
            Regex("(?i)\\b(meine?|deine?)\\s+($body)\\s+($conditions)\\s+(?:ist|sind)\\b").findAll(clause).forEach {m->injury(actor(m.groupValues[1]),m.groupValues[2],m.groupValues[3],m.value)}
            Regex("(?i)\\b($name)\\s+hat\\s+(?:ein|eine|einen)\\s+(verletzte\\p{L}*|gebrochene\\p{L}*|verwundete\\p{L}*)\\s+($body)\\b").findAll(clause).forEach {m->
                val c=when {normal(m.groupValues[2]).startsWith("gebrochen")->"gebrochen";normal(m.groupValues[2]).startsWith("verwundet")->"verwundet";else->"verletzt"}
                injury(actor(m.groupValues[1]),m.groupValues[3],c,m.value)
            }
            Regex("(?i)\\b($name)\\s+(?:bin|bist|ist)\\s+(nicht\\s+(?:mehr\\s+)?verletzt|unverletzt|geheilt)\\b").findAll(clause).forEach {m->
                val who=actor(m.groupValues[1]) ?: return@forEach
                emit(personEntity(who),"injury:allgemein","allgemein: unverletzt","Ausdrückliche allgemeine Heilung")
                working.values.filter {it.entity.id==personEntity(who).id && it.field.startsWith("injury:") && it.field!="injury:allgemein"}.toList().forEach {f->
                    emit(f.entity,f.field,f.field.substringAfter(':')+": unverletzt","Ausdrückliche allgemeine Heilung")
                }
            }
            Regex("(?i)\\b(meines|deines)\\s+(?:ist|bleibt)\\s+($conditions)\\b").find(clause)?.let {m->lastBody?.let {injury(actor(m.groupValues[1]),it,m.groupValues[2],m.value)}}
        }
        return result.distinctBy {it.fact.id}
    }
}
