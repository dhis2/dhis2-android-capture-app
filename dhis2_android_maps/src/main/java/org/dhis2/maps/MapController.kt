package org.dhis2.maps

import android.content.Context
import org.maplibre.android.MapLibre

class MapController {
    companion object {
        fun init(context: Context) {
            MapLibre.getInstance(context)
        }

        fun setConnected(connection: Boolean) {
            MapLibre.setConnected(connection)
        }

        fun isConnected(): Boolean = MapLibre.isConnected()
    }
}
