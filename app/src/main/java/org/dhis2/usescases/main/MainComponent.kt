package org.dhis2.usescases.main

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity
import org.dhis2.usescases.jira.JiraComponent
import org.dhis2.usescases.jira.JiraModule

@PerActivity
@Subcomponent(modules = [MainModule::class])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
    fun plus(jiraModule: JiraModule): JiraComponent
}
