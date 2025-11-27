package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.databinding.SectionSelectorFragmentBinding
import org.dhis2.form.model.EventMode
import org.dhis2.form.model.EventRecords
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FormView
import org.dhis2.mobile.commons.ui.NonEditableReasonBlock
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureAction
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.granularsync.OPEN_ERROR_LOCATION
import javax.inject.Inject

class EventCaptureFormFragment :
    FragmentGlobalAbstract(),
    EventCaptureFormView {
    @Inject
    lateinit var presenter: EventCaptureFormPresenter

    private lateinit var activity: EventCaptureActivity

    private lateinit var binding: SectionSelectorFragmentBinding

    private var formView: FormView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.activity = context as EventCaptureActivity
        activity.eventCaptureComponent
            ?.plus(
                EventCaptureFormModule(
                    this,
                    arguments?.getString(Constants.EVENT_UID)!!,
                ),
            )?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val eventUid = requireArguments().getString(Constants.EVENT_UID, "")
        val eventMode = arguments?.getString(Constants.EVENT_MODE)?.let { EventMode.valueOf(it) }
        val eventStatus = presenter.getEventStatus(eventUid)
        formView =
            FormView
                .Builder()
                .locationProvider(locationProvider)
                .onLoadingListener { loading: Boolean ->
                    if (loading) {
                        activity.showProgress()
                    } else {
                        activity.hideProgress()
                    }
                }.onItemChangeListener { action: RowAction ->
                    if (action.isEventDetailsRow) {
                        presenter.showOrHideSaveButton()
                    }
                    Unit
                }.onFinishDataEntry { presenter.saveAndExit(eventStatus) }
                .onFocused {
                    activity.hideNavigationBar()
                }.onPercentageUpdate { percentage: Float? ->
                    activity.updatePercentage(percentage!!)
                }.factory(activity.supportFragmentManager)
                .setRecords(EventRecords(eventUid, eventMode ?: EventMode.CHECK))
                .openErrorLocation(requireArguments().getBoolean(OPEN_ERROR_LOCATION, false))
                .setProgramUid(presenter.getEvent()?.program())
                .build()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.section_selector_fragment, container, false)
        val activityPresenter = activity.presenter
        binding.setPresenter(activityPresenter)

        activityPresenter.observeActions().observe(
            viewLifecycleOwner,
        ) { action: EventCaptureAction ->
            if (action == EventCaptureAction.ON_BACK) {
                formView?.onBackPressed()
                activityPresenter.emitAction(EventCaptureAction.NONE)
            }
        }

        binding.actionButton.setOnClickListener { view ->
            view.closeKeyboard()
            performSaveClick()
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.formViewContainer, formView!!).commit()
        formView!!.scrollCallback = { isSectionVisible: Boolean ->
            animateFabButton(isSectionVisible)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.showOrHideSaveButton()
    }

    private fun animateFabButton(sectionIsVisible: Boolean) {
        var translationX = 1000
        if (sectionIsVisible) translationX = 0

        binding.actionButton
            .animate()
            .translationX(translationX.toFloat())
            .setDuration(500)
            .start()
    }

    override fun performSaveClick() {
        formView?.onSaveClick()
    }

    override fun hideSaveButton() {
        binding.actionButton.visibility = View.GONE
    }

    override fun showSaveButton() {
        binding.actionButton.visibility = View.VISIBLE
    }

    override fun onReopen() {
        formView?.reload()
    }

    override fun showNonEditableMessage(
        reason: String,
        canBeReOpened: Boolean,
    ) {
        binding.editableReasonContainer.visibility = View.VISIBLE

        binding.editableReasonContainer.setContent {
            NonEditableReasonBlock(
                modifier = Modifier.fillMaxWidth(),
                reason = reason,
                canBeReopened = canBeReOpened,
                onReopenClick = presenter::reOpenEvent,
            )
        }
    }

    override fun hideNonEditableMessage() {
        binding.editableReasonContainer.visibility = View.GONE
    }

    companion object {
        fun newInstance(
            eventUid: String?,
            openErrorSection: Boolean?,
            eventMode: EventMode,
        ): EventCaptureFormFragment {
            val fragment = EventCaptureFormFragment()
            val args = Bundle()
            args.putString(Constants.EVENT_UID, eventUid)
            openErrorSection?.let { args.putBoolean(OPEN_ERROR_LOCATION, it) }
            args.putString(Constants.EVENT_MODE, eventMode.name)
            fragment.arguments = args
            return fragment
        }
    }
}
