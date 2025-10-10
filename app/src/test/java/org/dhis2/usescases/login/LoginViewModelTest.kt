package org.dhis2.usescases.login
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.UserManager
import org.dhis2.utils.MainCoroutineScopeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

class LoginViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private lateinit var loginViewModel: LoginViewModel
    private val resourceManager: ResourceManager = mock()
    private val testingDispatcher = StandardTestDispatcher()

    private fun instantiateLoginViewModel() {
        loginViewModel =
            LoginViewModel(
                view,
                resourceManager,
                userManager,
            )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @Test
    fun `Should show empty credentials message when trying to log in with fingerprint`() {
        instantiateLoginViewModel()
    }
}
