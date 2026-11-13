package org.dhis2.usescases.teiDashboard.ui.mapper

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.tracker.dashboard.domain.model.EnrollmentAOC
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        val result =
            mapper.map(
                dashboardModel = model,
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        assertEquals(result.title, model.teiHeader)
        assertEquals(result.additionalInfo[0].value, model.trackedEntityAttributes[0].second.value())
        assertEquals(result.additionalInfo[1].value, model.trackedEntityAttributes[1].second.value())
        assertEquals(result.additionalInfo[3].value, orgUnit().displayName())
    }

    @Test
    fun shouldShowOwnedAndEnrolledOrgUnit() {
        val model = createFakeModel(otherOrgUnit())

        val result =
            mapper.map(
                dashboardModel = model,
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        assertEquals(result.additionalInfo[3].value, otherOrgUnit().displayName())
        assertEquals(result.additionalInfo[4].value, model.orgUnits.first().displayName())
    }

    @Test
    fun shouldShowEnrollmentAOCAfterOrgUnitInfoForNonDefaultCategoryCombo() {
        val enrollmentAOC = EnrollmentAOC(label = "TB case type", value = "Index, Global Fund")
        val model = createFakeModel(enrollmentAOC = enrollmentAOC)

        val result =
            mapper.map(
                dashboardModel = model,
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        val aocItem = result.additionalInfo[4]
        assertEquals("TB case type", aocItem.key)
        assertEquals("Index, Global Fund", aocItem.value)
        assertTrue(aocItem.isConstantItem)
    }

    @Test
    fun shouldShowEnrollmentAOCAfterEnrolledInWhenOwnerDiffers() {
        val enrollmentAOC = EnrollmentAOC(label = "TB case type", value = "Contact")
        val model = createFakeModel(ownerOrgUnit = otherOrgUnit(), enrollmentAOC = enrollmentAOC)

        val result =
            mapper.map(
                dashboardModel = model,
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        assertEquals(model.orgUnits.first().displayName(), result.additionalInfo[4].value)
        assertEquals("TB case type", result.additionalInfo[5].key)
        assertEquals("Contact", result.additionalInfo[5].value)
    }

    @Test
    fun shouldNotShowEnrollmentAOCForDefaultCategoryCombo() {
        val model = createFakeModel(enrollmentAOC = null)

        val result =
            mapper.map(
                dashboardModel = model,
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        val withAOC =
            mapper.map(
                dashboardModel =
                    createFakeModel(
                        enrollmentAOC =
                            EnrollmentAOC(
                                "TB case type",
                                "Index",
                            ),
                    ),
                phoneCallback = {},
                emailCallback = {},
                programsCallback = {},
                onImageClick = {},
            )

        assertEquals(withAOC.additionalInfo.size - 1, result.additionalInfo.size)
        assertTrue(result.additionalInfo.none { it.key == "TB case type" })
    }

    private fun createFakeModel(
        ownerOrgUnit: OrganisationUnit = orgUnit(),
        enrollmentAOC: EnrollmentAOC? = null,
    ): DashboardEnrollmentModel {
        val attributeValues =
            listOf(
                Pair(getTEA("uid1", "First Name"), getTEAValue("Jonah")),
                Pair(getTEA("uid2", "Last Name"), getTEAValue("Hill")),
            )

        val model =
            DashboardEnrollmentModel(
                setEnrollment(),
                emptyList<ProgramStage>(),
                setTei(),
                attributeValues,
                emptyList<TrackedEntityAttributeValue>(),
                setPrograms(),
                listOf(orgUnit()),
                "header",
                "avatarFilepath",
                ownerOrgUnit,
                emptyList(),
                "Marked for follow-up",
                enrollmentAOC,
            )

        return model
    }

    private fun getTEAValue(value: String): TrackedEntityAttributeValue =
        mock {
            on { value() } doReturn value
        }

    private fun getTEA(
        uid: String,
        value: String,
    ) = TrackedEntityAttribute
        .builder()
        .uid(uid)
        .displayFormName(value)
        .build()

    private fun setTei() =
        TrackedEntityInstance
            .builder()
            .uid("TEIUid")
            .organisationUnit("OrgUnit")
            .aggregatedSyncState(State.SYNCED)
            .build()

    private fun setEnrollment() =
        Enrollment
            .builder()
            .uid("EnrollmentUid")
            .status(EnrollmentStatus.COMPLETED)
            .program("Program1Uid")
            .organisationUnit("orgUnitUid")
            .attributeOptionCombo("attributeOptionComboUid")
            .build()

    private fun setPrograms() =
        listOf<Pair<Program, MetadataIconData>>(
            Pair(
                Program
                    .builder()
                    .uid("Program1Uid")
                    .displayName("Program 1")
                    .categoryCombo(ObjectWithUid.create("categoryComboUid"))
                    .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
                    .build(),
                MetadataIconData.defaultIcon(),
            ),
            Pair(
                Program
                    .builder()
                    .uid("Program2Uid")
                    .displayName("Program 2")
                    .categoryCombo(ObjectWithUid.create("categoryComboUid"))
                    .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
                    .build(),
                MetadataIconData.defaultIcon(),
            ),
        )

    private fun orgUnit() =
        OrganisationUnit
            .builder()
            .uid("orgUnitUid")
            .displayName("orgUnitName")
            .build()

    private fun otherOrgUnit() =
        OrganisationUnit
            .builder()
            .uid("otherOrgUnitUid")
            .displayName("otherOrgUnitName")
            .build()
}
