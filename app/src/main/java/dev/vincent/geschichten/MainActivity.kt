package dev.vincent.geschichten

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.content.FileProvider
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ui.StoryApp
import java.io.File
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appViewModel: AppViewModel
    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        appViewModel.completeExport(uri)
    }
    private val installPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // This Settings action returns no consent result; re-query the permission.
        // Calling the ViewModel again re-hashes and re-checks the APK before sharing.
        if (packageManager.canRequestPackageInstalls()) appViewModel.installAppUpdate()
        else appViewModel.updateInstallPermissionRequired()
    }
    private val installPackage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // A successful launch/result code does not prove installation. Query Android.
        appViewModel.updateInstallerReturned()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(0xFF111317.toInt()),
        )
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appViewModel.exports.collect { export ->
                        try {
                            createDocument.launch(export.fileName)
                        } catch (_: android.content.ActivityNotFoundException) {
                            appViewModel.exportUnavailable()
                        }
                    }
                }
                launch { appViewModel.installations.collect { apk -> openUpdateInstaller(apk) } }
            }
        }
        setContent {
            val state by appViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.model.stage, state.updates.working) {
                val working = state.model.stage in setOf(
                    ModelStage.DOWNLOADING, ModelStage.VERIFYING, ModelStage.LOADING, ModelStage.GENERATING,
                ) || state.updates.working
                if (working) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            StoryApp(state, appViewModel)
        }
    }

    /** Reached only after the user taps Update installieren and validation succeeds. */
    private fun openUpdateInstaller(apk: File) {
        try {
            if (!packageManager.canRequestPackageInstalls()) {
                installPermission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
                return
            }
            val uri = FileProvider.getUriForFile(this, "$packageName.updates", apk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                clipData = ClipData.newRawUri("Geschichten App-Update", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // No resolveActivity() gate: Android package visibility can hide handlers.
            installPackage.launch(intent)
            appViewModel.updateInstallerOpened()
        } catch (_: android.content.ActivityNotFoundException) {
            appViewModel.updateInstallerUnavailable()
        } catch (_: SecurityException) {
            appViewModel.updateInstallPermissionRequired()
        } catch (_: IllegalArgumentException) {
            appViewModel.updateInstallerUnavailable()
        }
    }
}
