package org.dhis2.usescases.login
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LoginViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
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
                preferenceProvider,
                resourceManager,
                schedulers,
                userManager,
            )
    }

    private fun instantiateLoginViewModelWithNullUserManager() {
        loginViewModel =
            LoginViewModel(
                view,
                preferenceProvider,
                resourceManager,
                schedulers,
                null,
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

    @Test
    fun `Should handle log out when button is clicked`() {
        instantiateLoginViewModel()
        whenever(userManager.d2.userModule().logOut()) doReturn Completable.complete()
        loginViewModel.logOut()
        verify(view).handleLogout()
    }

    @Test
    fun `Should invoke openIdLogin method successfully`() {
        instantiateLoginViewModelWithNullUserManager()
        val openidconfig: OpenIDConnectConfig = mock()
        val it: IntentWithRequestCode = mock()
        whenever(view.initLogin()) doReturn userManager
        whenever(userManager.logIn(openidconfig)) doReturn Observable.just(it)
        instantiateLoginViewModelWithNullUserManager()
        loginViewModel.openIdLogin(openidconfig)
        verify(view).openOpenIDActivity(it)
    }
}
