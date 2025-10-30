package org.dhis2.usescases.datasets.datasetInitial.periods

import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriodMaxDate
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriods
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.HasDataInputPeriods
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val datasetPeriodPickerModule =
    module {
        single<DateUtils> { DateUtils() }
        single {
            DatasetPeriodRepository(get(), get())
        }
        single { GetDatasetPeriods(get()) }
        single { GetDatasetPeriodMaxDate(get()) }
        single { HasDataInputPeriods(get()) }

        viewModel {
            DatasetPeriodViewModel(get(), get(), get())
        }
    }
