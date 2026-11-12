package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.FormPluginTarget
import org.hisp.dhis.android.core.D2
import kotlin.coroutines.cancellation.CancellationException

/** The form configuration a data entry form looks up its plugins by, and where it is being filled. */
data class FormPluginContext(
    val formUid: String,
    val orgUnitUid: String?,
)

/**
 * Resolves which `capture/dataEntryForms` entry applies to a form. As in the web Capture app, an
 * event form is configured by its program stage and a registration form by its program.
 */
class FormPluginContextRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    suspend fun context(target: FormPluginTarget): FormPluginContext? =
        withContext(dispatcher.io) {
            try {
                when (target) {
                    is FormPluginTarget.Event ->
                        d2
                            .eventModule()
                            .events()
                            .uid(target.eventUid)
                            .blockingGet()
                            ?.let { event -> event.programStage()?.let { FormPluginContext(it, event.organisationUnit()) } }

                    is FormPluginTarget.Enrollment ->
                        d2
                            .enrollmentModule()
                            .enrollments()
                            .uid(target.enrollmentUid)
                            .blockingGet()
                            ?.let { enrollment -> enrollment.program()?.let { FormPluginContext(it, enrollment.organisationUnit()) } }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                null
            }
        }
}
