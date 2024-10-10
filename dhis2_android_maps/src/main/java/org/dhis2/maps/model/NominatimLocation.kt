package org.dhis2.maps.model

data class NominatimLocation(
    val lat: String,
    val lon: String,
    val name: String,
    val display_name: String?,
)
