package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.fileresource.FileResource
import org.hisp.dhis.android.core.fileresource.FileResourceCollectionRepository
import org.hisp.dhis.android.core.fileresource.FileResourceObjectRepository
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.TrackerDataView
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Covers that a file based value is rendered on a relationship card with the original file name,
 * instead of the absolute path of the file on disk.
 */
class RelationshipsRepositoryFileValuesTest {
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val resources: ResourceManager = mock()
    private val repository = TestRelationshipsRepository(d2, resources)

    private val teiUid = "teiUid"
    private val eventUid = "eventUid"
    private val fileUid = "afl3jai2i4u"
    private val filePath = "/sdk_resources/db/afl3jai2i4u.pdf"
    private val originalName = "report.pdf"

    @Test
    fun `should show the original file name for a file attribute`() =
        runTest {
            val attributeUid = "attributeUid"
            // Mocks are built up front: creating one inside whenever() leaves Mockito mid-stubbing
            val attribute = fileAttribute(attributeUid)
            mockFileResource()
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(attributeUid)
                    .blockingGet(),
            ) doReturn attribute
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributeValues()
                    .value(attributeUid, teiUid)
                    .blockingGet(),
            ) doReturn
                TrackedEntityAttributeValue
                    .builder()
                    .trackedEntityAttribute(attributeUid)
                    .trackedEntityInstance(teiUid)
                    .value(fileUid)
                    .build()

            val result =
                repository.teiAttributes(
                    teiUid = teiUid,
                    relationshipConstraint = constraintWithAttribute(attributeUid),
                )

            assertEquals(listOf("File" to originalName), result)
        }

    @Test
    fun `should show the original file name for a file data element`() =
        runTest {
            val dataElementUid = "dataElementUid"
            // Mocks are built up front: creating one inside whenever() leaves Mockito mid-stubbing
            val dataElement = fileDataElement(dataElementUid)
            mockFileResource()
            whenever(
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(dataElementUid)
                    .blockingGet(),
            ) doReturn dataElement
            whenever(
                d2
                    .eventModule()
                    .events()
                    .withTrackedEntityDataValues()
                    .uid(eventUid)
                    .blockingGet(),
            ) doReturn eventWithFileValue(dataElementUid)

            val result =
                repository.eventValues(
                    eventUid = eventUid,
                    relationshipConstraint = constraintWithDataElement(dataElementUid),
                )

            assertEquals(listOf("File" to originalName), result)
        }

    /**
     * The filter connectors cannot be reached through deep stubs, because `eq()` erases to its
     * `BaseRepository` upper bound, so every step of the chain is mocked explicitly.
     */
    private fun mockFileResource() {
        val fileResource = fileResource()
        val fileResources: FileResourceCollectionRepository = mock()
        val byUid: StringFilterConnector<FileResourceCollectionRepository> = mock()
        val byPath: StringFilterConnector<FileResourceCollectionRepository> = mock()
        val byUidEqUid: FileResourceCollectionRepository = mock()
        val byUidEqPath: FileResourceCollectionRepository = mock()
        val byPathEqPath: FileResourceCollectionRepository = mock()
        val oneByUidEqUid: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val oneByUidEqPath: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val oneByPathEqPath: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val byUidObjectRepository: FileResourceObjectRepository = mock()

        whenever(d2.fileResourceModule().fileResources()) doReturn fileResources
        whenever(fileResources.byUid()) doReturn byUid
        whenever(fileResources.byPath()) doReturn byPath
        whenever(byUid.eq(fileUid)) doReturn byUidEqUid
        whenever(byUid.eq(filePath)) doReturn byUidEqPath
        whenever(byPath.eq(filePath)) doReturn byPathEqPath
        whenever(byUidEqUid.one()) doReturn oneByUidEqUid
        whenever(byUidEqPath.one()) doReturn oneByUidEqPath
        whenever(byPathEqPath.one()) doReturn oneByPathEqPath

        // check() and checkValueTypeValue() resolve the value to the path of the file on disk
        whenever(oneByUidEqUid.blockingExists()) doReturn true
        whenever(fileResources.uid(fileUid)) doReturn byUidObjectRepository
        whenever(byUidObjectRepository.blockingGet()) doReturn fileResource

        // fileResourceNameOf() resolves the path back to the original file name
        whenever(oneByUidEqPath.blockingGet()) doReturn null
        whenever(oneByPathEqPath.blockingGet()) doReturn fileResource
    }

    private fun fileResource(): FileResource =
        mock {
            on { uid() } doReturn fileUid
            on { path() } doReturn filePath
            on { name() } doReturn originalName
        }

    private fun fileAttribute(uid: String): TrackedEntityAttribute =
        mock {
            on { uid() } doReturn uid
            on { displayFormName() } doReturn "File"
            on { valueType() } doReturn ValueType.FILE_RESOURCE
        }

    private fun fileDataElement(uid: String): DataElement =
        mock {
            on { uid() } doReturn uid
            on { displayName() } doReturn "File"
            on { valueType() } doReturn ValueType.FILE_RESOURCE
        }

    private fun eventWithFileValue(dataElementUid: String): Event =
        Event
            .builder()
            .uid(eventUid)
            .trackedEntityDataValues(
                listOf(
                    TrackedEntityDataValue
                        .builder()
                        .event(eventUid)
                        .dataElement(dataElementUid)
                        .value(fileUid)
                        .build(),
                ),
            ).build()

    private fun constraintWithAttribute(attributeUid: String): RelationshipConstraint =
        RelationshipConstraint
            .builder()
            .trackerDataView(
                TrackerDataView
                    .builder()
                    .attributes(listOf(attributeUid))
                    .dataElements(emptyList())
                    .build(),
            ).build()

    private fun constraintWithDataElement(dataElementUid: String): RelationshipConstraint =
        RelationshipConstraint
            .builder()
            .trackerDataView(
                TrackerDataView
                    .builder()
                    .attributes(emptyList())
                    .dataElements(listOf(dataElementUid))
                    .build(),
            ).build()
}

private class TestRelationshipsRepository(
    d2: D2,
    resources: ResourceManager,
) : RelationshipsRepository(d2, resources) {
    suspend fun teiAttributes(
        teiUid: String,
        relationshipConstraint: RelationshipConstraint,
    ) = getTeiAttributesForRelationship(teiUid, relationshipConstraint, null)

    suspend fun eventValues(
        eventUid: String,
        relationshipConstraint: RelationshipConstraint,
    ) = getEventValuesForRelationship(eventUid, relationshipConstraint, null)

    override suspend fun getRelationshipTypes(): List<RelationshipSection> = emptyList()

    override suspend fun getRelationshipsGroupedByTypeAndSide(relationshipSection: RelationshipSection): RelationshipSection =
        relationshipSection

    override suspend fun getRelationships(): List<RelationshipModel> = emptyList()

    override fun createRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Relationship = throw UnsupportedOperationException()
}
