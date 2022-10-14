package org.dhis2.android.rtsm.ui.home

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.core.content.ContextCompat.startActivity
import androidx.work.WorkInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INSTANT_DATA_SYNC
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.screens.HomeScreen
import org.dhis2.android.rtsm.ui.home.screens.components.MainContent
import org.dhis2.android.rtsm.ui.managestock.ManageStockActivity
import org.dhis2.android.rtsm.utils.NetworkUtils

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private var themeColor = R.color.colorPrimary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                updateTheme(viewModel.transactionType.collectAsState().value)
                HomeScreen(
                    this, viewModel, Color(colorResource(themeColor).toArgb()),
                    { scope, scaffold -> navigateToManageStock(scope, scaffold) },
                    { scope, scaffold -> synchronizeData(scope, scaffold) }
                )
            }
        }
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
        scaffoldState: ScaffoldState
    ) {
        val isNetworkAvailable: Boolean = NetworkUtils.isOnline(this@HomeActivity)
        if (!isNetworkAvailable) {
            showSnackBar(
                scope, scaffoldState,
                getString(R.string.unable_to_sync_data_no_network_available)
            )
        } else {
            viewModel.syncData()
            viewModel.getSyncDataStatus().observe(
                this@HomeActivity
            ) { workInfoList ->
                workInfoList.forEach { workInfo ->
                    if (workInfo.tags.contains(INSTANT_DATA_SYNC)) {
                        handleDataSyncResponse(workInfo, scope, scaffoldState)
                    }
                }
            }
        }
    }

    private fun handleDataSyncResponse(
        workInfo: WorkInfo,
        scope: CoroutineScope,
        scaffoldState: ScaffoldState
    ) {
        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                showSnackBar(scope, scaffoldState, getString(R.string.data_sync_in_progress))
            }
            WorkInfo.State.SUCCEEDED -> {
                showSnackBar(scope, scaffoldState, getString(R.string.sync_completed))
            }
            WorkInfo.State.FAILED -> {
                showSnackBar(scope, scaffoldState, getString(R.string.data_sync_error))
            }
            else -> {}
        }
    }

    private fun showSnackBar(
        scope: CoroutineScope,
        scaffoldState: ScaffoldState,
        message: String
    ) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    private fun navigateToManageStock(
        scope: CoroutineScope,
        scaffoldState: ScaffoldState
    ) {
        val fieldError = viewModel.checkForFieldErrors()
        if (fieldError != null) {
            scope.launch {
                scaffoldState.snackbarHostState
                    .showSnackbar(getString(fieldError))
            }
            return
        }
        startActivity(
            this.baseContext,
            ManageStockActivity
                .getManageStockActivityIntent(
                    this.baseContext,
                    viewModel.getData(),
                    intent.getParcelableExtra(INTENT_EXTRA_APP_CONFIG)
                ).apply {
                    this.addFlags(FLAG_ACTIVITY_NEW_TASK)
                },
            null
        )
    }

    companion object {
        @JvmStatic
        fun getHomeActivityIntent(context: Context, config: AppConfig): Intent? {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra(INTENT_EXTRA_APP_CONFIG, config)
            return intent
        }
    }
}
