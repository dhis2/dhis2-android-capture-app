package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class NotesModule {

    private final String programUid;
    private final String teiUid;

    public NotesModule(String programUid, String teiUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerFragment
    NotesContracts.Presenter providesPresenter(D2 d2, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider) {
        return new NotesPresenterImpl(d2, dashboardRepository, programUid, teiUid, schedulerProvider);
    }

}
