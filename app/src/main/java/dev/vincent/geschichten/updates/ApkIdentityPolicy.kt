package dev.vincent.geschichten.updates

internal data class ApkIdentity(
    val packageName: String,
    val versionCode: Long,
    val versionName: String,
    val minSdk: Int,
    val signerSha256: Set<String>,
)

internal object ApkIdentityPolicy {
    fun requireCompatible(installed: ApkIdentity, archive: ApkIdentity, update: AvailableUpdate, deviceSdk: Int) {
        if (archive.packageName != installed.packageName) throw AppUpdateException("Diese APK gehört zu einer anderen App und wird nicht installiert.")
        if (archive.versionCode != update.versionCode || archive.versionName != update.versionName) {
            throw AppUpdateException("Die Version in der APK stimmt nicht mit dem angekündigten Update überein.")
        }
        if (archive.versionCode <= installed.versionCode) throw AppUpdateException("Diese App-Version ist bereits installiert oder älter als dein Stand.")
        if (archive.minSdk != update.minSdk || archive.minSdk > deviceSdk) throw AppUpdateException("Dieses Update passt nicht zur Android-Version auf deinem Handy.")
        // Exact CURRENT signer set equality intentionally rejects key rotations.
        // A mere intersection with historical signing certificates is not enough.
        if (installed.signerSha256.isEmpty() || archive.signerSha256.isEmpty() || archive.signerSha256 != installed.signerSha256) {
            throw AppUpdateException("Die Signatur dieses Updates passt nicht zu deiner installierten App. Die Installation wurde gestoppt.")
        }
    }
}
