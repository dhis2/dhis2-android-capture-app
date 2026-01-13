package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class GetHomeFiltersTest {
    private val filterRepository: FilterRepository = mock()
    private lateinit var getHomeFilters: GetHomeFilters

    @Before
    fun setUp() {
        getHomeFilters = GetHomeFilters(filterRepository)
    }

    @Test
    fun `should return a list of home filters`() =
        runTest {
            val expectedFilters: List<FilterItem> = mock()
            whenever(filterRepository.homeFilters()) doReturn expectedFilters
            val result = getHomeFilters()
            assert(result.isSuccess)
            assert(result.getOrNull() == expectedFilters)
        }

    @Test
    fun `should return a failure when an exception is thrown`() =
        runTest {
            given(filterRepository.homeFilters()) willAnswer {
                throw DomainError.DataBaseError("Test")
            }
            val result = getHomeFilters()
            assert(result.isFailure)
        }
}
