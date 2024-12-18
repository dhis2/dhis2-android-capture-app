package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.hisp.dhis.android.core.relationship.Relationship
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddRelationshipTest {

    private lateinit var addRelationship: AddRelationship

    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.Unconfined
    }
    private val repository: RelationshipsRepository = mock()

    @Before
    fun setup() {
        addRelationship = AddRelationship(
            dispatcher = dispatcherProvider,
            repository = repository
        )
    }

    @Test
    fun shouldAddRelationship() = runTest {
        // Given user tries to add a relationship
        val selectedTeiUid = "selectedTeiUid"
        val relationshipTypeUid = "relationshipTypeUid"
        val side = RelationshipConstraintSide.TO

        val relationship = Relationship.builder()
            .uid("relationshipUid")
            .build()

        // When
        whenever(
            repository.createRelationship(selectedTeiUid, relationshipTypeUid, side)
        ) doReturn relationship
        whenever(
            repository.addRelationship(relationship)
        ) doReturn Result.success("relationshipUid")

        val result = addRelationship(selectedTeiUid, relationshipTypeUid, side)

        // Then
        assert(result.isSuccess)
    }

    @Test
    fun shouldFailWhenAddRelationship() = runTest {
        // Given user tries to add a relationship
        val selectedTeiUid = "selectedTeiUid"
        val relationshipTypeUid = "relationshipTypeUid"
        val side = RelationshipConstraintSide.TO

        val relationship = Relationship.builder()
            .uid("relationshipUid")
            .build()

        // When
        whenever(
            repository.createRelationship(selectedTeiUid, relationshipTypeUid, side)
        ) doReturn relationship
        whenever(
            repository.addRelationship(relationship)
        ) doReturn Result.failure(Exception("Failed to add relationship"))

        val result = addRelationship(selectedTeiUid, relationshipTypeUid, side)

        // Then there is an error when adding relationship
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Failed to add relationship")
    }
}