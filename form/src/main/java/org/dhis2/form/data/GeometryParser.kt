package org.dhis2.form.data

interface GeometryParser {
    fun parsePoint(coordinates: String): List<Double>

    fun parsePolygon(coordinates: String): List<List<List<Double>>>

    fun parseMultipolygon(coordinates: String): List<List<List<List<Double>>>>
}
