package org.dhis2.uicomponents.map.geometry.bound

class BoundsModel(var northBound:Double = 0.0, var southBound:Double = 0.0,
                  var eastBound:Double = 0.0, var westBound:Double = 0.0) {

    fun update(lat: Double, lon: Double) : BoundsModel {
        return if (wasNeverUpdated()){
            init(lat, lon)
        } else {
            if (northBound < lat) northBound = lat
            if (southBound > lat) southBound = lat
            if (eastBound < lon) eastBound = lon
            if (westBound > lon) westBound = lon

            return this
        }
    }

    private fun init(lat: Double, lon: Double) : BoundsModel {
        northBound = lat
        southBound = lat
        eastBound = lon
        westBound = lon

        return this
    }

    private fun wasNeverUpdated() : Boolean {
       return  northBound == 0.0 && southBound == 0.0 && eastBound == 0.0 && westBound == 0.0
    }
}