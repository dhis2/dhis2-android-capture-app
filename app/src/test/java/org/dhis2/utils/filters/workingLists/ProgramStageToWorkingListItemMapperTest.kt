package org.dhis2.utils.filters.workingLists

import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageQueryCriteria
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingList
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgramStageToWorkingListItemMapperTest {

    private lateinit var mapper: ProgramStageToWorkingListItemMapper

    @Before
    fun setUp() {
        mapper = ProgramStageToWorkingListItemMapper("defaultLabel")
    }

    @Test
    fun `Should map program stage filter to working list item`() {
        val result = mapper.map(
            ProgramStageWorkingList.builder()
                .uid("filterUid")
                .displayName("filterName")
                .program(ObjectWithUid.create("programUid"))
                .programStage(ObjectWithUid.create("stageUid"))
                .programStageQueryCriteria(
                    ProgramStageQueryCriteria.builder()
                        .assignedUserMode(AssignedUserMode.CURRENT)
                        .build(),
                )
                .build(),
        )

        assertTrue(result.uid == "filterUid")
        assertTrue(result.label == "filterName")
    }

    @Test
    fun `Should set program stage filter default values`() {
        val result = mapper.map(
            ProgramStageWorkingList.builder()
                .uid("filterUid")
                .program(ObjectWithUid.create("programUid"))
                .programStage(ObjectWithUid.create("stageUid"))
                .build(),
        )

        assertTrue(result.uid == "filterUid")
        assertTrue(result.label == "defaultLabel")
    }
}
