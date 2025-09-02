package org.dhis2.usescases.datasets.dataSetTable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import org.dhis2.mobile.aggregates.ui.UiActionHandlerImpl
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_DATA_SET_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ORGANISATION_UNIT_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_PERIOD_ID
import org.dhis2.mobile.aggregates.ui.constants.OPEN_ERROR_LOCATION
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class DataSetInstanceActivity : ActivityGlobalAbstract() {
    override var handleEdgeToEdge = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uiActionHandler =
            UiActionHandlerImpl(
                context = this,
                dataSetUid = intent.getStringExtra(INTENT_EXTRA_DATA_SET_UID) ?: "",
                fileHandler = FileHandlerImpl(),
            )

        setContent {
            DHIS2Theme {
                val useTwoPane =
                    when (calculateWindowSizeClass(this).widthSizeClass) {
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
                    uiActionHandler = uiActionHandler,
                )
            }
            supportFragmentManager
        }
    }

    private fun showGranularSync(
        dataSetParams: DataSetInstanceParameters,
        snackbarHostState: SnackbarHostState,
        onUpdateData: () -> Unit,
    ) {
        SyncStatusDialog
            .Builder()
            .withContext(this)
            .withSyncContext(
                SyncContext.DataSetInstance(
                    dataSetParams.dataSetUid,
                    dataSetParams.periodId,
                    dataSetParams.organisationUnitUid,
                    dataSetParams.attributeOptionComboUid,
                ),
            ).onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) onUpdateData()
                    }
                },
            ).onNoConnectionListener {
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar(
                        message = getString(R.string.sync_offline_check_connection),
                        duration = SnackbarDuration.Short,
                    )
                }
            }.show(DATA_VALUE_SYNC)
    }

    companion object {
        private const val DATA_VALUE_SYNC = "DATA_VALUE_SYNC"

        @JvmStatic
        fun getBundle(
            dataSetUid: String,
            orgUnitUid: String,
            periodId: String,
            catOptCombo: String,
            openErrorLocation: Boolean,
        ): Bundle {
            val bundle = Bundle()
            bundle.putString(INTENT_EXTRA_DATA_SET_UID, dataSetUid)
            bundle.putString(INTENT_EXTRA_ORGANISATION_UNIT_UID, orgUnitUid)
            bundle.putString(INTENT_EXTRA_PERIOD_ID, periodId)
            bundle.putString(INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID, catOptCombo)
            bundle.putString(INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID, catOptCombo)
            bundle.putBoolean(OPEN_ERROR_LOCATION, openErrorLocation)

            return bundle
        }

        fun intent(
            context: Context,
            dataSetUid: String,
            orgUnitUid: String,
            periodId: String,
            catOptCombo: String,
            openErrorLocation: Boolean,
        ): Intent {
            val intent = Intent(context, DataSetInstanceActivity::class.java)
            intent.putExtras(getBundle(dataSetUid, orgUnitUid, periodId, catOptCombo, openErrorLocation))
            return intent
        }
    }
}
