package org.dhis2.usescases.events

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import java.util.Calendar
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityEventScheduledBinding
import org.dhis2.databinding.WidgetDatepickerBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.DateUtils
import org.dhis2.utils.EventCreationType
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
                showCustomCalendar(
                    DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                        val date = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        presenter.setEventDate(date)
                    },
                    false
                )
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
                showCustomCalendar(
                    DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        val date = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        presenter.setDueDate(date)
                    },
                    true
                )
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

    private fun showCustomCalendar(
        listener: DatePickerDialog.OnDateSetListener,
        isDueDate: Boolean
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater)
        val datePicker = widgetBinding.widgetDatepicker

        val calendar = Calendar.getInstance()

        if (isDueDate) {
            calendar.time = event.dueDate()
            calendar.add(Calendar.DAY_OF_YEAR, stage.standardInterval() ?: 0)
        }

        datePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (program.expiryPeriodType() != null) {
            val minDate = DateUtils.getInstance().expDate(
                null,
                program.expiryDays() ?: 0,
                program.expiryPeriodType()
            )
            datePicker.minDate = minDate!!.time
        }

        if (!isDueDate) {
            datePicker.maxDate = System.currentTimeMillis() - 1000
        }

        val alertDialog = AlertDialog.Builder(context, R.style.DatePickerTheme)

        alertDialog.setView(widgetBinding.root)
        val dialog = alertDialog.create()

        widgetBinding.changeCalendarButton.setOnClickListener {
            showNativeCalendar(listener, isDueDate)
            dialog.dismiss()
        }
        widgetBinding.clearButton.setOnClickListener { dialog.dismiss() }
        widgetBinding.acceptButton.setOnClickListener {
            listener.onDateSet(datePicker, datePicker.year, datePicker.month, datePicker.dayOfMonth)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNativeCalendar(
        listener: DatePickerDialog.OnDateSetListener,
        isDueDate: Boolean
    ) {
        val calendar = Calendar.getInstance()

        if (isDueDate) {
            calendar.time = event.dueDate()
            calendar.add(Calendar.DAY_OF_YEAR, stage.standardInterval() ?: 0)
        }

        val datePickerDialog = DatePickerDialog(
            this, listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (program.expiryPeriodType() != null) {
            val minDate = DateUtils.getInstance().expDate(
                null,
                program.expiryDays() ?: 0,
                program.expiryPeriodType()
            )
            datePickerDialog.datePicker.minDate = minDate!!.time
        }

        if (!isDueDate) {
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.change_calendar)
            ) { _, _ ->
                datePickerDialog.dismiss()
                showCustomCalendar(listener, isDueDate)
            }
        }

        datePickerDialog.show()
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
