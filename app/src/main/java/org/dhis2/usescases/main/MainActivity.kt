package org.dhis2.usescases.main

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import java.io.File
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.extension.navigateTo
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog

private const val FRAGMENT = "Fragment"
private const val INIT_DATA_SYNC = "INIT_DATA_SYNC"
private const val WIPE_NOTIFICATION = "wipe_notification"
private const val RESTART = "Restart"
const val AVOID_SYNC = "AvoidSync"

class MainActivity :
    ActivityGlobalAbstract(),
    MainView,
    DrawerLayout.DrawerListener {

    private lateinit var binding: ActivityMainBinding
    lateinit var mainComponent: MainComponent

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var newAdapter: FiltersAdapter

    @Inject
    lateinit var pageConfigurator: NavigationPageConfigurator

    var notification: Boolean = false
    var forceToNotSynced = false

    private val getDevActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.navigationBar.pageConfiguration(pageConfigurator)
        }

    private var isPinLayoutVisible = false

    private var prefs: SharedPreferences? = null
    private var backDropActive = false
    private var elevation = 0f
    private val mainNavigator = MainNavigator(
        supportFragmentManager,
        {
            if (backDropActive) {
                showHideFilter()
            }
        }
    ) { titleRes, showFilterButton, showBottomNavigation ->
        setTitle(getString(titleRes))
        setFilterButtonVisibility(showFilterButton)
        setBottomNavigationVisibility(showBottomNavigation)
    }

    companion object {
        fun intent(
            context: Context,
            initScreen: MainNavigator.MainScreen? = null,
            launchDataSync: Boolean = false
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                initScreen?.let {
                    putExtra(FRAGMENT, initScreen.name)
                }
                putExtra(INIT_DATA_SYNC, launchDataSync)
            }
        }

        fun bundle(
            initScreen: MainNavigator.MainScreen? = null,
            launchDataSync: Boolean = false
        ) = Bundle().apply {
            initScreen?.let {
                putString(FRAGMENT, initScreen.name)
            }
            putBoolean(INIT_DATA_SYNC, launchDataSync)
        }
    }

    //region LIFECYCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        app().userComponent()?.let {
            mainComponent = it.plus(MainModule(this)).apply {
                inject(this@MainActivity)
            }
        } ?: navigateTo<LoginActivity>(true)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        forceToNotSynced = intent.getBooleanExtra(AVOID_SYNC, false)
        if (::presenter.isInitialized) {
            binding.presenter = presenter
        } else {
            navigateTo<LoginActivity>(true)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            changeFragment(item.itemId)
            false
        }

        binding.mainDrawerLayout.addDrawerListener(this)

        prefs = abstracContext.getSharedPreferences(
            Constants.SHARE_PREFS, Context.MODE_PRIVATE
        )

        binding.filterRecycler.adapter = newAdapter

        binding.navigationBar.pageConfiguration(pageConfigurator)
        binding.navigationBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_tasks -> {
                }
                R.id.navigation_programs -> {
                    mainNavigator.openPrograms()
                }
                R.id.navigation_analytics -> {
                    presenter.trackHomeAnalytics()
                    mainNavigator.openVisualizations()
                }
            }
            true
        }

        if (BuildConfig.DEBUG) {
            binding.menu.setOnLongClickListener {
                getDevActivityContent.launch(Intent(this, DevelopmentActivity::class.java))
                false
            }
        }

        elevation = ViewCompat.getElevation(binding.toolbar)

        val restoreScreenName = savedInstanceState?.getString(FRAGMENT)
        val openScreen = intent.getStringExtra(FRAGMENT)

        when {
            openScreen != null || restoreScreenName != null -> {
                changeFragment(
                    mainNavigator.currentNavigationViewItemId(
                        openScreen ?: restoreScreenName!!
                    )
                )
                mainNavigator.restoreScreen(
                    screenToRestoreName = openScreen ?: restoreScreenName!!,
                    languageSelectorOpened = openScreen != null &&
                        MainNavigator.MainScreen.valueOf(openScreen) ==
                        MainNavigator.MainScreen.TROUBLESHOOTING
                )
            }
            else -> {
                changeFragment(R.id.menu_home)
                initCurrentScreen()
            }
        }

        observeSyncState()

        if (!presenter.wasSyncAlreadyDone()) {
            presenter.launchInitialDataSync()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FRAGMENT, mainNavigator.currentScreenName())
    }

    override fun onResume() {
        super.onResume()

        presenter.init()
        presenter.initFilters()

        binding.totalFilters = FilterManager.getInstance().totalFilters
    }

    override fun onPause() {
        presenter.setOpeningFilterToNone()
        presenter.onDetach()
        super.onPause()
    }

    private fun observeSyncState() {
        presenter.observeDataSync().observe(this) {
            if (it.running) {
                setFilterButtonVisibility(false)
                setBottomNavigationVisibility(false)
            } else {
                setFilterButtonVisibility(true)
                setBottomNavigationVisibility(true)
                presenter.onDataSuccess()
            }
        }
    }

    override fun showGranularSync() {
        SyncStatusDialog.Builder()
            .setConflictType(ConflictType.ALL)
            .setUid("")
            .onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            mainNavigator.getCurrentIfProgram()?.presenter?.updateProgramQueries()
                        }
                    }
                })
            .build().show(supportFragmentManager, "ALL_SYNC")
    }

    override fun goToLogin(accountsCount: Int, isDeletion: Boolean) {
        startActivity(
            LoginActivity::class.java,
            LoginActivity.bundle(
                accountsCount = accountsCount,
                isDeletion = isDeletion
            ),
            true,
            true,
            null
        )
    }

    override fun renderUsername(username: String) {
        binding.userName = username
        (binding.navView.getHeaderView(0).findViewById<View>(R.id.user_info) as TextView)
            .text = username
        binding.executePendingBindings()
    }

    override fun openDrawer(gravity: Int) {
        if (!binding.mainDrawerLayout.isDrawerOpen(gravity)) {
            binding.mainDrawerLayout.openDrawer(gravity)
        } else {
            binding.mainDrawerLayout.closeDrawer(gravity)
        }
    }

    override fun showHideFilter() {
        val transition = ChangeBounds()
        transition.duration = 200
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition)
        backDropActive = !backDropActive
        val initSet = ConstraintSet()
        initSet.clone(binding.backdropLayout)
        if (backDropActive) {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.filterRecycler,
                ConstraintSet.BOTTOM,
                50
            )
            binding.navigationBar.hide()
        } else {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.toolbar,
                ConstraintSet.BOTTOM,
                0
            )
            binding.navigationBar.show()
        }
        initSet.applyTo(binding.backdropLayout)
        mainNavigator.getCurrentIfProgram()?.openFilter(backDropActive)
    }

    override fun onLockClick() {
        if (prefs!!.getString(Preference.PIN, null) == null) {
            binding.mainDrawerLayout.closeDrawers()
            PinDialog(
                PinDialog.Mode.SET,
                true,
                { presenter.blockSession() },
                {}
            ).show(supportFragmentManager, PIN_DIALOG_TAG)
            isPinLayoutVisible = true
        } else {
            presenter.blockSession()
        }
    }

    override fun onBackPressed() {
        when {
            !mainNavigator.isHome() -> presenter.onNavigateBackToHome()
            isPinLayoutVisible -> isPinLayoutVisible = false
            else -> super.onBackPressed()
        }
    }

    override fun goToHome() {
        mainNavigator.openHome(binding.navigationBar)
    }

    override fun changeFragment(id: Int) {
        binding.navView.setCheckedItem(id)
        binding.mainDrawerLayout.closeDrawers()
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
    }

    override fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance()
                .fromCalendarSelector(this) { FilterManager.getInstance().addPeriod(it) }
        } else {
            DateUtils.getInstance()
                .showPeriodDialog(
                    this,
                    { datePeriods -> FilterManager.getInstance().addPeriod(datePeriods) },
                    true
                )
        }
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    private fun setFilterButtonVisibility(showFilterButton: Boolean) {
        binding.filterActionButton.visibility = if (showFilterButton) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.syncActionButton.visibility = if (showFilterButton) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setBottomNavigationVisibility(showBottomNavigation: Boolean) {
        if (showBottomNavigation) {
            binding.navigationBar.show()
        } else {
            binding.navigationBar.hide()
        }
    }

    override fun setFilters(filters: List<FilterItem>) {
        newAdapter.submitList(filters)
    }

    override fun hideFilters() {
        binding.filterActionButton.visibility = View.GONE
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        initCurrentScreen()
        if (mainNavigator.isPrograms() && !isNotificationRunning()) {
            presenter.initFilters()
        }
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    private fun initCurrentScreen() {
        when (binding.navView.checkedItem?.itemId) {
            R.id.sync_manager -> {
                presenter.onClickSyncManager()
                mainNavigator.openSettings()
            }
            R.id.qr_scan -> {
                presenter.trackQRScanner()
                mainNavigator.openQR()
            }
            R.id.menu_jira -> {
                presenter.trackJiraReport()
                mainNavigator.openJira()
            }
            R.id.menu_about -> {
                mainNavigator.openAbout()
            }
            R.id.block_button -> {
                presenter.trackPinDialog()
                onLockClick()
            }
            R.id.logout_button -> {
                analyticsHelper.setEvent(CLOSE_SESSION, CLICK, CLOSE_SESSION)
                presenter.logOut()
            }
            R.id.menu_home -> {
                mainNavigator.openHome(binding.navigationBar)
            }
            R.id.menu_troubleshooting -> {
                mainNavigator.openTroubleShooting()
            }
            R.id.delete_account -> {
                confirmAccountDelete()
            }
        }

        if (backDropActive && mainNavigator.isPrograms()) {
            showHideFilter()
        }
    }

    private fun confirmAccountDelete() {
        AlertDialog.Builder(context, R.style.CustomDialog)
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.wipe_data_meesage))
            .setView(R.layout.warning_layout)
            .setPositiveButton(getString(R.string.wipe_data_ok)) { _, _ ->
                presenter.onDeleteAccount()
            }
            .setNegativeButton(getString(R.string.wipe_data_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun showProgressDeleteNotification() {
        notification = true
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                WIPE_NOTIFICATION,
                RESTART,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, WIPE_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(getString(R.string.wipe_data))
            .setContentText(getString(R.string.please_wait))
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(123456, notificationBuilder.build())
    }

    override fun obtainFileView(): File? {
        return this.cacheDir
    }

    private fun isNotificationRunning(): Boolean {
        return notification
    }

    override fun hasToNotSync(): Boolean {
        return forceToNotSynced
    }

    override fun cancelNotifications() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
