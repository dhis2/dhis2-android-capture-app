package org.dhis2.tracker.relationships.data

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentModule
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModule
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

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val trackedEntityModule: TrackedEntityModule = mock()
    private val trackedEntityInstances: TrackedEntityInstanceCollectionRepository = mock()
    private val enrollmentModule: EnrollmentModule = mock()
    private val enrollment: EnrollmentCollectionRepository = mock()

    @Before
    fun setup() {
        whenever(d2.trackedEntityModule()) doReturn trackedEntityModule
        whenever(trackedEntityModule.trackedEntityInstances()) doReturn trackedEntityInstances
        whenever(trackedEntityInstances.uid("teiUid")) doReturn mock()
        whenever(trackedEntityInstances.uid("teiUid").blockingGet()) doReturn mock()
        whenever(
            trackedEntityInstances.uid("teiUid").blockingGet()?.trackedEntityType()
        ) doReturn "trackedEntityTypeUid"

        whenever(d2.enrollmentModule()) doReturn enrollmentModule
        whenever(enrollmentModule.enrollments()) doReturn enrollment
        whenever(enrollment.uid("enrollmentUid")) doReturn mock()
        whenever(enrollment.uid("enrollmentUid").blockingGet()) doReturn mock()

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
        whenever(enrollment.uid("enrollmentUid").blockingGet()?.program()) doReturn "programUid_1"
        val title = trackerRelationshipsRepository.getRelationshipTitle(relationshipType)

        //Then the title should be the one from the From constraint
        assert(title == relationshipName)
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
        whenever(enrollment.uid("enrollmentUid").blockingGet()?.program()) doReturn "programUid_2"

        val title = trackerRelationshipsRepository.getRelationshipTitle(relationshipType)

        //Then the title should be the one from the To constraint
        assert(title == relationshipName)
    }
}