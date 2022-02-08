package org.dhis2.usescases.main

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.jira.JiraComponent
import org.dhis2.usescases.jira.JiraModule
import org.dhis2.usescases.troubleshooting.TroubleshootingComponent
import org.dhis2.usescases.troubleshooting.TroubleshootingModule

@PerActivity
@Subcomponent(modules = [MainModule::class])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
    fun plus(jiraModule: JiraModule): JiraComponent
    fun plus(troubleShootingModule: TroubleshootingModule): TroubleshootingComponent
}
