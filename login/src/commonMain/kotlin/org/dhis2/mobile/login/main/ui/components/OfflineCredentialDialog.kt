package org.dhis2.mobile.login.main.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.commons.extensions.deviceIsInLandscapeMode
import org.dhis2.mobile.commons.extensions.getWindowSizeClass
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.offline_credential_create_button
import org.dhis2.mobile.login.resources.offline_credential_create_description
import org.dhis2.mobile.login.resources.offline_credential_create_title
import org.dhis2.mobile.login.resources.offline_credential_enter_button
import org.dhis2.mobile.login.resources.offline_credential_enter_description
import org.dhis2.mobile.login.resources.offline_credential_enter_title
import org.dhis2.mobile.login.resources.offline_credential_forgot_button
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.FullScreenDialog
import org.hisp.dhis.mobile.ui.designsystem.component.InputSegmentedShell
import org.hisp.dhis.mobile.ui.designsystem.component.model.SegmentedShellType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/**
 * The two ways the offline credential dialog is used for token-based (OAuth2/OpenID) accounts.
 */
enum class OfflineCredentialMode {
    /** First login on a new account: create the mandatory offline credential. Non-dismissable. */
    CREATE,

    /** Re-opening an existing account offline: enter the credential to log in. Dismissable. */
    ENTER,
}

/**
 * Captures the offline-login credential (a numeric PIN in this first iteration) for a token-based
 * account and hands the entered value straight back to the caller via [onSubmit].
 *
 * Unlike the session-lock PIN component, this holds no business logic: it neither saves nor
 * validates the value locally — the caller stores it (CREATE) or uses it as the offline login
 * password (ENTER). It is intentionally free of ViewModel / use case / repository wiring.
 *
 * @param mode Whether the user is creating ([OfflineCredentialMode.CREATE]) or entering
 * ([OfflineCredentialMode.ENTER]) the credential.
 * @param onSubmit Invoked with the entered credential when the primary button is pressed.
 * @param onForgot Invoked in [OfflineCredentialMode.ENTER] when the user presses the secondary
 * button because they forgot the credential. Ignored in CREATE mode, which is non-dismissable.
 * @param onDismiss Invoked in [OfflineCredentialMode.ENTER] when the dialog is closed without
 * using it, which is a different intent from [onForgot] and must not start a recovery. Ignored in
 * CREATE mode, which is non-dismissable.
 * @param length Number of credential digits.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OfflineCredentialDialog(
    mode: OfflineCredentialMode,
    onSubmit: (String) -> Unit,
    onForgot: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    length: Int = 4,
    windowSizeClass: WindowSizeClass = getWindowSizeClass(),
) {
    var value by remember { mutableStateOf("") }
    val isComplete = value.length == length
    val isCreate = mode == OfflineCredentialMode.CREATE

    OfflineCredentialContent(
        title = stringResource(if (isCreate) Res.string.offline_credential_create_title else Res.string.offline_credential_enter_title),
        subtitle =
            stringResource(
                if (isCreate) Res.string.offline_credential_create_description else Res.string.offline_credential_enter_description,
            ),
        primaryButtonText =
            stringResource(
                if (isCreate) Res.string.offline_credential_create_button else Res.string.offline_credential_enter_button,
            ),
        secondaryButtonText = if (isCreate) null else stringResource(Res.string.offline_credential_forgot_button),
        primaryButtonEnabled = isComplete,
        showPrimaryButtonIcon = isCreate,
        length = length,
        isLandscape = deviceIsInLandscapeMode(),
        windowSizeClass = windowSizeClass,
        onValueChanged = { value = it.replace("-", "") },
        onPrimaryClick = { if (isComplete) onSubmit(value) },
        onSecondaryClick = onForgot,
        // CREATE is mandatory and non-dismissable: swallow dismiss so the caller's gate keeps it shown.
        onDismiss = if (isCreate) ({}) else onDismiss,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun OfflineCredentialContent(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    secondaryButtonText: String?,
    primaryButtonEnabled: Boolean,
    showPrimaryButtonIcon: Boolean,
    length: Int,
    isLandscape: Boolean,
    windowSizeClass: WindowSizeClass,
    onValueChanged: (String) -> Unit,
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
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    Header(title = title, subtitle = subtitle)
                    InputBlock(
                        length = length,
                        focusRequester = focusRequester,
                        windowSizeClass = windowSizeClass,
                        primaryButtonText = primaryButtonText,
                        secondaryButtonText = secondaryButtonText,
                        primaryButtonEnabled = primaryButtonEnabled,
                        showPrimaryButtonIcon = showPrimaryButtonIcon,
                        onValueChanged = onValueChanged,
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
                            .padding(horizontal = Spacing.Spacing56),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing56),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = Spacing.Spacing32),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Header(title = title, subtitle = subtitle)
                    }

                    VerticalDivider(
                        modifier = Modifier.padding(vertical = Spacing.Spacing32),
                        thickness = Spacing.Spacing1,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = Spacing.Spacing32),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        InputBlock(
                            length = length,
                            focusRequester = focusRequester,
                            windowSizeClass = windowSizeClass,
                            primaryButtonText = primaryButtonText,
                            secondaryButtonText = secondaryButtonText,
                            primaryButtonEnabled = primaryButtonEnabled,
                            showPrimaryButtonIcon = showPrimaryButtonIcon,
                            onValueChanged = onValueChanged,
                            onPrimaryClick = onPrimaryClick,
                            onSecondaryClick = onSecondaryClick,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun Header(
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
            contentDescription = null,
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun InputBlock(
    length: Int,
    focusRequester: FocusRequester,
    windowSizeClass: WindowSizeClass,
    primaryButtonText: String,
    secondaryButtonText: String?,
    primaryButtonEnabled: Boolean,
    showPrimaryButtonIcon: Boolean,
    onValueChanged: (String) -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            segmentCount = length,
            initialValue = null,
            supportingTextData = null,
            segmentedShellType = SegmentedShellType.Numeric,
            onValueChanged = onValueChanged,
        )

        Spacer(modifier = Modifier.height(Spacing.Spacing32))

        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact ->
                VerticalButtonBlock(
                    primaryButton = {
                        PrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = primaryButtonEnabled,
                            buttonText = primaryButtonText,
                            showIcon = showPrimaryButtonIcon,
                            onClick = onPrimaryClick,
                        )
                    },
                    secondaryButton =
                        secondaryButtonText?.let { buttonText ->
                            {
                                SecondaryButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    buttonText = buttonText,
                                    onClick = onSecondaryClick,
                                )
                            }
                        },
                )

            else ->
                HorizontalButtonBlock(
                    primaryButton = {
                        PrimaryButton(
                            modifier = Modifier.weight(1f),
                            enabled = primaryButtonEnabled,
                            buttonText = primaryButtonText,
                            showIcon = showPrimaryButtonIcon,
                            onClick = onPrimaryClick,
                        )
                    },
                    secondaryButton =
                        secondaryButtonText?.let { buttonText ->
                            {
                                SecondaryButton(
                                    modifier = Modifier.weight(1f),
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
private fun PrimaryButton(
    modifier: Modifier,
    enabled: Boolean,
    buttonText: String,
    showIcon: Boolean,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        style = ButtonStyle.FILLED,
        text = buttonText,
        icon =
            if (showIcon) {
                {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = null,
                    )
                }
            } else {
                null
            },
        onClick = onClick,
    )
}

@Composable
private fun SecondaryButton(
    modifier: Modifier,
    buttonText: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        style = ButtonStyle.OUTLINED,
        text = buttonText,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OfflineCredentialEnterPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))
    DHIS2Theme {
        OfflineCredentialContent(
            title = "Enter your offline PIN",
            subtitle = "Enter your 4-digit PIN to log in offline.",
            primaryButtonText = "Log in",
            secondaryButtonText = "Forgot your PIN?",
            primaryButtonEnabled = true,
            showPrimaryButtonIcon = false,
            length = 4,
            isLandscape = false,
            windowSizeClass = windowSizeClass,
            onValueChanged = {},
            onPrimaryClick = {},
            onSecondaryClick = {},
            onDismiss = {},
        )
    }
}
