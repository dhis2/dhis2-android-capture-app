package org.dhis2.ui.dialogs.signature

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.ui.theme.Dhis2Theme

const val TAG = "SignatureDialog"

class SignatureDialog(
    private val title: String,
    private val onSaveSignature: ((Bitmap) -> Unit)? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    @OptIn(ExperimentalComposeUiApi::class)
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
                    SignatureDialogUi(
                        title = title,
                        onSave = {
                            onSaveSignature?.invoke(it)
                            dismiss()
                        },
                        onCancel = { dismiss() }
                    )
                }
            }
        }
    }

    fun show(manager: FragmentManager) {
        super.show(manager, TAG)
    }
}
