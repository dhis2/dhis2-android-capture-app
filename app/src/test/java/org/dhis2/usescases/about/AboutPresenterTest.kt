package org.dhis2.usescases.about

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.user.UserRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.UserCredentials
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class AboutPresenterTest {

    private lateinit var presenter: AboutPresenterImpl

    private val view: AboutContracts.AboutView = mock()
    private val d2: D2 =  Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val scheduler = TrampolineSchedulerProvider()
    private val userRepository: UserRepository = mock()


    @Before
    fun setUp() {
        presenter = AboutPresenterImpl(d2, userRepository, scheduler)
    }

    @Test
    fun `Should render user credentials and server url`() {
        val userCredentials = UserCredentials.builder()
            .uid("userCredential")
            .username("userName")
            .build()
        val systemInfo = SystemInfo.builder().contextPath("url").build()

        whenever(userRepository.credentials()) doReturn Flowable.just(userCredentials)
        whenever(d2.systemInfoModule().systemInfo().get()) doReturn Single.just(systemInfo)

        presenter.init(view)

        verify(view).renderUserCredentials(userCredentials)
        verify(view).renderServerUrl("url")
    }

    @Test
    fun `Should clear disposable`() {
        presenter.onPause()

        assert(presenter.compositeDisposable.size() == 0)
    }
}