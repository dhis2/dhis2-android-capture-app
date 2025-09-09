package org.dhis2.usescases.settings.models

import org.dhis2.usescases.settings.GatewayValidator
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

data class SMSSettingsViewModel(
    val isEnabled: Boolean,
    val gatewayNumber: String,
    val responseNumber: String,
    val responseTimeout: Int,
    val isGatewayNumberEditable: Boolean,
    val isResponseNumberEditable: Boolean,
    val waitingForResponse: Boolean,
    val gatewayValidationResult: GatewayValidator.GatewayValidationResult,
    val resultSenderValidationResult: GatewayValidator.GatewayValidationResult,
) {
    fun gatewayState(
        gatewayValidation: GatewayValidator.GatewayValidationResult,
        smsSettings: SMSSettingsViewModel,
    ) = when {
        gatewayValidation != GatewayValidator.GatewayValidationResult.Valid -> InputShellState.ERROR
        smsSettings.isGatewayNumberEditable -> InputShellState.FOCUSED
        else -> InputShellState.DISABLED
    }

    fun timeoutState() =
        if (isGatewayNumberEditable) {
            InputShellState.FOCUSED
        } else {
            InputShellState.DISABLED
        }

    fun enableSmsState(gatewayNumber: String) =
        if (isGatewayNumberEditable && gatewayNumber.isNotEmpty()) {
            InputShellState.FOCUSED
        } else {
            InputShellState.DISABLED
        }

    fun responseState() =
        if (isResponseNumberEditable) {
            InputShellState.FOCUSED
        } else {
            InputShellState.DISABLED
        }

    fun waitForResponseState(resultSender: String) =
        if (isResponseNumberEditable && resultSender.isNotEmpty()) {
            InputShellState.FOCUSED
        } else {
            InputShellState.DISABLED
        }
}
