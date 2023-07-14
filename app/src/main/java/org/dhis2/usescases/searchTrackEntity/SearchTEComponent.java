package org.dhis2.usescases.searchTrackEntity;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEListComponent;
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEListModule;
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMapComponent;
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMapModule;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

@PerActivity
@Subcomponent(modules = SearchTEModule.class)
public interface SearchTEComponent {
    void inject(SearchTEActivity activity);

    SearchTEListComponent plus(SearchTEListModule searchTEListModule);
    SearchTEMapComponent plus(SearchTEMapModule searchTEListModule);
}