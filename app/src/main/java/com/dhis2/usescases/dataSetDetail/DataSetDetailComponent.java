package com.dhis2.usescases.dataSetDetail;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by frodriguez on 7/20/2018.
 */
@PerActivity
@Subcomponent(modules = DataSetDetailModule.class)
public interface DataSetDetailComponent {
    void inject(DataSetDetailActivity activity);
}
