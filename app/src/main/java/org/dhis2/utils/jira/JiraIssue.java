package org.dhis2.utils.jira;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class JiraIssue {
    private int id;
    private String key;
    private JiraIssueField fields;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public JiraIssueField getFields() {
        return fields;
    }
}
