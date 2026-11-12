package org.dhis2.mobile.plugin.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Reading the App Hub API (`https://apps.dhis2.org/api/v2`). */
object AppHub {
    const val API = "https://apps.dhis2.org/api/v2"

    /** The download URL of [version] in a `/apps/{id}/versions` response, or null. */
    fun downloadUrlFor(
        versionsJson: String,
        version: String,
    ): String? =
        results(versionsJson)
            .firstOrNull { it.string("version") == version }
            ?.string("downloadUrl")

    /**
     * The ids of the apps in a `/apps?query=` response named exactly [name]. The search is fuzzy, so
     * a plugin called "Matrix Select" must not match "Matrix Select Pro".
     */
    fun appIdsNamed(
        searchJson: String,
        name: String,
    ): List<String> =
        results(searchJson)
            .filter { it.string("name") == name }
            .mapNotNull { it.string("id") }

    private fun results(json: String): List<JsonObject> {
        val root: JsonElement =
            try {
                Json.parseToJsonElement(json)
            } catch (error: Exception) {
                return emptyList()
            }
        return ((root as? JsonObject)?.get("result") as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
}
