package org.dhis2.mobile.aggregates.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.aggregates.domain.CheckCompletionStatus
import org.dhis2.mobile.aggregates.domain.CheckValidationRules
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValueData
import org.dhis2.mobile.aggregates.domain.GetDataValueInput
import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.domain.SetDataValue
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val featureModule = module {
    singleOf(::ResourceManager)

    factory {
        Dispatcher(
            io = { Dispatchers.IO },
            main = { Dispatchers.Main },
            default = { Dispatchers.Default },
        )
    }

    factory { params ->
        GetDataSetInstanceData(
            datasetUid = params.get(),
            periodId = params.get(),
            orgUnitUid = params.get(),
            attrOptionComboUid = params.get(),
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
        )
    }

    factory { params ->
        GetDataValueData(
            datasetUid = params.get(),
            orgUnitUid = params.get(),
            periodId = params.get(),
            attrOptionComboUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    factory { params ->
        GetDataSetSectionIndicators(
            dataSetUid = params.get(),
            periodId = params.get(),
            orgUnitUid = params.get(),
            attributeOptionComboUid = params.get(),
            repository = get(),
        )
    }
    factory { params ->
        GetDataValueInput(
            dataSetUid = params.get(),
            periodId = params.get(),
            orgUnitUid = params.get(),
            attrOptionComboUid = params.get(),
            repository = get(),
        )
    }

    factory { params ->
        SetDataValue(
            periodId = params.get(),
            orgUnitUid = params.get(),
            attrOptionComboUid = params.get(),
            repository = get(),
        )
    }

    factory { params ->
        CheckValidationRules(
            dataSetUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    factory { params ->
        CheckCompletionStatus(
            dataSetUid = params.get(),
            periodId = params.get(),
            orgUnitUid = params.get(),
            attrOptionComboUid = params.get(),
            dataSetInstanceRepository = get(),
        )
    }

    viewModel { params ->
        val dataSetUid = params.get<String>()
        val periodId = params.get<String>()
        val orgUnitUid = params.get<String>()
        val attrOptionComboUid = params.get<String>()

        DataSetTableViewModel(
            getDataSetInstanceData = get {
                parametersOf(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                )
            },
            getDataSetSectionData = get {
                parametersOf(dataSetUid, orgUnitUid, periodId, attrOptionComboUid)
            },
            getDataValueData = get {
                parametersOf(dataSetUid, orgUnitUid, periodId, attrOptionComboUid)
            },
            getDataSetSectionIndicators = get {
                parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
            },
            getDataValueInput = get {
                parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
            },
            setDataValue = get {
                parametersOf(periodId, orgUnitUid, attrOptionComboUid)
            },
            resourceManager = get(),
            checkValidationRules = get {
                parametersOf(dataSetUid)
            },
            checkCompletionStatus = get {
                parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
            },
            checkMandatoryFieldsStatus = get {
                parametersOf(dataSetUid)
            },
            datasetModalDialogProvider = get {
                parametersOf()
            },
            dispatcher = get(),
        )
    }
}

internal expect val platformModule: Module

val aggregatesModule = module {
    includes(featureModule, platformModule)
}
