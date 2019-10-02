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
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityEventScheduledBinding
import org.dhis2.databinding.WidgetDatepickerBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.DateUtils
import org.dhis2.utils.custom_views.PeriodDialog
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import timber.log.Timber
import java.text.ParseException
import java.util.*
import javax.inject.Inject

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
        ((applicationContext as App).userComponent()!!.plus(ScheduledEventModule(intent.extras!!.getString(EXTRA_EVENT_UID)!!))).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_scheduled)
        binding.presenter = presenter
    }

    override fun onResume() {
        super.onResume()
        presenter.init(this)
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
        binding.dateLayout.hint = programStage.executionDateLabel()
                ?: getString(R.string.report_date)
        binding.dueDateLayout.hint = getString(R.string.due_date)

        if (programStage.hideDueDate() == true)
            binding.dueDateLayout.visibility = View.GONE

        setEventDateClickListener(programStage.periodType())
    }

    override fun setProgram(program: Program) {
        this.program = program
        binding.name = program.displayName()
    }

    fun setEventDateClickListener(periodType: PeriodType?) {
        binding.date.setOnClickListener { view ->
            if (periodType == null)
                showCustomCalendar(DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    val date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day)
                    try {
                        presenter.setEventDate(DateUtils.uiDateFormat().parse(date))
                    } catch (e: ParseException) {
                        Timber.e(e)
                    }

                }, false)
            else {
                var minDate = DateUtils.getInstance().expDate(null, program.expiryDays()!!, periodType)
                val lastPeriodDate = DateUtils.getInstance().getNextPeriod(periodType, minDate, -1, true)

                if (lastPeriodDate.after(DateUtils.getInstance().getNextPeriod(program.expiryPeriodType(), minDate, 0)))
                    minDate = DateUtils.getInstance().getNextPeriod(periodType, lastPeriodDate, 0)

                PeriodDialog()
                        .setPeriod(periodType)
                        .setMinDate(minDate)
                        .setMaxDate(DateUtils.getInstance().today)
                        .setPossitiveListener { selectedDate -> presenter.setEventDate(selectedDate) }
                        .show(supportFragmentManager, PeriodDialog::class.java.simpleName)
            }
        }

        binding.dueDate.setOnClickListener { view ->
            if (periodType == null)
                showCustomCalendar(DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    val date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day)
                    try {
                        presenter.setDueDate(DateUtils.uiDateFormat().parse(date))
                    } catch (e: ParseException) {
                        Timber.e(e)
                    }

                }, true)
            else {
                var minDate = DateUtils.getInstance().expDate(null, program.expiryDays()!!, periodType)
                val lastPeriodDate = DateUtils.getInstance().getNextPeriod(periodType, minDate, -1, true)

                if (lastPeriodDate.after(DateUtils.getInstance().getNextPeriod(program.expiryPeriodType(), minDate, 0)))
                    minDate = DateUtils.getInstance().getNextPeriod(periodType, lastPeriodDate, 0)

                PeriodDialog()
                        .setPeriod(periodType)
                        .setMinDate(minDate)
                        .setMaxDate(DateUtils.getInstance().today)
                        .setPossitiveListener { selectedDate -> presenter.setDueDate(selectedDate) }
                        .show(supportFragmentManager, PeriodDialog::class.java.simpleName)
            }
        }
    }

    private fun showCustomCalendar(listener: DatePickerDialog.OnDateSetListener, isDueDate: Boolean) {
        val layoutInflater = LayoutInflater.from(context)
        val widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater)
        val datePicker = widgetBinding.widgetDatepicker

        val calendar = Calendar.getInstance()

        if (isDueDate) {
            calendar.time = event.dueDate()
            calendar.add(Calendar.DAY_OF_YEAR, stage.standardInterval() ?: 0)
        }

        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        if (program.expiryPeriodType() != null) {
            val minDate = DateUtils.getInstance().expDate(null, program.expiryDays()
                    ?: 0, program.expiryPeriodType())
            datePicker.minDate = minDate!!.time
        }

        if (!isDueDate)
            datePicker.maxDate = System.currentTimeMillis() - 1000


        val alertDialog = AlertDialog.Builder(context, R.style.DatePickerTheme)

        alertDialog.setView(widgetBinding.root)
        val dialog = alertDialog.create()

        widgetBinding.changeCalendarButton.setOnClickListener { calendarButton ->
            showNativeCalendar(listener, isDueDate)
            dialog.dismiss()
        }
        widgetBinding.clearButton.setOnClickListener { clearButton -> dialog.dismiss() }
        widgetBinding.acceptButton.setOnClickListener { acceptButton ->
            listener.onDateSet(datePicker, datePicker.year, datePicker.month, datePicker.dayOfMonth)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNativeCalendar(listener: DatePickerDialog.OnDateSetListener, isDueDate: Boolean) {
        val calendar = Calendar.getInstance()

        if (isDueDate) {
            calendar.time = event.dueDate()
            calendar.add(Calendar.DAY_OF_YEAR, stage.standardInterval() ?: 0)
        }

        val datePickerDialog = DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))

        if (program.expiryPeriodType() != null) {
            val minDate = DateUtils.getInstance().expDate(null, program.expiryDays()
                    ?: 0, program.expiryPeriodType())
            datePickerDialog.datePicker.minDate = minDate!!.time
        }

        if (!isDueDate)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.resources.getString(R.string.change_calendar)) { dialog, which ->
                datePickerDialog.dismiss()
                showCustomCalendar(listener, isDueDate)
            }
        }

        datePickerDialog.show()
    }

    override fun setCatCombo(catCombo: CategoryCombo, selectedOptions: HashMap<String, CategoryOption>) {

        binding.catComboLayout.removeAllViews()

        /*for (category in catCombo.categories()!!) {
            val catSelectorBinding = CategorySelectorBinding.inflate(LayoutInflater.from(this))
            catSelectorBinding.catCombLayout.setHint(category.displayName())
            catSelectorBinding.catCombo.setOnClickListener(
                    { view ->
                        CategoryOptionPopUp.getInstance()
                                .setCategory(category)
                                .setDate(event.dueDate())
                                .setOnClick { item ->
                                    if (item != null)
                                        selectedOptions[category.uid()] = item
                                    else
                                        selectedOptions.remove(category.uid())
                                    catSelectorBinding.catCombo.setText(item?.displayName())
                                    if (selectedOptions.size == catCombo.categories()!!.size)
                                        presenter.setCatOptionCombo(catCombo.uid(), ArrayList(selectedOptions.values))
                                }
                                .show(this, catSelectorBinding.getRoot())
                    }
            )

            if (selectedOptions[category.uid()] != null)
                catSelectorBinding.catCombo.setText(selectedOptions[category.uid()]!!.displayName())

            binding.catComboLayout.addView(catSelectorBinding.getRoot())
        }*/
    }


}