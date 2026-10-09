package dev.vincent.geschichten.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Frozen compiled 0.7.3 catalog, exported before the 0.7.4 changes. */
internal object DeliveredV7CatalogFixture {
    val profiles: List<CharacterProfile> = requireNotNull(javaClass.getResourceAsStream("/catalog-v7.json")).use {
        Gson().fromJson(it.reader(Charsets.UTF_8), object : TypeToken<List<CharacterProfile>>() {}.type)
    }
}
