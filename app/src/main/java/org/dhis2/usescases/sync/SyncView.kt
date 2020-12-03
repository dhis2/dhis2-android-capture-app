package org.dhis2.usescases.sync

import org.dhis2.usescases.general.AbstractActivityContracts

interface SyncView : AbstractActivityContracts.View {
    fun saveTheme(themeId: Int?)
    fun saveFlag(s: String?)
    fun goToLogin()
}