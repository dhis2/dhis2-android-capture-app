package org.dhis2

import org.dhis2.common.keystore.KeyStoreRobot.Companion.PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.USERNAME
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.preferences.PreferencesTestingModule
import org.dhis2.data.schedulers.SchedulerModule
import org.dhis2.data.schedulers.SchedulersProviderImpl
import org.dhis2.data.server.ServerModule
import org.dhis2.data.service.workManager.WorkManagerModule
import org.dhis2.data.user.UserModule
import org.dhis2.utils.UtilsModule
import org.dhis2.utils.analytics.AnalyticsModule
import org.hisp.dhis.android.core.D2Manager

class AppTest : App() {

    @Override
    override fun onCreate() {
        isTesting = true
        super.onCreate()
    }

    @Override
    override fun setUpServerComponent() {
        D2Manager.blockingInstantiateD2(ServerModule.getD2Configuration(this))

        serverComponent = appComponent.plus(ServerModule())

        setUpUserComponent()
    }

    @Override
    override fun setUpUserComponent() {
        super.setUpUserComponent()

        val userManager = if (serverComponent == null)
            null
        else
            serverComponent!!.userManager()

        if (userManager != null) {
            userComponent = serverComponent!!.plus(UserModule())
        }

        logInUser()

        serverComponent?.let {
            val userManager = it.userManager()
            userManager.logIn("android","Android123","any")
        }
    }

    private fun logInUser() {
        val keyStoreRobot = TestingInjector.providesKeyStoreRobot(baseContext)
        keyStoreRobot.apply {
            setData(USERNAME, "android")
            setData(PASSWORD, "Android123")
        }
    }

    override fun prepareAppComponent(): AppComponent.Builder {
        return DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .schedulerModule(SchedulerModule(SchedulersProviderImpl()))
                .analyticsModule(AnalyticsModule())
                .preferenceModule(PreferencesTestingModule())
                .utilModule(UtilsModule())
                .workManagerController(WorkManagerModule())
    }
}