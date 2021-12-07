package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult

interface FormRepository {

    fun fetchFormItems(): List<FieldUiModel>
    fun processUserAction(action: RowAction): StoreResult
    fun composeList(): List<FieldUiModel>
    fun getConfigurationErrors(): List<RulesUtilsProviderConfigurationError>?
    fun runDataIntegrityCheck(): DataIntegrityCheckResult
    fun completedFieldsPercentage(value: List<FieldUiModel>): Float
    fun calculationLoopOverLimit(): Boolean
}
