package org.dhis2.usescases.settings.domain

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CONFIRM_DELETE_LOCAL_DATA
import timber.log.Timber

class DeleteLocalData(
    private val settingsRepository: SettingsRepository,
    private val settingsMessages: SettingsMessages,
    private val resourceManager: ResourceManager,
    private val analyticsHelper: AnalyticsHelper,
) {
    suspend operator fun invoke() {
        analyticsHelper.setEvent(
            CONFIRM_DELETE_LOCAL_DATA,
            CLICK,
            CONFIRM_DELETE_LOCAL_DATA,
        )
        try {
            settingsRepository.deleteLocalData()
            settingsMessages.sendMessage(
                resourceManager.getString(
                    R.string.delete_local_data_done,
                ),
            )
        } catch (e: Exception) {
            Timber.e(e)
            settingsMessages.sendMessage(
                resourceManager.getString(
                    R.string.delete_local_data_error,
                ),
            )
        }
    }
}
