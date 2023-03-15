package org.dhis2.usescases.sync

import org.dhis2.usescases.general.AbstractActivityContracts

interface SyncView : AbstractActivityContracts.View {
    fun setServerTheme(themeId: Int)
    fun setFlag(flagName: String?)
    fun goToLogin()
    fun setMetadataSyncStarted()
    fun setMetadataSyncSucceed()
    fun showMetadataFailedMessage(message: String?)
    fun goToMain()
}
