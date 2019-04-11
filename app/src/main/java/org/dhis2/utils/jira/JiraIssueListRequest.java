package org.dhis2.utils.jira;

import org.dhis2.utils.Constants;

import de.adorsys.android.securestoragelibrary.SecurePreferences;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class JiraIssueListRequest {
    private String jql;
    private int maxResults;

    public JiraIssueListRequest(String userName, int maxResults) {
        jql = "(project=10200 AND reporter=<USER_NAME>) order by updated"
                .replace("<USER_NAME>", SecurePreferences.getStringValue(Constants.JIRA_USER, ""));
        this.maxResults = maxResults;

    }

}
