package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 24/09/2018.
 */

@Subcomponent(modules = DataSetInitialModule.class)
@PerActivity
public interface DataSetInitialComponent {
    void inject(DataSetInitialActivity activity);
}
