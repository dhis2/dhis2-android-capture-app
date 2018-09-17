package org.dhis2.usescases.teiDashboard;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.utils.CodeGenerator;
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
    TeiDashboardContracts.View provideView(TeiDashboardActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.Presenter providePresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        return new TeiDashboardPresenter(d2, dashboardRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        return new DashboardRepositoryImpl(codeGenerator, briteDatabase);
    }

}
