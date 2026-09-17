package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The administrator's configuration for [InjectionPoint.DATA_SET_INSTANCE_CONTENT].
 *
 * ```json
 * "slotConfig": {
 *   "DATA_SET_INSTANCE_CONTENT": { "dataSetUids": ["lyLU2wR22tC", "BfMAe6Itzgt"] }
 * }
 * ```
 *
 * A *rendering* filter, not an access grant: it decides which data sets the plugin draws, and
 * narrows nothing about what it can read or write — see §6 of `docs/plugin-system.md`, the plugin
 * holds the whole SDK either way.
 *
 * An empty or absent list means the plugin replaces nothing, and every data set keeps the host's
 * own layout. That also makes it a kill switch that does not require deleting the plugin's entry.
 */
@Serializable
data class DataSetInstanceSlotConfig(
    val dataSetUids: List<String> = emptyList(),
)

/**
 * The data set instance the user opened, for a plugin rendering at
 * [InjectionPoint.DATA_SET_INSTANCE_CONTENT].
 *
 * Together these four values identify one instance, and they are what every SDK query about it
 * takes.
 */
data class DataSetInstanceSlotArguments(
    val dataSetUid: String,
    val periodId: String,
    val organisationUnitUid: String,
    val attributeOptionComboUid: String,
) : SlotArguments {
    override val injectionPoint = InjectionPoint.DATA_SET_INSTANCE_CONTENT

    override fun appliesTo(config: JsonObject?): Boolean {
        val configured =
            runCatching {
                config?.let { slotConfigJson.decodeFromJsonElement(DataSetInstanceSlotConfig.serializer(), it) }
            }.getOrNull() ?: return false

        return dataSetUid in configured.dataSetUids
    }
}
