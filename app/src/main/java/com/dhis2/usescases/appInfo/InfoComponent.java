package com.dhis2.usescases.appInfo;

import com.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 07/02/2018.
 */
@PerFragment
@Subcomponent(modules = InfoModule.class)
public interface InfoComponent {
    void inject(AppInfoFragment infoFragment);
}
