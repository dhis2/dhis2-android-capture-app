package org.dhis2.utils.filters.workingLists

import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.EntityQueryCriteria
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceEventFilter
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceFilter
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeiFilterToWorkingListItemMapperTest {
    private lateinit var mapper: TeiFilterToWorkingListItemMapper

    @Before
    fun setUp() {
        mapper = TeiFilterToWorkingListItemMapper("defaultLabel")
    }

    @Test
    fun `Should map filter to working list item`() {
        val result = mapper.map(
            TrackedEntityInstanceFilter.builder()
                .uid("uid")
                .displayName("name")
                .entityQueryCriteria(
                    EntityQueryCriteria.builder().enrollmentStatus(EnrollmentStatus.ACTIVE).build(),
                )
                .eventFilters(
                    listOf(
                        TrackedEntityInstanceEventFilter.builder()
                            .programStage("stage")
                            .assignedUserMode(AssignedUserMode.CURRENT)
                            .build(),
                    ),
                )
                .build(),
        )

        assertTrue(result.uid == "uid")
        assertTrue(result.label == "name")
    }

    @Test
    fun `Should set default values`() {
        val result = mapper.map(
            TrackedEntityInstanceFilter.builder()
                .uid("uid")
                .build(),
        )

        assertTrue(result.uid == "uid")
        assertTrue(result.label == "defaultLabel")
    }
}
