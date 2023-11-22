package org.dhis2.usescases.datasets.dataSetTable

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.processors.FlowableProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.validationrules.ValidationRuleResult
import org.hisp.dhis.android.core.validation.engine.ValidationResult.ValidationResultStatus
import java.util.Locale

class DataSetTablePresenter(
    private val view: DataSetTableContract.View,
    private val tableRepository: DataSetTableRepositoryImpl,
    private val periodUtils: DhisPeriodUtils,
    private val dispatchers: DispatcherProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val updateProcessor: FlowableProcessor<Unit>,
    private val openErrorLocation: Boolean,
) : ViewModel() {

    private val _dataSetScreenState = MutableStateFlow(
        DataSetScreenState(
            sections = emptyList(),
            renderDetails = null,
            initialSectionToOpenUid = null,
        ),
    )
    val dataSetScreenState: StateFlow<DataSetScreenState> = _dataSetScreenState

    init {
        viewModelScope.launch {
            val deferredSections = async(dispatchers.io()) {
                tableRepository.getSections().blockingFirst()
            }
            val renderDetails = async(dispatchers.io()) {
                DataSetRenderDetails(
                    tableRepository.getDataSet().blockingGet()?.displayName()!!,
                    tableRepository.getOrgUnit().blockingGet()?.displayName()!!,
                    tableRepository.getPeriod().map { period ->
                        periodUtils.getPeriodUIString(
                            period.periodType(),
                            period.startDate()!!,
                            Locale.getDefault(),
                        )
                    }.blockingGet(),
                    tableRepository.getCatComboName().blockingFirst(),
                    tableRepository.isComplete().blockingGet(),
                )
            }

            _dataSetScreenState.update {
                val sections = deferredSections.await()
                DataSetScreenState(
                    sections = sections,
                    renderDetails = renderDetails.await(),
                    initialSectionToOpenUid = getFirstSection(sections, openErrorLocation),
                )
            }
        }
    }

    fun runValidationRules() {
        viewModelScope.launch {
            val result = async(dispatchers.io()) {
                tableRepository.executeValidationRules().blockingFirst()
            }
            try {
                handleValidationResult(result.await())
            } catch (e: Exception) {
                view.showInternalValidationError()
            }
        }
    }

    private fun getFirstSection(
        sections: List<DataSetSection>,
        openErrorLocation: Boolean,
    ): String {
        var sectionIndexToOpen = 0
        if (openErrorLocation) {
            sectionIndexToOpen = tableRepository.getSectionIndexWithErrors(sectionIndexToOpen)
        }
        return sections[sectionIndexToOpen].uid
    }

    @VisibleForTesting
    fun handleValidationResult(result: ValidationRuleResult) {
        if (result.validationResultStatus == ValidationResultStatus.OK) {
            if (!isComplete()) {
                view.showSuccessValidationDialog()
            } else {
                view.saveAndFinish()
            }
        } else {
            view.showErrorsValidationDialog(result.violations)
        }
    }

    fun handleSaveClick() {
        if (view.isErrorBottomSheetShowing) {
            closeBottomSheet()
        }
        if (tableRepository.hasValidationRules()) {
            if (tableRepository.areValidationRulesMandatory()) {
                runValidationRules()
            } else {
                view.showValidationRuleDialog()
            }
        } else if (!isComplete()) {
            view.showSuccessValidationDialog()
        } else {
            view.saveAndFinish()
        }
    }

    fun onBackClick() {
        view.back()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun completeDataSet() {
        viewModelScope.launch {
            val mandatoryFieldOk = withContext(dispatchers.io()) {
                tableRepository.checkMandatoryFields().blockingGet().isEmpty()
            }
            val fieldCombinationOk = withContext(dispatchers.io()) {
                tableRepository.checkFieldCombination().blockingGet().val0()
            }
            val alreadyCompleted = withContext(dispatchers.io()) {
                if (mandatoryFieldOk && fieldCombinationOk) {
                    tableRepository.completeDataSetInstance().blockingGet()
                } else {
                    false
                }
            }

            if (!mandatoryFieldOk) {
                view.showMandatoryMessage(true)
            } else if (!fieldCombinationOk) {
                view.showMandatoryMessage(false)
            } else if (!alreadyCompleted) {
                view.savedAndCompleteMessage()
            } else {
                view.saveAndFinish()
            }
        }
    }

    fun reopenDataSet() {
        viewModelScope.launch {
            val result = withContext(dispatchers.io()) {
                tableRepository.reopenDataSet().blockingFirst()
            }
            view.displayReopenedMessage(result)
        }
    }

    fun shouldAllowCompleteAnyway(): Boolean {
        return !tableRepository.isComplete().blockingGet() && !isValidationMandatoryToComplete()
    }

    fun collapseExpandBottomSheet() {
        view.collapseExpandBottom()
    }

    fun closeBottomSheet() {
        view.closeBottomSheet()
    }

    fun onCompleteBottomSheet() {
        view.completeBottomSheet()
    }

    private fun isValidationMandatoryToComplete(): Boolean {
        return tableRepository.areValidationRulesMandatory()
    }

    fun isComplete(): Boolean {
        return tableRepository.isComplete().blockingGet()
    }

    fun updateData() {
        updateProcessor.onNext(Unit)
    }

    fun onClickSyncStatus() {
        analyticsHelper.trackMatomoEvent(
            Categories.DATASET_DETAIL,
            Actions.SYNC_DATASET,
            Labels.CLICK,
        )
    }

    fun editingCellValue(isEditing: Boolean) {
        if (isEditing) {
            view.startInputEdition()
        } else {
            view.finishInputEdition()
        }
    }
}
