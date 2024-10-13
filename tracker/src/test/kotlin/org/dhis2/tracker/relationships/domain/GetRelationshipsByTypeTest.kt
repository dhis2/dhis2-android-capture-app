package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipItem
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.tracker.relationships.model.RelationshipViewModel
import org.dhis2.tracker.ui.AvatarProvider
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class GetRelationshipsByTypeTest {

    lateinit var getRelationshipsByType: GetRelationshipsByType

    private val relationshipsRepository: RelationshipsRepository = mock()
    private val dateLabelProvider: DateLabelProvider = mock()
    private val avatarProvider: AvatarProvider = mock {
        on { getAvatar(any(), any(), any()) } doReturn AvatarProviderConfiguration.MainValueLabel(
            firstMainValue = "M"
        )
    }
    private val relationshipType1: RelationshipType = mock {
        on { uid() } doReturn "type1"
        on { displayName() } doReturn "Relationship 1"
    }
    private val relationshipType2: RelationshipType = mock {
        on { uid() } doReturn "type2"
        on { displayName() } doReturn "Relationship 2"
    }

    @Before
    fun setup() {
        getRelationshipsByType = GetRelationshipsByType(
            relationshipsRepository = relationshipsRepository,
            dateLabelProvider = dateLabelProvider,
            avatarProvider = avatarProvider
        )
    }

    @Test
    fun `invoke should return relationship sections grouped by type`() = runTest {
        //Given a list of relationship types and relationships
        whenever(relationshipsRepository.getRelationshipTypes()) doReturn getRelationshipTypesMock()
        whenever(relationshipsRepository.getRelationships()) doReturn getRelationshipsMock()
        whenever(dateLabelProvider.span(any())) doReturn "5 days ago"

        // When calling the use case to get the relationships grouped by type
        val result = getRelationshipsByType().first()

        // Then a list of RelationshipSections should be returned
        assertEquals(relationshipSections, result)

        // And verify that the repository and dateLabelProvider were called
        verify(relationshipsRepository).getRelationshipTypes()
        verify(relationshipsRepository).getRelationships()
        verify(dateLabelProvider, times(2)).span(any())
    }


    private fun getRelationshipTypesMock(): Flow<List<Pair<RelationshipType, String?>>> {
        return flowOf(
            listOf(
                relationshipType1 to "teiType1",
                relationshipType2 to "teiType2"
            )
        )
    }

    private fun getRelationshipsMock(): Flow<List<RelationshipViewModel>> {
        val relationshipViewModel1 = RelationshipViewModel(
            relationshipType = relationshipType1,
            ownerType = RelationshipOwnerType.TEI,
            ownerUid = "OwnerUid1",
            canBeOpened = true,
            fromValues = listOf(
                "MainValue1" to "Value1",
                "SecondMainValue1" to "SecondValue1",
            ),
            toValues = emptyList(),
            fromImage = null,
            toImage = null,
            fromDefaultImageResource = 0,
            toDefaultImageResource = 0,
            ownerStyle = mock(),
            fromLastUpdated = Date(),
            toLastUpdated = Date(),
            fromDescription = "Description 1",
            toDescription = "Description 1",
            fromGeometry = null,
            toGeometry = null,
            direction = RelationshipDirection.FROM,
            relationship = mock()
        )

        val relationshipViewModel2 = RelationshipViewModel(
            relationshipType = relationshipType2,
            ownerType = RelationshipOwnerType.TEI,
            ownerUid = "OwnerUid2",
            canBeOpened = true,
            fromValues = listOf(
                "MainValue2" to "Value2",
                "SecondMainValue2" to "SecondValue2",
            ),
            toValues = emptyList(),
            fromImage = null,
            toImage = null,
            fromDefaultImageResource = 0,
            toDefaultImageResource = 0,
            ownerStyle = mock(),
            fromLastUpdated = Date(),
            toLastUpdated = Date(),
            fromDescription = "Description 2",
            toDescription = "Description 2",
            fromGeometry = null,
            toGeometry = null,
            direction = RelationshipDirection.FROM,
            relationship = mock()
        )


        return flowOf(
            listOf(relationshipViewModel1, relationshipViewModel2)
        )
    }

    private val relationshipSections = listOf(
        RelationshipSection(
            relationshipType = relationshipType1,
            relationships = listOf(
                RelationshipItem(
                    title = "MainValue1: Value1",
                    description = "Description 1",
                    attributes = listOf(
                        "SecondMainValue1" to "SecondValue1"
                    ),
                    ownerType = RelationshipOwnerType.TEI,
                    ownerUid = "OwnerUid1",
                    avatar = AvatarProviderConfiguration.MainValueLabel(
                        firstMainValue = "M",
                    ),
                    canOpen = true,
                    lastUpdated = "5 days ago"
                )
            ),
            teiTypeUid = "teiType1"
        ),
        RelationshipSection(
            relationshipType = relationshipType2,
            relationships = listOf(
                RelationshipItem(
                    title = "MainValue2: Value2",
                    description = "Description 2",
                    attributes = listOf(
                        "SecondMainValue2" to "SecondValue2"
                    ),
                    ownerType = RelationshipOwnerType.TEI,
                    ownerUid = "OwnerUid2",
                    avatar = AvatarProviderConfiguration.MainValueLabel(
                        firstMainValue = "M",
                    ),
                    canOpen = true,
                    lastUpdated = "5 days ago"
                )
            ),
            teiTypeUid = "teiType2"
        )
    )
}