package org.dhis2.usescases.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.hasPermissions
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.ui.dialogs.alert.AlertDialog
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.extension.navigateTo
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import java.io.File
import javax.inject.Inject

private const val FRAGMENT = "Fragment"
private const val SINGLE_PROGRAM_NAVIGATION = "SINGLE_PROGRAM_NAVIGATION"
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
    lateinit var pageConfigurator: NavigationPageConfigurator

    private var singleProgramNavigationDone = false

    private val getDevActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // no-op
        }

    private val requestWritePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                onDownloadNewVersion()
            }
        }

    private var isPinLayoutVisible = false

    private val mainNavigator = MainNavigator(
        supportFragmentManager,
        { /*no-op*/ },
    ) { titleRes, _, showBottomNavigation ->
        setTitle(getString(titleRes))
        setBottomNavigationVisibility(showBottomNavigation)
    }

    private val navigationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    companion object {
        fun intent(
            context: Context,
            initScreen: MainNavigator.MainScreen? = null,
            launchDataSync: Boolean = false,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                initScreen?.let {
                    putExtra(FRAGMENT, initScreen.name)
                }
                putExtra(INIT_DATA_SYNC, launchDataSync)
            }
        }

        fun bundle(initScreen: MainNavigator.MainScreen? = null, launchDataSync: Boolean = false) =
            Bundle().apply {
                initScreen?.let {
                    putString(FRAGMENT, initScreen.name)
                }
                putBoolean(INIT_DATA_SYNC, launchDataSync)
            }
    }

    //region LIFECYCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        app().userComponent()?.let {
            mainComponent = it.plus(
                MainModule(
                    view = this,
                    forceToNotSynced = intent.getBooleanExtra(AVOID_SYNC, false),
                ),
            )
            mainComponent.inject(this@MainActivity)
        } ?: navigateTo<LoginActivity>(true)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

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

        setUpNavigationBar()
        setUpDevelopmentMode()

        val restoreScreenName = savedInstanceState?.getString(FRAGMENT)
        singleProgramNavigationDone =
            savedInstanceState?.getBoolean(SINGLE_PROGRAM_NAVIGATION) ?: false
        val openScreen = intent.getStringExtra(FRAGMENT)

        when {
            openScreen != null || restoreScreenName != null -> {
                changeFragment(
                    mainNavigator.currentNavigationViewItemId(
                        openScreen ?: restoreScreenName!!,
                    ),
                )
                mainNavigator.restoreScreen(
                    screenToRestoreName = openScreen ?: restoreScreenName!!,
                    languageSelectorOpened = openScreen != null &&
                        MainNavigator.MainScreen.valueOf(openScreen) ==
                        MainNavigator.MainScreen.TROUBLESHOOTING,
                )
            }

            else -> {
                changeFragment(R.id.menu_home)
                initCurrentScreen()
            }
        }

        observeSyncState()
        observeVersionUpdate()

        if (!presenter.wasSyncAlreadyDone()) {
            presenter.launchInitialDataSync()
        } else if (!singleProgramNavigationDone && presenter.hasOneHomeItem()) {
            navigateToSingleProgram()
        }

        checkNotificationPermission()

        registerOnBackPressedCallback()
    }

    private fun checkNotificationPermission() {
        if (!hasPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) and
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SINGLE_PROGRAM_NAVIGATION, singleProgramNavigationDone)
        outState.putString(FRAGMENT, mainNavigator.currentScreenName())
    }

    override fun onResume() {
        super.onResume()
        if (sessionManagerServiceImpl.isUserLoggedIn()) {
            presenter.init()
        }
    }

    override fun onPause() {
        presenter.setOpeningFilterToNone()
        presenter.onDetach()
        super.onPause()
    }

    private fun setUpDevelopmentMode() {
        if (BuildConfig.DEBUG) {
            binding.menu.setOnLongClickListener {
                getDevActivityContent.launch(Intent(this, DevelopmentActivity::class.java))
                false
            }
        }
    }

    private fun registerOnBackPressedCallback() {
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                backPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(this) {
                backPressed()
            }
        }
    }

    private fun setUpNavigationBar() {
        binding.navigationBar.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val currentScreen by mainNavigator.selectedScreen.observeAsState()
                val selectedItemIndex by remember {
                    derivedStateOf {
                        when (currentScreen) {
                            MainNavigator.MainScreen.PROGRAMS -> 0
                            MainNavigator.MainScreen.VISUALIZATIONS -> 1
                            else -> null
                        }
                    }
                }
                NavigationBar(
                    items = pageConfigurator.navigationItems(),
                    selectedItemIndex = selectedItemIndex ?: 0,
                ) { navigationPage ->
                    when (navigationPage) {
                        NavigationPage.ANALYTICS -> {
                            presenter.trackHomeAnalytics()
                            mainNavigator.openVisualizations()
                        }

                        NavigationPage.PROGRAMS -> mainNavigator.openPrograms()
                        else -> {
                            /*no-op*/
                        }
                    }
                }
            }
        }
    }

    private fun observeSyncState() {
        presenter.observeDataSync().observe(this) {
            when (it.running) {
                true -> {
                    setBottomNavigationVisibility(false)
                }

                false -> {
                    setBottomNavigationVisibility(true)
                    presenter.onDataSuccess()
                    if (presenter.hasOneHomeItem()) {
                        navigateToSingleProgram()
                    }
                }

                else -> {
                    // no action
                }
            }
        }
    }

    private fun navigateToSingleProgram() {
        presenter.getSingleItemData()?.let { homeItemData ->
            singleProgramNavigationDone = true
            navigationLauncher.navigateTo(this, homeItemData)
        }
    }

    private fun observeVersionUpdate() {
        presenter.versionToUpdate.observe(this) { versionName ->
            versionName?.takeIf { it.isNotEmpty() }?.let { showNewVersionAlert(it) }
        }
        presenter.downloadingVersion.observe(this) { downloading ->
            if (downloading) {
                binding.toolbarProgress.show()
            } else {
                binding.toolbarProgress.hide()
            }
        }
    }

    override fun showGranularSync() {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(SyncContext.Global())
            .onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            mainNavigator.getCurrentIfProgram()?.programViewModel?.updateProgramQueries()
                        }
                    }
                },
            )
            .onNoConnectionListener {
                val contextView = findViewById<View>(R.id.navigationBar)
                Snackbar.make(
                    contextView,
                    R.string.sync_offline_check_connection,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
            .show("ALL_SYNC")
    }

    override fun goToLogin(accountsCount: Int, isDeletion: Boolean) {
        startActivity(
            LoginActivity::class.java,
            LoginActivity.bundle(
                accountsCount = accountsCount,
                isDeletion = isDeletion,
            ),
            true,
            true,
            null,
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

    override fun onLockClick() {
        if (!presenter.isPinStored()) {
            binding.mainDrawerLayout.closeDrawers()
            PinDialog(
                PinDialog.Mode.SET,
                true,
                { presenter.blockSession() },
                {},
            ).show(supportFragmentManager, PIN_DIALOG_TAG)
            isPinLayoutVisible = true
        } else {
            presenter.blockSession()
        }
    }

    private fun backPressed() {
        when {
            !mainNavigator.isHome() -> presenter.onNavigateBackToHome()
            isPinLayoutVisible -> isPinLayoutVisible = false
        }
    }

    override fun goToHome() {
        mainNavigator.openHome()
    }

    override fun changeFragment(id: Int) {
        binding.navView.setCheckedItem(id)
        binding.mainDrawerLayout.closeDrawers()
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    private fun setBottomNavigationVisibility(showBottomNavigation: Boolean) {
        if (showBottomNavigation) {
            binding.navigationBar.show()
        } else {
            binding.navigationBar.hide()
        }
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        initCurrentScreen()
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
                mainNavigator.openHome()
            }

            R.id.menu_troubleshooting -> {
                mainNavigator.openTroubleShooting()
            }

            R.id.delete_account -> {
                confirmAccountDelete()
            }
        }
    }

    private fun confirmAccountDelete() {
        MaterialAlertDialogBuilder(this, R.style.MaterialDialog)
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
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                WIPE_NOTIFICATION,
                RESTART,
                NotificationManager.IMPORTANCE_HIGH,
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

    override fun cancelNotifications() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun showNewVersionAlert(version: String) {
        AlertDialog(
            labelText = getString(R.string.software_update),
            descriptionText = getString(R.string.new_version_message).format(version),
            iconResource = R.drawable.ic_software_update,
            spanText = version,
            dismissButton = ButtonUiModel(
                getString(R.string.remind_me_later),
                onClick = { presenter.remindLaterAlertNewVersion() },
            ),
            confirmButton = ButtonUiModel(
                getString(R.string.download_now),
                onClick = {
                    if (hasPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        onDownloadNewVersion()
                    } else {
                        requestWritePermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                },
            ),
        ).show(supportFragmentManager)
    }

    private fun onDownloadNewVersion() {
        presenter.downloadVersion(
            context = context,
            onDownloadCompleted = ::installAPK,
            onLaunchUrl = ::launchUrl,
        )
    }

    private fun installAPK(apkUri: Uri) {
        when {
            hasNoPermissionToInstall() ->
                manageUnknownSources.launch(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", packageName))),
                )

            !hasPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) ->
                requestReadStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            else -> Intent(Intent.ACTION_VIEW).apply {
                val mime = MimeTypeMap.getSingleton()
                val ext = apkUri.path?.substringAfterLast(("."))
                val type: String? = mime.getMimeTypeFromExtension(ext)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(apkUri, type)
                startActivity(this)
            }
        }
    }

    private fun hasNoPermissionToInstall(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !packageManager.canRequestPackageInstalls()

    private val manageUnknownSources =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasNoPermissionToInstall()) {
                Toast.makeText(
                    context,
                    getString(R.string.unknow_sources_denied),
                    Toast.LENGTH_LONG,
                ).show()
            } else {
                onDownloadNewVersion()
            }
        }

    private val requestReadStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onDownloadNewVersion()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.storage_denied),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(
                    context,
                    getString(R.string.permission_notification_granted),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permission_notification_denied),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    private fun launchUrl(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}
