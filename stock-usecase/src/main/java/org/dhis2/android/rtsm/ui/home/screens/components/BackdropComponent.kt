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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
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
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Backdrop(
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    modifier: Modifier = Modifier,
    supportFragmentManager: FragmentManager,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    onFinish: () -> Unit,
) {
    val context = LocalContext.current

    val backdropState = rememberBackdropScaffoldState(BackdropValue.Revealed)
    var isFrontLayerDisabled by remember { mutableStateOf<Boolean?>(null) }
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = manageStockViewModel.bottomSheetState.collectAsState()
    if (bottomSheetState.value) {
        launchBottomSheet(
            stringResource(R.string.not_saved),
            stringResource(R.string.transaction_not_confirmed),
            supportFragmentManager,
            onKeepEdition = {
                manageStockViewModel.onBottomSheetClosed()
            },
            onDiscard = {
                manageStockViewModel.onBottomSheetClosed()
                onFinish()
            },
        )
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
                viewModel,
                dataEntryUiState,
                supportFragmentManager,
                launchDialog = { msg, result ->
                    launchBottomSheet(
                        context.getString(R.string.not_saved),
                        context.getString(msg),
                        supportFragmentManager,
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
                onFacilitySelected = {
                    viewModel.setFacility(it)
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

private fun launchBottomSheet(
    title: String,
    subtitle: String,
    supportFragmentManager: FragmentManager,
    onDiscard: () -> Unit, // Perform the transaction change and clear data
    onKeepEdition: () -> Unit, // Leave it as it was
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel =
            BottomSheetDialogUiModel(
                title = title,
                message = subtitle,
                iconResource = R.drawable.ic_outline_error_36,
                mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.keep_editing),
                secondaryButton = DialogButtonStyle.DiscardButton(),
            ),
        onMainButtonClicked = {
            supportFragmentManager.popBackStack()
            onKeepEdition.invoke()
        },
        onSecondaryButtonClicked = { onDiscard.invoke() },
        showTopDivider = true,
    ).apply {
        this.show(supportFragmentManager.beginTransaction(), "DIALOG")
        this.isCancelable = false
    }
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
