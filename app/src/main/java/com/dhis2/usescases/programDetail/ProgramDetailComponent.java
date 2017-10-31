package com.dhis2.usescases.programDetail;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 31/10/2017.
 */

@PerActivity
@Subcomponent(modules = ProgramDetailModule.class)
public interface ProgramDetailComponent {
    void inject(ProgramDetailActivity activity);
}