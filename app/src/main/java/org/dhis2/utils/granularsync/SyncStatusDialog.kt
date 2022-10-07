package org.dhis2.utils.granularsync

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.checkSMSPermission
import org.dhis2.Bindings.showSMS
import org.dhis2.R
import org.dhis2.commons.bindings.setStateIcon
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.databinding.SyncBottomDialogBinding
import org.dhis2.usescases.settings.ErrorDialog
import org.dhis2.usescases.sms.InputArguments
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SYNC_GRANULAR
import org.dhis2.utils.analytics.SYNC_GRANULAR_ONLINE
import org.dhis2.utils.analytics.SYNC_GRANULAR_SMS
import org.dhis2.utils.customviews.MessageAmountDialog
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict

private const val SMS_PERMISSIONS_REQ_ID = 102
private const val SMS_APP_REQ_ID = 103

class SyncStatusDialog : BottomSheetDialogFragment(), GranularSyncContracts.View {

    @Inject
    lateinit var presenter: GranularSyncContracts.Presenter

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var networkUtils: NetworkUtils

    private lateinit var recordUid: String
    private lateinit var conflictType: ConflictType
    private var orgUnitDataValue: String? = null
    private var attributeComboDataValue: String? = null
    private var periodIdDataValue: String? = null
    var dismissListenerDialog: GranularSyncContracts.OnDismissListener? = null

    private var binding: SyncBottomDialogBinding? = null
    private var adapter: SyncConflictAdapter? = null
    private var syncing: Boolean = false

    private val config: SyncStatusDialogUiConfig by lazy {
        SyncStatusDialogUiConfig(resources, presenter, getInputArguments())
    }

    private val smsAppLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        adapter!!.addItem(
            StatusLogItem.create(
                Calendar.getInstance().time,
                getString(R.string.sms_sync_manual_confirmation)
            )
        )
        val smsConfirmationDialog = BottomSheetDialog(
            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = getString(R.string.sms_enabled),
                subtitle = getString(R.string.sms_sync_is_sms_sent),
                iconResource = R.drawable.ic_help,
                mainButton = DialogButtonStyle.NeutralButton(R.string.no),
                secondaryButton = DialogButtonStyle.NeutralButton(R.string.yes)
            ),
            onMainButtonClicked = {
                presenter.onSmsNotManuallySent(requireContext())
            },
            onSecondaryButtonClicked = {
                presenter.onSmsManuallySent(requireContext()) {
                    it.observe(this) { messageReceived ->
                        presenter.onConfirmationMessageStateChanged(messageReceived)
                    }
                }
            }
        ).also {
            it.isCancelable = false
        }
        smsConfirmationDialog
            .show(childFragmentManager, BottomSheetDialogUiModel::class.java.simpleName)
    }

    enum class ConflictType {
        ALL, PROGRAM, TEI, EVENT, DATA_SET, DATA_VALUES
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
                requireContext(),
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
    ): View {
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

    override fun showRefreshTitle() {
        binding!!.programName.text = getString(R.string.granular_sync_refresh_title)
    }

    override fun setState(
        state: State,
        conflicts: MutableList<TrackerImportConflict>
    ) {
        updateState(state)
        when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            State.UPLOADING -> setNoConflictMessage(getString(R.string.no_conflicts_update_message))
            State.SYNCED -> {
                setNoConflictMessage(getString(R.string.no_conflicts_synced_message))
                binding!!.connectionMessage.visibility = View.GONE
            }
            State.WARNING, State.ERROR ->
                if (conflictType == ConflictType.PROGRAM || conflictType == ConflictType.DATA_SET) {
                    setProgramConflictMessage(state)
                } else if (conflictType == ConflictType.DATA_VALUES) {
                    setDataSetInstanceMessage()
                } else if (conflicts.isNotEmpty()) {
                    prepareConflictAdapter(conflicts)
                } else {
                    setNoConflictMessage(getString(R.string.server_sync_error))
                }
            State.SYNCED_VIA_SMS, State.SENT_VIA_SMS ->
                setNoConflictMessage(getString(R.string.sms_synced_message))
            else -> { /*states not in use*/
            }
        }
    }

    override fun updateState(state: State) {
        binding!!.syncIcon.setStateIcon(state, true)
        binding!!.syncStatusBar.setBackgroundResource(getColorForState(state))
    }

    override fun closeDialog() {
        dismiss()
    }

    override fun openSmsApp(message: String, smsToNumber: String) {
        val chooser = createSMSIntent(message, smsToNumber)
        smsAppLauncher.launch(chooser)
    }

    private fun createSMSIntent(message: String, smsToNumber: String): Intent? {
        val uri = Uri.parse("smsto:$smsToNumber")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            putExtra("sms_body", message)
        }
        return Intent.createChooser(intent, getString(R.string.sms_sync_sms_app_chooser_title))
    }

    private fun setNetworkMessage() {
        if (!networkUtils.isOnline()) {
            if (presenter.isSMSEnabled(context?.showSMS() == true)) {
                if (presenter.canSendSMS()) {
                    analyticsHelper.setEvent(SYNC_GRANULAR_SMS, CLICK, SYNC_GRANULAR)
                    binding!!.connectionMessage.setText(R.string.network_unavailable_sms)
                    binding!!.syncButton.setText(R.string.action_sync_sms)
                    binding!!.syncButton.visibility = View.VISIBLE
                    binding!!.syncButton.setOnClickListener {
                        binding!!.noConflictMessage.visibility = View.GONE
                        binding!!.synsStatusRecycler.visibility = View.VISIBLE
                        syncSms()
                    }
                } else {
                    binding!!.connectionMessage.setText(R.string.sms_available_for_individual_records)
                    binding!!.syncButton.visibility = View.GONE
                    binding!!.syncButton.setOnClickListener(null)
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
            if (binding!!.syncIcon.tag == org.dhis2.commons.R.drawable.ic_status_synced) {
                binding!!.syncButton.text = getString(R.string.granular_sync_refresh)
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
                ImageSpan(requireContext(), R.drawable.ic_sync_warning),
                wIndex,
                wIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (eIndex > -1) {
            str.setSpan(
                ImageSpan(requireContext(), R.drawable.ic_sync_problem_red),
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
        setNetworkMessage()
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
            val dialog = dialog as com.google.android.material.bottomsheet.BottomSheetDialog?

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

        states.forEach { status ->
            adapter!!.addItem(config.initialStatusLogItem(status))
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
                presenter.onSmsNotAccepted()
            }
        }
        dialog.arguments = args
        dialog.show(requireActivity().supportFragmentManager, null)
    }

    private fun syncGranular() {
        syncing = true
        presenter.initGranularSync().observe(
            this
        ) { workInfo ->
            if (workInfo != null && workInfo.isNotEmpty()) {
                manageWorkInfo(workInfo[0])
            }
        }
    }

    private fun manageWorkInfo(workInfo: WorkInfo) {
        binding!!.synsStatusRecycler.visibility = View.VISIBLE
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
                binding!!.syncIcon.setImageResource(R.drawable.animator_sync_grey)
                if (binding!!.syncIcon.drawable is AnimatedVectorDrawable) {
                    (binding!!.syncIcon.drawable as AnimatedVectorDrawable).start()
                }
                logMessage(getString(R.string.start_sync_granular))
            }
            WorkInfo.State.RUNNING ->
                logMessage(getString(R.string.syncing))
            WorkInfo.State.SUCCEEDED -> {
                binding!!.syncButton.visibility = View.GONE
                logMessage(getString(R.string.end_sync_granular))
                binding!!.noConflictMessage.text = getString(R.string.no_conflicts_synced_message)
                updateState(State.SYNCED)
                setLastUpdated(SyncDate(Date()))
                dismissListenerDialog!!.onDismiss(true)
            }
            WorkInfo.State.FAILED -> {
                if (workInfo.outputData.keyValueMap["incomplete"] != null) {
                    logMessage(getString(R.string.sync_incomplete_error_text))
                }
                if (workInfo.outputData.keyValueMap["conflict"] != null) {
                    val listStatusLog = ArrayList<StatusLogItem>()
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
                    logMessage(getString(R.string.error_sync_check_logs), true)
                }
                updateState(State.ERROR)
                dismissListenerDialog!!.onDismiss(false)
            }
            WorkInfo.State.CANCELLED ->
                logMessage(getString(R.string.cancel_sync), true)
            else -> {
            }
        }
    }

    override fun setLastUpdated(result: SyncDate) {
        binding?.lastUpdated?.text = result.date?.let {
            it.toDateSpan(requireContext())
        } ?: getString(R.string.unknown_date)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == SMS_PERMISSIONS_REQ_ID &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            syncSms()
        }
    }

    private fun syncSms() {
        syncing = true
        presenter.onSmsSyncClick {
            it.observe(this) { state -> this.stateChanged(state) }
        }
    }

    override fun logWaitingForServerResponse() {
        logMessage(getString(R.string.sms_sync_waiting_for_response))
    }

    override fun logSmsReachedServer() {
        logMessage(getString(R.string.sms_sync_sms_reached_server))
    }

    override fun logSmsReachedServerError() {
        logMessage(getString(R.string.sms_sync_sms_reached_server_error))
    }

    override fun logSmsSent() {
        logMessage(getString(R.string.sms_sync_sent))
    }

    override fun logSmsNotSent() {
        logMessage(getString(R.string.sms_sync_not_sent))
    }

    override fun logOpeningSmsApp() {
        logMessage(getString(R.string.sms_sync_opening_app))
    }

    private fun logMessage(msg: String, openLogs: Boolean = false) {
        adapter!!.addItem(StatusLogItem.create(Calendar.getInstance().time, msg, openLogs))
    }

    override fun checkSmsPermission(): Boolean {
        return checkSMSPermission(true, SMS_PERMISSIONS_REQ_ID)
    }
}
