package org.dhis2.data.jira

data class JiraIssue(
    val id: Int = 0,
    val key: String,
    val fields: JiraIssueField? = null,
)

data class JiraIssueField(
    val issuetype: JiraField? = null,
    var summary: String? = null,
    val status: JiraField? = null,
)

data class JiraField(
    val name: String? = null,
    val id: Int = 0,
)
