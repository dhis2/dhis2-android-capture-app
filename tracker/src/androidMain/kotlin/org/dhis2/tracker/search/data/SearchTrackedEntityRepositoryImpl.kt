package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.mobile.commons.extensions.getTodayAsInstant
import org.dhis2.mobile.commons.extensions.toKtxInstant
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipGeometry
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainObjectStyle
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemHelper.toTrackedEntityInstance
import kotlin.time.Instant

class SearchTrackedEntityRepositoryImpl(
    private val d2: D2,
    private val filterPresenter: FilterPresenter,
    private val profilePictureProvider: ProfilePictureProvider,
) : SearchTrackedEntityRepository {
    private var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository? = null

    // Checks whether the dataId is an attribute of the teType
    override suspend fun isTETypeAttribute(
        teType: String,
        dataId: String,
    ): Boolean =
        d2
            .trackedEntityModule()
            .trackedEntityTypeAttributes()
            .byTrackedEntityTypeUid()
            .eq(teType)
            .byTrackedEntityAttributeUid()
            .eq(dataId)
            .one()
            .blockingExists()

    override suspend fun getTEAttribute(dataId: String): SearchTrackedEntityAttribute {
        val attribute =
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(dataId)
                .blockingGet()
        return SearchTrackedEntityAttribute(
            isUnique = attribute?.unique() == true,
            isOptionSet = (attribute?.optionSet() != null),
        )
    }

    override suspend fun addToQuery(
        dataId: String,
        dataValues: List<String>?,
        searchOperator: SearchOperator?,
    ) {
        if (dataValues.isNullOrEmpty()) return

        trackedEntityInstanceQuery =
            if (dataValues.size > 1) {
                // return any tracked entities with attributes that match the values in the list
                trackedEntityInstanceQuery?.byFilter(dataId)?.`in`(dataValues)
            } else {
                when (searchOperator) {
                    SearchOperator.LIKE -> {
                        trackedEntityInstanceQuery?.byFilter(dataId)?.like(dataValues[0])
                    }

                    SearchOperator.SW -> {
                        trackedEntityInstanceQuery?.byFilter(dataId)?.sw(dataValues[0])
                    }

                    SearchOperator.EW -> {
                        trackedEntityInstanceQuery?.byFilter(dataId)?.ew(dataValues[0])
                    }

                    SearchOperator.EQ -> {
                        trackedEntityInstanceQuery?.byFilter(dataId)?.eq(dataValues[0])
                    }

                    else -> trackedEntityInstanceQuery?.byFilter(dataId)?.like(dataValues[0])
                }
            }
    }

    override suspend fun addFiltersToQuery(
        program: String?,
        teType: String,
    ) {
        trackedEntityInstanceQuery =
            filterPresenter.filteredTrackedEntityInstances(
                program,
                teType,
            )
    }

    override suspend fun excludeValuesFromQuery(excludeValues: List<String>) {
        trackedEntityInstanceQuery = trackedEntityInstanceQuery?.excludeUids()?.`in`(excludeValues)
    }

    override fun fetchResults(
        isOnline: Boolean,
        hasStateFilters: Boolean,
        allowCache: Boolean,
        selectedProgram: String?,
    ): Flow<PagingData<TrackedEntitySearchItemResult>> {
        // if the device is online and there are no state filters, we can use online cache
        val pagerFlow =
            if (isOnline && !hasStateFilters) {
                trackedEntityInstanceQuery?.allowOnlineCache()?.eq(allowCache)?.offlineFirst()
            } else {
                // otherwise we use offline only
                trackedEntityInstanceQuery?.allowOnlineCache()?.eq(allowCache)?.offlineOnly()
            }
        val displayOrgUnit = shouldDisplayOrgUnit(selectedProgram)
        // map the paging data to TrackedEntitySearchItemResult
        return pagerFlow?.getPagingData(10)?.map { pagingData ->
            pagingData.map { item ->
                mapItemToDomainResult(item, selectedProgram, hasStateFilters, displayOrgUnit)
            }
        } ?: throw IllegalStateException("TrackedEntityInstanceQuery is not initialized")
    }

    override suspend fun fetchImmediateResults(
        isOnline: Boolean,
        hasStateFilters: Boolean,
        selectedProgram: String?,
    ): List<TrackedEntitySearchItemResult> {
        // if the device is online and there are no state filters, we can use online cache
        val results =
            if (isOnline && !hasStateFilters) {
                trackedEntityInstanceQuery?.offlineFirst()?.blockingGet()
            } else {
                // otherwise we use offline only
                trackedEntityInstanceQuery?.offlineOnly()?.blockingGet()
            }
        val displayOrgUnit = shouldDisplayOrgUnit(selectedProgram)
        return results?.map { item ->
            mapItemToDomainResult(item, selectedProgram, hasStateFilters, displayOrgUnit)
        } ?: throw IllegalStateException("TrackedEntityInstanceQuery is not initialized")
    }

    private fun mapItemToDomainResult(
        item: TrackedEntitySearchItem,
        selectedProgram: String?,
        hasStateFilters: Boolean,
        displayOrgUnit: Boolean,
    ): TrackedEntitySearchItemResult {
        val dbTei = getDatabaseTei(item)
        val selectedEnrollment = getSelectedEnrollment(dbTei, selectedProgram)
        val isOnline = !(!item.isOnline && !hasStateFilters && dbTei?.deleted() == false)
        val enrollments: List<DomainEnrollment> = getTeiEnrollments(item.uid)
        val overDueDate: Instant? = getOverdueDate(enrollments, selectedProgram)
        val relationShips: List<RelationshipModel>? =
            if (dbTei.aggregatedSyncState() != State.RELATIONSHIP && selectedProgram?.isNotEmpty() == true) {
                getRelationShips(item.uid, selectedProgram, item.type.uid())
            } else {
                null
            }

        val enrolledOrgUnit =
            if (selectedEnrollment != null) {
                orgUnitName(selectedEnrollment.orgUnit)
            } else {
                orgUnitName(item.organisationUnit)
            }

        val ownerOrgUnit = item.programOwners?.get(0)?.ownerOrgUnit
        val ownerOrgUnitName =
            if (ownerOrgUnit != selectedEnrollment?.orgUnit) {
                orgUnitName(ownerOrgUnit)
            } else {
                null
            }

        val profilePicture = profilePictureProvider.invoke(dbTei, selectedProgram)
        val enrolledPrograms = getEnrolledPrograms(enrollments)
        return item.toTrackedEntitySearchItemResult(
            selectedEnrollment = selectedEnrollment,
            isOnline = isOnline,
            overDueDate = overDueDate,
            ownerOrgUnit = ownerOrgUnitName,
            enrollmentOrgUnit = enrolledOrgUnit,
            shouldDisplayOrgUnit = displayOrgUnit,
            profilePicture = profilePicture,
            enrollments = enrollments,
            enrolledPrograms = enrolledPrograms,
            relationships = relationShips,
        )
    }

    private fun getEnrolledPrograms(enrollments: List<DomainEnrollment>): List<DomainProgram>? {
        val programUids =
            enrollments
                .mapNotNull { it.program }
                .distinct()
        if (programUids.isEmpty()) {
            return null
        }

        val programs =
            d2
                .programModule()
                .programs()
                .byUid()
                .`in`(programUids)
                .blockingGet()
        if (programs.isEmpty()) {
            return null
        }

        val programsByUid = programs.associateBy { it.uid() }
        val enrolledPrograms =
            enrollments.mapNotNull { enrollment ->
                val programUid = enrollment.program ?: return@mapNotNull null
                val program = programsByUid[programUid] ?: return@mapNotNull null
                DomainProgram(
                    uid = program.uid(),
                    displayName = program.displayName() ?: "",
                    style =
                        DomainObjectStyle(
                            icon = program.style()?.icon(),
                            color = program.style()?.color(),
                        ),
                )
            }
        return enrolledPrograms.ifEmpty { null }
    }

    private fun shouldDisplayOrgUnit(selectedProgram: String?): Boolean =
        if (selectedProgram != null) {
            d2
                .organisationUnitModule()
                .organisationUnits()
                .byProgramUids(listOf(selectedProgram))
                .blockingCount() > 1
        } else {
            false
        }

    private fun orgUnitName(orgUnit: String?): String? =
        orgUnit?.let {
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid(it)
                .blockingGet()
                ?.displayName()
        }

    private fun getRelationShips(
        teiUid: String,
        selectedProgram: String,
        teType: String,
    ): List<RelationshipModel> {
        val relationships =
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem
                    .builder()
                    .trackedEntityInstance(
                        RelationshipItemTrackedEntityInstance
                            .builder()
                            .trackedEntityInstance(teiUid)
                            .build(),
                    ).build(),
            )
        return relationships.mapNotNull { relationship ->
            relationship
                .from()
                ?.trackedEntityInstance()
                ?.let { trackedEntityInstanceFromRelationship ->
                    var relationshipTeiUid: String
                    var direction: RelationshipDirection
                    if (trackedEntityInstanceFromRelationship.trackedEntityInstance() == teiUid) {
                        relationshipTeiUid =
                            trackedEntityInstanceFromRelationship.trackedEntityInstance()
                        direction = RelationshipDirection.FROM
                    } else {
                        relationshipTeiUid =
                            relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()
                                ?: ""
                        direction = RelationshipDirection.TO
                    }

                    val fromTeiUid =
                        relationship.from()?.trackedEntityInstance()?.trackedEntityInstance()
                    val toTeiUid =
                        relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()

                    val fromTei =
                        d2
                            .trackedEntityModule()
                            .trackedEntityInstances()
                            .uid(fromTeiUid)
                            .blockingGet()
                    val toTei =
                        d2
                            .trackedEntityModule()
                            .trackedEntityInstances()
                            .uid(toTeiUid)
                            .blockingGet()
                    val fromAttributes: List<kotlin.Pair<String, String>> =
                        getTrackedEntityAttributesForRelationship(
                            fromTeiUid,
                            selectedProgram,
                            teType,
                        ).mapNotNull { trackedEntityAttributeValue ->
                            val attribute = trackedEntityAttributeValue.trackedEntityAttribute()
                            val value = trackedEntityAttributeValue.value()
                            if (attribute != null && value != null) {
                                Pair(attribute, value)
                            } else {
                                null
                            }
                        }
                    val toAttributes: List<Pair<String, String>> =
                        getTrackedEntityAttributesForRelationship(
                            toTeiUid,
                            selectedProgram,
                            teType,
                        ).mapNotNull { trackedEntityAttributeValue ->
                            val attribute = trackedEntityAttributeValue.trackedEntityAttribute()
                            val value = trackedEntityAttributeValue.value()
                            if (attribute != null && value != null) {
                                Pair(attribute, value)
                            } else {
                                null
                            }
                        }

                    val fromGeometry =
                        fromTei?.geometry()?.let { geometry ->
                            RelationshipGeometry(
                                featureType = geometry.type()?.name,
                                coordinates = geometry.coordinates(),
                            )
                        }
                    val toGeometry =
                        toTei?.geometry()?.let { geometry ->
                            RelationshipGeometry(
                                featureType = geometry.type()?.name,
                                coordinates = geometry.coordinates(),
                            )
                        }
                    RelationshipModel(
                        relationshipUid = relationship.uid()!!,
                        relationshipState = relationship.syncState()?.name ?: State.TO_POST.name,
                        fromGeometry = fromGeometry,
                        toGeometry = toGeometry,
                        direction = direction,
                        ownerUid = relationshipTeiUid,
                        ownerType = RelationshipOwnerType.TEI,
                        fromValues = fromAttributes,
                        toValues = toAttributes,
                        fromImage = null,
                        toImage = null,
                        fromDefaultImageResource = -1,
                        toDefaultImageResource = -1,
                        ownerStyleIcon = null,
                        ownerStyleColor = null,
                        canBeOpened = true,
                        toLastUpdated = null,
                        fromLastUpdated = null,
                        toDescription = null,
                        fromDescription = null,
                    )
                }
        }
    }

    private fun getTrackedEntityAttributesForRelationship(
        uid: String?,
        selectedProgram: String,
        teType: String,
    ): List<TrackedEntityAttributeValue> {
        val attributeValues =
            d2
                .trackedEntityModule()
                .trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(uid)
                .byTrackedEntityAttribute()
                .`in`(getProgramTrackedEntityAttributeUids(selectedProgram))
                .blockingGet()
        return attributeValues.ifEmpty {
            d2
                .trackedEntityModule()
                .trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(uid)
                .byTrackedEntityAttribute()
                .`in`(getTeTypeTrackedEntityAttributeUids(teType))
                .blockingGet()
        }
    }

    private fun getProgramTrackedEntityAttributeUids(selectedProgram: String): List<String> =
        d2
            .programModule()
            .programTrackedEntityAttributes()
            .byProgram()
            .eq(selectedProgram)
            .byDisplayInList()
            .isTrue
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
            .map { it.uid() }

    private fun getTeTypeTrackedEntityAttributeUids(teTypeUid: String): List<String> =
        d2
            .trackedEntityModule()
            .trackedEntityTypeAttributes()
            .byTrackedEntityTypeUid()
            .eq(teTypeUid)
            .byDisplayInList()
            .isTrue
            .blockingGet()
            .mapNotNull {
                it.trackedEntityAttribute()?.uid()
            }

    private fun getTeiEnrollments(teiUid: String): List<DomainEnrollment> {
        val enrollments =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(teiUid)
                .byDeleted()
                .eq(false)
                .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()
        return enrollments.map { it.toDomainEnrollment() }
    }

    /**
     * Returns the most recent overdue date from events in the given enrollments.
     * Returns null if there are no overdue events.
     *
     * This method checks:
     * 1. Events with OVERDUE status
     * 2. Events with SCHEDULE status that have passed their due date
     *
     * Uses kotlinx.datetime exclusively for date comparison (no java.util.Date operations)
     */
    private fun getOverdueDate(
        enrollments: List<DomainEnrollment>,
        selectedProgram: String?,
    ): Instant? {
        if (enrollments.isEmpty()) return null

        val enrollmentUids = enrollments.map { it.uid }

        // Query for OVERDUE events
        var overdueEventsQuery =
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .`in`(enrollmentUids)
                .byStatus()
                .eq(EventStatus.OVERDUE)

        // Query for SCHEDULE events that may be overdue
        var scheduledEventsQuery =
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .`in`(enrollmentUids)
                .byStatus()
                .eq(EventStatus.SCHEDULE)

        // Filter by program if specified
        if (selectedProgram != null) {
            overdueEventsQuery = overdueEventsQuery.byProgramUid().eq(selectedProgram)
            scheduledEventsQuery = scheduledEventsQuery.byProgramUid().eq(selectedProgram)
        }

        // Get events ordered by due date (most recent first)
        val overdueEvents =
            overdueEventsQuery
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()

        val scheduledEvents =
            scheduledEventsQuery
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()

        // Get today's date using kotlinx.datetime (non-deprecated way)
        val today = getTodayAsInstant()

        // Filter events that are actually overdue (due date is before today)
        val allOverdueEvents =
            buildList {
                // Add all OVERDUE events that have a due date before today
                overdueEvents.forEach { event ->
                    event.dueDate()?.let { dueDate ->
                        val dueDateLocal = dueDate.toKtxInstant()
                        // Event is overdue if due date is before today (not equal to today)
                        if (dueDateLocal < today) {
                            add(event)
                        }
                    }
                }

                // Add SCHEDULE events that have passed their due date
                scheduledEvents.forEach { event ->
                    event.dueDate()?.let { dueDate ->
                        val dueDateLocal = dueDate.toKtxInstant()
                        // Event is overdue if due date is before today (not equal to today)
                        if (dueDateLocal < today) {
                            add(event)
                        }
                    }
                }
            }

        // Return the most recent overdue date as Instant, or null if no overdue events
        return allOverdueEvents
            .mapNotNull { it.dueDate()?.toKtxInstant() }
            .maxOrNull()
    }

    private fun getDatabaseTei(item: TrackedEntitySearchItem): TrackedEntityInstance {
        val teiFromItem = toTrackedEntityInstance(item)

        return if (item.isOnline) {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(item.uid())
                .blockingGet() ?: teiFromItem
        } else {
            teiFromItem
        }
    }

    private fun getSelectedEnrollment(
        dbTei: TrackedEntityInstance?,
        selectedProgram: String?,
    ): DomainEnrollment? {
        var selectedEnrollment: DomainEnrollment? = null
        // SDK mapping from SearchTrackedEntityItem to TrackedEntityInstance, this might not be needed
        if (dbTei != null && dbTei.aggregatedSyncState() != State.RELATIONSHIP) {
            // if dbTei is not null  we will map the db tei instead of the online tei

            var enrollmentRepository =
                d2
                    .enrollmentModule()
                    .enrollments()
                    .byTrackedEntityInstance()
                    .eq(dbTei.uid())
            if (selectedProgram != null) {
                enrollmentRepository = enrollmentRepository.byProgram().eq(selectedProgram)
            }
            val enrollmentsInProgram =
                enrollmentRepository
                    .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
                    .blockingGet()
            if (selectedProgram != null && enrollmentsInProgram.isNotEmpty()) {
                selectedEnrollment =
                    enrollmentsInProgram
                        .firstOrNull { it.status() == EnrollmentStatus.ACTIVE }
                        ?.toDomainEnrollment() ?: enrollmentsInProgram.firstOrNull()?.toDomainEnrollment()
            }
        }
        return selectedEnrollment
    }
}
