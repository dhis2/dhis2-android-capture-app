package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
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
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.Constants
import org.dhis2.commons.animations.collapse
import org.dhis2.commons.animations.expand
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ObjectStyleUtils.getIconResource
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext.EnrollmentEvent
import org.dhis2.databinding.FragmentTeiDataBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.CategoryDialogInteractions
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventAdapter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventCatComboOptionSelector
import org.dhis2.usescases.teiDashboard.ui.setButtonContent
import org.dhis2.usescases.teiDashboard.ui.setFollowupButtonContent
import org.dhis2.usescases.teiDashboard.ui.setLockButtonContent
import org.dhis2.utils.CustomComparator
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.CREATE_EVENT_TEI
import org.dhis2.utils.analytics.TYPE_EVENT_TEI
import org.dhis2.utils.category.CategoryDialog
import org.dhis2.utils.category.CategoryDialog.Companion.TAG
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
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
import kotlin.reflect.KParameter

class TEIDataFragment : FragmentGlobalAbstract(), TEIDataContracts.View {

    lateinit var binding: FragmentTeiDataBinding
    @Inject
    lateinit var presenter: TEIDataPresenter

    @Inject
    lateinit var filterManager: FilterManager

    @Inject
    lateinit var filtersAdapter: FiltersAdapter

    @Inject
    lateinit var colorUtils: ColorUtils

    private var eventAdapter: EventAdapter? = null
    private var dialog: CustomDialog? = null
    private var programStageFromEvent: ProgramStage? = null
    private val followUp = ObservableBoolean(false)
    private var eventCatComboOptionSelector: EventCatComboOptionSelector? = null
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private var dashboardModel: DashboardProgramModel? = null
    private var teiModel: SearchTeiModel? = null
    var programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute>? = null
    private var internalAttributeValues: List<TrackedEntityAttributeValue>? = null
    private val dashboardActivity: TeiDashboardMobileActivity by lazy { context as TeiDashboardMobileActivity }

    private val detailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        dashboardActivity.getPresenter().init()
    }

    private val eventCreationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val eventCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val eventDetailsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val eventInitialLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        with(requireArguments()) {
            val programUid = getString("PROGRAM_UID")
            val teiUid = getString("TEI_UID")
                ?: throw NullPointerException("A TEI uid is required to launch fragment")
            val enrollmentUid = getString("ENROLLMENT_UID") ?: ""
            app().dashboardComponent()?.plus(
                TEIDataModule(
                    this@TEIDataFragment,
                    programUid,
                    teiUid,
                    enrollmentUid,
                ),
            )?.inject(this@TEIDataFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false)
        binding.presenter = presenter
        dashboardActivity.observeGrouping()?.observe(
            viewLifecycleOwner,
        ) { group: Boolean? ->
            showLoadingProgress(true)
            binding.isGrouping = group
            presenter.onGroupingChanged(group!!)
        }
        dashboardActivity.observeFilters()?.observe(viewLifecycleOwner) { showFilters: Boolean -> showHideFilters(showFilters) }
        dashboardActivity.updatedEnrollment()?.observe(viewLifecycleOwner) { enrollmentUid: String? -> updateEnrollment(enrollmentUid!!) }

        try {
            binding.filterLayout.adapter = filtersAdapter
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (isLandscape()) {
            binding.cardFrontLand!!.entityAttribute1.gravity = Gravity.END
            binding.cardFrontLand!!.entityAttribute2.gravity = Gravity.END
            binding.cardFrontLand!!.entityAttribute3.gravity = Gravity.END
            binding.cardFrontLand!!.entityAttribute4.gravity = Gravity.END
            binding.cardFrontLand!!.attributeListOpened = false
            binding.cardFrontLand!!.showAttributesButton.setOnClickListener {
                val imageView = requireActivity().findViewById<ImageView>(R.id.showAttributesButton)
                val layoutParams = imageView.layoutParams as MarginLayoutParams
                if (binding.cardFrontLand!!.attributeListOpened == true) {
                    binding.cardFrontLand!!.showAttributesButton.setImageResource(R.drawable.ic_arrow_up)
                    binding.cardFrontLand!!.attributeListOpened = false
                    layoutParams.bottomMargin = 0
                    binding.cardFrontLand!!.showAttributesButton.layoutParams = layoutParams
                } else {
                    binding.cardFrontLand!!.showAttributesButton.setImageResource(R.drawable.ic_arrow_down)
                    binding.cardFrontLand!!.attributeListOpened = true
                    binding.cardFrontLand!!.entityAttribute1.gravity = Gravity.END
                    binding.cardFrontLand!!.entityAttribute2.gravity = Gravity.END
                    binding.cardFrontLand!!.entityAttribute3.gravity = Gravity.END
                    binding.cardFrontLand!!.entityAttribute4.gravity = Gravity.END
                    layoutParams.bottomMargin = 90
                    binding.cardFrontLand!!.showAttributesButton.layoutParams = layoutParams
                }
            }
        }

        return binding.root
    }

    private fun updateEnrollment(enrollmentUid: String) {
        presenter.getEnrollment(enrollmentUid)
    }

    private fun updateFabItems() {
        val dialItems = presenter.newEventOptionsByTimeline()
        binding.dialFabLayout.addDialItems(dialItems) { clickedId: Int? ->
            when (clickedId) {
                REFERAL_ID -> createEvent(EventCreationType.REFERAL, 0)
                ADD_NEW_ID -> createEvent(EventCreationType.ADDNEW, 0)
                SCHEDULE_ID -> createEvent(EventCreationType.SCHEDULE, 0)
                else -> {}
            }
        }
    }

    override fun setEnrollment(enrollment: Enrollment) {
        binding.enrollment = enrollment
        dashboardViewModel.updateDashboard(dashboardModel)
        eventAdapter?.setEnrollment(enrollment)

        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }
        teiModel!!.setCurrentEnrollment(enrollment)
    }

    private fun getAttributeValue(attributeUid: String): TrackedEntityAttributeValue? {
        val filteredValue = internalAttributeValues!!.stream().filter { value: TrackedEntityAttributeValue ->
            println(value.trackedEntityAttribute().toString() + "===================" + attributeUid)
            value.trackedEntityAttribute() == attributeUid
        }.collect(Collectors.toList())
        return if (filteredValue.size > 0) filteredValue[0] else null
    }

    private fun setAttributesAndValues(programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute>?) {
        val linkedHashMapOfAttrValues = LinkedHashMap<String, TrackedEntityAttributeValue?>()
        var teiAttributesLoopCounter = 0
        while (teiAttributesLoopCounter < programTrackedEntityAttributes!!.size) {
            val value = getAttributeValue(programTrackedEntityAttributes[teiAttributesLoopCounter].trackedEntityAttribute()!!.uid())
            linkedHashMapOfAttrValues[programTrackedEntityAttributes[teiAttributesLoopCounter].displayShortName()!!.replace("Mother program ", "").replace("Newborn program ", "")] = value
            teiAttributesLoopCounter++
        }
        teiModel!!.attributeValues = linkedHashMapOfAttrValues
        if (isLandscape()) {
            binding.cardFrontLand!!.attributeNames = teiModel!!.attributeValues.keys
            binding.cardFrontLand!!.attribute = teiModel!!.attributeValues.values.stream().collect(Collectors.toList())
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        with(dashboardViewModel) {
            dashboardModel().observe(this@TEIDataFragment, ::setData)
            eventUid().observe(this@TEIDataFragment, ::displayGenerateEvent)
        }
    }

    override fun onPause() {
        presenter.setOpeningFilterToNone()
        presenter.onDettach()
        super.onPause()
    }

    override fun setEnrollmentData(program: Program?, enrollment: Enrollment?) {
        binding.program = program
        binding.enrollment = enrollment

        enrollment?.let {
            eventAdapter?.setEnrollment(enrollment)
            followUp.set(
                when (enrollment.followUp()) {
                    true -> true
                    else -> false
                },
            )

            binding.followup = followUp
        }
    }

    override fun setTrackedEntityInstance(trackedEntityInstance: TrackedEntityInstance?, organisationUnit: OrganisationUnit?, trackedEntityAttributeValues: List<TrackedEntityAttributeValue?>?) {
        binding.trackEntity = trackedEntityInstance

        if (isPortrait()) {
            if (organisationUnit != null) {
                binding.cardFront!!.orgUnit.text = organisationUnit.displayName()
            }
        }

        if (isLandscape()) {
            if (organisationUnit != null) {
                binding.cardFrontLand!!.orgUnit = organisationUnit.name()
            }
            this.internalAttributeValues = trackedEntityAttributeValues as List<TrackedEntityAttributeValue>?

            if (this.programTrackedEntityAttributes != null) {
                setAttributesAndValues(this.programTrackedEntityAttributes)
            }
        }

        if (teiModel == null) {
            teiModel = SearchTeiModel()
        }

        teiModel!!.tei = trackedEntityInstance
        if (organisationUnit != null) {
            teiModel!!.enrolledOrgUnit = organisationUnit.displayName()
        }
    }

    override fun setAttributeValues(attributeValues: List<TrackedEntityAttributeValue?>?) {
    }

    fun setData(dashboardModel: DashboardProgramModel) {
        this.dashboardModel = dashboardModel
        if (dashboardModel.currentEnrollment != null) {
            binding.dialFabLayout.setFabVisible(true)
            presenter.setDashboardProgram(dashboardModel)
            eventCatComboOptionSelector =
                EventCatComboOptionSelector(
                    dashboardModel.currentProgram.categoryComboUid(),
                    childFragmentManager,
                    object : CategoryDialogInteractions {},
                )
            binding.dashboardModel = dashboardModel
            updateFabItems()
        } else {
            binding.dialFabLayout.setFabVisible(false)
            binding.teiRecycler.adapter = DashboardProgramAdapter(presenter, dashboardModel)
            binding.teiRecycler.addItemDecoration(
                DividerItemDecoration(
                    abstracContext,
                    DividerItemDecoration.VERTICAL,
                ),
            )
            binding.dashboardModel = dashboardModel
            showLoadingProgress(false)
        }

        if (isPortrait()) {
            binding.cardFront!!.detailsButton.setButtonContent(dashboardActivity.presenter.teType) {
                presenter.seeDetails(binding.cardFront!!.cardData, dashboardModel)
            }
        } else {
            binding.cardFrontLand!!.detailsButton.setButtonContent(dashboardActivity.presenter.teType) { Unit }

            binding.cardFrontLand!!.followupButton.setFollowupButtonContent(dashboardActivity.presenter.teType, followUp.get()) {
                presenter.onFollowUp(dashboardModel)
                presenter.init()
            }

            binding.cardFrontLand!!.lockButton.setLockButtonContent(dashboardActivity.presenter.teType) {
                showEnrollmentStatusOptions()
            }
        }

        binding.executePendingBindings()
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

    private fun showEnrollmentStatusOptions() {
        val menu: Int = if (teiModel!!.selectedEnrollment?.status() == EnrollmentStatus.ACTIVE) {
            R.menu.tei_detail_options_active
        } else if (teiModel!!.selectedEnrollment?.status() == EnrollmentStatus.COMPLETED) {
            R.menu.tei_detail_options_completed
        } else {
            R.menu.tei_detail_options_cancelled
        }
        AppMenuHelper.Builder()
            .anchor(binding.teiData)
            .menu(requireActivity(), menu)
            .onMenuInflated { KParameter.Kind.INSTANCE }
            .onMenuItemClicked { itemId: Int? ->
                when (itemId) {
                    R.id.complete -> dashboardActivity.getPresenter()?.updateEnrollmentStatus(dashboardActivity.enrollmentUid, EnrollmentStatus.COMPLETED)
                    R.id.deactivate -> dashboardActivity.getPresenter()?.updateEnrollmentStatus(dashboardActivity.enrollmentUid, EnrollmentStatus.CANCELLED)
                    R.id.reOpen -> dashboardActivity.getPresenter()?.updateEnrollmentStatus(dashboardActivity.enrollmentUid, EnrollmentStatus.ACTIVE)
                }
                true
            }
            .build().show()
    }

    override fun setFilters(filterItems: List<FilterItem>) {
        filtersAdapter.submitList(filterItems)
    }

    override fun setRiskColor(risk: String?) {
        if (risk === "High Risk") {
            binding.highRisk = true
            binding.lowRisk = false
        }
        if (risk === "Low Risk") {
            binding.lowRisk = true
            binding.highRisk = false
        }
    }

    override fun setProgramAttributes(programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute?>?) {
        if (programTrackedEntityAttributes != null) {
            this.programTrackedEntityAttributes = programTrackedEntityAttributes.stream()
                .filter { attr: ProgramTrackedEntityAttribute? -> attr?.displayInList()!! }
                .collect(Collectors.toList()) as List<ProgramTrackedEntityAttribute>?
        }
        Collections.sort(this.programTrackedEntityAttributes, CustomComparator())
        if (isLandscape()) {
            if (internalAttributeValues != null) {
                setAttributesAndValues(this.programTrackedEntityAttributes)
            }
        }
    }

    override fun hideFilters() {
        dashboardActivity.hideFilter()
    }

    override fun observeStageSelection(
        currentProgram: Program,
        currentEnrollment: Enrollment,
    ): Flowable<StageSection> {
        if (eventAdapter == null) {
            eventAdapter = EventAdapter(presenter, currentProgram, colorUtils).also {
                it.setEnrollment(currentEnrollment)
            }
            binding.teiRecycler.adapter = eventAdapter
        }
        return eventAdapter?.stageSelector() ?: Flowable.empty()
    }

    override fun setEvents(events: List<EventViewModel>, canAddEvents: Boolean) {
        binding.canAddEvents = canAddEvents
        if (events.isEmpty()) {
            binding.emptyTeis.visibility = View.VISIBLE
            if (binding.dialFabLayout.isFabVisible()) {
                binding.emptyTeis.setText(R.string.empty_tei_add)
            } else {
                binding.emptyTeis.setText(R.string.empty_tei_no_add)
            }
        } else {
            binding.emptyTeis.visibility = View.GONE
            eventAdapter?.submitList(events)
            for (eventViewModel in events) {
                if (eventViewModel.isAfterToday(DateUtils.getInstance().today)) {
                    binding.teiRecycler.scrollToPosition(events.indexOf(eventViewModel))
                }
            }
        }
        showLoadingProgress(false)
    }

    override fun showCatComboDialog(eventUid: String?, eventDate: Date?, categoryComboUid: String?) {
        TODO("Not yet implemented")
    }

    private fun showLoadingProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.loadingProgress.root.visibility = View.VISIBLE
        } else {
            binding.loadingProgress.root.visibility = View.GONE
        }
    }

    override fun displayGenerateEvent(): Consumer<ProgramStage> {
        return Consumer { programStageModel: ProgramStage ->
            programStageFromEvent = programStageModel
            if (programStageModel.displayGenerateEventBox() == true || programStageModel.allowGenerateNextVisit() == true) {
                dialog = CustomDialog(
                    requireContext(),
                    getString(R.string.dialog_generate_new_event),
                    getString(R.string.message_generate_new_event),
                    getString(R.string.button_ok),
                    getString(R.string.cancel),
                    RC_GENERATE_EVENT,
                    object : DialogClickListener {
                        override fun onPositive() {
                            createEvent(
                                EventCreationType.SCHEDULE,
                                if (programStageFromEvent?.standardInterval() != null) programStageFromEvent?.standardInterval() else 0,
                            )
                        }

                        override fun onNegative() {
                            if (programStageFromEvent?.remindCompleted() == true) presenter.areEventsCompleted()
                        }
                    },
                )
                dialog?.show()
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

                override fun onNegative() {}
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

                        override fun onNegative() {}
                    },
                )
                dialog?.show()
            }
        }
    }

    override fun enrollmentCompleted(): Consumer<EnrollmentStatus> {
        return Consumer { enrollmentStatus ->
            if (enrollmentStatus == EnrollmentStatus.COMPLETED) dashboardActivity.updateStatus()
        }
    }

    private fun createEvent(eventCreationType: EventCreationType, scheduleIntervalDays: Int?) {
        if (isAdded) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name, CREATE_EVENT_TEI)
            val bundle = Bundle()
            bundle.putString(
                Constants.PROGRAM_UID,
                dashboardModel?.currentEnrollment?.program(),
            )
            bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, dashboardModel?.tei?.uid())
            dashboardModel?.currentOrgUnit?.uid()
                ?.takeIf { presenter.enrollmentOrgUnitInCaptureScope(it) }
                ?.let {
                    bundle.putString(Constants.ORG_UNIT, it)
                }

            bundle.putString(Constants.ENROLLMENT_UID, dashboardModel?.currentEnrollment?.uid())
            bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
            bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays ?: 0)
            val intent = Intent(context, ProgramStageSelectionActivity::class.java)
            intent.putExtras(bundle)
            eventCreationLauncher.launch(intent)
        }
    }

    override fun switchFollowUp(followUp: Boolean) {
        this.followUp.set(followUp)
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

    override fun seeDetails(intent: Intent, options: ActivityOptionsCompat) =
        detailsLauncher.launch(intent, options)

    override fun openEventDetails(intent: Intent, options: ActivityOptionsCompat) =
        eventDetailsLauncher.launch(intent, options)

    override fun openEventInitial(intent: Intent) = eventInitialLauncher.launch(intent)

    override fun openEventCapture(intent: Intent) = eventCaptureLauncher.launch(intent)

    override fun showTeiImage(filePath: String, defaultIcon: String) {
        if (filePath.isEmpty() && defaultIcon.isEmpty()) {
            if (isPortrait()) {
                if (filePath.isEmpty() && defaultIcon.isEmpty()) {
                    binding.cardFront!!.teiImage.visibility = View.GONE
                } else {
                    binding.cardFront!!.teiImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(File(filePath))
                        .error(
                            getIconResource(
                                requireContext(),
                                defaultIcon,
                                R.drawable.photo_temp_gray,
                                colorUtils,
                            ),
                        )
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(CircleCrop())
                        .into(binding.cardFront!!.teiImage)
                    binding.cardFront!!.teiImage.setOnClickListener {
                        val fileToShow = File(filePath)
                        if (fileToShow.exists()) {
                            ImageDetailBottomDialog(
                                null,
                                fileToShow,
                            ).show(childFragmentManager, ImageDetailBottomDialog.TAG)
                        }
                    }
                }
            }
        } else {
            if (isLandscape()) {
                binding.cardFrontLand!!.trackedEntityImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(File(filePath))
                    .error(
                        getIconResource(requireContext(), defaultIcon, R.drawable.photo_temp_gray, colorUtils),
                    )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CircleCrop())
                    .into(binding.cardFrontLand!!.trackedEntityImage)
                binding.cardFrontLand!!.trackedEntityImage.setOnClickListener {
                    val fileToShow = File(filePath)
                    if (fileToShow.exists()) {
                        ImageDetailBottomDialog(
                            null,
                            fileToShow,
                        ).show(childFragmentManager, ImageDetailBottomDialog.TAG)
                    }
                }
            }
        }
    }

    override fun goToEventInitial(
        eventCreationType: EventCreationType,
        programStage: ProgramStage,
    ) {
        val intent = Intent(activity, EventInitialActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.PROGRAM_UID, dashboardModel?.currentProgram?.uid())
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, dashboardModel?.tei?.uid())
        dashboardModel?.currentOrgUnit?.uid()
            ?.takeIf(presenter::enrollmentOrgUnitInCaptureScope)
            ?.let {
                bundle.putString(Constants.ORG_UNIT, it)
            }
        bundle.putString(Constants.ENROLLMENT_UID, dashboardModel?.currentEnrollment?.uid())
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
        bundle.putBoolean(Constants.EVENT_REPEATABLE, programStage.repeatable() ?: false)
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, programStage.periodType())
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid())
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, programStage.standardInterval() ?: 0)
        intent.putExtras(bundle)
        eventInitialLauncher.launch(intent)
    }

    private fun showHideFilters(showFilters: Boolean) {
        if (showFilters) {
            binding.filterLayout.expand(false) {
                binding.teiData.visibility = View.GONE
                binding.filterLayout.visibility = View.VISIBLE
            }
        } else {
            binding.filterLayout.collapse {
                binding.teiData.visibility = View.VISIBLE
                binding.filterLayout.visibility = View.GONE
            }
        }
    }

    override fun showPeriodRequest(periodRequest: PeriodRequest) {
        if (periodRequest == PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(
                dashboardActivity,
                FilterManager.getInstance()::addPeriod,
            )
        } else {
            DateUtils.getInstance().showPeriodDialog(
                dashboardActivity,
                FilterManager.getInstance()::addPeriod,
                true,
            )
        }
    }

    override fun openOrgUnitTreeSelector(programUid: String) {
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(
                FilterManager.getInstance().orgUnitUidsFilters,
            )
            .onSelection { selectedOrgUnits: List<OrganisationUnit?>? ->
                presenter.setOrgUnitFilters(
                    selectedOrgUnits,
                )
            }
            .build().show(childFragmentManager, "OUTreeFragment")
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
            }).show(enrollmentUid)
    }

    override fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>) {
        eventCatComboOptionSelector?.setEventsWithoutCatComboOption(data)
        eventCatComboOptionSelector?.requestCatComboOption(presenter::changeCatOption)
    }

    override fun showProgramRuleErrorMessage(message: String) {
        dashboardActivity.runOnUiThread { showDescription(message) }
    }

    override fun showCatOptComboDialog(catComboUid: String) {
        CategoryDialog(
            CategoryDialog.Type.CATEGORY_OPTION_COMBO,
            catComboUid,
            false,
            null,
        ) { selectedCatOptionCombo ->
            presenter.filterCatOptCombo(selectedCatOptionCombo)
        }.show(
            childFragmentManager,
            TAG,
        )
    }
    companion object {
        const val RC_GENERATE_EVENT = 1501
        const val RC_EVENTS_COMPLETED = 1601
        const val REFERAL_ID = 3
        const val ADD_NEW_ID = 2
        const val SCHEDULE_ID = 1
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
