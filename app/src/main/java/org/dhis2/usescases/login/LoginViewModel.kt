package org.dhis2.usescases.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.UserManager
import java.io.File

const val VERSION = "version"

class LoginViewModel(
    private val view: LoginContracts.View,
    private val resourceManager: ResourceManager,
    private var userManager: UserManager?,
) : ViewModel() {
    fun onImportDataBase(file: File) {
        userManager?.let {
            viewModelScope.launch {
                val resultJob =
                    async {
                        try {
                            val importedMetadata =
                                it.d2
                                    .maintenanceModule()
                                    .databaseImportExport()
                                    .importDatabase(file)
                            Result.success(importedMetadata)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }

                val result = resultJob.await()

                result.fold(
                    onSuccess = {
                        view.displayMessage(resourceManager.getString(R.string.importing_successful))
                    },
                    onFailure = {
                        view.displayMessage(resourceManager.parseD2Error(it))
                    },
                )
            }
        }
    }
}
