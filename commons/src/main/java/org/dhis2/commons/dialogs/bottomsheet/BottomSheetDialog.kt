package org.dhis2.commons.dialogs.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing24
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
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

    private fun getSecondaryButtonColor(buttonStyle: DialogButtonStyle): ColorStyle =
        when (buttonStyle) {
            is DialogButtonStyle.DiscardButton -> ColorStyle.WARNING
            else -> ColorStyle.DEFAULT
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DHIS2Theme {
                    val scrollState = rememberLazyListState()
                    BottomSheetShell(
                        uiState =
                            BottomSheetShellUIState(
                                showBottomSectionDivider = showBottomDivider,
                                showTopSectionDivider = showTopDivider,
                                title = bottomSheetDialogUiModel.title,
                                description =
                                    when (bottomSheetDialogUiModel.clickableWord) {
                                        null -> bottomSheetDialogUiModel.message
                                        else -> null
                                    },
                                headerTextAlignment = bottomSheetDialogUiModel.headerTextAlignment,
                            ),
                        icon = {
                            if (bottomSheetDialogUiModel.iconResource != -1) {
                                Icon(
                                    modifier = Modifier.size(Spacing24),
                                    painter = painterResource(bottomSheetDialogUiModel.iconResource),
                                    contentDescription = "Icon",
                                    tint = SurfaceColor.Primary,
                                )
                            }
                        },
                        buttonBlock = {
                            BottomSheetButtons(
                                bottomSheetDialogUiModel = bottomSheetDialogUiModel,
                                onMainButtonClicked = onMainButtonClicked,
                                onSecondaryButtonClicked = onSecondaryButtonClicked,
                            )
                        },
                        onDismiss = {
                            dismiss()
                        },
                        content =
                            when {
                                content != null -> {
                                    {
                                        content.invoke(this@BottomSheetDialog, scrollState)
                                    }
                                }

                                bottomSheetDialogUiModel.clickableWord != null -> {
                                    {
                                        ClickableTextContent(
                                            bottomSheetDialogUiModel.message ?: "",
                                            bottomSheetDialogUiModel.clickableWord!!,
                                        )
                                    }
                                }

                                else -> null
                            },
                        contentScrollState = scrollState,
                    )
                }
            }
        }

    @Composable
    fun BottomSheetButtons(
        bottomSheetDialogUiModel: BottomSheetDialogUiModel,
        onMainButtonClicked: ((org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog)) -> Unit,
        onSecondaryButtonClicked: () -> Unit,
    ) {
        if (bottomSheetDialogUiModel.secondaryButton != null || bottomSheetDialogUiModel.mainButton != null) {
            ButtonBlock(
                modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                primaryButton = {
                    bottomSheetDialogUiModel.secondaryButton?.let { style ->

                        Button(
                            style = bottomSheetDialogUiModel.secondaryButton?.buttonStyle ?: ButtonStyle.TEXT,
                            text =
                                bottomSheetDialogUiModel.secondaryButton?.let {
                                    it.textLabel
                                        ?: stringResource(id = it.textResource)
                                } ?: "",
                            colorStyle = getSecondaryButtonColor(style),
                            onClick = {
                                onSecondaryButtonClicked()
                                dismiss()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag(SECONDARY_BUTTON_TAG),
                        )
                    }
                },
                secondaryButton = {
                    bottomSheetDialogUiModel.mainButton?.let {
                        Button(
                            style = ButtonStyle.FILLED,
                            text =
                                it.textLabel
                                    ?: stringResource(id = it.textResource),
                            onClick = {
                                onMainButtonClicked(this@BottomSheetDialog)
                                dismiss()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag(MAIN_BUTTON_TAG),
                        )
                    }
                },
            )
        }
    }

    @Composable
    private fun ClickableTextContent(
        originalText: String,
        clickableText: String,
    ) {
        Column(Modifier.padding(Spacing.Spacing0)) {
            ClickableText(
                modifier =
                    Modifier
                        .testTag(CLICKABLE_TEXT_TAG)
                        .fillMaxWidth(),
                text =
                    buildAnnotatedString {
                        val clickableWordIndex = originalText.indexOf(clickableText)
                        append(originalText)
                        addStyle(
                            style =
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            start = clickableWordIndex,
                            end = clickableWordIndex + clickableText.length,
                        )
                    },
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = TextColor.OnSurfaceLight,
                        textAlign = TextAlign.Start,
                    ),
                onClick = {
                    onMessageClick()
                },
            )
        }
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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

            behavior.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int,
                    ) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(
                        bottomSheet: View,
                        slideOffset: Float,
                    ) {
                        // NoUse
                    }
                },
            )
        }
    }
}
