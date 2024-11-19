package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.Constants
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityEventCaptureBinding
import org.dhis2.form.model.EventMode
import org.dhis2.tracker.relationships.model.RelationshipTopBarIconState
import org.dhis2.ui.ThemeManager
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.DiscardButton
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.MainButton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormFragment
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataActivityContract
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment.Companion.newInstance
import org.dhis2.usescases.teiDashboard.ui.RelationshipTopBarIcon
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_EVENT
import org.dhis2.utils.analytics.SHOW_HELP
import org.dhis2.utils.customviews.MoreOptionsWithDropDownMenuButton
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.granularsync.OPEN_ERROR_LOCATION
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemStyle
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import javax.inject.Inject

class EventCaptureActivity :
    ActivityGlobalAbstract(),
    EventCaptureContract.View,
    MapButtonObservable,
    EventDetailsComponentProvider,
    TEIDataActivityContract {
    private lateinit var binding: ActivityEventCaptureBinding

    @Inject
    override lateinit var presenter: EventCaptureContract.Presenter

    @JvmField
    @Inject
    var pageConfigurator: NavigationPageConfigurator? = null

    @JvmField
    @Inject
    var themeManager: ThemeManager? = null
    private var isEventCompleted = false
    private lateinit var eventMode: EventMode

    @Inject
    lateinit var eventResourcesProvider: EventResourcesProvider

    @JvmField
    var eventCaptureComponent: EventCaptureComponent? = null
    var programUid: String? = null
    var eventUid: String? = null
    private var teiUid: String? = null
    private var enrollmentUid: String? = null
    private val relationshipMapButton: LiveData<Boolean> = MutableLiveData(false)
    private var adapter: EventCapturePagerAdapter? = null
    private var eventViewPager: ViewPager2? = null
    private var dashboardViewModel: DashboardViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        eventUid = intent.getStringExtra(Constants.EVENT_UID)
        programUid = intent.getStringExtra(Constants.PROGRAM_UID)
        setUpEventCaptureComponent(eventUid!!)
        teiUid = presenter.getTeiUid()
        enrollmentUid = presenter.getEnrollmentUid()
        themeManager!!.setProgramTheme(intent.getStringExtra(Constants.PROGRAM_UID)!!)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture)
        binding.presenter = presenter
        eventViewPager = when {
            this.isLandscape() -> binding.eventViewLandPager
            else -> binding.eventViewPager
        }
        eventMode = intent.getSerializableExtra(Constants.EVENT_MODE) as EventMode
        setUpViewPagerAdapter()
        setUpNavigationBar()
        setupMoreOptionsMenu()

        setUpEventCaptureFormLandscape(eventUid ?: "")
        if (this.isLandscape() && areTeiUidAndEnrollmentUidNotNull()) {
            val viewModelFactory = this.app().dashboardComponent()?.dashboardViewModelFactory()

            viewModelFactory?.let {
                dashboardViewModel =
                    ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]
                supportFragmentManager.beginTransaction()
                    .replace(R.id.tei_column, newInstance(programUid, teiUid, enrollmentUid))
                    .commit()
                dashboardViewModel?.updateSelectedEventUid(eventUid)
            }
        }
        showProgress()
        presenter.initNoteCounter()
        presenter.init()
        binding.syncButton.setOnClickListener { showSyncDialog(EVENT_SYNC) }

        if (intent.shouldLaunchSyncDialog()) {
            showSyncDialog(EVENT_SYNC)
        }
    }

    private fun setUpViewPagerAdapter() {
        eventViewPager?.isUserInputEnabled = false
        adapter = EventCapturePagerAdapter(
            this,
            intent.getStringExtra(Constants.PROGRAM_UID) ?: "",
            intent.getStringExtra(Constants.EVENT_UID) ?: "",
            pageConfigurator!!.displayAnalytics(),
            pageConfigurator!!.displayRelationships(),
            intent.getBooleanExtra(OPEN_ERROR_LOCATION, false),
            eventMode,
        )
        eventViewPager?.adapter = adapter
        eventViewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0 && eventMode !== EventMode.NEW) {
                    binding.syncButton.visibility = View.VISIBLE
                } else {
                    binding.syncButton.visibility = View.GONE
                }
                if (position != 1) {
                    hideProgress()
                }
            }
        })
    }

    private fun setUpNavigationBar() {
        eventViewPager?.registerOnPageChangeCallback(
            object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    presenter.onSetNavigationPage(position)
                }
            },
        )
        binding.navigationBar.setContent {
            DHIS2Theme {
                val uiState by presenter.observeNavigationBarUIState()
                val selectedItemIndex by remember(uiState) {
                    mutableIntStateOf(
                        uiState.items.indexOfFirst {
                            it.id == uiState.selectedItem
                        },
                    )
                }

                AnimatedVisibility(
                    visible = uiState.items.isNotEmpty(),
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        items = uiState.items,
                        selectedItemIndex = selectedItemIndex,
                    ) { page ->
                        presenter.onNavigationPageChanged(page)
                        eventViewPager?.currentItem = adapter!!.getDynamicTabIndex(page)
                    }
                }
            }
        }
    }

    private fun setUpEventCaptureFormLandscape(eventUid: String) {
        if (this.isLandscape()) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.event_form,
                    EventCaptureFormFragment.newInstance(eventUid, false, eventMode),
                )
                .commit()
        }
    }

    private fun setUpEventCaptureComponent(eventUid: String) {
        eventCaptureComponent = app().userComponent()!!.plus(
            EventCaptureModule(
                this,
                eventUid,
                this.isPortrait(),
            ),
        )
        eventCaptureComponent!!.inject(this)
    }

    private fun updateLandscapeViewsOnEventChange(newEventUid: String) {
        if (newEventUid != this.eventUid) {
            this.eventUid = newEventUid
            setUpEventCaptureComponent(newEventUid)
            setUpViewPagerAdapter()
            setUpNavigationBar()
            setUpEventCaptureFormLandscape(newEventUid)
            showProgress()
            presenter.initNoteCounter()
            presenter.init()
        }
    }

    private fun areTeiUidAndEnrollmentUidNotNull(): Boolean {
        return teiUid != null && enrollmentUid != null
    }

    fun openDetails() {
        presenter.onNavigationPageChanged(NavigationPage.DETAILS)
    }

    fun openForm() {
        supportFragmentManager.findFragmentByTag("EVENT_SYNC")?.let {
            if (it is SyncStatusDialog) {
                it.dismiss()
            }
        }
        presenter.onNavigationPageChanged(NavigationPage.DATA_ENTRY)
    }

    override fun onResume() {
        super.onResume()
        presenter.refreshTabCounters()
        with(dashboardViewModel) {
            this?.selectedEventUid()
                ?.observe(this@EventCaptureActivity, ::updateLandscapeViewsOnEventChange)
        }
    }

    override fun onDestroy() {
        presenter.onDettach()
        super.onDestroy()
    }

    override fun goBack() {
        onBackPressed()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishEditMode()
    }

    private fun finishEditMode() {
        if (binding.navigationBar.visibility == View.GONE) {
            showNavigationBar()
        } else {
            attemptFinish()
        }
    }

    private fun attemptFinish() {
        if (eventMode === EventMode.NEW) {
            val bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = getString(R.string.title_delete_go_back),
                message = getString(R.string.discard_go_back),
                iconResource = R.drawable.ic_error_outline,
                mainButton = MainButton(R.string.keep_editing),
                secondaryButton = DiscardButton(),
            )
            val dialog = BottomSheetDialog(
                bottomSheetDialogUiModel,
                {
                    /*Unused*/
                },
                { presenter.deleteEvent() },
            )
            dialog.show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
        } else if (isFormScreen()) {
            presenter.emitAction(EventCaptureAction.ON_BACK)
        } else {
            finishDataEntry()
        }
    }

    private fun isFormScreen(): Boolean {
        return if (this.isPortrait()) {
            adapter?.isFormScreenShown(binding.eventViewPager?.currentItem) == true
        } else {
            true
        }
    }

    override fun updatePercentage(primaryValue: Float) {
        binding.completion.setCompletionPercentage(primaryValue)
        if (!presenter.getCompletionPercentageVisibility()) {
            binding.completion.visibility = View.GONE
        }
    }

    override fun saveAndFinish() {
        displayMessage(getString(R.string.saved))
        finishDataEntry()
    }

    override fun showSnackBar(messageId: Int, programStage: String) {
        showToast(
            eventResourcesProvider.formatWithProgramStageEventLabel(
                messageId,
                programStage,
                programUid,
            ),
        )
    }

    override fun restartDataEntry() {
        val bundle = Bundle()
        startActivity(EventInitialActivity::class.java, bundle, true, false, null)
    }

    override fun finishDataEntry() {
        val intent = Intent()
        if (isEventCompleted) {
            intent.putExtra(
                Constants.EVENT_UID,
                getIntent().getStringExtra(
                    Constants.EVENT_UID,
                ),
            )
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun renderInitialInfo(stageName: String) {
        binding.programStageName.text = stageName
    }

    private fun setupMoreOptionsMenu() {
        binding.moreOptions.setContent {
            var expanded by remember { mutableStateOf(false) }

            MoreOptionsWithDropDownMenuButton(
                getMenuItems(),
                expanded,
                onMenuToggle = { expanded = it },
            ) { itemId ->
                when (itemId) {
                    EventCaptureMenuItem.SHOW_HELP -> {
                        analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                        showTutorial(false)
                    }

                    EventCaptureMenuItem.DELETE -> confirmDeleteEvent()
                }
            }
        }
    }

    private fun getMenuItems(): List<MenuItemData<EventCaptureMenuItem>> {
        return buildList {
            add(
                MenuItemData(
                    id = EventCaptureMenuItem.SHOW_HELP,
                    label = getString(R.string.showHelp),
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.HelpOutline),
                ),
            )
            if (presenter.canWrite() && presenter.isEnrollmentOpen()) {
                add(
                    MenuItemData(
                        id = EventCaptureMenuItem.DELETE,
                        label = getString(R.string.delete),
                        style = MenuItemStyle.ALERT,
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.DeleteForever),
                    ),
                )
            }
        }
    }

    override fun showTutorial(shaked: Boolean) {
        showToast(getString(R.string.no_intructions))
    }

    private fun confirmDeleteEvent() {
        presenter.programStage().let {
            CustomDialog(
                this,
                eventResourcesProvider.formatWithProgramStageEventLabel(
                    R.string.delete_event_label,
                    programStageUid = it,
                    programUid,
                ),
                eventResourcesProvider.formatWithProgramStageEventLabel(
                    R.string.confirm_delete_event_label,
                    programStageUid = it,
                    programUid,
                ),
                getString(R.string.delete),
                getString(R.string.cancel),
                0,
                object : DialogClickListener {
                    override fun onPositive() {
                        analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT)
                        presenter.deleteEvent()
                    }

                    override fun onNegative() {
                        // dismiss
                    }
                },
            ).show()
        }
    }

    override fun showEventIntegrityAlert() {
        MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setTitle(R.string.conflict)
            .setMessage(
                eventResourcesProvider.formatWithProgramStageEventLabel(
                    R.string.event_label_date_in_future_message,
                    programStageUid = presenter.programStage(),
                    programUid = programUid,
                ),
            )
            .setPositiveButton(
                R.string.change_event_date,
            ) { _, _ ->
                presenter.onSetNavigationPage(0)
            }
            .setNegativeButton(R.string.go_back) { _, _ -> back() }
            .setCancelable(false)
            .show()
    }

    override fun updateNoteBadge(numberOfNotes: Int) {
        presenter.updateNotesBadge(numberOfNotes)
    }

    override fun showProgress() {
        runOnUiThread { binding.toolbarProgress.show() }
    }

    override fun hideProgress() {
        Handler(Looper.getMainLooper()).postDelayed(
            { runOnUiThread { binding.toolbarProgress.hide() } },
            1000,
        )
    }

    override fun showNavigationBar() {
        binding.navigationBar.show()
    }

    override fun hideNavigationBar() {
        binding.navigationBar.hide()
    }

    override fun relationshipMap(): LiveData<Boolean> {
        return relationshipMapButton
    }

    override fun onRelationshipMapLoaded() {
        // there are no relationships on events
    }

    override fun updateRelationshipsTopBarIconState(topBarIconState: RelationshipTopBarIconState) {
        when (topBarIconState) {
            is RelationshipTopBarIconState.Selecting -> {
                binding.relationshipIcon.visibility = View.VISIBLE
                binding.relationshipIcon.setContent {
                    RelationshipTopBarIcon(
                        relationshipTopBarIconState = topBarIconState,
                    ) {
                        topBarIconState.onClickListener()
                    }
                }
            }

            else -> {
                binding.relationshipIcon.visibility = View.GONE
            }
        }
    }

    override fun provideEventDetailsComponent(module: EventDetailsModule?): EventDetailsComponent? {
        return eventCaptureComponent!!.plus(module)
    }

    private fun showSyncDialog(syncType: String) {
        val syncContext = when (syncType) {
            TEI_SYNC -> enrollmentUid?.let { SyncContext.Enrollment(it) }
            EVENT_SYNC -> SyncContext.Event(eventUid!!)
            else -> null
        }

        syncContext?.let {
            SyncStatusDialog.Builder()
                .withContext(this)
                .withSyncContext(it)
                .onDismissListener(object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged && syncType == TEI_SYNC) {
                            dashboardViewModel?.updateDashboard()
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
                }
                .show(syncType)
        }
    }

    override fun openSyncDialog() {
        showSyncDialog(TEI_SYNC)
    }

    override fun finishActivity() {
        finish()
    }

    override fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String) {
        // we do not restore adapter in events
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
        private const val SHOW_OPTIONS = "SHOW_OPTIONS"
        private const val TEI_SYNC = "SYNC_TEI"
        private const val EVENT_SYNC = "EVENT_SYNC"

        @JvmStatic
        fun getActivityBundle(eventUid: String, programUid: String, eventMode: EventMode): Bundle {
            val bundle = Bundle()
            bundle.putString(Constants.EVENT_UID, eventUid)
            bundle.putString(Constants.PROGRAM_UID, programUid)
            bundle.putSerializable(Constants.EVENT_MODE, eventMode)
            return bundle
        }

        fun intent(
            context: Context,
            eventUid: String,
            programUid: String,
            eventMode: EventMode,
        ): Intent {
            return Intent(context, EventCaptureActivity::class.java).apply {
                putExtra(Constants.EVENT_UID, eventUid)
                putExtra(Constants.PROGRAM_UID, programUid)
                putExtra(Constants.EVENT_MODE, eventMode)
            }
        }
    }
}

enum class EventCaptureMenuItem {
    SHOW_HELP,
    DELETE,
}
