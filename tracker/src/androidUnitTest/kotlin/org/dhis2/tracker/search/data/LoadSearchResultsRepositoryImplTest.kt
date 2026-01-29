package org.dhis2.tracker.search.data

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.data.FilterPresenter
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LoadSearchResultsRepositoryImplTest {
    private lateinit var repository: SearchTrackedEntityRepositoryImpl

    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val filterPresenter: FilterPresenter = mock()

    private val teType = "personType"
    private val programUid = "programUid"

    @Before
    fun setup() {
        repository =
            SearchTrackedEntityRepositoryImpl(
                d2 = d2,
                filterPresenter = filterPresenter,
            )
    }

    @Test
    fun `isTETypeAttribute should return true when attribute belongs to teType`() =
        runTest {
            // Given
            val attributeUid = "attr1"
            val mockOneRepository: ReadOnlyOneObjectRepositoryFinalImpl<TrackedEntityTypeAttribute> = mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid(),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid()
                    .eq(attributeUid),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid()
                    .eq(attributeUid)
                    .one(),
            ) doReturn mockOneRepository

            whenever(mockOneRepository.blockingExists()) doReturn true

            // When
            val result = repository.isTETypeAttribute(teType, attributeUid)

            // Then
            assertTrue(result)
        }

    @Test
    fun `isTETypeAttribute should return false when attribute does not belong to teType`() =
        runTest {
            // Given
            val attributeUid = "attr1"
            val mockOneRepository: ReadOnlyOneObjectRepositoryFinalImpl<TrackedEntityTypeAttribute> = mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid(),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid()
                    .eq(attributeUid),
            ) doReturn mock()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid()
                    .eq(teType)
                    .byTrackedEntityAttributeUid()
                    .eq(attributeUid)
                    .one(),
            ) doReturn mockOneRepository

            whenever(mockOneRepository.blockingExists()) doReturn false

            // When
            val result = repository.isTETypeAttribute(teType, attributeUid)

            // Then
            assertFalse(result)
        }

    @Test
    fun `getTEAttribute should return unique attribute properties`() =
        runTest {
            // Given
            val attributeUid = "uniqueAttr"
            val attribute =
                TrackedEntityAttribute
                    .builder()
                    .uid(attributeUid)
                    .unique(true)
                    .optionSet(null)
                    .build()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(attributeUid)
                    .blockingGet(),
            ) doReturn attribute

            // When
            val result = repository.getTEAttribute(attributeUid)

            // Then
            assertTrue(result.isUnique)
            assertFalse(result.isOptionSet)
        }

    @Test
    fun `getTEAttribute should return option set attribute properties`() =
        runTest {
            // Given
            val attributeUid = "optionSetAttr"

            val attribute =
                TrackedEntityAttribute
                    .builder()
                    .uid(attributeUid)
                    .unique(false)
                    .optionSet(
                        org.hisp.dhis.android.core.common.ObjectWithUid
                            .create("optionSetUid"),
                    ).build()

            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(attributeUid)
                    .blockingGet(),
            ) doReturn attribute

            // When
            val result = repository.getTEAttribute(attributeUid)

            // Then
            assertFalse(result.isUnique)
            assertTrue(result.isOptionSet)
        }

    @Test
    fun `addToQuery should add filter with multiple values using in`() =
        runTest {
            // Given
            val dataId = "attr1"
            val dataValues = listOf("value1", "value2", "value3")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            // Initialize query first
            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.addToQuery(
                dataId = dataId,
                dataValues = dataValues,
                isUnique = false,
                isOptionSet = false,
            )

            // Then - verify the filter was applied
            verify(mockQuery).byFilter(dataId)
        }

    @Test
    fun `addToQuery should add filter with exact match for unique attribute`() =
        runTest {
            // Given
            val dataId = "uniqueAttr"
            val dataValues = listOf("exactValue")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            // Initialize query first
            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.addToQuery(
                dataId = dataId,
                dataValues = dataValues,
                isUnique = true,
                isOptionSet = false,
            )

            // Then - verify the filter was applied
            verify(mockQuery).byFilter(dataId)
        }

    @Test
    fun `addToQuery should add filter with exact match for option set attribute`() =
        runTest {
            // Given
            val dataId = "optionSetAttr"
            val dataValues = listOf("optionValue")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            // Initialize query first
            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.addToQuery(
                dataId = dataId,
                dataValues = dataValues,
                isUnique = false,
                isOptionSet = true,
            )

            // Then - verify the filter was applied
            verify(mockQuery).byFilter(dataId)
        }

    @Test
    fun `addToQuery should add filter with like for regular attribute`() =
        runTest {
            // Given
            val dataId = "regularAttr"
            val dataValues = listOf("searchValue")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            // Initialize query first
            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.addToQuery(
                dataId = dataId,
                dataValues = dataValues,
                isUnique = false,
                isOptionSet = false,
            )

            // Then - verify the filter was applied
            verify(mockQuery).byFilter(dataId)
        }

    @Test
    fun `addToQuery should handle legacy option set regex format`() =
        runTest {
            // Given
            val dataId = "attr1"
            val dataValues = listOf("label_os_actualValue")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            // Initialize query first
            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.addToQuery(
                dataId = dataId,
                dataValues = dataValues,
                isUnique = false,
                isOptionSet = false,
            )

            // Then - verify the filter was applied
            verify(mockQuery).byFilter(dataId)
        }

    @Test
    fun `addFiltersToQuery should initialize query with filter presenter`() =
        runTest {
            // Given
            val mockQuery: TrackedEntitySearchCollectionRepository = mock()
            whenever(filterPresenter.filteredTrackedEntityInstances(programUid, teType)) doReturn mockQuery

            // When
            repository.addFiltersToQuery(programUid, teType)

            // Then
            verify(filterPresenter).filteredTrackedEntityInstances(programUid, teType)
        }

    @Test
    fun `excludeValuesFromQuery should add exclude filter`() =
        runTest {
            // Given
            val excludeValues = listOf("uid1", "uid2", "uid3")
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.excludeValuesFromQuery(excludeValues)

            // Then
            verify(mockQuery).excludeUids()
        }

    @Test
    fun `fetchResults should return offline only when offline or has state filters`() =
        runTest {
            // Given
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            val result =
                repository.fetchResults(
                    isOnline = false,
                    hasStateFilters = false,
                    allowCache = true,
                )

            // Then
            assertNotNull(result)
            verify(mockQuery).allowOnlineCache()
        }

    @Test
    fun `fetchResults should return offline first when online and no state filters`() =
        runTest {
            // Given
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            val result =
                repository.fetchResults(
                    isOnline = true,
                    hasStateFilters = false,
                    allowCache = false,
                )

            // Then
            assertNotNull(result)
            verify(mockQuery).allowOnlineCache()
        }

    @Test
    fun `fetchResults should use allowCache parameter correctly`() =
        runTest {
            // Given
            val mockQuery: TrackedEntitySearchCollectionRepository = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

            whenever(filterPresenter.filteredTrackedEntityInstances(any(), any())) doReturn mockQuery

            repository.addFiltersToQuery(programUid, teType)

            // When
            repository.fetchResults(
                isOnline = true,
                hasStateFilters = false,
                allowCache = true,
            )

            // Then
            verify(mockQuery).allowOnlineCache()
        }

    @Test(expected = IllegalStateException::class)
    fun `fetchResults should throw exception when query not initialized`() =
        runTest {
            // Given - no query initialized

            // When
            repository.fetchResults(
                isOnline = false,
                hasStateFilters = false,
                allowCache = true,
            )

            // Then - exception is thrown
        }
}
