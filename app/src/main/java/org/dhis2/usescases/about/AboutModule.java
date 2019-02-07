package org.dhis2.usescases.about;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.user.UserRepository;

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
