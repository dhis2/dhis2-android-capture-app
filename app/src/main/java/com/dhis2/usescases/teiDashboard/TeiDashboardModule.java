package com.dhis2.usescases.teiDashboard;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.usescases.teiDashboard.tablet.TeiDashboardTabletActivity;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Module
public class TeiDashboardModule {
    @Provides
    @PerActivity
    TeiDashboardContracts.View provideView(TeiDashboardMobileActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.Presenter providePresenter() {
        return new TeiDashboardPresenter();
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.Interactor provideInteractor(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        return new TeiDashboardInteractor(d2, dashboardRepository,metadataRepository);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(BriteDatabase briteDatabase) {
        return new DashboardRepositoryImpl(briteDatabase);
    }

}
