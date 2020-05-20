package org.dhis2.uicomponents.map

import android.content.Context
import androidx.annotation.NonNull
import com.mapbox.mapboxsdk.Mapbox
import org.jetbrains.annotations.Nullable

class MapController {

    companion object {
        fun init(@NonNull context: Context, @Nullable accessToken: String?) {
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
