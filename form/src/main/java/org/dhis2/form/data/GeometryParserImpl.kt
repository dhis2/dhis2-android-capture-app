package org.dhis2.form.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GeometryParserImpl : GeometryParser {
    override fun parsePoint(coordinates: String): List<Double> {
        val type = object : TypeToken<List<Double?>?>() {}.type
        return Gson().fromJson(coordinates, type)
    }

    override fun parsePolygon(coordinates: String): List<List<List<Double>>> {
        val type = object : TypeToken<List<List<List<Double?>?>?>?>() {}.type
        return Gson().fromJson(coordinates, type)
    }

    override fun parseMultipolygon(coordinates: String): List<List<List<List<Double>>>> {
        val type = object : TypeToken<List<List<List<List<Double?>?>?>?>?>() {}.type
        return Gson().fromJson(coordinates, type)
    }
}
