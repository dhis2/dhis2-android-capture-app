package org.dhis2.usescases.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.toUiStringResource
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.dhis2.databinding.ActivityEventScheduledBinding
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvideInputDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.ProvidePeriodSelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.willShowCalendar
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import javax.inject.Inject

const val EXTRA_EVENT_UID = "EVENT_UID"

class ScheduledEventActivity :
    ActivityGlobalAbstract(),
    ScheduledEventContract.View {
    companion object {
        fun getIntent(
            context: Context,
            eventUid: String,
        ): Intent {
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
                        EXTRA_EVENT_UID,
                    )!!,
                    this,
                ),
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

    override fun setStage(
        programStage: ProgramStage,
        event: Event,
    ) {
        this.stage = programStage
        binding.programStage = programStage
        binding.scheduledEventFieldContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing16)) {
                    val eventDate =
                        EventDate(
                            label =
                                programStage.executionDateLabel()
                                    ?: getString(R.string.report_date),
                            dateValue = "",
                        )
                    val dueDate =
                        EventDate(
                            label = programStage.dueDateLabel() ?: getString(R.string.due_date),
                            dateValue = DateUtils.uiDateFormat().format(event.dueDate() ?: ""),
                        )

                    if (willShowCalendar(programStage.periodType())) {
                        ProvideInputDate(
                            EventInputDateUiModel(
                                eventDate = eventDate,
                                allowsManualInput = false,
                                detailsEnabled = true,
                                onDateClick = {},
                                onDateSelected = { date ->
                                    presenter.setEventDate(
                                        presenter.formatDateValues(
                                            date,
                                        ),
                                    )
                                },
                                selectableDates = presenter.getSelectableDates(program, false),
                            ),
                        )

                        if (programStage.hideDueDate() == false) {
                            ProvideInputDate(
                                EventInputDateUiModel(
                                    eventDate = dueDate,
                                    allowsManualInput = false,
                                    detailsEnabled = true,
                                    onDateClick = {},
                                    onDateSelected = { date ->
                                        presenter.setDueDate(presenter.formatDateValues(date))
                                    },
                                    selectableDates = presenter.getSelectableDates(program, true),
                                ),
                            )
                        }
                    } else {
                        ProvidePeriodSelector(
                            uiModel =
                                EventInputDateUiModel(
                                    eventDate = eventDate,
                                    detailsEnabled = true,
                                    onDateClick = {
                                        showPeriodDialog(
                                            periodType = programStage.periodType(),
                                            scheduling = false,
                                        )
                                    },
                                    onDateSelected = {},
                                    onClear = { },
                                    required = true,
                                    showField = true,
                                    selectableDates = presenter.getSelectableDates(program, false),
                                ),
                            modifier = Modifier,
                        )

                        if (programStage.hideDueDate() == false) {
                            ProvidePeriodSelector(
                                uiModel =
                                    EventInputDateUiModel(
                                        eventDate = dueDate,
                                        detailsEnabled = true,
                                        onDateClick = {
                                            showPeriodDialog(
                                                periodType = programStage.periodType(),
                                                scheduling = true,
                                            )
                                        },
                                        onDateSelected = {},
                                        onClear = { },
                                        required = true,
                                        showField = true,
                                        selectableDates = presenter.getSelectableDates(program, false),
                                    ),
                                modifier = Modifier,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun setProgram(program: Program) {
        this.program = program
        binding.name = program.displayName()
    }

    private fun showPeriodDialog(
        periodType: PeriodType?,
        scheduling: Boolean,
    ) {
        BottomSheetDialog(
            bottomSheetDialogUiModel =
                BottomSheetDialogUiModel(
                    title = getString((periodType ?: PeriodType.Daily).toUiStringResource()),
                    iconResource = -1,
                ),
            showTopDivider = true,
            showBottomDivider = true,
            content = { bottomSheetDialog, scrollState ->
                val periods = presenter.fetchPeriods(scheduling).collectAsLazyPagingItems()
                PeriodSelectorContent(
                    periods = periods,
                    scrollState = scrollState,
                ) { selectedPeriod ->
                    selectedPeriod.startDate.let {
                        presenter.setDueDate(it)
                    }
                    bottomSheetDialog.dismiss()
                }
            },
        ).show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun openFormActivity() {
        val bundle =
            EventCaptureActivity.getActivityBundle(
                event.uid(),
                program.uid(),
                EventMode.SCHEDULE,
            )
        Intent(activity, EventCaptureActivity::class.java).apply {
            putExtras(bundle)
            startActivity(this)
            finish()
        }
    }
}
