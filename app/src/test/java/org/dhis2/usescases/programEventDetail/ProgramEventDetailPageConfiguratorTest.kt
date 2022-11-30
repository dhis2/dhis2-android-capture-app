package org.dhis2.usescases.programEventDetail

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
