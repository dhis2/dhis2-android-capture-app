package org.dhis2.tracker.dashboard.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.tracker.dashboard.data.DashboardRepository
import org.dhis2.tracker.dashboard.domain.model.EnrollmentAOC
import org.dhis2.tracker.dashboard.domain.model.EnrollmentData
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetEnrollmentAOCTest {
    private val repository: DashboardRepository = mock()
    private val getEnrollmentAOC = GetEnrollmentAOC(repository)

    private val input =
        EnrollmentData(
            enrollmentUid = "enrollmentUid",
            programUid = "programUid",
            attributeOptionCombo = "aocUid",
        )

    @Test
    fun `should return the category combo and option combo labels for a non-default combo`() =
        runTest {
            whenever(repository.enrollmentCategoryComboLabel("programUid")) doReturn "TB case type"
            whenever(repository.enrollmentCategoryOptionComboLabel("aocUid")) doReturn "Index, Global Fund"

            val result = getEnrollmentAOC(input)

            assertTrue(result.isSuccess)
            assertEquals(
                EnrollmentAOC(label = "TB case type", value = "Index, Global Fund"),
                result.getOrNull(),
            )
        }

    @Test
    fun `should return null for the default category combo`() =
        runTest {
            whenever(repository.enrollmentCategoryComboLabel("programUid")) doReturn null

            val result = getEnrollmentAOC(input)

            assertTrue(result.isSuccess)
            assertNull(result.getOrNull())
            verify(repository, never()).enrollmentCategoryOptionComboLabel(any())
        }

    @Test
    fun `should return failure when the repository throws a domain error`() =
        runTest {
            val error = DomainError.DataNotFoundError("Program not found")
            whenever(repository.enrollmentCategoryComboLabel("programUid")) doAnswer { throw error }

            val result = getEnrollmentAOC(input)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `should return failure instead of throwing on an unexpected error`() =
        runTest {
            val error = IllegalStateException("Unexpected")
            whenever(repository.enrollmentCategoryComboLabel("programUid")) doReturn "TB case type"
            whenever(repository.enrollmentCategoryOptionComboLabel("aocUid")) doAnswer { throw error }

            val result = getEnrollmentAOC(input)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
