package org.dhis2.utils.jira;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class JiraIssueField {

    private JiraField issuetype;
    String summary;
    private JiraField status;

    public JiraField getIssuetype() {
        return issuetype;
    }

    public JiraField getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public class JiraField {
        private String name;
        private int id;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
