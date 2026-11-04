package org.dhis2.data.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dhis2.BuildConfig
import org.dhis2.bindings.newVersion
import org.hisp.dhis.android.core.D2

class VersionRepository(
    val d2: D2,
) {
    private val _newAppVersion = MutableSharedFlow<String?>(replay = 1)
    val newAppVersion: SharedFlow<String?> get() = _newAppVersion

    suspend fun downloadLatestVersionInfo(): String? {
        d2.settingModule().latestAppVersion().blockingDownload()
        return checkVersionUpdates()
    }

    private fun getLatestVersionInfo(): String? =
        d2
            .settingModule()
            .latestAppVersion()
            .blockingGet()
            ?.version()
            .takeIf { it?.newVersion(BuildConfig.VERSION_NAME) ?: false }

    private suspend fun checkVersionUpdates(): String? {
        val versionNameOrNull = getLatestVersionInfo()
        _newAppVersion.emit(versionNameOrNull)
        return versionNameOrNull
    }

    fun getUrl(): String? =
        d2
            .settingModule()
            .latestAppVersion()
            .blockingGet()
            ?.downloadURL()

    fun removeVersionInfo() {
        _newAppVersion.tryEmit("")
    }
}
