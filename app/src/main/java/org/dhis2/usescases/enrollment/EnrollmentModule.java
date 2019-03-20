package org.dhis2.usescases.enrollment;

import org.dhis2.data.dagger.PerActivity;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class EnrollmentModule {

    private String enrollmentUid;

    public EnrollmentModule(String enrollmentUid) {
        this.enrollmentUid = enrollmentUid;
    }

    @Provides
    @PerActivity
    EnrollmentContracts.Presenter providePresenter(EnrollmentRepository enrollmentRepository, D2 d2) {
        return new EnrollmentPresenterImpl(enrollmentUid, enrollmentRepository, d2);
    }

    @Provides
    @PerActivity
    EnrollmentRepository enrollmentRepository() {
        return new EnrollmentRepositoryImpl(enrollmentUid);
    }

}
