package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.Constants
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
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.OnEditionListener
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable
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
    override fun onCreate(savedInstanceState: Bundle?) {
        eventUid = intent.getStringExtra(Constants.EVENT_UID)
        eventCaptureComponent = this.app().userComponent()!!.plus(
            EventCaptureModule(
                this,
                eventUid,
            ),
        )
        eventCaptureComponent!!.inject(this)
        themeManager!!.setProgramTheme(intent.getStringExtra(Constants.PROGRAM_UID)!!)
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
        setUpNavigationBar(navigationInitialPage)
        showProgress()
        presenter!!.initNoteCounter()
        presenter!!.init()
        binding?.syncButton?.setOnClickListener { showSyncDialog() }

        if (intent.shouldLaunchSyncDialog()) {
            showSyncDialog()
        }
    }

    private fun setUpViewPagerAdapter(initialPage: Int) {
        binding!!.eventViewPager.isUserInputEnabled = false
        adapter = EventCapturePagerAdapter(
            this,
            intent.getStringExtra(Constants.PROGRAM_UID),
            intent.getStringExtra(Constants.EVENT_UID),
            pageConfigurator!!.displayAnalytics(),
            pageConfigurator!!.displayRelationships(),
            intent.getBooleanExtra(OPEN_ERROR_LOCATION, false),
        )
        binding!!.eventViewPager.adapter = adapter
        binding!!.eventViewPager.setCurrentItem(initialPage, false)
        binding!!.eventViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
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
    }

    private fun setUpNavigationBar(initialPage: Int) {
        binding!!.navigationBar.setInitialPage(initialPage)
        binding!!.navigationBar.pageConfiguration(pageConfigurator!!)
        binding!!.navigationBar.setOnNavigationItemSelectedListener { item: MenuItem ->
            binding!!.eventViewPager.currentItem = adapter!!.getDynamicTabIndex(item.itemId)
            true
        }
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
        super.onBackPressed()
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
        if (binding!!.navigationBar.selectedItemId == R.id.navigation_data_entry) {
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

    override fun updateProgramStageName(stageName: String?) {
        binding?.programStageName?.text = stageName;
    }

    fun refreshProgramStageName() {
        presenter!!.refreshProgramStage()
    }

    override fun SaveAndFinish() {
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
        if (catOption != null && !catOption.isEmpty()) {
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
                Unit
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
        binding!!.navigationBar.hide()
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
            openDetailsAsFirstPage: Boolean,
            eventMode: EventMode,
        ): Intent {
            return Intent(context, EventCaptureActivity::class.java).apply {
                putExtra(Constants.EVENT_UID, eventUid)
                putExtra(Constants.PROGRAM_UID, programUid)
                putExtra(EXTRA_DETAILS_AS_FIRST_PAGE, openDetailsAsFirstPage)
                putExtra(Constants.EVENT_MODE, eventMode)
            }
        }
    }
}
