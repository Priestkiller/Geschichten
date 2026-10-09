package dev.vincent.geschichten.updates

import java.io.IOException
import java.net.URI

enum class UpdateStage {
    UNCONFIGURED, IDLE, CHECKING, NO_RELEASE, UP_TO_DATE, AVAILABLE,
    DOWNLOADING, VERIFYING, READY_TO_INSTALL, INSTALLER_OPENED, ERROR,
}

data class AvailableUpdate(
    val repository: String,
    val versionCode: Long,
    val versionName: String,
    val prerelease: Boolean,
    val releaseTag: String,
    val changelog: String,
    val minSdk: Int,
    val assetName: String,
    val downloadUrl: String,
    val byteCount: Long,
    val sha256: String,
)

data class AppUpdateState(
    val repository: String = "",
    val includePrerelease: Boolean = true,
    val installedVersion: String = "",
    val installedVersionCode: Long = 0,
    val stage: UpdateStage = UpdateStage.UNCONFIGURED,
    val available: AvailableUpdate? = null,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val checkedAt: Long? = null,
    val detail: String = "Für Updates fehlt noch die GitHub-Quelle.",
) {
    val working: Boolean get() = stage in setOf(UpdateStage.CHECKING, UpdateStage.DOWNLOADING, UpdateStage.VERIFYING)
}

class AppUpdateException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** Public GitHub owner/repo only; no tokens, enterprise hosts or arbitrary URLs. */
class GitHubRepository private constructor(val owner: String, val name: String) {
    override fun toString() = "$owner/$name"
    val apiPath: String get() = "/repos/$owner/$name/releases"

    fun requireAssetUrl(value: String) {
        val uri = try { URI(value) } catch (_: Exception) { throw AppUpdateException("Ungültige GitHub-Dateiadresse.") }
        val prefix = "/$owner/$name/releases/download/"
        if (uri.scheme != "https" || !uri.host.equals("github.com", true) || uri.port != -1 || uri.userInfo != null ||
            uri.fragment != null || uri.query != null || !uri.rawPath.startsWith(prefix, ignoreCase = true) ||
            uri.rawPath.length <= prefix.length || uri.path.split('/').any { it == "." || it == ".." }) {
            throw AppUpdateException("Die Update-Datei gehört nicht zur eingestellten GitHub-Quelle.")
        }
    }

    companion object {
        fun parse(raw: String): GitHubRepository {
            var value = raw.trim().trimEnd('/')
            if (value.startsWith("https://", true)) {
                val uri = try { URI(value) } catch (_: Exception) { throw invalidSource() }
                if (uri.scheme != "https" || !uri.host.equals("github.com", true) || uri.port != -1 ||
                    uri.userInfo != null || uri.query != null || uri.fragment != null) throw invalidSource()
                value = uri.path.trim('/')
            }
            value = value.removeSuffix(".git")
            val pieces = value.split('/')
            if (pieces.size != 2 || !pieces[0].matches(Regex("[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?")) ||
                !pieces[1].matches(Regex("[A-Za-z0-9_.-]{1,100}")) || pieces[1] in setOf(".", "..")) throw invalidSource()
            return GitHubRepository(pieces[0], pieces[1])
        }

        private fun invalidSource() = AppUpdateException("Bitte gib eine GitHub-Quelle als Besitzer/Repository ein, zum Beispiel über den Link zu eurem Repository.")
    }
}
