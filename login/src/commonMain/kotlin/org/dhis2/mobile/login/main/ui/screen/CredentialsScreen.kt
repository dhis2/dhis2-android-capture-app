package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import org.dhis2.mobile.commons.resources.getDrawableResource
import org.dhis2.mobile.login.main.ui.components.TaskExecutorButton
import org.dhis2.mobile.login.main.ui.state.AfterLoginAction
import org.dhis2.mobile.login.main.ui.state.CredentialsAction
import org.dhis2.mobile.login.main.ui.state.CredentialsUpdate
import org.dhis2.mobile.login.main.ui.state.LoginState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope)
fun CredentialsScreen(
    selectedServer: String,
    selectedServerName: String?,
    selectedUsername: String?,
    selectedServerFlag: String?,
    allowRecovery: Boolean,
    oidcInfo: OidcInfo?,
    fromHome: Boolean,
) {
    val context = LocalPlatformContext.current

    val viewModel =
        koinViewModel<CredentialsViewModel> {
            parametersOf(
                selectedServerName,
                selectedServer,
                selectedUsername,
                allowRecovery,
                oidcInfo,
                context,
                fromHome,
            )
        }

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

    LaunchedEffect(screenState.displayBiometricsDialog) {
        if (screenState.displayBiometricsDialog) {
            with(context) {
                viewModel.onBiometricsClicked()
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(Spacing.Spacing16),
        verticalArrangement = spacedBy(Spacing.Spacing24),
    ) {
        with(sharedTransitionScope) {
            ServerInfo(
                modifier =
                    Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "$selectedUsername@$selectedServer"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                serverName = selectedServerName,
                serverUrl = selectedServer,
                selectedUsername = selectedUsername,
                serverImageUrl = selectedServerFlag,
            )
        }
        CredentialsContainer(
            availableUsernames = screenState.credentialsInfo.availableUsernames,
            username = screenState.credentialsInfo.username,
            password = screenState.credentialsInfo.password,
            isUsernameEditable = screenState.credentialsInfo.usernameCanBeEdited,
            isLoggingIn = isLoggingIn,
            onCredentialsUpdate = { credentialsUpdate ->
                when (credentialsUpdate) {
                    CredentialsUpdate.Complete ->
                        viewModel.onLoginClicked()

                    is CredentialsUpdate.Password ->
                        viewModel.updatePassword(credentialsUpdate.password)

                    is CredentialsUpdate.Username ->
                        viewModel.updateUsername(credentialsUpdate.username)
                }
            },
        )
        LoginStatus(
            isLoggingIn = isLoggingIn,
            loginErrorMessage = screenState.errorMessage,
            onCancelLogin = viewModel::cancelLogin,
        )
        if (isLoggingIn.not()) {
            CredentialActions(
                modifier = Modifier.weight(1f),
                allowRecovery = screenState.allowRecovery,
                oidcInfo = screenState.oidcInfo,
                hasBiometrics = screenState.canUseBiometrics,
                canLogin = screenState.loginState == LoginState.Enabled,
                onCredentialsAction = { credentialsAction ->
                    handleCredentialAction(viewModel, context, credentialsAction)
                },
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

private fun handleCredentialAction(
    viewModel: CredentialsViewModel,
    context: PlatformContext,
    credentialsAction: CredentialsAction,
) {
    when (credentialsAction) {
        CredentialsAction.OnBiometricsClicked ->
            with(context) {
                viewModel.onBiometricsClicked()
            }

        CredentialsAction.OnLoginClicked ->
            viewModel.onLoginClicked()
        CredentialsAction.OnManageAccounts ->
            viewModel.onManageAccountsClicked()

        CredentialsAction.OnOpenIdLogin ->
            viewModel.onOpenIdLogin()
        CredentialsAction.OnRecoverAccount ->
            viewModel.onRecoverAccountClicked()
    }
}

@Composable
private fun ServerInfo(
    modifier: Modifier,
    serverName: String?,
    serverUrl: String,
    selectedUsername: String?,
    serverImageUrl: String?,
) {
    val flag = serverImageUrl?.let { getDrawableResource(it) }

    ListCard(
        modifier = modifier.fillMaxWidth(),
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
    onCredentialsUpdate: (CredentialsUpdate) -> Unit,
) {
    var userNameRange by remember {
        mutableStateOf(TextRange(username.length))
    }

    var usernameTextValue by remember(username) {
        mutableStateOf(
            TextFieldValue(username, userNameRange),
        )
    }

    var passwordTextValue by remember {
        mutableStateOf(
            TextFieldValue(password),
        )
    }

    val areCredentialsComplete by remember(usernameTextValue, passwordTextValue) {
        derivedStateOf {
            usernameTextValue.text.isNotEmpty() && passwordTextValue.text.isNotEmpty()
        }
    }

    var usernameHasFocus by remember {
        mutableStateOf(false)
    }
    var passwordHasFocus by remember {
        mutableStateOf(false)
    }
    val inputShellStateUsername by remember(isLoggingIn, usernameHasFocus) {
        mutableStateOf(getInputState(isLoggingIn, usernameHasFocus))
    }

    val inputShellStatePassword by remember(isLoggingIn, passwordHasFocus) {
        mutableStateOf(getInputState(isLoggingIn, passwordHasFocus))
    }

    val focusManager = LocalFocusManager.current

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
                        state = inputShellStateUsername,
                        inputTextFieldValue = usernameTextValue,
                        autoCompleteList = availableUsernames,
                        autoCompleteItemSelected = {
                            usernameTextValue =
                                it?.let { text -> TextFieldValue(text, TextRange(text.length)) }
                                    ?: TextFieldValue("")
                        },
                        onNextClicked = {
                            if (areCredentialsComplete) {
                                focusManager.clearFocus()
                                onCredentialsUpdate(CredentialsUpdate.Complete)
                            } else {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        },
                        onValueChanged = {
                            usernameTextValue = it ?: TextFieldValue("")
                            userNameRange = it?.selection ?: TextRange(0)
                            onCredentialsUpdate(CredentialsUpdate.Username(usernameTextValue.text))
                        },
                        onFocusChanged = { hasFocus ->
                            usernameHasFocus = hasFocus
                        },
                        imeAction = ImeAction.Next,
                    ),
            )
        }
        InputPassword(
            modifier = Modifier.fillMaxWidth(),
            uiModel =
                InputPasswordModel(
                    title = stringResource(Res.string.password_hint),
                    state = inputShellStatePassword,
                    inputTextFieldValue = passwordTextValue,
                    onNextClicked = {
                        if (areCredentialsComplete) {
                            focusManager.clearFocus()
                            onCredentialsUpdate(CredentialsUpdate.Complete)
                        } else {
                            focusManager.moveFocus(focusDirection = FocusDirection.Previous)
                        }
                    },
                    onValueChanged = {
                        passwordTextValue = it ?: TextFieldValue("")
                        onCredentialsUpdate(CredentialsUpdate.Password(passwordTextValue.text))
                    },
                    onFocusChanged = { hasFocus ->
                        passwordHasFocus = hasFocus
                    },
                    imeAction = ImeAction.Next,
                ),
        )
    }
}

private fun getInputState(
    isLoggingIn: Boolean,
    hasFocus: Boolean,
) = when {
    isLoggingIn -> InputShellState.DISABLED
    hasFocus -> InputShellState.FOCUSED
    else -> InputShellState.UNFOCUSED
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
    modifier: Modifier,
    allowRecovery: Boolean,
    hasBiometrics: Boolean,
    oidcInfo: OidcInfo?,
    canLogin: Boolean,
    hasOtherAccounts: Boolean,
    onCredentialsAction: (CredentialsAction) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(Spacing.Spacing0),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = canLogin,
            text = stringResource(Res.string.action_log_in),
            style = ButtonStyle.FILLED,
            onClick = {
                onCredentialsAction(CredentialsAction.OnLoginClicked)
            },
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
                onClick = {
                    onCredentialsAction(CredentialsAction.OnRecoverAccount)
                },
            )
        }
        if (hasBiometrics) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.Spacing24),
                contentAlignment = Alignment.Center,
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                FilledTonalIconButton(
                    modifier = Modifier.size(Spacing.Spacing48),
                    onClick = {
                        onCredentialsAction(CredentialsAction.OnBiometricsClicked)
                    },
                    interactionSource = interactionSource,
                    colors =
                        IconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp, 40.dp),
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "fingerprint",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
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
                text = oidcInfo.buttonText ?: stringResource(Res.string.action_openid_log_in),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Login,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                style = ButtonStyle.OUTLINED,
                onClick = {
                    onCredentialsAction(CredentialsAction.OnOpenIdLogin)
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
                    onClick = {
                        onCredentialsAction(CredentialsAction.OnManageAccounts)
                    },
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
                            onPermissionResult(false)
                        },
                    )
                },
                secondaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.action_yes),
                        onClick = {
                            onPermissionResult(true)
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
                            onPermissionResult(false)
                        },
                    )
                },
                secondaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.FILLED,
                        text = stringResource(Res.string.action_yes),
                        onClick = {
                            onPermissionResult(true)
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
