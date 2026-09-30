package org.dhis2.usescases.general.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

const val SESSION_RENEWAL_DIALOG_TAG = "SessionRenewalDialog"
const val SESSION_RENEWAL_CONFIRM_BUTTON_TAG = "SESSION_RENEWAL_CONFIRM_BUTTON_TAG"

class SessionRenewalDialog(
    private val onNotNow: () -> Unit,
    private val onLogInAgain: () -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow,
            )
            setContent {
                DHIS2Theme {
                    SessionRenewalDialogUi(
                        onNotNow = {
                            dismiss()
                            onNotNow()
                        },
                        onLogInAgain = {
                            dismiss()
                            onLogInAgain()
                        },
                    )
                }
            }
        }

    fun show(manager: FragmentManager) {
        if (manager.findFragmentByTag(SESSION_RENEWAL_DIALOG_TAG) != null) return
        super.show(manager, SESSION_RENEWAL_DIALOG_TAG)
    }
}

@Composable
private fun SessionRenewalDialogUi(
    onNotNow: () -> Unit,
    onLogInAgain: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNotNow,
        title = {
            Text(
                text = stringResource(R.string.session_renewal_title),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(text = stringResource(R.string.session_renewal_message))
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock_inactive),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "session expired alert",
            )
        },
        confirmButton = {
            Button(
                text = stringResource(R.string.session_renewal_action),
                modifier = Modifier.testTag(SESSION_RENEWAL_CONFIRM_BUTTON_TAG),
                onClick = onLogInAgain,
            )
        },
        dismissButton = {
            Button(
                text = stringResource(R.string.sync_dialog_action_not_now),
                onClick = onNotNow,
            )
        },
    )
}
