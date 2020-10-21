package org.dhis2.data.jira

import com.google.gson.Gson
import java.io.IOException

data class JiraIssueListResponse(
    val maxResults: Int,
    val total: Int,
    val issues: List<JiraIssue>
) {
    companion object {
        fun fromJson(response: String?): JiraIssueListResponse {
            return try {
                Gson()
                    .fromJson(
                        response,
                        JiraIssueListResponse::class.java
                    )
            } catch (e: IOException) {
                e.printStackTrace()
                JiraIssueListResponse(0, 0, arrayListOf())
            }
        }
    }
}
