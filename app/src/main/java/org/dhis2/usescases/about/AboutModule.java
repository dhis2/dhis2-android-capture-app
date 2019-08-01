package org.dhis2.usescases.about;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.user.UserRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */
@Module
public class AboutModule {

    @Provides
    @PerFragment
    AboutContracts.AboutPresenter providesPresenter(@NonNull D2 d2, @NonNull UserRepository userRepository) {
        return new AboutPresenterImpl(d2, userRepository);
    }
}
