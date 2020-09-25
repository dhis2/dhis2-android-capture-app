package org.dhis2.utils.granularsync

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.Bindings
import org.dhis2.Bindings.checkSMSPermission
import org.dhis2.Bindings.showSMS
import org.dhis2.R
import org.dhis2.databinding.SyncBottomDialogBinding
import org.dhis2.usescases.settings.ErrorDialog
import org.dhis2.usescases.sms.InputArguments
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.usescases.sms.StatusText
import org.dhis2.utils.DateUtils
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SYNC_GRANULAR
import org.dhis2.utils.analytics.SYNC_GRANULAR_ONLINE
import org.dhis2.utils.analytics.SYNC_GRANULAR_SMS
import org.dhis2.utils.customviews.MessageAmountDialog
import org.dhis2.utils.idlingresource.CountingIdlingResourceSingleton
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict

private const val SMS_PERMISSIONS_REQ_ID = 102

class SyncStatusDialog : BottomSheetDialogFragment(), GranularSyncContracts.View {

    @Inject
    lateinit var presenter: GranularSyncContracts.Presenter

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var recordUid: String
    private lateinit var conflictType: ConflictType
    private var orgUnitDataValue: String? = null
    private var attributeComboDataValue: String? = null
    private var periodIdDataValue: String? = null
    var dismissListenerDialog: GranularSyncContracts.OnDismissListener? = null

    private var binding: SyncBottomDialogBinding? = null
    private var adapter: SyncConflictAdapter? = null
    private var syncing: Boolean = false

    enum class ConflictType {
        PROGRAM, TEI, EVENT, DATA_SET, DATA_VALUES
    }

    companion object {
        private const val RECORD_UID = "RECORD_UID"
        private const val CONFLICT_TYPE = "CONFLICT_TYPE"
        private const val ORG_UNIT_DATA_VALUE = "ORG_UNIT_DATA_VALUE"
        private const val PERIOD_ID_DATA_VALUE = "PERIOD_ID_DATA_VALUE"
        private const val ATTRIBUTE_COMBO_DATA_VALUE = "ATTRIBUTE_COMBO_DATA_VALUE"

        @JvmStatic
        fun newInstance(
            recordUid: String,
            conflictType: ConflictType,
            orgUnitDataValue: String? = null,
            attributeComboDataValue: String? = null,
            periodIdDataValue: String? = null
        ) = SyncStatusDialog().apply {
            Bundle().apply {
                putString(RECORD_UID, recordUid)
                putSerializable(CONFLICT_TYPE, conflictType)
                putString(ORG_UNIT_DATA_VALUE, orgUnitDataValue)
                putString(PERIOD_ID_DATA_VALUE, periodIdDataValue)
                putString(ATTRIBUTE_COMBO_DATA_VALUE, attributeComboDataValue)
            }.also { arguments = it }
        }
    }

    class Builder {
        private lateinit var recordUid: String
        private lateinit var conflictType: ConflictType
        private var orgUnitDataValue: String? = null
        private var attributeComboDataValue: String? = null
        private var periodIdDataValue: String? = null
        private var dismissListener: GranularSyncContracts.OnDismissListener? = null

        fun setUid(uid: String): Builder {
            this.recordUid = uid
            return this
        }

        fun setConflictType(conflictType: ConflictType): Builder {
            this.conflictType = conflictType
            return this
        }

        fun setOrgUnit(orgUnit: String): Builder {
            this.orgUnitDataValue = orgUnit
            return this
        }

        fun setAttributeOptionCombo(attributeOptionCombo: String): Builder {
            this.attributeComboDataValue = attributeOptionCombo
            return this
        }

        fun setPeriodId(periodId: String): Builder {
            this.periodIdDataValue = periodId
            return this
        }

        fun onDismissListener(dismissListener: GranularSyncContracts.OnDismissListener): Builder {
            this.dismissListener = dismissListener
            return this
        }

        fun build(): SyncStatusDialog {
            if (conflictType == ConflictType.DATA_VALUES &&
                (
                    orgUnitDataValue == null ||
                        attributeComboDataValue == null ||
                        periodIdDataValue == null
                    )
            ) {
                throw NullPointerException(
                    "DataSets require non null, orgUnit, attributeOptionCombo and periodId"
                )
            }

            return newInstance(
                recordUid,
                conflictType,
                orgUnitDataValue,
                attributeComboDataValue,
                periodIdDataValue
            ).apply { dismissListenerDialog = dismissListener }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.recordUid = arguments?.getString(RECORD_UID) ?: ""
        this.conflictType = arguments?.getSerializable(CONFLICT_TYPE) as ConflictType
        this.orgUnitDataValue = arguments?.getString(ORG_UNIT_DATA_VALUE)
        this.attributeComboDataValue = arguments?.getString(ATTRIBUTE_COMBO_DATA_VALUE)
        this.periodIdDataValue = arguments?.getString(PERIOD_ID_DATA_VALUE)

        (context.applicationContext as App).serverComponent()!!.plus(
            GranularSyncModule(
                conflictType,
                recordUid,
                orgUnitDataValue,
                attributeComboDataValue,
                periodIdDataValue
            )
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.sync_bottom_dialog, container, false)
        adapter = SyncConflictAdapter(ArrayList()) { showErrorLog() }
        val layoutManager = LinearLayoutManager(context)
        binding!!.synsStatusRecycler.layoutManager = layoutManager
        binding!!.synsStatusRecycler.adapter = adapter

        presenter.configure(this)

        retainInstance = true

        return binding!!.root
    }

    private fun showErrorLog() {
        ErrorDialog()
            .setData(presenter.syncErrors())
            .show(childFragmentManager.beginTransaction(), ErrorDialog.TAG)
    }

    override fun showTitle(displayName: String) {
        binding!!.programName.text = displayName
    }

    override fun setState(state: State) {
        Bindings.setStateIcon(binding!!.syncIcon, state, true)
        binding!!.syncStatusName.setText(getTextByState(state))
        binding!!.syncStatusBar.setBackgroundResource(getColorForState(state))
        when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            State.UPLOADING -> setNoConflictMessage(getString(R.string.no_conflicts_update_message))
            State.SYNCED -> {
                setNoConflictMessage(getString(R.string.no_conflicts_synced_message))
                binding!!.syncButton.visibility = View.GONE
                binding!!.connectionMessage.visibility = View.GONE
            }
            State.WARNING, State.ERROR ->
                if (conflictType == ConflictType.PROGRAM || conflictType == ConflictType.DATA_SET) {
                    setProgramConflictMessage(state)
                } else if (conflictType == ConflictType.DATA_VALUES) {
                    setDataSetInstanceMessage()
                }
            State.SYNCED_VIA_SMS, State.SENT_VIA_SMS ->
                setNoConflictMessage(getString(R.string.sms_synced_message))
            else -> { /*states not in use*/
            }
        }
    }

    override fun closeDialog() {
        dismiss()
    }

    private fun setNetworkMessage() {
        if (!NetworkUtils.isOnline(context)) {
            if (presenter.isSMSEnabled(conflictType == ConflictType.TEI) &&
                context?.showSMS() == true
            ) {
                if (conflictType != ConflictType.PROGRAM &&
                    conflictType != ConflictType.DATA_SET
                ) {
                    analyticsHelper.setEvent(SYNC_GRANULAR_SMS, CLICK, SYNC_GRANULAR)
                    binding!!.connectionMessage.setText(R.string.network_unavailable_sms)
                    binding!!.syncButton.setText(R.string.action_sync_sms)
                    binding!!.syncButton.visibility = View.VISIBLE
                    binding!!.syncButton.setOnClickListener {
                        if (checkSMSPermission(true, SMS_PERMISSIONS_REQ_ID)) {
                            syncSMS()
                        }
                    }
                } else {
                    binding!!.syncButton.visibility = View.GONE
                }
            } else {
                analyticsHelper.setEvent(SYNC_GRANULAR_ONLINE, CLICK, SYNC_GRANULAR)
                binding!!.connectionMessage.setText(R.string.network_unavailable)
                binding!!.syncButton.visibility = View.GONE
                binding!!.syncButton.setOnClickListener(null)
            }
        } else {
            binding!!.connectionMessage.text = null
            binding!!.syncButton.setText(R.string.action_send)
            if (binding!!.syncStatusName.text == getString(R.string.state_synced)) {
                binding!!.syncButton.visibility = View.GONE
            }

            binding!!.syncButton.setOnClickListener { syncGranular() }
        }
    }

    override fun prepareConflictAdapter(conflicts: MutableList<TrackerImportConflict>) {
        binding!!.synsStatusRecycler.visibility = View.VISIBLE
        binding!!.noConflictMessage.visibility = View.GONE

        val listStatusLog = ArrayList<StatusLogItem>()

        for (tracker in conflicts) {
            listStatusLog.add(
                StatusLogItem.create(
                    tracker.created()!!,
                    tracker.displayDescription() ?: tracker.conflict() ?: ""
                )
            )
        }

        adapter!!.addItems(listStatusLog)
        setNetworkMessage()
    }

    private fun setNoConflictMessage(message: String) {
        binding!!.synsStatusRecycler.visibility = View.GONE
        binding!!.noConflictMessage.text = message
        binding!!.noConflictMessage.visibility = View.VISIBLE
        setNetworkMessage()
    }

    private fun setProgramConflictMessage(state: State) {
        binding!!.synsStatusRecycler.visibility = View.GONE
        binding!!.noConflictMessage.visibility = View.VISIBLE

        val src = getString(
            if (state == State.WARNING) {
                R.string.data_sync_warning_program
            } else {
                R.string.data_sync_error_program
            }
        )
        val str = SpannableString(src)
        val wIndex = src.indexOf('@')
        val eIndex = src.indexOf('$')
        if (wIndex > -1) {
            str.setSpan(
                ImageSpan(context!!, R.drawable.ic_sync_warning),
                wIndex,
                wIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (eIndex > -1) {
            str.setSpan(
                ImageSpan(context!!, R.drawable.ic_sync_problem_red),
                eIndex,
                eIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding!!.noConflictMessage.text = str
        setNetworkMessage()
    }

    private fun setDataSetInstanceMessage() {
        binding!!.synsStatusRecycler.visibility = View.GONE
        binding!!.noConflictMessage.visibility = View.VISIBLE

        binding!!.noConflictMessage.text = getString(R.string.data_values_error_sync_message)
    }

    private fun getTextByState(state: State): Int {
        return when (state) {
            State.SYNCED_VIA_SMS, State.SENT_VIA_SMS -> R.string.sync_by_sms
            State.WARNING -> R.string.state_warning
            State.ERROR -> R.string.state_error
            State.TO_UPDATE, State.UPLOADING -> R.string.state_to_update
            State.TO_POST -> R.string.state_to_post
            else -> R.string.state_synced
        }
    }

    private fun getColorForState(state: State): Int {
        return when (state) {
            State.SYNCED_VIA_SMS, State.SENT_VIA_SMS -> R.color.state_by_sms
            State.WARNING -> R.color.state_warning
            State.ERROR -> R.color.state_error
            State.TO_UPDATE, State.TO_POST, State.UPLOADING -> R.color.state_to_post
            else -> R.color.state_synced
        }
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?

            val bottomSheet =
                dialog!!
                    .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setPeekHeight(0)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        presenter.onDettach()
        dismissListenerDialog?.onDismiss(syncing)
        super.onDismiss(dialog)
    }

    private fun syncSMS() {
        binding!!.noConflictMessage.visibility = View.GONE
        binding!!.synsStatusRecycler.visibility = View.VISIBLE

        if (checkPermissions()) {
            presenter.initSMSSync().observe(this, Observer { this.stateChanged(it) })
        } else {
            closeDialog()
        }
    }

    private fun checkPermissions(): Boolean {
        // check permissions
        val smsPermissions = arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (!hasPermissions(smsPermissions)) {
            ActivityCompat.requestPermissions(activity!!, smsPermissions, SMS_PERMISSIONS_REQ_ID)
            return false
        }
        return true
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context!!, permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun getInputArguments(): InputArguments {
        val bundle = Bundle()
        when (conflictType) {
            ConflictType.TEI -> InputArguments.setEnrollmentData(bundle, recordUid)
            ConflictType.EVENT -> InputArguments.setSimpleEventData(bundle, recordUid)
            ConflictType.DATA_VALUES -> InputArguments.setDataSet(
                bundle,
                recordUid,
                orgUnitDataValue,
                periodIdDataValue,
                attributeComboDataValue
            )
            else -> {
            }
        }
        return InputArguments(bundle)
    }

    private fun stateChanged(states: List<SmsSendingService.SendingStatus>?) {
        if (states.isNullOrEmpty()) return

        states.forEach {
            adapter!!.addItem(
                StatusLogItem.create(
                    Date(),
                    StatusText.getTextSubmissionType(resources, getInputArguments()) + ": " +
                        StatusText.getTextForStatus(resources, it)
                )
            )
        }

        val lastState = states[states.size - 1]
        when (lastState.state!!) {
            SmsSendingService.State.WAITING_COUNT_CONFIRMATION -> {
                askForMessagesAmount(lastState.total)
                syncing = true
            }
            SmsSendingService.State.STARTED,
            SmsSendingService.State.CONVERTED,
            SmsSendingService.State.SENDING,
            SmsSendingService.State.WAITING_RESULT,
            SmsSendingService.State.RESULT_CONFIRMED,
            SmsSendingService.State.SENT ->
                syncing = true
            SmsSendingService.State.ITEM_NOT_READY,
            SmsSendingService.State.COUNT_NOT_ACCEPTED,
            SmsSendingService.State.WAITING_RESULT_TIMEOUT,
            SmsSendingService.State.ERROR,
            SmsSendingService.State.COMPLETED -> {
            }
        }
    }

    private fun askForMessagesAmount(amount: Int) {
        val args = Bundle()
        args.putInt(MessageAmountDialog.ARG_AMOUNT, amount)
        val dialog = MessageAmountDialog { accepted ->
            if (accepted) {
                presenter.sendSMS()
            } else {
                presenter.reportState(SmsSendingService.State.COUNT_NOT_ACCEPTED, 0, 0)
            }
        }
        dialog.arguments = args
        dialog.show(activity!!.supportFragmentManager, null)
    }

    private fun syncGranular() {
        syncing = true
        presenter.initGranularSync().observe(
            this,
            Observer { workInfo ->
                if (workInfo != null && workInfo.size > 0) {
                    manageWorkInfo(workInfo[0])
                }
            }
        )
    }

    private fun manageWorkInfo(workInfo: WorkInfo) {
        binding!!.synsStatusRecycler.visibility = View.VISIBLE
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
                CountingIdlingResourceSingleton.countingIdlingResource.increment()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding!!.syncIcon.setImageResource(R.drawable.animator_sync_grey)
                    if (binding!!.syncIcon.drawable is AnimatedVectorDrawable) {
                        (binding!!.syncIcon.drawable as AnimatedVectorDrawable).start()
                    }
                }
                adapter!!.addItem(
                    StatusLogItem.create(
                        Calendar.getInstance().time,
                        getString(R.string.start_sync_granular)
                    )
                )
            }
            WorkInfo.State.RUNNING ->
                adapter!!.addItem(
                    StatusLogItem.create(
                        Calendar.getInstance().time,
                        getString(R.string.syncing)
                    )
                )
            WorkInfo.State.SUCCEEDED -> {
                binding!!.syncButton.visibility = View.GONE
                adapter!!.addItem(
                    StatusLogItem.create(
                        Calendar.getInstance().time,
                        getString(R.string.end_sync_granular)
                    )
                )
                binding!!.noConflictMessage.text = getString(R.string.no_conflicts_synced_message)
                Bindings.setStateIcon(binding!!.syncIcon, State.SYNCED, true)
                dismissListenerDialog!!.onDismiss(true)
                CountingIdlingResourceSingleton.countingIdlingResource.decrement()
            }
            WorkInfo.State.FAILED -> {
                val listStatusLog = ArrayList<StatusLogItem>()
                if (workInfo.outputData.keyValueMap["conflict"] != null) {
                    for (tracker in workInfo.outputData.getStringArray("conflict")!!) {
                        try {
                            listStatusLog.add(
                                StatusLogItem.create(
                                    DateUtils.databaseDateFormat().parse(
                                        tracker
                                            .split("/".toRegex())
                                            .dropLastWhile { it.isEmpty() }
                                            .toTypedArray()[0]
                                    ),
                                    tracker
                                        .split("/".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()[1]
                                )
                            )
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    }

                    adapter!!.addAllItems(listStatusLog)
                } else {
                    adapter!!.addItem(
                        StatusLogItem.create(
                            Calendar.getInstance().time,
                            getString(R.string.error_sync_check_logs),
                            true
                        )
                    )
                }
                Bindings.setStateIcon(binding!!.syncIcon, State.ERROR, true)
                dismissListenerDialog!!.onDismiss(false)
                CountingIdlingResourceSingleton.countingIdlingResource.decrement()
            }
            WorkInfo.State.CANCELLED ->
                adapter!!.addItem(
                    StatusLogItem.create(
                        Calendar.getInstance().time,
                        getString(R.string.cancel_sync),
                        true
                    )
                )
            else -> {
            }
        }
    }

    override fun emptyEnrollmentError(): String {
        return getString(R.string.granular_sync_enrollments_empty)
    }

    override fun unsupportedTask(): String {
        return getString(R.string.granular_sync_unsupported_task)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == SMS_PERMISSIONS_REQ_ID &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            syncSMS()
        }
    }
}
