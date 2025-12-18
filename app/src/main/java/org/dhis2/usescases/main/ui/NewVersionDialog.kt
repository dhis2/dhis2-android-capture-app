package org.dhis2.usescases.main.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

const val TAG = "NotificationDialog"

class NewVersionDialog(
    private val newVersion: String,
    private val onRemindMeLater: () -> Unit,
    private val onDownloadVersion: () -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
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
                    Dhis2AlertDialogUi(
                        newVersion = newVersion,
                        onRemindMeLater = {
                            dismiss()
                            onRemindMeLater()
                        },
                        onDownloadVersion = {
                            dismiss()
                            onDownloadVersion()
                        },
                    )
                }
            }
        }

    fun show(manager: FragmentManager) {
        super.show(manager, TAG)
    }
}

@Composable
private fun Dhis2AlertDialogUi(
    newVersion: String,
    onRemindMeLater: () -> Unit,
    onDownloadVersion: () -> Unit,
) {
    val description = stringResource(R.string.new_version_message, newVersion)
    AlertDialog(
        onDismissRequest = onRemindMeLater,
        title = {
            Text(
                text = stringResource(R.string.software_update),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column {
                Text(
                    text =
                        buildAnnotatedString {
                            append(description)
                            addStyle(
                                style = SpanStyle(MaterialTheme.colorScheme.primary),
                                start = description.indexOf(newVersion),
                                end = description.indexOf(newVersion) + newVersion.length,
                            )
                        },
                )
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_software_update),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "notification alert",
            )
        },
        confirmButton = {
            Button(
                text = stringResource(R.string.download_now),
                modifier = Modifier.testTag(CONFIRM_BUTTON_TAG),
                onClick = onDownloadVersion,
            )
        },
        dismissButton = {
            Button(
                text = stringResource(R.string.remind_me_later),
                onClick = onRemindMeLater,
            )
        },
    )
}

const val CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG"
