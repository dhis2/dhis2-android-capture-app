package org.dhis2.usescases.login

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.sync.WAS_INITIAL_SYNC_DONE
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

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
            userManager.d2.dataStoreModule().localDataStore().value(WAS_INITIAL_SYNC_DONE)
        ) doReturn mock()
        whenever(
            userManager.d2.dataStoreModule().localDataStore().value(WAS_INITIAL_SYNC_DONE)
                .blockingExists()
        ) doReturn false

        val result = interactor.execute()

        assert(!result)
    }
}
