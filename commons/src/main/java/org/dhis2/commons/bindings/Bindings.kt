package org.dhis2.commons.bindings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import org.dhis2.commons.R
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import java.text.SimpleDateFormat
import java.util.Date

@BindingAdapter(value = ["stateIcon", "showSynced"], requireAll = false)
fun ImageView.setStateIcon(state: State?, showSynced: Boolean?) {
    when (state) {
        State.TO_POST, State.TO_UPDATE, State.UPLOADING -> {
            setImageResource(R.drawable.ic_sync_problem_grey)
            visibility = View.VISIBLE
            tag = R.drawable.ic_sync_problem_grey
        }
        State.ERROR -> {
            setImageResource(R.drawable.ic_sync_problem_red)
            visibility = View.VISIBLE
            tag = R.drawable.ic_sync_problem_red
        }
        State.SYNCED -> {
            setImageResource(R.drawable.ic_status_synced)
            if (showSynced != true) {
                visibility = View.GONE
            }
            tag = R.drawable.ic_status_synced
        }
        State.WARNING -> {
            setImageResource(R.drawable.ic_sync_warning)
            visibility = View.VISIBLE
            tag = R.drawable.ic_sync_warning
        }
        State.SENT_VIA_SMS, State.SYNCED_VIA_SMS -> {
            setImageResource(R.drawable.ic_sync_sms)
            visibility = View.VISIBLE
            tag = R.drawable.ic_sync_sms
        }
        else -> {
        }
    }
}

@BindingAdapter(
    value = ["eventStatusIcon", "enrollmentStatusIcon", "eventProgramStage", "eventProgram"],
    requireAll = false,
)
fun ImageView.setEventIcon(
    event: Event?,
    enrollment: Enrollment?,
    eventProgramStage: ProgramStage?,
    program: Program,
) {
    event?.let {
        val status = event.status() ?: EventStatus.ACTIVE
        val isEnrollmentActive = enrollment?.let {
            it.status() == EnrollmentStatus.ACTIVE
        } ?: true
        val drawableResource = when (status) {
            EventStatus.ACTIVE -> getOpenIcon(
                isEnrollmentActive && !event.isExpired(eventProgramStage, program),
            )
            EventStatus.OVERDUE -> getOverdueIcon(isEnrollmentActive)
            EventStatus.COMPLETED -> getCompletedIcon(isEnrollmentActive)
            EventStatus.SKIPPED -> getSkippedIcon(isEnrollmentActive)
            EventStatus.SCHEDULE -> getScheduleIcon(isEnrollmentActive)
            else -> getOpenIcon(false)
        }
        setImageDrawable(AppCompatResources.getDrawable(context, drawableResource))
        tag = drawableResource
    }
}

private fun Event.isExpired(eventProgramStage: ProgramStage?, program: Program): Boolean {
    var eventDate = eventDate()
    if (eventProgramStage?.periodType()?.name?.contains(PeriodType.Weekly.name) == true) {
        eventDate = DateUtils.getInstance()
            .getNextPeriod(eventProgramStage.periodType(), eventDate, 0, true)
    }
    return DateUtils.getInstance().isEventExpired(
        eventDate,
        null,
        status(),
        program.completeEventsExpiryDays() ?: 0,
        eventProgramStage?.periodType() ?: program.expiryPeriodType(),
        program.expiryDays() ?: 0,
    )
}

private fun getOpenIcon(isActive: Boolean) = when (isActive) {
    true -> R.drawable.ic_event_status_open
    false -> R.drawable.ic_event_status_open_read
}

private fun getOverdueIcon(isActive: Boolean) = when (isActive) {
    true -> R.drawable.ic_event_status_overdue
    false -> R.drawable.ic_event_status_overdue_read
}

private fun getCompletedIcon(isActive: Boolean) = when (isActive) {
    true -> R.drawable.ic_event_status_complete
    false -> R.drawable.ic_event_status_complete_read
}

private fun getSkippedIcon(isActive: Boolean) = when (isActive) {
    true -> R.drawable.ic_event_status_skipped
    false -> R.drawable.ic_event_status_skipped_read
}

private fun getScheduleIcon(isActive: Boolean) = when (isActive) {
    true -> R.drawable.ic_event_status_schedule
    false -> R.drawable.ic_event_status_schedule_read
}

@BindingAdapter("eventWithoutRegistrationStatusIcon")
fun ImageView.setEventWithoutRegistrationStatusIcon(event: ProgramEventViewModel) {
    val drawableResource: Int = when (event.eventStatus()) {
        EventStatus.COMPLETED ->
            if (event.canBeEdited()) {
                R.drawable.ic_event_status_complete
            } else {
                R.drawable.ic_event_status_complete_read
            }
        else ->
            if (event.canBeEdited()) {
                R.drawable.ic_event_status_open
            } else {
                R.drawable.ic_event_status_open_read
            }
    }
    setImageResource(drawableResource)
}

@BindingAdapter("date")
fun TextView.parseDate(date: Date?) {
    if (date != null) {
        val formatOut: SimpleDateFormat = DateUtils.uiDateFormat()
        val dateOut = formatOut.format(date)
        text = dateOut
    }
}

@BindingAdapter("set_metadata_icon")
fun ComposeView.setIconStyle(metadataIconData: MetadataIconData?) {
    metadataIconData?.let {
        setUpMetadataIcon(
            metadataIconData,
            true,
        )
    }
}
