// kotlin
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.main.ui.contracts.serverQrReader
import org.dhis2.mobile.login.main.ui.state.ServerValidationUiState
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.action_next
import org.dhis2.mobile.login.resources.server_url_title
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
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource

const val SERVER_VALIDATION_CONTENT_BUTTON_TAG = "ServerValidationContentButtonTag"

@Composable
internal fun ServerValidationContent(
    availableServers: List<String>,
    state: ServerValidationUiState,
    onValidate: (String) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.server_verification_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.size(Spacing.Spacing16))

        var server by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(state.currentServer))
        }
        LaunchedEffect(state.currentServer) {
            if (state.currentServer != server.text) {
                server = TextFieldValue(state.currentServer, TextRange(state.currentServer.length))
            }
        }

        var inputFocusState by remember(state.error, state.validationRunning) {
            mutableStateOf(
                when {
                    state.validationRunning -> InputShellState.DISABLED
                    state.error != null -> InputShellState.ERROR
                    else -> InputShellState.UNFOCUSED
                },
            )
        }

        val qrReader =
            serverQrReader { serverUrl ->
                server =
                    server.copy(
                        text = serverUrl ?: "",
                        selection = TextRange(serverUrl?.length ?: 0),
                    )
            }
        val focusManager = LocalFocusManager.current

        val shouldClearFocus = remember { mutableStateOf(false) }

        InputQRCode(
            title = stringResource(Res.string.server_url_title),
            state = inputFocusState,
            supportingText =
                state.error
                    ?.takeIf { inputFocusState == InputShellState.ERROR }
                    ?.let {
                        listOf(
                            SupportingTextData(
                                text = it,
                                state = SupportingTextState.ERROR,
                            ),
                        )
                    },
            onQRButtonClicked = qrReader::launch,
            inputTextFieldValue = server,
            autoCompleteList = availableServers,
            displayQRCapturedIcon = false,
            autoCompleteItemSelected = { selectedServer ->
                selectedServer?.let { server = TextFieldValue(it, TextRange(it.length)) }
                shouldClearFocus.value = true
            },
            onValueChanged = {
                server = it ?: TextFieldValue()
                inputFocusState = InputShellState.FOCUSED
            },
            imeAction = ImeAction.Done,
            onNextClicked = {
                onValidate(server.text)
                inputFocusState = InputShellState.UNFOCUSED
            },
            onFocusChanged = { focused ->
                inputFocusState =
                    if (focused) InputShellState.FOCUSED else InputShellState.UNFOCUSED
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (shouldClearFocus.value) {
            LaunchedEffect(Unit) {
                focusManager.clearFocus()
                shouldClearFocus.value = false
            }
        }

        Spacer(Modifier.size(Spacing.Spacing24))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            horizontalArrangement = spacedBy(Spacing.Spacing16),
        ) {
            val enabled = server.text.isNotEmpty() && !state.validationRunning
            Button(
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(SERVER_VALIDATION_CONTENT_BUTTON_TAG),
                enabled = enabled,
                style = ButtonStyle.FILLED,
                icon = {
                    if (state.validationRunning) {
                        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Check login flow button",
                            tint = if (enabled) MaterialTheme.colorScheme.onPrimary else TextColor.OnDisabledSurface,
                        )
                    }
                },
                text =
                    if (state.validationRunning) {
                        stringResource(Res.string.server_verification_running)
                    } else {
                        stringResource(Res.string.action_next)
                    },
                onClick = { onValidate(server.text) },
            )

            AnimatedVisibility(visible = state.validationRunning) {
                IconButton(
                    style = IconButtonStyle.TONAL,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                        )
                    },
                    onClick = onCancel,
                )
            }
        }
    }
}
