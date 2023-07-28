package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import io.reactivex.Single
import java.util.Locale
import org.dhis2.Bindings.applyFilters
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.getProgramStageName
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class TeiDataRepositoryImpl(
    private val d2: D2,
    private val programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String?,
    private val periodUtils: DhisPeriodUtils
) : TeiDataRepository {

    override fun getTEIEnrollmentEvents(
        selectedStage: StageSection?,
        groupedByStage: Boolean,
        periodFilters: MutableList<DatePeriod>,
        orgUnitFilters: MutableList<String>,
        stateFilters: MutableList<State>,
        assignedToMe: Boolean,
        eventStatusFilters: MutableList<EventStatus>,
        catOptComboFilters: MutableList<CategoryOptionCombo>,
        sortingItem: SortingItem?,
        replaceProgramStageName: Boolean
    ): Single<List<EventViewModel>> {
        var eventRepo = d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)

        eventRepo = eventRepo.applyFilters(
            periodFilters,
            orgUnitFilters,
            stateFilters,
            if (assignedToMe) {
                d2.userModule().user().blockingGet().uid()
            } else {
                null
            },
            eventStatusFilters,
            catOptComboFilters
        )



        return if (groupedByStage) {
            if (selectedStage == null){
                throw Exception("When groupedByStage is true then selectedStage can't be null")
            }

            getGroupedEvents(eventRepo, selectedStage, sortingItem, replaceProgramStageName)
        } else {
            getTimelineEvents(eventRepo, sortingItem,replaceProgramStageName)
        }
    }

    override fun getEnrollment(): Single<Enrollment> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
    }

    override fun getEnrollmentProgram(): Single<Program> {
        return d2.programModule().programs().uid(programUid).get()
    }

    override fun getTrackedEntityInstance(): Single<TrackedEntityInstance> {
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
                        .byAttributeOptionComboUid().eq(defaultCatOptCombo.uid())
                        .get()
                    val eventsWithNoCatCombo = d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byAttributeOptionComboUid().isNull
                        .get()
                    val eventSource = Single.zip(
                        eventsWithDefaultCatCombo,
                        eventsWithNoCatCombo
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
                                displayDate = null
                            )
                        }
                    }
                }
            }
    }

    override fun getOrgUnitName(orgUnitUid: String): String {
        return d2.organisationUnitModule()
            .organisationUnits().uid(orgUnitUid).blockingGet().displayName() ?: ""
    }

    private fun getGroupedEvents(
        eventRepository: EventCollectionRepository,
        selectedStage: StageSection,
        sortingItem: SortingItem?,
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

                    eventRepo = eventRepoSorting(sortingItem, eventRepo)
                    val eventList = eventRepo.blockingGet()

                    val isSelected = programStage.uid() == selectedStage.stageUid

                    val canAddEventToEnrollment = enrollmentUid?.let {
                        programStage.access()?.data()?.write() == true &&
                            d2.eventModule().eventService().blockingCanAddEventToEnrollment(
                                it,
                                programStage.uid()
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
                            displayDate = null
                        )
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
                                        .uid(event.organisationUnit()).blockingGet().displayName()
                                        ?: "",
                                    catComboName = getCatComboName(event.attributeOptionCombo()),
                                    dataElementValues = getEventValues(
                                        event.uid(),
                                        programStage.uid()
                                    ),
                                    groupedByStage = true,
                                    showTopShadow = showTopShadow,
                                    showBottomShadow = showBottomShadow,
                                    displayDate = periodUtils.getPeriodUIString(
                                        programStage.periodType() ?: PeriodType.Daily,
                                        event.eventDate() ?: event.dueDate()!!,
                                        Locale.getDefault()
                                    )
                                )
                            )
                        }
                    }
                }
                eventViewModels
            }
    }

    private fun getTimelineEvents(
        eventRepository: EventCollectionRepository,
        sortingItem: SortingItem?,
        replaceProgramStageName: Boolean = false
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()
        var eventRepo = eventRepository

        eventRepo = eventRepoSorting(sortingItem, eventRepo)

        return eventRepo
            .byDeleted().isFalse
            .get()
            .map { eventList ->
                checkEventStatus(eventList).forEachIndexed { index, event ->
                    val programStage = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet()

                    val finalProgramStage = if (replaceProgramStageName == true)
                        programStage.toBuilder().displayName(getProgramStageName(d2, event.uid())).build()
                    else programStage

                    val showTopShadow = index == 0
                    val showBottomShadow = index == eventList.size - 1
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
                                .uid(event.organisationUnit()).blockingGet().displayName()
                                ?: "",
                            catComboName = getCatComboName(event.attributeOptionCombo()),
                            dataElementValues = getEventValues(event.uid(), finalProgramStage.uid()),
                            groupedByStage = false,
                            displayDate = periodUtils.getPeriodUIString(
                                programStage.periodType() ?: PeriodType.Daily,
                                event.eventDate() ?: event.dueDate()!!,
                                Locale.getDefault()
                            )
                        )
                    )
                }
                eventViewModels
            }
    }

    private fun eventRepoSorting(
        sortingItem: SortingItem?,
        eventRepo: EventCollectionRepository
    ): EventCollectionRepository {
        return if (sortingItem != null) {
            when (sortingItem.filterSelectedForSorting) {
                Filters.ORG_UNIT ->
                    if (sortingItem.sortingStatus == SortingStatus.ASC) {
                        eventRepo.orderByOrganisationUnitName(RepositoryScope.OrderByDirection.ASC)
                    } else {
                        eventRepo.orderByOrganisationUnitName(RepositoryScope.OrderByDirection.DESC)
                    }
                Filters.PERIOD -> {
                    if (sortingItem.sortingStatus === SortingStatus.ASC) {
                        eventRepo
                            .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                    } else {
                        eventRepo
                            .orderByTimeline(RepositoryScope.OrderByDirection.DESC)
                    }
                }
                else -> {
                    eventRepo
                }
            }
        } else {
            eventRepo
                .orderByTimeline(RepositoryScope.OrderByDirection.DESC)
        }
    }

    private fun checkEventStatus(events: List<Event>): List<Event> {
        return events.map { event ->
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

    private fun getEventValues(eventUid: String, stageUid: String): List<Pair<String, String?>> {
        val displayInListDataElements = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .byDisplayInReports().isTrue
            .blockingGet().map {
                it.dataElement()?.uid()!!
            }
        return if (displayInListDataElements.isNotEmpty()) {
            displayInListDataElements.map {
                val valueRepo = d2.trackedEntityModule().trackedEntityDataValues()
                    .value(eventUid, it)
                val de = d2.dataElementModule().dataElements()
                    .uid(it).blockingGet()
                Pair(
                    de.displayFormName() ?: de.displayName() ?: "",
                    if (valueRepo.blockingExists()) {
                        valueRepo.blockingGet().userFriendlyValue(d2)
                    } else {
                        "-"
                    }
                )
            }
        } else {
            emptyList()
        }
    }

    private fun getCatComboName(categoryOptionComboUid: String?): String? {
        return categoryOptionComboUid?.let {
            d2.categoryModule().categoryOptionCombos().uid(categoryOptionComboUid).blockingGet()
                .displayName()
        }
    }
}
