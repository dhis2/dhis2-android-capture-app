package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import java.util.Date

class CreateOrUpdateEventDetails(
    private val repository: EventDetailsRepository,
    private val resourcesProvider: EventDetailResourcesProvider,
) {

    operator fun invoke(
        selectedDate: Date,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        coordinates: String?,
    ): Flow<Result<String>> {
        repository.getEvent()?.let {
            if (repository.getEditableStatus() is Editable) {
                repository.updateEvent(
                    selectedDate,
                    selectedOrgUnit,
                    catOptionComboUid,
                    coordinates,
                )
                return flowOf(Result.success(resourcesProvider.provideEventCreatedMessage()))
            }
        }

        return flowOf(Result.failure(Throwable(resourcesProvider.provideEventCreationError())))
    }
}
