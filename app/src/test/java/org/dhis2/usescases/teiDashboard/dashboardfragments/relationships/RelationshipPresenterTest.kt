package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.test.StandardTestDispatcher
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.ui.AvatarProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RelationshipPresenterTest {
    @JvmField
    @Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    lateinit var presenter: RelationshipPresenter
    private val view: RelationshipView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val relationshipMapsRepository: RelationshipMapsRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection = mock()
    private val mapStyleConfiguration: MapStyleConfiguration = mock()
    private val relationshipsRepository: RelationshipsRepository = mock()
    private val avatarProvider: AvatarProvider = mock()
    private val dateLabelProvider: DateLabelProvider = mock()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { ui() } doReturn StandardTestDispatcher()
        }

    @Before
    fun setup() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .withTrackedEntityAttributeValues()
                .uid("teiUid")
                .blockingGet()
                ?.trackedEntityType(),
        ) doReturn "teiType"
        whenever(
            d2
                .eventModule()
                .events()
                .uid("eventUid")
                .blockingGet()
                ?.programStage(),
        ) doReturn "programStageUid"
        presenter =
            RelationshipPresenter(
                view,
                d2,
                "teiUid",
                null,
                relationshipMapsRepository,
                analyticsHelper,
                mapRelationshipsToFeatureCollection,
                mapStyleConfiguration,
                relationshipsRepository,
                avatarProvider,
                dateLabelProvider,
                dispatcherProvider,
            )
    }

    @Test
    fun `If user has permission should create a new relationship`() {
        val relationshipTypeUid = "relationshipTypeUid"
        val teiTypeToAdd = "teiTypeToAdd"

        whenever(
            relationshipsRepository.hasWritePermission(relationshipTypeUid),
        ) doReturn true

        presenter.goToAddRelationship(relationshipTypeUid, teiTypeToAdd)

        verify(view, times(1)).goToAddRelationship("teiUid", teiTypeToAdd)
        verify(view, times(0)).showPermissionError()
    }

    @Test
    fun `If user don't have permission should show an error`() {
        val relationshipTypeUid = "relationshipTypeUid"
        whenever(
            relationshipsRepository.hasWritePermission(relationshipTypeUid),
        ) doReturn false

        presenter.goToAddRelationship(relationshipTypeUid, "teiTypeToAdd")

        verify(view, times(1)).showPermissionError()
    }

    @Test
    fun `Should open dashboard`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid("teiUid")
                .blockingGet(),
        ) doReturn getMockedTei(State.SYNCED)
        whenever(
            d2.enrollmentModule().enrollments(),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance(),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq("teiUid"),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq("teiUid")
                .blockingGet(),
        ) doReturn getMockedEntollmentList()
        presenter.openDashboard("teiUid")

        verify(view).openDashboardFor("teiUid")
    }

    @Test
    fun `Should show enrollment error`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid("teiUid")
                .blockingGet(),
        ) doReturn getMockedTei(State.SYNCED)
        whenever(
            d2.enrollmentModule().enrollments(),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance(),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq("teiUid"),
        ) doReturn mock()
        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq("teiUid")
                .blockingGet(),
        ) doReturn emptyList()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityTypes()
                .uid("teiType")
                .blockingGet(),
        ) doReturn getMockedTeiType()
        presenter.openDashboard("teiUid")

        verify(view).showTeiWithoutEnrollmentError(getMockedTeiType().displayName()!!)
    }

    @Test
    fun `Should show relationship error`() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid("teiUid")
                .blockingGet(),
        ) doReturn getMockedTei(State.RELATIONSHIP)
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityTypes()
                .uid("teiType")
                .blockingGet(),
        ) doReturn getMockedTeiType()
        presenter.openDashboard("teiUid")

        verify(view).showRelationshipNotFoundError(getMockedTeiType().displayName()!!)
    }

    private fun getMockedRelationship(): Relationship =
        Relationship
            .builder()
            .uid("relationshipUid")
            .build()

    private fun getMockedRelationshipType(bidirectional: Boolean): RelationshipType =
        RelationshipType
            .builder()
            .uid("relationshipType")
            .bidirectional(bidirectional)
            .toConstraint(
                RelationshipConstraint
                    .builder()
                    .trackedEntityType(ObjectWithUid.create("teiType"))
                    .build(),
            ).build()

    private fun getMockedTei(state: State): TrackedEntityInstance =
        TrackedEntityInstance
            .builder()
            .uid("teiUid")
            .aggregatedSyncState(state)
            .build()

    private fun getMockedEntollmentList(): List<Enrollment> =
        arrayListOf(
            Enrollment
                .builder()
                .uid("enrollmentUid")
                .build(),
        )

    private fun getMockedTeiType(): TrackedEntityType =
        TrackedEntityType
            .builder()
            .uid("teiType")
            .displayName("name")
            .build()
}
