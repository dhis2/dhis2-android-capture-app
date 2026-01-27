package org.dhis2.usescases.main.domain

import android.content.Context
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.domain.model.DownloadMethod

class DownloadNewVersion(
    private val versionRepository: VersionRepository,
) : UseCase<Context, DownloadMethod> {
    override suspend fun invoke(input: Context): Result<DownloadMethod> =
        try {
            val url = versionRepository.getUrl()
            url?.let {
                Result.success(DownloadMethod.Url(it))
            } ?: Result.failure(DomainError.UnexpectedError("No url provided"))
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
