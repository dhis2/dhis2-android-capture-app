package org.dhis2.data.sorting

import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainObjectStyle
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import kotlin.time.Instant as KtlInstant
import java.time.Instant

class SearchSortingValueSetterTest {
    lateinit var searchSortingValueSetter: SearchSortingValueSetter
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentUiDataHelper: EnrollmentUiDataHelper = mock()
    private val enrollmentUid = "enrollmentUid"
    private val programUid = "programUid"
    private val enrollmentOrgUnit = "enrollmentOrgUnit"
    private val teiUid = "teiUid"
    private val selectedEnrollment =      DomainEnrollment(
        uid = enrollmentUid,
        orgUnit = enrollmentOrgUnit,
        program = programUid,
        enrollmentDate = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
        incidentDate = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
        completedDate = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
        followUp = false,
        status = EnrollmentStatus.ACTIVE,
        trackedEntityInstance = teiUid,
    )
    private val enrollments = listOf(
           selectedEnrollment
    )
    @Before
    fun setUp() {
        searchSortingValueSetter =
            SearchSortingValueSetter(
                d2,
                "-",
                "eventLabel",
                "enrollmentStatusLabel",
                "enrollmentDefaultLabel",
                "d/M/yyyy",
                enrollmentUiDataHelper,
            )
    }

    @Test
    fun `Sorting by event date should return correct key value for online result`() {
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid"),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn emptyList()

        val result =
            searchSortingValueSetter.setSortingItem(
                onlineSearchTeiModel(),
                SortingItem(Filters.PERIOD, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "eventLabel")
            assertTrue(this?.second == "-")
        }
    }

    @Test
    fun `Sorting by event date should return correct key value`() {
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid"),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn getEventLists()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.PERIOD, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "eventLabel")
            assertTrue(this?.second == "1/1/2020")
        }
    }

    @Test
    fun `Sorting by event date should return correct key value with schedule event`() {
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid"),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn getScheduleEventLists()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.PERIOD, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "eventLabel")
            assertTrue(this?.second == "1/1/2020")
        }
    }

    @Test
    fun `Sorting by event date should return unknown value`() {
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid"),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq("enrollmentUid")
                .byDeleted()
                .isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn emptyList()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.PERIOD, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "eventLabel")
            assertTrue(this?.second == "-")
        }
    }

    @Test
    fun `Sorting by org unit should return correct key value for enrollment`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("enrollmentOrgUnit")
                .blockingGet(),
        ) doReturn
            OrganisationUnit
                .builder()
                .uid("enrollmentOrgUnit")
                .displayName("EnrollmentOrgUnit")
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.ORG_UNIT, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key and unknown value for enrollment`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("enrollmentOrgUnit")
                .blockingGet(),
        ) doReturn
            OrganisationUnit
                .builder()
                .uid("enrollmentOrgUnit")
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.ORG_UNIT, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key value for tei`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("teiOrgUnit"),
        ) doReturn mock()
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("teiOrgUnit")
                .blockingGet(),
        ) doReturn
            OrganisationUnit
                .builder()
                .uid("teiOrgUnit")
                .displayName("teiOrgUnit")
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                searchTeiModelWithoutEnrollment(),
                SortingItem(Filters.ORG_UNIT, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key and unknown value for tei`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("teiOrgUnit"),
        ) doReturn mock()
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("teiOrgUnit")
                .blockingGet(),
        ) doReturn
            OrganisationUnit
                .builder()
                .uid("teiOrgUnit")
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                searchTeiModelWithoutEnrollment(),
                SortingItem(Filters.ORG_UNIT, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by status should return correct key value`() {
        whenever(
            enrollmentUiDataHelper.getEnrollmentStatusClientName(EnrollmentStatus.ACTIVE),
        ) doReturn "clientStatus"

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.ENROLLMENT_STATUS, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentStatusLabel")
            assertTrue(this?.second == "clientStatus")
        }
    }

    @Test
    fun `Sorting by status should return correct key and unknown value`() {
        val result =
            searchSortingValueSetter.setSortingItem(
                searchTeiModelWithoutEnrollment(),
                SortingItem(Filters.ENROLLMENT_STATUS, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentStatusLabel")
            assertTrue(this?.second == "-")
        }
    }

    @Test
    fun `Sorting by enrollment date should return correct program key and value`() {
        whenever(
            d2
                .programModule()
                .programs()
                .uid("programUid")
                .blockingGet(),
        ) doReturn
            Program
                .builder()
                .uid("programUid")
                .categoryCombo(ObjectWithUid.create("categoryComboUid"))
                .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
                .enrollmentDateLabel("programEnrollmentDateLabel")
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "programEnrollmentDateLabel")
            assertTrue(this?.second == "1/1/2020")
        }
    }

    @Test
    fun `Sorting by enrollment date should return default program key and value`() {
        whenever(
            d2
                .programModule()
                .programs()
                .uid("programUid"),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programs()
                .uid("programUid")
                .blockingGet(),
        ) doReturn
            Program
                .builder()
                .uid("programUid")
                .categoryCombo(ObjectWithUid.create("categoryComboUid"))
                .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
                .build()

        val result =
            searchSortingValueSetter.setSortingItem(
                generateSearchTeiModel(),
                SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentDefaultLabel")
            assertTrue(this?.second == "1/1/2020")
        }
    }

    @Test
    fun `Sorting by enrollment date should return correct default key and unknown value`() {
        val result =
            searchSortingValueSetter.setSortingItem(
                searchTeiModelWithoutEnrollment(),
                SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC),
            )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentDefaultLabel")
            assertTrue(this?.second == "-")
        }
    }

    private fun getEventLists(): List<Event> =
        arrayListOf(
            Event
                .builder()
                .uid("eventUid1")
                .status(EventStatus.ACTIVE)
                .eventDate(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .dueDate(Date.from(Instant.parse("2020-01-10T00:00:00.00Z")))
                .build(),
        )

    private fun getScheduleEventLists(): List<Event> =
        arrayListOf(
            Event
                .builder()
                .uid("eventUid1")
                .status(EventStatus.OVERDUE)
                .dueDate(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .build(),
        )

    private fun generateSearchTeiModel(): SearchTeiModel {
        val tei = TrackedEntitySearchItemResult(
            uid = "teiUid",
            created = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdated = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
            createdAtClient = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdatedAtClient = KtlInstant.parse("2020-01-01T00:00:00.00Z"),
            ownerOrgUnit = "ownerOrgUnit",
            enrollmentOrgUnit = "enrollmentOrgUnit",
            shouldDisplayOrgUnit = true,
            geometry = null,
            syncState = SyncState.SYNCED,
            aggregatedSyncState = SyncState.SYNCED,
            deleted = false,
            isOnline = true,
            teTypeName = "teTypeName",
            type = TrackedEntityTypeDomain(
                trackedEntityTypeAttributeDomains = listOf(TrackedEntityTypeAttributeDomain(
                     trackedEntityTypeUid=  "trackedEntityTypeUid",
                     trackedEntityAttributeUid = "trackedEntityAttributeUid",
                 displayInList= true,
                       mandatory = false,
                 searchable = true,
                 sortOrder = 1,
                )),
                featureType = GeometryFeatureType.POINT,
            ),
            header = "teiHeader",
            overDueDate = null,
            selectedEnrollment = selectedEnrollment,
            profilePicture = null,
            enrolledPrograms = listOf(DomainProgram(
                uid = "programUid",
                displayName = "programDisplayName",
                style = DomainObjectStyle(
                    icon = "iconUid",
                    color = "colorUid"
                )
            )),
            enrollments = enrollments,
            relationships = null,
            defaultTypeIcon = null,
            attributeValues = emptyList(),
        )
        val searchTeiModel = SearchTeiModel()
        searchTeiModel.tei = tei
        return searchTeiModel
    }

    private fun onlineSearchTeiModel(): SearchTeiModel  {
        val searchTeiModel = generateSearchTeiModel()
        searchTeiModel.tei = searchTeiModel.tei?.copy(isOnline = true)
        return searchTeiModel
    }


    private fun searchTeiModelWithoutEnrollment(): SearchTeiModel {
        val searchTeiModel = generateSearchTeiModel()
        searchTeiModel.tei = searchTeiModel.tei?.copy(selectedEnrollment = null, enrollments = emptyList())
        return searchTeiModel
    }

}
