package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.FormPluginTarget
import org.hisp.dhis.android.core.D2
import kotlin.coroutines.cancellation.CancellationException

/** The stored values of a form's fields: option codes and raw values, not what the form displays. */
class PluginFieldValueRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    suspend fun storedValues(
        target: FormPluginTarget,
        uids: List<String>,
    ): Map<String, String?> =
        withContext(dispatcher.io) {
            try {
                when (target) {
                    is FormPluginTarget.Event ->
                        d2
                            .trackedEntityModule()
                            .trackedEntityDataValues()
                            .byEvent()
                            .eq(target.eventUid)
                            .byDataElement()
                            .`in`(uids)
                            .blockingGet()
                            .associate { it.dataElement().orEmpty() to it.value() }

                    is FormPluginTarget.Enrollment -> {
                        val trackedEntity =
                            d2
                                .enrollmentModule()
                                .enrollments()
                                .uid(target.enrollmentUid)
                                .blockingGet()
                                ?.trackedEntityInstance()
                        trackedEntity
                            ?.let {
                                d2
                                    .trackedEntityModule()
                                    .trackedEntityAttributeValues()
                                    .byTrackedEntityInstance()
                                    .eq(it)
                                    .byTrackedEntityAttribute()
                                    .`in`(uids)
                                    .blockingGet()
                                    .associate { value -> value.trackedEntityAttribute().orEmpty() to value.value() }
                            }.orEmpty()
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                emptyMap()
            }
        }
}
