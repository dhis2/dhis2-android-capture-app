package org.dhis2.usescases.teiDashboard.teiDataDetail;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import org.hisp.dhis.android.core.D2;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by frodriguez on 12/13/2017.
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
    TeiDataDetailContracts.Presenter providePresenter(DashboardRepository dashboardRepository, EnrollmentStatusStore enrollmentStatusStore) {
        return new TeiDataDetailPresenter(dashboardRepository, enrollmentStatusStore);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(CodeGenerator codeGenerator, BriteDatabase briteDatabase, D2 d2) {
        return new DashboardRepositoryImpl(codeGenerator, briteDatabase, d2);
    }

    @Provides
    @PerActivity
    EnrollmentStatusStore enrollmentStatusStore(@NonNull BriteDatabase briteDatabase, D2 d2) {
        return new EnrollmentStatusStore(briteDatabase, enrollmentUid, d2);
    }
}
