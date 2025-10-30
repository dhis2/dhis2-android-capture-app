package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import dagger.Module
import dagger.Provides
import org.dhis2.commons.date.DateUtils

@Module
class SchedulingModule {
    @Provides
    fun providesDateUtils(): DateUtils = DateUtils.getInstance()
}
