package org.dhis2.usescases.settings.domain

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase

class CheckVersionUpdate(
    private val versionRepository: VersionRepository,
    private val settingsMessages: SettingsMessages,
    private val resourceManager: ResourceManager,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> = try {
        val newVersion = versionRepository.downloadLatestVersionInfo()
        if (newVersion == null) {
            settingsMessages.sendMessage(resourceManager.getString(R.string.no_updates))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
