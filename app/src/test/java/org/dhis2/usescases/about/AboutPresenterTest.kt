package org.dhis2.usescases.about

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.user.UserRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class AboutPresenterTest {

    private lateinit var aboutPresenter: AboutPresenter
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val userRepository: UserRepository = mock()
    private val aboutView: AboutView = mock()
    private val providesPresenterFactory = TrampolineSchedulerProvider()

    @Before
    fun setup() {
        aboutPresenter = AboutPresenter(
            aboutView,
            d2,
            providesPresenterFactory,
            userRepository,
        )
    }

    @Test
    fun `Should print user credentials in view`() {
        val user = User.builder()
            .uid(UUID.randomUUID().toString())
            .id(6654654)
            .username("demo@demo.es")
            .build()
        whenever(userRepository.credentials()) doReturn Flowable.just(user)
        val userName = SystemInfo.builder()
            .contextPath("https://url.es").build()
        whenever(d2.systemInfoModule().systemInfo().get()) doReturn Single.just(userName)

        aboutPresenter.init()
        verify(aboutView).renderUserCredentials(user)
        verify(aboutView).renderServerUrl(userName.contextPath())
    }

    @Test
    fun `Should clear disposable`() {
        aboutPresenter.onPause()

        assert(aboutPresenter.disposable.size() == 0)
    }
}
