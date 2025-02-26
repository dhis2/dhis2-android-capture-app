package org.dhis2.maps.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimLocation(
    val lat: String,
    val lon: String,
    val name: String,
    @SerialName("display_name") val displayName: String?,
)
