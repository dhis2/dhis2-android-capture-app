package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.login.main.domain.model.ServerValidationResult

interface LoginRepository {
    suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult
}
