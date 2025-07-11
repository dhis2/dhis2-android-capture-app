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
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.settings.CustomIntentActionType
import timber.log.Timber

abstract class DataEntryBaseRepository(
    private val conf: FormBaseConfiguration,
    private val fieldFactory: FieldViewModelFactory,
    private val metadataIconProvider: MetadataIconProvider,
) : DataEntryRepository {

    abstract val programUid: String?
    abstract val defaultStyleColor: Color
    override fun firstSectionToOpen(): String? {
        return sectionUids().blockingFirst().firstOrNull()
    }

    override fun updateSection(
        sectionToUpdate: FieldUiModel,
        isSectionOpen: Boolean?,
        totalFields: Int,
        fieldsWithValue: Int,
        errorCount: Int,
        warningCount: Int,
    ): FieldUiModel {
        return (sectionToUpdate as SectionUiModelImpl).copy(
            isOpen = isSectionOpen,
            totalFields = totalFields,
            completedFields = fieldsWithValue,
            errors = errorCount,
            warnings = warningCount,
        )
    }

    override fun updateField(
        fieldUiModel: FieldUiModel,
        warningMessage: String?,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): FieldUiModel {
        return warningMessage?.let { fieldUiModel.setError(it) } ?: fieldUiModel
    }

    private fun optionsFromGroups(optionGroupUids: List<String>): List<String> {
        if (optionGroupUids.isEmpty()) return emptyList()
        val optionsFromGroups = arrayListOf<String>()
        val optionGroups = conf.optionGroups(optionGroupUids)
        for (optionGroup in optionGroups) {
            for (option in optionGroup.options()!!) {
                if (!optionsFromGroups.contains(option.uid())) {
                    optionsFromGroups.add(option.uid())
                }
            }
        }
        return optionsFromGroups
    }

    private fun uidIsACustomIntentTrigger(uid: String?): Boolean {
        return conf.customIntents().any { customIntent ->
            customIntent?.trigger()?.attributes()?.any {
                it.uid() == uid
            } == true ||
                customIntent?.trigger()?.dataElements()?.any {
                    it.uid() == uid
                } == true
        }
    }

    fun getCustomIntentFromUid(uid: String?): CustomIntentModel? {
        return if (uidIsACustomIntentTrigger(uid)) {
            val customIntentDTO = conf.customIntents().firstOrNull { customIntent ->
                customIntent?.action()?.contains(CustomIntentActionType.DATA_ENTRY) == true &&
                    customIntent.trigger()?.attributes()?.any { it.uid() == uid } == true ||
                    customIntent?.trigger()?.dataElements()?.any { it.uid() == uid } == true
            }
            customIntentDTO?.let {
                // TODO: will be mapped correctly in issue #ANDROAPP-7130
                val customIntentResponse = if (uid == "bYZCH0o9l8W" || uid == "goBca56SGgZ") {
                    listOf(
                        CustomIntentResponseDataModel(
                            name = it.response()?.data()?.argument() ?: "",
                            extraType = CustomIntentResponseExtraType.OBJECT,
                            key = it.response()?.data()?.path(),
                        ),
                    )
                } else {
                    listOf(
                        CustomIntentResponseDataModel(
                            name = it.response()?.data()?.argument() ?: "",
                            extraType = CustomIntentResponseExtraType.LIST_OF_OBJECTS,
                            key = it.response()?.data()?.path(),
                        ),
                    )
                }

                CustomIntentModel(
                    uid = it.uid(),
                    name = it.name(),
                    customIntentRequest = it.request()?.arguments()?.map { arg ->
                        CustomIntentRequestArgumentModel(
                            key = arg.key(),
                            value = arg.value(),
                        )
                    } ?: emptyList(),
                    customIntentResponse = customIntentResponse,
                    packageName = it.packageName() ?: "",
                )
            }
        } else {
            null
        }
    }

    override fun options(
        optionSetUid: String,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): Pair<MutableStateFlow<String>, Flow<PagingData<OptionSetConfiguration.OptionData>>> {
        val searchFlow = MutableStateFlow("")
        return Pair(
            searchFlow,
            searchFlow.debounce(300)
                .flatMapLatest { query ->
                    conf.options(
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
                }
                .catch {
                    Timber.e(it)
                },
        )
    }

    override fun dateFormatConfiguration(): String? {
        return conf.dateFormatConfiguration()
    }

    internal fun transformSection(
        sectionUid: String,
        sectionName: String?,
        sectionDescription: String? = null,
        isOpen: Boolean = false,
        totalFields: Int = 0,
        completedFields: Int = 0,
    ): FieldUiModel {
        return fieldFactory.createSection(
            sectionUid,
            sectionName,
            sectionDescription,
            isOpen,
            totalFields,
            completedFields,
            SectionRenderingType.LISTING.name,
        )
    }

    internal fun getError(conflict: TrackerImportConflict?, dataValue: String?) = conflict?.let {
        if (it.value() == dataValue) {
            it.displayDescription()
        } else {
            null
        }
    }

    override fun disableCollapsableSections(): Boolean? {
        return programUid?.let { conf.disableCollapsableSectionsInProgram(programUid = it) }
    }
}
