package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable

class CreateOrUpdateEventDetails(
    private val repository: EventDetailsRepository
) {

    operator fun invoke(
        selectedDate: Date,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        coordinates: String?
    ): Flow<Boolean> {
        repository.getEvent()?.let {
            if (repository.getEditableStatus() is Editable) {
                repository.updateEvent(
                    selectedDate,
                    selectedOrgUnit,
                    catOptionComboUid,
                    coordinates
                )
                return flowOf(true)
            }
        }

        return flowOf(false)
    }
}
