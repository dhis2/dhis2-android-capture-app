package org.dhis2.usescases.login

import android.content.Context
import com.google.gson.Gson
import org.dhis2.R
import org.dhis2.utils.TestingCredential

class LoginRepository(
    private val context: Context,
    private val gson: Gson,
) {
    fun getTestingCredentials(): List<TestingCredential> {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.testing_credentials)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, Array<TestingCredential>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
