package org.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.programDetail.ProgramRepository;
import org.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class ProgramEventDetailModule {


    @Provides
    @PerActivity
    ProgramEventDetailContract.View provideView(ProgramEventDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.Presenter providesPresenter(@NonNull ProgramEventDetailRepository programEventDetailRepository,
                                                           @NonNull MetadataRepository metadataRepository) {
        return new ProgramEventDetailPresenter(programEventDetailRepository,metadataRepository);
    }

    @Provides
    @PerActivity
    ProgramEventDetailAdapter provideProgramEventDetailAdapter(ProgramEventDetailContract.Presenter presenter) {
        return new ProgramEventDetailAdapter(presenter);
    }

    @Provides
    @PerActivity
    ProgramEventDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new ProgramEventDetailRepositoryImpl(briteDatabase);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
