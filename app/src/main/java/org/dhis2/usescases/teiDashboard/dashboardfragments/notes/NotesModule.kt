package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.hisp.dhis.android.core.D2

@Module
@PerFragment
class NotesModule(private val view: NotesContracts.View) {

    @Provides
    @PerFragment
    fun providesPresenter(
        d2: D2,
        dashboardRepository: DashboardRepository,
        schedulerProvider: SchedulerProvider
    ): NotesPresenter {
        return NotesPresenter(d2, dashboardRepository, schedulerProvider, view)
    }
}
