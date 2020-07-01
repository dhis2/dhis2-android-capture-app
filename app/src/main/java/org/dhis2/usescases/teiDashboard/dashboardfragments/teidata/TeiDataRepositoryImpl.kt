package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import io.reactivex.Single
import org.dhis2.Bindings.applyFilters
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModelType
import org.dhis2.utils.DateUtils
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingStatus
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class TeiDataRepositoryImpl(
    private val d2: D2,
    private val programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : TeiDataRepository {

    override fun getTEIEnrollmentEvents(
        selectedStage: String?,
        groupedByStage: Boolean,
        periodFilters: MutableList<DatePeriod>,
        orgUnitFilters: MutableList<String>,
        stateFilters: MutableList<State>,
        assignedToMe: Boolean,
        eventStatusFilters: MutableList<EventStatus>,
        catOptComboFilters: MutableList<CategoryOptionCombo>,
        sortingItem: SortingItem?
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
            getGroupedEvents(eventRepo, selectedStage, sortingItem)
        } else {
            getTimelineEvents(eventRepo, sortingItem)
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

    private fun getGroupedEvents(
        eventRepository: EventCollectionRepository,
        selectedStage: String?,
        sortingItem: SortingItem?
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

                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.STAGE,
                            programStage,
                            null,
                            eventList.size,
                            if (eventList.isEmpty()) null else eventList[0].lastUpdated(),
                            programStage.uid() == selectedStage,
                            checkAddEvent(),
                            orgUnitName = ""
                        )
                    )
                    if (selectedStage != null && selectedStage == programStage.uid()) {
                        checkEventStatus(eventList).forEach { event ->
                            eventViewModels.add(
                                EventViewModel(
                                    EventViewModelType.EVENT,
                                    programStage,
                                    event,
                                    0,
                                    null,
                                    isSelected = true,
                                    canAddNewEvent = true,
                                    orgUnitName = ""
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
        sortingItem: SortingItem?
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()
        var eventRepo = eventRepository

        eventRepo = eventRepoSorting(sortingItem, eventRepo)

        return eventRepo
            .byDeleted().isFalse
            .get()
            .map { eventList ->
                /*eventList.sortWith(
                    Comparator { event1, event2 ->
                        event2.primaryDate().compareTo(event1.primaryDate())
                    }
                )*/
                checkEventStatus(eventList).forEach { event ->
                    val stageUid = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet()
                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.EVENT,
                            stageUid,
                            event,
                            0,
                            null,
                            isSelected = true,
                            canAddNewEvent = true,
                            orgUnitName = ""
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
                    // TODO: SDK must add method to order events by orgUnit Name
                    eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                Filters.PERIOD -> {
                    if (sortingItem.sortingStatus === SortingStatus.ASC) {
                        eventRepo
                            .orderByEventDate(RepositoryScope.OrderByDirection.ASC)
                            .orderByDueDate(RepositoryScope.OrderByDirection.ASC)
                    } else {
                        eventRepo
                            .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                            .orderByDueDate(RepositoryScope.OrderByDirection.DESC)
                    }
                }
                else -> {
                    eventRepo
                }
            }
        } else {
            eventRepo
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC)
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

    private fun checkAddEvent(): Boolean {
        val enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
        return !(enrollment == null || enrollment.status() != EnrollmentStatus.ACTIVE)
    }
}
