package org.dhis2.tracker.relationships.data

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
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
    private val enrollment: Enrollment = mock()

    @Before
    fun setup() {
        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid("enrollmentUid")
                .blockingGet()
        ) doReturn enrollment

        trackerRelationshipsRepository = TrackerRelationshipsRepository(
            d2 = d2,
            resources = resources,
            teiUid = "teiUid",
            enrollmentUid = "enrollmentUid",
            profilePictureProvider = profilePictureProvider
        )
    }

    @Test
    fun shouldShowFromToNameRelationshipTitle() {
        val relationshipName = "FromRelationship name"

        val program: ObjectWithUid = mock {
            on { uid() } doReturn "programUid_1"
        }
        val fromConstraint: RelationshipConstraint = mock {
            on { program() } doReturn program
        }
        //Given a TEI with relationship type in From constraint and related to a program
        val relationshipType: RelationshipType = mock {
            on { fromConstraint() } doReturn fromConstraint
            on { fromToName() } doReturn relationshipName
        }

        //When getting the relationship title
        whenever(enrollment.program()) doReturn "programUid_1"
        val title = trackerRelationshipsRepository.getRelationshipDirectionInfo(relationshipType)

        //Then the title should be the one from the From constraint
        assert(title.first == relationshipName)
    }

    @Test
    fun shouldShowToFromNameRelationshipTitle() {
        val relationshipName = "ToRelationship name"

        val program: ObjectWithUid = mock {
            on { uid() } doReturn "programUid_2"
        }
        val toConstraint: RelationshipConstraint = mock {
            on { program() } doReturn program
        }
        //Given a TEI with relationship type in To constraint and related to a program
        val relationshipType: RelationshipType = mock {
            on { toConstraint() } doReturn toConstraint
            on { toFromName() } doReturn relationshipName
        }

        //When getting the relationship title
        whenever(enrollment.program()) doReturn "programUid_2"

        val title = trackerRelationshipsRepository.getRelationshipDirectionInfo(relationshipType)

        //Then the title should be the one from the To constraint
        assert(title.first == relationshipName)
    }
}