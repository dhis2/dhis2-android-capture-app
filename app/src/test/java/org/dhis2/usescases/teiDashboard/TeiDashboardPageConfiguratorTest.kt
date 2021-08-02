package org.dhis2.usescases.teiDashboard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.junit.Assert.assertTrue
import org.junit.Test

class TeiDashboardPageConfiguratorTest {
    private val dashboardRepository: DashboardRepository = mock()
    private val isPortrait = true
    private val pageConfigurator: NavigationPageConfigurator =
        TeiDashboardPageConfigurator(dashboardRepository, isPortrait)

    @Test
    fun `Should display the details screen`() {
        assertTrue(pageConfigurator.displayDetails())
    }

    @Test
    fun `Should display analytics screen if the program is configured`() {
        whenever(dashboardRepository.programHasAnalytics()) doReturn true
        assertTrue(pageConfigurator.displayAnalytics())
    }

    @Test
    fun `Should not display analytics screen if the program is configured`() {
        whenever(dashboardRepository.programHasAnalytics()) doReturn false
        assertTrue(!pageConfigurator.displayAnalytics())
    }

    @Test
    fun `Should display relationships screen if the program is configured`() {
        whenever(dashboardRepository.programHasRelationships()) doReturn true
        assertTrue(pageConfigurator.displayRelationships())
    }

    @Test
    fun `Should not display relationships screen if the program is configured`() {
        whenever(dashboardRepository.programHasRelationships()) doReturn false
        assertTrue(!pageConfigurator.displayRelationships())
    }

    @Test
    fun `Should display the notes screen`() {
        assertTrue(pageConfigurator.displayNotes())
    }
}
