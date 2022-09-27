package org.dhis2.maps

import android.content.Context
import androidx.annotation.NonNull
import com.mapbox.android.telemetry.TelemetryEnabler
import com.mapbox.mapboxsdk.Mapbox
import org.jetbrains.annotations.Nullable

class MapController {
    companion object {
        fun init(@NonNull context: Context, @Nullable accessToken: String?) {
            TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED)
            Mapbox.getInstance(context, accessToken)
        }

        fun getAccessToken(): String? {
            return Mapbox.getAccessToken()
        }

        fun setAccessToken(token: String?) {
            Mapbox.setAccessToken(token)
        }

        fun setConnected(connection: Boolean) {
            Mapbox.setConnected(connection)
        }

        fun isConnected(): Boolean {
            return Mapbox.isConnected()
        }
    }
}
