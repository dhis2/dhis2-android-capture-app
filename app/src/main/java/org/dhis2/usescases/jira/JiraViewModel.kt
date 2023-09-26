package org.dhis2.usescases.jira

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.jira.ClickedIssueData
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.data.jira.toJiraIssueUri
import timber.log.Timber

const val MEDIA_TYPE_APPLICATION_JSON = "application/json"
const val BASIC_AUTH_CODE = "%s:%s"
const val MAX_RESULTS = 20

class JiraViewModel(
    private val jiraRepository: JiraRepository,
    private val resources: ResourceManager,
    private val schedulerProvider: SchedulerProvider,
) : ViewModel() {

    private var disposable = CompositeDisposable()

    private var userName: String? = null
    private var pass: String? = null

    private var rememberCredentials = jiraRepository.hasJiraSessionSaved()
    private var summary: String = ""
    private var description: String = ""

    val formCompleted = ObservableField<Boolean>(false)
    val isSessionOpen = ObservableField<Boolean>(false)

    val issueListResponse = MutableLiveData<JiraIssuesResult>()
    var issueMessage: MutableLiveData<String> = MutableLiveData()
    val clickedIssueData: MutableLiveData<ClickedIssueData> = MutableLiveData()

    fun init() {
        if (jiraRepository.hasJiraSessionSaved()) {
            getJiraTickets()
        }
    }

    fun openSession() {
        if (!userName.isNullOrEmpty() && !pass.isNullOrEmpty()) {
            jiraRepository.setAuth(userName!!, pass!!)
            getJiraTickets()
        }
    }

    fun closeSession() {
        jiraRepository.closeSession()
        rememberCredentials = false
        isSessionOpen.set(false)
    }

    fun sendIssue() {
        disposable.add(
            jiraRepository.sendJiraIssue(summary, description)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { handleSendResponse(true) },
                    { handleThrowableResponse(it) },
                ),
        )
    }

    fun getJiraTickets() {
        disposable.add(
            jiraRepository.getJiraIssues(userName)
                .map { JiraIssuesResult(it.issues) }
                .onErrorReturn { JiraIssuesResult(errorMessage = it.message) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { handleResponse(it) },
                    { t -> Timber.e(t) },
                ),
        )
    }

    fun onSummaryChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        summary = s.toString()
        checkFormCompletion()
    }

    fun onDescriptionChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        description = s.toString()
        checkFormCompletion()
    }

    fun onJiraUserChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        userName = s.toString()
    }

    fun onJiraPassChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        pass = s.toString()
    }

    fun onCheckedChanged(isChecked: Boolean) {
        rememberCredentials = isChecked
    }

    fun onJiraIssueClick(jiraKey: String) {
        clickedIssueData.value = ClickedIssueData(
            jiraKey.toJiraIssueUri(),
            jiraRepository.getSession() ?: "",
        )
    }

    private fun saveCredentialsIfNeeded() {
        if (rememberCredentials) {
            jiraRepository.saveCredentials()
        }
    }

    private fun handleSendResponse(responseIsSuccessful: Boolean) {
        if (responseIsSuccessful) {
            issueMessage.value = resources.jiraIssueSentMessage()
            getJiraTickets()
        } else {
            issueMessage.value = resources.jiraIssueSentErrorMessage()
        }
    }

    private fun handleThrowableResponse(throwable: Throwable) {
        issueMessage.value = throwable.localizedMessage
    }

    private fun handleResponse(jiraIssuesResult: JiraIssuesResult) {
        if (jiraIssuesResult.isSuccess()) {
            saveCredentialsIfNeeded()
            isSessionOpen.set(true)
        } else {
            closeSession()
        }
        issueListResponse.value = jiraIssuesResult
    }

    private fun checkFormCompletion() {
        val check = summary.isNotEmpty() &&
            description.isNotEmpty() &&
            isSessionOpen.get() ?: false

        formCompleted.set(check)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
