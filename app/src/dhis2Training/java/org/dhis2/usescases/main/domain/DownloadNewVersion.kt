package org.dhis2.usescases.main.domain

import android.content.Context
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.domain.model.DownloadMethod

class DownloadNewVersion : UseCase<Context, DownloadMethod> {
    override suspend fun invoke(input: Context): Result<DownloadMethod> =
        Result.failure(DomainError.UnexpectedError("Version download is not supported in Training flavor"))
}
