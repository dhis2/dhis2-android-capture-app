package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.bindings.app
import org.dhis2.commons.dialogs.PeriodDialog
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.form.R
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date
import javax.inject.Inject

class SchedulingDialog(
    val enrollment: Enrollment,
    val programStages: List<ProgramStage>,
    val onScheduled: (String) -> Unit,
) : BottomSheetDialogFragment() {
    companion object {
        const val SCHEDULING_DIALOG = "SCHEDULING_DIALOG"
    }

    @Inject
    lateinit var factory: SchedulingViewModelFactory
    val viewModel: SchedulingViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        app().userComponent()?.plus(
            SchedulingModule(
                enrollment,
                programStages,
            ),
        )?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel.showCalendar = {
            showCalendarDialog()
        }

        viewModel.showPeriods = {
            showPeriodDialog()
        }

        viewModel.onEventScheduled = {
            dismiss()
            onScheduled(viewModel.programStage.value.uid())
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                SchedulingDialogUi(
                    viewModel = viewModel,
                    programStages = programStages,
                    orgUnitUid = enrollment.organisationUnit(),
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

    private fun showPeriodDialog() {
        PeriodDialog()
            .setPeriod(viewModel.eventDate.value.periodType)
            .setMinDate(viewModel.eventDate.value.minDate)
            .setMaxDate(viewModel.eventDate.value.maxDate)
            .setPossitiveListener { selectedDate: Date ->
                viewModel.setUpEventReportDate(selectedDate)
            }
            .show(requireActivity().supportFragmentManager, PeriodDialog::class.java.simpleName)
    }
}
