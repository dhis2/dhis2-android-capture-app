package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class SearchTEModule {

    private final SearchTEContractsModule.View view;
    private final String teiType;
    private final String initialProgram;

    public SearchTEModule(SearchTEContractsModule.View view,
                          String tEType,
                          String initialProgram) {
        this.view = view;
        this.teiType = tEType;
        this.initialProgram = initialProgram;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter(D2 d2,
                                                       SearchRepository searchRepository,
                                                       SchedulerProvider schedulerProvider,
                                                       AnalyticsHelper analyticsHelper) {
        return new SearchTEPresenter(view, d2, searchRepository, schedulerProvider, analyticsHelper, initialProgram);
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(@NonNull D2 d2) {
        return new SearchRepositoryImpl(teiType, d2);
    }
}
