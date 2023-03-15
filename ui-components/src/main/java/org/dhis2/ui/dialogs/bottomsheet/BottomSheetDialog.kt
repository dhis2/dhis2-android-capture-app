package org.dhis2.ui.dialogs.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.ui.R

class BottomSheetDialog(
    var bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    var onMainButtonClicked: () -> Unit = {},
    var onSecondaryButtonClicked: () -> Unit = {},
    var onMessageClick: () -> Unit = {},
    val content: @Composable
    ((org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog) -> Unit)? = null
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
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
                    BottomSheetDialogUi(
                        bottomSheetDialogUiModel = bottomSheetDialogUiModel,
                        onMainButtonClicked = {
                            onMainButtonClicked.invoke()
                            dismiss()
                        },
                        onSecondaryButtonClicked = {
                            onSecondaryButtonClicked.invoke()
                            dismiss()
                        },
                        onMessageClick = onMessageClick,
                        extraContent = content?.let {
                            { it.invoke(this@BottomSheetDialog) }
                        }
                    )
                }
            }
        }
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
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
}
