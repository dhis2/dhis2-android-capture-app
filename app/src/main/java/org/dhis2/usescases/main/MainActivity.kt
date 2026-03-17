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
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.bindings.hasPermissions
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog.Companion.FILTER_DIALOG
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.ui.NewVersionDialog
import org.dhis2.usescases.main.ui.TAG
import org.dhis2.usescases.main.ui.model.HomeEffect
import org.dhis2.usescases.main.ui.model.HomeScreenState
import org.dhis2.usescases.main.ui.model.VersionToUpdateState
import org.dhis2.usescases.main.ui.screens.MainScreen
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.dhis2.mobile.plugin.domain.LoadPluginsUseCase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.getValue

private const val FRAGMENT = "Fragment"
private const val INIT_DATA_SYNC = "INIT_DATA_SYNC"
private const val WIPE_NOTIFICATION = "wipe_notification"
private const val RESTART = "Restart"
const val AVOID_SYNC = "AvoidSync"

class MainActivity : ActivityGlobalAbstract() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel(parameters = {
        parametersOf(
            context,
            supportFragmentManager,
            intent.getBooleanExtra(AVOID_SYNC, false),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(FRAGMENT, MainScreenType::class.java)
                    ?: MainScreenType.Home(
                        HomeScreen.Programs
                    )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(FRAGMENT) ?: MainScreenType.Home(HomeScreen.Programs)
            }
        )
    })

    private val filtersAdapter: FiltersAdapter = FiltersAdapter()
    private val loadPluginsUseCase: LoadPluginsUseCase by inject()

    private var backDropActive = false
    private val requestWritePermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || granted) {
                onDownloadNewVersion()
            } else {
                Toast
                    .makeText(
                        context,
                        getString(R.string.storage_denied),
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }

    private val navigationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    companion object {
        fun intent(
            context: Context,
            initScreen: MainScreenType? = null,
            launchDataSync: Boolean = false,
        ): Intent =
            Intent(context, MainActivity::class.java).apply {
                putExtras(
                    bundle(
                        initScreen = initScreen,
                        launchDataSync = launchDataSync
                    )
                )
            }

        fun bundle(
            initScreen: MainScreenType? = null,
            launchDataSync: Boolean = false,
        ) = Bundle().apply {
            initScreen?.let {
                putParcelable(FRAGMENT, initScreen)
            }
            putBoolean(INIT_DATA_SYNC, launchDataSync)
        }
    }

    //region LIFECYCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DHIS2Theme {
                val screenState by mainViewModel.homeScreenState.collectAsState()
                MainScreen(
                    screenState = screenState,
                    effects = mainViewModel.homeEffects,
                    onAction = mainViewModel::onAction,
                    onEffect = { effect ->
                        when (effect) {
                            HomeEffect.BlockSession -> finish()
                            is HomeEffect.GoToLogin -> goToLogin(
                                accountsCount = effect.accountsCount,
                                isDeletion = effect.isDeletion,
                            )
                            HomeEffect.OrgUnitFilterRequest -> openOrgUnitTreeSelector()
                            is HomeEffect.PeriodFilterRequest -> showPeriodRequest(effect.periodRequest)
                            HomeEffect.ShowDeleteNotification -> showProgressDeleteNotification()
                            HomeEffect.ShowGranularSync -> showGranularSync()
                            is HomeEffect.SingleProgramNavigation -> navigationLauncher.navigateTo(
                                this@MainActivity,
                                effect.homeItemData,
                            )
                            HomeEffect.ShowPinDialog -> { /*handled in composable*/ }
                            HomeEffect.ToggleSideMenu -> openDrawer()
                            HomeEffect.ToggleFilters -> showHideFilter()
                        }
                    },
                    onNewState = { state ->
                        if (::binding.isInitialized) {
                            updateScreen(state)
                        }
                    },
                    onNewScreen = { currentScreen ->
                        if (backDropActive) {
                            showHideFilter()
                        }
                        val navigationId = when (currentScreen) {
                            MainScreenType.About -> R.id.menu_about
                            is MainScreenType.Home -> R.id.menu_home
                            MainScreenType.Loading -> null
                            MainScreenType.QRScanner -> R.id.qr_scan
                            MainScreenType.Settings -> R.id.sync_manager
                            MainScreenType.TroubleShooting -> R.id.menu_troubleshooting
                        }
                        navigationId?.let {
                            changeFragment(it)
                            initCurrentScreen()
                        }
                    },
                    onLayoutInflated = {
                        binding = it
                        initBinding()
                    },
                )
            }
        }
    }

    private fun initBinding() {
        binding.navView.setNavigationItemSelectedListener { item ->
            changeFragment(item.itemId)
            false
        }

        binding.mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

            override fun onDrawerOpened(drawerView: View) = Unit

            override fun onDrawerClosed(drawerView: View) {
                initCurrentScreen()
            }

            override fun onDrawerStateChanged(newState: Int) = Unit
        })

        binding.filterRecycler.adapter = filtersAdapter

        if (BuildConfig.DEBUG || BuildConfig.FLAVOR == "dhis2Training") {
            binding.navView.menu
                .findItem(R.id.menu_troubleshooting)
                .isVisible = true
            binding.navView.menu
                .findItem(R.id.menu_dev)
                .isVisible = true
        }

        lifecycleScope.launch { loadPluginsUseCase(Unit) }

        checkNotificationPermission()
    }

    private fun updateScreen(screenState: HomeScreenState) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.user_info)
            .text = screenState.userName

        if (screenState.homeFilters.isNotEmpty()) {
            filtersAdapter.submitList(screenState.homeFilters)
        }

        when (val versionState = screenState.versionToUpdate) {
            VersionToUpdateState.Downloading ->
                binding.toolbarProgress.show()

            is VersionToUpdateState.New ->
                if (supportFragmentManager.findFragmentByTag(TAG) == null) {
                    showNewVersionAlert(versionState.version)
                }

            VersionToUpdateState.None ->
                binding.toolbarProgress.hide()
        }
    }

    /*TODO: MOVE TO BE HANDLED AFTER LOGIN*/
    private fun checkNotificationPermission() {
        if (!hasPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) and
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showHideFilter() {
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
                50,
            )
            mainViewModel.updateNavigationBarVisibility(false)
        } else {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.toolbarProgress,
                ConstraintSet.BOTTOM,
                0,
            )
            mainViewModel.updateNavigationBarVisibility(true)
        }
        initSet.applyTo(binding.backdropLayout)
    }

    private fun showGranularSync() {
        SyncStatusDialog
            .Builder()
            .withContext(this)
            .withSyncContext(SyncContext.Global())
            .onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        mainViewModel.onGranularSyncFinished(hasChanged)
                    }
                },
            ).onNoConnectionListener {
                val contextView = findViewById<View>(R.id.navigationBar)
                Snackbar
                    .make(
                        contextView,
                        R.string.sync_offline_check_connection,
                        Snackbar.LENGTH_SHORT,
                    ).show()
            }.show("ALL_SYNC")
    }

    private fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            FilterPeriodsDialog
                .newPeriodsFilter(filterType = Filters.PERIOD, isFromToFilter = true)
                .show(supportFragmentManager, FILTER_DIALOG)
        } else {
            FilterPeriodsDialog
                .newPeriodsFilter(filterType = Filters.PERIOD)
                .show(supportFragmentManager, FILTER_DIALOG)
        }
    }

    private fun openOrgUnitTreeSelector() {
        OUTreeFragment
            .Builder()
            .withPreselectedOrgUnits(
                FilterManager
                    .getInstance()
                    .orgUnitFilters
                    .map { it.uid() }
                    .toMutableList(),
            ).onSelection(mainViewModel::setOrgUnitFilters)
            .build()
            .show(supportFragmentManager, "OUTreeFragment")
    }

    private fun goToLogin(
        accountsCount: Int,
        isDeletion: Boolean,
    ) {
        startActivity(
            LoginActivity::class.java,
            LoginActivity.bundle(
                accountsCount = accountsCount,
                isDeletion = isDeletion,
                fromMainActivity = true,
            ),
            finishCurrent = true,
            finishAll = true,
            transition = null,
        )
    }

    private fun openDrawer() {
        if (!binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.openDrawer(GravityCompat.START)
        } else {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun changeFragment(id: Int) {
        binding.navView.setCheckedItem(id)
        binding.mainDrawerLayout.closeDrawers()
    }

    private fun initCurrentScreen() {
        when (binding.navView.checkedItem?.itemId) {
            R.id.sync_manager ->
                mainViewModel.onChangeScreen(MainScreenType.Settings)

            R.id.qr_scan ->
                mainViewModel.onChangeScreen(MainScreenType.QRScanner)

            R.id.menu_about ->
                mainViewModel.onChangeScreen(MainScreenType.About)

            R.id.block_button ->
                mainViewModel.onBlockSession()

            R.id.logout_button ->
                mainViewModel.logOut()

            R.id.menu_home ->
                mainViewModel.onChangeToHome()

            R.id.menu_troubleshooting ->
                mainViewModel.onChangeScreen(MainScreenType.TroubleShooting)

            R.id.menu_dev ->
                startActivity(Intent(this, DevelopmentActivity::class.java))

            R.id.delete_account ->
                confirmAccountDelete()

        }
    }

    private fun confirmAccountDelete() {
        MaterialAlertDialogBuilder(this, R.style.MaterialDialog)
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.wipe_data_meesage))
            .setView(R.layout.warning_layout)
            .setPositiveButton(getString(R.string.wipe_data_ok)) { _, _ -> mainViewModel.onDeleteAccount() }
            .setNegativeButton(getString(R.string.wipe_data_no)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showProgressDeleteNotification() {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel =
                NotificationChannel(
                    WIPE_NOTIFICATION,
                    RESTART,
                    NotificationManager.IMPORTANCE_HIGH,
                )
            notificationManager.createNotificationChannel(mChannel)
        }
        val notificationBuilder =
            NotificationCompat
                .Builder(context, WIPE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_sync)
                .setContentTitle(getString(R.string.wipe_data))
                .setContentText(getString(R.string.please_wait))
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(123456, notificationBuilder.build())
    }

    private fun showNewVersionAlert(version: String) {
        NewVersionDialog(
            newVersion = version,
            onRemindMeLater = mainViewModel::remindLaterAlertNewVersion,
            onDownloadVersion = {
                if (hasPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    onDownloadNewVersion()
                } else {
                    requestWritePermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            },
        ).show(supportFragmentManager)
    }

    private fun onDownloadNewVersion() {
        mainViewModel.downloadVersion(
            onDownloadCompleted = ::installAPK,
            onLaunchUrl = ::launchUrl,
        )
    }

    private fun installAPK(apkUri: Uri) {
        when {
            hasNoPermissionToInstall() ->
                manageUnknownSources.launch(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(String.format("package:%s", packageName).toUri()),
                )

            !hasPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) && Build.VERSION.SDK_INT < Build.VERSION_CODES.R ->
                requestReadStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            else ->
                Intent(Intent.ACTION_VIEW).apply {
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
                Toast
                    .makeText(
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                onDownloadNewVersion()
            } else if (granted) {
                onDownloadNewVersion()
            } else {
                Toast
                    .makeText(
                        context,
                        getString(R.string.storage_denied),
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val message = if (granted) {
                getString(R.string.permission_notification_granted)
            } else {
                getString(R.string.permission_notification_denied)
            }

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    private fun launchUrl(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    /** Required by QrReaderFragment. It should retrieve the activity mainViewModel and call
     * onChangeToHome directly
     * */
    fun goToHome() {
        mainViewModel.onChangeToHome()
    }
}
