package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.ui.contracts.serverQrReader
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.action_next
import org.dhis2.mobile.login.resources.server_verification_running
import org.dhis2.mobile.login.resources.server_verification_title
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputQRCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

const val ServerValidationContentButtonTag = "ServerValidationContentButtonTag"

@Composable
internal fun ServerValidationContent(
    availableServers: List<String>,
) {
    val viewModel: LoginViewModel = koinViewModel()
    val currentScreen by viewModel.currentScreen.collectAsState(null)
    val isServerValidationRunning by remember(currentScreen) {
        derivedStateOf {
            (currentScreen as? LoginScreenState.ServerValidation)?.validationRunning == true
        }
    }
    val errorMessage by remember(currentScreen) {
        derivedStateOf {
            (currentScreen as? LoginScreenState.ServerValidation)?.error
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.server_verification_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.size(Spacing.Spacing16))
        var server by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue((currentScreen as? LoginScreenState.ServerValidation)?.currentServer ?: ""))
        }
        var state by remember(errorMessage, isServerValidationRunning) {
            mutableStateOf(
                when {
                    isServerValidationRunning -> InputShellState.DISABLED
                    errorMessage != null -> InputShellState.ERROR
                    else -> InputShellState.UNFOCUSED
                },
            )
        }
        val qrReader = serverQrReader { serverUrl ->
            server = server.copy(text = serverUrl ?: "")
        }

        InputQRCode(
            title = "Server URL",
            state = state,
            supportingText = errorMessage?.takeIf { state == InputShellState.ERROR }?.let {
                listOf(
                    SupportingTextData(text = it, state = SupportingTextState.ERROR),
                )
            },
            onQRButtonClicked = qrReader::launch,
            inputTextFieldValue = server,
            autoCompleteList = availableServers,
            autoCompleteItemSelected = { selectedServer ->
                selectedServer?.let {
                    server = TextFieldValue(it)
                }
            },
            onValueChanged = {
                server = it ?: TextFieldValue()
                state = InputShellState.FOCUSED
            },
            imeAction = ImeAction.Done,
            onNextClicked = {
                viewModel.onValidateServer(server.text)
                state = InputShellState.UNFOCUSED
            },
            onFocusChanged = {
                state = if (it) InputShellState.FOCUSED else InputShellState.UNFOCUSED
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(Spacing.Spacing24))
        Row(
            modifier = Modifier.fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = spacedBy(Spacing.Spacing16),
        ) {
            Button(
                modifier = Modifier.weight(1f)
                    .testTag(ServerValidationContentButtonTag),
                enabled = server.text.isNotEmpty() && !isServerValidationRunning,
                style = ButtonStyle.FILLED,
                icon = if (server.text.isNotEmpty()) {
                    {
                        if (isServerValidationRunning) {
                            ProgressIndicator(
                                type = ProgressIndicatorType.CIRCULAR_SMALL,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Check login flow button",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                } else {
                    null
                },
                text = if (isServerValidationRunning) {
                    stringResource(Res.string.server_verification_running)
                } else {
                    stringResource(Res.string.action_next)
                },
                onClick = { viewModel.onValidateServer(server.text) },
            )

            AnimatedVisibility(
                visible = isServerValidationRunning,
            ) {
                IconButton(
                    style = IconButtonStyle.TONAL,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                        )
                    },
                    onClick = viewModel::cancelServerValidation,
                )
            }
        }
    }
}
