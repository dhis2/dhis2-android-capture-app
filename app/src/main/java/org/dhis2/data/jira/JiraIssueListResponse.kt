package org.dhis2.data.jira

data class JiraIssueListResponse(
    val maxResults: Int,
    val total: Int,
    val issues: List<JiraIssue>,
)
