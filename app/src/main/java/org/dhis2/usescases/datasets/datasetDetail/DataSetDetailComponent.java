package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListComponent;
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListModule;

import dagger.Subcomponent;


@Subcomponent (modules = DataSetDetailModule.class)
@PerActivity
public interface DataSetDetailComponent {
    void inject(DataSetDetailActivity activity);
    DataSetListComponent plus(DataSetListModule dataSetListModule);
}
