package org.dhis2.usescases.main;


import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 17/10/2017.
 */
@PerActivity
@Subcomponent(modules = MainModule.class)
public interface MainComponent {
    void inject(MainActivity mainActivity);
}
