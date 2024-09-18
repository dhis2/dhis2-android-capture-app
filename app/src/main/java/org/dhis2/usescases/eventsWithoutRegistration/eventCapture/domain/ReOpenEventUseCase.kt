package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error

class ReOpenEventUseCase(
    private val dispatcher: DispatcherProvider,
    private val d2: D2,
) {
    suspend operator fun invoke(
        eventUid: String,
    ): Result<Unit> = withContext(dispatcher.io()) {
        try {
            d2.eventModule().events().uid(eventUid).setStatus(EventStatus.ACTIVE)
            Result.success(Unit)
        } catch (error: D2Error) {
            Result.failure(error)
        }
    }
}
