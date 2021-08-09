package org.dhis2.usescases.jira

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.utils.resources.ResourceManager

@Module
class JiraModule {
    @Provides
    @PerFragment
    fun jiraViewModelFactory(
        preferenceProvider: PreferenceProvider,
        resourceManager: ResourceManager,
        schedulerProvider: SchedulerProvider
    ): JiraViewModelFactory {
        return JiraViewModelFactory(preferenceProvider, resourceManager, schedulerProvider)
    }
}
