package com.dhis2.usescases.programDetailTablet;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 31/10/2017.
 */

@PerActivity
@Subcomponent(modules = ProgramDetailTabletModule.class)
public interface ProgramDetailTabletComponent {
    void inject(ProgramDetailTabletActivity activity);
}