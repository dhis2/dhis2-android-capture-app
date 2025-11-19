package org.dhis2.form.data

import android.text.TextUtils
import androidx.paging.PagingData
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.bindings.blockingGetValueCheck
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.bindings.program
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.inDateRange
import org.dhis2.commons.extensions.inOrgUnit
import org.dhis2.commons.periods.data.EventPeriodRepository
import org.dhis2.commons.periods.domain.GetEventPeriods
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.R
import org.dhis2.form.data.metadata.FormBaseConfiguration
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryOption
import org.dhis2.form.model.EventMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class EventRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2,
    metadataIconProvider: MetadataIconProvider,
    private val resources: ResourceManager,
    private val eventResourcesProvider: EventResourcesProvider,
    private val eventMode: EventMode,
    private val customIntentRepository: CustomIntentRepository,
    dispatcherProvider: DispatcherProvider,
) : DataEntryBaseRepository(
        FormBaseConfiguration(d2, dispatcherProvider),
        fieldFactory,
        metadataIconProvider,
    ) {
    private val getEventPeriods =
        GetEventPeriods(
            EventPeriodRepository(d2),
        )

    private var event =
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .blockingGet()

    private val programStage by lazy {
        d2
            .programModule()
            .programStages()
            .uid(event?.programStage())
            .blockingGet()
    }

    override val defaultStyleColor by lazy {
        programStage?.program()?.uid()?.let {
            d2
                .program(it)
                ?.style()
                ?.color()
                ?.toColor()
        } ?: SurfaceColor.Primary
    }

    override fun firstSectionToOpen(): String? =
        when (eventMode) {
            EventMode.NEW,
            EventMode.SCHEDULE,
            -> super.firstSectionToOpen()

            EventMode.CHECK -> firstSectionToOpenForEvent()
        }

    private fun firstSectionToOpenForEvent(): String? {
        val (eventDataCompleted, attrOptionComboCompleted) = isEventDataCompleted()
        return when {
            !eventDataCompleted ->
                EVENT_DETAILS_SECTION_UID

            !attrOptionComboCompleted ->
                EVENT_CATEGORY_COMBO_SECTION_UID

            else ->
                sectionUids().blockingFirst().firstOrNull { sectionUid ->
                    sectionUid != EVENT_DETAILS_SECTION_UID &&
                        sectionUid != EVENT_CATEGORY_COMBO_SECTION_UID
                }
        }
    }

    private fun isEventDataCompleted(): Pair<Boolean, Boolean> {
        val eventDateCompleted = event?.eventDate() != null
        val orgUnitCompleted = event?.organisationUnit() != null
        val coordinatesCompleted =
            if (shouldShowCoordinates()) {
                event?.geometry() != null
            } else {
                true
            }

        val dataCompleted = eventDateCompleted && orgUnitCompleted && coordinatesCompleted
        val attrOptionComboCompleted = event?.attributeOptionCombo() != null

        return Pair(dataCompleted, attrOptionComboCompleted)
    }

    override val programUid by lazy {
        event?.program()
    }

    private val sectionMap by lazy {
        d2
            .programModule()
            .programStageSections()
            .byProgramStageUid()
            .eq(event?.programStage())
            .withDataElements()
            .blockingGet()
            .associateBy { section -> section.uid() }
    }

    override fun sectionUids(): Flowable<List<String>> {
        val sectionUIDs = mutableListOf(EVENT_DETAILS_SECTION_UID)
        if (shouldAddCategoryComboSection()) {
            sectionUIDs.add(EVENT_CATEGORY_COMBO_SECTION_UID)
        }
        if (sectionMap.keys.isEmpty()) {
            sectionUIDs.add(EVENT_DATA_SECTION_UID)
        } else {
            sectionUIDs.addAll(sectionMap.keys.toList())
        }
        return Flowable.just(sectionUIDs)
    }

    override fun list(): Flowable<List<FieldUiModel>> =
        d2
            .programModule()
            .programStageSections()
            .byProgramStageUid()
            .eq(programStage?.uid())
            .withDataElements()
            .get()
            .flatMap { programStageSection ->
                if (programStageSection.isEmpty()) {
                    getFieldsForSingleSection()
                        .map { singleSectionList ->
                            val list = getEventDataSectionList()
                            list.addAll(singleSectionList)
                            list
                        }
                } else {
                    getFieldsForMultipleSections()
                }
            }.map { list ->
                val fields = getEventDetails()
                fields.addAll(list)
                fields.add(fieldFactory.createClosingSection())
                fields.toList()
            }.toFlowable()

    private fun getEventDataSectionList(): MutableList<FieldUiModel> =
        mutableListOf(
            fieldFactory.createSection(
                sectionUid = EVENT_DATA_SECTION_UID,
                sectionName =
                    eventResourcesProvider.formatWithProgramStageEventLabel(
                        stringResource = R.string.event_data_section_title,
                        programStageUid = programStage?.uid(),
                        programUid = programUid,
                    ),
                description = null,
                isOpen = true,
                totalFields = 0,
                completedFields = 0,
                rendering = SectionRenderingType.LISTING.name,
            ),
        )

    override fun isEvent(): Boolean = true

    override fun isEventEditable() =
        d2
            .eventModule()
            .eventService()
            .blockingIsEditable(eventUid)

    override fun eventMode(): EventMode = eventMode

    override fun validationStrategy(): ValidationStrategy? =
        d2
            .programModule()
            .programStages()
            .uid(programStage?.uid())
            .blockingGet()
            ?.validationStrategy()

    private fun getEventDetails(): MutableList<FieldUiModel> {
        event =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
        val eventDataItems = mutableListOf<FieldUiModel>()
        eventDataItems.apply {
            add(createEventDetailsSection())
            add(createEventReportDateField())
            add(createEventOrgUnitField())

            if (shouldShowCoordinates()) {
                add(createEventCoordinatesField())
            }

            getCatCombo()?.let { categoryCombo ->
                if (categoryCombo.isDefault == false) {
                    add(createCategoryComboSection())
                    add(createEventCategoryComboField(categoryCombo))
                }
            }
        }
        return eventDataItems
    }

    private fun shouldAddCategoryComboSection(): Boolean =
        getCatCombo()?.let { categoryCombo ->
            categoryCombo.isDefault == false
        } ?: false

    private fun createEventCategoryComboField(categoryCombo: CategoryCombo): FieldUiModel {
        val categories = getCategories(categoryCombo.categories())
        val categoryOptions =
            getOptionsFromCatOptionCombo(categoryCombo)?.entries?.associate { entry ->
                entry.key to
                    EventCategoryOption(
                        uid = entry.value.uid(),
                        name = entry.value.displayName() ?: entry.value.code() ?: "",
                    )
            } ?: emptyMap()
        val catComboDisplayName = getCatComboDisplayName(categoryCombo.uid() ?: "")

        return fieldFactory.create(
            id = "$EVENT_CATEGORY_COMBO_UID-${categoryCombo.uid()}",
            label = catComboDisplayName ?: resources.getString(R.string.cat_combo),
            valueType = ValueType.TEXT,
            value =
                categoryOptions.values.joinToString(",") {
                    it.uid
                },
            mandatory = true,
            programStageSection = EVENT_CATEGORY_COMBO_SECTION_UID,
            editable = isEventEditable(),
            description = null,
            eventCategories = categories,
        )
    }

    private fun getCatComboDisplayName(categoryComboUid: String): String? =
        d2
            .categoryModule()
            .categoryCombos()
            .uid(categoryComboUid)
            .blockingGet()
            ?.displayName()

    private fun getOptionsFromCatOptionCombo(categoryCombo: CategoryCombo): Map<String, CategoryOption>? =
        event?.let { event ->
            val map = mutableMapOf<String, CategoryOption>()
            if (categoryCombo.isDefault == false && event.attributeOptionCombo() != null) {
                val selectedCatOptions =
                    d2
                        .categoryModule()
                        .categoryOptionCombos()
                        .withCategoryOptions()
                        .uid(event.attributeOptionCombo())
                        .blockingGet()
                        ?.categoryOptions()
                categoryCombo.categories()?.forEach { category ->
                    selectedCatOptions?.forEach { categoryOption ->
                        val categoryOptions =
                            d2
                                .categoryModule()
                                .categoryOptions()
                                .byCategoryUid(category.uid())
                                .blockingGet()
                        if (categoryOptions.contains(categoryOption)) {
                            map[category.uid()] = categoryOption
                        }
                    }
                }
            }
            map
        }

    private fun getCategories(categories: MutableList<Category>?): List<EventCategory> =
        categories?.map { category ->
            EventCategory(
                uid = category.uid(),
                name = category.displayName() ?: category.uid(),
                options =
                    getCategoryOptions(category.uid())
                        .filter { option ->
                            option.inDateRange(event?.eventDate())
                        }.filter { option ->
                            option.inOrgUnit(event?.organisationUnit())
                        }.map {
                            EventCategoryOption(
                                uid = it.uid(),
                                name = it.displayName() ?: it.code() ?: "",
                            )
                        },
            )
        } ?: emptyList()

    private fun getCategoryOptions(categoryUid: String): List<CategoryOption> =
        d2
            .categoryModule()
            .categoryOptions()
            .withOrganisationUnits()
            .byCategoryUid(categoryUid)
            .byAccessDataWrite()
            .isTrue
            .blockingGet()

    private fun getCatCombo(): CategoryCombo? =
        d2
            .programModule()
            .programs()
            .uid(programUid)
            .get()
            .flatMap { program: Program ->
                d2
                    .categoryModule()
                    .categoryCombos()
                    .withCategories()
                    .uid(program.categoryCombo()?.uid())
                    .get()
            }.blockingGet()

    private fun createCategoryComboSection(): FieldUiModel =
        fieldFactory.createSection(
            sectionUid = EVENT_CATEGORY_COMBO_SECTION_UID,
            sectionName = resources.getString(R.string.category_combo),
            description = null,
            isOpen = false,
            totalFields = 0,
            completedFields = 0,
            rendering = SectionRenderingType.LISTING.name,
        )

    private fun shouldShowCoordinates(): Boolean {
        programStage?.let { programStage ->
            programStage.featureType()?.let {
                return it != FeatureType.NONE
            }
        }
        return false
    }

    private fun createEventCoordinatesField(): FieldUiModel {
        val nonEditableStatus = ArrayList<EventStatus?>()
        nonEditableStatus.add(EventStatus.COMPLETED)
        nonEditableStatus.add(EventStatus.SKIPPED)
        val shouldBlockEdition =
            !d2
                .eventModule()
                .eventService()
                .blockingIsEditable(eventUid) &&
                nonEditableStatus.contains(
                    d2
                        .eventModule()
                        .events()
                        .uid(eventUid)
                        .blockingGet()
                        ?.status(),
                )
        val featureType = programStage?.featureType()
        val accessDataWrite = hasAccessDataWrite() && isEnrollmentOpen()
        val coordinatesValue =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
                ?.geometry()
                ?.coordinates()

        return fieldFactory.create(
            id = EVENT_COORDINATE_UID,
            label = resources.getString(R.string.coordinates),
            valueType = ValueType.COORDINATE,
            mandatory = false,
            value = coordinatesValue,
            programStageSection = EVENT_DETAILS_SECTION_UID,
            editable = accessDataWrite && !shouldBlockEdition,
            description = null,
            featureType = featureType,
        )
    }

    private fun isEnrollmentOpen() =
        event?.enrollment() == null ||
            event?.let {
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(it.enrollment())
                    .blockingGet()
                    ?.status() == EnrollmentStatus.ACTIVE
            } ?: false

    private fun hasAccessDataWrite(): Boolean =
        d2
            .eventModule()
            .eventService()
            .isEditable(eventUid)
            .blockingGet()

    private fun createEventOrgUnitField(): FieldUiModel =
        fieldFactory.create(
            id = EVENT_ORG_UNIT_UID,
            label = resources.getString(R.string.org_unit),
            valueType = ValueType.ORGANISATION_UNIT,
            mandatory = true,
            optionSet = null,
            value = getStoredOrgUnit(),
            programStageSection = EVENT_DETAILS_SECTION_UID,
            editable = eventMode == EventMode.NEW,
            description = null,
            orgUnitSelectorScope = programUid?.let { OrgUnitSelectorScope.ProgramCaptureScope(it) },
        )

    private fun getStoredOrgUnit(): String? =
        event
            ?.organisationUnit()
            ?.let { orgUnitUID ->
                d2
                    .organisationUnitModule()
                    .organisationUnits()
                    .byUid()
                    .eq(orgUnitUID)
                    .one()
                    .blockingGet()
            }?.displayName()

    private fun createEventReportDateField(): FieldUiModel {
        val dateValue =
            event?.eventDate()?.let { date ->
                DateUtils.oldUiDateFormat().format(date)
            }

        return fieldFactory.create(
            id = EVENT_REPORT_DATE_UID,
            label =
                programStage?.displayExecutionDateLabel()
                    ?: eventResourcesProvider.formatWithProgramStageEventLabel(
                        R.string.event_label_date,
                        programStage?.uid(),
                        programUid,
                    ),
            valueType = ValueType.DATE,
            mandatory = true,
            optionSet = null,
            value = dateValue,
            programStageSection = EVENT_DETAILS_SECTION_UID,
            allowFutureDates = false,
            editable = isEventEditable(),
            description = null,
            periodSelector = getPeriodSelector(),
        )
    }

    private fun getPeriodSelector(): PeriodSelector? =
        programStage?.periodType()?.let { periodType ->
            if (periodType != PeriodType.Daily) {
                PeriodSelector(
                    type = periodType,
                    minDate = null,
                    maxDate = null,
                )
            } else {
                null
            }
        }

    private fun createEventDetailsSection(): FieldUiModel =
        fieldFactory.createSection(
            sectionUid = EVENT_DETAILS_SECTION_UID,
            sectionName =
                eventResourcesProvider.formatWithProgramStageEventLabel(
                    stringResource = R.string.event_details_section_title,
                    programStageUid = programStage?.uid(),
                    programUid = programUid,
                ),
            description = programStage?.description(),
            isOpen = false,
            totalFields = 0,
            completedFields = 0,
            rendering = SectionRenderingType.LISTING.name,
        )

    override fun getSpecificDataEntryItems(uid: String): List<FieldUiModel> =
        when (uid) {
            EVENT_REPORT_DATE_UID,
            EVENT_ORG_UNIT_UID,
            -> {
                getEventDetails()
            }

            else -> {
                emptyList()
            }
        }

    override fun fetchPeriods(): Flow<PagingData<Period>> {
        val periodType = programStage?.periodType() ?: PeriodType.Daily
        val stage = programStage ?: return flowOf()
        val eventEnrollmentUid = event?.enrollment() ?: return flowOf()
        return getEventPeriods(
            eventUid = eventUid,
            periodType = periodType,
            selectedDate =
                if (eventMode == EventMode.SCHEDULE) {
                    event?.dueDate()
                } else {
                    event?.eventDate()
                },
            programStage = stage,
            isScheduling = eventMode == EventMode.SCHEDULE,
            eventEnrollmentUid = eventEnrollmentUid,
        )
    }

    override fun evaluateCustomIntentRequestParameters(customIntentUid: String): Map<String, Any?> {
        val orgUnitUid =
            event
                ?.organisationUnit()
        return orgUnitUid?.let {
            customIntentRepository.reEvaluateCustomIntentRequestParams(it, customIntentUid)
        } ?: emptyMap()
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> =
        Single.fromCallable {
            val stageDataElements =
                d2
                    .programModule()
                    .programStageDataElements()
                    .withRenderType()
                    .byProgramStage()
                    .eq(programStage?.uid())
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()

            stageDataElements.map { programStageDataElement ->
                transform(programStageDataElement, EVENT_DATA_SECTION_UID)
            }
        }

    private fun getFieldsForMultipleSections(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val fields = mutableListOf<FieldUiModel>()
            sectionMap.values.forEach { programStageSection ->
                fields.add(
                    transformSection(
                        programStageSection.uid(),
                        programStageSection.displayName(),
                        programStageSection.displayDescription(),
                    ),
                )
                programStageSection.dataElements()?.forEach { dataElement ->
                    d2
                        .programModule()
                        .programStageDataElements()
                        .withRenderType()
                        .byProgramStage()
                        .eq(programStage?.uid())
                        .byDataElement()
                        .eq(dataElement.uid())
                        .one()
                        .blockingGet()
                        ?.let {
                            fields.add(
                                transform(it, programStageSection.uid()),
                            )
                        }
                }
            }
            return@fromCallable fields
        }
    }

    private fun transform(
        programStageDataElement: ProgramStageDataElement,
        sectionUid: String,
    ): FieldUiModel {
        val de =
            d2
                .dataElementModule()
                .dataElements()
                .uid(
                    programStageDataElement.dataElement()!!.uid(),
                ).blockingGet()
        val uid = de?.uid() ?: ""
        val customIntent =
            customIntentRepository.getCustomIntent(
                uid,
                event?.organisationUnit(),
                CustomIntentActionTypeModel.DATA_ENTRY,
            )
        val displayName = de?.displayName() ?: ""
        val valueType = de?.valueType()
        val mandatory = programStageDataElement.compulsory() ?: false
        val optionSet = de?.optionSetUid()
        val valueRepository =
            d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid)
        val programStageSection: ProgramStageSection? =
            sectionMap.values.firstOrNull { section ->
                section.dataElements()?.map { it.uid() }?.contains(de?.uid()) ?: false
            }
        var dataValue =
            when {
                valueRepository.blockingExists() -> valueRepository.blockingGet()?.value()
                else -> null
            }
        val friendlyValue =
            dataValue?.let {
                valueRepository
                    .blockingGetValueCheck(d2, uid)
                    .userFriendlyValue(d2, addPercentageSymbol = false)
            }
        val allowFutureDates = programStageDataElement.allowFutureDate() ?: false
        val formName = de?.displayFormName()
        val description = de?.displayDescription()
        var optionSetConfig: OptionSetConfiguration? = null
        if (!TextUtils.isEmpty(optionSet)) {
            if (!TextUtils.isEmpty(dataValue) &&
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSet)
                    .byCode()
                    .eq(dataValue)
                    .one()
                    .blockingExists()
            ) {
                dataValue =
                    d2
                        .optionModule()
                        .options()
                        .byOptionSetUid()
                        .eq(optionSet)
                        .byCode()
                        .eq(dataValue)
                        .one()
                        .blockingGet()
                        ?.displayName()
            }
            val (searchEmitter, optionFlow) =
                options(
                    optionSetUid = optionSet!!,
                    optionsToHide = emptyList(),
                    optionGroupsToHide = emptyList(),
                    optionGroupsToShow = emptyList(),
                )
            optionSetConfig =
                OptionSetConfiguration(
                    searchEmitter = searchEmitter,
                    optionFlow = optionFlow,
                    onSearch = { searchEmitter.value = it },
                )
        }
        val fieldRendering = getValueTypeDeviceRendering(programStageDataElement)
        val objectStyle = getObjectStyle(de)

        val (error, warning) =
            de?.uid()?.let { deUid ->
                getConflictErrorsAndWarnings(deUid, dataValue)
            } ?: Pair(null, null)

        val isOrgUnit =
            valueType === ValueType.ORGANISATION_UNIT
        val isDate = valueType != null && valueType.isDate
        if (!isOrgUnit && !isDate) {
            dataValue = friendlyValue
        }
        val renderingType = getSectionRenderingType(programStageSection)
        val featureType = getFeatureType(valueType)

        var fieldViewModel =
            fieldFactory.create(
                uid,
                formName ?: displayName,
                valueType!!,
                mandatory,
                optionSet,
                dataValue,
                sectionUid,
                allowFutureDates,
                isEventEditable(),
                renderingType,
                description,
                fieldRendering,
                objectStyle,
                de.fieldMask(),
                optionSetConfig,
                featureType,
                customIntentModel = customIntent,
            )

        if (!error.isNullOrEmpty()) {
            fieldViewModel = fieldViewModel.setError(error)
        }

        if (!warning.isNullOrEmpty()) {
            fieldViewModel = fieldViewModel.setWarning(warning)
        }

        return fieldViewModel
    }

    private fun getConflictErrorsAndWarnings(
        dataElementUid: String,
        dataValue: String?,
    ): Pair<String?, String?> {
        var error: String? = null
        var warning: String? = null

        val conflicts =
            d2
                .importModule()
                .trackerImportConflicts()
                .byEventUid()
                .eq(eventUid)
                .blockingGet()

        val conflict =
            conflicts
                .find { it.dataElement() == dataElementUid }

        when (conflict?.status()) {
            ImportStatus.WARNING -> warning = getError(conflict, dataValue)
            ImportStatus.ERROR -> error = getError(conflict, dataValue)
            else -> {
                // no-op
            }
        }

        return Pair(error, warning)
    }

    private fun getObjectStyle(de: DataElement?) = de?.style() ?: ObjectStyle.builder().build()

    private fun getValueTypeDeviceRendering(programStageDataElement: ProgramStageDataElement) =
        if (programStageDataElement.renderType() != null) {
            programStageDataElement
                .renderType()!!
                .mobile()
        } else {
            null
        }

    private fun getFeatureType(valueType: ValueType?) =
        when (valueType) {
            ValueType.COORDINATE -> FeatureType.POINT
            else -> null
        }

    private fun getSectionRenderingType(programStageSection: ProgramStageSection?) = programStageSection?.renderType()?.mobile()?.type()

    companion object {
        const val EVENT_DETAILS_SECTION_UID = "EVENT_DETAILS_SECTION_UID"
        const val EVENT_REPORT_DATE_UID = "EVENT_REPORT_DATE_UID"
        const val EVENT_ORG_UNIT_UID = "EVENT_ORG_UNIT_UID"
        const val EVENT_COORDINATE_UID = "EVENT_COORDINATE_UID"
        const val EVENT_CATEGORY_COMBO_SECTION_UID = "EVENT_CATEGORY_COMBO_SECTION_UID"
        const val EVENT_CATEGORY_COMBO_UID = "EVENT_CATEGORY_COMBO_UID"
        const val EVENT_DATA_SECTION_UID = "EVENT_DATA_SECTION_UID"
    }
}
