package dev.vincent.geschichten.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.ai.StoryPrompt
import dev.vincent.geschichten.ai.LocalModelCatalog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

/** All content and mutations enter through state/actions; the UI never fabricates chat. */
@Composable
fun StoryApp(state: AppUiState, actions: AppActions) {
    val snackbar = remember { SnackbarHostState() }
    var profileId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastScreenName by rememberSaveable { mutableStateOf(state.screen.name) }
    var setupOriginName by rememberSaveable { mutableStateOf(AppScreen.CHARACTERS.name) }
    LaunchedEffect(state.screen) {
        profileId = null
        if (state.screen == AppScreen.SETUP && lastScreenName != AppScreen.SETUP.name) setupOriginName = lastScreenName
        lastScreenName = state.screen.name
    }
    val setupOrigin = AppScreen.valueOf(setupOriginName)
    LaunchedEffect(state.notice) {
        state.notice?.let { notice ->
            snackbar.showSnackbar(message = notice, withDismissAction = true, duration = SnackbarDuration.Long)
            actions.dismissNotice()
        }
    }
    val mainTab = state.screen in setOf(AppScreen.CHARACTERS, AppScreen.STORIES, AppScreen.MEMORIES, AppScreen.SETUP)
    val backTarget = when (state.screen) {
        AppScreen.CHAT -> AppScreen.STORIES
        AppScreen.MEMORIES -> if (state.current != null) AppScreen.CHAT else AppScreen.CHARACTERS
        AppScreen.SETUP -> setupOrigin
        else -> AppScreen.CHARACTERS
    }
    BackHandler(enabled = state.screen != AppScreen.CHARACTERS) { actions.navigate(backTarget) }

    MaterialTheme(colorScheme = StoryColors, typography = StoryTypography) {
        Scaffold(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            containerColor = Ink,
            contentColor = Paper,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(snackbar, modifier = Modifier.navigationBarsPadding()) { data ->
                    Snackbar(data, containerColor = PanelRaised, contentColor = Paper, dismissActionContentColor = Gold)
                }
            },
            bottomBar = { if (mainTab) StoryNavigation(state.screen, actions) },
        ) { padding ->
            Box(
                Modifier.fillMaxSize().padding(padding).background(
                    Brush.verticalGradient(listOf(Color(0xFF151D23), Ink, Color(0xFF0C1318)))
                )
            ) {
                when (state.screen) {
                    AppScreen.CHARACTERS -> CharactersScreen(state, actions, onProfile = { profileId = it.id })
                    AppScreen.STORIES -> StoriesScreen(state, actions)
                    AppScreen.CHAT -> key(state.current?.story?.id) { ChatScreen(state, actions, onProfile = { profileId = it.id }) }
                    AppScreen.MEMORIES -> MemoriesScreen(state, actions)
                    AppScreen.EDITOR -> key(state.editingCharacter?.id ?: "new") { CharacterEditor(state, actions) }
                    AppScreen.SETUP -> SetupScreen(state, actions, onBack = { actions.navigate(setupOrigin) })
                }
            }
        }
        val profile = state.characters.firstOrNull { it.id == profileId }
            ?: state.current?.character?.takeIf { it.id == profileId }
          profile?.let { CharacterProfileDialog(it, onDismiss = { profileId = null }) }
          state.modelCleanupId?.let(LocalModelCatalog::find)?.let { previous ->
              AlertDialog(
                  onDismissRequest = { if (!state.modelBusy) actions.keepPreviousModel() },
                  modifier = Modifier.testTag("model_cleanup_dialog"),
                  title = { Text("Bisherige KI behalten?", style = MaterialTheme.typography.titleLarge) },
                  text = { Text("${previous.name} ist weiterhin auf deinem Handy gespeichert (${formatBytes(previous.bytes)}). " +
                      "Möchtest du die Datei behalten oder löschen, um Speicher freizugeben? Deine Gespräche, Figuren und Erinnerungen bleiben erhalten. " +
                      "Nach dem Löschen ist für dieses Modell ein erneuter Download nötig.") },
                  confirmButton = { TextButton(onClick = actions::deletePreviousModel, enabled = !state.modelBusy && !state.busy,
                      modifier = Modifier.testTag("delete_previous_model")) { Text("Modell löschen", color = Danger) } },
                  dismissButton = { TextButton(onClick = actions::keepPreviousModel, enabled = !state.modelBusy,
                      modifier = Modifier.testTag("keep_previous_model")) { Text("Behalten", color = GoldLight) } },
                  containerColor = Panel,
              )
          }
    }
}

@Composable
private fun StoryNavigation(current: AppScreen, actions: AppActions) {
    Column(Modifier.fillMaxWidth().background(Ink).navigationBarsPadding()) {
        HorizontalDivider(color = Line.copy(alpha = .65f))
        Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(horizontal = 8.dp)) {
            val tabs = listOf(
                Triple(AppScreen.CHARACTERS, "Figuren", Icons.Outlined.Person),
                Triple(AppScreen.STORIES, "Verlauf", Icons.Outlined.History),
                Triple(AppScreen.MEMORIES, "Gedächtnis", Icons.Outlined.Description),
                Triple(AppScreen.SETUP, "Einstellungen", Icons.Outlined.Settings),
            )
            tabs.forEach { (screen, label, icon) ->
                val selected = current == screen
                Column(
                    Modifier.weight(1f).clip(SmallShape).clickable(onClickLabel = "$label öffnen") { actions.navigate(screen) }
                        .semantics { this.selected = selected; role = Role.Tab }
                        .padding(bottom = 10.dp).testTag("tab_${screen.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.height(3.dp).width(64.dp).background(if (selected) Gold else Color.Transparent, RoundedCornerShape(50)))
                    Spacer(Modifier.height(10.dp))
                    Icon(icon, null, Modifier.size(25.dp), tint = if (selected) Gold else Muted)
                    Spacer(Modifier.height(5.dp))
                    Text(label, color = if (selected) GoldLight else Muted, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CharactersScreen(state: AppUiState, actions: AppActions, onProfile: (CharacterProfile) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("Alle") }
    val genres = remember(state.characters) { listOf("Alle") + state.characters.map { it.genre }.filter { it.isNotBlank() }.distinct() }
    val filtered = remember(state.characters, query, genre) {
        state.characters.filter { character ->
            (genre == "Alle" || character.genre == genre) &&
                (query.isBlank() || listOf(character.name, character.role, character.genre, character.traits).any { it.contains(query.trim(), ignoreCase = true) })
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(144.dp),
        modifier = Modifier.fillMaxSize().testTag("characters_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            PageHeader("Figuren", "Wähle deine Geschichte.", state.model, { actions.navigate(AppScreen.SETUP) }, horizontalPadding = 4.dp)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().testTag("character_search"),
                singleLine = true,
                placeholder = { Text("Figur suchen", color = Muted) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
                trailingIcon = if (query.isNotEmpty()) ({ IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Suche löschen") } }) else null,
                shape = RoundedCornerShape(15.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Line, focusedBorderColor = Gold,
                    unfocusedContainerColor = Panel, focusedContainerColor = Panel,
                    focusedTextColor = Paper, unfocusedTextColor = Paper, cursorColor = Gold,
                ),
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                items(genres) { item -> GenrePill(item, genre == item, { genre = item }, Modifier.testTag("genre_$item")) }
            }
        }
        if (filtered.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyStoryState(Icons.Outlined.Search, "Noch keine passende Figur", "Versuche einen anderen Namen oder ein anderes Genre.")
            }
        }
        items(filtered, key = { it.id }) { character ->
            CharacterCard(character, { actions.selectCharacter(character) }, { actions.editCharacter(character) }, { onProfile(character) })
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            GoldButton("Eigene Figur erstellen", actions::createCharacter, Modifier.fillMaxWidth().padding(top = 3.dp), icon = Icons.Outlined.Add, tag = "create_character")
        }
        if (!state.model.isReady()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier.fillMaxWidth().clip(SmallShape).clickable { actions.navigate(AppScreen.SETUP) }.padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(19.dp), tint = Gold)
                    Text("Richte deine KI ein, um loszuschreiben.", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, "KI einrichten", tint = Gold, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(character: CharacterProfile, onSelect: () -> Unit, onEdit: () -> Unit, onProfile: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(CardShape).background(Panel).border(1.dp, Line, CardShape)
            .clickable(onClickLabel = "Geschichte mit ${character.name} beginnen", onClick = onSelect)
            .testTag("character_${character.id}")
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(.77f)) {
            StoryArtwork(character.avatarKey, Modifier.matchParentSize(), description = null)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Ink.copy(alpha = .97f)))))
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(48.dp).testTag("edit_character_${character.id}"),
            ) {
                Box(Modifier.size(29.dp).background(Ink.copy(alpha = .7f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Edit, "${character.name} bearbeiten", Modifier.size(16.dp), tint = GoldLight)
                }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 12.dp, vertical = 13.dp)) {
                Text(character.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(character.role, color = Paper, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        HorizontalDivider(color = Line)
        TextButton(
            onClick = onProfile,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("profile_character_${character.id}")
                .semantics { contentDescription = "Persönlichkeit von ${character.name} ansehen" },
            colors = ButtonDefaults.textButtonColors(contentColor = GoldLight),
            contentPadding = PaddingValues(horizontal = 9.dp),
        ) {
            Icon(Icons.Outlined.Info, null, Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Profil", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StoriesScreen(state: AppUiState, actions: AppActions) {
    val characters = remember(state.characters) { state.characters.associateBy { it.id } }
    val dates = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }
    var confirmClearHistory by rememberSaveable { mutableStateOf(false) }
    val canClear = state.stories.isNotEmpty() && !state.busy && !state.historyBusy && !state.modelBusy
    LazyColumn(
        Modifier.fillMaxSize().testTag("stories_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PageHeader("Verlauf", "Deine gespeicherten Geschichten.", state.model, { actions.navigate(AppScreen.SETUP) }) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { confirmClearHistory = true }, enabled = canClear,
                    modifier = Modifier.testTag("clear_history")) {
                    Icon(Icons.Outlined.DeleteSweep, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.historyBusy) "Verlauf wird geleert …" else "Verlauf leeren")
                }
            }
        }
        if (state.stories.isEmpty()) {
            item {
                EmptyStoryState(Icons.Outlined.MenuBook, "Jede Geschichte beginnt mit dir", "Wähle eine Figur und erlebe euer erstes Abenteuer. Hier kannst du es später fortsetzen.", "Figuren entdecken", { actions.navigate(AppScreen.CHARACTERS) })
            }
        }
        items(state.stories.sortedByDescending { it.updatedAt }, key = { it.id }) { story ->
            val character = characters[story.characterId]
            Surface(
                onClick = { actions.openStory(story.id) },
                enabled = !state.historyBusy,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).testTag("story_${story.id}"),
                shape = CardShape,
                color = Panel,
                border = BorderStroke(1.dp, Line),
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    StoryArtwork(character?.avatarKey ?: "runa", Modifier.width(74.dp).height(96.dp).clip(SmallShape), description = null)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(character?.name ?: "Deine Geschichte", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                        Text(story.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(story.summary.ifBlank { "Hier geht euer Abenteuer weiter." }, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(dates.format(Date(story.updatedAt)), color = Soft, style = MaterialTheme.typography.labelSmall)
                    }
                    Icon(Icons.Outlined.ChevronRight, "Geschichte fortsetzen", Modifier.size(19.dp), tint = Gold)
                }
            }
        }
    }
    if (confirmClearHistory) {
        AlertDialog(
            onDismissRequest = { confirmClearHistory = false },
            title = { Text("Verlauf leeren?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Alle gespeicherten Gespräche, ihre Nachrichten und zugehörigen Erinnerungen werden endgültig gelöscht. " +
                "Deine Figuren, Einstellungen und heruntergeladenen Modelle bleiben erhalten.") },
            confirmButton = {
                TextButton(onClick = { confirmClearHistory = false; actions.clearHistory() }, enabled = canClear,
                    modifier = Modifier.testTag("confirm_clear_history")) { Text("Verlauf löschen", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmClearHistory = false }, modifier = Modifier.testTag("cancel_clear_history")) { Text("Abbrechen") } },
            containerColor = Panel,
        )
    }
}

@Composable
private fun ChatScreen(state: AppUiState, actions: AppActions, onProfile: (CharacterProfile) -> Unit) {
    val bundle = state.current
    if (bundle == null) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            PageHeader("Deine Geschichte", onBack = { actions.navigate(AppScreen.STORIES) })
            EmptyStoryState(Icons.Outlined.MenuBook, "Wen möchtest du treffen?", "Wähle eine Figur, um eine Geschichte zu beginnen.", "Figuren entdecken", { actions.navigate(AppScreen.CHARACTERS) })
        }
        return
    }
    val character = bundle.character
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    var chatMenu by remember { mutableStateOf(false) }
    var confirmNewStory by remember { mutableStateOf(false) }
    var followReplies by remember { mutableStateOf(true) }
    var initialPositioned by remember { mutableStateOf(false) }
    val hasStreaming = !state.memoryBusy && (state.busy || state.partialReply.isNotBlank())
    val canStop = state.busy
    // A separate end marker keeps the *bottom* of a reply visible even if it exceeds a screen.
    val itemCount = 2 + bundle.messages.size + if (hasStreaming) 1 else 0

    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { if (it is DragInteraction.Start) followReplies = false }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward to listState.isScrollInProgress }.collect { (canScroll, moving) ->
            if (!canScroll && !moving) followReplies = true
        }
    }
    LaunchedEffect(bundle.messages.size, state.partialReply, state.busy, state.memoryBusy) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it == itemCount }
        if (!initialPositioned) {
            initialPositioned = true
            if (bundle.messages.size <= 1 && !hasStreaming) return@LaunchedEffect
        }
        withFrameNanos { }
        if (followReplies) listState.scrollToItem(itemCount - 1)
    }

    Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding().testTag("chat_screen")) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 5.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { keyboard?.hide(); actions.navigate(AppScreen.STORIES) }, modifier = Modifier.testTag("chat_back")) {
                Icon(Icons.Outlined.ArrowBack, "Zurück zum Verlauf", tint = Paper)
            }
            CharacterAvatar(character.avatarKey, character.name, Modifier.size(46.dp))
            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                Text(character.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                OfflineBadge(state.model) { keyboard?.hide(); actions.navigate(AppScreen.SETUP) }
            }
            IconButton(onClick = { keyboard?.hide(); actions.navigate(AppScreen.MEMORIES) }, modifier = Modifier.testTag("chat_memories")) {
                Icon(Icons.Outlined.MenuBook, "Gedächtnis dieser Geschichte", tint = GoldLight, modifier = Modifier.size(27.dp))
            }
            Box {
                IconButton(onClick = { chatMenu = true }, modifier = Modifier.testTag("chat_menu")) {
                    Icon(Icons.Outlined.MoreVert, "Weitere Möglichkeiten", tint = Muted)
                }
                DropdownMenu(expanded = chatMenu, onDismissRequest = { chatMenu = false }) {
                    DropdownMenuItem(text = { Text("Persönlichkeit ansehen") }, leadingIcon = { Icon(Icons.Outlined.Info, null) }, modifier = Modifier.testTag("chat_personality"), onClick = { chatMenu = false; keyboard?.hide(); onProfile(character) })
                    DropdownMenuItem(text = { Text("Neue Geschichte") }, leadingIcon = { Icon(Icons.Outlined.Add, null) }, onClick = { chatMenu = false; confirmNewStory = true })
                    DropdownMenuItem(text = { Text("Geschichte exportieren") }, leadingIcon = { Icon(Icons.Outlined.FileUpload, null) }, onClick = { chatMenu = false; actions.exportCurrentStory() })
                    DropdownMenuItem(text = { Text("Einstellungen") }, leadingIcon = { Icon(Icons.Outlined.Settings, null) }, onClick = { chatMenu = false; keyboard?.hide(); actions.navigate(AppScreen.SETUP) })
                }
            }
        }
        HorizontalDivider(color = Line.copy(alpha = .5f))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("chat_messages"),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "scene") { StoryScene(bundle) }
            items(bundle.messages, key = { it.id }) { message ->
                Column {
                    if (message.id == bundle.messages.firstOrNull()?.id && message.role == ChatRole.CHARACTER) {
                        Text("Euer Geschichteneinstieg", color = GoldLight,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 20.dp).testTag("chat_introduction_label"))
                        Spacer(Modifier.height(8.dp))
                    }
                    MessageBubble(message.text, message.role == ChatRole.USER, character)
                }
            }
            if (hasStreaming) {
                item(key = "streaming") {
                    MessageBubble(state.partialReply.ifBlank { "…" }, false, character, streaming = true)
                }
            }
            item(key = "end") { Spacer(Modifier.fillMaxWidth().height(1.dp).testTag("chat_end")) }
        }
        if (!state.model.isReady() && !state.busy) {
            Surface(color = Color(0xFF282721), border = BorderStroke(1.dp, Gold.copy(alpha = .25f))) {
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 3.dp, bottom = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(18.dp), tint = Gold)
                    Text("Deine KI ist noch nicht bereit.", style = MaterialTheme.typography.bodySmall, color = GoldLight, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    TextButton(onClick = { keyboard?.hide(); actions.navigate(AppScreen.SETUP) }, modifier = Modifier.testTag("chat_setup")) { Text("Einrichten", color = GoldLight) }
                }
            }
        }
        if(state.busy && state.teamStatus.isNotBlank()) {
            Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=9.dp).semantics {liveRegion=LiveRegionMode.Polite}.testTag("team_status"),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                CircularProgressIndicator(Modifier.size(13.dp),strokeWidth=1.5.dp,color=Gold)
                Text(state.teamStatus,color=Muted,style=MaterialTheme.typography.bodySmall)
            }
        }
        if (state.memoryBusy) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 9.dp).semantics { liveRegion = LiveRegionMode.Polite }.testTag("memory_update_status"), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 1.5.dp, color = Gold)
                Text("Erinnerungen werden aktualisiert …", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        HorizontalDivider(color = Line.copy(alpha = .65f))
        Row(
            Modifier.fillMaxWidth().background(Ink).padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = { actions.changeDraft(it.take(StoryPrompt.MAX_USER_MESSAGE_CHARS)) },
                readOnly = state.busy || state.memoryBusy,
                modifier = Modifier.weight(1f).heightIn(min = 54.dp).semantics { contentDescription = "Deine Nachricht an ${character.name}" }.testTag("chat_input"),
                placeholder = { Text("Was sagst oder tust du?", style = MaterialTheme.typography.bodyMedium, color = Muted) },
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                supportingText = if (state.draft.length >= StoryPrompt.MAX_USER_MESSAGE_CHARS - 100) ({ Text("${state.draft.length} / ${StoryPrompt.MAX_USER_MESSAGE_CHARS}", style = MaterialTheme.typography.labelSmall, color = Muted) }) else null,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (state.draft.isNotBlank() && state.model.isReady() && !state.busy && !state.modelBusy && !state.memoryBusy) actions.sendMessage() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Line, focusedBorderColor = Gold,
                    unfocusedContainerColor = Panel, focusedContainerColor = Panel,
                    focusedTextColor = Paper, unfocusedTextColor = Paper, cursorColor = Gold,
                ),
            )
            FilledIconButton(
                onClick = { if (canStop) actions.stopGeneration() else actions.sendMessage() },
                enabled = canStop || (!state.memoryBusy && !state.busy && !state.modelBusy && state.draft.isNotBlank() && state.model.isReady()),
                modifier = Modifier.size(54.dp).testTag(if (canStop) "chat_stop" else "chat_send"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Gold, contentColor = Ink, disabledContainerColor = PanelRaised, disabledContentColor = Soft),
            ) {
                Icon(
                    if (canStop) Icons.Outlined.Stop else Icons.Outlined.Send,
                    if (canStop && state.memoryBusy) "Aktualisieren anhalten" else if (canStop) "Antwort anhalten" else "Nachricht senden",
                    Modifier.size(24.dp),
                )
            }
        }
    }
    if (confirmNewStory) {
        AlertDialog(
            onDismissRequest = { confirmNewStory = false },
            title = { Text("Ein neues Abenteuer?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Beginne eine neue Geschichte mit ${character.name}. Eure bisherige Geschichte bleibt gespeichert.") },
            confirmButton = { TextButton(onClick = { confirmNewStory = false; keyboard?.hide(); actions.startNewStory(character) }, modifier = Modifier.testTag("confirm_new_story")) { Text("Neue Geschichte") } },
            dismissButton = { TextButton(onClick = { confirmNewStory = false }) { Text("Abbrechen") } },
            containerColor = Panel,
        )
    }
}

@Composable
private fun StoryScene(bundle: StoryBundle) {
    Column {
        Text(
            bundle.story.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).semantics { heading() },
        )
        StorySceneArtwork(bundle.character.avatarKey, bundle.character.name, bundle.character.genre)
    }
}

@Composable
private fun MessageBubble(text: String, fromUser: Boolean, character: CharacterProfile, streaming: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!fromUser) {
            CharacterAvatar(character.avatarKey, character.name, Modifier.padding(top = 3.dp).size(31.dp))
            Spacer(Modifier.width(9.dp))
        } else Spacer(Modifier.width(46.dp))
        Surface(
            modifier = Modifier.weight(1f, fill = false).testTag(if (streaming) "streaming_reply" else if (fromUser) "user_message" else "character_message")
                .then(if (streaming) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
            shape = RoundedCornerShape(topStart = if (fromUser) 17.dp else 4.dp, topEnd = 17.dp, bottomStart = 17.dp, bottomEnd = if (fromUser) 4.dp else 17.dp),
            color = if (fromUser) UserBlue else PanelRaised,
            border = BorderStroke(.5.dp, if (fromUser) Color(0xFF5477A0) else Line.copy(alpha = .5f)),
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                if (text == "…" && streaming) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Gold)
                        Text("${character.name} schreibt …", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    val formatted = remember(text, fromUser) { storyText(text, characterSpeech = !fromUser) }
                    SelectionContainer { Text(formatted, color = Paper, style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
        if (!fromUser) Spacer(Modifier.width(16.dp))
    }
}
