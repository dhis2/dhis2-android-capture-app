package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

class RelationshipPresenterTest {
    private val dashboardRepository: DashboardRepositoryImpl = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: RelationshipView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private lateinit var presenter: RelationshipPresenterImpl

    @Before
    fun setUp() {
        mockTrackedEntityInstanceTypeModule("tei_uid")
        presenter = RelationshipPresenterImpl(
            d2, "program_uid",
            "tei_uid", dashboardRepository, schedulers, view
        )
    }

    @Test
    fun `Should build a list of RelationshipViewModel with FROM type`() {
        mockRelationTrackedEntityTA()
        whenever(
            d2.relationshipModule().relationships()
                .getByItem(relationShipItem("tei_uid"))
        ) doReturn listRelationshipItemsFrom()

        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .withTrackedEntityAttributeValues()
                .uid("tei_from_uid").blockingGet()
        ) doReturn trackedEntityInstance("tei_from_uid")

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_from_uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_from_uid")
                .byTrackedEntityAttribute()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_from_uid")
                .byTrackedEntityAttribute().`in`(listOf("tea_uid"))
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_from_uid")
                .byTrackedEntityAttribute().`in`(listOf("tea_uid"))
                .blockingGet()
        ) doReturn trackedEntityAttributeValue()

        presenter.updateRelationships()

        verify(view).setRelationships(relationshipViewModelsFrom())
    }

    @Test
    fun `Should build a list of RelationshipViewModel with TO type`() {
        // Necessary set presenter again to change teiUid and test TO case
        mockTrackedEntityInstanceTypeModule("tei_from_uid")
        presenter = RelationshipPresenterImpl(
            d2, "program_uid",
            "tei_from_uid", dashboardRepository, schedulers, view
        )
        mockRelationTrackedEntityTA()
        whenever(
            d2.relationshipModule().relationships()
                .getByItem(relationShipItem("tei_from_uid"))
        ) doReturn listRelationshipItemsTo()

        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .withTrackedEntityAttributeValues().uid("tei_to_uid")
                .blockingGet()
        ) doReturn trackedEntityInstance("tei_to_uid")

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_to_uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_to_uid")
                .byTrackedEntityAttribute()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_to_uid")
                .byTrackedEntityAttribute().`in`(listOf("tea_uid"))
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq("tei_to_uid")
                .byTrackedEntityAttribute().`in`(listOf("tea_uid"))
                .blockingGet()
        ) doReturn trackedEntityAttributeValue()

        presenter.updateRelationships()

        verify(view).setRelationships(relationshipViewModelsTo())
    }

    @Test
    fun `Should set relationship types in function of teiType`() {
        val relationshipTypes =
            listOf(
                Trio.create(
                    RelationshipType.builder()
                        .uid("relationship_type").build(),
                    "tei_type", 0
                )
            )

        whenever(
            dashboardRepository
                .relationshipsForTeiType("tei_type")
        ) doReturn relationshipForTeiType()

        whenever(view.abstracContext) doReturn mock()
        whenever(
            dashboardRepository
                .getObjectStyle(view.abstracContext, "tei_type")
        ) doReturn 0

        presenter.relationshipForTeiType()

        verify(view).setRelationshipTypes(relationshipTypes)
    }

    @Test
    fun `Should delete a relationship`() {
        whenever(
            d2.relationshipModule().relationships().withItems()
                .uid("relationship_uid")
        ) doReturn mock()
        whenever(view.analyticsHelper()) doReturn mock()

        val testSubscriber = presenter.updateRelationships.test()
        presenter.deleteRelationship(Relationship.builder().uid("relationship_uid").build())

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(true)
    }

    @Test
    fun `Should add a relationship`() {
        val testingRelationshipUid = "note_uid"
        whenever(
            d2.relationshipModule().relationships().blockingAdd(
                RelationshipHelper.teiToTeiRelationship("tei_uid", "tei_to", "type")
            )
        ) doReturn testingRelationshipUid

        val testSubscriber = presenter.updateRelationships.test()
        presenter.addRelationship("tei_to", "type")

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(true)
    }

    @Test
    fun `Should call to go to add relationship`() {
        whenever(
            d2.programModule().programs().uid("program_uid")
                .blockingGet()
        ) doReturn getProgramDefaultAccessTrue()
        whenever(view.analyticsHelper()) doReturn mock()

        presenter.goToAddRelationship("type_add")

        verify(view).goToAddRelationship("tei_uid", "type_add")
    }

    @Test
    fun `Should open dashboard`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one().blockingGet()
        ) doReturn teiWithState()

        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq("tei_uid").blockingGet()
        ) doReturn listOf(
            Enrollment.builder()
                .uid("enrollment_uid").trackedEntityInstance("tei_uid").build()
        )

        presenter.openDashboard("tei_uid")

        verify(view).goToTeiDashboard("tei_uid")
    }

    @Test
    fun `Should not open  dashboard and show dialog WithoutEnrollment`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one().blockingGet()
        ) doReturn teiWithState()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq("tei_uid").blockingGet()
        ) doReturn listOf()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes()
                .uid("tei_type").blockingGet()
        ) doReturn TrackedEntityType
            .builder().uid("tei_type").displayName("displayName").build()

        presenter.openDashboard("tei_uid")

        verify(view).showDialogRelationshipWithoutEnrollment("displayName")
    }

    @Test
    fun `Should not open  dashboard and show dialog NotFoundMessage`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq("tei_uid").one().blockingGet()
        ) doReturn teiWithStateRelationship()

        whenever(
            d2.trackedEntityModule().trackedEntityTypes()
                .uid("tei_type").blockingGet()
        ) doReturn TrackedEntityType
            .builder().uid("tei_type").displayName("displayName").build()

        presenter.openDashboard("tei_uid")

        verify(view).showDialogRelationshipNotFoundMessage("displayName")
    }

    private fun mockRelationTrackedEntityTA() {
        whenever(
            d2.relationshipModule().relationshipTypes()
                .blockingGet()
        ) doReturn relationshipTypes()

        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq("tei_type")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq("tei_type").byDisplayInList()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq("tei_type").byDisplayInList().isTrue
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq("tei_type")
                .byDisplayInList().isTrue.blockingGet()
        ) doReturn trackedEntityTypeAttribute()
    }

    private fun relationshipForTeiType() =
        Observable.just(
            listOf(
                Pair.create(
                    RelationshipType.builder()
                        .uid("relationship_type")
                        .build(),
                    "tei_type"
                )
            )
        )

    private fun relationShipItem(tei: String) =
        RelationshipItem.builder().trackedEntityInstance(
            RelationshipItemTrackedEntityInstance
                .builder()
                .trackedEntityInstance(tei)
                .build()
        ).build()

    private fun listRelationshipItemsFrom() = listOf(
        Relationship.builder()
            .relationshipType("relationship_type")
            .from(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder()
                        .trackedEntityInstance("tei_from_uid")
                        .build()
                )
                    .build()
            ).build()
    )

    private fun teiWithState() = TrackedEntityInstance.builder()
        .uid("tei_uid").state(State.TO_POST).build()

    private fun teiWithStateRelationship() = TrackedEntityInstance.builder()
        .uid("tei_uid").state(State.RELATIONSHIP).build()

    private fun listRelationshipItemsTo() = listOf(
        Relationship.builder()
            .relationshipType("relationship_type")
            .from(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder()
                        .trackedEntityInstance("tei_from_uid")
                        .build()
                )
                    .build()
            )
            .to(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder()
                        .trackedEntityInstance("tei_to_uid")
                        .build()
                )
                    .build()
            ).build()
    )

    private fun mockTrackedEntityInstanceTypeModule(tei: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid()
                .eq(tei)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid()
                .eq(tei).withTrackedEntityAttributeValues()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid()
                .eq(tei).withTrackedEntityAttributeValues().one()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byUid()
                .eq(tei).withTrackedEntityAttributeValues().one()
                .blockingGet()
        ) doReturn trackedEntityInstance(tei)
    }

    private fun relationshipTypes() =
        listOf(RelationshipType.builder().uid("relationship_type").build())

    private fun trackedEntityInstance(tei: String) =
        TrackedEntityInstance.builder()
            .uid(tei)
            .trackedEntityType("tei_type")
            .build()

    private fun trackedEntityTypeAttribute() =
        listOf(
            TrackedEntityTypeAttribute.builder()
                .trackedEntityAttribute(ObjectWithUid.create("tea_uid"))
                .searchable(true)
                .displayInList(true)
                .trackedEntityType(ObjectWithUid.create("te_type")).build()
        )

    private fun trackedEntityAttributeValue() =
        listOf(
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("tea_uid")
                .trackedEntityInstance("tei_uid")
                .build()
        )

    private fun relationshipViewModelsFrom() =
        listOf(
            RelationshipViewModel.create(
                listRelationshipItemsFrom()[0],
                relationshipTypes()[0], RelationshipViewModel.RelationshipDirection.FROM,
                "tei_from_uid", trackedEntityAttributeValue()
            )
        )

    private fun relationshipViewModelsTo() =
        listOf(
            RelationshipViewModel.create(
                listRelationshipItemsTo()[0],
                relationshipTypes()[0], RelationshipViewModel.RelationshipDirection.TO,
                "tei_to_uid", trackedEntityAttributeValue()
            )
        )

    private fun getProgramDefaultAccessTrue(): Program {
        return Program.builder()
            .uid("program_uid")
            .access(
                Access.create(
                    false, false,
                    DataAccess.create(false, true)
                )
            )
            .build()
    }
}
