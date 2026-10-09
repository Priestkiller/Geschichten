package dev.vincent.geschichten.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.memory.*

@Composable
internal fun MemoriesScreen(state: AppUiState, actions: AppActions) {
    val bundle = state.current
    var editedFactId by rememberSaveable(bundle?.story?.id) { mutableStateOf<String?>(null) }
    var showPast by rememberSaveable(bundle?.story?.id) { mutableStateOf(false) }
    var addState by rememberSaveable(bundle?.story?.id) { mutableStateOf(false) }
    var editedEntry by rememberSaveable(bundle?.story?.id, stateSaver = MemoryDraftSaver) { mutableStateOf<MemoryEntry?>(null) }
    var pendingDelete by rememberSaveable(stateSaver = MemoryDraftSaver) { mutableStateOf<MemoryEntry?>(null) }
    // An in-flight operation must not become a permanently disabled button after process death.
    var pendingSave by remember(bundle?.story?.id) { mutableStateOf<MemoryEntry?>(null) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(bundle?.memories, pendingSave) {
        pendingSave?.let { saved ->
            if (bundle?.memories?.any { it.id == saved.id && it.kind == saved.kind && it.text == saved.text && it.pinned == saved.pinned } == true) {
                editedEntry = null
                pendingSave = null
                saveError = null
            }
        }
    }
    LaunchedEffect(state.notice, pendingSave) {
        if (state.notice != null && pendingSave != null) {
            saveError = state.notice
            pendingSave = null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("memories_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader("Gedächtnis", model = state.model, onSetup = { actions.navigate(AppScreen.SETUP) })
        }
        if (bundle == null) {
            item {
                EmptyStoryState(
                    Icons.Outlined.Description,
                    "Was bleibt, gehört hierher",
                    "Öffne eine Geschichte, um gemeinsame Erlebnisse und wichtige Details festzuhalten.",
                    if (state.stories.isNotEmpty()) "Geschichte auswählen" else "Figuren entdecken",
                    { actions.navigate(if (state.stories.isNotEmpty()) AppScreen.STORIES else AppScreen.CHARACTERS) },
                )
            }
        } else {
            if (state.memoryBusy) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CircularProgressIndicator(Modifier.size(14.dp), color = Gold, strokeWidth = 1.5.dp)
                        Text("Erinnerungen werden aktualisiert …", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    CharacterAvatar(bundle.character.avatarKey, bundle.character.name, Modifier.size(68.dp))
                    Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(bundle.character.name, style = MaterialTheme.typography.titleLarge)
                        Text(bundle.story.title, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = actions::exportCurrentStory, modifier = Modifier.testTag("export_story")) {
                        Icon(Icons.Outlined.FileUpload, "Geschichte exportieren", tint = Gold)
                    }
                }
            }
            item {
                Text(
                    "Hier stehen der aktuelle Stand, Beziehungen und Ziele dieser Geschichte. Korrekturen gelten für die nächste Antwort. Das Figurenprofil gilt auch nach einer Profiländerung; der ursprüngliche Einstieg bleibt erhalten.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 3.dp),
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if(bundle.memory.pendingSources > 0) item {
                Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text("${bundle.memory.pendingSources} ältere Nachrichten sind noch nicht ausgewertet. Unklare Angaben bleiben unbekannt.",color=Muted)
                    TextButton(onClick=actions::processMemoryBatch,enabled=!state.busy && !state.memoryBusy){Text("Weitere Nachrichten auswerten")}
                }
            }
            val facts=bundle.memory.facts.filter {showPast || it.status == FactStatus.CURRENT || it.status == FactStatus.UNCERTAIN}
            EntityKind.entries.forEach { kind ->
                val group=facts.filter {it.entity.kind == kind}
                if(group.isNotEmpty()) {
                    item {Text(when(kind) {EntityKind.PERSON,EntityKind.SCENE -> "Szenenstand";EntityKind.ITEM -> "Gegenstände";EntityKind.GOAL -> "Offene und abgeschlossene Ziele";EntityKind.RELATIONSHIP -> "Beziehungen";EntityKind.EVENT -> "Wichtige Erlebnisse"},Modifier.padding(horizontal=20.dp,vertical=8.dp),color=Gold)}
                    items(group,key={"state_${it.id}"}) {fact -> StateMemoryCard(fact,bundle) {editedFactId=fact.id}}
                }
            }
            item {TextButton(onClick={showPast=!showPast},modifier=Modifier.padding(horizontal=18.dp)){Text(if(showPast) "Historische / ausgeschlossene Angaben ausblenden" else "Historische / ausgeschlossene Angaben ansehen")}}
            item {GoldButton("Zustand festhalten",{addState=true},Modifier.fillMaxWidth().padding(horizontal=18.dp),tag="add_state")}
            item {Text("Eigene und übernommene Notizen",Modifier.padding(horizontal=20.dp),color=Gold)}
            MemoryKind.entries.forEach { kind ->
                val entries = bundle.memories.filter { it.kind == kind }
                if (entries.isEmpty()) {
                    item(key = "empty_${kind.name}") {
                        EmptyMemoryCard(kind) { editedEntry = MemoryEntry(storyId = bundle.story.id, kind = kind, text = "") }
                    }
                } else {
                    items(entries, key = { it.id }) { entry -> MemoryCard(entry) { editedEntry = entry } }
                }
            }
            if (bundle.memories.any { it.id == "auto-summary-${bundle.story.id}" }) {
                item {
                    Text(
                        "Frühere KI-Zusammenfassungen bleiben zur Prüfung erhalten. Sie werden nicht automatisch als gesicherter aktueller Stand verwendet. Anheften blockiert keine weiteren Aktualisierungen.",
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            item {
                GoldButton(
                    "Erinnerung hinzufügen",
                    { editedEntry = MemoryEntry(storyId = bundle.story.id, kind = MemoryKind.FACT, text = "") },
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp),
                    icon = Icons.Outlined.Add,
                    tag = "add_memory",
                )
            }
            item {
                QuietButton("Zur Geschichte", { actions.navigate(AppScreen.CHAT) }, Modifier.fillMaxWidth().padding(horizontal = 18.dp), Icons.Outlined.ChatBubbleOutline)
            }
        }
    }

    bundle?.memory?.facts?.firstOrNull {it.id == editedFactId}?.let {fact -> StateMemoryEditor(fact,state,actions){editedFactId=null}}
    if(addState && bundle != null) StateMemoryCreation(bundle.story.id,state,actions){addState=false}

    editedEntry?.let { entry ->
        MemoryEditorDialog(
            entry,
            isExisting = bundle?.memories?.any { it.id == entry.id } == true,
            isSaving = pendingSave != null,
            saveError = saveError,
            onDismiss = { if (pendingSave == null) { editedEntry = null; saveError = null } },
            onSave = {
                if (state.busy || state.memoryBusy) {
                    saveError = "Warte kurz, bis die Antwort und ihre Erinnerungen fertig sind. Dein Entwurf bleibt erhalten."
                } else {
                    saveError = null
                    actions.dismissNotice()
                    pendingSave = it
                    actions.saveMemory(it)
                }
            },
            onDelete = { editedEntry = null; pendingDelete = entry },
        )
    }
    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Erinnerung löschen?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Dieser Eintrag wird aus der Geschichte entfernt. Die Chatnachrichten bleiben erhalten.") },
            confirmButton = {
                TextButton(onClick = { actions.deleteMemory(entry); pendingDelete = null }, modifier = Modifier.testTag("confirm_delete_memory")) { Text("Löschen", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Behalten") } },
            containerColor = Panel,
        )
    }
}

@Composable
private fun MemoryCard(entry: MemoryEntry, onEdit: () -> Unit) {
    Surface(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).testTag("memory_${entry.id}"),
        color = Panel,
        shape = CardShape,
        border = BorderStroke(1.dp, Line),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, top = 19.dp, end = 7.dp, bottom = 19.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(memoryIcon(entry.kind), null, Modifier.size(27.dp), tint = Gold)
            Column(Modifier.weight(1f).padding(start = 16.dp, end = 5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(entry.kind.title, style = MaterialTheme.typography.titleLarge)
                Text(entry.text, color = Paper, style = MaterialTheme.typography.bodyLarge)
                if (entry.pinned) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.PushPin, null, Modifier.size(12.dp), tint = Soft)
                        Text("Angeheftet", color = Soft, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "${entry.kind.title} bearbeiten", Modifier.size(20.dp), tint = Gold) }
        }
    }
}

@Composable
private fun EmptyMemoryCard(kind: MemoryKind, onAdd: () -> Unit) {
    Surface(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).testTag("empty_memory_${kind.name.lowercase()}"),
        color = Panel.copy(alpha = .55f),
        shape = CardShape,
        border = BorderStroke(1.dp, Line.copy(alpha = .7f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, top = 17.dp, end = 15.dp, bottom = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(memoryIcon(kind), null, Modifier.size(27.dp), tint = Gold.copy(alpha = .75f))
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(kind.title, style = MaterialTheme.typography.titleLarge)
                Text("Noch nicht festgehalten.", color = Soft, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Outlined.Add, "${kind.title} hinzufügen", Modifier.size(22.dp), tint = Gold)
        }
    }
}

@Composable
private fun MemoryEditorDialog(
    initial: MemoryEntry,
    isExisting: Boolean,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (MemoryEntry) -> Unit,
    onDelete: () -> Unit,
) {
    var text by rememberSaveable(initial.id) { mutableStateOf(initial.text) }
    var kindName by rememberSaveable(initial.id) { mutableStateOf(initial.kind.name) }
    val kind = MemoryKind.valueOf(kindName)
    var pinned by rememberSaveable(initial.id) { mutableStateOf(initial.pinned) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var kindMenu by remember { mutableStateOf(false) }
    AlertDialog(
        modifier = Modifier.testTag("memory_editor"),
        onDismissRequest = onDismiss,
        title = { Text(if (isExisting) "Erinnerung bearbeiten" else "Neue Erinnerung", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box {
                    OutlinedButton(
                        onClick = { kindMenu = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("memory_kind"),
                        shape = SmallShape,
                        border = BorderStroke(1.dp, Line),
                    ) {
                        Icon(memoryIcon(kind), null, Modifier.size(19.dp), tint = Gold)
                        Text(kind.title, Modifier.weight(1f).padding(horizontal = 10.dp), color = Paper)
                        Icon(Icons.Outlined.ExpandMore, "Art der Erinnerung auswählen", Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = kindMenu, onDismissRequest = { kindMenu = false }) {
                        MemoryKind.entries.forEach { option ->
                            DropdownMenuItem(text = { Text(option.title) }, leadingIcon = { Icon(memoryIcon(option), null) }, onClick = { kindName = option.name; kindMenu = false })
                        }
                    }
                }
                StoryTextField(
                    text, { text = it.take(4000) }, "Was soll in Erinnerung bleiben?",
                    Modifier.testTag("memory_text"), minLines = 3, maxLines = 7,
                    error = if (attempted && text.isBlank()) "Schreibe eine Erinnerung auf." else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Row(Modifier.fillMaxWidth().clickable { pinned = !pinned }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(pinned, { pinned = it }, modifier = Modifier.semantics { contentDescription = "Erinnerung als wichtig anheften" }.testTag("memory_pinned"), colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Ink))
                    Column(Modifier.weight(1f)) {
                        Text("Als wichtig anheften", color = Paper, style = MaterialTheme.typography.bodyMedium)
                        Text("Bei Antworten bevorzugt berücksichtigen.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (isExisting) {
                    TextButton(onClick = onDelete, enabled = !isSaving, modifier = Modifier.testTag("delete_memory")) {
                        Icon(Icons.Outlined.DeleteOutline, null, tint = Danger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Erinnerung löschen", color = Danger)
                    }
                }
                if (saveError != null) Text(saveError, color = Danger, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (text.isNotBlank()) onSave(initial.copy(kind = kind, text = text.trim(), pinned = pinned))
            }, enabled = !isSaving, modifier = Modifier.testTag("save_memory")) { Text(if (isSaving) "Wird gespeichert …" else "Speichern", color = GoldLight, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Abbrechen", color = Muted) } },
        containerColor = Panel,
        titleContentColor = Paper,
        textContentColor = Paper,
    )
}

private val MemoryDraftSaver = listSaver<MemoryEntry?, Any>(
    save = { value ->
        if (value == null) emptyList() else listOf(value.id, value.storyId, value.kind.name, value.text, value.pinned, value.createdAt, value.updatedAt)
    },
    restore = { values ->
        if (values.isEmpty()) null else MemoryEntry(
            id = values[0] as String,
            storyId = values[1] as String,
            kind = MemoryKind.valueOf(values[2] as String),
            text = values[3] as String,
            pinned = values[4] as Boolean,
            createdAt = values[5] as Long,
            updatedAt = if (values.size > 6) values[6] as Long else values[5] as Long,
        )
    },
)
