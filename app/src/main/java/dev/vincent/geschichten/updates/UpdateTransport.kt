package dev.vincent.geschichten.updates

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Public GitHub reads only. No account token, cookie, prompt, or chat text is sent. */
internal class UpdateTransport(private val metadataCache: File) {
    private val active = AtomicReference<HttpURLConnection?>(null)

    fun cancel() { active.getAndSet(null)?.disconnect() }

    suspend fun text(url: String, limit: Int, cache: Boolean = false): Pair<String, String?> {
        metadataCache.mkdirs()
        val key = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val bodyFile = File(metadataCache, "$key.json")
        val tagFile = File(metadataCache, "$key.etag")
        val linkFile = File(metadataCache, "$key.link")
        val etag = if (cache && bodyFile.isFile && bodyFile.length() <= limit && tagFile.isFile && tagFile.length() < 1000 &&
            linkFile.isFile && linkFile.length() < 8000) tagFile.readText() else null
        val connection = open(url, etag = etag)
        try {
            if (connection.responseCode == 304 && etag != null && bodyFile.isFile) {
                return bodyFile.readText() to linkFile.takeIf { it.isFile && it.length() < 8000 }?.readText()?.ifBlank { null }
            }
            requireSuccess(connection)
            if (connection.responseCode != 200) throw AppUpdateException("GitHub hat keine vollständige Updatebeschreibung geliefert.")
            if (connection.contentLengthLong > limit) throw AppUpdateException("Die Updatebeschreibung ist unerwartet groß.")
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            connection.inputStream.use { input ->
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (bytes.size() + count > limit) throw AppUpdateException("Die Updatebeschreibung ist unerwartet groß.")
                    bytes.write(buffer, 0, count)
                }
            }
            val text = bytes.toString(Charsets.UTF_8.name())
            val link = connection.getHeaderField("Link")?.take(8000)
            if (cache) {
                // Corrupt/stale cache can only produce a visible check failure, never
                // installation; every APK is independently hashed and signer-checked.
                bodyFile.writeText(text)
                tagFile.writeText(connection.getHeaderField("ETag").orEmpty().take(999))
                linkFile.writeText(link.orEmpty())
            }
            return text to link
        } finally {
            active.compareAndSet(connection, null)
            connection.disconnect()
        }
    }

    suspend fun download(update: AvailableUpdate, partial: File, progress: (Long) -> Unit) {
        if (partial.length() > update.byteCount) partial.delete()
        val offset = partial.length()
        if (offset == update.byteCount) return
        val connection = open(update.downloadUrl, range = "bytes=$offset-${update.byteCount - 1}")
        try {
            requireSuccess(connection)
            val body = UpdateDownloadProtocol.validate(
                status = connection.responseCode, offset = offset, size = update.byteCount,
                range = connection.getHeaderField("Content-Range"), length = connection.contentLengthLong,
                encoding = connection.getHeaderField("Content-Encoding"),
            )
            var count = body.offset
            var lastPublish = 0L
            val buffer = ByteArray(256 * 1024)
            FileOutputStream(partial, body.offset > 0).use { output ->
                connection.inputStream.buffered().use { input ->
                    while (count < update.byteCount) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), update.byteCount - count).toInt())
                        if (read < 0) throw AppUpdateException("Der Update-Download wurde unterbrochen. Du kannst ihn fortsetzen.")
                        output.write(buffer, 0, read)
                        count += read
                        val now = System.nanoTime()
                        if (now - lastPublish >= 100_000_000L || count == update.byteCount) {
                            progress(count)
                            lastPublish = now
                        }
                    }
                    if (input.read() != -1) throw AppUpdateException("Der Update-Server hat mehr Daten als angekündigt geliefert.")
                }
                output.fd.sync()
            }
        } finally {
            active.compareAndSet(connection, null)
            connection.disconnect()
        }
    }

    private suspend fun open(initial: String, etag: String? = null, range: String? = null): HttpURLConnection {
        var url = initial
        repeat(7) {
            currentCoroutineContext().ensureActive()
            UpdateManifest.requireDownloadHost(url)
            val connection = URL(url).openConnection() as HttpURLConnection
            active.set(connection)
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 20_000
            connection.readTimeout = 30_000
            connection.useCaches = false
            connection.setRequestProperty("User-Agent", "Geschichten-Android-Updater")
            connection.setRequestProperty("Accept-Encoding", "identity")
            if (URL(url).host == "api.github.com") {
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("X-GitHub-Api-Version", "2026-03-10")
            }
            if (!etag.isNullOrBlank()) connection.setRequestProperty("If-None-Match", etag)
            if (range != null) connection.setRequestProperty("Range", range)
            val status = connection.responseCode
            if (status !in setOf(301, 302, 303, 307, 308)) return connection
            val location = connection.getHeaderField("Location")
            active.compareAndSet(connection, null)
            connection.disconnect()
            if (location.isNullOrBlank()) throw AppUpdateException("Die GitHub-Weiterleitung ist unvollständig.")
            url = URL(URL(url), location).toString()
        }
        throw AppUpdateException("GitHub hat zu viele Weiterleitungen geliefert.")
    }

    private fun requireSuccess(connection: HttpURLConnection) {
        when (connection.responseCode) {
            200, 206 -> Unit
            403, 429 -> throw AppUpdateException(
                if (connection.responseCode == 429 || connection.getHeaderField("X-RateLimit-Remaining") == "0")
                    "GitHub begrenzt gerade die Update-Abfragen. Bitte versuche es später erneut."
                else "GitHub erlaubt den Zugriff auf diese Update-Datei nicht. Die Quelle muss öffentlich erreichbar sein."
            )
            404 -> throw AppUpdateException("Die GitHub-Quelle oder Update-Datei ist nicht öffentlich erreichbar. Bitte prüfe Besitzer und Repository.")
            else -> throw AppUpdateException("Die Updatesuche ist fehlgeschlagen (HTTP ${connection.responseCode}). Bitte erneut versuchen.")
        }
    }
}

internal object UpdateDownloadProtocol {
    data class Body(val offset: Long)
    fun validate(status: Int, offset: Long, size: Long, range: String?, length: Long, encoding: String?): Body {
        if (size <= 0 || offset !in 0 until size) throw AppUpdateException("Ungültiger Download-Fortschritt.")
        if (!encoding.isNullOrBlank() && !encoding.equals("identity", true)) throw AppUpdateException("Das Update wurde in einem unerwarteten Format übertragen.")
        val start = when (status) {
            200 -> {
                if (!range.isNullOrBlank()) throw AppUpdateException("Widersprüchliche Update-Downloaddaten.")
                0L // Server ignored Range: overwrite; never append a full body.
            }
            206 -> {
                val match = Regex("bytes (\\d+)-(\\d+)/(\\d+)").matchEntire(range.orEmpty().trim())
                    ?: throw AppUpdateException("Der Server bestätigt den fortgesetzten Update-Download nicht.")
                if (match.groupValues[1].toLongOrNull() != offset || match.groupValues[2].toLongOrNull() != size - 1 ||
                    match.groupValues[3].toLongOrNull() != size) throw AppUpdateException("Der Server liefert einen falschen Update-Dateiausschnitt.")
                offset
            }
            else -> throw AppUpdateException("Unvollständige Update-Antwort (HTTP $status).")
        }
        if (length >= 0 && length != size - start) throw AppUpdateException("Die Update-Dateigröße stimmt nicht überein.")
        return Body(start)
    }
}
