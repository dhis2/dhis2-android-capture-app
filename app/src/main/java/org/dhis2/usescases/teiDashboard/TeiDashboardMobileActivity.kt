package org.dhis2.usescases.teiDashboard

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.TEI_UID
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityDashboardMobileBinding
import org.dhis2.ui.ThemeManager
import org.dhis2.ui.dialogs.bottomsheet.DeleteBottomSheetDialog
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.enrollment.EnrollmentActivity.Companion.getIntent
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.qrCodes.QrActivity
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter.Companion.NO_POSITION
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter.DashboardPageType
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment.Companion.newInstance
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity
import org.dhis2.usescases.teiDashboard.ui.setButtonContent
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SHARE_TEI
import org.dhis2.utils.analytics.SHOW_HELP
import org.dhis2.utils.analytics.TYPE_QR
import org.dhis2.utils.analytics.TYPE_SHARE
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import javax.inject.Inject

class TeiDashboardMobileActivity :
    ActivityGlobalAbstract(),
    TeiDashboardContracts.View,
    MapButtonObservable {
    private var currentOrientation = -1

    @Inject
    lateinit var presenter: TeiDashboardContracts.Presenter

    @Inject
    lateinit var filterManager: FilterManager

    @Inject
    lateinit var pageConfigurator: NavigationPageConfigurator

    @Inject
    lateinit var viewModelFactory: DashboardViewModelFactory

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var resourceManager: ResourceManager

    lateinit var programModel: DashboardProgramModel
    var teiUid: String? = null
    var programUid: String? = null
    var enrollmentUid: String? = null
    lateinit var binding: ActivityDashboardMobileBinding
    var adapter: DashboardPagerAdapter? = null
    private lateinit var dashboardViewModel: DashboardViewModel
    private var fromRelationship = false

    private var relationshipMap: MutableLiveData<Boolean> = MutableLiveData(false)

    private var elevation = 0f
    private var restartingActivity = false

    private val detailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
    }

    private val teiProgramListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            it.data?.let { dataIntent ->
                if (dataIntent.hasExtra(GO_TO_ENROLLMENT)) {
                    val intent = getIntent(
                        this,
                        dataIntent.getStringExtra(GO_TO_ENROLLMENT) ?: "",
                        dataIntent.getStringExtra(GO_TO_ENROLLMENT_PROGRAM) ?: "",
                        EnrollmentActivity.EnrollmentMode.NEW,
                        false,
                    )
                    startActivity(intent)
                    finish()
                }
                if (dataIntent.hasExtra(CHANGE_PROGRAM)) {
                    startActivity(
                        intent(
                            this,
                            teiUid,
                            dataIntent.getStringExtra(CHANGE_PROGRAM),
                            dataIntent.getStringExtra(CHANGE_PROGRAM_ENROLLMENT),
                        ),
                    )
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.TRACKED_ENTITY_INSTANCE)) {
            teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE)
            programUid = savedInstanceState.getString(Constants.PROGRAM_UID)
        } else {
            teiUid = intent.getStringExtra(TEI_UID)
            programUid = intent.getStringExtra(Constants.PROGRAM_UID)
            enrollmentUid = intent.getStringExtra(Constants.ENROLLMENT_UID)
        }
        (applicationContext as App).createDashboardComponent(
            TeiDashboardModule(
                this,
                teiUid ?: "",
                programUid,
                enrollmentUid,
                this.isPortrait(),
            ),
        ).inject(this)
        setTheme(themeManager.getProgramTheme())
        super.onCreate(savedInstanceState)
        dashboardViewModel =
            ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile)
        showLoadingProgress(true)
        binding.presenter = presenter
        filterManager.setUnsupportedFilters(Filters.ENROLLMENT_DATE, Filters.ENROLLMENT_STATUS)
        presenter.prefSaveCurrentProgram(programUid)
        elevation = ViewCompat.getElevation(binding.toolbar)
        binding.relationshipMapIcon.setOnClickListener {
            networkUtils.performIfOnline(
                this,
                {
                    if (java.lang.Boolean.FALSE == relationshipMap.value) {
                        binding.relationshipMapIcon.setImageResource(R.drawable.ic_list)
                    } else {
                        binding.relationshipMapIcon.setImageResource(R.drawable.ic_map)
                    }
                    val showMap = !relationshipMap.value!!
                    if (showMap) {
                        binding.toolbarProgress.visibility = View.VISIBLE
                        binding.toolbarProgress.hide()
                    }
                    relationshipMap.value = showMap
                },
                {},
                getString(R.string.msg_network_connection_maps),
            )
        }
        binding.syncButton.setOnClickListener { openSyncDialog() }
        if (intent.shouldLaunchSyncDialog()) {
            openSyncDialog()
        }
        setNavigationBar()
        setEditButton()
        dashboardViewModel.showStatusErrorMessages.observe(this) {
            displayStatusError(it)
        }
        dashboardViewModel.dashboardModel.observe(this) {
            when (it) {
                is DashboardEnrollmentModel -> setData(it)
                is DashboardTEIModel -> setDataWithOutProgram(it)
            }
        }
    }

    private fun setEditButton() {
        binding.editButton.setButtonContent(presenter.teType) {
            enrollmentUid?.let { enrollmentUid ->
                programUid?.let { programUid ->
                    detailsLauncher.launch(
                        getIntent(
                            context = this,
                            enrollmentUid = enrollmentUid,
                            programUid = programUid,
                            enrollmentMode = EnrollmentActivity.EnrollmentMode.CHECK,
                            forRelationship = false,
                        ),
                    )
                }
            }
        }
    }

    private fun setNavigationBar() {
        if (programUid != null) {
            binding.navigationBar.visibility = View.VISIBLE
            binding.navigationBar.pageConfiguration(pageConfigurator)
            binding.navigationBar.setOnItemSelectedListener { item: MenuItem ->
                adapter?.let { pagerAdapter ->
                    when (item.itemId) {
                        R.id.navigation_analytics -> presenter.trackDashboardAnalytics()
                        R.id.navigation_relationships -> presenter.trackDashboardRelationships()
                        R.id.navigation_notes -> presenter.trackDashboardNotes()
                    }
                    pagerAdapter.getNavigationPagePosition(item.itemId)
                        .takeIf { it != NO_POSITION }
                        ?.let {
                            when {
                                this.isLandscape() -> binding.teiTablePager?.currentItem = it
                                else -> binding.teiPager?.currentItem = it
                            }
                        }
                }
                true
            }
        } else {
            binding.navigationBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentOrientation != -1) {
            val nextOrientation = if (this.isLandscape()) 1 else 0
            if (currentOrientation != nextOrientation && adapter != null) {
                adapter?.notifyDataSetChanged()
            }
        }
        currentOrientation = if (this.isLandscape()) 1 else 0
        if (adapter == null) {
            restoreAdapter(programUid)
        }
        presenter.refreshTabCounters()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onDestroy() {
        (applicationContext as App).releaseDashboardComponent()
        super.onDestroy()
    }

    fun openSyncDialog() {
        enrollmentUid?.let { enrollmentUid ->
            SyncStatusDialog.Builder()
                .withContext(this, null)
                .withSyncContext(
                    SyncContext.Enrollment(enrollmentUid),
                )
                .onDismissListener(object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged && !restartingActivity) {
                            restartingActivity = true
                            val activityOptions = ActivityOptions.makeCustomAnimation(
                                context,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out,
                            )
                            startActivity(
                                intent(context, teiUid, programUid, enrollmentUid),
                                activityOptions.toBundle(),
                            )
                            finish()
                        }
                    }
                })
                .onNoConnectionListener {
                    val contextView = findViewById<View>(R.id.navigationBar)
                    Snackbar.make(
                        contextView,
                        R.string.sync_offline_check_connection,
                        Snackbar.LENGTH_SHORT,
                    ).show()
                }.show(TEI_SYNC)
        }
    }

    private fun setViewpagerAdapter() {
        adapter = teiUid?.let {
            DashboardPagerAdapter(
                this,
                programUid,
                it,
                enrollmentUid,
                pageConfigurator.displayAnalytics(),
                pageConfigurator.displayRelationships(),
            )
        }
        when {
            this.isPortrait() -> setPortraitPager()
            else -> setLandscapePager()
        }
    }

    private fun setPortraitPager() {
        binding.teiPager?.adapter = null
        binding.teiPager?.isUserInputEnabled = false
        binding.teiPager?.adapter = adapter
        binding.teiPager?.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    showLoadingProgress(false)
                    val pageType = adapter?.pageType(position)
                    if (pageType === DashboardPageType.RELATIONSHIPS) {
                        binding.relationshipMapIcon.visibility = View.VISIBLE
                    } else {
                        binding.relationshipMapIcon.visibility = View.GONE
                    }
                    if (pageType == DashboardPageType.TEI_DETAIL && programUid != null) {
                        binding.toolbarTitle.visibility = View.GONE
                        binding.editButton.visibility = View.VISIBLE
                        binding.syncButton.visibility = View.GONE
                    } else {
                        binding.toolbarTitle.visibility = View.VISIBLE
                        binding.editButton.visibility = View.GONE
                        binding.syncButton.visibility = View.VISIBLE
                    }
                    binding.navigationBar.selectItemAt(position)
                }
            },
        )
        if (fromRelationship) {
            binding.teiPager?.setCurrentItem(2, false)
        }
    }

    private fun setLandscapePager() {
        binding.teiTablePager?.adapter = adapter
        binding.teiTablePager?.isUserInputEnabled = false
        binding.teiTablePager?.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    showLoadingProgress(false)
                    binding.relationshipMapIcon.visibility = when (adapter?.pageType(position)) {
                        DashboardPageType.RELATIONSHIPS -> View.VISIBLE
                        else -> View.GONE
                    }
                    binding.navigationBar.selectItemAt(position)
                }
            },
        )
        if (fromRelationship) binding.teiTablePager?.setCurrentItem(1, false)
    }

    private fun showLoadingProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.toolbarProgress.show()
        } else {
            binding.toolbarProgress.hide()
        }
    }

    private fun setData(dashboardModel: DashboardEnrollmentModel) {
        themeManager.setProgramTheme(dashboardModel.currentProgram().uid())
        setProgramColor(dashboardModel.currentProgram().uid())
        val title = String.format(
            "%s %s",
            if (dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) != null) {
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(1)
            } else {
                ""
            },
            if (dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) != null) {
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(2)
            } else {
                ""
            },
        )
        binding.title = title
        binding.executePendingBindings()
        enrollmentUid = dashboardModel.currentEnrollment.uid()
        if (this.isLandscape()) {
            if (binding.teiTablePager?.adapter == null) {
                setViewpagerAdapter()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.tei_main_view, newInstance(programUid, teiUid, enrollmentUid))
                .commitAllowingStateLoss()
        } else {
            if (binding.teiPager?.adapter == null) {
                setViewpagerAdapter()
            }
        }
        val enrollmentStatus =
            dashboardModel.currentEnrollment.status() == EnrollmentStatus.ACTIVE
        if (intent.getStringExtra(Constants.EVENT_UID) != null && enrollmentStatus) {
            dashboardViewModel.updateEventUid(
                intent.getStringExtra(Constants.EVENT_UID),
            )
        }
        presenter.initNoteCounter()
    }

    override fun restoreAdapter(programUid: String?) {
        adapter = null
        this.programUid = programUid
    }

    override fun handleTeiDeletion() {
        finish()
    }

    override fun handleEnrollmentDeletion(hasMoreEnrollments: Boolean) {
        if (hasMoreEnrollments) {
            startActivity(intent(this, teiUid, null, null))
            finish()
        } else {
            finish()
        }
    }

    override fun authorityErrorMessage() {
        displayMessage(getString(R.string.delete_authority_error))
    }

    private fun setDataWithOutProgram(dashboardModel: DashboardTEIModel) {
        themeManager.clearProgramTheme()
        setProgramColor(null)
        val title = String.format(
            "%s %s - %s",
            if (dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) != null) {
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(
                    1,
                )
            } else {
                ""
            },
            if (dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) != null) {
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(
                    2,
                )
            } else {
                ""
            },
            getString(
                R.string.dashboard_overview,
            ),
        )
        binding.title = title
        binding.executePendingBindings()
        setViewpagerAdapter()
        binding.relationshipMapIcon.visibility = View.GONE
        if (this.isLandscape()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tei_main_view, newInstance(programUid, teiUid, enrollmentUid))
                .commitAllowingStateLoss()
        }
        showLoadingProgress(false)
    }

    override fun goToEnrollmentList() {
        val intent = Intent(this, TeiProgramListActivity::class.java)
        intent.putExtra(TEI_UID, teiUid)
        teiProgramListLauncher.launch(intent)
    }

    override fun setTutorial() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (abstractActivity != null) {
                HelpManager.getInstance().show(
                    activity,
                    HelpManager.TutorialName.TEI_DASHBOARD,
                    null,
                )
            }
        }, 500)
    }

    override fun showTutorial(shaked: Boolean) {
        if (this.isLandscape()) {
            setTutorial()
        } else {
            if (binding.teiPager?.currentItem == 0) setTutorial() else showToast(getString(R.string.no_intructions))
        }
    }

    fun toRelationships() {
        fromRelationship = true
    }

    private fun setProgramColor(programUid: String?) {
        themeManager.getThemePrimaryColor(
            programUid,
            { programColor: Int ->
                binding.toolbar.setBackgroundColor(programColor)
                binding.navigationBar.setIconsColor(programColor)
            },
        ) { themeColorRes: Int ->
            binding.toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    this@TeiDashboardMobileActivity,
                    themeColorRes,
                ),
            )
            binding.navigationBar.setIconsColor(
                ContextCompat.getColor(
                    this@TeiDashboardMobileActivity,
                    themeColorRes,
                ),
            )
        }
        binding.executePendingBindings()
        setTheme(themeManager.getProgramTheme())
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val typedValue = TypedValue()
            val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
            val colorToReturn = a.getColor(0, 0)
            a.recycle()
            window.statusBarColor = colorToReturn
        }
    }

    override fun showMoreOptions(view: View?) {
        val menu: Int = if (enrollmentUid == null) {
            R.menu.dashboard_tei_menu
        } else if (dashboardViewModel.groupByStage.value != false) {
            R.menu.dashboard_menu_group
        } else {
            R.menu.dashboard_menu
        }
        AppMenuHelper.Builder()
            .anchor(view!!)
            .menu(this, menu)
            .onMenuInflated { popupMenu: PopupMenu ->
                val deleteTeiItem = popupMenu.menu.findItem(R.id.deleteTei)
                val showDeleteTeiItem = presenter.checkIfTEICanBeDeleted()
                if (showDeleteTeiItem) {
                    deleteTeiItem.isVisible = true
                    deleteTeiItem.title =
                        String.format(deleteTeiItem.title.toString(), presenter.teType)
                } else {
                    deleteTeiItem.isVisible = false
                }

                val deleteEnrollmentItem = popupMenu.menu.findItem(R.id.deleteEnrollment)
                deleteEnrollmentItem.isVisible = if (enrollmentUid != null) {
                    presenter.checkIfEnrollmentCanBeDeleted(enrollmentUid)
                } else {
                    false
                }

                if (enrollmentUid != null) {
                    popupMenu.menu.findItem(R.id.deleteEnrollment).let { deleteEnrollmentItem ->
                        deleteEnrollmentItem.title = resourceManager.formatWithEnrollmentLabel(
                            programUid!!,
                            R.string.dashboard_menu_delete_enrollment_V2,
                            1,
                        )
                    }
                    popupMenu.menu.findItem(R.id.programSelector).let { programSelectorItem ->
                        programSelectorItem.title = resourceManager.formatWithEnrollmentLabel(
                            programUid!!,
                            R.string.program_selector_V2,
                            2,
                        )
                    }
                    val status = presenter.getEnrollmentStatus(enrollmentUid)
                    if (status == EnrollmentStatus.COMPLETED) {
                        popupMenu.menu.findItem(R.id.complete).isVisible = false
                    } else if (status == EnrollmentStatus.CANCELLED) {
                        popupMenu.menu.findItem(R.id.deactivate).isVisible = false
                    } else {
                        popupMenu.menu.findItem(R.id.activate).isVisible = false
                    }
                    if (dashboardViewModel.showFollowUpBar.value) {
                        popupMenu.menu.findItem(R.id.markForFollowUp).isVisible = false
                    }
                }
                Unit
            }
            .onMenuItemClicked { itemId: Int? ->
                when (itemId) {
                    R.id.showHelp -> {
                        analyticsHelper.setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                        showTutorial(true)
                    }

                    R.id.markForFollowUp -> dashboardViewModel.onFollowUp()
                    R.id.deleteTei -> showDeleteTEIConfirmationDialog()
                    R.id.deleteEnrollment -> showRemoveEnrollmentConfirmationDialog()
                    R.id.programSelector -> presenter.onEnrollmentSelectorClick()
                    R.id.groupEvents -> dashboardViewModel.setGrouping(true)
                    R.id.showTimeline -> dashboardViewModel.setGrouping(false)
                    R.id.complete -> {
                        dashboardViewModel.updateEnrollmentStatus(
                            EnrollmentStatus.COMPLETED,
                        )
                    }

                    R.id.activate -> dashboardViewModel.updateEnrollmentStatus(
                        EnrollmentStatus.ACTIVE,
                    )

                    R.id.deactivate -> dashboardViewModel.updateEnrollmentStatus(
                        EnrollmentStatus.CANCELLED,
                    )

                    R.id.share -> startQRActivity()
                }
                true
            }
            .build().show()
    }

    override fun updateNoteBadge(numberOfNotes: Int) {
        binding.navigationBar.updateBadge(R.id.navigation_notes, numberOfNotes)
    }

    fun observeFilters(): LiveData<Boolean>? {
        return null
    }

    override fun relationshipMap(): LiveData<Boolean> {
        return relationshipMap
    }

    override fun hideTabsAndDisableSwipe() {
        ViewCompat.setElevation(binding.toolbar, 0f)
    }

    override fun showTabsAndEnableSwipe() {
        ViewCompat.setElevation(binding.toolbar, elevation)
    }

    override fun displayStatusError(statusCode: StatusChangeResultCode) {
        when (statusCode) {
            StatusChangeResultCode.FAILED -> displayMessage(getString(R.string.something_wrong))
            StatusChangeResultCode.ACTIVE_EXIST -> displayMessage(getString(R.string.status_change_error_active_exist))
            StatusChangeResultCode.WRITE_PERMISSION_FAIL -> displayMessage(getString(R.string.permission_denied))
            StatusChangeResultCode.CHANGED -> {
                /*No message needed to be displayed*/
            }
        }
    }

    private fun showDeleteTEIConfirmationDialog() {
        DeleteBottomSheetDialog(
            title = getString(R.string.delete_tei_dialog_title).format(presenter.teType),
            description = getString(R.string.delete_tei_dialog_message).format(presenter.teType),
            mainButtonText = getString(R.string.delete),
            deleteForever = true,
            onMainButtonClick = {
                presenter.deleteTei()
            },
        ).show(
            supportFragmentManager,
            DeleteBottomSheetDialog.TAG,
        )
    }

    private fun showRemoveEnrollmentConfirmationDialog() {
        val dashboardModel = dashboardViewModel.dashboardModel.value
        if (dashboardModel is DashboardEnrollmentModel) {
            DeleteBottomSheetDialog(
                title = getString(R.string.remove_enrollment_dialog_title).format(
                    dashboardModel.currentProgram().displayName(),
                ),
                description = getString(R.string.remove_enrollment_dialog_message).format(
                    dashboardModel.currentProgram().displayName(),
                ),
                mainButtonText = getString(R.string.remove),
                onMainButtonClick = {
                    dashboardViewModel.deleteEnrollment { authorityErrorMessage() }
                },
            ).show(
                supportFragmentManager,
                DeleteBottomSheetDialog.TAG,
            )
        }
    }

    override fun onRelationshipMapLoaded() {
        binding.toolbarProgress.hide()
    }

    private fun startQRActivity() {
        analyticsHelper.trackMatomoEvent(TYPE_SHARE, TYPE_QR, SHARE_TEI)
        val intent = Intent(context, QrActivity::class.java)
        intent.putExtra(TEI_UID, teiUid)
        startActivity(intent)
    }

    companion object {
        private const val TEI_SYNC = "SYNC_TEI"

        @JvmStatic
        fun intent(
            context: Context?,
            teiUid: String?,
            programUid: String?,
            enrollmentUid: String?,
        ): Intent {
            val intent = Intent(context, TeiDashboardMobileActivity::class.java)
            intent.putExtra(TEI_UID, teiUid)
            intent.putExtra(Constants.PROGRAM_UID, programUid)
            intent.putExtra(Constants.ENROLLMENT_UID, enrollmentUid)
            return intent
        }
    }
}
