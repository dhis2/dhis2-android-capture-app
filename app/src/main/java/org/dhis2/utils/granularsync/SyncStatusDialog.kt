package org.dhis2.utils.granularsync

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.App
import org.dhis2.bindings.checkSMSPermission
import org.dhis2.bindings.showSMS
import org.dhis2.R

import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.OnSyncNavigationListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.ui.icons.SyncStateIcon
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUi
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.ui.items.SyncStatusItem
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.customviews.MessageAmountDialog
import org.hisp.dhis.android.core.common.State
import javax.inject.Inject

private const val SMS_PERMISSIONS_REQ_ID = 102
private const val SYNC_CONTEXT = "SYNC_CONTEXT"

class SyncStatusDialog : BottomSheetDialogFragment(), GranularSyncContracts.View {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var networkUtils: NetworkUtils

    var dismissListenerDialog: OnDismissListener? = null

    var syncStatusDialogNavigator: SyncStatusDialogNavigator? = null

    private var syncing: Boolean = false

    private var smsSenderHelper: SMSSenderHelper? = null

    @Inject
    lateinit var viewModelFactory: GranularSyncViewModelFactory

    private val viewModel: GranularSyncPresenter by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).serverComponent()!!.plus(
            GranularSyncModule(
                requireContext(),
                this,
                arguments?.getParcelable(SYNC_CONTEXT)
                    ?: throw NullPointerException("Missing sync context"),
            ),
        ).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, org.dhis2.ui.R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    val syncState by viewModel.currentState.collectAsState()
                    syncState?.let { syncUiState ->
                        when {
                            syncUiState.shouldDismissOnUpdate -> dismiss()
                            syncing && syncUiState.syncState == State.SYNCED -> {
                                dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.sync_successful),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                        BottomSheetDialogUi(
                            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                                title = syncUiState.title,
                                subtitle = syncUiState.lastSyncDate?.date?.toDateSpan(
                                    requireContext(),
                                ),
                                message = syncUiState.message,
                                iconResource = R.drawable.ic_sync_warning,
                                mainButton = syncUiState.mainActionLabel?.let {
                                    DialogButtonStyle.MainButtonLabel(it)
                                },
                                secondaryButton = syncUiState.secondaryActionLabel?.let {
                                    DialogButtonStyle.SecondaryButtonLabel(it)
                                },
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
                                        verticalArrangement = spacedBy(8.dp),
                                    ) {
                                        items(syncUiState.content) { item ->
                                            SyncStatusItem(
                                                title = item.displayName,
                                                subtitle = item.description,
                                                onClick = {
                                                    syncStatusDialogNavigator?.navigateTo(item) {
                                                        dismiss()
                                                    }
                                                },
                                            ) {
                                                SyncStateIcon(state = item.state)
                                            }
                                        }
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshContent()
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
            this,
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
                    SMSSenderHelper.Status.RETURNED_TO_APP -> { // Do nothing
                    }
                }
            },
        ).also {
            if (it.smsCount() > 1) {
                askForMessagesAmount(
                    amount = it.smsCount(),
                    onAccept = { it.pollSms() },
                    onDecline = {
                        /*Do nothing*/
                    },
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
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet,
                )
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    /*NoUse*/
                }
            })
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
                    onDecline = { viewModel.onSmsNotAccepted() },
                )
                syncing = true
            }
            SmsSendingService.State.STARTED,
            SmsSendingService.State.CONVERTED,
            SmsSendingService.State.SENDING,
            SmsSendingService.State.WAITING_RESULT,
            SmsSendingService.State.RESULT_CONFIRMED,
            SmsSendingService.State.SENT,
            ->
                syncing = true
            SmsSendingService.State.ITEM_NOT_READY,
            SmsSendingService.State.COUNT_NOT_ACCEPTED,
            SmsSendingService.State.WAITING_RESULT_TIMEOUT,
            SmsSendingService.State.ERROR,
            SmsSendingService.State.COMPLETED,
            ->
                viewModel.restartSmsSender()
        }
    }

    private fun askForMessagesAmount(amount: Int, onAccept: () -> Unit, onDecline: () -> Unit) {
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
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
        private var context: Context? = null
        private var fm: FragmentManager? = null
        private var navigator: SyncStatusDialogNavigator? = null
        private lateinit var syncContext: SyncContext
        private var dismissListener: OnDismissListener? = null

        fun withContext(
            context: FragmentActivity,
            onSyncNavigationListener: OnSyncNavigationListener? = null,
        ): Builder {
            this.context = context
            this.navigator = SyncStatusDialogNavigator(
                context,
                onSyncNavigationListener = onSyncNavigationListener,
            )
            this.fm = context.supportFragmentManager
            return this
        }

        fun withContext(
            fragment: Fragment,
            onSyncNavigationListener: OnSyncNavigationListener? = null,
        ): Builder {
            this.context = fragment.context
            this.navigator = SyncStatusDialogNavigator(
                fragment.requireActivity(),
                onSyncNavigationListener = onSyncNavigationListener,
            )
            this.fm = fragment.childFragmentManager
            return this
        }

        fun withSyncContext(syncContext: SyncContext): Builder {
            this.syncContext = syncContext
            return this
        }

        fun onDismissListener(dismissListener: OnDismissListener): Builder {
            this.dismissListener = dismissListener
            return this
        }

        private fun build(): SyncStatusDialog {
            return SyncStatusDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(SYNC_CONTEXT, syncContext)
                }
                dismissListenerDialog = dismissListener
                syncStatusDialogNavigator = navigator
            }
        }

        fun show(tag: String) {
            if (fm != null) {
                build().show(fm!!, tag)
            } else {
                throw NullPointerException(
                    "Required non null fragment manager. Use withContext builder method.",
                )
            }
        }
    }
}
