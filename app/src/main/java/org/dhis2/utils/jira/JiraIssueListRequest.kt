package org.dhis2.utils.jira

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
class JiraIssueListRequest(userName: String, private val maxResults: Int) {
    private val jql: String = "(project=10200 AND reporter=<USER_NAME> AND issueType=10006) order by created"
            .replace("<USER_NAME>", userName)

}
