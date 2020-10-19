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
        private Issue issuetype;
        private String summary;
        private ArrayList<Component> components;
        private String description;
        private String environment;
        private ArrayList<FixVersion> affectedVersions;

        Fields(String summary, String description) {
            this.summary = summary;
            this.description = formatDescription(description);
            this.project = new Project("10200");
            this.issuetype = new Issue("Bug");
            this.components = new ArrayList<>();
            this.components.add(new Component("AndroidApp"));
            this.affectedVersions = new ArrayList<FixVersion>();
            this.affectedVersions.add(new FixVersion());
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

    public String formatDescription(String description) {
        return String.format("{panel:title=Bug description}\n%s\n{panel}", description);
    }
}