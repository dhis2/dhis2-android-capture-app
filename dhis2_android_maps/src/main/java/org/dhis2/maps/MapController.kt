package org.dhis2.maps

import android.content.Context
import androidx.annotation.NonNull
import org.jetbrains.annotations.Nullable

class MapController {
    companion object {
        fun init(@NonNull context: Context, @Nullable accessToken: String?) {
        }

        fun getAccessToken(): String? {
            return null
        }

        fun setAccessToken(token: String?) {
        }

        fun setConnected(connection: Boolean) {
        }

        fun isConnected(): Boolean {
            return false
        }
    }
}
