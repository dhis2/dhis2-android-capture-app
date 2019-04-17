package org.dhis2.utils.jira;

import org.dhis2.BuildConfig;

import java.util.ArrayList;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public class IssueRequest {

    private Fields fields;

    public IssueRequest(String summary, String description) {
        this.fields = new Fields(summary, description);
    }

    private class Fields {
        private Project project;
        private String summary;
        private String description;
        private Issue issuetype;
        private ArrayList<Component> components;
        private ArrayList<FixVersion> fixVersions;

        Fields(String summary, String description) {
            this.summary = summary;
            this.description = description;
            this.project = new Project("10200");
            this.issuetype = new Issue("Bug");
            this.components = new ArrayList<>();
            this.components.add(new Component("AndroidApp"));
            this.fixVersions = new ArrayList<FixVersion>();
            this.fixVersions.add(new FixVersion());
        }


    }

    private class Project {
        private String id;

        Project(String projectId) {
            this.id = projectId;
        }
    }

    private class Issue {
        private String name;

        Issue(String bug) {
            this.name = bug;
        }
    }

    private class Component {
        private String name;

        Component(String componentName) {
            name = componentName;
        }
    }

    private class FixVersion {
        String name = BuildConfig.VERSION_NAME;
    }
}