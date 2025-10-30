package org.dhis2.usescases.settings.domain

import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.SettingsRepository

class UpdateSmsResponse(
    private val settingsRepository: SettingsRepository,
    private val gatewayValidator: GatewayValidator,
) {
    sealed interface ResponseSetting {
        data class Enable(
            val resultSender: String,
        ) : ResponseSetting

        data object Disable : ResponseSetting
    }

    sealed interface UpdateSmsResponseResult {
        data object Success : UpdateSmsResponseResult

        data class ValidationError(
            val validationResult: GatewayValidator.GatewayValidationResult,
        ) : UpdateSmsResponseResult
    }

    suspend operator fun invoke(enableWaitForResponse: ResponseSetting): UpdateSmsResponseResult =
        when (enableWaitForResponse) {
            ResponseSetting.Disable -> {
                settingsRepository.saveWaitForSmsResponse(false)
                UpdateSmsResponseResult.Success
            }

            is ResponseSetting.Enable -> {
                when (val validation = gatewayValidator(enableWaitForResponse.resultSender)) {
                    GatewayValidator.GatewayValidationResult.Empty,
                    GatewayValidator.GatewayValidationResult.Invalid,
                    ->
                        UpdateSmsResponseResult.ValidationError(validation)

                    GatewayValidator.GatewayValidationResult.Valid -> {
                        settingsRepository.saveSmsResultSender(enableWaitForResponse.resultSender)
                        settingsRepository.saveWaitForSmsResponse(true)
                        UpdateSmsResponseResult.Success
                    }
                }
            }
        }
}
