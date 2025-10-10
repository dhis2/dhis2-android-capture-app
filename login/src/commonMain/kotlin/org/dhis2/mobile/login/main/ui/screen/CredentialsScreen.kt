package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import coil3.compose.LocalPlatformContext
import org.dhis2.mobile.commons.resources.getDrawableResource
import org.dhis2.mobile.login.main.ui.components.TaskExecutorButton
import org.dhis2.mobile.login.main.ui.states.AfterLoginAction
import org.dhis2.mobile.login.main.ui.states.LoginState
import org.dhis2.mobile.login.main.ui.states.OidcInfo
import org.dhis2.mobile.login.main.ui.viewmodel.CredentialsViewModel
import org.dhis2.mobile.login.pin.ui.components.PinBottomSheet
import org.dhis2.mobile.login.pin.ui.components.PinMode
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.action_log_in
import org.dhis2.mobile.login.resources.action_manage_account
import org.dhis2.mobile.login.resources.action_no_now
import org.dhis2.mobile.login.resources.action_openid_log_in
import org.dhis2.mobile.login.resources.action_recover_account
import org.dhis2.mobile.login.resources.action_yes
import org.dhis2.mobile.login.resources.biometrics_login_text
import org.dhis2.mobile.login.resources.biometrics_login_title
import org.dhis2.mobile.login.resources.logging_in
import org.dhis2.mobile.login.resources.openid_or
import org.dhis2.mobile.login.resources.password_hint
import org.dhis2.mobile.login.resources.privacy_policy
import org.dhis2.mobile.login.resources.tracking_description
import org.dhis2.mobile.login.resources.tracking_description_link
import org.dhis2.mobile.login.resources.tracking_title
import org.dhis2.mobile.login.resources.username_hint
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InputPassword
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputUser
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.InputPasswordModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.InputUserModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CredentialsScreen(
    selectedServer: String,
    selectedServerName: String?,
    selectedUsername: String?,
    selectedServerFlag: String?,
    allowRecovery: Boolean,
) {
    val viewModel =
        koinViewModel<CredentialsViewModel> {
            parametersOf(selectedServerName, selectedServer, selectedUsername, allowRecovery)
        }
    val context = LocalPlatformContext.current
    val screenState by viewModel.credentialsScreenState.collectAsState()
    val displayBiometricMessage by remember(screenState) {
        derivedStateOf {
            screenState.afterLoginActions.firstOrNull() is AfterLoginAction.DisplayBiometricsMessage
        }
    }
    val displayTrackingMessage by remember {
        derivedStateOf {
            screenState.afterLoginActions.firstOrNull() is AfterLoginAction.DisplayTrackingMessage
        }
    }

    val isLoggingIn by remember(screenState) {
        derivedStateOf {
            screenState.loginState == LoginState.Running
        }
    }

    LaunchedEffect(screenState) {
        val action = screenState.afterLoginActions.firstOrNull()
        if (action is AfterLoginAction.NavigateToNextScreen) {
            viewModel.goToNextScreen(action.initialSyncDone)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(Spacing.Spacing16),
        verticalArrangement = spacedBy(Spacing.Spacing24),
    ) {
        ServerInfo(
            serverName = selectedServerName,
            serverUrl = selectedServer,
            selectedUsername = selectedUsername,
            serverImageUrl = selectedServerFlag,
        )
        CredentialsContainer(
            availableUsernames = screenState.credentialsInfo.availableUsernames,
            username = screenState.credentialsInfo.username,
            password = screenState.credentialsInfo.password,
            isUsernameEditable = screenState.credentialsInfo.usernameCanBeEdited,
            isLoggingIn = isLoggingIn,
            onUserNameChanged = {
                viewModel.updateUsername(it)
            },
            onPasswordChanged = {
                viewModel.updatePassword(it)
            },
        )
        LoginStatus(
            isLoggingIn = isLoggingIn,
            loginErrorMessage = screenState.errorMessage,
            onCancelLogin = viewModel::cancelLogin,
        )
        if (isLoggingIn.not()) {
            CredentialActions(
                allowRecovery = screenState.allowRecovery,
                oidcInfo = screenState.oidcInfo,
                hasBiometrics = screenState.canUseBiometrics,
                canLogin = screenState.loginState == LoginState.Enabled,
                onLoginClicked = viewModel::onLoginClicked,
                onOpenIdLogin = viewModel::onOpenIdLogin,
                onBiometricsClicked = {
                    with(context) {
                        viewModel.onBiometricsClicked()
                    }
                },
                onManageAccounts = viewModel::onManageAccountsClicked,
                onRecoverAccount = viewModel::onRecoverAccountClicked,
                hasOtherAccounts = screenState.hasOtherAccounts,
            )
        }
    }
    if (displayTrackingMessage) {
        TrackingPermissionDialog(
            onPermissionResult = viewModel::onTrackingPermission,
            onOpenPrivacyPolicy = viewModel::checkPrivacyPolicy,
        )
    } else if (displayBiometricMessage) {
        BiometricsDialog(
            onPermissionResult = { granted ->
                with(context) {
                    viewModel.onEnableBiometrics(granted)
                }
            },
        )
    }

    if (screenState.isSessionLocked) {
        PinBottomSheet(
            mode = PinMode.ASK,
            onSuccess = {
                viewModel.onPinUnlocked()
            },
            onDismiss = {
                viewModel.onPinDismissed()
            },
        )
    }
}

@Composable
private fun ServerInfo(
    serverName: String?,
    serverUrl: String,
    selectedUsername: String?,
    serverImageUrl: String?,
) {
    val flag = serverImageUrl?.let { getDrawableResource(it) }

    ListCard(
        modifier = Modifier.fillMaxWidth(),
        listCardState =
            rememberListCardState(
                title =
                    ListCardTitleModel(
                        text = serverName ?: "-",
                    ),
                description =
                    ListCardDescriptionModel(
                        text = serverUrl,
                    ),
                additionalInfoColumnState =
                    rememberAdditionalInfoColumnState(
                        additionalInfoList =
                            selectedUsername?.let {
                                listOf(
                                    AdditionalInfoItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = "",
                                                tint = TextColor.OnSurfaceLight,
                                            )
                                        },
                                        value = it,
                                    ),
                                )
                            } ?: emptyList(),
                        syncProgressItem =
                            AdditionalInfoItem(
                                value = "",
                            ),
                    ),
            ),
        listAvatar = {
            Avatar(
                style =
                    flag?.let { painter ->
                        AvatarStyleData.Image(painter)
                    } ?: run {
                        AvatarStyleData.Text(serverName?.first().toString())
                    },
            )
        },
        onCardClick = { },
    )
}

@Composable
private fun CredentialsContainer(
    username: String,
    password: String,
    availableUsernames: List<String>,
    isUsernameEditable: Boolean,
    isLoggingIn: Boolean,
    onUserNameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
) {
    var usernameTextValue by remember(username) {
        mutableStateOf(
            TextFieldValue(username, TextRange(username.length)),
        )
    }

    var passwordTextValue by remember(password) {
        mutableStateOf(
            TextFieldValue(password),
        )
    }

    val areCredentialsComplete by remember(username, password) {
        derivedStateOf {
            username.isNotBlank() && password.isNotBlank()
        }
    }

    val inputShellState by remember(isLoggingIn) {
        mutableStateOf(
            when {
                isLoggingIn -> InputShellState.DISABLED
                else -> InputShellState.UNFOCUSED
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(Spacing.Spacing8),
    ) {
        if (isUsernameEditable) {
            InputUser(
                modifier = Modifier.fillMaxWidth(),
                uiModel =
                    InputUserModel(
                        title = stringResource(Res.string.username_hint),
                        state = inputShellState,
                        inputTextFieldValue = usernameTextValue,
                        autoCompleteList = availableUsernames,
                        autoCompleteItemSelected = {
                            usernameTextValue =
                                it?.let { text -> TextFieldValue(text, TextRange(text.length)) }
                                    ?: TextFieldValue("")
                        },
                        onNextClicked = {
                        },
                        onValueChanged = {
                            usernameTextValue = it ?: TextFieldValue("")
                            onUserNameChanged(usernameTextValue.text)
                        },
                        onFocusChanged = {
                        },
                        imeAction = if (areCredentialsComplete) ImeAction.Done else ImeAction.Next,
                    ),
            )
        }
        InputPassword(
            modifier = Modifier.fillMaxWidth(),
            uiModel =
                InputPasswordModel(
                    title = stringResource(Res.string.password_hint),
                    state = inputShellState,
                    inputTextFieldValue = passwordTextValue,
                    onNextClicked = {},
                    onValueChanged = {
                        passwordTextValue = it ?: TextFieldValue("")
                        onPasswordChanged(passwordTextValue.text)
                    },
                    onFocusChanged = {},
                    imeAction = if (areCredentialsComplete) ImeAction.Done else ImeAction.Next,
                ),
        )
    }
}

@Composable
private fun LoginStatus(
    isLoggingIn: Boolean,
    loginErrorMessage: String?,
    onCancelLogin: () -> Unit,
) {
    if (isLoggingIn) {
        TaskExecutorButton(
            modifier = Modifier,
            taskRunning = true,
            actionText = "",
            taskRunningText = stringResource(Res.string.logging_in),
            icon = {},
            onClick = {},
            onCancel = onCancelLogin,
        )
    } else if (loginErrorMessage != null) {
        InfoBar(
            modifier = Modifier,
            text = loginErrorMessage,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            },
        )
    }
}

@Composable
private fun CredentialActions(
    allowRecovery: Boolean,
    hasBiometrics: Boolean,
    oidcInfo: OidcInfo?,
    canLogin: Boolean,
    hasOtherAccounts: Boolean,
    onLoginClicked: () -> Unit,
    onOpenIdLogin: (url: String) -> Unit,
    onBiometricsClicked: () -> Unit,
    onManageAccounts: () -> Unit,
    onRecoverAccount: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = spacedBy(Spacing.Spacing8),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = canLogin,
            text = stringResource(Res.string.action_log_in),
            style = ButtonStyle.FILLED,
            onClick = onLoginClicked,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Login,
                    contentDescription = "Log in icon",
                )
            },
        )
        if (allowRecovery) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_recover_account),
                style = ButtonStyle.TONAL,
                onClick = onRecoverAccount,
            )
        }
        if (hasBiometrics) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.Spacing24),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    style = IconButtonStyle.STANDARD,
                    icon = {
                        Icon(
                            modifier = Modifier.size(Spacing.Spacing48),
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "fingerprint",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = onBiometricsClicked,
                )
            }
        }
        if (oidcInfo != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.Spacing16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(Spacing.Spacing16),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(stringResource(Res.string.openid_or))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = oidcInfo.oidcLoginText ?: stringResource(Res.string.action_openid_log_in),
                icon =
                    oidcInfo.oidcIcon?.let {
                        {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Login,
                                contentDescription = "",
                                tint = Color.Unspecified,
                            )
                        }
                    },
                style = ButtonStyle.OUTLINED,
                onClick = {
                    oidcInfo.oidcUrl?.let { onOpenIdLogin(it) }
                },
            )
        }
        if (hasOtherAccounts) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.action_manage_account),
                    style = ButtonStyle.OUTLINED,
                    onClick = onManageAccounts,
                )
            }
        }
    }
}

@Composable
fun TrackingPermissionDialog(
    onPermissionResult: (granted: Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    BottomSheetShell(
        modifier = Modifier,
        uiState =
            BottomSheetShellUIState(
                title = stringResource(Res.string.tracking_title),
                showTopSectionDivider = false,
                showBottomSectionDivider = true,
                headerTextAlignment = TextAlign.Start,
            ),
        content = {
            Text(
                text =
                    buildAnnotatedString {
                        append(stringResource(Res.string.tracking_description))
                        append("\n\n")
                        append(stringResource(Res.string.tracking_description_link))
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = stringResource(Res.string.privacy_policy),
                                styles =
                                    TextLinkStyles(
                                        style =
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                            ),
                                    ),
                                linkInteractionListener = {
                                    onOpenPrivacyPolicy()
                                },
                            ),
                        ) {
                            append(stringResource(Res.string.privacy_policy))
                        }
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = TextColor.OnSurfaceLight,
            )
        },
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        buttonBlock = {
            ButtonBlock(
                modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                primaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.OUTLINED,
                        text = stringResource(Res.string.action_no_now),
                        onClick = {
                            onPermissionResult(true)
                        },
                    )
                },
                secondaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.action_yes),
                        onClick = {
                            onPermissionResult(false)
                        },
                    )
                },
            )
        },
        onDismiss = {
            onPermissionResult(false)
        },
    )
}

@Composable
private fun BiometricsDialog(onPermissionResult: (granted: Boolean) -> Unit) {
    BottomSheetShell(
        modifier = Modifier,
        uiState =
            BottomSheetShellUIState(
                title = stringResource(Res.string.biometrics_login_title),
                description = stringResource(Res.string.biometrics_login_text),
                showTopSectionDivider = true,
                showBottomSectionDivider = true,
                headerTextAlignment = TextAlign.Start,
            ),
        content = null,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        buttonBlock = {
            ButtonBlock(
                modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                primaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.OUTLINED,
                        text = stringResource(Res.string.action_no_now),
                        onClick = {
                            onPermissionResult(true)
                        },
                    )
                },
                secondaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.action_yes),
                        onClick = {
                            onPermissionResult(false)
                        },
                    )
                },
            )
        },
        onDismiss = {
            onPermissionResult(false)
        },
    )
}
