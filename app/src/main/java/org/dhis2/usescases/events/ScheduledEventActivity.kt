package org.dhis2.usescases.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.databinding.ActivityEventScheduledBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.DateUtils
import org.dhis2.utils.EventMode
import org.dhis2.utils.customviews.PeriodDialog
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

const val EXTRA_EVENT_UID = "EVENT_UID"

class ScheduledEventActivity : ActivityGlobalAbstract(), ScheduledEventContract.View {

    companion object {
        fun getIntent(context: Context, eventUid: String): Intent {
            val intent = Intent(context, ScheduledEventActivity::class.java)
            intent.putExtra(EXTRA_EVENT_UID, eventUid)
            return intent
        }
    }

    private lateinit var stage: ProgramStage
    private lateinit var program: Program
    private lateinit var event: Event
    private lateinit var binding: ActivityEventScheduledBinding

    @Inject
    lateinit var presenter: ScheduledEventContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        (
            (applicationContext as App).userComponent()!!.plus(
                ScheduledEventModule(
                    intent.extras!!.getString(
                        EXTRA_EVENT_UID
                    )!!,
                    this
                )
            )
            ).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_scheduled)
        binding.presenter = presenter
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        presenter.finish()
        super.onPause()
    }

    override fun setEvent(event: Event) {
        this.event = event

        binding.dueDate.setText(DateUtils.uiDateFormat().format(event.dueDate()))

        when (event.status()) {
            EventStatus.OVERDUE, EventStatus.SCHEDULE -> {
                binding.actionButton.visibility = View.VISIBLE
                binding.actionButton.text = getString(R.string.skip)
                binding.actionButton.setOnClickListener { presenter.skipEvent() }
            }
            else -> {
                binding.actionButton.visibility = View.GONE
                binding.actionButton.setOnClickListener(null)
            }
        }
    }

    override fun setStage(programStage: ProgramStage) {
        this.stage = programStage
        binding.programStage = programStage
        binding.dateLayout.hint =
            programStage.executionDateLabel() ?: getString(R.string.report_date)
        binding.dueDateLayout.hint = programStage.dueDateLabel() ?: getString(R.string.due_date)

        if (programStage.hideDueDate() == true) {
            binding.dueDateLayout.visibility = View.GONE
        }

        setEventDateClickListener(programStage.periodType())
    }

    override fun setProgram(program: Program) {
        this.program = program
        binding.name = program.displayName()
    }

    fun setEventDateClickListener(periodType: PeriodType?) {
        binding.date.setOnClickListener {
            if (periodType == null) {
                showCustomCalendar(false)
            } else {
                var minDate =
                    DateUtils.getInstance().expDate(null, program.expiryDays()!!, periodType)
                val lastPeriodDate =
                    DateUtils.getInstance().getNextPeriod(periodType, minDate, -1, true)

                if (lastPeriodDate.after(
                        DateUtils.getInstance().getNextPeriod(
                                program.expiryPeriodType(),
                                minDate,
                                0
                            )
                    )
                ) {
                    minDate = DateUtils.getInstance().getNextPeriod(periodType, lastPeriodDate, 0)
                }

                PeriodDialog()
                    .setPeriod(periodType)
                    .setMinDate(minDate)
                    .setMaxDate(DateUtils.getInstance().today)
                    .setPossitiveListener { selectedDate -> presenter.setEventDate(selectedDate) }
                    .show(supportFragmentManager, PeriodDialog::class.java.simpleName)
            }
        }

        binding.dueDate.setOnClickListener {
            if (periodType == null) {
                showCustomCalendar(true)
            } else {
                var minDate =
                    DateUtils.getInstance().expDate(null, program.expiryDays()!!, periodType)
                val lastPeriodDate =
                    DateUtils.getInstance().getNextPeriod(periodType, minDate, -1, true)

                if (lastPeriodDate.after(
                        DateUtils.getInstance().getNextPeriod(
                                program.expiryPeriodType(),
                                minDate,
                                0
                            )
                    )
                ) {
                    minDate = DateUtils.getInstance().getNextPeriod(periodType, lastPeriodDate, 0)
                }

                PeriodDialog()
                    .setPeriod(periodType)
                    .setMinDate(minDate)
                    .setMaxDate(DateUtils.getInstance().today)
                    .setPossitiveListener { selectedDate -> presenter.setDueDate(selectedDate) }
                    .show(supportFragmentManager, PeriodDialog::class.java.simpleName)
            }
        }
    }

    private fun showCustomCalendar(isDueDate: Boolean) {
        val dialog = CalendarPicker(this)

        if (isDueDate) {
            dialog.setInitialDate(event.dueDate())
            dialog.setScheduleInterval(stage.standardInterval() ?: 0)
        }

        if (program.expiryPeriodType() != null) {
            val minDate = DateUtils.getInstance().expDate(
                null,
                program.expiryDays() ?: 0,
                program.expiryPeriodType()
            )
            dialog.setMinDate(minDate)
        }

        if (!isDueDate) {
            dialog.setMaxDate(Date(System.currentTimeMillis() - 1000))
        }
        dialog.setListener(object : OnDatePickerListener {
            override fun onNegativeClick() {
                dialog.dismiss()
            }

            override fun onPositiveClick(datePicker: DatePicker) {
                val date = Calendar.getInstance().apply {
                    set(Calendar.YEAR, datePicker.year)
                    set(Calendar.MONTH, datePicker.month)
                    set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
                    set(Calendar.HOUR, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                if (isDueDate) {
                    presenter.setDueDate(date)
                } else {
                    presenter.setEventDate(date)
                }
            }
        })
        dialog.show()
    }

    override fun openInitialActivity() {
        val bundle = EventInitialActivity.getBundle(
            program.uid(),
            event.uid(),
            EventCreationType.DEFAULT.name,
            presenter.getEventTei(),
            stage.periodType(),
            presenter.getEnrollment().organisationUnit(),
            stage.uid(),
            event.enrollment(),
            stage.standardInterval() ?: 0,
            presenter.getEnrollment().status()
        )
        startActivity(Intent(this, EventInitialActivity::class.java).apply { putExtras(bundle) })
        finish()
    }

    override fun openFormActivity() {
        val bundle = EventCaptureActivity.getActivityBundle(
            event.uid(),
            program.uid(),
            EventMode.CHECK
        )
        Intent(activity, EventCaptureActivity::class.java).apply {
            putExtras(bundle)
            startActivity(this)
            finish()
        }
    }
}
