package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailComponent;
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailModule;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueComponent;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueModule;

import dagger.Subcomponent;

@Subcomponent(modules = DataSetTableModule.class)
@PerActivity
public interface DataSetTableComponent {
    void inject(DataSetTableActivity activity);

    DataSetDetailComponent plus(DataSetDetailModule dataSetDetailModule);
    DataValueComponent plus(DataValueModule dataValueModule);
}


