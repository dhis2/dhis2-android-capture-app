package org.dhis2.mobile.aggregates.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val featureModule = module {
    factory {
        Dispatcher(
            io = { Dispatchers.IO },
            main = { Dispatchers.Main },
            default = { Dispatchers.Default },
        )
    }

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

    factory { params ->
        GetDataSetSectionData(
            datasetUid = params.get(),
            orgUnitUid = params.get(),
            periodId = params.get(),
            attrOptionComboUid = params.get(),
            dataSetInstanceRepository = get(),
            valueParser = get(),
        )
    }

    viewModel { params ->
        val dataSetUid = params.get<String>()
        val periodId = params.get<String>()
        val orgUnitUid = params.get<String>()
        val attrOptionComboUid = params.get<String>()

        DataSetTableViewModel(
            getDataSetInstanceDetails = get {
                parametersOf(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                )
            },
            getDataSetSections = get {
                parametersOf(dataSetUid)
            },
            getDataSetRenderingConfig = get {
                parametersOf(dataSetUid)
            },
            getDataSetSectionData = get {
                parametersOf(dataSetUid, orgUnitUid, periodId, attrOptionComboUid)
            },
            dispatcher = get(),
        )
    }
}

internal expect val platformModule: Module

val aggregatesModule = module {
    includes(featureModule, platformModule)
}
