package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepositoryImpl
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController

@Module
class DataSetDetailModule(
    private val dataSetDetailView: DataSetDetailView,
    private val dataSetUid: String
) {
    @Provides
    @PerFragment
    fun providePresenter(
        dataSetTableRepository: DataSetTableRepositoryImpl,
        schedulers: SchedulerProvider,
        matomoAnalyticsController: MatomoAnalyticsController,
        updateProcessor: FlowableProcessor<Unit>
    ): DataSetDetailPresenter {
        return DataSetDetailPresenter(
            dataSetDetailView,
            dataSetTableRepository,
            schedulers,
            matomoAnalyticsController,
            updateProcessor
        )
    }
}
