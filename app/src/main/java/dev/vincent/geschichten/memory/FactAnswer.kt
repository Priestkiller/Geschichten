package dev.vincent.geschichten.memory

import dev.vincent.geschichten.data.StoryBundle
import java.util.Locale

/** Short, source-bound current-state answers. No model-generated paraphrase or event inference.
 * Null always means ordinary generation, including unknown, historical and ambiguous facts. */
data class FactAnswer(val text: String, val stateVersion: Long, val facts: List<StateFact>, val intent: String)

object FactAnswers {
    private fun normalized(text:String)=text.trim().lowercase(Locale.GERMAN).replace(Regex("\\s+")," ")
    private val nonCurrent=Regex("(?i)\\b(wenn|falls|würde|wäre|hätte|könnte|vielleicht|angenommen|gestern|damals|früher|warum|wieso|weshalb)\\b")
    private val timing="(?:\\s+(?:jetzt|aktuell|inzwischen|gerade))?"
    private val genders=mapOf("Ring" to "der", "Brief" to "der", "Schlüssel" to "der", "Dolch" to "der", "Kristall" to "der", "Trank" to "der", "Anhänger" to "der", "Schwert" to "das", "Buch" to "das", "Gerät" to "das", "Amulett" to "das", "Artefakt" to "das", "Messer" to "das", "Gewehr" to "das", "Karte" to "die", "Tasche" to "die", "Münze" to "die", "Kette" to "die", "Schriftrolle" to "die", "Pistole" to "die")
    private fun noun(entity:MemoryEntity):String {
        val name=entity.name
        val article=genders.entries.firstOrNull {it.key.equals(name,true)}?.value
        return if(article!=null) "$article $name" else "»$name«"
    }
    fun resolve(bundle:StoryBundle, question:String=bundle.messages.lastOrNull()?.text.orEmpty()):FactAnswer? {
        val q=question.trim()
        if(q.count {it=='?'}!=1 || !q.endsWith('?') || q.length>180 || nonCurrent.containsMatchIn(q) || MemoryRules.privateAction(q))return null
        // Anchored syntax deliberately excludes additional actions, feelings, requests and instructions.
        val known=bundle.memory.forCharacter().filter {f ->
            f.status==FactStatus.CURRENT && f.storyId==bundle.story.id && f.entity.storyId==bundle.story.id &&
                f.value.length in 1..120 && !f.value.contains(Regex("[\n\r„“\"]")) &&
                f.entity.name.length in 1..100 && !f.entity.name.contains(Regex("[\n\r„“\"]")) &&
                (f.manual || bundle.messages.any {it.id==f.sourceId && it.storyId==bundle.story.id && f.sourceText.isNotBlank() && it.text.contains(f.sourceText)})
        }
        fun result(sentence:String,facts:List<StateFact>,intent:String)=FactAnswer("„$sentence“",bundle.memory.version,facts,intent)
        fun actor(value:String,possessive:Boolean=false):String = when {
            value=="Du" -> if(possessive) "dir" else "Du"
            value.equals(bundle.character.name,true) || value.lowercase(Locale.GERMAN) in bundle.memory.characterAliases -> if(possessive) "mir" else "Ich"
            else -> value
        }
        fun item(reference:String):MemoryEntity? {
            val r=normalized(reference).replace(Regex("^(?:der|die|das|den|dieser|diese|dieses|diesen)\\s+"),"")
            // An unqualified base noun must not choose between two distinct coloured objects.
            return known.map {it.entity}.distinctBy {it.id}.filter {it.kind==EntityKind.ITEM &&
                (normalized(it.name)==r || normalized(it.name).endsWith(" $r"))}.singleOrNull()
        }
        val objectQuestions=listOf("holder" to "wer (?:trägt|hält|hat) (.+?)$timing\\?", "owner" to "wem gehört (.+?)$timing\\?", "placement" to "wo (?:liegt|ist|befindet sich) (.+?)$timing\\?")
        for((field,pattern) in objectQuestions) {
            val match=Regex(pattern,RegexOption.IGNORE_CASE).matchEntire(q) ?: continue
            val entity=item(match.groupValues[1]) ?: return null
            val fact=known.singleOrNull {it.entity.id==entity.id && it.field==field && !RoleEvidence.legacyOwnership(it)} ?: return null
            val n=noun(entity)
            val sentence=when(field) {
                "owner" -> if(fact.value==GroundedFacts.NO_HOLDER || fact.value==GroundedFacts.NOT_PLACED)return null else "${n.replaceFirstChar {it.uppercase()}} gehört ${actor(fact.value,true)}."
                "holder" -> if(fact.value==GroundedFacts.NO_HOLDER) "Niemand hält ${if(n.startsWith("der "))n.replaceFirst("der ","den ") else n} gerade." else {
                    val person=actor(fact.value)
                    "$person ${if(person=="Ich") "halte" else if(person=="Du") "hältst" else "hält"} ${if(n.startsWith("der "))n.replaceFirst("der ","den ") else n}."
                }
                else -> {
                    if(fact.value==GroundedFacts.NOT_PLACED)return null
                    if(!Regex("(?i)^(?:auf|unter|hinter|neben|vor|in|im|am|über)\\s+[\\p{L} ’'-]+$").matches(fact.value))return null
                    "${n.replaceFirstChar {it.uppercase()}} liegt ${fact.value}."
                }
            }
            return result(sentence,listOf(fact),field)
        }
        Regex("ist (?:das )?ziel (.+?) (?:bereits |schon )?(?:abgeschlossen|erledigt)\\?",RegexOption.IGNORE_CASE).matchEntire(q)?.let {m ->
            val goal=known.filter {it.entity.kind==EntityKind.GOAL && it.field=="status" && normalized(it.entity.name)==normalized(m.groupValues[1].trim('„','“','"','»','«'))}.singleOrNull() ?: return null
            return when(goal.value) {
                "Abgeschlossen" -> result("Ja, das Ziel »${goal.entity.name}« ist abgeschlossen.",listOf(goal),"goal")
                "Offen" -> result("Nein, das Ziel »${goal.entity.name}« ist noch offen.",listOf(goal),"goal")
                else -> null
            }
        }
        val injury=Regex("welche person (?:ist verletzt|hat eine (gebrochene|verletzte|verwundete) (linke|rechte) (hand|schulter|arm|bein))\\?",RegexOption.IGNORE_CASE).matchEntire(q) ?: return null
        val injuries=known.filter {f -> f.entity.kind==EntityKind.PERSON && f.field.startsWith("injury:") &&
            f.value.substringAfter(':').trim() in setOf("verletzt","gebrochen","verwundet") &&
            (injury.groupValues[1].isEmpty() || normalized(f.field.substringAfter(':'))==normalized("${injury.groupValues[2]} ${injury.groupValues[3]}") && f.value.substringAfter(':').trim()==injury.groupValues[1].dropLast(1))}
        if(injuries.isEmpty() || injuries.any {it.field=="injury:allgemein"})return null
        val sentences=injuries.map {f ->
            val words=f.value.substringBefore(':').trim().split(' ')
            val body=words.dropLast(1).joinToString(" ",postfix=if(words.size>1) " " else "")+words.last().replaceFirstChar {it.uppercase()}
            val state=f.value.substringAfter(':').trim()
            val feminine=words.last().lowercase(Locale.GERMAN) in setOf("hand","schulter")
            val article=if(feminine) "Die" else if(words.last().equals("Bein",true)) "Das" else "Der"
            val possessor=when(actor(f.entity.name)) {"Ich"->if(feminine) "Meine" else "Mein";"Du"->if(feminine) "Deine" else "Dein";else->article}
            val person=if(possessor==article) " von ${actor(f.entity.name)}" else ""
            "$possessor $body$person ist $state."
        }.distinct()
        return result(sentences.joinToString(" "),injuries,"injury")
    }
}
