package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.databinding.EventDetailsFragmentBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.period.PeriodType

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
                eventUid = requireArguments().getString(Constants.EVENT_UID),
                context = requireContext(),
                eventCreationType = getEventCreationType(
                    requireArguments().getString(Constants.EVENT_CREATION_TYPE)
                ),
                programStageUid = requireArguments().getString(Constants.PROGRAM_STAGE_UID)!!,
                programId = requireArguments().getString(Constants.PROGRAM_UID)!!,
                periodType = requireArguments()
                    .getSerializable(Constants.EVENT_PERIOD_TYPE) as PeriodType?
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

    private fun getEventCreationType(typeString: String?): EventCreationType {
        return typeString?.let {
            EventCreationType.valueOf(it)
        } ?: EventCreationType.DEFAULT
    }
}
