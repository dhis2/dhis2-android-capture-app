package org.dhis2.utils.filters.workingLists

import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.DateFilterPeriod
import org.hisp.dhis.android.core.common.DatePeriodType
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.event.EventFilter
import org.hisp.dhis.android.core.event.EventQueryCriteria
import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventFilterToWorkingListItemMapperTest {

    lateinit var mapper: EventFilterToWorkingListItemMapper

    @Before
    fun setUp() {
        mapper = EventFilterToWorkingListItemMapper(
            "defaultLabel",
        )
    }

    @Test
    fun `Should map eventFilter to working list item`() {
        val result = mapper.map(
            EventFilter.builder()
                .uid("filterUid")
                .displayName("filterName")
                .eventQueryCriteria(
                    EventQueryCriteria.builder()
                        .assignedUserMode(AssignedUserMode.CURRENT)
                        .eventDate(
                            DateFilterPeriod.builder()
                                .type(DatePeriodType.RELATIVE)
                                .period(RelativePeriod.LAST_3_DAYS)
                                .build(),
                        )
                        .eventStatus(EventStatus.ACTIVE)
                        .organisationUnit("orgUnitUid")
                        .build(),
                )
                .build(),
        )

        assertTrue(result.uid == "filterUid")
        assertTrue(result.label == "filterName")
    }

    @Test
    fun `Should set default values`() {
        val result = mapper.map(
            EventFilter.builder()
                .uid("filterUid")
                .build(),
        )

        assertTrue(result.uid == "filterUid")
        assertTrue(result.label == "defaultLabel")
    }
}
