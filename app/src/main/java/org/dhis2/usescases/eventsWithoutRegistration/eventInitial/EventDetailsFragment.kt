package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.dhis2.R
import org.dhis2.data.forms.dataentry.FormView
import org.dhis2.databinding.EventDetailsFragmentBinding
import org.dhis2.form.data.FormRepository
import org.dhis2.utils.Constants
import javax.inject.Inject

class EventDetailsFragment : Fragment() {

    @Inject
    lateinit var factory: EventDetailsViewModelFactory

    @Inject
    lateinit var repository: FormRepository

    private val viewModel: EventDetailsViewModel by viewModels {
        factory
    }

    private lateinit var binding: EventDetailsFragmentBinding
    private lateinit var formView: FormView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as EventInitialActivity).eventInitialComponent.plus(
            EventDetailsModule(
                eventUid = requireArguments().getString(Constants.EVENT_UID)!!,
                context = requireContext(),
                eventCreationType = requireArguments().getString(Constants.EVENT_CREATION_TYPE)
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
        formView = FormView.Builder()
            .repository(repository)
            .factory(requireActivity().supportFragmentManager)
            .build()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.formViewContainer, formView).commit()
    }
}