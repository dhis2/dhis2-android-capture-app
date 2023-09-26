package org.dhis2.utils.granularsync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import org.dhis2.R
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class SMSSenderHelper(
    private val context: Context,
    registry: ActivityResultRegistry,
    private val fragmentManager: FragmentManager,
    private val smsNumberTo: String,
    smsMessage: String,
    private var onStatusChanged: ((Status) -> Unit)? = null,
) {
    enum class Status {
        ALL_SMS_SENT,
        SMS_NOT_MANUALLY_SENT,
        RETURNED_TO_APP,
    }

    private val mWho = UUID.randomUUID().toString()
    private val mNextLocalRequestCode = AtomicInteger()

    private var smsQueue: Queue<String> = LinkedList<String>().apply {
        addAll(
            SmsManager.getDefault().divideMessage(smsMessage),
        )
    }

    private val launcher = registry.register(
        generateActivityResultKey(),
        ActivityResultContracts.StartActivityForResult(),
    ) {
        onReturningFromSmsApp()
    }

    fun pollSms() {
        smsQueue.poll()?.let { msg ->
            launcher.launch(createSMSIntent(msg, smsNumberTo))
        } ?: onStatusChanged?.invoke(Status.ALL_SMS_SENT)
    }

    fun cancel() {
        smsQueue.clear()
    }

    fun smsCount() = smsQueue.count()

    private fun onReturningFromSmsApp() {
        onStatusChanged?.invoke(Status.RETURNED_TO_APP)
        val smsConfirmationDialog = BottomSheetDialog(
            bottomSheetDialogUiModel = BottomSheetDialogUiModel(
                title = context.getString(R.string.sms_enabled),
                subtitle = context.getString(R.string.sms_sync_is_sms_sent),
                iconResource = R.drawable.ic_help,
                mainButton = DialogButtonStyle.NeutralButton(R.string.no),
                secondaryButton = DialogButtonStyle.NeutralButton(R.string.yes),
            ),
            onMainButtonClicked = {
                cancel()
                onStatusChanged?.invoke(Status.SMS_NOT_MANUALLY_SENT)
            },
            onSecondaryButtonClicked = {
                pollSms()
            },
        ).also {
            it.isCancelable = false
        }
        smsConfirmationDialog
            .show(fragmentManager, BottomSheetDialogUiModel::class.java.simpleName)
    }

    private fun createSMSIntent(message: String, smsToNumber: String): Intent? {
        val uri = Uri.parse("smsto:$smsToNumber")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            putExtra("sms_body", message)
        }
        return Intent.createChooser(
            intent,
            context.getString(R.string.sms_sync_sms_app_chooser_title),
        )
    }

    private fun generateActivityResultKey(): String {
        return "sms" + mWho + "_rq#" + mNextLocalRequestCode.getAndIncrement()
    }
}
