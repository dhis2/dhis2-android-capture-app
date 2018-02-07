package com.dhis2.usescases.login;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.server.ConfigurationRepository;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 07/02/2018.
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
