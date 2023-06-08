package org.dhis2.usescases.programEventDetail

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProgramEventDetailPageConfiguratorTest {

    private val repository: ProgramEventDetailRepository = mock()
    private val pageConfigurator: NavigationPageConfigurator =
        ProgramEventPageConfigurator(repository)

    @Test
    fun `Should display the map screen if configured`() {
        whenever(repository.programHasCoordinates()) doReturn true
        assertTrue(pageConfigurator.displayMapView())
    }

    @Test
    fun `Should not display the map screen if configured`() {
        whenever(repository.programHasCoordinates()) doReturn false
        assertFalse(pageConfigurator.displayMapView())
    }
}
