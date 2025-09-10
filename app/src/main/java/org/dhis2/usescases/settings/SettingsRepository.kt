package org.dhis2.usescases.settings

import io.reactivex.Single
import org.dhis2.BuildConfig
import org.dhis2.bindings.toSeconds
import org.dhis2.commons.Constants
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.DEFAULT_NUMBER_RV
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.commons.prefs.Preference.Companion.NUMBER_RV
import org.dhis2.commons.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.commons.prefs.Preference.Companion.TIME_WEEKLY
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.SyncResult
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.settings.SynchronizationSettings
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase
import timber.log.Timber

class SettingsRepository(
    val d2: D2,
    val prefs: PreferenceProvider,
    val featureConfigRepository: FeatureConfigRepository,
) {
    private val syncSettings: SynchronizationSettings?
        get() =
            if (d2.settingModule().synchronizationSettings().blockingExists()) {
                d2.settingModule().synchronizationSettings().blockingGet()
            } else {
                null
            }
    private val generalSettings: GeneralSettings?
        get() =
            if (d2.settingModule().generalSetting().blockingExists()) {
                d2.settingModule().generalSetting().blockingGet()
            } else {
                null
            }
    private val programSettings: ProgramSettings?
        get() =
            if (d2.settingModule().programSetting().blockingExists()) {
                d2.settingModule().programSetting().blockingGet()
            } else {
                null
            }
    private val smsConfig: ConfigCase.SmsConfig
        get() =
            d2
                .smsModule()
                .configCase()
                .getSmsModuleConfig()
                .blockingGet()

    fun dataSync(): Single<DataSettingsViewModel> =
        Single.just(
            DataSettingsViewModel(
                dataSyncPeriod = dataPeriod(),
                lastDataSync = prefs.getString(Constants.LAST_DATA_SYNC, "-")!!,
                syncHasErrors = !prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true),
                dataHasErrors = dataHasErrors(),
                dataHasWarnings = dataHasWarning(),
                canEdit = syncSettings?.dataSync() == null,
                syncResult =
                    prefs.getString(Constants.SYNC_RESULT, null)?.let {
                        SyncResult.valueOf(it)
                    },
                syncInProgress = false,
            ),
        )

    fun metaSync(): Single<MetadataSettingsViewModel> =
        Single.just(
            MetadataSettingsViewModel(
                metadataSyncPeriod = metadataPeriod(),
                lastMetadataSync = prefs.getString(Constants.LAST_META_SYNC, "-")!!,
                hasErrors = !prefs.getBoolean(Constants.LAST_META_SYNC_STATUS, true),
                canEdit = syncSettings?.metadataSync() == null,
                syncInProgress = false,
            ),
        )

    fun syncParameters(): Single<SyncParametersViewModel> =
        Single.just(
            SyncParametersViewModel(
                teiToDownload(),
                eventsToDownload(),
                currentTeiCount(),
                currentEventCount(),
                limitScope(),
                programSettings?.globalSettings()?.teiDownload() == null,
                programSettings?.globalSettings()?.eventsDownload() == null,
                programSettings?.globalSettings()?.settingDownload() == null,
                programSettings?.specificSettings()?.size ?: 0,
            ),
        )

    fun reservedValues(): Single<ReservedValueSettingsViewModel> =
        Single.just(
            ReservedValueSettingsViewModel(
                generalSettings?.reservedValues() ?: prefs.getInt(
                    NUMBER_RV,
                    DEFAULT_NUMBER_RV,
                ),
                generalSettings?.reservedValues() == null,
            ),
        )

    fun sms(): Single<SMSSettingsViewModel> =
        Single.just(
            SMSSettingsViewModel(
                isEnabled = smsConfig.isModuleEnabled,
                gatewayNumber = smsConfig.gateway,
                responseNumber = smsConfig.resultSender,
                responseTimeout = smsConfig.resultWaitingTimeout,
                isGatewayNumberEditable = generalSettings?.smsGateway() == null,
                isResponseNumberEditable = generalSettings?.smsResultSender() == null,
                waitingForResponse = smsConfig.isWaitingForResult,
                gatewayValidationResult = GatewayValidator.GatewayValidationResult.Valid,
                resultSenderValidationResult = GatewayValidator.GatewayValidationResult.Valid,
            ),
        )

    private fun dataHasErrors(): Boolean =
        d2
            .eventModule()
            .events()
            .byAggregatedSyncState()
            .`in`(State.ERROR)
            .blockingGet()
            .isNotEmpty() ||
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.ERROR)
                .blockingGet()
                .isNotEmpty() ||
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.ERROR)
                .blockingGet()
                .isNotEmpty()

    private fun dataHasWarning(): Boolean =
        d2
            .eventModule()
            .events()
            .byAggregatedSyncState()
            .`in`(State.WARNING)
            .blockingGet()
            .isNotEmpty() ||
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.WARNING)
                .blockingGet()
                .isNotEmpty() ||
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.WARNING)
                .blockingGet()
                .isNotEmpty()

    private fun metadataPeriod(): Int =
        generalSettings?.metadataSync()?.toSeconds() ?: prefs.getInt(
            Preference.TIME_META,
            TIME_WEEKLY,
        )

    private fun dataPeriod(): Int =
        generalSettings?.dataSync()?.toSeconds() ?: prefs.getInt(
            Preference.TIME_DATA,
            TIME_DAILY,
        )

    private fun teiToDownload(): Int =
        programSettings?.globalSettings()?.teiDownload() ?: prefs.getInt(
            Constants.TEI_MAX,
            Constants.TEI_MAX_DEFAULT,
        )

    private fun currentTeiCount(): Int =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState()
            .neq(State.RELATIONSHIP)
            .byDeleted()
            .isFalse
            .blockingCount()

    private fun currentEventCount(): Int =
        d2
            .eventModule()
            .events()
            .byEnrollmentUid()
            .isNull
            .byDeleted()
            .isFalse
            .bySyncState()
            .neq(State.RELATIONSHIP)
            .blockingCount()

    private fun eventsToDownload(): Int =
        programSettings?.globalSettings()?.eventsDownload() ?: prefs.getInt(
            Constants.EVENT_MAX,
            Constants.EVENT_MAX_DEFAULT,
        )

    private fun limitScope(): LimitScope =
        programSettings?.globalSettings()?.settingDownload()
            ?: getLimitedScopeFromPreferences()

    private fun getLimitedScopeFromPreferences(): LimitScope {
        val byOrgUnit = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false)
        val byProgram = prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false)
        return if (byOrgUnit && !byProgram) {
            LimitScope.PER_ORG_UNIT
        } else if (!byOrgUnit && byProgram) {
            LimitScope.PER_PROGRAM
        } else if (byOrgUnit && byProgram) {
            LimitScope.PER_OU_AND_PROGRAM
        } else {
            LimitScope.GLOBAL
        }
    }

    suspend fun saveEventsToDownload(numberOfEvents: Int) {
        prefs.setValue(Constants.EVENT_MAX, numberOfEvents)
    }

    suspend fun saveTeiToDownload(numberOfTeis: Int) {
        prefs.setValue(Constants.TEI_MAX, numberOfTeis)
    }

    suspend fun saveLimitScope(limitScope: LimitScope) {
        when (limitScope) {
            LimitScope.ALL_ORG_UNITS -> {
            }

            LimitScope.GLOBAL -> {
                prefs.setValue(LIMIT_BY_ORG_UNIT, false)
                prefs.setValue(LIMIT_BY_PROGRAM, false)
            }

            LimitScope.PER_ORG_UNIT -> {
                prefs.setValue(LIMIT_BY_ORG_UNIT, true)
                prefs.setValue(LIMIT_BY_PROGRAM, false)
            }

            LimitScope.PER_PROGRAM -> {
                prefs.setValue(LIMIT_BY_ORG_UNIT, false)
                prefs.setValue(LIMIT_BY_PROGRAM, true)
            }

            LimitScope.PER_OU_AND_PROGRAM -> {
                prefs.setValue(LIMIT_BY_ORG_UNIT, true)
                prefs.setValue(LIMIT_BY_PROGRAM, true)
            }
        }
    }

    suspend fun saveReservedValuesToDownload(reservedValuesCount: Int) {
        prefs.setValue(NUMBER_RV, reservedValuesCount)
    }

    fun saveGatewayNumber(gatewayNumber: String) {
        try {
            d2
                .smsModule()
                .configCase()
                .setGatewayNumber(gatewayNumber)
                .blockingAwait()
        } catch (e: Exception) {
            Timber.d(e.message)
        }
    }

    fun saveSmsResultSender(smsResultSender: String) {
        d2
            .smsModule()
            .configCase()
            .setConfirmationSenderNumber(smsResultSender)
            .blockingAwait()
    }

    fun saveSmsResponseTimeout(smsResponseTimeout: Int) {
        d2
            .smsModule()
            .configCase()
            .setWaitingResultTimeout(smsResponseTimeout)
            .blockingAwait()
    }

    fun saveWaitForSmsResponse(shouldWait: Boolean) {
        d2
            .smsModule()
            .configCase()
            .setWaitingForResultEnabled(shouldWait)
            .blockingAwait()
    }

    suspend fun enableSmsModule(enable: Boolean) {
        val job =
            if (enable) {
                d2
                    .smsModule()
                    .configCase()
                    .setModuleEnabled(true)
                    .andThen(d2.smsModule().configCase().refreshMetadataIds())
            } else {
                d2.smsModule().configCase().setModuleEnabled(false)
            }
        job.blockingAwait()
    }

    suspend fun deleteLocalData() {
        d2.wipeModule().wipeData()
    }

    suspend fun d2Errors() = d2.maintenanceModule().d2Errors().blockingGet()

    suspend fun trackerImportConflicts() = d2.importModule().trackerImportConflicts().blockingGet()

    suspend fun foreignKeyViolations() = d2.maintenanceModule().foreignKeyViolations().blockingGet()

    suspend fun exportDatabase() = d2.maintenanceModule().databaseImportExport().exportLoggedUserDatabase()

    fun getVersionName(): String = BuildConfig.VERSION_NAME

    fun isTwoFAConfigured(): Boolean = featureConfigRepository.isFeatureEnable(Feature.TWO_FACTOR_AUTHENTICATION)
}
