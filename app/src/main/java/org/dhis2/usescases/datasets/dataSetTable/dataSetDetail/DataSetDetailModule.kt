package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepositoryImpl

@Module
@PerFragment
class DataSetDetailModule(
    private val dataSetDetailView: DataSetDetailView,
    private val dataSetUid: String
) {
    @Provides
    @PerFragment
    fun providePresenter(
        dataSetTableRepository: DataSetTableRepositoryImpl,
        schedulers: SchedulerProvider
    ): DataSetDetailPresenter {
        return DataSetDetailPresenter(dataSetDetailView, dataSetTableRepository, schedulers)
    }
}