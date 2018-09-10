package org.dhis2.usescases.login;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.server.ConfigurationRepository;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
public class LoginModule {

    @Provides
    @PerActivity
    LoginContracts.Presenter providePresenter(ConfigurationRepository configurationRepository, MetadataRepository metadataRepository, FirebaseJobDispatcher jobDispatcher) {
        return new LoginPresenter(configurationRepository, metadataRepository, jobDispatcher);
    }
}
