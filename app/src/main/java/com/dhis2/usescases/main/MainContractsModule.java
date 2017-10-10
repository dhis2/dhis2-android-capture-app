package com.dhis2.usescases.main;


import com.dhis2.usescases.general.AbstractActivityContracts;
import com.squareup.sqlbrite2.BriteDatabase;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Consumer;

@Module
public class MainContractsModule {

    @Provides
    View homeView(MainActivity activity){
        return activity;
    }

    @Provides
    MainPresenter homePresenter(View view, HomeRepository homeRepository) {
        return new MainPresenter(view, homeRepository);
    }

    @Provides
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }

    interface View extends AbstractActivityContracts.View {
        void renderError(String error);

        Consumer<List<HomeViewModel>> swapData();
    }

    interface Presenter {

    }

    interface Interactor {
        void getData();
    }

    interface Router {

    }

}