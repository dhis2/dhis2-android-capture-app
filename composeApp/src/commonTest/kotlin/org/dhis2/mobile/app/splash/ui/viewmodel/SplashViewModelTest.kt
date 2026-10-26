package org.dhis2.mobile.app.splash.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.app.splash.domain.usecase.GetStartDestination
import org.dhis2.mobile.app.splash.ui.state.SplashUiState
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val getStartDestination: GetStartDestination = mock()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a start destination WHEN the splash starts THEN it is ready to navigate there`() =
        runTest {
            whenever(getStartDestination(Unit)) doReturn Result.success(StartDestination.Home)

            val viewModel = SplashViewModel(getStartDestination)

            viewModel.uiState.test {
                assertEquals(SplashUiState.Loading, awaitItem())
                assertEquals(SplashUiState.Ready(StartDestination.Home), awaitItem())
            }
        }

    @Test
    fun `GIVEN the start destination cannot be resolved WHEN the splash starts THEN it goes to login`() =
        runTest {
            whenever(getStartDestination(Unit)) doReturn Result.failure(Exception("cannot check session"))

            val viewModel = SplashViewModel(getStartDestination)

            viewModel.uiState.test {
                assertEquals(SplashUiState.Loading, awaitItem())
                assertEquals(SplashUiState.Ready(StartDestination.Login), awaitItem())
            }
        }
}
