package dev.vincent.geschichten.data

import com.google.gson.Gson

/** Frozen from the compiled release 0.3.0 before the editorial revision; never derives from current profiles. */
internal object DeliveredV3CatalogFixture {
    val profiles: List<CharacterProfile> = checkNotNull(javaClass.getResourceAsStream("/catalog-v3.json"))
        .bufferedReader(Charsets.UTF_8).use {
            Gson().fromJson(it, Array<CharacterProfile>::class.java).toList()
        }
}
