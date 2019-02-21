package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.programDetail.ProgramRepository;
import org.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
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
    TeiProgramListContract.TeiProgramListView provideView(TeiProgramListActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.TeiProgramListPresenter providesPresenter(TeiProgramListContract.TeiProgramListInteractor interactor) {
        return new TeiProgramListPresenterImpl(interactor, teiUid);
    }

    @Provides
    @PerActivity
    TeiProgramListContract.TeiProgramListInteractor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository) {
        return new TeiProgramListInteractorImpl(teiProgramListRepository);
    }

    @Provides
    @PerActivity
    TeiProgramListAdapter provideProgramEventDetailAdapter(TeiProgramListContract.TeiProgramListPresenter presenter) {
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
