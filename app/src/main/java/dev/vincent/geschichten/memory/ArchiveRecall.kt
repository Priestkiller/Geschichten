package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.*
import java.text.Normalizer
import java.util.Locale

/** Exact slice of an archived original, never a newly asserted world fact. */
data class ArchiveExcerpt(val message: ChatMessage, val start: Int, val end: Int, val score: Int) {
    val text get() = message.text.substring(start, end)
}

/** Local lexical retrieval over the complete story archive. Chunk sizes only organize
 * search: the runtime tokenizer, including roles and reserve, binds prompt inclusion. */
object ArchiveRecall {
    private val stop = ("aber alle allem allen alles also am an auch auf aus bei bin bis bist das dass dein deine deiner dem den denn der des dich die dir doch dort du ein eine einem einen einer eines er es etwas für habe haben hast hat hatte hatten hier ich im in ist ja kann kein keine man mein meine meiner mich mir mit nach noch nun ob oder ohne sein seine sich sie sind so und uns unser unsere unter vom von vor war waren was welche welcher welches wenn wer wie wir wird wo zu zum zur jetzt gerade bitte gesagt sage sagst weiß weißt wissen farbe trägt tragen befindet aktuell").split(' ').toSet()
    private fun words(text: String): Set<String> = Regex("[\\p{L}\\p{N}]{3,}").findAll(
        Normalizer.normalize(text, Normalizer.Form.NFC).lowercase(Locale.GERMAN))
        .map { it.value }.filter { it !in stop }
        .map { if(it.length > 5) it.replace(Regex("(?:en|er|es|em|e|n)$"), "") else it }.toSet()
    fun asksAboutPast(text: String) = Regex("(?i)\\b(damals|früher|zuvor|vorhin|gestern|ursprünglich|erinner\\p{L}*|besprochen|versprochen|versprach\\p{L}*|hatte|hatten|warum|wann)\\b").containsMatchIn(text)

    /** Shared permission check for lexical and semantic candidates, and again at inclusion.
     * A vector never grants access to a source and never establishes a world fact. */
    fun eligible(bundle: StoryBundle, excerpt: ArchiveExcerpt, question: String,
                 evidence: List<RoleEvent> = RoleEvidence.trusted(bundle)): Boolean {
        val original=bundle.messages.firstOrNull {it.id==excerpt.message.id} ?: return false
        if(original.storyId!=bundle.story.id || original!=excerpt.message || excerpt.start<0 || excerpt.end>original.text.length || excerpt.start>=excerpt.end) return false
        if(MemoryRules.privateAction(question) || original.id==bundle.messages.lastOrNull()?.takeIf {it.role==ChatRole.USER}?.id) return false
        if(bundle.memory.facts.any {it.status==FactStatus.EXCLUDED && it.sourceId==original.id}) return false
        if(original.role==ChatRole.USER && MemoryRules.privateAction(original.text)) return false
        if(!RoleSourcePolicy.eligible(bundle,excerpt,evidence))return false
        if(!asksAboutPast(question)) {
            val known=bundle.memory.forCharacter().associateBy {it.slot}
            if(MemoryRules.proposals(bundle,original.copy(text=excerpt.text)).any {p -> known[p.fact.slot]?.let {it.value!=p.fact.value}==true}) return false
        }
        return true
    }

    fun candidates(bundle: StoryBundle, question: String, evidence: List<RoleEvent> = RoleEvidence.trusted(bundle),
                   checkCancellation: () -> Unit = {}): List<ArchiveExcerpt> {
        val result=mutableListOf<ArchiveExcerpt>();val seen=mutableSetOf<String>()
        for(message in bundle.messages) {
            checkCancellation();if(message.storyId!=bundle.story.id)continue
            for(excerpt in sections(message)) {
                checkCancellation()
                if(eligible(bundle,excerpt,question,evidence) && seen.add("${message.role}:${excerpt.text}"))result+=excerpt
            }
        }
        return result
    }

    /** Full coverage, exact offsets; long unpunctuated paragraphs split at word boundaries. */
    fun sections(message: ChatMessage): List<ArchiveExcerpt> {
        val text=message.text
        val ends=Regex("(?<=[.!?])\\s+|\\n+").findAll(text).map {it.range.last+1}.toList()+text.length
        val spans=mutableListOf<ArchiveExcerpt>()
        var begin=0
        for(end in ends.distinct()) {
            var from=begin
            while(from<end) {
                var to=minOf(end,from+1200)
                if(to<end) {
                    val space=text.lastIndexOf(' ',to)
                    if(space>from) to=space+1
                    if(to<end && to>from && text[to-1].isHighSurrogate() && text[to].isLowSurrogate()) to--
                }
                if(to==from) to=minOf(end,from+2)
                spans+=ArchiveExcerpt(message,from,to,0);from=to
            }
            begin=end
        }
        return spans
    }

    /** A ranked sentence often contains a pronoun or the second half of an event.
     * Include at most two preceding sections and one following section, within 1200
     * characters. Recheck the combined original; no rank or similarity grants access.
     * If old contradictory facts make the context ineligible, retain just the hit. */
    fun withContext(bundle:StoryBundle, excerpt:ArchiveExcerpt, question:String,
                    evidence:List<RoleEvent> = RoleEvidence.trusted(bundle)):ArchiveExcerpt {
        if(!eligible(bundle,excerpt,question,evidence))return excerpt
        val sections=sections(excerpt.message)
        val at=sections.indexOfFirst {it.start==excerpt.start && it.end==excerpt.end}
        if(at<0)return excerpt
        val from=sections[maxOf(0,at-2)].start;val to=sections[minOf(sections.lastIndex,at+1)].end
        if(to-from>1200)return excerpt
        val context=excerpt.copy(start=from,end=to)
        return if(eligible(bundle,context,question,evidence))context else excerpt
    }

    fun search(bundle: StoryBundle, question: String, limit: Int = 12, evidence:List<RoleEvent>?=null): List<ArchiveExcerpt> {
        if(MemoryRules.privateAction(question)) return emptyList()
        val names=words(bundle.character.name)+words(bundle.memory.current.firstOrNull {it.field=="identity"}?.value.orEmpty())
        val query=words(question)-names
        if(query.isEmpty()) return emptyList()
        val excluded=bundle.memory.facts.filter {it.status==FactStatus.EXCLUDED}.mapNotNull {it.sourceId}.toSet()
        val pending=bundle.messages.lastOrNull()?.takeIf {it.role==ChatRole.USER}?.id
        val seen=mutableSetOf<String>()
        val ground=evidence ?: RoleEvidence.trusted(bundle)
        return bundle.messages.asSequence().filter { it.storyId==bundle.story.id && it.id!=pending && it.id !in excluded &&
            !(it.role==ChatRole.USER && MemoryRules.privateAction(it.text)) }
            .flatMap {sections(it).asSequence()}.mapNotNull {excerpt ->
                val hits=words(excerpt.text).intersect(query).size
                if(hits==0 || !seen.add("${excerpt.message.role}:${excerpt.text}")) return@mapNotNull null
                if(!eligible(bundle,excerpt,question,ground))return@mapNotNull null
                excerpt.copy(score=hits*10 + if(excerpt.message.role==ChatRole.USER) 1 else 0)
            }.sortedWith(compareByDescending<ArchiveExcerpt> {it.score}.thenByDescending {it.message.createdAt}.thenBy {it.start})
            .take(limit.coerceIn(1,24)).toList()
    }

    fun render(bundle: StoryBundle, excerpts: List<ArchiveExcerpt>, namedEvidence:Boolean=false,evidence:List<RoleEvent>?=null): String = if(excerpts.isEmpty()) "" else buildString {
        appendLine("\nORIGINALQUELLEN ZUR AKTUELLEN FRAGE:")
        appendLine("Historische Originalaussagen, keine neue Handlung. Der aktuelle Stand hat Vorrang. Fragen und Vermutungen bestätigen keine Ereignisse. Figurenbehauptungen sind keine ungeprüfte Wahrheit. Behaupte nur eine Vergangenheit, die diese Quellen belegen; sonst frage nach.")
        excerpts.forEachIndexed {index,e ->
            val author=if(e.message.role==ChatRole.USER) "Spielerfigur (ich=Spielerfigur, du=${bundle.character.name})" else "${bundle.character.name} (ich=${bundle.character.name}, du=Spielerfigur)"
            val named=if(namedEvidence) RoleEvidence.renderNamed(bundle,e,evidence ?: RoleEvidence.trusted(bundle)) else null
            val originalSpeaker=if(e.message.role==ChatRole.USER) RoleEvidence.player(bundle) else bundle.character.name
            appendLine(if(named!=null) "Originalstelle ${index+1}, abgeleitete Ereignisangaben (kein wörtliches Zitat), Sprecher $originalSpeaker, Archivzeit ${e.message.createdAt}, Abschnitt ${e.start+1}–${e.end}:"
                else "Originalstelle ${index+1}, $author, Archivzeit ${e.message.createdAt}, Abschnitt ${e.start+1}–${e.end}:")
            appendLine(named ?: e.text)
            appendLine("Ende Originalstelle ${index+1}.")
        }
    }
}
