package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.commons.di.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = DataValueModule.class)
public interface DataValueComponent {
    void inject(DataSetSectionFragment fragment);
}
