package org.dhis2.usescases.datasets.dataSetTable

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.mobile.aggregates.di.mappers.toDataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.DataSetInstanceScreen
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class DataSetInstanceActivity : ActivityGlobalAbstract() {

    override var handleEdgeToEdge = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            DHIS2Theme {
                val useTwoPane = when (calculateWindowSizeClass(this).widthSizeClass) {
                    WindowWidthSizeClass.Medium -> false
                    WindowWidthSizeClass.Compact -> false
                    WindowWidthSizeClass.Expanded -> true
                    else -> false
                }
                val dataSetParams = intent.toDataSetInstanceParameters()
                val snackbarHostState = remember { SnackbarHostState() }
                DataSetInstanceScreen(
                    parameters = intent.toDataSetInstanceParameters(),
                    useTwoPane = useTwoPane,
                    onBackClicked = onBackPressedDispatcher::onBackPressed,
                    snackbarHostState = snackbarHostState,
                    onSyncClicked = { onUpdateData ->
                        showGranularSync(
                            dataSetParams = dataSetParams,
                            snackbarHostState = snackbarHostState,
                            onUpdateData = onUpdateData,
                        )
                    },
                    activity = activity,
                )
            }
            supportFragmentManager
        }
    }

    private fun showGranularSync(dataSetParams: DataSetInstanceParameters, snackbarHostState: SnackbarHostState, onUpdateData: () -> Unit) {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(
                SyncContext.DataSetInstance(
                    dataSetParams.dataSetUid,
                    dataSetParams.periodId,
                    dataSetParams.organisationUnitUid,
                    dataSetParams.attributeOptionComboUid,
                ),
            )
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) onUpdateData()
                }
            })
            .onNoConnectionListener {
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar(
                        message = getString(R.string.sync_offline_check_connection),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
            .show(DATA_VALUE_SYNC)
    }

    companion object {
        private const val DATA_VALUE_SYNC = "DATA_VALUE_SYNC"
    }
}
