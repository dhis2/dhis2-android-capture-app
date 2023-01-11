package org.dhis2.android.rtsm.ui.home.screens.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Backdrop(
    activity: Activity,
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> }
) {
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Concealed)
    var isFrontLayerDisabled by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    val settingsUiState by viewModel.settingsUiState.collectAsState()

    BackdropScaffold(
        appBar = {
            Toolbar(
                settingsUiState.transactionType.name,
                settingsUiState.fromFacilitiesLabel().asString(),
                settingsUiState.deliverToLabel()?.asString(),
                themeColor,
                navigationAction = {
                    activity.finish()
                },
                backdropState,
                scaffoldState,
                syncAction,
                settingsUiState.hasFacilitySelected(),
                settingsUiState.hasDestinationSelected()
            )
        },
        backLayerBackgroundColor = themeColor,
        backLayerContent = {
            val height = filterList(
                viewModel,
                themeColor,
                supportFragmentManager
            )
            if (height > 160.dp) {
                scope.launch { backdropState.reveal() }
            }
        },
        frontLayerElevation = 5.dp,
        frontLayerContent = {
            MainContent(
                backdropState,
                isFrontLayerDisabled,
                themeColor,
                viewModel,
                manageStockViewModel,
                barcodeLauncher
            )
        },
        scaffoldState = backdropState,
        gesturesEnabled = false,
        frontLayerScrimColor = if (
            settingsUiState.transactionType == TransactionType.DISTRIBUTION
        ) {
            if (settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected()) {
                isFrontLayerDisabled = false
                Color.Unspecified
            } else {
                isFrontLayerDisabled = true
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            }
        } else {
            if (!settingsUiState.hasFacilitySelected()) {
                isFrontLayerDisabled = true
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            } else {
                isFrontLayerDisabled = false
                Color.Unspecified
            }
        }
    )
}
