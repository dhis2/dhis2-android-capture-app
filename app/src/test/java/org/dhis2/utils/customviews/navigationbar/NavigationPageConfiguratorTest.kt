package org.dhis2.utils.customviews.navigationbar

import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationPageConfiguratorTest {

    private class DefaultPageConfigurator : NavigationPageConfigurator

    @Test
    fun `default implementation should always return false`() {
        val defaultPageConfigurator = DefaultPageConfigurator()
        NavigationPage.values().forEach {
            assertTrue(
                !defaultPageConfigurator.pageVisibility(it.id),
            )
        }
    }
}
