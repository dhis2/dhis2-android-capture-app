package com.dhis2.usescases.main;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.data.dagger.PerActivity;
import com.data.dagger.PerFragment;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.main.home.HomeRepository;
import com.dhis2.usescases.main.home.HomeRepositoryImpl;
import com.squareup.sqlbrite.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Consumer;

@Module
public final class MainContractsModule {

    @Provides
    View homeView(MainActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }

    @Provides
    @PerActivity
    Presenter homePresenter(D2 d2,
                            @NonNull UserRepository userRepository,
                            @NonNull HomeRepository homeRepository) {
        return new MainPresenter(d2, userRepository, homeRepository);
    }

    interface View extends AbstractActivityContracts.View {

        @NonNull
        @UiThread
        Consumer<String> renderUsername();

        @NonNull
        @UiThread
        Consumer<String> renderUserInfo();

        @NonNull
        @UiThread
        Consumer<String> renderUserInitials();
    }

    public interface Presenter {
        void init(View view);
        public void logOut();
    }

    interface Interactor {
    }

    interface Router {

    }

}