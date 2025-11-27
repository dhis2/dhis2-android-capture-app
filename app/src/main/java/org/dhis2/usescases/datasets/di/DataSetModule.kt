package org.dhis2.usescases.datasets.di

import org.dhis2.usescases.datasets.datasetInitial.periods.datasetPeriodPickerModule
import org.koin.dsl.module

val dataSetModules =
    module {
        includes(datasetPeriodPickerModule)
    }
