package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.event.EventNonEditableReason

class EventDetailResourcesProvider(
    private val resourceManager: ResourceManager,
) {
    fun provideDueDate() = resourceManager.getString(R.string.due_date)

    fun provideEventDate() = resourceManager.getString(R.string.event_date)

    fun provideEditionStatus(reason: EventNonEditableReason): String {
        return when (reason) {
            EventNonEditableReason.BLOCKED_BY_COMPLETION ->
                resourceManager.getString(R.string.blocked_by_completion)
            EventNonEditableReason.EXPIRED ->
                resourceManager.getString(R.string.edition_expired)
            EventNonEditableReason.NO_DATA_WRITE_ACCESS ->
                resourceManager.getString(R.string.edition_no_write_access)
            EventNonEditableReason.EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE ->
                resourceManager.getString(R.string.event_date_not_in_orgunit_range)
            EventNonEditableReason.NO_CATEGORY_COMBO_ACCESS ->
                resourceManager.getString(R.string.edition_no_catcombo_access)
            EventNonEditableReason.ENROLLMENT_IS_NOT_OPEN ->
                resourceManager.getString(R.string.edition_enrollment_is_no_open)
            EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE ->
                resourceManager.getString(R.string.edition_orgunit_capture_scope)
        }
    }

    fun provideButtonUpdate() = resourceManager.getString(R.string.update)

    fun provideButtonNext() = resourceManager.getString(R.string.next)

    fun provideButtonCheck() = resourceManager.getString(R.string.check_event)

    fun provideEventCreatedMessage() = resourceManager.getString(R.string.event_updated)

    fun provideEventCreationError() = resourceManager.getString(R.string.failed_insert_event)

    fun provideReOpened() = resourceManager.getString(R.string.re_opened)
}
