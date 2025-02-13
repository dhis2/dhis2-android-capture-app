package org.dhis2.usescases.datasets.datasetInitial.periods

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.datasets.datasetInitial.periods.ui.DataSetPeriodDialog

@PerActivity
@Subcomponent(modules = [DatasetPeriodDialogModule::class])
fun interface DatasetPeriodDialogComponent {
    fun inject(datasetPeriodDialog: DataSetPeriodDialog)
}
