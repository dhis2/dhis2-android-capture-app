package com.dhis2.usescases.main;


import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.user.UserRepository;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public final class MainContractsModule {

    @Provides
    MainContracts.View homeView(MainActivity activity) {
        return activity;
    }


    @Provides
    @PerActivity
    MainContracts.Presenter homePresenter(D2 d2,
                                          @NonNull UserRepository userRepository) {
        return new MainPresenter(d2, userRepository);
    }



}