package org.dhis2.data.sorting

import java.time.Instant
import java.util.Date
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchSortingValueSetterTest {

    lateinit var searchSortingValueSetter: SearchSortingValueSetter
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentUiDataHelper: EnrollmentUiDataHelper = mock()

    @Before
    fun setUp() {
        searchSortingValueSetter = SearchSortingValueSetter(
            d2,
            "-",
            "eventLabel",
            "enrollmentStatusLabel",
            "enrollmentDefaultLabel",
            "d/M/yyyy",
            enrollmentUiDataHelper
        )
    }

    @Test
    fun `Sorting by event date should return correct key value for online result`() {
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn emptyList()

        val result = searchSortingValueSetter.setSortingItem(
            onlineSearchTeiModel(),
            SortingItem(Filters.PERIOD, SortingStatus.ASC)
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
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn getEventLists()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.PERIOD, SortingStatus.ASC)
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
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn getScheduleEventLists()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.PERIOD, SortingStatus.ASC)
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
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn emptyList()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.PERIOD, SortingStatus.ASC)
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
            d2.organisationUnitModule().organisationUnits()
                .uid("enrollmentOrgUnit")
                .blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid("enrollmentOrgUnit")
            .displayName("EnrollmentOrgUnit")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.ORG_UNIT, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key and unknown value for enrollment`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("enrollmentOrgUnit")
                .blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid("enrollmentOrgUnit")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.ORG_UNIT, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key value for tei`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("teiOrgUnit")
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("teiOrgUnit")
                .blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid("teiOrgUnit")
            .displayName("teiOrgUnit")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModelWithoutEnrollment(),
            SortingItem(Filters.ORG_UNIT, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by org unit should return correct key and unknown value for tei`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("teiOrgUnit")
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("teiOrgUnit")
                .blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid("teiOrgUnit")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModelWithoutEnrollment(),
            SortingItem(Filters.ORG_UNIT, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this == null)
        }
    }

    @Test
    fun `Sorting by status should return correct key value`() {
        whenever(
            enrollmentUiDataHelper.getEnrollmentStatusClientName(EnrollmentStatus.ACTIVE)
        ) doReturn "clientStatus"

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.ENROLLMENT_STATUS, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentStatusLabel")
            assertTrue(this?.second == "clientStatus")
        }
    }

    @Test
    fun `Sorting by status should return correct key and unknown value`() {
        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModelWithoutEnrollment(),
            SortingItem(Filters.ENROLLMENT_STATUS, SortingStatus.ASC)
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
            d2.programModule().programs()
                .uid("programUid")
                .blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .enrollmentDateLabel("programEnrollmentDateLabel")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC)
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
            d2.programModule().programs()
                .uid("programUid")
        ) doReturn mock()
        whenever(
            d2.programModule().programs()
                .uid("programUid")
                .blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .build()

        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModel(),
            SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentDefaultLabel")
            assertTrue(this?.second == "1/1/2020")
        }
    }

    @Test
    fun `Sorting by enrollment date should return correct default key and unknown value`() {
        val result = searchSortingValueSetter.setSortingItem(
            searchTeiModelWithoutEnrollment(),
            SortingItem(Filters.ENROLLMENT_DATE, SortingStatus.ASC)
        )

        result.apply {
            assertTrue(this != null)
            assertTrue(this?.first == "enrollmentDefaultLabel")
            assertTrue(this?.second == "-")
        }
    }

    private fun getEventLists(): List<Event> {
        return arrayListOf(
            Event.builder()
                .uid("eventUid1")
                .status(EventStatus.ACTIVE)
                .eventDate(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .dueDate(Date.from(Instant.parse("2020-01-10T00:00:00.00Z")))
                .build()
        )
    }

    private fun getScheduleEventLists(): List<Event> {
        return arrayListOf(
            Event.builder()
                .uid("eventUid1")
                .status(EventStatus.OVERDUE)
                .dueDate(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .build()
        )
    }

    private fun searchTeiModel(): SearchTeiModel {
        return SearchTeiModel().apply {
            setCurrentEnrollment(
                Enrollment.builder()
                    .uid("enrollmentUid")
                    .organisationUnit("enrollmentOrgUnit")
                    .status(EnrollmentStatus.ACTIVE)
                    .program("programUid")
                    .enrollmentDate(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                    .build()
            )
            tei = TrackedEntityInstance.builder()
                .uid("teiUid")
                .organisationUnit("teiOrgUnit")
                .build()
        }
    }

    private fun onlineSearchTeiModel(): SearchTeiModel {
        return SearchTeiModel().apply {
            tei = TrackedEntityInstance.builder()
                .uid("teiUid")
                .organisationUnit("teiOrgUnit")
                .build()
        }
    }

    private fun searchTeiModelWithoutEnrollment(): SearchTeiModel {
        return SearchTeiModel().apply {
            tei = TrackedEntityInstance.builder()
                .uid("teiUid")
                .organisationUnit("teiOrgUnit")
                .build()
        }
    }
}
