package org.dhis2.usescases.teiDashboard

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.TEI_UID
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OUTreeModel
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityDashboardMobileBinding
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.ui.provider.FormResultDialogProvider
import org.dhis2.tracker.TEIDashboardItems
import org.dhis2.tracker.relationships.model.RelationshipTopBarIconState
import org.dhis2.ui.ThemeManager
import org.dhis2.ui.dialogs.bottomsheet.DeleteBottomSheetDialog
import org.dhis2.usescases.enrollment.DateEditionWarningHandler
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.enrollment.EnrollmentActivity.Companion.getIntent
import org.dhis2.usescases.enrollment.EnrollmentFormBuilderConfig
import org.dhis2.usescases.enrollment.buildEnrollmentForm
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.qrCodes.QrActivity
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VISUALIZATION_TYPE
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataActivityContract
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment.Companion.newInstance
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity
import org.dhis2.usescases.teiDashboard.ui.RelationshipTopBarIcon
import org.dhis2.usescases.teiDashboard.ui.getEnrollmentMenuList
import org.dhis2.usescases.teiDashboard.ui.setButtonContent
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SHARE_TEI
import org.dhis2.utils.analytics.SHOW_HELP
import org.dhis2.utils.analytics.TYPE_QR
import org.dhis2.utils.analytics.TYPE_SHARE
import org.dhis2.utils.customviews.MoreOptionsWithDropDownMenuButton
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import javax.inject.Inject

class TeiDashboardMobileActivity :
    ActivityGlobalAbstract(),
    TeiDashboardContracts.View,
    MapButtonObservable,
    TEIDataActivityContract {
    private var currentOrientation = -1

    @Inject
    lateinit var presenter: TeiDashboardContracts.Presenter

    @Inject
    lateinit var dateEditionWarningHandler: DateEditionWarningHandler

    @Inject
    lateinit var enrollmentResultDialogProvider: FormResultDialogProvider

    var featureConfig: FeatureConfigRepository? = null
        @Inject set

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

    @Inject
    lateinit var eventResourcesProvider: EventResourcesProvider

    lateinit var programModel: DashboardProgramModel
    var teiUid: String? = null
    var programUid: String? = null
    var enrollmentUid: String? = null
    lateinit var binding: ActivityDashboardMobileBinding
    private lateinit var dashboardViewModel: DashboardViewModel

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

        setRelationshipMapIcon()
        setSyncButtonListener()
        setFormViewForLandScape()
        setEditButton()
        observeErrorMessages()
        observeProgressBar()
        observeDashboardModel()
        setUpNavigationBar()
        showLoadingProgress(false)
        setupMoreOptionsMenu()
    }

    private fun observeErrorMessages() {
        dashboardViewModel.showStatusErrorMessages.observe(this) {
            displayStatusError(it)
        }
    }

    private fun observeProgressBar() {
        dashboardViewModel.isLoading.observe(this) {
            showLoadingProgress(it)
        }
    }

    private fun setSyncButtonListener() {
        binding.syncButton.setOnClickListener { openSyncDialog() }
        if (intent.shouldLaunchSyncDialog()) {
            openSyncDialog()
        }
    }

    private fun observeDashboardModel() {
        dashboardViewModel.dashboardModel.observe(this) {
            if (sessionManagerServiceImpl.isUserLoggedIn()) {
                when (it) {
                    is DashboardEnrollmentModel -> setData(it)
                    is DashboardTEIModel -> setDataWithOutProgram(it)
                    else -> // Do nothing
                        Unit
                }
            }
        }
    }

    private fun setRelationshipMapIcon() {
        binding.relationshipIcon.setContent {
            val relationshipTopBarIconState by dashboardViewModel.relationshipTopBarIconState.collectAsState()
            RelationshipTopBarIcon(
                relationshipTopBarIconState = relationshipTopBarIconState,
            ) {
                when (val uiState = relationshipTopBarIconState) {
                    is RelationshipTopBarIconState.Selecting -> {
                        uiState.onClickListener()
                    }

                    is RelationshipTopBarIconState.List -> {
                        networkUtils.performIfOnline(
                            context = this,
                            action = {
                                dashboardViewModel.updateRelationshipsTopBarIconState(
                                    RelationshipTopBarIconState.Map(),
                                )
                            },
                            noNetworkMessage = getString(R.string.msg_network_connection_maps),
                        )

                        binding.toolbarProgress.visibility = View.VISIBLE
                        binding.toolbarProgress.hide()
                        relationshipMap.value = true
                    }

                    is RelationshipTopBarIconState.Map -> {
                        networkUtils.performIfOnline(
                            context = this,
                            action = {
                                dashboardViewModel.updateRelationshipsTopBarIconState(
                                    RelationshipTopBarIconState.List(),
                                )
                            },
                            noNetworkMessage = getString(R.string.msg_network_connection_maps),
                        )
                        relationshipMap.value = false
                    }
                }
            }
        }
    }

    private fun setFormViewForLandScape() {
        if (isLandscape() && enrollmentUid != null) {
            val saveButton = findViewById<View>(R.id.saveLand) as FloatingActionButton
            buildEnrollmentForm(
                config = EnrollmentFormBuilderConfig(
                    enrollmentUid = enrollmentUid!!,
                    programUid = programUid!!,
                    enrollmentMode = EnrollmentMode.CHECK,
                    hasWriteAccess = presenter.hasWriteAccess(),
                    openErrorLocation = false,
                    containerId = R.id.tei_form_view,
                    loadingView = binding.toolbarProgress,
                    saveButton = saveButton,
                ),
                locationProvider = locationProvider,
                dateEditionWarningHandler = dateEditionWarningHandler,
                enrollmentResultDialogProvider = enrollmentResultDialogProvider,
            ) {
                dashboardViewModel.updateDashboard()
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

    private fun setUpNavigationBar() {
        binding.navigationBar.setContent {
            DHIS2Theme {
                val uiState by dashboardViewModel.navigationBarUIState.collectAsState()
                var selectedHomeItemIndex by remember(uiState) {
                    mutableIntStateOf(
                        uiState.items.indexOfFirst {
                            it.id == uiState.selectedItem
                        },
                    )
                }

                NavigationBar(
                    items = uiState.items,
                    selectedItemIndex = selectedHomeItemIndex,
                ) { itemId ->
                    selectedHomeItemIndex = uiState.items.indexOfFirst { it.id == itemId }
                    dashboardViewModel.onNavigationItemSelected(itemId)
                }

                uiState.selectedItem?.let {
                    navigateToFragment(it)
                }
            }
        }
    }

    private fun navigateToFragment(item: TEIDashboardItems) {
        val fragment = when (item) {
            TEIDashboardItems.DETAILS -> newInstance(
                programUid,
                teiUid,
                enrollmentUid,
            )

            TEIDashboardItems.ANALYTICS -> {
                presenter.trackDashboardAnalytics()
                IndicatorsFragment().apply {
                    arguments = Bundle().apply {
                        putString(VISUALIZATION_TYPE, VisualizationType.TRACKER.name)
                    }
                }
            }

            TEIDashboardItems.RELATIONSHIPS -> {
                presenter.trackDashboardRelationships()
                RelationshipFragment().apply {
                    arguments = RelationshipFragment.withArguments(
                        programUid,
                        teiUid,
                        enrollmentUid,
                        null,
                    )
                }
            }
            TEIDashboardItems.NOTES -> {
                presenter.trackDashboardNotes()
                NotesFragment.newTrackerInstance(programUid!!, teiUid!!)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, item.name)
            .commit()

        updateTopBar(item)
    }

    private fun updateTopBar(item: TEIDashboardItems) {
        if (item === TEIDashboardItems.RELATIONSHIPS) {
            binding.relationshipIcon.visibility = View.VISIBLE
        } else {
            binding.relationshipIcon.visibility = View.GONE
        }

        if (this.isPortrait()) {
            if (item == TEIDashboardItems.DETAILS && programUid != null) {
                binding.toolbarTitle?.visibility = View.GONE
                binding.editButton?.visibility = View.VISIBLE
                binding.syncButton.visibility = View.GONE
            } else {
                binding.toolbarTitle?.visibility = View.VISIBLE
                binding.editButton?.visibility = View.GONE
                binding.syncButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManagerServiceImpl.isUserLoggedIn()) {
            currentOrientation = if (this.isLandscape()) 1 else 0
            presenter.refreshTabCounters()
            dashboardViewModel.updateDashboard()
        }
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun onDestroy() {
        (applicationContext as App).releaseDashboardComponent()
        super.onDestroy()
    }

    override fun openSyncDialog() {
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.tei_main_view, newInstance(programUid, teiUid, enrollmentUid))
                .commitAllowingStateLoss()
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
        this.programUid = programUid
    }

    override fun handleTeiDeletion() {
        finish()
    }

    private fun handleEnrollmentDeletion(hasMoreEnrollments: Boolean) {
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
        if (this.isLandscape()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tei_main_view, newInstance(programUid, teiUid, enrollmentUid))
                .commitAllowingStateLoss()
        } else {
            navigateToFragment(TEIDashboardItems.DETAILS)
        }
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
            if (binding.editButton?.visibility == View.VISIBLE) {
                setTutorial()
            } else {
                showToast(getString(R.string.no_intructions))
            }
        }
    }

    private fun setProgramColor(programUid: String?) {
        themeManager.getThemePrimaryColor(
            programUid,
            { programColor: Int ->
                binding.toolbar.setBackgroundColor(programColor)
            },
        ) { themeColorRes: Int ->
            binding.toolbar.setBackgroundColor(
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

    override fun updateNoteBadge(numberOfNotes: Int) {
        dashboardViewModel.updateNoteCounter(numberOfNotes)
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

    override fun showOrgUnitSelector(
        programUid: String,
    ) {
        val ownerOrgUnit = dashboardViewModel.dashboardModel.value?.ownerOrgUnit
        OUTreeFragment.Builder()
            .singleSelection()
            .withModel(
                OUTreeModel(
                    title = getString(
                        R.string.transfer_tei_org_sheet_title,
                        presenter.teType.lowercase(),
                    ),
                    subtitle = getString(
                        R.string.transfer_tei_org_sheet_description,
                        ownerOrgUnit?.displayName(),
                    ),
                    headerAlignment = TextAlign.Start,
                    showClearButton = false,
                    doneButtonText = getString(R.string.transfer),
                    doneButtonIcon = Icons.Outlined.MoveDown,
                    hideOrgUnits = ownerOrgUnit?.let { listOf(it) },
                ),
            )
            .orgUnitScope(
                OrgUnitSelectorScope.ProgramSearchScope(programUid),
            )
            .onSelection { selectedOrgUnits ->
                if (selectedOrgUnits.isNotEmpty()) {
                    dashboardViewModel.transferTei(
                        selectedOrgUnits.first().uid(),
                    ) {
                        val contextView = findViewById<View>(R.id.navigationBar)
                        Snackbar.make(
                            contextView,
                            R.string.successfully_transferred,
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
            .build()
            .show(supportFragmentManager, "ORG_UNIT_DIALOG")
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
                    binding.toolbarProgress.show()
                    dashboardViewModel.deleteEnrollment(
                        onSuccess = {
                            binding.toolbarProgress.hide()
                            handleEnrollmentDeletion(it == true)
                        },
                        onAuthorityError = {
                            authorityErrorMessage()
                        },
                    )
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

    override fun updateRelationshipsTopBarIconState(topBarIconState: RelationshipTopBarIconState) {
        dashboardViewModel.updateRelationshipsTopBarIconState(topBarIconState)
    }

    override fun finishActivity() {
        finish()
    }

    override fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String) {
        startActivity(
            intent(
                this,
                teiUid,
                programUid,
                enrollmentUid,
            ),
        )
    }

    override fun executeOnUIThread() {
        activity.runOnUiThread {
            showDescription(getString(R.string.error_applying_rule_effects))
        }
    }

    override fun getContext(): Context {
        return this
    }

    override fun activityTeiUid(): String? {
        return teiUid
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

    private fun setupMoreOptionsMenu() {
        binding.moreOptions.setContent {
            val menuItems = getEnrollmentMenuList(
                enrollmentUid = enrollmentUid,
                resourceManager = resourceManager,
                presenter = presenter,
                dashboardViewModel = dashboardViewModel,
            )

            var expanded by remember { mutableStateOf(false) }

            MoreOptionsWithDropDownMenuButton(
                menuItems,
                expanded,
                onMenuToggle = { expanded = it },
            ) { itemId ->
                when (itemId) {
                    EnrollmentMenuItem.SYNC -> openSyncDialog()
                    EnrollmentMenuItem.TRANSFER -> presenter.onTransferClick()
                    EnrollmentMenuItem.FOLLOW_UP -> dashboardViewModel.onFollowUp()
                    EnrollmentMenuItem.GROUP_BY_STAGE -> dashboardViewModel.setGrouping(true)
                    EnrollmentMenuItem.VIEW_TIMELINE -> dashboardViewModel.setGrouping(false)
                    EnrollmentMenuItem.HELP -> {
                        analyticsHelper.setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                        showTutorial(true)
                    }
                    EnrollmentMenuItem.ENROLLMENTS -> presenter.onEnrollmentSelectorClick()
                    EnrollmentMenuItem.SHARE -> startQRActivity()
                    EnrollmentMenuItem.ACTIVATE -> dashboardViewModel.updateEnrollmentStatus(
                        EnrollmentStatus.ACTIVE,
                    )

                    EnrollmentMenuItem.DEACTIVATE -> dashboardViewModel.updateEnrollmentStatus(
                        EnrollmentStatus.CANCELLED,
                    )

                    EnrollmentMenuItem.COMPLETE -> {
                        dashboardViewModel.updateEnrollmentStatus(
                            EnrollmentStatus.COMPLETED,
                        )
                    }

                    EnrollmentMenuItem.DELETE -> showDeleteTEIConfirmationDialog()
                    EnrollmentMenuItem.REMOVE -> showRemoveEnrollmentConfirmationDialog()
                }
            }
        }
    }
}

enum class EnrollmentMenuItem {
    SYNC,
    TRANSFER,
    FOLLOW_UP,
    GROUP_BY_STAGE,
    VIEW_TIMELINE,
    HELP,
    ENROLLMENTS,
    SHARE,
    ACTIVATE,
    DEACTIVATE,
    COMPLETE,
    DELETE,
    REMOVE
}
