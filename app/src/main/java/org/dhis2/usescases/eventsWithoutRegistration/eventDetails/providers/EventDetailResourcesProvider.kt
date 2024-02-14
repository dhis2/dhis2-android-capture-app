package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.event.EventNonEditableReason

class EventDetailResourcesProvider(
    private val programUid: String,
    private val programStage: String?,
    private val resourceManager: ResourceManager,
) {
    fun provideDueDate() = resourceManager.getString(R.string.due_date)

    fun provideEventDate() = resourceManager.formatWithEventLabel(
        R.string.event_label_date,
        programStage,
    )

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
                resourceManager.formatWithEnrollmentLabel(
                    programUid,
                    R.string.edition_enrollment_is_no_open_V2,
                    1,
                )
            EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE ->
                resourceManager.getString(R.string.edition_orgunit_capture_scope)
        }
    }

    fun provideButtonUpdate() = resourceManager.getString(R.string.update)

    fun provideButtonNext() = resourceManager.getString(R.string.next)

    fun provideButtonCheck() = resourceManager.getString(R.string.check_event)

    fun provideEventCreatedMessage() = resourceManager.formatWithEventLabel(
        R.string.event_label_updated,
        programStage,
    )

    fun provideEventCreationError() = resourceManager.formatWithEventLabel(
        R.string.failed_insert_event_label,
        programStage,
    )

    fun provideReOpened() = resourceManager.getString(R.string.re_opened)
}
