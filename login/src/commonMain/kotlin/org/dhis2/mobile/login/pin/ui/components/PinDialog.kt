package org.dhis2.mobile.login.pin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.commons.extensions.deviceIsInLandscapeMode
import org.dhis2.mobile.commons.extensions.getWindowSizeClass
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.ui.state.PinUiState
import org.dhis2.mobile.login.pin.ui.viewmodel.PinViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.FullScreenDialog
import org.hisp.dhis.mobile.ui.designsystem.component.InputSegmentedShell
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.component.model.SegmentedShellType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * DHIS2 PIN Bottom Sheet component with integrated ViewModel.
 *
 * A modal bottom sheet that displays a PIN input interface using DHIS2 Mobile UI components.
 * Supports both SET (creating PIN) and ASK (verifying PIN) modes.
 * Uses [FullScreenDialog] as container and [InputSegmentedShell] for PIN input.
 *
 * State management is handled internally by [PinViewModel], automatically injected via Koin,
 * including PIN validation, attempt tracking, and error messages.
 *
 * @param mode The PIN mode (SET for creation, ASK for verification).
 * @param onSuccess Callback invoked when PIN operation succeeds.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 * @param modifier Optional modifier for customization.
 * @param windowSizeClass Window size class for layout adjustments.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PinDialog(
    mode: PinMode,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = getWindowSizeClass(),
) {
    val viewModel: PinViewModel = koinViewModel { parametersOf(mode) }
    val uiState by viewModel.uiState.collectAsState()
    val isLandscape = deviceIsInLandscapeMode()

    LaunchedEffect(uiState.isSuccess, uiState.isDismissed, uiState.isTooManyAttempts) {
        when {
            uiState.isSuccess -> onSuccess()
            uiState.isTooManyAttempts -> viewModel.onForgotPin()
            uiState.isDismissed -> {
                viewModel.resetAttempts()
                viewModel.resetState()
                onDismiss()
            }
        }
    }

    PinBottomSheetContent(
        uiState = uiState,
        isLandscape = isLandscape,
        windowSizeClass = windowSizeClass,
        onPinChanged = viewModel::onPinChanged,
        onPrimaryClick = viewModel::onPinComplete,
        onSecondaryClick = viewModel::onForgotPin,
        onDismiss = {
            viewModel.resetAttempts()
            viewModel.resetState()
            onDismiss()
        },
        modifier = modifier,
    )
}

/**
 * Stateless UI content for the PIN bottom sheet.
 *
 * Renders the [FullScreenDialog] with either a portrait (stacked) or landscape (side-by-side)
 * layout depending on [isLandscape]. This composable holds no business logic and is safe
 * to use in Compose Previews.
 *
 * @param uiState The current UI state containing display content and button states.
 * @param isLandscape Whether the device is in landscape orientation.
 * @param windowSizeClass Window size class used to choose the button layout.
 * @param onPinChanged Callback invoked on every PIN value change.
 * @param onPrimaryClick Callback for the primary button click.
 * @param onSecondaryClick Callback for the secondary button click.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param modifier Optional modifier for customization.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun PinBottomSheetContent(
    uiState: PinUiState,
    isLandscape: Boolean,
    windowSizeClass: WindowSizeClass,
    onPinChanged: (String) -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    FullScreenDialog(
        onDismiss = onDismiss,
        content = {
            if (!isLandscape) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    PinHeader(
                        title = uiState.title,
                        subtitle = uiState.subtitle,
                    )

                    PinInputBlock(
                        focusRequester = focusRequester,
                        pinLength = uiState.pinLength,
                        errorMessage = uiState.errorMessage,
                        windowSizeClass = windowSizeClass,
                        primaryButtonIsEnabled = uiState.primaryButtonIsEnabled,
                        primaryButtonText = uiState.primaryButtonText,
                        secondaryButtonIsEnabled = !uiState.isLoading,
                        secondaryButtonText = uiState.secondaryButtonText,
                        onPinChanged = onPinChanged,
                        onPrimaryClick = onPrimaryClick,
                        onSecondaryClick = onSecondaryClick,
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(horizontal = Spacing.Spacing56)
                            .padding(bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing56),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PinHeader(
                        title = uiState.title,
                        subtitle = uiState.subtitle,
                        modifier = Modifier.weight(1f),
                    )

                    VerticalDivider(
                        thickness = Spacing.Spacing1,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    PinInputBlock(
                        focusRequester = focusRequester,
                        pinLength = uiState.pinLength,
                        errorMessage = uiState.errorMessage,
                        windowSizeClass = windowSizeClass,
                        primaryButtonIsEnabled = uiState.primaryButtonIsEnabled,
                        primaryButtonText = uiState.primaryButtonText,
                        secondaryButtonIsEnabled = !uiState.isLoading,
                        secondaryButtonText = uiState.secondaryButtonText,
                        onPinChanged = onPinChanged,
                        onPrimaryClick = onPrimaryClick,
                        onSecondaryClick = onSecondaryClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

/**
 * Displays the PIN screen header: a PIN icon, a title, and a subtitle.
 *
 * @param title The main heading text.
 * @param subtitle The descriptive text shown below the title.
 * @param modifier Optional modifier for customization.
 */
@Composable
private fun PinHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Pin,
            contentDescription = "Pin Icon",
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = Spacing.Spacing16),
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.Spacing16),
        )
    }
}

/**
 * Displays the PIN input field and action buttons.
 *
 * Renders [InputSegmentedShell] for PIN entry and adapts the button layout
 * (vertical vs horizontal) based on [windowSizeClass].
 *
 * @param focusRequester Focus requester to autofocus the input field.
 * @param pinLength Number of PIN digits.
 * @param errorMessage Optional error message shown below the input.
 * @param windowSizeClass Window size class used to choose the button layout.
 * @param primaryButtonIsEnabled Whether the primary action button is enabled.
 * @param primaryButtonText Label for the primary action button.
 * @param secondaryButtonIsEnabled Whether the secondary action button is enabled.
 * @param secondaryButtonText Optional label for the secondary action button; omitted when null.
 * @param onPinChanged Callback invoked on every PIN value change.
 * @param onPrimaryClick Callback for the primary button click.
 * @param onSecondaryClick Callback for the secondary button click.
 * @param modifier Optional modifier for customization.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun PinInputBlock(
    focusRequester: FocusRequester,
    pinLength: Int,
    errorMessage: String?,
    windowSizeClass: WindowSizeClass,
    primaryButtonIsEnabled: Boolean,
    primaryButtonText: String,
    secondaryButtonIsEnabled: Boolean,
    secondaryButtonText: String?,
    onPinChanged: (String) -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stabilize supportingTextData to prevent recomposition issues
    val supportingTextData =
        remember(errorMessage) {
            errorMessage?.let {
                SupportingTextData(
                    text = it,
                    state = SupportingTextState.ERROR,
                )
            }
        }

    // Request focus on initial composition only
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        InputSegmentedShell(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Spacing16)
                    .focusRequester(focusRequester),
            segmentCount = pinLength,
            initialValue = null,
            supportingTextData = supportingTextData,
            segmentedShellType = SegmentedShellType.Numeric,
            onValueChanged = onPinChanged,
        )

        Spacer(modifier = Modifier.height(Spacing.Spacing32))

        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                VerticalButtonBlock(
                    primaryButton = {
                        PinPrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = primaryButtonIsEnabled,
                            buttonText = primaryButtonText,
                            onClick = onPrimaryClick,
                        )
                    },
                    secondaryButton =
                        secondaryButtonText?.let { buttonText ->
                            {
                                PinSecondaryButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = secondaryButtonIsEnabled,
                                    buttonText = buttonText,
                                    onClick = onSecondaryClick,
                                )
                            }
                        },
                )

            else ->
                HorizontalButtonBlock(
                    primaryButton = {
                        PinPrimaryButton(
                            modifier = Modifier.weight(1f),
                            enabled = primaryButtonIsEnabled,
                            buttonText = primaryButtonText,
                            onClick = onPrimaryClick,
                        )
                    },
                    secondaryButton =
                        secondaryButtonText?.let { buttonText ->
                            {
                                PinSecondaryButton(
                                    modifier = Modifier.weight(1f),
                                    enabled = secondaryButtonIsEnabled,
                                    buttonText = buttonText,
                                    onClick = onSecondaryClick,
                                )
                            }
                        },
                )
        }
    }
}

@Composable
private fun VerticalButtonBlock(
    primaryButton: @Composable () -> Unit,
    secondaryButton: (@Composable () -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(BottomSheetShellDefaults.buttonBlockPaddings()),
        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        primaryButton()
        secondaryButton?.invoke()
    }
}

@Composable
private fun HorizontalButtonBlock(
    primaryButton: @Composable RowScope.() -> Unit,
    secondaryButton: (@Composable RowScope.() -> Unit)?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(BottomSheetShellDefaults.buttonBlockPaddings()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        secondaryButton?.invoke(this)
        primaryButton()
    }
}

@Composable
private fun PinPrimaryButton(
    modifier: Modifier,
    enabled: Boolean,
    buttonText: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        style = ButtonStyle.FILLED,
        text = buttonText,
        onClick = onClick,
    )
}

@Composable
private fun PinSecondaryButton(
    modifier: Modifier,
    enabled: Boolean,
    buttonText: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        style = ButtonStyle.OUTLINED,
        text = buttonText,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PinAskPortraitPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))
    DHIS2Theme {
        PinBottomSheetContent(
            uiState =
                PinUiState(
                    title = "Enter your PIN",
                    subtitle = "Enter your 4-digit PIN to access your account.",
                    primaryButtonText = "Unlock",
                    secondaryButtonText = "Forgot your PIN?",
                ),
            isLandscape = false,
            windowSizeClass = windowSizeClass,
            onPinChanged = {},
            onPrimaryClick = {},
            onSecondaryClick = {},
            onDismiss = {},
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun PinAskLandscapePreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 800.dp))
    DHIS2Theme {
        PinBottomSheetContent(
            uiState =
                PinUiState(
                    title = "Enter your PIN",
                    subtitle = "Enter your 4-digit PIN to access your account.",
                    primaryButtonText = "Unlock",
                    secondaryButtonText = "Forgot your PIN?",
                ),
            isLandscape = true,
            windowSizeClass = windowSizeClass,
            onPinChanged = {},
            onPrimaryClick = {},
            onSecondaryClick = {},
            onDismiss = {},
        )
    }
}
