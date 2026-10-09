package dev.vincent.geschichten.updates

import org.junit.Assert.*
import org.junit.Test

class UpdatePolicyTest {
    private val repo = GitHubRepository.parse("example/Geschichten")
    private val hash = "a".repeat(64)
    private val asset = GitHubAsset("Geschichten-0.2.0.apk", "https://github.com/example/Geschichten/releases/download/v0.2.0/Geschichten-0.2.0.apk", 123456, "sha256:$hash")
    private val release = GitHubRelease("v0.2.0", "Neue Figuren", true, listOf(asset))
    private val json = """{"schemaVersion":1,"applicationId":"dev.vincent.geschichten","versionCode":2,"versionName":"0.2.0","channel":"test","minSdk":31,"apk":{"assetName":"Geschichten-0.2.0.apk","size":123456,"sha256":"$hash"}}"""
    private fun update() = UpdateManifest.candidate(json, release, repo, "dev.vincent.geschichten")!!

    @Test fun githubLinkNormalizesToExplicitRepository() {
        assertEquals("example/Geschichten", GitHubRepository.parse("https://github.com/example/Geschichten.git/").toString())
    }

    @Test fun arbitraryOrCredentialedSourceIsRejected() {
        listOf("https://evil.example/example/Geschichten", "https://token@github.com/example/Geschichten", "example/../Geschichten", "http://github.com/example/Geschichten").forEach { value ->
            assertThrows(AppUpdateException::class.java) { GitHubRepository.parse(value) }
        }
    }

    @Test fun validAppManifestSelectsExactAsset() {
        val selected = update()
        assertEquals(2L, selected.versionCode)
        assertEquals(asset.url, selected.downloadUrl)
        assertEquals(hash, selected.sha256)
    }

    @Test fun anotherAppsManifestIsIgnored() {
        assertNull(UpdateManifest.candidate(json.replace("dev.vincent.geschichten", "other.app"), release, repo, "dev.vincent.geschichten"))
    }

    @Test fun fractionalVersionCodeIsRejected() {
        assertThrows(AppUpdateException::class.java) { UpdateManifest.candidate(json.replace("\"versionCode\":2", "\"versionCode\":2.5"), release, repo, "dev.vincent.geschichten") }
    }

    @Test fun manifestAndGithubDigestMustAgree() {
        assertThrows(AppUpdateException::class.java) {
            UpdateManifest.candidate(json, release.copy(assets = listOf(asset.copy(digest = "sha256:${"b".repeat(64)}"))), repo, "dev.vincent.geschichten")
        }
    }

    @Test fun absentGithubDigestStillRequiresManifestHash() {
        assertNotNull(UpdateManifest.candidate(json, release.copy(assets = listOf(asset.copy(digest = null))), repo, "dev.vincent.geschichten"))
        assertThrows(AppUpdateException::class.java) {
            UpdateManifest.candidate(json.replace(hash, ""), release.copy(assets = listOf(asset.copy(digest = null))), repo, "dev.vincent.geschichten")
        }
    }

    @Test fun assetFromOtherReleaseIsRejected() {
        assertThrows(AppUpdateException::class.java) {
            UpdateManifest.candidate(json, release.copy(assets = listOf(asset.copy(url = asset.url.replace("/v0.2.0/", "/v9.9.9/")))), repo, "dev.vincent.geschichten")
        }
    }

    @Test fun channelAndReleaseMustAgree() {
        assertThrows(AppUpdateException::class.java) { UpdateManifest.candidate(json, release.copy(prerelease = false), repo, "dev.vincent.geschichten") }
    }

    @Test fun draftsAndUnselectedPrereleasesAreIgnored() {
        val items = """[
            {"draft":true,"prerelease":false},
            {"draft":false,"prerelease":true,"published_at":"2026-10-03","tag_name":"test","body":"","assets":[]},
            {"draft":false,"prerelease":false,"published_at":"2026-10-03","tag_name":"stable","body":"","assets":[]}
        ]"""
        assertEquals(listOf("stable"), UpdateManifest.releases(items, false).map { it.tag })
        assertEquals(listOf("test", "stable"), UpdateManifest.releases(items, true).map { it.tag })
    }

    @Test fun androidCodeWinsOverVersionNameOrdering() {
        val newest = UpdateManifest.newest(listOf(update().copy(versionCode = 9, versionName = "9.9"), update().copy(versionCode = 10, versionName = "1.10")))
        assertEquals(10L, newest?.versionCode)
    }

    @Test fun conflictingFilesWithSameCodeAreRejected() {
        assertThrows(AppUpdateException::class.java) { UpdateManifest.newest(listOf(update(), update().copy(sha256 = "b".repeat(64)))) }
    }

    @Test fun redirectsRequireExactHttpsHostWithoutCredentials() {
        UpdateManifest.requireDownloadHost("https://release-assets.githubusercontent.com/github-production-release-asset/1/file?signature=public")
        listOf("http://github.com/x/y", "https://github.com.evil.example/x", "https://user@github.com/x", "https://github.com:8443/x").forEach { url ->
            assertThrows(AppUpdateException::class.java) { UpdateManifest.requireDownloadHost(url) }
        }
    }

    @Test fun exactInstalledSignerAndPackageAreRequired() {
        val installed = ApkIdentity("dev.vincent.geschichten", 1, "0.1.0", 31, setOf("trusted"))
        val good = installed.copy(versionCode = 2, versionName = "0.2.0")
        ApkIdentityPolicy.requireCompatible(installed, good, update(), 35)
        assertThrows(AppUpdateException::class.java) { ApkIdentityPolicy.requireCompatible(installed, good.copy(packageName = "other.app"), update(), 35) }
        assertThrows(AppUpdateException::class.java) { ApkIdentityPolicy.requireCompatible(installed, good.copy(signerSha256 = setOf("attacker")), update(), 35) }
        assertThrows(AppUpdateException::class.java) { ApkIdentityPolicy.requireCompatible(installed, good.copy(signerSha256 = setOf("trusted", "extra")), update(), 35) }
    }

    @Test fun downgradeAndMismatchedArchiveCodeAreRejected() {
        val installed = ApkIdentity("dev.vincent.geschichten", 2, "0.2.0", 31, setOf("trusted"))
        assertThrows(AppUpdateException::class.java) { ApkIdentityPolicy.requireCompatible(installed, installed, update(), 35) }
        assertThrows(AppUpdateException::class.java) { ApkIdentityPolicy.requireCompatible(installed.copy(versionCode = 1), installed.copy(versionCode = 9), update(), 35) }
    }

    @Test fun ignoredRangeRestartsAndMismatchedRangeCannotAppend() {
        assertEquals(0L, UpdateDownloadProtocol.validate(200, 100, 1000, null, 1000, null).offset)
        assertEquals(100L, UpdateDownloadProtocol.validate(206, 100, 1000, "bytes 100-999/1000", 900, "identity").offset)
        assertThrows(AppUpdateException::class.java) { UpdateDownloadProtocol.validate(206, 100, 1000, "bytes 0-999/1000", 1000, null) }
        assertThrows(AppUpdateException::class.java) { UpdateDownloadProtocol.validate(206, 100, 1000, "bytes 100-999/1000", 1000, null) }
        assertThrows(AppUpdateException::class.java) { UpdateDownloadProtocol.validate(200, 0, 1000, null, 1000, "gzip") }
    }
}
