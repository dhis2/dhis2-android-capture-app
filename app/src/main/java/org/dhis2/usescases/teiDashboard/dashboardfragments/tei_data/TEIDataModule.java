package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class TEIDataModule {

    private TEIDataContracts.View view;
    private final String programUid;
    private final String teiUid;
    private final String enrollmentUid;

    public TEIDataModule(TEIDataContracts.View view, String programUid, String teiUid,String enrollmentUid) {
        this.view = view;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.enrollmentUid = enrollmentUid;
    }

    @Provides
    @PerFragment
    TEIDataContracts.Presenter providesPresenter(D2 d2,
                                                 DashboardRepository dashboardRepository,
                                                 TeiDataRepository teiDataRepository,
                                                 SchedulerProvider schedulerProvider,
                                                 AnalyticsHelper analyticsHelper,
                                                 PreferenceProvider preferenceProvider) {
        return new TEIDataPresenterImpl(view,
                d2,
                dashboardRepository,
                teiDataRepository,
                programUid,
                teiUid,
                enrollmentUid,
                schedulerProvider,
                preferenceProvider,
                analyticsHelper);

    }

    @Provides
    @PerFragment
    TeiDataRepository providesRepository(D2 d2) {
        return new TeiDataRepositoryImpl(d2,
                programUid,
                teiUid,
                enrollmentUid);
    }

}
