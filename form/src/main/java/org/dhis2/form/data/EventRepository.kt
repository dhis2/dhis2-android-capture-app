package org.dhis2.form.data

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.bindings.blockingGetValueCheck
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.data.metadata.FormBaseConfiguration
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryCombo
import org.dhis2.form.model.EventMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
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
import java.util.Date

class EventRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val eventUid: String,
    private val d2: D2,
    private val metadataIconProvider: MetadataIconProvider,
    private val resources: ResourceManager,
    private val dateUtils: DateUtils,
    private val eventMode: EventMode,
) : DataEntryBaseRepository(FormBaseConfiguration(d2), fieldFactory) {

    private val event by lazy {
        d2.eventModule().events().uid(eventUid)
            .blockingGet()
    }

    private val programStage by lazy {
        d2.programModule()
            .programStages()
            .uid(event?.programStage())
            .blockingGet()
    }

    override fun firstSectionToOpen(): String? {
        return when (eventMode) {
            EventMode.NEW -> super.firstSectionToOpen()
            EventMode.CHECK -> firstSectionToOpenForEvent()
        }
    }

    private fun firstSectionToOpenForEvent(): String? {
        val (eventDataCompleted, attrOptionComboCompleted) = isEventDataCompleted()
        return when {
            !eventDataCompleted ->
                EVENT_DETAILS_SECTION_UID

            !attrOptionComboCompleted ->
                EVENT_CATEGORY_COMBO_SECTION_UID

            else -> sectionUids().blockingFirst().firstOrNull { sectionUid ->
                sectionUid != EVENT_DETAILS_SECTION_UID &&
                    sectionUid != EVENT_CATEGORY_COMBO_SECTION_UID
            }
        }
    }

    private fun isEventDataCompleted(): Pair<Boolean, Boolean> {
        val eventDateCompleted = event?.eventDate() != null
        val orgUnitCompleted = event?.organisationUnit() != null
        val coordinatesCompleted = if (shouldShowCoordinates()) {
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
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(event?.programStage())
            .withDataElements()
            .blockingGet()
            .map { section -> section.uid() to section }
            .toMap()
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

    override fun list(): Flowable<List<FieldUiModel>> {
        return d2.programModule().programStageSections()
            .byProgramStageUid().eq(event?.programStage())
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
    }

    private fun getEventDataSectionList(): MutableList<FieldUiModel> {
        return mutableListOf(
            fieldFactory.createSection(
                sectionUid = EVENT_DATA_SECTION_UID,
                sectionName = resources.formatWithEventLabel(
                    stringResource = R.string.event_data_section_title,
                    programStageUid = event?.programStage(),
                ),
                description = null,
                isOpen = true,
                totalFields = 0,
                completedFields = 0,
                rendering = SectionRenderingType.LISTING.name,
            ),
        )
    }

    override fun isEvent(): Boolean {
        return true
    }

    private fun getEventDetails(): MutableList<FieldUiModel> {
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
                    addAll(createEventCategoryComboFields(categoryCombo))
                }
            }
        }
        return eventDataItems
    }

    private fun shouldAddCategoryComboSection(): Boolean {
        return getCatCombo()?.let { categoryCombo ->
            categoryCombo.isDefault == false
        } ?: false
    }

    private fun createEventCategoryComboFields(categoryCombo: CategoryCombo): List<FieldUiModel> {
        val fields = mutableListOf<FieldUiModel>()
        val categories = getCategories(categoryCombo.categories())
        val categoryOptions = getOptionsFromCatOptionCombo(categoryCombo)

        val catComboDisplayName = getCatComboDisplayName(categoryCombo.uid() ?: "")

        val eventCatCombo = EventCategoryCombo(
            categories = categories,
            categoryOptions = categoryOptions,
            selectedCategoryOptions = getSelectedCategoryOptions(categories, categoryOptions),
            displayName = catComboDisplayName,
            date = event?.eventDate(),
            orgUnitUID = event?.organisationUnit(),
        )

        fields.add(
            fieldFactory.create(
                id = "$EVENT_CATEGORY_COMBO_UID-${categoryCombo.uid()}",
                label = eventCatCombo.displayName ?: "",
                valueType = ValueType.TEXT,
                value = getUidsList(
                    eventCatCombo.selectedCategoryOptions.toMap().values.filterNotNull(),
                ).joinToString(","),
                mandatory = true,
                programStageSection = EVENT_CATEGORY_COMBO_SECTION_UID,
                editable = isEventEditable(),
                description = null,
                eventCatCombo = eventCatCombo,
            ),
        )
        return fields
    }

    private fun getCatComboDisplayName(categoryComboUid: String): String? {
        return d2.categoryModule().categoryCombos().uid(categoryComboUid)
            .blockingGet()?.displayName()
    }

    private fun getSelectedCategoryOptions(
        categories: List<EventCategory>,
        categoryOptions: Map<String, CategoryOption>?,
    ) = categories.associateBy(EventCategory::uid) { category ->
        categoryOptions?.get(category.uid)
    }

    private fun getOptionsFromCatOptionCombo(categoryCombo: CategoryCombo): Map<String, CategoryOption>? {
        return event?.let { event ->
            val map = mutableMapOf<String, CategoryOption>()
            if (categoryCombo.isDefault == false && event.attributeOptionCombo() != null) {
                val selectedCatOptions = d2.categoryModule()
                    .categoryOptionCombos()
                    .withCategoryOptions()
                    .uid(event.attributeOptionCombo())
                    .blockingGet()?.categoryOptions()
                categoryCombo.categories()?.forEach { category ->
                    selectedCatOptions?.forEach { categoryOption ->
                        val categoryOptions = d2.categoryModule()
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
    }

    private fun getCategories(categories: MutableList<Category>?): List<EventCategory> {
        return categories?.map { category ->
            EventCategory(
                uid = category.uid(),
                name = category.displayName() ?: category.uid(),
                options = getCategoryOptions(category.uid()),
            )
        } ?: emptyList()
    }

    private fun getCategoryOptions(categoryUid: String): List<CategoryOption> {
        return d2.categoryModule()
            .categoryOptions()
            .withOrganisationUnits()
            .byCategoryUid(categoryUid)
            .blockingGet()
    }

    private fun getCatCombo(): CategoryCombo? {
        return d2.programModule().programs().uid(programUid).get()
            .flatMap { program: Program ->
                d2.categoryModule().categoryCombos()
                    .withCategories()
                    .uid(program.categoryComboUid())
                    .get()
            }.blockingGet()
    }

    private fun createCategoryComboSection(): FieldUiModel {
        return fieldFactory.createSection(
            sectionUid = EVENT_CATEGORY_COMBO_SECTION_UID,
            sectionName = resources.getString(R.string.category_combo),
            description = null,
            isOpen = false,
            totalFields = 0,
            completedFields = 0,
            rendering = SectionRenderingType.LISTING.name,
        )
    }

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
        val shouldBlockEdition = !d2.eventModule().eventService()
            .blockingIsEditable(eventUid) && nonEditableStatus.contains(
            d2.eventModule().events().uid(eventUid).blockingGet()?.status(),
        )
        val featureType = programStage?.featureType()
        val accessDataWrite = hasAccessDataWrite() && isEnrollmentOpen()
        val coordinatesValue =
            d2.eventModule().events().uid(eventUid).blockingGet()?.geometry()?.coordinates()

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
        event?.enrollment() == null || event?.let {
            d2.enrollmentModule().enrollments()
                .uid(it.enrollment()).blockingGet()?.status() == EnrollmentStatus.ACTIVE
        } ?: false

    private fun hasAccessDataWrite(): Boolean {
        return d2.eventModule().eventService().isEditable(eventUid).blockingGet()
    }

    private fun createEventOrgUnitField(): FieldUiModel {
        return fieldFactory.create(
            id = EVENT_ORG_UNIT_UID,
            label = resources.getString(R.string.org_unit),
            valueType = ValueType.ORGANISATION_UNIT,
            mandatory = true,
            optionSet = null,
            value = getStoredOrgUnit(),
            programStageSection = EVENT_DETAILS_SECTION_UID,
            editable = eventMode == EventMode.NEW,
            description = null,
        )
    }

    private fun getStoredOrgUnit(): String? {
        return event?.organisationUnit()?.let { orgUnitUID ->
            d2.organisationUnitModule().organisationUnits()
                .byUid()
                .eq(orgUnitUID)
                .one().blockingGet()
        }?.displayName()
    }

    private fun createEventReportDateField(): FieldUiModel {
        val dateValue = event?.eventDate()?.let { date ->
            DateUtils.oldUiDateFormat().format(date)
        }

        return fieldFactory.create(
            id = EVENT_REPORT_DATE_UID,
            label = programStage?.executionDateLabel() ?: resources.formatWithEventLabel(
                R.string.event_label_date,
                programStage?.uid(),
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

    private fun getPeriodSelector(): PeriodSelector? {
        return programStage?.periodType()?.let { periodType ->
            if (periodType != PeriodType.Daily) {
                PeriodSelector(
                    type = periodType,
                    minDate = getPeriodMinDate(periodType),
                    maxDate = dateUtils.today,
                )
            } else {
                null
            }
        }
    }

    private fun getPeriodMinDate(periodType: PeriodType): Date? {
        d2.programModule().programs()
            .withTrackedEntityType()
            .byUid().eq(programUid)
            .one().blockingGet()?.let { program ->
                var minDate = dateUtils.expDate(
                    null,
                    program.expiryDays() ?: 0,
                    periodType,
                )
                val lastPeriodDate = dateUtils.getNextPeriod(
                    periodType,
                    minDate,
                    -1,
                    true,
                )

                if (lastPeriodDate.after(
                        dateUtils.getNextPeriod(
                            program.expiryPeriodType(),
                            minDate,
                            0,
                        ),
                    )
                ) {
                    minDate = dateUtils.getNextPeriod(periodType, lastPeriodDate, 0)
                }
                return minDate
            }
        return null
    }

    private fun createEventDetailsSection(): FieldUiModel {
        return fieldFactory.createSection(
            sectionUid = EVENT_DETAILS_SECTION_UID,
            sectionName = resources.formatWithEventLabel(
                stringResource = R.string.event_details_section_title,
                programStageUid = event?.programStage(),
            ),
            description = programStage?.description(),
            isOpen = false,
            totalFields = 0,
            completedFields = 0,
            rendering = SectionRenderingType.LISTING.name,
        )
    }

    override fun getSpecificDataEntryItems(uid: String): List<FieldUiModel> {
        return when (uid) {
            EVENT_ORG_UNIT_UID -> {
                getEventDetails()
            }

            else -> {
                emptyList()
            }
        }
    }

    private fun getFieldsForSingleSection(): Single<List<FieldUiModel>> {
        return Single.fromCallable {
            val stageDataElements =
                d2.programModule().programStageDataElements().withRenderType()
                    .byProgramStage().eq(event?.programStage())
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()

            stageDataElements.map { programStageDataElement ->
                transform(programStageDataElement, EVENT_DATA_SECTION_UID)
            }
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
                    d2.programModule().programStageDataElements().withRenderType()
                        .byProgramStage().eq(event?.programStage())
                        .byDataElement().eq(dataElement.uid())
                        .one().blockingGet()?.let {
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
        val de = d2.dataElementModule().dataElements().uid(
            programStageDataElement.dataElement()!!.uid(),
        ).blockingGet()
        val uid = de?.uid() ?: ""
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
        var dataValue = when {
            valueRepository.blockingExists() -> valueRepository.blockingGet()?.value()
            else -> null
        }
        val friendlyValue = dataValue?.let {
            valueRepository.blockingGetValueCheck(d2, uid).userFriendlyValue(d2)
        }
        val allowFutureDates = programStageDataElement.allowFutureDate() ?: false
        val formName = de?.displayFormName()
        val description = de?.displayDescription()
        var optionSetConfig: OptionSetConfiguration? = null
        if (!TextUtils.isEmpty(optionSet)) {
            if (!TextUtils.isEmpty(dataValue) && d2.optionModule().options()
                    .byOptionSetUid()
                    .eq(optionSet).byCode()
                    .eq(dataValue)
                    .one().blockingExists()
            ) {
                dataValue =
                    d2.optionModule().options().byOptionSetUid().eq(optionSet)
                        .byCode()
                        .eq(dataValue).one().blockingGet()?.displayName()
            }
            val optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .blockingCount()
            optionSetConfig = OptionSetConfiguration.config(optionCount) {
                val options = d2.optionModule().options().byOptionSetUid().eq(optionSet)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()

                val metadataIconMap =
                    options.associate { it.uid() to metadataIconProvider(it.style()) }

                OptionSetConfiguration.OptionConfigData(
                    options = options,
                    metadataIconMap = metadataIconMap,
                )
            }
        }
        val fieldRendering = getValueTypeDeviceRendering(programStageDataElement)
        val objectStyle = getObjectStyle(de)

        val (error, warning) = de?.uid()?.let { deUid ->
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

        var fieldViewModel = fieldFactory.create(
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

        val conflicts = d2.importModule().trackerImportConflicts()
            .byEventUid().eq(eventUid)
            .blockingGet()

        val conflict = conflicts
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

    private fun getObjectStyle(de: DataElement?) =
        de?.style() ?: ObjectStyle.builder().build()

    private fun getValueTypeDeviceRendering(programStageDataElement: ProgramStageDataElement) =
        if (programStageDataElement.renderType() != null) {
            programStageDataElement.renderType()!!
                .mobile()
        } else {
            null
        }

    private fun getFeatureType(valueType: ValueType?) = when (valueType) {
        ValueType.COORDINATE -> FeatureType.POINT
        else -> null
    }

    private fun getSectionRenderingType(programStageSection: ProgramStageSection?) =
        programStageSection?.renderType()?.mobile()?.type()

    private fun isEventEditable() =
        d2.eventModule().eventService().blockingIsEditable(eventUid)

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
