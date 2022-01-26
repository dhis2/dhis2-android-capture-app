package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.dhis2.R
import org.dhis2.databinding.EventDetailsFragmentBinding
import javax.inject.Inject

class EventDetailsFragment : Fragment() {

    @Inject
    lateinit var factory: EventDetailsViewModelFactory

    private val viewModel: EventDetailsViewModel by viewModels {
        factory
    }

    private lateinit var binding: EventDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as EventInitialActivity).eventInitialComponent.plus(
            EventDetailsModule(requireArguments().getString("eventUid")!!)
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

}