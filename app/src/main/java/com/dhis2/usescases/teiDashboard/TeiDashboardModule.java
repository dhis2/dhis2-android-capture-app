package com.dhis2.usescases.teiDashboard;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.squareup.sqlbrite2.BriteDatabase;

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
    TeiDashboardContracts.Presenter providePresenter(DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        return new TeiDashboardPresenter(dashboardRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(BriteDatabase briteDatabase) {
        return new DashboardRepositoryImpl(briteDatabase);
    }

}
