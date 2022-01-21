package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.data.dhislogic.DhisEnrollmentUtils;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.forms.dataentry.ValueStore;
import org.dhis2.data.forms.dataentry.ValueStoreImpl;
import org.dhis2.form.data.FormValueStore;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.utils.reporting.CrashReportController;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@Module
public class TEIDataModule {

    private TEIDataContracts.View view;
    private final String programUid;
    private final String teiUid;
    private final String enrollmentUid;

    public TEIDataModule(TEIDataContracts.View view, String programUid, String teiUid, String enrollmentUid) {
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
                                                 RuleEngineRepository ruleEngineRepository,
                                                 SchedulerProvider schedulerProvider,
                                                 AnalyticsHelper analyticsHelper,
                                                 PreferenceProvider preferenceProvider,
                                                 FilterManager filterManager,
                                                 FilterRepository filterRepository,
                                                 FormValueStore valueStore) {
        return new TEIDataPresenterImpl(view,
                d2,
                dashboardRepository,
                teiDataRepository,
                ruleEngineRepository,
                programUid,
                teiUid,
                enrollmentUid,
                schedulerProvider,
                preferenceProvider,
                analyticsHelper,
                filterManager,
                filterRepository,
                valueStore);

    }

    @Provides
    @PerFragment
    TeiDataRepository providesRepository(D2 d2, DhisPeriodUtils periodUtils) {
        return new TeiDataRepositoryImpl(d2,
                programUid,
                teiUid,
                enrollmentUid,
                periodUtils);
    }

    @Provides
    @PerFragment
    FiltersAdapter provideNewFiltersAdapter() {
        return new FiltersAdapter();
    }

    @Provides
    @PerFragment
    FormValueStore valueStore(
            D2 d2,
            CrashReportController crashReportController,
            NetworkUtils networkUtils
    ){
        return new ValueStoreImpl(
                d2,
                teiUid,
                DataEntryStore.EntryMode.ATTR,
                new DhisEnrollmentUtils(d2),
                crashReportController,
                networkUtils
        );
    }
}
