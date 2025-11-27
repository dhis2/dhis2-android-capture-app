package org.dhis2.usescases.settings.domain

import androidx.lifecycle.MutableLiveData
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.service.VersionRepository

class CheckVersionUpdate(
    private val versionRepository: VersionRepository,
    private val settingsMessages: SettingsMessages,
    private val resourceManager: ResourceManager,
) {
    private val updatesLoading = MutableLiveData<Boolean>()

    suspend operator fun invoke() {
        updatesLoading.postValue(true)
        val newVersion = versionRepository.getLatestVersionInfo()
        if (newVersion != null) {
            versionRepository.checkVersionUpdates()
        } else {
            settingsMessages.sendMessage(resourceManager.getString(R.string.no_updates))
        }
        updatesLoading.postValue(false)
    }
}
