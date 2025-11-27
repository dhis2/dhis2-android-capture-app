package org.dhis2.usescases.programEventDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.BarChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.R
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ProgramEventDetailViewModelTest {
    private var viewModel: ProgramEventDetailViewModel? = null

    private val testingDispatcher = UnconfinedTestDispatcher()

    private val dispatcher: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
        }

    @Before
    fun setup() {
        Dispatchers.setMain(testingDispatcher)

        viewModel =
            ProgramEventDetailViewModel(
                mapStyleConfig = mock(),
                eventRepository = mock(),
                dispatcher = dispatcher,
                createEventUseCase = mock(),
                pageConfigurator =
                    mock {
                        on { displayListView() } doReturn true
                        on { displayMapView() } doReturn false
                        on { displayAnalytics() } doReturn true
                    },
                resourceManager =
                    mock {
                        on { getString(R.string.navigation_list_view) } doReturn "List"
                        on { getString(R.string.navigation_charts) } doReturn "Charts"
                    },
            )

        val navBarUIState =
            viewModel
                ?.navigationBarUIState
                ?.value
        val navigationItems =
            navBarUIState
                ?.items
                .orEmpty()

        assertTrue(
            navigationItems ==
                listOf(
                    NavigationBarItem(
                        id = NavigationPage.LIST_VIEW,
                        icon = Icons.AutoMirrored.Outlined.List,
                        selectedIcon = Icons.AutoMirrored.Filled.List,
                        label = "List",
                    ),
                    NavigationBarItem(
                        id = NavigationPage.ANALYTICS,
                        icon = Icons.Outlined.BarChart,
                        selectedIcon = Icons.Filled.BarChart,
                        label = "Charts",
                    ),
                ),
        )
        assertTrue(navBarUIState?.selectedItem == NavigationPage.LIST_VIEW)
    }

    @Test
    fun changingNavigationPageShouldWorkCorrectly() {
        // given
        val navBarUIState =
            viewModel
                ?.navigationBarUIState
                ?.value

        assertTrue(navBarUIState?.selectedItem == NavigationPage.LIST_VIEW)

        // when
        viewModel?.onNavigationPageChanged(NavigationPage.ANALYTICS)

        // then
        val changedNavBarUIState =
            viewModel
                ?.navigationBarUIState
                ?.value

        assertTrue(changedNavBarUIState?.selectedItem == NavigationPage.ANALYTICS)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        viewModel = null
    }
}
