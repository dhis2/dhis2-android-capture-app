package org.dhis2.usescases.jira

import android.util.Base64
import com.google.gson.Gson
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.jira.IssueRequest
import org.dhis2.data.jira.JiraIssueListRequest
import org.dhis2.data.jira.JiraIssueListResponse
import org.dhis2.data.jira.toBasicAuth
import org.dhis2.data.jira.toJiraJql

class JiraRepository(
    private val jiraApi: JiraIssueService,
    private val prefs: PreferenceProvider
) {
    private var session: String? = prefs.getString(Constants.JIRA_AUTH, null)
    private var userName: String? = prefs.getString(Constants.JIRA_USER, null)

    fun hasJiraSessionSaved(): Boolean {
        return prefs.contains(Constants.JIRA_USER)
    }

    fun getJiraIssues(userName: String? = null): Single<JiraIssueListResponse> {
        userName?.let { this.userName = userName }
        val basic = session?.toBasicAuth()
        val request = JiraIssueListRequest(
            prefs.getString(Constants.JIRA_USER, this.userName)!!.toJiraJql(),
            MAX_RESULTS
        )
        val requestBody =
            RequestBody.create(MediaType.parse(MEDIA_TYPE_APPLICATION_JSON), Gson().toJson(request))
        return jiraApi.getJiraIssues(basic, requestBody)
    }

    fun sendJiraIssue(summary: String, description: String): Single<ResponseBody> {
        val basic = session?.toBasicAuth()
        val issueRequest = IssueRequest(summary, description)
        val requestBody =
            RequestBody.create(
                MediaType.parse(MEDIA_TYPE_APPLICATION_JSON),
                Gson().toJson(issueRequest)
            )
        return jiraApi.createIssue(basic, requestBody)
    }

    fun saveCredentials() {
        session?.let { prefs.saveJiraCredentials(it) }
        userName?.let { prefs.saveJiraUser(it) }
    }

    fun setAuth(userName: String, pass: String) {
        session = Base64.encodeToString(
            BASIC_AUTH_CODE.format(userName, pass).toByteArray(),
            Base64.NO_WRAP
        )
    }

    fun getSession(): String? {
        return session
    }

    fun closeSession() {
        prefs.closeJiraSession()
        session = null
    }
}
