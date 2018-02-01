package com.dhis2.usescases.enrollment;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/01/2018.
 */

@Module
public class EnrollmentModule {


    @Provides
    @PerActivity
    EnrollmentContracts.View provideView(EnrollmentActivity enrollmentActivity) {
        return enrollmentActivity;
    }

    @Provides
    @PerActivity
    EnrollmentContracts.Presenter providePresenter(D2 d2, MetadataRepository metadataRepository) {
        return new EnrollmentPresenter(metadataRepository);
    }



}
