package org.dhis2.mobile.login.pin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import org.dhis2.mobile.login.pin.domain.model.PinState
import org.dhis2.mobile.login.pin.ui.viewmodel.PinViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.create_pin
import org.dhis2.mobile.login.resources.create_pin_button
import org.dhis2.mobile.login.resources.create_pin_description
import org.dhis2.mobile.login.resources.enter_pin
import org.dhis2.mobile.login.resources.enter_pin_button
import org.dhis2.mobile.login.resources.enter_pin_description
import org.dhis2.mobile.login.resources.forgot_pin_button
import org.dhis2.mobile.login.resources.pin_error_remaining_attempts
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputSegmentedShell
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.component.model.SegmentedShellType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * PIN mode enumeration for different bottom sheet behaviors.
 */
enum class PinMode {
    /**
     * SET mode - Used when creating/setting a new PIN.
     */
    SET,

    /**
     * ASK mode - Used when verifying/entering an existing PIN.
     */
    ASK,
}

/**
 * DHIS2 PIN Bottom Sheet component with integrated ViewModel.
 *
 * A modal bottom sheet that displays a PIN input interface using DHIS2 Mobile UI components.
 * Supports both SET (creating PIN) and ASK (verifying PIN) modes.
 * Uses [BottomSheetShell] as container and [InputSegmentedShell] for PIN input.
 *
 * State management is handled internally by [PinViewModel], automatically injected via Koin,
 * including PIN validation, attempt tracking, and error messages.
 *
 * @param mode The PIN mode (SET for creation, ASK for verification).
 * @param onSuccess Callback invoked when PIN operation succeeds.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun PinBottomSheet(
    mode: PinMode,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PinViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var currentPin by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Handle state changes from ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            is PinState.Success -> {
                onSuccess()
            }

            is PinState.TooManyAttempts -> {
                viewModel.onForgotPin()
            }

            is PinState.Dismissed -> {
                viewModel.resetAttempts()
                viewModel.resetState()
                onDismiss()
            }

            else -> { // Do nothing for Idle, Loading, Error
            }
        }
    }

    // Reset attempts when dismissed
    LaunchedEffect(Unit) {
        return@LaunchedEffect
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val (title, subtitle) =
        when (mode) {
            PinMode.SET -> stringResource(Res.string.create_pin) to stringResource(Res.string.create_pin_description)
            PinMode.ASK -> stringResource(Res.string.enter_pin) to stringResource(Res.string.enter_pin_description)
        }

    val primaryButtonText =
        when (mode) {
            PinMode.SET -> stringResource(Res.string.create_pin_button)
            PinMode.ASK -> stringResource(Res.string.enter_pin_button)
        }

    val secondaryButtonText =
        when (mode) {
            PinMode.SET -> null
            PinMode.ASK -> stringResource(Res.string.forgot_pin_button)
        }

    // Get error message from ViewModel state
    val errorMessage =
        when (val state = uiState) {
            is PinState.Error -> {
                state.remainingAttempts?.let { attempts ->
                    "${state.message}. ${
                        stringResource(
                            Res.string.pin_error_remaining_attempts,
                            attempts,
                        )
                    }"
                } ?: state.message
            }

            else -> null
        }

    val pinLength = 4
    val isLoading = uiState is PinState.Loading

    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                title = title,
                subtitle = null,
                description = subtitle,
                showTopSectionDivider = false,
                showBottomSectionDivider = false,
                headerTextAlignment = TextAlign.Center,
                animateHeaderOnKeyboardAppearance = false,
                scrollableContainerMinHeight = Spacing.Spacing40,
                contentPadding =
                    PaddingValues(
                        horizontal = Spacing.Spacing24,
                        vertical = Spacing.Spacing0,
                    ),
            ),
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.Spacing16),
            ) {
                Spacer(modifier = Modifier.height(Spacing.Spacing8))

                // PIN Input using InputSegmentedShell
                InputSegmentedShell(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    segmentCount = pinLength,
                    initialValue = null,
                    supportingTextData =
                        errorMessage?.let {
                            SupportingTextData(
                                text = it,
                                state = SupportingTextState.ERROR,
                            )
                        },
                    segmentedShellType = SegmentedShellType.Numeric,
                    onValueChanged = { newPin ->
                        currentPin = newPin
                        // Clear error when user starts typing
                        if (uiState is PinState.Error) {
                            viewModel.resetState()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.Spacing8))
            }
        },
        icon = {
            Icon(
                Icons.Outlined.Pin,
                tint = SurfaceColor.Primary,
                contentDescription = stringResource(Res.string.enter_pin),
            )
        },
        buttonBlock = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Primary Button (Save / Continue)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentPin.replace("-", "").length == pinLength && !isLoading,
                    style = ButtonStyle.FILLED,
                    text = primaryButtonText,
                    onClick = {
                        viewModel.onPinComplete(
                            pin = currentPin.replace("-", ""),
                            mode = mode,
                        )
                    },
                )

                secondaryButtonText?.let { buttonText ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        style = ButtonStyle.OUTLINED,
                        text = buttonText,
                        onClick = {
                            viewModel.onForgotPin()
                        },
                    )
                }
            }
        },
        onDismiss = {
            viewModel.resetAttempts()
            viewModel.resetState()
            onDismiss()
        },
    )
}
