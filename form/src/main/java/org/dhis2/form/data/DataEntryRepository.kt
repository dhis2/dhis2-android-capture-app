package org.dhis2.form.data

import androidx.paging.PagingData
import io.reactivex.Flowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.commons.periods.model.Period
import org.dhis2.form.model.EventMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.hisp.dhis.android.core.common.ValidationStrategy

interface DataEntryRepository {
    fun list(): Flowable<List<FieldUiModel>>

    fun firstSectionToOpen(): String?

    fun sectionUids(): Flowable<List<String>>

    fun updateSection(
        sectionToUpdate: FieldUiModel,
        isSectionOpen: Boolean?,
        totalFields: Int,
        fieldsWithValue: Int,
        errorCount: Int,
        warningCount: Int,
    ): FieldUiModel

    fun updateField(
        fieldUiModel: FieldUiModel,
        warningMessage: String?,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): FieldUiModel

    fun isEvent(): Boolean

    fun isEventEditable(): Boolean?

    fun eventMode(): EventMode?

    fun validationStrategy(): ValidationStrategy?

    fun dateFormatConfiguration(): String?

    fun disableCollapsableSections(): Boolean?

    fun getSpecificDataEntryItems(uid: String): List<FieldUiModel>

    fun fetchPeriods(): Flow<PagingData<Period>>

    fun options(
        optionSetUid: String,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): Pair<MutableStateFlow<String>, Flow<PagingData<OptionSetConfiguration.OptionData>>>

    fun evaluateCustomIntentRequestParameters(customIntentUid: String): Map<String, Any?>
}
