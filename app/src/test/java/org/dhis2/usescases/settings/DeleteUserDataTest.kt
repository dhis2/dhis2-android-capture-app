package org.dhis2.usescases.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteUserDataTest {
    private lateinit var deleteUserData: DeleteUserData

    private val workManagerController: WorkManagerController = mock()
    private val filterManager: FilterManager = mock()
    private val preferencesProvider: PreferenceProvider = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
        on { ui() } doReturn testingDispatcher
    }

    @Before
    fun setup() {
        deleteUserData =
            DeleteUserData(workManagerController, filterManager, preferencesProvider, dispatcherProvider)
    }

    @Test
    fun `Should delete user data`() = runTest {
        deleteUserData.wipeCacheAndPreferences(null)

        verify(workManagerController).cancelAllWork()
        verify(workManagerController).pruneWork()
        verify(preferencesProvider).clear()
    }
}
