package org.dhis2.utils.granularsync

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.checkSMSPermission
import org.dhis2.Bindings.showSMS
import org.dhis2.R
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.ui.icons.SyncStateIcon
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUi
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.ui.items.SyncStatusItem
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.customviews.MessageAmountDialog

private const val SMS_PERMISSIONS_REQ_ID = 102

class SyncStatusDialog : BottomSheetDialogFragment(), GranularSyncContracts.View {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var networkUtils: NetworkUtils

    var dismissListenerDialog: OnDismissListener? = null

    private var syncing: Boolean = false

    private var smsSenderHelper: SMSSenderHelper? = null

    @Inject
    lateinit var viewModelFactory: GranularSyncViewModelFactory

    private val viewModel: GranularSyncPresenter by viewModels { viewModelFactory }

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).serverComponent()!!.plus(
            GranularSyncModule(
                requireContext(),
                this,
                arguments?.getSerializable(CONFLICT_TYPE) as ConflictType,
                arguments?.getString(RECORD_UID) ?: "",
                arguments?.getString(ORG_UNIT_DATA_VALUE),
                arguments?.getString(ATTRIBUTE_COMBO_DATA_VALUE),
                arguments?.getString(PERIOD_ID_DATA_VALUE)
            )
        ).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, org.dhis2.ui.R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    val syncState by viewModel.currentState.collectAsState()
                    syncState?.let { syncUiState ->
                        BottomSheetDialogUi(
                            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                                title = syncUiState.title,
                                subtitle = syncUiState.lastSyncDate?.date?.toDateSpan(requireContext()),
                                message = syncUiState.message,
                                iconResource = R.drawable.ic_sync_warning,
                                mainButton = syncUiState.mainActionLabel?.let {
                                    DialogButtonStyle.MainButtonLabel(it)
                                },
                                secondaryButton = syncUiState.secondaryActionLabel?.let {
                                    DialogButtonStyle.SecondaryButtonLabel(it)
                                }
                            ),
                            onMainButtonClicked = {
                                onSyncClick()
                            },
                            onSecondaryButtonClicked = {
                                dismiss()
                            },
                            icon = {
                                SyncStateIcon(state = syncUiState.syncState)
                            },
                            extraContent = if (syncUiState.content.isNotEmpty()) {
                                {
                                    LazyColumn(
                                        verticalArrangement = spacedBy(8.dp)
                                    ) {
                                        items(syncUiState.content) { item ->
                                            SyncStatusItem(
                                                title = item.displayName,
                                                subtitle = item.description,
                                                onClick = {
                                                    // TODO() onSyncItemClick(item)
                                                }
                                            ) {
                                                SyncStateIcon(state = item.state)
                                            }
                                        }
                                    }
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    private fun onSyncClick() {
        when {
            networkUtils.isOnline() -> syncGranular()
            viewModel.canSendSMS() &&
                viewModel.isSMSEnabled(context?.showSMS() == true) -> syncSms()
        }
    }

    private fun syncGranular() {
        syncing = true
        viewModel.initGranularSync().observe(
            this
        ) { workInfo ->
            viewModel.manageWorkInfo(workInfo[0])
        }
    }

    private fun syncSms() {
        syncing = true
        viewModel.onSmsSyncClick {
            it.observe(this) { state -> this.stateChanged(state) }
        }
    }

    override fun openSmsApp(message: String, smsToNumber: String) {
        smsSenderHelper = SMSSenderHelper(
            requireContext(),
            requireActivity().activityResultRegistry,
            childFragmentManager,
            smsToNumber,
            message,
            onStatusChanged = { status ->
                when (status) {
                    SMSSenderHelper.Status.ALL_SMS_SENT -> allSmsSent()
                    SMSSenderHelper.Status.SMS_NOT_MANUALLY_SENT -> smsNotManuallySent()
                    SMSSenderHelper.Status.RETURNED_TO_APP -> {}
                }
            }
        ).also {
            if (it.smsCount() > 1) {
                askForMessagesAmount(
                    it.smsCount(),
                    { it.pollSms() },
                    { /*logSmsNotSent()*/ }
                )
            } else {
                it.pollSms()
            }
        }
    }

    private fun allSmsSent() {
        viewModel.onSmsManuallySent(requireContext()) {
            it.observe(this) { messageReceived ->
                viewModel.onConfirmationMessageStateChanged(messageReceived)
            }
        }
    }

    private fun smsNotManuallySent() {
        viewModel.onSmsNotManuallySent(requireContext())
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
        viewModel.onDettach()
        dismissListenerDialog?.onDismiss(syncing)
        super.onDismiss(dialog)
    }

    private fun stateChanged(states: List<SmsSendingService.SendingStatus>?) {
        if (states.isNullOrEmpty()) return

        val lastState = states[states.size - 1]
        when (lastState.state!!) {
            SmsSendingService.State.WAITING_COUNT_CONFIRMATION -> {
                askForMessagesAmount(
                    amount = lastState.total,
                    onAccept = { viewModel.sendSMS() },
                    onDecline = { viewModel.onSmsNotAccepted() }
                )
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
                viewModel.restartSmsSender()
                if (lastState.state == SmsSendingService.State.COMPLETED) {
                }
            }
        }
    }

    private fun askForMessagesAmount(
        amount: Int,
        onAccept: () -> Unit,
        onDecline: () -> Unit
    ) {
        val args = Bundle()
        args.putInt(MessageAmountDialog.ARG_AMOUNT, amount)
        val dialog = MessageAmountDialog { accepted ->
            if (accepted) {
                onAccept()
            } else {
                onDecline()
            }
        }
        dialog.arguments = args
        dialog.show(requireActivity().supportFragmentManager, null)
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

    override fun checkSmsPermission(): Boolean {
        return checkSMSPermission(true, SMS_PERMISSIONS_REQ_ID)
    }

    class Builder {
        private lateinit var recordUid: String
        private lateinit var conflictType: ConflictType
        private var orgUnitDataValue: String? = null
        private var attributeComboDataValue: String? = null
        private var periodIdDataValue: String? = null
        private var dismissListener: OnDismissListener? = null

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

        fun onDismissListener(dismissListener: OnDismissListener): Builder {
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
}
