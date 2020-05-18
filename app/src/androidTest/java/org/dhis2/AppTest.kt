package org.dhis2

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
        wantToImportDB = true
        super.onCreate()
    }

    @Override
    override fun setUpServerComponent() {
        D2Manager.setTestingDatabase(DB_TO_IMPORT,"android")
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

    companion object {
        const val DB_TO_IMPORT = "127-0-0-1-8080_android_unencrypted.db"
    }
}