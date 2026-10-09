package dev.vincent.geschichten.memory

import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.*

data class MemoryPromptPlan(val system: String, val history: List<ModelMessage>, val inputTokens: Int,
                            val reserve: Int, val omittedSourceIds: List<String>, val planningMs: Long,
                            val recalledSources: List<ArchiveExcerpt> = emptyList(), val archiveMatches: Int = 0)

object MemoryPrompt {
    private fun stateLines(bundle: StoryBundle): List<String> {
        val known=bundle.memory.forCharacter()
        val player=known.firstOrNull {it.entity.kind == EntityKind.PERSON && it.field=="identity"}?.value ?: "Spielerfigur"
        fun name(n:String)=if(n=="Du") "$player (Spielerfigur)" else if(n.lowercase(java.util.Locale.GERMAN) in bundle.memory.characterAliases) bundle.character.name else n.replace("Du und ","$player und ")
        return known.filter {it.entity.kind != EntityKind.EVENT || it.pinned}.map { fact ->
            "${name(fact.entity.name)} · ${fact.label}: ${if(fact.field in setOf("holder","owner")) name(fact.value) else fact.value}" +
                (if(fact.status==FactStatus.HISTORICAL) " (zuletzt beobachtet; aktueller Stand unbekannt)" else "") +
                (if(fact.manual) " (Nutzerkorrektur)" else "")
        }
    }
    fun core(bundle: StoryBundle, adult: Boolean): String = buildString {
        appendLine("Du spielst ${bundle.character.name}; ich spiele die andere Person. Schreibe ausschließlich die nächste Antwort der Figur auf Deutsch.")
        appendLine("Reagiere konkret auf meine letzte Nachricht. Keine Rollenverwechslung. Erzähle ihre Handlungen in dritter Person zwischen Sternchen, ihre Worte in Anführungszeichen. Entscheide nicht für meine Person.")
        appendLine("Der folgende aktuelle Stand hat Vorrang vor historischen Gesprächsrunden und dem Einstieg. Unveränderte Verletzungen bleiben. Abgeschlossene Ziele bleiben abgeschlossen. Derselbe Gegenstand wird durch eine neue Eigenschaft kein zweiter Gegenstand.")
        appendLine("Figurenwissen ist begrenzt: Die Figur und dieser Erzähler dürfen nur mitgeteilte oder beobachtete Informationen verwenden. Unbeobachtete Handlungen geben ihr kein Wissen. Bei unbekannter gemeinsamer Vergangenheit ehrlich nachfragen, keine Erinnerungen erfinden. Fragen und Vermutungen sind keine Ereignisse.")
        appendLine(if(adult) "Erwachsene Figuren, düstere Themen und einvernehmliche Romantik; intime Szenen ausblenden." else "Ohne sexuelle Szenen oder drastische Gewalt.")
        appendLine("\nFIGURENPROFIL (live; spätere Profiländerungen gelten auch in dieser Geschichte):")
        appendLine("Name: ${bundle.character.name}. Rolle: ${bundle.character.role}. Welt: ${bundle.character.genre}.")
        appendLine("Eigenschaften: ${bundle.character.traits}")
        appendLine(CharacterIntroductions.personalityForInference(bundle.character))
        appendLine("\nAKTUELLER STAND AUS FIGURENSICHT:")
        val known = bundle.memory.forCharacter()
        if(known.isEmpty()) appendLine("Noch keine gesicherten laufenden Zustände. Der Einstieg ist die historische Ausgangslage, kein Beleg für späteres Geschehen.")
        stateLines(bundle).forEach {appendLine(it)}
        // The pending last user input is supplied immediately below. Its checkpoint is
        // intentionally delayed until commit; that does not mean older facts are missing.
        val archived=if(bundle.messages.lastOrNull()?.role==ChatRole.USER)bundle.messages.dropLast(1) else bundle.messages
        if(bundle.memory.pendingSources > 0 && archived.any {it.id !in bundle.memory.scannedSources})
            appendLine("Ältere Quellen sind teilweise noch unausgewertet. Die oben angegebenen Zustände sind bereits belegt; andere fehlende Angaben sind unbekannt. Keine Erinnerungen erfinden.")
    }

    /** The binding budget counts the runtime-rendered COMPLETE prompt every time.
     * Required state/pins/latest message are indivisible. Overflow is explicit.
     * History is selected in whole turns, never by characters or message counts. */
    suspend fun plan(bundle: StoryBundle, adult: Boolean, context: Int = 4096, reserve: Int = 512,
                     count: suspend (String, List<ModelMessage>) -> Int): MemoryPromptPlan = planInternal(bundle,adult,context,reserve,null,count)

    suspend fun planWithRecall(bundle: StoryBundle, adult: Boolean, recall: List<ArchiveExcerpt>, context: Int = 4096, reserve: Int = 512,
                             count: suspend (String, List<ModelMessage>) -> Int): MemoryPromptPlan = planInternal(bundle,adult,context,reserve,recall,count)

    private suspend fun planInternal(bundle: StoryBundle, adult: Boolean, context: Int, reserve: Int,
                                     recall: List<ArchiveExcerpt>?, count: suspend (String,List<ModelMessage>) -> Int): MemoryPromptPlan {
        val started=System.nanoTime()
        val source = bundle.messages.filter { it.storyId == bundle.story.id && it.text.isNotBlank() }
        val pending=source.lastOrNull()?.takeIf { it.role == ChatRole.USER } ?: error("Es fehlt die letzte Nachricht.")
        val evidence=RoleEvidence.trusted(bundle)
        var system=core(bundle,adult)
        // Repeat the authoritative state directly before the latest input, using names
        // rather than perspective-dependent 'Du'. Long filler history must not bury it.
        val reminder=stateLines(bundle).takeIf {it.isNotEmpty()}?.joinToString("\n",prefix="AKTUELLES FIGURENWISSEN DER APP (keine neue Handlung):\n",postfix="\n\nNÄCHSTE NUTZERNACHRICHT:\n").orEmpty()
        val last=ModelMessage(true,reminder+MemoryRules.visibleText(pending))
        var history=listOf(last)
        var tokens=count(system,history)
        check(tokens + reserve <= context) { "Das aktuelle Gedächtnis und deine Nachricht passen nicht gemeinsam in das Kontextfenster. Bitte entferne unwichtige angeheftete Notizen oder kürze deine Nachricht. Es wurde nichts gelöscht." }
        // The authored start is historical and optional. Do not reintroduce legacy summaries
        // or excluded/uncertain facts through this secondary channel.
        val excludedSources=bundle.memory.facts.filter { it.status == FactStatus.EXCLUDED }.mapNotNull { it.sourceId }.toSet()
        val background=if(source.firstOrNull()?.id in excludedSources) "" else bundle.story.startContext.ifBlank {bundle.messages.firstOrNull()?.text.orEmpty()}
        val known=bundle.memory.forCharacter().associateBy {it.slot}
        fun conflicts(text:String): Boolean = MemoryRules.proposals(bundle,ChatMessage(storyId=bundle.story.id,role=ChatRole.USER,text=text)).any {p -> known[p.fact.slot]?.let {it.value != p.fact.value} == true}
        val optional=background.split(Regex("\n\\s*\n")).filter {it.isNotBlank() && !conflicts(it)}.map { "\nHISTORISCHER AUSGANGSPUNKT (aktuelle Fakten haben Vorrang):\n$it" } + listOf(
            bundle.memory.overview.takeIf { it.isNotBlank() }?.let { "\nHANDLUNGSÜBERBLICK:\n$it" }).filterNotNull()
        val omitted=mutableListOf<String>()
        val turns=mutableListOf<MutableList<ChatMessage>>()
        for(message in source.dropLast(1)) {
            if(turns.isEmpty() || message.role == ChatRole.USER) turns+=mutableListOf(message) else turns.last()+=message
        }
        val nearest=turns.lastOrNull()?.takeIf {it.first().role==ChatRole.USER && it.any {m -> m.role==ChatRole.CHARACTER} && it.none {m -> m.id in excludedSources}}
        if(nearest!=null) {
            val candidate=nearest.map {ModelMessage(it.role==ChatRole.USER,RoleSourcePolicy.visible(bundle,it,evidence))}+history
            val n=count(system,candidate)
            if(n+reserve<=context) {history=candidate;tokens=n}
        }
        val included=if(history.size>1) nearest.orEmpty().map {it.id}.toSet() else emptySet()
        val matches=(recall ?: ArchiveRecall.search(bundle,pending.text,evidence=evidence))
            .filter {it.message.id !in included && ArchiveRecall.eligible(bundle,it,pending.text,evidence)}
            .map {ArchiveRecall.withContext(bundle,it,pending.text,evidence)}.distinctBy {HybridRecall.key(it)}
        val recalled=mutableListOf<ArchiveExcerpt>()
        val base=system
        for(excerpt in matches) {
            if(recalled.size>=4) break
            // Named rendering improved manual controls, but the automatic variants did
            // not reliably answer the decisive letter question. Keep original excerpts.
            val candidate=base+ArchiveRecall.render(bundle,recalled+excerpt,evidence=evidence)
            val n=count(candidate,history)
            if(n+reserve<=context) {recalled+=excerpt;system=candidate;tokens=n}
        }
        check(matches.isEmpty() || recalled.isNotEmpty()) { "Eine passende ältere Originalstelle wurde gefunden, passt aber zusammen mit dem Pflichtstand nicht in das Kontextfenster. Bitte grenze deine Frage ein oder entferne unwichtige angeheftete Notizen. Dein Entwurf bleibt erhalten." }
        var full=false
        val seenTurns=mutableSetOf<String>()
        if(included.isNotEmpty()) seenTurns+=MemoryRules.digest(nearest!!.joinToString("\u0000") {it.role.name+"\u0001"+it.text})
        for(turn in turns.asReversed()) {
            if(turn.any {it.id in included}) continue
            val outdatedOpening=turn.size==1 && turn.single()==source.first() && turn.single().role==ChatRole.CHARACTER && conflicts(turn.single().text)
            val duplicate=!seenTurns.add(MemoryRules.digest(turn.joinToString("\u0000") {it.role.name+"\u0001"+it.text}))
            if(full || duplicate || outdatedOpening || turn.any { it.id in excludedSources }) {omitted+=turn.map { it.id };continue}
            val candidate=turn.map { ModelMessage(it.role == ChatRole.USER,RoleSourcePolicy.visible(bundle,it,evidence)) } + history
            // Every Gemma/Qwen formatter must get a USER before an authored opening.
            val formatted=if(!candidate.first().user) listOf(ModelMessage(true,"Setze die verfasste Ausgangsszene fort."))+candidate else candidate
            val n=count(system,formatted)
            if(n+reserve<=context) {history=formatted;tokens=n} else {
                // The long authored opening remains whole in archive AND source scanning.
                // Only complete final paragraphs are used as the optional inference excerpt.
                if(turn.size == 1 && turn.single() == source.first() && turn.single().role == ChatRole.CHARACTER) {
                    var tail=""
                    for(paragraph in turn.single().text.split(Regex("\n\\s*\n")).asReversed()) {
                        val next=paragraph + if(tail.isEmpty()) "" else "\n\n$tail"
                        val suffix=listOf(ModelMessage(true,"Verfasster Szenenauftakt; im Verlauf gespeichert."),ModelMessage(false,next))+history
                        val size=count(system,suffix)
                        if(size+reserve>context) break
                        tail=next;tokens=size
                    }
                    if(tail.isNotEmpty()) history=listOf(ModelMessage(true,"Verfasster Szenenauftakt; im Verlauf gespeichert."),ModelMessage(false,tail))+history
                }
                full=true;omitted+=turn.map { it.id }
            }
        }
        for(section in optional) {
            val candidate=system+section;val n=count(candidate,history)
            if(n+reserve<=context) {system=candidate;tokens=n}
        }
        // Ordinary unpinned memories are optional; current state was already mandatory.
        val words=Regex("[\\p{L}]{4,}").findAll(pending.text.lowercase()).map {it.value}.toSet() - setOf("eine","einen","einer","dieser","diese","dieses","nicht","noch","aber","dann","jetzt","habe","haben","wird","sind","auch","hier","dort")
        val memories=bundle.memory.forCharacter().filter { it.entity.kind == EntityKind.EVENT && !it.pinned }
            .sortedWith(compareByDescending<StateFact> {f -> words.count {word -> "${f.entity.name} ${f.value}".contains(word,true)} }.thenByDescending {it.createdAt})
        for(fact in memories) {
            val candidate=system+"\nRelevante Notiz: ${fact.value}"
            val n=count(candidate,history);if(n+reserve<=context){system=candidate;tokens=n}
        }
        return MemoryPromptPlan(system,history,tokens,reserve,omitted,(System.nanoTime()-started)/1_000_000,recalled,matches.size)
    }
}
