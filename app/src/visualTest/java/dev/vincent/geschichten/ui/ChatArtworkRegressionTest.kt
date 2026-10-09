package dev.vincent.geschichten.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.R
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.ModelState
import dev.vincent.geschichten.data.BuiltinCharacters
import dev.vincent.geschichten.data.CharacterProfile
import dev.vincent.geschichten.data.ChatMessage
import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.MemoryEntry
import dev.vincent.geschichten.data.Story
import dev.vincent.geschichten.data.StoryBundle
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Real StoryApp rendering with the shipped character/scenario/opening text. No invented
 * dialogue, repository, model, network access or inference is used. The image comparison
 * examines rendered pixels, not an Alignment or ContentScale configuration value.
 * Screenshots are captured BEFORE the eye assertion so a failing baseline is reviewable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChatArtworkRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aruunFirstChatAt360dpKeepsHisEyeVisible() {
        renderFirstChat(character("aruun"))
        captureScreen("aruun-360x780.png")
        assertSceneFullyVisible()
        assertAruunEyeMatchesOriginalAtTop()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w430dp-h900dp-xxhdpi")
    fun aruunFirstChatAt430dpKeepsHisEyeVisible() {
        renderFirstChat(character("aruun"))
        captureScreen("aruun-430x900.png")
        assertSceneFullyVisible()
        assertAruunEyeMatchesOriginalAtTop()
    }

    @Test
    fun runaLandscapeSceneReference() {
        renderFirstChat(character("runa"))
        captureScreen("runa-landscape-reference.png")
        assertSceneFullyVisible()
    }

    @Test
    fun renderAllNinetyFirstChatsAndSelectionCardsForVisualReview() {
        assertEquals("The audit must cover the complete shipped catalog", 90, BuiltinCharacters.profiles.size)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        for (profile in BuiltinCharacters.profiles) {
            assertTrue("Missing portrait resource for ${profile.id}",
                context.resources.getIdentifier("portrait_${profile.avatarKey}", "drawable", context.packageName) != 0)
        }
        val current = mutableStateOf(firstChatState(BuiltinCharacters.profiles.first()))
        compose.setContent {
            // Each scene starts at its genuine initial scroll position, with one opening.
            key(current.value.screen, current.value.current?.story?.id) {
                StoryApp(current.value, ArtworkNoOpActions)
            }
        }
        for (profile in BuiltinCharacters.profiles) {
            compose.runOnIdle { current.value = firstChatState(profile) }
            compose.waitForIdle()
            compose.onNodeWithTag("chat_input").assertIsDisplayed()
            assertSceneFullyVisible()
            captureScreen("all-90/chat-${profile.id}.png")
        }

        compose.runOnIdle {
            current.value = AppUiState(
                screen = AppScreen.CHARACTERS,
                characters = BuiltinCharacters.profiles,
                model = ModelState(stage = ModelStage.MISSING),
            )
        }
        compose.waitForIdle()
        for (profile in BuiltinCharacters.profiles) {
            compose.onNodeWithTag("characters_screen")
                .performScrollToNode(hasTestTag("character_${profile.id}"))
            val card = compose.onNodeWithTag("character_${profile.id}")
            card.assertIsDisplayed()
            val node = card.fetchSemanticsNode()
            assertEquals("Complete card width for ${profile.name}", node.size.width.toFloat(), node.boundsInRoot.width, 1.5f)
            assertEquals("Complete card height for ${profile.name}", node.size.height.toFloat(), node.boundsInRoot.height, 1.5f)
            card.captureRoboImage(output("all-90/card-${profile.id}.png").absolutePath)
        }
    }

    private fun renderFirstChat(profile: CharacterProfile) {
        compose.setContent { StoryApp(firstChatState(profile), ArtworkNoOpActions) }
        compose.waitForIdle()
        compose.onNodeWithTag("chat_screen").assertIsDisplayed()
        compose.onNodeWithTag("chat_input").assertIsDisplayed()
        compose.onNodeWithTag("chat_setup").assertIsDisplayed()
    }

    private fun assertSceneFullyVisible() {
        val image = compose.onNodeWithTag("chat_scene_artwork", useUnmergedTree = true)
        image.assertIsDisplayed()
        val node = image.fetchSemanticsNode()
        // A partially clipped image is not enough: its entire layout must fit the viewport.
        assertEquals("Complete scene width", node.size.width.toFloat(), node.boundsInRoot.width, 1.5f)
        assertEquals("Complete scene height", node.size.height.toFloat(), node.boundsInRoot.height, 1.5f)
    }

    private fun assertAruunEyeMatchesOriginalAtTop() {
        val image = compose.onNodeWithTag("chat_scene_artwork", useUnmergedTree = true)
        // Roborazzi supports native Robolectric rendering directly. Compose's separate
        // PixelCopy captureToImage callback can time out in this JVM environment.
        val pixelSample = File("build/ui-artwork-pixel-samples", "aruun-${image.fetchSemanticsNode().size.width}px.png")
        check(pixelSample.parentFile?.isDirectory == true || pixelSample.parentFile?.mkdirs() == true)
        image.captureRoboImage(pixelSample.absolutePath)
        val rendered = requireNotNull(BitmapFactory.decodeFile(pixelSample.absolutePath))
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val original = requireNotNull(
            BitmapFactory.decodeResource(context.resources, R.drawable.portrait_aruun, BitmapFactory.Options().apply { inScaled = false }),
        )
        val scale = rendered.width.toFloat() / original.width
        val reference = Bitmap.createScaledBitmap(original, rendered.width, (original.height * scale).roundToInt(), true)
        try {
            // The distinctive golden eye is at x=42..52%, y=16..22.5% in the shipped
            // portrait. At the requested top placement it must survive at this screen
            // position. The old vertically centered crop instead shows chest feathers.
            val left = (reference.width * .42f).roundToInt()
            val right = (reference.width * .52f).roundToInt()
            val top = (reference.height * .16f).roundToInt()
            val bottom = (reference.height * .225f).roundToInt()
            assertTrue("The complete eye patch must fit inside the rendered scene", bottom + 2 < rendered.height)

            // Two-pixel tolerance permits native resampling/rounding differences without
            // accepting a displaced head. Mean RGB error is measured on the whole patch.
            var bestError = Double.POSITIVE_INFINITY
            for (dy in -2..2) for (dx in -2..2) {
                var error = 0L
                var channels = 0L
                for (y in top until bottom step 2) for (x in left until right step 2) {
                    val expected = reference.getPixel(x, y)
                    val actual = rendered.getPixel(x + dx, y + dy)
                    error += abs(Color.red(expected) - Color.red(actual))
                    error += abs(Color.green(expected) - Color.green(actual))
                    error += abs(Color.blue(expected) - Color.blue(actual))
                    channels += 3
                }
                bestError = minOf(bestError, error.toDouble() / channels)
            }
            println("Aruun original-eye patch mean RGB error: $bestError (maximum 18.0)")
            assertTrue("Aruun's original eye must remain visible near the top; mean RGB error=$bestError", bestError < 18.0)
        } finally {
            if (reference !== original) reference.recycle()
            original.recycle()
            rendered.recycle()
        }
    }

    private fun captureScreen(name: String) {
        compose.onRoot().captureRoboImage(output(name).absolutePath)
    }

    private fun output(name: String): File = File("build/ui-screenshots/artwork-regression", name).also {
        check(it.parentFile?.isDirectory == true || it.parentFile?.mkdirs() == true)
    }

    private fun character(id: String) = BuiltinCharacters.profiles.single { it.id == id }

    private fun firstChatState(profile: CharacterProfile): AppUiState {
        val time = 1_759_536_000_000L
        val story = Story(
            id = "artwork-audit-${profile.id}", characterId = profile.id, title = profile.storyTitle,
            createdAt = time, updatedAt = time,
        )
        return AppUiState(
            screen = AppScreen.CHAT,
            characters = BuiltinCharacters.profiles,
            stories = listOf(story),
            current = StoryBundle(
                story = story,
                character = profile,
                messages = listOf(ChatMessage(
                    id = "opening-${profile.id}", storyId = story.id, role = ChatRole.CHARACTER,
                    text = profile.openingMessage, createdAt = time,
                )),
                memories = BuiltinCharacters.initialMemories(profile, story.id, time),
            ),
            model = ModelState(stage = ModelStage.MISSING),
        )
    }
}

private object ArtworkNoOpActions : AppActions {
    override fun navigate(screen: AppScreen) = Unit
    override fun selectCharacter(character: CharacterProfile) = Unit
    override fun openStory(storyId: String) = Unit
    override fun startNewStory(character: CharacterProfile) = Unit
    override fun createCharacter() = Unit
    override fun editCharacter(character: CharacterProfile) = Unit
    override fun saveCharacter(character: CharacterProfile) = Unit
    override fun changeDraft(value: String) = Unit
    override fun sendMessage() = Unit
    override fun stopGeneration() = Unit
    override fun saveMemory(memory: MemoryEntry) = Unit
    override fun deleteMemory(memory: MemoryEntry) = Unit
    override fun downloadModel() = Unit
    override fun cancelDownload() = Unit
    override fun loadModel() = Unit
    override fun dismissNotice() = Unit
    override fun exportCurrentStory() = Unit
    override fun setAdultThemes(enabled: Boolean) = Unit
}
