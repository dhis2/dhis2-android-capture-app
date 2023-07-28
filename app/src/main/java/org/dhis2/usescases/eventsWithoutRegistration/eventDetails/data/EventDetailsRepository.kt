package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data

import io.reactivex.Observable
import java.util.Calendar
import java.util.Date
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

class EventDetailsRepository(
    private val d2: D2,
    private val programUid: String,
    private val eventUid: String?,
    private val programStageUid: String?,
    private val fieldFactory: FieldViewModelFactory,
    private val d2ErrorMapper: D2ErrorUtils
) {

    fun getProgramStage(): ProgramStage {
        return d2.programModule()
            .programStages()
            .uid(programStageUid ?: getEvent()?.programStage())
            .blockingGet()
    }

    fun getObjectStyle(): ObjectStyle? {
        val programStage: ProgramStage = getProgramStage()
        val program = getProgram()
        return when (program?.registration()) {
            true -> programStage.style()
            else -> program?.style()
        }
    }

    fun getEditableStatus(): EventEditableStatus? {
        return eventUid?.let {
            d2.eventModule().eventService().getEditableStatus(it).blockingGet()
        }
    }

    fun getEvent(): Event? {
        return d2.eventModule().events().uid(eventUid).blockingGet()
    }

    fun getProgram(): Program? {
        return d2.programModule().programs()
            .withTrackedEntityType().byUid().eq(programUid).one().blockingGet()
    }

    fun getMinDaysFromStartByProgramStage(): Int {
        val programStage = getProgramStage()
        return if (programStage.minDaysFromStart() != null) {
            programStage.minDaysFromStart()!!
        } else {
            0
        }
    }

    fun getStageLastDate(enrollmentUid: String?): Date {
        val activeEvents =
            d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid()
                .eq(programStageUid)
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet()
        val scheduleEvents =
            d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid()
                .eq(programStageUid)
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC).blockingGet()

        var activeDate: Date? = null
        var scheduleDate: Date? = null
        if (activeEvents.isNotEmpty()) {
            activeDate = activeEvents[0].eventDate()
        }
        if (scheduleEvents.isNotEmpty()) scheduleDate = scheduleEvents[0].dueDate()

        return activeDate ?: (scheduleDate ?: Calendar.getInstance().time)
    }

    fun hasAccessDataWrite(): Boolean {
        return if (eventUid != null) {
            d2.eventModule().eventService().isEditable(eventUid).blockingGet()
        } else {
            return getProgramStage().access().data().write()
        }
    }

    fun isEnrollmentOpen(): Boolean {
        val event = d2.eventModule().events().uid(eventUid).blockingGet()
        return event?.enrollment() == null || d2.enrollmentModule().enrollments()
            .uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE
    }

    fun getEnrollmentDate(uid: String?): Date? {
        val enrollment = d2.enrollmentModule().enrollments().byUid().eq(uid).blockingGet().first()
        return enrollment.enrollmentDate()
    }

    fun getFilteredOrgUnits(date: String?, parentUid: String?): List<OrganisationUnit> {
        val organisationUnits = parentUid?.let {
            getOrgUnitsByParentUid(it)
        } ?: getOrganisationUnits()

        date?.let {
            return organisationUnits.filter {
                it.openingDate()?.after(DateUtils.databaseDateFormat().parse(date)) == false &&
                    it.closedDate()?.before(DateUtils.databaseDateFormat().parse(date)) == false
            }
        }
        return organisationUnits
    }

    private fun getOrgUnitsByParentUid(parentUid: String): List<OrganisationUnit> {
        return d2.organisationUnitModule().organisationUnits()
            .byProgramUids(listOf(programUid))
            .byParentUid().eq(parentUid)
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .blockingGet()
    }

    fun getOrganisationUnit(orgUnitUid: String): OrganisationUnit? {
        return d2.organisationUnitModule().organisationUnits()
            .byUid()
            .eq(orgUnitUid)
            .one().blockingGet()
    }

    fun getOrganisationUnits(): List<OrganisationUnit> {
        return d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byProgramUids(listOf(programUid))
            .blockingGet()
    }

    fun getGeometryModel(): FieldUiModel {
        val nonEditableStatus = ArrayList<EventStatus?>()
        nonEditableStatus.add(EventStatus.COMPLETED)
        nonEditableStatus.add(EventStatus.SKIPPED)
        val shouldBlockEdition = eventUid != null &&
            !d2.eventModule().eventService().blockingIsEditable(eventUid) &&
            nonEditableStatus.contains(
                d2.eventModule().events().uid(eventUid).blockingGet().status()
            )
        val featureType = getProgramStage().featureType()
        val accessDataWrite = hasAccessDataWrite() && isEnrollmentOpen()
        val coordinatesValue = eventUid?.let {
            d2.eventModule().events().uid(eventUid).blockingGet().geometry()?.coordinates()
        }

        return fieldFactory.create(
            id = "",
            label = "",
            valueType = ValueType.COORDINATE,
            mandatory = false,
            value = coordinatesValue,
            editable = accessDataWrite && !shouldBlockEdition,
            description = null,
            featureType = featureType
        )
    }

    fun getCatOptionCombos(categoryComboUid: String): List<CategoryOptionCombo> {
        return d2.categoryModule().categoryOptionCombos()
            .byCategoryComboUid()
            .eq(categoryComboUid)
            .blockingGet()
    }

    fun getCategoryOptionCombo(
        categoryComboUid: String?,
        categoryOptionsUid: List<String?>?
    ): String? {
        return d2.categoryModule().categoryOptionCombos()
            .byCategoryComboUid().eq(categoryComboUid)
            .byCategoryOptions(categoryOptionsUid)
            .one()?.blockingGet()?.uid()
    }

    fun getCatOption(selectedOption: String?): CategoryOption? {
        return d2.categoryModule().categoryOptions().uid(selectedOption).blockingGet()
    }

    fun getCatOptionSize(uid: String?): Int {
        return d2.categoryModule().categoryOptions()
            .byCategoryUid(uid)
            .byAccessDataWrite().isTrue
            .blockingCount()
    }

    fun getCategoryOptions(categoryUid: String): List<CategoryOption> {
        return d2.categoryModule()
            .categoryOptions()
            .withOrganisationUnits()
            .byCategoryUid(categoryUid)
            .blockingGet() ?: emptyList()
    }

    fun getOptionsFromCatOptionCombo(): Map<String, CategoryOption>? {
        return getEvent()?.let { event ->
            catCombo().let { categoryCombo ->
                val map = mutableMapOf<String, CategoryOption>()
                if (categoryCombo.isDefault == false && event.attributeOptionCombo() != null) {
                    val selectedCatOptions = d2.categoryModule()
                        .categoryOptionCombos()
                        .withCategoryOptions()
                        .uid(event.attributeOptionCombo())
                        .blockingGet().categoryOptions()
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
    }

    fun catCombo(): CategoryCombo {
        return d2.programModule().programs().uid(programUid).get()
            .flatMap { program: Program ->
                d2.categoryModule().categoryCombos()
                    .withCategories()
                    .uid(program.categoryComboUid())
                    .get()
            }.blockingGet()
    }

    fun updateEvent(
        selectedDate: Date,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        coordinates: String?
    ): Event {
        val geometry = coordinates?.let {
            Geometry.builder()
                .coordinates(it)
                .type(getProgramStage().featureType())
                .build()
        }

        return Observable.fromCallable {
            d2.eventModule().events().uid(eventUid)
        }
            .map { eventRepository: EventObjectRepository ->
                eventRepository.setEventDate(selectedDate)
                eventRepository.setOrganisationUnitUid(selectedOrgUnit)
                eventRepository.setAttributeOptionComboUid(catOptionComboUid)
                val featureType =
                    d2.programModule().programStages()
                        .uid(eventRepository.blockingGet().programStage())
                        .blockingGet().featureType()
                featureType?.let { type ->
                    when (type) {
                        FeatureType.POINT,
                        FeatureType.POLYGON,
                        FeatureType.MULTI_POLYGON -> eventRepository.setGeometry(geometry)
                        else -> {
                        }
                    }
                }
                eventRepository.blockingGet()
            }.blockingFirst()
    }

    fun getCanReopen(): Boolean = getEvent()?.let {
        it.status() == EventStatus.COMPLETED && hasReopenAuthority()
    } ?: false

    private fun hasReopenAuthority(): Boolean = d2.userModule().authorities()
        .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
        .one()
        .blockingExists()

    fun reopenEvent() = try {
        eventUid?.let {
            d2.eventModule().events().uid(it).setStatus(EventStatus.ACTIVE)
            Result.success(Unit)
        } ?: Result.success(Unit)
    } catch (d2Error: D2Error) {
        Result.failure(
            java.lang.Exception(
                d2ErrorMapper.getErrorMessage(d2Error),
                d2Error
            )
        )
    }
}
