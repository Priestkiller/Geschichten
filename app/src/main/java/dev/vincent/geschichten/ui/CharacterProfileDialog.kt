package dev.vincent.geschichten.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.vincent.geschichten.data.CharacterProfile

/** Read the installed profile, including user edits, without opening or creating a story. */
@Composable
internal fun CharacterProfileDialog(character: CharacterProfile, onDismiss: () -> Unit) {
    val parts = character.personality.split("\n\n", limit = 2)
    val identity = parts.first()
    val behavior = parts.getOrElse(1) { "" }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.94f).widthIn(max = 640.dp).fillMaxHeight(.86f)
                .testTag("personality_dialog"),
            shape = CardShape, color = Panel, contentColor = Paper,
        ) {
            Column {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Profil", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_personality")) {
                        Icon(Icons.Outlined.Close, "Profil schließen")
                    }
                }
                HorizontalDivider(color = Line)
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth().testTag("personality_content"),
                    contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            CharacterAvatar(character.avatarKey, character.name, Modifier.size(64.dp))
                            Column(Modifier.weight(1f)) {
                                Text(character.name, style = MaterialTheme.typography.headlineSmall)
                                Text(character.role, style = MaterialTheme.typography.bodyMedium, color = GoldLight)
                                Text(character.genre, style = MaterialTheme.typography.labelMedium, color = Muted)
                            }
                        }
                    }
                    item { SectionLabel("Wer ${character.name} ist") }
                    item {
                        SelectionContainer {
                            Text(identity.ifBlank { "Für diese Figur ist noch kein Hintergrund hinterlegt." },
                                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("character_identity_text"))
                        }
                    }
                    if (character.traits.isNotBlank()) {
                        item { SectionLabel("Eigenschaften") }
                        item { Text(character.traits, style = MaterialTheme.typography.bodyLarge, color = GoldLight,
                            modifier = Modifier.testTag("character_traits_text")) }
                    }
                    if (behavior.isNotBlank()) {
                        item { SectionLabel("Charakter und Sprechweise") }
                        item {
                            SelectionContainer {
                                Text(behavior, style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.testTag("character_personality_text"))
                            }
                        }
                    }
                    item { Spacer(Modifier.fillMaxWidth().height(1.dp).testTag("profile_end")) }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Schließen", color = GoldLight)
                }
            }
        }
    }
}
