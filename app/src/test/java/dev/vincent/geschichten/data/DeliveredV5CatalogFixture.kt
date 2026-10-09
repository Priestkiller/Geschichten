package dev.vincent.geschichten.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Frozen actual compiled 0.4.0 catalog, independent of current authored profiles. */
internal object DeliveredV5CatalogFixture {
    val profiles: List<CharacterProfile> = requireNotNull(javaClass.getResourceAsStream("/catalog-v5.json")).use {
        Gson().fromJson(it.reader(Charsets.UTF_8), object : TypeToken<List<CharacterProfile>>() {}.type)
    }
}
