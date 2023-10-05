package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.FieldUiModel

interface DataEntryRepository {
    fun list(): Flowable<List<FieldUiModel>>
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

    fun disableCollapsableSections(): Boolean?
}
