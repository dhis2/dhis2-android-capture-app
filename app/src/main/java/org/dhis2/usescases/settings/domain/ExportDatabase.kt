package org.dhis2.usescases.settings.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.usescases.settings.SettingsRepository
import java.io.File

class ExportDatabase(
    private val settingsRepository: SettingsRepository,
    private val fileHandler: FileHandler,
    private val settingsMessages: SettingsMessages,
    private val resourceManager: ResourceManager,
) {
    private val _exporting = MutableLiveData<Boolean>()
    val exporting: LiveData<Boolean> = _exporting

    sealed interface ExportType {
        data object Download : ExportType

        data object Share : ExportType
    }

    sealed interface ExportResult {
        data object Success : ExportResult

        data class Share(
            val db: File,
        ) : ExportResult

        data object Error : ExportResult
    }

    suspend operator fun invoke(exportType: ExportType = ExportType.Download): ExportResult {
        _exporting.postValue(true)
        return try {
            val db = settingsRepository.exportDatabase()
            fileHandler.copyAndOpen(db) {}
            when (exportType) {
                ExportType.Download -> {
                    settingsMessages.sendMessage(resourceManager.getString(R.string.database_export_downloaded))
                    ExportResult.Success
                }

                ExportType.Share ->
                    ExportResult.Share(db)
            }
        } catch (e: Exception) {
            settingsMessages.sendMessage(resourceManager.parseD2Error(e) ?: "")
            ExportResult.Error
        } finally {
            _exporting.postValue(false)
        }
    }
}
