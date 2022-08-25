package org.dhis2.android.rtsm.services

import android.content.Context
import com.google.gson.Gson
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.models.OpenIDAuthConfigModel
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class OpenIdProviderImpl @Inject constructor(
    private val context: Context
): OpenIdProvider {

    /**
     * Loads the OpenID configuration from 'res/raw/openid_config' file
     *
     * @return The OpenID configuration object
     */
    override fun loadProvider(): OpenIDConnectConfig? {
        context.resources.openRawResource(R.raw.openid_config).use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))

            return try {
                Gson().fromJson(reader, OpenIDAuthConfigModel::class.java)
                    .toOpenIDConnectionConfig()
            } catch (ex: Exception) {
                ex.printStackTrace()

                null
            }
        }
    }
}