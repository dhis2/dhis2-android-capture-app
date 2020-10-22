package org.dhis2.usescases.jira

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import okhttp3.OkHttpClient
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.prefs.PreferenceProviderImpl
import org.dhis2.utils.resources.ResourceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val JIRA_URL = "https://jira.dhis2.org/"

class JiraViewModelFactory(
    val applicationContext: Context
) : ViewModelProvider.Factory {

    private fun jiraService(): JiraIssueService {
        val retrofit = Retrofit.Builder()
            .baseUrl(JIRA_URL)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .validateEagerly(true)
            .build()

        return retrofit.create<JiraIssueService>(JiraIssueService::class.java)
    }

    private fun preferencesProvide(): PreferenceProvider {
        return PreferenceProviderImpl(applicationContext)
    }

    private fun resourceManager(): ResourceManager {
        return ResourceManager(applicationContext)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return JiraViewModel(
            JiraRepository(jiraService(), preferencesProvide()),
            resourceManager()
        ) as T
    }
}
