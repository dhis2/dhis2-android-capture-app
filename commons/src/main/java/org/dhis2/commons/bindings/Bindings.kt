package org.dhis2.commons.bindings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import org.dhis2.commons.R
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.date.DateUtils
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
    if (state != null) {
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
                setImageResource(R.drawable.ic_sync)
                if (showSynced != true) {
                    visibility = View.GONE
                }
                tag = R.drawable.ic_sync
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
}

@BindingAdapter(
    value = ["eventStatusIcon", "enrollmentStatusIcon", "eventProgramStage", "eventProgram"],
    requireAll = false
)
fun ImageView.setEventIcon(
    event: Event?,
    enrollment: Enrollment?,
    eventProgramStage: ProgramStage?,
    program: Program
) {
    if (event != null) {
        var status = event.status()
        var enrollmentStatus: EnrollmentStatus? = EnrollmentStatus.ACTIVE
        if (enrollment != null) {
            enrollmentStatus = enrollment.status()
        }
        if (status == null) status = EventStatus.ACTIVE
        if (enrollmentStatus == null) enrollmentStatus = EnrollmentStatus.ACTIVE
        val drawableResource: Int
        when (status) {
            EventStatus.ACTIVE -> {
                var eventDate = event.eventDate()
                if (eventProgramStage?.periodType() != null && eventProgramStage.periodType()!!.name.contains(
                        PeriodType.Weekly.name
                    )
                ) {
                    eventDate = DateUtils.getInstance()
                        .getNextPeriod(eventProgramStage.periodType(), eventDate, 0, true)
                }
                val isExpired: Boolean = DateUtils.getInstance().isEventExpired(
                    eventDate,
                    null,
                    event.status(),
                    program.completeEventsExpiryDays() ?: 0,
                    eventProgramStage?.periodType() ?: program.expiryPeriodType(),
                    program.expiryDays() ?: 0
                )
                drawableResource =
                    if (enrollmentStatus == EnrollmentStatus.ACTIVE && !isExpired) R.drawable.ic_event_status_open else R.drawable.ic_event_status_open_read
            }
            EventStatus.OVERDUE -> drawableResource =
                if (enrollmentStatus == EnrollmentStatus.ACTIVE) R.drawable.ic_event_status_overdue else R.drawable.ic_event_status_overdue_read
            EventStatus.COMPLETED -> drawableResource =
                if (enrollmentStatus == EnrollmentStatus.ACTIVE) R.drawable.ic_event_status_complete else R.drawable.ic_event_status_complete_read
            EventStatus.SKIPPED -> drawableResource =
                if (enrollmentStatus == EnrollmentStatus.ACTIVE) R.drawable.ic_event_status_skipped else R.drawable.ic_event_status_skipped_read
            EventStatus.SCHEDULE -> drawableResource =
                if (enrollmentStatus == EnrollmentStatus.ACTIVE) R.drawable.ic_event_status_schedule else R.drawable.ic_event_status_schedule_read
            else -> drawableResource = R.drawable.ic_event_status_open_read
        }
        setImageDrawable(AppCompatResources.getDrawable(context, drawableResource))
        tag = drawableResource
    }
}

@BindingAdapter("eventWithoutRegistrationStatusIcon")
fun ImageView.setEventWithoutRegistrationStatusIcon(event: ProgramEventViewModel) {
    val drawableResource: Int = when (event.eventStatus()) {
        EventStatus.COMPLETED -> if (event.canBeEdited()) R.drawable.ic_event_status_complete else R.drawable.ic_event_status_complete_read
        else -> if (event.canBeEdited()) R.drawable.ic_event_status_open else R.drawable.ic_event_status_open_read
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
