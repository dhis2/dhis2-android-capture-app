package org.dhis2.android.rtsm.ui.home.screens.components

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.LocalThemeColor
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Backdrop(
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current

    val backdropState = rememberBackdropScaffoldState(BackdropValue.Revealed)
    var isFrontLayerDisabled by remember { mutableStateOf<Boolean?>(null) }
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = manageStockViewModel.bottomSheetState.collectAsState()
    if (bottomSheetState.value) {
        viewModel.onOpenManageStockBottomSheet()
    }

    BackHandler {
        manageStockViewModel.onHandleBackNavigation()
    }

    manageStockViewModel.backToListing()
    DisplaySnackBar(manageStockViewModel, scaffoldState)
    BackdropScaffold(
        modifier = modifier,
        appBar = {
            Toolbar(
                settingsUiState.selectedTransactionItem.label,
                settingsUiState.fromFacilitiesLabel().asString(),
                settingsUiState.deliverToLabel()?.asString(),
                LocalThemeColor.current,
                launchBottomSheet = {
                    manageStockViewModel.onHandleBackNavigation()
                },
                backdropState,
                scaffoldState,
                syncAction,
                settingsUiState.hasFacilitySelected(),
                settingsUiState.hasDestinationSelected(),
            )
        },
        backLayerBackgroundColor = LocalThemeColor.current,
        backLayerContent = {
            FilterList(
                viewModel = viewModel,
                dataEntryUiState = dataEntryUiState,
                launchDialog = { msg, result ->
                    launchBottomSheet(
                        context.getString(R.string.not_saved),
                        context.getString(msg),
                        onKeepEdition = {
                            result.invoke(EditionDialogResult.KEEP)
                        },
                        onDiscard = {
                            manageStockViewModel.cleanItemsFromCache()
                            result.invoke(EditionDialogResult.DISCARD)
                            manageStockViewModel.onHandleBackNavigation()
                        },
                    )
                },
                onTransitionSelected = {
                    viewModel.selectTransaction(it)
                },
            ) {
                viewModel.setDestination(it)
            }
        },
        frontLayerElevation = 5.dp,
        frontLayerContent = {
            MainContent(
                backdropState,
                isFrontLayerDisabled,
                viewModel,
                manageStockViewModel,
            )
        },
        scaffoldState = backdropState,
        gesturesEnabled = false,
        frontLayerBackgroundColor = Color.White,
        frontLayerScrimColor = getScrimColor(settingsUiState),
    )
    isFrontLayerDisabled = getBackdropState(settingsUiState)
    if (dataEntryUiState.step == DataEntryStep.COMPLETED) {
        scope.launch {
            backdropState.reveal()
        }
        manageStockViewModel.updateStep(DataEntryStep.START)
        viewModel.resetSettings()
    }
}

@Composable
private fun getScrimColor(settingsUiState: SettingsUiState): Color =
    if (settingsUiState.selectedTransactionItem.type == TransactionType.DISTRIBUTION) {
        if (settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected()) {
            Color.Unspecified
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
        }
    } else {
        if (!settingsUiState.hasFacilitySelected()) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
        } else {
            Color.Unspecified
        }
    }

@Composable
private fun getBackdropState(settingsUiState: SettingsUiState): Boolean =
    if (
        settingsUiState.selectedTransactionItem.type == TransactionType.DISTRIBUTION
    ) {
        !(settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected())
    } else {
        !settingsUiState.hasFacilitySelected()
    }

@Composable
fun DisplaySnackBar(
    manageStockViewModel: ManageStockViewModel,
    scaffoldState: ScaffoldState,
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        manageStockViewModel.dataEntryUiState.collectLatest {
            if (it.step == DataEntryStep.COMPLETED) {
                coroutineScope.launch {
                    val result =
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = "Snackbar # ",
                            actionLabel = "Action on ",
                            duration = SnackbarDuration.Short,
                        )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            // action has been performed
                        }
                        SnackbarResult.Dismissed -> {
                            // dismissed, no action needed
                        }
                    }
                }
            }
        }
    }
}
