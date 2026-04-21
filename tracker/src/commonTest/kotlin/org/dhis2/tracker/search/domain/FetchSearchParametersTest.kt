package org.dhis2.tracker.search.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.data.SearchParametersRepository
import org.dhis2.tracker.search.model.FetchSearchParametersData
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchParameterModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FetchSearchParametersTest {
    private lateinit var useCase: FetchSearchParameters

    private val repository: SearchParametersRepository = mock()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcher =
        Dispatcher(
            io = testDispatcher,
            main = Dispatchers.Unconfined,
            default = Dispatchers.Unconfined,
        )

    @Before
    fun setup() {
        useCase =
            FetchSearchParameters(
                dispatcher = dispatcher,
                repository = repository,
            )
    }

    @Test
    fun `invoke should return sorted parameters for program`() =
        runTest {
            // Given
            val programUid = "programUid123"
            val teiTypeUid = "teiTypeUid123"
            val input = FetchSearchParametersData(teiTypeUid = teiTypeUid, programUid = programUid)

            val parameters =
                listOf(
                    createSearchParameter("param1", TrackerInputType.TEXT, isUnique = false),
                    createSearchParameter("param2", TrackerInputType.QR_CODE, isUnique = true),
                    createSearchParameter("param3", TrackerInputType.BAR_CODE, isUnique = false),
                    createSearchParameter(
                        "param4",
                        TrackerInputType.IMAGE,
                        isUnique = false,
                    ),
                )

            whenever(repository.getSearchParametersByProgram(programUid)) doReturn parameters

            // When
            val result = useCase.invoke(input)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).getSearchParametersByProgram(programUid)

            val sortedParameters = result.getOrNull()
            assertEquals(3, sortedParameters?.size)
            // QR_CODE unique should be first
            assertEquals("param2", sortedParameters?.get(0)?.uid)
            assertEquals(TrackerInputType.QR_CODE, sortedParameters?.get(0)?.inputType)
            assertTrue(sortedParameters?.get(0)?.isUnique == true)
        }

    @Test
    fun `invoke should return sorted parameters for tracked entity type when no program`() =
        runTest {
            // Given
            val teiTypeUid = "teiTypeUid123"
            val input = FetchSearchParametersData(teiTypeUid = teiTypeUid, programUid = null)

            val parameters =
                listOf(
                    createSearchParameter("param1", TrackerInputType.TEXT, isUnique = true),
                    createSearchParameter("param2", TrackerInputType.EMAIL, isUnique = false),
                )

            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid)) doReturn parameters

            // When
            val result = useCase.invoke(input)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).getSearchParametersByTrackedEntityType(teiTypeUid)

            val sortedParameters = result.getOrNull()
            assertEquals(2, sortedParameters?.size)
        }

    @Test
    fun `invoke should return failure when repository throws DomainError`() =
        runTest {
            // Given
            val teiTypeUid = "teiTypeUid123"
            val input = FetchSearchParametersData(teiTypeUid = teiTypeUid, programUid = null)
            val domainError = DomainError.UnexpectedError("Test error")

            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid)).thenAnswer { throw domainError }

            val result = useCase.invoke(input)

            // Then
            assertTrue(result.isFailure)
            assertEquals(domainError, result.exceptionOrNull())
        }

    @Test
    fun `sortSearchParameters should prioritize QR and BarCode unique parameters`() {
        // Given
        val parameters =
            listOf(
                createSearchParameter("text1", TrackerInputType.TEXT, isUnique = false),
                createSearchParameter("qr1", TrackerInputType.QR_CODE, isUnique = true),
                createSearchParameter("text2", TrackerInputType.TEXT, isUnique = true),
                createSearchParameter("bar1", TrackerInputType.BAR_CODE, isUnique = true),
                createSearchParameter("qr2", TrackerInputType.QR_CODE, isUnique = false),
            )

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then
        assertEquals(5, sorted.size)
        // First two should be QR_CODE or BAR_CODE with unique
        assertTrue(useCase.isQrCodeOrBarCode(sorted[0].inputType) && sorted[0].isUnique)
        assertTrue(useCase.isQrCodeOrBarCode(sorted[1].inputType) && sorted[1].isUnique)
        // Third should be QR_CODE without unique
        assertTrue(useCase.isQrCodeOrBarCode(sorted[2].inputType))
        // Fourth should be text with unique
        assertEquals(TrackerInputType.TEXT, sorted[3].inputType)
        assertTrue(sorted[3].isUnique)
        // Last should be text without unique
        assertEquals(TrackerInputType.TEXT, sorted[4].inputType)
        assertFalse(sorted[4].isUnique)
    }

    @Test
    fun `sortSearchParameters should handle empty list`() {
        // Given
        val parameters = emptyList<SearchParameterModel>()

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then
        assertTrue(sorted.isEmpty())
    }

    @Test
    fun `sortSearchParameters should handle list with only non-QR-BarCode parameters`() {
        // Given
        val parameters =
            listOf(
                createSearchParameter("text1", TrackerInputType.TEXT, isUnique = false),
                createSearchParameter("email1", TrackerInputType.EMAIL, isUnique = true),
                createSearchParameter("number1", TrackerInputType.NUMBER, isUnique = false),
            )

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then
        assertEquals(3, sorted.size)
        // Unique should come before non-unique
        assertTrue(sorted[0].isUnique)
        assertEquals("email1", sorted[0].uid)
        assertFalse(sorted[1].isUnique)
        assertFalse(sorted[2].isUnique)
    }

    @Test
    fun `sortSearchParameters should handle list with only QR and BarCode parameters`() {
        // Given
        val parameters =
            listOf(
                createSearchParameter("qr1", TrackerInputType.QR_CODE, isUnique = false),
                createSearchParameter("bar1", TrackerInputType.BAR_CODE, isUnique = true),
                createSearchParameter("qr2", TrackerInputType.QR_CODE, isUnique = true),
                createSearchParameter("bar2", TrackerInputType.BAR_CODE, isUnique = false),
            )

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then
        assertEquals(4, sorted.size)
        // First two should be unique QR or BarCode
        assertTrue(sorted[0].isUnique)
        assertTrue(sorted[1].isUnique)
        // Last two should be non-unique QR or BarCode
        assertFalse(sorted[2].isUnique)
        assertFalse(sorted[3].isUnique)
    }

    @Test
    fun `isQrCodeOrBarCode should return true for QR_CODE`() {
        // When
        val result = useCase.isQrCodeOrBarCode(TrackerInputType.QR_CODE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isQrCodeOrBarCode should return true for BAR_CODE`() {
        // When
        val result = useCase.isQrCodeOrBarCode(TrackerInputType.BAR_CODE)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isQrCodeOrBarCode should return false for TEXT`() {
        // When
        val result = useCase.isQrCodeOrBarCode(TrackerInputType.TEXT)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isQrCodeOrBarCode should return false for null`() {
        // When
        val result = useCase.isQrCodeOrBarCode(null)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isQrCodeOrBarCode should return false for other input types`() {
        // Test various other input types
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.EMAIL))
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.NUMBER))
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.DATE))
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.DATE_TIME))
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.PHONE_NUMBER))
        assertFalse(useCase.isQrCodeOrBarCode(TrackerInputType.ORGANISATION_UNIT))
    }

    @Test
    fun `sortSearchParameters should maintain stable order for equal priority items`() {
        // Given - multiple items with same priority
        val parameters =
            listOf(
                createSearchParameter("text1", TrackerInputType.TEXT, isUnique = false),
                createSearchParameter("text2", TrackerInputType.TEXT, isUnique = false),
                createSearchParameter("text3", TrackerInputType.TEXT, isUnique = false),
            )

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then - order should be maintained
        assertEquals(3, sorted.size)
        assertEquals("text1", sorted[0].uid)
        assertEquals("text2", sorted[1].uid)
        assertEquals("text3", sorted[2].uid)
    }

    @Test
    fun `sortSearchParameters should handle complex mixed scenario`() {
        // Given - a complex realistic scenario
        val parameters =
            listOf(
                createSearchParameter("firstName", TrackerInputType.TEXT, isUnique = false),
                createSearchParameter("nationalId", TrackerInputType.TEXT, isUnique = true),
                createSearchParameter("qrCode", TrackerInputType.QR_CODE, isUnique = true),
                createSearchParameter("email", TrackerInputType.EMAIL, isUnique = false),
                createSearchParameter("barCode", TrackerInputType.BAR_CODE, isUnique = false),
                createSearchParameter("uniqueCode", TrackerInputType.TEXT, isUnique = true),
                createSearchParameter("scanQr", TrackerInputType.QR_CODE, isUnique = false),
            )

        // When
        val sorted = useCase.sortSearchParameters(parameters)

        // Then - verify correct sorting priority
        assertEquals(7, sorted.size)

        // Position 0: QR_CODE unique
        assertEquals("qrCode", sorted[0].uid)
        assertTrue(sorted[0].isUnique)
        assertEquals(TrackerInputType.QR_CODE, sorted[0].inputType)

        // Position 1-2: BAR_CODE or QR_CODE non-unique
        assertTrue(useCase.isQrCodeOrBarCode(sorted[1].inputType))
        assertTrue(useCase.isQrCodeOrBarCode(sorted[2].inputType))

        // Position 3-4: Other unique parameters
        assertTrue(sorted[3].isUnique)
        assertTrue(sorted[4].isUnique)
        assertFalse(useCase.isQrCodeOrBarCode(sorted[3].inputType))
        assertFalse(useCase.isQrCodeOrBarCode(sorted[4].inputType))

        // Position 5-6: Non-unique, non-QR/BarCode
        assertFalse(sorted[5].isUnique)
        assertFalse(sorted[6].isUnique)
        assertFalse(useCase.isQrCodeOrBarCode(sorted[5].inputType))
        assertFalse(useCase.isQrCodeOrBarCode(sorted[6].inputType))
    }

    // Helper function to create SearchParameterModel for testing
    private fun createSearchParameter(
        uid: String,
        inputType: TrackerInputType,
        isUnique: Boolean,
    ): SearchParameterModel =
        SearchParameterModel(
            uid = uid,
            label = "Label for $uid",
            inputType = inputType,
            optionSet = null,
            customIntentUid = null,
            minCharactersToSearch = null,
            searchOperator = SearchOperator.LIKE,
            isUnique = isUnique,
        )
}
