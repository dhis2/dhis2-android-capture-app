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
        data class Enable(val smsGateway: String, val timeout: Int) : SmsSetting
        data object Disable : SmsSetting
    }

    sealed interface EnableSmsResult {
        data object Success : EnableSmsResult
        data object Error : EnableSmsResult
        data class ValidationError(val validationResult: GatewayValidator.GatewayValidationResult) :
            EnableSmsResult
    }

    suspend operator fun invoke(smsSetting: SmsSetting): EnableSmsResult {
        return when (smsSetting) {
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
        }
    }

    private suspend fun updateSmsModule(enableSms: Boolean): EnableSmsResult {
        return try {
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
}
