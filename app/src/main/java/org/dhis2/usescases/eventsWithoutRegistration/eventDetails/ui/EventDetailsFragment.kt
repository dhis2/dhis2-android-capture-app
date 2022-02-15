package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.databinding.EventDetailsFragmentBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.utils.Constants.ENROLLMENT_UID
import org.dhis2.utils.Constants.EVENT_CREATION_TYPE
import org.dhis2.utils.Constants.EVENT_PERIOD_TYPE
import org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL
import org.dhis2.utils.Constants.EVENT_UID
import org.dhis2.utils.Constants.PROGRAM_STAGE_UID
import org.dhis2.utils.Constants.PROGRAM_UID
import org.dhis2.utils.customviews.PeriodDialog
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date
import javax.inject.Inject

class EventDetailsFragment : Fragment() {

    @Inject
    lateinit var factory: EventDetailsViewModelFactory

    private val viewModel: EventDetailsViewModel by viewModels {
        factory
    }

    private lateinit var binding: EventDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as EventInitialActivity).eventInitialComponent.plus(
            EventDetailsModule(
                eventUid = requireArguments().getString(EVENT_UID),
                context = requireContext(),
                eventCreationType = getEventCreationType(
                    requireArguments().getString(EVENT_CREATION_TYPE)
                ),
                programStageUid = requireArguments().getString(PROGRAM_STAGE_UID)!!,
                programId = requireArguments().getString(PROGRAM_UID)!!,
                periodType = requireArguments()
                    .getSerializable(EVENT_PERIOD_TYPE) as PeriodType?,
                enrollmentId = requireArguments().getString(ENROLLMENT_UID),
                scheduleInterval = requireArguments().getInt(EVENT_SCHEDULE_INTERVAL)
            )
        ).inject(this)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.event_details_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showCalendar = {
            val dialog = CalendarPicker(requireContext())
            dialog.setInitialDate(viewModel.eventDate.value.currentDate)
            dialog.setMinDate(viewModel.eventDate.value.minDate)
            dialog.setMaxDate(viewModel.eventDate.value.maxDate)
            dialog.setScheduleInterval(viewModel.eventDate.value.scheduleInterval)
            dialog.isFutureDatesAllowed(viewModel.eventDate.value.allowFutureDates)
            dialog.setListener(
                object : OnDatePickerListener {
                    override fun onNegativeClick() {}
                    override fun onPositiveClick(datePicker: DatePicker) {
                        viewModel.onDateSet(
                            datePicker.year,
                            datePicker.month,
                            datePicker.dayOfMonth
                        )
                    }
                }
            )
            dialog.show()
        }

        viewModel.showPeriods = {
            PeriodDialog()
                .setPeriod(viewModel.eventDate.value.periodType)
                .setMinDate(viewModel.eventDate.value.minDate)
                .setMaxDate(viewModel.eventDate.value.maxDate)
                .setPossitiveListener { selectedDate: Date ->
                    viewModel.onDateSet(selectedDate)
                }
                .show(requireActivity().supportFragmentManager, PeriodDialog::class.java.simpleName)
        }
    }

    private fun getEventCreationType(typeString: String?): EventCreationType {
        return typeString?.let {
            EventCreationType.valueOf(it)
        } ?: EventCreationType.DEFAULT
    }
}
