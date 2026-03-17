package org.dhis2.mobile.plugin.sdk.dto

import kotlinx.serialization.Serializable

/**
 * A minimal, SDK-agnostic representation of a DHIS2 data value.
 *
 * Plugin code always receives DTOs — never raw SDK model objects.
 *
 * @property dataElementUid UID of the data element this value belongs to.
 * @property value The string representation of the value.
 * @property period The ISO period string (e.g. `"202401"` for January 2024).
 * @property orgUnitUid UID of the organisation unit the value was reported for.
 * @property categoryOptionComboUid UID of the category option combo. Defaults to the default combo.
 */
@Serializable
data class DataValueDto(
    val dataElementUid: String,
    val value: String,
    val period: String,
    val orgUnitUid: String,
    val categoryOptionComboUid: String = "",
)
