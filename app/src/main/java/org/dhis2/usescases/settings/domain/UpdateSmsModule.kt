package org.dhis2.usescases.settings.domain

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.domain.UpdateSmsModule.EnableSmsResult.Error
import org.dhis2.usescases.settings.domain.UpdateSmsModule.EnableSmsResult.Success
import org.dhis2.usescases.settings.domain.UpdateSmsModule.EnableSmsResult.ValidationError
import timber.log.Timber

class UpdateSmsModule(
    private val settingsRepository: SettingsRepository,
    private val gatewayValidator: GatewayValidator,
    private val settingsMessages: SettingsMessages,
    private val resourceManager: ResourceManager,
) {
    sealed interface SmsSetting {
        data class SaveGatewayNumber(
            val smsGateway: String,
        ) : SmsSetting

        data class SaveTimeout(
            val timeout: Int,
        ) : SmsSetting

        data class SaveResultNumber(
            val resultNumber: String,
        ) : SmsSetting

        data class Enable(
            val smsGateway: String,
            val timeout: Int,
        ) : SmsSetting

        data object Disable : SmsSetting
    }

    sealed interface EnableSmsResult {
        data object Success : EnableSmsResult

        data object Error : EnableSmsResult

        data class ValidationError(
            val validationResult: GatewayValidator.GatewayValidationResult,
        ) : EnableSmsResult
    }

    suspend operator fun invoke(smsSetting: SmsSetting): EnableSmsResult =
        when (smsSetting) {
            is SmsSetting.Enable -> {
                when (val validation = gatewayValidator(smsSetting.smsGateway)) {
                    GatewayValidator.GatewayValidationResult.Empty,
                    GatewayValidator.GatewayValidationResult.Invalid,
                    -> {
                        ValidationError(validation)
                    }

                    GatewayValidator.GatewayValidationResult.Valid -> {
                        settingsMessages.sendMessage(resourceManager.getString(R.string.sms_downloading_data))
                        settingsRepository.saveGatewayNumber(smsSetting.smsGateway)
                        settingsRepository.saveSmsResponseTimeout(smsSetting.timeout)
                        updateSmsModule(true)
                    }
                }
            }

            SmsSetting.Disable -> {
                updateSmsModule(false)
            }

            is SmsSetting.SaveGatewayNumber ->
                when (val validation = gatewayValidator(smsSetting.smsGateway)) {
                    GatewayValidator.GatewayValidationResult.Invalid ->
                        ValidationError(validation)
                    GatewayValidator.GatewayValidationResult.Empty,
                    GatewayValidator.GatewayValidationResult.Valid,
                    -> {
                        settingsRepository.saveGatewayNumber(smsSetting.smsGateway)
                        settingsMessages.sendMessage("Gateway saved")
                        Success
                    }
                }
            is SmsSetting.SaveResultNumber ->
                when (val validation = gatewayValidator(smsSetting.resultNumber)) {
                    GatewayValidator.GatewayValidationResult.Invalid ->
                        ValidationError(validation)
                    GatewayValidator.GatewayValidationResult.Empty,
                    GatewayValidator.GatewayValidationResult.Valid,
                    -> {
                        settingsRepository.saveGatewayNumber(smsSetting.resultNumber)
                        settingsMessages.sendMessage("Result sender saved")
                        Success
                    }
                }
            is SmsSetting.SaveTimeout -> {
                settingsRepository.saveSmsResponseTimeout(smsSetting.timeout)
                settingsMessages.sendMessage("Timeout updated")
                Success
            }
        }

    private suspend fun updateSmsModule(enableSms: Boolean): EnableSmsResult =
        try {
            settingsRepository.enableSmsModule(enableSms)
            settingsMessages.sendMessage(
                if (enableSms) {
                    resourceManager.getString(R.string.sms_enabled)
                } else {
                    resourceManager.getString(R.string.sms_disabled)
                },
            )
            Success
        } catch (e: Exception) {
            Timber.e(e)
            if (enableSms) {
                settingsMessages.sendMessage(resourceManager.getString(R.string.sms_disabled))
            } else {
                settingsMessages.sendMessage(resourceManager.getString(R.string.sms_enabled))
            }
            Error
        }
}
