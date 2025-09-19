package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import org.dhis2.bindings.app
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.date.toUiStringResource
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.dhis2.form.R
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.hisp.dhis.android.core.period.PeriodType
import javax.inject.Inject

class SchedulingDialog : BottomSheetDialogFragment() {
    companion object {
        const val SCHEDULING_DIALOG = "SCHEDULING_DIALOG"
        const val SCHEDULING_DIALOG_RESULT = "SCHEDULING_DIALOG_RESULT"
        const val SCHEDULING_EVENT_SKIPPED = "SCHEDULING_EVENT_SKIPPED"
        const val SCHEDULING_EVENT_DUE_DATE_UPDATED = "SCHEDULING_EVENT_DUE_DATE_UPDATED"
        const val PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID"
        const val EVENT_LABEL = "EVENT_LABEL"

        private const val TAG_LAUNCH_MODE = "LAUNCH_MODE"

        fun newSchedule(
            enrollmentUid: String,
            programStagesUids: List<String>,
            ownerOrgUnitUid: String?,
            showYesNoOptions: Boolean,
            eventCreationType: EventCreationType,
        ): SchedulingDialog {
            val launchMode =
                LaunchMode.NewSchedule(
                    enrollmentUid = enrollmentUid,
                    programStagesUids = programStagesUids,
                    ownerOrgUnitUid = ownerOrgUnitUid,
                    showYesNoOptions = showYesNoOptions,
                    eventCreationType = eventCreationType,
                )

            return SchedulingDialog().apply {
                arguments =
                    bundleOf(
                        TAG_LAUNCH_MODE to launchMode,
                    )
            }
        }

        fun enterEvent(
            eventUid: String,
            showYesNoOptions: Boolean,
            eventCreationType: EventCreationType,
        ): SchedulingDialog {
            val launchMode =
                LaunchMode.EnterEvent(
                    eventUid = eventUid,
                    showYesNoOptions = showYesNoOptions,
                    eventCreationType = eventCreationType,
                )

            return SchedulingDialog().apply {
                arguments =
                    bundleOf(
                        TAG_LAUNCH_MODE to launchMode,
                    )
            }
        }
    }

    private lateinit var launchMode: LaunchMode

    @Inject
    lateinit var factory: SchedulingViewModelFactory.Factory

    val viewModel: SchedulingViewModel by viewModels {
        factory.build(launchMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        val arguments = arguments
        if (arguments != null) {
            launchMode = LaunchMode.fromBundle(arguments)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app().userComponent()?.plus(SchedulingModule())?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel.onEventScheduled = {
            setFragmentResult(SCHEDULING_DIALOG_RESULT, bundleOf(PROGRAM_STAGE_UID to it))
            dismiss()
        }

        viewModel.onEventSkipped = {
            setFragmentResult(SCHEDULING_EVENT_SKIPPED, bundleOf(EVENT_LABEL to it))
            dismiss()
        }

        viewModel.onDueDateUpdated = {
            setFragmentResult(SCHEDULING_EVENT_DUE_DATE_UPDATED, bundleOf())
            dismiss()
        }

        viewModel.onEnterEvent = { eventUid, programUid ->
            val bundle =
                EventCaptureActivity.getActivityBundle(
                    eventUid,
                    programUid,
                    EventMode.SCHEDULE,
                )
            Intent(activity, EventCaptureActivity::class.java).apply {
                putExtras(bundle)
                startActivity(this)
            }

            dismiss()
        }

        viewModel.showCalendar = {
            showCalendarDialog()
        }

        viewModel.showPeriods = { periodType ->
            showPeriodDialog(periodType)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                SchedulingDialogUi(
                    viewModel = viewModel,
                    launchMode = launchMode,
                    onDismiss = { dismiss() },
                )
            }
        }
    }

    private fun showCalendarDialog() {
        val dialog = CalendarPicker(requireContext())
        dialog.setInitialDate(viewModel.eventDate.value.currentDate)
        dialog.setMinDate(viewModel.eventDate.value.minDate)
        dialog.setMaxDate(viewModel.eventDate.value.maxDate)
        dialog.isFutureDatesAllowed(viewModel.eventDate.value.allowFutureDates)
        dialog.setListener(
            object : OnDatePickerListener {
                override fun onNegativeClick() {
                    // Unused
                }

                override fun onPositiveClick(datePicker: DatePicker) {
                    viewModel.onDateSet(
                        datePicker.year,
                        datePicker.month,
                        datePicker.dayOfMonth,
                    )
                }
            },
        )
        dialog.show()
    }

    private fun showPeriodDialog(periodType: PeriodType) {
        BottomSheetDialog(
            bottomSheetDialogUiModel =
                BottomSheetDialogUiModel(
                    title = getString(periodType.toUiStringResource()),
                    iconResource = -1,
                ),
            onSecondaryButtonClicked = {
            },
            onMainButtonClicked = { _ ->
            },
            showTopDivider = true,
            showBottomDivider = true,
            content = { bottomSheetDialog, scrollState ->
                val periods = viewModel.fetchPeriods().collectAsLazyPagingItems()
                PeriodSelectorContent(
                    periods = periods,
                    scrollState = scrollState,
                ) { period ->
                    period.startDate.let {
                        viewModel.setUpEventReportDate(it)
                    }
                    bottomSheetDialog.dismiss()
                }
            },
        ).show(childFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    sealed interface LaunchMode : Parcelable {
        companion object {
            fun fromBundle(args: Bundle): LaunchMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    args.getParcelable(TAG_LAUNCH_MODE, LaunchMode::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    args.getParcelable(TAG_LAUNCH_MODE)!!
                }
        }

        val showYesNoOptions: Boolean
        val eventCreationType: EventCreationType

        @Parcelize
        data class NewSchedule(
            val enrollmentUid: String,
            val programStagesUids: List<String>,
            val ownerOrgUnitUid: String?,
            override val showYesNoOptions: Boolean,
            override val eventCreationType: EventCreationType,
        ) : LaunchMode

        @Parcelize
        data class EnterEvent(
            val eventUid: String,
            override val showYesNoOptions: Boolean,
            override val eventCreationType: EventCreationType,
        ) : LaunchMode
    }
}
