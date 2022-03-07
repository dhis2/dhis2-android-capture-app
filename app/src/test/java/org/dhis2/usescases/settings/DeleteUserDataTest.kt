package org.dhis2.usescases.settings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test

class DeleteUserDataTest {

    private lateinit var deleteUserData: DeleteUserData

    private val workManagerController: WorkManagerController = mock()
    private val filterManager: FilterManager = mock()
    private val preferencesProvider: PreferenceProvider = mock()
    private val d2: D2 = mock()

    @Before
    fun setup() {
        deleteUserData =
            DeleteUserData(workManagerController, filterManager, preferencesProvider, d2)
    }

    @Test
    fun `Should delete user data`() {
        whenever(d2.wipeModule()) doReturn mock()
        whenever(d2.userModule()) doReturn mock()
        whenever(d2.userModule().logOut()) doReturn mock()

        deleteUserData.wipeDBAndPreferences(null)

        verify(workManagerController).cancelAllWork()
        verify(workManagerController).pruneWork()
        verify(preferencesProvider).clear()
        verify(d2.wipeModule()).wipeEverything()
        verify(d2.userModule().logOut()).blockingAwait()
    }
}
