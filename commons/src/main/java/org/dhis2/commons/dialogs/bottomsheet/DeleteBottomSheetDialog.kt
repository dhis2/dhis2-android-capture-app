package org.dhis2.commons.dialogs.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class
DeleteBottomSheetDialog(
    private val title: String,
    private val description: String,
    private val mainButtonText: String,
    private val deleteForever: Boolean = false,
    private val onMainButtonClick: () -> Unit,
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG: String = "DELETE_DIALOG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                BottomSheetShell(
                    uiState =
                        BottomSheetShellUIState(
                            title = title,
                            showTopSectionDivider = true,
                            showBottomSectionDivider = false,
                            description = description,
                        ),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Button",
                            tint = SurfaceColor.Error,
                        )
                    },
                    buttonBlock = {
                        ButtonBlock(
                            modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                            primaryButton = {
                                Button(
                                    style = ButtonStyle.OUTLINED,
                                    text = getString(R.string.cancel),
                                    onClick = { dismiss() },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            secondaryButton = {
                                Button(
                                    style = ButtonStyle.FILLED,
                                    icon = {
                                        Icon(
                                            imageVector =
                                                if (deleteForever) {
                                                    Icons.Filled.DeleteForever
                                                } else {
                                                    Icons.Filled.Delete
                                                },
                                            contentDescription = "Button",
                                        )
                                    },
                                    text = mainButtonText,
                                    colorStyle = ColorStyle.ERROR,
                                    onClick = {
                                        dismiss()
                                        onMainButtonClick()
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                )
                            },
                        )
                    },
                    onDismiss = {
                        dismiss()
                    },
                    content = {
                        // no-op
                    },
                )
            }
        }
}
