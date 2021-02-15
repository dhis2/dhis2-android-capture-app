package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import dagger.Subcomponent
import org.dhis2.data.dagger.PerFragment

@Subcomponent(modules = [DataSetDetailModule::class])
@PerFragment
interface DataSetDetailComponent {
    fun inject(dataSetDetailFragment: DataSetDetailFragment)
}
