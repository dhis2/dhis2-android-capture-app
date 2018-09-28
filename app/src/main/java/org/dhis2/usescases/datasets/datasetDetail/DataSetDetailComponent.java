package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;


@Subcomponent (modules = DataSetDetailModule.class)
@PerActivity
public interface DataSetDetailComponent {
    void inject(DataSetDetailActivity activity);
}
