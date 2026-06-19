package org.dhis2

import android.app.Application
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cat.ereza.customactivityoncrash.config.CaocConfig
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentComponent
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.commons.di.dagger.PerUser
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerComponent
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerModule
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityComponent
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityModule
import org.dhis2.commons.featureconfig.di.FeatureConfigModule
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.network.NetworkUtilsModule
import org.dhis2.commons.orgunitselector.OUTreeComponent
import org.dhis2.commons.orgunitselector.OUTreeModule
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceModule
import org.dhis2.commons.schedulers.SchedulerModule
import org.dhis2.commons.schedulers.SchedulersProviderImpl
import org.dhis2.commons.service.SessionManagerModule
import org.dhis2.commons.sync.SyncComponentProvider
import org.dhis2.data.dispatcher.DispatcherModule
import org.dhis2.data.server.SSLContextInitializer
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.ServerModule
import org.dhis2.data.server.ServerModule.Companion.getD2Configuration
import org.dhis2.data.service.workManager.WorkManagerModule
import org.dhis2.data.user.UserComponent
import org.dhis2.data.user.UserModule
import org.dhis2.di.KoinInitialization.invoke
import org.dhis2.maps.MapController
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.sync.domain.CheckPeriodicJobs
import org.dhis2.usescases.crash.CrashActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent
import org.dhis2.usescases.teiDashboard.TeiDashboardModule
import org.dhis2.utils.analytics.AnalyticsModule
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.dhis2.utils.granularsync.SyncStatusDialogProvider
import org.dhis2.utils.timber.DebugTree
import org.hisp.dhis.android.core.D2Manager
import org.koin.android.ext.android.inject
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.io.IOException
import javax.inject.Singleton

open class App : Application(), Components, DefaultLifecycleObserver {
    @Singleton
    lateinit var appComponent: AppComponent

    @PerServer
    var serverComponent: ServerComponent? = null
        protected set

    @PerUser
    protected var userComponent: UserComponent? = null

    @PerActivity
    private var dashboardComponent: TeiDashboardComponent? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val networkStatusProvider: NetworkStatusProvider by inject()
    private val checkPeriodicJobs: CheckPeriodicJobs by inject()

    private var fromBackGround = false
    private var recreated = false

    override fun onCreate() {
        super<Application>.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        MapController.init(this)

        setUpAppComponent()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }

        setUpSecurityProvider()
        setUpServerComponent()

        this(getD2Configuration(this))

        initCrashController()
        setUpRxPlugin()
        initCustomCrashActivity()
        observeConnectivity()
    }


    private fun observeConnectivity() {
        scope.launch {
            networkStatusProvider.connectionStatus
                .filter { it }
                .collect {
                    checkAndEnqueueSyncJobs()
                }
        }
    }

    private fun checkAndEnqueueSyncJobs() {
        scope.launch {
            checkPeriodicJobs()
        }
    }

    fun initCrashController() {
        if (areTrackingPermissionGranted()) {
            initSentry()
        }
    }

    private fun initSentry() {
        if (BuildConfig.SENTRY_DSN.isEmpty()) {
            Timber.w("Sentry DSN is empty. Skipping Sentry initialization.")
            return
        }
        SentryAndroid.init(this) { options ->
            options.setDsn(BuildConfig.SENTRY_DSN)
            options.isAnrReportInDebug = true

            // Add a callback that will be used before the event is sent to Sentry.
            // With this callback, you can modify the event or, when returning null, also discard the event.
            options.beforeSend =
                SentryOptions.BeforeSendCallback { event, _ ->
                    if (SentryLevel.DEBUG == event.level) null else event
                }
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
            options.isDebug = BuildConfig.DEBUG
            // Enable view hierarchy for crashes
            options.isAttachViewHierarchy = true
            // Enable the performance API by setting a sample-rate
            options.setTracesSampleRate(if (BuildConfig.DEBUG) 1.0 else 0.1)
            // Enable profiling when starting transactions
            options.setProfilesSampleRate(if (BuildConfig.DEBUG) 1.0 else 0.1)
        }
    }

    private fun setUpSecurityProvider() {
        SSLContextInitializer.initializeSSLContext(this)
    }

    private fun initCustomCrashActivity() {
        CaocConfig.Builder.create()
            .errorActivity(CrashActivity::class.java)
            .apply()
    }

    private fun setUpAppComponent() {
        appComponent = prepareAppComponent().build()
        appComponent.inject(this)
    }

    protected open fun setUpServerComponent() {
        serverComponent = appComponent.plus(ServerModule())
        if (serverComponent?.userManager()?.isUserLoggedIn()
                ?.blockingFirst() == true
        ) setUpUserComponent()
    }


    protected open fun setUpUserComponent() {
        serverComponent?.userManager()?.takeIf { it.isUserLoggedIn.blockingFirst() }?.let {
            userComponent = serverComponent!!.plus(UserModule())
        }
    }

    protected open fun prepareAppComponent(): AppComponent.Builder {
        return DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .schedulerModule(SchedulerModule(SchedulersProviderImpl()))
            .analyticsModule(AnalyticsModule())
            .preferenceModule(PreferenceModule())
            .networkUtilsModule(NetworkUtilsModule())
            .workManagerController(WorkManagerModule())
            .sessionManagerService(SessionManagerModule())
            .coroutineDispatchers(DispatcherModule())
            .featureConfigModule(FeatureConfigModule())
    }

    override fun appComponent(): AppComponent {
        return appComponent
    }

    override fun createServerComponent(): ServerComponent {
        if (!D2Manager.isD2Instantiated()) setUpServerComponent()
        return serverComponent!!
    }

    override fun serverComponent(): ServerComponent? {
        return serverComponent
    }

    override fun releaseServerComponent() {
        serverComponent = null
    }

    override fun createUserComponent(): UserComponent {
        return (serverComponent!!.plus(UserModule()).also { userComponent = it })
    }

    override fun userComponent(): UserComponent? {
        return userComponent
    }

    override fun releaseUserComponent() {
        userComponent = null
    }

    fun createDashboardComponent(dashboardModule: TeiDashboardModule): TeiDashboardComponent {
        if (dashboardComponent != null) {
            this.recreated = true
        }
        dashboardComponent = userComponent?.plus(dashboardModule)
        return dashboardComponent!!
    }

    fun dashboardComponent(): TeiDashboardComponent? {
        return dashboardComponent
    }

    fun releaseDashboardComponent() {
        if (!this.recreated) {
            dashboardComponent = null
        } else {
            recreated = false
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        Timber.tag("BG").d("App in background")
    }

    override fun onStart(owner: LifecycleOwner) {
        Timber.tag("BG").d("App in foreground")
        fromBackGround = true
    }

    fun disableBackGroundFlag() {
        fromBackGround = false
    }

    val isSessionBlocked: Boolean
        get() {
            val shouldShowPinDialog =
                fromBackGround && appComponent().preferenceProvider()
                    .getBoolean(Preference.SESSION_LOCKED, false)
            fromBackGround = false
            return shouldShowPinDialog
        }

    private fun setUpRxPlugin() {
        val asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { asyncMainThreadScheduler }
        RxJavaPlugins.setErrorHandler { e ->
            var e = e
            if (e is UndeliverableException) {
                e = e.cause
            }
            if (e is IOException) {
                return@setErrorHandler
            }
            if ((e is NullPointerException) || (e is IllegalArgumentException)) {
                Timber.d("Error in app")
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    e
                )
            }
            if (e is IllegalStateException) {
                Timber.d("Error in RxJava")
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    e
                )
            }
            Timber.d(e)
        }
    }

    override fun provideFeatureConfigActivityComponent(): FeatureConfigActivityComponent? {
        return userComponent?.plus(FeatureConfigActivityModule())
    }

    override fun provideCalendarPickerComponent(): CalendarPickerComponent? {
        return userComponent?.plus(CalendarPickerModule())
    }

    override fun provideAnalyticsFragmentComponent(module: AnalyticsFragmentModule?): AnalyticsFragmentComponent? {
        return userComponent?.plus(module)
    }

    override fun provideFilterPresenter(): FilterPresenter? {
        return userComponent?.filterPresenter()
    }

    override fun provideOUTreeComponent(module: OUTreeModule): OUTreeComponent? {
        return serverComponent?.plus(module)
    }

    override val syncComponentProvider: SyncComponentProvider
        get() = SyncStatusDialogProvider()

    private fun areTrackingPermissionGranted(): Boolean {
        val isUserLoggedIn = serverComponent != null &&
                serverComponent!!.userManager().isUserLoggedIn().blockingFirst()
        if (!D2Manager.isD2Instantiated() || !isUserLoggedIn) {
            return false
        }
        val granted = D2Manager.getD2().dataStoreModule().localDataStore()
            .value(DATA_STORE_ANALYTICS_PERMISSION_KEY).blockingGet()
        return granted != null && granted.value().toBoolean()
    }
}
