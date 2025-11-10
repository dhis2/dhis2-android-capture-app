package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dhis2.org.analytics.charts.idling.AnalyticsCountingIdlingResource
import org.dhis2.AppTest
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.common.BaseRobot
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.featureConfig.FeatureConfigRobot
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_USERNAME
import org.dhis2.common.keystore.KeyStoreRobot.Companion.PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.USERNAME
import org.dhis2.common.mockwebserver.MockWebServerRobot
import org.dhis2.common.preferences.PreferencesRobot
import org.dhis2.common.rules.DisableAnimations
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.orgunitselector.OrgUnitIdlingResource
import org.dhis2.commons.prefs.Preference
import org.dhis2.form.ui.idling.FormCountingIdlingResource
import org.dhis2.maps.utils.OnMapReadyIdlingResourceSingleton
import org.dhis2.mobile.commons.coroutine.AndroidIdlingResource
import org.dhis2.mobile.commons.coroutine.IdlingResourceProvider
import org.dhis2.mobile.commons.coroutine.NoOpIdlingResource
import org.dhis2.usescases.eventsWithoutRegistration.EventIdlingResourceSingleton
import org.dhis2.usescases.notes.NotesIdlingResource
import org.dhis2.usescases.programEventDetail.eventList.EventListIdlingResourceSingleton
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.rules.Timeout
import timber.log.Timber
import java.util.concurrent.TimeUnit


open class BaseTest {

    @JvmField
    protected var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var testContext = InstrumentationRegistry.getInstrumentation().context
    private var isIntentsEnable = false
    private lateinit var keyStoreRobot: KeyStoreRobot
    lateinit var preferencesRobot: PreferencesRobot
    lateinit var mockWebServerRobot: MockWebServerRobot
    lateinit var featureConfigRobot: FeatureConfigRobot
    var restoreDataBaseOnBeforeAction = true


    protected open fun getPermissionsToBeAccepted() = arrayOf<String>()

    @get:Rule
    val timeout: Timeout = Timeout(120000, TimeUnit.MILLISECONDS)

    @get: Rule
    var testName: TestName = TestName()

    @get:Rule
    var permissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        val currentTest = testName.methodName
        Timber.tag("RUNNER_LOG").d("Executing Before Actions for $currentTest")
        if(restoreDataBaseOnBeforeAction){
            (context.applicationContext as AppTest).restoreDB()
        }
        injectDependencies()
        registerCountingIdlingResource()
        setupCredentials()
    }

    @After
    @Throws(Exception::class)
    open fun teardown() {
        val currentTest = testName.methodName
        Timber.tag("RUNNER_LOG").d("Executing After Actions for $currentTest")
        unregisterCountingIdlingResource()
        closeKeyboard()
        disableIntents()
        cleanPreferences()
        cleanLocalDatabase()
        cleanKeystore()
        stopMockServer()
    }

    private fun injectDependencies() {
        TestingInjector.apply {
            keyStoreRobot = providesKeyStoreRobot(context)
            preferencesRobot = providesPreferencesRobot(context)
            mockWebServerRobot = providesMockWebserverRobot(testContext)
            featureConfigRobot = providesFeatureConfigRobot()
        }
    }

    private val idlingResources = listOf(
        EventListIdlingResourceSingleton.countingIdlingResource,
        FormCountingIdlingResource.countingIdlingResource,
        TeiDataIdlingResourceSingleton.countingIdlingResource,
        EventIdlingResourceSingleton.countingIdlingResource,
        OnMapReadyIdlingResourceSingleton.countingIdlingResource,
        AnalyticsCountingIdlingResource.countingIdlingResource,
        NotesIdlingResource.countingIdlingResource,
        OrgUnitIdlingResource.countingIdlingResource,
        AndroidIdlingResource.getIdlingResource(),
    )

    private fun registerCountingIdlingResource() {
        IdlingResourceProvider.idlingResource = AndroidIdlingResource
        IdlingRegistry.getInstance().register(*idlingResources.toTypedArray())
    }

    private fun unregisterCountingIdlingResource() {
        IdlingResourceProvider.idlingResource = NoOpIdlingResource
        IdlingRegistry.getInstance()
            .unregister(*idlingResources.toTypedArray())
    }

    fun setupMockServer() {
        mockWebServerRobot.start()
    }

    fun enableIntents() {
        if (!isIntentsEnable) {
            Intents.init()
            isIntentsEnable = true
        }
    }

    fun setupCredentials() {
        val keyStoreRobot = TestingInjector.providesKeyStoreRobot(context)
        keyStoreRobot.apply {
            setData(KEYSTORE_USERNAME, USERNAME)
            setData(KEYSTORE_PASSWORD, PASSWORD)
        }
    }

    fun setDatePicker() {
        preferencesRobot.saveValue(Preference.DATE_PICKER, true)
    }

    private fun closeKeyboard() {
        BaseRobot().closeKeyboard()
    }

    private fun disableIntents() {
        if (isIntentsEnable) {
            Intents.release()
            isIntentsEnable = false
        }
    }

    private fun cleanPreferences() {
        if(::preferencesRobot.isInitialized){
            preferencesRobot.cleanPreferences()
        }
    }

    private fun cleanKeystore() {
        if(::keyStoreRobot.isInitialized) {
            keyStoreRobot.apply {
                removeData(KEYSTORE_USERNAME)
                removeData(KEYSTORE_PASSWORD)
            }
        }
    }

    private fun stopMockServer() {
        if(::mockWebServerRobot.isInitialized) {
            mockWebServerRobot.shutdown()
        }
    }

    fun cleanLocalDatabase() {
       val deleted = (context.applicationContext as AppTest).deleteDatabase(DB_TO_IMPORT)
        val currentTest = testName.methodName
        Timber.tag("RUNNER_LOG").d("CleanDataBaseResult. Is deleted? answer: $deleted for $currentTest")
    }

    protected fun enableFeatureConfigValue(feature: Feature) {
        featureConfigRobot.enableFeature(feature)
    }

    protected fun disableFeatureConfigValue(feature: Feature) {
        featureConfigRobot.disableFeature(feature)
    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
        const val MOCK_SERVER_URL = "http://127.0.0.1:8080"
        const val API = "api"
    }
}
