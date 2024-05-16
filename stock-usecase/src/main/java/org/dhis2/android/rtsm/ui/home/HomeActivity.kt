package org.dhis2.android.rtsm.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import com.google.android.material.composethemeadapter.MdcTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.screens.HomeScreen
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.android.rtsm.utils.NetworkUtils
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.OnSyncNavigationListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncDialog
import org.dhis2.commons.sync.SyncStatusItem

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private val manageStockViewModel: ManageStockViewModel by viewModels()
    private var themeColor = R.color.colorPrimary
    private lateinit var filterManager: FilterManager
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manageStockViewModel.shouldCloseActivity.observe(this) { finish() }
        manageStockViewModel.shouldNavigateBack.observe(this) {
            onBackPressedDispatcher.onBackPressed()
        }

        filterManager = FilterManager.getInstance()
        intent.getParcelableExtra<AppConfig>(INTENT_EXTRA_APP_CONFIG)
            ?.let { manageStockViewModel.setConfig(it) }

        setContent {
            val settingsUiState by viewModel.settingsUiState.collectAsState()
            updateTheme(settingsUiState.selectedTransactionItem.type)
            manageStockViewModel.setThemeColor(Color(colorResource(themeColor).toArgb()))
            MdcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(colorResource(themeColor).toArgb()),
                ) {
                    HomeScreen(
                        this,
                        viewModel,
                        manageStockViewModel,
                        Color(colorResource(themeColor).toArgb()),
                        supportFragmentManager,
                        barcodeLauncher,
                        { _, _ -> manageStockViewModel.onButtonClick() },
                    ) { scope, scaffold ->
                        synchronizeData(
                            scope,
                            scaffold,
                            settingsUiState.programUid,
                        )
                    }
                }
            }
        }

        configureScanner()
    }

    private fun updateTheme(type: TransactionType) {
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
        if (color != -1) {
            this.theme.applyStyle(theme, true)

            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val typedValue = TypedValue()
            val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
            val colorToReturn = a.getColor(0, 0)
            a.recycle()
            window.statusBarColor = colorToReturn
            themeColor = color
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
                dismissListener = object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        manageStockViewModel.refreshData()
                    }
                },
                onSyncNavigationListener = object : OnSyncNavigationListener {
                    override fun intercept(
                        syncStatusItem: SyncStatusItem,
                        intent: Intent,
                    ): Intent? {
                        return null
                    }
                },
            ).show()
        }
    }

    private fun showSnackBar(scope: CoroutineScope, scaffoldState: ScaffoldState, message: String) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    private fun configureScanner() {
        val barcodeLauncher: ActivityResultLauncher<ScanOptions> =
            registerForActivityResult(
                ScanContract(),
            ) { scanIntentResult ->
                if (scanIntentResult.contents == null) {
                    Toast.makeText(this, "Scan cancelled!", Toast.LENGTH_SHORT).show()
                } else {
                    onScanCompleted(
                        scanIntentResult,
                    )
                }
            }
        this.barcodeLauncher = barcodeLauncher
    }

    private fun onScanCompleted(result: ScanIntentResult) {
        val data = result.contents
        manageStockViewModel.onSearchQueryChanged(data)
    }
}
