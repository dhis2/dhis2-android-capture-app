package com.dhis2.usescases.searchTrackEntity;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 */
@Module
public class SearchTEModule {

    @Provides
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter() {
        return new SearchTEPresenter();
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Interactor provideInteractor(D2 d2, SearchRepository searchRepository, UserRepository userRepository) {
        return new SearchTEInteractor(d2, searchRepository, userRepository);
    }

    @Provides
    SearchRepository searchRepository(BriteDatabase briteDatabase) {
        return new SearchRepositoryImpl(briteDatabase);
    }

}
