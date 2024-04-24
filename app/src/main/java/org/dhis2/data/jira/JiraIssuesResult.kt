package org.dhis2.data.jira

data class JiraIssuesResult(
    val issues: List<JiraIssue> = emptyList(),
    val errorMessage: String? = null,
) {
    fun isSuccess(): Boolean {
        return errorMessage == null
    }
}
