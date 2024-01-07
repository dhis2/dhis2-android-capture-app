package org.dhis2.usescases.eventswithoutregistration.eventteidetails

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.orgunitselector.OUTreeFragment.Companion.newInstance
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ObjectStyleUtils.getIconResource
import org.dhis2.commons.sync.ConflictType
import org.dhis2.databinding.FragmentEventTeiDetailsBinding
import org.dhis2.usescases.eventswithoutregistration.eventcapture.EventCaptureActivity
import org.dhis2.usescases.eventswithoutregistration.eventinitial.EventInitialActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity
import org.dhis2.usescases.teidashboard.DashboardProgramModel
import org.dhis2.usescases.teidashboard.DashboardViewModel
import org.dhis2.usescases.teidashboard.TeiDashboardMobileActivity.Companion.intent
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.DashboardProgramAdapter
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.TEIDataContracts
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.TEIDataModule
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.teievents.CategoryDialogInteractions
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.teievents.EventAdapter
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.teievents.EventCatComboOptionSelector
import org.dhis2.usescases.teidashboard.ui.TeiDetailDashboard
import org.dhis2.usescases.teidashboard.ui.mapper.InfoBarMapper
import org.dhis2.usescases.teidashboard.ui.mapper.TeiDashboardCardMapper
import org.dhis2.usescases.teidashboard.ui.model.InfoBarType
import org.dhis2.utils.CustomComparator
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.CREATE_EVENT_TEI
import org.dhis2.utils.analytics.TYPE_EVENT_TEI
import org.dhis2.utils.category.CategoryDialog
import org.dhis2.utils.category.CategoryDialog.Companion.TAG
import org.dhis2.utils.dialFloatingActionButton.DialItem
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.isLandscape
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber
import java.io.File
import java.util.Collections
import java.util.Date
import java.util.stream.Collectors
import javax.inject.Inject

class EventTeiDetailsFragment : FragmentGlobalAbstract(), TEIDataContracts.View {
    private var binding: FragmentEventTeiDetailsBinding? = null

    @Inject
    lateinit var presenter: TEIDataPresenter

    @JvmField
    @Inject
    var filterManager: FilterManager? = null

    @JvmField
    @Inject
    var filtersAdapter: FiltersAdapter? = null

    @Inject
    lateinit var teiDashboardCardMapper: TeiDashboardCardMapper

    @Inject
    lateinit var infoBarMapper: InfoBarMapper

    @Inject
    lateinit var colorUtils: ColorUtils

    private var adapter: EventAdapter? = null
    private var dialog: CustomDialog? = null
    private var programStageFromEvent: ProgramStage? = null
    private val followUp = ObservableBoolean(false)
    private var context: Context? = null
    private var dashboardModel: DashboardProgramModel? = null
    private var activity: EventCaptureActivity? = null
    var teiUid: String? = null
    var programUid: String? = null
    var enrollmentUid: String? = null
    var programStageUid: String? = null
    var eventUid: String? = null
    var programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute?>? = null
    private var internalAttributeValues: List<TrackedEntityAttributeValue?>? = null
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private var eventCatComboOptionSelector: EventCatComboOptionSelector? = null
    private val dashboardActivity: EventCaptureActivity by lazy { context as EventCaptureActivity }
    override fun onAttach(context: Context) {
        teiUid = requireArguments().getString("TEI_UID")
        enrollmentUid = requireArguments().getString("ENROLLMENT_UID")
        programUid = requireArguments().getString("PROGRAM_UID")
        eventUid = requireArguments().getString("EVENT_UID")
        programStageUid = requireArguments().getString(Constants.PROGRAM_STAGE_UID)
        super.onAttach(context)
        this.context = context
        activity = context as EventCaptureActivity
        (context.getApplicationContext() as App)
                .dashboardComponent()
                ?.plus(
                        TEIDataModule(
                                this,
                                requireArguments().getString("PROGRAM_UID"),
                                requireArguments().getString("TEI_UID")!!,
                                requireArguments().getString("ENROLLMENT_UID")!!,
                        ),
                )
                ?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_tei_details, container, false)
        binding!!.presenter = presenter
        try {
            binding!!.filterLayout.adapter = filtersAdapter
        } catch (e: Exception) {
            Timber.e(e)
        }
        return binding!!.root
    }

    private fun updateFabItems() {
        val dialItems: MutableList<DialItem> = ArrayList()
        dialItems.add(
                DialItem(REFERAL_ID, getString(R.string.referral), R.drawable.ic_arrow_forward),
        )
        dialItems.add(
                DialItem(ADD_NEW_ID, getString(R.string.add_new), R.drawable.ic_note_add),
        )
        dialItems.add(
                DialItem(SCHEDULE_ID, getString(R.string.schedule_new), R.drawable.ic_date_range),
        )
        binding!!.dialFabLayout.addDialItems(dialItems) { clickedId: Int? ->
            when (clickedId) {
                REFERAL_ID -> createEvent(EventCreationType.REFERAL, 0)
                ADD_NEW_ID -> createEvent(EventCreationType.ADDNEW, 0)
                SCHEDULE_ID -> createEvent(EventCreationType.SCHEDULE, 0)
                else -> {}
            }
        }
    }

    override fun setEnrollment(enrollment: Enrollment) {
        binding!!.enrollment = enrollment
        if (adapter != null) {
            adapter!!.setEnrollment(enrollment)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        with(dashboardViewModel) {
            dashboardModel().observe(this@EventTeiDetailsFragment, ::setData)
            eventUid().observe(this@EventTeiDetailsFragment, ::displayGenerateEvent)
        }
    }

    override fun onPause() {
        presenter.setOpeningFilterToNone()
        presenter.onDettach()
        super.onPause()
    }

    override fun setEnrollmentData(program: Program?, enrollment: Enrollment?) {
        if (adapter != null) {
            adapter!!.setEnrollment(enrollment!!)
        }
        binding!!.program = program
        binding!!.enrollment = enrollment
        if (enrollment != null) {
            followUp.set(enrollment.followUp() != null && enrollment.followUp()!!)
        }
        binding!!.followup = followUp
        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }
        teiModel!!.setCurrentEnrollment(enrollment)
    }

    fun setData(dashboardModel: DashboardProgramModel) {
        this.dashboardModel = dashboardModel
        if (dashboardModel.currentEnrollment != null) {
            binding!!.dialFabLayout.setFabVisible(true)
            presenter.setDashboardProgram(dashboardModel)
            eventCatComboOptionSelector = EventCatComboOptionSelector(
                    dashboardModel.currentProgram.categoryComboUid(),
                    childFragmentManager,
                    object : CategoryDialogInteractions {},
            )
            binding!!.dashboardModel = dashboardModel
            updateFabItems()
            dashboardModel.teiHeader = presenter.getTeiHeader()
            dashboardModel.avatarPath = presenter.getTeiProfilePath()
            binding!!.detailCard.setContent {
                val followUp by dashboardViewModel.showFollowUpBar.collectAsState()
                val syncNeeded by dashboardViewModel.syncNeeded.collectAsState()
                val enrollmentStatus by dashboardViewModel.showStatusBar.collectAsState()
                val state by dashboardViewModel.state.collectAsState()
                dashboardModel.currentEnrollmentStatus = enrollmentStatus
                dashboardModel.enrollmentState = state
                val syncInfoBar = infoBarMapper.map(
                        infoBarType = InfoBarType.SYNC,
                        item = dashboardModel,
                        actionCallback = { dashboardActivity.showTeiSyncDialog() },
                        showInfoBar = syncNeeded,
                )
                val followUpInfoBar = infoBarMapper.map(
                        infoBarType = InfoBarType.FOLLOW_UP,
                        item = dashboardModel,
                        actionCallback = {
                            dashboardViewModel.onFollowUp(dashboardModel)
                        },
                        showInfoBar = followUp,
                )
                val enrollmentInfoBar = infoBarMapper.map(
                        infoBarType = InfoBarType.ENROLLMENT_STATUS,
                        item = dashboardModel,
                        actionCallback = { },
                        showInfoBar = enrollmentStatus != EnrollmentStatus.ACTIVE,
                )
                val card = teiDashboardCardMapper.map(
                        dashboardModel = dashboardModel,
                        onImageClick = { fileToShow ->
                            ImageDetailBottomDialog(
                                    null,
                                    fileToShow,
                            ).show(childFragmentManager, ImageDetailBottomDialog.TAG)
                        },
                        phoneCallback = { openChooser(it, Intent.ACTION_DIAL) },
                        emailCallback = { openChooser(it, Intent.ACTION_SENDTO) },
                        programsCallback = {
                            startActivity(
                                    intent(
                                            dashboardActivity.context,
                                            dashboardActivity.teiUid,
                                            null,
                                            null,
                                    ),
                            )
                        },
                )
                TeiDetailDashboard(
                        syncData = syncInfoBar,
                        followUpData = followUpInfoBar,
                        enrollmentData = enrollmentInfoBar,
                        card = card,
                )
            }
        } else {
            binding!!.dialFabLayout.setFabVisible(false)
            binding!!.teiRecycler.adapter = DashboardProgramAdapter(presenter, dashboardModel)
            binding!!.teiRecycler.addItemDecoration(DividerItemDecoration(abstracContext, DividerItemDecoration.VERTICAL))
            binding!!.dashboardModel = dashboardModel
            showLoadingProgress(false)
        }
        binding!!.executePendingBindings()
        if (sharedPreferences.getString(PREF_COMPLETED_EVENT, null) != null) {
            presenter.displayGenerateEvent(sharedPreferences.getString(PREF_COMPLETED_EVENT, null))
            sharedPreferences.edit().remove(PREF_COMPLETED_EVENT).apply()
        }
    }

    private var onActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            activity?.presenter?.init()
        }
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

    override fun hideFilters() {
        // No filters to hide
    }

    override fun observeStageSelection(currentProgram: Program, currentEnrollment: Enrollment): Flowable<StageSection> {
        if (adapter == null) {
            adapter = EventAdapter(presenter, currentProgram, colorUtils, stageSelected = programStageUid
                    ?: "", eventSelected = eventUid ?: "").also {
                it.setEnrollment(currentEnrollment)
            }
            binding?.teiRecycler?.adapter = adapter
        }
        return adapter?.stageSelector() ?: Flowable.empty()
    }

    override fun setEvents(events: List<EventViewModel>, canAddEvents: Boolean) {
        binding!!.canAddEvents = canAddEvents
        handleFabVisibility()
        handleEmptyTeiMessage(events)
        handleEventList(events)
        showLoadingProgress(false)
    }

    private fun handleFabVisibility() {
        if (isLandscape()) {
            requireActivity().findViewById<View>(R.id.dialFabLayout).visibility = View.GONE
        }
    }

    private fun handleEmptyTeiMessage(events: List<EventViewModel>) {
        if (events.isEmpty()) {
            binding!!.emptyTeis.visibility = View.VISIBLE
            val messageRes = if (binding!!.dialFabLayout.isFabVisible()) R.string.empty_tei_add else R.string.empty_tei_no_add
            binding!!.emptyTeis.setText(messageRes)
        } else {
            binding!!.emptyTeis.visibility = View.GONE
        }
    }

    private fun handleEventList(events: List<EventViewModel>) {
        adapter!!.submitList(events)
        for (eventViewModel in events) {
            handleEventViewModel(eventViewModel, events)
        }
    }

    private fun handleEventViewModel(eventViewModel: EventViewModel, events: List<EventViewModel>) {
        if (eventViewModel.type === EventViewModelType.EVENT) {
            val event = eventViewModel.event
            if (event!!.eventDate() != null && event.eventDate()!!.after(DateUtils.getInstance().today)) {
                binding!!.teiRecycler.scrollToPosition(events.indexOf(eventViewModel))
            }
        }
    }

    private fun showLoadingProgress(showProgress: Boolean) {
        if (showProgress) {
            binding!!.loadingProgress.root.visibility = View.VISIBLE
        } else {
            binding!!.loadingProgress.root.visibility = View.GONE
        }
    }

    override fun displayGenerateEvent(): Consumer<ProgramStage> {
        return Consumer { programStageModel: ProgramStage ->
            programStageFromEvent = programStageModel
            if (programStageModel.displayGenerateEventBox()!! || programStageModel.allowGenerateNextVisit()!!) {
                dialog = CustomDialog(
                        requireContext(),
                        getString(R.string.dialog_generate_new_event),
                        getString(R.string.message_generate_new_event),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_GENERATE_EVENT,
                        object : DialogClickListener {
                            override fun onPositive() {
                                createEvent(EventCreationType.SCHEDULE, if (programStageFromEvent!!.standardInterval() != null) programStageFromEvent!!.standardInterval() else 0)
                            }

                            override fun onNegative() {
                                if (java.lang.Boolean.TRUE == programStageFromEvent!!.remindCompleted()) presenter.areEventsCompleted()
                            }
                        },
                )
                dialog!!.show()
            } else if (java.lang.Boolean.TRUE == programStageModel.remindCompleted()) showDialogCloseProgram()
        }
    }

    private fun showDialogCloseProgram() {
        dialog = CustomDialog(
                requireContext(),
                getString(R.string.event_completed),
                getString(R.string.complete_enrollment_message),
                getString(R.string.button_ok),
                getString(R.string.cancel),
                RC_EVENTS_COMPLETED,
                object : DialogClickListener {
                    override fun onPositive() {
                        presenter.completeEnrollment()
                    }

                    override fun onNegative() {
                        // Nothing to show when negative
                    }
                },
        )
        dialog!!.show()
    }

    override fun areEventsCompleted(): Consumer<Single<Boolean>> {
        return Consumer { eventsCompleted: Single<Boolean> ->
            if (java.lang.Boolean.TRUE == eventsCompleted.blockingGet()) {
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
                                // Nothing to show on negative
                            }
                        },
                )
                dialog!!.show()
            }
        }
    }

    override fun enrollmentCompleted(): Consumer<EnrollmentStatus> {
        return Consumer { enrollmentStatus: EnrollmentStatus ->
            if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
                // TODO: Add implementation for when completed
            }
        }
    }

    private fun createEvent(eventCreationType: EventCreationType, scheduleIntervalDays: Int?) {
        if (isAdded) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name, CREATE_EVENT_TEI)
            val bundle = Bundle()
            bundle.putString(Constants.PROGRAM_UID, dashboardModel!!.currentEnrollment.program())
            bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, dashboardModel!!.tei.uid())
            if (presenter.enrollmentOrgUnitInCaptureScope(dashboardModel!!.currentOrgUnit.uid())) {
                bundle.putString(Constants.ORG_UNIT, dashboardModel!!.currentOrgUnit.uid())
            }
            bundle.putString(Constants.ENROLLMENT_UID, dashboardModel!!.currentEnrollment.uid())
            bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
            bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays!!)
            val intent = Intent(getContext(), ProgramStageSelectionActivity::class.java)
            intent.putExtras(bundle)
            onActivityResultLauncher.launch(intent)
        }
    }

    override fun showCatComboDialog(eventUid: String?, eventDate: Date?, categoryComboUid: String?) {
        val categoryDialog = CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                categoryComboUid!!,
                true,
                eventDate,
        ) { selectedCatOptComboUid: String? ->
            presenter.changeCatOption(eventUid, selectedCatOptComboUid)
        }
        categoryDialog.isCancelable = false
        categoryDialog.show(childFragmentManager, TAG)
    }

    override fun switchFollowUp(followUp: Boolean) {
        this.followUp.set(followUp)
    }

    override fun displayGenerateEvent(eventUid: String) {
        presenter.displayGenerateEvent(eventUid)
    }

    override fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String) {
        requireActivity().startActivity(intent(activity, teiUid, programUid, enrollmentUid))
        requireActivity().finish()
    }

    override fun openEventInitial(intent: Intent) {
        onActivityResultLauncher.launch(intent)
    }

    override fun openEventCapture(intent: Intent) {
        onActivityResultLauncher.launch(intent)
    }

    override fun showTeiImage(filePath: String, defaultIcon: String) {
        Glide.with(this)
                .load(File(filePath))
                .error(
                        getIconResource(requireContext(), defaultIcon, R.drawable.photo_temp_gray, ColorUtils()),
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
    }

    override fun goToEventInitial(eventCreationType: EventCreationType, programStage: ProgramStage) {
        val intent = Intent(activity, EventInitialActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.PROGRAM_UID, programUid)
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid)
        if (presenter.enrollmentOrgUnitInCaptureScope("V5XvX1wr1kF")) {
            bundle.putString(Constants.ORG_UNIT, "V5XvX1wr1kF")
        }
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
        bundle.putBoolean(Constants.EVENT_REPEATABLE, programStage.repeatable()!!)
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, programStage.periodType())
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid())
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, (if (programStage.standardInterval() != null) programStage.standardInterval() else 0)!!)
        intent.putExtras(bundle)
        onActivityResultLauncher.launch(intent)
    }

    override fun showPeriodRequest(periodRequest: PeriodRequest) {
        if (periodRequest == PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(
                    activity,
            ) { datePeriod: List<DatePeriod?>? -> FilterManager.getInstance().addPeriod(datePeriod) }
        } else {
            DateUtils.getInstance().showPeriodDialog(
                    activity,
                    { datePeriod: List<DatePeriod?>? -> FilterManager.getInstance().addPeriod(datePeriod) },
                    true,
            )
        }
    }

    override fun openOrgUnitTreeSelector(programUid: String) {
        val ouTreeFragment = newInstance(true, FilterManager.getInstance().orgUnitUidsFilters)
        ouTreeFragment.show(childFragmentManager, "OUTreeFragment")
    }

    fun showSyncDialog(uid: String?) {
        val syncDialog = SyncStatusDialog.Builder()
                .setConflictType(ConflictType.TEI)
                .setUid(uid!!)
                .onDismissListener { hasChanged: Boolean -> if (hasChanged) FilterManager.getInstance().publishData() }
                .build()
        syncDialog.show(childFragmentManager, uid)
    }

    override fun setRiskColor(risk: String?) {
        // No risk color to set here
    }

    override fun setProgramAttributes(programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute?>?) {
        this.programTrackedEntityAttributes = programTrackedEntityAttributes!!.stream()
                .filter { attr: ProgramTrackedEntityAttribute? -> attr!!.displayInList()!! }
                .collect(Collectors.toList())
        this.programTrackedEntityAttributes?.let { Collections.sort(it, CustomComparator()) }
    }

    override fun setAttributeValues(attributeValues: List<TrackedEntityAttributeValue?>?) {
        // No attribute values to set here
    }

    override fun seeDetails(intent: Intent, options: ActivityOptionsCompat) {
        // No details to show
    }

    override fun openEventDetails(intent: Intent, options: ActivityOptionsCompat) {
        // No event details to open
    }

    override fun showSyncDialog(eventUid: String, enrollmentUid: String) {
        // No sync dialog to show
    }

    override fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>) {
        eventCatComboOptionSelector?.setEventsWithoutCatComboOption(data)
        eventCatComboOptionSelector?.requestCatComboOption(presenter::changeCatOption)
    }

    override fun showProgramRuleErrorMessage(message: String) {
        // No program rule error to show
    }

    override fun showCatOptComboDialog(catComboUid: String) {
        // No catOptionCombo to show
    }

    override fun setFilters(filterItems: List<FilterItem>) {
        // No filters to set
    }

    override fun setTrackedEntityInstance(trackedEntityInstance: TrackedEntityInstance?, organisationUnit: OrganisationUnit?, trackedEntityAttributeValues: List<TrackedEntityAttributeValue?>?) {
        if (isLandscape()) {
            binding!!.cardFrontLand.orgUnit.text = organisationUnit!!.name()
            internalAttributeValues = trackedEntityAttributeValues
        }
        binding!!.trackEntity = trackedEntityInstance
        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }
        teiModel!!.tei = trackedEntityInstance
        teiModel!!.enrolledOrgUnit = organisationUnit!!.displayName()
    }

    companion object {
        private const val RC_GENERATE_EVENT = 1501
        private const val RC_EVENTS_COMPLETED = 1601
        private const val REFERAL_ID = 3
        private const val ADD_NEW_ID = 2
        private const val SCHEDULE_ID = 1
        private const val PREF_COMPLETED_EVENT = "COMPLETED_EVENT"
        private var teiModel: SearchTeiModel? = null

        @JvmStatic
        fun newInstance(programUid: String?, teiUid: String?, enrollmentUid: String?, stageUid: String?, attributeNames: Set<String>?, eventUid: String?): EventTeiDetailsFragment {
            val fragment = EventTeiDetailsFragment()
            val args = Bundle()
            args.putString("PROGRAM_UID", programUid)
            args.putString("TEI_UID", teiUid)
            args.putString("ENROLLMENT_UID", enrollmentUid)
            args.putString("EVENT_UID", eventUid)
            args.putString(Constants.PROGRAM_STAGE_UID, stageUid)
            val x = attributeNames?.let { ArrayList(it) }
            args.putStringArrayList("ATTRIBUTE_NAMES", x)
            fragment.arguments = args
            return fragment
        }
    }
}
