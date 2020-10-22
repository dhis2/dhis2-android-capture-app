package org.dhis2.usescases.jira

import androidx.annotation.VisibleForTesting
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.data.jira.ClickedIssueData
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.data.jira.toJiraIssueUri
import org.dhis2.utils.resources.ResourceManager

const val MEDIA_TYPE_APPLICATION_JSON = "application/json"
const val BASIC_AUTH_CODE = "%s:%s"
const val MAX_RESULTS = 20

class JiraViewModel(
    private val jiraRepository: JiraRepository,
    private val resources: ResourceManager
) : ViewModel() {

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
        jiraRepository.sendJiraIssue(
            summary, description,
            onResponse = {
                handleSendResponse(it.isSuccessful)
            },
            onError = {
                handleThrowableResponse(it)
            }
        )
    }

    fun getJiraTickets() {
        jiraRepository.getJiraIssues(userName) { handleResponse(it) }
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
            jiraRepository.getSession() ?: ""
        )
    }

    private fun saveCredentialsIfNeeded() {
        if (rememberCredentials) {
            jiraRepository.saveCredentials()
        }
    }

    @VisibleForTesting
    fun handleSendResponse(responseIsSuccessful: Boolean) {
        if (responseIsSuccessful) {
            issueMessage.value = resources.jiraIssueSentMessage()
            getJiraTickets()
        } else {
            issueMessage.value = resources.jiraIssueSentErrorMessage()
        }
    }

    @VisibleForTesting
    fun handleThrowableResponse(throwable: Throwable) {
        issueMessage.value = throwable.localizedMessage
    }

    @VisibleForTesting
    fun handleResponse(jiraIssuesResult: JiraIssuesResult) {
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
}
