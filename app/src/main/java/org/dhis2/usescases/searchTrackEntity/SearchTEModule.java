package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.utils.CodeGenerator;
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
    SearchTEContractsModule.Presenter providePresenter(D2 d2, SearchRepository searchRepository) {
        return new SearchTEPresenter(d2, searchRepository);
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(@NonNull CodeGenerator codeGenerator,
                                      @NonNull BriteDatabase briteDatabase,
                                      @NonNull D2 d2) {
        return new SearchRepositoryImpl(codeGenerator, briteDatabase, teiType,d2);
    }
}
