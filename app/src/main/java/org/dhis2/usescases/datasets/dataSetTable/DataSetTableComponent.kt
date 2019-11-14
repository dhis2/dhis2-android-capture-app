package org.dhis2.usescases.datasets.dataSetTable

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@Subcomponent(modules = [DataSetTableModule::class])
@PerActivity
interface DataSetTableComponent {
    fun inject(activity: DataSetTableActivity)
}
