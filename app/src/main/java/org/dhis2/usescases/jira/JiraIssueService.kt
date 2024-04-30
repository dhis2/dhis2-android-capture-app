package org.dhis2.usescases.jira

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dhis2.data.jira.JiraIssueListResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface JiraIssueService {
    @POST("rest/api/2/issue")
    fun createIssue(
        @Header("Authorization")
        auth: String?,
        @Body issueRequest: RequestBody,
    ): Single<ResponseBody>

    @POST("rest/api/2/search")
    fun getJiraIssues(
        @Header("Authorization")
        auth: String?,
        @Body issueRequest: RequestBody,
    ): Single<JiraIssueListResponse>
}
