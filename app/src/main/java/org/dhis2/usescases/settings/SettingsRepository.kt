package org.dhis2.usescases.settings

import io.reactivex.Single
import org.dhis2.Bindings.toSeconds
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.Preference.Companion.DEFAULT_NUMBER_RV
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.data.prefs.Preference.Companion.NUMBER_RV
import org.dhis2.data.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.data.prefs.Preference.Companion.TIME_WEEKLY
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase

class SettingsRepository(
    val d2: D2,
    val prefs: PreferenceProvider
) {
    private val hasProgramSettings: Boolean = d2.settingModule().programSetting().blockingExists()
    private val hasGeneralSettings: Boolean = d2.settingModule().generalSetting().blockingExists()
    private val generalSettings: GeneralSettings?
    private val programSettings: ProgramSettings?
    private val smsConfig: ConfigCase.SmsConfig

    init {
        generalSettings = if (hasGeneralSettings) {
            d2.settingModule().generalSetting().blockingGet()
        } else {
            null
        }
        programSettings = if (hasProgramSettings) {
            d2.settingModule().programSetting().blockingGet()
        } else {
            null
        }
        smsConfig = d2.smsModule().configCase().smsModuleConfig.blockingGet()
    }

    fun dataSync(): Single<DataSettingsViewModel> {
        return Single.just(
            DataSettingsViewModel(
                dataPeriod(),
                prefs.getString(Constants.LAST_DATA_SYNC, "-")!!,
                !prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true),
                dataHasErrors(),
                dataHasWarning(),
                generalSettings?.dataSync() == null
            )
        )
    }

    fun metaSync(): Single<MetadataSettingsViewModel> {
        return Single.just(
            MetadataSettingsViewModel(
                metadataPeriod(),
                prefs.getString(Constants.LAST_META_SYNC, "-")!!,
                !prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true),
                generalSettings?.metadataSync() == null
            )
        )
    }

    fun syncParameters(): Single<SyncParametersViewModel> {
        return Single.just(
            SyncParametersViewModel(
                teiToDownload(),
                eventsToDownload(),
                currentTeiCount(),
                currentEventCount(),
                limitScope(),
                programSettings?.globalSettings()?.teiDownload() == null,
                programSettings?.globalSettings()?.eventsDownload() == null,
                programSettings?.globalSettings()?.settingDownload() == null,
                programSettings?.specificSettings()?.size ?: 0
            )
        )
    }

    fun reservedValues(): Single<ReservedValueSettingsViewModel> {
        return Single.just(
            ReservedValueSettingsViewModel(
                generalSettings?.reservedValues() ?: prefs.getInt(
                    NUMBER_RV,
                    DEFAULT_NUMBER_RV
                ),
                generalSettings?.reservedValues() == null
            )
        )
    }

    fun sms(): Single<SMSSettingsViewModel> {
        return Single.just(
            SMSSettingsViewModel(
                smsConfig.isModuleEnabled,
                smsConfig.gateway,
                smsConfig.resultSender,
                smsConfig.resultWaitingTimeout,
                generalSettings?.numberSmsToSend() == null,
                generalSettings?.numberSmsConfirmation() == null,
                smsConfig.isWaitingForResult
            )
        )
    }

    private fun dataHasErrors(): Boolean {
        return d2.eventModule().events()
            .byState().`in`(State.ERROR)
            .blockingGet().isNotEmpty() ||
                d2.trackedEntityModule().trackedEntityInstances()
                    .byState().`in`(State.ERROR)
                    .blockingGet().isNotEmpty() ||
                d2.dataValueModule().dataValues()
                    .byState().`in`(State.ERROR)
                    .blockingGet().isNotEmpty()
    }

    private fun dataHasWarning(): Boolean {
        return d2.eventModule().events()
            .byState().`in`(State.WARNING)
            .blockingGet().isNotEmpty() ||
                d2.trackedEntityModule().trackedEntityInstances()
                    .byState().`in`(State.WARNING)
                    .blockingGet().isNotEmpty() ||
                d2.dataValueModule().dataValues()
                    .byState().`in`(State.WARNING)
                    .blockingGet().isNotEmpty()
    }


    private fun metadataPeriod(): Int {
        return generalSettings?.metadataSync()?.toSeconds() ?: prefs.getInt(
            Preference.TIME_META,
            TIME_WEEKLY
        )
    }

    private fun dataPeriod(): Int {
        return generalSettings?.dataSync()?.toSeconds() ?: prefs.getInt(
            Preference.TIME_DATA,
            TIME_DAILY
        )
    }

    private fun teiToDownload(): Int {
        return programSettings?.globalSettings()?.teiDownload() ?: prefs.getInt(
            Constants.TEI_MAX,
            Constants.TEI_MAX_DEFAULT
        )
    }

    private fun currentTeiCount(): Int {
        return d2.trackedEntityModule().trackedEntityInstances()
            .byState().neq(State.RELATIONSHIP)
            .byDeleted().isFalse
            .blockingCount()
    }

    private fun currentEventCount(): Int {
        return d2.eventModule().events()
            .byEnrollmentUid().isNull
            .byDeleted().isFalse
            .blockingCount()
    }

    private fun eventsToDownload(): Int {
        return programSettings?.globalSettings()?.eventsDownload() ?: prefs.getInt(
            Constants.EVENT_MAX,
            Constants.EVENT_MAX_DEFAULT
        )
    }

    private fun limitScope(): LimitScope {
        return programSettings?.globalSettings()?.settingDownload()
            ?: getLimitedScopeFromPreferences()
    }

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

    fun saveEventsToDownload(numberOfEvents: Int) {
        prefs.setValue(Constants.EVENT_MAX, numberOfEvents)
    }

    fun saveTeiToDownload(numberOfTeis: Int) {
        prefs.setValue(Constants.TEI_MAX, numberOfTeis)
    }

    fun saveLimitScope(limitScope: LimitScope) {
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

    fun saveReservedValuesToDownload(reservedValuesCount: Int) {
        prefs.setValue(NUMBER_RV, reservedValuesCount)
    }

    fun saveGatewayNumber(gatewayNumber: String) {
        d2.smsModule().configCase().setGatewayNumber(gatewayNumber).blockingAwait()
    }

    fun saveSmsResultSender(smsResultSender: String) {
        d2.smsModule().configCase().setConfirmationSenderNumber(smsResultSender).blockingAwait()
    }

    fun saveSmsResponseTimeout(smsResponseTimeout: Int) {
        d2.smsModule().configCase().setWaitingResultTimeout(smsResponseTimeout).blockingAwait()
    }

    fun saveWaitForSmsResponse(shouldWait: Boolean) {
        d2.smsModule().configCase().setWaitingForResultEnabled(shouldWait).blockingAwait()
    }

    fun enableSmsModule(enable: Boolean, onCompleteListener: Runnable) {
        if (enable) {
            d2.smsModule().configCase().setModuleEnabled(enable)
                .andThen(d2.smsModule().configCase().refreshMetadataIds())
                .doOnComplete { onCompleteListener.run() }
                .blockingAwait()
        } else {
            d2.smsModule().configCase().setModuleEnabled(enable)
                .doOnComplete { onCompleteListener.run() }
                .blockingAwait()
        }
    }
}