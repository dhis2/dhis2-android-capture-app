package org.dhis2.android.rtsm.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.screens.HomeScreen
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.android.rtsm.utils.NetworkUtils
import org.dhis2.commons.Constants
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.OnSyncNavigationListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncDialog
import org.dhis2.commons.sync.SyncStatusItem
import org.dhis2.mobile.commons.extensions.toColor
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModel()
    private val manageStockViewModel: ManageStockViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manageStockViewModel.shouldCloseActivity.observe(this) { finish() }
        manageStockViewModel.shouldNavigateBack.observe(this) {
            onBackPressedDispatcher.onBackPressed()
        }

        intent
            .getStringExtra(Constants.PROGRAM_UID)
            ?.let { manageStockViewModel.setConfig(it) }

        enableEdgeToEdge()
        setContent {
            val settingsUiState by viewModel.settingsUiState.collectAsState()
            val themeColor by remember {
                derivedStateOf {
                    updateTheme(settingsUiState.selectedTransactionItem.type)
                }
            }
            val helperText by viewModel.helperText.collectAsState()
            manageStockViewModel.setHelperText(helperText)
            DHIS2Theme {
                CompositionLocalProvider(
                    LocalThemeColor provides themeColor,
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        manageStockViewModel = manageStockViewModel,
                        supportFragmentManager = supportFragmentManager,
                        proceedAction = { _, _ -> manageStockViewModel.onButtonClick() },
                        onFinish = {
                            finish()
                        },
                        syncAction = { scope, scaffold ->
                            synchronizeData(
                                scope,
                                scaffold,
                                settingsUiState.programUid,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun updateTheme(type: TransactionType): Color {
        val color: Int
        val theme: Int

        when (type) {
            TransactionType.DISTRIBUTION -> {
                color = R.color.colorPrimary
                theme = R.style.AppTheme
            }

            TransactionType.DISCARD -> {
                color = R.color.discard_color
                theme = R.style.discard
            }

            TransactionType.CORRECTION -> {
                color = R.color.correction_color
                theme = R.style.correction
            }
        }
        return if (color != -1) {
            this.theme.applyStyle(theme, true)

            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val typedValue = TypedValue()
            val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
            val colorToReturn = a.getColor(0, 0)
            a.recycle()
            window.statusBarColor = colorToReturn
            Color(ContextCompat.getColor(this, color))
        } else {
            Color(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }

    private fun synchronizeData(
        scope: CoroutineScope,
        scaffoldState: ScaffoldState,
        programUid: String,
    ) {
        val isNetworkAvailable: Boolean = NetworkUtils.isOnline(this@HomeActivity)
        if (!isNetworkAvailable) {
            showSnackBar(
                scope,
                scaffoldState,
                getString(R.string.unable_to_sync_data_no_network_available),
            )
        } else {
            SyncDialog(
                activity = this@HomeActivity,
                recordUid = programUid,
                syncContext = SyncContext.TrackerProgram(programUid),
                dismissListener =
                    object : OnDismissListener {
                        override fun onDismiss(hasChanged: Boolean) {
                            manageStockViewModel.refreshData()
                        }
                    },
                onSyncNavigationListener =
                    object : OnSyncNavigationListener {
                        override fun intercept(
                            syncStatusItem: SyncStatusItem,
                            intent: Intent,
                        ): Intent? = null
                    },
            ).show()
        }
    }

    private fun showSnackBar(
        scope: CoroutineScope,
        scaffoldState: ScaffoldState,
        message: String,
    ) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }
}

val LocalThemeColor = compositionLocalOf { "#FF007DEB".toColor() }
