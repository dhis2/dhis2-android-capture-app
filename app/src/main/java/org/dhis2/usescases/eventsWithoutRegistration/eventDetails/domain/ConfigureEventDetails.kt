package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Date
import kotlin.jvm.Throws
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus.CANCELLED
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.event.EventStatus.OVERDUE

class ConfigureEventDetails(
    private val repository: EventDetailsRepository,
    private val resourcesProvider: EventDetailResourcesProvider,
    private val creationType: EventCreationType,
    private val enrollmentStatus: EnrollmentStatus?
) {

    operator fun invoke(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        isCatComboCompleted: Boolean,
        coordinates: String?,
        tempCreate: String?
    ): Flow<EventDetails> {
        val isEventCompleted = isCompleted(
            selectedDate = selectedDate,
            selectedOrgUnit = selectedOrgUnit,
            isCatComboCompleted = isCatComboCompleted,
            tempCreate = tempCreate
        )
        val storedEvent = repository.getEvent()
        val programStage = repository.getProgramStage()
        return flowOf(
            EventDetails(
                name = programStage.displayName(),
                description = programStage.displayDescription(),
                style = repository.getObjectStyle(),
                enabled = isEnable(storedEvent),
                isEditable = isEditable(),
                editableReason = getEditableReason(),
                temCreate = tempCreate,
                selectedDate = selectedDate,
                selectedOrgUnit = selectedOrgUnit,
                catOptionComboUid = catOptionComboUid,
                coordinates = coordinates,
                isCompleted = isEventCompleted,
                isActionButtonVisible = isActionButtonVisible(isEventCompleted, storedEvent),
                actionButtonText = getActionButtonText(),
                canReopen = getCanReopen()
            )
        )
    }

    private fun getActionButtonText(): String {
        return repository.getEditableStatus()?.let {
            when (it) {
                is Editable -> resourcesProvider.provideButtonUpdate()
                is NonEditable -> resourcesProvider.provideButtonCheck()
            }
        } ?: resourcesProvider.provideButtonNext()
    }

    private fun isActionButtonVisible(isEventCompleted: Boolean, storedEvent: Event?): Boolean {
        return storedEvent?.let {
            !(it.status() == OVERDUE && enrollmentStatus == CANCELLED) &&
                repository.getEditableStatus() !is NonEditable
        } ?: isEventCompleted
    }

    @Throws(D2Error::class)
    fun reopenEvent() {
        eventId?.let {
            d2.eventModule().events().uid(it).setStatus(EventStatus.ACTIVE)
        }
    }

    private fun isCompleted(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        isCatComboCompleted: Boolean,
        tempCreate: String?
    ) = selectedDate != null &&
        !selectedOrgUnit.isNullOrEmpty() &&
        isCatComboCompleted &&
        (creationType != REFERAL || tempCreate != null)

    private fun isEditable(): Boolean {
        return getEditableReason() == null
    }

    private fun getEditableReason(): String? {
        repository.getEditableStatus().let {
            if (it is NonEditable) {
                return resourcesProvider.provideEditionStatus(it.reason)
            }
        }
        return null
    }

    private fun isEnable(storedEvent: Event?): Boolean {
        return storedEvent?.let {
            repository.getEditableStatus() is Editable
        } ?: true
    }

    private fun hasReopenAuthority(): Boolean =
        d2.userModule().authorities()
            .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
            .one()
            .blockingExists()

    private fun getCanReopen(): Boolean =
        eventId?.let {
            getStoredEvent()?.status() == EventStatus.COMPLETED && hasReopenAuthority()
        } ?: false
}
