package dev.vincent.geschichten.updates

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Exercises the packaged provider metadata and real FileProvider path resolution. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateFileProviderIntegrationTest {
    @Test
    fun installationProviderSharesOnlyAnApkInsideItsDedicatedCache() {
        assumeTrue("AndroidX FileProvider expects Unix '/' paths; Windows Robolectric uses '\\'. See fileprovider-bytecode.txt; verify on Android/Linux.",File.separatorChar == '/')
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authority = "${context.packageName}.updates"
        val directory = File(context.cacheDir, "app_updates").also { it.mkdirs() }
        val apk = File.createTempFile("provider-check-", ".apk", directory)
        try {
            @Suppress("DEPRECATION")
            val provider = context.packageManager.getProviderInfo(
                ComponentName(context, UpdateFileProvider::class.java),
                PackageManager.GET_META_DATA,
            )
            // Android attaches the declared provider before use. Robolectric's plain
            // application context does not; attach it to this test's unique data dir
            // instead of reusing FileProvider's static root from an earlier test.
            UpdateFileProvider().attachInfo(context,provider)
            val uri = FileProvider.getUriForFile(context, authority, apk)
            assertEquals("content", uri.scheme)
            assertEquals(authority, uri.authority)
            assertTrue(uri.path.orEmpty().startsWith("/app_updates/"))
            assertFalse(provider.exported)
            assertTrue(provider.grantUriPermissions)
            assertEquals(authority, provider.authority)
        } finally {
            apk.delete()
        }
    }

    @Test
    fun otherCacheFilesAndPrivateStoryStorageCannotBeSharedByUpdateProvider() {
        assumeTrue("FileProvider path resolution requires Android/Linux filesystem separators.",File.separatorChar == '/')
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authority = "${context.packageName}.updates"
        val privateFile = File(context.filesDir, "provider-private-check.txt")
        val cacheFile = File(context.cacheDir, "provider-outside-update-cache.apk")
        val traversedFile = File(context.cacheDir, "app_updates/../provider-outside-update-cache.apk")
        for (file in listOf(privateFile, cacheFile, traversedFile)) {
            assertThrows(IllegalArgumentException::class.java) {
                FileProvider.getUriForFile(context, authority, file)
            }
        }
    }
    @Test fun packagedMetadataRestrictsTheProviderToTheUpdateCache(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        @Suppress("DEPRECATION")
        val info=context.packageManager.getProviderInfo(ComponentName(context,UpdateFileProvider::class.java),PackageManager.GET_META_DATA)
        assertFalse(info.exported);assertTrue(info.grantUriPermissions);assertEquals("${context.packageName}.updates",info.authority)
        val parser=context.resources.getXml(info.metaData.getInt("android.support.FILE_PROVIDER_PATHS"))
        val roots=mutableListOf<Pair<String,String?>>()
        parser.use {while(it.eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT){if(it.eventType==org.xmlpull.v1.XmlPullParser.START_TAG && it.name != "paths") roots+=it.name to it.getAttributeValue(null,"path");it.next()}}
        assertEquals(listOf("cache-path" to "app_updates/"),roots)
    }
}
