package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.data.service.SyncStatusController;
import org.dhis2.usescases.main.program.ProgramViewModelMapper;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */
@Module
public class TeiProgramListModule {


    private final TeiProgramListContract.View view;
    private final String teiUid;

    TeiProgramListModule(TeiProgramListContract.View view, String teiUid) {
        this.view = view;
        this.teiUid = teiUid;
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
    TeiProgramListContract.Interactor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository,
                                                        SyncStatusController syncStatusController) {
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
            ColorUtils colorUtils,
            MetadataIconProvider metadataIconProvider) {
        ResourceManager resourceManager = new ResourceManager(view.getContext(), colorUtils);
        return new TeiProgramListRepositoryImpl(
                d2,
                new ProgramViewModelMapper(resourceManager),
                metadataIconProvider
        );
    }
}
