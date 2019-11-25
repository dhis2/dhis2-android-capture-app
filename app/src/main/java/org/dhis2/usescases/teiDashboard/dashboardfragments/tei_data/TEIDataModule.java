package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

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
public class TEIDataModule {

    private final String programUid;
    private final String teiUid;
    private TEIDataContracts.View view;

    public TEIDataModule(TEIDataContracts.View view, String programUid, String teiUid) {
        this.view = view;
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerFragment
    TEIDataContracts.Presenter providesPresenter(D2 d2, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider) {
        return new TEIDataPresenterImpl(view, d2, dashboardRepository, programUid, teiUid, schedulerProvider);
    }

}
