package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

class ImportDatabase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(path: String): Result<Unit> = repository.importDatabase(path)
}
