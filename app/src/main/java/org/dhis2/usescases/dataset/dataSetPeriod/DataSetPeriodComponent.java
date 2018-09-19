package org.dhis2.usescases.dataset.dataSetPeriod;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by frodriguez on 7/20/2018.
 */
@PerActivity
@Subcomponent(modules = DataSetPeriodModule.class)
public interface DataSetPeriodComponent {
    void inject(DataSetPeriodActivity activity);
}
