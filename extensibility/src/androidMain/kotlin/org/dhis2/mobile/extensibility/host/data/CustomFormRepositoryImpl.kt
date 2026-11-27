package org.dhis2.mobile.extensibility.host.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2

/**
 * Reads the custom form the SDK stored with the data set's metadata (B2). Nothing to refresh
 * after sync: it is read every time the data set opens, by uid, so only one form's HTML is loaded.
 */
class CustomFormRepositoryImpl(
    private val d2: D2,
    private val io: CoroutineDispatcher,
) : CustomFormRepository {
    override suspend fun customFormHtml(dataSetUid: String): String? {
        // TODO: to be implemented in ANDROAPP-8005
        return null
    }

    override suspend fun isSecureServer(): Boolean =
        withContext(io) {
            d2
                .systemInfoModule()
                .systemInfo()
                .blockingGet()
                ?.contextPath()
                ?.startsWith("https://", ignoreCase = true) == true
        }
}
