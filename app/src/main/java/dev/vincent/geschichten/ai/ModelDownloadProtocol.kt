package dev.vincent.geschichten.ai

import java.io.IOException

/** Pure protocol validation, separated so corrupt resume paths can be unit tested. */
internal object ModelDownloadProtocol {
    data class Body(val writeOffset: Long, val byteCount: Long)

    fun validate(
        status: Int,
        offset: Long,
        expectedBytes: Long,
        contentRange: String?,
        contentLength: Long,
        contentEncoding: String?,
    ): Body {
        if (expectedBytes <= 0 || offset !in 0 until expectedBytes) {
            throw IOException("Ungültiger Download-Fortschritt.")
        }
        if (!contentEncoding.isNullOrBlank() && !contentEncoding.equals("identity", true)) {
            throw IOException("Der Server hat ein unerwartetes Dateiformat geliefert.")
        }
        val body = when (status) {
            200 -> {
                if (!contentRange.isNullOrBlank()) {
                    throw IOException("Der Server hat widersprüchliche Download-Daten geliefert.")
                }
                // A server may ignore Range. Never append that full body to a partial file.
                Body(writeOffset = 0, byteCount = expectedBytes)
            }
            206 -> {
                val match = Regex("bytes (\\d+)-(\\d+)/(\\d+)")
                    .matchEntire(contentRange?.trim().orEmpty())
                    ?: throw IOException("Der Server bestätigt den fortgesetzten Download nicht.")
                val first = match.groupValues[1].toLongOrNull()
                val last = match.groupValues[2].toLongOrNull()
                val total = match.groupValues[3].toLongOrNull()
                if (first != offset || total != expectedBytes || last != expectedBytes - 1) {
                    throw IOException("Der Server hat einen unpassenden Dateiausschnitt geliefert.")
                }
                Body(writeOffset = offset, byteCount = expectedBytes - offset)
            }
            401, 403 -> throw IOException(
                "Die öffentliche KI-Datei ist momentan nicht erreichbar. Bitte später erneut versuchen."
            )
            else -> throw IOException("Download nicht verfügbar (HTTP $status). Bitte erneut versuchen.")
        }
        if (contentLength >= 0 && contentLength != body.byteCount) {
            throw IOException("Die gemeldete Dateigröße stimmt nicht mit dem KI-Modell überein.")
        }
        return body
    }
}
