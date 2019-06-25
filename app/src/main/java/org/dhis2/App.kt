package org.dhis2

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.google.android.gms.security.ProviderInstaller
import io.fabric.sdk.android.Fabric
import io.ona.kujaku.KujakuLibrary
import io.reactivex.android.schedulers.AndroidSchedulers
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.dagger.PerServer
import org.dhis2.data.dagger.PerUser
import org.dhis2.data.database.DbModule
import org.dhis2.data.forms.FormComponent
import org.dhis2.data.forms.FormModule
import org.dhis2.data.metadata.MetadataModule
import org.dhis2.data.schedulers.SchedulerModule
import org.dhis2.data.schedulers.SchedulersProviderImpl
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.ServerModule
import org.dhis2.data.user.UserComponent
import org.dhis2.data.user.UserModule
import org.dhis2.usescases.login.LoginComponent
import org.dhis2.usescases.login.LoginModule
import org.dhis2.usescases.sync.SyncComponent
import org.dhis2.usescases.sync.SyncModule
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent
import org.dhis2.usescases.teiDashboard.TeiDashboardModule
import org.dhis2.utils.UtilsModule
import org.dhis2.utils.timber.ReleaseTree
import org.hisp.dhis.android.core.configuration.Configuration
import org.hisp.dhis.android.core.configuration.ConfigurationManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class App: MultiDexApplication(), Components {

    companion object {
        private lateinit var instance: App
        private val DATABASE_NAME = "dhis.db"

    }

    @Inject
    lateinit var configurationManager: ConfigurationManager

    @NonNull
    @Singleton
    lateinit var appComponent: AppComponent

    @Nullable
    @PerServer
    var serverComponent: ServerComponent? = null

    @Nullable
    @PerUser
    var userComponent: UserComponent? = null

    @Nullable
    @PerActivity
    var formComponent: FormComponent? = null

    @Nullable
    @PerActivity
    var loginComponent: LoginComponent? = null

    @Nullable
    @PerActivity
    var syncComponent: SyncComponent? = null

    @Nullable
    @PerActivity
    private var dashboardComponent: TeiDashboardComponent? = null


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree()) else Timber.plant(ReleaseTree())
        val startTime = System.currentTimeMillis()
        Timber.d("APPLICATION INITIALIZATION")
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            Timber.d("Sthetho initialization end at ${System.currentTimeMillis() - startTime}")
        }

        KujakuLibrary.setEnableMapDownloadResume(false)
        KujakuLibrary.init(this)

        Fabric.with(this, Crashlytics())
        Timber.d("Fabric initialization end at ${System.currentTimeMillis() - startTime}")
        instance = this
        setUpAppComponent()
        setUpServerComponent()
        setUpUserComponent()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            upgradeSecurityProvider()
            Timber.d("Security initialization at ${System.currentTimeMillis() - startTime}")
        }

        val asyncMainThreadScheduler: io.reactivex.Scheduler = AndroidSchedulers.from(Looper.getMainLooper(), true)
        Timber.d("RXJAVAPLUGIN INITIALIZATION END AT %s", System.currentTimeMillis() - startTime)

        Timber.d("APPLICATION INITIALIZATION END AT %s", System.currentTimeMillis() - startTime)
        Timber.d("APPLICATION INITIALIZATION END AT %s", System.currentTimeMillis())

    }

    private fun upgradeSecurityProvider() {
        try {
            ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
                override fun onProviderInstalled() {
                    Timber.e("New security provider installed.")
                }

                override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent) {
                    Timber.e("New security provider install failed.")
                }
            })
        } catch (ex: Exception) {
            Timber.e(ex, "Unknown issue trying to install a new security provider")
        }

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setUpAppComponent() {
        appComponent = prepareAppComponent().build()
        appComponent.inject(this)
    }

    private fun setUpServerComponent() {
        val configuration = configurationManager.get()
        if (configuration != null) {
            serverComponent = appComponent.plus(ServerModule(configuration))
        }
    }

    private fun setUpUserComponent() {
        val userManager = if (serverComponent == null)
            null
        else
            serverComponent!!.userManager()
        if (userManager != null && userManager.isUserLoggedIn.blockingFirst()) {
            userComponent = serverComponent?.plus(UserModule())
        }
    }

    protected fun prepareAppComponent(): AppComponent.Builder {
        return DaggerAppComponent.builder()
                .dbModule(DbModule(DATABASE_NAME))
                .appModule(AppModule(this))
                .schedulerModule(SchedulerModule(SchedulersProviderImpl()))
                .metadataModule(MetadataModule())
                .utilModule(UtilsModule())
    }

    protected fun createAppComponent(): AppComponent {
        appComponent = prepareAppComponent().build()
        return appComponent
    }


    override fun appComponent(): AppComponent {
        return appComponent
    }

    override fun createLoginComponent(): LoginComponent {
        loginComponent = appComponent.plus(LoginModule())
        return loginComponent!!
    }

    override fun loginComponent(): LoginComponent? {
        return loginComponent
    }

    override fun releaseLoginComponent() {
        loginComponent = null
    }

    override fun createSyncComponent(): SyncComponent {
        syncComponent = appComponent.plus(SyncModule())
        return syncComponent!!
    }

    override fun syncComponent(): SyncComponent? {
        return syncComponent
    }

    override fun releaseSyncComponent() {
        syncComponent = null
    }

    override fun createServerComponent(configuration: Configuration): ServerComponent {
        serverComponent = appComponent.plus(ServerModule(configuration))
        return serverComponent!!
    }

    override fun serverComponent(): ServerComponent? {
        return serverComponent
    }

    override fun releaseServerComponent() {
        serverComponent = null
    }

    override fun createUserComponent(): UserComponent {
        userComponent = serverComponent?.plus(UserModule())
        return userComponent!!
    }

    override fun userComponent(): UserComponent? {
        return userComponent
    }

    override fun releaseUserComponent() {
        userComponent = null
    }

    override fun createFormComponent(formModule: FormModule): FormComponent {
        formComponent = userComponent?.plus(formModule)
        return formComponent!!
    }

    override fun formComponent(): FormComponent? {
        return formComponent
    }

    override fun releaseFormComponent() {
        formComponent = null
    }

    fun createDashboardComponent(dashboardModule: TeiDashboardModule): TeiDashboardComponent {
        dashboardComponent = userComponent!!.plus(dashboardModule)
        return dashboardComponent!!
    }

    fun dashboardComponent(): TeiDashboardComponent? {
        return dashboardComponent
    }

    fun releaseDashboardComponent() {
        dashboardComponent = null
    }


}