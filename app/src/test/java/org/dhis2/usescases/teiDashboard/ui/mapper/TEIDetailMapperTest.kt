package org.dhis2.usescases.teiDashboard.ui.mapper

import org.dhis2.R
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TEIDetailMapperTest {

    private val resourceManager: ResourceManager = mock()
    private lateinit var mapper: TeiDashboardCardMapper

    @Before
    fun setUp() {
        whenever(resourceManager.getString(R.string.show_more)) doReturn "Show more"
        whenever(resourceManager.getString(R.string.show_less)) doReturn "Show less"

        mapper = TeiDashboardCardMapper(resourceManager)
    }

    @Test
    fun shouldReturnCardFull() {
        val model = createFakeModel()

        val result = mapper.map(
            dashboardModel = model,
            phoneCallback = {},
            emailCallback = {},
            programsCallback = {},
            onImageClick = {},

        )

        assertEquals(result.title, model.teiHeader)
        assertEquals(result.additionalInfo[0].value, model.attributes[0]?.val1()?.value())
        assertEquals(result.additionalInfo[1].value, model.attributes[1]?.val1()?.value())
    }

    private fun createFakeModel(): DashboardProgramModel {
        val attributeValues = listOf(
            Pair.create(getTEA("uid1", "First Name"), getTEAValue("Jonah")),
            Pair.create(getTEA("uid2", "Last Name"), getTEAValue("Hill")),
        )

        val model = DashboardProgramModel(
            setTei(),
            setEnrollment(),
            emptyList<ProgramStage>(),
            emptyList<Event>(),
            attributeValues,
            emptyList<TrackedEntityAttributeValue>(),
            emptyList<OrganisationUnit>(),
            setPrograms(),
        )

        model.teiHeader = "header"
        model.avatarPath = "avatarFilepath"

        return model
    }

    private fun getTEAValue(value: String) =
        TrackedEntityAttributeValue.builder()
            .value(value)
            .build()

    private fun getTEA(uid: String, value: String) =
        TrackedEntityAttribute.builder()
            .uid(uid)
            .displayFormName(value)
            .build()

    private fun setTei() = TrackedEntityInstance.builder()
        .uid("TEIUid")
        .organisationUnit("OrgUnit")
        .aggregatedSyncState(State.SYNCED)
        .build()

    private fun setEnrollment() = Enrollment.builder()
        .uid("EnrollmentUid")
        .status(EnrollmentStatus.COMPLETED)
        .program("Program1Uid")
        .build()

    private fun setPrograms() = listOf<Program>(
        Program.builder()
            .uid("Program1Uid")
            .displayName("Program 1")
            .build(),
        Program.builder()
            .uid("Program2Uid")
            .displayName("Program 2")
            .build(),
    )
}
