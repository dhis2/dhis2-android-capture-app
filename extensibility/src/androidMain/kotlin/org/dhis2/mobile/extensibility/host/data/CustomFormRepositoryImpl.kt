package org.dhis2.mobile.extensibility.host.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FormType

/**
 * Reads the custom form the SDK stored with the data set's metadata (B2). Nothing to refresh
 * after sync: it is read every time the data set opens, by uid, so only one form's HTML is loaded.
 */
class CustomFormRepositoryImpl(
    private val d2: D2,
    private val io: CoroutineDispatcher,
) : CustomFormRepository {
    override suspend fun customFormHtml(dataSetUid: String): String? =
        withContext(io) {
            val dataSet =
                d2
                    .dataSetModule()
                    .dataSets()
                    .uid(dataSetUid)
                    .blockingGet()
            if (dataSet?.formType() != FormType.CUSTOM) return@withContext null
            // CUSTOM without a form: returned as "" so the host records Failed(EMPTY_CONTENT).
            val formUid = dataSet.dataEntryForm()?.uid() ?: return@withContext ""
            d2
                .dataSetModule()
                .dataEntryForms()
                .uid(formUid)
                .blockingGet()
                ?.htmlCode()
                .orEmpty()
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
