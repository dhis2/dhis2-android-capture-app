package org.dhis2.usescases.settings

import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.workManager.WorkManagerController
import java.io.File

class  DeleteUserData(
    private val workManagerController: WorkManagerController,
    private val filterManager: FilterManager,
    private val preferencesProvider: PreferenceProvider,
    private val dispatcherProvider: DispatcherProvider,
) {
     suspend fun wipeCacheAndPreferences(file: File?) {
        withContext(dispatcherProvider.ui()) {
            filterManager.clearAllFilters()
        }
        workManagerController.cancelAllWork()
        workManagerController.pruneWork()
        if (file != null) {
            deleteCache(file)
        }
        preferencesProvider.clear()
    }
}
