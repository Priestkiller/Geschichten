package dev.vincent.geschichten.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.vincent.geschichten.data.StoryBundle
import dev.vincent.geschichten.memory.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun StateMemoryCard(fact: StateFact, bundle: StoryBundle, onEdit: () -> Unit) {
    Surface(onClick=onEdit,color=Panel,shape=CardShape,modifier=Modifier.fillMaxWidth().padding(horizontal=18.dp).testTag("state_memory_${fact.id}")) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("${fact.entity.name} · ${fact.label}",style=MaterialTheme.typography.titleMedium,color=Gold)
            Text(fact.value,color=Paper)
            Text(when(fact.status) {FactStatus.CURRENT -> "Aktuell";FactStatus.HISTORICAL -> "Historisch – durch neueren Stand ersetzt";FactStatus.UNCERTAIN -> "Unsicher – wird nicht als Fakt abgerufen";FactStatus.EXCLUDED -> "Ausgeschlossen – wird nicht abgerufen" } + if(fact.pinned) " · Angeheftet" else "",color=Soft,style=MaterialTheme.typography.bodySmall)
            val known=bundle.memory.knowledge.filter { it.factId == fact.id }.map { it.knower }
            Text(if("character" in known) "${bundle.character.name} kennt diese Angabe." else "${bundle.character.name} wurde diese Angabe nicht mitgeteilt.",color=Muted,style=MaterialTheme.typography.bodySmall)
            Text(if(fact.manual) "Quelle: eigene Korrektur / Notiz" else if(fact.sourceId == null) "Quelle: übernommene Notiz" else "Quelle: Chatnachricht vom ${SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.GERMAN).format(Date(fact.createdAt))}",color=Muted,style=MaterialTheme.typography.bodySmall)
            Text("Ansehen und bearbeiten",color=Gold,style=MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun StateMemoryEditor(fact: StateFact, state: AppUiState, actions: AppActions, onDismiss: () -> Unit) {
    var value by rememberSaveable(fact.id) {mutableStateOf(fact.value)}
    var pinned by rememberSaveable(fact.id) {mutableStateOf(fact.pinned)}
    var known by rememberSaveable(fact.id) {mutableStateOf(state.current?.memory?.knowledge?.any {it.factId == fact.id && it.knower == "character"} == true)}
    var error by remember {mutableStateOf<String?>(null)}
    var submittedAt by remember {mutableStateOf<Long?>(null)}
    var deleting by remember {mutableStateOf(false)}
    LaunchedEffect(state.current?.memory?.version) {if(submittedAt != null && state.current?.memory?.version != submittedAt) onDismiss()}
    LaunchedEffect(state.notice) {if(submittedAt != null && state.notice != null) {error=state.notice;submittedAt=null}}
    MemoryDialog("${fact.entity.name}: ${fact.label}", "state_memory_editor", onDismiss,
        body={
            Text("Diese Korrektur gilt ab der nächsten Antwort. Der Verlauf und frühere Fassungen bleiben erhalten.",color=Muted)
            if(fact.field == "status") {
                Row {TextButton(onClick={value="Offen"}){Text("Offen")};TextButton(onClick={value="Abgeschlossen"}){Text("Abgeschlossen")}}
            }
            StoryTextField(value,{value=it},"Aktueller Wert",Modifier.testTag("state_memory_value"),minLines=2,maxLines=6)
            Row {Checkbox(pinned,{pinned=it});Text("Anheften: stets berücksichtigen; spätere gültige Änderungen bleiben möglich.",Modifier.weight(1f),color=Muted)}
            Row {Checkbox(known,{known=it});Text("Die Figur kennt diesen Stand. Ohne Haken erfährt sie die Korrektur nicht automatisch.",Modifier.weight(1f),color=Muted)}
            HorizontalDivider(color=Line)
            Text("Herkunft",color=Gold)
            Text(fact.sourceText.ifBlank {"Quelle nicht mehr verfügbar."},color=Paper)
            state.current?.messages?.firstOrNull {it.id == fact.sourceId}?.let {source ->
                Text("Originalnachricht",color=Gold);Text(source.text,color=Muted)
            }
            TextButton(onClick={deleting=true}){Text("Vom Gedächtnis ausschließen",color=Danger)}
            if(deleting) {
                Text("Die Chatnachricht bleibt im Archiv. Diese Quellen werden den Eintrag nicht erneut anlegen; spätere neue Ereignisse bleiben möglich.",color=Muted)
                TextButton(onClick={ if(!state.busy && !state.memoryBusy) {submittedAt=state.current?.memory?.version;actions.editStateFact(fact.storyId,fact.id,value,pinned,true)} }){Text("Ausschließen bestätigen",color=Danger)}
            }
            if(error != null) Text(error!!,color=Danger)
        },
        confirmButton={TextButton(onClick={if(value.isBlank()) error="Bitte einen Wert eintragen." else {submittedAt=state.current?.memory?.version;actions.dismissNotice();actions.editStateFact(fact.storyId,fact.id,value,pinned,known=known)}},enabled=!state.busy && !state.memoryBusy && submittedAt == null,modifier=Modifier.testTag("save_state_memory")){Text("Speichern")}},
        dismissButton={TextButton(onClick=onDismiss){Text("Schließen")}})
}

@Composable
internal fun StateMemoryCreation(storyId: String, state: AppUiState, actions: AppActions, onDismiss: () -> Unit) {
    var name by rememberSaveable {mutableStateOf("")};var value by rememberSaveable {mutableStateOf("")}
    var selected by rememberSaveable {mutableStateOf(0)};var expanded by remember {mutableStateOf(false)}
    var known by rememberSaveable {mutableStateOf(true)};var error by remember {mutableStateOf<String?>(null)}
    var submittedAt by remember {mutableStateOf<Long?>(null)}
    LaunchedEffect(state.current?.memory?.version) {if(submittedAt != null && state.current?.memory?.version != submittedAt) onDismiss()}
    LaunchedEffect(state.notice) {if(submittedAt != null && state.notice != null) {error=state.notice;submittedAt=null}}
    val choices=listOf(
        Triple("Gegenstand: Träger",EntityKind.ITEM,"holder"),Triple("Gegenstand: Ablage (auf, in, unter …)",EntityKind.ITEM,"placement"),Triple("Gegenstand: Eigentümer",EntityKind.ITEM,"owner"),Triple("Gegenstand: Farbe",EntityKind.ITEM,"color"),
        Triple("Person: Aufenthaltsort",EntityKind.PERSON,"location"),Triple("Person: Verletzung",EntityKind.PERSON,"injury"),
        Triple("Ziel: Offen oder abgeschlossen",EntityKind.GOAL,"status"),Triple("Beziehung: Versprechen",EntityKind.RELATIONSHIP,"promise"),
        Triple("Beziehung: Vertrauen",EntityKind.RELATIONSHIP,"trust"),Triple("Beziehung: Konflikt",EntityKind.RELATIONSHIP,"conflict"),
        Triple("Wichtige Entscheidung oder Erlebnis",EntityKind.EVENT,"event"),Triple("Offene Frage",EntityKind.EVENT,"question"))
    MemoryDialog("Zustand festhalten", "state_memory_creation", onDismiss,
        body={
            Text("Für dieselbe Person oder denselben Gegenstand immer denselben Namen wählen. Bestehende Angaben kannst du direkt auf ihrer Karte korrigieren.",color=Muted)
            Box {TextButton(onClick={expanded=true}){Text(choices[selected].first)}
                DropdownMenu(expanded,{expanded=false}){choices.forEachIndexed {i,c->DropdownMenuItem(text={Text(c.first)},onClick={selected=i;expanded=false})}}}
            StoryTextField(name,{name=it},"Name",Modifier.testTag("new_memory_name"),minLines=1,maxLines=2)
            StoryTextField(value,{value=it},"Wert mit Begründung",Modifier.testTag("new_memory_value"),minLines=2,maxLines=5)
            if(choices[selected].third == "holder") Text("Name der tragenden Person oder Niemand. Die Ablage wird getrennt festgehalten.",color=Muted)
            if(choices[selected].third == "placement") Text("Mit räumlicher Beziehung, zum Beispiel auf der Kommode oder unter dem Bett.",color=Muted)
            if(choices[selected].third == "injury") Text("Körperstelle und Zustand, zum Beispiel linke Hand: verletzt. So bleiben mehrere Verletzungen erhalten.",color=Muted)
            if(choices[selected].third == "status") Row {TextButton(onClick={value="Offen"}){Text("Offen")};TextButton(onClick={value="Abgeschlossen"}){Text("Abgeschlossen")}}
            Row {Checkbox(known,{known=it});Text("Die Figur weiß davon. Ohne Haken kennt zunächst nur deine Person den Fakt.",Modifier.weight(1f),color=Muted)}
            if(error!=null) Text(error!!,color=Danger)
        },confirmButton={TextButton(onClick={if(name.isBlank() || value.isBlank()) error="Name und Wert eintragen." else {
            submittedAt=state.current?.memory?.version;actions.dismissNotice()
            actions.addStateFact(storyId,choices[selected].second,name,choices[selected].third,value,known)
        }},enabled=!state.busy && !state.memoryBusy && submittedAt == null,modifier=Modifier.testTag("save_new_memory")){Text("Speichern")}},dismissButton={TextButton(onClick=onDismiss){Text("Abbrechen")}})
}

/** Explicit bounded layout: long provenance scrolls independently of the action buttons. */
@Composable
private fun MemoryDialog(title: String, tag: String, onDismiss: () -> Unit,
                         body: @Composable ColumnScope.() -> Unit,
                         confirmButton: @Composable () -> Unit, dismissButton: @Composable () -> Unit) {
    val height = (LocalConfiguration.current.screenHeightDp * .82f).dp
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(color=Panel,shape=CardShape,modifier=Modifier.fillMaxWidth().padding(18.dp).height(height).testTag(tag)) {
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(title,style=MaterialTheme.typography.titleLarge,color=Gold)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp),content=body)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {dismissButton();confirmButton()}
            }
        }
    }
}
