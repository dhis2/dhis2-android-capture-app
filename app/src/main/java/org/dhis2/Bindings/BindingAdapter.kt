package org.dhis2.Bindings

import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.dhis2.R
import org.dhis2.usescases.programEventDetail.ProgramEventViewModel
import org.dhis2.utils.CatComboAdapter
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.category.CategoryOptionComboModel
import org.hisp.dhis.android.core.common.ObjectStyleModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentModel
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventModel
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramModel
import org.hisp.dhis.android.core.program.ProgramStageModel
import timber.log.Timber
import java.text.ParseException
import java.util.*


@BindingAdapter("elevation")
fun setElevation(view: View, elevation: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        view.elevation = elevation
    } else {
        val drawable = view.resources.getDrawable(android.R.drawable.dialog_holo_light_frame)
        drawable.colorFilter = PorterDuffColorFilter(view.resources.getColor(R.color.colorGreyDefault), PorterDuff.Mode.MULTIPLY)
        view.background = drawable
    }
}

@BindingAdapter("scrollingTextView")
fun setScrollingTextView(textView: TextView, canScroll: Boolean) {
    if (canScroll)
        textView.movementMethod = ScrollingMovementMethod()
}

@BindingAdapter("date")
fun setDate(textView: TextView, date: String) {
    val formatIn = DateUtils.databaseDateFormat()
    val formatOut = DateUtils.uiDateFormat()
    try {
        val dateIn = formatIn.parse(date)
        val dateOut = formatOut.format(dateIn)
        textView.text = dateOut
    } catch (e: ParseException) {
        Timber.e(e)
    }
}

@BindingAdapter("date")
fun parseDate(textView: TextView, date: Date) {
    val formatOut = DateUtils.uiDateFormat()
    val dateOut = formatOut.format(date)
    textView.text = dateOut
}

@BindingAdapter("drawableEnd")
fun setDrawableEnd(textView: TextView, drawable: Drawable) {
    textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (drawable is AnimatedVectorDrawable)
            drawable.start()
    }
}

@BindingAdapter(value = ["initGrid", "spanCount"], requireAll = false)
fun setLayoutManager(recyclerView: RecyclerView, horizontal: Boolean, spanCount: Int) {
    val recyclerLayout: RecyclerView.LayoutManager
    var count = spanCount
    if (count == -1)
        count = 1

    recyclerLayout = GridLayoutManager(recyclerView.context, count, RecyclerView.VERTICAL, false)
    recyclerView.layoutManager = recyclerLayout
}

@BindingAdapter("spanSize")
fun setSpanSize(recyclerView: RecyclerView, setSpanSize: Boolean) {
    if (recyclerView.layoutManager is GridLayoutManager) {
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemViewType = recyclerView.adapter!!.getItemViewType(position)
                return if (itemViewType == 4 || itemViewType == 8) 2 else 1
            }
        }
    }
}

@BindingAdapter("progressColor")
fun setProgressColor(progressBar: ProgressBar, color: Int) {
    val typedValue = TypedValue()
    val a = progressBar.context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
    val color2 = a.getColor(0, 0)
    a.recycle()
    progressBar.indeterminateDrawable.setColorFilter(color2, PorterDuff.Mode.SRC_IN)
}

@BindingAdapter("enrolmentIcon")
fun setEnrolmentIcon(imageView: ImageView, enrollmentStatus: EnrollmentStatus?) {
    var status = enrollmentStatus
    if (status == null) status = EnrollmentStatus.ACTIVE
    val lock: Drawable? = when (status) {
        EnrollmentStatus.ACTIVE -> AppCompatResources.getDrawable(imageView.context, R.drawable.ic_lock_open_green)
        EnrollmentStatus.COMPLETED -> AppCompatResources.getDrawable(imageView.context, R.drawable.ic_lock_completed)
        EnrollmentStatus.CANCELLED -> AppCompatResources.getDrawable(imageView.context, R.drawable.ic_lock_inactive)
        else -> AppCompatResources.getDrawable(imageView.context, R.drawable.ic_lock_read_only)
    }
    imageView.setImageDrawable(lock)
}

@BindingAdapter("enrolmentText")
fun setEnrollmentText(textView: TextView, enrollmentStatus: EnrollmentStatus?) {
    var status = enrollmentStatus
    if (status == null) status = EnrollmentStatus.ACTIVE
    val text = when (status) {
        EnrollmentStatus.ACTIVE ->  textView.context.getString(R.string.event_open)
        EnrollmentStatus.COMPLETED ->  textView.context.getString(R.string.completed)
        EnrollmentStatus.CANCELLED ->  textView.context.getString(R.string.cancelled)
        else ->  textView.context.getString(R.string.read_only)
    }
    textView.text = text
}

@BindingAdapter(value = ["eventStatusIcon", "enrollmentStatusIcon",
    "eventProgramStage", "eventProgram"], requireAll = false)
fun setEventIcon(view: ImageView, event: EventModel?, enrollmentModel: EnrollmentModel,
                 eventProgramStage: ProgramStageModel, program: ProgramModel) {
    if (event != null) {
        var status = event.status()
        var enrollmentStatus = enrollmentModel.enrollmentStatus()
        if (status == null)
            status = EventStatus.ACTIVE
        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE

        when (enrollmentStatus) {
            EnrollmentStatus.ACTIVE -> when (status) {
                EventStatus.ACTIVE -> {
                    var eventDate = event.eventDate()
                    if (eventProgramStage.periodType() != null && eventProgramStage.periodType()!!.name.contains(PeriodType.Weekly.name))
                        eventDate = DateUtils.getInstance().getNextPeriod(eventProgramStage.periodType(), eventDate!!, 0, true)
                    if (DateUtils.getInstance().isEventExpired(eventDate!!, null, event.status()!!,
                                    program.completeEventsExpiryDays()!!,
                                    if (eventProgramStage.periodType() != null) eventProgramStage.periodType()
                                    else program.expiryPeriodType(), program.expiryDays()!!)!!) {
                        view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_eye_red))
                    } else {
                        view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_edit))
                    }
                }
                EventStatus.OVERDUE, EventStatus.COMPLETED, EventStatus.SKIPPED -> view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_visibility))
                EventStatus.SCHEDULE -> view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_edit))
                EventStatus.VISITED -> view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_edit))
                else -> view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_edit))
            }
            EnrollmentStatus.COMPLETED -> view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_visibility))
            else ->
                view.setImageDrawable(AppCompatResources.getDrawable(view.context, R.drawable.ic_visibility))
        }
    }
}

@BindingAdapter(value = ["eventStatusText", "enrollmentStatus",
    "eventProgramStage", "eventProgram"])
fun setEventText(view: TextView, event: EventModel?, enrollmentModel: EnrollmentModel,
                 eventProgramStage: ProgramStageModel, program: ProgramModel) {
    if (event != null) {
        var status = event.status()
        var enrollmentStatus = enrollmentModel.enrollmentStatus()
        if (status == null)
            status = EventStatus.ACTIVE
        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE


        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            when (status) {
                EventStatus.ACTIVE -> {
                    var eventDate = event.eventDate()
                    if (eventProgramStage.periodType() != null && eventProgramStage.periodType()!!.name.contains(PeriodType.Weekly.name))
                        eventDate = DateUtils.getInstance().getNextPeriod(eventProgramStage.periodType(), eventDate!!, 0, true)
                    if (DateUtils.getInstance().isEventExpired(eventDate!!, null, event.status()!!, program.completeEventsExpiryDays()!!, if (eventProgramStage.periodType() != null) eventProgramStage.periodType() else program.expiryPeriodType(), program.expiryDays()!!)!!) {
                        view.text = view.context.getString(R.string.event_expired)
                    } else {
                        view.text = view.context.getString(R.string.event_open)
                    }
                }
                EventStatus.COMPLETED -> if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays()!!)) {
                    view.text = view.context.getString(R.string.event_expired)
                } else {
                    view.text = view.context.getString(R.string.event_completed)
                }
                EventStatus.SCHEDULE -> if (DateUtils.getInstance().hasExpired(event, program.expiryDays()!!, program.completeEventsExpiryDays()!!, if (eventProgramStage.periodType() != null) eventProgramStage.periodType() else program.expiryPeriodType())) {
                    view.text = view.context.getString(R.string.event_expired)
                } else {
                    view.text = view.context.getString(R.string.event_schedule)
                }
                EventStatus.SKIPPED -> view.text = view.context.getString(R.string.event_skipped)
                EventStatus.OVERDUE -> view.setText(R.string.event_overdue)
                else -> view.text = view.context.getString(R.string.read_only)
            }
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            view.text = view.context.getString(R.string.program_completed)
        } else {
            view.text = view.context.getString(R.string.program_inactive)
        }
    }
}


@BindingAdapter(value = ["eventColor", "eventProgramStage", "eventProgram"])
fun setEventColor(view: View, event: EventModel?, programStage: ProgramStageModel, program: ProgramModel) {
    if (event != null) {
        val bgColor: Int
        when {
            DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays()!!) -> bgColor = R.drawable.item_event_dark_gray_ripple
            event.status() != null -> when (event.status()) {
                EventStatus.ACTIVE -> {
                    var eventDate = event.eventDate()
                    if (programStage.periodType() != null && programStage.periodType()!!.name.contains(PeriodType.Weekly.name))
                        eventDate = DateUtils.getInstance().getNextPeriod(programStage.periodType(), eventDate!!, 0, true)
                    if (DateUtils.getInstance().isEventExpired(eventDate!!, null, event.status()!!, program.completeEventsExpiryDays()!!, if (programStage.periodType() != null) programStage.periodType() else program.expiryPeriodType(), program.expiryDays()!!)!!) {
                        bgColor = R.drawable.item_event_dark_gray_ripple
                    } else
                        bgColor = R.drawable.item_event_yellow_ripple
                }
                EventStatus.COMPLETED -> if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays()!!)) {
                    bgColor = R.drawable.item_event_dark_gray_ripple
                } else
                    bgColor = R.drawable.item_event_gray_ripple
                EventStatus.SCHEDULE -> if (DateUtils.getInstance().hasExpired(event, program.expiryDays()!!, program.completeEventsExpiryDays()!!, if (programStage.periodType() != null) programStage.periodType() else program.expiryPeriodType())) {
                    bgColor = R.drawable.item_event_dark_gray_ripple
                } else
                    bgColor = R.drawable.item_event_green_ripple
                EventStatus.VISITED, EventStatus.SKIPPED -> bgColor = R.drawable.item_event_red_ripple
                else -> bgColor = R.drawable.item_event_red_ripple
            }
            else -> bgColor = R.drawable.item_event_red_ripple
        }
        view.background = AppCompatResources.getDrawable(view.context, bgColor)
    }
}

@BindingAdapter("statusColor")
fun setStatusColor(view: ImageView, status: ImportStatus) {
    val icon: Drawable?
    when (status) {
        ImportStatus.ERROR -> icon = AppCompatResources.getDrawable(view.context, R.drawable.red_circle)
        ImportStatus.SUCCESS -> icon = AppCompatResources.getDrawable(view.context, R.drawable.green_circle)
        ImportStatus.WARNING -> icon = AppCompatResources.getDrawable(view.context, R.drawable.yellow_circle)
        else -> icon = null
    }
    view.setImageDrawable(icon)
}

@BindingAdapter("eventWithoutRegistrationStatusText")
fun setEventWithoutRegistrationStatusText(textView: TextView, event: ProgramEventViewModel) {
    when (event.eventStatus()) {
        EventStatus.ACTIVE -> if (event.isExpired) {
            textView.text = textView.context.getString(R.string.event_editing_expired)
        } else {
            textView.text = textView.context.getString(R.string.event_open)
        }
        EventStatus.COMPLETED -> if (event.isExpired) {
            textView.text = textView.context.getString(R.string.event_editing_expired)
        } else {
            textView.text = textView.context.getString(R.string.event_completed)
        }
        EventStatus.SKIPPED -> textView.text = textView.context.getString(R.string.event_editing_expired)
        else -> textView.text = textView.context.getString(R.string.read_only)
    }
}

@BindingAdapter("eventWithoutRegistrationStatusIcon")
fun setEventWithoutRegistrationStatusIcon(imageView: ImageView, event: ProgramEventViewModel) {
    if (event.eventStatus() == EventStatus.ACTIVE && !event.isExpired)
        imageView.setImageResource(R.drawable.ic_edit)
    else
        imageView.setImageResource(R.drawable.ic_visibility)
}

@BindingAdapter("stateText")
fun setStateText(textView: TextView, state: State) {
    when (state) {
        State.TO_POST -> textView.text = textView.context.getString(R.string.state_to_post)
        State.TO_UPDATE -> textView.text = textView.context.getString(R.string.state_to_update)
        State.TO_DELETE -> textView.text = textView.context.getString(R.string.state_to_delete)
        State.ERROR -> textView.text = textView.context.getString(R.string.state_error)
        State.SYNCED -> textView.text = textView.context.getString(R.string.state_synced)
        else -> {
        }
    }
}

@BindingAdapter("stateIcon")
fun setStateIcon(imageView: ImageView, state: State?) {
    if (state != null) {
        when (state) {
            State.TO_POST, State.TO_UPDATE, State.TO_DELETE -> imageView.setImageResource(R.drawable.ic_sync_problem_grey)
            State.ERROR -> imageView.setImageResource(R.drawable.ic_sync_problem_red)
            State.SYNCED -> imageView.setImageResource(R.drawable.ic_sync)
            State.WARNING -> imageView.setImageResource(R.drawable.ic_sync_warning)
            State.SENT_VIA_SMS, State.SYNCED_VIA_SMS -> imageView.setImageResource(R.drawable.ic_sync_sms)
            else -> {
            }
        }
    }
}

@BindingAdapter("spinnerOptions")
fun setSpinnerOptions(spinner: Spinner, options: MutableList<CategoryOptionComboModel>) {
    val adapter = CatComboAdapter(spinner.context,
            R.layout.spinner_layout,
            R.id.spinner_text,
            options,
            "",
            R.color.white_faf)
    spinner.adapter = adapter
}

@BindingAdapter("fromResBgColor")
fun setFromResBgColor(view: View, color: Int) {
    val tintedColor: String

    val rgb = ArrayList<Double>()
    rgb.add(Color.red(color) / 255.0)
    rgb.add(Color.green(color) / 255.0)
    rgb.add(Color.blue(color) / 255.0)

    var r: Double? = null
    var g: Double? = null
    var b: Double? = null
    for (c in rgb) {
        var cClone = c
        cClone /= if (c <= 0.03928)
            12.92
        else
            Math.pow((c + 0.055) / 1.055, 2.4)

        when {
            r == null -> r = c
            g == null -> g = c
            else -> b = c
        }
    }

    val L = 0.2126 * r!! + 0.7152 * g!! + 0.0722 * b!!


    tintedColor = if (L > 0.179)
        "#000000"
    else
        "#FFFFFF"

    if (view is TextView) {
        view.setTextColor(Color.parseColor(tintedColor))
    }
    if (view is ImageView) {
        val drawable = view.drawable
        drawable?.setColorFilter(Color.parseColor(tintedColor), PorterDuff.Mode.SRC_IN)
        view.setImageDrawable(drawable)
    }
}

@BindingAdapter("imageBackground")
fun setImageBackground(imageView: ImageView, drawable: Drawable) {

    val typedValue = TypedValue()
    val a = imageView.context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
    val b = imageView.context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryLight))
    val colorPrimaryDark = a.getColor(0, 0)
    val colorPrimaryLight = b.getColor(0, 0)

    val px = (1 * Resources.getSystem().displayMetrics.density).toInt()
    (drawable.mutate() as GradientDrawable).setStroke(px, colorPrimaryDark)
    imageView.background = drawable
}

@BindingAdapter("searchOrAdd")
fun setFabIcoin(fab: FloatingActionButton, needSearch: Boolean) {
    val drawable: Drawable?
    drawable = if (needSearch) {
        AppCompatResources.getDrawable(fab.context, R.drawable.ic_search)
    } else {
        AppCompatResources.getDrawable(fab.context, R.drawable.ic_add_accent)
    }
    fab.setColorFilter(Color.WHITE)
    fab.setImageDrawable(drawable)
}

@BindingAdapter("versionVisibility")
fun setVisibility(linearLayout: LinearLayout, check: Boolean) {
    if (check && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        linearLayout.visibility = View.GONE
    }
}

@BindingAdapter("settingIcon")
fun setSettingIcon(view: ImageView, drawableReference: Int) {
    val drawable = AppCompatResources.getDrawable(view.context, drawableReference)
    view.setImageDrawable(drawable)
}

fun setObjectStyle(view: View, itemView: View, objectStyle: ObjectStyleModel) {
    if (objectStyle.icon() != null) {
        val resources = view.context.resources
        val iconName = if (objectStyle.icon()!!.startsWith("ic_")) objectStyle.icon() else "ic_" + objectStyle.icon()!!
        val icon = resources.getIdentifier(iconName, "drawable", view.context.packageName)
        if (view is ImageView)
            view.setImageResource(icon)
    }

    if (objectStyle.color() != null) {
        val color = if (objectStyle.color()!!.startsWith("#")) objectStyle.color() else "#" + objectStyle.color()!!
        val colorRes: Int
        colorRes = if (color!!.length == 4) {
            ContextCompat.getColor(view.context, R.color.colorPrimary)
        } else
            Color.parseColor(color)

        itemView.setBackgroundColor(colorRes)
        setFromResBgColor(view, colorRes)
    }
}



