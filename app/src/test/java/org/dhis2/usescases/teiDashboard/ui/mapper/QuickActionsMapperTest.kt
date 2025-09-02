package org.dhis2.usescases.teiDashboard.ui.mapper

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.ui.model.QuickActionType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class QuickActionsMapperTest {
    private val resourceManager: ResourceManager = mock()
    private lateinit var mapper: QuickActionsMapper

    @Before
    fun setUp() {
        mapper = QuickActionsMapper("programUid", resourceManager)
        whenever(resourceManager.getString(R.string.more_enrollments)) doReturn "More enrollments"
        whenever(resourceManager.getString(R.string.mark_follow_up)) doReturn "Mark follow up"
        whenever(resourceManager.getString(R.string.transfer)) doReturn "Transfer"
        whenever(
            resourceManager.formatWithEnrollmentLabel(mapper.programUid, R.string.complete_enrollment_label, 1),
        ) doReturn "Complete enrollment"
        whenever(
            resourceManager.formatWithEnrollmentLabel(mapper.programUid, R.string.reopen_enrollment_label, 1),
        ) doReturn "Re-open enrollment"
        whenever(
            resourceManager.formatWithEnrollmentLabel(mapper.programUid, R.string.deactivate_enrollment_label, 1),
        ) doReturn "Deactivate enrollment"
    }

    @Test
    fun shouldMapAllItem() {
        val dashboardEnrollmentModel =
            fakeEnrollmentModel(
                status = EnrollmentStatus.ACTIVE,
                quickActions =
                    listOf(
                        QuickActionType.MARK_FOLLOW_UP.name,
                        QuickActionType.TRANSFER.name,
                        QuickActionType.COMPLETE_ENROLLMENT.name,
                        QuickActionType.CANCEL_ENROLLMENT.name,
                        QuickActionType.MORE_ENROLLMENTS.name,
                    ),
            )
        val quickActions = mapper.map(dashboardEnrollmentModel, true) {}
        assert(quickActions.size == 5)
    }

    @Test
    fun shouldMapMoreEnrollments() {
        val dashboardEnrollmentModel =
            fakeEnrollmentModel(
                status = EnrollmentStatus.ACTIVE,
                quickActions = listOf(QuickActionType.MORE_ENROLLMENTS.name),
            )
        val quickActions = mapper.map(dashboardEnrollmentModel, true) {}
        assert(quickActions.first().label == "More enrollments")
    }

    @Test
    fun shouldNotMapMarkFollowUpWhenEnrollmentFollowUpIsTrue() {
        val dashboardEnrollmentModel =
            fakeEnrollmentModel(
                status = EnrollmentStatus.ACTIVE,
                followup = true,
                quickActions = listOf(QuickActionType.MARK_FOLLOW_UP.name),
            )
        val quickActions = mapper.map(dashboardEnrollmentModel, true) {}
        assert(quickActions.isEmpty())
    }

    @Test
    fun shouldShouldShowReopenWhenEnrollmentIsCompleted() {
        val dashboardEnrollmentModel =
            fakeEnrollmentModel(
                status = EnrollmentStatus.COMPLETED,
                quickActions =
                    listOf(
                        QuickActionType.COMPLETE_ENROLLMENT.name,
                        QuickActionType.CANCEL_ENROLLMENT.name,
                    ),
            )
        val quickActions = mapper.map(dashboardEnrollmentModel, true) {}
        assert(quickActions.map { it.label } == listOf("Re-open enrollment", "Deactivate enrollment"))
    }

    @Test
    fun shouldNotMapTransferActionIfNotAvailable() {
        val dashboardEnrollmentModel =
            fakeEnrollmentModel(
                status = EnrollmentStatus.COMPLETED,
                quickActions =
                    listOf(
                        QuickActionType.TRANSFER.name,
                        QuickActionType.CANCEL_ENROLLMENT.name,
                    ),
            )
        val quickActions = mapper.map(dashboardEnrollmentModel, false) {}
        assert(quickActions.map { it.label } == listOf("Deactivate enrollment"))
    }

    private fun fakeEnrollmentModel(
        status: EnrollmentStatus,
        followup: Boolean = false,
        quickActions: List<String> = emptyList(),
    ): DashboardEnrollmentModel {
        val enrollment =
            Enrollment
                .builder()
                .uid("EnrollmentUid")
                .status(status)
                .followUp(followup)
                .program("Program1Uid")
                .build()
        val tei =
            TrackedEntityInstance
                .builder()
                .uid("TEIUid")
                .organisationUnit("OrgUnit")
                .build()
        val model =
            DashboardEnrollmentModel(
                enrollment,
                emptyList(),
                tei,
                listOf(),
                emptyList(),
                emptyList(),
                emptyList(),
                null,
                null,
                null,
                quickActions,
            )
        return model
    }
}
