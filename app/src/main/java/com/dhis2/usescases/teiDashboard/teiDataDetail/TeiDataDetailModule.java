package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.DashboardRepository;
import com.dhis2.usescases.teiDashboard.DashboardRepositoryImpl;
import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by frodriguez on 12/13/2017.
 */
@PerActivity
@Module
public class TeiDataDetailModule {

    private String enrollmentUid;

    TeiDataDetailModule(String enrollmentUid) {
        this.enrollmentUid = enrollmentUid;
    }


    @Provides
    @PerActivity
    TeiDataDetailContracts.View provideView(TeiDataDetailActivity detailActivity) {
        return detailActivity;
    }

    @Provides
    @PerActivity
    TeiDataDetailContracts.Presenter providePresenter(DashboardRepository dashboardRepository, MetadataRepository metadataRepository, EnrollmentStatusStore enrollmentStatusStore) {
        return new TeiDataDetailPresenter(dashboardRepository, metadataRepository, enrollmentStatusStore);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        return new DashboardRepositoryImpl(codeGenerator, briteDatabase);
    }

    @Provides
    @PerActivity
    EnrollmentStatusStore enrollmentStatusStore(@NonNull BriteDatabase briteDatabase) {
        return new EnrollmentStatusStore(briteDatabase, enrollmentUid);
    }
}
