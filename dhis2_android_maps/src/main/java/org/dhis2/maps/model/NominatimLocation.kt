package org.dhis2.maps.model

import com.google.gson.annotations.SerializedName

data class NominatimLocation(
    val lat: String,
    val lon: String,
    val name: String,
    @SerializedName("display_name") val displayName: String,
)
