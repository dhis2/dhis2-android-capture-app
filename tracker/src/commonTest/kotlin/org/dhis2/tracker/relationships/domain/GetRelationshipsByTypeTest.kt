package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.tracker.relationships.relationshipModel1
import org.dhis2.tracker.relationships.relationshipModel2
import org.dhis2.tracker.relationships.relationshipSection1
import org.dhis2.tracker.relationships.relationshipSection2
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetRelationshipsByTypeTest {
    private lateinit var getRelationshipsByType: GetRelationshipsByType

    private val relationshipsRepository: RelationshipsRepositoryActions = mock()

    @Before
    fun setup() {
        getRelationshipsByType =
            GetRelationshipsByType(
                relationshipsRepository = relationshipsRepository,
            )
    }

    @Test
    fun `invoke should return relationship sections grouped by type`() =
        runTest {
            // Given a list of relationship types and relationships
            whenever(relationshipsRepository.getRelationshipTypes()) doReturn getRelationshipSectionsMock()
            whenever(
                relationshipsRepository.getRelationshipsGroupedByTypeAndSide(relationshipSection1),
            ) doReturn
                relationshipSection1.copy(
                    relationships = listOf(relationshipModel1),
                )

            whenever(
                relationshipsRepository.getRelationshipsGroupedByTypeAndSide(relationshipSection2),
            ) doReturn
                relationshipSection2.copy(
                    relationships = listOf(relationshipModel2),
                )

            // When calling the use case to get the relationships grouped by type
            val result = getRelationshipsByType()

            // Then a list of RelationshipSections with their relationships should be returned
            val expectedResult =
                listOf(
                    relationshipSection1.copy(
                        relationships = listOf(relationshipModel1),
                    ),
                    relationshipSection2.copy(
                        relationships = listOf(relationshipModel2),
                    ),
                )
            assertEquals(expectedResult, result)
        }

    private fun getRelationshipSectionsMock(): List<RelationshipSection> =
        listOf(
            relationshipSection1,
            relationshipSection2,
        )
}
