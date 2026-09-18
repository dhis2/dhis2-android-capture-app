package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [DataSetInstanceSlotArguments.appliesTo] is the whole rule deciding whether a plugin replaces the
 * data set a user just opened, so its answers are pinned here — including the ones that must be
 * "no" rather than a crash, because it runs inside the host's composition.
 */
class DataSetInstanceSlotTest {
    private val arguments =
        DataSetInstanceSlotArguments(
            dataSetUid = "lyLU2wR22tC",
            periodId = "202401",
            organisationUnitUid = "DiszpKrYNg8",
            attributeOptionComboUid = "HllvX50cXC0",
        )

    private fun configOf(vararg uids: String) =
        buildJsonObject {
            putJsonArray("dataSetUids") { uids.forEach { add(it) } }
        }

    @Test
    fun `applies to a data set the administrator listed`() {
        assertTrue(arguments.appliesTo(configOf("BfMAe6Itzgt", "lyLU2wR22tC")))
    }

    @Test
    fun `does not apply to a data set the administrator left out`() {
        assertFalse(arguments.appliesTo(configOf("BfMAe6Itzgt")))
    }

    @Test
    fun `an unconfigured slot replaces nothing`() {
        // The default for a replacement slot must be "renders nowhere": replacing every data set
        // in the instance because an administrator forgot a field is not a recoverable mistake.
        assertFalse(arguments.appliesTo(null))
    }

    @Test
    fun `an empty list replaces nothing, so it works as a kill switch`() {
        assertFalse(arguments.appliesTo(configOf()))
    }

    @Test
    fun `a configuration missing the field replaces nothing`() {
        assertFalse(arguments.appliesTo(JsonObject(emptyMap())))
    }

    @Test
    fun `a malformed configuration replaces nothing rather than throwing`() {
        // This runs inside the host's composition, where an exception takes down a screen that is
        // still showing the host's own top bar, save button and validation state.
        val malformed = buildJsonObject { put("dataSetUids", "lyLU2wR22tC") }

        assertFalse(arguments.appliesTo(malformed))
    }

    @Test
    fun `unknown fields in the configuration are tolerated`() {
        val config =
            buildJsonObject {
                putJsonArray("dataSetUids") { add("lyLU2wR22tC") }
                put("addedByANewerApp", true)
            }

        assertTrue(arguments.appliesTo(config))
    }

    @Test
    fun `the arguments name the slot they belong to`() {
        assertTrue(arguments.injectionPoint == InjectionPoint.DATA_SET_INSTANCE_CONTENT)
    }
}
