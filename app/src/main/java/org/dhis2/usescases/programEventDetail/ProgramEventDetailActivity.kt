package org.dhis2.usescases.programEventDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.clipWithRoundedCorners
import org.dhis2.bindings.dp
import org.dhis2.commons.Constants
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.matomo.Actions.Companion.CREATE_EVENT
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityProgramEventDetailBinding
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel.EventProgramScreen
import org.dhis2.usescases.programEventDetail.eventList.EventListFragment
import org.dhis2.usescases.programEventDetail.eventMap.EventMapFragment
import org.dhis2.utils.DateUtils
import org.dhis2.utils.EventMode
import org.dhis2.utils.analytics.DATA_CREATION
import org.dhis2.utils.category.CategoryDialog
import org.dhis2.utils.category.CategoryDialog.Companion.TAG
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import javax.inject.Inject

class ProgramEventDetailActivity :
    ActivityGlobalAbstract(),
    ProgramEventDetailView {

    private lateinit var binding: ActivityProgramEventDetailBinding

    @Inject
    lateinit var presenter: ProgramEventDetailPresenter

    @Inject
    lateinit var filtersAdapter: FiltersAdapter

    @Inject
    lateinit var pageConfigurator: NavigationPageConfigurator

    @Inject
    lateinit var networkUtils: NetworkUtils

    @JvmField
    @Inject
    var themeManager: ThemeManager? = null

    @Inject
    lateinit var viewModelFactory: ProgramEventDetailViewModelFactory

    private var backDropActive = false
    private var programUid: String = ""

    private val programEventsViewModel: ProgramEventDetailViewModel by viewModels {
        viewModelFactory
    }

    var component: ProgramEventDetailComponent? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        initExtras()
        initInjection()
        themeManager?.setProgramTheme(programUid)
        super.onCreate(savedInstanceState)
        initEventFilters()
        initViewModel()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_event_detail)
        binding.presenter = presenter
        binding.totalFilters = FilterManager.getInstance().totalFilters
        binding.navigationBar.pageConfiguration(pageConfigurator)
        binding.navigationBar.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_list_view -> {
                    programEventsViewModel.showList()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_map_view -> {
                    networkUtils.performIfOnline(
                        this,
                        {
                            presenter.trackEventProgramMap()
                            programEventsViewModel.showMap()
                        },
                        {
                            binding.navigationBar.selectItemAt(0)
                        },
                        getString(R.string.msg_network_connection_maps),
                    )
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_analytics -> {
                    presenter.trackEventProgramAnalytics()
                    programEventsViewModel.showAnalytics()
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
        binding.fragmentContainer.clipWithRoundedCorners(16.dp)
        binding.filterLayout.adapter = filtersAdapter
        presenter.init()
        binding.syncButton.setOnClickListener { showSyncDialogProgram() }

        if (intent.shouldLaunchSyncDialog()) {
            showSyncDialogProgram()
        }
    }

    private fun initExtras() {
        programUid = intent.getStringExtra(EXTRA_PROGRAM_UID) ?: ""
    }

    private fun initInjection() {
        component = app().userComponent()
            ?.plus(ProgramEventDetailModule(this, programUid))
        component?.inject(this)
    }

    private fun initEventFilters() {
        FilterManager.getInstance().clearCatOptCombo()
        FilterManager.getInstance().clearEventStatus()
    }

    private fun initViewModel() {
        programEventsViewModel.progress().observe(this) { showProgress: Boolean ->
            if (showProgress) {
                binding.toolbarProgress.show()
            } else {
                binding.toolbarProgress.hide()
            }
        }
        programEventsViewModel.eventSyncClicked.observe(this) { eventUid: String? ->
            if (eventUid != null) {
                presenter.onSyncIconClick(eventUid)
            }
        }
        programEventsViewModel.eventClicked.observe(this) { eventData: Pair<String, String>? ->
            if (eventData != null && !programEventsViewModel.recreationActivity) {
                programEventsViewModel.onRecreationActivity(false)
                navigateToEvent(eventData.component1(), eventData.component2())
            } else if (programEventsViewModel.recreationActivity) {
                programEventsViewModel.onRecreationActivity(false)
            }
        }
        programEventsViewModel.writePermission.observe(this) { canWrite: Boolean ->
            binding.addEventButton.visibility = if (canWrite) View.VISIBLE else View.GONE
        }
        programEventsViewModel.currentScreen.observe(this) { currentScreen: EventProgramScreen? ->
            currentScreen?.let {
                when (it) {
                    EventProgramScreen.LIST -> showList()
                    EventProgramScreen.MAP -> showMap()
                    EventProgramScreen.ANALYTICS -> showAnalytics()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.addEventButton.isEnabled = true
        binding.totalFilters = FilterManager.getInstance().totalFilters
    }

    private fun showSyncDialogProgram() {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(SyncContext.EventProgram(programUid))
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) FilterManager.getInstance().publishData()
                }
            }).show("EVENT_SYNC")
    }

    public override fun onPause() {
        super.onPause()
        if (isChangingConfigurations) {
            programEventsViewModel.onRecreationActivity(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.setOpeningFilterToNone()
        presenter.onDettach()
        FilterManager.getInstance().clearEventStatus()
        FilterManager.getInstance().clearCatOptCombo()
        FilterManager.getInstance().clearWorkingList(true)
        FilterManager.getInstance().clearAssignToMe()
        presenter.clearOtherFiltersIfWebAppIsConfig()
    }

    override fun setProgram(programModel: Program) {
        binding.name = programModel.displayName()
    }

    override fun showFilterProgress() {
        programEventsViewModel.setProgress(true)
    }

    override fun renderError(message: String) {
        if (activity != null) {
            MaterialAlertDialogBuilder(activity, R.style.MaterialDialog)
                .setPositiveButton(getString(R.string.button_ok), null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show()
        }
    }

    override fun showHideFilter() {
        val transition: Transition = ChangeBounds()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                if (!backDropActive) {
                    binding.clearFilters.hide()
                }
            }

            override fun onTransitionEnd(transition: Transition) {
                programEventsViewModel.updateBackdrop(backDropActive)
                if (backDropActive) {
                    binding.clearFilters.show()
                }
            }

            override fun onTransitionCancel(transition: Transition) {
                /*No action needed*/
            }

            override fun onTransitionPause(transition: Transition) {
                /*No action needed*/
            }

            override fun onTransitionResume(transition: Transition) {
                /*No action needed*/
            }
        })
        transition.duration = 200
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition)
        backDropActive = !backDropActive
        val initSet = ConstraintSet()
        initSet.clone(binding.backdropLayout)
        if (backDropActive) {
            initSet.connect(
                R.id.fragmentContainer,
                ConstraintSet.TOP,
                R.id.filterLayout,
                ConstraintSet.BOTTOM,
                16.dp,
            )
            binding.navigationBar.hide()
        } else {
            initSet.connect(
                R.id.fragmentContainer,
                ConstraintSet.TOP,
                R.id.backdropGuideTop,
                ConstraintSet.BOTTOM,
                0,
            )
            binding.navigationBar.show()
        }
        initSet.applyTo(binding.backdropLayout)
    }

    override fun startNewEvent() {
        analyticsHelper.setEvent(CREATE_EVENT, DATA_CREATION, CREATE_EVENT)
        binding.addEventButton.isEnabled = false
        val bundle = EventInitialActivity.getBundle(
            programUid,
            null,
            EventCreationType.ADDNEW.name,
            null,
            null,
            null,
            presenter.stageUid,
            null,
            0,
            null,
        )
        startActivity(
            EventInitialActivity::class.java,
            bundle,
            false,
            false,
            null,
        )
    }

    override fun setWritePermission(canWrite: Boolean) {
        programEventsViewModel.writePermission.value = canWrite
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
        binding.executePendingBindings()
    }

    override fun showPeriodRequest(periodRequest: PeriodRequest) {
        if (periodRequest == PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(this) { datePeriod: List<DatePeriod?>? ->
                FilterManager.getInstance().addPeriod(datePeriod)
            }
        } else {
            DateUtils.getInstance().showPeriodDialog(
                this,
                { datePeriods: List<DatePeriod?>? ->
                    FilterManager.getInstance().addPeriod(datePeriods)
                },
                true,
            )
        }
    }

    override fun openOrgUnitTreeSelector() {
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(FilterManager.getInstance().orgUnitUidsFilters)
            .onSelection { selectedOrgUnits ->
                presenter.setOrgUnitFilters(selectedOrgUnits)
            }
            .build()
            .show(supportFragmentManager, "OUTreeFragment")
    }

    override fun showTutorial(shaked: Boolean) {
        setTutorial()
    }

    override fun navigateToEvent(eventId: String, orgUnit: String) {
        programEventsViewModel.updateEvent = eventId
        val bundle = Bundle()
        bundle.putString(Constants.PROGRAM_UID, programUid)
        bundle.putString(Constants.EVENT_UID, eventId)
        bundle.putString(Constants.ORG_UNIT, orgUnit)
        startActivity(
            EventCaptureActivity::class.java,
            EventCaptureActivity.getActivityBundle(eventId, programUid, EventMode.CHECK),
            false,
            false,
            null,
        )
    }

    override fun showSyncDialog(uid: String) {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(SyncContext.Event(uid))
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) FilterManager.getInstance().publishData()
                }
            }).show(FRAGMENT_TAG)
    }

    private fun showList() {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer,
            EventListFragment(),
            "EVENT_LIST",
        ).commitNow()
        binding.addEventButton.visibility =
            if (programEventsViewModel.writePermission.value == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.filter.visibility = View.VISIBLE
    }

    private fun showMap() {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer,
            EventMapFragment(),
            "EVENT_MAP",
        ).commitNow()
        binding.addEventButton.visibility = View.GONE
        binding.filter.visibility = View.VISIBLE
    }

    private fun showAnalytics() {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer,
            GroupAnalyticsFragment.forProgram(programUid),
        ).commitNow()
        binding.addEventButton.visibility = View.GONE
        binding.filter.visibility = View.GONE
    }

    override fun showCatOptComboDialog(catComboUid: String) {
        CategoryDialog(
            CategoryDialog.Type.CATEGORY_OPTION_COMBO,
            catComboUid,
            false,
            null,
        ) { selectedCatOptionCombo ->
            presenter.filterCatOptCombo(selectedCatOptionCombo)
        }.show(supportFragmentManager, TAG)
    }

    override fun setFilterItems(programFilters: List<FilterItem>) {
        filtersAdapter.submitList(programFilters)
    }

    override fun hideFilters() {
        binding.filter.visibility = View.GONE
    }

    companion object {
        private const val FRAGMENT_TAG = "SYNC"
        const val EXTRA_PROGRAM_UID = "PROGRAM_UID"
        fun getBundle(programUid: String?): Bundle {
            val bundle = Bundle()
            bundle.putString(EXTRA_PROGRAM_UID, programUid)
            return bundle
        }

        fun intent(context: Context, programUid: String): Intent {
            return Intent(context, ProgramEventDetailActivity::class.java).apply {
                putExtra(EXTRA_PROGRAM_UID, programUid)
            }
        }
    }
}
