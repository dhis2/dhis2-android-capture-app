package org.dhis2.usescases.settings

import java.io.File
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController

class DeleteUserData(
    private val workManagerController: WorkManagerController,
    private val filterManager: FilterManager,
    private val preferencesProvider: PreferenceProvider
) {

    fun wipeCacheAndPreferences(file: File?) {
        filterManager.clearAllFilters()
        workManagerController.cancelAllWork()
        workManagerController.pruneWork()
        if (file != null) {
            deleteCache(file)
        }
        preferencesProvider.clear()
    }
}
