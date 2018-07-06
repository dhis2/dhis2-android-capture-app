package com.dhis2.usescases.about;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerFragment;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */
@Module
public class AboutModule {

    @Provides
    @PerFragment
    AboutContracts.AboutPresenter providesPresenter(@NonNull MetadataRepository metadataRepository, @NonNull UserRepository userRepository) {
        return new AboutPresenterImpl(metadataRepository, userRepository);
    }
}
