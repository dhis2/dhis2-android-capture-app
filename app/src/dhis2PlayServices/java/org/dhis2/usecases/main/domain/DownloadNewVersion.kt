package org.dhis2.usescases.main.domain

import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.main.domain.model.DownloadMethod

class DownloadNewVersion(
    private val versionRepository: VersionRepository,
) : UseCase<Unit, DownloadMethod> {
    override suspend fun invoke(input: Unit): Result<DownloadMethod> =
        try {
            val url = versionRepository.getUrl()
            url?.let {
                Result.success(DownloadMethod.Url(it))
            } ?: Result.failure(Exception("No url provided"))
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
