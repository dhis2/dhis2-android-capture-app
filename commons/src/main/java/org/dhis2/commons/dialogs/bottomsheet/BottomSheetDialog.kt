package org.dhis2.commons.dialogs.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog as BottomSheetShellImplementation

@Deprecated("Use BottomSheetShell directly instead")
class BottomSheetDialog(
    var bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    var onMainButtonClicked: ((BottomSheetShellImplementation)) -> Unit = {},
    var onSecondaryButtonClicked: () -> Unit = {},
    var onMessageClick: () -> Unit = {},
    val showTopDivider: Boolean = false,
    val showBottomDivider: Boolean = false,
    val content: @Composable
    ((BottomSheetShellImplementation, scrollState: LazyListState) -> Unit)? = null,
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DHIS2Theme {
                    if (bottomSheetDialogUiModel.clickableWord == null) {
                        org.dhis2.commons.dialogs.bottomsheet.ui.BottomSheetDialog(
                            bottomSheetDialogUiModel = bottomSheetDialogUiModel,
                            showTopDivider = showTopDivider,
                            showBottomDivider = showBottomDivider,
                            onMainButtonClicked = {
                                onMainButtonClicked(this@BottomSheetDialog)
                            },
                            onSecondaryButtonClicked = onSecondaryButtonClicked,
                            onDismiss = this@BottomSheetDialog::dismiss,
                            content = when {
                                content != null -> {
                                    { scrollState ->
                                        content.invoke(this@BottomSheetDialog, scrollState)
                                    }
                                }

                                else -> null
                            },
                        )
                    } else {
                        org.dhis2.commons.dialogs.bottomsheet.ui.BottomSheetDialog(
                            bottomSheetDialogUiModel = bottomSheetDialogUiModel,
                            showTopDivider = showTopDivider,
                            showBottomDivider = showBottomDivider,
                            onMainButtonClicked = {
                                onMainButtonClicked(this@BottomSheetDialog)
                            },
                            onSecondaryButtonClicked = onSecondaryButtonClicked,
                            onDismiss = this@BottomSheetDialog::dismiss,
                            onClickableTextContent = onMessageClick,
                        )
                    }
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
}
