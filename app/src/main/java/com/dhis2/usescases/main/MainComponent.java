package com.dhis2.usescases.main;


import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 17/10/2017.
 */
@PerActivity
@Subcomponent(modules = MainContractsModule.class)
public interface MainComponent {
    void inject(MainActivity mainActivity);
}
