package org.dhis2.usescases.main.domain

import android.content.Context
import org.dhis2.data.service.ApkUpdateRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.main.domain.model.DownloadMethod
import kotlin.coroutines.cancellation.CancellationException

class DownloadNewVersion(
    private val apkUpdateRepository: ApkUpdateRepository,
) : UseCase<Context, DownloadMethod> {
    override suspend fun invoke(input: Context): Result<DownloadMethod> =
        try {
            apkUpdateRepository.downloadLatestVersion()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
}
