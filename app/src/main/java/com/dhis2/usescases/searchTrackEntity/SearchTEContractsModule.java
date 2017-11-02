package com.dhis2.usescases.searchTrackEntity;

import com.dhis2.usescases.general.AbstractActivityContracts;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 02/11/2017.
 */
@Module
public class SearchTEContractsModule {

    @Provides
    View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    Presenter providePresenter(View view) {
        return new SearchTEPresenter(view);
    }

    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {

    }

    interface Interactor {

    }

    interface Router {

    }
}
