package dev.vincent.geschichten.ai

import dev.vincent.geschichten.memory.TeamPort
import dev.vincent.geschichten.memory.TeamRole

/** Two independent selections, shared immutable files, at most one loaded text engine.
 * Engine.unload waits for the operation lock; native handles never close during work. */
class TeamEnginePort(private val narrator:LocalModelEngine,private val helper:LocalModelEngine):TeamPort {
    private var active:LocalModelEngine?=null
    override suspend fun activate(role:TeamRole) {
        active=null
        if(role==TeamRole.HELPER){narrator.unload();helper.loadModel();active=helper}
        else {helper.unload();narrator.loadModel();active=narrator}
        checkNotNull(active).clearConversationCache()
    }
    override suspend fun count(system:String,history:List<ModelMessage>)=checkNotNull(active).countPrompt(system,history)
    override suspend fun generate(system:String,history:List<ModelMessage>,onToken:(String)->Unit)=checkNotNull(active).generate(system,history,true,onToken)
    suspend fun release() {active=null;helper.unload();narrator.unload()}
    suspend fun restoreNarrator() {helper.unload();narrator.loadModel()}
    fun cancel() {helper.cancelGeneration();narrator.cancelGeneration()}
}
