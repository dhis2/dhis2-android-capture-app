package org.dhis2.form.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.form.di.Injector
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetDialogViewModel
import org.dhis2.form.model.OptionSetDialogViewModelFactory

const val TAG = "OptionSetDialog"

class OptionSetDialog(
    private val field: FieldUiModel,
    private val onClearValue: () -> Unit,
    private val onSaveOptionValue: (optionCode: String?) -> Unit
) : DialogFragment() {

    val viewModel by viewModels<OptionSetDialogViewModel> {
        OptionSetDialogViewModelFactory(
            Injector.provideOptionSetDialog(),
            field,
            Injector.provideDispatchers()
        )
    }

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
                MdcTheme {
                    OptionSetDialogScreen(
                        viewModel,
                        onCancelClick = { dismiss() },
                        onClearClick = {
                            onClearValue()
                            dismiss()
                        },
                        onOptionClick = { optionCode ->
                            onSaveOptionValue(optionCode)
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    fun show(manager: FragmentManager) {
        super.show(manager, TAG)
    }
}
