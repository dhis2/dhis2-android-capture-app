package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddRelationshipTest {
    private lateinit var addRelationship: AddRelationship
    private val repository: RelationshipsRepositoryActions = mock()

    @Before
    fun setup() {
        addRelationship =
            AddRelationship(
                repository = repository,
            )
    }

    @Test
    fun shouldAddRelationship() =
        runTest {
            // Given user tries to add a relationship
            val selectedTeiUid = "selectedTeiUid"
            val relationshipTypeUid = "relationshipTypeUid"
            val side = RelationshipConstraintSide.TO

            // When
            whenever(
                repository.addRelationship(any(), any(), any()),
            ) doReturn Result.success("relationshipUid")

            val result = addRelationship(selectedTeiUid, relationshipTypeUid, side)

            // Then
            assert(result.isSuccess)
        }

    @Test
    fun shouldFailWhenAddRelationship() =
        runTest {
            // Given user tries to add a relationship
            val selectedTeiUid = "selectedTeiUid"
            val relationshipTypeUid = "relationshipTypeUid"
            val side = RelationshipConstraintSide.TO

            // When
            whenever(
                repository.addRelationship(any(), any(), any()),
            ) doReturn Result.failure(Exception("Failed to add relationship"))

            val result = addRelationship(selectedTeiUid, relationshipTypeUid, side)

            // Then there is an error when adding relationship
            assert(result.isFailure)
            assert(result.exceptionOrNull()?.message == "Failed to add relationship")
        }
}
