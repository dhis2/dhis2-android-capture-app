package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import io.reactivex.Single
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.bindings.userFriendlyValue
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.StageSection
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.getProgramStageName
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.Locale

class TeiDataRepositoryImpl(
    private val d2: D2,
    private val programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String?,
    private val periodUtils: DhisPeriodUtils,
) : TeiDataRepository {

    override fun getTEIEnrollmentEvents(
        selectedStage: StageSection?,
        groupedByStage: Boolean,
        replaceProgramStageName: Boolean
    ): Single<List<EventViewModel>> {
        val eventRepo = d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)

        return if (groupedByStage) {
            getGroupedEvents(eventRepo, selectedStage!!, replaceProgramStageName)
        } else {
            getTimelineEvents(eventRepo, replaceProgramStageName)
        }
    }

    override fun getEnrollment(): Single<Enrollment?> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
    }

    override fun getEnrollmentProgram(): Single<Program?> {
        return d2.programModule().programs().uid(programUid).get()
    }

    override fun getTrackedEntityInstance(): Single<TrackedEntityInstance?> {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
    }

    override fun enrollingOrgUnit(): Single<OrganisationUnit> {
        return if (programUid == null) {
            getTrackedEntityInstance()
                .map { it.organisationUnit() }
        } else {
            getEnrollment()
                .map { it.organisationUnit() }
        }
            .flatMap {
                d2.organisationUnitModule().organisationUnits().uid(it).get()
            }
    }

    override fun eventsWithoutCatCombo(): Single<List<EventViewModel>> {
        return getEnrollmentProgram()
            .flatMap { program ->
                d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).get()
            }
            .flatMap { categoryCombo ->
                if (categoryCombo.isDefault == true) {
                    Single.just(emptyList())
                } else {
                    val defaultCatOptCombo = d2.categoryModule().categoryOptionCombos()
                        .byDisplayName().eq("default")
                        .one()
                        .blockingGet()
                    val eventsWithDefaultCatCombo = d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byAttributeOptionComboUid().eq(defaultCatOptCombo?.uid())
                        .get()
                    val eventsWithNoCatCombo = d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byAttributeOptionComboUid().isNull
                        .get()
                    val eventSource = Single.zip(
                        eventsWithDefaultCatCombo,
                        eventsWithNoCatCombo,
                    ) { sourceA, sourceB ->
                        mutableListOf<Event>().apply {
                            addAll(sourceA)
                            addAll(sourceB)
                        }
                    }
                    return@flatMap eventSource.map { events ->
                        events.map {
                            val stage = d2.programModule().programStages()
                                .uid(it.programStage())
                                .blockingGet()
                            EventViewModel(
                                type = EventViewModelType.EVENT,
                                stage = stage,
                                event = it,
                                eventCount = 0,
                                lastUpdate = null,
                                isSelected = false,
                                canAddNewEvent = false,
                                orgUnitName = it.organisationUnit()!!,
                                catComboName = null,
                                dataElementValues = null,
                                displayDate = null,
                                nameCategoryOptionCombo = null,
                            )
                        }
                    }
                }
            }
    }

    override fun getOrgUnitName(orgUnitUid: String): String {
        return d2.organisationUnitModule()
            .organisationUnits().uid(orgUnitUid).blockingGet()?.displayName() ?: ""
    }

    override fun getTeiProfilePath(): String? {
        val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
        return tei?.profilePicturePath(d2, programUid)
    }

    override fun getTeiHeader(): String? {
        return d2.trackedEntityModule().trackedEntitySearch()
            .byProgram().eq(programUid)
            .uid(teiUid).blockingGet()?.header
    }

    private fun getGroupedEvents(
        eventRepository: EventCollectionRepository,
        selectedStage: StageSection,
        replaceProgramStageName: Boolean = false
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()
        var eventRepo: EventCollectionRepository

        return d2.programModule().programStages()
            .byProgramUid().eq(programUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .get()
            .map { programStages ->
                programStages.forEach { programStage ->
                    eventRepo = eventRepository.byDeleted().isFalse
                        .byProgramStageUid().eq(programStage.uid())

                    val eventList = eventRepo
                        .orderByTimeline(RepositoryScope.OrderByDirection.DESC)
                        .blockingGet()

                    val isSelected = programStage.uid() == selectedStage.stageUid

                    val canAddEventToEnrollment = enrollmentUid?.let {
                        programStage.access()?.data()?.write() == true &&
                            d2.eventModule().eventService().blockingCanAddEventToEnrollment(
                                it,
                                programStage.uid(),
                            )
                    } ?: false

                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.STAGE,
                            programStage,
                            null,
                            eventList.size,
                            if (eventList.isEmpty()) null else eventList[0].lastUpdated(),
                            selectedStage.showOptions && isSelected,
                            canAddEventToEnrollment,
                            orgUnitName = "",
                            catComboName = "",
                            dataElementValues = emptyList(),
                            groupedByStage = true,
                            displayDate = null,
                            nameCategoryOptionCombo = null,
                        ),
                    )
                    if (isSelected) {
                        checkEventStatus(eventList).forEachIndexed { index, event ->
                            val showTopShadow = index == 0
                            val showBottomShadow = index == eventList.size - 1

                            val finalProgramStage = if (replaceProgramStageName == true)
                                programStage.toBuilder().displayName(getProgramStageName(d2, event.uid())).build()
                            else programStage

                            eventViewModels.add(
                                EventViewModel(
                                    EventViewModelType.EVENT,
                                    finalProgramStage,
                                    event,
                                    0,
                                    null,
                                    isSelected = true,
                                    canAddNewEvent = true,
                                    orgUnitName = d2.organisationUnitModule().organisationUnits()
                                        .uid(event.organisationUnit()).blockingGet()?.displayName()
                                        ?: "",
                                    catComboName = getCatOptionComboName(event.attributeOptionCombo()),
                                    dataElementValues = getEventValues(
                                        event.uid(),
                                        programStage.uid(),
                                    ),
                                    groupedByStage = true,
                                    showTopShadow = showTopShadow,
                                    showBottomShadow = showBottomShadow,
                                    displayDate = periodUtils.getPeriodUIString(
                                        programStage.periodType() ?: PeriodType.Daily,
                                        event.eventDate() ?: event.dueDate()!!,
                                        Locale.getDefault(),
                                    ),
                                    nameCategoryOptionCombo =
                                    getCategoryComboFromOptionCombo(event.attributeOptionCombo())?.displayName(),
                                ),
                            )
                        }
                    }
                }
                eventViewModels
            }
    }

    private fun getTimelineEvents(
        eventRepository: EventCollectionRepository,
        replaceProgramStageName: Boolean = false
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()

        return eventRepository
            .orderByTimeline(RepositoryScope.OrderByDirection.DESC)
            .byDeleted().isFalse
            .get()
            .map { eventList ->
                checkEventStatus(eventList).forEachIndexed { _, event ->
                    val programStage = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet()

                    val finalProgramStage = if (replaceProgramStageName == true)
                        programStage?.toBuilder()?.displayName(getProgramStageName(d2, event.uid()))?.build()
                    else programStage

                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.EVENT,
                            finalProgramStage,
                            event,
                            0,
                            null,
                            isSelected = true,
                            canAddNewEvent = true,
                            orgUnitName = d2.organisationUnitModule().organisationUnits()
                                .uid(event.organisationUnit()).blockingGet()?.displayName()
                                ?: "",
                            catComboName = getCatOptionComboName(event.attributeOptionCombo()),
                            dataElementValues = getEventValues(event.uid(), finalProgramStage?.uid()),
                            groupedByStage = false,
                            displayDate = periodUtils.getPeriodUIString(
                                programStage?.periodType() ?: PeriodType.Daily,
                                event.eventDate() ?: event.dueDate()!!,
                                Locale.getDefault(),
                            ),
                            nameCategoryOptionCombo =
                            getCategoryComboFromOptionCombo(event.attributeOptionCombo())?.displayName(),
                        ),
                    )
                }
                eventViewModels
            }
    }

    private fun getCategoryComboFromOptionCombo(categoryOptionComboUid: String?): CategoryCombo? {
        val catOptionComboUid = categoryOptionComboUid?.let {
            d2.categoryModule()
                .categoryOptionCombos()
                .uid(it)
                .blockingGet()?.categoryCombo()?.uid()
        }

        return catOptionComboUid?.let {
            d2.categoryModule()
                .categoryCombos()
                .uid(it)
                .blockingGet()
        }
    }

    private fun checkEventStatus(events: List<Event>): List<Event> {
        return events.mapNotNull { event ->
            if (event.status() == EventStatus.SCHEDULE &&
                event.dueDate()?.before(DateUtils.getInstance().today) == true
            ) {
                d2.eventModule().events().uid(event.uid()).setStatus(EventStatus.OVERDUE)
                d2.eventModule().events().uid(event.uid()).blockingGet()
            } else {
                event
            }
        }
    }

    private fun getEventValues(eventUid: String, stageUid: String?): List<Pair<String, String?>> {
        val displayInListDataElements = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .byDisplayInReports().isTrue
            .blockingGet().map {
                it.dataElement()?.uid()!!
            }
        return if (displayInListDataElements.isNotEmpty()) {
            displayInListDataElements.mapNotNull {
                val valueRepo = d2.trackedEntityModule().trackedEntityDataValues()
                    .value(eventUid, it)
                val de = d2.dataElementModule().dataElements()
                    .uid(it).blockingGet()
                if (isAcceptedValueType(de?.valueType())) {
                    Pair(
                        de?.displayFormName() ?: de?.displayName() ?: "",
                        if (valueRepo.blockingExists()) {
                            valueRepo.blockingGet().userFriendlyValue(d2)
                        } else {
                            "-"
                        },
                    )
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }
    }

    private fun isAcceptedValueType(valueType: ValueType?): Boolean {
        return when (valueType) {
            ValueType.IMAGE, ValueType.COORDINATE, ValueType.FILE_RESOURCE -> false
            else -> true
        }
    }

    private fun getCatOptionComboName(categoryOptionComboUid: String?): String? {
        return categoryOptionComboUid?.let {
            d2.categoryModule().categoryOptionCombos().uid(categoryOptionComboUid).blockingGet()
                ?.displayName()
        }
    }
}
