package org.dhis2.utils.jira

import org.dhis2.BuildConfig

import java.util.ArrayList

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
class IssueRequest(summary: String, description: String) {

    private val fields: Fields

    init {
        this.fields = Fields(summary, description)
    }

    private inner class Fields internal constructor(private val summary: String, private val description: String) {
        private val project: Project = Project("10200")
        private val issuetype: Issue = Issue("Bug")
        private val components: ArrayList<Component> = ArrayList()
        private val fixVersions: ArrayList<FixVersion>

        init {
            this.components.add(Component("AndroidApp"))
            this.fixVersions = ArrayList()
            this.fixVersions.add(FixVersion())
        }


    }

    private inner class Project internal constructor(private val id: String)

    private inner class Issue internal constructor(private val name: String)

    private inner class Component internal constructor(private val name: String)

    private inner class FixVersion {
        internal var name = BuildConfig.VERSION_NAME
    }
}