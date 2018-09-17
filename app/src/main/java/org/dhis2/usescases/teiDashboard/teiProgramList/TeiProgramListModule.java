package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.programDetail.ProgramRepository;
import org.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class TeiProgramListModule {


    private final String teiUid;

    TeiProgramListModule(String teiUid) {
        this.teiUid = teiUid;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.View provideView(TeiProgramListActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Presenter providesPresenter(TeiProgramListContract.Interactor interactor) {
        return new TeiProgramListPresenter(interactor, teiUid);
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Interactor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository) {
        return new TeiProgramListInteractor(teiProgramListRepository);
    }

    @Provides
    @PerActivity
    TeiProgramListAdapter provideProgramEventDetailAdapter(TeiProgramListContract.Presenter presenter) {
        return new TeiProgramListAdapter(presenter);
    }

    @Provides
    @PerActivity
    TeiProgramListRepository eventDetailRepository(@NonNull CodeGenerator codeGenerator, @NonNull BriteDatabase briteDatabase) {
        return new TeiProgramListRepositoryImpl(codeGenerator, briteDatabase);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(@NonNull BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
