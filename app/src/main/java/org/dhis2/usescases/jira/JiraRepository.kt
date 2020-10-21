package org.dhis2.usescases.jira

import android.util.Base64
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dhis2.data.jira.IssueRequest
import org.dhis2.data.jira.JiraIssue
import org.dhis2.data.jira.JiraIssueListRequest
import org.dhis2.data.jira.JiraIssueListResponse
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.data.jira.toBasicAuth
import org.dhis2.data.jira.toJiraJql
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JiraRepository(
    private val jiraApi: JiraIssueService,
    private val prefs: PreferenceProvider
) {
    private var session: String? = prefs.getString(Constants.JIRA_AUTH, null)
    private var userName: String? = prefs.getString(Constants.JIRA_USER, null)

    fun hasJiraSessionSaved(): Boolean {
        return prefs.contains(Constants.JIRA_USER)
    }

    fun getJiraIssues(
        userName: String? = null,
        onResponse: (JiraIssuesResult) -> Unit
    ) {
        userName?.let { this.userName = userName }
        val basic = session?.toBasicAuth()
        val request = JiraIssueListRequest(
            prefs.getString(Constants.JIRA_USER, this.userName)!!.toJiraJql(),
            MAX_RESULTS
        )
        val requestBody =
            RequestBody.create(MediaType.parse(MEDIA_TYPE_APPLICATION_JSON), Gson().toJson(request))
        jiraApi.getJiraIssues(basic, requestBody).enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val issueList: List<JiraIssue> =
                            JiraIssueListResponse.fromJson(response.body()?.string()).issues
                        onResponse(JiraIssuesResult(issueList))
                    } else {
                        onResponse(JiraIssuesResult(errorMessage = response.message()))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResponse(JiraIssuesResult(errorMessage = t.localizedMessage))
                }
            }
        )
    }

    fun sendJiraIssue(
        summary: String,
        description: String,
        onResponse: (Response<ResponseBody>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val basic = session?.toBasicAuth()
        val issueRequest = IssueRequest(summary, description)
        val requestBody =
            RequestBody.create(
                MediaType.parse(MEDIA_TYPE_APPLICATION_JSON),
                Gson().toJson(issueRequest)
            )
        jiraApi.createIssue(basic, requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onError(t)
            }
        })
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
