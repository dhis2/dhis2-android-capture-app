package org.dhis2.uicomponents.map.geometry

fun areLngLatCorrect(lon: Double, lat: Double) = isLatitudeValid(lat) && isLongitudeValid(lon)

fun isLongitudeValid(longitude: Double) = longitude >= -180 && longitude <= 180

fun isLatitudeValid(latitude: Double) = latitude >= -90 && latitude <= 90
