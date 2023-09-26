package org.dhis2.ui.dialogs.alert

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.Dhis2Theme
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

const val TAG = "NotificationDialog"

class AlertDialog(
    val labelText: String,
    val descriptionText: String,
    val spanText: String? = null,
    val iconResource: Int? = null,
    @RawRes val animationRes: Int? = null,
    val dismissButton: ButtonUiModel,
    val confirmButton: ButtonUiModel,
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
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow,
            )
            setContent {
                Dhis2Theme {
                    Dhis2AlertDialogUi(
                        labelText = labelText,
                        descriptionText = descriptionText,
                        iconResource = iconResource,
                        spanText = spanText,
                        animationRes = animationRes,
                        confirmButton = confirmButton.copy(
                            onClick = {
                                confirmButton.onClick()
                                dismiss()
                            },
                        ),
                        dismissButton = dismissButton.copy(
                            onClick = {
                                dismissButton.onClick()
                                dismiss()
                            },
                        ),
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
    iconResource: Int?,
    spanText: String? = null,
    @RawRes animationRes: Int? = null,
    dismissButton: ButtonUiModel,
    confirmButton: ButtonUiModel,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes ?: -1),
    )

    var confirmButtonClick = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = dismissButton.onClick,
        title = { Text(text = labelText, textAlign = TextAlign.Center) },
        text = {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append(descriptionText)
                        spanText?.let {
                            addStyle(
                                style = SpanStyle(MaterialTheme.colorScheme.primary),
                                start = descriptionText.indexOf(spanText),
                                end = descriptionText.indexOf(spanText) + spanText.length,
                            )
                        }
                    },
                )
                animationRes?.let {
                    Spacer(modifier = Modifier.size(16.dp))
                    if (!confirmButtonClick.value) {
                        LottieAnimation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProgressIndicator(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(100.dp),
                                type = ProgressIndicatorType.CIRCULAR,
                            )
                        }
                    }
                }
            }
        },
        icon = {
            iconResource?.let {
                Icon(
                    painter = painterResource(id = iconResource),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "notification alert",
                )
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag(CONFIRM_BUTTON_TAG),
                onClick = {
                    confirmButtonClick.value = true
                    animationRes?.let {
                        val job = Job()
                        val scope = CoroutineScope(job)

                        scope.launch {
                            delay(5000)
                            confirmButton.onClick.invoke()
                        }
                    } ?: confirmButton.onClick.invoke()
                },
            ) {
                Text(text = confirmButton.text)
            }
        },
        dismissButton = {
            TextButton(onClick = dismissButton.onClick) {
                Text(text = dismissButton.text)
            }
        },
    )
}

const val CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG"
