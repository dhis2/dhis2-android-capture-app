package org.dhis2

import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import org.dhis2.common.coroutine.DispatcherTestingModule
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.preferences.PreferencesTestingModule
import org.dhis2.commons.schedulers.SchedulerModule
import org.dhis2.commons.schedulers.SchedulersProviderImpl
import org.dhis2.data.server.ServerModule
import org.dhis2.data.user.UserModule
import org.dhis2.usescases.sync.MockedWorkManagerModule
import org.dhis2.usescases.sync.MockedWorkManagerController
import org.dhis2.utils.analytics.AnalyticsModule
import org.hisp.dhis.android.core.D2Manager

class AppTest : App() {

    val mutableWorkInfoStatuses = MutableLiveData<List<WorkInfo>>()

    @Override
    override fun onCreate() {
        populateDBIfNeeded()
        super.onCreate()
    }

    private fun populateDBIfNeeded() {
        TestingInjector.provideDBImporter(applicationContext).apply {
            copyDatabaseFromAssetsIfNeeded()
        }
    }

    @Override
    override fun setUpServerComponent() {
        D2Manager.setTestingDatabase(DB_TO_IMPORT, USERNAME)
        D2Manager.blockingInstantiateD2(ServerModule.getD2Configuration(this))

        serverComponent = appComponent.plus(ServerModule())

        setUpUserComponent()
    }

    @Override
    override fun setUpUserComponent() {
        super.setUpUserComponent()

        val userManager = if (serverComponent == null) {
            null
        } else {
            serverComponent!!.userManager()
        }
        if (userManager != null) {
            userComponent = serverComponent!!.plus(UserModule())
        }
    }

    override fun prepareAppComponent(): AppComponent.Builder {
        return DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .schedulerModule(SchedulerModule(SchedulersProviderImpl()))
            .analyticsModule(AnalyticsModule())
            .preferenceModule(PreferencesTestingModule())
            .coroutineDispatchers(DispatcherTestingModule())
            .workManagerController(
                MockedWorkManagerModule(
                    MockedWorkManagerController(
                        mutableWorkInfoStatuses
                    )
                )
            )
    }

    companion object {
        const val DB_TO_IMPORT = "127-0-0-1-8080_android_unencrypted.db"
        const val USERNAME = "android"
    }
}
