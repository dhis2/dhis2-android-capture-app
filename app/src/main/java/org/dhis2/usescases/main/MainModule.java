package org.dhis2.usescases.main;


import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.user.UserRepository;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public final class MainModule {

    @Provides
    @PerActivity
    MainContracts.View homeView(MainActivity activity) {
        return activity;
    }


    @Provides
    @PerActivity
    MainContracts.Presenter homePresenter(D2 d2,
                                          @NonNull UserRepository userRepository, FirebaseJobDispatcher firebaseJobDispatcher) {
        return new MainPresenter(d2, userRepository, firebaseJobDispatcher);
    }

}