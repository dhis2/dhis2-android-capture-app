package org.dhis2.usescases.about

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.usescases.about.domain.GetAboutInfo
import org.dhis2.usescases.about.model.AboutInfo
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.state.AboutUiEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    private val getAboutInfo: GetAboutInfo = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()

    private val aboutInfo =
        AboutInfo(
            version = "3.5.0",
            buildHash = "2c28d35",
            sdkVersion = "1.15.0",
            serverUrl = "play.im.dhis2.org/stable-2-43-1",
            username = "android",
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AboutViewModel(getAboutInfo, networkStatusProvider)

    @Test
    fun `Should expose the install info when it can be read`() =
        runTest {
            whenever(getAboutInfo(Unit)) doReturn Result.success(aboutInfo)

            val viewModel = viewModel()

            assertEquals(aboutInfo, viewModel.uiState.value.info)
        }

    @Test
    fun `Should have no install info when it cannot be read`() =
        runTest {
            whenever(getAboutInfo(Unit)) doReturn Result.failure(Exception("Boom"))

            val viewModel = viewModel()

            assertNull(viewModel.uiState.value.info)
        }

    @Test
    fun `Should open an external link when there is a connection`() =
        runTest {
            whenever(getAboutInfo(Unit)) doReturn Result.success(aboutInfo)
            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            val viewModel = viewModel()

            viewModel.uiEvent.test {
                viewModel.onLinkClicked(AboutLink.SOURCE_CODE)

                assertEquals(AboutUiEvent.OpenLink(AboutLink.SOURCE_CODE), awaitItem())
            }
        }

    @Test
    fun `Should show the no connection message when an external link is clicked offline`() =
        runTest {
            whenever(getAboutInfo(Unit)) doReturn Result.success(aboutInfo)
            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(false)
            val viewModel = viewModel()

            viewModel.uiEvent.test {
                viewModel.onLinkClicked(AboutLink.SUPPORTED_FEATURES)

                assertEquals(AboutUiEvent.ShowNoConnection, awaitItem())
            }
        }

    @Test
    fun `Should open the privacy policy even when there is no connection`() =
        runTest {
            whenever(getAboutInfo(Unit)) doReturn Result.success(aboutInfo)
            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(false)
            val viewModel = viewModel()

            viewModel.uiEvent.test {
                viewModel.onLinkClicked(AboutLink.PRIVACY_POLICY)

                assertEquals(AboutUiEvent.OpenLink(AboutLink.PRIVACY_POLICY), awaitItem())
            }
        }
}
