package org.dhis2.utils.jira

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
class JiraIssueField {

    val issuetype: JiraField? = null
    var summary: String? = null
        internal set
    val status: JiraField? = null

    inner class JiraField {
        val name: String? = null
        val id: Int = 0
    }
}
