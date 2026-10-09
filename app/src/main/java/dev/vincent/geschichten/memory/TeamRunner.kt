package dev.vincent.geschichten.memory

import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.StoryBundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

enum class TeamRole { HELPER, NARRATOR }
interface TeamPort {
    /** Finish native work and unload the other text model before loading this one. */
    suspend fun activate(role:TeamRole)
    suspend fun count(system:String,history:List<ModelMessage>):Int
    suspend fun generate(system:String,history:List<ModelMessage>,onToken:(String)->Unit):String
}
data class TeamCall(val task:String,val loadMs:Long,val planningMs:Long,val firstInternalTextMs:Long?,val totalMs:Long,val inputTokens:Int)
data class TeamOutcome(val reply:String,val proposals:List<StateFact>,val recall:List<ArchiveExcerpt>,val work:TeamWork,
                       val interpretation:TeamInterpretation,val calls:List<TeamCall>,val repaired:Boolean,val fallback:Boolean,val notice:String?)

/** Bounded three/five inference calls, provisional facts only, no JSON retries. */
object TeamRunner {
    suspend fun run(bundle:StoryBundle,request:String,adult:Boolean,recall:List<ArchiveExcerpt>,port:TeamPort,
                    beforeFacts:Boolean=true,status:(String)->Unit={}):TeamOutcome {
        val work=TeamProtocol.work(bundle,request,recall)
        val calls=mutableListOf<TeamCall>();var fallback=false;var notice:String?=null
        suspend fun helper(task:String,reply:String?):Pair<String,TeamWork> {
            currentCoroutineContext().ensureActive();status(if(reply==null) "Fakten-KI wertet Angaben aus …" else "Fakten-KI prüft den Entwurf …")
            val begin=System.nanoTime();port.activate(TeamRole.HELPER);val loaded=System.nanoTime()
            val p=TeamProtocol.prompt(work,reply,port::count);val planned=System.nanoTime();var first:Long?=null
            val text=port.generate(p.system,p.history){if(first==null && it.isNotBlank())first=(System.nanoTime()-planned)/1_000_000}
            calls+=TeamCall(task,(loaded-begin)/1_000_000,(planned-loaded)/1_000_000,first,(System.nanoTime()-begin)/1_000_000,p.tokens)
            return text to p.supplied
        }
        var interpretation=TeamInterpretation()
        if(beforeFacts)try {val h=helper("facts",null);interpretation=TeamProtocol.interpret(bundle,h.second,h.first)}
        catch(c:CancellationException){throw c}
        catch(e:Exception){fallback=true;notice="Faktenauswertung nicht verfügbar. Der bisherige Ablauf bleibt aktiv."}
        suspend fun narrate(repair:TeamReview?=null):Pair<String,MemoryPromptPlan> {
            currentCoroutineContext().ensureActive();status(if(repair==null) "Deine Figur schreibt einen Entwurf …" else "Deine Figur verbessert den Entwurf einmal …")
            val begin=System.nanoTime();port.activate(TeamRole.NARRATOR);val loaded=System.nanoTime()
            val supplement=TeamProtocol.narratorCue(interpretation)+if(repair==null) "" else "\n"+TeamProtocol.repair(work,repair)
            val plan=MemoryPrompt.planWithRecall(bundle,adult,recall,count={s,h ->port.count(s+supplement,h)})
            val planned=System.nanoTime();var first:Long?=null
            val text=port.generate(plan.system+supplement,plan.history){if(first==null && it.isNotBlank())first=(System.nanoTime()-planned)/1_000_000}
            calls+=TeamCall(if(repair==null)"narrator" else "repair",(loaded-begin)/1_000_000,(planned-loaded)/1_000_000,first,(System.nanoTime()-begin)/1_000_000,plan.inputTokens)
            return text to plan
        }
        fun deterministic(text:String,sources:List<ArchiveExcerpt>):String?=try {
            StoryReplyValidation.requireStoryReply(text,work.question);MemoryReplyGuard.validate(bundle,text,sources);TeamProtocol.privateConflict(bundle,text)
        } catch(e:Exception){e.message ?: "Bestätigter Fehler im Entwurf."}
        var (reply,plan)=narrate()
        var det=deterministic(reply,plan.recalledSources)
        fun combined(r:TeamReview,afterRepair:Boolean=false):TeamReview {
            val conflicts=(r.conflicts+listOfNotNull(det)+if(afterRepair)TeamProtocol.confirmedConflicts(bundle,work,reply) else emptyList()).distinct()
            return r.copy(conflicts=conflicts,verdict=if(conflicts.isNotEmpty())"conflict" else r.verdict)
        }
        var review=try {val h=helper("review",reply);combined(TeamProtocol.review(bundle,h.second,reply,h.first))}
        catch(c:CancellationException){throw c}
        catch(e:Exception) {
            fallback=true;notice="Hilfsprüfung nicht verfügbar. Antwort durch den bisherigen Schutzablauf geprüft."
            TeamReview(listOfNotNull(det),emptyList(),if(det!=null)"conflict" else "uncertain")
        }
        val repaired=review.conflicts.isNotEmpty()
        if(repaired) {
            val improved=narrate(review);reply=improved.first;plan=improved.second
            det=deterministic(reply,plan.recalledSources)
            review=try {val h=helper("review-repair",reply);combined(TeamProtocol.review(bundle,h.second,reply,h.first),true)}
            catch(c:CancellationException){throw c}
            catch(e:Exception){throw IllegalStateException("Die Reparatur konnte technisch nicht vollständig geprüft werden. Deine Nachricht bleibt erhalten.",e)}
            check(review.conflicts.isEmpty()){ "Auch der einmal verbesserte Entwurf enthält einen bestätigten Fehler. Deine Nachricht bleibt erhalten." }
        }
        currentCoroutineContext().ensureActive()
        if(review.verdict=="uncertain" && !fallback)notice="Die zusätzliche Prüfung ist teilweise unsicher. Es wurde kein weiterer belegter Konflikt bestätigt."
        return TeamOutcome(reply,interpretation.proposals,plan.recalledSources,work,interpretation,calls,repaired,fallback,notice)
    }
}
