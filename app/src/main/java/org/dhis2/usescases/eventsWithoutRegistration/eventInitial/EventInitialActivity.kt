package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseBooleanArray
import android.view.View
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SingleEventEnforcer
import org.dhis2.commons.schedulers.SingleEventEnforcerImpl
import org.dhis2.databinding.ActivityEventInitialBinding
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity.Companion.getActivityBundle
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsFragment
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationActivity
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CREATE_EVENT
import org.dhis2.utils.analytics.DATA_CREATION
import org.dhis2.utils.analytics.DELETE_EVENT
import org.dhis2.utils.analytics.SHOW_HELP
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Objects
import javax.inject.Inject

class EventInitialActivity :
    ActivityGlobalAbstract(),
    EventInitialContract.View,
    EventDetailsComponentProvider {

    @Inject
    lateinit var presenter: EventInitialPresenter

    @Inject
    lateinit var resourceManager: ResourceManager

    private lateinit var binding: ActivityEventInitialBinding

    // Bundle variables
    private var programUid: String? = null
    private var eventUid: String? = null
    private var eventCreationType: EventCreationType? = null
    private var getTrackedEntityInstance: String? = null
    private var enrollmentUid: String? = null
    private var selectedOrgUnit: String? = null
    private var periodType: PeriodType? = null
    private var programStageUid: String? = null
    private var enrollmentStatus: EnrollmentStatus? = null
    private var eventScheduleInterval = 0

    private var programStage: ProgramStage? = null
    private var program: Program? = null
    private var accessData: Boolean? = null
    private var eventDetails = EventDetails()

    private var singleEventEnforcer: SingleEventEnforcer? = null

    private val disposable = CompositeDisposable()

    var eventInitialComponent: EventInitialComponent? = null

    private fun initVariables() {
        programUid = intent.getStringExtra(Constants.PROGRAM_UID)
        eventUid = intent.getStringExtra(Constants.EVENT_UID)
        eventCreationType =
            if (intent.getStringExtra(Constants.EVENT_CREATION_TYPE) != null) {
                EventCreationType.valueOf(
                    intent.getStringExtra(Constants.EVENT_CREATION_TYPE)!!,
                )
            } else {
                EventCreationType.DEFAULT
            }
        getTrackedEntityInstance = intent.getStringExtra(Constants.TRACKED_ENTITY_INSTANCE)
        enrollmentUid = intent.getStringExtra(Constants.ENROLLMENT_UID)
        selectedOrgUnit = intent.getStringExtra(Constants.ORG_UNIT)
        periodType = intent.getSerializableExtra(Constants.EVENT_PERIOD_TYPE) as PeriodType?
        programStageUid = intent.getStringExtra(Constants.PROGRAM_STAGE_UID)
        enrollmentStatus =
            intent.getSerializableExtra(Constants.ENROLLMENT_STATUS) as EnrollmentStatus?
        eventScheduleInterval = intent.getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0)
        singleEventEnforcer = SingleEventEnforcerImpl()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        initVariables()
        eventInitialComponent = Objects.requireNonNull((applicationContext as App).userComponent())
            ?.plus(
                EventInitialModule(
                    this,
                    eventUid,
                    programStageUid,
                    context,
                ),
            )
        eventInitialComponent!!.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial)
        binding.setPresenter(presenter)

        initProgressBar()

        val bundle = Bundle()
        bundle.putString(Constants.EVENT_UID, eventUid)
        bundle.putString(
            Constants.EVENT_CREATION_TYPE,
            intent.getStringExtra(Constants.EVENT_CREATION_TYPE),
        )
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStageUid)
        bundle.putString(Constants.PROGRAM_UID, programUid)
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, periodType)
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval)
        bundle.putString(Constants.ORG_UNIT, selectedOrgUnit)
        bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus)

        val eventDetailsFragment = EventDetailsFragment()
        eventDetailsFragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentDetailsContainer, eventDetailsFragment).commit()

        eventDetailsFragment.onEventDetailsChange = { eventDetails: EventDetails ->
            this.eventDetails = eventDetails
            Unit
        }
        eventDetailsFragment.onButtonCallback = {
            singleEventEnforcer!!.processEvent {
                onActionButtonClick()
                null
            }
            Unit
        }
        presenter.init(programUid, eventUid, selectedOrgUnit, programStageUid)
    }

    private fun onActionButtonClick() {
        val programStageModelUid = if (programStage == null) "" else programStage!!.uid()
        var geometry: Geometry? = null
        if (eventDetails.coordinates != null) {
            geometry = Geometry.builder()
                .coordinates(eventDetails.coordinates)
                .type(programStage!!.featureType())
                .build()
        }

        if (eventUid == null) { // This is a new Event
            presenter.onEventCreated()
            analyticsHelper().setEvent(CREATE_EVENT, DATA_CREATION, CREATE_EVENT)
            if (eventCreationType == EventCreationType.REFERAL && eventDetails.temCreate != null && eventDetails.temCreate == Constants.PERMANENT) {
                presenter.scheduleEventPermanent(
                    enrollmentUid,
                    getTrackedEntityInstance,
                    programStageModelUid,
                    eventDetails.selectedDate,
                    eventDetails.selectedOrgUnit,
                    null,
                    eventDetails.catOptionComboUid,
                    geometry,
                )
            } else if (eventCreationType == EventCreationType.SCHEDULE || eventCreationType == EventCreationType.REFERAL) {
                presenter.scheduleEvent(
                    enrollmentUid,
                    programStageModelUid,
                    eventDetails.selectedDate,
                    eventDetails.selectedOrgUnit,
                    null,
                    eventDetails.catOptionComboUid,
                    geometry,
                )
            } else {
                presenter.createEvent(
                    enrollmentUid,
                    programStageModelUid,
                    eventDetails.selectedDate,
                    eventDetails.selectedOrgUnit,
                    null,
                    eventDetails.catOptionComboUid,
                    geometry,
                    getTrackedEntityInstance,
                )
            }
        } else {
            startFormActivity(eventUid!!, false)
        }
    }

    override fun onDestroy() {
        presenter.onDettach()
        disposable.dispose()
        super.onDestroy()
    }

    private fun initProgressBar() {
        if (eventUid != null && presenter.completionPercentageVisibility) {
            binding.completion.visibility = View.VISIBLE
        } else {
            binding.completion.visibility = View.GONE
        }
    }

    override fun setProgram(program: Program) {
        this.program = program

        setUpActivityTitle()
    }

    private fun setUpActivityTitle() {
        val activityTitle = if (eventCreationType == EventCreationType.REFERAL) {
            getString(R.string.referral)
        } else {
            if (eventUid == null) {
                resourceManager.formatWithEventLabel(
                    R.string.new_event_label,
                    programStageUid,
                    1,
                    false,
                )
            } else {
                program!!.displayName()!!
            }
        }
        binding.name = activityTitle
    }

    override fun onEventCreated(eventUid: String) {
        showToast(
            resourceManager.formatWithEventLabel(
                R.string.event_label_created,
                programStageUid,
                1,
                false,
            ),
        )
        if (eventCreationType != EventCreationType.SCHEDULE && eventCreationType != EventCreationType.REFERAL) {
            startFormActivity(eventUid, true)
        } else {
            finish()
        }
    }

    override fun onEventUpdated(eventUid: String) {
        startFormActivity(eventUid, false)
    }

    private fun startFormActivity(eventUid: String, isNew: Boolean) {
        val intent = Intent(
            this,
            EventCaptureActivity::class.java,
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        intent.putExtras(
            getActivityBundle(
                eventUid,
                programUid!!,
                if (isNew) EventMode.NEW else EventMode.CHECK,
            ),
        )
        startActivity(intent)
        finish()
    }

    override fun setProgramStage(programStage: ProgramStage) {
        this.programStage = programStage
        binding.programStage = programStage

        if (periodType == null) periodType = programStage.periodType()
    }

    override fun updatePercentage(primaryValue: Float) {
        binding.completion.setCompletionPercentage(primaryValue)
    }

    override fun showProgramStageSelection() {
        presenter.getProgramStage(programStageUid)
    }

    override fun setAccessDataWrite(canWrite: Boolean) {
        this.accessData = canWrite
    }

    override fun showQR() {
        val intent = Intent(
            this@EventInitialActivity,
            QrEventsWORegistrationActivity::class.java,
        )
        intent.putExtra(Constants.EVENT_UID, eventUid)
        startActivity(intent)
    }

    override fun setTutorial() {
        Handler(Looper.getMainLooper()).postDelayed({
            val stepConditions = SparseBooleanArray()
            stepConditions.put(0, eventUid == null)
            HelpManager.getInstance()
                .show(activity, HelpManager.TutorialName.EVENT_INITIAL, stepConditions)
        }, 500)
    }

    override fun showMoreOptions(view: View) {
        AppMenuHelper.Builder().menu(this, R.menu.event_menu).anchor(view)
            .onMenuInflated { popupMenu: PopupMenu ->
                popupMenu.menu.findItem(R.id.menu_delete).setVisible(
                    accessData!! && presenter.isEnrollmentOpen,
                )
                popupMenu.menu.findItem(R.id.menu_share).setVisible(eventUid != null)
                Unit
            }
            .onMenuItemClicked { itemId: Int? ->
                when (itemId) {
                    R.id.showHelp -> {
                        analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                        setTutorial()
                    }

                    R.id.menu_delete -> confirmDeleteEvent()
                    R.id.menu_share -> presenter.onShareClick()
                    else -> {
                        // do nothing
                    }
                }
                false
            }
            .build()
            .show()
    }

    fun confirmDeleteEvent() {
        CustomDialog(
            this,
            resourceManager.formatWithEventLabel(
                R.string.delete_event_label,
                programStageUid,
                1,
                false,
            ),
            resourceManager.formatWithEventLabel(
                R.string.confirm_delete_event_label,
                programStageUid,
                1,
                false,
            ),
            getString(R.string.delete),
            getString(R.string.cancel),
            0,
            object : DialogClickListener {
                override fun onPositive() {
                    analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT)
                    presenter.deleteEvent(getTrackedEntityInstance)
                }

                override fun onNegative() {
                    // dismiss
                }
            },
        ).show()
    }

    override fun showEventWasDeleted() {
        showToast(
            resourceManager.formatWithEventLabel(
                R.string.event_label_was_deleted,
                programStageUid,
                1,
                false,
            ),
        )
        finish()
    }

    override fun showDeleteEventError() {
        showToast(
            resourceManager.formatWithEventLabel(
                R.string.delete_event_label_error,
                programStageUid,
                1,
                false,
            ),
        )
    }

    override fun provideEventDetailsComponent(module: EventDetailsModule?): EventDetailsComponent? {
        return eventInitialComponent!!.plus(module)
    }

    companion object {
        fun getBundle(
            programUid: String?,
            eventUid: String?,
            eventCreationType: String?,
            teiUid: String?,
            eventPeriodType: PeriodType?,
            orgUnit: String?,
            stageUid: String?,
            enrollmentUid: String?,
            eventScheduleInterval: Int,
            enrollmentStatus: EnrollmentStatus?,
        ): Bundle {
            val bundle = Bundle()
            bundle.putString(Constants.PROGRAM_UID, programUid)
            bundle.putString(Constants.EVENT_UID, eventUid)
            bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType)
            bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid)
            bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
            bundle.putString(Constants.ORG_UNIT, orgUnit)
            bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, eventPeriodType)
            bundle.putString(Constants.PROGRAM_STAGE_UID, stageUid)
            bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval)
            bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus)
            return bundle
        }
    }
}
