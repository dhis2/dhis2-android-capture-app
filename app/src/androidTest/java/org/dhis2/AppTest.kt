package org.dhis2

import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import androidx.work.Configuration
import org.dhis2.common.preferences.PreferencesTestingModule
import org.dhis2.data.schedulers.SchedulerModule
import org.dhis2.data.schedulers.SchedulersProviderImpl
import org.dhis2.data.server.ServerModule
import org.dhis2.data.service.workManager.WorkManagerModule
import org.dhis2.data.user.UserModule
import org.dhis2.utils.analytics.AnalyticsModule
import org.hisp.dhis.android.core.D2Manager
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class AppTest : App(), Configuration.Provider {

    @Override
    override fun onCreate() {
        wantToImportDB = true
        super.onCreate()
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
            .workManagerController(WorkManagerModule())
    }

    companion object {
        const val DB_TO_IMPORT = "127-0-0-1-8080_android_unencrypted.db"
        const val USERNAME = "android"
        const val IDLING_THREAD = "IDLING_WORK_MANAGER"
    }

    override fun getWorkManagerConfiguration(): Configuration {
        // This value is the same as the core pool size for AsyncTask#THREAD_POOL_EXECUTOR.
        val nThreads =
            2.coerceAtLeast((Runtime.getRuntime().availableProcessors() - 1).coerceAtMost(4))
        return Configuration.Builder()
            .setExecutor(
                IdlingThreadPoolExecutor(
                    IDLING_THREAD,
                    nThreads, nThreads,
                    0L, TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory()
                )
            ).build()
    }
}
