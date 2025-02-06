package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val featureModule = module {
    factory { params ->
        GetDataSetInstanceDetails(
            dataSetUid = params.get(),
            periodId = params.get(),
            orgUnitUid = params.get(),
            attrOptionComboUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    factory { params ->
        GetDataSetSections(
            dataSetUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    factory { params ->
        GetDataSetRenderingConfig(
            datasetUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    viewModel { params ->
        DataSetTableViewModel(
            getDataSetInstanceDetails = get { parametersOf(params.get(), params.get(), params.get(), params.get()) },
            getDataSetSections = get { parametersOf(params.get()) },
            getDataSetRenderingConfig = get { parametersOf(params.get()) },
        )
    }
}

internal expect val platformModule: Module

val aggregatesModule = module {
    includes(featureModule, platformModule)
}
