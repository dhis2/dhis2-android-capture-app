package org.dhis2.mobile.aggregates.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.aggregates.domain.CheckCompletionStatus
import org.dhis2.mobile.aggregates.domain.CheckValidationRulesConfiguration
import org.dhis2.mobile.aggregates.domain.CompleteDataSet
import org.dhis2.mobile.aggregates.domain.ComputeResizeAction
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValueData
import org.dhis2.mobile.aggregates.domain.GetDataValueInput
import org.dhis2.mobile.aggregates.domain.ReopenDataSet
import org.dhis2.mobile.aggregates.domain.RunValidationRules
import org.dhis2.mobile.aggregates.domain.SetDataValue
import org.dhis2.mobile.aggregates.domain.UploadFile
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.provider.DataSetModalDialogProvider
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.aggregates.ui.states.mapper.InputDataUiStateMapper
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.commons.providers.FieldErrorMessageProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val featureModule =
    module {
        singleOf(::ResourceManager)
        singleOf(::FieldErrorMessageProvider)

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
                openErrorLocation = params.get(),
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
                optionRepository = get(),
            )
        }

        factory { params ->
            SetDataValue(
                dataSetUid = params.get(),
                periodId = params.get(),
                orgUnitUid = params.get(),
                attrOptionComboUid = params.get(),
                repository = get(),
            )
        }

        factory { params ->
            CheckValidationRulesConfiguration(
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

        factory {
            DataSetModalDialogProvider(
                resourceManager = get(),
            )
        }

        factory { params ->
            CompleteDataSet(
                dataSetUid = params.get(),
                periodId = params.get(),
                orgUnitUid = params.get(),
                attrOptionComboUid = params.get(),
                dataSetInstanceRepository = get(),
            )
        }

        factory { params ->
            ReopenDataSet(
                dataSetUid = params.get(),
                periodId = params.get(),
                orgUnitUid = params.get(),
                attrOptionComboUid = params.get(),
                dataSetInstanceRepository = get(),
            )
        }

        factory { params ->
            RunValidationRules(
                dataSetUid = params.get(),
                periodId = params.get(),
                orgUnitUid = params.get(),
                attrOptionComboUid = params.get(),
                dataSetInstanceRepository = get(),
            )
        }

        factory { params ->
            UploadFile(
                repository = get(),
            )
        }

        factory {
            InputDataUiStateMapper(
                resourceManager = get(),
            )
        }

        factory { params ->
            ComputeResizeAction(
                dimensionRepository = get { parametersOf(params.get()) },
            )
        }

        viewModel { params ->
            val dataSetUid = params.get<String>()
            val periodId = params.get<String>()
            val orgUnitUid = params.get<String>()
            val attrOptionComboUid = params.get<String>()
            val openErrorLocation = params.getOrNull<Boolean>() ?: false
            val onClose = params.get<() -> Unit>()
            val uiActionHandler = params.get<UiActionHandler>()

            DataSetTableViewModel(
                onClose = onClose,
                getDataSetInstanceData =
                    get {
                        parametersOf(
                            dataSetUid,
                            periodId,
                            orgUnitUid,
                            attrOptionComboUid,
                            openErrorLocation,
                        )
                    },
                getDataSetSectionData =
                    get {
                        parametersOf(dataSetUid, orgUnitUid, periodId, attrOptionComboUid)
                    },
                getDataValueData =
                    get {
                        parametersOf(dataSetUid, orgUnitUid, periodId, attrOptionComboUid)
                    },
                getDataSetSectionIndicators =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                getDataValueInput =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                setDataValue =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                uploadFile = get(),
                resourceManager = get(),
                checkValidationRulesConfiguration =
                    get {
                        parametersOf(dataSetUid)
                    },
                checkCompletionStatus =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                datasetModalDialogProvider = get(),
                completeDataSet =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                reopenDataSet =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                dispatcher = get(),
                runValidationRules =
                    get {
                        parametersOf(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
                    },
                uiActionHandler = uiActionHandler,
                inputDataUiStateMapper = get(),
                fieldErrorMessageProvider = get(),
                computeResizeAction =
                    get {
                        parametersOf(dataSetUid)
                    },
            )
        }
    }

internal expect val platformModule: Module

val aggregatesModule =
    module {
        includes(featureModule, platformModule)
    }
