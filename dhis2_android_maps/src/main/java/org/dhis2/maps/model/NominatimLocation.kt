package org.dhis2.maps.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class NominatimLocation(
    val lat: String,
    val lon: String,
    val name: String,
    @JsonProperty("display_name") val displayName: String?,
)
