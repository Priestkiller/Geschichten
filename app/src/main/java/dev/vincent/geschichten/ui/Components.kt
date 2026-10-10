package dev.vincent.geschichten.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.ModelState
import dev.vincent.geschichten.data.MemoryKind
import java.text.NumberFormat
import java.util.Locale

internal val Ink = Color(0xFF10171C)
internal val Panel = Color(0xFF1C252E)
internal val PanelRaised = Color(0xFF242E38)
internal val Line = Color(0xFF38434F)
internal val Gold = Color(0xFFE0AC7B)
internal val GoldLight = Color(0xFFF0CBA7)
internal val Paper = Color(0xFFF3F0EB)
internal val Muted = Color(0xFFB9C0C9)
internal val Soft = Color(0xFF8E9AAA)
internal val OfflineGreen = Color(0xFF70D6A1)
internal val UserBlue = Color(0xFF304D70)
internal val Danger = Color(0xFFFFB4AB)
internal val CardShape = RoundedCornerShape(18.dp)
internal val SmallShape = RoundedCornerShape(12.dp)

internal val StoryColors = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    primaryContainer = Color(0xFF4C382A),
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Panel,
    onSurface = Paper,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = Muted,
    outline = Line,
    error = Danger,
)

internal val StoryTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, color = Paper),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, color = Paper),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp, color = Paper),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, color = Paper),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp),
)

internal fun ModelState.isReady(): Boolean = stage == ModelStage.READY || stage == ModelStage.GENERATING

@Composable
internal fun OfflineBadge(model: ModelState, onClick: () -> Unit) {
    val ready = model.isReady()
    val label = when (model.stage) {
        ModelStage.READY, ModelStage.GENERATING -> if (model.server) "PC-KI" else "Offline"
        ModelStage.DOWNLOADING -> "Download läuft"
        ModelStage.VERIFYING -> "Wird geprüft"
        ModelStage.LOADING -> if (model.server) "Verbinde PC" else "KI startet"
        ModelStage.DOWNLOADED -> "KI starten"
        else -> "KI einrichten"
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClickLabel = if (ready) "KI-Einstellungen öffnen" else "KI-Einrichtung öffnen", onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 7.dp)
            .testTag("model_status"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (ready) {
            Box(Modifier.size(7.dp).background(OfflineGreen, CircleShape))
        } else {
            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(15.dp), tint = Gold)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (ready) Paper else GoldLight)
    }
}

@Composable
internal fun PageHeader(
    title: String,
    subtitle: String? = null,
    model: ModelState? = null,
    onSetup: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    horizontalPadding: Dp = 20.dp,
) {
    Column(Modifier.fillMaxWidth().padding(start = horizontalPadding, end = horizontalPadding, top = 13.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp).offset(x = (-10).dp).testTag("back_button")) {
                    Icon(Icons.Outlined.ArrowBack, "Zurück", tint = Paper)
                }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f), maxLines = 2)
            if (trailing != null) trailing()
            if (model != null) OfflineBadge(model, onSetup)
        }
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
internal fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    tag: String? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp).then(if (tag != null) Modifier.testTag(tag) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink, disabledContainerColor = Line, disabledContentColor = Soft),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(20.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), shape = SmallShape, border = BorderStroke(1.dp, Line)) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Paper)
    }
}

@Composable
internal fun StoryArtwork(
    avatarKey: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    scene: Boolean = false,
) {
    ArtworkImage(rememberStoryArtwork(avatarKey, scene), modifier, description)
}

private data class StoryArtworkSource(val drawableId: Int, val isScene: Boolean)

@Composable
private fun rememberStoryArtwork(avatarKey: String, scene: Boolean): StoryArtworkSource {
    val context = LocalContext.current
    return remember(context, avatarKey, scene) {
        val sceneId = if (scene) context.resources.getIdentifier("scene_$avatarKey", "drawable", context.packageName) else 0
        StoryArtworkSource(
            drawableId = if (sceneId != 0) sceneId else context.resources.getIdentifier("portrait_$avatarKey", "drawable", context.packageName),
            isScene = sceneId != 0,
        )
    }
}

/** Portraits need enough vertical space to show the head, including long creature faces. */
@Composable
internal fun StorySceneArtwork(avatarKey: String, name: String, genre: String) {
    val source = rememberStoryArtwork(avatarKey, scene = true)
    val portrait = source.drawableId != 0 && !source.isScene
    Box(
        Modifier.fillMaxWidth()
            .then(if (portrait) Modifier.aspectRatio(5f / 4f) else Modifier.height(235.dp))
    ) {
        ArtworkImage(
            source,
            Modifier.matchParentSize().testTag("chat_scene_artwork"),
            "Szene mit $name",
            alignment = if (portrait) Alignment.TopCenter else Alignment.Center,
        )
        val fadeStart = if (portrait) .82f else .5f
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(
            0f to Color.Transparent,
            fadeStart to Color.Transparent,
            1f to Ink,
        )))
        Text(
            genre.uppercase(Locale.GERMAN),
            color = GoldLight,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 15.dp),
        )
    }
}

@Composable
private fun ArtworkImage(
    source: StoryArtworkSource,
    modifier: Modifier,
    description: String?,
    alignment: Alignment = Alignment.Center,
) {
    if (source.drawableId != 0) {
        Image(painterResource(source.drawableId), description, modifier, contentScale = ContentScale.Crop, alignment = alignment)
    } else {
        Box(
            modifier.background(Brush.verticalGradient(listOf(Color(0xFF46525C), Color(0xFF2E2928), Ink))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Person, description, Modifier.size(56.dp), tint = Gold.copy(alpha = .65f))
        }
    }
}

@Composable
internal fun CharacterAvatar(key: String, name: String, modifier: Modifier = Modifier) {
    StoryArtwork(key, modifier.clip(CircleShape).border(1.dp, Gold.copy(alpha = .5f), CircleShape), description = "Porträt von $name")
}

@Composable
internal fun StoryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    placeholder: String? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leading: ImageVector? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = if (placeholder != null) ({ Text(placeholder, color = Soft) }) else null,
        leadingIcon = if (leading != null) ({ Icon(leading, null, tint = Muted) }) else null,
        minLines = minLines,
        maxLines = maxLines,
        singleLine = maxLines == 1,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = SmallShape,
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Line,
            focusedLabelColor = GoldLight,
            unfocusedLabelColor = Muted,
            focusedContainerColor = Panel,
            unfocusedContainerColor = Panel,
            cursorColor = Gold,
            focusedTextColor = Paper,
            unfocusedTextColor = Paper,
        ),
    )
}

@Composable
internal fun GenrePill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp).semantics { role = Role.Tab; this.selected = selected },
        shape = RoundedCornerShape(50),
        color = if (selected) Gold else Color.Transparent,
        contentColor = if (selected) Ink else Paper,
        border = BorderStroke(1.dp, if (selected) GoldLight else Line),
    ) {
        Box(Modifier.padding(horizontal = 17.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun EmptyStoryState(
    icon: ImageVector,
    title: String,
    body: String,
    button: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(76.dp).background(Panel, CircleShape).border(1.dp, Line, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(32.dp), tint = Gold)
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = Muted, textAlign = TextAlign.Center)
        if (button != null) GoldButton(button, onClick, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = modifier.padding(top = 10.dp, bottom = 2.dp))
}

internal fun memoryIcon(kind: MemoryKind): ImageVector = when (kind) {
    MemoryKind.FACT -> Icons.Outlined.Description
    MemoryKind.LOCATION -> Icons.Outlined.LocationOn
    MemoryKind.GOAL -> Icons.Outlined.Flag
    MemoryKind.EVENT -> Icons.Outlined.Groups
}

internal fun formatBytes(bytes: Long): String {
    val number = NumberFormat.getNumberInstance(Locale.GERMAN).apply { maximumFractionDigits = 1 }
    return when {
        bytes >= 1_000_000_000L -> "${number.format(bytes / 1_000_000_000.0)} GB"
        bytes >= 1_000_000L -> "${number.format(bytes / 1_000_000.0)} MB"
        bytes >= 1_000L -> "${number.format(bytes / 1_000.0)} KB"
        else -> "$bytes B"
    }
}

/** Presentation only: retain text, parse simple emphasis, and distinguish quoted character speech. */
internal fun storyText(text: String, characterSpeech: Boolean = false): AnnotatedString {
    val marked = buildAnnotatedString {
        val markers = Regex("\\*\\*(.+?)\\*\\*|\\*([^*]+?)\\*", RegexOption.DOT_MATCHES_ALL)
        var end = 0
        markers.findAll(text).forEach { match ->
            append(text.substring(end, match.range.first))
            if (match.groups[1] != null) {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                append(match.groupValues[1])
            } else {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Muted))
                append(match.groupValues[2])
            }
            pop()
            end = match.range.last + 1
        }
        append(text.substring(end))
    }
    if (!characterSpeech) return marked
    val quotes = Regex("„[^“\\n]+“|“[^”\\n]+”|«[^»\\n]+»|\"[^\"\\n]+\"").findAll(marked.text).toList()
    return buildAnnotatedString {
        if (quotes.isNotEmpty()) pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Muted))
        append(marked)
        if (quotes.isNotEmpty()) pop()
        quotes.forEach { quote ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal, color = GoldLight),
                quote.range.first, quote.range.last + 1)
        }
    }
}
