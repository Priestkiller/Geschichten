package dev.vincent.geschichten.updates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.util.Locale

internal data class GitHubAsset(val name: String, val url: String, val bytes: Long, val digest: String?)
internal data class GitHubRelease(val tag: String, val notes: String, val prerelease: Boolean, val assets: List<GitHubAsset>)

/** Parsing is pure Kotlin/JVM, so selection and trust rules can be tested without an emulator. */
internal object UpdateManifest {
    const val FILE_NAME = "geschichten-android-update.json"
    const val MAX_APK_BYTES = 1_073_741_824L
    const val MAX_MANIFEST_BYTES = 65_536

    fun releases(json: String, includePrerelease: Boolean): List<GitHubRelease> {
        val root = parse(json)
        if (!root.isJsonArray) throw AppUpdateException("GitHub hat keine gültige Veröffentlichungsliste geliefert.")
        return root.asJsonArray.mapNotNull { value ->
            val item = value.asObject()
            if (item.boolean("draft") || (item.boolean("prerelease") && !includePrerelease)) return@mapNotNull null
            if (!item.has("published_at") || item["published_at"].isJsonNull) return@mapNotNull null
            val assets = item["assets"]?.takeIf { it.isJsonArray }?.asJsonArray
                ?: throw AppUpdateException("In der GitHub-Veröffentlichung fehlen die Dateien.")
            GitHubRelease(
                tag = item.text("tag_name", 180),
                notes = item["body"]?.takeUnless { it.isJsonNull }?.asString.orEmpty().take(4000),
                prerelease = item.boolean("prerelease"),
                assets = assets.mapNotNull { asset ->
                    val entry = asset.asObject()
                    if (entry.text("state", 40) != "uploaded") return@mapNotNull null
                    GitHubAsset(
                        name = entry.text("name", 180), url = entry.text("browser_download_url", 2048),
                        bytes = entry.integer("size"),
                        digest = entry["digest"]?.takeUnless { it.isJsonNull }?.asString,
                    )
                },
            )
        }
    }

    fun candidate(json: String, release: GitHubRelease, repository: GitHubRepository, applicationId: String): AvailableUpdate? {
        val root = parse(json).asObject()
        if (root.text("applicationId", 180) != applicationId) return null
        if (root.integer("schemaVersion") != 1L) throw AppUpdateException("Dieses Updateformat wird von der App noch nicht unterstützt.")
        val versionCode = root.integer("versionCode")
        if (versionCode !in 1..Int.MAX_VALUE.toLong()) throw AppUpdateException("Die Update-Versionsnummer ist ungültig.")
        val channel = root.text("channel", 16)
        if (channel != if (release.prerelease) "test" else "stable") {
            throw AppUpdateException("Testkanal und GitHub-Veröffentlichung stimmen nicht überein.")
        }
        val minSdk = root.integer("minSdk")
        if (minSdk !in 21..200) throw AppUpdateException("Die Android-Mindestversion im Update ist ungültig.")
        val apk = root["apk"]?.asObject() ?: throw AppUpdateException("Die APK-Angaben im Update fehlen.")
        val assetName = apk.text("assetName", 160)
        if (!assetName.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,150}\\.apk"))) {
            throw AppUpdateException("Das Update benennt keine gültige Android-App-Datei.")
        }
        val size = apk.integer("size")
        if (size !in 1..MAX_APK_BYTES) throw AppUpdateException("Die Update-Dateigröße ist ungültig.")
        val sha256 = apk.text("sha256", 64).lowercase(Locale.ROOT)
        if (!sha256.matches(Regex("[0-9a-f]{64}"))) throw AppUpdateException("Die SHA-256-Prüfsumme des Updates fehlt oder ist ungültig.")
        val matches = release.assets.filter { it.name == assetName }
        if (matches.size != 1) throw AppUpdateException("Die angekündigte APK fehlt in der GitHub-Veröffentlichung.")
        val asset = matches.single()
        if (asset.bytes != size) throw AppUpdateException("Die Dateigröße von Updatebeschreibung und GitHub stimmt nicht überein.")
        verifyDigestMetadata(asset.digest, sha256)
        repository.requireAssetUrl(asset.url)
        requireReleaseAsset(asset.url, repository, release.tag, assetName)
        return AvailableUpdate(
            repository = repository.toString(), versionCode = versionCode,
            versionName = root.text("versionName", 80), prerelease = release.prerelease,
            releaseTag = release.tag, changelog = release.notes, minSdk = minSdk.toInt(),
            assetName = assetName, downloadUrl = asset.url, byteCount = size, sha256 = sha256,
        )
    }

    fun requireReleaseAsset(url: String, repository: GitHubRepository, tag: String, name: String) {
        repository.requireAssetUrl(url)
        val prefixLength = "/${repository.owner}/${repository.name}/releases/download/".length
        if (URI(url).path.substring(prefixLength) != "$tag/$name") {
            throw AppUpdateException("Die Update-Datei gehört nicht zur angekündigten Veröffentlichung.")
        }
    }

    fun newest(candidates: List<AvailableUpdate>): AvailableUpdate? {
        val version = candidates.maxOfOrNull { it.versionCode } ?: return null
        val newest = candidates.filter { it.versionCode == version }
        if (newest.map { listOf(it.sha256, it.versionName, it.byteCount.toString(), it.minSdk.toString()) }.distinct().size != 1) {
            throw AppUpdateException("GitHub enthält widersprüchliche Dateien für dieselbe App-Version. Bitte die Veröffentlichung prüfen.")
        }
        return newest.firstOrNull { !it.prerelease } ?: newest.first()
    }

    fun verifyDigestMetadata(digest: String?, expectedSha: String) {
        if (digest == null) return // Older GitHub assets may not have this extra field.
        if (!digest.matches(Regex("sha256:[0-9a-fA-F]{64}")) || !digest.substringAfter(':').equals(expectedSha, true)) {
            throw AppUpdateException("Die GitHub-Prüfsumme stimmt nicht mit der Updatebeschreibung überein.")
        }
    }

    fun requireDownloadHost(url: String) {
        val uri = try { URI(url) } catch (_: Exception) { throw AppUpdateException("Ungültige Updateadresse.") }
        val allowed = setOf("api.github.com", "github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com", "github-releases.githubusercontent.com")
        if (uri.scheme != "https" || uri.host?.lowercase(Locale.ROOT) !in allowed || uri.port != -1 || uri.userInfo != null || uri.fragment != null) {
            throw AppUpdateException("Eine unerwartete Download-Weiterleitung wurde blockiert.")
        }
    }

    internal fun parse(value: String): JsonElement = try { JsonParser.parseString(value) }
    catch (error: Exception) { throw AppUpdateException("Die Updatebeschreibung konnte nicht gelesen werden.", error) }

    private fun JsonElement.asObject(): JsonObject = if (isJsonObject) asJsonObject else throw AppUpdateException("Die Updatebeschreibung ist ungültig.")
    private fun JsonObject.text(name: String, max: Int): String {
        val value = get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
            ?: throw AppUpdateException("In der Updatebeschreibung fehlt $name.")
        if (value.isBlank() || value.length > max) throw AppUpdateException("Die Updateangabe $name ist ungültig.")
        return value
    }
    private fun JsonObject.integer(name: String): Long {
        val text = get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asString
            ?: throw AppUpdateException("In der Updatebeschreibung fehlt die Zahl $name.")
        if (!text.matches(Regex("[0-9]+"))) throw AppUpdateException("Die Updateangabe $name ist keine ganze Zahl.")
        return text.toLongOrNull() ?: throw AppUpdateException("Die Updatezahl $name ist zu groß.")
    }
    private fun JsonObject.boolean(name: String): Boolean = get(name)
        ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }?.asBoolean
        ?: throw AppUpdateException("In der GitHub-Veröffentlichung fehlt $name.")
}
