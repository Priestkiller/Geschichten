package dev.vincent.geschichten.updates

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.StatFs
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * User-triggered updater for this exact installed Android package. Model setup and
 * chat never call this class. Check/download/install are separate explicit actions.
 * Release metadata is untrusted until length, hash, package and signer checks pass.
 */
class AppUpdater(context: Context, defaultRepository: String, defaultIncludePrerelease: Boolean) : AutoCloseable {
    private val app = context.applicationContext
    private val preferences = app.getSharedPreferences("app_update_settings", Context.MODE_PRIVATE)
    private val directory = File(app.cacheDir, "app_updates")
    private val pendingFile = File(directory, "pending-update.json")
    private val transport = UpdateTransport(File(app.cacheDir, "app_update_metadata"))
    private val operation = Mutex()
    private val job = AtomicReference<Job?>(null)
    private val closed = AtomicBoolean(false)
    private val gson = Gson()
    private val installedAtStart = installedIdentity()
    private val _state = MutableStateFlow(
        AppUpdateState(
            repository = preferences.getString("repository", defaultRepository).orEmpty(),
            includePrerelease = preferences.getBoolean("include_prerelease", defaultIncludePrerelease),
            installedVersion = installedAtStart.versionName,
            installedVersionCode = installedAtStart.versionCode,
        ).let { initial -> initial.copy(
            stage = if (initial.repository.isBlank()) UpdateStage.UNCONFIGURED else UpdateStage.IDLE,
            detail = if (initial.repository.isBlank()) "Für Updates fehlt noch die GitHub-Quelle."
            else "Updates werden nur gesucht, wenn du den Button antippst.",
        ) }
    )
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    /** Local recovery only: this NEVER performs a network request at app launch. */
    suspend fun restoreDownloadedUpdate() = withContext(Dispatchers.IO) {
        operation.withLock {
            if (closed.get() || !pendingFile.isFile || pendingFile.length() > UpdateManifest.MAX_MANIFEST_BYTES) return@withLock
            try {
                val update = gson.fromJson(pendingFile.readText(), AvailableUpdate::class.java) ?: return@withLock
                requireCachedMetadata(update)
                if (update.versionCode <= installedAtStart.versionCode) {
                    fileFor(update).delete()
                    partialFor(update).delete()
                    pendingFile.delete()
                    return@withLock
                }
                if (update.prerelease && !_state.value.includePrerelease) return@withLock
                val file = fileFor(update)
                if (file.isFile && file.length() == update.byteCount) {
                    _state.value = _state.value.copy(
                        stage = UpdateStage.READY_TO_INSTALL, available = update,
                        downloadedBytes = update.byteCount, progress = 1f,
                        detail = "Das heruntergeladene Update liegt bereit. Vor der Installation wird es erneut geprüft.",
                    )
                } else {
                    _state.value = _state.value.copy(
                        stage = UpdateStage.AVAILABLE, available = update,
                        downloadedBytes = partialFor(update).length().coerceIn(0, update.byteCount),
                        detail = "Zuletzt gefunden: ${update.versionName}. Du kannst den Download fortsetzen oder erneut nach Updates suchen.",
                    )
                }
            } catch (_: Exception) {
                // A stale/bad cache never makes the app believe it is up to date.
                pendingFile.delete()
            }
        }
    }

    suspend fun configure(repository: String, includePrerelease: Boolean) = withContext(Dispatchers.IO) {
        operation.withLock {
            ensureOpen()
            val valid = GitHubRepository.parse(repository).toString()
            if (!preferences.edit().putString("repository", valid).putBoolean("include_prerelease", includePrerelease).commit()) {
                throw AppUpdateException("Die GitHub-Quelle konnte nicht gespeichert werden. Bitte prüfe den freien Speicher.")
            }
            pendingFile.delete()
            _state.value = _state.value.copy(
                repository = valid, includePrerelease = includePrerelease, stage = UpdateStage.IDLE,
                available = null, checkedAt = null, progress = 0f, downloadedBytes = 0,
                detail = "GitHub-Quelle gespeichert. Du kannst jetzt nach Updates suchen.",
            )
        }
    }

    suspend fun setIncludePrerelease(include: Boolean) = withContext(Dispatchers.IO) {
        operation.withLock {
            ensureOpen()
            preferences.edit().putBoolean("include_prerelease", include).apply()
            pendingFile.delete()
            _state.value = _state.value.copy(
                includePrerelease = include,
                stage = if (_state.value.repository.isBlank()) UpdateStage.UNCONFIGURED else UpdateStage.IDLE,
                available = null, checkedAt = null, progress = 0f, downloadedBytes = 0,
                detail = if (include) "Die nächste Suche berücksichtigt auch veröffentlichte Testversionen."
                else "Die nächste Suche berücksichtigt nur reguläre Veröffentlichungen.",
            )
        }
    }

    suspend fun checkForUpdates() = work(UpdateStage.CHECKING, "Veröffentlichte App-Updates werden bei GitHub geprüft …") {
        val repository = source()
        val installed = installedIdentity()
        val releases = mutableListOf<GitHubRelease>()
        var page = 1
        while (true) {
            val url = "https://api.github.com${repository.apiPath}?per_page=100&page=$page"
            val (json, link) = transport.text(url, 4 * 1024 * 1024, cache = true)
            releases += UpdateManifest.releases(json, _state.value.includePrerelease)
            val hasMore = link.orEmpty().split(',').any { it.contains("rel=\"next\"") }
            if (!hasMore) break
            // We increment our own validated URL rather than following arbitrary Link URLs.
            if (++page > 5) throw AppUpdateException("Diese Quelle hat sehr viele Veröffentlichungen. Die Suche konnte nicht vollständig geprüft werden.")
        }
        val candidates = mutableListOf<AvailableUpdate>()
        var descriptionsRead = 0
        for (release in releases) {
            currentCoroutineContext().ensureActive()
            val manifests = release.assets.filter { it.name == UpdateManifest.FILE_NAME }
            if (manifests.isEmpty()) continue // Other apps/APKs/tags are not updates for this app.
            if (manifests.size != 1) throw AppUpdateException("Eine Veröffentlichung enthält mehrdeutige Updatebeschreibungen.")
            if (++descriptionsRead > 50) throw AppUpdateException("Zu viele App-Veröffentlichungen für eine vollständige Prüfung. Bitte die Updatequelle eingrenzen.")
            val manifest = manifests.single()
            if (manifest.bytes !in 1..UpdateManifest.MAX_MANIFEST_BYTES.toLong()) throw AppUpdateException("Die Updatebeschreibung hat eine ungültige Größe.")
            UpdateManifest.requireReleaseAsset(manifest.url, repository, release.tag, UpdateManifest.FILE_NAME)
            val (json, _) = transport.text(manifest.url, UpdateManifest.MAX_MANIFEST_BYTES, cache = true)
            val actualBytes = json.toByteArray(Charsets.UTF_8)
            if (actualBytes.size.toLong() != manifest.bytes) throw AppUpdateException("Die Updatebeschreibung wurde unvollständig übertragen.")
            UpdateManifest.verifyDigestMetadata(manifest.digest, sha256(actualBytes))
            UpdateManifest.candidate(json, release, repository, app.packageName)?.let(candidates::add)
        }
        val newestPublished = UpdateManifest.newest(candidates)
        val latest = UpdateManifest.newest(candidates.filter { it.minSdk <= Build.VERSION.SDK_INT })
        val base = _state.value.copy(
            installedVersion = installed.versionName, installedVersionCode = installed.versionCode,
            checkedAt = System.currentTimeMillis(), progress = 0f, downloadedBytes = 0,
        )
        when {
            newestPublished != null && newestPublished.versionCode > installed.versionCode &&
                (latest == null || latest.versionCode <= installed.versionCode) -> _state.value = base.copy(
                    stage = UpdateStage.NO_RELEASE, available = null,
                    detail = "Version ${newestPublished.versionName} benötigt eine neuere Android-Version. Für dein Handy ist noch kein neueres passendes Update veröffentlicht.",
                )
            latest == null -> _state.value = base.copy(
                stage = UpdateStage.NO_RELEASE, available = null,
                detail = "In dieser Quelle ist noch keine passende ${if (base.includePrerelease) "App-Version" else "reguläre App-Version"} mit Updatebeschreibung veröffentlicht.",
            )
            latest.versionCode <= installed.versionCode -> {
                pendingFile.delete()
                _state.value = base.copy(stage = UpdateStage.UP_TO_DATE, available = null, detail = "Du hast Version ${installed.versionName}. Es ist kein neueres passendes Update veröffentlicht.")
            }
            else -> {
                persistPending(latest)
                _state.value = base.copy(stage = UpdateStage.AVAILABLE, available = latest, detail = "Version ${latest.versionName}${if (latest.prerelease) " (Test)" else ""} ist verfügbar.")
            }
        }
    }

    suspend fun downloadUpdate() = work(UpdateStage.DOWNLOADING, "Das App-Update wird heruntergeladen …") {
        val update = candidate()
        prepareDirectory()
        persistPending(update)
        val destination = fileFor(update)
        val partial = partialFor(update)
        if (destination.isFile && destination.length() == update.byteCount) {
            verify(destination, update)
        } else {
            val remaining = update.byteCount - partial.length().coerceIn(0, update.byteCount)
            if (StatFs(directory.absolutePath).availableBytes < remaining + 96L * 1024 * 1024) {
                throw AppUpdateException("Für das App-Update ist zu wenig freier Speicher vorhanden. Bitte mache etwas Speicher frei.")
            }
            transport.download(update, partial) { bytes ->
                _state.value = _state.value.copy(progress = bytes.toFloat() / update.byteCount, downloadedBytes = bytes)
            }
            verify(partial, update)
            currentCoroutineContext().ensureActive()
            Files.move(partial.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        }
        _state.value = _state.value.copy(
            stage = UpdateStage.READY_TO_INSTALL, progress = 1f, downloadedBytes = update.byteCount,
            detail = "Update geprüft. Mit „Update installieren“ öffnest du die Android-Installation.",
        )
    }

    /** Revalidate immediately before sharing the APK, including after a Settings round trip. */
    suspend fun prepareInstall(): File = work(UpdateStage.VERIFYING, "Das App-Update wird vor der Installation geprüft …") {
        val update = candidate()
        val file = fileFor(update)
        if (!file.isFile) throw AppUpdateException("Die Update-Datei fehlt. Bitte lade das Update erneut herunter.")
        verify(file, update)
        _state.value = _state.value.copy(stage = UpdateStage.READY_TO_INSTALL, progress = 1f, detail = "Update geprüft. Android fragt dich jetzt nach der Installation.")
        file
    }

    fun installerOpened() {
        _state.value = _state.value.copy(stage = UpdateStage.INSTALLER_OPENED, detail = "Die Android-Installation wurde geöffnet. Eine erfolgreiche Installation ist noch nicht bestätigt.")
    }

    fun installPermissionRequired() {
        _state.value = _state.value.copy(stage = UpdateStage.READY_TO_INSTALL, detail = "Android muss die Installation aus dieser App erlauben. Tippe erneut auf „Update installieren“, um die Einstellung zu öffnen.")
    }

    fun installerUnavailable() {
        _state.value = _state.value.copy(stage = UpdateStage.READY_TO_INSTALL, detail = "Die Android-Installation konnte nicht geöffnet werden. Das geprüfte Update bleibt gespeichert.")
    }

    suspend fun installerReturned() = withContext(Dispatchers.IO) {
        operation.withLock {
            if (closed.get()) return@withLock
            val installed = installedIdentity()
            val update = _state.value.available
            if (update != null && installed.versionCode >= update.versionCode) {
                pendingFile.delete()
                fileFor(update).delete()
                _state.value = _state.value.copy(
                    installedVersion = installed.versionName, installedVersionCode = installed.versionCode,
                    available = null, stage = UpdateStage.IDLE,
                    detail = "Version ${installed.versionName} ist installiert.",
                )
            } else {
                _state.value = _state.value.copy(
                    stage = if (update != null) UpdateStage.READY_TO_INSTALL else UpdateStage.IDLE,
                    detail = "Das Update ist noch nicht installiert. Du kannst die Android-Installation erneut öffnen.",
                )
            }
        }
    }

    fun cancel() {
        job.get()?.cancel(CancellationException("Update-Vorgang angehalten."))
        transport.cancel()
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) cancel()
    }

    private suspend fun <T> work(stage: UpdateStage, detail: String, block: suspend () -> T): T = withContext(Dispatchers.IO) {
        operation.withLock {
            ensureOpen()
            val currentJob = currentCoroutineContext()[Job]!!
            job.set(currentJob)
            _state.value = _state.value.copy(stage = stage, detail = detail, progress = 0f)
            try {
                block()
            } catch (cancelled: CancellationException) {
                _state.value = _state.value.copy(
                    stage = if (_state.value.available != null) UpdateStage.AVAILABLE
                    else if (_state.value.repository.isBlank()) UpdateStage.UNCONFIGURED else UpdateStage.IDLE,
                    detail = "Update-Vorgang angehalten. Du kannst ihn später fortsetzen.",
                )
                throw cancelled
            } catch (error: Exception) {
                if (!currentJob.isActive) {
                    _state.value = _state.value.copy(stage = if (_state.value.available != null) UpdateStage.AVAILABLE else UpdateStage.IDLE, detail = "Update-Vorgang angehalten.")
                    throw CancellationException("Update-Vorgang angehalten.", error)
                }
                val failure = when (error) {
                    is AppUpdateException -> error
                    is SocketTimeoutException -> AppUpdateException("GitHub antwortet gerade zu langsam. Bitte versuche es erneut.", error)
                    else -> AppUpdateException("Der Update-Vorgang konnte nicht abgeschlossen werden. Prüfe Internetverbindung und freien Speicher und versuche es erneut.", error)
                }
                _state.value = _state.value.copy(stage = if (_state.value.repository.isBlank()) UpdateStage.UNCONFIGURED else UpdateStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } finally {
                job.compareAndSet(currentJob, null)
                transport.cancel()
            }
        }
    }

    private suspend fun verify(file: File, update: AvailableUpdate) {
        _state.value = _state.value.copy(stage = UpdateStage.VERIFYING, detail = "Datei, App-Version und Signatur werden geprüft …", progress = 0f)
        try {
            if (file.length() != update.byteCount) throw AppUpdateException("Die Update-Datei ist unvollständig. Bitte lade sie erneut herunter.")
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(256 * 1024)
            var readBytes = 0L
            var lastPublish = 0L
            FileInputStream(file).buffered().use { input ->
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                    readBytes += count
                    val now = System.nanoTime()
                    if (now - lastPublish > 100_000_000L || readBytes == update.byteCount) {
                        _state.value = _state.value.copy(progress = readBytes.toFloat() / update.byteCount, downloadedBytes = update.byteCount)
                        lastPublish = now
                    }
                }
            }
            if (readBytes != update.byteCount || hex(digest.digest()) != update.sha256) throw AppUpdateException("Die Prüfsumme des Updates stimmt nicht. Die Datei wurde verworfen; bitte erneut herunterladen.")
            currentCoroutineContext().ensureActive()
            @Suppress("DEPRECATION")
            val archive = app.packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
                ?: throw AppUpdateException("Die Datei ist keine gültig signierte Android-App.")
            ApkIdentityPolicy.requireCompatible(installedIdentity(), identity(archive), update, Build.VERSION.SDK_INT)
        } catch (cancelled: CancellationException) {
            throw cancelled // Keep a complete/partial file for safe re-verification.
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    private fun installedIdentity(): ApkIdentity {
        @Suppress("DEPRECATION")
        val info = app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        return identity(info)
    }

    private fun identity(info: PackageInfo) = ApkIdentity(
        packageName = info.packageName,
        versionCode = info.longVersionCode,
        versionName = info.versionName.orEmpty(),
        minSdk = info.applicationInfo?.minSdkVersion ?: 0,
        signerSha256 = info.signingInfo?.apkContentsSigners?.map { sha256(it.toByteArray()) }?.toSet().orEmpty(),
    )

    private fun source(): GitHubRepository {
        if (_state.value.repository.isBlank()) throw AppUpdateException("Bitte hinterlege zuerst die GitHub-Quelle für diese App.")
        return GitHubRepository.parse(_state.value.repository)
    }

    private fun candidate(): AvailableUpdate {
        val update = _state.value.available ?: throw AppUpdateException("Bitte suche zuerst nach einem veröffentlichten App-Update.")
        requireCachedMetadata(update)
        return update
    }

    private fun requireCachedMetadata(update: AvailableUpdate) {
        val source = source()
        if (update.repository != source.toString() || update.versionCode !in 1..Int.MAX_VALUE.toLong() ||
            update.byteCount !in 1..UpdateManifest.MAX_APK_BYTES || !update.sha256.matches(Regex("[0-9a-f]{64}")) ||
            update.versionName.isBlank() || update.versionName.length > 80 || update.minSdk !in 21..200 ||
            update.changelog.length > 4000) throw AppUpdateException("Die gespeicherten Updateangaben sind ungültig. Bitte erneut nach Updates suchen.")
        source.requireAssetUrl(update.downloadUrl)
        UpdateManifest.requireReleaseAsset(update.downloadUrl, source, update.releaseTag, update.assetName)
    }

    private fun persistPending(update: AvailableUpdate) {
        prepareDirectory()
        val temporary = File(directory, "pending-update.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(gson.toJson(update).toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        Files.move(temporary.toPath(), pendingFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun prepareDirectory() {
        if (!directory.isDirectory && !directory.mkdirs()) throw AppUpdateException("Der Speicherordner für das Update konnte nicht angelegt werden.")
    }

    private fun fileFor(update: AvailableUpdate) = File(directory, "update-${update.versionCode}-${update.sha256.take(16)}.apk")
    private fun partialFor(update: AvailableUpdate) = File(directory, "update-${update.versionCode}-${update.sha256.take(16)}.part.apk")
    private fun ensureOpen() { if (closed.get()) throw CancellationException("Die App wurde geschlossen.") }
    private fun sha256(bytes: ByteArray) = hex(MessageDigest.getInstance("SHA-256").digest(bytes))
    private fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
