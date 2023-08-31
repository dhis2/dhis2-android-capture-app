package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_RELATIONSHIP
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RelationshipPresenterTest {

    lateinit var presenter: RelationshipPresenter
    private val view: RelationshipView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val repository: RelationshipRepository = mock()
    private val schedulerProvider: SchedulerProvider = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val mapRelationshipToRelationshipMapModel = MapRelationshipToRelationshipMapModel()
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection = mock()
    private val relationshipConstrain: RelationshipConstraint = mock()
    private val relationshipType: RelationshipType = mock {
        on { fromConstraint() } doReturn relationshipConstrain
    }
    private val mapStyleConfiguration: MapStyleConfiguration = mock()

    @Before
    fun setup() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .withTrackedEntityAttributeValues()
                .uid("teiUid")
                .blockingGet().trackedEntityType()
        ) doReturn "teiType"
        whenever(
            d2.eventModule().events()
                .uid("eventUid").blockingGet().programStage()
        ) doReturn "programStageUid"
        presenter = RelationshipPresenter(
            view,
            d2,
            "programUid",
            "teiUid",
            null,
            repository,
            schedulerProvider,
            analyticsHelper,
            mapRelationshipToRelationshipMapModel,
            mapRelationshipsToFeatureCollection,
            mapStyleConfiguration
        )
    }

    @Test
    fun `Should set relationships and init fab`() {
        whenever(repository.relationships()) doReturn Single.just(arrayListOf())
        whenever(repository.relationshipTypes()) doReturn Single.just(
            arrayListOf()
        )
        whenever(repository.getTeiTypeDefaultRes(any())) doReturn -1

        presenter.init()

        verify(view).setRelationships(any())
        verify(view).initFab(any())
    }

    @Test
    fun `If user has permission should create a new relationship`() {
        whenever(
            d2.relationshipModule().relationshipService().hasAccessPermission(relationshipType)
        ) doReturn true

        presenter.goToAddRelationship("teiType", relationshipType)

        verify(view, times(1)).goToAddRelationship("teiUid", "teiType")
        verify(view, times(0)).showPermissionError()
    }

    @Test
    fun `If user don't have permission should show an error`() {
        whenever(
            d2.relationshipModule().relationshipService().hasAccessPermission(relationshipType)
        ) doReturn false

        presenter.goToAddRelationship("teiType", relationshipType)

        verify(view, times(1)).showPermissionError()
    }

    @Test
    fun `Should delete relationship`() {
        presenter.deleteRelationship(getMockedRelationship().uid()!!)
        verify(analyticsHelper).setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP)
    }

    @Test
    fun `Should create a relationship`() {
        whenever(
            d2.relationshipModule().relationshipTypes().withConstraints().uid("relationshipTypeUid")
                .blockingGet()
        ) doReturn getMockedRelationshipType(true)
        presenter.addRelationship("selectedTei", "relationshipTypeUid")

        verify(view, times(0)).displayMessage(any())
    }

    @Test
    fun `Should open dashboard`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid("teiUid").blockingGet()
        ) doReturn getMockedTei(State.SYNCED)
        whenever(
            d2.enrollmentModule().enrollments()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq("teiUid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq("teiUid").blockingGet()
        ) doReturn getMockedEntollmentList()
        presenter.openDashboard("teiUid")

        verify(view).openDashboardFor("teiUid")
    }

    @Test
    fun `Should show enrollment error`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid("teiUid").blockingGet()
        ) doReturn getMockedTei(State.SYNCED)
        whenever(
            d2.enrollmentModule().enrollments()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq("teiUid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq("teiUid").blockingGet()
        ) doReturn emptyList()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("teiType").blockingGet()
        ) doReturn getMockedTeiType()
        presenter.openDashboard("teiUid")

        verify(view).showTeiWithoutEnrollmentError(getMockedTeiType().displayName()!!)
    }

    @Test
    fun `Should show relationship error`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid("teiUid").blockingGet()
        ) doReturn getMockedTei(State.RELATIONSHIP)
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid("teiType").blockingGet()
        ) doReturn getMockedTeiType()
        presenter.openDashboard("teiUid")

        verify(view).showRelationshipNotFoundError(getMockedTeiType().displayName()!!)
    }

    private fun getMockedProgram(access: Boolean): Program {
        return Program.builder()
            .uid("programUid")
            .access(
                Access.create(
                    access,
                    access,
                    DataAccess.create(
                        access,
                        access
                    )
                )
            )
            .build()
    }

    private fun getMockedRelationship(): Relationship {
        return Relationship.builder()
            .uid("relationshipUid")
            .build()
    }

    private fun getMockedRelationshipType(bidirectional: Boolean): RelationshipType {
        return RelationshipType.builder()
            .uid("relationshipType")
            .bidirectional(bidirectional)
            .toConstraint(
                RelationshipConstraint.builder()
                    .trackedEntityType(ObjectWithUid.create("teiType"))
                    .build()
            )
            .build()
    }

    private fun getMockedTei(state: State): TrackedEntityInstance {
        return TrackedEntityInstance.builder()
            .uid("teiUid")
            .state(state)
            .build()
    }

    private fun getMockedEntollmentList(): List<Enrollment> {
        return arrayListOf(
            Enrollment.builder()
                .uid("enrollmentUid")
                .build()
        )
    }

    private fun getMockedTeiType(): TrackedEntityType {
        return TrackedEntityType.builder()
            .uid("teiType")
            .displayName("name")
            .build()
    }
}
