package dev.vincent.geschichten.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.LocalModelCatalog
import dev.vincent.geschichten.ai.LocalModelSpec
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import dev.vincent.geschichten.updates.AppUpdateState
import dev.vincent.geschichten.updates.UpdateStage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SetupScreen(state: AppUiState, actions: AppActions, onBack: () -> Unit) {
    val model = state.model
    val selectedModel = LocalModelCatalog.restored(model.modelId)
    val ready = model.isReady()
    val returnTo = if (state.current != null) AppScreen.CHAT else AppScreen.CHARACTERS
    var ageConfirmation by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().navigationBarsPadding().testTag("setup_screen"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { PageHeader("Einstellungen", onBack = onBack) }
        item {
            val helper=state.helperModel
            val helperSpec=LocalModelCatalog.restored(helper.modelId)
            val sameFile=helperSpec.sha256==selectedModel.sha256
            var choose by remember {mutableStateOf(false)}
            Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp).testTag("team_options"),color=Panel,shape=CardShape,border=BorderStroke(1.dp,Line)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("Zusätzliche Fakten-KI",style=MaterialTheme.typography.titleLarge)
                    Text("Experiment: Eine zweite lokale Text-KI wertet Angaben aus und prüft den ganzen Entwurf. Deine gewählte Erzähl-KI schreibt die Geschichte. Bei einem belegten Fehler darf sie den Entwurf einmal verbessern. Erst geprüfte Antworten erscheinen im Chat.",color=Muted)
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("Testoption aktiv",Modifier.weight(1f))
                        Switch(state.teamEnabled,actions::setTeamEnabled,enabled=!state.busy && !state.modelBusy,modifier=Modifier.testTag("team_switch"))
                    }
                    Text("Erzähl-KI: ${selectedModel.name}")
                    Box {
                        OutlinedButton(onClick={choose=true},enabled=!state.busy && !state.modelBusy,modifier=Modifier.testTag("helper_picker")){Text("Fakten-KI: ${helperSpec.name}")}
                        DropdownMenu(choose,{choose=false}) {
                            LocalModelCatalog.models.filter {it.id in setOf("huihui-qwen3-4b","gemma-4-e2b",model.modelId)}.forEach {spec ->
                                DropdownMenuItem(text={Text(spec.name)},onClick={choose=false;actions.selectHelperModel(spec.id)},modifier=Modifier.testTag("helper_${spec.id}"))
                            }
                        }
                    }
                    if(sameFile)Text("Dieselbe Modelldatei: Das ist eine Selbstprüfung mit getrennten Aufträgen, kein unabhängiges zweites Modell.",color=Gold,modifier=Modifier.testTag("helper_self_check"))
                    Text("Die Modelle rechnen nacheinander. Laden und Wechseln können deutlich mehr Wartezeit und vorübergehend mehr Speicherbedarf verursachen. Ein Qualitätsgewinn und die Leistung auf dem S24 sind noch nicht garantiert.",color=Muted)
                    Text(helper.detail.ifBlank {if(helper.modelId in helper.downloadedModelIds) "Datei vorhanden; wird bei Bedarf geladen." else "Diese Modelldatei fehlt noch."},color=Muted,modifier=Modifier.testTag("helper_status"))
                    if(helper.stage==ModelStage.DOWNLOADING) {
                        LinearProgressIndicator(progress={helper.progress.coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
                        TextButton(actions::cancelHelperDownload){Text("Helfer-Download anhalten")}
                    } else if(helper.modelId !in helper.downloadedModelIds) {
                        Button(actions::downloadHelperModel,enabled=!state.busy && !state.modelBusy){Text("Fakten-KI herunterladen (${String.format(Locale.GERMANY,"%.2f",helperSpec.bytes/1_000_000_000.0)} GB)")}
                        Text("Es wird die bestehende Katalogdatei unter ihren bisherigen Modellbedingungen verwendet. Eine vorhandene Datei wird mitgenutzt.",color=Muted,style=MaterialTheme.typography.bodySmall)
                    }
                    Text("Bei einem technischen Helferfehler wird ein Rückfall ausdrücklich gemeldet. Bereits bestätigte Widersprüche werden dabei nicht freigegeben.",color=Muted,style=MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp).testTag("facts_answer_options"),color=Panel,shape=CardShape,border=BorderStroke(1.dp,Line)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("Kurze Faktenantworten",style=MaterialTheme.typography.titleLarge)
                    Text("Testoption: Eindeutige Fragen nach Besitz, Träger, Ablage, Verletzungen und Zielstatus werden direkt aus dem bestätigten Figurenwissen beantwortet. Die Antwort bleibt kurz. Bei fehlenden Angaben oder mehrdeutigen Fragen schreibt deine gewählte KI weiter.",color=Muted)
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("Testoption aktiv",Modifier.weight(1f))
                        Switch(checked=state.factsAnswers,onCheckedChange=actions::setFactsAnswers,enabled=!state.busy,modifier=Modifier.testTag("facts_answer_switch"))
                    }
                    Text("Für Gefühle, neue Handlungen und freies Erzählen bleibt deine KI zuständig. Diese Option benötigt keinen weiteren Download.",color=Muted)
                }
            }
        }
        item {
            val search=state.semanticSearch
            val uri=LocalUriHandler.current
            Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp).testTag("semantic_search_options"),color=Panel,shape=CardShape,border=BorderStroke(1.dp,Line)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("Bedeutungssuche für Erinnerungen",style=MaterialTheme.typography.titleLarge)
                    Text("Abschaltbare Testoption: Findet ältere Originalstellen auch bei anderer Wortwahl. Die Figuren-KI bleibt deine ausgewählte KI. Die Suche bestätigt keine neuen Fakten.",color=Muted)
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("Testoption aktiv",Modifier.weight(1f))
                        Switch(checked=search.enabled,onCheckedChange=actions::setSemanticSearch,modifier=Modifier.testTag("semantic_search_switch"))
                    }
                    Text("EmbeddingGemma · 300M · etwa 334 MB. Nach dem Download vollständig lokal. Die Wortsuche bleibt bei fehlenden Vektoren, Zeitlimit oder Fehlern aktiv.",color=Muted)
                    Text(search.detail,modifier=Modifier.semantics {liveRegion=LiveRegionMode.Polite})
                    if(search.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
                    if(search.total>0)Text("${search.indexed} von ${search.total} Abschnitten dieser Geschichte vorbereitet.",color=Muted)
                    if(!search.ready) {
                        TextButton(onClick={uri.openUri("https://ai.google.dev/gemma/terms")}){Text("Gemma-Nutzungsbedingungen")}
                        Button(onClick=actions::downloadSearchModel,enabled=!search.busy && !state.busy){Text(if(search.busy) "Download ${search.progress} %" else "Suchmodell herunterladen")}
                        Text("Mit dem Download verwendest du das Modell unter den Gemma-Nutzungsbedingungen.",style=MaterialTheme.typography.bodySmall,color=Muted)
                    } else {
                        Button(onClick={actions.prepareSearchIndex(false)},enabled=search.enabled && !search.busy && !state.busy && state.current!=null){Text("Erinnerungen vorbereiten")}
                        TextButton(onClick={actions.prepareSearchIndex(true)},enabled=search.enabled && !search.busy && !state.busy && state.current!=null){Text("Suchindex dieser Geschichte neu aufbauen")}
                    }
                    if(search.busy)TextButton(onClick=actions::stopSearchWork){Text("Anhalten")}
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(82.dp).background(Brush.radialGradient(listOf(Color(0xFF4A3E33), Panel)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(if (ready) Icons.Outlined.CheckCircle else Icons.Outlined.AutoAwesome, null, Modifier.size(35.dp), tint = if (ready) OfflineGreen else Gold)
                }
                Spacer(Modifier.height(18.dp))
                Text(if (ready) "Bereit für deine\nGeschichten." else "Deine KI.\nImmer dabei.", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    if (ready) "Deine KI läuft auf deinem Handy. Wähle eine Figur und schreibe dein nächstes Kapitel."
                    else "Einmal einrichten. Danach schreibst du mit deinen Figuren direkt auf deinem Handy – auch ohne Internet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = Panel, shape = CardShape, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Text("Ausgewählt: ${selectedModel.name}", style = MaterialTheme.typography.titleMedium, color = GoldLight, modifier = Modifier.testTag("selected_model_name"))
                    when (model.stage) {
                        ModelStage.MISSING -> {
                            Text("Alles in dieser App", style = MaterialTheme.typography.titleLarge)
                            Text("Diese KI wird direkt hier heruntergeladen. Nutze WLAN und halte die App geöffnet. " +
                                "Für die Einrichtung brauchst du etwa ${formatBytes(selectedModel.bytes + dev.vincent.geschichten.ai.ModelArtifact.FREE_SPACE_RESERVE)} freien Speicher.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                            if (model.totalBytes > 0) Text("Download: ${formatBytes(model.totalBytes)}", color = GoldLight, style = MaterialTheme.typography.bodySmall)
                            GoldButton(if (model.downloadedBytes > 0) "Download fortsetzen" else "KI herunterladen", actions::downloadModel, Modifier.fillMaxWidth(), icon = Icons.Outlined.Download, tag = "download_model")
                        }
                        ModelStage.DOWNLOADING -> {
                            Text("Deine KI kommt an", style = MaterialTheme.typography.titleLarge)
                            Text("Halte die App geöffnet. Du kannst dich währenddessen in der App umsehen oder den Download pausieren.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                            Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (model.totalBytes > 0) {
                                    LinearProgressIndicator(progress = { model.progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(5.dp).testTag("download_progress"), color = Gold, trackColor = Line)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${formatBytes(model.downloadedBytes)} von ${formatBytes(model.totalBytes)}", color = Muted, style = MaterialTheme.typography.bodySmall)
                                        Text("${(model.progress.coerceIn(0f, 1f) * 100).toInt()} %", color = GoldLight, style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(5.dp), color = Gold, trackColor = Line)
                                    Text(if (model.downloadedBytes > 0) "${formatBytes(model.downloadedBytes)} geladen" else "Verbindung wird hergestellt …", color = Muted, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            QuietButton("Download pausieren", actions::cancelDownload, Modifier.fillMaxWidth().testTag("cancel_download"), Icons.Outlined.Close)
                        }
                        ModelStage.VERIFYING -> {
                            Text("Fast geschafft", style = MaterialTheme.typography.titleLarge)
                            Text("Die heruntergeladenen Daten werden geprüft. Das kann einen Moment dauern.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(5.dp), color = Gold, trackColor = Line)
                            QuietButton("Abbrechen", actions::cancelDownload, Modifier.fillMaxWidth().testTag("cancel_download"))
                        }
                        ModelStage.DOWNLOADED -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = OfflineGreen, modifier = Modifier.size(22.dp))
                                Text("Download abgeschlossen", style = MaterialTheme.typography.titleLarge)
                            }
                            Text("Deine KI ist auf dem Handy gespeichert. Starte sie, um mit deinen Figuren zu schreiben.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                            GoldButton("KI starten", actions::loadModel, Modifier.fillMaxWidth(), icon = Icons.Outlined.AutoAwesome, tag = "load_model")
                        }
                        ModelStage.LOADING -> {
                            Text(if (model.optimizing) "Geschwindigkeit wird geprüft" else "Deine KI wacht auf", style = MaterialTheme.typography.titleLarge)
                            Text("Die App macht alles für deine erste Antwort bereit. Beim ersten Start kann das etwas länger dauern.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(5.dp), color = Gold, trackColor = Line)
                        }
                        ModelStage.READY, ModelStage.GENERATING -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(8.dp).background(OfflineGreen, CircleShape))
                                Text("Offline bereit", color = OfflineGreen, style = MaterialTheme.typography.titleMedium)
                            }
                            Text("Zum Schreiben brauchst du jetzt keine Internetverbindung.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                            GoldButton(if (state.current != null) "Zur Geschichte" else "Figuren entdecken", { actions.navigate(returnTo) }, Modifier.fillMaxWidth(), icon = Icons.Outlined.MenuBook, tag = "setup_continue")
                        }
                        ModelStage.ERROR -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = Danger, modifier = Modifier.size(23.dp))
                                Text("Das hat noch nicht geklappt", style = MaterialTheme.typography.titleLarge)
                            }
                            Text(model.detail.ifBlank { "Bitte versuche es noch einmal." }, color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("setup_error"))
                            GoldButton("Erneut versuchen", actions::downloadModel, Modifier.fillMaxWidth(), icon = Icons.Outlined.Refresh, tag = "retry_model")
                        }
                    }
                }
            }
        }
        item {
            ModelPerformanceCard(state, actions)
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Welche KI passt zu dir?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("model_selection_heading"))
                Text("Alle Modelle schreiben direkt auf deinem Handy und erhalten dieselben Figuren und Erinnerungen. " +
                    "Lade nur die Modelle herunter, die du ausprobieren möchtest. Nach einem erfolgreichen Wechsel entscheidest du, ob du die bisherige KI behältst oder ihre Datei löschst. Deine Geschichten bleiben erhalten.",
                    style = MaterialTheme.typography.bodyMedium, color = Muted)
                Text("Einsatzempfehlungen sind Orientierung, keine Rangliste. Mit „Geschwindigkeit optimieren“ kannst du die Wartezeit auf deinem Gerät vergleichen. Welche KI besser schreibt, entscheidest du beim Ausprobieren. " +
                    "Ein kleiner Download bedeutet nicht denselben Bedarf an Arbeitsspeicher.", style = MaterialTheme.typography.bodySmall, color = Muted,
                    modifier = Modifier.testTag("model_comparison_note"))
                if (state.modelBusy || state.busy) Text("Warte auf die Einrichtung oder halte die laufende Antwort an, bevor du die KI wechselst.", color = GoldLight, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(LocalModelCatalog.models.size, key = { "model_${LocalModelCatalog.models[it].id}" }) { index ->
            val spec = LocalModelCatalog.models[index]
            ModelChoiceCard(spec, model.modelId == spec.id, spec.id in model.downloadedModelIds,
                enabled = !state.modelBusy && !state.busy && model.stage !in setOf(ModelStage.DOWNLOADING, ModelStage.VERIFYING, ModelStage.LOADING, ModelStage.GENERATING),
                onSelect = { actions.selectModel(spec.id) }, onDelete = { actions.requestModelDeletion(spec.id) })
        }
        item {
            Column(Modifier.padding(horizontal = 25.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                SetupDetail(Icons.Outlined.Smartphone, "Auf deinem Handy", "Deine Geschichten und Erinnerungen werden auf diesem Gerät gespeichert.")
                SetupDetail(Icons.Outlined.WifiOff, "Nach dem Download offline", "Für neue Antworten brauchst du weder einen PC noch eine zweite App.")
            }
        }
        item { AppUpdatesCard(state.updates, actions) }
        item {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = Panel, shape = CardShape, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Erwachsene Themen (18+)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(end = 8.dp))
                        Switch(
                            checked = state.adultThemes,
                            onCheckedChange = { if (it) ageConfirmation = true else actions.setAdultThemes(false) },
                            modifier = Modifier.semantics { contentDescription = "Erwachsene Themen ab 18 Jahren" }.testTag("adult_themes"),
                            colors = SwitchDefaults.colors(checkedThumbColor = Ink, checkedTrackColor = Gold, uncheckedThumbColor = Muted, uncheckedTrackColor = PanelRaised, uncheckedBorderColor = Line),
                        )
                    }
                    Text("Derbe Sprache, düstere Geschichten und einvernehmliche Romantik. Intime Szenen werden ausgeblendet.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }
    }

    if (ageConfirmation) {
        AlertDialog(
            onDismissRequest = { ageConfirmation = false },
            title = { Text("Erwachsene Themen aktivieren?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Dieser Modus ist für Erwachsene. Er erlaubt derbere Sprache, düstere Themen und einvernehmliche Romantik. Intime Szenen werden ausgeblendet.") },
            confirmButton = {
                TextButton(onClick = { ageConfirmation = false; actions.setAdultThemes(true) }, modifier = Modifier.testTag("confirm_adult_themes")) {
                    Text("Ich bin mindestens 18 Jahre alt", color = GoldLight, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { ageConfirmation = false }) { Text("Abbrechen", color = Muted) } },
            containerColor = Panel,
        )
    }
}

@Composable
private fun ModelPerformanceCard(state: AppUiState, actions: AppActions) {
    val model = state.model
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("model_performance"), color = Panel, shape = CardShape, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Schneller zum ersten Text", style = MaterialTheme.typography.titleLarge)
            Text("Gerät: ${model.deviceName} · ${model.cpuThreads} Rechenthreads", color = GoldLight, style = MaterialTheme.typography.bodyMedium)
            Text("Die App kann für die ausgewählte KI mehrere Prozessoreinstellungen direkt auf deinem Handy vergleichen. " +
                "Das funktioniert für S24 und S24 Ultra sowie für andere Geräte. Die gemessene Einstellung wird für diese KI gespeichert. " +
                "Deine Einführungen und Antworten bleiben ausführlich. Die kurzen Messantworten werden nicht in deinem Verlauf gespeichert.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            model.performanceDetail?.let { Text(it, color = GoldLight, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("performance_result")) }
            model.firstTextMs?.let { Text("Letzte Antwort: ${String.format(Locale.GERMANY, "%.1f", it / 1000.0)} Sekunden bis zum ersten Text.", color = Muted, style = MaterialTheme.typography.bodySmall) }
            if (model.optimizing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Gold, trackColor = Line)
                QuietButton("Messung abbrechen", actions::cancelModelOptimization, Modifier.fillMaxWidth().testTag("cancel_model_optimization"))
            } else {
                GoldButton("Geschwindigkeit optimieren", actions::optimizeModel, Modifier.fillMaxWidth(), enabled = model.stage == ModelStage.READY && !state.modelBusy && !state.busy && !state.historyBusy, tag = "optimize_model")
                Text("Lass das Handy vor der Messung abkühlen und schließe andere aufwendige Apps. Die Prüfung kann einige Minuten dauern. Beim ersten Chat müssen die Angaben einmal vollständig verarbeitet werden.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ModelChoiceCard(spec: LocalModelSpec, selected: Boolean, downloaded: Boolean, enabled: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("model_choice_${spec.id}"),
        color = if (selected) PanelRaised else Panel, shape = CardShape,
        border = BorderStroke(1.dp, if (selected) Gold else Line),
    ) {
        Column(Modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .padding(18.dp).testTag("select_model_${spec.id}"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(if (selected) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked, null, tint = if (selected) Gold else Muted)
                Column(Modifier.weight(1f)) {
                    Text(spec.name, style = MaterialTheme.typography.titleLarge)
                    Text(spec.subtitle, style = MaterialTheme.typography.bodySmall, color = GoldLight)
                }
            }
            Text(spec.description, style = MaterialTheme.typography.bodyMedium)
            Text("Gut zum Ausprobieren: ${spec.useFor}", style = MaterialTheme.typography.bodySmall, color = Muted)
            Text("Beachte: ${spec.limitation}", style = MaterialTheme.typography.bodySmall, color = Muted)
            HorizontalDivider(color = Line)
            Text("Download: ${formatBytes(spec.bytes)} · Deutsch möglich · nach dem Download offline", style = MaterialTheme.typography.bodySmall, color = GoldLight)
            Text(if (downloaded) "Download vorhanden · wird vor dem Start geprüft" else "Noch nicht heruntergeladen", style = MaterialTheme.typography.bodySmall, color = if (downloaded) OfflineGreen else Muted)
            Text(if (selected) "Ausgewählt" else "Antippen zum Auswählen", style = MaterialTheme.typography.labelLarge, color = if (selected) Gold else Paper)
            TextButton(onClick = { uriHandler.openUri(spec.informationUrl ?: spec.sourceUrl) }, contentPadding = PaddingValues(0.dp)) {
                Text("Modellbeschreibung des Anbieters", color = GoldLight)
            }
            TextButton(onClick = { uriHandler.openUri(spec.sourceUrl) }, contentPadding = PaddingValues(0.dp)) {
                Text("Downloadquelle und Lizenz", color = GoldLight)
            }
            if (downloaded && !selected) {
                TextButton(onClick = onDelete, enabled = enabled, modifier = Modifier.testTag("delete_model_${spec.id}"), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Modell-Datei löschen", color = Muted)
                }
            }
        }
    }
}

@Composable
private fun AppUpdatesCard(state: AppUpdateState, actions: AppActions) {
    var editSource by rememberSaveable { mutableStateOf(state.repository.isBlank()) }
    var source by remember(state.repository) { mutableStateOf(state.repository) }
    var showChanges by remember(state.available?.versionCode) { mutableStateOf(false) }
    LaunchedEffect(state.repository) { if (state.repository.isNotBlank()) editSource = false }
    val candidate = state.available
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("app_updates"),
        color = Panel, shape = CardShape, border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.SystemUpdate, null, Modifier.size(25.dp), tint = Gold)
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("App-Updates", style = MaterialTheme.typography.titleLarge)
                    if (state.installedVersion.isNotBlank()) Text("Installiert: ${state.installedVersion}", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            Text("Neue App-Versionen kommen direkt aus eurem GitHub-Repository. Die Suche startet nur, wenn du sie antippst.", style = MaterialTheme.typography.bodyMedium, color = Muted)
            if (state.repository.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("GitHub · ${state.repository}", style = MaterialTheme.typography.bodySmall, color = GoldLight, modifier = Modifier.weight(1f))
                    TextButton(onClick = { editSource = !editSource }, enabled = !state.working) { Text("Ändern", color = Muted) }
                }
            }
            if (editSource) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { if (it.length <= 250) source = it },
                    modifier = Modifier.fillMaxWidth().testTag("update_repository"),
                    label = { Text("GitHub: Besitzer/Repository") },
                    placeholder = { Text("GitHub-Link oder Besitzer/Repository") },
                    singleLine = true, enabled = !state.working,
                    shape = SmallShape,
                )
                GoldButton("Updatequelle speichern", { actions.saveUpdateRepository(source) }, Modifier.fillMaxWidth(),
                    enabled = source.isNotBlank() && !state.working, icon = Icons.Outlined.Save, tag = "save_update_repository")
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(end = 10.dp)) {
                    Text("Testversionen einbeziehen", style = MaterialTheme.typography.bodyMedium)
                    Text(if (state.includePrerelease) "Auch veröffentlichte GitHub-Testversionen werden geprüft." else "Es werden nur reguläre Veröffentlichungen geprüft.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Switch(
                    checked = state.includePrerelease, onCheckedChange = actions::setIncludeTestUpdates,
                    enabled = !state.working,
                    modifier = Modifier.testTag("include_test_updates").semantics { contentDescription = "Testversionen bei App-Updates einbeziehen" },
                    colors = SwitchDefaults.colors(checkedThumbColor = Ink, checkedTrackColor = Gold, uncheckedThumbColor = Muted, uncheckedTrackColor = PanelRaised, uncheckedBorderColor = Line),
                )
            }
            HorizontalDivider(color = Line)
            Text(
                state.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.stage == UpdateStage.ERROR) Danger else if (state.stage == UpdateStage.UP_TO_DATE) OfflineGreen else Paper,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("update_status"),
            )
            if (candidate != null) {
                Text("${candidate.versionName}${if (candidate.prerelease) " · Testversion" else ""} · ${formatBytes(candidate.byteCount)}", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                if (candidate.changelog.isNotBlank()) {
                    TextButton(onClick = { showChanges = !showChanges }, contentPadding = PaddingValues(0.dp)) {
                        Text(if (showChanges) "Änderungen ausblenden" else "Was ist neu?", color = GoldLight)
                    }
                    if (showChanges) Text(candidate.changelog, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            when (state.stage) {
                UpdateStage.CHECKING -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth().height(5.dp), color = Gold, trackColor = Line)
                    QuietButton("Suche anhalten", actions::cancelAppUpdate, Modifier.fillMaxWidth(), Icons.Outlined.Close)
                }
                UpdateStage.DOWNLOADING, UpdateStage.VERIFYING -> {
                    LinearProgressIndicator(progress = { state.progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Gold, trackColor = Line)
                    if (state.stage == UpdateStage.DOWNLOADING && candidate != null) {
                        Text("${formatBytes(state.downloadedBytes)} von ${formatBytes(candidate.byteCount)}", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    QuietButton("Update-Vorgang anhalten", actions::cancelAppUpdate, Modifier.fillMaxWidth(), Icons.Outlined.Close)
                }
                UpdateStage.AVAILABLE -> GoldButton(
                    if (state.downloadedBytes > 0) "Update-Download fortsetzen" else "Update herunterladen",
                    actions::downloadAppUpdate, Modifier.fillMaxWidth(), icon = Icons.Outlined.Download, tag = "download_app_update",
                )
                UpdateStage.READY_TO_INSTALL, UpdateStage.INSTALLER_OPENED -> {
                    GoldButton("Update installieren", actions::installAppUpdate, Modifier.fillMaxWidth(), icon = Icons.Outlined.SystemUpdate, tag = "install_app_update")
                    Text("Android fragt dich vor der Installation. Falls nötig, erlaubst du sie dort einmal für diese App.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                UpdateStage.ERROR -> if (candidate != null) {
                    QuietButton("Update-Download erneut versuchen", actions::downloadAppUpdate, Modifier.fillMaxWidth(), Icons.Outlined.Refresh)
                }
                else -> Unit
            }
            if (!state.working) {
                GoldButton("Nach Updates suchen", actions::checkForUpdates, Modifier.fillMaxWidth(),
                    enabled = state.repository.isNotBlank(), icon = Icons.Outlined.Refresh, tag = "check_app_updates")
            }
            state.checkedAt?.let { timestamp ->
                Text("Zuletzt geprüft: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date(timestamp))}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
private fun SetupDetail(icon: ImageVector, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.padding(top = 2.dp).size(22.dp), tint = Gold)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Paper, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
