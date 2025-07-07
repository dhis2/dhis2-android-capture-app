package org.dhis2.usescases.settings.models

import org.dhis2.usescases.settings.GatewayValidator

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
)
