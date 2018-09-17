package org.dhis2.usescases.jira;

import org.dhis2.data.dagger.PerFragment;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

@Module
public class JiraModule {

    @Provides
    @PerFragment
    JiraPresenter providesPresenter() {
        return new JiraPresenterImpl();
    }
}
