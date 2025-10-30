package org.dhis2.usescases.main.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.ui.icons.imagevectors.Form
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

class ConfigureHomeNavigationBar(
    private val homeRepository: HomeRepository,
    private val resourceManager: ResourceManager,
) : UseCase<Unit, List<NavigationBarItem<NavigationPage>>> {
    override suspend operator fun invoke(input: Unit) =
        try {
            val list =
                buildList {
                    add(
                        NavigationBarItem(
                            id = NavigationPage.PROGRAMS,
                            icon = Icons.Filled.Form,
                            label = resourceManager.getString(R.string.navigation_programs),
                        ),
                    )
                    if (homeRepository.hasHomeAnalytics()) {
                        add(
                            NavigationBarItem(
                                id = NavigationPage.ANALYTICS,
                                icon = Icons.Filled.BarChart,
                                label = resourceManager.getString(R.string.navigation_charts),
                            ),
                        )
                    }
                }
            Result.success(list)
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
