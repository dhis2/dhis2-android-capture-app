package org.dhis2.usescases.settings

import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.bindings.toSeconds
import org.dhis2.commons.Constants
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.prefs.Preference.Companion.DEFAULT_NUMBER_RV
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX_DEFAULT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.commons.prefs.Preference.Companion.NUMBER_RV
import org.dhis2.commons.prefs.Preference.Companion.TEI_MAX
import org.dhis2.commons.prefs.Preference.Companion.TEI_MAX_DEFAULT
import org.dhis2.commons.prefs.Preference.Companion.TIME_15M
import org.dhis2.commons.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.commons.prefs.Preference.Companion.TIME_DATA
import org.dhis2.commons.prefs.Preference.Companion.TIME_META
import org.dhis2.commons.prefs.Preference.Companion.TIME_WEEKLY
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferencesProvider: PreferenceProvider = mock()
    private val featureConfigRepository: FeatureConfigRepository = mock()
    private val smsConfig: ConfigCase.SmsConfig =
        mock {
            on { isModuleEnabled } doReturn true
            on { gateway } doReturn "gatewaynumber"
            on { isWaitingForResult } doReturn true
            on { resultSender } doReturn "confirmationNumber"
            on { resultWaitingTimeout } doReturn 120
        }
    private val configCase: ConfigCase =
        mock {
            on { getSmsModuleConfig() } doReturn Single.just(smsConfig)
        }

    private val dispatcher: Dispatcher =
        mock {
            on { io } doReturn testDispatcher
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository =
            SettingsRepository(
                d2,
                preferencesProvider,
                featureConfigRepository,
                dispatcher,
            )
        configurePreferences()
        configureDataCount()
        configureSMSConfig()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should return metadata period from general settings if exist`() =
        runTest {
            configureGeneralSettings(true)
            val metadataResult = settingsRepository.metaSync()
            assertTrue(metadataResult.metadataSyncPeriod == SETTINGS_METADATA_PERIOD.toSeconds())
        }

    @Test
    fun `Should return metadata period from preferences if general settings does not exist`() =
        runTest {
            configureGeneralSettings(false)
            val metadataResult = settingsRepository.metaSync()
            assertTrue(metadataResult.metadataSyncPeriod == SETTINGS_PREF_METADATA_PERIOD)
        }

    @Test
    fun `Should return data period from general settings if exist`() =
        runTest {
            configureGeneralSettings(true)
            configureDataErrors()
            val dataResult = settingsRepository.dataSync()
            assertTrue(dataResult.dataSyncPeriod == SETTINGS_DATA_PERIOD.toSeconds())
        }

    @Test
    fun `Should return data period from preferences if general settings does not exist`() =
        runTest {
            configureGeneralSettings(false)
            configureDataErrors()
            val dataResult = settingsRepository.dataSync()
            assertTrue(dataResult.dataSyncPeriod == SETTINGS_PREF_DATA_PERIOD)
        }

    @Test
    fun `Should return parameters from general settings if exist`() =
        runTest {
            configureGeneralSettings(true)
            configureProgramSettings(true)
            val syncParameters = settingsRepository.syncParameters()
            assertTrue(syncParameters.numberOfTeiToDownload == SETTINGS_TEI_DOWNLOAD)
            assertTrue(syncParameters.numberOfEventsToDownload == SETTINGS_EVENT_DOWNLOAD)
            assertTrue(syncParameters.limitScope == SETTINGS_LIMIT_SCOPE)
        }

    @Test
    fun `Should return parameters from preferences if general settings does not exist`() =
        runTest {
            configureGeneralSettings(false)
            configureProgramSettings(false)
            val syncParameters = settingsRepository.syncParameters()
            assertTrue(syncParameters.numberOfTeiToDownload == SETTINGS_PREF_TEI_DOWNLOAD)
            assertTrue(syncParameters.numberOfEventsToDownload == SETTINGS_PREF_EVENT_DOWNLOAD)
            assertTrue(syncParameters.limitScope != SETTINGS_LIMIT_SCOPE)
        }

    @Test
    fun `Should return reserved values from settings if exist`() =
        runTest {
            configureGeneralSettings(true)
            val reservedValues = settingsRepository.reservedValues()
            assertTrue(reservedValues.numberOfReservedValuesToDownload == SETTINGS_RV)
        }

    @Test
    fun `Should return reserved values from preferences if settings does not exist`() =
        runTest {
            configureGeneralSettings(false)
            val reservedValues = settingsRepository.reservedValues()
            assertTrue(reservedValues.numberOfReservedValuesToDownload == SETTINGS_PREF_RV)
        }

    @Test
    fun `Should return editable sms configuration if settings does not exist`() =
        runTest {
            configureGeneralSettings(false)
            val sms = settingsRepository.sms()
            assertTrue(sms.isGatewayNumberEditable && sms.isResponseNumberEditable)
        }

    private fun configureGeneralSettings(hasGeneralSettings: Boolean) {
        whenever(d2.settingModule().generalSetting().blockingExists()) doReturn
            hasGeneralSettings
        whenever(userManager.theme) doReturn Single.just(Pair("flag", 1))
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
            preferencesProvider.getString(Constants.LAST_META_SYNC, "-"),
        ) doReturn "2019-02-02"
        whenever(
            preferencesProvider.getString(Constants.LAST_DATA_SYNC, "-"),
        ) doReturn "2019-02-02"
        whenever(
            preferencesProvider.getBoolean(
                Constants.LAST_META_SYNC_STATUS,
                true,
            ),
        ) doReturn true
        whenever(
            preferencesProvider.getBoolean(
                Constants.LAST_DATA_SYNC_STATUS,
                true,
            ),
        ) doReturn true
        whenever(
            preferencesProvider.getInt(NUMBER_RV, DEFAULT_NUMBER_RV),
        ) doReturn SETTINGS_PREF_RV
        whenever(
            preferencesProvider.getInt(
                TIME_META,
                TIME_WEEKLY,
            ),
        ) doReturn SETTINGS_PREF_METADATA_PERIOD
        whenever(
            preferencesProvider.getInt(
                TIME_DATA,
                TIME_DAILY,
            ),
        ) doReturn SETTINGS_PREF_DATA_PERIOD
        whenever(
            preferencesProvider.getInt(
                TEI_MAX,
                TEI_MAX_DEFAULT,
            ),
        ) doReturn SETTINGS_PREF_TEI_DOWNLOAD
        whenever(
            preferencesProvider.getInt(
                EVENT_MAX,
                EVENT_MAX_DEFAULT,
            ),
        ) doReturn SETTINGS_PREF_EVENT_DOWNLOAD
        whenever(preferencesProvider.getBoolean(LIMIT_BY_ORG_UNIT, false)) doReturn true
        whenever(preferencesProvider.getBoolean(LIMIT_BY_PROGRAM, false)) doReturn false
    }

    private fun configureDataErrors() {
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
        whenever(d2.dataValueModule().dataValues()) doReturn mock()

        whenever(d2.eventModule().events().byAggregatedSyncState()) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .`in`(State.ERROR),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .`in`(State.WARNING),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .`in`(State.ERROR)
                .blockingGet(),
        ) doReturn
            emptyList()
        whenever(
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .`in`(State.WARNING)
                .blockingGet(),
        ) doReturn
            emptyList()

        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState(),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.ERROR),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.WARNING),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.ERROR)
                .blockingGet(),
        ) doReturn emptyList()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.WARNING)
                .blockingGet(),
        ) doReturn emptyList()

        whenever(
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState(),
        ) doReturn mock()
        whenever(
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.ERROR),
        ) doReturn mock()
        whenever(
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.WARNING),
        ) doReturn mock()
        whenever(
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.ERROR)
                .blockingGet(),
        ) doReturn emptyList()
        whenever(
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .`in`(State.WARNING)
                .blockingGet(),
        ) doReturn emptyList()
    }

    private fun configureDataCount() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState(),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .neq(State.RELATIONSHIP),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .neq(State.RELATIONSHIP)
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .neq(State.RELATIONSHIP)
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .neq(State.RELATIONSHIP)
                .byDeleted()
                .isFalse
                .blockingCount(),
        ) doReturn 0

        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull
                .byDeleted()
                .isFalse,
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull
                .byDeleted()
                .isFalse
                .bySyncState(),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull
                .byDeleted()
                .isFalse
                .bySyncState()
                .neq(State.RELATIONSHIP),
        ) doReturn mock()
        whenever(
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .isNull
                .byDeleted()
                .isFalse
                .bySyncState()
                .neq(State.RELATIONSHIP)
                .blockingCount(),
        ) doReturn 0
    }

    private fun configureSMSConfig() {
        whenever(d2.smsModule().configCase()) doReturn configCase
    }

    private fun mockedGeneralSettings(): GeneralSettings =
        GeneralSettings
            .builder()
            .dataSync(SETTINGS_DATA_PERIOD)
            .metadataSync(SETTINGS_METADATA_PERIOD)
            .encryptDB(SETTINGS_ENCRYPT)
            .reservedValues(SETTINGS_RV)
            .build()

    private fun mockedProgramSettings(): ProgramSettings =
        ProgramSettings
            .builder()
            .globalSettings(
                ProgramSetting
                    .builder()
                    .eventsDownload(SETTINGS_TEI_DOWNLOAD)
                    .teiDownload(SETTINGS_EVENT_DOWNLOAD)
                    .settingDownload(SETTINGS_LIMIT_SCOPE)
                    .build(),
            ).specificSettings(
                mutableMapOf(
                    Pair(
                        "programUid",
                        ProgramSetting
                            .builder()
                            .eventsDownload(200)
                            .teiDownload(300)
                            .build(),
                    ),
                ),
            ).build()

    companion object {
        private val SETTINGS_PREF_EVENT_DOWNLOAD = 50
        private val SETTINGS_PREF_TEI_DOWNLOAD = 50
        private val SETTINGS_METADATA_PERIOD = MetadataSyncPeriod.EVERY_7_DAYS
        private val SETTINGS_DATA_PERIOD = DataSyncPeriod.EVERY_HOUR
        private val SETTINGS_ENCRYPT = false
        private val SETTINGS_RV = 50
        private val SETTINGS_TEI_DOWNLOAD = 100
        private val SETTINGS_EVENT_DOWNLOAD = 100
        private val SETTINGS_LIMIT_SCOPE = LimitScope.PER_OU_AND_PROGRAM
        private val SETTINGS_PREF_METADATA_PERIOD = TIME_DAILY
        private val SETTINGS_PREF_DATA_PERIOD = TIME_15M
        private val SETTINGS_PREF_RV = 25
    }
}
