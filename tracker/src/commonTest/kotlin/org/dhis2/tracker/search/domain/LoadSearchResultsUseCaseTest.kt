package org.dhis2.tracker.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.data.SearchTrackedEntityRepository
import org.dhis2.tracker.search.model.SearchTrackedEntitiesInput
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LoadSearchResultsUseCaseTest {
    private lateinit var useCase: SearchTrackedEntities

    private val repository: SearchTrackedEntityRepository = mock()
    private val customIntentRepository: CustomIntentRepository = mock()
    private val teType = "personType"

    @Before
    fun setup() {
        useCase =
            SearchTrackedEntities(
                repository = repository,
                customIntentRepository = customIntentRepository,
                teType = teType,
            )
    }

    @Test
    fun `invoke should successfully return flow when query has data`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("attr1" to listOf("value1"), "attr2" to listOf("value2"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(eq(teType), any())) doReturn true
            whenever(repository.getTEAttribute(any())) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = false,
                    isOptionSet = false,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    any(),
                    eq(CustomIntentActionTypeModel.SEARCH),
                ),
            ) doReturn false
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addFiltersToQuery("programUid", teType)
            verify(repository).addToQuery(
                dataId = "attr1",
                dataValues = listOf("value1"),
                isUnique = false,
                isOptionSet = false,
            )
            verify(repository).addToQuery(
                dataId = "attr2",
                dataValues = listOf("value2"),
                isUnique = false,
                isOptionSet = false,
            )
            verify(repository).fetchResults(
                isOnline = true,
                hasStateFilters = false,
                allowCache = true,
            )
        }

    @Test
    fun `invoke should successfully return flow when query is null`() =
        runTest {
            // Given
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = false,
                    queryData = null,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addFiltersToQuery("programUid", teType)
            verify(repository, never()).addToQuery(any(), any(), any(), any())
            verify(repository).fetchResults(
                isOnline = false,
                hasStateFilters = false,
                allowCache = true,
            )
        }

    @Test
    fun `invoke should exclude values when no program is selected`() =
        runTest {
            // Given
            val excludedValues = hashSetOf("excludedUid1", "excludedUid2")
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = null,
                    allowCache = true,
                    excludeValues = excludedValues,
                    hasStateFilters = false,
                    isOnline = false,
                    queryData = null,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).excludeValuesFromQuery(excludedValues.toList())
        }

    @Test
    fun `invoke should not exclude values when program is selected`() =
        runTest {
            // Given
            val excludedValues = hashSetOf("excludedUid1", "excludedUid2")
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = excludedValues,
                    hasStateFilters = false,
                    isOnline = false,
                    queryData = null,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository, never()).excludeValuesFromQuery(any())
        }

    @Test
    fun `invoke should add query for unique attributes`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("uniqueAttr" to listOf("exactValue"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(eq(teType), any())) doReturn true
            whenever(repository.getTEAttribute("uniqueAttr")) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = true,
                    isOptionSet = false,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    any(),
                    eq(CustomIntentActionTypeModel.SEARCH),
                ),
            ) doReturn false
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addToQuery(
                dataId = "uniqueAttr",
                dataValues = listOf("exactValue"),
                isUnique = true,
                isOptionSet = false,
            )
        }

    @Test
    fun `invoke should add query for option set attributes`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("optionSetAttr" to listOf("optionValue"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(eq(teType), any())) doReturn true
            whenever(repository.getTEAttribute("optionSetAttr")) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = false,
                    isOptionSet = true,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    any(),
                    eq(CustomIntentActionTypeModel.SEARCH),
                ),
            ) doReturn false
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addToQuery(
                dataId = "optionSetAttr",
                dataValues = listOf("optionValue"),
                isUnique = false,
                isOptionSet = true,
            )
        }

    @Test
    fun `invoke should join multiple values when custom intent does not return list`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("attr1" to listOf("value1", "value2", "value3"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(eq(teType), any())) doReturn true
            whenever(repository.getTEAttribute(any())) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = false,
                    isOptionSet = false,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    "attr1",
                    CustomIntentActionTypeModel.SEARCH,
                ),
            ) doReturn false
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addToQuery(
                dataId = "attr1",
                dataValues = listOf("value1,value2,value3"),
                isUnique = false,
                isOptionSet = false,
            )
        }

    @Test
    fun `invoke should keep multiple values when custom intent returns list`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("attr1" to listOf("value1", "value2", "value3"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(eq(teType), any())) doReturn true
            whenever(repository.getTEAttribute(any())) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = false,
                    isOptionSet = false,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    "attr1",
                    CustomIntentActionTypeModel.SEARCH,
                ),
            ) doReturn true
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addToQuery(
                dataId = "attr1",
                dataValues = listOf("value1", "value2", "value3"),
                isUnique = false,
                isOptionSet = false,
            )
        }

    @Test
    fun `invoke should skip attributes not belonging to teType when no program selected`() =
        runTest {
            // Given
            val queryData = mutableMapOf<String, List<String>?>("attr1" to listOf("value1"), "attr2" to listOf("value2"))
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = null,
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = false,
                    queryData = queryData,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.isTETypeAttribute(teType, "attr1")) doReturn true
            whenever(repository.isTETypeAttribute(teType, "attr2")) doReturn false
            whenever(repository.getTEAttribute("attr1")) doReturn
                SearchTrackedEntityAttribute(
                    isUnique = false,
                    isOptionSet = false,
                )
            whenever(
                customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                    any(),
                    eq(CustomIntentActionTypeModel.SEARCH),
                ),
            ) doReturn false
            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).addToQuery(
                dataId = "attr1",
                dataValues = listOf("value1"),
                isUnique = false,
                isOptionSet = false,
            )
            verify(repository, never()).getTEAttribute("attr2")
        }

    @Test
    fun `invoke should return failure when domain error occurs`() =
        runTest {
            // Given
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = true,
                    queryData = null,
                )

            val domainError = DomainError.UnexpectedError("Test error")

            whenever(repository.fetchResults(any(), any(), any())).thenAnswer { throw domainError }

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is DomainError.UnexpectedError)
            assertEquals("Test error", (result.exceptionOrNull() as DomainError.UnexpectedError).message)
        }

    @Test
    fun `invoke should handle online search with state filters`() =
        runTest {
            // Given
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = false,
                    excludeValues = hashSetOf(),
                    hasStateFilters = true,
                    isOnline = true,
                    queryData = null,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).fetchResults(
                isOnline = true,
                hasStateFilters = true,
                allowCache = false,
            )
        }

    @Test
    fun `invoke should handle offline search`() =
        runTest {
            // Given
            val searchParams =
                SearchTrackedEntitiesInput(
                    selectedProgram = "programUid",
                    allowCache = true,
                    excludeValues = hashSetOf(),
                    hasStateFilters = false,
                    isOnline = false,
                    queryData = null,
                )

            val mockPagingData: PagingData<TrackedEntitySearchItemResult> = PagingData.empty()
            val mockFlow: Flow<PagingData<TrackedEntitySearchItemResult>> = flowOf(mockPagingData)

            whenever(repository.fetchResults(any(), any(), any())) doReturn mockFlow

            // When
            val result = useCase.invoke(searchParams)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).fetchResults(
                isOnline = false,
                hasStateFilters = false,
                allowCache = true,
            )
        }
}
