package com.dhis2.usescases.splash;

import android.support.annotation.Nullable;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.server.ServerComponent;
import com.dhis2.data.server.UserManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 07/02/2018.
 */

@Module
public final class SplashModule {

    private final UserManager userManager;

    SplashModule(@Nullable ServerComponent serverComponent) {
        this.userManager = serverComponent == null ? null : serverComponent.userManager();
    }

    @Provides
    @PerActivity
    SplashContracts.Presenter providePresenter(MetadataRepository metadataRepository) {
        return new SplashPresenter(userManager, metadataRepository);
    }

}
