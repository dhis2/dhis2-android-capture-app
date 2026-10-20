package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.parseDataEntryForms
import org.hisp.dhis.android.core.D2
import kotlin.coroutines.cancellation.CancellationException

private const val CAPTURE_NAMESPACE = "capture"
private const val DATA_ENTRY_FORMS_KEY = "dataEntryForms"

/**
 * The Capture app's plugin configuration: the `capture` dataStore namespace the web Capture app and
 * the Tracker Plugin Configurator use, so one configuration places a plugin on both platforms.
 */
class CaptureConfigRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    /**
     * Pulls the `capture` namespace into the local dataStore. Offline-first: a failure is reported,
     * and [formFieldPlacements] still reads what a previous refresh cached.
     */
    suspend fun refresh(): Result<Unit> =
        io {
            d2
                .dataStoreModule()
                .dataStoreDownloader()
                .byNamespace()
                .eq(CAPTURE_NAMESPACE)
                .blockingDownload()
        }

    /** The form field plugins placed in any form, from the cached `capture/dataEntryForms`. */
    suspend fun formFieldPlacements(): Result<List<FormFieldPluginPlacement>> =
        io {
            val config =
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value(CAPTURE_NAMESPACE, DATA_ENTRY_FORMS_KEY)
                    .blockingGet()
                    ?.takeIf { it.deleted() != true }
                    ?.value()
            config?.let(::parseDataEntryForms).orEmpty()
        }

    private suspend fun <T> io(block: () -> T): Result<T> =
        withContext(dispatcher.io) {
            try {
                Result.success(block())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
}
