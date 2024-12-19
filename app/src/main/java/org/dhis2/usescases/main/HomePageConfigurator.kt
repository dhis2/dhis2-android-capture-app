package org.dhis2.usescases.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.icons.imagevectors.Form
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

class HomePageConfigurator(
    private val homeRepository: HomeRepository,
    private val resourceManager: ResourceManager,
) : NavigationPageConfigurator {

    override fun displayPrograms(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return homeRepository.hasHomeAnalytics()
    }

    override fun navigationItems(): List<NavigationBarItem<NavigationPage>> {
        return buildList {
            add(
                NavigationBarItem(
                    id = NavigationPage.PROGRAMS,
                    icon = Icons.Filled.Form,
                    label = resourceManager.getString(R.string.navigation_programs),
                ),
            )
            if (displayAnalytics()) {
                add(
                    NavigationBarItem(
                        id = NavigationPage.ANALYTICS,
                        icon = Icons.Filled.BarChart,
                        label = resourceManager.getString(R.string.navigation_charts),
                    ),
                )
            }
        }
    }
}
