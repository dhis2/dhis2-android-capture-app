package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class ConfigureHomeNavigationBarTest {

    private val homeRepository: HomeRepository = mock()
    private val resourceManager: ResourceManager = mock()
    lateinit var configureHomeNavigationBar: ConfigureHomeNavigationBar

    @Before
    fun setUp() {
        whenever(resourceManager.getString(R.string.navigation_programs)) doReturn "programs"
        whenever(resourceManager.getString(R.string.navigation_charts)) doReturn "analytics"
        configureHomeNavigationBar = ConfigureHomeNavigationBar(
            homeRepository = homeRepository,
            resourceManager = resourceManager,
        )
    }

    @Test
    fun `should return programs and analytics items if configured`() = runTest {
        whenever(homeRepository.hasHomeAnalytics()) doReturn true
        with(configureHomeNavigationBar()) {
            assertTrue(isSuccess)
            assertTrue(getOrNull()?.size == 2)
        }
    }

    @Test
    fun `should return just programs if analytics is not configured`() = runTest {
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        with(configureHomeNavigationBar()) {
            assertTrue(isSuccess)
            assertTrue(getOrNull()?.size == 1)
        }
    }

    @Test
    fun `should return just programs if an exception is thrown`() = runTest {
        given(homeRepository.hasHomeAnalytics()) willAnswer { throw DomainError.DataBaseError("Test") }
        with(configureHomeNavigationBar()) {
            assertTrue(isSuccess)
            assertTrue(getOrNull()?.size == 1)
        }
    }

}