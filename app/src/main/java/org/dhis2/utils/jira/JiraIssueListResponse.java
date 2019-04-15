package org.dhis2.utils.jira;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class JiraIssueListResponse {
    private int maxResults;
    private int total;
    private List<JiraIssue> issues;

    public int getMaxResults() {
        return maxResults;
    }

    public int getTotal() {
        return total;
    }

    public List<JiraIssue> getIssues() {
        return issues;
    }
}
