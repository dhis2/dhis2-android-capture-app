package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.commons.di.dagger.PerActivity;

import dagger.Subcomponent;

@Subcomponent(modules = DataSetInitialModule.class)
@PerActivity
public interface DataSetInitialComponent {
    void inject(DataSetInitialActivity activity);
}
