package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.map
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.Constants
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext.EnrollmentEvent
import org.dhis2.databinding.FragmentTeiDataBinding
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.DashboardTEIModel
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventAdapter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventCatComboOptionSelector
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.ui.mapper.TEIEventCardMapper
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog.Companion.PROGRAM_STAGE_UID
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog.Companion.SCHEDULING_DIALOG
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog.Companion.SCHEDULING_DIALOG_RESULT
import org.dhis2.usescases.teiDashboard.ui.TeiDetailDashboard
import org.dhis2.usescases.teiDashboard.ui.mapper.InfoBarMapper
import org.dhis2.usescases.teiDashboard.ui.mapper.TeiDashboardCardMapper
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarType
import org.dhis2.usescases.teiDashboard.ui.model.TimelineEventsHeaderModel
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import timber.log.Timber
import javax.inject.Inject

class TEIDataFragment : FragmentGlobalAbstract(), TEIDataContracts.View {

    lateinit var binding: FragmentTeiDataBinding

    @Inject
    lateinit var presenter: TEIDataPresenter

    @Inject
    lateinit var colorUtils: ColorUtils

    @Inject
    lateinit var teiDashboardCardMapper: TeiDashboardCardMapper

    @Inject
    lateinit var infoBarMapper: InfoBarMapper

    @Inject
    lateinit var contractHandler: TeiDataContractHandler

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var cardMapper: TEIEventCardMapper

    private var eventAdapter: EventAdapter? = null
    private var dialog: CustomDialog? = null
    private var programStageFromEvent: ProgramStage? = null
    private var eventCatComboOptionSelector: EventCatComboOptionSelector? = null
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val dashboardActivity: TeiDashboardMobileActivity by lazy { context as TeiDashboardMobileActivity }

    private var showAllEnrollment = false
    private var programUid: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        with(requireArguments()) {
            programUid = getString("PROGRAM_UID")
            val teiUid = getString("TEI_UID")
                ?: throw NullPointerException("A TEI uid is required to launch fragment")
            val enrollmentUid = getString("ENROLLMENT_UID") ?: ""
            app().dashboardComponent()?.plus(
                TEIDataModule(
                    this@TEIDataFragment,
                    programUid,
                    teiUid,
                    enrollmentUid,
                    requireActivity().activityResultRegistry,
                ),
            )?.inject(this@TEIDataFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentTeiDataBinding.inflate(inflater, container, false).also { binding ->
            this.binding = binding
            dashboardViewModel.groupByStage.observe(viewLifecycleOwner) { group ->
                showLoadingProgress(false)
                presenter.onGroupingChanged(group)
            }

            with(dashboardViewModel) {
                eventUid().observe(viewLifecycleOwner, ::displayGenerateEvent)
                noEnrollmentSelected.observe(viewLifecycleOwner) { noEnrollmentSelected ->
                    if (noEnrollmentSelected) {
                        showAllEnrollment = true
                        showLegacyCard(dashboardModel.value as DashboardTEIModel)
                    } else {
                        showDetailCard()
                    }
                }
                dashboardModel.observe(viewLifecycleOwner) {
                    if (sharedPreferences.getString(PREF_COMPLETED_EVENT, null) != null) {
                        presenter.displayGenerateEvent(
                            sharedPreferences.getString(
                                PREF_COMPLETED_EVENT,
                                null,
                            ),
                        )
                        sharedPreferences.edit().remove(PREF_COMPLETED_EVENT).apply()
                    }
                }
            }

            presenter.events.observe(viewLifecycleOwner) {
                setEvents(it)
                showLoadingProgress(false)
            }

            setFragmentResultListener(SCHEDULING_DIALOG_RESULT) { _, bundle ->
                showToast(
                    resourceManager.formatWithEventLabel(
                        R.string.event_label_created,
                        bundle.getString(PROGRAM_STAGE_UID),
                    ),
                )
                presenter.fetchEvents()
            }
        }.root
    }

    private fun showDetailCard() {
        binding.detailCard.setContent {
            val dashboardModel by dashboardViewModel.dashboardModel.observeAsState()
            val followUp by dashboardViewModel.showFollowUpBar.collectAsState()
            val syncNeeded by dashboardViewModel.syncNeeded.collectAsState()
            val enrollmentStatus by dashboardViewModel.showStatusBar.collectAsState()
            val groupingEvents by dashboardViewModel.groupByStage.observeAsState()
            val displayEventCreationButton by presenter.shouldDisplayEventCreationButton.observeAsState(
                false,
            )
            val eventCount by presenter.events.map { it.count() }.observeAsState(0)

            val syncInfoBar = dashboardModel.takeIf { it is DashboardEnrollmentModel }?.let {
                infoBarMapper.map(
                    infoBarType = InfoBarType.SYNC,
                    item = dashboardModel as DashboardEnrollmentModel,
                    actionCallback = { dashboardActivity.openSyncDialog() },
                    showInfoBar = syncNeeded,
                )
            }

            val followUpInfoBar =
                dashboardModel.takeIf { it is DashboardEnrollmentModel }?.let {
                    infoBarMapper.map(
                        infoBarType = InfoBarType.FOLLOW_UP,
                        item = dashboardModel as DashboardEnrollmentModel,
                        actionCallback = {
                            dashboardViewModel.onFollowUp()
                        },
                        showInfoBar = followUp,
                    )
                }
            val enrollmentInfoBar =
                dashboardModel.takeIf { it is DashboardEnrollmentModel }?.let {
                    infoBarMapper.map(
                        infoBarType = InfoBarType.ENROLLMENT_STATUS,
                        item = dashboardModel as DashboardEnrollmentModel,
                        actionCallback = { },
                        showInfoBar = enrollmentStatus != EnrollmentStatus.ACTIVE,
                    )
                }

            val card = dashboardModel?.let {
                teiDashboardCardMapper.map(
                    dashboardModel = it,
                    onImageClick = { fileToShow ->
                        val intent = ImageDetailActivity.intent(
                            context = requireActivity(),
                            title = null,
                            imagePath = fileToShow.path,
                        )

                        startActivity(intent)
                    },
                    phoneCallback = { openChooser(it, Intent.ACTION_DIAL) },
                    emailCallback = { openChooser(it, Intent.ACTION_SENDTO) },
                    programsCallback = {
                        startActivity(
                            TeiDashboardMobileActivity.intent(
                                dashboardActivity.context,
                                dashboardActivity.teiUid,
                                null,
                                null,
                            ),
                        )
                    },
                )
            }

            TeiDetailDashboard(
                syncData = syncInfoBar,
                followUpData = followUpInfoBar,
                enrollmentData = enrollmentInfoBar,
                card = card,
                isGrouped = groupingEvents ?: true,
                timelineEventHeaderModel = TimelineEventsHeaderModel(
                    displayEventCreationButton,
                    eventCount,
                    presenter.getNewEventOptionsByStages(null),
                ),
                timelineOnEventCreationOptionSelected = {
                    presenter.onAddNewEventOptionSelected(it, null)
                },
            )
        }
    }

    private fun showLegacyCard(dashboardModel: DashboardTEIModel?) {
        binding.noEnrollmentSelected = true
        if (dashboardModel?.avatarPath.isNullOrEmpty()) {
            binding.cardFront.teiImage.visibility = View.GONE
        } else {
            binding.cardFront.teiImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(dashboardModel?.avatarPath)
                .fallback(R.drawable.photo_temp_gray)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(binding.cardFront.teiImage)
        }
        binding.header = when {
            !dashboardModel?.teiHeader.isNullOrEmpty() -> {
                dashboardModel?.teiHeader
            }

            else -> {
                String.format(
                    "%s %s",
                    if (dashboardModel?.getTrackedEntityAttributeValueBySortOrder(1) != null) {
                        dashboardModel.getTrackedEntityAttributeValueBySortOrder(1)
                    } else {
                        ""
                    },
                    if (dashboardModel?.getTrackedEntityAttributeValueBySortOrder(2) != null) {
                        dashboardModel.getTrackedEntityAttributeValueBySortOrder(2)
                    } else {
                        ""
                    },
                )
            }
        }
        binding.teiRecycler.adapter = DashboardProgramAdapter(presenter, dashboardModel!!)
        binding.teiRecycler.addItemDecoration(
            DividerItemDecoration(abstracContext, DividerItemDecoration.VERTICAL),
        )
        showLoadingProgress(false)
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        if (!showAllEnrollment) {
            dashboardViewModel.updateDashboard()
        }
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    private fun openChooser(value: String, action: String) {
        val intent = Intent(action).apply {
            when (action) {
                Intent.ACTION_DIAL -> {
                    data = Uri.parse("tel:$value")
                }

                Intent.ACTION_SENDTO -> {
                    data = Uri.parse("mailto:$value")
                }
            }
        }
        val title = resources.getString(R.string.open_with)
        val chooser = Intent.createChooser(intent, title)

        try {
            startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Timber.e("No activity found that can handle this action")
        }
    }

    override fun observeStageSelection(
        currentProgram: Program,
    ): Flowable<StageSection> {
        if (eventAdapter == null) {
            eventAdapter = EventAdapter(
                presenter,
                currentProgram,
                colorUtils,
                cardMapper,
            )
            binding.teiRecycler.adapter = eventAdapter
        }
        return eventAdapter?.stageSelector() ?: Flowable.empty()
    }

    override fun setEvents(events: List<EventViewModel>) {
        if (events.isEmpty()) {
            binding.emptyTeis.visibility = View.VISIBLE
            binding.teiRecycler.visibility = View.GONE

            if (presenter.shouldDisplayEventCreationButton.value == true) {
                binding.emptyTeis.setText(R.string.empty_tei_add)
            } else {
                binding.emptyTeis.setText(R.string.empty_tei_no_add)
            }
        } else {
            binding.emptyTeis.visibility = View.GONE
            binding.teiRecycler.visibility = View.VISIBLE

            eventAdapter?.submitList(events)
            for (eventViewModel in events) {
                if (eventViewModel.isAfterToday(DateUtils.getInstance().today)) {
                    binding.teiRecycler.scrollToPosition(events.indexOf(eventViewModel))
                }
            }
        }
    }

    private fun showLoadingProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.loadingProgress.root.visibility = View.VISIBLE
        } else {
            binding.loadingProgress.root.visibility = View.GONE
        }
    }

    override fun displayScheduleEvent() {
        val model = dashboardViewModel.dashboardModel.value
        if (model is DashboardEnrollmentModel) {
            SchedulingDialog.newInstance(
                enrollment = model.currentEnrollment,
                programStages = presenter.filterAvailableStages(model.programStages),
            ).show(parentFragmentManager, SCHEDULING_DIALOG)
        }
    }

    override fun showDialogCloseProgram() {
        dialog = CustomDialog(
            requireContext(),
            resourceManager.formatWithEventLabel(
                R.string.event_label_completed,
                programStageFromEvent?.uid(),
            ),
            resourceManager.formatWithEnrollmentLabel(
                programUid = programUid,
                stringResource = R.string.complete_enrollment_message_V2,
                quantity = 1,
            ),
            getString(R.string.button_ok),
            getString(R.string.cancel),
            RC_EVENTS_COMPLETED,
            object : DialogClickListener {
                override fun onPositive() {
                    presenter.completeEnrollment()
                }

                override fun onNegative() {
                    // Not necessary for this implementation
                }
            },
        )
        dialog?.show()
    }

    override fun areEventsCompleted(): Consumer<Single<Boolean>> {
        return Consumer { eventsCompleted: Single<Boolean> ->
            if (eventsCompleted.blockingGet()) {
                dialog = CustomDialog(
                    requireContext(),
                    getString(R.string.event_completed_title),
                    getString(R.string.event_completed_message),
                    getString(R.string.button_ok),
                    getString(R.string.cancel),
                    RC_EVENTS_COMPLETED,
                    object : DialogClickListener {
                        override fun onPositive() {
                            presenter.completeEnrollment()
                        }

                        override fun onNegative() {
                            // Not necessary for this implementation
                        }
                    },
                )
                dialog?.show()
            }
        }
    }

    override fun viewLifecycleOwner(): LifecycleOwner {
        return this.viewLifecycleOwner
    }

    override fun displayGenerateEvent(eventUid: String) {
        presenter.displayGenerateEvent(eventUid)
        dashboardViewModel.updateEventUid(null)
    }

    override fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String) {
        dashboardActivity.startActivity(
            TeiDashboardMobileActivity.intent(
                activity,
                teiUid,
                programUid,
                enrollmentUid,
            ),
        )
        dashboardActivity.finish()
    }

    override fun openEventDetails(intent: Intent, options: ActivityOptionsCompat) =
        contractHandler.scheduleEvent(intent, options).observe(viewLifecycleOwner) {
            presenter.fetchEvents()
        }

    override fun openEventInitial(intent: Intent) =
        contractHandler.editEvent(intent).observe(viewLifecycleOwner) {
            presenter.fetchEvents()
        }

    override fun openEventCapture(intent: Intent) =
        contractHandler.editEvent(intent).observe(viewLifecycleOwner) {
            presenter.fetchEvents()
        }

    override fun goToEventInitial(
        eventCreationType: EventCreationType,
        programStage: ProgramStage,
    ) {
        val model = dashboardViewModel.dashboardModel.value
        if (model is DashboardEnrollmentModel) {
            val intent = Intent(activity, EventInitialActivity::class.java)
            val bundle = Bundle()

            bundle.putString(Constants.PROGRAM_UID, model.currentProgram().uid())
            bundle.putString(
                Constants.TRACKED_ENTITY_INSTANCE,
                model.trackedEntityInstance.uid(),
            )
            model.getCurrentOrgUnit().uid()?.takeIf(presenter::enrollmentOrgUnitInCaptureScope)
                ?.let {
                    bundle.putString(Constants.ORG_UNIT, it)
                }
            bundle.putString(Constants.ENROLLMENT_UID, model.currentEnrollment.uid())
            bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
            bundle.putBoolean(Constants.EVENT_REPEATABLE, programStage.repeatable() ?: false)
            bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, programStage.periodType())
            bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid())
            bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, programStage.standardInterval() ?: 0)
            intent.putExtras(bundle)
            contractHandler.createEvent(intent)
        }
    }

    override fun goToEventDetails(
        eventUid: String,
        eventMode: EventMode,
        programUid: String,
    ) {
        val intent = EventCaptureActivity.intent(
            context = requireContext(),
            eventUid = eventUid,
            programUid = programUid,
            eventMode = eventMode,
        )
        startActivity(intent)
    }

    override fun displayOrgUnitSelectorForNewEvent(programUid: String, programStageUid: String) {
        OUTreeFragment.Builder()
            .showAsDialog()
            .singleSelection()
            .orgUnitScope(
                OrgUnitSelectorScope.ProgramCaptureScope(programUid),
            )
            .onSelection { selectedOrgUnits ->
                if (selectedOrgUnits.isNotEmpty()) {
                    presenter.onOrgUnitForNewEventSelected(
                        orgUnitUid = selectedOrgUnits.first().uid(),
                        programStageUid = programStageUid,
                    )
                }
            }
            .build()
            .show(parentFragmentManager, "ORG_UNIT_DIALOG")
    }

    override fun showSyncDialog(eventUid: String, enrollmentUid: String) {
        SyncStatusDialog.Builder()
            .withContext(this, null)
            .withSyncContext(
                EnrollmentEvent(eventUid, enrollmentUid),
            )
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) FilterManager.getInstance().publishData()
                }
            })
            .onNoConnectionListener {
                val contextView = activity?.findViewById<View>(R.id.navigationBar)
                Snackbar.make(
                    contextView!!,
                    R.string.sync_offline_check_connection,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }.show(enrollmentUid)
    }

    override fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>) {
        eventCatComboOptionSelector?.setEventsWithoutCatComboOption(data)
        eventCatComboOptionSelector?.requestCatComboOption(presenter::changeCatOption)
    }

    override fun showProgramRuleErrorMessage() {
        dashboardActivity.runOnUiThread {
            showDescription(getString(R.string.error_applying_rule_effects))
        }
    }

    companion object {
        const val RC_EVENTS_COMPLETED = 1601
        const val PREF_COMPLETED_EVENT = "COMPLETED_EVENT"

        @JvmStatic
        fun newInstance(
            programUid: String?,
            teiUid: String?,
            enrollmentUid: String?,
        ): TEIDataFragment {
            val fragment = TEIDataFragment()
            val args = Bundle()
            args.putString("PROGRAM_UID", programUid)
            args.putString("TEI_UID", teiUid)
            args.putString("ENROLLMENT_UID", enrollmentUid)
            fragment.arguments = args
            return fragment
        }
    }
}
