package com.dhis2.usescases.searchTrackEntity;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 */
@PerActivity
@Module
public class SearchTEModule {

    @Provides
    @PerActivity
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter(SearchRepository searchRepository, UserRepository userRepository, MetadataRepository metadataRepository) {
        return new SearchTEPresenter(searchRepository, userRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(BriteDatabase briteDatabase) {
        return new SearchRepositoryImpl(briteDatabase);
    }

   /* @Provides
    @PerActivity
    SearchTEAdapter searchTEAdapter(SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository){
        return new SearchTEAdapter(presenter,metadataRepository);
    }*/

}
