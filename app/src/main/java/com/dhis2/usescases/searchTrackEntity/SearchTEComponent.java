package com.dhis2.usescases.searchTrackEntity;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 31/10/2017.
 */

@PerActivity
@Subcomponent(modules = SearchTEModule.class)
public interface SearchTEComponent {
    void inject(SearchTEActivity activity);
}