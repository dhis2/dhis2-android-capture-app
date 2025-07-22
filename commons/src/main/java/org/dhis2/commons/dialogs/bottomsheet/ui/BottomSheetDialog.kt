package org.dhis2.commons.dialogs.bottomsheet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.CLICKABLE_TEXT_TAG
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetInsets
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetLowerPadding
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing24
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun BottomSheetDialog(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    showTopDivider: Boolean,
    showBottomDivider: Boolean,
    onMainButtonClicked: () -> Unit = {},
    onSecondaryButtonClicked: () -> Unit = {},
    onDismiss: () -> Unit,
    content: @Composable ((scrollState: LazyListState) -> Unit)?,
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = bottomSheetDialogUiModel,
        showTopDivider = showTopDivider,
        showBottomDivider = showBottomDivider,
        onMainButtonClicked = onMainButtonClicked,
        onSecondaryButtonClicked = onSecondaryButtonClicked,
        onDismiss = onDismiss,
        onClickableTextContent = {},
        content = content,
    )
}

@Composable
fun BottomSheetDialog(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    showTopDivider: Boolean,
    showBottomDivider: Boolean,
    onMainButtonClicked: () -> Unit = {},
    onSecondaryButtonClicked: () -> Unit = {},
    onDismiss: () -> Unit,
    onClickableTextContent: () -> Unit,
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = bottomSheetDialogUiModel,
        showTopDivider = showTopDivider,
        showBottomDivider = showBottomDivider,
        onMainButtonClicked = onMainButtonClicked,
        onSecondaryButtonClicked = onSecondaryButtonClicked,
        onDismiss = onDismiss,
        onClickableTextContent = onClickableTextContent,
        content = null,
    )
}

@Composable
private fun BottomSheetDialog(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    showTopDivider: Boolean,
    showBottomDivider: Boolean,
    onMainButtonClicked: () -> Unit = {},
    onSecondaryButtonClicked: () -> Unit = {},
    onDismiss: () -> Unit,
    onClickableTextContent: () -> Unit,
    content: @Composable ((scrollState: LazyListState) -> Unit)?,
) {
    val scrollState = rememberLazyListState()
    BottomSheetShell(
        modifier = Modifier.navigationBarsPadding(),
        uiState = BottomSheetShellUIState(
            bottomPadding = bottomSheetLowerPadding(),
            showBottomSectionDivider = showBottomDivider,
            showTopSectionDivider = showTopDivider,
            title = bottomSheetDialogUiModel.title,
            description = when (bottomSheetDialogUiModel.clickableWord) {
                null -> bottomSheetDialogUiModel.message
                else -> null
            },
            headerTextAlignment = bottomSheetDialogUiModel.headerTextAlignment,
        ),
        windowInsets = { bottomSheetInsets() },

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
                onDismiss = onDismiss,
            )
        },
        onDismiss = onDismiss,
        content = when {
            content != null -> { { content(scrollState) } }
            bottomSheetDialogUiModel.clickableWord != null -> {
                {
                    ClickableTextContent(
                        originalText = bottomSheetDialogUiModel.message ?: "",
                        clickableText = bottomSheetDialogUiModel.clickableWord!!,
                        onMessageClick = onClickableTextContent,
                    )
                }
            }
            else -> null
        },
        contentScrollState = scrollState,
    )
}

@Composable
fun BottomSheetButtons(
    bottomSheetDialogUiModel: BottomSheetDialogUiModel,
    onMainButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (bottomSheetDialogUiModel.secondaryButton != null || bottomSheetDialogUiModel.mainButton != null) {
        ButtonBlock(
            modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
            primaryButton = {
                bottomSheetDialogUiModel.secondaryButton?.let { style ->
                    Button(
                        style = bottomSheetDialogUiModel.secondaryButton?.buttonStyle
                            ?: ButtonStyle.TEXT,
                        text = bottomSheetDialogUiModel.secondaryButton?.let {
                            it.textLabel
                                ?: stringResource(id = it.textResource)
                        } ?: "",
                        colorStyle = when (style) {
                            is DialogButtonStyle.DiscardButton -> ColorStyle.WARNING
                            else -> ColorStyle.DEFAULT
                        },
                        onClick = {
                            onSecondaryButtonClicked()
                            onDismiss()
                        },
                        modifier = Modifier
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
                            onMainButtonClicked()
                            onDismiss
                        },
                        modifier = Modifier
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
    onMessageClick: () -> Unit,
) {
    Column(Modifier.padding(Spacing.Spacing0)) {
        ClickableText(
            modifier = Modifier
                .testTag(CLICKABLE_TEXT_TAG)
                .fillMaxWidth(),
            text = buildAnnotatedString {
                val clickableWordIndex = originalText.indexOf(clickableText)
                append(originalText)
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
                    start = clickableWordIndex,
                    end = clickableWordIndex + clickableText.length,
                )
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextColor.OnSurfaceLight,
                textAlign = TextAlign.Start,
            ),
            onClick = {
                onMessageClick()
            },
        )
    }
}
