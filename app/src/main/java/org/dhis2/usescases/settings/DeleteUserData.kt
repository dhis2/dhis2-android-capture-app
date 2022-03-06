package org.dhis2.usescases.settings

import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2
import java.io.File
import java.lang.Exception

class DeleteUserData(
    private val workManagerController: WorkManagerController,
    private val filterManager: FilterManager,
    private val preferencesProvider: PreferenceProvider,
    private val d2: D2
) {

    fun wipeDBAndPreferences(file: File?){
            filterManager.clearAllFilters()
            workManagerController.cancelAllWork()
            workManagerController.pruneWork()
            if (file != null){
                deleteCache(file)
            }
            preferencesProvider.clear()
            d2.wipeModule().wipeEverything()
            d2.userModule().logOut().blockingAwait()
    }
}