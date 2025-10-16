package org.dhis2.form.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.periods.model.Period
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.hisp.dhis.android.core.common.ValueType

interface FormRepository {
    suspend fun fetchFormItems(shouldOpenErrorLocation: Boolean = false): List<FieldUiModel>

    suspend fun composeList(skipProgramRules: Boolean = false): List<FieldUiModel>

    fun completeEvent()

    fun getConfigurationErrors(): List<RulesUtilsProviderConfigurationError>?

    suspend fun runDataIntegrityCheck(backPressed: Boolean): DataIntegrityCheckResult

    fun completedFieldsPercentage(value: List<FieldUiModel>): Float

    fun calculationLoopOverLimit(): Boolean

    fun backupOfChangedItems(): List<FieldUiModel>

    fun updateErrorList(action: RowAction)

    fun save(
        id: String,
        value: String?,
        extraData: String?,
    ): StoreResult?

    fun updateValueOnList(
        uid: String,
        value: String?,
        valueType: ValueType?,
    )

    fun currentFocusedItem(): FieldUiModel?

    fun setFocusedItem(action: RowAction)

    fun updateSectionOpened(action: RowAction)

    fun getDateFormatConfiguration(): String

    fun removeAllValues()

    fun setFieldLoading(
        uid: String,
        requestInProcess: Boolean,
    )

    fun setFieldAddingImage(
        uid: String,
        requestInProcess: Boolean,
    )

    fun clearFocusItem()

    fun storeFile(
        id: String,
        filePath: String?,
    ): StoreResult?

    fun areSectionCollapsable(): Boolean

    fun hasLegendSet(dataElementUid: String): Boolean

    fun getListFromPreferences(uid: String): List<String>

    fun saveListToPreferences(
        uid: String,
        list: List<String>,
    )

    fun activateEvent()

    fun fetchPeriods(): Flow<PagingData<Period>>

    fun fetchOptions(
        id: String,
        optionSetUid: String,
    )

    fun reEvaluateRequestParams(customIntentUid: String): List<CustomIntentRequestArgumentModel>

    fun isEvent(): Boolean

    fun isEventEditable(): Boolean?
}
