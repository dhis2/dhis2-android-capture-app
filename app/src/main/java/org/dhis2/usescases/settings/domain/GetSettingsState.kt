package org.dhis2.usescases.settings.domain

import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.SettingsState

class GetSettingsState(
    private val settingsRepository: SettingsRepository,
    private val gatewayValidator: GatewayValidator,
) {
    suspend operator fun invoke(
        openedItem: SettingItem?,
        hasConnection: Boolean,
        metadataSyncInProgress: Boolean,
        dataSyncInProgress: Boolean,
    ): SettingsState =
        SettingsState(
            openedItem = openedItem,
            hasConnection = hasConnection,
            metadataSettingsViewModel =
                settingsRepository.metaSync().blockingGet().copy(
                    syncInProgress = metadataSyncInProgress,
                ),
            dataSettingsViewModel =
                settingsRepository.dataSync().blockingGet().copy(
                    syncInProgress = dataSyncInProgress,
                ),
            syncParametersViewModel = settingsRepository.syncParameters().blockingGet(),
            reservedValueSettingsViewModel = settingsRepository.reservedValues().blockingGet(),
            smsSettingsViewModel =
                with(settingsRepository.sms().blockingGet()) {
                    copy(
                        gatewayValidationResult = gatewayValidator(this.gatewayNumber),
                    )
                },
            isTwoFAConfigured = settingsRepository.isTwoFAConfigured(),
            versionName = settingsRepository.getVersionName(),
        )
}
