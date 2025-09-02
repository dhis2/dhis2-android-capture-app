package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.dhis2.R
import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.InputPhoneNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

@Composable
internal fun SMSSettingItem(
    smsSettings: SMSSettingsViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    saveGatewayNumber: (gatewayNumber: String) -> Unit,
    saveTimeout: (timeout: Int) -> Unit,
    enableSms: (gatewayNumber: String, timeout: Int) -> Unit,
    disableSms: () -> Unit,
    saveResultSender: (resultSender: String) -> Unit,
    enableWaitForResponse: (resultSenderNumber: String) -> Unit,
    disableWaitForResponse: () -> Unit,
) {
    var gatewayNumber by remember(smsSettings) {
        mutableStateOf(smsSettings.gatewayNumber)
    }

    var resultTimeout by remember(smsSettings) {
        mutableIntStateOf(smsSettings.responseTimeout)
    }

    var smsEnabled by remember(smsSettings) {
        mutableStateOf(smsSettings.isEnabled)
    }

    var resultSender by remember(smsSettings) {
        mutableStateOf(smsSettings.responseNumber)
    }

    var waitForResponse by remember(smsSettings) {
        mutableStateOf(smsSettings.waitingForResponse)
    }

    var gatewayValidation by remember(smsSettings) {
        mutableStateOf(smsSettings.gatewayValidationResult)
    }

    var gateWayState by remember(smsSettings, gatewayValidation) {
        mutableStateOf(
            smsSettings.gatewayState(gatewayValidation, smsSettings),
        )
    }

    var gateWayTimeLeft by remember(smsSettings) { mutableIntStateOf(-1) }
    var timeoutTimeLeft by remember(smsSettings) { mutableIntStateOf(-1) }
    var resultSenderTimeLeft by remember(smsSettings) { mutableIntStateOf(-1) }

    LaunchedEffect(gateWayTimeLeft) {
        if (gateWayTimeLeft < 0) return@LaunchedEffect
        while (gateWayTimeLeft > 0) {
            delay(1000L)
            gateWayTimeLeft--
        }
        saveGatewayNumber(gatewayNumber)
    }

    LaunchedEffect(timeoutTimeLeft) {
        if (timeoutTimeLeft < 0) return@LaunchedEffect
        while (timeoutTimeLeft > 0) {
            delay(1000L)
            timeoutTimeLeft--
        }
        saveTimeout(resultTimeout)
    }

    LaunchedEffect(resultSenderTimeLeft) {
        if (resultSenderTimeLeft < 0) return@LaunchedEffect
        while (resultSenderTimeLeft > 0) {
            delay(1000L)
            resultSenderTimeLeft--
        }
        saveResultSender(resultSender)
    }

    SettingItem(
        title = stringResource(id = R.string.settingsSms),
        subtitle = stringResource(R.string.settingsSms_descr),
        icon = Icons.Outlined.Sms,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                InputPhoneNumber(
                    title = stringResource(R.string.settings_sms_receiver_number),
                    onCallActionClicked = {},
                    state = gateWayState,
                    inputTextFieldValue =
                        TextFieldValue(
                            text = gatewayNumber,
                            selection = TextRange(gatewayNumber.length),
                        ),
                    onValueChanged = {
                        gateWayTimeLeft = 3
                        gatewayValidation = GatewayValidator.GatewayValidationResult.Valid
                        gatewayNumber = it?.text ?: ""
                    },
                    imeAction = ImeAction.Done,
                    supportingText = phoneNumberValidationMessage(gatewayValidation),
                )
                InputPositiveIntegerOrZero(
                    title = stringResource(R.string.settings_sms_result_timeout),
                    state = smsSettings.timeoutState(),
                    inputTextFieldValue =
                        TextFieldValue(
                            text = resultTimeout.toString(),
                            selection = TextRange(resultTimeout.toString().length),
                        ),
                    onValueChanged = {
                        timeoutTimeLeft = 3
                        resultTimeout = it?.text?.toIntOrNull() ?: 0
                    },
                    imeAction = ImeAction.Done,
                )
                InputYesOnlySwitch(
                    title = stringResource(R.string.settings_sms_module_switch),
                    state = smsSettings.enableSmsState(gatewayNumber),
                    isChecked = smsEnabled,
                    onClick = {
                        if (smsEnabled) {
                            disableSms()
                        } else {
                            enableSms(gatewayNumber, resultTimeout)
                        }
                        smsEnabled = !smsEnabled
                    },
                )
                InputPhoneNumber(
                    title = stringResource(R.string.settings_sms_result_sender_number),
                    onCallActionClicked = {},
                    state = smsSettings.responseState(),
                    inputTextFieldValue =
                        TextFieldValue(
                            text = resultSender,
                            selection = TextRange(resultSender.length),
                        ),
                    onValueChanged = {
                        resultSenderTimeLeft = 3
                        resultSender = it?.text ?: ""
                    },
                    imeAction = ImeAction.Done,
                    supportingText = phoneNumberValidationMessage(smsSettings.resultSenderValidationResult),
                )
                InputYesOnlySwitch(
                    title = stringResource(R.string.settings_sms_response_wait_switch),
                    state = smsSettings.waitForResponseState(resultSender),
                    isChecked = waitForResponse,
                    onClick = {
                        if (waitForResponse) {
                            disableWaitForResponse()
                        } else {
                            enableWaitForResponse(resultSender)
                        }
                        waitForResponse = !smsSettings.waitingForResponse
                    },
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun phoneNumberValidationMessage(validation: GatewayValidator.GatewayValidationResult) =
    when (validation) {
        GatewayValidator.GatewayValidationResult.Empty ->
            listOf(
                SupportingTextData(
                    text = stringResource(R.string.sms_empty_gateway),
                    state = SupportingTextState.ERROR,
                ),
            )

        GatewayValidator.GatewayValidationResult.Invalid ->
            listOf(
                SupportingTextData(
                    text = stringResource(R.string.invalid_phone_number),
                    state = SupportingTextState.ERROR,
                ),
            )

        GatewayValidator.GatewayValidationResult.Valid -> null
    }
