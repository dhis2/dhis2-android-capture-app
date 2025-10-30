package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.relationshipSection1
import org.dhis2.tracker.relationships.relationshipSection2
import org.dhis2.tracker.relationships.relationshipTypeEventToTei
import org.dhis2.tracker.relationships.relationshipTypeTeiToTei
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.relationship.RelationshipConstraintType
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
    private val trackedEntityType = "trackedEntityType1"

    @Before
    fun setup() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet(),
        ) doReturn trackedEntityInstance
        whenever(
            trackedEntityInstance.trackedEntityType(),
        ) doReturn trackedEntityType

        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .blockingGet(),
        ) doReturn enrollment

        trackerRelationshipsRepository =
            TrackerRelationshipsRepository(
                d2 = d2,
                resources = resources,
                teiUid = teiUid,
                enrollmentUid = enrollmentUid,
                profilePictureProvider = profilePictureProvider,
            )
    }

    @Test
    fun shouldGetRelationshipTypes() =
        runTest {
            // Given
            // A TEI enrolled in a program
            whenever(enrollment.program()) doReturn "programUid_1"

            // With two relationship types related
            whenever(
                d2
                    .relationshipModule()
                    .relationshipService()
                    .getRelationshipTypesForTrackedEntities(
                        trackedEntityType = trackedEntityType,
                        programUid = "programUid_1",
                    ),
            ) doReturn relationshipWithEntitySideList

            // When getting the relationship types
            val relationshipTypes = trackerRelationshipsRepository.getRelationshipTypes()

            // Then a relationshipSectionList is returned
            val expectedResult =
                listOf(
                    relationshipSection1,
                    relationshipSection2,
                )
            assertEquals(expectedResult, relationshipTypes)
        }

    private val relationshipWithEntitySideList =
        listOf(
            RelationshipTypeWithEntitySide(
                relationshipType = relationshipTypeTeiToTei,
                entitySide = RelationshipConstraintType.FROM,
            ),
            RelationshipTypeWithEntitySide(
                relationshipType = relationshipTypeEventToTei,
                entitySide = RelationshipConstraintType.FROM,
            ),
        )
}
