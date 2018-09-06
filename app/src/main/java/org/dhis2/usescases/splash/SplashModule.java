package org.dhis2.usescases.splash;

import android.support.annotation.Nullable;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.UserManager;
import com.squareup.sqlbrite2.BriteDatabase;

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
    SplashContracts.Presenter providePresenter(MetadataRepository metadataRepository, SplashRepository splashRepository) {
        return new SplashPresenter(userManager, metadataRepository, splashRepository);
    }

    @Provides
    @PerActivity
    SplashRepository splashRepository(BriteDatabase briteDatabase) {
        return new SplashRepositoryImpl(briteDatabase);
    }

}
