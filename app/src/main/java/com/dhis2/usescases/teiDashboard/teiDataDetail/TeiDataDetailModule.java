package com.dhis2.usescases.teiDashboard.teiDataDetail;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.DashboardRepository;
import com.dhis2.usescases.teiDashboard.DashboardRepositoryImpl;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by frodriguez on 12/13/2017.
 */

@Module
public class TeiDataDetailModule {
    @Provides
    TeiDataDetailContracts.View provideView(TeiDataDetailActivity detailActivity) {
        return detailActivity;
    }

    @Provides
    @PerActivity
    TeiDataDetailContracts.Presenter providePresenter() {
        return new TeiDataDetailPresenter();
    }

    @Provides
    @PerActivity
    TeiDataDetailContracts.Interactor provideInteractor(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        return new TeiDataDetailInteractor(d2, dashboardRepository,metadataRepository);
    }

    @Provides
    DashboardRepository dashboardRepository(BriteDatabase briteDatabase) {
        return new DashboardRepositoryImpl(briteDatabase);
    }
}
