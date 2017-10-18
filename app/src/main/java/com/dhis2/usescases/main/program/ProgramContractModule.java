package com.dhis2.usescases.main.program;

import android.support.annotation.UiThread;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.dagger.PerFragment;
import com.squareup.sqlbrite2.BriteDatabase;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.
 */
@Module
public class ProgramContractModule {

    @Provides
    View provideView(ProgramFragment fragment) {
        return fragment;
    }

    @Provides
    Presenter provideProgramPresenter(View view, HomeRepository homeRepository) {
        return new ProgramPresenter(view, homeRepository);
    }

    @Provides
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }

    interface View {

        void setUpRecycler();

        Consumer<List<HomeViewModel>> swapData();

        @UiThread
        void renderError(String message);
    }

    public interface Presenter {
        void onItemClick(HomeViewModel homeViewModel);
    }

    interface Interactor {
        void getPrograms();
    }

    interface Router {
        void goToProgramDetail();
    }


}
