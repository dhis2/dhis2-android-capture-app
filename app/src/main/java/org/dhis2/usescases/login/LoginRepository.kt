package org.dhis2.usescases.login

import android.content.res.Resources
import com.google.gson.Gson
import kotlinx.coroutines.withContext
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.TestingCredential

class LoginRepository(
    private val resources: Resources,
    private val gson: Gson,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun getTestingCredentials(): List<TestingCredential> =
        withContext(dispatchers.io()) {
            if (BuildConfig.DEBUG) {
                getCredentialsFromFile(R.raw.testing_credentials)
            } else if (BuildConfig.FLAVOR == "dhis2Training") {
                getCredentialsFromFile(R.raw.training_credentials)
            } else {
                emptyList()
            }
        }

    private fun getCredentialsFromFile(rawFile: Int): List<TestingCredential> {
        return try {
            val inputStream = resources.openRawResource(rawFile)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, Array<TestingCredential>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
