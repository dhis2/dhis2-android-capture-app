package org.dhis2.usescases.datasets.datasetInitial.periods

import dagger.Module
import dagger.Provides
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriodMaxDate
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriods
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.HasDataInputPeriods
import org.hisp.dhis.android.core.D2

@Module
class DatasetPeriodDialogModule {

    @Provides
    fun providesDateUtils(): DateUtils = DateUtils.getInstance()

    @Provides
    @PerActivity
    fun providesDataInputPeriodRepository(
        d2: D2,
        dateUtils: DateUtils,
    ) = DatasetPeriodRepository(d2, dateUtils)

    @Provides
    @PerActivity
    fun datasetPeriodViewModelFactory(
        getDatasetPeriods: GetDatasetPeriods,
        hasDatasetPeriods: HasDataInputPeriods,
        getDatasetPeriodMaxDate: GetDatasetPeriodMaxDate,
    ) = DatasetPeriodViewModelFactory(getDatasetPeriods, hasDatasetPeriods, getDatasetPeriodMaxDate)

    @Provides
    @PerActivity
    fun provideGetDatasetPeriods(
        dataInputPeriodRepository: DatasetPeriodRepository,
    ) = GetDatasetPeriods(dataInputPeriodRepository)

    @Provides
    @PerActivity
    fun provideHasDataInputPeriods(
        dataInputPeriodRepository: DatasetPeriodRepository,
    ) = HasDataInputPeriods(dataInputPeriodRepository)

    @Provides
    @PerActivity
    fun provideGetDatasetPeriodMaxDate(
        dataInputPeriodRepository: DatasetPeriodRepository,
    ) = GetDatasetPeriodMaxDate(dataInputPeriodRepository)
}
