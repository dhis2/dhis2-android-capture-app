package org.dhis2.uicomponents.map.geometry

fun areLngLatCorrect(lon: Double, lat: Double): Boolean {
    return (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180)
}
