package org.dhis2.usescases.main.domain

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.main.domain.model.DownloadMethod
import kotlin.coroutines.resume

class DownloadNewVersion(
    private val versionRepository: VersionRepository,
) : UseCase<Context, DownloadMethod> {
    override suspend fun invoke(input: Context): Result<DownloadMethod> =
        try {
            suspendCancellableCoroutine { continuation ->
                versionRepository.download(
                    context = input,
                    onDownloadCompleted = {
                        continuation.resume(Result.success(DownloadMethod.File(it)))
                    },
                    onDownloading = {
                        // no-op
                    },
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
