package org.dhis2.mobile.plugin.sdk.dto

import kotlinx.serialization.Serializable

/**
 * A minimal, SDK-agnostic representation of a DHIS2 Tracked Entity Instance (TEI).
 *
 * Plugin code always receives DTOs — never raw SDK model objects. This keeps the plugin
 * API stable even as the underlying DHIS2 Android SDK evolves.
 *
 * @property uid The unique identifier of the TEI.
 * @property programUid The program the TEI is enrolled in.
 * @property attributes Key/value map of tracked entity attribute UIDs to their current values.
 */
@Serializable
data class TrackedEntityInstanceDto(
    val uid: String,
    val programUid: String,
    val attributes: Map<String, String> = emptyMap(),
)
