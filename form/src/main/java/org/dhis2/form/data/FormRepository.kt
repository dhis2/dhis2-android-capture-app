package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.hisp.dhis.android.core.common.ValueType

interface FormRepository {

    fun fetchFormItems(): List<FieldUiModel>
    fun composeList(): List<FieldUiModel>
    fun getConfigurationErrors(): List<RulesUtilsProviderConfigurationError>?
    fun runDataIntegrityCheck(allowDiscard: Boolean): DataIntegrityCheckResult
    fun completedFieldsPercentage(value: List<FieldUiModel>): Float
    fun calculationLoopOverLimit(): Boolean
    fun backupOfChangedItems(): List<FieldUiModel>
    fun updateErrorList(action: RowAction)
    fun save(id: String, value: String?, extraData: String?): StoreResult?
    fun updateValueOnList(uid: String, value: String?, valueType: ValueType?)
    fun currentFocusedItem(): FieldUiModel?
    fun setFocusedItem(action: RowAction)
    fun updateSectionOpened(action: RowAction)
    fun removeAllValues()
    fun setFieldRequestingCoordinates(uid: String, requestInProcess: Boolean)
    fun optionService(): OptionSetDialogRepository
}
