package org.dhis2.usescases.login;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.server.ConfigurationRepository;

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
    LoginContracts.Presenter providePresenter(ConfigurationRepository configurationRepository) {
        return new LoginPresenter(configurationRepository);
    }


}
