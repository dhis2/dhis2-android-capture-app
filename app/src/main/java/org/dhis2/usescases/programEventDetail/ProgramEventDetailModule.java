package org.dhis2.usescases.programEventDetail;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.hisp.dhis.android.core.D2;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class ProgramEventDetailModule {


    private final String programUid;

    public ProgramEventDetailModule(String programUid) {
        this.programUid = programUid;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.ProgramEventDetailView provideView(ProgramEventDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.ProgramEventDetailPresenter providesPresenter(
                                                           @NonNull ProgramEventDetailRepository programEventDetailRepository) {
        return new ProgramEventDetailPresenterImpl(programUid,programEventDetailRepository);
    }

    @Provides
    @PerActivity
    ProgramEventDetailAdapter provideProgramEventDetailAdapter(ProgramEventDetailContract.ProgramEventDetailPresenter presenter) {
        return new ProgramEventDetailAdapter(presenter);
    }

    @Provides
    @PerActivity
    ProgramEventDetailRepository eventDetailRepository(BriteDatabase briteDatabase, D2 d2) {
        return new ProgramEventDetailRepositoryImpl(programUid,briteDatabase, d2);
    }
}
