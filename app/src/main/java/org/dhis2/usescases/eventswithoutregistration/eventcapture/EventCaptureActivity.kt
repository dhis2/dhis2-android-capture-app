package org.dhis2.usescases.eventswithoutregistration.eventcapture

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.clipWithRoundedCorners
import org.dhis2.bindings.dp
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.PROGRAM_UID
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityEventCaptureBinding
import org.dhis2.ui.ErrorFieldList
import org.dhis2.ui.ThemeManager
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.DiscardButton
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle.MainButton
import org.dhis2.usescases.eventswithoutregistration.eventDetails.injection.EventDetailsComponent
import org.dhis2.usescases.eventswithoutregistration.eventDetails.injection.EventDetailsComponentProvider
import org.dhis2.usescases.eventswithoutregistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment.EventCaptureFormFragment
import org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment.OnEditionListener
import org.dhis2.usescases.eventswithoutregistration.eventcapture.model.EventCompletionDialog
import org.dhis2.usescases.eventswithoutregistration.eventinitial.EventInitialActivity
import org.dhis2.usescases.eventswithoutregistration.eventteidetails.EventTeiDetailsFragment
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teidashboard.DashboardProgramModel
import org.dhis2.usescases.teidashboard.DashboardViewModel
import org.dhis2.usescases.teidashboard.dashboardfragments.relationships.MapButtonObservable
import org.dhis2.utils.EventMode
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_EVENT
import org.dhis2.utils.analytics.SHOW_HELP
import org.dhis2.utils.customviews.FormBottomDialog
import org.dhis2.utils.customviews.FormBottomDialog.Companion.instance
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.customviews.navigationbar.setInitialPage
import org.dhis2.utils.granularsync.OPEN_ERROR_LOCATION
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import javax.inject.Inject

const val EXTRA_DETAILS_AS_FIRST_PAGE = "EXTRA_DETAILS_AS_FIRST_PAGE"

class EventCaptureActivity :
    ActivityGlobalAbstract(),
    EventCaptureContract.View,
    MapButtonObservable,
    EventDetailsComponentProvider {
    private var binding: ActivityEventCaptureBinding? = null

    @JvmField
    @Inject
    var presenter: EventCaptureContract.Presenter? = null

    var programStageUid: String? = null

    var enrollmentUid: String? = null
    private var setOfAttributeNames: Set<String> = emptySet()
    var teiUid: String? = null

    private var dashboardViewModel: DashboardViewModel? = null

    @JvmField
    @Inject
    var pageConfigurator: NavigationPageConfigurator? = null

    @JvmField
    @Inject
    var themeManager: ThemeManager? = null
    private var isEventCompleted = false
    private var eventMode: EventMode? = null

    @JvmField
    var eventCaptureComponent: EventCaptureComponent? = null
    var programUid: String? = null
    var eventUid: String? = null
    private val relationshipMapButton: LiveData<Boolean> = MutableLiveData(false)
    private var onEditionListener: OnEditionListener? = null
    private var adapter: EventCapturePagerAdapter? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        eventUid = intent.getStringExtra(Constants.EVENT_UID)
        eventCaptureComponent = this.app().userComponent()!!.plus(
            EventCaptureModule(
                this,
                eventUid,
                isPortrait(),
            ),
        )

        teiUid = intent.getStringExtra(Constants.TEI_UID)

        enrollmentUid = intent.getStringExtra(Constants.ENROLLMENT_UID)

        programUid = intent.getStringExtra(PROGRAM_UID)

        programStageUid = intent.getStringExtra(Constants.PROGRAM_STAGE_UID)
        eventCaptureComponent!!.inject(this)
        themeManager!!.setProgramTheme(intent.getStringExtra(PROGRAM_UID)!!)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture)
        binding?.presenter = presenter
        val navigationInitialPage =
            if (intent.getBooleanExtra(EXTRA_DETAILS_AS_FIRST_PAGE, false)) {
                0
            } else {
                1
            }
        eventMode = intent.getSerializableExtra(Constants.EVENT_MODE) as EventMode?
        setUpViewPagerAdapter(navigationInitialPage)
        presenter!!.programStageUid()
        setUpNavigationBar(navigationInitialPage)

        if (isLandscape()) {
            val stageUid: String = presenter!!.programStageUidString
            programStageUid = stageUid
            dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
            supportFragmentManager.beginTransaction()
                .replace(R.id.event_form, EventCaptureFormFragment.newInstance(eventUid, false))
                .commitAllowingStateLoss()
            supportFragmentManager.beginTransaction().replace(
                R.id.tei_column,
                EventTeiDetailsFragment.newInstance(
                    programUid,
                    teiUid,
                    enrollmentUid,
                    programStageUid,
                    setOfAttributeNames,
                ),
            ).commitAllowingStateLoss()
        }
        showProgress()
        presenter!!.initNoteCounter()
        presenter!!.init()
        binding?.syncButton?.setOnClickListener { showSyncDialog() }

        if (intent.shouldLaunchSyncDialog()) {
            showSyncDialog()
        }
    }

    private fun setUpViewPagerAdapter(initialPage: Int) {
        if (isLandscape()) {
            binding!!.eventViewLandPager!!.isUserInputEnabled = false
            adapter = EventCapturePagerAdapter(
                this,
                intent.getStringExtra(PROGRAM_UID),
                intent.getStringExtra(Constants.EVENT_UID),
                pageConfigurator!!.displayAnalytics(),
                pageConfigurator!!.displayRelationships(),
                false,
                false,
            )
            binding!!.eventViewLandPager!!.adapter = adapter
            binding!!.eventViewLandPager!!.setCurrentItem(initialPage, false)
            binding!!.eventViewLandPager!!.clipWithRoundedCorners(16.dp)
            binding!!.eventViewLandPager!!.registerOnPageChangeCallback(object :
                OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0 && eventMode !== EventMode.NEW) {
                        binding!!.syncButton.visibility = View.VISIBLE
                    } else {
                        binding!!.syncButton.visibility = View.GONE
                    }
                    if (position != 1) {
                        hideProgress()
                    }
                }
            })
        } else {
            binding!!.eventViewPager!!.isUserInputEnabled = false
            adapter = EventCapturePagerAdapter(
                this,
                intent.getStringExtra(PROGRAM_UID),
                intent.getStringExtra(Constants.EVENT_UID),
                pageConfigurator!!.displayAnalytics(),
                pageConfigurator!!.displayRelationships(),
                true,
                intent.getBooleanExtra(OPEN_ERROR_LOCATION, false),
            )

            binding!!.eventViewPager!!.adapter = adapter
            binding!!.eventViewPager!!.setCurrentItem(initialPage, false)
            binding!!.eventViewPager!!.clipWithRoundedCorners(16.dp)
            binding!!.eventViewPager!!.registerOnPageChangeCallback(object :
                OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onPageChange(position)
                }
            })
        }
    }

    private fun onPageChange(position: Int) {
        if (position == 0 && eventMode !== EventMode.NEW) {
            binding!!.syncButton.visibility = View.VISIBLE
        } else {
            binding!!.syncButton.visibility = View.GONE
        }
        if (position != 1) {
            hideProgress()
        }
    }

    private fun setUpNavigationBar(initialPage: Int) {
        binding!!.navigationBar.setInitialPage(initialPage)
        binding!!.navigationBar.pageConfiguration(pageConfigurator!!)
        binding!!.navigationBar.setOnItemSelectedListener { item: MenuItem ->
            run {
                if (isLandscape()) {
                    binding!!.eventViewLandPager!!.currentItem =
                        adapter!!.getDynamicTabIndex(item.itemId)
                } else {
                    binding!!.eventViewPager!!.currentItem =
                        adapter!!.getDynamicTabIndex(item.itemId)
                }
            }

            true
        }
    }

    fun restoreAdapter(programUid: String?, eventUid: String?) {
        adapter = null
        this.programUid = programUid
        this.eventUid = eventUid
        presenter!!.init()
    }

    override fun onPause() {
        presenter!!.onDettach()
        super.onPause()
    }

    override fun preselectStage(programStageUid: String?) {
//        No stage to pre select
    }

    override fun setData(program: DashboardProgramModel?) {
//        No data to set here
    }

    fun openDetails() {
        binding?.navigationBar?.selectItemAt(0)
    }

    fun openForm() {
        supportFragmentManager.findFragmentByTag("EVENT_SYNC")?.let {
            if (it is SyncStatusDialog) {
                it.dismiss()
            }
        }
        binding?.navigationBar?.selectItemAt(1)
    }

    override fun onResume() {
        super.onResume()
        presenter!!.refreshTabCounters()
    }

    override fun onDestroy() {
        presenter!!.onDettach()
        super.onDestroy()
    }

    override fun goBack() {
        onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (onEditionListener != null) {
            onEditionListener!!.onEditionListener()
        }
        finishEditMode()
    }

    private fun finishEditMode() {
        if (binding!!.navigationBar.isHidden()) {
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
                { presenter!!.deleteEvent() },
            )
            dialog.show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
        } else if (isFormScreen()) {
            presenter?.emitAction(EventCaptureAction.ON_BACK)
        } else {
            finishDataEntry()
        }
    }

    private fun isFormScreen(): Boolean {
        return adapter?.isFormScreenShown(binding?.eventViewPager?.currentItem) == true
    }

    override fun updatePercentage(primaryValue: Float) {
        binding!!.completion.setCompletionPercentage(primaryValue)
        if (!presenter!!.completionPercentageVisibility) {
            binding!!.completion.visibility = View.GONE
        }
    }

    override fun showCompleteActions(
        canComplete: Boolean,
        emptyMandatoryFields: Map<String, String>,
        eventCompletionDialog: EventCompletionDialog,
    ) {
        if (isPortrait() && (binding!!.navigationBar.selectedItemId == R.id.navigation_data_entry)) {
            val dialog = BottomSheetDialog(
                bottomSheetDialogUiModel = eventCompletionDialog.bottomSheetDialogUiModel,
                onMainButtonClicked = {
                    setAction(eventCompletionDialog.mainButtonAction)
                },
                onSecondaryButtonClicked = {
                    eventCompletionDialog.secondaryButtonAction?.let { setAction(it) }
                },
                content = if (eventCompletionDialog.fieldsWithIssues.isNotEmpty()) {
                    { bottomSheetDialog ->
                        ErrorFieldList(eventCompletionDialog.fieldsWithIssues) {
                            bottomSheetDialog.dismiss()
                        }
                    }
                } else {
                    null
                },
            )
            dialog.show(supportFragmentManager, SHOW_OPTIONS)
        }

        if (isLandscape() && hasOneOfTheBottomNavigationButtonSelected()) {
            val dialog = BottomSheetDialog(
                bottomSheetDialogUiModel = eventCompletionDialog.bottomSheetDialogUiModel,
                onMainButtonClicked = {
                    setAction(eventCompletionDialog.mainButtonAction)
                },
                onSecondaryButtonClicked = {
                    eventCompletionDialog.secondaryButtonAction?.let { setAction(it) }
                },
                content = if (eventCompletionDialog.fieldsWithIssues.isNotEmpty()) {
                    { bottomSheetDialog ->
                        ErrorFieldList(eventCompletionDialog.fieldsWithIssues) {
                            bottomSheetDialog.dismiss()
                        }
                    }
                } else {
                    null
                },
            )
            dialog.show(supportFragmentManager, SHOW_OPTIONS)
        }
    }

    private fun hasOneOfTheBottomNavigationButtonSelected(): Boolean {
        return (
            binding!!.navigationBar.selectedItemId == R.id.navigation_data_entry ||
                binding!!.navigationBar.selectedItemId == R.id.navigation_details ||
                binding!!.navigationBar.selectedItemId == R.id.navigation_analytics ||
                binding!!.navigationBar.selectedItemId == R.id.navigation_notes
            )
    }

    override fun saveAndFinish() {
        displayMessage(getString(R.string.saved))
        setAction(FormBottomDialog.ActionType.FINISH)
    }

    override fun attemptToSkip() {
        instance
            .setAccessDataWrite(presenter!!.canWrite())
            .setIsExpired(presenter!!.hasExpired())
            .setSkip(true)
            .setListener { actionType: FormBottomDialog.ActionType -> setAction(actionType) }
            .show(supportFragmentManager, SHOW_OPTIONS)
    }

    fun executeRules() {
        val fragment: EventTeiDetailsFragment? =
            supportFragmentManager.findFragmentById(R.id.tei_column) as EventTeiDetailsFragment?
        fragment?.onResume()
    }

    override fun attemptToReschedule() {
        instance
            .setAccessDataWrite(presenter!!.canWrite())
            .setIsExpired(presenter!!.hasExpired())
            .setReschedule(true)
            .setListener { actionType: FormBottomDialog.ActionType -> setAction(actionType) }
            .show(supportFragmentManager, SHOW_OPTIONS)
    }

    private fun setAction(actionType: FormBottomDialog.ActionType) {
        when (actionType) {
            FormBottomDialog.ActionType.COMPLETE -> {
                isEventCompleted = true
                presenter!!.completeEvent(false)
            }

            FormBottomDialog.ActionType.COMPLETE_ADD_NEW -> presenter!!.completeEvent(true)
            FormBottomDialog.ActionType.FINISH_ADD_NEW -> restartDataEntry()
            FormBottomDialog.ActionType.SKIP -> presenter!!.skipEvent()
            FormBottomDialog.ActionType.RESCHEDULE -> { // Do nothing
            }

            FormBottomDialog.ActionType.CHECK_FIELDS -> { // Do nothing
            }

            FormBottomDialog.ActionType.FINISH -> finishDataEntry()
            FormBottomDialog.ActionType.NONE -> { // Do nothing
            }
        }
    }

    override fun showSnackBar(messageId: Int) {
        val mySnackbar =
            Snackbar.make(binding!!.root, messageId, BaseTransientBottomBar.LENGTH_SHORT)
        mySnackbar.show()
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

    override fun renderInitialInfo(
        stageName: String,
        eventDate: String,
        orgUnit: String,
        catOption: String,
    ) {
        binding!!.programStageName.text = stageName
        val eventDataString = StringBuilder(String.format("%s | %s", eventDate, orgUnit))
        if (catOption.isNotEmpty()) {
            eventDataString.append(String.format(" | %s", catOption))
        }
        binding!!.eventSecundaryInfo.text = eventDataString
    }

    override fun getPresenter(): EventCaptureContract.Presenter {
        return presenter!!
    }

    override fun showMoreOptions(view: View) {
        AppMenuHelper.Builder().menu(this, R.menu.event_menu).anchor(view)
            .onMenuInflated { popupMenu: PopupMenu ->
                popupMenu.menu.findItem(R.id.menu_delete).isVisible =
                    presenter!!.canWrite() && presenter!!.isEnrollmentOpen
                popupMenu.menu.findItem(R.id.menu_share).isVisible = false
            }
            .onMenuItemClicked { itemId: Int? ->
                when (itemId) {
                    R.id.showHelp -> {
                        analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                        showTutorial(false)
                    }

                    R.id.menu_delete -> confirmDeleteEvent()
                    else -> { // Do nothing
                    }
                }
                false
            }
            .build()
            .show()
    }

    override fun showTutorial(shaked: Boolean) {
        showToast(getString(R.string.no_intructions))
    }

    private fun confirmDeleteEvent() {
        CustomDialog(
            this,
            getString(R.string.delete_event),
            getString(R.string.confirm_delete_event),
            getString(R.string.delete),
            getString(R.string.cancel),
            0,
            object : DialogClickListener {
                override fun onPositive() {
                    analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT)
                    presenter!!.deleteEvent()
                }

                override fun onNegative() {
                    // dismiss
                }
            },
        ).show()
    }

    override fun showEventIntegrityAlert() {
        MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setTitle(R.string.conflict)
            .setMessage(R.string.event_date_in_future_message)
            .setPositiveButton(
                R.string.change_event_date,
            ) { _, _ -> binding!!.navigationBar.selectItemAt(0) }
            .setNegativeButton(R.string.go_back) { _, _ -> back() }
            .setCancelable(false)
            .show()
    }

    override fun updateNoteBadge(numberOfNotes: Int) {
        binding!!.navigationBar.updateBadge(R.id.navigation_notes, numberOfNotes)
    }

    override fun showProgress() {
        runOnUiThread { binding!!.toolbarProgress.show() }
    }

    override fun hideProgress() {
        Handler(Looper.getMainLooper()).postDelayed(
            { runOnUiThread { binding!!.toolbarProgress.hide() } },
            1000,
        )
    }

    override fun showNavigationBar() {
        binding!!.navigationBar.show()
    }

    override fun hideNavigationBar() {
        if (isPortrait()) {
            binding!!.navigationBar.hide()
        }
    }

    override fun relationshipMap(): LiveData<Boolean> {
        return relationshipMapButton
    }

    override fun onRelationshipMapLoaded() {
        // there are no relationships on events
    }

    fun setFormEditionListener(onEditionListener: OnEditionListener?) {
        this.onEditionListener = onEditionListener
    }

    override fun provideEventDetailsComponent(module: EventDetailsModule?): EventDetailsComponent? {
        return eventCaptureComponent!!.plus(module)
    }

    private fun showSyncDialog() {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(SyncContext.Event(eventUid!!))
            .show("EVENT_SYNC")
    }

    companion object {
        private const val SHOW_OPTIONS = "SHOW_OPTIONS"

        @JvmStatic
        fun getActivityBundle(
            eventUid: String,
            programUid: String,
            eventMode: EventMode,
            teiUid: String?,
            enrollmentUid: String?,
        ): Bundle {
            val bundle = Bundle()
            bundle.putString(Constants.EVENT_UID, eventUid)
            bundle.putString(PROGRAM_UID, programUid)
            bundle.putSerializable(Constants.EVENT_MODE, eventMode)

            if (teiUid != null) {
                bundle.putString(Constants.TEI_UID, teiUid)
            }

            if (enrollmentUid != null) {
                bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
            }
            return bundle
        }

        fun intent(
            context: Context,
            eventUid: String,
            programUid: String,
            openDetailsAsFirstPage: Boolean,
            eventMode: EventMode,
        ): Intent {
            return Intent(context, EventCaptureActivity::class.java).apply {
                putExtra(Constants.EVENT_UID, eventUid)
                putExtra(PROGRAM_UID, programUid)
                putExtra(EXTRA_DETAILS_AS_FIRST_PAGE, openDetailsAsFirstPage)
                putExtra(Constants.EVENT_MODE, eventMode)
            }
        }
    }
}
