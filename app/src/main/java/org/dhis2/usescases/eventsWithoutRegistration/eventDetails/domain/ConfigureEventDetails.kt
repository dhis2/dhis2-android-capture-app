package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus.CANCELLED
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.event.EventStatus.OVERDUE
import java.util.Date

class ConfigureEventDetails(
    private val repository: EventDetailsRepository,
    private val resourcesProvider: EventDetailResourcesProvider,
    private val creationType: EventCreationType,
    private val enrollmentStatus: EnrollmentStatus?,
    private val metadataIconProvider: MetadataIconProvider,
) {

    operator fun invoke(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        isCatComboCompleted: Boolean,
        coordinates: String?,
        tempCreate: String?,
    ): Flow<EventDetails> {
        val isEventCompleted = isCompleted(
            selectedDate = selectedDate,
            selectedOrgUnit = selectedOrgUnit,
            isCatComboCompleted = isCatComboCompleted,
            tempCreate = tempCreate,
        )
        val storedEvent = repository.getEvent()
        val programStage = repository.getProgramStage()
        return flowOf(
            EventDetails(
                name = programStage?.displayName(),
                description = programStage?.displayDescription(),
                metadataIconData = programStage?.style()?.let { metadataIconProvider(programStage.style()) },
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
                canReopen = repository.getCanReopen(),
            ),
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
        return if (!isEventCompleted) {
            false
        } else {
            storedEvent?.let {
                !(it.status() == OVERDUE && enrollmentStatus == CANCELLED) &&
                    repository.getEditableStatus() !is NonEditable
            } ?: true
        }
    }

    fun reopenEvent() = repository.reopenEvent()

    private fun isCompleted(
        selectedDate: Date?,
        selectedOrgUnit: String?,
        isCatComboCompleted: Boolean,
        tempCreate: String?,
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
}
