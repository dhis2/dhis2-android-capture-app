package org.dhis2.commons.date

import android.content.Context
import androidx.annotation.StringRes
import org.dhis2.commons.resources.ResourceManager
import java.util.Date

class DateLabelProvider(
    val context: Context,
    val resourceManager: ResourceManager,
) {
    fun span(date: Date?) = date.toDateSpan(context)

    fun format(eventDate: Date?): String =
        eventDate?.let {
            DateUtils.getInstance().formatDate(eventDate)
        } ?: ""

    fun scheduleFormat(
        date: Date?,
        @StringRes scheduleLabelRes: Int,
    ): String =
        resourceManager
            .getString(scheduleLabelRes)
            .format(date)
}
