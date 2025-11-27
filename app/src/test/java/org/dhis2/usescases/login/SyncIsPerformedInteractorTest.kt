package org.dhis2.usescases.login

import org.dhis2.data.server.UserManager
import org.dhis2.usescases.sync.WAS_INITIAL_SYNC_DONE
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncIsPerformedInteractorTest {
    private lateinit var interactor: SyncIsPerformedInteractor
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setup() {
        interactor = SyncIsPerformedInteractor(userManager)
    }

    @Test
    fun `Should check if entry exists on datastore module`() {
        whenever(userManager.d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            userManager.d2
                .dataStoreModule()
                .localDataStore()
                .value(WAS_INITIAL_SYNC_DONE),
        ) doReturn mock()
        whenever(
            userManager.d2
                .dataStoreModule()
                .localDataStore()
                .value(WAS_INITIAL_SYNC_DONE)
                .blockingExists(),
        ) doReturn false
        whenever(
            userManager.d2
                .userModule()
                .accountManager()
                .getCurrentAccount(),
        ) doReturn null

        val result = interactor.execute()

        assert(!result)
    }

    @Test
    fun `Should check if import exists on datastore module`() {
        val serverUrl = "https://play.dhis2.org/40"
        val userName = "pepe"

        whenever(userManager.d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            userManager.d2
                .dataStoreModule()
                .localDataStore()
                .value(WAS_INITIAL_SYNC_DONE),
        ) doReturn mock()
        whenever(
            userManager.d2
                .dataStoreModule()
                .localDataStore()
                .value(WAS_INITIAL_SYNC_DONE)
                .blockingExists(),
        ) doReturn false

        val mockedSystemInfo: SystemInfo =
            mock {
                on { contextPath() } doReturn serverUrl
            }

        val mockedUser: User =
            mock {
                on { username() } doReturn userName
            }

        whenever(
            userManager.d2
                .userModule()
                .user()
                .blockingGet(),
        ) doReturn mockedUser

        whenever(
            userManager.d2
                .systemInfoModule()
                .systemInfo()
                .blockingGet(),
        ) doReturn mockedSystemInfo

        val mockedAccount: DatabaseAccount =
            mock {
                on { serverUrl() } doReturn serverUrl
                on { username() } doReturn userName
                on { importDB() } doReturn mock()
            }

        whenever(
            userManager.d2
                .userModule()
                .accountManager()
                .getCurrentAccount(),
        ) doReturn mockedAccount

        val result = interactor.execute()

        assert(result)
    }
}
