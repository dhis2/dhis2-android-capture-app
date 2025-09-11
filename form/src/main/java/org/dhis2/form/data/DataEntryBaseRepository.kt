@file:OptIn(ExperimentalCoroutinesApi::class)

package org.dhis2.form.data

import androidx.compose.ui.graphics.Color
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.form.data.metadata.FormBaseConfiguration
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.SectionRenderingType
import timber.log.Timber

abstract class DataEntryBaseRepository(
    private val conf: FormBaseConfiguration,
    private val fieldFactory: FieldViewModelFactory,
    private val metadataIconProvider: MetadataIconProvider,
) : DataEntryRepository {
    abstract val programUid: String?
    abstract val defaultStyleColor: Color

    override fun firstSectionToOpen(): String? = sectionUids().blockingFirst().firstOrNull()

    override fun updateSection(
        sectionToUpdate: FieldUiModel,
        isSectionOpen: Boolean?,
        totalFields: Int,
        fieldsWithValue: Int,
        errorCount: Int,
        warningCount: Int,
    ): FieldUiModel =
        (sectionToUpdate as SectionUiModelImpl).copy(
            isOpen = isSectionOpen,
            totalFields = totalFields,
            completedFields = fieldsWithValue,
            errors = errorCount,
            warnings = warningCount,
        )

    override fun updateField(
        fieldUiModel: FieldUiModel,
        warningMessage: String?,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): FieldUiModel = warningMessage?.let { fieldUiModel.setError(it) } ?: fieldUiModel

    override fun options(
        optionSetUid: String,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): Pair<MutableStateFlow<String>, Flow<PagingData<OptionSetConfiguration.OptionData>>> {
        val searchFlow = MutableStateFlow("")
        return Pair(
            searchFlow,
            searchFlow
                .debounce(300)
                .flatMapLatest { query ->
                    conf
                        .options(
                            optionSetUid,
                            query,
                            optionsToHide,
                            optionGroupsToHide,
                            optionGroupsToShow,
                        ).map { pagingData ->
                            pagingData.map { option ->
                                OptionSetConfiguration.OptionData(
                                    option,
                                    metadataIconProvider(option.style(), defaultStyleColor),
                                )
                            }
                        }
                }.catch {
                    Timber.e(it)
                },
        )
    }

    override fun dateFormatConfiguration(): String? = conf.dateFormatConfiguration()

    internal fun transformSection(
        sectionUid: String,
        sectionName: String?,
        sectionDescription: String? = null,
        isOpen: Boolean = false,
        totalFields: Int = 0,
        completedFields: Int = 0,
    ): FieldUiModel =
        fieldFactory.createSection(
            sectionUid,
            sectionName,
            sectionDescription,
            isOpen,
            totalFields,
            completedFields,
            SectionRenderingType.LISTING.name,
        )

    internal fun getError(
        conflict: TrackerImportConflict?,
        dataValue: String?,
    ) = conflict?.let {
        if (it.value() == dataValue) {
            it.displayDescription()
        } else {
            null
        }
    }

    override fun disableCollapsableSections(): Boolean? = programUid?.let { conf.disableCollapsableSectionsInProgram(programUid = it) }
}
