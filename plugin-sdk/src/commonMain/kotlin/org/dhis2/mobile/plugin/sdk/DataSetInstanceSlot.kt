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
 * A *rendering* filter, not an access grant — the plugin holds the whole SDK either way (§6 of
 * `docs/plugin-system.md`). An empty or absent list replaces nothing, which doubles as a kill
 * switch that does not require deleting the plugin's entry.
 */
@Serializable
data class DataSetInstanceSlotConfig(
    val dataSetUids: List<String> = emptyList(),
)

/** The data set instance the user opened, for [InjectionPoint.DATA_SET_INSTANCE_CONTENT]. */
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
