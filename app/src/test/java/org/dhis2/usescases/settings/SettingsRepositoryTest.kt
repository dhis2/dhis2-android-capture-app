package org.dhis2.usescases.settings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.Bindings.toSeconds
import org.dhis2.data.prefs.Preference.Companion.DEFAULT_NUMBER_RV
import org.dhis2.data.prefs.Preference.Companion.EVENT_MAX
import org.dhis2.data.prefs.Preference.Companion.EVENT_MAX_DEFAULT
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.data.prefs.Preference.Companion.NUMBER_RV
import org.dhis2.data.prefs.Preference.Companion.TEI_MAX
import org.dhis2.data.prefs.Preference.Companion.TEI_MAX_DEFAULT
import org.dhis2.data.prefs.Preference.Companion.TIME_15M
import org.dhis2.data.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.data.prefs.Preference.Companion.TIME_DATA
import org.dhis2.data.prefs.Preference.Companion.TIME_META
import org.dhis2.data.prefs.Preference.Companion.TIME_WEEKLY
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase
import org.hisp.dhis.android.core.sms.domain.repository.WebApiRepository
import org.hisp.dhis.android.core.sms.domain.repository.internal.LocalDbRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SettingsRepositoryTest {

    private lateinit var settingsRepository: SettingsRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferencesProvider: PreferenceProvider = mock()
    private var localDbRepository: LocalDbRepository = mock()
    private var webApiRepository: WebApiRepository = mock()

    private val SETTINGS_METADATA_PERIOD = MetadataSyncPeriod.EVERY_7_DAYS
    private val SETTINGS_DATA_PERIOD = DataSyncPeriod.EVERY_HOUR
    private val SETTINGS_ENCRYPT = false
    private val SETTINGS_GATEWAY = "+1111111111"
    private val SETTINGS_RESPONSE = "+2222222222"
    private val SETTINGS_RV = 50
    private val SETTINGS_TEI_DOWNLOAD = 100
    private val SETTINGS_EVENT_DOWNLOAD = 100
    private val SETTINGS_LIMIT_SCOPE = LimitScope.PER_OU_AND_PROGRAM

    private val SETTINGS_PREF_METADATA_PERIOD = TIME_DAILY
    private val SETTINGS_PREF_DATA_PERIOD = TIME_15M
    private val SETTINGS_PREF_RV = 25
    private val SETTINGS_PREF_TEI_DOWNLOAD = 50
    private val SETTINGS_PREF_EVENT_DOWNLOAD = 50

    @Before
    fun setUp() {
        settingsRepository = SettingsRepository(d2, preferencesProvider)
        configurePreferences()
        configureDataCount()
        configureSMSConfig()
    }

    @Test
    fun `Should return metadata period from general settings if exist`() {
        configureGeneralSettings(true)
        val testObserver = settingsRepository.metaSync().test()
        testObserver
            .assertNoErrors()
            .assertValue { metadataSettings ->
                metadataSettings.metadataSyncPeriod == SETTINGS_METADATA_PERIOD.toSeconds()
            }
    }

    @Test
    fun `Should return metadata period from preferences if general settings does not exist`() {
        configureGeneralSettings(false)
        val testObserver = settingsRepository.metaSync().test()
        testObserver
            .assertNoErrors()
            .assertValue { metadataSettings ->
                metadataSettings.metadataSyncPeriod == SETTINGS_PREF_METADATA_PERIOD
            }
    }

    @Test
    fun `Should return data period from general settings if exist`() {
        configureGeneralSettings(true)
        configureDataErrors()
        val testObserver = settingsRepository.dataSync().test()
        testObserver
            .assertNoErrors()
            .assertValue { dataSettings ->
                dataSettings.dataSyncPeriod == SETTINGS_DATA_PERIOD.toSeconds()
            }
    }

    @Test
    fun `Should return data period from preferences if general settings does not exist`() {
        configureGeneralSettings(false)
        configureDataErrors()
        val testObserver = settingsRepository.dataSync().test()
        testObserver
            .assertNoErrors()
            .assertValue { dataSettings ->
                dataSettings.dataSyncPeriod == SETTINGS_PREF_DATA_PERIOD
            }
    }

    @Test
    fun `Should return parameters from general settings if exist`() {
        configureGeneralSettings(true)
        configureProgramSettings(true)
        val testObserver = settingsRepository.syncParameters().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it.numberOfTeiToDownload == SETTINGS_TEI_DOWNLOAD &&
                    it.numberOfEventsToDownload == SETTINGS_EVENT_DOWNLOAD &&
                    it.limitScope == SETTINGS_LIMIT_SCOPE
            }
    }

    @Test
    fun `Should return parameters from preferences if general settings does not exist`() {
        configureGeneralSettings(false)
        configureProgramSettings(false)
        val testObserver = settingsRepository.syncParameters().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it.numberOfTeiToDownload == SETTINGS_PREF_TEI_DOWNLOAD &&
                    it.numberOfEventsToDownload == SETTINGS_PREF_EVENT_DOWNLOAD &&
                    it.limitScope != SETTINGS_LIMIT_SCOPE
            }
    }

    @Test
    fun `Should return reserved values from settings if exist`() {
        configureGeneralSettings(true)
        val testObserver = settingsRepository.reservedValues().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it.numberOfReservedValuesToDownload == SETTINGS_RV
            }
    }

    @Test
    fun `Should return reserved values from preferences if settings does not exist`() {
        configureGeneralSettings(false)
        val testObserver = settingsRepository.reservedValues().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it.numberOfReservedValuesToDownload == SETTINGS_PREF_RV
            }
    }

    @Test
    fun `Should return non editable sms configuration if settings exist`() {
        configureGeneralSettings(true)
        val testObserver = settingsRepository.sms().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                !it.isGatewayNumberEditable && !it.isResponseNumberEditable
            }
    }

    @Test
    fun `Should return editable sms configuration if settings does not exist`() {
        configureGeneralSettings(false)
        val testObserver = settingsRepository.sms().test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it.isGatewayNumberEditable && it.isResponseNumberEditable
            }
    }

    private fun configureGeneralSettings(hasGeneralSettings: Boolean) {
        whenever(d2.settingModule().generalSetting().blockingExists()) doReturn
            hasGeneralSettings
        if (hasGeneralSettings) {
            whenever(d2.settingModule().generalSetting().blockingGet()) doReturn
                mockedGeneralSettings()
        }
    }

    private fun configureProgramSettings(hasProgramSettings: Boolean) {
        whenever(d2.settingModule().programSetting().blockingExists()) doReturn
            hasProgramSettings
        if (hasProgramSettings) {
            whenever(d2.settingModule().programSetting().blockingGet()) doReturn
                mockedProgramSettings()
        }
    }

    private fun configurePreferences() {
        whenever(
            preferencesProvider.getString(Constants.LAST_META_SYNC, "-")
        ) doReturn "2019-02-02"
        whenever(
            preferencesProvider.getString(Constants.LAST_DATA_SYNC, "-")
        ) doReturn "2019-02-02"
        whenever(
            preferencesProvider.getBoolean(
                Constants.LAST_META_SYNC_STATUS,
                true
            )
        ) doReturn true
        whenever(
            preferencesProvider.getBoolean(
                Constants.LAST_DATA_SYNC_STATUS,
                true
            )
        ) doReturn true
        whenever(
            preferencesProvider.getInt(NUMBER_RV, DEFAULT_NUMBER_RV)
        ) doReturn SETTINGS_PREF_RV
        whenever(
            preferencesProvider.getInt(
                TIME_META,
                TIME_WEEKLY
            )
        ) doReturn SETTINGS_PREF_METADATA_PERIOD
        whenever(
            preferencesProvider.getInt(
                TIME_DATA,
                TIME_DAILY
            )
        ) doReturn SETTINGS_PREF_DATA_PERIOD
        whenever(
            preferencesProvider.getInt(
                TEI_MAX,
                TEI_MAX_DEFAULT
            )
        ) doReturn SETTINGS_PREF_TEI_DOWNLOAD
        whenever(
            preferencesProvider.getInt(
                EVENT_MAX,
                EVENT_MAX_DEFAULT
            )
        ) doReturn SETTINGS_PREF_EVENT_DOWNLOAD
        whenever(preferencesProvider.getBoolean(LIMIT_BY_ORG_UNIT, false)) doReturn true
        whenever(preferencesProvider.getBoolean(LIMIT_BY_PROGRAM, false)) doReturn false
    }

    private fun configureDataErrors() {
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
        whenever(d2.dataValueModule().dataValues()) doReturn mock()

        whenever(d2.eventModule().events().byState()) doReturn mock()
        whenever(d2.eventModule().events().byState().`in`(State.ERROR)) doReturn mock()
        whenever(d2.eventModule().events().byState().`in`(State.WARNING)) doReturn mock()
        whenever(d2.eventModule().events().byState().`in`(State.ERROR).blockingGet()) doReturn
            emptyList()
        whenever(d2.eventModule().events().byState().`in`(State.WARNING).blockingGet()) doReturn
            emptyList()

        whenever(d2.trackedEntityModule().trackedEntityInstances().byState()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().`in`(State.ERROR)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().`in`(State.WARNING)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().`in`(State.ERROR).blockingGet()
        ) doReturn emptyList()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().`in`(State.WARNING).blockingGet()
        ) doReturn emptyList()

        whenever(
            d2.dataValueModule().dataValues()
                .byState()
        ) doReturn mock()
        whenever(
            d2.dataValueModule().dataValues()
                .byState().`in`(State.ERROR)
        ) doReturn mock()
        whenever(
            d2.dataValueModule().dataValues()
                .byState().`in`(State.WARNING)
        ) doReturn mock()
        whenever(
            d2.dataValueModule().dataValues()
                .byState().`in`(State.ERROR).blockingGet()
        ) doReturn emptyList()
        whenever(
            d2.dataValueModule().dataValues()
                .byState().`in`(State.WARNING).blockingGet()
        ) doReturn emptyList()
    }

    private fun configureDataCount() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().neq(State.RELATIONSHIP)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().neq(State.RELATIONSHIP).byDeleted()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().neq(State.RELATIONSHIP).byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .byState().neq(State.RELATIONSHIP).byDeleted().isFalse.blockingCount()
        ) doReturn 0

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().isNull
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().isNull.byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().isNull.byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().isNull.byDeleted().isFalse.blockingCount()
        ) doReturn 0
    }

    private fun configureSMSConfig() {
        whenever(localDbRepository.isModuleEnabled) doReturn
            Single.just(true)
        whenever(localDbRepository.gatewayNumber) doReturn
            Single.just("gatewaynumber")
        whenever(localDbRepository.waitingForResultEnabled) doReturn
            Single.just(true)
        whenever(localDbRepository.confirmationSenderNumber) doReturn
            Single.just("confirmationNumber")
        whenever(localDbRepository.waitingResultTimeout) doReturn
            Single.just(120)
        whenever(d2.smsModule().configCase()) doReturn
            ConfigCase(
                webApiRepository,
                localDbRepository
            )
    }

    private fun mockedGeneralSettings(): GeneralSettings {
        return GeneralSettings.builder()
            .dataSync(SETTINGS_DATA_PERIOD)
            .metadataSync(SETTINGS_METADATA_PERIOD)
            .encryptDB(SETTINGS_ENCRYPT)
            .numberSmsConfirmation(SETTINGS_GATEWAY)
            .numberSmsToSend(SETTINGS_RESPONSE)
            .reservedValues(SETTINGS_RV)
            .build()
    }

    private fun mockedProgramSettings(): ProgramSettings {
        return ProgramSettings.builder()
            .globalSettings(
                ProgramSetting.builder()
                    .eventsDownload(SETTINGS_TEI_DOWNLOAD)
                    .teiDownload(SETTINGS_EVENT_DOWNLOAD)
                    .settingDownload(SETTINGS_LIMIT_SCOPE)
                    .build()
            )
            .specificSettings(
                mutableMapOf(
                    Pair(
                        "programUid",
                        ProgramSetting.builder()
                            .eventsDownload(200)
                            .teiDownload(300)
                            .build()
                    )
                )
            )
            .build()
    }
}
