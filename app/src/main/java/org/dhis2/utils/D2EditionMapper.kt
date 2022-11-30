package org.dhis2.utils

import android.content.Context
import org.dhis2.R
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventNonEditableReason.BLOCKED_BY_COMPLETION
import org.hisp.dhis.android.core.event.EventNonEditableReason.ENROLLMENT_IS_NOT_OPEN
import org.hisp.dhis.android.core.event.EventNonEditableReason.EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE
import org.hisp.dhis.android.core.event.EventNonEditableReason.EXPIRED
import org.hisp.dhis.android.core.event.EventNonEditableReason.NO_CATEGORY_COMBO_ACCESS
import org.hisp.dhis.android.core.event.EventNonEditableReason.NO_DATA_WRITE_ACCESS
import org.hisp.dhis.android.core.event.EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE

class D2EditionMapper {

    companion object {

        @JvmStatic
        fun mapEditionStatus(context: Context, reason: EventNonEditableReason): String {
            return when (reason) {
                BLOCKED_BY_COMPLETION -> context.getString(R.string.blocked_by_completion)
                EXPIRED -> context.getString(R.string.edition_expired)
                NO_DATA_WRITE_ACCESS -> context.getString(R.string.edition_no_write_access)
                EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE ->
                    context.getString(R.string.event_date_not_in_orgunit_range)
                NO_CATEGORY_COMBO_ACCESS -> context.getString(R.string.edition_no_catcombo_access)
                ENROLLMENT_IS_NOT_OPEN -> context.getString(R.string.edition_enrollment_is_no_open)
                ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE ->
                    context.getString(R.string.edition_orgunit_capture_scope)
            }
        }
    }
}
