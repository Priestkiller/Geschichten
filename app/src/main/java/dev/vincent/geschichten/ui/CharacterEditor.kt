package dev.vincent.geschichten.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.vincent.geschichten.data.CharacterProfile
import dev.vincent.geschichten.data.BuiltinCharacters
import dev.vincent.geschichten.data.CharacterIntroductions

@Composable
internal fun CharacterEditor(state: AppUiState, actions: AppActions) {
    val initial = state.editingCharacter
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var role by rememberSaveable { mutableStateOf(initial?.role.orEmpty()) }
    var genre by rememberSaveable { mutableStateOf(initial?.genre ?: "Fantasy") }
    var traits by rememberSaveable { mutableStateOf(initial?.traits.orEmpty()) }
    var personality by rememberSaveable { mutableStateOf(initial?.personality.orEmpty()) }
    var scenario by rememberSaveable { mutableStateOf(initial?.scenario.orEmpty()) }
    var storyTitle by rememberSaveable { mutableStateOf(initial?.storyTitle.orEmpty()) }
    var opening by rememberSaveable { mutableStateOf(initial?.openingMessage.orEmpty()) }
    var avatarKey by rememberSaveable { mutableStateOf(initial?.avatarKey ?: "runa") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val genreOptions = remember(initial?.genre) {
        (listOfNotNull(initial?.genre) + BuiltinCharacters.profiles.map { it.genre } +
            listOf("Abenteuer", "Romantik", "Gegenwart")).distinct()
    }

    val dirty = name != initial?.name.orEmpty() || role != initial?.role.orEmpty() ||
        genre != (initial?.genre ?: "Fantasy") || traits != initial?.traits.orEmpty() ||
        personality != initial?.personality.orEmpty() || scenario != initial?.scenario.orEmpty() ||
        storyTitle != initial?.storyTitle.orEmpty() || opening != initial?.openingMessage.orEmpty() || avatarKey != (initial?.avatarKey ?: "runa")
    val back = { if (dirty) showDiscard = true else { keyboard?.hide(); actions.navigate(AppScreen.CHARACTERS) } }
    androidx.activity.compose.BackHandler { back() }

    Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding().testTag("character_editor")) {
        PageHeader(if (initial == null) "Eigene Figur" else "Figur bearbeiten", onBack = back)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Gib deiner Figur eine Stimme, eigene Ziele und eine Geschichte.", color = Muted, style = MaterialTheme.typography.bodyLarge)
            }
            item { SectionLabel("Ein Gesicht für deine Figur") }
            item {
                LazyRow(Modifier.fillMaxWidth().testTag("editor_portrait_choices"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(BuiltinCharacters.profiles, key = { it.avatarKey }) { portrait ->
                        val key = portrait.avatarKey
                        val label = portrait.name
                        Column(Modifier.width(82.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.fillMaxWidth().aspectRatio(.84f).clip(RoundedCornerShape(13.dp))
                                    .border(if (avatarKey == key) 2.dp else 1.dp, if (avatarKey == key) Gold else Line, RoundedCornerShape(13.dp))
                                    .clickable(role = Role.RadioButton, onClickLabel = "Porträt $label auswählen") { avatarKey = key }
                                    .semantics { selected = avatarKey == key }.testTag("editor_portrait_$key")
                            ) {
                                StoryArtwork(key, Modifier.matchParentSize(), description = "Porträt $label")
                                if (avatarKey == key) {
                                    Box(Modifier.align(Alignment.BottomEnd).padding(5.dp).size(20.dp).background(Gold, CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Check, null, Modifier.size(14.dp), tint = Ink)
                                    }
                                }
                            }
                            Text(label, color = if (avatarKey == key) GoldLight else Muted,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 6.dp), maxLines = 1)
                        }
                    }
                }
            }
            item { SectionLabel("Wer ist deine Figur?") }
            item {
                StoryTextField(name, { name = it.take(60) }, "Name *", Modifier.testTag("editor_name"), placeholder = "Wie heißt deine Figur?", error = if (attempted && name.isBlank()) "Gib deiner Figur einen Namen." else null)
            }
            item {
                StoryTextField(role, { role = it.take(100) }, "Rolle", Modifier.testTag("editor_role"), placeholder = "Zum Beispiel: Kundschafterin")
            }
            item {
                Text("Genre", color = Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 9.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genreOptions) { option ->
                        GenrePill(option, genre == option, { genre = option }, Modifier.testTag("editor_genre_$option"))
                    }
                }
            }
            item {
                StoryTextField(traits, { traits = it.take(100) }, "Eigenschaften", Modifier.testTag("editor_traits"), placeholder = "Wachsam, eigensinnig, trockener Humor", maxLines = 2)
            }
            item { SectionLabel("Ihre Persönlichkeit") }
            item {
                StoryTextField(
                    personality, { personality = it.take(CharacterIntroductions.MAX_PERSONALITY_CHARS) }, "Persönlichkeit und Hintergrund *",
                    Modifier.testTag("editor_personality"), minLines = 4, maxLines = 9,
                    placeholder = "Wie spricht und handelt sie? Was treibt sie an, was fürchtet sie?",
                    error = if (attempted && personality.isBlank()) "Beschreibe die Persönlichkeit deiner Figur." else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                CharacterCount(personality.length, CharacterIntroductions.MAX_PERSONALITY_CHARS)
            }
            item { SectionLabel("Euer erstes Kapitel") }
            item {
                StoryTextField(storyTitle, { storyTitle = it.take(100) }, "Titel der Geschichte", Modifier.testTag("editor_story_title"), placeholder = "Zum Beispiel: Die Hütte im Schnee")
            }
            item {
                StoryTextField(
                    scenario, { scenario = it.take(1_000) }, "Einstiegsszene *",
                    Modifier.testTag("editor_scenario"), minLines = 3, maxLines = 6,
                    placeholder = "Wer ist die Spielerfigur für sie? Wo begegnet ihr euch und warum spricht sie dich an?",
                    error = if (attempted && scenario.isBlank()) "Lege fest, wie eure Geschichte beginnt." else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                CharacterCount(scenario.length, 1_000)
            }
            item {
                StoryTextField(
                    opening, { opening = it.take(dev.vincent.geschichten.data.CharacterIntroductions.MAX_OPENING_CHARS) }, "Einführung und erste Nachricht *",
                    Modifier.testTag("editor_opening"), minLines = 3, maxLines = 8,
                    placeholder = "Eure Vorgeschichte, die Situation und ihre ersten Worte beim Chat-Start.",
                    error = if (attempted && opening.isBlank()) "Schreibe die erste Nachricht deiner Figur." else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                CharacterCount(opening.length, dev.vincent.geschichten.data.CharacterIntroductions.MAX_OPENING_CHARS)
            }
            item { Text("* Diese Felder braucht deine Figur zum Starten.", color = Soft, style = MaterialTheme.typography.bodySmall) }
        }
        HorizontalDivider(color = Line.copy(alpha = .7f))
        GoldButton(
            if (initial == null) "Figur erstellen" else "Änderungen speichern",
            onClick = {
                attempted = true
                if (name.isNotBlank() && personality.isNotBlank() && scenario.isNotBlank() && opening.isNotBlank()) {
                    val profile = CharacterProfile(
                        id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.trim(),
                        role = role.trim().ifBlank { "Eigene Figur" },
                        genre = genre,
                        traits = traits.trim(),
                        personality = personality.trim(),
                        scenario = scenario.trim(),
                        storyTitle = storyTitle.trim().ifBlank { "Begegnung mit ${name.trim()}" },
                        openingMessage = opening.trim(),
                        avatarKey = avatarKey,
                        custom = true,
                    )
                    keyboard?.hide()
                    actions.saveCharacter(profile)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            icon = Icons.Outlined.Check,
            tag = "save_character",
        )
    }
    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Änderungen verwerfen?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Deine noch nicht gespeicherten Änderungen gehen verloren.") },
            confirmButton = { TextButton(onClick = { showDiscard = false; keyboard?.hide(); actions.navigate(AppScreen.CHARACTERS) }) { Text("Verwerfen", color = Danger) } },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Weiter bearbeiten") } },
            containerColor = Panel,
        )
    }
}

@Composable
private fun CharacterCount(count: Int, maximum: Int) {
    Text("$count / $maximum", color = Soft, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth().padding(end = 4.dp, top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
}
