package dev.vincent.geschichten.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Frozen compiled public 0.5.0 catalog, independent of today's entries. */
internal object DeliveredV6CatalogFixture {
    val profiles: List<CharacterProfile> = requireNotNull(javaClass.getResourceAsStream("/catalog-v6.json")).use {
        Gson().fromJson(it.reader(Charsets.UTF_8), object : TypeToken<List<CharacterProfile>>() {}.type)
    }
}
