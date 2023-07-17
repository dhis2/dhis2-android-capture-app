package org.dhis2.usescases.jira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import okhttp3.OkHttpClient
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

const val JIRA_URL = "https://jira.dhis2.org/"

@Suppress("UNCHECKED_CAST")
class JiraViewModelFactory(
    val preferenceProvider: PreferenceProvider,
    val resourceManager: ResourceManager,
    val schedulerProvider: SchedulerProvider
) : ViewModelProvider.Factory {

    private fun jiraService(): JiraIssueService {
        val retrofit = Retrofit.Builder()
            .baseUrl(JIRA_URL)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .validateEagerly(true)
            .build()

        return retrofit.create<JiraIssueService>(JiraIssueService::class.java)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return JiraViewModel(
            JiraRepository(jiraService(), preferenceProvider),
            resourceManager,
            schedulerProvider
        ) as T
    }
}
