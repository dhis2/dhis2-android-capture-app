package org.dhis2

import android.os.StrictMode
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dhis2.common.coroutine.DispatcherTestingModule
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.preferences.PreferencesTestingModule
import org.dhis2.commons.schedulers.SchedulerModule
import org.dhis2.commons.schedulers.SchedulersProviderImpl
import org.dhis2.data.server.ServerModule
import org.dhis2.data.user.UserModule
import org.dhis2.usescases.sync.MockedWorkManagerController
import org.dhis2.usescases.sync.MockedWorkManagerModule
import org.dhis2.utils.analytics.AnalyticsModule
import org.hisp.dhis.android.core.D2Manager

class AppTest : App() {

    val mutableWorkInfoStatuses = MutableLiveData<List<WorkInfo>>()

    @Override
    override fun onCreate() {
   //     enableStrictMode()
        populateDBIfNeeded()
        super.onCreate()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    private fun populateDBIfNeeded() {
        Log.d("populateDB", "Populating D.B")
    /*    TestingInjector.provideDBImporter(applicationContext).apply {
            runBlocking {
                Log.d("populateDB", "Populating runblocking")
                withContext(Dispatchers.IO) {
                    Log.d("populateDB", "Populating WithContext")
                    copyDatabaseFromAssetsIfNeeded()
                    Log.d("populateDB", "Populate successfully")
                }
            }
        } */

        TestingInjector.provideDBImporter(applicationContext).apply {
            copyDatabaseFromAssetsIfNeeded()
        }
    }

    @Override
    override fun setUpServerComponent() {
        D2Manager.setTestingDatabase(DB_TO_IMPORT, USERNAME)
        D2Manager.setTestMode(true)
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
