package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.relationshipSection1
import org.dhis2.tracker.relationships.relationshipSection2
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipConstraintType
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.relationship.RelationshipTypeWithEntitySide
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TrackerRelationshipsRepositoryTest {

    private lateinit var trackerRelationshipsRepository: TrackerRelationshipsRepository

    private val resources: ResourceManager = mock()
    private val profilePictureProvider: ProfilePictureProvider = mock()

    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val trackedEntityInstance: TrackedEntityInstance = mock()
    private val enrollment: Enrollment = mock()

    private val teiUid = "teiUid"
    private val enrollmentUid = "enrollmentUid"
    private val trackedEntityType = "trackedEntityType"

    @Before
    fun setup() {
        whenever(
            d2.trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet()
        ) doReturn trackedEntityInstance
        whenever(
            trackedEntityInstance.trackedEntityType()
        ) doReturn trackedEntityType

        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        ) doReturn enrollment

        trackerRelationshipsRepository = TrackerRelationshipsRepository(
            d2 = d2,
            resources = resources,
            teiUid = teiUid,
            enrollmentUid = enrollmentUid,
            profilePictureProvider = profilePictureProvider
        )
    }

    @Test
    fun shouldGetRelationshipTypes() = runTest {
        //Given
        //A TEI enrolled in a program
        whenever(enrollment.program()) doReturn "programUid_1"


        //With two relationship types related
        whenever(
            d2.relationshipModule().relationshipService()
                .getRelationshipTypesForTrackedEntities(
                    trackedEntityType = trackedEntityType,
                    programUid = "programUid_1"
                )
        ) doReturn relationshipWithEntitySideList

        //When getting the relationship types
        val relationshipTypes = trackerRelationshipsRepository.getRelationshipTypes()


        //Then a relationshipSectionList is returned
        assertEquals(relationshipTypes, expectedResult)
    }



    private val relationshipTypeTeiToTei = RelationshipType.builder()
        .uid("relationshipTypeUid1")
        .fromToName("RelationshipType1 FROM")
        .toFromName("RelationshipType1 TO")
        .displayName("Tei to Tei relationship")
        .fromConstraint(
            RelationshipConstraint.builder()
                .trackedEntityType(
                    ObjectWithUid.create(trackedEntityType)
                ).build()
        )
        .toConstraint(
            RelationshipConstraint.builder()
                .trackedEntityType(
                    ObjectWithUid.create("trackedEntityTypeUid2")
                )
                .build()
        )
        .build()

    private val relationshipWithEntitySideList = listOf(
        RelationshipTypeWithEntitySide(
            relationshipType = relationshipTypeTeiToTei,
            entitySide = RelationshipConstraintType.FROM
        ),
        RelationshipTypeWithEntitySide(
            relationshipType = RelationshipType.builder()
                .uid("relationshipTypeUid2")
                .fromToName("RelationshipType2 FROM")
                .toFromName("RelationshipType2 TO")
                .displayName("relationshipType2")
                .build(),
            entitySide = RelationshipConstraintType.FROM
        )
    )

    private val expectedResult = listOf(
        relationshipSection1,
        relationshipSection2,
    )
}