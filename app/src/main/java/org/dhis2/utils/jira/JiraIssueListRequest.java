package org.dhis2.utils.jira;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class JiraIssueListRequest {
    private String jql;
    private int maxResults;

    public JiraIssueListRequest(String userName, int maxResults) {
        jql = "(project=10200 AND reporter=<USER_NAME> AND issueType=10006) order by created"
                .replace("<USER_NAME>", userName);
        this.maxResults = maxResults;
    }

}
