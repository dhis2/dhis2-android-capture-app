package org.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.user.UserRepository;
import org.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 *
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
    SearchTEContractsModule.Presenter providePresenter(D2 d2, SearchRepository searchRepository, MetadataRepository metadataRepository) {
        return new SearchTEPresenter(searchRepository, metadataRepository,d2);
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(@NonNull CodeGenerator codeGenerator,
                                      @NonNull BriteDatabase briteDatabase) {
        return new SearchRepositoryImpl(codeGenerator, briteDatabase);
    }
}
