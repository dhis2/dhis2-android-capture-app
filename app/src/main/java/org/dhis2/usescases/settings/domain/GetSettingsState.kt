package org.dhis2.usescases.settings.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.usescases.settings.models.SyncStateInput

class GetSettingsState(
    private val settingsRepository: SettingsRepository,
    private val gatewayValidator: GatewayValidator,
) : UseCase<SyncStateInput, SettingsState> {
    override suspend fun invoke(input: SyncStateInput) =
        try {
            val state =
                SettingsState(
                    openedItem = input.openedItem,
                    hasConnection = input.hasConnection,
                    metadataSettingsViewModel =
                        settingsRepository.metaSync().blockingGet().copy(
                            syncInProgress = input.metadataSyncInProgress,
                        ),
                    dataSettingsViewModel =
                        settingsRepository.dataSync().blockingGet().copy(
                            syncInProgress = input.dataSyncInProgress,
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
            Result.success(state)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
