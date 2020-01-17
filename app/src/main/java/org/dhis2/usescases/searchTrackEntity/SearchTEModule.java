package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 */
@PerActivity
@Module
public class SearchTEModule {

    private final String teiType;

    public SearchTEModule(String tEType) {
        this.teiType = tEType;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter(D2 d2, SearchRepository searchRepository, SchedulerProvider schedulerProvider) {
        return new SearchTEPresenter(d2, searchRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(@NonNull D2 d2) {
        return new SearchRepositoryImpl(teiType, d2);
    }
}
