package org.dhis2.android.rtsm.utils

import android.content.Context
import org.dhis2.android.rtsm.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager

class Sdk {
    companion object {
        @JvmStatic
        fun d2(context: Context): D2 {
            return try {
                D2Manager.getD2()
            } catch (e: IllegalStateException) {
                D2Manager.blockingInstantiateD2(getD2Configuration(context))!!
            }
        }

        private fun getD2Configuration(context: Context): D2Configuration {
            // This will be null if not debug mode to make sure your data is safe

            return D2Configuration.builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .readTimeoutInSeconds(10 * 60)
                .connectTimeoutInSeconds(10 * 60)
                .writeTimeoutInSeconds(10 * 60)
                .context(context)
                .build()
        }
    }
}
