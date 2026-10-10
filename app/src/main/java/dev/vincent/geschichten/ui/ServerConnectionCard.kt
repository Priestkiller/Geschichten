package dev.vincent.geschichten.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.vincent.geschichten.ai.ModelStage

@Composable
internal fun ServerConnectionCard(state: AppUiState, actions: AppActions) {
    var address by rememberSaveable(state.server.address) { mutableStateOf(state.server.address) }
    var model by rememberSaveable(state.server.model) { mutableStateOf(state.server.model) }
    val editable = !state.busy && !state.modelBusy
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("ollama_settings"), color = Panel,
        shape = CardShape, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("KI auf deinem PC", style = MaterialTheme.typography.titleLarge)
            Text("Ollama schreibt auf deinem PC. Für eine Antwort erhält der PC das Figurenprofil, passende Erinnerungen und den ausgewählten Gesprächsverlauf. Deine Geschichten und dein Gedächtnis bleiben auf dem Handy gespeichert.", color = Muted)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("PC-KI verwenden", Modifier.weight(1f))
                Switch(state.server.enabled, { enabled -> if (enabled) actions.connectServer(address, model) else actions.setServerEnabled(false) },
                    enabled = editable, modifier = Modifier.testTag("ollama_enabled"))
            }
            OutlinedTextField(address, { address = it.take(300) }, label = { Text("Serveradresse") },
                singleLine = true, enabled = editable, modifier = Modifier.fillMaxWidth().testTag("ollama_address"))
            OutlinedTextField(model, { model = it.take(200) }, label = { Text("Ollama-Modell") },
                singleLine = true, enabled = editable, modifier = Modifier.fillMaxWidth().testTag("ollama_model"))
            Text("PC und Handy müssen einander im Netzwerk erreichen können. Diese Anbindung verwendet das geprüfte Gemma 4 12B. Bis zum ersten Text kann es mehrere Minuten dauern. Du kannst die Antwort im Chat anhalten.", color = Muted, style = MaterialTheme.typography.bodySmall)
            GoldButton("Verbindung prüfen und verwenden", { actions.connectServer(address, model) },
                Modifier.fillMaxWidth().testTag("ollama_connect"), enabled = editable)
            if (state.server.enabled) {
                Text(state.model.detail, color = if (state.model.stage == ModelStage.ERROR) Danger else GoldLight,
                    modifier = Modifier.testTag("ollama_status"))
                if (state.model.stage == ModelStage.LOADING) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    TextButton(actions::cancelDownload) { Text("Verbindung anhalten") }
                }
                if (state.model.isReady()) {
                    GoldButton(if (state.current != null) "Zur Geschichte" else "Figuren entdecken",
                        { actions.navigate(if (state.current != null) AppScreen.CHAT else AppScreen.CHARACTERS) }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
