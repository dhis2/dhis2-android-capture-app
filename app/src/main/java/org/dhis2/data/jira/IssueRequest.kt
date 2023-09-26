package org.dhis2.data.jira

import org.dhis2.BuildConfig

data class IssueRequest(private val fields: Fields) {
    constructor(summary: String, description: String) : this(
        Fields(
            summary,
            arrayListOf(Component(ANDROIDAPP_COMPONENT)),
            description.jiraBugFormated(),
            DEFAULT_ENVIRONMENT,
            arrayListOf(FixVersion(BuildConfig.VERSION_NAME)),
            Project(JIRA_PROJECT_NUMBER),
            Issue(ISSUE_TYPE_BUG),
        ),
    )
}

data class Fields(
    private val summary: String,
    private val components: List<Component>,
    private val description: String,
    private val environment: String,
    private val versions: List<FixVersion>,
    private val project: Project,
    private val issuetype: Issue,
)

data class Project(val id: String)
data class Issue(val name: String)
data class Component(val name: String)
data class FixVersion(val name: String)

const val ANDROIDAPP_COMPONENT = "AndroidApp"
const val DEFAULT_ENVIRONMENT = "."
const val DEFAULT_BUG_TEMPLATE = "{panel:title=Bug description}\n%s\n{panel}"
const val JIRA_PROJECT_NUMBER = "10200"
const val ISSUE_TYPE_BUG = "Bug"
