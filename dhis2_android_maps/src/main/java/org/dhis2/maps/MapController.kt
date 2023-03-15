package org.dhis2.maps

import android.content.Context
import androidx.annotation.NonNull
import com.mapbox.mapboxsdk.Mapbox

class MapController {
    companion object {
        fun init(@NonNull context: Context) {
            Mapbox.getInstance(context)
        }

        fun setConnected(connection: Boolean) {
            Mapbox.setConnected(connection)
        }

        fun isConnected(): Boolean {
            return Mapbox.isConnected()
        }
    }
}
