package org.dhis2.ui.dialogs.alert

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.Dhis2Theme

const val TAG = "NotificationDialog"

class AlertDialog(
    val labelText: String,
    val descriptionText: String,
    val spanText: String? = null,
    val iconResource: Int,
    val dismissButton: ButtonUiModel,
    val confirmButton: ButtonUiModel
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
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow
            )
            setContent {
                Dhis2Theme {
                    Dhis2AlertDialogUi(
                        labelText = labelText,
                        descriptionText = descriptionText,
                        iconResource = iconResource,
                        spanText = spanText,
                        confirmButton = confirmButton.copy(
                            onClick = {
                                confirmButton.onClick()
                                dismiss()
                            }
                        ),
                        dismissButton = dismissButton.copy(
                            onClick = {
                                dismissButton.onClick()
                                dismiss()
                            }
                        )
                    )
                }
            }
        }
    }

    fun show(manager: FragmentManager) {
        super.show(manager, TAG)
    }
}

@Composable
fun Dhis2AlertDialogUi(
    labelText: String,
    descriptionText: String,
    iconResource: Int,
    spanText: String? = null,
    dismissButton: ButtonUiModel,
    confirmButton: ButtonUiModel
) {
    AlertDialog(
        onDismissRequest = dismissButton.onClick,
        title = { Text(text = labelText, textAlign = TextAlign.Center) },
        text = {
            Text(
                text = buildAnnotatedString {
                    append(descriptionText)
                    spanText?.let {
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.primary),
                            start = descriptionText.indexOf(spanText),
                            end = descriptionText.indexOf(spanText) + spanText.length
                        )
                    }
                }
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = iconResource),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "notification alert"
            )
        },
        confirmButton = {
            TextButton(onClick = confirmButton.onClick) {
                Text(text = confirmButton.text)
            }
        },
        dismissButton = {
            TextButton(onClick = dismissButton.onClick) {
                Text(text = dismissButton.text)
            }
        }
    )
}
