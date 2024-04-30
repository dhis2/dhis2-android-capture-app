package org.dhis2.usescases.settings

import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DeleteUserDataTest {

    private lateinit var deleteUserData: DeleteUserData

    private val workManagerController: WorkManagerController = mock()
    private val filterManager: FilterManager = mock()
    private val preferencesProvider: PreferenceProvider = mock()

    @Before
    fun setup() {
        deleteUserData =
            DeleteUserData(workManagerController, filterManager, preferencesProvider)
    }

    @Test
    fun `Should delete user data`() {
        deleteUserData.wipeCacheAndPreferences(null)

        verify(workManagerController).cancelAllWork()
        verify(workManagerController).pruneWork()
        verify(preferencesProvider).clear()
    }
}
