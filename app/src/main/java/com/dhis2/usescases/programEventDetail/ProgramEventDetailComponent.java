package com.dhis2.usescases.programEventDetail;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

@PerActivity
@Subcomponent(modules = ProgramEventDetailModule.class)
public interface ProgramEventDetailComponent {
    void inject(ProgramEventDetailActivity activity);
}