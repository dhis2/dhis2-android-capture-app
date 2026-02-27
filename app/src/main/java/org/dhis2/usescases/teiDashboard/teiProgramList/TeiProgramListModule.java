package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.mobile.sync.domain.SyncStatusController;
import org.dhis2.usescases.main.program.ProgramViewModelMapper;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class TeiProgramListModule {


    private final TeiProgramListContract.View view;
    private final String teiUid;

    private final SyncStatusController syncStatusController;

    TeiProgramListModule(TeiProgramListContract.View view, String teiUid, SyncStatusController syncStatusController) {
        this.view = view;
        this.teiUid = teiUid;
        this.syncStatusController = syncStatusController;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.View provideView(TeiProgramListActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Presenter providesPresenter(TeiProgramListContract.Interactor interactor,
                                                       PreferenceProvider preferenceProvider,
                                                       AnalyticsHelper analyticsHelper,
                                                       D2 d2) {
        return new TeiProgramListPresenter(view, interactor, teiUid, preferenceProvider, analyticsHelper,
                d2.enrollmentModule().enrollmentService());
    }

    @Provides
    @PerActivity
    TeiProgramListContract.Interactor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository) {
        return new TeiProgramListInteractor(teiProgramListRepository, syncStatusController);
    }

    @Provides
    @PerActivity
    TeiProgramListAdapter provideProgramEventDetailAdapter(TeiProgramListContract.Presenter presenter) {
        return new TeiProgramListAdapter(presenter);
    }

    @Provides
    @PerActivity
    TeiProgramListRepository eventDetailRepository(
            D2 d2,
            MetadataIconProvider metadataIconProvider) {
        return new TeiProgramListRepositoryImpl(
                d2,
                new ProgramViewModelMapper(),
                metadataIconProvider
        );
    }
}
